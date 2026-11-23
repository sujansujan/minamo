package com.example.koipond

import android.app.WallpaperColors
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.Choreographer
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Battery-Hardened Multi-Instance ASCII Split-View Live Wallpaper Service (`minamo`).
 * - VSYNC-aligned rendering via thread-local `Choreographer` with deep CPU sleep between calm frames.
 * - Adaptive FPS: drops to calm 20fps when serene, boosts to 30/60fps on touch/predator/feeding,
 *   and drops to 15fps + disables sensors under Battery Saver, low battery (<15%), or thermal warmth.
 * - Low-power `TYPE_GAME_ROTATION_VECTOR` (no magnetometer draw, `SENSOR_DELAY_UI` + hardware batching)
 *   with adaptive neutral baseline so holding your phone tilted maintains a true 3D window view.
 * - Native `WallpaperColors` reporting (API 27+ / API 31+ `HINT_SUPPORTS_DARK_TEXT`) so Android's
 *   launcher & lock screen automatically switch text/clock contrast between Solar Light and Dark modes.
 */
class KoiPondWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = AsciiPondEngine(ENGINE_ID_GEN.incrementAndGet())

    inner class AsciiPondEngine(private val engineId: Int) : Engine(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        SensorEventListener {

        private val surfaceLock = Any()

        private lateinit var settingsRepo: WallpaperSettingsRepository
        @Volatile
        private var currentConfig = WallpaperConfig()

        private var powerManager: PowerManager? = null
        private var batteryManager: BatteryManager? = null
        @Volatile
        private var isSystemBatterySaver = false
        @Volatile
        private var isThermalWarm = false
        @Volatile
        private var isDisplayInteractive = true
        @Volatile
        private var isDreaming = false
        @Volatile
        private var currentBatteryPct = 85

        private var sensorManager: SensorManager? = null
        private var rotationSensor: Sensor? = null
        private var gyroFallbackSensor: Sensor? = null
        private var isSensorRegistered = false
        private val rotationMatrix = FloatArray(9)
        private val orientationAngles = FloatArray(3)
        private var hasNeutralBaseline = false
        private var neutralPitch = 0f
        private var neutralRoll = 0f

        @Volatile
        private var scrollOffsetX = 0.5f
        @Volatile
        private var tiltX = 0f
        @Volatile
        private var tiltY = 0f

        private var renderThread: HandlerThread? = null
        private var renderHandler: Handler? = null
        private var renderChoreographer: Choreographer? = null

        private val simulation = PondSimulation()
        private var surfaceWidth = 1080
        private var surfaceHeight = 2400

        @Volatile
        private var isWallpaperVisible = false
        @Volatile
        private var surfaceReady = false
        @Volatile
        private var isDestroyed = false
        private var lastFrameTimestampNs = 0L
        private var lastInteractionMs = 0L
        private var lastUnlockShuffleMs = 0L
        private var lastQuickTileSeq = 0
        private var touchDownTimeMs = 0L
        private var touchDownX = 0f
        private var touchDownY = 0f
        private var lastDragX = 0f
        private var lastDragEventMs = 0L
        private var hasDraggedFar = false
        private var longPressTriggered = false

        // WallpaperColors change throttling (only notifies SystemUI on polarity flip or >=20m sky shift)
        private var lastReportedLightSurface: Boolean? = null
        private var lastReportedSkyBg: Int = 0
        private var lastColorNotifyMs: Long = 0L

        // In-memory telemetry batching (flushed on visibility pause or every 30s — zero per-frame disk I/O)
        private var pendingCatches = 0
        private var pendingEscapes = 0
        private var pendingCrowns = 0
        private var pendingLastEvent: String? = null
        private var lastTelemetryFlushMs = 0L

        private val systemEventReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (isDestroyed) return
                when (intent?.action) {
                    PowerManager.ACTION_POWER_SAVE_MODE_CHANGED,
                    Intent.ACTION_BATTERY_LOW,
                    Intent.ACTION_BATTERY_OKAY -> {
                        updatePowerStates()
                        updateSensorRegistration()
                    }
                    Intent.ACTION_SCREEN_OFF,
                    Intent.ACTION_DREAMING_STARTED -> {
                        if (intent.action == Intent.ACTION_SCREEN_OFF) isDisplayInteractive = false
                        if (intent.action == Intent.ACTION_DREAMING_STARTED) isDreaming = true
                        pauseRenderingImmediately()
                        flushTelemetryIfNeeded(force = true)
                    }
                    Intent.ACTION_DREAMING_STOPPED -> {
                        isDreaming = false
                        updatePowerStates()
                        if (shouldRenderNow()) {
                            updateSensorRegistration()
                            scheduleNextFrame(0L)
                        }
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        isDisplayInteractive = true
                        isDreaming = false
                        updatePowerStates()
                        updateSensorRegistration()
                        if (shouldRenderNow()) {
                            renderHandler?.post {
                                lastFrameTimestampNs = SystemClock.elapsedRealtimeNanos()
                                scheduleNextFrame(0L)
                            }
                        }
                    }
                    Intent.ACTION_USER_PRESENT -> {
                        // Shuffle only on actual unlock (with 30s debounce) and preserve user's Elder Crown fish!
                        isDisplayInteractive = true
                        isDreaming = false
                        updatePowerStates()
                        updateSensorRegistration()
                        val nowMs = SystemClock.elapsedRealtime()
                        if (!isPreview && nowMs - lastUnlockShuffleMs > 30_000L) {
                            lastUnlockShuffleMs = nowMs
                            renderHandler?.post {
                                simulation.randomizeEcosystemOnUnlock(
                                    seed = System.nanoTime(),
                                    preserveElderCrown = true
                                )
                                lastFrameTimestampNs = SystemClock.elapsedRealtimeNanos()
                                scheduleNextFrame(0L)
                            }
                        } else if (shouldRenderNow()) {
                            scheduleNextFrame(0L)
                        }
                    }
                }
            }
        }
        private var isReceiverRegistered = false

        private val vsyncArmRunnable = Runnable {
            if (!shouldRenderNow()) {
                return@Runnable
            }
            val ch = renderChoreographer
            if (ch != null) {
                ch.postFrameCallback(frameCallback)
            } else {
                drawOneFrame(SystemClock.elapsedRealtimeNanos())
            }
        }

        private val frameCallback = Choreographer.FrameCallback { frameTimeNanos ->
            drawOneFrame(frameTimeNanos)
        }

        private fun isSystemNightMode(): Boolean {
            val uiMask = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return uiMask == Configuration.UI_MODE_NIGHT_YES
        }

        private fun isEcoActive(cfg: WallpaperConfig): Boolean {
            return when (cfg.powerSaveMode) {
                PowerSaveMode.MAX_ECO -> true
                PowerSaveMode.AUTO_SAVER -> isSystemBatterySaver || currentBatteryPct < 15 || isThermalWarm
                PowerSaveMode.OFF -> isThermalWarm
            }
        }

        private fun drawOneFrame(frameTimeNanos: Long) {
            if (!shouldRenderNow()) return

            val dt = if (lastFrameTimestampNs == 0L) {
                1f / 30f
            } else {
                ((frameTimeNanos - lastFrameTimestampNs) / 1_000_000_000.0f).coerceIn(0.001f, 0.05f)
            }
            lastFrameTimestampNs = frameTimeNanos

            val cfg = currentConfig
            val effectivePowerSaver = isEcoActive(cfg)
            val systemDark = isSystemNightMode()

            // Synchronize canvas lock/unlock with surfaceLock so onSurfaceDestroyed never races mid-draw
            synchronized(surfaceLock) {
                if (!surfaceReady || isDestroyed) return
                val holder = surfaceHolder
                val canvas = try {
                    holder.lockHardwareCanvas()
                } catch (_: Throwable) {
                    try {
                        holder.lockCanvas()
                    } catch (_: Throwable) {
                        null
                    }
                }

                if (canvas != null) {
                    try {
                        val px = (scrollOffsetX - 0.5f) * 1.2f + tiltX * 0.65f
                        val py = tiltY * 0.65f
                        simulation.updateAndRender(
                            canvas = canvas,
                            dt = dt,
                            config = cfg,
                            parallaxX = px,
                            parallaxY = py,
                            isBatterySaver = effectivePowerSaver,
                            isWallpaperMode = true,
                            batteryPercent = currentBatteryPct,
                            isSystemDark = systemDark
                        )
                    } finally {
                        try {
                            holder.unlockCanvasAndPost(canvas)
                        } catch (_: Throwable) {}
                    }
                }
            }

            maybeNotifyWallpaperColors()
            flushTelemetryIfNeeded(force = false)

            if (shouldRenderNow()) {
                val nowMs = SystemClock.elapsedRealtime()
                val recentlyInteracted = (nowMs - lastInteractionMs) < 4_000L
                val effectiveFps = when {
                    effectivePowerSaver -> 15
                    cfg.targetFps >= 60 -> if (simulation.hasHighMotionActivity || recentlyInteracted) 60 else 30
                    simulation.hasHighMotionActivity || recentlyInteracted -> 30
                    else -> 20 // Serene calm cruising: 20fps saves ~33% GPU/CPU power cleanly
                }
                val targetIntervalMs = 1000L / effectiveFps
                val workDurationMs = (SystemClock.elapsedRealtimeNanos() - frameTimeNanos) / 1_000_000L
                // For 60fps, hook directly onto next VSYNC; for <=30fps, let CPU sleep until ~4ms before next VSYNC!
                val sleepDelayMs = if (effectiveFps >= 60) {
                    0L
                } else {
                    (targetIntervalMs - workDurationMs - 4L).coerceAtLeast(4L)
                }
                scheduleNextFrame(sleepDelayMs)
            }
        }

        private fun maybeNotifyWallpaperColors() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return
            val snap = simulation.lastResolvedSnapshot ?: return
            val nowMs = SystemClock.elapsedRealtime()
            val polarityChanged = (lastReportedLightSurface != snap.isLightSurface)
            val skyShifted = (lastReportedSkyBg != snap.colors.skyBg && (nowMs - lastColorNotifyMs) >= 1_200_000L)
            if (polarityChanged || skyShifted) {
                lastReportedLightSurface = snap.isLightSurface
                lastReportedSkyBg = snap.colors.skyBg
                lastColorNotifyMs = nowMs
                try {
                    notifyColorsChanged()
                } catch (_: Throwable) {}
            }
        }

        @RequiresApi(Build.VERSION_CODES.O_MR1)
        override fun onComputeColors(): WallpaperColors {
            val cfg = currentConfig
            val snap = simulation.lastResolvedSnapshot ?: TimeOfDayPalette.resolve(
                mode = cfg.timeCycleMode,
                family = cfg.paletteFamily,
                themeMode = cfg.themeMode,
                isSystemDark = isSystemNightMode()
            )
            val c = snap.colors
            val primary = Color.valueOf(c.skyBg)
            val secondary = Color.valueOf(c.meniscusWaveFg)
            val tertiary = Color.valueOf(c.fishCoral)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val hints = if (snap.isLightSurface) WallpaperColors.HINT_SUPPORTS_DARK_TEXT else 0
                WallpaperColors(primary, secondary, tertiary, hints)
            } else {
                WallpaperColors(primary, secondary, tertiary)
            }
        }

        private fun shouldRenderNow(): Boolean {
            if (isDestroyed || !surfaceReady || !isWallpaperVisible) return false
            if (currentConfig.pauseOnBackgroundAndScreenOff && (!isDisplayInteractive || isDreaming)) return false
            return true
        }

        private fun pauseRenderingImmediately() {
            unregisterSensor()
            renderHandler?.removeCallbacks(vsyncArmRunnable)
            renderHandler?.post {
                renderChoreographer?.removeFrameCallback(frameCallback)
            }
        }

        private fun flushTelemetryIfNeeded(force: Boolean) {
            if (pendingCatches == 0 && pendingEscapes == 0 && pendingCrowns == 0 && pendingLastEvent == null) {
                return
            }
            val nowMs = SystemClock.elapsedRealtime()
            if (!force && nowMs - lastTelemetryFlushMs < 30_000L) {
                return
            }
            lastTelemetryFlushMs = nowMs
            val c = pendingCatches
            val e = pendingEscapes
            val cr = pendingCrowns
            val ev = pendingLastEvent
            pendingCatches = 0
            pendingEscapes = 0
            pendingCrowns = 0
            pendingLastEvent = null
            settingsRepo.flushTelemetryBatch(c, e, cr, ev)
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)

            simulation.loadBundledFont(this@KoiPondWallpaperService)
            settingsRepo = WallpaperSettingsRepository(this@KoiPondWallpaperService)
            currentConfig = settingsRepo.loadConfig()
            lastQuickTileSeq = settingsRepo.getQuickTileCommandSeq()
            lastTelemetryFlushMs = SystemClock.elapsedRealtime()
            lastInteractionMs = SystemClock.elapsedRealtime()

            simulation.onHuntOutcomeListener = { caught, predName, preyName ->
                if (caught) {
                    pendingCatches++
                    pendingLastEvent = "! " + predName + " caught " + preyName
                } else {
                    pendingEscapes++
                    pendingLastEvent = "ok: school evaded " + predName
                }
            }
            simulation.onCrownLevelUpListener = { speciesName ->
                pendingCrowns++
                pendingLastEvent = "* " + speciesName + " grew into elder crown!"
            }
            simulation.onLegendarySightingListener = { legName ->
                settingsRepo.recordLegendarySighting(legName)
            }
            settingsRepo.registerListener(this)

            powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            batteryManager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            updatePowerStates()

            try {
                val filter = IntentFilter().apply {
                    addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
                    addAction(Intent.ACTION_BATTERY_LOW)
                    addAction(Intent.ACTION_BATTERY_OKAY)
                    addAction(Intent.ACTION_SCREEN_OFF)
                    addAction(Intent.ACTION_SCREEN_ON)
                    addAction(Intent.ACTION_DREAMING_STARTED)
                    addAction(Intent.ACTION_DREAMING_STOPPED)
                    addAction(Intent.ACTION_USER_PRESENT)
                }
                ContextCompat.registerReceiver(
                    this@KoiPondWallpaperService,
                    systemEventReceiver,
                    filter,
                    ContextCompat.RECEIVER_EXPORTED
                )
                isReceiverRegistered = true
            } catch (e: Exception) {
                Log.w("AsciiPondEngine", "Could not register system receiver", e)
            }

            sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            gyroFallbackSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

            val thread = HandlerThread("MinamoAsciiThread-" + engineId.toString())
            thread.start()
            renderThread = thread
            val handler = Handler(thread.looper)
            renderHandler = handler
            handler.post {
                renderChoreographer = Choreographer.getInstance()
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            renderHandler?.post {
                if (isDestroyed) return@post
                synchronized(surfaceLock) {
                    val frame = holder.surfaceFrame
                    surfaceWidth = frame.width().coerceAtLeast(320)
                    surfaceHeight = frame.height().coerceAtLeast(480)
                    simulation.configureGrid(
                        surfaceWidth,
                        surfaceHeight,
                        currentConfig.gridDensity,
                        currentConfig.waterlinePercent
                    )
                    surfaceReady = true
                }
                lastFrameTimestampNs = 0L
                scheduleNextFrame(0L)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            renderHandler?.post {
                if (isDestroyed) return@post
                synchronized(surfaceLock) {
                    surfaceWidth = width.coerceAtLeast(320)
                    surfaceHeight = height.coerceAtLeast(480)
                    simulation.configureGrid(
                        surfaceWidth,
                        surfaceHeight,
                        currentConfig.gridDensity,
                        currentConfig.waterlinePercent
                    )
                }
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            pauseRenderingImmediately()
            synchronized(surfaceLock) {
                surfaceReady = false
            }
            val latch = CountDownLatch(1)
            val posted = renderHandler?.post {
                renderChoreographer?.removeFrameCallback(frameCallback)
                flushTelemetryIfNeeded(force = true)
                latch.countDown()
            } ?: false
            if (posted) {
                try {
                    latch.await(120L, TimeUnit.MILLISECONDS)
                } catch (_: InterruptedException) {}
            }
            super.onSurfaceDestroyed(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isWallpaperVisible = visible
            updatePowerStates()
            if (visible && isDisplayInteractive && !isDreaming) {
                lastInteractionMs = SystemClock.elapsedRealtime()
                updateSensorRegistration()
                renderHandler?.post {
                    lastFrameTimestampNs = SystemClock.elapsedRealtimeNanos()
                    scheduleNextFrame(0L)
                }
            } else {
                pauseRenderingImmediately()
                renderHandler?.post {
                    flushTelemetryIfNeeded(force = true)
                }
            }
        }

        override fun onOffsetsChanged(
            xOffset: Float,
            yOffset: Float,
            xOffsetStep: Float,
            yOffsetStep: Float,
            xPixelOffset: Int,
            yPixelOffset: Int
        ) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset)
            if (kotlin.math.abs(scrollOffsetX - xOffset) > 0.004f) {
                lastInteractionMs = SystemClock.elapsedRealtime()
            }
            scrollOffsetX = xOffset
        }

        override fun onTouchEvent(event: MotionEvent) {
            lastInteractionMs = SystemClock.elapsedRealtime()
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    touchDownTimeMs = event.downTime
                    touchDownX = event.x
                    touchDownY = event.y
                    lastDragX = event.x
                    hasDraggedFar = false
                    longPressTriggered = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val moveDist = kotlin.math.hypot(event.x - touchDownX, event.y - touchDownY)
                    if (moveDist > 28f) {
                        hasDraggedFar = true
                    }
                    if (!hasDraggedFar && !longPressTriggered && event.eventTime - touchDownTimeMs > 480L) {
                        longPressTriggered = true
                        val tx = event.x
                        val ty = event.y
                        renderHandler?.post {
                            simulation.onLongPress(tx, ty)
                        }
                    } else if (hasDraggedFar && event.eventTime - lastDragEventMs >= 28L) {
                        lastDragEventMs = event.eventTime
                        val tx = event.x
                        val ty = event.y
                        val dx = tx - lastDragX
                        lastDragX = tx
                        renderHandler?.post {
                            simulation.onDrag(tx, ty, dx)
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (!longPressTriggered && !hasDraggedFar && event.eventTime - touchDownTimeMs <= 480L) {
                        val tx = event.x
                        val ty = event.y
                        renderHandler?.post {
                            simulation.onTouch(tx, ty)
                        }
                    }
                }
            }
            super.onTouchEvent(event)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (isDestroyed) return
            val newCfg = settingsRepo.loadConfig()
            currentConfig = newCfg
            TimeOfDayPalette.invalidateCache()
            val tileSeq = settingsRepo.getQuickTileCommandSeq()
            renderHandler?.post {
                if (isDestroyed) return@post
                synchronized(surfaceLock) {
                    simulation.configureGrid(
                        surfaceWidth,
                        surfaceHeight,
                        newCfg.gridDensity,
                        newCfg.waterlinePercent
                    )
                }
                if (tileSeq != lastQuickTileSeq) {
                    lastQuickTileSeq = tileSeq
                    lastInteractionMs = SystemClock.elapsedRealtime()
                    when (tileSeq % 3) {
                        1 -> {
                            simulation.randomizeEcosystemOnUnlock(System.nanoTime(), preserveElderCrown = false)
                            simulation.onLongPress(surfaceWidth * 0.5f, surfaceHeight * 0.6f)
                        }
                        2 -> {
                            val isNightNow = simulation.lastResolvedSnapshot?.isNight == true
                            simulation.triggerOceanVisitor(System.nanoTime(), isNightNow)
                        }
                        else -> {
                            simulation.triggerPredatorVisit(System.nanoTime(), 0.70f)
                        }
                    }
                }
                if (shouldRenderNow()) {
                    scheduleNextFrame(0L)
                }
            }
            updateSensorRegistration()
        }

        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_GAME_ROTATION_VECTOR -> {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    val pitch = orientationAngles[1]
                    val roll = orientationAngles[2]
                    if (!hasNeutralBaseline) {
                        neutralPitch = pitch
                        neutralRoll = roll
                        hasNeutralBaseline = true
                    } else {
                        // Slowly adapt neutral holding angle so any posture feels centered
                        neutralPitch += (pitch - neutralPitch) * 0.012f
                        neutralRoll += (roll - neutralRoll) * 0.012f
                    }
                    val targetX = ((roll - neutralRoll) * 2.1f).coerceIn(-1.25f, 1.25f)
                    val targetY = ((pitch - neutralPitch) * 2.1f).coerceIn(-1.25f, 1.25f)
                    tiltX += (targetX - tiltX) * 0.22f
                    tiltY += (targetY - tiltY) * 0.22f
                }
                Sensor.TYPE_GYROSCOPE -> {
                    tiltX = (tiltX * 0.92f + event.values[1] * 0.16f).coerceIn(-1.25f, 1.25f)
                    tiltY = (tiltY * 0.92f + event.values[0] * 0.16f).coerceIn(-1.25f, 1.25f)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

        override fun onDestroy() {
            isDestroyed = true
            pauseRenderingImmediately()
            synchronized(surfaceLock) {
                surfaceReady = false
            }
            if (isReceiverRegistered) {
                try {
                    unregisterReceiver(systemEventReceiver)
                } catch (_: Exception) {}
                isReceiverRegistered = false
            }
            settingsRepo.unregisterListener(this)
            flushTelemetryIfNeeded(force = true)
            renderHandler?.removeCallbacksAndMessages(null)
            renderThread?.quitSafely()
            renderThread = null
            renderHandler = null
            renderChoreographer = null
            super.onDestroy()
        }

        private fun scheduleNextFrame(delayMs: Long) {
            val handler = renderHandler ?: return
            if (!shouldRenderNow()) return
            handler.removeCallbacks(vsyncArmRunnable)
            if (delayMs <= 0L) {
                handler.post(vsyncArmRunnable)
            } else {
                handler.postDelayed(vsyncArmRunnable, delayMs)
            }
        }

        private fun updatePowerStates() {
            val pm = powerManager
            isSystemBatterySaver = pm?.isPowerSaveMode == true
            isDisplayInteractive = pm?.isInteractive != false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && pm != null) {
                isThermalWarm = pm.currentThermalStatus >= PowerManager.THERMAL_STATUS_MODERATE
            } else {
                isThermalWarm = false
            }
            val pct = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85
            if (pct in 1..100) {
                currentBatteryPct = pct
            }
        }

        private fun updateSensorRegistration() {
            val shouldUseSensor = shouldRenderNow() && currentConfig.enableGyro && !isEcoActive(currentConfig)
            val chosenSensor = rotationSensor ?: gyroFallbackSensor
            if (shouldUseSensor && !isSensorRegistered && chosenSensor != null) {
                hasNeutralBaseline = false
                // SENSOR_DELAY_UI (~16Hz) + 40ms hardware FIFO batching minimizes IMU battery draw!
                sensorManager?.registerListener(this, chosenSensor, SensorManager.SENSOR_DELAY_UI, 40_000)
                isSensorRegistered = true
            } else if (!shouldUseSensor && isSensorRegistered) {
                unregisterSensor()
            }
        }

        private fun unregisterSensor() {
            if (isSensorRegistered) {
                sensorManager?.unregisterListener(this)
                isSensorRegistered = false
                hasNeutralBaseline = false
                tiltX = 0f
                tiltY = 0f
            }
        }
    }

    companion object {
        private val ENGINE_ID_GEN = AtomicInteger(0)
    }
}

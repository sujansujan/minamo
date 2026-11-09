package com.example.koipond

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Hardened Multi-Instance ASCII Split-View Live Wallpaper Service (`minamo`).
 * - Each Engine instance (active wallpaper vs. concurrent system preview) runs on an isolated
 *   uniquely named render thread (`MinamoAsciiThread-<id>`).
 * - Synchronizes `onSurfaceDestroyed` with the render thread via `surfaceLock` and a bounded
 *   `CountDownLatch` so `unlockCanvasAndPost` never races with native Surface destruction.
 * - Batches telemetry writes in memory so zero `SharedPreferences` disk I/O occurs during active frames.
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
        private var isDisplayInteractive = true
        @Volatile
        private var currentBatteryPct = 85

        private var sensorManager: SensorManager? = null
        private var gyroSensor: Sensor? = null
        private var isGyroRegistered = false

        @Volatile
        private var scrollOffsetX = 0.5f
        @Volatile
        private var gyroTiltX = 0f
        @Volatile
        private var gyroTiltY = 0f

        private var renderThread: HandlerThread? = null
        private var renderHandler: Handler? = null

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
        private var lastQuickTileSeq = 0
        private var touchDownTimeMs = 0L
        private var longPressTriggered = false

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
                    PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                        updatePowerStates()
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        isDisplayInteractive = false
                        pauseRenderingImmediately()
                        flushTelemetryIfNeeded(force = true)
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        isDisplayInteractive = true
                        updatePowerStates()
                        if (!isPreview) {
                            renderHandler?.post {
                                simulation.randomizeEcosystemOnUnlock(System.nanoTime())
                                lastFrameTimestampNs = SystemClock.elapsedRealtimeNanos()
                                scheduleNextFrame()
                            }
                        }
                    }
                    Intent.ACTION_USER_PRESENT -> {
                        isDisplayInteractive = true
                        updatePowerStates()
                        if (!isPreview) {
                            renderHandler?.post {
                                simulation.randomizeEcosystemOnUnlock(System.nanoTime())
                                scheduleNextFrame()
                            }
                        }
                    }
                }
            }
        }
        private var isReceiverRegistered = false

        private val drawRunnable = object : Runnable {
            override fun run() {
                if (!shouldRenderNow()) return

                val frameStartNs = SystemClock.elapsedRealtimeNanos()
                val dt = if (lastFrameTimestampNs == 0L) {
                    1f / 30f
                } else {
                    ((frameStartNs - lastFrameTimestampNs) / 1_000_000_000.0f).coerceIn(0.001f, 0.05f)
                }
                lastFrameTimestampNs = frameStartNs

                val cfg = currentConfig
                val effectivePowerSaver = when (cfg.powerSaveMode) {
                    PowerSaveMode.MAX_ECO -> true
                    PowerSaveMode.AUTO_SAVER -> isSystemBatterySaver
                    PowerSaveMode.OFF -> false
                }

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
                            val px = (scrollOffsetX - 0.5f) * 1.2f + gyroTiltX * 0.6f
                            val py = gyroTiltY * 0.6f
                            simulation.updateAndRender(
                                canvas = canvas,
                                dt = dt,
                                config = cfg,
                                parallaxX = px,
                                parallaxY = py,
                                isBatterySaver = effectivePowerSaver,
                                isWallpaperMode = true,
                                batteryPercent = currentBatteryPct
                            )
                        } finally {
                            try {
                                holder.unlockCanvasAndPost(canvas)
                            } catch (_: Throwable) {}
                        }
                    }
                }

                flushTelemetryIfNeeded(force = false)

                if (shouldRenderNow()) {
                    val targetFps = if (effectivePowerSaver) 15 else cfg.targetFps
                    val targetIntervalMs = 1000L / targetFps
                    val workDurationMs = (SystemClock.elapsedRealtimeNanos() - frameStartNs) / 1_000_000L
                    val delayMs = (targetIntervalMs - workDurationMs).coerceAtLeast(5L)
                    renderHandler?.postDelayed(this, delayMs)
                }
            }
        }

        private fun shouldRenderNow(): Boolean {
            if (isDestroyed || !surfaceReady || !isWallpaperVisible) return false
            if (currentConfig.pauseOnBackgroundAndScreenOff && !isDisplayInteractive) return false
            return true
        }

        private fun pauseRenderingImmediately() {
            unregisterGyro()
            renderHandler?.removeCallbacks(drawRunnable)
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
            settingsRepo.registerListener(this)

            powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            batteryManager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            updatePowerStates()

            try {
                val filter = IntentFilter().apply {
                    addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
                    addAction(Intent.ACTION_SCREEN_OFF)
                    addAction(Intent.ACTION_SCREEN_ON)
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
            gyroSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

            val thread = HandlerThread("MinamoAsciiThread-" + engineId.toString())
            thread.start()
            renderThread = thread
            renderHandler = Handler(thread.looper)
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
                scheduleNextFrame()
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
            // Wait up to 120ms for any queued render-thread work to drain before releasing the Surface
            val latch = CountDownLatch(1)
            val posted = renderHandler?.post {
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
            if (visible && isDisplayInteractive) {
                updateGyroRegistration()
                renderHandler?.post {
                    lastFrameTimestampNs = SystemClock.elapsedRealtimeNanos()
                    scheduleNextFrame()
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
            scrollOffsetX = xOffset
        }

        override fun onTouchEvent(event: MotionEvent) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    touchDownTimeMs = event.downTime
                    longPressTriggered = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!longPressTriggered && event.eventTime - touchDownTimeMs > 480L) {
                        longPressTriggered = true
                        val tx = event.x
                        val ty = event.y
                        renderHandler?.post {
                            simulation.onLongPress(tx, ty)
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (!longPressTriggered && event.eventTime - touchDownTimeMs <= 480L) {
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
                    if (tileSeq % 2 == 0) {
                        simulation.triggerPredatorVisit(System.nanoTime(), 0.70f)
                    } else {
                        simulation.randomizeEcosystemOnUnlock(System.nanoTime())
                        simulation.onLongPress(surfaceWidth * 0.5f, surfaceHeight * 0.6f)
                    }
                }
            }
            updateGyroRegistration()
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                gyroTiltX = (gyroTiltX * 0.92f + event.values[1] * 0.08f).coerceIn(-1f, 1f)
                gyroTiltY = (gyroTiltY * 0.92f + event.values[0] * 0.08f).coerceIn(-1f, 1f)
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
            super.onDestroy()
        }

        private fun scheduleNextFrame() {
            renderHandler?.removeCallbacks(drawRunnable)
            if (shouldRenderNow()) {
                renderHandler?.post(drawRunnable)
            }
        }

        private fun updatePowerStates() {
            isSystemBatterySaver = powerManager?.isPowerSaveMode == true
            isDisplayInteractive = powerManager?.isInteractive != false
            val pct = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85
            if (pct in 1..100) {
                currentBatteryPct = pct
            }
        }

        private fun updateGyroRegistration() {
            val effectiveEco = currentConfig.powerSaveMode == PowerSaveMode.MAX_ECO ||
                (currentConfig.powerSaveMode == PowerSaveMode.AUTO_SAVER && isSystemBatterySaver)
            val shouldUseGyro = shouldRenderNow() && currentConfig.enableGyro && !effectiveEco
            if (shouldUseGyro && !isGyroRegistered && gyroSensor != null) {
                sensorManager?.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME)
                isGyroRegistered = true
            } else if (!shouldUseGyro && isGyroRegistered) {
                unregisterGyro()
            }
        }

        private fun unregisterGyro() {
            if (isGyroRegistered) {
                sensorManager?.unregisterListener(this)
                isGyroRegistered = false
                gyroTiltX = 0f
                gyroTiltY = 0f
            }
        }
    }

    companion object {
        private val ENGINE_ID_GEN = AtomicInteger(0)
    }
}

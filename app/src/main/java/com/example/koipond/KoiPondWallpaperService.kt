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
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat

/**
 * Plain-Text ASCII Split-View Live Wallpaper Service (`minamo`).
 * - Renders the colorful Half-Over / Half-Underwater character grid at 30 or 60 FPS.
 * - Shifts colors dynamically based on the real-time clock (Dawn / Noon / Dusk / Night).
 * - Randomizes the aquatic fish species mix every time the phone is unlocked (`ACTION_USER_PRESENT`).
 */
class KoiPondWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = AsciiPondEngine()

    inner class AsciiPondEngine : Engine(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        SensorEventListener {

        private lateinit var settingsRepo: WallpaperSettingsRepository
        @Volatile
        private var currentConfig = WallpaperConfig()

        private var powerManager: PowerManager? = null
        @Volatile
        private var isBatterySaverActive = false

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
        private var isVisibleNow = false
        private var surfaceReady = false
        private var lastFrameTimestampNs = 0L

        private val systemEventReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                        updateBatterySaverState()
                    }
                    Intent.ACTION_USER_PRESENT, Intent.ACTION_SCREEN_ON -> {
                        renderHandler?.post {
                            simulation.randomizeEcosystemOnUnlock(System.nanoTime())
                        }
                    }
                }
            }
        }
        private var isReceiverRegistered = false

        private val drawRunnable = object : Runnable {
            override fun run() {
                if (!isVisibleNow || !surfaceReady) return

                val frameStartNs = SystemClock.elapsedRealtimeNanos()
                val dt = if (lastFrameTimestampNs == 0L) {
                    1f / 30f
                } else {
                    ((frameStartNs - lastFrameTimestampNs) / 1_000_000_000.0f).coerceIn(0.001f, 0.08f)
                }
                lastFrameTimestampNs = frameStartNs

                val cfg = currentConfig
                val batterySaver = isBatterySaverActive
                val holder = surfaceHolder

                var canvas = try {
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
                            isBatterySaver = batterySaver,
                            isWallpaperMode = true
                        )
                    } finally {
                        try {
                            holder.unlockCanvasAndPost(canvas)
                        } catch (_: Throwable) {}
                    }
                }

                val targetFps = if (batterySaver) 20 else cfg.targetFps
                val targetIntervalMs = 1000L / targetFps
                val workDurationMs = (SystemClock.elapsedRealtimeNanos() - frameStartNs) / 1_000_000L
                val delayMs = (targetIntervalMs - workDurationMs).coerceAtLeast(4L)

                renderHandler?.postDelayed(this, delayMs)
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)

            settingsRepo = WallpaperSettingsRepository(this@KoiPondWallpaperService)
            currentConfig = settingsRepo.loadConfig()
            settingsRepo.registerListener(this)

            powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            updateBatterySaverState()

            try {
                val filter = IntentFilter().apply {
                    addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
                    addAction(Intent.ACTION_USER_PRESENT)
                    addAction(Intent.ACTION_SCREEN_ON)
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

            val thread = HandlerThread("MinamoAsciiThread")
            thread.start()
            renderThread = thread
            renderHandler = Handler(thread.looper)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            renderHandler?.post {
                val frame = holder.surfaceFrame
                surfaceWidth = frame.width().coerceAtLeast(320)
                surfaceHeight = frame.height().coerceAtLeast(480)
                simulation.configureGrid(surfaceWidth, surfaceHeight, currentConfig.gridDensity)
                surfaceReady = true
                lastFrameTimestampNs = 0L
                scheduleNextFrame()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            renderHandler?.post {
                surfaceWidth = width.coerceAtLeast(320)
                surfaceHeight = height.coerceAtLeast(480)
                simulation.configureGrid(surfaceWidth, surfaceHeight, currentConfig.gridDensity)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            renderHandler?.removeCallbacks(drawRunnable)
            surfaceReady = false
            super.onSurfaceDestroyed(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisibleNow = visible
            if (visible) {
                updateBatterySaverState()
                updateGyroRegistration()
                renderHandler?.post {
                    lastFrameTimestampNs = SystemClock.elapsedRealtimeNanos()
                    scheduleNextFrame()
                }
            } else {
                unregisterGyro()
                renderHandler?.removeCallbacks(drawRunnable)
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
            if (event.actionMasked == MotionEvent.ACTION_DOWN ||
                (event.actionMasked == MotionEvent.ACTION_MOVE && event.historySize > 0 && event.eventTime - event.downTime > 160L)
            ) {
                val tx = event.x
                val ty = event.y
                renderHandler?.post {
                    simulation.onTouch(tx, ty)
                }
            }
            super.onTouchEvent(event)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            val newCfg = settingsRepo.loadConfig()
            currentConfig = newCfg
            renderHandler?.post {
                simulation.configureGrid(surfaceWidth, surfaceHeight, newCfg.gridDensity)
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
            unregisterGyro()
            if (isReceiverRegistered) {
                try {
                    unregisterReceiver(systemEventReceiver)
                } catch (_: Exception) {}
                isReceiverRegistered = false
            }
            settingsRepo.unregisterListener(this)
            renderHandler?.removeCallbacksAndMessages(null)
            renderThread?.quitSafely()
            renderThread = null
            renderHandler = null
            super.onDestroy()
        }

        private fun scheduleNextFrame() {
            renderHandler?.removeCallbacks(drawRunnable)
            if (isVisibleNow && surfaceReady) {
                renderHandler?.post(drawRunnable)
            }
        }

        private fun updateBatterySaverState() {
            isBatterySaverActive = powerManager?.isPowerSaveMode == true
        }

        private fun updateGyroRegistration() {
            val shouldUseGyro = isVisibleNow && currentConfig.enableGyro && !isBatterySaverActive
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
}

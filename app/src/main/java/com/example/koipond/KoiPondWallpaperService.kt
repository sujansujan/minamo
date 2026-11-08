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
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.os.SystemClock
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder

/**
 * Android Live Wallpaper service managing a dedicated EGL14 / OpenGL ES 3.0 render thread,
 * frame-rate pacing (30/60 FPS), visibility lifecycle, Battery Saver integration,
 * home-screen scroll offsets, gyroscope parallax, and water touch ripples.
 */
class KoiPondWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = KoiPondEngine()

    inner class KoiPondEngine : Engine(),
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

        private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
        private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
        private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
        private var eglConfig: EGLConfig? = null

        private val simulation = PondSimulation()
        private var renderer: KoiPondRenderer? = null

        @Volatile
        private var isVisibleNow = false
        private var surfaceReady = false
        private var lastFrameTimestampNs = 0L

        private val powerSaverReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                updateBatterySaverState()
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
                    ((frameStartNs - lastFrameTimestampNs) / 1_000_000_000.0f).coerceIn(0.001f, 0.1f)
                }
                lastFrameTimestampNs = frameStartNs

                val cfg = currentConfig
                val batterySaver = isBatterySaverActive

                renderer?.let { r ->
                    r.setParallaxTargets(scrollOffsetX, gyroTiltX, gyroTiltY)
                    r.drawFrame(dt, cfg, batterySaver)
                    EGL14.eglSwapBuffers(eglDisplay, eglSurface)

                    val frameElapsedMs = (SystemClock.elapsedRealtimeNanos() - frameStartNs) / 1_000_000f
                    r.updateDynamicResolution(frameElapsedMs, cfg, batterySaver)
                }

                val targetFps = if (batterySaver) 30 else cfg.targetFps
                val targetIntervalMs = 1000L / targetFps
                val workDurationMs = (SystemClock.elapsedRealtimeNanos() - frameStartNs) / 1_000_000L
                val delayMs = (targetIntervalMs - workDurationMs).coerceAtLeast(1L)

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

            val filter = IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
            registerReceiver(powerSaverReceiver, filter)
            isReceiverRegistered = true

            sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            gyroSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

            val thread = HandlerThread("KoiPondGLThread")
            thread.start()
            renderThread = thread
            renderHandler = Handler(thread.looper)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            renderHandler?.post {
                initEgl(holder)
                val r = KoiPondRenderer(simulation)
                r.onSurfaceCreated()
                renderer = r
                surfaceReady = true
                lastFrameTimestampNs = 0L
                scheduleNextFrame()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            renderHandler?.post {
                renderer?.onSurfaceChanged(width, height, currentConfig, isBatterySaverActive)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            renderHandler?.removeCallbacks(drawRunnable)
            renderHandler?.post {
                surfaceReady = false
                releaseEglAndResources()
            }
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
                (event.actionMasked == MotionEvent.ACTION_MOVE && event.historySize > 0 && event.eventTime - event.downTime > 180L)
            ) {
                val tx = event.x
                val ty = event.y
                renderHandler?.post {
                    renderer?.onTouchWater(tx, ty)
                }
            }
            super.onTouchEvent(event)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            currentConfig = settingsRepo.loadConfig()
            updateGyroRegistration()
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                // Integrate angular rate softly with exponential decay back to center
                gyroTiltX = (gyroTiltX * 0.94f + event.values[1] * 0.08f).coerceIn(-1f, 1f)
                gyroTiltY = (gyroTiltY * 0.94f + event.values[0] * 0.08f).coerceIn(-1f, 1f)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

        override fun onDestroy() {
            unregisterGyro()
            if (isReceiverRegistered) {
                unregisterReceiver(powerSaverReceiver)
                isReceiverRegistered = false
            }
            settingsRepo.unregisterListener(this)

            renderHandler?.removeCallbacksAndMessages(null)
            renderHandler?.post {
                releaseEglAndResources()
                renderThread?.quitSafely()
            }
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

        private fun initEgl(holder: SurfaceHolder) {
            eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val version = IntArray(2)
            EGL14.eglInitialize(eglDisplay, version, 0, version, 1)

            val attribList = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT or 0x0040, // ES 3.0 bit
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 0,
                EGL14.EGL_STENCIL_SIZE, 0,
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, 1, numConfigs, 0)
            eglConfig = configs[0]

            val contextAttribs = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL14.EGL_NONE
            )
            eglContext = EGL14.eglCreateContext(
                eglDisplay,
                eglConfig,
                EGL14.EGL_NO_CONTEXT,
                contextAttribs,
                0
            )

            val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
            eglSurface = EGL14.eglCreateWindowSurface(
                eglDisplay,
                eglConfig,
                holder.surface,
                surfaceAttribs,
                0
            )

            EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
        }

        private fun releaseEglAndResources() {
            renderer?.release()
            renderer = null

            if (eglDisplay !== EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(
                    eglDisplay,
                    EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT
                )
                if (eglSurface !== EGL14.EGL_NO_SURFACE) {
                    EGL14.eglDestroySurface(eglDisplay, eglSurface)
                    eglSurface = EGL14.EGL_NO_SURFACE
                }
                if (eglContext !== EGL14.EGL_NO_CONTEXT) {
                    EGL14.eglDestroyContext(eglDisplay, eglContext)
                    eglContext = EGL14.EGL_NO_CONTEXT
                }
                EGL14.eglTerminate(eglDisplay)
                eglDisplay = EGL14.EGL_NO_DISPLAY
            }
        }
    }
}

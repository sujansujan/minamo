package com.example.koipond

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.AttributeSet
import android.view.Choreographer
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View

class AsciiPondView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs), SensorEventListener {

    val simulation = PondSimulation().apply {
        loadBundledFont(context)
    }
    var config: WallpaperConfig = WallpaperConfig()
        set(value) {
            field = value
            TimeOfDayPalette.invalidateCache()
            if (width > 0 && height > 0) {
                simulation.configureGrid(width, height, value.gridDensity, value.waterlinePercent)
            }
            if (isRunning) {
                syncSensorRegistration()
            }
        }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
    private val gyroFallbackSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private var isSensorRegistered = false
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var hasNeutralBaseline = false
    private var neutralPitch = 0f
    private var neutralRoll = 0f
    private var tiltX = 0f
    private var tiltY = 0f

    private var lastFrameNs = 0L
    private var lastDrawNs = 0L
    private var lastDragX = 0f
    private var lastDragEventMs = 0L
    private var isRunning = false
    var previewHourOverride: Float? = null
    var onSpeciesRandomized: ((String, String) -> Unit)? = null
    var onWaferDroppedListener: (() -> Unit)? = null

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isRunning) return
            val ecoActive = config.powerSaveMode == PowerSaveMode.MAX_ECO
            val targetFps = if (ecoActive) 15 else config.targetFps
            val minIntervalNs = 1_000_000_000L / targetFps
            if (frameTimeNanos - lastDrawNs >= minIntervalNs - 2_000_000L) {
                lastDrawNs = frameTimeNanos
                invalidate()
            }
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                simulation.onTouch(e.x, e.y)
                performClick()
                invalidate()
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                simulation.onLongPress(e.x, e.y)
                onWaferDroppedListener?.invoke()
                invalidate()
            }
        }
    )

    private fun isSystemNightMode(): Boolean {
        val uiMask = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return uiMask == Configuration.UI_MODE_NIGHT_YES
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            simulation.configureGrid(w, h, config.gridDensity, config.waterlinePercent)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val nowNs = SystemClock.elapsedRealtimeNanos()
        val dt = if (lastFrameNs == 0L) {
            1f / 30f
        } else {
            ((nowNs - lastFrameNs) / 1_000_000_000.0f).coerceIn(0.001f, 0.05f)
        }
        lastFrameNs = nowNs

        val ecoActive = config.powerSaveMode == PowerSaveMode.MAX_ECO
        val px = if (config.enableGyro) tiltX * 0.7f else 0f
        val py = if (config.enableGyro) tiltY * 0.7f else 0f

        simulation.updateAndRender(
            canvas = canvas,
            dt = dt,
            config = config,
            parallaxX = px,
            parallaxY = py,
            isBatterySaver = ecoActive,
            isWallpaperMode = false,
            batteryPercent = 85,
            isSystemDark = isSystemNightMode(),
            previewHourOverride = previewHourOverride
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastDragX = event.x
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.eventTime - lastDragEventMs >= 28L) {
                    lastDragEventMs = event.eventTime
                    val dx = event.x - lastDragX
                    lastDragX = event.x
                    simulation.onDrag(event.x, event.y, dx)
                    invalidate()
                }
            }
        }
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun performClick(): Boolean = super.performClick()

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

    fun shuffleEcosystem(): String {
        val now = System.nanoTime()
        simulation.randomizeEcosystemOnUnlock(now, preserveElderCrown = false)
        val snap = TimeOfDayPalette.resolve(
            mode = config.timeCycleMode,
            family = config.paletteFamily,
            themeMode = config.themeMode,
            isSystemDark = isSystemNightMode()
        )
        val visitorName = simulation.triggerOceanVisitor(now + 17L, snap.isNight)
        onSpeciesRandomized?.invoke(simulation.activeSpeciesSummary, snap.timeLabel)
        invalidate()
        return visitorName
    }

    fun summonPredatorNow(): String {
        val chance = if (config.huntMode == HuntMode.HARMLESS) 0f else config.huntMode.catchChance.coerceAtLeast(0.65f)
        simulation.triggerPredatorVisit(System.nanoTime(), chance)
        invalidate()
        return simulation.predator.species.displayName
    }

    fun startAnimation() {
        if (!isRunning) {
            isRunning = true
            lastFrameNs = SystemClock.elapsedRealtimeNanos()
            lastDrawNs = 0L
            syncSensorRegistration()
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    fun stopAnimation() {
        isRunning = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        unregisterSensor()
    }

    private fun syncSensorRegistration() {
        val ecoActive = config.powerSaveMode == PowerSaveMode.MAX_ECO
        val shouldUse = isRunning && config.enableGyro && !ecoActive
        val chosen = rotationSensor ?: gyroFallbackSensor
        if (shouldUse && !isSensorRegistered && chosen != null) {
            hasNeutralBaseline = false
            sensorManager?.registerListener(this, chosen, SensorManager.SENSOR_DELAY_UI, 40_000)
            isSensorRegistered = true
        } else if (!shouldUse && isSensorRegistered) {
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

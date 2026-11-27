package com.example.koipond

import android.content.Context
import android.graphics.Canvas
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
) : View(context, attrs) {

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

    private val tiltSensor = TiltParallaxSensor(context)

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
            val targetFps = config.performanceMode.maxFps
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

        val ecoActive = config.performanceMode == PerformanceMode.ECO
        val px = if (config.enableGyro && !ecoActive) tiltSensor.tiltX * 0.7f else 0f
        val py = if (config.enableGyro && !ecoActive) tiltSensor.tiltY * 0.7f else 0f

        simulation.updateAndRender(
            canvas = canvas,
            dt = dt,
            config = config,
            parallaxX = px,
            parallaxY = py,
            isBatterySaver = ecoActive,
            isWallpaperMode = false,
            batteryPercent = 85,
            isSystemDark = context.isSystemNightMode(),
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

    fun shuffleEcosystem(): String {
        val now = System.nanoTime()
        simulation.randomizeEcosystemOnUnlock(now, preserveElderCrown = false)
        val snap = TimeOfDayPalette.resolve(
            mode = config.timeCycleMode,
            family = config.paletteFamily,
            themeMode = config.themeMode,
            isSystemDark = context.isSystemNightMode(),
            overrideHour = previewHourOverride
        )
        val visitorName = simulation.triggerOceanVisitor(now + 17L, snap.isNight)
        onSpeciesRandomized?.invoke(simulation.activeSpeciesSummary, snap.timeLabel)
        invalidate()
        return visitorName
    }

    fun summonPredatorNow(): String {
        if (config.sceneBiome == SceneBiome.AQUARIUM) {
            val msg = simulation.summonAquariumCatNow()
            invalidate()
            return msg
        }
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
        tiltSensor.unregister()
    }

    private fun syncSensorRegistration() {
        val ecoActive = config.performanceMode == PerformanceMode.ECO
        tiltSensor.setActive(isRunning && config.enableGyro && !ecoActive)
    }
}

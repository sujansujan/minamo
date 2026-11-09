package com.example.koipond

import android.content.Context
import android.graphics.Canvas
import android.os.SystemClock
import android.util.AttributeSet
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
            if (width > 0 && height > 0) {
                simulation.configureGrid(width, height, value.gridDensity, value.waterlinePercent)
            }
        }

    private var lastFrameNs = 0L
    private var isRunning = false
    var onSpeciesRandomized: ((String, String) -> Unit)? = null
    var onWaferDroppedListener: (() -> Unit)? = null

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

        val ecoActive = config.powerSaveMode == PowerSaveMode.MAX_ECO
        simulation.updateAndRender(
            canvas = canvas,
            dt = dt,
            config = config,
            parallaxX = 0f,
            parallaxY = 0f,
            isBatterySaver = ecoActive,
            isWallpaperMode = false,
            batteryPercent = 85
        )

        if (isRunning) {
            val fps = if (ecoActive) 15 else config.targetFps
            val delayMs = (1000L / fps).coerceAtLeast(16L)
            postInvalidateDelayed(delayMs)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun performClick(): Boolean = super.performClick()

    fun shuffleEcosystem() {
        simulation.randomizeEcosystemOnUnlock(System.nanoTime())
        val snap = TimeOfDayPalette.resolve(config.timeCycleMode, config.paletteFamily)
        onSpeciesRandomized?.invoke(simulation.activeSpeciesSummary, snap.timeLabel)
        invalidate()
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
            invalidate()
        }
    }

    fun stopAnimation() {
        isRunning = false
    }
}

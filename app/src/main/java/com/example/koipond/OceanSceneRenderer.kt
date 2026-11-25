package com.example.koipond

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Zero-allocation Sculpted Silhouette + Monospace ASCII-Dither Scene Renderer.
 * Mirrors the exact visual grammar of [MarineCreatureRenderer]:
 * - Smooth organic contours (`Path`) with solid water/sky-tinted occlusion fills (and `#000000` on true-black AMOLED)
 * - Crisp hairline outlines + delicate JetBrains Mono ASCII texture glyphs (`# = ~ : . · ° *`)
 * - Scales cleanly from viewport dimensions (`refSpanPx`) so every element looks balanced on any screen size.
 */
class OceanSceneRenderer {

    private val shapePath = Path()
    private val detailPath = Path()
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val singleCharBuf = CharArray(1)

    private fun solidTint(bg: Int, fg: Int, mix: Float, isTrueBlackOled: Boolean): Int {
        if (isTrueBlackOled) return 0xFF000000.toInt()
        return TimeOfDayPalette.lerpColor(bg, fg, mix.coerceIn(0f, 1f)) or (0xFF shl 24)
    }

    private fun drawChar(canvas: Canvas, ch: Char, x: Float, y: Float, paint: Paint) {
        singleCharBuf[0] = ch
        canvas.drawText(singleCharBuf, 0, 1, x, y, paint)
    }

    // =========================================================================
    // 1. CELESTIAL BODIES (SUN & TRUE LUNAR PHASES)
    // =========================================================================

    fun drawSun(
        canvas: Canvas,
        textPaint: Paint,
        cx: Float,
        cy: Float,
        radiusPx: Float,
        celestialColor: Int,
        skyBg: Int,
        simTime: Float,
        isLightSurface: Boolean,
        isTrueBlackOled: Boolean,
        calmScrim: Boolean
    ) {
        val pulse = 1f + 0.06f * sin(simTime * 1.6f)
        val r = radiusPx * pulse

        // Soft outer atmospheric halo ring
        if (!isTrueBlackOled) {
            val haloAlpha = if (calmScrim) 22 else if (isLightSurface) 34 else 28
            fillPaint.color = (celestialColor and 0x00FFFFFF) or (haloAlpha shl 24)
            canvas.drawCircle(cx, cy, r * 1.75f, fillPaint)
        }

        // Solid solar disc
        fillPaint.color = solidTint(skyBg, celestialColor, if (isLightSurface) 0.42f else 0.34f, isTrueBlackOled)
        canvas.drawCircle(cx, cy, r, fillPaint)

        val strokeAlpha = if (calmScrim) 140 else 210
        strokePaint.color = (celestialColor and 0x00FFFFFF) or (strokeAlpha shl 24)
        strokePaint.strokeWidth = (r * 0.08f).coerceIn(1.2f, 2.2f)
        canvas.drawCircle(cx, cy, r, strokePaint)

        // 8 delicate breathing solar rays rotating very slowly
        val rot = simTime * 0.08f
        for (i in 0 until 8) {
            val ang = rot + i * (PI.toFloat() / 4f)
            val rayLen = if (i % 2 == 0) r * 0.56f else r * 0.36f
            val r0 = r * 1.20f
            val r1 = r0 + rayLen * (0.90f + 0.12f * sin(simTime * 2.2f + i))
            canvas.drawLine(
                cx + cos(ang) * r0,
                cy + sin(ang) * r0,
                cx + cos(ang) * r1,
                cy + sin(ang) * r1,
                strokePaint
            )
        }

        // Crisp monospace core glyph
        val prevSize = textPaint.textSize
        textPaint.textSize = (r * 0.85f).coerceIn(8f, prevSize * 1.15f)
        textPaint.color = celestialColor
        drawChar(canvas, '°', cx - textPaint.textSize * 0.28f, cy + textPaint.textSize * 0.32f, textPaint)
        textPaint.textSize = prevSize
    }

    fun drawMoon(
        canvas: Canvas,
        textPaint: Paint,
        cx: Float,
        cy: Float,
        radiusPx: Float,
        lunarPhaseIndex: Int, // 0=Waxing Crescent, 1=Half Moon, 2=Full Moon, 3=Waning Crescent
        celestialColor: Int,
        skyBg: Int,
        isLightSurface: Boolean,
        isTrueBlackOled: Boolean,
        calmScrim: Boolean
    ) {
        val r = radiusPx
        if (!isTrueBlackOled) {
            val haloAlpha = if (calmScrim) 18 else 26
            fillPaint.color = (celestialColor and 0x00FFFFFF) or (haloAlpha shl 24)
            canvas.drawCircle(cx, cy, r * 1.65f, fillPaint)
        }

        val discFill = solidTint(skyBg, celestialColor, if (isLightSurface) 0.45f else 0.36f, isTrueBlackOled)
        val strokeAlpha = if (calmScrim) 145 else 215
        strokePaint.color = (celestialColor and 0x00FFFFFF) or (strokeAlpha shl 24)
        strokePaint.strokeWidth = (r * 0.085f).coerceIn(1.2f, 2.2f)

        shapePath.reset()
        when (lunarPhaseIndex) {
            0 -> {
                // Waxing Crescent (right curve lit)
                shapePath.moveTo(cx, cy - r)
                shapePath.quadTo(cx + r * 1.15f, cy - r * 0.55f, cx + r, cy)
                shapePath.quadTo(cx + r * 1.15f, cy + r * 0.55f, cx, cy + r)
                shapePath.quadTo(cx + r * 0.48f, cy, cx, cy - r)
                shapePath.close()
                fillPaint.color = discFill
                canvas.drawPath(shapePath, fillPaint)
                canvas.drawPath(shapePath, strokePaint)
            }
            1 -> {
                // First Quarter Half Moon
                shapePath.moveTo(cx, cy - r)
                shapePath.quadTo(cx + r * 1.15f, cy - r * 0.55f, cx + r, cy)
                shapePath.quadTo(cx + r * 1.15f, cy + r * 0.55f, cx, cy + r)
                shapePath.lineTo(cx, cy - r)
                shapePath.close()
                fillPaint.color = discFill
                canvas.drawPath(shapePath, fillPaint)
                canvas.drawPath(shapePath, strokePaint)
            }
            2 -> {
                // Full Moon with subtle ASCII crater dither
                fillPaint.color = discFill
                canvas.drawCircle(cx, cy, r, fillPaint)
                canvas.drawCircle(cx, cy, r, strokePaint)
                val prevSize = textPaint.textSize
                textPaint.textSize = (r * 0.65f).coerceIn(8f, prevSize)
                textPaint.color = celestialColor
                drawChar(canvas, '·', cx - r * 0.35f, cy - r * 0.08f, textPaint)
                drawChar(canvas, ':', cx + r * 0.10f, cy + r * 0.32f, textPaint)
                textPaint.textSize = prevSize
            }
            else -> {
                // Waning Crescent (left curve lit)
                shapePath.moveTo(cx, cy - r)
                shapePath.quadTo(cx - r * 1.15f, cy - r * 0.55f, cx - r, cy)
                shapePath.quadTo(cx - r * 1.15f, cy + r * 0.55f, cx, cy + r)
                shapePath.quadTo(cx - r * 0.48f, cy, cx, cy - r)
                shapePath.close()
                fillPaint.color = discFill
                canvas.drawPath(shapePath, fillPaint)
                canvas.drawPath(shapePath, strokePaint)
            }
        }
    }

    // =========================================================================
    // 2. DRIFTING CLOUDS, SOARING SEABIRDS, SAILBOAT, LIGHTHOUSE & DRAGONFLY
    // =========================================================================

    fun drawCloud(
        canvas: Canvas,
        textPaint: Paint,
        cx: Float,
        cy: Float,
        widthPx: Float,
        cloudColor: Int,
        skyBg: Int,
        isLightSurface: Boolean,
        isTrueBlackOled: Boolean,
        calmScrim: Boolean
    ) {
        val hw = widthPx * 0.5f
        val hh = widthPx * 0.22f
        if (cx + hw < -12f || cx - hw > canvas.width + 12f) return

        shapePath.reset()
        shapePath.moveTo(cx - hw * 0.85f, cy + hh * 0.45f)
        shapePath.quadTo(cx - hw * 1.05f, cy - hh * 0.15f, cx - hw * 0.50f, cy - hh * 0.35f)
        shapePath.quadTo(cx - hw * 0.25f, cy - hh * 1.25f, cx + hw * 0.18f, cy - hh * 0.88f)
        shapePath.quadTo(cx + hw * 0.58f, cy - hh * 1.05f, cx + hw * 0.72f, cy - hh * 0.22f)
        shapePath.quadTo(cx + hw * 1.04f, cy, cx + hw * 0.82f, cy + hh * 0.45f)
        shapePath.close()

        fillPaint.color = solidTint(skyBg, cloudColor, if (calmScrim) 0.12f else if (isLightSurface) 0.28f else 0.18f, isTrueBlackOled)
        canvas.drawPath(shapePath, fillPaint)

        val alpha = if (calmScrim) 90 else 150
        strokePaint.color = (cloudColor and 0x00FFFFFF) or (alpha shl 24)
        strokePaint.strokeWidth = 1.2f
        canvas.drawPath(shapePath, strokePaint)

        val prevSize = textPaint.textSize
        textPaint.textSize = (hh * 0.82f).coerceIn(8f, prevSize * 0.90f)
        textPaint.color = (cloudColor and 0x00FFFFFF) or ((alpha + 15).coerceAtMost(200) shl 24)
        drawChar(canvas, '~', cx - hw * 0.22f, cy + hh * 0.18f, textPaint)
        drawChar(canvas, '·', cx + hw * 0.24f, cy + hh * 0.08f, textPaint)
        textPaint.textSize = prevSize
    }

    fun drawSeabird(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        spanPx: Float,
        flapPhase: Float,
        birdColor: Int,
        calmScrim: Boolean
    ) {
        val hs = spanPx * 0.5f
        if (cx + hs < -8f || cx - hs > canvas.width + 8f) return
        val lift = sin(flapPhase) * (hs * 0.48f)
        val bodyBob = -lift * 0.18f

        strokePaint.color = (birdColor and 0x00FFFFFF) or ((if (calmScrim) 135 else 205) shl 24)
        strokePaint.strokeWidth = (spanPx * 0.055f).coerceIn(1.2f, 2.2f)

        shapePath.reset()
        shapePath.moveTo(cx - hs, cy - lift * 0.85f)
        shapePath.quadTo(cx - hs * 0.48f, cy - hs * 0.34f - lift * 0.35f, cx, cy + bodyBob)
        shapePath.quadTo(cx + hs * 0.48f, cy - hs * 0.34f - lift * 0.35f, cx + hs, cy - lift * 0.85f)
        canvas.drawPath(shapePath, strokePaint)
    }

    fun drawSailboat(
        canvas: Canvas,
        textPaint: Paint,
        cx: Float,
        waterY: Float,
        widthPx: Float,
        boatColor: Int,
        sailAccentColor: Int,
        skyHorizonBg: Int,
        simTime: Float,
        isTrueBlackOled: Boolean
    ) {
        val hw = widthPx * 0.5f
        if (cx + hw < -16f || cx - hw > canvas.width + 16f) return
        val bob = sin(simTime * 1.9f) * 1.6f
        val baseY = waterY + bob
        val mastH = widthPx * 0.88f

        strokePaint.strokeWidth = 1.25f
        strokePaint.color = (boatColor and 0x00FFFFFF) or (190 shl 24)

        // Main billowing triangular sail
        shapePath.reset()
        shapePath.moveTo(cx, baseY - mastH)
        shapePath.quadTo(cx + hw * 0.65f, baseY - mastH * 0.52f, cx + hw * 0.82f, baseY - mastH * 0.16f)
        shapePath.lineTo(cx, baseY - mastH * 0.16f)
        shapePath.close()
        fillPaint.color = solidTint(skyHorizonBg, sailAccentColor, 0.36f, isTrueBlackOled)
        canvas.drawPath(shapePath, fillPaint)
        canvas.drawPath(shapePath, strokePaint)

        // Smaller front jib sail
        detailPath.reset()
        detailPath.moveTo(cx - 1.5f, baseY - mastH * 0.85f)
        detailPath.quadTo(cx - hw * 0.52f, baseY - mastH * 0.48f, cx - hw * 0.72f, baseY - mastH * 0.18f)
        detailPath.lineTo(cx - 1.5f, baseY - mastH * 0.18f)
        detailPath.close()
        fillPaint.color = solidTint(skyHorizonBg, boatColor, 0.28f, isTrueBlackOled)
        canvas.drawPath(detailPath, fillPaint)
        canvas.drawPath(detailPath, strokePaint)

        // Hull resting cleanly on the horizon
        shapePath.reset()
        shapePath.moveTo(cx - hw, baseY - mastH * 0.14f)
        shapePath.lineTo(cx + hw, baseY - mastH * 0.14f)
        shapePath.quadTo(cx + hw * 0.75f, baseY + 2f, cx + hw * 0.55f, baseY + 2f)
        shapePath.lineTo(cx - hw * 0.62f, baseY + 2f)
        shapePath.close()
        fillPaint.color = solidTint(skyHorizonBg, boatColor, 0.55f, isTrueBlackOled)
        canvas.drawPath(shapePath, fillPaint)
        canvas.drawPath(shapePath, strokePaint)

        // Tiny monospace sail texture
        val prevSize = textPaint.textSize
        textPaint.textSize = (widthPx * 0.24f).coerceIn(7f, prevSize * 0.8f)
        textPaint.color = boatColor
        drawChar(canvas, ':', cx + hw * 0.18f, baseY - mastH * 0.34f, textPaint)
        textPaint.textSize = prevSize
    }

    fun drawLighthouse(
        canvas: Canvas,
        textPaint: Paint,
        cx: Float,
        waterY: Float,
        heightPx: Float,
        towerColor: Int,
        beaconColor: Int,
        skyHorizonBg: Int,
        beaconOn: Boolean,
        simTime: Float,
        isTrueBlackOled: Boolean,
        extraFlash: Boolean = false
    ) {
        val baseW = heightPx * 0.92f
        val towerW = heightPx * 0.24f
        val topY = waterY - heightPx
        val showBeacon = beaconOn || extraFlash

        // Soft glowing rotating light beam cones at dusk/night (or extra vivid pulse when tapped!)
        if (showBeacon) {
            val beamSpread = heightPx * (if (extraFlash) 2.55f else 1.8f + 0.35f * sin(simTime * 2.5f))
            val beamY = topY + heightPx * 0.12f
            if (!isTrueBlackOled) {
                val beamAlpha = if (extraFlash) 76 else 42
                fillPaint.color = (beaconColor and 0x00FFFFFF) or (beamAlpha shl 24)
                shapePath.reset()
                shapePath.moveTo(cx, beamY)
                shapePath.lineTo(cx - beamSpread, beamY - heightPx * 0.30f)
                shapePath.lineTo(cx - beamSpread, beamY + heightPx * 0.26f)
                shapePath.close()
                canvas.drawPath(shapePath, fillPaint)

                shapePath.reset()
                shapePath.moveTo(cx, beamY)
                shapePath.lineTo(cx + beamSpread * 0.72f, beamY - heightPx * 0.24f)
                shapePath.lineTo(cx + beamSpread * 0.72f, beamY + heightPx * 0.20f)
                shapePath.close()
                canvas.drawPath(shapePath, fillPaint)
            } else if (extraFlash) {
                strokePaint.strokeWidth = 1.25f
                strokePaint.color = (beaconColor and 0x00FFFFFF) or (145 shl 24)
                canvas.drawLine(cx, beamY, cx - beamSpread * 0.85f, beamY - heightPx * 0.16f, strokePaint)
                canvas.drawLine(cx, beamY, cx - beamSpread * 0.85f, beamY + heightPx * 0.14f, strokePaint)
            }
        }

        strokePaint.strokeWidth = 1.25f
        strokePaint.color = (towerColor and 0x00FFFFFF) or (195 shl 24)

        // Rocky coastal headland mound
        shapePath.reset()
        shapePath.moveTo(cx - baseW, waterY + 2f)
        shapePath.quadTo(cx - baseW * 0.45f, waterY - heightPx * 0.32f, cx, waterY - heightPx * 0.26f)
        shapePath.quadTo(cx + baseW * 0.55f, waterY - heightPx * 0.30f, cx + baseW * 0.85f, waterY + 2f)
        shapePath.close()
        fillPaint.color = solidTint(skyHorizonBg, towerColor, 0.45f, isTrueBlackOled)
        canvas.drawPath(shapePath, fillPaint)
        canvas.drawPath(shapePath, strokePaint)

        // Tapered lighthouse tower
        shapePath.reset()
        shapePath.moveTo(cx - towerW * 0.72f, waterY - heightPx * 0.22f)
        shapePath.lineTo(cx - towerW * 0.44f, topY + heightPx * 0.20f)
        shapePath.lineTo(cx + towerW * 0.44f, topY + heightPx * 0.20f)
        shapePath.lineTo(cx + towerW * 0.72f, waterY - heightPx * 0.22f)
        shapePath.close()
        fillPaint.color = solidTint(skyHorizonBg, towerColor, 0.32f, isTrueBlackOled)
        canvas.drawPath(shapePath, fillPaint)
        canvas.drawPath(shapePath, strokePaint)

        // Lantern room dome cap
        detailPath.reset()
        detailPath.moveTo(cx - towerW * 0.56f, topY + heightPx * 0.20f)
        detailPath.quadTo(cx, topY - heightPx * 0.02f, cx + towerW * 0.56f, topY + heightPx * 0.20f)
        detailPath.close()
        fillPaint.color = solidTint(skyHorizonBg, if (showBeacon) beaconColor else towerColor, 0.55f, isTrueBlackOled)
        canvas.drawPath(detailPath, fillPaint)
        canvas.drawPath(detailPath, strokePaint)

        if (showBeacon) {
            val prevSize = textPaint.textSize
            textPaint.textSize = (heightPx * (if (extraFlash) 0.38f else 0.32f)).coerceIn(8f, prevSize)
            textPaint.color = beaconColor
            drawChar(canvas, '*', cx - textPaint.textSize * 0.28f, topY + heightPx * 0.24f, textPaint)
            textPaint.textSize = prevSize
            if (extraFlash) {
                strokePaint.color = (beaconColor and 0x00FFFFFF) or (165 shl 24)
                strokePaint.strokeWidth = 1.2f
                canvas.drawCircle(cx, topY + heightPx * 0.14f, heightPx * 0.28f, strokePaint)
            }
        }
    }

    fun drawWhaleSpout(
        canvas: Canvas,
        textPaint: Paint,
        cx: Float,
        waterY: Float,
        heightPx: Float,
        frac: Float, // 0..1 lifecycle
        bubbleColor: Int
    ) {
        val arch = sin(frac * PI.toFloat()).coerceAtLeast(0f)
        if (arch <= 0.05f) return
        val h = heightPx * arch
        val spread = h * 0.52f

        strokePaint.strokeWidth = 1.4f
        strokePaint.color = (bubbleColor and 0x00FFFFFF) or ((185f * arch).toInt().coerceIn(30, 200) shl 24)

        // Twin graceful parabolic fountain arcs!
        shapePath.reset()
        shapePath.moveTo(cx, waterY)
        shapePath.quadTo(cx - spread * 0.15f, waterY - h * 0.95f, cx - spread, waterY - h * 0.65f)
        canvas.drawPath(shapePath, strokePaint)

        detailPath.reset()
        detailPath.moveTo(cx, waterY)
        detailPath.quadTo(cx + spread * 0.15f, waterY - h * 0.95f, cx + spread, waterY - h * 0.65f)
        canvas.drawPath(detailPath, strokePaint)

        val prevSize = textPaint.textSize
        textPaint.textSize = (heightPx * 0.20f).coerceIn(8f, prevSize)
        textPaint.color = (bubbleColor and 0x00FFFFFF) or ((210f * arch).toInt().coerceIn(40, 220) shl 24)
        drawChar(canvas, '*', cx - textPaint.textSize * 0.28f, waterY - h * 0.92f, textPaint)
        drawChar(canvas, '°', cx - spread * 0.92f, waterY - h * 0.58f, textPaint)
        drawChar(canvas, '·', cx + spread * 0.85f, waterY - h * 0.58f, textPaint)
        textPaint.textSize = prevSize
    }

    fun drawSurfaceInsect(
        canvas: Canvas,
        textPaint: Paint,
        cx: Float,
        cy: Float,
        sizePx: Float,
        phase: Float,
        facingRight: Boolean,
        isNight: Boolean,
        color: Int,
        isTrueBlackOled: Boolean
    ) {
        if (isNight) {
            // Softly pulsing bioluminescent night firefly!
            val glow = 0.5f + 0.5f * sin(phase * 1.8f)
            if (!isTrueBlackOled) {
                fillPaint.color = (color and 0x00FFFFFF) or ((30 + glow * 55).toInt() shl 24)
                canvas.drawCircle(cx, cy, sizePx * (0.42f + 0.25f * glow), fillPaint)
            }
            textPaint.color = color
            drawChar(canvas, if (glow > 0.55f) '*' else '°', cx - textPaint.textSize * 0.28f, cy + textPaint.textSize * 0.30f, textPaint)
            return
        }

        // Daytime sculpted dragonfly with 4 fluttering gossamer wings!
        val dir = if (facingRight) 1f else -1f
        val flutter = sin(phase * 4.5f) * (sizePx * 0.26f)
        strokePaint.color = (color and 0x00FFFFFF) or (210 shl 24)
        strokePaint.strokeWidth = 1.4f

        // Slender dragonfly body
        canvas.drawLine(cx - dir * sizePx * 0.55f, cy + 1f, cx + dir * sizePx * 0.38f, cy - 1f, strokePaint)

        // Translucent fluttering upper wings
        strokePaint.strokeWidth = 1.1f
        shapePath.reset()
        shapePath.moveTo(cx + dir * sizePx * 0.08f, cy)
        shapePath.quadTo(cx - dir * sizePx * 0.12f, cy - sizePx * 0.58f + flutter, cx - dir * sizePx * 0.36f, cy - sizePx * 0.28f + flutter)
        shapePath.close()
        if (!isTrueBlackOled) {
            fillPaint.color = (color and 0x00FFFFFF) or (55 shl 24)
            canvas.drawPath(shapePath, fillPaint)
        }
        canvas.drawPath(shapePath, strokePaint)

        detailPath.reset()
        detailPath.moveTo(cx + dir * sizePx * 0.08f, cy)
        detailPath.quadTo(cx + dir * sizePx * 0.32f, cy - sizePx * 0.45f - flutter, cx + dir * sizePx * 0.02f, cy - sizePx * 0.18f - flutter)
        detailPath.close()
        canvas.drawPath(detailPath, strokePaint)
    }

    // =========================================================================
    // 3. CONTINUOUS SPRING-DRIVEN WATERLINE MENISCUS & ZEN POND SURFACE
    // =========================================================================

    fun drawMeniscusWave(
        canvas: Canvas,
        textPaint: Paint,
        cols: Int,
        cellWidthPx: Float,
        waterY: Float,
        cellHeightPx: Float,
        waveHeight: FloatArray,
        simTime: Float,
        meniscusColor: Int,
        waterBgTop: Int,
        isLightSurface: Boolean,
        isTrueBlackOled: Boolean
    ) {
        if (cols < 4) return
        shapePath.reset()
        val ampPx = cellHeightPx * 0.42f
        val step = 2
        var first = true
        for (c in 0 until cols step step) {
            val x = c * cellWidthPx
            val ambient = sin(c * 0.42f + simTime * 2.3f) * 0.32f + cos(c * 0.23f - simTime * 1.4f) * 0.22f
            val y = waterY - (ambient + waveHeight[c]) * ampPx
            if (first) {
                shapePath.moveTo(x, y)
                first = false
            } else {
                shapePath.lineTo(x, y)
            }
        }
        val lastAmbient = sin((cols - 1) * 0.42f + simTime * 2.3f) * 0.32f + cos((cols - 1) * 0.23f - simTime * 1.4f) * 0.22f
        shapePath.lineTo(canvas.width.toFloat(), waterY - (lastAmbient + waveHeight[cols - 1]) * ampPx)

        // Soft foam meniscus glow band just under the wave line
        if (!isTrueBlackOled) {
            strokePaint.color = (meniscusColor and 0x00FFFFFF) or ((if (isLightSurface) 46 else 38) shl 24)
            strokePaint.strokeWidth = 5.5f
            canvas.drawPath(shapePath, strokePaint)
        }
        // Crisp luminous waterline hairline
        strokePaint.color = (meniscusColor and 0x00FFFFFF) or (215 shl 24)
        strokePaint.strokeWidth = 1.7f
        canvas.drawPath(shapePath, strokePaint)

        // Delicate monospace '~' & '·' wave crest sparkles riding along the curve!
        val prevSize = textPaint.textSize
        textPaint.textSize = (prevSize * 0.78f).coerceAtLeast(8f)
        val glintColor = TimeOfDayPalette.lerpColor(waterBgTop, meniscusColor, 0.85f)
        textPaint.color = glintColor
        val stride = (cols / 10).coerceAtLeast(6)
        for (c in 3 until cols - 3 step stride) {
            val hVal = sin(c * 0.42f + simTime * 2.3f) * 0.32f + waveHeight[c]
            val x = c * cellWidthPx
            val y = waterY - hVal * ampPx
            val ch = if (hVal > 0.35f) '~' else '·'
            drawChar(canvas, ch, x, y + textPaint.textSize * 0.45f, textPaint)
        }
        textPaint.textSize = prevSize
    }

    fun drawLilyPadAndLotus(
        canvas: Canvas,
        textPaint: Paint,
        cx: Float,
        waterY: Float,
        widthPx: Float,
        hasLotus: Boolean,
        simTime: Float,
        padColor: Int,
        lotusColor: Int,
        waterBgTop: Int,
        isTrueBlackOled: Boolean
    ) {
        val hw = widthPx * 0.5f
        val hh = widthPx * 0.17f

        // Delicate dangling underwater roots curving softly below the pad
        strokePaint.strokeWidth = 1.15f
        val rootCol = TimeOfDayPalette.lerpColor(waterBgTop, padColor, 0.50f)
        strokePaint.color = (rootCol and 0x00FFFFFF) or (165 shl 24)
        for (rIdx in -1..1) {
            val rx = cx + rIdx * (hw * 0.28f)
            val rootLen = widthPx * (0.55f + (if (rIdx == 0) 0.22f else 0f))
            val sway = sin(simTime * 1.5f + rIdx * 1.2f) * (hw * 0.18f)
            detailPath.reset()
            detailPath.moveTo(rx, waterY + hh * 0.4f)
            detailPath.quadTo(rx + sway, waterY + rootLen * 0.55f, rx - sway * 0.7f, waterY + rootLen)
            canvas.drawPath(detailPath, strokePaint)
        }

        // Organic notched floating lily pad disc
        shapePath.reset()
        shapePath.moveTo(cx + hw * 0.22f, waterY - hh * 0.15f)
        shapePath.lineTo(cx + hw, waterY - hh * 0.45f)
        shapePath.quadTo(cx + hw * 0.70f, waterY - hh * 1.15f, cx, waterY - hh * 1.10f)
        shapePath.quadTo(cx - hw * 1.05f, waterY - hh * 0.95f, cx - hw, waterY)
        shapePath.quadTo(cx - hw * 0.95f, waterY + hh * 1.15f, cx + hw * 0.15f, waterY + hh * 1.05f)
        shapePath.quadTo(cx + hw * 0.95f, waterY + hh * 0.85f, cx + hw * 0.92f, waterY + hh * 0.15f)
        shapePath.close()

        fillPaint.color = solidTint(waterBgTop, padColor, 0.48f, isTrueBlackOled)
        canvas.drawPath(shapePath, fillPaint)
        strokePaint.color = (padColor and 0x00FFFFFF) or (215 shl 24)
        strokePaint.strokeWidth = 1.4f
        canvas.drawPath(shapePath, strokePaint)

        // Blooming sculpted lotus flower resting on the pad!
        if (hasLotus) {
            val fw = widthPx * 0.40f
            val fh = widthPx * 0.34f
            fillPaint.color = solidTint(waterBgTop, lotusColor, 0.55f, isTrueBlackOled)
            strokePaint.color = (lotusColor and 0x00FFFFFF) or (225 shl 24)
            for (p in -1..1) {
                val tilt = p * fw * 0.42f
                detailPath.reset()
                detailPath.moveTo(cx, waterY - hh * 0.2f)
                detailPath.quadTo(cx + tilt - fw * 0.28f, waterY - fh * 0.75f, cx + tilt, waterY - fh * (if (p == 0) 1.05f else 0.82f))
                detailPath.quadTo(cx + tilt + fw * 0.28f, waterY - fh * 0.75f, cx, waterY - hh * 0.2f)
                detailPath.close()
                canvas.drawPath(detailPath, fillPaint)
                canvas.drawPath(detailPath, strokePaint)
            }
            val prevSize = textPaint.textSize
            textPaint.textSize = (fh * 0.45f).coerceIn(7f, prevSize)
            textPaint.color = lotusColor
            drawChar(canvas, '*', cx - textPaint.textSize * 0.28f, waterY - fh * 0.22f, textPaint)
            textPaint.textSize = prevSize
        }
    }

    fun drawShorelineReeds(
        canvas: Canvas,
        baseX: Float,
        waterY: Float,
        heightPx: Float,
        simTime: Float,
        reedColor: Int
    ) {
        strokePaint.color = (reedColor and 0x00FFFFFF) or (195 shl 24)
        strokePaint.strokeWidth = 1.5f
        for (i in 0 until 5) {
            val rx = baseX + (i - 2) * (heightPx * 0.16f)
            val h = heightPx * (0.68f + (i % 3) * 0.18f)
            val sway = sin(simTime * 1.35f + i * 0.85f) * (heightPx * 0.14f)
            shapePath.reset()
            shapePath.moveTo(rx, waterY + 3f)
            shapePath.quadTo(rx + sway * 0.4f, waterY - h * 0.55f, rx + sway, waterY - h)
            canvas.drawPath(shapePath, strokePaint)
        }
    }

    fun drawShoreRockAndAmphibian(
        canvas: Canvas,
        textPaint: Paint,
        cx: Float,
        waterY: Float,
        widthPx: Float,
        isTurtle: Boolean,
        isBlinking: Boolean,
        isHidden: Boolean,
        showSplash: Boolean,
        tongueOut: Boolean,
        rockColor: Int,
        amphColor: Int,
        accentColor: Int,
        eyeColor: Int,
        skyBg: Int,
        isTrueBlackOled: Boolean
    ) {
        val hw = widthPx * 0.5f
        val rh = widthPx * 0.24f

        // Smooth mossy shoreline rock
        shapePath.reset()
        shapePath.moveTo(cx - hw, waterY + 2f)
        shapePath.quadTo(cx - hw * 0.72f, waterY - rh * 1.15f, cx, waterY - rh)
        shapePath.quadTo(cx + hw * 0.78f, waterY - rh * 1.05f, cx + hw, waterY + 2f)
        shapePath.close()
        fillPaint.color = solidTint(skyBg, rockColor, 0.45f, isTrueBlackOled)
        canvas.drawPath(shapePath, fillPaint)
        strokePaint.color = (rockColor and 0x00FFFFFF) or (205 shl 24)
        strokePaint.strokeWidth = 1.4f
        canvas.drawPath(shapePath, strokePaint)

        if (isHidden) {
            if (showSplash) {
                strokePaint.color = (accentColor and 0x00FFFFFF) or (200 shl 24)
                canvas.drawLine(cx, waterY - rh, cx - hw * 0.35f, waterY - rh * 2.2f, strokePaint)
                canvas.drawLine(cx, waterY - rh, cx, waterY - rh * 2.5f, strokePaint)
                canvas.drawLine(cx, waterY - rh, cx + hw * 0.35f, waterY - rh * 2.2f, strokePaint)
            }
            return
        }

        val aW = widthPx * 0.44f
        val aH = widthPx * 0.26f
        val baseY = waterY - rh * 0.85f

        // Sculpted Frog or Basking Pond Turtle perched on the rock
        shapePath.reset()
        shapePath.moveTo(cx - aW * 0.5f, baseY)
        shapePath.quadTo(cx - aW * 0.45f, baseY - aH * 1.25f, cx, baseY - aH * 1.15f)
        shapePath.quadTo(cx + aW * 0.45f, baseY - aH * 1.25f, cx + aW * 0.5f, baseY)
        shapePath.close()
        fillPaint.color = solidTint(skyBg, amphColor, 0.55f, isTrueBlackOled)
        canvas.drawPath(shapePath, fillPaint)
        strokePaint.color = (amphColor and 0x00FFFFFF) or (220 shl 24)
        canvas.drawPath(shapePath, strokePaint)

        if (tongueOut && !isTurtle) {
            strokePaint.color = (accentColor and 0x00FFFFFF) or (235 shl 24)
            strokePaint.strokeWidth = 1.8f
            canvas.drawLine(cx - aW * 0.35f, baseY - aH * 0.45f, cx - aW * 1.55f, baseY - aH * 0.72f, strokePaint)
        }

        val prevSize = textPaint.textSize
        textPaint.textSize = (aH * 0.72f).coerceIn(8f, prevSize)
        textPaint.color = eyeColor
        if (isTurtle) {
            drawChar(canvas, '@', cx - textPaint.textSize * 0.28f, baseY - aH * 0.28f, textPaint)
        } else {
            val eCh = if (isBlinking) '-' else '°'
            drawChar(canvas, eCh, cx - aW * 0.28f, baseY - aH * 0.92f, textPaint)
            drawChar(canvas, eCh, cx + aW * 0.08f, baseY - aH * 0.92f, textPaint)
        }
        textPaint.textSize = prevSize
    }

    fun drawSurfaceDrifter(
        canvas: Canvas,
        cx: Float,
        waterY: Float,
        widthPx: Float,
        isPaperBoat: Boolean,
        color: Int,
        skyBg: Int,
        isTrueBlackOled: Boolean
    ) {
        val hw = widthPx * 0.5f
        val hh = widthPx * 0.38f
        strokePaint.color = (color and 0x00FFFFFF) or (215 shl 24)
        strokePaint.strokeWidth = 1.3f
        fillPaint.color = solidTint(skyBg, color, 0.42f, isTrueBlackOled)

        if (isPaperBoat) {
            // Crisp origami paper boat silhouette
            shapePath.reset()
            shapePath.moveTo(cx - hw, waterY - hh * 0.28f)
            shapePath.lineTo(cx + hw, waterY - hh * 0.28f)
            shapePath.lineTo(cx + hw * 0.62f, waterY + 2f)
            shapePath.lineTo(cx - hw * 0.62f, waterY + 2f)
            shapePath.close()
            canvas.drawPath(shapePath, fillPaint)
            canvas.drawPath(shapePath, strokePaint)

            detailPath.reset()
            detailPath.moveTo(cx - hw * 0.45f, waterY - hh * 0.28f)
            detailPath.lineTo(cx, waterY - hh * 1.15f)
            detailPath.lineTo(cx + hw * 0.45f, waterY - hh * 0.28f)
            detailPath.close()
            canvas.drawPath(detailPath, fillPaint)
            canvas.drawPath(detailPath, strokePaint)
        } else {
            // Curved floating autumn leaf
            shapePath.reset()
            shapePath.moveTo(cx - hw * 0.85f, waterY)
            shapePath.quadTo(cx, waterY - hh * 0.72f, cx + hw * 0.85f, waterY - hh * 0.12f)
            shapePath.quadTo(cx, waterY + hh * 0.42f, cx - hw * 0.85f, waterY)
            shapePath.close()
            canvas.drawPath(shapePath, fillPaint)
            canvas.drawPath(shapePath, strokePaint)
        }
    }

    // =========================================================================
    // 4. UNDERWATER TERRAIN, SUNBEAMS, CORAL REEF, ANEMONE, GIANT CLAM, LANTERN & KELP
    // =========================================================================

    fun drawSubmergedTerrainRidge(
        canvas: Canvas,
        baseY: Float,
        amplitudePx: Float,
        freq: Float,
        phaseShift: Float,
        ridgeColor: Int,
        waterBgDeep: Int,
        fillAlphaMix: Float,
        isTrueBlackOled: Boolean
    ) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()
        shapePath.reset()
        shapePath.moveTo(0f, h)
        val steps = 20
        for (i in 0..steps) {
            val frac = i / steps.toFloat()
            val x = frac * w
            val ridge = sin(frac * freq + phaseShift) * amplitudePx +
                cos(frac * freq * 1.7f - phaseShift * 0.6f) * (amplitudePx * 0.42f)
            shapePath.lineTo(x, baseY + ridge)
        }
        shapePath.lineTo(w, h)
        shapePath.close()

        if (!isTrueBlackOled) {
            fillPaint.color = solidTint(waterBgDeep, ridgeColor, fillAlphaMix, false)
            canvas.drawPath(shapePath, fillPaint)
        }

        // Delicate upper contour line only
        detailPath.reset()
        for (i in 0..steps) {
            val frac = i / steps.toFloat()
            val x = frac * w
            val ridge = sin(frac * freq + phaseShift) * amplitudePx +
                cos(frac * freq * 1.7f - phaseShift * 0.6f) * (amplitudePx * 0.42f)
            if (i == 0) detailPath.moveTo(x, baseY + ridge) else detailPath.lineTo(x, baseY + ridge)
        }
        strokePaint.color = (ridgeColor and 0x00FFFFFF) or ((fillAlphaMix * 320f).toInt().coerceIn(38, 120) shl 24)
        strokePaint.strokeWidth = 1.2f
        canvas.drawPath(detailPath, strokePaint)
    }

    fun drawSunbeamRibbons(
        canvas: Canvas,
        waterY: Float,
        floorY: Float,
        simTime: Float,
        rayCount: Int,
        sunRayColor: Int,
        calmScrim: Boolean,
        isLightSurface: Boolean,
        isTrueBlackOled: Boolean
    ) {
        if (isTrueBlackOled) return
        val w = canvas.width.toFloat()
        val rayAlpha = when {
            calmScrim -> 14
            isLightSurface -> 26
            else -> 20
        }
        fillPaint.color = (sunRayColor and 0x00FFFFFF) or (rayAlpha shl 24)
        val depth = (floorY - waterY) * 0.85f
        for (r in 0 until rayCount) {
            val topX = w * (0.14f + r * 0.24f) + sin(simTime * 0.35f + r * 1.4f) * (w * 0.025f)
            val topW = w * (0.045f + (r % 2) * 0.018f)
            val slant = depth * 0.34f
            val botW = topW * 1.85f
            shapePath.reset()
            shapePath.moveTo(topX, waterY)
            shapePath.lineTo(topX + topW, waterY)
            shapePath.lineTo(topX + slant + botW, waterY + depth)
            shapePath.lineTo(topX + slant, waterY + depth)
            shapePath.close()
            canvas.drawPath(shapePath, fillPaint)
        }
    }

    fun drawSeabedPebbles(
        canvas: Canvas,
        textPaint: Paint,
        floorY: Float,
        parallaxPx: Float,
        pebbleColor: Int,
        waterBgDeep: Int,
        isTrueBlackOled: Boolean
    ) {
        val w = canvas.width.toFloat()
        val count = 11
        val spacing = w / count
        strokePaint.color = (pebbleColor and 0x00FFFFFF) or (155 shl 24)
        strokePaint.strokeWidth = 1.2f
        fillPaint.color = solidTint(waterBgDeep, pebbleColor, 0.32f, isTrueBlackOled)

        val prevSize = textPaint.textSize
        textPaint.textSize = (prevSize * 0.72f).coerceAtLeast(8f)
        textPaint.color = pebbleColor

        for (i in -1..count) {
            val hash = ((i * 73 + 19) and 0x7FFFFFFF)
            val px = i * spacing + (hash % 23) - parallaxPx
            val rx = spacing * (0.28f + (hash % 5) * 0.045f)
            val ry = rx * (0.36f + (hash % 3) * 0.06f)
            val py = floorY + (hash % 3) * 4f

            shapePath.reset()
            shapePath.moveTo(px - rx, py + ry * 0.3f)
            shapePath.quadTo(px - rx * 0.65f, py - ry * 1.05f, px, py - ry)
            shapePath.quadTo(px + rx * 0.75f, py - ry * 0.95f, px + rx, py + ry * 0.3f)
            shapePath.quadTo(px, py + ry * 0.72f, px - rx, py + ry * 0.3f)
            shapePath.close()
            canvas.drawPath(shapePath, fillPaint)
            canvas.drawPath(shapePath, strokePaint)
            if (i % 2 == 0) {
                drawChar(canvas, ':', px - textPaint.textSize * 0.25f, py + ry * 0.15f, textPaint)
            }
        }
        textPaint.textSize = prevSize
    }

    fun drawCoralCluster(
        canvas: Canvas,
        textPaint: Paint,
        cx: Float,
        floorY: Float,
        heightPx: Float,
        coralColor: Int,
        waterBgDeep: Int,
        isTrueBlackOled: Boolean
    ) {
        strokePaint.color = (coralColor and 0x00FFFFFF) or (205 shl 24)
        strokePaint.strokeWidth = (heightPx * 0.085f).coerceIn(2.0f, 4.8f)

        // Organic branching staghorn coral arms!
        val branches = 5
        for (b in 0 until branches) {
            val frac = (b / (branches - 1f)) - 0.5f
            val tipX = cx + frac * heightPx * 1.15f
            val tipY = floorY - heightPx * (0.72f + (1f - kotlin.math.abs(frac) * 1.1f) * 0.35f)
            shapePath.reset()
            shapePath.moveTo(cx + frac * heightPx * 0.18f, floorY)
            shapePath.quadTo(cx + frac * heightPx * 0.55f, floorY - heightPx * 0.45f, tipX, tipY)
            canvas.drawPath(shapePath, strokePaint)

            // Secondary fork twig
            if (b % 2 == 0) {
                val midX = (cx + tipX) * 0.52f
                val midY = (floorY + tipY) * 0.52f
                canvas.drawLine(midX, midY, midX + (if (frac >= 0f) 1 else -1) * heightPx * 0.22f, midY - heightPx * 0.24f, strokePaint)
            }
        }

        // Base reef rock cushion
        val baseRx = heightPx * 0.48f
        val baseRy = heightPx * 0.16f
        fillPaint.color = solidTint(waterBgDeep, coralColor, 0.36f, isTrueBlackOled)
        canvas.drawOval(cx - baseRx, floorY - baseRy, cx + baseRx, floorY + baseRy * 0.6f, fillPaint)
        strokePaint.strokeWidth = 1.3f
        canvas.drawOval(cx - baseRx, floorY - baseRy, cx + baseRx, floorY + baseRy * 0.6f, strokePaint)

        // Tiny ASCII polyp sparkles
        val prevSize = textPaint.textSize
        textPaint.textSize = (heightPx * 0.18f).coerceIn(8f, prevSize)
        textPaint.color = coralColor
        drawChar(canvas, '·', cx - heightPx * 0.26f, floorY - heightPx * 0.62f, textPaint)
        drawChar(canvas, ':', cx + heightPx * 0.14f, floorY - heightPx * 0.78f, textPaint)
        textPaint.textSize = prevSize
    }

    fun drawSeaAnemone(
        canvas: Canvas,
        cx: Float,
        floorY: Float,
        heightPx: Float,
        simTime: Float,
        oceanCurrent: Float,
        anemoneColor: Int,
        tipColor: Int,
        waterBgDeep: Int,
        isTrueBlackOled: Boolean
    ) {
        val baseW = heightPx * 0.65f
        val baseH = heightPx * 0.26f

        // 7 swaying sinuous anemone tentacles!
        strokePaint.strokeWidth = (heightPx * 0.065f).coerceIn(1.6f, 3.2f)
        strokePaint.color = (anemoneColor and 0x00FFFFFF) or (205 shl 24)
        fillPaint.color = (tipColor and 0x00FFFFFF) or (225 shl 24)

        for (t in 0 until 7) {
            val frac = (t / 6f) - 0.5f
            val bx = cx + frac * baseW * 0.78f
            val sway = (sin(simTime * 2.0f + t * 0.9f) + oceanCurrent * 0.45f) * (heightPx * 0.22f)
            val tx = cx + frac * baseW * 1.45f + sway
            val ty = floorY - heightPx * (0.78f + (1f - kotlin.math.abs(frac)) * 0.28f)
            shapePath.reset()
            shapePath.moveTo(bx, floorY - baseH * 0.5f)
            shapePath.quadTo(bx + sway * 0.5f, floorY - heightPx * 0.48f, tx, ty)
            canvas.drawPath(shapePath, strokePaint)
            canvas.drawCircle(tx, ty, strokePaint.strokeWidth * 0.68f, fillPaint)
        }

        // Rounded fleshy anemone base cup
        shapePath.reset()
        shapePath.moveTo(cx - baseW * 0.45f, floorY + 2f)
        shapePath.quadTo(cx - baseW * 0.55f, floorY - baseH, cx, floorY - baseH)
        shapePath.quadTo(cx + baseW * 0.55f, floorY - baseH, cx + baseW * 0.45f, floorY + 2f)
        shapePath.close()
        fillPaint.color = solidTint(waterBgDeep, anemoneColor, 0.45f, isTrueBlackOled)
        canvas.drawPath(shapePath, fillPaint)
        strokePaint.strokeWidth = 1.35f
        canvas.drawPath(shapePath, strokePaint)
    }

    fun drawGiantClam(
        canvas: Canvas,
        textPaint: Paint,
        cx: Float,
        floorY: Float,
        widthPx: Float,
        openFrac: Float, // 0 = closed, 1 = wide open revealing glowing pearl!
        shellColor: Int,
        pearlColor: Int,
        waterBgDeep: Int,
        isTrueBlackOled: Boolean
    ) {
        val hw = widthPx * 0.5f
        val hh = widthPx * 0.28f
        val openLift = hh * (0.15f + 0.85f * openFrac.coerceIn(0f, 1f))

        strokePaint.strokeWidth = 1.45f
        strokePaint.color = (shellColor and 0x00FFFFFF) or (210 shl 24)

        // Upper hinged fluted shell valve
        shapePath.reset()
        shapePath.moveTo(cx - hw, floorY - hh * 0.15f)
        shapePath.quadTo(cx - hw * 0.35f, floorY - hh - openLift, cx, floorY - hh * 0.95f - openLift)
        shapePath.quadTo(cx + hw * 0.35f, floorY - hh - openLift, cx + hw, floorY - hh * 0.15f)
        shapePath.quadTo(cx, floorY - openLift * 0.25f, cx - hw, floorY - hh * 0.15f)
        shapePath.close()
        fillPaint.color = solidTint(waterBgDeep, shellColor, 0.38f, isTrueBlackOled)
        canvas.drawPath(shapePath, fillPaint)
        canvas.drawPath(shapePath, strokePaint)

        // Glowing luminous pearl nestled inside when open!
        if (openFrac > 0.25f) {
            val pr = (hh * 0.36f * openFrac).coerceAtLeast(2.2f)
            fillPaint.color = (pearlColor and 0x00FFFFFF) or ((220f * openFrac).toInt().coerceIn(50, 240) shl 24)
            canvas.drawCircle(cx, floorY - hh * 0.35f, pr, fillPaint)
        }

        // Lower resting fluted shell valve
        detailPath.reset()
        detailPath.moveTo(cx - hw, floorY - hh * 0.15f)
        detailPath.quadTo(cx, floorY + hh * 0.72f, cx + hw, floorY - hh * 0.15f)
        detailPath.quadTo(cx, floorY + hh * 0.10f, cx - hw, floorY - hh * 0.15f)
        detailPath.close()
        fillPaint.color = solidTint(waterBgDeep, shellColor, 0.48f, isTrueBlackOled)
        canvas.drawPath(detailPath, fillPaint)
        canvas.drawPath(detailPath, strokePaint)

        // Subtle shell rib glyph
        val prevSize = textPaint.textSize
        textPaint.textSize = (hh * 0.65f).coerceIn(7f, prevSize)
        textPaint.color = shellColor
        drawChar(canvas, '=', cx - textPaint.textSize * 0.28f, floorY + hh * 0.22f, textPaint)
        textPaint.textSize = prevSize
    }

    fun drawStoneLantern(
        canvas: Canvas,
        textPaint: Paint,
        cx: Float,
        floorY: Float,
        heightPx: Float,
        stoneColor: Int,
        glowColor: Int,
        waterBgDeep: Int,
        isLowBatteryBlink: Boolean,
        isTrueBlackOled: Boolean,
        pulseFrac: Float = 0f,
        isCharging: Boolean = false,
        simTime: Float = 0f
    ) {
        val hw = heightPx * 0.44f
        val roofY = floorY - heightPx * 0.72f
        val boxTopY = roofY + heightPx * 0.06f
        val boxBotY = floorY - heightPx * 0.32f
        val chamberCy = (boxTopY + boxBotY) * 0.5f

        strokePaint.strokeWidth = 1.35f
        strokePaint.color = (stoneColor and 0x00FFFFFF) or (205 shl 24)

        // Soft glowing underwater lantern aura (breathes warmly while device is charging!)
        if (!isLowBatteryBlink) {
            val chargeBreathe = if (isCharging) (0.5f + 0.5f * sin(simTime * 2.4f)) else 0f
            if (!isTrueBlackOled) {
                val auraAlpha = if (isCharging) (42 + (chargeBreathe * 34f).toInt()) else 34
                val auraRadius = heightPx * (if (isCharging) (0.62f + chargeBreathe * 0.26f) else 0.56f)
                fillPaint.color = (glowColor and 0x00FFFFFF) or (auraAlpha shl 24)
                canvas.drawCircle(cx, chamberCy, auraRadius, fillPaint)
            } else if (isCharging) {
                strokePaint.color = (glowColor and 0x00FFFFFF) or ((75 + (chargeBreathe * 65f).toInt()) shl 24)
                strokePaint.strokeWidth = 1.15f
                canvas.drawCircle(cx, chamberCy, heightPx * (0.42f + chargeBreathe * 0.22f), strokePaint)
                strokePaint.color = (stoneColor and 0x00FFFFFF) or (205 shl 24)
                strokePaint.strokeWidth = 1.35f
            }
        }

        // Expanding golden underwater pulse ring when tapped!
        if (pulseFrac > 0.01f) {
            val progress = 1f - pulseFrac.coerceIn(0f, 1f)
            val ringR = heightPx * (0.34f + progress * 1.45f)
            val ringAlpha = (pulseFrac * 195f).toInt().coerceIn(0, 200)
            strokePaint.color = (glowColor and 0x00FFFFFF) or (ringAlpha shl 24)
            strokePaint.strokeWidth = (1.8f * pulseFrac).coerceAtLeast(0.8f)
            canvas.drawCircle(cx, chamberCy, ringR, strokePaint)
            strokePaint.color = (stoneColor and 0x00FFFFFF) or (205 shl 24)
            strokePaint.strokeWidth = 1.35f
        }

        // Pedestal base
        shapePath.reset()
        shapePath.moveTo(cx - hw * 0.85f, floorY + 2f)
        shapePath.lineTo(cx - hw * 0.42f, boxBotY)
        shapePath.lineTo(cx + hw * 0.42f, boxBotY)
        shapePath.lineTo(cx + hw * 0.85f, floorY + 2f)
        shapePath.close()
        fillPaint.color = solidTint(waterBgDeep, stoneColor, 0.40f, isTrueBlackOled)
        canvas.drawPath(shapePath, fillPaint)
        canvas.drawPath(shapePath, strokePaint)

        // Glowing firebox chamber
        val boxHw = hw * 0.48f
        fillPaint.color = solidTint(waterBgDeep, glowColor, if (isLowBatteryBlink) 0.18f else 0.52f, isTrueBlackOled)
        canvas.drawRect(cx - boxHw, boxTopY, cx + boxHw, boxBotY, fillPaint)
        strokePaint.color = (glowColor and 0x00FFFFFF) or (215 shl 24)
        canvas.drawRect(cx - boxHw, boxTopY, cx + boxHw, boxBotY, strokePaint)

        // Curved Japanese Toro pagoda roof cap!
        strokePaint.color = (stoneColor and 0x00FFFFFF) or (210 shl 24)
        detailPath.reset()
        detailPath.moveTo(cx - hw * 1.05f, roofY + heightPx * 0.08f)
        detailPath.quadTo(cx - hw * 0.35f, roofY - heightPx * 0.04f, cx, roofY - heightPx * 0.28f)
        detailPath.quadTo(cx + hw * 0.35f, roofY - heightPx * 0.04f, cx + hw * 1.05f, roofY + heightPx * 0.08f)
        detailPath.close()
        fillPaint.color = solidTint(waterBgDeep, stoneColor, 0.46f, isTrueBlackOled)
        canvas.drawPath(detailPath, fillPaint)
        canvas.drawPath(detailPath, strokePaint)

        // Crisp monospace core indicator inside lantern chamber
        val prevSize = textPaint.textSize
        textPaint.textSize = (heightPx * 0.22f).coerceIn(8f, prevSize)
        textPaint.color = glowColor
        drawChar(canvas, if (isLowBatteryBlink) '!' else '◉', cx - textPaint.textSize * 0.30f, (boxTopY + boxBotY) * 0.5f + textPaint.textSize * 0.34f, textPaint)
        textPaint.textSize = prevSize
    }

    fun drawKelpRibbon(
        canvas: Canvas,
        textPaint: Paint,
        baseX: Float,
        rootY: Float,
        heightPx: Float,
        cellWidthPx: Float,
        simTime: Float,
        phase: Float,
        bendOffsetCols: Float,
        kelpColor: Int,
        waterBgDeep: Int,
        isBatterySaver: Boolean,
        isTrueBlackOled: Boolean
    ) {
        val segs = if (isBatterySaver) 6 else 9
        val ribbonHalfW = (cellWidthPx * 0.42f).coerceIn(3.5f, 7.5f)

        shapePath.reset()
        shapePath.moveTo(baseX - ribbonHalfW * 0.55f, rootY)

        // Left edge going up
        for (i in 1..segs) {
            val frac = i / segs.toFloat()
            val sway = sin(simTime * 1.25f + phase - i * 0.52f) * (0.55f + frac * 1.15f) * cellWidthPx
            val bend = bendOffsetCols * frac * frac * cellWidthPx
            val w = ribbonHalfW * (1f - frac * 0.65f) * (0.75f + 0.35f * sin(i * 1.7f + phase))
            shapePath.lineTo(baseX + sway + bend - w, rootY - frac * heightPx)
        }
        // Right edge coming back down
        for (i in segs downTo 0) {
            val frac = i / segs.toFloat()
            val sway = sin(simTime * 1.25f + phase - i * 0.52f) * (0.55f + frac * 1.15f) * cellWidthPx
            val bend = bendOffsetCols * frac * frac * cellWidthPx
            val w = ribbonHalfW * (1f - frac * 0.65f) * (0.75f + 0.35f * sin(i * 1.7f + phase))
            shapePath.lineTo(baseX + sway + bend + w, rootY - frac * heightPx)
        }
        shapePath.close()

        fillPaint.color = solidTint(waterBgDeep, kelpColor, 0.36f, isTrueBlackOled)
        canvas.drawPath(shapePath, fillPaint)
        strokePaint.color = (kelpColor and 0x00FFFFFF) or (185 shl 24)
        strokePaint.strokeWidth = 1.25f
        canvas.drawPath(shapePath, strokePaint)

        // Delicate monospace '~' leaf texture along the swaying kelp ribbon
        val prevSize = textPaint.textSize
        textPaint.textSize = (cellWidthPx * 0.92f).coerceIn(8f, prevSize * 0.82f)
        textPaint.color = kelpColor
        for (i in 2 until segs step 3) {
            val frac = i / segs.toFloat()
            val sway = sin(simTime * 1.25f + phase - i * 0.52f) * (0.55f + frac * 1.15f) * cellWidthPx
            val bend = bendOffsetCols * frac * frac * cellWidthPx
            drawChar(canvas, if (i % 2 == 0) '~' else ':', baseX + sway + bend - textPaint.textSize * 0.28f, rootY - frac * heightPx, textPaint)
        }
        textPaint.textSize = prevSize
    }

    fun drawFoodPellet(
        canvas: Canvas,
        textPaint: Paint,
        cx: Float,
        cy: Float,
        radiusPx: Float,
        isAlgaeWafer: Boolean,
        color: Int,
        waterBgDeep: Int,
        isTrueBlackOled: Boolean
    ) {
        if (isAlgaeWafer) {
            fillPaint.color = solidTint(waterBgDeep, color, 0.58f, isTrueBlackOled)
            canvas.drawOval(cx - radiusPx * 1.45f, cy - radiusPx * 0.72f, cx + radiusPx * 1.45f, cy + radiusPx * 0.72f, fillPaint)
            strokePaint.color = (color and 0x00FFFFFF) or (225 shl 24)
            strokePaint.strokeWidth = 1.3f
            canvas.drawOval(cx - radiusPx * 1.45f, cy - radiusPx * 0.72f, cx + radiusPx * 1.45f, cy + radiusPx * 0.72f, strokePaint)
            val prevSize = textPaint.textSize
            textPaint.textSize = (radiusPx * 1.55f).coerceIn(7f, prevSize)
            textPaint.color = color
            drawChar(canvas, '@', cx - textPaint.textSize * 0.30f, cy + textPaint.textSize * 0.32f, textPaint)
            textPaint.textSize = prevSize
        } else {
            fillPaint.color = (color and 0x00FFFFFF) or (225 shl 24)
            canvas.drawCircle(cx, cy, radiusPx, fillPaint)
        }
    }

    // =========================================================================
    // 5. SCULPTED WEATHER (AURORA CURTAINS, RAIN SPLASHES, SNOW) & WATER REALISM
    // =========================================================================

    fun drawAuroraCurtains(
        canvas: Canvas,
        textPaint: Paint,
        waterY: Float,
        simTime: Float,
        colorA: Int,
        colorB: Int,
        colorC: Int,
        calmScrim: Boolean,
        isBatterySaver: Boolean,
        isTrueBlackOled: Boolean
    ) {
        val w = canvas.width.toFloat()
        val skyH = waterY.coerceAtLeast(80f)
        val layers = if (isBatterySaver) 2 else 3
        val segs = 16

        for (layer in 0 until layers) {
            val baseY = skyH * (0.26f + layer * 0.14f)
            val bandH = skyH * (0.16f - layer * 0.02f)
            val col = when (layer) {
                0 -> colorA
                1 -> colorB
                else -> colorC
            }
            val speed = 0.85f + layer * 0.25f
            val freq = 3.8f + layer * 1.4f

            shapePath.reset()
            for (i in 0..segs) {
                val frac = i / segs.toFloat()
                val x = frac * w
                val wave = sin(frac * freq + simTime * speed + layer * 1.9f) * (skyH * 0.07f) +
                    cos(frac * freq * 1.6f - simTime * 0.55f) * (skyH * 0.03f)
                val y = (baseY + wave).coerceIn(12f, waterY - 24f)
                if (i == 0) shapePath.moveTo(x, y) else shapePath.lineTo(x, y)
            }
            if (!isTrueBlackOled) {
                for (i in segs downTo 0) {
                    val frac = i / segs.toFloat()
                    val x = frac * w
                    val wave = sin(frac * freq + simTime * speed + layer * 1.9f) * (skyH * 0.07f) +
                        cos(frac * freq * 1.6f - simTime * 0.55f) * (skyH * 0.03f)
                    val drop = bandH * (0.65f + 0.35f * sin(frac * 6.5f + simTime * 1.1f + layer))
                    val y = (baseY + wave + drop).coerceIn(16f, waterY - 12f)
                    shapePath.lineTo(x, y)
                }
                shapePath.close()
                val fillAlpha = if (calmScrim) 18 else (32 - layer * 5).coerceAtLeast(16)
                fillPaint.color = (col and 0x00FFFFFF) or (fillAlpha shl 24)
                canvas.drawPath(shapePath, fillPaint)
            }

            // Luminous flowing upper aurora edge + delicate ASCII shimmer
            detailPath.reset()
            for (i in 0..segs) {
                val frac = i / segs.toFloat()
                val x = frac * w
                val wave = sin(frac * freq + simTime * speed + layer * 1.9f) * (skyH * 0.07f) +
                    cos(frac * freq * 1.6f - simTime * 0.55f) * (skyH * 0.03f)
                val y = (baseY + wave).coerceIn(12f, waterY - 24f)
                if (i == 0) detailPath.moveTo(x, y) else detailPath.lineTo(x, y)
            }
            val lineAlpha = if (calmScrim) 70 else 135
            strokePaint.color = (col and 0x00FFFFFF) or (lineAlpha shl 24)
            strokePaint.strokeWidth = 1.4f
            canvas.drawPath(detailPath, strokePaint)

            if (layer == 0 && !isBatterySaver) {
                val prevSize = textPaint.textSize
                textPaint.textSize = (prevSize * 0.72f).coerceAtLeast(8f)
                textPaint.color = (col and 0x00FFFFFF) or ((lineAlpha + 15).coerceAtMost(180) shl 24)
                for (i in 2 until segs step 4) {
                    val frac = i / segs.toFloat()
                    val x = frac * w
                    val wave = sin(frac * freq + simTime * speed) * (skyH * 0.07f)
                    drawChar(canvas, if (i % 8 == 2) ':' else '·', x, baseY + wave + bandH * 0.45f, textPaint)
                }
                textPaint.textSize = prevSize
            }
        }
    }

    fun drawRainDrop(
        canvas: Canvas,
        x: Float,
        y: Float,
        lengthPx: Float,
        color: Int
    ) {
        strokePaint.color = (color and 0x00FFFFFF) or (155 shl 24)
        strokePaint.strokeWidth = 1.25f
        canvas.drawLine(x, y, x - lengthPx * 0.22f, y + lengthPx, strokePaint)
    }

    fun drawSnowFlake(
        canvas: Canvas,
        textPaint: Paint,
        x: Float,
        y: Float,
        radiusPx: Float,
        useGlyph: Boolean,
        color: Int
    ) {
        if (useGlyph) {
            val prevSize = textPaint.textSize
            textPaint.textSize = (radiusPx * 3.2f).coerceIn(8f, prevSize * 0.85f)
            textPaint.color = (color and 0x00FFFFFF) or (185 shl 24)
            drawChar(canvas, '*', x - textPaint.textSize * 0.28f, y + textPaint.textSize * 0.30f, textPaint)
            textPaint.textSize = prevSize
        } else {
            fillPaint.color = (color and 0x00FFFFFF) or (180 shl 24)
            canvas.drawCircle(x, y, radiusPx, fillPaint)
        }
    }

    fun drawRainSplashCrown(
        canvas: Canvas,
        cx: Float,
        waterY: Float,
        lifeFrac: Float, // 0 = impact, 1 = faded
        maxRadiusPx: Float,
        color: Int
    ) {
        val alpha = ((1f - lifeFrac) * 185f).toInt().coerceIn(0, 195)
        if (alpha <= 5) return
        val r = maxRadiusPx * (0.25f + 0.75f * lifeFrac)
        val archH = maxRadiusPx * 0.55f * sin(lifeFrac * PI.toFloat()).coerceAtLeast(0f)

        strokePaint.color = (color and 0x00FFFFFF) or (alpha shl 24)
        strokePaint.strokeWidth = 1.2f
        // Expanding water ripple ring on meniscus
        canvas.drawOval(cx - r, waterY - r * 0.22f, cx + r, waterY + r * 0.22f, strokePaint)
        // Delicate leaping twin water-crown droplets
        if (archH > 1.2f) {
            canvas.drawLine(cx - r * 0.35f, waterY, cx - r * 0.58f, waterY - archH, strokePaint)
            canvas.drawLine(cx + r * 0.35f, waterY, cx + r * 0.58f, waterY - archH, strokePaint)
        }
    }

    fun drawSeabedCaustics(
        canvas: Canvas,
        floorY: Float,
        refPx: Float,
        simTime: Float,
        parallaxPx: Float,
        sunRayColor: Int,
        calmScrim: Boolean,
        isLightSurface: Boolean,
        isRaining: Boolean,
        isTrueBlackOled: Boolean
    ) {
        if (isTrueBlackOled) return
        val w = canvas.width.toFloat()
        val baseAlpha = when {
            isRaining -> 12
            calmScrim -> 16
            isLightSurface -> 34
            else -> 26
        }
        strokePaint.color = (sunRayColor and 0x00FFFFFF) or (baseAlpha shl 24)
        strokePaint.strokeWidth = 1.5f
        val count = 7
        val span = w / count
        for (i in 0 until count) {
            val phase = simTime * 1.25f + i * 1.35f
            val cx = (i + 0.5f) * span + sin(phase * 0.7f) * (span * 0.22f) - parallaxPx * 0.45f
            val cy = floorY - refPx * (0.015f + (i % 3) * 0.014f)
            val rx = refPx * (0.042f + 0.014f * sin(phase))
            val ry = rx * 0.24f
            canvas.drawOval(cx - rx, cy - ry, cx + rx, cy + ry, strokePaint)
        }
    }

    fun drawSurfaceReflections(
        canvas: Canvas,
        waterY: Float,
        celX: Float,
        celColor: Int,
        lhX: Float,
        lhBeaconOn: Boolean,
        lhColor: Int,
        refPx: Float,
        simTime: Float,
        calmScrim: Boolean,
        isTrueBlackOled: Boolean
    ) {
        if (isTrueBlackOled) return
        // Soft inverted shimmer bands beneath Sun / Moon
        val maxAlpha = if (calmScrim) 28 else 52
        for (band in 0 until 5) {
            val frac = band / 4f
            val y = waterY + 6f + band * (refPx * 0.013f)
            val halfW = refPx * (0.045f * (1f - frac * 0.58f)) * (0.85f + 0.22f * sin(simTime * 2.6f + band * 1.4f))
            val driftX = sin(simTime * 2.1f + band * 1.7f) * (refPx * 0.008f)
            val a = ((1f - frac * 0.72f) * maxAlpha).toInt().coerceIn(8, 65)
            fillPaint.color = (celColor and 0x00FFFFFF) or (a shl 24)
            canvas.drawOval(celX + driftX - halfW, y - 2.2f, celX + driftX + halfW, y + 2.2f, fillPaint)
        }

        // Inverted golden lighthouse beacon reflection dancing on dark water at dusk/night!
        if (lhBeaconOn) {
            for (band in 0 until 3) {
                val frac = band / 2f
                val y = waterY + 7f + band * (refPx * 0.012f)
                val halfW = refPx * (0.026f * (1f - frac * 0.45f))
                val driftX = cos(simTime * 3.1f + band) * 3.5f
                val a = ((1f - frac * 0.6f) * (if (calmScrim) 26 else 48)).toInt().coerceIn(8, 56)
                fillPaint.color = (lhColor and 0x00FFFFFF) or (a shl 24)
                canvas.drawOval(lhX + driftX - halfW, y - 1.8f, lhX + driftX + halfW, y + 1.8f, fillPaint)
            }
        }
    }

    /**
     * Sculpted soft elliptical seabed drop shadow cast by fish, sharks, whales, orcas, rays & legendaries!
     * Replaces legacy ASCII `.===.` / `...` shadow text while keeping AMOLED pure black.
     */
    fun drawCreatureShadow(
        canvas: Canvas,
        cx: Float,
        shadowY: Float,
        halfWidthPx: Float,
        shadowColor: Int,
        waterBgDeep: Int,
        alphaMix: Float,
        isTrueBlackOled: Boolean
    ) {
        if (isTrueBlackOled) return
        val clampedMix = alphaMix.coerceIn(0f, 1f)
        if (clampedMix <= 0.04f || halfWidthPx < 3f) return
        val rx = halfWidthPx.coerceAtLeast(4f)
        val ry = (rx * 0.19f).coerceIn(2.2f, 16f)
        val tintedShadow = TimeOfDayPalette.lerpColor(waterBgDeep, shadowColor, 0.72f)

        // Outer soft penumbra
        val outerAlpha = (clampedMix * 56f).toInt().coerceIn(8, 68)
        fillPaint.color = (tintedShadow and 0x00FFFFFF) or (outerAlpha shl 24)
        canvas.drawOval(cx - rx * 1.18f, shadowY - ry * 1.15f, cx + rx * 1.18f, shadowY + ry * 1.15f, fillPaint)

        // Inner darker umbra core
        val innerAlpha = (clampedMix * 88f).toInt().coerceIn(12, 108)
        fillPaint.color = (tintedShadow and 0x00FFFFFF) or (innerAlpha shl 24)
        canvas.drawOval(cx - rx * 0.72f, shadowY - ry * 0.68f, cx + rx * 0.72f, shadowY + ry * 0.68f, fillPaint)
    }

    /**
     * Soft wave-shimmering underside surface reflection cast by fish & dolphins swimming near the meniscus!
     * Replaces legacy ASCII `~..~` reflection text.
     */
    fun drawCreatureSurfaceReflection(
        canvas: Canvas,
        cx: Float,
        reflectY: Float,
        halfWidthPx: Float,
        bodyColor: Int,
        waterBgTop: Int,
        alphaMix: Float,
        simTime: Float,
        isTrueBlackOled: Boolean
    ) {
        if (isTrueBlackOled) return
        val clampedMix = alphaMix.coerceIn(0f, 1f)
        if (clampedMix <= 0.04f || halfWidthPx < 3f) return
        val rx = (halfWidthPx * 0.85f).coerceAtLeast(4f)
        val ry = (rx * 0.16f).coerceIn(1.8f, 7.5f)
        val shimmerCol = TimeOfDayPalette.lerpColor(waterBgTop, bodyColor, 0.65f)
        val drift1 = sin(simTime * 2.8f + cx * 0.04f) * (rx * 0.12f)
        val drift2 = cos(simTime * 2.3f + cx * 0.05f) * (rx * 0.15f)

        val a1 = (clampedMix * 68f).toInt().coerceIn(8, 76)
        fillPaint.color = (shimmerCol and 0x00FFFFFF) or (a1 shl 24)
        canvas.drawOval(cx + drift1 - rx, reflectY - ry, cx + drift1 + rx, reflectY + ry, fillPaint)

        val a2 = (clampedMix * 42f).toInt().coerceIn(6, 48)
        fillPaint.color = (shimmerCol and 0x00FFFFFF) or (a2 shl 24)
        canvas.drawOval(cx + drift2 - rx * 0.68f, reflectY + ry * 1.4f, cx + drift2 + rx * 0.68f, reflectY + ry * 2.4f, fillPaint)
    }
}

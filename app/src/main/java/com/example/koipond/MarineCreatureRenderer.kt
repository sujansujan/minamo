package com.example.koipond

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin

enum class BodyPlan {
    BAITFISH,      // Tiny sleek schooling darts (sardines, chromis, anthias)
    REEF_OVAL,     // Plump rounded reef fish (clownfish, tang, mandarin, koi)
    REEF_TALL,     // Tall diamond/banner reef fish (moorish idol, emperor angel)
    PUFFER,        // Round chubby blowfish
    LIONFISH,      // Radiating feathery fin spines
    PELAGIC_FAST,  // Torpedo swimmers with sickle forked tail (tuna, dorado, barracuda)
    TURTLE,        // Domed carapace + neck/head + sweeping wing-like flippers
    RAY,           // Sweeping diamond wings + trailing whip tail (eagle ray, giant manta)
    DOLPHIN,       // Curved rostrum, melon forehead, curved dorsal fin, horizontal flukes
    WHALE,         // Colossal humpback whale (long pectoral fin, pleated throat, broad flukes)
    ORCA,          // Sleek whale + tall iconic dorsal fin + bold eye/saddle patches
    SHARK,         // Pointed/broad snout, dual dorsals, asymmetric heterocercal tail (reef shark, whale shark)
    SWORDFISH,     // Sleek billfish with long anterior sword + tall sail dorsal
    EEL,           // Sinuous multi-wave slithering moray eel / oarfish
    SQUID,         // Torpedo mantle + side fins + trailing undulating tentacles
    JELLYFISH,     // Translucent pulsing dome bell + trailing delicate ASCII tentacles
    CRAB,          // Scurrying reef crab with domed carapace, jointed legs & raised pincers
    SHRIMP,        // Arched cleaner shrimp with long sweeping antennae & fan tail
    GOBY,          // Flat-bellied sand goby / bottom catfish with whisker barbels
    CONCH          // Coiled spiral sea conch with gliding foot & eye-stalks
}

enum class MarkingStyle {
    NONE,
    CLOWN_BANDS,   // 3 bold vertical light bars
    IDOL_BANDS,    // 2 dramatic vertical dark/accent bars + long dorsal pennant
    ANGEL_STRIPES, // Fine horizontal pinstripes
    KOI_PATCHES,   // Organic saddle patches
    SPOTS,         // Constellation of pale dots (puffer, eagle ray, whale shark)
    TANG_MASK,     // Bright tail accent + dorsal highlight
    ORCA_PATCHES,  // Iconic white eye patch + dorsal saddle + light belly
    WHALE_PLEATS,  // Ventral throat grooves + dappled back
    TURTLE_SCUTES, // Mosaic carapace plates
    MANDARIN_SWIRL // Wavy bands
}

/**
 * Zero-allocation Hybrid Organic Silhouette + Monospace ASCII-Dither Renderer.
 * - Every creature scales from true screen-width proportions (`lengthPx = canvasWidth * lengthFrac * perspScale`)
 *   independent of background character grid density (`68c / 82c / 96c`).
 * - Renders a whisper-soft translucent anatomical contour flexing along a real traveling S-wave spine,
 *   paired with crisp JetBrains Mono ASCII dither glyphs (`# = ~ : . · | / \ °`) following the exact body curve.
 * - On true-black AMOLED night mode, switches silhouette fill off and uses crisp hairline + ASCII glyphs
 *   so 90%+ of OLED pixels remain completely turned off.
 */
class MarineCreatureRenderer {

    private val bodyPath = Path()
    private val finPath = Path()
    private val accentPath = Path()

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val singleCharBuf = CharArray(1)

    // Pre-allocated spine contour scratch buffers (16 spine stations from nose s=0 to tail s=1)
    private val spineCount = 16
    private val spineX = FloatArray(spineCount)
    private val spineY = FloatArray(spineCount)
    private val upperX = FloatArray(spineCount)
    private val upperY = FloatArray(spineCount)
    private val lowerX = FloatArray(spineCount)
    private val lowerY = FloatArray(spineCount)
    private val clownBandStations = intArrayOf(3, 6, 9)
    private val idolBandStations = intArrayOf(4, 8)
    private val mandarinSwirlStations = intArrayOf(4, 7, 10)

    fun drawCreature(
        canvas: Canvas,
        textPaint: Paint,
        baseTextSizePx: Float,
        centerX: Float,
        centerY: Float,
        lengthPx: Float,
        aspectRatio: Float,
        plan: BodyPlan,
        marking: MarkingStyle,
        bodyColor: Int,
        accentColor: Int,
        eyeColor: Int,
        localWaterBg: Int,
        fogFactor: Float,
        inSunbeam: Boolean,
        sunbeamHighlight: Int,
        isLightSurface: Boolean,
        isTrueBlackOled: Boolean,
        isBatterySaver: Boolean,
        facingRight: Boolean,
        cosYaw: Float,
        sinYaw: Float,
        pitchDeg: Float,
        animPhase: Float,
        isChomping: Boolean = false,
        waterSurfacePx: Float = -1f,
        allowAboveWater: Boolean = false
    ) {
        val halfL = lengthPx * 0.5f
        val halfH = (lengthPx * aspectRatio * 0.5f).coerceAtLeast(3f)

        // Early frustum cull (zero work when off-screen!)
        val margin = halfL * 1.45f + 16f
        if (centerX + margin < 0f || centerX - margin > canvas.width) return
        if (centerY + halfH * 2.6f < 0f || centerY - halfH * 2.6f > canvas.height) return
        if (waterSurfacePx > 0f && !allowAboveWater && centerY + halfH * 2.2f < waterSurfacePx) return

        val litBody = if (inSunbeam) {
            TimeOfDayPalette.lerpColor(bodyColor, sunbeamHighlight, if (isLightSurface) 0.24f else 0.34f)
        } else {
            bodyColor
        }
        val litAccent = if (inSunbeam) {
            TimeOfDayPalette.lerpColor(accentColor, sunbeamHighlight, if (isLightSurface) 0.28f else 0.42f)
        } else {
            accentColor
        }
        val foggedBody = TimeOfDayPalette.lerpColor(litBody, localWaterBg, fogFactor)
        val foggedAccent = TimeOfDayPalette.lerpColor(litAccent, localWaterBg, fogFactor)
        val foggedEye = TimeOfDayPalette.lerpColor(eyeColor, localWaterBg, fogFactor * 0.65f)
        val bellyColor = TimeOfDayPalette.lerpColor(foggedBody, foggedEye, 0.32f)

        // Seamless 3D horizontal foreshortening & approach/recede perspective blend (zero popping!)
        val absCos = abs(cosYaw)
        val isDepthOriented = plan != BodyPlan.JELLYFISH && plan != BodyPlan.CRAB && plan != BodyPlan.CONCH
        val approachFactor = if (isDepthOriented && sinYaw < 0f) ((0.62f - absCos) / 0.62f).coerceIn(0f, 1f) else 0f
        val recedeFactor = if (isDepthOriented && sinYaw > 0f) ((0.62f - absCos) / 0.62f).coerceIn(0f, 1f) else 0f
        val foreshortenX = if (isDepthOriented) {
            (0.38f + 0.62f * absCos.pow(0.72f)).coerceIn(0.38f, 1.0f)
        } else {
            1f
        }
        val dirSign = if (facingRight) 1f else -1f

        canvas.save()
        if (waterSurfacePx > 0f && !allowAboveWater) {
            canvas.clipRect(0f, waterSurfacePx, canvas.width.toFloat(), canvas.height.toFloat())
        }
        canvas.translate(centerX, centerY)
        if (abs(pitchDeg) > 0.3f) {
            canvas.rotate(pitchDeg)
        }
        if (plan != BodyPlan.JELLYFISH && plan != BodyPlan.CRAB) {
            canvas.scale(dirSign * foreshortenX, 1f)
        }

        // Solid opaque occlusion fill blended gently into water tone:
        // - Keeps Minamo's refined soft aesthetic against water
        // - Guarantees 100% clean depth occlusion (closer fish cleanly hide deeper fish behind them!)
        // - On true-black AMOLED, fills with opaque #FF000000 so covered deep fish stay hidden while OLED stays off!
        val bodyRichness = if (isLightSurface) 0.44f else 0.36f
        val finRichness = if (isLightSurface) 0.34f else 0.28f
        val solidBodyFill = if (isTrueBlackOled) {
            0xFF000000.toInt()
        } else {
            TimeOfDayPalette.lerpColor(localWaterBg, foggedBody, bodyRichness * (1f - fogFactor * 0.20f)) or (0xFF shl 24)
        }
        val solidFinFill = if (isTrueBlackOled) {
            0xFF000000.toInt()
        } else {
            TimeOfDayPalette.lerpColor(localWaterBg, foggedAccent, finRichness * (1f - fogFactor * 0.20f)) or (0xFF shl 24)
        }
        val jellyAlpha = when {
            isTrueBlackOled -> 0
            isLightSurface -> ((96f * (1f - fogFactor * 0.40f)).toInt()).coerceIn(38, 108)
            else -> ((82f * (1f - fogFactor * 0.45f)).toInt()).coerceIn(30, 92)
        }
        val markAlpha = when {
            isTrueBlackOled -> 0
            isLightSurface -> ((135f * (1f - fogFactor * 0.35f)).toInt()).coerceIn(48, 150)
            else -> ((120f * (1f - fogFactor * 0.40f)).toInt()).coerceIn(40, 136)
        }
        val outlineAlpha = ((205f * (1f - fogFactor * 0.45f)).toInt()).coerceIn(70, 225)

        // Micro-ASCII text size scales proportionally to the creature (stays crisp & terminal-like!)
        val glyphSize = (lengthPx * when (plan) {
            BodyPlan.BAITFISH -> 0.22f
            BodyPlan.WHALE, BodyPlan.ORCA -> 0.048f
            BodyPlan.SHARK -> 0.068f
            BodyPlan.RAY -> 0.082f
            BodyPlan.DOLPHIN, BodyPlan.TURTLE -> 0.092f
            BodyPlan.CRAB, BodyPlan.SHRIMP, BodyPlan.CONCH -> 0.165f
            else -> 0.135f
        }).coerceIn(baseTextSizePx * 0.46f, baseTextSizePx * 1.15f)
        textPaint.textSize = glyphSize
        val glyphHalfH = glyphSize * 0.34f

        when (plan) {
            BodyPlan.JELLYFISH -> drawJellyfish(
                canvas, textPaint, halfL, halfH, foggedBody, foggedAccent, foggedEye,
                jellyAlpha, outlineAlpha, animPhase, glyphSize, isBatterySaver
            )
            BodyPlan.TURTLE -> drawSeaTurtle(
                canvas, textPaint, lengthPx, halfH, foggedBody, foggedAccent, foggedEye,
                solidBodyFill, solidFinFill, outlineAlpha, animPhase, glyphSize, glyphHalfH,
                approachFactor, recedeFactor, isBatterySaver
            )
            BodyPlan.RAY -> drawRay(
                canvas, textPaint, lengthPx, halfH, foggedBody, foggedAccent, foggedEye,
                marking, solidBodyFill, outlineAlpha, animPhase, glyphSize, glyphHalfH,
                approachFactor, recedeFactor, isBatterySaver
            )
            BodyPlan.SQUID -> drawSquid(
                canvas, textPaint, lengthPx, halfH, foggedBody, foggedAccent, foggedEye,
                solidBodyFill, outlineAlpha, animPhase, glyphSize, glyphHalfH, isBatterySaver
            )
            BodyPlan.CRAB -> drawCrab(
                canvas, textPaint, lengthPx, halfH, foggedBody, foggedAccent, foggedEye,
                solidBodyFill, solidFinFill, outlineAlpha, animPhase, glyphSize, glyphHalfH,
                isChomping, isBatterySaver
            )
            BodyPlan.SHRIMP -> drawShrimp(
                canvas, textPaint, lengthPx, halfH, foggedBody, foggedAccent, foggedEye,
                solidBodyFill, solidFinFill, outlineAlpha, animPhase, glyphSize, glyphHalfH,
                isBatterySaver
            )
            BodyPlan.CONCH -> drawConch(
                canvas, textPaint, lengthPx, halfH, foggedBody, foggedAccent, foggedEye,
                solidBodyFill, solidFinFill, outlineAlpha, animPhase, glyphSize, glyphHalfH,
                isBatterySaver
            )
            else -> drawSpinedSwimmer(
                canvas, textPaint, lengthPx, halfH, plan, marking,
                foggedBody, foggedAccent, bellyColor, foggedEye,
                solidBodyFill, solidFinFill, markAlpha, outlineAlpha,
                animPhase, glyphSize, glyphHalfH,
                approachFactor, recedeFactor,
                isChomping, isBatterySaver
            )
        }

        canvas.restore()
        textPaint.textSize = baseTextSizePx
    }

    /**
     * Draws any spined ocean swimmer (baitfish, reef fish, tuna, dolphin, orca, humpback whale,
     * shark, whale shark, swordfish, lionfish, puffer, eel, sand goby) along a real traveling S-wave spine!
     * Local coordinate frame: nose is at +halfL (right), tail is at -halfL (left).
     */
    private fun drawSpinedSwimmer(
        canvas: Canvas,
        textPaint: Paint,
        lengthPx: Float,
        halfH: Float,
        plan: BodyPlan,
        marking: MarkingStyle,
        bodyColor: Int,
        accentColor: Int,
        bellyColor: Int,
        eyeColor: Int,
        solidBodyFill: Int,
        solidFinFill: Int,
        markAlpha: Int,
        outlineAlpha: Int,
        animPhase: Float,
        glyphSize: Float,
        glyphHalfH: Float,
        approachFactor: Float,
        recedeFactor: Float,
        isChomping: Boolean,
        isBatterySaver: Boolean
    ) {
        val halfL = lengthPx * 0.5f
        val waveFreq = when (plan) {
            BodyPlan.EEL -> 8.2f
            BodyPlan.WHALE -> 3.0f
            BodyPlan.DOLPHIN, BodyPlan.ORCA -> 3.6f
            BodyPlan.PUFFER, BodyPlan.GOBY -> 2.8f
            else -> 4.2f
        }
        val waveAmp = when (plan) {
            BodyPlan.EEL -> halfH * 1.15f
            BodyPlan.WHALE -> halfH * 0.36f
            BodyPlan.DOLPHIN, BodyPlan.ORCA -> halfH * 0.44f
            BodyPlan.PUFFER -> halfH * 0.14f
            BodyPlan.GOBY -> halfH * 0.22f
            BodyPlan.BAITFISH -> halfH * 0.42f
            else -> halfH * 0.36f
        }

        // 1. Evaluate smooth undulating spine and upper/lower anatomical profile
        val tailStart = when (plan) {
            BodyPlan.EEL -> 0.94f
            BodyPlan.WHALE, BodyPlan.ORCA, BodyPlan.DOLPHIN -> 0.80f
            BodyPlan.PUFFER -> 0.78f
            else -> 0.74f
        }

        for (i in 0 until spineCount) {
            val s = i.toFloat() / (spineCount - 1).toFloat() // 0 = nose, 1 = tail tip
            val x = halfL - s * lengthPx
            // Head stays steady while mid-body & tail flex smoothly along a real traveling swim wave!
            val flexWeight = if (plan == BodyPlan.EEL) (0.15f + 0.85f * s) else (s * s)
            val y = sin(animPhase - s * waveFreq) * flexWeight * waveAmp
            spineX[i] = x
            spineY[i] = y
            writeProfileStation(i, plan, s, tailStart, halfH, x, y)
        }

        // 2. Build organic closed body silhouette Path (smooth snout + tailored crescent/fan tail!)
        bodyPath.reset()
        val last = spineCount - 1
        bodyPath.moveTo(spineX[0], spineY[0])
        bodyPath.quadTo(spineX[0], upperY[1], (spineX[0] + upperX[1]) * 0.5f, upperY[1])
        for (i in 2 until spineCount) {
            val prevX = upperX[i - 1]
            val prevY = upperY[i - 1]
            val midX = (prevX + upperX[i]) * 0.5f
            val midY = (prevY + upperY[i]) * 0.5f
            bodyPath.quadTo(prevX, prevY, midX, midY)
        }
        bodyPath.lineTo(upperX[last], upperY[last])

        val hasTailNotch = plan != BodyPlan.EEL && plan != BodyPlan.PUFFER && plan != BodyPlan.REEF_OVAL && plan != BodyPlan.GOBY
        if (hasTailNotch) {
            val notchDepth = when (plan) {
                BodyPlan.PELAGIC_FAST, BodyPlan.SWORDFISH -> lengthPx * 0.11f
                BodyPlan.WHALE, BodyPlan.ORCA, BodyPlan.DOLPHIN -> lengthPx * 0.085f
                BodyPlan.SHARK -> lengthPx * 0.095f
                else -> lengthPx * 0.055f
            }
            bodyPath.lineTo(spineX[last] + notchDepth, spineY[last])
            bodyPath.lineTo(lowerX[last], lowerY[last])
        } else if (plan != BodyPlan.EEL) {
            bodyPath.quadTo(spineX[last] - lengthPx * 0.055f, spineY[last], lowerX[last], lowerY[last])
        } else {
            bodyPath.lineTo(lowerX[last], lowerY[last])
        }

        for (i in last - 1 downTo 1) {
            val prevX = lowerX[i + 1]
            val prevY = lowerY[i + 1]
            val midX = (prevX + lowerX[i]) * 0.5f
            val midY = (prevY + lowerY[i]) * 0.5f
            bodyPath.quadTo(prevX, prevY, midX, midY)
        }
        bodyPath.quadTo(spineX[0], lowerY[1], spineX[0], spineY[0])
        bodyPath.close()

        // 3. Draw Fins (Dorsal, Pectoral, Ventral, 3D Splayed Approach Pectorals, Sword, Lionfish, Goby Barbels)
        drawAppendageFins(
            canvas, plan, lengthPx, halfH, accentColor, bodyColor,
            solidFinFill, outlineAlpha, animPhase, approachFactor, recedeFactor, isChomping
        )

        // 4. Fill SOLID opaque water-tinted silhouette (100% clean depth occlusion!) + crisp outline
        fillPaint.color = solidBodyFill
        canvas.drawPath(bodyPath, fillPaint)
        strokePaint.strokeWidth = (lengthPx * 0.012f).coerceIn(1.1f, 2.4f)
        strokePaint.color = (bodyColor and 0x00FFFFFF) or (outlineAlpha shl 24)
        canvas.drawPath(bodyPath, strokePaint)

        // 5. Species-Specific Markings (Orca eye/saddle, Clownfish bars, Koi patches, Whale pleats, Spots, Stripes)
        drawSpeciesMarkings(
            canvas, textPaint, marking, lengthPx, halfH,
            accentColor, bellyColor, eyeColor,
            markAlpha, glyphSize, glyphHalfH, isBatterySaver
        )

        // 6. Monospace ASCII Texture Overlay along the undulating spine (keeps iconic Minamo DNA!)
        if (plan == BodyPlan.BAITFISH) {
            textPaint.color = bodyColor
            drawChar(canvas, '>', spineX[spineCount - 2] - glyphSize * 0.35f, spineY[spineCount - 2] + glyphHalfH, textPaint)
            textPaint.color = accentColor
            drawChar(canvas, '~', spineX[7] - glyphSize * 0.3f, spineY[7] + glyphHalfH, textPaint)
        } else {
            val step = if (isBatterySaver) 3 else 2
            val maxStation = spineCount - 2
            for (i in 2..maxStation step step) {
                val sx = spineX[i]
                val sy = spineY[i]
                val localH = ((lowerY[i] - upperY[i]) * 0.5f).coerceAtLeast(1f)
                val bodyChar = when {
                    i >= spineCount - 3 -> if (sin(animPhase) > 0f) '}' else '>'
                    plan == BodyPlan.WHALE || plan == BodyPlan.ORCA -> if (i % 4 == 0) '=' else '~'
                    plan == BodyPlan.EEL -> '~'
                    marking == MarkingStyle.SPOTS -> if (i % 4 == 0) '·' else ':'
                    else -> if (i % 4 == 0) ')' else ':'
                }
                if (localH > glyphSize * 0.55f) {
                    textPaint.color = accentColor
                    drawChar(canvas, if (i % 4 == 0) '#' else '~', sx - glyphSize * 0.3f, sy - localH * 0.42f + glyphHalfH, textPaint)
                }
                textPaint.color = if (i % 4 == 0) accentColor else bodyColor
                drawChar(canvas, bodyChar, sx - glyphSize * 0.3f, sy + glyphHalfH, textPaint)
                if (localH > glyphSize * 1.05f) {
                    textPaint.color = bellyColor
                    drawChar(canvas, if (plan == BodyPlan.WHALE) '=' else '.', sx - glyphSize * 0.3f, sy + localH * 0.45f + glyphHalfH, textPaint)
                }
            }
        }

        // 7. 3D Depth Perspective Overlay:
        //    - When swimming AWAY into the deep (`recedeFactor > 0.25`): draw foreground wagging tail over receding torso!
        //    - When swimming TOWARD the glass (`approachFactor > 0.25`): show symmetric binocular eyes on front of snout!
        if (recedeFactor > 0.25f) {
            val tailWagX = sin(animPhase) * (lengthPx * 0.16f * recedeFactor)
            val tailCenterX = spineX[last] * (1f - recedeFactor * 0.45f) + tailWagX
            val tailCenterY = spineY[last]
            val tailSpanY = halfH * (0.68f + 0.22f * recedeFactor)
            val tailSpanX = lengthPx * (0.09f + 0.05f * recedeFactor)

            finPath.reset()
            finPath.moveTo(tailCenterX, tailCenterY - tailSpanY * 0.22f)
            finPath.quadTo(tailCenterX - tailSpanX, tailCenterY - tailSpanY, tailCenterX + tailWagX * 0.4f, tailCenterY - tailSpanY * 0.85f)
            finPath.quadTo(tailCenterX + tailSpanX * 0.45f, tailCenterY, tailCenterX + tailWagX * 0.4f, tailCenterY + tailSpanY * 0.85f)
            finPath.quadTo(tailCenterX - tailSpanX, tailCenterY + tailSpanY, tailCenterX, tailCenterY + tailSpanY * 0.22f)
            finPath.close()

            fillPaint.color = solidFinFill
            canvas.drawPath(finPath, fillPaint)
            strokePaint.color = (accentColor and 0x00FFFFFF) or (outlineAlpha shl 24)
            strokePaint.strokeWidth = (lengthPx * 0.012f).coerceIn(1.1f, 2.2f)
            canvas.drawPath(finPath, strokePaint)
        } else {
            val eyeStation = when (plan) {
                BodyPlan.WHALE -> 2
                BodyPlan.DOLPHIN, BodyPlan.SWORDFISH -> 3
                else -> 2
            }
            val eyeX = spineX[eyeStation] + lengthPx * (0.05f * approachFactor)
            val eyeY = spineY[eyeStation] - ((lowerY[eyeStation] - upperY[eyeStation]) * 0.14f)
            textPaint.color = eyeColor
            val eyeGlyph = when {
                plan == BodyPlan.BAITFISH -> '°'
                isChomping -> '*'
                else -> '◉'
            }
            drawChar(canvas, eyeGlyph, eyeX - glyphSize * 0.3f, eyeY + glyphHalfH, textPaint)
            if (approachFactor > 0.32f) {
                // Second binocular eye glides smoothly into view as creature turns to swim toward you!
                val pairOffset = lengthPx * (0.14f + 0.06f * approachFactor)
                drawChar(canvas, eyeGlyph, eyeX - pairOffset - glyphSize * 0.3f, eyeY + glyphHalfH, textPaint)
            }
        }
    }

    /**
     * Zero-allocation anatomical contour station writer at spine fraction `s ∈ [0..1]`.
     */
    private fun writeProfileStation(
        idx: Int,
        plan: BodyPlan,
        s: Float,
        tailStart: Float,
        halfH: Float,
        x: Float,
        y: Float
    ) {
        val topR: Float
        val botR: Float
        if (plan == BodyPlan.EEL) {
            val taper = (1f - s * 0.65f) * sin((s * 0.92f + 0.08f) * PI.toFloat()).coerceAtLeast(0.18f)
            val r = halfH * taper
            topR = r
            botR = r
        } else if (s > tailStart) {
            val t = ((s - tailStart) / (1f - tailStart)).coerceIn(0f, 1f)
            val tailFlare = when (plan) {
                BodyPlan.PELAGIC_FAST, BodyPlan.SWORDFISH -> (0.20f + 0.98f * t.pow(0.85f))
                BodyPlan.WHALE -> (0.22f + 0.88f * t.pow(0.9f))
                BodyPlan.ORCA, BodyPlan.DOLPHIN -> (0.20f + 0.78f * t.pow(0.9f))
                BodyPlan.SHARK -> (0.18f + 0.92f * t.pow(0.85f))
                BodyPlan.PUFFER -> (0.24f + 0.40f * t)
                else -> (0.22f + 0.65f * t.pow(0.9f))
            }
            topR = halfH * tailFlare * (if (plan == BodyPlan.SHARK) 1.25f else 1.0f)
            botR = halfH * tailFlare * (if (plan == BodyPlan.SHARK) 0.78f else 1.0f)
        } else {
            val u = (s / tailStart).coerceIn(0f, 1f)
            when (plan) {
                BodyPlan.WHALE -> {
                    topR = halfH * (0.24f + 0.78f * sin(u.pow(0.68f) * PI.toFloat()))
                    botR = halfH * (0.22f + 0.88f * sin(u.pow(0.62f) * PI.toFloat()))
                }
                BodyPlan.ORCA -> {
                    val arch = sin(u.pow(0.75f) * PI.toFloat())
                    topR = halfH * (0.16f + 0.86f * arch)
                    botR = halfH * (0.14f + 0.76f * arch)
                }
                BodyPlan.DOLPHIN -> {
                    val melon = if (u < 0.16f) {
                        0.22f + (u / 0.16f) * 0.25f
                    } else {
                        0.20f + 0.82f * sin(((u - 0.08f) / 0.92f).coerceIn(0f, 1f).pow(0.75f) * PI.toFloat())
                    }
                    val belly = 0.16f + 0.72f * sin(u.pow(0.80f) * PI.toFloat())
                    topR = halfH * melon
                    botR = halfH * belly
                }
                BodyPlan.SHARK -> {
                    topR = halfH * (0.16f + 0.88f * sin(u.pow(0.74f) * PI.toFloat()))
                    botR = halfH * (0.14f + 0.68f * sin(u.pow(0.78f) * PI.toFloat()))
                }
                BodyPlan.PUFFER -> {
                    val round = sin(u.pow(0.86f) * PI.toFloat())
                    topR = halfH * (0.18f + 0.84f * round)
                    botR = halfH * (0.20f + 0.92f * round)
                }
                BodyPlan.REEF_TALL -> {
                    val diamond = sin(u.pow(0.82f) * PI.toFloat()).coerceAtLeast(0f).pow(0.80f)
                    topR = halfH * (0.16f + 0.96f * diamond)
                    botR = halfH * (0.16f + 0.88f * diamond)
                }
                BodyPlan.PELAGIC_FAST, BodyPlan.SWORDFISH -> {
                    val torpedo = sin(u.pow(0.72f) * PI.toFloat())
                    topR = halfH * (0.14f + 0.88f * torpedo)
                    botR = halfH * (0.14f + 0.84f * torpedo)
                }
                BodyPlan.GOBY -> {
                    // Bottom-hugging sand goby: gently arched back + flat seabed-skimming belly
                    val arch = sin(u.pow(0.70f) * PI.toFloat())
                    topR = halfH * (0.20f + 0.92f * arch)
                    botR = halfH * (0.22f + 0.28f * arch)
                }
                else -> {
                    val oval = sin(u.pow(0.78f) * PI.toFloat())
                    topR = halfH * (0.18f + 0.84f * oval)
                    botR = halfH * (0.18f + 0.82f * oval)
                }
            }
        }
        upperX[idx] = x
        upperY[idx] = y - topR
        lowerX[idx] = x
        lowerY[idx] = y + botR
    }

    private fun drawAppendageFins(
        canvas: Canvas,
        plan: BodyPlan,
        lengthPx: Float,
        halfH: Float,
        accentColor: Int,
        bodyColor: Int,
        solidFinFill: Int,
        outlineAlpha: Int,
        animPhase: Float,
        approachFactor: Float,
        recedeFactor: Float,
        isChomping: Boolean
    ) {
        val finColor = accentColor
        fillPaint.color = solidFinFill
        strokePaint.color = (finColor and 0x00FFFFFF) or (outlineAlpha shl 24)
        strokePaint.strokeWidth = (lengthPx * 0.011f).coerceIn(1.0f, 2.2f)

        val flutter = sin(animPhase * 1.2f)

        // 3D splayed lateral pectoral fins when approaching toward or swimming away into depth!
        val zSplay = maxOf(approachFactor, recedeFactor)
        if (zSplay > 0.20f && plan != BodyPlan.EEL && plan != BodyPlan.BAITFISH) {
            val wingSpread = lengthPx * (0.24f * zSplay)
            val wingDrop = halfH * (0.48f + 0.16f * flutter)
            val anchorX = spineX[4]
            val anchorY = spineY[4] + halfH * 0.15f

            // Left lateral fin (visible from head-on / rear-on 3D view)
            finPath.reset()
            finPath.moveTo(anchorX, anchorY)
            finPath.quadTo(anchorX - wingSpread * 1.15f, anchorY + wingDrop * 0.45f, anchorX - wingSpread, anchorY + wingDrop)
            finPath.quadTo(anchorX - wingSpread * 0.35f, anchorY + wingDrop * 0.65f, anchorX, anchorY + halfH * 0.30f)
            finPath.close()
            canvas.drawPath(finPath, fillPaint)
            canvas.drawPath(finPath, strokePaint)

            // Right lateral fin
            finPath.reset()
            finPath.moveTo(anchorX, anchorY)
            finPath.quadTo(anchorX + wingSpread * 1.15f, anchorY + wingDrop * 0.45f, anchorX + wingSpread, anchorY + wingDrop)
            finPath.quadTo(anchorX + wingSpread * 0.35f, anchorY + wingDrop * 0.65f, anchorX, anchorY + halfH * 0.30f)
            finPath.close()
            canvas.drawPath(finPath, fillPaint)
            canvas.drawPath(finPath, strokePaint)
        }

        when (plan) {
            BodyPlan.ORCA -> {
                finPath.reset()
                finPath.moveTo(upperX[5], upperY[5] + 2f)
                finPath.quadTo(upperX[7] + lengthPx * 0.04f, upperY[6] - halfH * 1.45f, upperX[8] - lengthPx * 0.05f, upperY[6] - halfH * 1.55f)
                finPath.quadTo(upperX[8], upperY[7] - halfH * 0.55f, upperX[9], upperY[9] + 2f)
                finPath.close()
                canvas.drawPath(finPath, fillPaint)
                canvas.drawPath(finPath, strokePaint)

                finPath.reset()
                finPath.moveTo(lowerX[4], lowerY[4] - 2f)
                finPath.quadTo(lowerX[6], lowerY[5] + halfH * 0.95f + flutter * 3f, lowerX[7], lowerY[6] + 1f)
                finPath.close()
                canvas.drawPath(finPath, fillPaint)
                canvas.drawPath(finPath, strokePaint)
            }
            BodyPlan.WHALE -> {
                finPath.reset()
                finPath.moveTo(lowerX[4], lowerY[4] - halfH * 0.15f)
                finPath.quadTo(
                    lowerX[7] + lengthPx * 0.04f,
                    lowerY[6] + halfH * 1.35f + flutter * halfH * 0.18f,
                    lowerX[9],
                    lowerY[8] + halfH * 0.95f + flutter * halfH * 0.22f
                )
                finPath.quadTo(lowerX[7], lowerY[6] + halfH * 0.25f, lowerX[6], lowerY[5] - halfH * 0.1f)
                finPath.close()
                canvas.drawPath(finPath, fillPaint)
                canvas.drawPath(finPath, strokePaint)

                finPath.reset()
                finPath.moveTo(upperX[9], upperY[9] + 2f)
                finPath.quadTo(upperX[10], upperY[10] - halfH * 0.36f, upperX[11], upperY[11] + 2f)
                finPath.close()
                canvas.drawPath(finPath, fillPaint)
                canvas.drawPath(finPath, strokePaint)
            }
            BodyPlan.DOLPHIN, BodyPlan.SHARK -> {
                val dHeight = if (plan == BodyPlan.SHARK) halfH * 1.25f else halfH * 0.95f
                finPath.reset()
                finPath.moveTo(upperX[5], upperY[5] + 1f)
                finPath.quadTo(upperX[7], upperY[6] - dHeight, upperX[9] - lengthPx * 0.02f, upperY[6] - dHeight * 0.92f)
                finPath.quadTo(upperX[8], upperY[7] - dHeight * 0.32f, upperX[9], upperY[9] + 1f)
                finPath.close()
                canvas.drawPath(finPath, fillPaint)
                canvas.drawPath(finPath, strokePaint)

                finPath.reset()
                finPath.moveTo(lowerX[4], lowerY[4] - 1f)
                finPath.quadTo(lowerX[6], lowerY[5] + halfH * 0.85f + flutter * 2f, lowerX[7], lowerY[6])
                finPath.close()
                canvas.drawPath(finPath, fillPaint)
                canvas.drawPath(finPath, strokePaint)
            }
            BodyPlan.SWORDFISH -> {
                strokePaint.strokeWidth = (lengthPx * 0.018f).coerceIn(1.5f, 3.0f)
                canvas.drawLine(spineX[0], spineY[0], spineX[0] + lengthPx * 0.28f, spineY[0], strokePaint)
                strokePaint.strokeWidth = (lengthPx * 0.011f).coerceIn(1.0f, 2.2f)
                finPath.reset()
                finPath.moveTo(upperX[3], upperY[3] + 1f)
                finPath.quadTo(upperX[5], upperY[5] - halfH * 1.45f, upperX[9], upperY[9] + 1f)
                finPath.close()
                canvas.drawPath(finPath, fillPaint)
                canvas.drawPath(finPath, strokePaint)
            }
            BodyPlan.REEF_TALL -> {
                finPath.reset()
                finPath.moveTo(upperX[4], upperY[4] + 2f)
                finPath.quadTo(
                    upperX[8],
                    upperY[5] - halfH * 0.95f + flutter * halfH * 0.14f,
                    upperX[14] - lengthPx * 0.18f,
                    upperY[8] - halfH * 0.65f + sin(animPhase - 2.2f) * halfH * 0.25f
                )
                canvas.drawPath(finPath, strokePaint)
                finPath.reset()
                finPath.moveTo(lowerX[5], lowerY[5] - 1f)
                finPath.quadTo(lowerX[8], lowerY[7] + halfH * 0.55f, lowerX[10], lowerY[10] - 1f)
                finPath.close()
                canvas.drawPath(finPath, fillPaint)
                canvas.drawPath(finPath, strokePaint)
            }
            BodyPlan.LIONFISH -> {
                for (r in 0..5) {
                    val idx = 3 + r
                    val sway = sin(animPhase - r * 0.65f) * halfH * 0.16f
                    canvas.drawLine(upperX[idx], upperY[idx], upperX[idx] - lengthPx * 0.12f, upperY[idx] - halfH * 0.85f + sway, strokePaint)
                    canvas.drawLine(lowerX[idx], lowerY[idx], lowerX[idx] - lengthPx * 0.10f, lowerY[idx] + halfH * 0.78f - sway, strokePaint)
                }
            }
            BodyPlan.REEF_OVAL, BodyPlan.PELAGIC_FAST -> {
                finPath.reset()
                finPath.moveTo(upperX[4], upperY[4] + 1f)
                finPath.quadTo(upperX[7], upperY[6] - halfH * 0.62f, upperX[10], upperY[10] + 1f)
                finPath.close()
                canvas.drawPath(finPath, fillPaint)
                canvas.drawPath(finPath, strokePaint)

                finPath.reset()
                finPath.moveTo(lowerX[5], lowerY[5] - 1f)
                finPath.quadTo(lowerX[8], lowerY[7] + halfH * 0.52f + flutter * 1.5f, lowerX[10], lowerY[10] - 1f)
                finPath.close()
                canvas.drawPath(finPath, fillPaint)
                canvas.drawPath(finPath, strokePaint)
            }
            BodyPlan.GOBY -> {
                // Tall speckled sand-goby dorsal sail + tiny whisker barbels!
                finPath.reset()
                finPath.moveTo(upperX[3], upperY[3] + 1f)
                finPath.quadTo(upperX[6], upperY[5] - halfH * 0.85f, upperX[11], upperY[11] + 1f)
                finPath.close()
                canvas.drawPath(finPath, fillPaint)
                canvas.drawPath(finPath, strokePaint)

                // Whisker barbels extending down-forward from snout
                canvas.drawLine(spineX[0], spineY[0] + halfH * 0.15f, spineX[0] + lengthPx * 0.12f, spineY[0] + halfH * 0.55f + flutter, strokePaint)
                canvas.drawLine(spineX[1], spineY[1] + halfH * 0.20f, spineX[0] + lengthPx * 0.08f, spineY[0] + halfH * 0.62f - flutter, strokePaint)
            }
            BodyPlan.EEL -> {
                finPath.reset()
                finPath.moveTo(upperX[1], upperY[1])
                for (i in 2 until spineCount - 1) {
                    val crestH = halfH * (0.45f + 0.25f * sin(animPhase - i * 0.7f))
                    finPath.lineTo(upperX[i], upperY[i] - crestH)
                }
                finPath.lineTo(upperX[spineCount - 1], upperY[spineCount - 1])
                for (i in spineCount - 2 downTo 1) {
                    finPath.lineTo(upperX[i], upperY[i])
                }
                finPath.close()
                canvas.drawPath(finPath, fillPaint)
                canvas.drawPath(finPath, strokePaint)
            }
            else -> Unit
        }

        // Predator jaw snap flash
        if (isChomping) {
            strokePaint.color = (bodyColor and 0x00FFFFFF) or (outlineAlpha shl 24)
            canvas.drawLine(spineX[0], spineY[0], spineX[0] + lengthPx * 0.08f, spineY[0] - halfH * 0.32f, strokePaint)
            canvas.drawLine(spineX[0], spineY[0], spineX[0] + lengthPx * 0.08f, spineY[0] + halfH * 0.32f, strokePaint)
        }
    }

    private fun drawSpeciesMarkings(
        canvas: Canvas,
        textPaint: Paint,
        marking: MarkingStyle,
        lengthPx: Float,
        halfH: Float,
        accentColor: Int,
        bellyColor: Int,
        eyeColor: Int,
        markAlpha: Int,
        glyphSize: Float,
        glyphHalfH: Float,
        isBatterySaver: Boolean
    ) {
        if (marking == MarkingStyle.NONE) return

        when (marking) {
            MarkingStyle.CLOWN_BANDS, MarkingStyle.IDOL_BANDS -> {
                // Vertical contrast stripes inside the body profile!
                val bandColor = if (marking == MarkingStyle.CLOWN_BANDS) eyeColor else accentColor
                strokePaint.color = (bandColor and 0x00FFFFFF) or ((markAlpha + 70).coerceAtMost(210) shl 24)
                strokePaint.strokeWidth = (lengthPx * 0.045f).coerceIn(2.0f, 6.0f)
                val stations = if (marking == MarkingStyle.CLOWN_BANDS) clownBandStations else idolBandStations
                for (st in stations) {
                    canvas.drawLine(upperX[st], upperY[st] + 2f, lowerX[st], lowerY[st] - 2f, strokePaint)
                }
            }
            MarkingStyle.ANGEL_STRIPES -> {
                // Fine horizontal pinstripes along the Emperor Angelfish flank!
                strokePaint.color = (accentColor and 0x00FFFFFF) or ((markAlpha + 55).coerceAtMost(185) shl 24)
                strokePaint.strokeWidth = 1.25f
                for (band in -1..1) {
                    val yOff = band * (halfH * 0.30f)
                    canvas.drawLine(spineX[3], spineY[3] + yOff, spineX[10], spineY[10] + yOff * 0.7f, strokePaint)
                }
            }
            MarkingStyle.TANG_MASK -> {
                // Iconic Regal Blue Tang bright accent tail wedge & dorsal highlight!
                strokePaint.color = (accentColor and 0x00FFFFFF) or ((markAlpha + 75).coerceAtMost(215) shl 24)
                strokePaint.strokeWidth = (lengthPx * 0.032f).coerceIn(1.6f, 4.2f)
                canvas.drawLine(spineX[11], spineY[11], spineX[14], spineY[14], strokePaint)
                canvas.drawLine(upperX[3], upperY[3] + 2f, upperX[9], upperY[9] + 2f, strokePaint)
            }
            MarkingStyle.MANDARIN_SWIRL -> {
                // Ornate Mandarin Dragonet psychedelic wavy bands
                strokePaint.color = (accentColor and 0x00FFFFFF) or ((markAlpha + 65).coerceAtMost(200) shl 24)
                strokePaint.strokeWidth = (lengthPx * 0.025f).coerceIn(1.4f, 3.2f)
                for (st in mandarinSwirlStations) {
                    canvas.drawLine(upperX[st] + 2f, upperY[st] + 3f, lowerX[st - 1], lowerY[st - 1] - 3f, strokePaint)
                }
            }
            MarkingStyle.ORCA_PATCHES -> {
                // Iconic white Orca eye patch & saddle behind dorsal fin!
                if (markAlpha > 0) {
                    fillPaint.color = (eyeColor and 0x00FFFFFF) or (markAlpha shl 24)
                    val ex = spineX[3] - lengthPx * 0.02f
                    val ey = spineY[3] - halfH * 0.14f
                    canvas.drawOval(ex - lengthPx * 0.045f, ey - halfH * 0.16f, ex + lengthPx * 0.045f, ey + halfH * 0.16f, fillPaint)
                    val sx = spineX[9]
                    val sy = upperY[9] + halfH * 0.25f
                    canvas.drawOval(sx - lengthPx * 0.06f, sy - halfH * 0.22f, sx + lengthPx * 0.06f, sy + halfH * 0.22f, fillPaint)
                }
            }
            MarkingStyle.KOI_PATCHES -> {
                if (markAlpha > 0) {
                    fillPaint.color = (accentColor and 0x00FFFFFF) or (markAlpha shl 24)
                    canvas.drawOval(spineX[4] - lengthPx * 0.08f, upperY[4] + 2f, spineX[4] + lengthPx * 0.08f, spineY[4] + halfH * 0.2f, fillPaint)
                    canvas.drawOval(spineX[8] - lengthPx * 0.07f, upperY[8] + 2f, spineX[8] + lengthPx * 0.06f, spineY[8] + halfH * 0.15f, fillPaint)
                }
            }
            MarkingStyle.SPOTS -> {
                if (!isBatterySaver) {
                    textPaint.color = accentColor
                    for (st in 3..10 step 2) {
                        val rH = (lowerY[st] - upperY[st]) * 0.24f
                        drawChar(canvas, '·', spineX[st] - glyphSize * 0.25f, spineY[st] - rH + glyphHalfH, textPaint)
                        drawChar(canvas, '·', spineX[st] + glyphSize * 0.15f, spineY[st] + rH * 0.5f + glyphHalfH, textPaint)
                    }
                }
            }
            MarkingStyle.WHALE_PLEATS -> {
                // Fine ventral throat pleats under the humpback's chin
                strokePaint.color = (bellyColor and 0x00FFFFFF) or ((markAlpha + 45).coerceAtMost(160) shl 24)
                strokePaint.strokeWidth = 1.2f
                for (row in 0..2) {
                    val yOff = row * (halfH * 0.14f)
                    canvas.drawLine(spineX[1], spineY[1] + halfH * 0.25f + yOff, spineX[6], spineY[6] + halfH * 0.30f + yOff, strokePaint)
                }
            }
            else -> Unit
        }
    }

    /**
     * Graceful Green Sea Turtle with domed mosaic carapace, extending head, and sweeping wing-like flippers!
     */
    private fun drawSeaTurtle(
        canvas: Canvas,
        textPaint: Paint,
        lengthPx: Float,
        halfH: Float,
        bodyColor: Int,
        accentColor: Int,
        eyeColor: Int,
        solidBodyFill: Int,
        solidFinFill: Int,
        outlineAlpha: Int,
        animPhase: Float,
        glyphSize: Float,
        glyphHalfH: Float,
        approachFactor: Float,
        recedeFactor: Float,
        isBatterySaver: Boolean
    ) {
        val halfL = lengthPx * 0.5f
        val strokeAngle = sin(animPhase)

        // Sweeping wing-like front flipper (strokes cleanly up & down!)
        finPath.reset()
        finPath.moveTo(halfL * 0.16f, halfH * 0.15f)
        val tipX = -halfL * 0.16f + strokeAngle * halfL * 0.14f
        val tipY = halfH * (0.25f + strokeAngle * 1.15f)
        finPath.quadTo(halfL * 0.06f, tipY * 0.85f, tipX, tipY)
        finPath.quadTo(-halfL * 0.08f, halfH * 0.35f, -halfL * 0.04f, halfH * 0.22f)
        finPath.close()

        fillPaint.color = solidFinFill
        strokePaint.color = (accentColor and 0x00FFFFFF) or (outlineAlpha shl 24)
        strokePaint.strokeWidth = (lengthPx * 0.013f).coerceIn(1.1f, 2.2f)
        canvas.drawPath(finPath, fillPaint)
        canvas.drawPath(finPath, strokePaint)

        // Trailing hind flipper
        accentPath.reset()
        accentPath.moveTo(-halfL * 0.52f, halfH * 0.15f)
        accentPath.quadTo(-halfL * 0.82f, halfH * (0.35f - strokeAngle * 0.25f), -halfL * 0.92f, halfH * 0.18f)
        accentPath.quadTo(-halfL * 0.72f, halfH * 0.05f, -halfL * 0.54f, halfH * 0.02f)
        accentPath.close()
        canvas.drawPath(accentPath, fillPaint)
        canvas.drawPath(accentPath, strokePaint)

        // Head & neck extending gracefully forward
        if (recedeFactor < 0.45f) {
            accentPath.reset()
            accentPath.moveTo(halfL * 0.44f, -halfH * 0.18f)
            accentPath.quadTo(halfL * 0.86f, -halfH * 0.28f, halfL * 0.98f, 0f)
            accentPath.quadTo(halfL * 0.86f, halfH * 0.26f, halfL * 0.44f, halfH * 0.22f)
            accentPath.close()
            canvas.drawPath(accentPath, fillPaint)
            canvas.drawPath(accentPath, strokePaint)
        }

        // Domed Carapace Shell (solid opaque occlusion!)
        bodyPath.reset()
        bodyPath.moveTo(-halfL * 0.64f, halfH * 0.16f)
        bodyPath.quadTo(-halfL * 0.08f, -halfH * 1.08f, halfL * 0.52f, halfH * 0.02f)
        bodyPath.quadTo(-halfL * 0.05f, halfH * 0.44f, -halfL * 0.64f, halfH * 0.16f)
        bodyPath.close()
        fillPaint.color = solidBodyFill
        canvas.drawPath(bodyPath, fillPaint)
        strokePaint.color = (bodyColor and 0x00FFFFFF) or (outlineAlpha shl 24)
        canvas.drawPath(bodyPath, strokePaint)

        // Mosaic ASCII shell scutes
        textPaint.color = accentColor
        val scutes = if (isBatterySaver) 2 else 4
        for (k in 0 until scutes) {
            val sx = -halfL * 0.36f + k * (halfL * 0.22f)
            val sy = -halfH * 0.18f + (if (k == 0 || k == scutes - 1) halfH * 0.12f else -halfH * 0.08f)
            drawChar(canvas, if (k % 2 == 0) '#' else ':', sx - glyphSize * 0.3f, sy + glyphHalfH, textPaint)
        }

        // Crisp eye (hidden when gliding away into the deep!)
        if (recedeFactor < 0.35f) {
            textPaint.color = eyeColor
            drawChar(canvas, '◉', halfL * 0.78f - glyphSize * 0.3f, -halfH * 0.04f + glyphHalfH, textPaint)
            if (approachFactor > 0.35f) {
                drawChar(canvas, '°', halfL * 0.58f - glyphSize * 0.3f, -halfH * 0.04f + glyphHalfH, textPaint)
            }
        }
    }

    /**
     * Sweeping Spotted Eagle Ray / Giant Oceanic Manta with sinusoidal flying wings + undulating whip tail!
     */
    private fun drawRay(
        canvas: Canvas,
        textPaint: Paint,
        lengthPx: Float,
        halfH: Float,
        bodyColor: Int,
        accentColor: Int,
        eyeColor: Int,
        marking: MarkingStyle,
        solidBodyFill: Int,
        outlineAlpha: Int,
        animPhase: Float,
        glyphSize: Float,
        glyphHalfH: Float,
        approachFactor: Float,
        recedeFactor: Float,
        isBatterySaver: Boolean
    ) {
        val halfL = lengthPx * 0.5f
        val wingBeat = sin(animPhase)

        // Long undulating whip tail streaming gracefully behind
        strokePaint.color = (accentColor and 0x00FFFFFF) or (outlineAlpha shl 24)
        strokePaint.strokeWidth = (lengthPx * 0.011f).coerceIn(1.0f, 2.2f)
        finPath.reset()
        finPath.moveTo(-halfL * 0.22f, 0f)
        for (tStep in 1..6) {
            val tf = tStep / 6f
            val tx = -halfL * (0.22f + tf * 0.85f)
            val ty = sin(animPhase - tf * 4.5f) * halfH * 0.26f * tf
            finPath.lineTo(tx, ty)
        }
        canvas.drawPath(finPath, strokePaint)

        // Sweeping diamond wings curving smoothly up & down!
        val upperTipY = -halfH * (0.95f + wingBeat * 0.42f)
        val lowerTipY = halfH * (0.95f - wingBeat * 0.42f)
        val wingLeadX = halfL * 0.12f
        val wingTipX = -halfL * 0.08f

        bodyPath.reset()
        bodyPath.moveTo(halfL * 0.58f, 0f)
        bodyPath.quadTo(wingLeadX, upperTipY * 0.72f, wingTipX, upperTipY)
        bodyPath.quadTo(-halfL * 0.20f, upperTipY * 0.35f, -halfL * 0.28f, 0f)
        bodyPath.quadTo(-halfL * 0.20f, lowerTipY * 0.35f, wingTipX, lowerTipY)
        bodyPath.quadTo(wingLeadX, lowerTipY * 0.72f, halfL * 0.58f, 0f)
        bodyPath.close()

        fillPaint.color = solidBodyFill
        canvas.drawPath(bodyPath, fillPaint)
        strokePaint.color = (bodyColor and 0x00FFFFFF) or (outlineAlpha shl 24)
        canvas.drawPath(bodyPath, strokePaint)

        // Delicate ASCII wing texture / spots
        textPaint.color = accentColor
        val wingGlyph = if (marking == MarkingStyle.SPOTS) '·' else '~'
        drawChar(canvas, wingGlyph, halfL * 0.12f, upperTipY * 0.38f + glyphHalfH, textPaint)
        drawChar(canvas, wingGlyph, halfL * 0.12f, lowerTipY * 0.38f + glyphHalfH, textPaint)
        if (!isBatterySaver) {
            drawChar(canvas, ':', -halfL * 0.02f, glyphHalfH, textPaint)
            drawChar(canvas, '~', -halfL * 0.55f, sin(animPhase - 2f) * halfH * 0.12f + glyphHalfH, textPaint)
        }

        // Crisp eye
        if (recedeFactor < 0.35f) {
            textPaint.color = eyeColor
            drawChar(canvas, '◉', halfL * 0.36f - glyphSize * 0.3f, -halfH * 0.10f + glyphHalfH, textPaint)
            if (approachFactor > 0.35f) {
                drawChar(canvas, '°', halfL * 0.36f - glyphSize * 0.3f, halfH * 0.14f + glyphHalfH, textPaint)
            }
        }
    }

    /**
     * Nocturnal Giant Squid: sleek torpedo mantle with lateral fins + 4 undulating tentacles.
     */
    private fun drawSquid(
        canvas: Canvas,
        textPaint: Paint,
        lengthPx: Float,
        halfH: Float,
        bodyColor: Int,
        accentColor: Int,
        eyeColor: Int,
        solidBodyFill: Int,
        outlineAlpha: Int,
        animPhase: Float,
        glyphSize: Float,
        glyphHalfH: Float,
        isBatterySaver: Boolean
    ) {
        val halfL = lengthPx * 0.5f
        val pulse = 1f + 0.10f * sin(animPhase)

        // 4 Undulating trailing tentacles
        strokePaint.color = (accentColor and 0x00FFFFFF) or (outlineAlpha shl 24)
        strokePaint.strokeWidth = (lengthPx * 0.011f).coerceIn(1.0f, 2.0f)
        val tentCount = if (isBatterySaver) 2 else 4
        for (t in 0 until tentCount) {
            val baseOffY = ((t / (tentCount - 1f).coerceAtLeast(1f)) - 0.5f) * halfH * 1.1f
            finPath.reset()
            finPath.moveTo(-halfL * 0.05f, baseOffY * 0.6f)
            for (s in 1..5) {
                val sf = s / 5f
                val tx = -halfL * (0.05f + sf * 0.92f)
                val ty = baseOffY + sin(animPhase * 1.4f - sf * 4.5f + t) * halfH * 0.35f * sf
                finPath.lineTo(tx, ty)
            }
            canvas.drawPath(finPath, strokePaint)
        }

        // Streamlined torpedo mantle (solid occlusion!)
        bodyPath.reset()
        bodyPath.moveTo(halfL * 0.92f, 0f)
        bodyPath.quadTo(halfL * 0.45f, -halfH * 0.95f * pulse, -halfL * 0.08f, -halfH * 0.55f)
        bodyPath.lineTo(-halfL * 0.08f, halfH * 0.55f)
        bodyPath.quadTo(halfL * 0.45f, halfH * 0.95f * pulse, halfL * 0.92f, 0f)
        bodyPath.close()
        fillPaint.color = solidBodyFill
        canvas.drawPath(bodyPath, fillPaint)
        strokePaint.color = (bodyColor and 0x00FFFFFF) or (outlineAlpha shl 24)
        canvas.drawPath(bodyPath, strokePaint)

        textPaint.color = accentColor
        drawChar(canvas, '#', halfL * 0.35f, glyphHalfH, textPaint)
        drawChar(canvas, ':', halfL * 0.14f, glyphHalfH, textPaint)
        textPaint.color = eyeColor
        drawChar(canvas, '◉', -halfL * 0.04f, -halfH * 0.12f + glyphHalfH, textPaint)
    }

    /**
     * Scurrying Reef Crab: domed oval carapace, 3 pairs of animated jointed legs, raised curved pincers & stalked eyes!
     */
    private fun drawCrab(
        canvas: Canvas,
        textPaint: Paint,
        lengthPx: Float,
        halfH: Float,
        bodyColor: Int,
        accentColor: Int,
        eyeColor: Int,
        solidBodyFill: Int,
        solidFinFill: Int,
        outlineAlpha: Int,
        animPhase: Float,
        glyphSize: Float,
        glyphHalfH: Float,
        isSnapping: Boolean,
        isBatterySaver: Boolean
    ) {
        val halfL = lengthPx * 0.5f
        val shellRx = halfL * 0.52f
        val shellRy = halfH * 0.58f
        val strokeW = (lengthPx * 0.022f).coerceIn(1.2f, 2.2f)
        strokePaint.strokeWidth = strokeW
        strokePaint.color = (accentColor and 0x00FFFFFF) or (outlineAlpha shl 24)

        // 3 pairs of scurrying jointed legs stepping out of phase
        val legPairs = if (isBatterySaver) 2 else 3
        for (side in -1..1 step 2) {
            val sSign = side.toFloat()
            for (l in 0 until legPairs) {
                val frac = l / (legPairs - 1f).coerceAtLeast(1f)
                val step = sin(animPhase * 2.2f + l * 1.9f + (if (side > 0) 1.5f else 0f)) * (halfH * 0.22f)
                val baseX = sSign * shellRx * (0.72f + frac * 0.18f)
                val baseY = shellRy * (0.05f + frac * 0.48f)
                val kneeX = sSign * (halfL * (0.86f - frac * 0.08f))
                val kneeY = baseY - halfH * 0.18f + step * 0.5f
                val footX = sSign * (halfL * (0.98f - frac * 0.12f))
                val footY = halfH * 0.85f + step
                finPath.reset()
                finPath.moveTo(baseX, baseY)
                finPath.lineTo(kneeX, kneeY)
                finPath.lineTo(footX, footY)
                canvas.drawPath(finPath, strokePaint)
            }
        }

        // Raised curved pincer claws (snap wide when defensive!)
        val clawLift = if (isSnapping) halfH * 0.35f else (sin(animPhase * 1.1f) * halfH * 0.12f)
        val clawGap = if (isSnapping) 0.26f else 0.10f
        fillPaint.color = solidFinFill
        for (side in -1..1 step 2) {
            val sSign = side.toFloat()
            val armStartX = sSign * shellRx * 0.65f
            val armStartY = -shellRy * 0.25f
            val clawX = sSign * halfL * 0.78f
            val clawY = -halfH * 0.78f - clawLift
            canvas.drawLine(armStartX, armStartY, clawX, clawY + halfH * 0.22f, strokePaint)

            // Pincer crescent
            accentPath.reset()
            accentPath.moveTo(clawX, clawY + halfH * 0.22f)
            accentPath.quadTo(clawX + sSign * halfL * 0.18f, clawY - halfH * 0.18f, clawX - sSign * halfL * clawGap, clawY - halfH * 0.26f)
            accentPath.quadTo(clawX - sSign * halfL * 0.02f, clawY + halfH * 0.05f, clawX, clawY + halfH * 0.22f)
            accentPath.close()
            canvas.drawPath(accentPath, fillPaint)
            canvas.drawPath(accentPath, strokePaint)
        }

        // Solid domed oval carapace
        bodyPath.reset()
        bodyPath.moveTo(-shellRx, shellRy * 0.12f)
        bodyPath.quadTo(-shellRx * 0.88f, -shellRy * 1.05f, 0f, -shellRy * 1.05f)
        bodyPath.quadTo(shellRx * 0.88f, -shellRy * 1.05f, shellRx, shellRy * 0.12f)
        bodyPath.quadTo(0f, shellRy * 0.92f, -shellRx, shellRy * 0.12f)
        bodyPath.close()
        fillPaint.color = solidBodyFill
        canvas.drawPath(bodyPath, fillPaint)
        strokePaint.color = (bodyColor and 0x00FFFFFF) or (outlineAlpha shl 24)
        canvas.drawPath(bodyPath, strokePaint)

        // Monospace ASCII shell markings
        textPaint.color = accentColor
        drawChar(canvas, '#', -glyphSize * 0.30f, glyphHalfH * 0.5f, textPaint)
        if (!isBatterySaver) {
            drawChar(canvas, ':', -shellRx * 0.48f - glyphSize * 0.25f, glyphHalfH * 0.7f, textPaint)
            drawChar(canvas, ':', shellRx * 0.48f - glyphSize * 0.25f, glyphHalfH * 0.7f, textPaint)
        }

        // Cute stalked eyes peeking above shell
        textPaint.color = eyeColor
        val eyeY = -shellRy * 0.92f
        drawChar(canvas, '°', -shellRx * 0.32f - glyphSize * 0.28f, eyeY + glyphHalfH, textPaint)
        drawChar(canvas, '°', shellRx * 0.32f - glyphSize * 0.28f, eyeY + glyphHalfH, textPaint)
    }

    /**
     * Delicate Cleaner Shrimp: graceful arched segmented carapace, fan tail, fluttering swimmerets & long sweeping antennae!
     */
    private fun drawShrimp(
        canvas: Canvas,
        textPaint: Paint,
        lengthPx: Float,
        halfH: Float,
        bodyColor: Int,
        accentColor: Int,
        eyeColor: Int,
        solidBodyFill: Int,
        solidFinFill: Int,
        outlineAlpha: Int,
        animPhase: Float,
        glyphSize: Float,
        glyphHalfH: Float,
        isBatterySaver: Boolean
    ) {
        val halfL = lengthPx * 0.5f
        val sway = sin(animPhase * 1.8f)

        strokePaint.strokeWidth = (lengthPx * 0.018f).coerceIn(1.0f, 1.9f)
        strokePaint.color = (accentColor and 0x00FFFFFF) or (outlineAlpha shl 24)

        // Long graceful sweeping antennae arching forward
        finPath.reset()
        finPath.moveTo(halfL * 0.56f, -halfH * 0.15f)
        finPath.quadTo(halfL * 0.92f, -halfH * (0.95f + sway * 0.22f), halfL * 1.25f, -halfH * (0.45f - sway * 0.30f))
        canvas.drawPath(finPath, strokePaint)

        finPath.reset()
        finPath.moveTo(halfL * 0.58f, 0f)
        finPath.quadTo(halfL * 0.96f, -halfH * (0.38f - sway * 0.18f), halfL * 1.18f, halfH * (0.18f + sway * 0.22f))
        canvas.drawPath(finPath, strokePaint)

        // Fluttering swimmeret legs underneath
        val legs = if (isBatterySaver) 3 else 4
        for (l in 0 until legs) {
            val lx = halfL * (0.35f - l * 0.22f)
            val flutter = sin(animPhase * 3.0f - l * 1.2f) * (halfL * 0.06f)
            canvas.drawLine(lx, halfH * 0.18f, lx + flutter, halfH * 0.72f, strokePaint)
        }

        // Fan tail at rear
        accentPath.reset()
        accentPath.moveTo(-halfL * 0.62f, halfH * 0.28f)
        accentPath.quadTo(-halfL * 0.95f, halfH * (0.10f + sway * 0.18f), -halfL * 0.98f, halfH * 0.55f)
        accentPath.quadTo(-halfL * 0.78f, halfH * 0.65f, -halfL * 0.58f, halfH * 0.42f)
        accentPath.close()
        fillPaint.color = solidFinFill
        canvas.drawPath(accentPath, fillPaint)
        canvas.drawPath(accentPath, strokePaint)

        // Arched segmented shrimp torso (solid occlusion!)
        bodyPath.reset()
        bodyPath.moveTo(halfL * 0.66f, 0f)
        bodyPath.quadTo(halfL * 0.12f, -halfH * 0.92f, -halfL * 0.66f, halfH * 0.34f)
        bodyPath.quadTo(-halfL * 0.02f, -halfH * 0.08f, halfL * 0.52f, halfH * 0.28f)
        bodyPath.close()
        fillPaint.color = solidBodyFill
        canvas.drawPath(bodyPath, fillPaint)
        strokePaint.color = (bodyColor and 0x00FFFFFF) or (outlineAlpha shl 24)
        canvas.drawPath(bodyPath, strokePaint)

        // Monospace segmented ASCII stripes + crisp eye
        textPaint.color = accentColor
        drawChar(canvas, ':', halfL * 0.10f - glyphSize * 0.25f, -halfH * 0.18f + glyphHalfH, textPaint)
        drawChar(canvas, '~', -halfL * 0.24f - glyphSize * 0.25f, halfH * 0.02f + glyphHalfH, textPaint)
        textPaint.color = eyeColor
        drawChar(canvas, '°', halfL * 0.52f - glyphSize * 0.25f, -halfH * 0.12f + glyphHalfH, textPaint)
    }

    /**
     * Gliding Sea Conch: conical coiled spiral shell, soft gliding foot & swaying eye-stalk antennae!
     */
    private fun drawConch(
        canvas: Canvas,
        textPaint: Paint,
        lengthPx: Float,
        halfH: Float,
        bodyColor: Int,
        accentColor: Int,
        eyeColor: Int,
        solidBodyFill: Int,
        solidFinFill: Int,
        outlineAlpha: Int,
        animPhase: Float,
        glyphSize: Float,
        glyphHalfH: Float,
        isBatterySaver: Boolean
    ) {
        val halfL = lengthPx * 0.5f
        val feelerSway = sin(animPhase * 1.4f) * (halfH * 0.14f)

        strokePaint.strokeWidth = (lengthPx * 0.022f).coerceIn(1.1f, 2.0f)
        strokePaint.color = (accentColor and 0x00FFFFFF) or (outlineAlpha shl 24)

        // Soft gliding foot underneath
        accentPath.reset()
        accentPath.moveTo(-halfL * 0.45f, halfH * 0.58f)
        accentPath.quadTo(halfL * 0.20f, halfH * 0.30f, halfL * 0.78f, halfH * 0.42f)
        accentPath.quadTo(halfL * 0.55f, halfH * 0.80f, -halfL * 0.45f, halfH * 0.68f)
        accentPath.close()
        fillPaint.color = solidFinFill
        canvas.drawPath(accentPath, fillPaint)
        canvas.drawPath(accentPath, strokePaint)

        // Two tiny swaying eye-stalks extending forward
        canvas.drawLine(halfL * 0.62f, halfH * 0.38f, halfL * 0.88f, -halfH * 0.12f + feelerSway, strokePaint)
        canvas.drawLine(halfL * 0.54f, halfH * 0.38f, halfL * 0.74f, -halfH * 0.02f - feelerSway, strokePaint)

        // Coiled conical spiral conch shell (solid occlusion!)
        bodyPath.reset()
        bodyPath.moveTo(-halfL * 0.82f, halfH * 0.25f)
        bodyPath.quadTo(-halfL * 0.30f, -halfH * 0.96f, halfL * 0.36f, -halfH * 0.55f)
        bodyPath.quadTo(halfL * 0.65f, -halfH * 0.05f, halfL * 0.35f, halfH * 0.52f)
        bodyPath.quadTo(-halfL * 0.30f, halfH * 0.62f, -halfL * 0.82f, halfH * 0.25f)
        bodyPath.close()
        fillPaint.color = solidBodyFill
        canvas.drawPath(bodyPath, fillPaint)
        strokePaint.color = (bodyColor and 0x00FFFFFF) or (outlineAlpha shl 24)
        canvas.drawPath(bodyPath, strokePaint)

        // Spiral whorl markings + iconic '@' ASCII shell coil
        textPaint.color = accentColor
        drawChar(canvas, '@', -halfL * 0.02f - glyphSize * 0.28f, -halfH * 0.04f + glyphHalfH, textPaint)
        if (!isBatterySaver) {
            drawChar(canvas, ':', -halfL * 0.42f - glyphSize * 0.25f, halfH * 0.14f + glyphHalfH, textPaint)
        }
        textPaint.color = eyeColor
        drawChar(canvas, '°', halfL * 0.84f - glyphSize * 0.22f, -halfH * 0.18f + feelerSway + glyphHalfH, textPaint)
    }

    /**
     * Bioluminescent Night Moon Jellyfish: pulsing dome bell + undulating ASCII tentacles.
     */
    private fun drawJellyfish(
        canvas: Canvas,
        textPaint: Paint,
        halfL: Float,
        halfH: Float,
        bodyColor: Int,
        accentColor: Int,
        eyeColor: Int,
        bodyAlpha: Int,
        outlineAlpha: Int,
        animPhase: Float,
        glyphSize: Float,
        isBatterySaver: Boolean
    ) {
        val pulse = sin(animPhase)
        val bellRx = halfL * (0.76f + 0.14f * pulse)
        val bellRy = halfH * (0.68f - 0.12f * pulse)

        // Pulsing translucent dome bell
        bodyPath.reset()
        bodyPath.moveTo(-bellRx, 0f)
        bodyPath.quadTo(-bellRx * 0.92f, -bellRy * 1.45f, 0f, -bellRy * 1.45f)
        bodyPath.quadTo(bellRx * 0.92f, -bellRy * 1.45f, bellRx, 0f)
        bodyPath.quadTo(0f, bellRy * 0.25f, -bellRx, 0f)
        bodyPath.close()

        if (bodyAlpha > 0) {
            fillPaint.color = (bodyColor and 0x00FFFFFF) or ((bodyAlpha + 14).coerceAtMost(95) shl 24)
            canvas.drawPath(bodyPath, fillPaint)
        }
        strokePaint.strokeWidth = 1.4f
        strokePaint.color = (bodyColor and 0x00FFFFFF) or (outlineAlpha shl 24)
        canvas.drawPath(bodyPath, strokePaint)

        // Trailing undulating ASCII tentacles
        val tCount = if (isBatterySaver) 3 else 5
        textPaint.color = accentColor
        for (t in 0 until tCount) {
            val frac = (t / (tCount - 1f).coerceAtLeast(1f)) - 0.5f
            val baseX = frac * bellRx * 1.35f
            for (step in 1..4) {
                val sway = sin(animPhase * 1.2f - step * 0.7f + t * 1.1f) * (glyphSize * 0.35f)
                val ch = when {
                    step == 4 -> '·'
                    sway >= 0f -> ')'
                    else -> '('
                }
                drawChar(canvas, ch, baseX + sway - glyphSize * 0.25f, step * glyphSize * 0.78f, textPaint)
            }
        }
        textPaint.color = eyeColor
        drawChar(canvas, '°', -bellRx * 0.28f, -bellRy * 0.45f, textPaint)
        drawChar(canvas, '°', bellRx * 0.12f, -bellRy * 0.45f, textPaint)
    }

    private fun drawChar(canvas: Canvas, ch: Char, x: Float, y: Float, paint: Paint) {
        singleCharBuf[0] = ch
        canvas.drawText(singleCharBuf, 0, 1, x, y, paint)
    }
}

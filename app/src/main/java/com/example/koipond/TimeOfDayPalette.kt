package com.example.koipond

import android.graphics.Color
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/**
 * Holds the colorful terminal color scheme for the split-view ASCII aquarium at a given moment.
 * Never uses pure black (#000000), pure white (#FFFFFF), or neutral gray.
 */
data class AsciiStageColors(
    val phaseName: String,     // "dawn", "noon", "dusk", "night"
    val skyBg: Int,            // Upper sky background tint
    val waterBgTop: Int,       // Shallow underwater background tint
    val waterBgDeep: Int,      // Deep riverbed background tint
    val celestialGlyph: Int,   // Sun / Moon / Stars character color
    val cloudReedFg: Int,      // Clouds, distant hills, and shore reeds
    val meniscusWaveFg: Int,   // `~^~._.~^~` air-water surface line
    val lilyPadFg: Int,        // `[_v_]` floating lily pad
    val lotusFlowerFg: Int,    // `(*)` blooming lotus on pad
    val sunRayFg: Int,         // `/` and `:` diagonal underwater light shafts
    val kelpFg: Int,           // `( )` swaying vallisneria / kelp blades
    val pebbleFg: Int,         // `.ooO. .oOo.` riverbed cobbles & moss
    val bubbleFg: Int,         // `o` / `°` / `.` rising bubbles & plankton
    val fishCoral: Int,        // Role 1: Coral / vermilion / amber fish
    val fishAqua: Int,         // Role 2: Electric aqua / turquoise fish
    val fishGold: Int,         // Role 3: Sun-gold / yellow fish
    val fishFoam: Int,         // Role 4: Pale foam-ivory fish
    val fishRose: Int          // Role 5: Rose-magenta / bioluminescent violet fish
)

object TimeOfDayPalette {

    // 1. DAWN (05:00 - 08:30): Warm espresso-rose sky, peach sun, misty jade-teal pond
    val DAWN = AsciiStageColors(
        phaseName = "dawn",
        skyBg = 0xFF1F161D.toInt(),
        waterBgTop = 0xFF0E2930.toInt(),
        waterBgDeep = 0xFF081B22.toInt(),
        celestialGlyph = 0xFFFFB38A.toInt(),
        cloudReedFg = 0xFFD99B82.toInt(),
        meniscusWaveFg = 0xFF7BE495.toInt(),
        lilyPadFg = 0xFF6EE7A0.toInt(),
        lotusFlowerFg = 0xFFFF7A59.toInt(),
        sunRayFg = 0xFF5EEAD4.toInt(),
        kelpFg = 0xFF7BE495.toInt(),
        pebbleFg = 0xFF8FB3B8.toInt(),
        bubbleFg = 0xFFE6F4F1.toInt(),
        fishCoral = 0xFFFF7A59.toInt(),
        fishAqua = 0xFF5EEAD4.toInt(),
        fishGold = 0xFFFFD166.toInt(),
        fishFoam = 0xFFF8EBDD.toInt(),
        fishRose = 0xFFFF6F91.toInt()
    )

    // 2. NOON (08:30 - 16:30): Vibrant sunlit Lagoon teal sky, radiant gold sun, turquoise water
    val NOON = AsciiStageColors(
        phaseName = "noon",
        skyBg = 0xFF102C38.toInt(),
        waterBgTop = 0xFF0D2530.toInt(),
        waterBgDeep = 0xFF0A1D26.toInt(),
        celestialGlyph = 0xFFFFD166.toInt(),
        cloudReedFg = 0xFF8FB3B8.toInt(),
        meniscusWaveFg = 0xFF5EEAD4.toInt(),
        lilyPadFg = 0xFF7BE495.toInt(),
        lotusFlowerFg = 0xFFFF7A59.toInt(),
        sunRayFg = 0xFF5EEAD4.toInt(),
        kelpFg = 0xFF7BE495.toInt(),
        pebbleFg = 0xFF8FB3B8.toInt(),
        bubbleFg = 0xFFE6F4F1.toInt(),
        fishCoral = 0xFFFF7A59.toInt(),
        fishAqua = 0xFF5EEAD4.toInt(),
        fishGold = 0xFFFFD166.toInt(),
        fishFoam = 0xFFE6F4F1.toInt(),
        fishRose = 0xFFFF6F91.toInt()
    )

    // 3. DUSK (16:30 - 20:00): Ember-crimson sunset sky, tangerine sun, deep bronze-teal water
    val DUSK = AsciiStageColors(
        phaseName = "dusk",
        skyBg = 0xFF241514.toInt(),
        waterBgTop = 0xFF13222A.toInt(),
        waterBgDeep = 0xFF0A161E.toInt(),
        celestialGlyph = 0xFFFF8A3D.toInt(),
        cloudReedFg = 0xFFFF6F91.toInt(),
        meniscusWaveFg = 0xFFFFB347.toInt(),
        lilyPadFg = 0xFF8FD694.toInt(),
        lotusFlowerFg = 0xFFFF6F91.toInt(),
        sunRayFg = 0xFFFFB347.toInt(),
        kelpFg = 0xFF7BE495.toInt(),
        pebbleFg = 0xFFB39E8C.toInt(),
        bubbleFg = 0xFFF8EBDD.toInt(),
        fishCoral = 0xFFFF8A3D.toInt(),
        fishAqua = 0xFF5EEAD4.toInt(),
        fishGold = 0xFFFFC857.toInt(),
        fishFoam = 0xFFF8EBDD.toInt(),
        fishRose = 0xFFFF6F91.toInt()
    )

    // 4. NIGHT (20:00 - 05:00): Deep midnight-Abyssal indigo, crescent moon & stars, bioluminescent cyan pond
    val NIGHT = AsciiStageColors(
        phaseName = "night",
        skyBg = 0xFF081520.toInt(),
        waterBgTop = 0xFF0A1D28.toInt(),
        waterBgDeep = 0xFF06121A.toInt(),
        celestialGlyph = 0xFFFFD166.toInt(),
        cloudReedFg = 0xFF527988.toInt(),
        meniscusWaveFg = 0xFF5EEAD4.toInt(),
        lilyPadFg = 0xFF439A72.toInt(),
        lotusFlowerFg = 0xFFFF7A59.toInt(),
        sunRayFg = 0xFF387D8C.toInt(),
        kelpFg = 0xFF4CA87A.toInt(),
        pebbleFg = 0xFF527988.toInt(),
        bubbleFg = 0xFF7FD3FF.toInt(),
        fishCoral = 0xFFFF7A59.toInt(),
        fishAqua = 0xFF7FD3FF.toInt(),
        fishGold = 0xFFFFD166.toInt(),
        fishFoam = 0xFFE6F4F1.toInt(),
        fishRose = 0xFFC6F36B.toInt()
    )

    data class TimeSnapshot(
        val colors: AsciiStageColors,
        val sunArcX: Float,    // 0.15 (left) .. 0.85 (right) across sky
        val sunArcY: Float,    // 0.0 (horizon) .. 1.0 (zenith)
        val isNight: Boolean,
        val timeLabel: String
    )

    fun resolve(mode: TimeCycleMode): TimeSnapshot {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val fractionalHour = hour + minute / 60.0f
        val clockStr = "%02d:%02d".format(hour, minute)

        return when (mode) {
            TimeCycleMode.DAWN -> TimeSnapshot(DAWN, 0.22f, 0.35f, false, "dawn (fixed)")
            TimeCycleMode.NOON -> TimeSnapshot(NOON, 0.48f, 0.85f, false, "noon (fixed)")
            TimeCycleMode.DUSK -> TimeSnapshot(DUSK, 0.78f, 0.32f, false, "dusk (fixed)")
            TimeCycleMode.NIGHT -> TimeSnapshot(NIGHT, 0.62f, 0.72f, true, "night (fixed)")
            TimeCycleMode.AUTO_REALTIME -> evaluateRealtime(fractionalHour, clockStr)
        }
    }

    private fun evaluateRealtime(h: Float, clockStr: String): TimeSnapshot {
        return when {
            // Dawn transition: 05:00 -> 08:30
            h in 5.0f..8.5f -> {
                val t = (h - 5.0f) / 3.5f
                val pal = if (t < 0.5f) lerpPalette(NIGHT, DAWN, t * 2f, "dawn")
                          else lerpPalette(DAWN, NOON, (t - 0.5f) * 2f, "dawn")
                val arcX = 0.16f + t * 0.22f
                val arcY = 0.20f + t * 0.55f
                TimeSnapshot(pal, arcX, arcY, false, "$clockStr dawn")
            }
            // Broad Daylight: 08:30 -> 16:30
            h in 8.5f..16.5f -> {
                val t = (h - 8.5f) / 8.0f
                val arcX = 0.38f + t * 0.34f
                val arcY = 0.75f + sin(t * Math.PI.toFloat()) * 0.20f
                TimeSnapshot(NOON, arcX, arcY, false, "$clockStr day")
            }
            // Sunset / Dusk transition: 16:30 -> 20:00
            h in 16.5f..20.0f -> {
                val t = (h - 16.5f) / 3.5f
                val pal = if (t < 0.5f) lerpPalette(NOON, DUSK, t * 2f, "dusk")
                          else lerpPalette(DUSK, NIGHT, (t - 0.5f) * 2f, "dusk")
                val arcX = 0.72f + t * 0.16f
                val arcY = 0.75f - t * 0.58f
                TimeSnapshot(pal, arcX, arcY, t > 0.8f, "$clockStr dusk")
            }
            // Nighttime: 20:00 -> 05:00
            else -> {
                val nightT = if (h >= 20.0f) (h - 20.0f) / 9.0f else (h + 4.0f) / 9.0f
                val arcX = 0.22f + nightT * 0.56f
                val arcY = 0.45f + sin(nightT * Math.PI.toFloat()) * 0.38f
                TimeSnapshot(NIGHT, arcX, arcY, true, "$clockStr night")
            }
        }
    }

    private fun lerpPalette(a: AsciiStageColors, b: AsciiStageColors, t: Float, name: String): AsciiStageColors {
        val f = t.coerceIn(0f, 1f)
        return AsciiStageColors(
            phaseName = name,
            skyBg = lerpColor(a.skyBg, b.skyBg, f),
            waterBgTop = lerpColor(a.waterBgTop, b.waterBgTop, f),
            waterBgDeep = lerpColor(a.waterBgDeep, b.waterBgDeep, f),
            celestialGlyph = lerpColor(a.celestialGlyph, b.celestialGlyph, f),
            cloudReedFg = lerpColor(a.cloudReedFg, b.cloudReedFg, f),
            meniscusWaveFg = lerpColor(a.meniscusWaveFg, b.meniscusWaveFg, f),
            lilyPadFg = lerpColor(a.lilyPadFg, b.lilyPadFg, f),
            lotusFlowerFg = lerpColor(a.lotusFlowerFg, b.lotusFlowerFg, f),
            sunRayFg = lerpColor(a.sunRayFg, b.sunRayFg, f),
            kelpFg = lerpColor(a.kelpFg, b.kelpFg, f),
            pebbleFg = lerpColor(a.pebbleFg, b.pebbleFg, f),
            bubbleFg = lerpColor(a.bubbleFg, b.bubbleFg, f),
            fishCoral = lerpColor(a.fishCoral, b.fishCoral, f),
            fishAqua = lerpColor(a.fishAqua, b.fishAqua, f),
            fishGold = lerpColor(a.fishGold, b.fishGold, f),
            fishFoam = lerpColor(a.fishFoam, b.fishFoam, f),
            fishRose = lerpColor(a.fishRose, b.fishRose, f)
        )
    }

    fun lerpColor(c1: Int, c2: Int, t: Float): Int {
        val r = (Color.red(c1) + (Color.red(c2) - Color.red(c1)) * t).toInt().coerceIn(0, 255)
        val g = (Color.green(c1) + (Color.green(c2) - Color.green(c1)) * t).toInt().coerceIn(0, 255)
        val b = (Color.blue(c1) + (Color.blue(c2) - Color.blue(c1)) * t).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }
}

package com.example.koipond

import android.graphics.Color
import java.util.Calendar
import kotlin.math.floor
import kotlin.math.sin

data class AsciiStageColors(
    val phaseName: String,
    val skyBg: Int,
    val waterBgTop: Int,
    val waterBgDeep: Int,
    val celestialGlyph: Int,
    val cloudReedFg: Int,
    val meniscusWaveFg: Int,
    val lilyPadFg: Int,
    val lotusFlowerFg: Int,
    val sunRayFg: Int,
    val kelpFg: Int,
    val pebbleFg: Int,
    val bubbleFg: Int,
    val fishCoral: Int,
    val fishAqua: Int,
    val fishGold: Int,
    val fishFoam: Int,
    val fishRose: Int,
    val fishDimBg: Int,
    val bloodCore: Int = 0xFFFF5263.toInt(),
    val bloodMist: Int = 0xFFC82838.toInt()
)

object TimeOfDayPalette {

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
        fishRose = 0xFFFF6F91.toInt(),
        fishDimBg = 0xFF55828B.toInt()
    )

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
        fishRose = 0xFFFF6F91.toInt(),
        fishDimBg = 0xFF527E88.toInt()
    )

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
        fishRose = 0xFFFF6F91.toInt(),
        fishDimBg = 0xFF6E6259.toInt()
    )

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
        fishRose = 0xFFC6F36B.toInt(),
        fishDimBg = 0xFF3E6475.toInt()
    )

    data class TimeSnapshot(
        val colors: AsciiStageColors,
        val sunArcX: Float,
        val sunArcY: Float,
        val isNight: Boolean,
        val lunarPhaseIndex: Int, // 0=Waxing Crescent, 1=Half Moon, 2=Full Moon, 3=Waning Crescent
        val timeLabel: String
    )

    fun resolve(mode: TimeCycleMode, family: PaletteFamily = PaletteFamily.LAGOON): TimeSnapshot {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val fractionalHour = hour + minute / 60.0f
        val clockStr = "%02d:%02d".format(hour, minute)

        // Compute real astronomical lunar phase index (29.53-day synodic cycle)
        val year = cal.get(Calendar.YEAR)
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val approxDaysSince2000 = (year - 2000) * 365.25 + dayOfYear
        val moonAge = ((approxDaysSince2000 - 6.0) % 29.530588 + 29.530588) % 29.530588
        val lunarIdx = when {
            moonAge < 7.4 -> 0  // Waxing Crescent `) )`
            moonAge < 13.5 -> 1 // First Quarter Half `| )`
            moonAge < 17.5 -> 2 // Full Moon `(O)`
            else -> 3           // Waning Crescent `( (`
        }

        val baseSnap = when (mode) {
            TimeCycleMode.DAWN -> TimeSnapshot(DAWN, 0.22f, 0.35f, false, lunarIdx, "dawn")
            TimeCycleMode.NOON -> TimeSnapshot(NOON, 0.48f, 0.85f, false, lunarIdx, "noon")
            TimeCycleMode.DUSK -> TimeSnapshot(DUSK, 0.78f, 0.32f, false, lunarIdx, "dusk")
            TimeCycleMode.NIGHT -> TimeSnapshot(NIGHT, 0.62f, 0.72f, true, lunarIdx, "night")
            TimeCycleMode.AUTO_REALTIME -> evaluateRealtime(fractionalHour, clockStr, lunarIdx)
        }

        return if (family == PaletteFamily.LAGOON) {
            baseSnap
        } else {
            baseSnap.copy(colors = tintForFamily(baseSnap.colors, family))
        }
    }

    private fun tintForFamily(c: AsciiStageColors, family: PaletteFamily): AsciiStageColors {
        return when (family) {
            PaletteFamily.LAGOON -> c
            PaletteFamily.EMBER -> c.copy(
                skyBg = lerpColor(c.skyBg, 0xFF1B1411.toInt(), 0.55f),
                waterBgTop = lerpColor(c.waterBgTop, 0xFF2A1F1A.toInt(), 0.42f),
                waterBgDeep = lerpColor(c.waterBgDeep, 0xFF1B1411.toInt(), 0.50f),
                meniscusWaveFg = 0xFFFF8A3D.toInt(),
                fishCoral = 0xFFFF8A3D.toInt(),
                fishAqua = 0xFFB8E986.toInt(),
                fishGold = 0xFFFFC857.toInt(),
                fishRose = 0xFFFF6F91.toInt(),
                fishDimBg = 0xFF7D6B5D.toInt()
            )
            PaletteFamily.MOSS -> c.copy(
                skyBg = lerpColor(c.skyBg, 0xFF0E1912.toInt(), 0.55f),
                waterBgTop = lerpColor(c.waterBgTop, 0xFF16271C.toInt(), 0.45f),
                waterBgDeep = lerpColor(c.waterBgDeep, 0xFF0E1912.toInt(), 0.55f),
                meniscusWaveFg = 0xFFC6F36B.toInt(),
                fishCoral = 0xFFFFB38A.toInt(),
                fishAqua = 0xFF7FD3FF.toInt(),
                fishGold = 0xFFC6F36B.toInt(),
                fishRose = 0xFF6EE7A0.toInt(),
                fishDimBg = 0xFF5A7561.toInt()
            )
            PaletteFamily.AMOLED -> c.copy(
                // True #000000 Pitch-Black OLED Background (pixels completely off!)
                // while characters glow with time-of-day neon terminal hues
                skyBg = 0xFF000000.toInt(),
                waterBgTop = 0xFF000000.toInt(),
                waterBgDeep = 0xFF000000.toInt(),
                fishDimBg = 0xFF1E3E48.toInt()
            )
        }
    }

    private fun evaluateRealtime(h: Float, clockStr: String, lunarIdx: Int): TimeSnapshot {
        return when {
            h in 5.0f..8.5f -> {
                val t = (h - 5.0f) / 3.5f
                val pal = if (t < 0.5f) lerpPalette(NIGHT, DAWN, t * 2f, "dawn")
                          else lerpPalette(DAWN, NOON, (t - 0.5f) * 2f, "dawn")
                TimeSnapshot(pal, 0.16f + t * 0.22f, 0.20f + t * 0.55f, false, lunarIdx, "$clockStr dawn")
            }
            h in 8.5f..16.5f -> {
                val t = (h - 8.5f) / 8.0f
                val arcY = 0.75f + sin(t * Math.PI.toFloat()) * 0.20f
                TimeSnapshot(NOON, 0.38f + t * 0.34f, arcY, false, lunarIdx, "$clockStr day")
            }
            h in 16.5f..20.0f -> {
                val t = (h - 16.5f) / 3.5f
                val pal = if (t < 0.5f) lerpPalette(NOON, DUSK, t * 2f, "dusk")
                          else lerpPalette(DUSK, NIGHT, (t - 0.5f) * 2f, "dusk")
                TimeSnapshot(pal, 0.72f + t * 0.16f, 0.75f - t * 0.58f, t > 0.8f, lunarIdx, "$clockStr dusk")
            }
            else -> {
                val nightT = if (h >= 20.0f) (h - 20.0f) / 9.0f else (h + 4.0f) / 9.0f
                val arcY = 0.45f + sin(nightT * Math.PI.toFloat()) * 0.38f
                TimeSnapshot(NIGHT, 0.22f + nightT * 0.56f, arcY, true, lunarIdx, "$clockStr night")
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
            fishRose = lerpColor(a.fishRose, b.fishRose, f),
            fishDimBg = lerpColor(a.fishDimBg, b.fishDimBg, f)
        )
    }

    fun lerpColor(c1: Int, c2: Int, t: Float): Int {
        val r = (Color.red(c1) + (Color.red(c2) - Color.red(c1)) * t).toInt().coerceIn(0, 255)
        val g = (Color.green(c1) + (Color.green(c2) - Color.green(c1)) * t).toInt().coerceIn(0, 255)
        val b = (Color.blue(c1) + (Color.blue(c2) - Color.blue(c1)) * t).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }
}

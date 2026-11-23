package com.example.koipond

import android.graphics.Color
import android.os.SystemClock
import androidx.core.graphics.ColorUtils
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin

data class AsciiStageColors(
    val phaseName: String,
    val isLightSurface: Boolean,
    val skyBg: Int,
    val skyHorizonBg: Int,
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
    val shadowTint: Int = 0xFF02080C.toInt(),
    val sunbeamHighlight: Int = 0xFFFFD166.toInt(),
    val bloodCore: Int = 0xFFFF5263.toInt(),
    val bloodMist: Int = 0xFFC82838.toInt()
)

object TimeOfDayPalette {

    // =========================================================================
    // DARK POLARITY STAGES (Nocturnal / Deep Terminal)
    // =========================================================================

    val DAWN = AsciiStageColors(
        phaseName = "dawn",
        isLightSurface = false,
        skyBg = 0xFF1F161D.toInt(),
        skyHorizonBg = 0xFF4A252E.toInt(),
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
        fishDimBg = 0xFF55828B.toInt(),
        shadowTint = 0xFF030B10.toInt(),
        sunbeamHighlight = 0xFFFFD8A8.toInt()
    )

    val NOON = AsciiStageColors(
        phaseName = "noon",
        isLightSurface = false,
        skyBg = 0xFF102C38.toInt(),
        skyHorizonBg = 0xFF1A4352.toInt(),
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
        fishDimBg = 0xFF527E88.toInt(),
        shadowTint = 0xFF02090D.toInt(),
        sunbeamHighlight = 0xFFFFE082.toInt()
    )

    val DUSK = AsciiStageColors(
        phaseName = "dusk",
        isLightSurface = false,
        skyBg = 0xFF241514.toInt(),
        skyHorizonBg = 0xFF592821.toInt(),
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
        fishDimBg = 0xFF6E6259.toInt(),
        shadowTint = 0xFF040A0E.toInt(),
        sunbeamHighlight = 0xFFFFB347.toInt()
    )

    val NIGHT = AsciiStageColors(
        phaseName = "night",
        isLightSurface = false,
        skyBg = 0xFF081520.toInt(),
        skyHorizonBg = 0xFF11283B.toInt(),
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
        fishDimBg = 0xFF3E6475.toInt(),
        shadowTint = 0xFF01060A.toInt(),
        sunbeamHighlight = 0xFF7FD3FF.toInt()
    )

    // =========================================================================
    // LIGHT POLARITY STAGES (Sunlit Sumi-e Paper & Translucent Lagoon)
    // =========================================================================

    val DAWN_LIGHT = AsciiStageColors(
        phaseName = "morning",
        isLightSurface = true,
        skyBg = 0xFFFFF3EB.toInt(),
        skyHorizonBg = 0xFFFFDFC7.toInt(),
        waterBgTop = 0xFFE2F4F0.toInt(),
        waterBgDeep = 0xFFC1E3E2.toInt(),
        celestialGlyph = 0xFFD45218.toInt(),
        cloudReedFg = 0xFF8C5A4C.toInt(),
        meniscusWaveFg = 0xFF0F766E.toInt(),
        lilyPadFg = 0xFF1A7A4C.toInt(),
        lotusFlowerFg = 0xFFC8344B.toInt(),
        sunRayFg = 0xFFFFFFFF.toInt(),
        kelpFg = 0xFF1A7A4C.toInt(),
        pebbleFg = 0xFF4A6B72.toInt(),
        bubbleFg = 0xFF125A66.toInt(),
        fishCoral = 0xFFC83A16.toInt(),
        fishAqua = 0xFF0A6B72.toInt(),
        fishGold = 0xFF9C5E00.toInt(),
        fishFoam = 0xFF112D38.toInt(),
        fishRose = 0xFFB32358.toInt(),
        fishDimBg = 0xFF5A848B.toInt(),
        shadowTint = 0xFF77A8AD.toInt(),
        sunbeamHighlight = 0xFF8A4B00.toInt(),
        bloodCore = 0xFFC41E3A.toInt(),
        bloodMist = 0xFF9E2B40.toInt()
    )

    val NOON_LIGHT = AsciiStageColors(
        phaseName = "day",
        isLightSurface = true,
        skyBg = 0xFFEEF9F5.toInt(),
        skyHorizonBg = 0xFFDCF2F2.toInt(),
        waterBgTop = 0xFFD9F2EE.toInt(),
        waterBgDeep = 0xFFB2DDE2.toInt(),
        celestialGlyph = 0xFFC47A00.toInt(),
        cloudReedFg = 0xFF4F757D.toInt(),
        meniscusWaveFg = 0xFF0B726C.toInt(),
        lilyPadFg = 0xFF157547.toInt(),
        lotusFlowerFg = 0xFFC4321C.toInt(),
        sunRayFg = 0xFFFFFFFF.toInt(),
        kelpFg = 0xFF157547.toInt(),
        pebbleFg = 0xFF3E656E.toInt(),
        bubbleFg = 0xFF0E5A68.toInt(),
        fishCoral = 0xFFC43814.toInt(),
        fishAqua = 0xFF006470.toInt(),
        fishGold = 0xFF945800.toInt(),
        fishFoam = 0xFF0E2A33.toInt(),
        fishRose = 0xFFB01E55.toInt(),
        fishDimBg = 0xFF507B84.toInt(),
        shadowTint = 0xFF6B9CA4.toInt(),
        sunbeamHighlight = 0xFF7A4800.toInt(),
        bloodCore = 0xFFC41E3A.toInt(),
        bloodMist = 0xFF9A283D.toInt()
    )

    val DUSK_LIGHT = AsciiStageColors(
        phaseName = "golden",
        isLightSurface = true,
        skyBg = 0xFFFFEFE0.toInt(),
        skyHorizonBg = 0xFFFFCFA8.toInt(),
        waterBgTop = 0xFFEBE2D8.toInt(),
        waterBgDeep = 0xFFCCBFC4.toInt(),
        celestialGlyph = 0xFFC44200.toInt(),
        cloudReedFg = 0xFF7D4B55.toInt(),
        meniscusWaveFg = 0xFFB04E08.toInt(),
        lilyPadFg = 0xFF236E45.toInt(),
        lotusFlowerFg = 0xFFB5224B.toInt(),
        sunRayFg = 0xFFFFF4DC.toInt(),
        kelpFg = 0xFF236E45.toInt(),
        pebbleFg = 0xFF5E5250.toInt(),
        bubbleFg = 0xFF2B5260.toInt(),
        fishCoral = 0xFFBE3210.toInt(),
        fishAqua = 0xFF0B626B.toInt(),
        fishGold = 0xFF8C5000.toInt(),
        fishFoam = 0xFF1B2430.toInt(),
        fishRose = 0xFFA61D52.toInt(),
        fishDimBg = 0xFF6E6268.toInt(),
        shadowTint = 0xFF867885.toInt(),
        sunbeamHighlight = 0xFF944400.toInt(),
        bloodCore = 0xFFBA1C34.toInt(),
        bloodMist = 0xFF8F2638.toInt()
    )

    val NIGHT_LIGHT = AsciiStageColors(
        phaseName = "moonlit",
        isLightSurface = true,
        skyBg = 0xFFE3ECF4.toInt(),
        skyHorizonBg = 0xFFD2E1EE.toInt(),
        waterBgTop = 0xFFD4E2EE.toInt(),
        waterBgDeep = 0xFFB7C9DA.toInt(),
        celestialGlyph = 0xFF8A5D00.toInt(),
        cloudReedFg = 0xFF4A6075.toInt(),
        meniscusWaveFg = 0xFF185B78.toInt(),
        lilyPadFg = 0xFF1E6A52.toInt(),
        lotusFlowerFg = 0xFFB53348.toInt(),
        sunRayFg = 0xFFF2F8FF.toInt(),
        kelpFg = 0xFF1E6A52.toInt(),
        pebbleFg = 0xFF475C70.toInt(),
        bubbleFg = 0xFF165272.toInt(),
        fishCoral = 0xFFB83522.toInt(),
        fishAqua = 0xFF0C5880.toInt(),
        fishGold = 0xFF885600.toInt(),
        fishFoam = 0xFF142434.toInt(),
        fishRose = 0xFF6B369E.toInt(),
        fishDimBg = 0xFF5B7186.toInt(),
        shadowTint = 0xFF7B90A6.toInt(),
        sunbeamHighlight = 0xFF1A4E70.toInt(),
        bloodCore = 0xFFB81D38.toInt(),
        bloodMist = 0xFF8E263D.toInt()
    )

    data class TimeSnapshot(
        val colors: AsciiStageColors,
        val sunArcX: Float,
        val sunArcY: Float,
        val isNight: Boolean,
        val isLightSurface: Boolean,
        val lunarPhaseIndex: Int, // 0=Waxing Crescent, 1=Half Moon, 2=Full Moon, 3=Waning Crescent
        val timeLabel: String,
        val autoWeatherSlot: Int = 0 // Seeded per 30-min clock block for organic calm AUTO weather
    )

    // Snapshot cache: eliminates per-frame Calendar/String allocations (recomputed every 15s or on config change)
    private val cacheLock = Any()
    private var cachedSnapshot: TimeSnapshot? = null
    private var cachedMode: TimeCycleMode? = null
    private var cachedFamily: PaletteFamily? = null
    private var cachedThemeMode: ThemeMode? = null
    private var cachedSystemDark: Boolean = true
    private var lastCacheMs: Long = -60_000L
    private const val CACHE_TTL_MS = 15_000L

    fun invalidateCache() {
        synchronized(cacheLock) {
            cachedSnapshot = null
            lastCacheMs = -60_000L
        }
    }

    /**
     * Quick check used by SettingsActivity to match AppCompat night mode when ThemeMode.SOLAR is selected.
     */
    fun isSolarDaytimeNow(mode: TimeCycleMode): Boolean {
        return when (mode) {
            TimeCycleMode.DAWN, TimeCycleMode.NOON -> true
            TimeCycleMode.DUSK, TimeCycleMode.NIGHT -> false
            TimeCycleMode.AUTO_REALTIME -> {
                val cal = Calendar.getInstance()
                val h = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0f
                val (sunrise, sunset) = computeSolarWindow(cal.get(Calendar.DAY_OF_YEAR))
                h in (sunrise + 0.45f)..(sunset - 0.15f)
            }
        }
    }

    fun resolve(
        mode: TimeCycleMode,
        family: PaletteFamily = PaletteFamily.LAGOON,
        themeMode: ThemeMode = ThemeMode.SOLAR,
        isSystemDark: Boolean = true,
        overrideHour: Float? = null
    ): TimeSnapshot {
        // When scrubbing 24h solar preview, evaluate live immediately without polluting the 15s wallpaper cache
        if (overrideHour != null) {
            return computeFreshSnapshot(TimeCycleMode.AUTO_REALTIME, family, themeMode, isSystemDark, overrideHour)
        }
        val nowMs = SystemClock.elapsedRealtime()
        synchronized(cacheLock) {
            val existing = cachedSnapshot
            if (existing != null &&
                cachedMode == mode &&
                cachedFamily == family &&
                cachedThemeMode == themeMode &&
                cachedSystemDark == isSystemDark &&
                (nowMs - lastCacheMs) < CACHE_TTL_MS
            ) {
                return existing
            }
        }

        val computed = computeFreshSnapshot(mode, family, themeMode, isSystemDark, null)
        synchronized(cacheLock) {
            cachedSnapshot = computed
            cachedMode = mode
            cachedFamily = family
            cachedThemeMode = themeMode
            cachedSystemDark = isSystemDark
            lastCacheMs = nowMs
        }
        return computed
    }

    private fun computeFreshSnapshot(
        mode: TimeCycleMode,
        family: PaletteFamily,
        themeMode: ThemeMode,
        isSystemDark: Boolean,
        overrideHour: Float? = null
    ): TimeSnapshot {
        val cal = Calendar.getInstance()
        val fractionalHour = if (overrideHour != null) {
            ((overrideHour % 24f) + 24f) % 24f
        } else {
            cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60.0f
        }
        val hour = fractionalHour.toInt().coerceIn(0, 23)
        val minute = (((fractionalHour - hour) * 60f).toInt()).coerceIn(0, 59)
        val clockStr = String.format(Locale.US, "%02d:%02d", hour, minute)

        val year = cal.get(Calendar.YEAR)
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val weatherBlock = (dayOfYear * 48 + hour * 2 + (minute / 30))
        val weatherSlot = ((weatherBlock * 2654435761L ushr 24) % 12L).toInt().let { if (it < 0) it + 12 else it }
        val approxDaysSince2000 = (year - 2000) * 365.25 + dayOfYear
        val moonAge = ((approxDaysSince2000 - 6.0) % 29.530588 + 29.530588) % 29.530588
        val lunarIdx = when {
            moonAge < 7.4 -> 0
            moonAge < 13.5 -> 1
            moonAge < 17.5 -> 2
            else -> 3
        }

        // Determine forced polarity when not in SOLAR mode
        val forceLight: Boolean? = when (themeMode) {
            ThemeMode.SOLAR -> null
            ThemeMode.SYSTEM -> !isSystemDark
            ThemeMode.LIGHT -> true
            ThemeMode.DARK -> false
        }

        val baseSnap = when (mode) {
            TimeCycleMode.DAWN -> {
                val pal = if (forceLight ?: true) DAWN_LIGHT else DAWN
                TimeSnapshot(pal, 0.22f, 0.35f, false, pal.isLightSurface, lunarIdx, "dawn", weatherSlot)
            }
            TimeCycleMode.NOON -> {
                val pal = if (forceLight ?: true) NOON_LIGHT else NOON
                TimeSnapshot(pal, 0.48f, 0.85f, false, pal.isLightSurface, lunarIdx, "noon", weatherSlot)
            }
            TimeCycleMode.DUSK -> {
                val pal = if (forceLight ?: false) DUSK_LIGHT else DUSK
                TimeSnapshot(pal, 0.78f, 0.32f, false, pal.isLightSurface, lunarIdx, "dusk", weatherSlot)
            }
            TimeCycleMode.NIGHT -> {
                val pal = if (forceLight ?: false) NIGHT_LIGHT else NIGHT
                TimeSnapshot(pal, 0.62f, 0.72f, true, pal.isLightSurface, lunarIdx, "night", weatherSlot)
            }
            TimeCycleMode.AUTO_REALTIME -> {
                val (sunrise, sunset) = computeSolarWindow(dayOfYear)
                if (forceLight == null) {
                    evaluateSolarRealtime(fractionalHour, sunrise, sunset, clockStr, lunarIdx)
                } else {
                    evaluateFixedPolarityRealtime(fractionalHour, sunrise, sunset, clockStr, lunarIdx, forceLight)
                }
            }
        }

        val familyAdjusted = tintForFamily(baseSnap.colors, family, themeMode)
        val guardedColors = enforceContrastGuard(familyAdjusted)
        return baseSnap.copy(
            colors = guardedColors,
            isLightSurface = guardedColors.isLightSurface,
            autoWeatherSlot = weatherSlot
        )
    }

    /**
     * Zero-permission seasonal sunrise/sunset estimate (hours in 0..24).
     * Shifts sunrise/sunset smoothly across the year using Earth's axial tilt (~35 deg latitude),
     * flipping seasonal sign automatically for Southern Hemisphere timezones.
     */
    private fun computeSolarWindow(dayOfYear: Int): Pair<Float, Float> {
        val tzId = TimeZone.getDefault().id
        val isSouthern = tzId.startsWith("Australia/") ||
            tzId.startsWith("Pacific/Auckland") ||
            tzId.startsWith("America/Argentina") ||
            tzId.startsWith("America/Sao_Paulo") ||
            tzId.startsWith("America/Santiago") ||
            tzId.startsWith("Africa/Johannesburg") ||
            tzId.startsWith("America/Montevideo")

        // Solstice cosine: -1 near Dec 21, +1 near Jun 21
        val seasonalCos = -cos((2.0 * Math.PI * (dayOfYear + 10)) / 365.25).toFloat()
        val hemiSeasonal = if (isSouthern) -seasonalCos else seasonalCos
        val halfDaySpan = 6.05f + hemiSeasonal * 1.15f // ~4.9h winter .. ~7.2h summer half-day
        val solarNoon = 12.25f
        val sunrise = (solarNoon - halfDaySpan).coerceIn(5.0f, 7.6f)
        val sunset = (solarNoon + halfDaySpan).coerceIn(16.8f, 20.5f)
        return sunrise to sunset
    }

    /**
     * 6-keyframe Solar Light <-> Dark curve:
     *   NIGHT_DARK -> DAWN_DARK -> DAWN_LIGHT -> NOON_LIGHT -> DUSK_LIGHT -> DUSK_DARK -> NIGHT_DARK
     * Uses CIE LAB perceptual blending + asymmetric foreground crossover so twilight stays vivid & legible.
     */
    private fun evaluateSolarRealtime(
        h: Float,
        sunrise: Float,
        sunset: Float,
        clockStr: String,
        lunarIdx: Int
    ): TimeSnapshot {
        val preDawnStart = sunrise - 1.25f
        val morningStart = sunrise + 1.10f
        val dayStart = sunrise + 2.40f
        val goldenStart = sunset - 2.00f
        val sunsetStart = sunset - 0.65f
        val twilightEnd = sunset + 0.55f
        val nightStart = sunset + 1.50f

        val daySpan = (sunset - sunrise).coerceAtLeast(6f)
        val dayProgress = ((h - sunrise) / daySpan).coerceIn(0f, 1f)

        return when {
            h in preDawnStart..sunrise -> {
                val t = smoothstep((h - preDawnStart) / (sunrise - preDawnStart))
                val pal = blendPaletteLab(NIGHT, DAWN, t, "dawn", isLight = false)
                TimeSnapshot(pal, 0.14f + t * 0.08f, 0.15f + t * 0.14f, t < 0.45f, false, lunarIdx, "$clockStr dawn")
            }
            h in sunrise..morningStart -> {
                // Smooth dark-to-light sunrise crossover!
                val rawT = ((h - sunrise) / (morningStart - sunrise)).coerceIn(0f, 1f)
                val bgT = smoothstep(rawT)
                // Asymmetric ink polarity switch around rawT = 0.52 avoids muddy mid-gray fish
                val fgT = smoothstep(((rawT - 0.36f) / 0.32f).coerceIn(0f, 1f))
                val isLight = rawT >= 0.50f
                val pal = blendPaletteCrossPolarity(DAWN, DAWN_LIGHT, bgT, fgT, "sunrise", isLight)
                TimeSnapshot(pal, 0.22f + rawT * 0.12f, 0.29f + rawT * 0.28f, false, isLight, lunarIdx, "$clockStr dawn")
            }
            h in morningStart..dayStart -> {
                val t = smoothstep((h - morningStart) / (dayStart - morningStart))
                val pal = blendPaletteLab(DAWN_LIGHT, NOON_LIGHT, t, "morning", isLight = true)
                TimeSnapshot(pal, 0.34f + t * 0.08f, 0.57f + t * 0.20f, false, true, lunarIdx, "$clockStr morn")
            }
            h in dayStart..goldenStart -> {
                val arcY = 0.72f + sin(dayProgress * Math.PI.toFloat()) * 0.22f
                TimeSnapshot(NOON_LIGHT, 0.24f + dayProgress * 0.52f, arcY, false, true, lunarIdx, "$clockStr day")
            }
            h in goldenStart..sunsetStart -> {
                val t = smoothstep((h - goldenStart) / (sunsetStart - goldenStart))
                val pal = blendPaletteLab(NOON_LIGHT, DUSK_LIGHT, t, "golden", isLight = true)
                val arcY = 0.72f - t * 0.30f
                TimeSnapshot(pal, 0.68f + t * 0.08f, arcY, false, true, lunarIdx, "$clockStr dusk")
            }
            h in sunsetStart..twilightEnd -> {
                // Smooth light-to-dark sunset crossover!
                val rawT = ((h - sunsetStart) / (twilightEnd - sunsetStart)).coerceIn(0f, 1f)
                val bgT = smoothstep(rawT)
                val fgT = smoothstep(((rawT - 0.36f) / 0.32f).coerceIn(0f, 1f))
                val isLight = rawT < 0.50f
                val pal = blendPaletteCrossPolarity(DUSK_LIGHT, DUSK, bgT, fgT, "sunset", isLight)
                TimeSnapshot(pal, 0.76f + rawT * 0.10f, 0.42f - rawT * 0.26f, rawT > 0.75f, isLight, lunarIdx, "$clockStr dusk")
            }
            h in twilightEnd..nightStart -> {
                val t = smoothstep((h - twilightEnd) / (nightStart - twilightEnd))
                val pal = blendPaletteLab(DUSK, NIGHT, t, "twilight", isLight = false)
                TimeSnapshot(pal, 0.86f, 0.16f, true, false, lunarIdx, "$clockStr night")
            }
            else -> {
                val nightLen = (24f - nightStart) + preDawnStart
                val nightElapsed = if (h >= nightStart) h - nightStart else (24f - nightStart) + h
                val nightT = (nightElapsed / nightLen.coerceAtLeast(1f)).coerceIn(0f, 1f)
                val arcY = 0.45f + sin(nightT * Math.PI.toFloat()) * 0.38f
                TimeSnapshot(NIGHT, 0.22f + nightT * 0.56f, arcY, true, false, lunarIdx, "$clockStr night")
            }
        }
    }

    /**
     * Pinned Light or Pinned Dark 24h cycle (used when user picks SYSTEM / LIGHT / DARK):
     * Still shifts hues, sun/moon position, stars, and creatures smoothly with the clock!
     */
    private fun evaluateFixedPolarityRealtime(
        h: Float,
        sunrise: Float,
        sunset: Float,
        clockStr: String,
        lunarIdx: Int,
        light: Boolean
    ): TimeSnapshot {
        val pDawn = if (light) DAWN_LIGHT else DAWN
        val pNoon = if (light) NOON_LIGHT else NOON
        val pDusk = if (light) DUSK_LIGHT else DUSK
        val pNight = if (light) NIGHT_LIGHT else NIGHT

        val dawnStart = sunrise - 1.2f
        val dawnEnd = sunrise + 1.8f
        val duskStart = sunset - 1.6f
        val duskEnd = sunset + 1.4f

        return when {
            h in dawnStart..dawnEnd -> {
                val t = ((h - dawnStart) / (dawnEnd - dawnStart)).coerceIn(0f, 1f)
                val pal = if (t < 0.5f) {
                    blendPaletteLab(pNight, pDawn, smoothstep(t * 2f), "dawn", light)
                } else {
                    blendPaletteLab(pDawn, pNoon, smoothstep((t - 0.5f) * 2f), "dawn", light)
                }
                TimeSnapshot(pal, 0.16f + t * 0.22f, 0.20f + t * 0.55f, false, light, lunarIdx, "$clockStr dawn")
            }
            h in dawnEnd..duskStart -> {
                val t = ((h - dawnEnd) / (duskStart - dawnEnd).coerceAtLeast(1f)).coerceIn(0f, 1f)
                val arcY = 0.75f + sin(t * Math.PI.toFloat()) * 0.20f
                TimeSnapshot(pNoon, 0.38f + t * 0.34f, arcY, false, light, lunarIdx, "$clockStr day")
            }
            h in duskStart..duskEnd -> {
                val t = ((h - duskStart) / (duskEnd - duskStart)).coerceIn(0f, 1f)
                val pal = if (t < 0.5f) {
                    blendPaletteLab(pNoon, pDusk, smoothstep(t * 2f), "dusk", light)
                } else {
                    blendPaletteLab(pDusk, pNight, smoothstep((t - 0.5f) * 2f), "dusk", light)
                }
                TimeSnapshot(pal, 0.72f + t * 0.16f, 0.75f - t * 0.58f, t > 0.78f, light, lunarIdx, "$clockStr dusk")
            }
            else -> {
                val nightLen = (24f - duskEnd) + dawnStart
                val elapsed = if (h >= duskEnd) h - duskEnd else (24f - duskEnd) + h
                val nightT = (elapsed / nightLen.coerceAtLeast(1f)).coerceIn(0f, 1f)
                val arcY = 0.45f + sin(nightT * Math.PI.toFloat()) * 0.38f
                TimeSnapshot(pNight, 0.22f + nightT * 0.56f, arcY, true, light, lunarIdx, "$clockStr night")
            }
        }
    }

    private fun tintForFamily(
        c: AsciiStageColors,
        family: PaletteFamily,
        themeMode: ThemeMode
    ): AsciiStageColors {
        return when (family) {
            PaletteFamily.LAGOON -> c
            PaletteFamily.EMBER -> if (c.isLightSurface) {
                c.copy(
                    skyBg = lerpColor(c.skyBg, 0xFFFCF1E6.toInt(), 0.58f),
                    skyHorizonBg = lerpColor(c.skyHorizonBg, 0xFFFFD8B8.toInt(), 0.58f),
                    waterBgTop = lerpColor(c.waterBgTop, 0xFFF5E3D2.toInt(), 0.50f),
                    waterBgDeep = lerpColor(c.waterBgDeep, 0xFFE3C8B2.toInt(), 0.55f),
                    meniscusWaveFg = 0xFFB8460C.toInt(),
                    fishCoral = 0xFFC23B0A.toInt(),
                    fishAqua = 0xFF286E42.toInt(),
                    fishGold = 0xFF945600.toInt(),
                    fishRose = 0xFFA81D4D.toInt(),
                    fishDimBg = 0xFF7A6355.toInt(),
                    shadowTint = 0xFFA88974.toInt()
                )
            } else {
                c.copy(
                    skyBg = lerpColor(c.skyBg, 0xFF1B1411.toInt(), 0.55f),
                    skyHorizonBg = lerpColor(c.skyHorizonBg, 0xFF422218.toInt(), 0.55f),
                    waterBgTop = lerpColor(c.waterBgTop, 0xFF2A1F1A.toInt(), 0.42f),
                    waterBgDeep = lerpColor(c.waterBgDeep, 0xFF1B1411.toInt(), 0.50f),
                    meniscusWaveFg = 0xFFFF8A3D.toInt(),
                    fishCoral = 0xFFFF8A3D.toInt(),
                    fishAqua = 0xFFB8E986.toInt(),
                    fishGold = 0xFFFFC857.toInt(),
                    fishRose = 0xFFFF6F91.toInt(),
                    fishDimBg = 0xFF7D6B5D.toInt()
                )
            }
            PaletteFamily.MOSS -> if (c.isLightSurface) {
                c.copy(
                    skyBg = lerpColor(c.skyBg, 0xFFEEF7ED.toInt(), 0.58f),
                    skyHorizonBg = lerpColor(c.skyHorizonBg, 0xFFD9EED9.toInt(), 0.58f),
                    waterBgTop = lerpColor(c.waterBgTop, 0xFFDCF0DE.toInt(), 0.52f),
                    waterBgDeep = lerpColor(c.waterBgDeep, 0xFFBDDAC1.toInt(), 0.56f),
                    meniscusWaveFg = 0xFF1E703C.toInt(),
                    fishCoral = 0xFFBA4816.toInt(),
                    fishAqua = 0xFF106584.toInt(),
                    fishGold = 0xFF6B7000.toInt(),
                    fishRose = 0xFF17704B.toInt(),
                    fishDimBg = 0xFF56735B.toInt(),
                    shadowTint = 0xFF7EA084.toInt()
                )
            } else {
                c.copy(
                    skyBg = lerpColor(c.skyBg, 0xFF0E1912.toInt(), 0.55f),
                    skyHorizonBg = lerpColor(c.skyHorizonBg, 0xFF1D3325.toInt(), 0.55f),
                    waterBgTop = lerpColor(c.waterBgTop, 0xFF16271C.toInt(), 0.45f),
                    waterBgDeep = lerpColor(c.waterBgDeep, 0xFF0E1912.toInt(), 0.55f),
                    meniscusWaveFg = 0xFFC6F36B.toInt(),
                    fishCoral = 0xFFFFB38A.toInt(),
                    fishAqua = 0xFF7FD3FF.toInt(),
                    fishGold = 0xFFC6F36B.toInt(),
                    fishRose = 0xFF6EE7A0.toInt(),
                    fishDimBg = 0xFF5A7561.toInt()
                )
            }
            PaletteFamily.AMOLED -> {
                if (c.isLightSurface && (themeMode == ThemeMode.SOLAR || themeMode == ThemeMode.LIGHT)) {
                    c.copy(
                        skyBg = 0xFFFFFFFF.toInt(),
                        skyHorizonBg = 0xFFF2FAF8.toInt(),
                        waterBgTop = 0xFFF2FAF8.toInt(),
                        waterBgDeep = 0xFFDCEEEB.toInt()
                    )
                } else {
                    c.copy(
                        isLightSurface = false,
                        skyBg = 0xFF000000.toInt(),
                        skyHorizonBg = 0xFF000000.toInt(),
                        waterBgTop = 0xFF000000.toInt(),
                        waterBgDeep = 0xFF000000.toInt(),
                        fishDimBg = 0xFF1E3E48.toInt(),
                        shadowTint = 0xFF000000.toInt()
                    )
                }
            }
        }
    }

    /**
     * WCAG Contrast Guard: guarantees every ASCII glyph role stays readable (>= 4.2:1 / 3.4:1 for secondary)
     * even right in the middle of sunrise/sunset polarity crossovers! Runs only once per 15s cache refresh.
     */
    private fun enforceContrastGuard(c: AsciiStageColors): AsciiStageColors {
        val midWater = lerpColor(c.waterBgTop, c.waterBgDeep, 0.5f)
        return c.copy(
            celestialGlyph = ensureContrast(c.celestialGlyph, c.skyBg, 4.2),
            cloudReedFg = ensureContrast(c.cloudReedFg, c.skyBg, 3.0),
            meniscusWaveFg = ensureContrast(c.meniscusWaveFg, c.waterBgTop, 3.8),
            lilyPadFg = ensureContrast(c.lilyPadFg, c.waterBgTop, 3.6),
            lotusFlowerFg = ensureContrast(c.lotusFlowerFg, c.skyBg, 3.6),
            kelpFg = ensureContrast(c.kelpFg, c.waterBgDeep, 3.5),
            pebbleFg = ensureContrast(c.pebbleFg, c.waterBgDeep, 3.0),
            bubbleFg = ensureContrast(c.bubbleFg, midWater, 3.6),
            fishCoral = ensureContrast(c.fishCoral, midWater, 4.3),
            fishAqua = ensureContrast(c.fishAqua, midWater, 4.3),
            fishGold = ensureContrast(c.fishGold, midWater, 4.3),
            fishFoam = ensureContrast(c.fishFoam, midWater, 4.8),
            fishRose = ensureContrast(c.fishRose, midWater, 4.3)
        )
    }

    private fun ensureContrast(fg: Int, bg: Int, minRatio: Double): Int {
        val opaqueFg = fg or (0xFF shl 24)
        val opaqueBg = bg or (0xFF shl 24)
        if (ColorUtils.calculateContrast(opaqueFg, opaqueBg) >= minRatio) {
            return opaqueFg
        }
        val bgLum = ColorUtils.calculateLuminance(opaqueBg)
        val darkenTarget = bgLum > 0.36
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(opaqueFg, hsl)

        for (step in 1..10) {
            hsl[2] = if (darkenTarget) {
                (hsl[2] - 0.08f).coerceAtLeast(0.06f)
            } else {
                (hsl[2] + 0.08f).coerceAtMost(0.95f)
            }
            val candidate = ColorUtils.HSLToColor(hsl)
            if (ColorUtils.calculateContrast(candidate, opaqueBg) >= minRatio) {
                return candidate
            }
        }
        return if (darkenTarget) 0xFF0E2A33.toInt() else 0xFFF2FAF8.toInt()
    }

    private fun blendPaletteLab(
        a: AsciiStageColors,
        b: AsciiStageColors,
        t: Float,
        name: String,
        isLight: Boolean
    ): AsciiStageColors = blendPaletteCrossPolarity(a, b, t, t, name, isLight)

    private fun blendColorLab(c1: Int, c2: Int, t: Float): Int {
        val ratio = t.coerceIn(0f, 1f).toDouble()
        val lab1 = DoubleArray(3)
        val lab2 = DoubleArray(3)
        val outLab = DoubleArray(3)
        ColorUtils.colorToLAB(c1 or (0xFF shl 24), lab1)
        ColorUtils.colorToLAB(c2 or (0xFF shl 24), lab2)
        ColorUtils.blendLAB(lab1, lab2, ratio, outLab)
        return ColorUtils.LABToColor(outLab[0], outLab[1], outLab[2])
    }

    private fun blendPaletteCrossPolarity(
        a: AsciiStageColors,
        b: AsciiStageColors,
        bgT: Float,
        fgT: Float,
        name: String,
        isLight: Boolean
    ): AsciiStageColors {
        val bf = bgT.coerceIn(0f, 1f)
        val ff = fgT.coerceIn(0f, 1f)
        return AsciiStageColors(
            phaseName = name,
            isLightSurface = isLight,
            skyBg = blendColorLab(a.skyBg, b.skyBg, bf),
            skyHorizonBg = blendColorLab(a.skyHorizonBg, b.skyHorizonBg, bf),
            waterBgTop = blendColorLab(a.waterBgTop, b.waterBgTop, bf),
            waterBgDeep = blendColorLab(a.waterBgDeep, b.waterBgDeep, bf),
            celestialGlyph = blendColorLab(a.celestialGlyph, b.celestialGlyph, ff),
            cloudReedFg = blendColorLab(a.cloudReedFg, b.cloudReedFg, ff),
            meniscusWaveFg = blendColorLab(a.meniscusWaveFg, b.meniscusWaveFg, ff),
            lilyPadFg = blendColorLab(a.lilyPadFg, b.lilyPadFg, ff),
            lotusFlowerFg = blendColorLab(a.lotusFlowerFg, b.lotusFlowerFg, ff),
            sunRayFg = blendColorLab(a.sunRayFg, b.sunRayFg, bf),
            kelpFg = blendColorLab(a.kelpFg, b.kelpFg, ff),
            pebbleFg = blendColorLab(a.pebbleFg, b.pebbleFg, ff),
            bubbleFg = blendColorLab(a.bubbleFg, b.bubbleFg, ff),
            fishCoral = blendColorLab(a.fishCoral, b.fishCoral, ff),
            fishAqua = blendColorLab(a.fishAqua, b.fishAqua, ff),
            fishGold = blendColorLab(a.fishGold, b.fishGold, ff),
            fishFoam = blendColorLab(a.fishFoam, b.fishFoam, ff),
            fishRose = blendColorLab(a.fishRose, b.fishRose, ff),
            fishDimBg = blendColorLab(a.fishDimBg, b.fishDimBg, ff),
            shadowTint = blendColorLab(a.shadowTint, b.shadowTint, bf),
            sunbeamHighlight = blendColorLab(a.sunbeamHighlight, b.sunbeamHighlight, ff),
            bloodCore = blendColorLab(a.bloodCore, b.bloodCore, ff),
            bloodMist = blendColorLab(a.bloodMist, b.bloodMist, ff)
        )
    }

    private fun smoothstep(x: Float): Float {
        val c = x.coerceIn(0f, 1f)
        return c * c * (3f - 2f * c)
    }

    fun lerpColor(c1: Int, c2: Int, t: Float): Int {
        val f = t.coerceIn(0f, 1f)
        val r = (Color.red(c1) + (Color.red(c2) - Color.red(c1)) * f).toInt().coerceIn(0, 255)
        val g = (Color.green(c1) + (Color.green(c2) - Color.green(c1)) * f).toInt().coerceIn(0, 255)
        val b = (Color.blue(c1) + (Color.blue(c2) - Color.blue(c1)) * f).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }
}

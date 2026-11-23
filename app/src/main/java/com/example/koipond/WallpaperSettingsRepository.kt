package com.example.koipond

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

enum class ThemeMode(val id: Int, val label: String) {
    SOLAR(3, "solar"),
    SYSTEM(0, "system"),
    LIGHT(1, "light"),
    DARK(2, "dark");

    companion object {
        fun fromId(id: Int): ThemeMode = entries.firstOrNull { it.id == id } ?: SOLAR
    }
}

enum class SceneBiome(val id: Int, val label: String) {
    OCEAN(0, "ocean"),
    ZEN_POND(1, "pond");

    companion object {
        fun fromId(id: Int): SceneBiome = entries.firstOrNull { it.id == id } ?: OCEAN
    }
}

enum class PaletteFamily(val id: Int, val label: String) {
    LAGOON(0, "lagoon"),
    EMBER(1, "ember"),
    MOSS(2, "moss"),
    AMOLED(3, "amoled");

    companion object {
        fun fromId(id: Int): PaletteFamily = entries.firstOrNull { it.id == id } ?: LAGOON
    }
}

enum class TimeCycleMode(val id: Int, val label: String) {
    AUTO_REALTIME(0, "clock"),
    DAWN(1, "dawn"),
    NOON(2, "noon"),
    DUSK(3, "dusk"),
    NIGHT(4, "night");

    companion object {
        fun fromId(id: Int): TimeCycleMode = entries.firstOrNull { it.id == id } ?: AUTO_REALTIME
    }
}

enum class WeatherMode(val id: Int, val label: String) {
    AUTO(0, "auto"),
    CLEAR(1, "clear"),
    RAIN(2, "rain"),
    SNOW(3, "snow"),
    AURORA(4, "aurora");

    companion object {
        fun fromId(id: Int): WeatherMode = entries.firstOrNull { it.id == id } ?: AUTO
    }
}

enum class HuntMode(val id: Int, val label: String, val catchChance: Float) {
    OFF(3, "off", 0.0f),
    HARMLESS(2, "harmless", 0.0f),
    NATURAL(0, "natural", 0.42f),
    FIERCE(1, "fierce", 0.78f);

    val isEnabled: Boolean get() = this != OFF

    companion object {
        fun fromId(id: Int): HuntMode = entries.firstOrNull { it.id == id } ?: NATURAL
    }
}

enum class GridDensity(val id: Int, val label: String, val targetCols: Int) {
    COZY(0, "68c", 68),
    BALANCED(1, "82c", 82),
    DENSE(2, "96c", 96);

    companion object {
        fun fromId(id: Int): GridDensity = entries.firstOrNull { it.id == id } ?: BALANCED
    }
}

/**
 * Unified performance & battery profile (replaces overlapping PowerSaveMode + TargetFps toggles):
 * - ECO: 15fps cap, reduced ambient particles, tilt sensor off
 * - BALANCED: adaptive 20fps calm / 30fps active, auto-drops to 15fps under Battery Saver / <15% / thermal warm
 * - SMOOTH: adaptive 30fps calm / 60fps active, still safely throttles under thermal/low-battery
 */
enum class PerformanceMode(val id: Int, val label: String, val maxFps: Int) {
    ECO(1, "eco", 15),
    BALANCED(0, "balanced", 30),
    SMOOTH(2, "smooth", 60);

    companion object {
        fun fromId(id: Int): PerformanceMode = entries.firstOrNull { it.id == id } ?: BALANCED
    }
}

data class WallpaperConfig(
    val sceneBiome: SceneBiome = SceneBiome.OCEAN,
    val themeMode: ThemeMode = ThemeMode.SOLAR,
    val paletteFamily: PaletteFamily = PaletteFamily.LAGOON,
    val timeCycleMode: TimeCycleMode = TimeCycleMode.AUTO_REALTIME,
    val weatherMode: WeatherMode = WeatherMode.AUTO,
    val waterlinePercent: Int = 36,
    val heroFishCount: Int = 6,
    val waterClarity: Float = 0.85f,
    val gridDensity: GridDensity = GridDensity.BALANCED,
    val performanceMode: PerformanceMode = PerformanceMode.BALANCED,
    val enableGyro: Boolean = true,
    val enableBatteryLantern: Boolean = true,
    val huntMode: HuntMode = HuntMode.NATURAL,
    val enableBottomFeeders: Boolean = true,
    val calmIconMode: Boolean = false
)

class WallpaperSettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadConfig(): WallpaperConfig {
        val rawHunt = HuntMode.fromId(prefs.getInt(KEY_HUNT_MODE, HuntMode.NATURAL.id))
        val migratedHunt = if (prefs.contains(KEY_LEGACY_ENABLE_PREDATORS) &&
            !prefs.getBoolean(KEY_LEGACY_ENABLE_PREDATORS, true) &&
            rawHunt != HuntMode.OFF
        ) {
            HuntMode.OFF
        } else {
            rawHunt
        }

        val perfMode = if (prefs.contains(KEY_PERFORMANCE_MODE)) {
            PerformanceMode.fromId(prefs.getInt(KEY_PERFORMANCE_MODE, PerformanceMode.BALANCED.id))
        } else {
            val legacyEco = prefs.getInt(KEY_LEGACY_POWER_SAVE_MODE, 0) == 1
            val legacyFps = prefs.getInt(KEY_LEGACY_TARGET_FPS, 30)
            when {
                legacyEco -> PerformanceMode.ECO
                legacyFps >= 60 -> PerformanceMode.SMOOTH
                else -> PerformanceMode.BALANCED
            }
        }

        return WallpaperConfig(
            sceneBiome = SceneBiome.fromId(prefs.getInt(KEY_SCENE_BIOME, SceneBiome.OCEAN.id)),
            themeMode = ThemeMode.fromId(prefs.getInt(KEY_THEME_MODE, ThemeMode.SOLAR.id)),
            paletteFamily = PaletteFamily.fromId(prefs.getInt(KEY_PALETTE_FAMILY, PaletteFamily.LAGOON.id)),
            timeCycleMode = TimeCycleMode.fromId(prefs.getInt(KEY_TIME_CYCLE, TimeCycleMode.AUTO_REALTIME.id)),
            weatherMode = WeatherMode.fromId(prefs.getInt(KEY_WEATHER_MODE, WeatherMode.AUTO.id)),
            waterlinePercent = prefs.getInt(KEY_WATERLINE_PCT, 36).coerceIn(25, 65),
            heroFishCount = prefs.getInt(KEY_FISH_COUNT, 6).coerceIn(2, 9),
            waterClarity = prefs.getFloat(KEY_WATER_CLARITY, 0.85f).coerceIn(0.35f, 1.0f),
            gridDensity = GridDensity.fromId(prefs.getInt(KEY_GRID_DENSITY, GridDensity.BALANCED.id)),
            performanceMode = perfMode,
            enableGyro = prefs.getBoolean(KEY_ENABLE_GYRO, true),
            enableBatteryLantern = prefs.getBoolean(KEY_BATTERY_LANTERN, true),
            huntMode = migratedHunt,
            enableBottomFeeders = prefs.getBoolean(KEY_ENABLE_BOTTOM_FEEDERS, true),
            calmIconMode = prefs.getBoolean(KEY_CALM_ICON_MODE, false)
        )
    }

    fun saveConfig(config: WallpaperConfig) {
        prefs.edit()
            .putInt(KEY_SCENE_BIOME, config.sceneBiome.id)
            .putInt(KEY_THEME_MODE, config.themeMode.id)
            .putInt(KEY_PALETTE_FAMILY, config.paletteFamily.id)
            .putInt(KEY_TIME_CYCLE, config.timeCycleMode.id)
            .putInt(KEY_WEATHER_MODE, config.weatherMode.id)
            .putInt(KEY_WATERLINE_PCT, config.waterlinePercent.coerceIn(25, 65))
            .putInt(KEY_FISH_COUNT, config.heroFishCount.coerceIn(2, 9))
            .putFloat(KEY_WATER_CLARITY, config.waterClarity.coerceIn(0.35f, 1.0f))
            .putInt(KEY_GRID_DENSITY, config.gridDensity.id)
            .putInt(KEY_PERFORMANCE_MODE, config.performanceMode.id)
            .putBoolean(KEY_ENABLE_GYRO, config.enableGyro)
            .putBoolean(KEY_BATTERY_LANTERN, config.enableBatteryLantern)
            .putInt(KEY_HUNT_MODE, config.huntMode.id)
            .putBoolean(KEY_ENABLE_BOTTOM_FEEDERS, config.enableBottomFeeders)
            .putBoolean(KEY_CALM_ICON_MODE, config.calmIconMode)
            .remove(KEY_LEGACY_ENABLE_PREDATORS)
            .apply()
    }

    fun triggerQuickTileCommand(): Int {
        val nextSeq = prefs.getInt(KEY_QUICK_TILE_SEQ, 0) + 1
        prefs.edit().putInt(KEY_QUICK_TILE_SEQ, nextSeq).apply()
        return nextSeq
    }

    fun getQuickTileCommandSeq(): Int = prefs.getInt(KEY_QUICK_TILE_SEQ, 0)

    fun recordCatchEvent(predatorName: String, preyName: String) {
        flushTelemetryBatch(1, 0, 0, "! " + predatorName + " caught " + preyName)
    }

    fun recordEscapeEvent(predatorName: String) {
        flushTelemetryBatch(0, 1, 0, "ok: school evaded " + predatorName)
    }

    fun recordCrownKoiLevelUp(speciesName: String) {
        flushTelemetryBatch(0, 0, 1, "* " + speciesName + " grew into elder crown!")
    }

    fun flushTelemetryBatch(
        deltaCatches: Int,
        deltaEscapes: Int,
        deltaCrowns: Int,
        lastEvent: String?
    ) {
        if (deltaCatches == 0 && deltaEscapes == 0 && deltaCrowns == 0 && lastEvent == null) return
        val editor = prefs.edit()
        if (deltaCatches > 0) {
            editor.putInt(KEY_STAT_CATCHES, prefs.getInt(KEY_STAT_CATCHES, 0) + deltaCatches)
        }
        if (deltaEscapes > 0) {
            editor.putInt(KEY_STAT_ESCAPES, prefs.getInt(KEY_STAT_ESCAPES, 0) + deltaEscapes)
        }
        if (deltaCrowns > 0) {
            editor.putInt(KEY_STAT_CROWNS, prefs.getInt(KEY_STAT_CROWNS, 0) + deltaCrowns)
        }
        if (lastEvent != null) {
            editor.putString(KEY_LAST_EVENT, lastEvent)
        }
        editor.apply()
    }

    fun recordLegendarySighting(legendaryName: String) {
        val count = prefs.getInt(KEY_STAT_LEGENDARIES, 0) + 1
        prefs.edit()
            .putInt(KEY_STAT_LEGENDARIES, count)
            .putString(KEY_LAST_EVENT, "★ rare legendary: " + legendaryName + "!")
            .apply()
    }

    fun getTotalLegendaries(): Int = prefs.getInt(KEY_STAT_LEGENDARIES, 0)
    fun getTotalCatches(): Int = prefs.getInt(KEY_STAT_CATCHES, 0)
    fun getTotalEscapes(): Int = prefs.getInt(KEY_STAT_ESCAPES, 0)
    fun getTotalCrowns(): Int = prefs.getInt(KEY_STAT_CROWNS, 0)
    fun getLastEvent(): String = prefs.getString(KEY_LAST_EVENT, "ok: calm waters") ?: "ok: calm waters"

    fun applyAppNightMode(themeMode: ThemeMode, timeCycleMode: TimeCycleMode = TimeCycleMode.AUTO_REALTIME) {
        val mode = when (themeMode) {
            ThemeMode.SOLAR -> {
                if (TimeOfDayPalette.isSolarDaytimeNow(timeCycleMode)) {
                    AppCompatDelegate.MODE_NIGHT_NO
                } else {
                    AppCompatDelegate.MODE_NIGHT_YES
                }
            }
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }
        if (AppCompatDelegate.getDefaultNightMode() != mode) {
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        const val PREFS_NAME = "minamo_ascii_prefs"
        const val KEY_SCENE_BIOME = "scene_biome"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_CALM_ICON_MODE = "calm_icon_mode"
        const val KEY_PALETTE_FAMILY = "palette_family"
        const val KEY_TIME_CYCLE = "time_cycle"
        const val KEY_WEATHER_MODE = "weather_mode"
        const val KEY_WATERLINE_PCT = "waterline_pct"
        const val KEY_FISH_COUNT = "koi_count"
        const val KEY_WATER_CLARITY = "water_clarity"
        const val KEY_GRID_DENSITY = "grid_density"
        const val KEY_PERFORMANCE_MODE = "performance_mode"
        const val KEY_ENABLE_GYRO = "enable_gyro"
        const val KEY_BATTERY_LANTERN = "battery_lantern"
        const val KEY_HUNT_MODE = "hunt_mode"
        const val KEY_ENABLE_BOTTOM_FEEDERS = "enable_bottom_feeders"
        const val KEY_QUICK_TILE_SEQ = "quick_tile_seq"
        const val KEY_STAT_CATCHES = "stat_catches"
        const val KEY_STAT_ESCAPES = "stat_escapes"
        const val KEY_STAT_CROWNS = "stat_crowns"
        const val KEY_STAT_LEGENDARIES = "stat_legendaries"
        const val KEY_LAST_EVENT = "last_event"
        // Legacy keys for seamless preference migration
        private const val KEY_LEGACY_ENABLE_PREDATORS = "enable_predators"
        private const val KEY_LEGACY_POWER_SAVE_MODE = "power_save_mode"
        private const val KEY_LEGACY_TARGET_FPS = "target_fps"
    }
}

package com.example.koipond

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

enum class ThemeMode(val id: Int, val label: String) {
    SYSTEM(0, "system"),
    LIGHT(1, "light"),
    DARK(2, "dark");

    companion object {
        fun fromId(id: Int): ThemeMode = entries.firstOrNull { it.id == id } ?: SYSTEM
    }
}

enum class PaletteFamily(val id: Int, val label: String) {
    LAGOON(0, "lagoon"),
    EMBER(1, "ember"),
    MOSS(2, "moss");

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
    SNOW(3, "snow");

    companion object {
        fun fromId(id: Int): WeatherMode = entries.firstOrNull { it.id == id } ?: AUTO
    }
}

enum class HuntMode(val id: Int, val label: String, val catchChance: Float) {
    NATURAL(0, "natural", 0.42f),
    FIERCE(1, "fierce", 0.78f),
    HARMLESS(2, "harmless", 0.0f);

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

enum class PowerSaveMode(val id: Int, val label: String) {
    AUTO_SAVER(0, "auto"),
    MAX_ECO(1, "eco"),
    OFF(2, "off");

    companion object {
        fun fromId(id: Int): PowerSaveMode = entries.firstOrNull { it.id == id } ?: AUTO_SAVER
    }
}

data class WallpaperConfig(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val paletteFamily: PaletteFamily = PaletteFamily.LAGOON,
    val timeCycleMode: TimeCycleMode = TimeCycleMode.AUTO_REALTIME,
    val weatherMode: WeatherMode = WeatherMode.AUTO,
    val heroFishCount: Int = 6,
    val waterClarity: Float = 0.85f,
    val targetFps: Int = 30,
    val gridDensity: GridDensity = GridDensity.BALANCED,
    val enableGyro: Boolean = true,
    val powerSaveMode: PowerSaveMode = PowerSaveMode.AUTO_SAVER,
    val pauseOnBackgroundAndScreenOff: Boolean = true,
    val enablePredators: Boolean = true,
    val huntMode: HuntMode = HuntMode.NATURAL,
    val enableBottomFeeders: Boolean = true
)

class WallpaperSettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadConfig(): WallpaperConfig {
        return WallpaperConfig(
            themeMode = ThemeMode.fromId(prefs.getInt(KEY_THEME_MODE, ThemeMode.SYSTEM.id)),
            paletteFamily = PaletteFamily.fromId(prefs.getInt(KEY_PALETTE_FAMILY, PaletteFamily.LAGOON.id)),
            timeCycleMode = TimeCycleMode.fromId(prefs.getInt(KEY_TIME_CYCLE, TimeCycleMode.AUTO_REALTIME.id)),
            weatherMode = WeatherMode.fromId(prefs.getInt(KEY_WEATHER_MODE, WeatherMode.AUTO.id)),
            heroFishCount = prefs.getInt(KEY_FISH_COUNT, 6).coerceIn(2, 9),
            waterClarity = prefs.getFloat(KEY_WATER_CLARITY, 0.85f).coerceIn(0.35f, 1.0f),
            targetFps = if (prefs.getInt(KEY_TARGET_FPS, 30) == 60) 60 else 30,
            gridDensity = GridDensity.fromId(prefs.getInt(KEY_GRID_DENSITY, GridDensity.BALANCED.id)),
            enableGyro = prefs.getBoolean(KEY_ENABLE_GYRO, true),
            powerSaveMode = PowerSaveMode.fromId(prefs.getInt(KEY_POWER_SAVE_MODE, PowerSaveMode.AUTO_SAVER.id)),
            pauseOnBackgroundAndScreenOff = prefs.getBoolean(KEY_PAUSE_BG, true),
            enablePredators = prefs.getBoolean(KEY_ENABLE_PREDATORS, true),
            huntMode = HuntMode.fromId(prefs.getInt(KEY_HUNT_MODE, HuntMode.NATURAL.id)),
            enableBottomFeeders = prefs.getBoolean(KEY_ENABLE_BOTTOM_FEEDERS, true)
        )
    }

    fun saveConfig(config: WallpaperConfig) {
        prefs.edit()
            .putInt(KEY_THEME_MODE, config.themeMode.id)
            .putInt(KEY_PALETTE_FAMILY, config.paletteFamily.id)
            .putInt(KEY_TIME_CYCLE, config.timeCycleMode.id)
            .putInt(KEY_WEATHER_MODE, config.weatherMode.id)
            .putInt(KEY_FISH_COUNT, config.heroFishCount.coerceIn(2, 9))
            .putFloat(KEY_WATER_CLARITY, config.waterClarity.coerceIn(0.35f, 1.0f))
            .putInt(KEY_TARGET_FPS, config.targetFps)
            .putInt(KEY_GRID_DENSITY, config.gridDensity.id)
            .putBoolean(KEY_ENABLE_GYRO, config.enableGyro)
            .putInt(KEY_POWER_SAVE_MODE, config.powerSaveMode.id)
            .putBoolean(KEY_PAUSE_BG, config.pauseOnBackgroundAndScreenOff)
            .putBoolean(KEY_ENABLE_PREDATORS, config.enablePredators)
            .putInt(KEY_HUNT_MODE, config.huntMode.id)
            .putBoolean(KEY_ENABLE_BOTTOM_FEEDERS, config.enableBottomFeeders)
            .apply()
    }

    fun recordCatchEvent(predatorName: String, preyName: String) {
        val catches = prefs.getInt(KEY_STAT_CATCHES, 0) + 1
        prefs.edit()
            .putInt(KEY_STAT_CATCHES, catches)
            .putString(KEY_LAST_EVENT, "! $predatorName caught $preyName")
            .apply()
    }

    fun recordEscapeEvent(predatorName: String) {
        val escapes = prefs.getInt(KEY_STAT_ESCAPES, 0) + 1
        prefs.edit()
            .putInt(KEY_STAT_ESCAPES, escapes)
            .putString(KEY_LAST_EVENT, "ok: school evaded $predatorName")
            .apply()
    }

    fun getTotalCatches(): Int = prefs.getInt(KEY_STAT_CATCHES, 0)
    fun getTotalEscapes(): Int = prefs.getInt(KEY_STAT_ESCAPES, 0)
    fun getLastEvent(): String = prefs.getString(KEY_LAST_EVENT, "ok: calm waters") ?: "ok: calm waters"

    fun applyAppNightMode(themeMode: ThemeMode) {
        val mode = when (themeMode) {
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        const val PREFS_NAME = "minamo_ascii_prefs"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_PALETTE_FAMILY = "palette_family"
        const val KEY_TIME_CYCLE = "time_cycle"
        const val KEY_WEATHER_MODE = "weather_mode"
        const val KEY_FISH_COUNT = "koi_count"
        const val KEY_WATER_CLARITY = "water_clarity"
        const val KEY_TARGET_FPS = "target_fps"
        const val KEY_GRID_DENSITY = "grid_density"
        const val KEY_ENABLE_GYRO = "enable_gyro"
        const val KEY_POWER_SAVE_MODE = "power_save_mode"
        const val KEY_PAUSE_BG = "pause_on_bg"
        const val KEY_ENABLE_PREDATORS = "enable_predators"
        const val KEY_HUNT_MODE = "hunt_mode"
        const val KEY_ENABLE_BOTTOM_FEEDERS = "enable_bottom_feeders"
        const val KEY_STAT_CATCHES = "stat_catches"
        const val KEY_STAT_ESCAPES = "stat_escapes"
        const val KEY_LAST_EVENT = "last_event"
    }
}

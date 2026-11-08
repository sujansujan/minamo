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

/**
 * Controls how the ASCII aquarium colors change throughout the day:
 *   AUTO_REALTIME: Reads actual local clock hour (Dawn 05-08, Day 08-17, Dusk 17-20, Night 20-05)
 *   DAWN: Warm rose-amber sunrise sky, mist, and golden-mint water
 *   NOON: Crisp cyan-lagoon sky, bright aqua sunbeams, and emerald water
 *   DUSK: Crimson-tangerine sunset sky, violet-amber reflection, and deep teal water
 *   NIGHT: Starry indigo sky, crescent moon reflection, and bioluminescent midnight water
 */
enum class TimeCycleMode(val id: Int, val label: String) {
    AUTO_REALTIME(0, "live clock"),
    DAWN(1, "dawn"),
    NOON(2, "noon"),
    DUSK(3, "dusk"),
    NIGHT(4, "night");

    companion object {
        fun fromId(id: Int): TimeCycleMode = entries.firstOrNull { it.id == id } ?: AUTO_REALTIME
    }
}

enum class GridDensity(val id: Int, val label: String, val targetCols: Int) {
    COZY(0, "cozy (38c)", 38),
    BALANCED(1, "std (46c)", 46),
    DENSE(2, "dense (56c)", 56);

    companion object {
        fun fromId(id: Int): GridDensity = entries.firstOrNull { it.id == id } ?: BALANCED
    }
}

data class WallpaperConfig(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val timeCycleMode: TimeCycleMode = TimeCycleMode.AUTO_REALTIME,
    val heroFishCount: Int = 5,
    val waterClarity: Float = 0.85f,
    val targetFps: Int = 30,
    val gridDensity: GridDensity = GridDensity.BALANCED,
    val enableGyro: Boolean = true
)

class WallpaperSettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadConfig(): WallpaperConfig {
        return WallpaperConfig(
            themeMode = ThemeMode.fromId(prefs.getInt(KEY_THEME_MODE, ThemeMode.SYSTEM.id)),
            timeCycleMode = TimeCycleMode.fromId(prefs.getInt(KEY_TIME_CYCLE, TimeCycleMode.AUTO_REALTIME.id)),
            heroFishCount = prefs.getInt(KEY_FISH_COUNT, 5).coerceIn(2, 9),
            waterClarity = prefs.getFloat(KEY_WATER_CLARITY, 0.85f).coerceIn(0.35f, 1.0f),
            targetFps = if (prefs.getInt(KEY_TARGET_FPS, 30) == 60) 60 else 30,
            gridDensity = GridDensity.fromId(prefs.getInt(KEY_GRID_DENSITY, GridDensity.BALANCED.id)),
            enableGyro = prefs.getBoolean(KEY_ENABLE_GYRO, true)
        )
    }

    fun saveConfig(config: WallpaperConfig) {
        prefs.edit()
            .putInt(KEY_THEME_MODE, config.themeMode.id)
            .putInt(KEY_TIME_CYCLE, config.timeCycleMode.id)
            .putInt(KEY_FISH_COUNT, config.heroFishCount.coerceIn(2, 9))
            .putFloat(KEY_WATER_CLARITY, config.waterClarity.coerceIn(0.35f, 1.0f))
            .putInt(KEY_TARGET_FPS, config.targetFps)
            .putInt(KEY_GRID_DENSITY, config.gridDensity.id)
            .putBoolean(KEY_ENABLE_GYRO, config.enableGyro)
            .apply()
    }

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
        const val KEY_TIME_CYCLE = "time_cycle"
        const val KEY_FISH_COUNT = "koi_count"
        const val KEY_WATER_CLARITY = "water_clarity"
        const val KEY_TARGET_FPS = "target_fps"
        const val KEY_GRID_DENSITY = "grid_density"
        const val KEY_ENABLE_GYRO = "enable_gyro"
    }
}

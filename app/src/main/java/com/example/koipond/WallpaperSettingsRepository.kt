package com.example.koipond

import android.content.Context
import android.content.SharedPreferences

enum class QualityPreset(val id: Int, val minScale: Float, val maxScale: Float, val particleMultiplier: Float) {
    LOW(0, 0.60f, 0.70f, 0.45f),
    BALANCED(1, 0.68f, 0.88f, 0.75f),
    ULTRA(2, 0.75f, 1.00f, 1.00f);

    companion object {
        fun fromId(id: Int): QualityPreset = entries.firstOrNull { it.id == id } ?: BALANCED
    }
}

data class WallpaperConfig(
    val koiCount: Int = 6,
    val waterClarity: Float = 0.82f,
    val sunAngleDeg: Float = 45f,
    val targetFps: Int = 30,
    val qualityPreset: QualityPreset = QualityPreset.ULTRA,
    val enableGyro: Boolean = true
)

class WallpaperSettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadConfig(): WallpaperConfig {
        return WallpaperConfig(
            koiCount = prefs.getInt(KEY_KOI_COUNT, 6).coerceIn(3, 9),
            waterClarity = prefs.getFloat(KEY_WATER_CLARITY, 0.82f).coerceIn(0.35f, 1.0f),
            sunAngleDeg = prefs.getFloat(KEY_SUN_ANGLE, 45f).coerceIn(25f, 75f),
            targetFps = if (prefs.getInt(KEY_TARGET_FPS, 30) == 60) 60 else 30,
            qualityPreset = QualityPreset.fromId(prefs.getInt(KEY_QUALITY_PRESET, QualityPreset.ULTRA.id)),
            enableGyro = prefs.getBoolean(KEY_ENABLE_GYRO, true)
        )
    }

    fun saveConfig(config: WallpaperConfig) {
        prefs.edit()
            .putInt(KEY_KOI_COUNT, config.koiCount.coerceIn(3, 9))
            .putFloat(KEY_WATER_CLARITY, config.waterClarity.coerceIn(0.35f, 1.0f))
            .putFloat(KEY_SUN_ANGLE, config.sunAngleDeg.coerceIn(25f, 75f))
            .putInt(KEY_TARGET_FPS, config.targetFps)
            .putInt(KEY_QUALITY_PRESET, config.qualityPreset.id)
            .putBoolean(KEY_ENABLE_GYRO, config.enableGyro)
            .apply()
    }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        const val PREFS_NAME = "koi_pond_wallpaper_prefs"
        const val KEY_KOI_COUNT = "koi_count"
        const val KEY_WATER_CLARITY = "water_clarity"
        const val KEY_SUN_ANGLE = "sun_angle"
        const val KEY_TARGET_FPS = "target_fps"
        const val KEY_QUALITY_PRESET = "quality_preset"
        const val KEY_ENABLE_GYRO = "enable_gyro"
    }
}

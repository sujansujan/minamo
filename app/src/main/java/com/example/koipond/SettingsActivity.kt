package com.example.koipond

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlin.math.roundToInt

/**
 * Interactive settings activity allowing real-time adjustment of Koi Count, Water Clarity,
 * Sun Elevation Angle, FPS Cap (30/60), Quality Preset (Low / Balanced / Ultra),
 * and Gyroscope Parallax, plus a shortcut button to launch the system Live Wallpaper picker.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var repository: WallpaperSettingsRepository
    private lateinit var currentConfig: WallpaperConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        repository = WallpaperSettingsRepository(this)
        currentConfig = repository.loadConfig()

        val textFishCount = findViewById<TextView>(R.id.textFishCount)
        val seekFishCount = findViewById<SeekBar>(R.id.seekFishCount)

        val textWaterClarity = findViewById<TextView>(R.id.textWaterClarity)
        val seekWaterClarity = findViewById<SeekBar>(R.id.seekWaterClarity)

        val textSunAngle = findViewById<TextView>(R.id.textSunAngle)
        val seekSunAngle = findViewById<SeekBar>(R.id.seekSunAngle)

        val radioGroupFps = findViewById<RadioGroup>(R.id.radioGroupFps)
        val radioFps30 = findViewById<RadioButton>(R.id.radioFps30)
        val radioFps60 = findViewById<RadioButton>(R.id.radioFps60)

        val radioGroupPreset = findViewById<RadioGroup>(R.id.radioGroupPreset)
        val radioPresetLow = findViewById<RadioButton>(R.id.radioPresetLow)
        val radioPresetBalanced = findViewById<RadioButton>(R.id.radioPresetBalanced)
        val radioPresetUltra = findViewById<RadioButton>(R.id.radioPresetUltra)

        val switchGyro = findViewById<SwitchMaterial>(R.id.switchGyro)
        val buttonApplyWallpaper = findViewById<Button>(R.id.buttonApplyWallpaper)

        // Initialize UI states from saved configuration
        seekFishCount.max = 6 // 3 .. 9 koi
        seekFishCount.progress = (currentConfig.koiCount - 3).coerceIn(0, 6)
        textFishCount.text = getString(R.string.label_fish_count, currentConfig.koiCount)

        val clarityPercent = (currentConfig.waterClarity * 100f).roundToInt().coerceIn(35, 100)
        seekWaterClarity.max = 65 // 35% .. 100%
        seekWaterClarity.progress = clarityPercent - 35
        textWaterClarity.text = getString(R.string.label_water_clarity, clarityPercent)

        val sunDeg = currentConfig.sunAngleDeg.roundToInt().coerceIn(25, 75)
        seekSunAngle.max = 50 // 25° .. 75°
        seekSunAngle.progress = sunDeg - 25
        textSunAngle.text = getString(R.string.label_sun_angle, sunDeg)

        if (currentConfig.targetFps == 60) {
            radioFps60.isChecked = true
        } else {
            radioFps30.isChecked = true
        }

        when (currentConfig.qualityPreset) {
            QualityPreset.LOW -> radioPresetLow.isChecked = true
            QualityPreset.BALANCED -> radioPresetBalanced.isChecked = true
            QualityPreset.ULTRA -> radioPresetUltra.isChecked = true
        }

        switchGyro.isChecked = currentConfig.enableGyro

        // Listeners
        seekFishCount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val count = progress + 3
                textFishCount.text = getString(R.string.label_fish_count, count)
                updateConfig(currentConfig.copy(koiCount = count))
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        seekWaterClarity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val pct = progress + 35
                textWaterClarity.text = getString(R.string.label_water_clarity, pct)
                updateConfig(currentConfig.copy(waterClarity = pct / 100f))
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        seekSunAngle.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val deg = progress + 25
                textSunAngle.text = getString(R.string.label_sun_angle, deg)
                updateConfig(currentConfig.copy(sunAngleDeg = deg.toFloat()))
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        radioGroupFps.setOnCheckedChangeListener { _, checkedId ->
            val fps = if (checkedId == R.id.radioFps60) 60 else 30
            updateConfig(currentConfig.copy(targetFps = fps))
        }

        radioGroupPreset.setOnCheckedChangeListener { _, checkedId ->
            val preset = when (checkedId) {
                R.id.radioPresetLow -> QualityPreset.LOW
                R.id.radioPresetBalanced -> QualityPreset.BALANCED
                else -> QualityPreset.ULTRA
            }
            updateConfig(currentConfig.copy(qualityPreset = preset))
        }

        switchGyro.setOnCheckedChangeListener { _, isChecked ->
            updateConfig(currentConfig.copy(enableGyro = isChecked))
        }

        buttonApplyWallpaper.setOnClickListener {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this@SettingsActivity, KoiPondWallpaperService::class.java)
                )
            }
            startActivity(intent)
        }
    }

    private fun updateConfig(newConfig: WallpaperConfig) {
        currentConfig = newConfig
        repository.saveConfig(newConfig)
    }
}

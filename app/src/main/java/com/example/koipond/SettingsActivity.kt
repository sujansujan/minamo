package com.example.koipond

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.roundToInt

class SettingsActivity : AppCompatActivity() {

    private lateinit var repository: WallpaperSettingsRepository
    @Volatile
    private lateinit var currentConfig: WallpaperConfig
    private var glPreviewView: GLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        repository = WallpaperSettingsRepository(this)
        currentConfig = repository.loadConfig()

        val previewSurface = findViewById<GLSurfaceView>(R.id.glPreviewSurface)
        previewSurface.setEGLContextClientVersion(3)
        val previewSimulation = PondSimulation()
        val previewRenderer = KoiPondRenderer(previewSimulation)

        previewSurface.setRenderer(object : GLSurfaceView.Renderer {
            private var lastTimeNs = 0L

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                previewRenderer.onSurfaceCreated()
                lastTimeNs = SystemClock.elapsedRealtimeNanos()
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                previewRenderer.onSurfaceChanged(width, height, currentConfig, isBatterySaver = false)
            }

            override fun onDrawFrame(gl: GL10?) {
                val now = SystemClock.elapsedRealtimeNanos()
                val dt = ((now - lastTimeNs) / 1_000_000_000.0f).coerceIn(0.001f, 0.05f)
                lastTimeNs = now
                previewRenderer.drawFrame(dt, currentConfig, isBatterySaver = false)
            }
        })
        previewSurface.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        previewSurface.setOnTouchListener { v, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_MOVE) {
                val tx = event.x
                val ty = event.y
                previewSurface.queueEvent {
                    previewRenderer.onTouchWater(tx, ty)
                }
                v.performClick()
            }
            true
        }
        glPreviewView = previewSurface

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
        val buttonShuffleFish = findViewById<Button>(R.id.buttonShuffleFish)

        seekFishCount.max = 6 // 3 .. 9 main fish
        seekFishCount.progress = (currentConfig.koiCount - 3).coerceIn(0, 6)
        textFishCount.text = getString(R.string.label_fish_count, currentConfig.koiCount)

        val clarityPercent = (currentConfig.waterClarity * 100f).roundToInt().coerceIn(35, 100)
        seekWaterClarity.max = 65
        seekWaterClarity.progress = clarityPercent - 35
        textWaterClarity.text = getString(R.string.label_water_clarity, clarityPercent)

        val sunDeg = currentConfig.sunAngleDeg.roundToInt().coerceIn(25, 75)
        seekSunAngle.max = 50
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

        buttonShuffleFish.setOnClickListener {
            previewSurface.queueEvent {
                previewRenderer.randomizeFishOnUnlock()
            }
        }

        buttonApplyWallpaper.setOnClickListener {
            launchWallpaperPicker()
        }
    }

    override fun onResume() {
        super.onResume()
        glPreviewView?.onResume()
    }

    override fun onPause() {
        glPreviewView?.onPause()
        super.onPause()
    }

    private fun launchWallpaperPicker() {
        val component = ComponentName(this, KoiPondWallpaperService::class.java)
        try {
            val changeIntent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
            }
            startActivity(changeIntent)
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
            } catch (_: ActivityNotFoundException) {
                try {
                    startActivity(Intent(Intent.ACTION_SET_WALLPAPER))
                } catch (_: Exception) {
                    Toast.makeText(
                        this,
                        "Long-press your home screen and choose Wallpapers -> Live Wallpapers -> Minamo",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun updateConfig(newConfig: WallpaperConfig) {
        currentConfig = newConfig
        repository.saveConfig(newConfig)
    }
}

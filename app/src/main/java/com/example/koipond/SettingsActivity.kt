package com.example.koipond

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.roundToInt

class SettingsActivity : AppCompatActivity() {

    private lateinit var repository: WallpaperSettingsRepository
    private lateinit var currentConfig: WallpaperConfig
    private var previousConfigForUndo: WallpaperConfig? = null

    private lateinit var textHeaderPath: TextView
    private lateinit var textHeaderClockTag: TextView
    private lateinit var bannerUndo: LinearLayout
    private lateinit var textUndoMessage: TextView
    private lateinit var btnUndoReset: TextView

    private lateinit var screenPond: LinearLayout
    private lateinit var screenSettings: ScrollView
    private lateinit var screenAbout: ScrollView

    private lateinit var navPond: TextView
    private lateinit var navSet: TextView
    private lateinit var navAbout: TextView

    private lateinit var asciiPondView: AsciiPondView
    private lateinit var textBiomeStatus: TextView

    private lateinit var btnCycleAuto: TextView
    private lateinit var btnCycleDawn: TextView
    private lateinit var btnCycleNoon: TextView
    private lateinit var btnCycleDusk: TextView
    private lateinit var btnCycleNight: TextView

    private lateinit var btnThemeSystem: TextView
    private lateinit var btnThemeLight: TextView
    private lateinit var btnThemeDark: TextView

    private lateinit var textFishBar: TextView
    private lateinit var textClarityBar: TextView

    private lateinit var textPredatorsCheck: TextView
    private lateinit var textBottomFeedersCheck: TextView
    private lateinit var textPauseBgSwitch: TextView

    private lateinit var btnPowerAuto: TextView
    private lateinit var btnPowerEco: TextView
    private lateinit var btnPowerOff: TextView

    private lateinit var btnGridCozy: TextView
    private lateinit var btnGridBalanced: TextView
    private lateinit var btnGridDense: TextView

    private lateinit var btnFps30: TextView
    private lateinit var btnFps60: TextView
    private lateinit var textGyroSwitch: TextView

    private var activeTab = 1
    private val uiHandler = Handler(Looper.getMainLooper())
    private val hideUndoRunnable = Runnable {
        bannerUndo.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        repository = WallpaperSettingsRepository(this)
        currentConfig = repository.loadConfig()
        repository.applyAppNightMode(currentConfig.themeMode)

        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_settings)

        val root = findViewById<View>(R.id.rootContainer)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        bindViews()
        setupListeners()

        if (savedInstanceState != null) {
            activeTab = savedInstanceState.getInt("active_tab", 1)
        }
        switchTab(activeTab)
        refreshAsciiUi()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("active_tab", activeTab)
    }

    override fun onResume() {
        super.onResume()
        asciiPondView.startAnimation()
        refreshAsciiUi()
    }

    override fun onPause() {
        asciiPondView.stopAnimation()
        super.onPause()
    }

    private fun bindViews() {
        textHeaderPath = findViewById(R.id.textHeaderPath)
        textHeaderClockTag = findViewById(R.id.textHeaderClockTag)
        bannerUndo = findViewById(R.id.bannerUndo)
        textUndoMessage = findViewById(R.id.textUndoMessage)
        btnUndoReset = findViewById(R.id.btnUndoReset)

        screenPond = findViewById(R.id.screenPond)
        screenSettings = findViewById(R.id.screenSettings)
        screenAbout = findViewById(R.id.screenAbout)

        navPond = findViewById(R.id.navPond)
        navSet = findViewById(R.id.navSet)
        navAbout = findViewById(R.id.navAbout)

        asciiPondView = findViewById(R.id.asciiPondView)
        textBiomeStatus = findViewById(R.id.textBiomeStatus)

        btnCycleAuto = findViewById(R.id.btnCycleAuto)
        btnCycleDawn = findViewById(R.id.btnCycleDawn)
        btnCycleNoon = findViewById(R.id.btnCycleNoon)
        btnCycleDusk = findViewById(R.id.btnCycleDusk)
        btnCycleNight = findViewById(R.id.btnCycleNight)

        btnThemeSystem = findViewById(R.id.btnThemeSystem)
        btnThemeLight = findViewById(R.id.btnThemeLight)
        btnThemeDark = findViewById(R.id.btnThemeDark)

        textFishBar = findViewById(R.id.textFishBar)
        textClarityBar = findViewById(R.id.textClarityBar)

        textPredatorsCheck = findViewById(R.id.textPredatorsCheck)
        textBottomFeedersCheck = findViewById(R.id.textBottomFeedersCheck)
        textPauseBgSwitch = findViewById(R.id.textPauseBgSwitch)

        btnPowerAuto = findViewById(R.id.btnPowerAuto)
        btnPowerEco = findViewById(R.id.btnPowerEco)
        btnPowerOff = findViewById(R.id.btnPowerOff)

        btnGridCozy = findViewById(R.id.btnGridCozy)
        btnGridBalanced = findViewById(R.id.btnGridBalanced)
        btnGridDense = findViewById(R.id.btnGridDense)

        btnFps30 = findViewById(R.id.btnFps30)
        btnFps60 = findViewById(R.id.btnFps60)
        textGyroSwitch = findViewById(R.id.textGyroSwitch)
    }

    private fun setupListeners() {
        navPond.setOnClickListener { haptic(it); switchTab(1) }
        navSet.setOnClickListener { haptic(it); switchTab(2) }
        navAbout.setOnClickListener { haptic(it); switchTab(3) }

        asciiPondView.onSpeciesRandomized = { speciesStr, timeLabel ->
            textBiomeStatus.text = "biome .. $speciesStr"
            textBiomeStatus.contentDescription = "Active fish biome: $speciesStr"
            textHeaderClockTag.text = "[$timeLabel]"
        }

        findViewById<TextView>(R.id.btnShuffleFish).apply {
            contentDescription = "Shuffle fish and critters"
            setOnClickListener {
                haptic(it)
                asciiPondView.shuffleEcosystem()
            }
        }

        findViewById<TextView>(R.id.btnSummonPredator).apply {
            contentDescription = "Summon a random predator fish now"
            setOnClickListener {
                haptic(it)
                if (!currentConfig.enablePredators) {
                    updateConfig(currentConfig.copy(enablePredators = true))
                }
                val predName = asciiPondView.summonPredatorNow()
                textBiomeStatus.text = "! alert .. $predName spotted (fish hiding)"
            }
        }

        findViewById<TextView>(R.id.btnSetWallpaper).apply {
            contentDescription = "Set live wallpaper"
            setOnClickListener {
                haptic(it)
                launchWallpaperPicker()
            }
        }

        // Time-of-day cycle radios
        btnCycleAuto.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(timeCycleMode = TimeCycleMode.AUTO_REALTIME)) }
        btnCycleDawn.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(timeCycleMode = TimeCycleMode.DAWN)) }
        btnCycleNoon.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(timeCycleMode = TimeCycleMode.NOON)) }
        btnCycleDusk.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(timeCycleMode = TimeCycleMode.DUSK)) }
        btnCycleNight.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(timeCycleMode = TimeCycleMode.NIGHT)) }

        // Predators & Bottom feeders checkboxes
        findViewById<LinearLayout>(R.id.rowPredatorsCheck).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(enablePredators = !currentConfig.enablePredators))
        }
        findViewById<LinearLayout>(R.id.rowBottomFeedersCheck).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(enableBottomFeeders = !currentConfig.enableBottomFeeders))
        }

        // Power Saver & Pause on Background controls
        findViewById<LinearLayout>(R.id.rowPauseBgSwitch).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(pauseOnBackgroundAndScreenOff = !currentConfig.pauseOnBackgroundAndScreenOff))
        }
        btnPowerAuto.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(powerSaveMode = PowerSaveMode.AUTO_SAVER)) }
        btnPowerEco.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(powerSaveMode = PowerSaveMode.MAX_ECO)) }
        btnPowerOff.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(powerSaveMode = PowerSaveMode.OFF)) }

        // Theme mode radios
        btnThemeSystem.setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(themeMode = ThemeMode.SYSTEM))
            repository.applyAppNightMode(ThemeMode.SYSTEM)
        }
        btnThemeLight.setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(themeMode = ThemeMode.LIGHT))
            repository.applyAppNightMode(ThemeMode.LIGHT)
        }
        btnThemeDark.setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(themeMode = ThemeMode.DARK))
            repository.applyAppNightMode(ThemeMode.DARK)
        }

        // Fish count steppers
        findViewById<TextView>(R.id.btnFishMinus).apply {
            contentDescription = "Decrease fish count"
            setOnClickListener {
                haptic(it)
                updateConfig(currentConfig.copy(heroFishCount = (currentConfig.heroFishCount - 1).coerceIn(2, 9)))
            }
        }
        findViewById<TextView>(R.id.btnFishPlus).apply {
            contentDescription = "Increase fish count"
            setOnClickListener {
                haptic(it)
                updateConfig(currentConfig.copy(heroFishCount = (currentConfig.heroFishCount + 1).coerceIn(2, 9)))
            }
        }

        // Clarity steppers
        findViewById<TextView>(R.id.btnClarityMinus).apply {
            contentDescription = "Decrease water clarity"
            setOnClickListener {
                haptic(it)
                updateConfig(currentConfig.copy(waterClarity = (currentConfig.waterClarity - 0.10f).coerceIn(0.35f, 1.0f)))
            }
        }
        findViewById<TextView>(R.id.btnClarityPlus).apply {
            contentDescription = "Increase water clarity"
            setOnClickListener {
                haptic(it)
                updateConfig(currentConfig.copy(waterClarity = (currentConfig.waterClarity + 0.10f).coerceIn(0.35f, 1.0f)))
            }
        }

        // Grid density radios
        btnGridCozy.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(gridDensity = GridDensity.COZY)) }
        btnGridBalanced.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(gridDensity = GridDensity.BALANCED)) }
        btnGridDense.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(gridDensity = GridDensity.DENSE)) }

        // FPS radios
        btnFps30.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(targetFps = 30)) }
        btnFps60.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(targetFps = 60)) }

        // Gyro switch
        findViewById<LinearLayout>(R.id.rowGyroSwitch).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(enableGyro = !currentConfig.enableGyro))
        }

        // Destructive Reset with Inline Undo
        findViewById<TextView>(R.id.btnResetDefaults).apply {
            contentDescription = "Reset settings to defaults"
            setOnClickListener {
                haptic(it)
                previousConfigForUndo = currentConfig
                val defaults = WallpaperConfig()
                updateConfig(defaults)
                repository.applyAppNightMode(defaults.themeMode)
                showUndoBanner("ok: reset to defaults.")
            }
        }

        btnUndoReset.contentDescription = "Undo reset"
        btnUndoReset.setOnClickListener {
            haptic(it)
            previousConfigForUndo?.let { prev ->
                updateConfig(prev)
                repository.applyAppNightMode(prev.themeMode)
            }
            bannerUndo.visibility = View.GONE
        }
    }

    private fun switchTab(tab: Int) {
        activeTab = tab
        screenPond.visibility = if (tab == 1) View.VISIBLE else View.GONE
        screenSettings.visibility = if (tab == 2) View.VISIBLE else View.GONE
        screenAbout.visibility = if (tab == 3) View.VISIBLE else View.GONE

        textHeaderPath.text = when (tab) {
            1 -> "~/minamo/pond"
            2 -> "~/minamo/set"
            else -> "~/minamo/about"
        }

        val cAccent = ContextCompat.getColor(this, R.color.ascii_accent)
        val cBg = ContextCompat.getColor(this, R.color.ascii_bg)
        val cFg = ContextCompat.getColor(this, R.color.ascii_fg)
        val cSurface = ContextCompat.getColor(this, R.color.ascii_surface)

        fun styleNav(tv: TextView, selected: Boolean, spoken: String) {
            tv.setBackgroundColor(if (selected) cAccent else cSurface)
            tv.setTextColor(if (selected) cBg else cFg)
            tv.contentDescription = spoken
        }

        styleNav(navPond, tab == 1, "Pond preview tab")
        styleNav(navSet, tab == 2, "Settings tab")
        styleNav(navAbout, tab == 3, "About tab")
    }

    private fun refreshAsciiUi() {
        asciiPondView.config = currentConfig
        val snap = TimeOfDayPalette.resolve(currentConfig.timeCycleMode)
        textHeaderClockTag.text = "[${snap.timeLabel}]"
        textBiomeStatus.text = "biome .. ${asciiPondView.simulation.activeSpeciesSummary}"

        val cAccent = ContextCompat.getColor(this, R.color.ascii_accent)
        val cFg = ContextCompat.getColor(this, R.color.ascii_fg)
        val cDim = ContextCompat.getColor(this, R.color.ascii_dim)

        fun styleRadio(tv: TextView, checked: Boolean, label: String) {
            tv.text = if (checked) "(*) $label" else "( ) $label"
            tv.setTextColor(if (checked) cAccent else cFg)
            tv.contentDescription = "$label, ${if (checked) "selected" else "not selected"}"
        }

        styleRadio(btnCycleAuto, currentConfig.timeCycleMode == TimeCycleMode.AUTO_REALTIME, "clock")
        styleRadio(btnCycleDawn, currentConfig.timeCycleMode == TimeCycleMode.DAWN, "dawn")
        styleRadio(btnCycleNoon, currentConfig.timeCycleMode == TimeCycleMode.NOON, "noon")
        styleRadio(btnCycleDusk, currentConfig.timeCycleMode == TimeCycleMode.DUSK, "dusk")
        styleRadio(btnCycleNight, currentConfig.timeCycleMode == TimeCycleMode.NIGHT, "night")

        textPredatorsCheck.text = if (currentConfig.enablePredators) "[x]" else "[ ]"
        textPredatorsCheck.setTextColor(if (currentConfig.enablePredators) cAccent else cDim)
        findViewById<LinearLayout>(R.id.rowPredatorsCheck).contentDescription =
            "Predator visits, ${if (currentConfig.enablePredators) "enabled" else "disabled"}"

        textBottomFeedersCheck.text = if (currentConfig.enableBottomFeeders) "[x]" else "[ ]"
        textBottomFeedersCheck.setTextColor(if (currentConfig.enableBottomFeeders) cAccent else cDim)
        findViewById<LinearLayout>(R.id.rowBottomFeedersCheck).contentDescription =
            "Bottom feeders, ${if (currentConfig.enableBottomFeeders) "enabled" else "disabled"}"

        textPauseBgSwitch.text = if (currentConfig.pauseOnBackgroundAndScreenOff) "[ on]" else "[off]"
        textPauseBgSwitch.setTextColor(if (currentConfig.pauseOnBackgroundAndScreenOff) cAccent else cDim)
        findViewById<LinearLayout>(R.id.rowPauseBgSwitch).contentDescription =
            "Pause when in background or screen is off, ${if (currentConfig.pauseOnBackgroundAndScreenOff) "on" else "off"}"

        styleRadio(btnPowerAuto, currentConfig.powerSaveMode == PowerSaveMode.AUTO_SAVER, "auto")
        styleRadio(btnPowerEco, currentConfig.powerSaveMode == PowerSaveMode.MAX_ECO, "eco")
        styleRadio(btnPowerOff, currentConfig.powerSaveMode == PowerSaveMode.OFF, "off")

        styleRadio(btnThemeSystem, currentConfig.themeMode == ThemeMode.SYSTEM, "system")
        styleRadio(btnThemeLight, currentConfig.themeMode == ThemeMode.LIGHT, "light")
        styleRadio(btnThemeDark, currentConfig.themeMode == ThemeMode.DARK, "dark")

        val fishCells = currentConfig.heroFishCount.coerceIn(2, 9)
        val fishBarStr = "[" + "#".repeat(fishCells) + "-".repeat(9 - fishCells) + "] $fishCells"
        textFishBar.text = "fish: $fishBarStr"
        textFishBar.contentDescription = "Main fish count: $fishCells out of 9"

        val clarityPct = (currentConfig.waterClarity * 100f).roundToInt()
        val clarityFilled = ((currentConfig.waterClarity * 10f).roundToInt()).coerceIn(1, 10)
        val clarityBarStr = "[" + "#".repeat(clarityFilled) + "-".repeat(10 - clarityFilled) + "] $clarityPct%"
        textClarityBar.text = "rays: $clarityBarStr"
        textClarityBar.contentDescription = "Water clarity and sun rays: $clarityPct percent"

        styleRadio(btnGridCozy, currentConfig.gridDensity == GridDensity.COZY, "38c")
        styleRadio(btnGridBalanced, currentConfig.gridDensity == GridDensity.BALANCED, "46c")
        styleRadio(btnGridDense, currentConfig.gridDensity == GridDensity.DENSE, "56c")

        styleRadio(btnFps30, currentConfig.targetFps == 30, "30fps")
        styleRadio(btnFps60, currentConfig.targetFps == 60, "60fps")

        textGyroSwitch.text = if (currentConfig.enableGyro) "[ on]" else "[off]"
        textGyroSwitch.setTextColor(if (currentConfig.enableGyro) cAccent else cDim)
        findViewById<LinearLayout>(R.id.rowGyroSwitch).contentDescription =
            "Gyroscope tilt parallax, ${if (currentConfig.enableGyro) "on" else "off"}"
    }

    private fun updateConfig(newConfig: WallpaperConfig) {
        currentConfig = newConfig
        repository.saveConfig(newConfig)
        refreshAsciiUi()
        asciiPondView.invalidate()
    }

    private fun showUndoBanner(msg: String) {
        textUndoMessage.text = msg
        bannerUndo.visibility = View.VISIBLE
        uiHandler.removeCallbacks(hideUndoRunnable)
        uiHandler.postDelayed(hideUndoRunnable, 6000L)
    }

    private fun haptic(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
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
                    showUndoBanner("! open system settings -> wallpaper -> minamo")
                }
            }
        }
    }
}

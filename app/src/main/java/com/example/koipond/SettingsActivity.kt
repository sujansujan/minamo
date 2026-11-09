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
    private lateinit var textHuntStatus: TextView
    private lateinit var textAboutTelemetry: TextView

    private lateinit var btnCycleAuto: TextView
    private lateinit var btnCycleDawn: TextView
    private lateinit var btnCycleNoon: TextView
    private lateinit var btnCycleDusk: TextView
    private lateinit var btnCycleNight: TextView

    private lateinit var btnWeatherAuto: TextView
    private lateinit var btnWeatherClear: TextView
    private lateinit var btnWeatherRain: TextView
    private lateinit var btnWeatherSnow: TextView
    private lateinit var btnWeatherAurora: TextView

    private lateinit var btnPalLagoon: TextView
    private lateinit var btnPalEmber: TextView
    private lateinit var btnPalMoss: TextView

    private lateinit var btnThemeSystem: TextView
    private lateinit var btnThemeLight: TextView
    private lateinit var btnThemeDark: TextView

    private lateinit var textFishBar: TextView
    private lateinit var textWaterlineBar: TextView
    private lateinit var textClarityBar: TextView

    private lateinit var textPredatorsCheck: TextView
    private lateinit var btnHuntNatural: TextView
    private lateinit var btnHuntFierce: TextView
    private lateinit var btnHuntHarmless: TextView
    private lateinit var textBottomFeedersCheck: TextView
    private lateinit var textPauseBgSwitch: TextView
    private lateinit var textBatteryLanternSwitch: TextView

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
        textHuntStatus = findViewById(R.id.textHuntStatus)
        textAboutTelemetry = findViewById(R.id.textAboutTelemetry)

        btnCycleAuto = findViewById(R.id.btnCycleAuto)
        btnCycleDawn = findViewById(R.id.btnCycleDawn)
        btnCycleNoon = findViewById(R.id.btnCycleNoon)
        btnCycleDusk = findViewById(R.id.btnCycleDusk)
        btnCycleNight = findViewById(R.id.btnCycleNight)

        btnWeatherAuto = findViewById(R.id.btnWeatherAuto)
        btnWeatherClear = findViewById(R.id.btnWeatherClear)
        btnWeatherRain = findViewById(R.id.btnWeatherRain)
        btnWeatherSnow = findViewById(R.id.btnWeatherSnow)
        btnWeatherAurora = findViewById(R.id.btnWeatherAurora)

        btnPalLagoon = findViewById(R.id.btnPalLagoon)
        btnPalEmber = findViewById(R.id.btnPalEmber)
        btnPalMoss = findViewById(R.id.btnPalMoss)

        btnThemeSystem = findViewById(R.id.btnThemeSystem)
        btnThemeLight = findViewById(R.id.btnThemeLight)
        btnThemeDark = findViewById(R.id.btnThemeDark)

        textFishBar = findViewById(R.id.textFishBar)
        textWaterlineBar = findViewById(R.id.textWaterlineBar)
        textClarityBar = findViewById(R.id.textClarityBar)

        textPredatorsCheck = findViewById(R.id.textPredatorsCheck)
        btnHuntNatural = findViewById(R.id.btnHuntNatural)
        btnHuntFierce = findViewById(R.id.btnHuntFierce)
        btnHuntHarmless = findViewById(R.id.btnHuntHarmless)

        textBottomFeedersCheck = findViewById(R.id.textBottomFeedersCheck)
        textPauseBgSwitch = findViewById(R.id.textPauseBgSwitch)
        textBatteryLanternSwitch = findViewById(R.id.textBatteryLanternSwitch)

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
            textBiomeStatus.text = "biome ... " + speciesStr
            textBiomeStatus.contentDescription = "Active fish biome: " + speciesStr
            textHeaderClockTag.text = "[" + timeLabel + "]"
        }

        asciiPondView.onWaferDroppedListener = {
            val cGood = ContextCompat.getColor(this, R.color.ascii_good)
            textHuntStatus.setTextColor(cGood)
            textHuntStatus.text = "event ... [@] algae wafer dropped for bottom feeders"
        }

        asciiPondView.simulation.onCrownLevelUpListener = { speciesName ->
            repository.recordCrownKoiLevelUp(speciesName)
            val cGold = ContextCompat.getColor(this, R.color.ascii_accent3)
            textHuntStatus.setTextColor(cGold)
            textHuntStatus.text = "event ... ^ " + speciesName + " leveled up to elder crown!"
            updateTelemetryBox()
        }

        asciiPondView.simulation.onHuntOutcomeListener = { caught, predName, preyName ->
            if (caught) {
                repository.recordCatchEvent(predName, preyName)
                val cErr = ContextCompat.getColor(this, R.color.ascii_error)
                textHuntStatus.setTextColor(cErr)
                textHuntStatus.text = "event ... ! " + predName + " caught " + preyName
            } else {
                repository.recordEscapeEvent(predName)
                val cGood = ContextCompat.getColor(this, R.color.ascii_good)
                textHuntStatus.setTextColor(cGood)
                textHuntStatus.text = "event ... ok: school evaded " + predName
            }
            updateTelemetryBox()
        }

        findViewById<TextView>(R.id.btnShuffleFish).apply {
            contentDescription = "Shuffle fish and critters"
            setOnClickListener {
                haptic(it)
                asciiPondView.shuffleEcosystem()
                textHuntStatus.setTextColor(ContextCompat.getColor(this@SettingsActivity, R.color.ascii_fg))
                textHuntStatus.text = "event ... ok: new biome spawned (hold water for [@] wafer)"
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
                textHuntStatus.setTextColor(ContextCompat.getColor(this@SettingsActivity, R.color.ascii_warn))
                textHuntStatus.text = "event ... ! " + predName + " hunting"
            }
        }

        findViewById<TextView>(R.id.btnSetWallpaper).apply {
            contentDescription = "Set live wallpaper"
            setOnClickListener {
                haptic(it)
                launchWallpaperPicker()
            }
        }

        btnCycleAuto.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(timeCycleMode = TimeCycleMode.AUTO_REALTIME)) }
        btnCycleDawn.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(timeCycleMode = TimeCycleMode.DAWN)) }
        btnCycleNoon.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(timeCycleMode = TimeCycleMode.NOON)) }
        btnCycleDusk.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(timeCycleMode = TimeCycleMode.DUSK)) }
        btnCycleNight.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(timeCycleMode = TimeCycleMode.NIGHT)) }

        btnWeatherAuto.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(weatherMode = WeatherMode.AUTO)) }
        btnWeatherClear.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(weatherMode = WeatherMode.CLEAR)) }
        btnWeatherRain.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(weatherMode = WeatherMode.RAIN)) }
        btnWeatherSnow.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(weatherMode = WeatherMode.SNOW)) }
        btnWeatherAurora.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(weatherMode = WeatherMode.AURORA)) }

        btnPalLagoon.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(paletteFamily = PaletteFamily.LAGOON)) }
        btnPalEmber.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(paletteFamily = PaletteFamily.EMBER)) }
        btnPalMoss.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(paletteFamily = PaletteFamily.MOSS)) }

        findViewById<LinearLayout>(R.id.rowPredatorsCheck).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(enablePredators = !currentConfig.enablePredators))
        }
        btnHuntNatural.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(huntMode = HuntMode.NATURAL)) }
        btnHuntFierce.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(huntMode = HuntMode.FIERCE)) }
        btnHuntHarmless.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(huntMode = HuntMode.HARMLESS)) }

        findViewById<LinearLayout>(R.id.rowBottomFeedersCheck).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(enableBottomFeeders = !currentConfig.enableBottomFeeders))
        }

        findViewById<LinearLayout>(R.id.rowPauseBgSwitch).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(pauseOnBackgroundAndScreenOff = !currentConfig.pauseOnBackgroundAndScreenOff))
        }
        findViewById<LinearLayout>(R.id.rowBatteryLanternSwitch).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(enableBatteryLantern = !currentConfig.enableBatteryLantern))
        }

        btnPowerAuto.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(powerSaveMode = PowerSaveMode.AUTO_SAVER)) }
        btnPowerEco.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(powerSaveMode = PowerSaveMode.MAX_ECO)) }
        btnPowerOff.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(powerSaveMode = PowerSaveMode.OFF)) }

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

        findViewById<TextView>(R.id.btnWaterlineMinus).apply {
            contentDescription = "Move waterline higher"
            setOnClickListener {
                haptic(it)
                updateConfig(currentConfig.copy(waterlinePercent = (currentConfig.waterlinePercent - 4).coerceIn(25, 65)))
            }
        }
        findViewById<TextView>(R.id.btnWaterlinePlus).apply {
            contentDescription = "Move waterline lower"
            setOnClickListener {
                haptic(it)
                updateConfig(currentConfig.copy(waterlinePercent = (currentConfig.waterlinePercent + 4).coerceIn(25, 65)))
            }
        }

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

        btnGridCozy.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(gridDensity = GridDensity.COZY)) }
        btnGridBalanced.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(gridDensity = GridDensity.BALANCED)) }
        btnGridDense.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(gridDensity = GridDensity.DENSE)) }

        btnFps30.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(targetFps = 30)) }
        btnFps60.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(targetFps = 60)) }

        findViewById<LinearLayout>(R.id.rowGyroSwitch).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(enableGyro = !currentConfig.enableGyro))
        }

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

        if (tab == 3) {
            updateTelemetryBox()
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

    private fun updateTelemetryBox() {
        val catches = repository.getTotalCatches()
        val escapes = repository.getTotalEscapes()
        val crowns = repository.getTotalCrowns()
        val lastEv = repository.getLastEvent()
        val nl = 10.toChar()
        textAboutTelemetry.text =
            "predator catches ... " + catches.toString() + nl +
            "school escapes ..... " + escapes.toString() + nl +
            "elder crown fish ... " + crowns.toString() + nl +
            "last hunt event .... " + lastEv + nl +
            "feeding ............ tap (*) pellet / hold [@] wafer" + nl +
            "quick settings ..... minamo [>°] tile enabled"
    }

    private fun refreshAsciiUi() {
        asciiPondView.config = currentConfig
        val snap = TimeOfDayPalette.resolve(currentConfig.timeCycleMode, currentConfig.paletteFamily)
        textHeaderClockTag.text = "[" + snap.timeLabel + "]"
        textBiomeStatus.text = "biome ... " + asciiPondView.simulation.activeSpeciesSummary
        updateTelemetryBox()

        val cAccent = ContextCompat.getColor(this, R.color.ascii_accent)
        val cFg = ContextCompat.getColor(this, R.color.ascii_fg)
        val cDim = ContextCompat.getColor(this, R.color.ascii_dim)

        fun styleRadio(tv: TextView, checked: Boolean, label: String) {
            tv.text = if (checked) "(*) " + label else "( ) " + label
            tv.setTextColor(if (checked) cAccent else cFg)
            tv.contentDescription = label + ", " + (if (checked) "selected" else "not selected")
        }

        fun styleSwitch(tv: TextView, row: View, enabled: Boolean, spokenName: String) {
            tv.text = if (enabled) "[ on]" else "[off]"
            tv.setTextColor(if (enabled) cAccent else cDim)
            row.contentDescription = spokenName + ", " + (if (enabled) "on" else "off")
        }

        styleRadio(btnCycleAuto, currentConfig.timeCycleMode == TimeCycleMode.AUTO_REALTIME, "clock")
        styleRadio(btnCycleDawn, currentConfig.timeCycleMode == TimeCycleMode.DAWN, "dawn")
        styleRadio(btnCycleNoon, currentConfig.timeCycleMode == TimeCycleMode.NOON, "noon")
        styleRadio(btnCycleDusk, currentConfig.timeCycleMode == TimeCycleMode.DUSK, "dusk")
        styleRadio(btnCycleNight, currentConfig.timeCycleMode == TimeCycleMode.NIGHT, "night")

        styleRadio(btnWeatherAuto, currentConfig.weatherMode == WeatherMode.AUTO, "auto")
        styleRadio(btnWeatherClear, currentConfig.weatherMode == WeatherMode.CLEAR, "clear")
        styleRadio(btnWeatherRain, currentConfig.weatherMode == WeatherMode.RAIN, "rain")
        styleRadio(btnWeatherSnow, currentConfig.weatherMode == WeatherMode.SNOW, "snow")
        styleRadio(btnWeatherAurora, currentConfig.weatherMode == WeatherMode.AURORA, "aurora")

        styleRadio(btnPalLagoon, currentConfig.paletteFamily == PaletteFamily.LAGOON, "lagoon")
        styleRadio(btnPalEmber, currentConfig.paletteFamily == PaletteFamily.EMBER, "ember")
        styleRadio(btnPalMoss, currentConfig.paletteFamily == PaletteFamily.MOSS, "moss")

        styleRadio(btnThemeSystem, currentConfig.themeMode == ThemeMode.SYSTEM, "system")
        styleRadio(btnThemeLight, currentConfig.themeMode == ThemeMode.LIGHT, "light")
        styleRadio(btnThemeDark, currentConfig.themeMode == ThemeMode.DARK, "dark")

        styleSwitch(
            textPredatorsCheck,
            findViewById(R.id.rowPredatorsCheck),
            currentConfig.enablePredators,
            "Predator ambushes"
        )
        styleRadio(btnHuntNatural, currentConfig.huntMode == HuntMode.NATURAL, "natural")
        styleRadio(btnHuntFierce, currentConfig.huntMode == HuntMode.FIERCE, "fierce")
        styleRadio(btnHuntHarmless, currentConfig.huntMode == HuntMode.HARMLESS, "harmless")

        styleSwitch(
            textBottomFeedersCheck,
            findViewById(R.id.rowBottomFeedersCheck),
            currentConfig.enableBottomFeeders,
            "Bottom-feeder critters"
        )
        styleSwitch(
            textPauseBgSwitch,
            findViewById(R.id.rowPauseBgSwitch),
            currentConfig.pauseOnBackgroundAndScreenOff,
            "Pause when in background or screen is off"
        )
        styleSwitch(
            textBatteryLanternSwitch,
            findViewById(R.id.rowBatteryLanternSwitch),
            currentConfig.enableBatteryLantern,
            "Battery level stone lantern"
        )
        styleSwitch(
            textGyroSwitch,
            findViewById(R.id.rowGyroSwitch),
            currentConfig.enableGyro,
            "Gyroscope tilt parallax"
        )

        styleRadio(btnPowerAuto, currentConfig.powerSaveMode == PowerSaveMode.AUTO_SAVER, "auto")
        styleRadio(btnPowerEco, currentConfig.powerSaveMode == PowerSaveMode.MAX_ECO, "eco")
        styleRadio(btnPowerOff, currentConfig.powerSaveMode == PowerSaveMode.OFF, "off")

        val fishCells = currentConfig.heroFishCount.coerceIn(2, 9)
        val fishBarStr = "[" + "#".repeat(fishCells) + "-".repeat(9 - fishCells) + "] " + fishCells.toString()
        textFishBar.text = "fish: " + fishBarStr
        textFishBar.contentDescription = "Main fish count: " + fishCells.toString() + " out of 9"

        val wlPct = currentConfig.waterlinePercent.coerceIn(25, 65)
        val wlFilled = (((wlPct - 25) / 40f) * 10f).roundToInt().coerceIn(1, 10)
        val wlBarStr = "[" + "#".repeat(wlFilled) + "-".repeat(10 - wlFilled) + "] " + wlPct.toString() + "%"
        textWaterlineBar.text = "line: " + wlBarStr
        textWaterlineBar.contentDescription = "Waterline split position: " + wlPct.toString() + " percent from top"

        val clarityPct = (currentConfig.waterClarity * 100f).roundToInt()
        val clarityFilled = ((currentConfig.waterClarity * 10f).roundToInt()).coerceIn(1, 10)
        val clarityBarStr = "[" + "#".repeat(clarityFilled) + "-".repeat(10 - clarityFilled) + "] " + clarityPct.toString() + "%"
        textClarityBar.text = "rays: " + clarityBarStr
        textClarityBar.contentDescription = "Water clarity and sun rays: " + clarityPct.toString() + " percent"

        styleRadio(btnGridCozy, currentConfig.gridDensity == GridDensity.COZY, "68c")
        styleRadio(btnGridBalanced, currentConfig.gridDensity == GridDensity.BALANCED, "82c")
        styleRadio(btnGridDense, currentConfig.gridDensity == GridDensity.DENSE, "96c")

        styleRadio(btnFps30, currentConfig.targetFps == 30, "30fps")
        styleRadio(btnFps60, currentConfig.targetFps == 60, "60fps")
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

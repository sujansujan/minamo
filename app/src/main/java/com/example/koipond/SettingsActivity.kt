package com.example.koipond

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class SettingsActivity : AppCompatActivity() {

    private lateinit var repository: WallpaperSettingsRepository
    private lateinit var currentConfig: WallpaperConfig
    private var previousConfigForUndo: WallpaperConfig? = null

    private lateinit var headerBar: View
    private lateinit var headerDivider: View
    private lateinit var textHeaderPath: TextView
    private lateinit var textHeaderClockTag: TextView
    private lateinit var bannerUndo: LinearLayout
    private lateinit var textUndoMessage: TextView
    private lateinit var btnUndoReset: TextView

    private lateinit var screenPond: LinearLayout
    private lateinit var pondBoxContainer: LinearLayout
    private lateinit var textPondBoxTitle: TextView
    private lateinit var pondActionBar: View
    private lateinit var bottomNavDivider: View
    private lateinit var bottomNavBar: View
    private lateinit var screenSettings: ScrollView
    private lateinit var screenAbout: ScrollView

    private lateinit var navPond: TextView
    private lateinit var navSet: TextView
    private lateinit var navAbout: TextView

    private lateinit var asciiPondView: AsciiPondView
    private lateinit var textSolarBar: TextView
    private lateinit var btnSolarLive: TextView
    private lateinit var btnFullscreenPreview: TextView
    private lateinit var textBiomeStatus: TextView
    private lateinit var textHuntStatus: TextView
    private lateinit var textAboutTelemetry: TextView

    private lateinit var btnPresetReef: TextView
    private lateinit var btnPresetAbyss: TextView
    private lateinit var btnPresetZen: TextView
    private lateinit var btnPresetBetta: TextView
    private lateinit var btnPresetOled: TextView

    private lateinit var btnBiomeOcean: TextView
    private lateinit var btnBiomePond: TextView
    private lateinit var btnBiomeAquarium: TextView
    private lateinit var btnSummonPredator: TextView
    private lateinit var textSceneSettingsTitle: TextView
    private lateinit var groupOutdoorOnly: LinearLayout
    private lateinit var groupAquariumOnly: LinearLayout
    private lateinit var btnBettaCount2: TextView
    private lateinit var btnBettaCount3: TextView
    private lateinit var textCatVisitorSwitch: TextView
    private lateinit var textBottomFeedersLabel: TextView
    private lateinit var textBatteryLanternLabel: TextView

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
    private lateinit var btnPalAmoled: TextView

    private lateinit var btnThemeSolar: TextView
    private lateinit var btnThemeSystem: TextView
    private lateinit var btnThemeLight: TextView
    private lateinit var btnThemeDark: TextView

    private lateinit var textFishBar: TextView
    private lateinit var textWaterlineBar: TextView
    private lateinit var textClarityBar: TextView

    private lateinit var btnHuntOff: TextView
    private lateinit var btnHuntHarmless: TextView
    private lateinit var btnHuntNatural: TextView
    private lateinit var btnHuntFierce: TextView
    private lateinit var textBottomFeedersCheck: TextView
    private lateinit var textBatteryLanternSwitch: TextView

    private lateinit var btnPerfEco: TextView
    private lateinit var btnPerfBalanced: TextView
    private lateinit var btnPerfSmooth: TextView

    private lateinit var textCalmIconsSwitch: TextView
    private lateinit var textGyroSwitch: TextView

    private var activeTab = 1
    private var isFullscreenPreview = false
    private val uiHandler = Handler(Looper.getMainLooper())
    private val hideUndoRunnable = Runnable {
        bannerUndo.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        repository = WallpaperSettingsRepository(this)
        currentConfig = repository.loadConfig()
        repository.applyAppNightMode(currentConfig.themeMode, currentConfig.timeCycleMode)

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
            isFullscreenPreview = savedInstanceState.getBoolean("fullscreen_preview", false)
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFullscreenPreview) {
                    setFullscreenPreview(false)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
        switchTab(activeTab)
        setFullscreenPreview(isFullscreenPreview)
        refreshAsciiUi()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("active_tab", activeTab)
        outState.putBoolean("fullscreen_preview", isFullscreenPreview)
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
        headerBar = findViewById(R.id.headerBar)
        headerDivider = findViewById(R.id.headerDivider)
        textHeaderPath = findViewById(R.id.textHeaderPath)
        textHeaderClockTag = findViewById(R.id.textHeaderClockTag)
        bannerUndo = findViewById(R.id.bannerUndo)
        textUndoMessage = findViewById(R.id.textUndoMessage)
        btnUndoReset = findViewById(R.id.btnUndoReset)

        screenPond = findViewById(R.id.screenPond)
        pondBoxContainer = findViewById(R.id.pondBoxContainer)
        textPondBoxTitle = findViewById(R.id.textPondBoxTitle)
        pondActionBar = findViewById(R.id.pondActionBar)
        bottomNavDivider = findViewById(R.id.bottomNavDivider)
        bottomNavBar = findViewById(R.id.bottomNavBar)
        screenSettings = findViewById(R.id.screenSettings)
        screenAbout = findViewById(R.id.screenAbout)

        navPond = findViewById(R.id.navPond)
        navSet = findViewById(R.id.navSet)
        navAbout = findViewById(R.id.navAbout)

        asciiPondView = findViewById(R.id.asciiPondView)
        textSolarBar = findViewById(R.id.textSolarBar)
        btnSolarLive = findViewById(R.id.btnSolarLive)
        btnFullscreenPreview = findViewById(R.id.btnFullscreenPreview)
        textBiomeStatus = findViewById(R.id.textBiomeStatus)
        textHuntStatus = findViewById(R.id.textHuntStatus)
        textAboutTelemetry = findViewById(R.id.textAboutTelemetry)

        btnPresetReef = findViewById(R.id.btnPresetReef)
        btnPresetAbyss = findViewById(R.id.btnPresetAbyss)
        btnPresetZen = findViewById(R.id.btnPresetZen)
        btnPresetBetta = findViewById(R.id.btnPresetBetta)
        btnPresetOled = findViewById(R.id.btnPresetOled)

        btnBiomeOcean = findViewById(R.id.btnBiomeOcean)
        btnBiomePond = findViewById(R.id.btnBiomePond)
        btnBiomeAquarium = findViewById(R.id.btnBiomeAquarium)
        btnSummonPredator = findViewById(R.id.btnSummonPredator)
        textSceneSettingsTitle = findViewById(R.id.textSceneSettingsTitle)
        groupOutdoorOnly = findViewById(R.id.groupOutdoorOnly)
        groupAquariumOnly = findViewById(R.id.groupAquariumOnly)
        btnBettaCount2 = findViewById(R.id.btnBettaCount2)
        btnBettaCount3 = findViewById(R.id.btnBettaCount3)
        textCatVisitorSwitch = findViewById(R.id.textCatVisitorSwitch)
        textBottomFeedersLabel = findViewById(R.id.textBottomFeedersLabel)
        textBatteryLanternLabel = findViewById(R.id.textBatteryLanternLabel)

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
        btnPalAmoled = findViewById(R.id.btnPalAmoled)

        btnThemeSolar = findViewById(R.id.btnThemeSolar)
        btnThemeSystem = findViewById(R.id.btnThemeSystem)
        btnThemeLight = findViewById(R.id.btnThemeLight)
        btnThemeDark = findViewById(R.id.btnThemeDark)

        textFishBar = findViewById(R.id.textFishBar)
        textWaterlineBar = findViewById(R.id.textWaterlineBar)
        textClarityBar = findViewById(R.id.textClarityBar)

        btnHuntOff = findViewById(R.id.btnHuntOff)
        btnHuntHarmless = findViewById(R.id.btnHuntHarmless)
        btnHuntNatural = findViewById(R.id.btnHuntNatural)
        btnHuntFierce = findViewById(R.id.btnHuntFierce)

        textBottomFeedersCheck = findViewById(R.id.textBottomFeedersCheck)
        textBatteryLanternSwitch = findViewById(R.id.textBatteryLanternSwitch)

        btnPerfEco = findViewById(R.id.btnPerfEco)
        btnPerfBalanced = findViewById(R.id.btnPerfBalanced)
        btnPerfSmooth = findViewById(R.id.btnPerfSmooth)

        textCalmIconsSwitch = findViewById(R.id.textCalmIconsSwitch)
        textGyroSwitch = findViewById(R.id.textGyroSwitch)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        navPond.setOnClickListener { haptic(it); switchTab(1) }
        navSet.setOnClickListener { haptic(it); switchTab(2) }
        navAbout.setOnClickListener { haptic(it); switchTab(3) }

        asciiPondView.onSpeciesRandomized = { speciesStr, timeLabel ->
            textBiomeStatus.text = "biome ... " + speciesStr
            textBiomeStatus.contentDescription = "Active ocean biome: " + speciesStr
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
            textHuntStatus.text = "event ... * " + speciesName + " earned golden fins!"
            updateTelemetryBox()
        }

        asciiPondView.simulation.onLegendarySightingListener = { legName ->
            repository.recordLegendarySighting(legName)
            val cGold = ContextCompat.getColor(this, R.color.ascii_accent3)
            textHuntStatus.setTextColor(cGold)
            textHuntStatus.text = "event ... ★ rare legendary " + legName + " appeared!"
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
            contentDescription = "Shuffle ocean wildlife or refresh betta aquarium"
            setOnClickListener {
                haptic(it)
                val visitor = asciiPondView.shuffleEcosystem()
                textHuntStatus.setTextColor(ContextCompat.getColor(this@SettingsActivity, R.color.ascii_accent2))
                if (currentConfig.sceneBiome == SceneBiome.AQUARIUM) {
                    textHuntStatus.text = "aquarium ... ~ refreshed betta morphs!"
                } else {
                    textHuntStatus.text = "ocean ... ~ " + visitor + " gliding past window"
                }
            }
        }

        findViewById<TextView>(R.id.btnSummonPredator).apply {
            contentDescription = "Summon predator fish or curious aquarium cat"
            setOnClickListener {
                haptic(it)
                if (currentConfig.sceneBiome == SceneBiome.AQUARIUM) {
                    if (!currentConfig.enableCatVisitor) {
                        updateConfig(currentConfig.copy(enableCatVisitor = true))
                    }
                    val catEvent = asciiPondView.summonPredatorNow()
                    textHuntStatus.setTextColor(ContextCompat.getColor(this@SettingsActivity, R.color.ascii_warn))
                    textHuntStatus.text = "aquarium ... ! " + catEvent
                } else {
                    if (!currentConfig.huntMode.isEnabled) {
                        updateConfig(currentConfig.copy(huntMode = HuntMode.NATURAL))
                    }
                    val predName = asciiPondView.summonPredatorNow()
                    textHuntStatus.setTextColor(ContextCompat.getColor(this@SettingsActivity, R.color.ascii_warn))
                    textHuntStatus.text = "event ... ! " + predName + " hunting"
                }
            }
        }

        findViewById<TextView>(R.id.btnSetWallpaper).apply {
            contentDescription = "Set live wallpaper"
            setOnClickListener {
                haptic(it)
                launchWallpaperPicker()
            }
        }

        // 24-Hour Solar Preview Scrubber: drag across the ASCII bar to preview any hour live!
        textSolarBar.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val w = v.width.coerceAtLeast(1).toFloat()
                    val frac = (event.x / w).coerceIn(0f, 0.999f)
                    asciiPondView.previewHourOverride = frac * 24.0f
                    updateSolarScrubberUi()
                    asciiPondView.invalidate()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                    true
                }
                else -> false
            }
        }
        btnSolarLive.contentDescription = "Return preview to live clock time"
        btnSolarLive.setOnClickListener {
            haptic(it)
            asciiPondView.previewHourOverride = null
            updateSolarScrubberUi()
            asciiPondView.invalidate()
        }

        btnFullscreenPreview.contentDescription = "Toggle fullscreen live preview"
        btnFullscreenPreview.setOnClickListener {
            haptic(it)
            setFullscreenPreview(!isFullscreenPreview)
        }

        fun applyMoodPreset(label: String, preset: WallpaperConfig) {
            previousConfigForUndo = currentConfig
            updateConfig(preset)
            repository.applyAppNightMode(preset.themeMode, preset.timeCycleMode)
            showUndoBanner("ok: applied " + label + " mood.")
        }
        btnPresetReef.contentDescription = "Apply Tropical Reef mood preset"
        btnPresetReef.setOnClickListener {
            haptic(it)
            applyMoodPreset(
                "tropical reef",
                currentConfig.copy(
                    sceneBiome = SceneBiome.OCEAN,
                    paletteFamily = PaletteFamily.LAGOON,
                    themeMode = ThemeMode.SOLAR,
                    timeCycleMode = TimeCycleMode.AUTO_REALTIME,
                    weatherMode = WeatherMode.AUTO,
                    heroFishCount = 7,
                    waterClarity = 0.90f,
                    waterlinePercent = 36,
                    calmIconMode = false
                )
            )
        }
        btnPresetAbyss.contentDescription = "Apply Deep Abyss aurora mood preset"
        btnPresetAbyss.setOnClickListener {
            haptic(it)
            applyMoodPreset(
                "deep abyss",
                currentConfig.copy(
                    sceneBiome = SceneBiome.OCEAN,
                    paletteFamily = PaletteFamily.LAGOON,
                    themeMode = ThemeMode.DARK,
                    timeCycleMode = TimeCycleMode.NIGHT,
                    weatherMode = WeatherMode.AURORA,
                    heroFishCount = 6,
                    waterClarity = 0.72f,
                    waterlinePercent = 30,
                    calmIconMode = false
                )
            )
        }
        btnPresetZen.contentDescription = "Apply Zen Koi pond mood preset"
        btnPresetZen.setOnClickListener {
            haptic(it)
            applyMoodPreset(
                "zen koi",
                currentConfig.copy(
                    sceneBiome = SceneBiome.ZEN_POND,
                    paletteFamily = PaletteFamily.MOSS,
                    themeMode = ThemeMode.SOLAR,
                    timeCycleMode = TimeCycleMode.AUTO_REALTIME,
                    weatherMode = WeatherMode.CLEAR,
                    heroFishCount = 5,
                    waterClarity = 0.85f,
                    waterlinePercent = 38,
                    calmIconMode = false
                )
            )
        }
        btnPresetBetta.contentDescription = "Apply Natural Betta Aquarium mood preset"
        btnPresetBetta.setOnClickListener {
            haptic(it)
            applyMoodPreset(
                "betta tank",
                currentConfig.copy(
                    sceneBiome = SceneBiome.AQUARIUM,
                    paletteFamily = PaletteFamily.LAGOON,
                    themeMode = ThemeMode.SOLAR,
                    timeCycleMode = TimeCycleMode.AUTO_REALTIME,
                    weatherMode = WeatherMode.CLEAR,
                    heroFishCount = 6,
                    waterClarity = 0.88f,
                    waterlinePercent = 38,
                    enableBottomFeeders = true,
                    calmIconMode = false
                )
            )
        }
        btnPresetOled.contentDescription = "Apply AMOLED Minimal battery-saving mood preset"
        btnPresetOled.setOnClickListener {
            haptic(it)
            applyMoodPreset(
                "amoled minimal",
                currentConfig.copy(
                    paletteFamily = PaletteFamily.AMOLED,
                    themeMode = ThemeMode.DARK,
                    timeCycleMode = TimeCycleMode.AUTO_REALTIME,
                    weatherMode = WeatherMode.CLEAR,
                    waterClarity = 0.72f,
                    calmIconMode = true
                )
            )
        }

        btnBiomeOcean.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(sceneBiome = SceneBiome.OCEAN)) }
        btnBiomePond.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(sceneBiome = SceneBiome.ZEN_POND)) }
        btnBiomeAquarium.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(sceneBiome = SceneBiome.AQUARIUM)) }

        fun selectTimeCycle(mode: TimeCycleMode) {
            updateConfig(currentConfig.copy(timeCycleMode = mode))
            if (currentConfig.themeMode == ThemeMode.SOLAR) {
                repository.applyAppNightMode(ThemeMode.SOLAR, mode)
            }
        }
        btnCycleAuto.setOnClickListener { haptic(it); selectTimeCycle(TimeCycleMode.AUTO_REALTIME) }
        btnCycleDawn.setOnClickListener { haptic(it); selectTimeCycle(TimeCycleMode.DAWN) }
        btnCycleNoon.setOnClickListener { haptic(it); selectTimeCycle(TimeCycleMode.NOON) }
        btnCycleDusk.setOnClickListener { haptic(it); selectTimeCycle(TimeCycleMode.DUSK) }
        btnCycleNight.setOnClickListener { haptic(it); selectTimeCycle(TimeCycleMode.NIGHT) }

        btnWeatherAuto.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(weatherMode = WeatherMode.AUTO)) }
        btnWeatherClear.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(weatherMode = WeatherMode.CLEAR)) }
        btnWeatherRain.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(weatherMode = WeatherMode.RAIN)) }
        btnWeatherSnow.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(weatherMode = WeatherMode.SNOW)) }
        btnWeatherAurora.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(weatherMode = WeatherMode.AURORA)) }

        btnPalLagoon.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(paletteFamily = PaletteFamily.LAGOON)) }
        btnPalEmber.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(paletteFamily = PaletteFamily.EMBER)) }
        btnPalMoss.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(paletteFamily = PaletteFamily.MOSS)) }
        btnPalAmoled.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(paletteFamily = PaletteFamily.AMOLED)) }

        btnHuntOff.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(huntMode = HuntMode.OFF)) }
        btnHuntHarmless.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(huntMode = HuntMode.HARMLESS)) }
        btnHuntNatural.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(huntMode = HuntMode.NATURAL)) }
        btnHuntFierce.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(huntMode = HuntMode.FIERCE)) }

        findViewById<LinearLayout>(R.id.rowBottomFeedersCheck).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(enableBottomFeeders = !currentConfig.enableBottomFeeders))
        }
        findViewById<LinearLayout>(R.id.rowBatteryLanternSwitch).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(enableBatteryLantern = !currentConfig.enableBatteryLantern))
        }

        btnPerfEco.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(performanceMode = PerformanceMode.ECO)) }
        btnPerfBalanced.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(performanceMode = PerformanceMode.BALANCED)) }
        btnPerfSmooth.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(performanceMode = PerformanceMode.SMOOTH)) }

        fun selectThemeMode(mode: ThemeMode) {
            updateConfig(currentConfig.copy(themeMode = mode))
            repository.applyAppNightMode(mode, currentConfig.timeCycleMode)
        }
        btnThemeSolar.setOnClickListener { haptic(it); selectThemeMode(ThemeMode.SOLAR) }
        btnThemeSystem.setOnClickListener { haptic(it); selectThemeMode(ThemeMode.SYSTEM) }
        btnThemeLight.setOnClickListener { haptic(it); selectThemeMode(ThemeMode.LIGHT) }
        btnThemeDark.setOnClickListener { haptic(it); selectThemeMode(ThemeMode.DARK) }

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

        btnBettaCount2.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(heroFishCount = 3)) }
        btnBettaCount3.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(heroFishCount = 6)) }
        findViewById<LinearLayout>(R.id.rowCatVisitorSwitch).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(enableCatVisitor = !currentConfig.enableCatVisitor))
        }
        findViewById<LinearLayout>(R.id.rowCalmIconsSwitch).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(calmIconMode = !currentConfig.calmIconMode))
        }
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
                repository.applyAppNightMode(defaults.themeMode, defaults.timeCycleMode)
                showUndoBanner("ok: reset to defaults.")
            }
        }

        btnUndoReset.contentDescription = "Undo reset"
        btnUndoReset.setOnClickListener {
            haptic(it)
            previousConfigForUndo?.let { prev ->
                updateConfig(prev)
                repository.applyAppNightMode(prev.themeMode, prev.timeCycleMode)
            }
            bannerUndo.visibility = View.GONE
        }
    }

    private fun setFullscreenPreview(enabled: Boolean) {
        isFullscreenPreview = enabled && (activeTab == 1)
        val chromeVis = if (isFullscreenPreview) View.GONE else View.VISIBLE
        headerBar.visibility = chromeVis
        headerDivider.visibility = chromeVis
        textPondBoxTitle.visibility = chromeVis
        textBiomeStatus.visibility = chromeVis
        textHuntStatus.visibility = chromeVis
        pondActionBar.visibility = chromeVis
        bottomNavDivider.visibility = chromeVis
        bottomNavBar.visibility = chromeVis

        val padPx = if (isFullscreenPreview) 0 else (12f * resources.displayMetrics.density).roundToInt()
        val boxPadPx = if (isFullscreenPreview) 0 else (2f * resources.displayMetrics.density).roundToInt()
        val boxTopMarginPx = if (isFullscreenPreview) 0 else (9f * resources.displayMetrics.density).roundToInt()
        screenPond.setPadding(padPx, padPx, padPx, padPx)
        pondBoxContainer.setPadding(boxPadPx, boxPadPx, boxPadPx, boxPadPx)
        val lp = pondBoxContainer.layoutParams as? android.widget.FrameLayout.LayoutParams
        if (lp != null) {
            lp.topMargin = boxTopMarginPx
            pondBoxContainer.layoutParams = lp
        }
        if (isFullscreenPreview) {
            pondBoxContainer.background = null
        } else {
            pondBoxContainer.setBackgroundResource(R.drawable.bg_ascii_box)
        }
        btnFullscreenPreview.text = if (isFullscreenPreview) "[exit]" else "[full]"
        btnFullscreenPreview.contentDescription = if (isFullscreenPreview) "Exit fullscreen live preview" else "Expand fullscreen live preview"
    }

    private fun switchTab(tab: Int) {
        activeTab = tab
        if (tab != 1 && isFullscreenPreview) {
            setFullscreenPreview(false)
        }
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
        val legendaries = repository.getTotalLegendaries()
        val lastEv = repository.getLastEvent()
        val nl = 10.toChar()
        textAboutTelemetry.text =
            "rare legendaries ... " + legendaries.toString() + " (ryujin / oarfish / coelacanth)" + nl +
            "predator catches ... " + catches.toString() + nl +
            "school escapes ..... " + escapes.toString() + nl +
            "elder crown fish ... " + crowns.toString() + nl +
            "last pond event .... " + lastEv + nl +
            "engine ............. solar lab + spine wave + eco vsync"
    }

    private fun updateSolarScrubberUi() {
        val overrideH = asciiPondView.previewHourOverride
        val snap = TimeOfDayPalette.resolve(
            mode = currentConfig.timeCycleMode,
            family = currentConfig.paletteFamily,
            themeMode = currentConfig.themeMode,
            isSystemDark = isSystemNightMode(),
            overrideHour = overrideH
        )
        textHeaderClockTag.text = "[" + snap.timeLabel + "]"

        val slots = 14
        if (overrideH == null) {
            val cal = Calendar.getInstance()
            val curH = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
            val pos = ((curH / 24f) * (slots - 1)).roundToInt().coerceIn(0, slots - 1)
            val bar = "-".repeat(pos) + "|" + "-".repeat(slots - 1 - pos)
            textSolarBar.text = "sun: [" + bar + "] live"
            btnSolarLive.setTextColor(ContextCompat.getColor(this, R.color.ascii_dim))
        } else {
            val pos = ((overrideH / 24f) * (slots - 1)).roundToInt().coerceIn(0, slots - 1)
            val bar = "=".repeat(pos) + "|" + "-".repeat(slots - 1 - pos)
            val hInt = overrideH.toInt().coerceIn(0, 23)
            val mInt = (((overrideH - hInt) * 60f).toInt()).coerceIn(0, 59)
            val tStr = String.format(Locale.US, "%02d:%02d", hInt, mInt)
            textSolarBar.text = "sun: [" + bar + "] " + tStr
            btnSolarLive.setTextColor(ContextCompat.getColor(this, R.color.ascii_accent))
        }
    }

    private fun refreshAsciiUi() {
        asciiPondView.config = currentConfig
        updateSolarScrubberUi()
        val isAquarium = currentConfig.sceneBiome == SceneBiome.AQUARIUM
        textBiomeStatus.text = if (isAquarium) {
            "aquarium ... bettas · rasboras · glass cat · kuhli · cleaners"
        } else {
            "biome ... " + asciiPondView.simulation.activeSpeciesSummary
        }
        btnSummonPredator.text = if (isAquarium) "[cat]" else getString(R.string.action_summon_predator)
        textSceneSettingsTitle.text = when (currentConfig.sceneBiome) {
            SceneBiome.OCEAN -> " 1. ocean settings "
            SceneBiome.ZEN_POND -> " 1. zen pond settings "
            SceneBiome.AQUARIUM -> " 1. betta tank settings "
        }
        groupOutdoorOnly.visibility = if (isAquarium) View.GONE else View.VISIBLE
        groupAquariumOnly.visibility = if (isAquarium) View.VISIBLE else View.GONE
        textBottomFeedersLabel.text = if (isAquarium) "tank cleaners & critters" else "bottom-feeder critters"
        textBatteryLanternLabel.text = if (isAquarium) "charging lamp LED" else "battery level stone lantern"
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

        styleRadio(btnBiomeOcean, currentConfig.sceneBiome == SceneBiome.OCEAN, "ocean")
        styleRadio(btnBiomePond, currentConfig.sceneBiome == SceneBiome.ZEN_POND, "zen pond")
        styleRadio(btnBiomeAquarium, currentConfig.sceneBiome == SceneBiome.AQUARIUM, "betta tank")

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
        styleRadio(btnPalAmoled, currentConfig.paletteFamily == PaletteFamily.AMOLED, "amoled")

        styleRadio(btnThemeSolar, currentConfig.themeMode == ThemeMode.SOLAR, "solar")
        styleRadio(btnThemeSystem, currentConfig.themeMode == ThemeMode.SYSTEM, "system")
        styleRadio(btnThemeLight, currentConfig.themeMode == ThemeMode.LIGHT, "light")
        styleRadio(btnThemeDark, currentConfig.themeMode == ThemeMode.DARK, "dark")

        styleRadio(btnHuntOff, currentConfig.huntMode == HuntMode.OFF, "off")
        styleRadio(btnHuntHarmless, currentConfig.huntMode == HuntMode.HARMLESS, "harmless")
        styleRadio(btnHuntNatural, currentConfig.huntMode == HuntMode.NATURAL, "natural")
        styleRadio(btnHuntFierce, currentConfig.huntMode == HuntMode.FIERCE, "fierce")

        styleSwitch(
            textBottomFeedersCheck,
            findViewById(R.id.rowBottomFeedersCheck),
            currentConfig.enableBottomFeeders,
            "Bottom-feeder critters"
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
            "3D tilt window parallax"
        )

        styleRadio(btnPerfEco, currentConfig.performanceMode == PerformanceMode.ECO, "eco")
        styleRadio(btnPerfBalanced, currentConfig.performanceMode == PerformanceMode.BALANCED, "balanced")
        styleRadio(btnPerfSmooth, currentConfig.performanceMode == PerformanceMode.SMOOTH, "smooth")

        val fishCells = currentConfig.heroFishCount.coerceIn(2, 9)
        val fishBarStr = "[" + "#".repeat(fishCells) + "-".repeat(9 - fishCells) + "] " + fishCells.toString()
        textFishBar.text = "fish: " + fishBarStr
        textFishBar.contentDescription = "Main fish count: " + fishCells.toString() + " out of 9"

        val wlPct = currentConfig.waterlinePercent.coerceIn(25, 65)
        val wlFilled = (((wlPct - 25) / 40f) * 10f).roundToInt().coerceIn(1, 10)
        val wlBarStr = "[" + "#".repeat(wlFilled) + "-".repeat(10 - wlFilled) + "] " + wlPct.toString() + "%"
        textWaterlineBar.text = (if (isAquarium) "tank: " else "line: ") + wlBarStr
        textWaterlineBar.contentDescription = if (isAquarium) {
            "Tank rim height position: " + wlPct.toString() + " percent from top"
        } else {
            "Waterline split position: " + wlPct.toString() + " percent from top"
        }

        val clarityPct = (currentConfig.waterClarity * 100f).roundToInt()
        val clarityFilled = ((currentConfig.waterClarity * 10f).roundToInt()).coerceIn(1, 10)
        val clarityBarStr = "[" + "#".repeat(clarityFilled) + "-".repeat(10 - clarityFilled) + "] " + clarityPct.toString() + "%"
        textClarityBar.text = (if (isAquarium) "led:  " else "rays: ") + clarityBarStr
        textClarityBar.contentDescription = if (isAquarium) {
            "Overhead LED light rays intensity: " + clarityPct.toString() + " percent"
        } else {
            "Water clarity and sun rays: " + clarityPct.toString() + " percent"
        }

        val isThreeBettas = currentConfig.heroFishCount > 4
        styleRadio(btnBettaCount2, !isThreeBettas, "2 bettas")
        styleRadio(btnBettaCount3, isThreeBettas, "3 bettas")
        styleSwitch(
            textCatVisitorSwitch,
            findViewById(R.id.rowCatVisitorSwitch),
            currentConfig.enableCatVisitor,
            "Occasional curious cat visits"
        )
        styleSwitch(
            textCalmIconsSwitch,
            findViewById(R.id.rowCalmIconsSwitch),
            currentConfig.calmIconMode,
            "Calm home-screen icon scrim"
        )
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

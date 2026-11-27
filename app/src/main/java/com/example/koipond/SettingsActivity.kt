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
    private lateinit var rowPreviewSceneBar: LinearLayout
    private lateinit var btnPreviewOcean: TextView
    private lateinit var btnPreviewPond: TextView
    private lateinit var btnPreviewAquarium: TextView
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
    private lateinit var btnSummonPredator: TextView

    // Card 1: Scene & Quick Lighting Presets
    private lateinit var textSceneDesc: TextView
    private lateinit var btnBiomeOcean: TextView
    private lateinit var btnBiomePond: TextView
    private lateinit var btnBiomeAquarium: TextView
    private lateinit var btnPresetDaylight: TextView
    private lateinit var btnPresetDusk: TextView
    private lateinit var btnPresetOled: TextView

    // Card 2: Scene-Specific Options
    private lateinit var textSceneSettingsTitle: TextView
    private lateinit var groupAquariumOnly: LinearLayout
    private lateinit var groupOutdoorOnly: LinearLayout
    private lateinit var btnBettaCount2: TextView
    private lateinit var btnBettaCount3: TextView
    private lateinit var textCatVisitorSwitch: TextView

    private lateinit var textFishValue: TextView
    private lateinit var btnWeatherAuto: TextView
    private lateinit var btnWeatherClear: TextView
    private lateinit var btnWeatherRain: TextView
    private lateinit var btnWeatherSnow: TextView
    private lateinit var btnWeatherAurora: TextView
    private lateinit var btnHuntOff: TextView
    private lateinit var btnHuntHarmless: TextView
    private lateinit var btnHuntNatural: TextView
    private lateinit var btnHuntFierce: TextView
    private lateinit var textCalmIconsSwitch: TextView

    private lateinit var textBottomFeedersLabel: TextView
    private lateinit var textBottomFeedersSub: TextView
    private lateinit var textBottomFeedersCheck: TextView
    private lateinit var textWaterlineLabel: TextView
    private lateinit var textWaterlineSub: TextView
    private lateinit var textWaterlineValue: TextView
    private lateinit var textClarityLabel: TextView
    private lateinit var btnClarityLow: TextView
    private lateinit var btnClarityMid: TextView
    private lateinit var btnClarityHigh: TextView
    private lateinit var textBatteryLanternLabel: TextView
    private lateinit var textBatteryLanternSub: TextView
    private lateinit var textBatteryLanternSwitch: TextView

    // Card 3: Time & Colors
    private lateinit var btnCycleAuto: TextView
    private lateinit var btnCycleDawn: TextView
    private lateinit var btnCycleNoon: TextView
    private lateinit var btnCycleDusk: TextView
    private lateinit var btnCycleNight: TextView
    private lateinit var btnPalLagoon: TextView
    private lateinit var btnPalEmber: TextView
    private lateinit var btnPalMoss: TextView
    private lateinit var btnPalAmoled: TextView
    private lateinit var btnThemeSolar: TextView
    private lateinit var btnThemeSystem: TextView
    private lateinit var btnThemeLight: TextView
    private lateinit var btnThemeDark: TextView

    // Card 4: Motion & Battery
    private lateinit var btnPerfEco: TextView
    private lateinit var btnPerfBalanced: TextView
    private lateinit var btnPerfSmooth: TextView
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
        refreshUi()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("active_tab", activeTab)
        outState.putBoolean("fullscreen_preview", isFullscreenPreview)
    }

    override fun onResume() {
        super.onResume()
        asciiPondView.startAnimation()
        refreshUi()
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
        rowPreviewSceneBar = findViewById(R.id.rowPreviewSceneBar)
        btnPreviewOcean = findViewById(R.id.btnPreviewOcean)
        btnPreviewPond = findViewById(R.id.btnPreviewPond)
        btnPreviewAquarium = findViewById(R.id.btnPreviewAquarium)
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
        btnSummonPredator = findViewById(R.id.btnSummonPredator)

        textSceneDesc = findViewById(R.id.textSceneDesc)
        btnBiomeOcean = findViewById(R.id.btnBiomeOcean)
        btnBiomePond = findViewById(R.id.btnBiomePond)
        btnBiomeAquarium = findViewById(R.id.btnBiomeAquarium)
        btnPresetDaylight = findViewById(R.id.btnPresetDaylight)
        btnPresetDusk = findViewById(R.id.btnPresetDusk)
        btnPresetOled = findViewById(R.id.btnPresetOled)

        textSceneSettingsTitle = findViewById(R.id.textSceneSettingsTitle)
        groupAquariumOnly = findViewById(R.id.groupAquariumOnly)
        groupOutdoorOnly = findViewById(R.id.groupOutdoorOnly)
        btnBettaCount2 = findViewById(R.id.btnBettaCount2)
        btnBettaCount3 = findViewById(R.id.btnBettaCount3)
        textCatVisitorSwitch = findViewById(R.id.textCatVisitorSwitch)

        textFishValue = findViewById(R.id.textFishValue)
        btnWeatherAuto = findViewById(R.id.btnWeatherAuto)
        btnWeatherClear = findViewById(R.id.btnWeatherClear)
        btnWeatherRain = findViewById(R.id.btnWeatherRain)
        btnWeatherSnow = findViewById(R.id.btnWeatherSnow)
        btnWeatherAurora = findViewById(R.id.btnWeatherAurora)
        btnHuntOff = findViewById(R.id.btnHuntOff)
        btnHuntHarmless = findViewById(R.id.btnHuntHarmless)
        btnHuntNatural = findViewById(R.id.btnHuntNatural)
        btnHuntFierce = findViewById(R.id.btnHuntFierce)
        textCalmIconsSwitch = findViewById(R.id.textCalmIconsSwitch)

        textBottomFeedersLabel = findViewById(R.id.textBottomFeedersLabel)
        textBottomFeedersSub = findViewById(R.id.textBottomFeedersSub)
        textBottomFeedersCheck = findViewById(R.id.textBottomFeedersCheck)
        textWaterlineLabel = findViewById(R.id.textWaterlineLabel)
        textWaterlineSub = findViewById(R.id.textWaterlineSub)
        textWaterlineValue = findViewById(R.id.textWaterlineValue)
        textClarityLabel = findViewById(R.id.textClarityLabel)
        btnClarityLow = findViewById(R.id.btnClarityLow)
        btnClarityMid = findViewById(R.id.btnClarityMid)
        btnClarityHigh = findViewById(R.id.btnClarityHigh)
        textBatteryLanternLabel = findViewById(R.id.textBatteryLanternLabel)
        textBatteryLanternSub = findViewById(R.id.textBatteryLanternSub)
        textBatteryLanternSwitch = findViewById(R.id.textBatteryLanternSwitch)

        btnCycleAuto = findViewById(R.id.btnCycleAuto)
        btnCycleDawn = findViewById(R.id.btnCycleDawn)
        btnCycleNoon = findViewById(R.id.btnCycleNoon)
        btnCycleDusk = findViewById(R.id.btnCycleDusk)
        btnCycleNight = findViewById(R.id.btnCycleNight)
        btnPalLagoon = findViewById(R.id.btnPalLagoon)
        btnPalEmber = findViewById(R.id.btnPalEmber)
        btnPalMoss = findViewById(R.id.btnPalMoss)
        btnPalAmoled = findViewById(R.id.btnPalAmoled)
        btnThemeSolar = findViewById(R.id.btnThemeSolar)
        btnThemeSystem = findViewById(R.id.btnThemeSystem)
        btnThemeLight = findViewById(R.id.btnThemeLight)
        btnThemeDark = findViewById(R.id.btnThemeDark)

        btnPerfEco = findViewById(R.id.btnPerfEco)
        btnPerfBalanced = findViewById(R.id.btnPerfBalanced)
        btnPerfSmooth = findViewById(R.id.btnPerfSmooth)
        textGyroSwitch = findViewById(R.id.textGyroSwitch)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        navPond.setOnClickListener { haptic(it); switchTab(1) }
        navSet.setOnClickListener { haptic(it); switchTab(2) }
        navAbout.setOnClickListener { haptic(it); switchTab(3) }

        asciiPondView.onSpeciesRandomized = { speciesStr, timeLabel ->
            if (currentConfig.sceneBiome != SceneBiome.AQUARIUM) {
                textBiomeStatus.text = "biome ... " + speciesStr
                textBiomeStatus.contentDescription = "Active ocean biome: " + speciesStr
            }
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

        btnSummonPredator.apply {
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
            setOnClickListener { haptic(it); launchWallpaperPicker() }
        }
        findViewById<TextView>(R.id.btnSettingsApplyWallpaper).apply {
            contentDescription = "Apply live wallpaper"
            setOnClickListener { haptic(it); launchWallpaperPicker() }
        }

        // 24-Hour Solar Preview Scrubber
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
        btnSolarLive.setOnClickListener {
            haptic(it)
            asciiPondView.previewHourOverride = null
            updateSolarScrubberUi()
            asciiPondView.invalidate()
        }
        btnFullscreenPreview.setOnClickListener {
            haptic(it)
            setFullscreenPreview(!isFullscreenPreview)
        }

        // Scene selection (both Preview quick bar & Settings Scene card remember per-scene profiles!)
        fun switchScene(target: SceneBiome) {
            val next = repository.switchScenePreservingProfile(currentConfig, target)
            updateConfig(next)
            repository.applyAppNightMode(next.themeMode, next.timeCycleMode)
        }
        btnPreviewOcean.setOnClickListener { haptic(it); switchScene(SceneBiome.OCEAN) }
        btnPreviewPond.setOnClickListener { haptic(it); switchScene(SceneBiome.ZEN_POND) }
        btnPreviewAquarium.setOnClickListener { haptic(it); switchScene(SceneBiome.AQUARIUM) }
        btnBiomeOcean.setOnClickListener { haptic(it); switchScene(SceneBiome.OCEAN) }
        btnBiomePond.setOnClickListener { haptic(it); switchScene(SceneBiome.ZEN_POND) }
        btnBiomeAquarium.setOnClickListener { haptic(it); switchScene(SceneBiome.AQUARIUM) }

        // Card 1: Lighting Presets (cleanly apply to whichever scene is active!)
        fun applyLightingPreset(label: String, preset: WallpaperConfig) {
            previousConfigForUndo = currentConfig
            updateConfig(preset)
            repository.applyAppNightMode(preset.themeMode, preset.timeCycleMode)
            showUndoBanner("ok: applied " + label + " lighting.")
        }
        btnPresetDaylight.setOnClickListener {
            haptic(it)
            val naturalPal = if (currentConfig.sceneBiome == SceneBiome.ZEN_POND) PaletteFamily.MOSS else PaletteFamily.LAGOON
            applyLightingPreset(
                "daylight",
                currentConfig.copy(
                    paletteFamily = naturalPal,
                    themeMode = ThemeMode.SOLAR,
                    timeCycleMode = TimeCycleMode.AUTO_REALTIME,
                    weatherMode = if (currentConfig.sceneBiome == SceneBiome.OCEAN) WeatherMode.AUTO else WeatherMode.CLEAR,
                    waterClarity = 0.85f,
                    calmIconMode = false
                )
            )
        }
        btnPresetDusk.setOnClickListener {
            haptic(it)
            applyLightingPreset(
                "cozy dusk",
                currentConfig.copy(
                    paletteFamily = PaletteFamily.EMBER,
                    themeMode = ThemeMode.DARK,
                    timeCycleMode = TimeCycleMode.DUSK,
                    weatherMode = if (currentConfig.sceneBiome == SceneBiome.OCEAN) WeatherMode.AURORA else WeatherMode.CLEAR,
                    waterClarity = 0.78f,
                    calmIconMode = false
                )
            )
        }
        btnPresetOled.setOnClickListener {
            haptic(it)
            applyLightingPreset(
                "oled black",
                currentConfig.copy(
                    paletteFamily = PaletteFamily.AMOLED,
                    themeMode = ThemeMode.DARK,
                    timeCycleMode = TimeCycleMode.NIGHT,
                    weatherMode = WeatherMode.CLEAR,
                    waterClarity = 0.72f,
                    calmIconMode = true
                )
            )
        }

        // Card 2: Scene-Specific Controls
        btnBettaCount2.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(heroFishCount = 3)) }
        btnBettaCount3.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(heroFishCount = 6)) }
        findViewById<LinearLayout>(R.id.rowCatVisitorSwitch).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(enableCatVisitor = !currentConfig.enableCatVisitor))
        }

        findViewById<TextView>(R.id.btnFishMinus).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(heroFishCount = (currentConfig.heroFishCount - 1).coerceIn(2, 9)))
        }
        findViewById<TextView>(R.id.btnFishPlus).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(heroFishCount = (currentConfig.heroFishCount + 1).coerceIn(2, 9)))
        }

        btnWeatherAuto.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(weatherMode = WeatherMode.AUTO)) }
        btnWeatherClear.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(weatherMode = WeatherMode.CLEAR)) }
        btnWeatherRain.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(weatherMode = WeatherMode.RAIN)) }
        btnWeatherSnow.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(weatherMode = WeatherMode.SNOW)) }
        btnWeatherAurora.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(weatherMode = WeatherMode.AURORA)) }

        btnHuntOff.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(huntMode = HuntMode.OFF)) }
        btnHuntHarmless.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(huntMode = HuntMode.HARMLESS)) }
        btnHuntNatural.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(huntMode = HuntMode.NATURAL)) }
        btnHuntFierce.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(huntMode = HuntMode.FIERCE)) }

        findViewById<LinearLayout>(R.id.rowCalmIconsSwitch).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(calmIconMode = !currentConfig.calmIconMode))
        }
        findViewById<LinearLayout>(R.id.rowBottomFeedersCheck).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(enableBottomFeeders = !currentConfig.enableBottomFeeders))
        }
        findViewById<TextView>(R.id.btnWaterlineMinus).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(waterlinePercent = (currentConfig.waterlinePercent - 4).coerceIn(25, 65)))
        }
        findViewById<TextView>(R.id.btnWaterlinePlus).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(waterlinePercent = (currentConfig.waterlinePercent + 4).coerceIn(25, 65)))
        }

        btnClarityLow.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(waterClarity = 0.50f)) }
        btnClarityMid.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(waterClarity = 0.78f)) }
        btnClarityHigh.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(waterClarity = 0.95f)) }

        findViewById<LinearLayout>(R.id.rowBatteryLanternSwitch).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(enableBatteryLantern = !currentConfig.enableBatteryLantern))
        }

        // Card 3: Time & Colors
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

        btnPalLagoon.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(paletteFamily = PaletteFamily.LAGOON)) }
        btnPalEmber.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(paletteFamily = PaletteFamily.EMBER)) }
        btnPalMoss.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(paletteFamily = PaletteFamily.MOSS)) }
        btnPalAmoled.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(paletteFamily = PaletteFamily.AMOLED)) }

        fun selectThemeMode(mode: ThemeMode) {
            updateConfig(currentConfig.copy(themeMode = mode))
            repository.applyAppNightMode(mode, currentConfig.timeCycleMode)
        }
        btnThemeSolar.setOnClickListener { haptic(it); selectThemeMode(ThemeMode.SOLAR) }
        btnThemeSystem.setOnClickListener { haptic(it); selectThemeMode(ThemeMode.SYSTEM) }
        btnThemeLight.setOnClickListener { haptic(it); selectThemeMode(ThemeMode.LIGHT) }
        btnThemeDark.setOnClickListener { haptic(it); selectThemeMode(ThemeMode.DARK) }

        // Card 4: Motion & Battery
        btnPerfEco.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(performanceMode = PerformanceMode.ECO)) }
        btnPerfBalanced.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(performanceMode = PerformanceMode.BALANCED)) }
        btnPerfSmooth.setOnClickListener { haptic(it); updateConfig(currentConfig.copy(performanceMode = PerformanceMode.SMOOTH)) }
        findViewById<LinearLayout>(R.id.rowGyroSwitch).setOnClickListener {
            haptic(it)
            updateConfig(currentConfig.copy(enableGyro = !currentConfig.enableGyro))
        }

        // Footer Reset + Undo
        findViewById<TextView>(R.id.btnResetDefaults).setOnClickListener {
            haptic(it)
            previousConfigForUndo = currentConfig
            repository.clearAllSceneProfiles()
            val defaults = WallpaperConfig()
            updateConfig(defaults)
            repository.applyAppNightMode(defaults.themeMode, defaults.timeCycleMode)
            showUndoBanner("ok: reset all scenes & settings to defaults.")
        }
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
        rowPreviewSceneBar.visibility = chromeVis
        textPondBoxTitle.visibility = chromeVis
        textBiomeStatus.visibility = chromeVis
        textHuntStatus.visibility = chromeVis
        pondActionBar.visibility = chromeVis
        bottomNavDivider.visibility = chromeVis
        bottomNavBar.visibility = chromeVis

        val padPx = if (isFullscreenPreview) 0 else (12f * resources.displayMetrics.density).roundToInt()
        val boxPadPx = if (isFullscreenPreview) 0 else (4f * resources.displayMetrics.density).roundToInt()
        screenPond.setPadding(padPx, padPx, padPx, padPx)
        pondBoxContainer.setPadding(boxPadPx, boxPadPx, boxPadPx, boxPadPx)
        if (isFullscreenPreview) {
            pondBoxContainer.background = null
        } else {
            pondBoxContainer.setBackgroundResource(R.drawable.bg_card)
        }
        btnFullscreenPreview.text = if (isFullscreenPreview) "exit" else "full"
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
            2 -> "~/minamo/settings"
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
        styleNav(navPond, tab == 1, "Preview tab")
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
            "engine ............. vsync-locked smooth wing & spine physics"
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

    private fun refreshUi() {
        asciiPondView.config = currentConfig
        updateSolarScrubberUi()
        val isAquarium = currentConfig.sceneBiome == SceneBiome.AQUARIUM

        textBiomeStatus.text = if (isAquarium) {
            "aquarium ... bettas · rasboras · glass cat · kuhli · cleaners"
        } else {
            "biome ... " + asciiPondView.simulation.activeSpeciesSummary
        }
        textPondBoxTitle.text = when (currentConfig.sceneBiome) {
            SceneBiome.OCEAN -> "OCEAN WINDOW · tap water to feed"
            SceneBiome.ZEN_POND -> "ZEN POND · tap water to feed"
            SceneBiome.AQUARIUM -> "BETTA TANK · tap water to feed · rim for cat"
        }
        btnSummonPredator.text = if (isAquarium) "cat" else "predator"
        textSceneDesc.text = when (currentConfig.sceneBiome) {
            SceneBiome.OCEAN -> "coral reef, whales, rays & open ocean horizon"
            SceneBiome.ZEN_POND -> "koi pond, blooming lotus pads & shoreline reeds"
            SceneBiome.AQUARIUM -> "planted betta tank, rasboras, glass catfish & curious cat"
        }
        textSceneSettingsTitle.text = when (currentConfig.sceneBiome) {
            SceneBiome.OCEAN -> "OCEAN OPTIONS"
            SceneBiome.ZEN_POND -> "ZEN POND OPTIONS"
            SceneBiome.AQUARIUM -> "BETTA TANK OPTIONS"
        }
        groupAquariumOnly.visibility = if (isAquarium) View.VISIBLE else View.GONE
        groupOutdoorOnly.visibility = if (isAquarium) View.GONE else View.VISIBLE

        // Adapt shared labels cleanly to active scene
        textBottomFeedersLabel.text = if (isAquarium) "tank cleaners & critters" else "bottom critters"
        textBottomFeedersSub.text = if (isAquarium) {
            "otos, pleco, corys, cherry shrimps & snail"
        } else {
            "crabs, cleaner shrimp, sand goby & conch"
        }
        textWaterlineLabel.text = if (isAquarium) "tank rim position" else "horizon height"
        textWaterlineSub.text = if (isAquarium) {
            "leaves room above for your lock-screen clock"
        } else {
            "adjusts sky vs underwater split"
        }
        textClarityLabel.text = if (isAquarium) "overhead LED light rays" else "water clarity & sun rays"
        btnClarityLow.text = if (isAquarium) "soft" else "hazy"
        btnClarityMid.text = if (isAquarium) "bright" else "clear"
        btnClarityHigh.text = if (isAquarium) "vivid" else "crystal"
        textBatteryLanternLabel.text = if (isAquarium) "charging lamp LED" else "battery stone lantern"
        textBatteryLanternSub.text = if (isAquarium) {
            "gentle green pulse on lamp bar while charging"
        } else {
            "underwater lantern glows with battery level"
        }
        updateTelemetryBox()

        val cBg = ContextCompat.getColor(this, R.color.ascii_bg)
        val cFg = ContextCompat.getColor(this, R.color.ascii_fg)
        val cDim = ContextCompat.getColor(this, R.color.ascii_dim)

        fun stylePill(tv: TextView, selected: Boolean, spoken: String) {
            tv.setBackgroundResource(if (selected) R.drawable.bg_pill_selected else R.drawable.bg_pill_unselected)
            tv.setTextColor(if (selected) cBg else cFg)
            tv.contentDescription = spoken + ", " + (if (selected) "selected" else "not selected")
        }

        fun styleToggle(tv: TextView, row: View, enabled: Boolean, spoken: String) {
            tv.text = if (enabled) "ON" else "OFF"
            tv.setBackgroundResource(if (enabled) R.drawable.bg_pill_selected else R.drawable.bg_pill_unselected)
            tv.setTextColor(if (enabled) cBg else cDim)
            row.contentDescription = spoken + ", " + (if (enabled) "on" else "off")
        }

        // Card 1 & Preview Scene Bar: Scene + Highlight active lighting preset if matched
        stylePill(btnPreviewOcean, currentConfig.sceneBiome == SceneBiome.OCEAN, "Ocean scene")
        stylePill(btnPreviewPond, currentConfig.sceneBiome == SceneBiome.ZEN_POND, "Zen Pond scene")
        stylePill(btnPreviewAquarium, currentConfig.sceneBiome == SceneBiome.AQUARIUM, "Betta Tank scene")
        stylePill(btnBiomeOcean, currentConfig.sceneBiome == SceneBiome.OCEAN, "Ocean scene")
        stylePill(btnBiomePond, currentConfig.sceneBiome == SceneBiome.ZEN_POND, "Zen Pond scene")
        stylePill(btnBiomeAquarium, currentConfig.sceneBiome == SceneBiome.AQUARIUM, "Betta Tank scene")

        val isOledPreset = currentConfig.paletteFamily == PaletteFamily.AMOLED
        val isDuskPreset = !isOledPreset && currentConfig.paletteFamily == PaletteFamily.EMBER && currentConfig.timeCycleMode == TimeCycleMode.DUSK
        val isDaylightPreset = !isOledPreset && !isDuskPreset && currentConfig.timeCycleMode == TimeCycleMode.AUTO_REALTIME && currentConfig.themeMode == ThemeMode.SOLAR
        stylePill(btnPresetDaylight, isDaylightPreset, "Daylight preset")
        stylePill(btnPresetDusk, isDuskPreset, "Cozy Dusk preset")
        stylePill(btnPresetOled, isOledPreset, "OLED Black preset")

        // Card 2: Scene Options
        val isThreeBettas = currentConfig.heroFishCount > 4
        stylePill(btnBettaCount2, !isThreeBettas, "2 bettas")
        stylePill(btnBettaCount3, isThreeBettas, "3 bettas")
        styleToggle(textCatVisitorSwitch, findViewById(R.id.rowCatVisitorSwitch), currentConfig.enableCatVisitor, "Curious pet cat")

        val fishCount = currentConfig.heroFishCount.coerceIn(2, 9)
        textFishValue.text = fishCount.toString()
        stylePill(btnWeatherAuto, currentConfig.weatherMode == WeatherMode.AUTO, "Auto weather")
        stylePill(btnWeatherClear, currentConfig.weatherMode == WeatherMode.CLEAR, "Clear sky")
        stylePill(btnWeatherRain, currentConfig.weatherMode == WeatherMode.RAIN, "Rain weather")
        stylePill(btnWeatherSnow, currentConfig.weatherMode == WeatherMode.SNOW, "Snow weather")
        stylePill(btnWeatherAurora, currentConfig.weatherMode == WeatherMode.AURORA, "Aurora sky")

        stylePill(btnHuntOff, currentConfig.huntMode == HuntMode.OFF, "Predators off")
        stylePill(btnHuntHarmless, currentConfig.huntMode == HuntMode.HARMLESS, "Gentle predators")
        stylePill(btnHuntNatural, currentConfig.huntMode == HuntMode.NATURAL, "Natural predators")
        stylePill(btnHuntFierce, currentConfig.huntMode == HuntMode.FIERCE, "Fierce predators")
        styleToggle(textCalmIconsSwitch, findViewById(R.id.rowCalmIconsSwitch), currentConfig.calmIconMode, "Calm icon backdrop")

        styleToggle(textBottomFeedersCheck, findViewById(R.id.rowBottomFeedersCheck), currentConfig.enableBottomFeeders, "Bottom critters")
        textWaterlineValue.text = currentConfig.waterlinePercent.coerceIn(25, 65).toString() + "%"

        val clarity = currentConfig.waterClarity
        stylePill(btnClarityLow, clarity < 0.64f, "Soft light rays")
        stylePill(btnClarityMid, clarity in 0.64f..0.88f, "Clear light rays")
        stylePill(btnClarityHigh, clarity > 0.88f, "Vivid light rays")
        styleToggle(textBatteryLanternSwitch, findViewById(R.id.rowBatteryLanternSwitch), currentConfig.enableBatteryLantern, "Battery indicator")

        // Card 3: Time & Colors
        stylePill(btnCycleAuto, currentConfig.timeCycleMode == TimeCycleMode.AUTO_REALTIME, "Live clock time")
        stylePill(btnCycleDawn, currentConfig.timeCycleMode == TimeCycleMode.DAWN, "Dawn time")
        stylePill(btnCycleNoon, currentConfig.timeCycleMode == TimeCycleMode.NOON, "Noon time")
        stylePill(btnCycleDusk, currentConfig.timeCycleMode == TimeCycleMode.DUSK, "Dusk time")
        stylePill(btnCycleNight, currentConfig.timeCycleMode == TimeCycleMode.NIGHT, "Night time")

        stylePill(btnPalLagoon, currentConfig.paletteFamily == PaletteFamily.LAGOON, "Lagoon theme")
        stylePill(btnPalEmber, currentConfig.paletteFamily == PaletteFamily.EMBER, "Ember theme")
        stylePill(btnPalMoss, currentConfig.paletteFamily == PaletteFamily.MOSS, "Moss theme")
        stylePill(btnPalAmoled, currentConfig.paletteFamily == PaletteFamily.AMOLED, "AMOLED black theme")

        stylePill(btnThemeSolar, currentConfig.themeMode == ThemeMode.SOLAR, "Auto sun polarity")
        stylePill(btnThemeSystem, currentConfig.themeMode == ThemeMode.SYSTEM, "System theme polarity")
        stylePill(btnThemeLight, currentConfig.themeMode == ThemeMode.LIGHT, "Light theme polarity")
        stylePill(btnThemeDark, currentConfig.themeMode == ThemeMode.DARK, "Dark theme polarity")

        // Card 4: Motion & Battery
        stylePill(btnPerfEco, currentConfig.performanceMode == PerformanceMode.ECO, "Eco 20fps")
        stylePill(btnPerfBalanced, currentConfig.performanceMode == PerformanceMode.BALANCED, "Balanced frame rate")
        stylePill(btnPerfSmooth, currentConfig.performanceMode == PerformanceMode.SMOOTH, "Smooth 60fps")
        styleToggle(textGyroSwitch, findViewById(R.id.rowGyroSwitch), currentConfig.enableGyro, "3D tilt parallax")
    }

    private fun updateConfig(newConfig: WallpaperConfig) {
        currentConfig = newConfig
        repository.saveConfig(newConfig)
        refreshUi()
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

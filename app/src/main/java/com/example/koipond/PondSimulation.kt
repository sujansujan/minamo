package com.example.koipond

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * 15 Distinct Ocean Species (3 schooling baitfish + 12 reef & pelagic swimmers).
 * Each species defines real screen-width proportions (`lengthFrac`, `aspectRatio`) and biological
 * anatomy (`bodyPlan`, `marking`) so fish scale naturally across any screen size or grid density.
 */
enum class AsciiFishSpecies(
    val id: Int,
    val displayName: String,
    val isHeroMultiLine: Boolean,
    val primaryRole: Int,
    val finAccentRole: Int,
    val baseSpeedColsPerSec: Float,
    val turnAgilityRadPerSec: Float,
    val lengthFrac: Float,
    val aspectRatio: Float,
    val bodyPlan: BodyPlan,
    val marking: MarkingStyle
) {
    // --- SCHOOLING BAITFISH (delicate shimmering darts ~3.6-3.8% screen width) ---
    BLUE_CHROMIS(0, "blue chromis", false, 1, 2, 7.4f, 2.6f, 0.036f, 0.36f, BodyPlan.BAITFISH, MarkingStyle.NONE),
    SILVER_SARDINE(1, "silver sardine", false, 3, 1, 8.0f, 2.8f, 0.038f, 0.28f, BodyPlan.BAITFISH, MarkingStyle.NONE),
    SUNSET_ANTHIAS(2, "sunset anthias", false, 0, 2, 7.0f, 2.5f, 0.037f, 0.34f, BodyPlan.BAITFISH, MarkingStyle.NONE),

    // --- REEF & PELAGIC OCEAN LIFE (refined spacious mobile-window proportions) ---
    CLOWNFISH(3, "clownfish", true, 0, 3, 4.5f, 1.9f, 0.058f, 0.44f, BodyPlan.REEF_OVAL, MarkingStyle.CLOWN_BANDS),
    BLUE_TANG(4, "regal tang", true, 1, 2, 5.4f, 1.8f, 0.074f, 0.48f, BodyPlan.REEF_OVAL, MarkingStyle.TANG_MASK),
    MOORISH_IDOL(5, "moorish idol", true, 2, 3, 4.3f, 1.7f, 0.072f, 0.72f, BodyPlan.REEF_TALL, MarkingStyle.IDOL_BANDS),
    YELLOWFIN_TUNA(6, "yellowfin tuna", true, 1, 2, 7.2f, 1.35f, 0.112f, 0.32f, BodyPlan.PELAGIC_FAST, MarkingStyle.NONE),
    PUFFERFISH(7, "spotted puffer", true, 2, 0, 3.2f, 2.0f, 0.060f, 0.62f, BodyPlan.PUFFER, MarkingStyle.SPOTS),
    EMPEROR_ANGEL(8, "emperor angel", true, 4, 2, 4.2f, 1.6f, 0.080f, 0.62f, BodyPlan.REEF_TALL, MarkingStyle.ANGEL_STRIPES),
    LIONFISH(9, "reef lionfish", true, 0, 4, 3.4f, 1.5f, 0.078f, 0.56f, BodyPlan.LIONFISH, MarkingStyle.CLOWN_BANDS),
    SEA_TURTLE(10, "green sea turtle", true, 1, 2, 3.5f, 1.0f, 0.118f, 0.46f, BodyPlan.TURTLE, MarkingStyle.TURTLE_SCUTES),
    EAGLE_RAY(11, "spotted eagle ray", true, 3, 1, 4.8f, 1.15f, 0.130f, 0.48f, BodyPlan.RAY, MarkingStyle.SPOTS),
    MANDARIN(12, "mandarin dragonet", true, 4, 1, 4.1f, 1.8f, 0.056f, 0.42f, BodyPlan.REEF_OVAL, MarkingStyle.MANDARIN_SWIRL),
    KOHAKU_KOI(13, "kohaku koi", true, 3, 0, 4.2f, 1.45f, 0.088f, 0.36f, BodyPlan.REEF_OVAL, MarkingStyle.KOI_PATCHES),
    MAHI_MAHI(14, "golden dorado", true, 2, 1, 6.2f, 1.5f, 0.105f, 0.36f, BodyPlan.PELAGIC_FAST, MarkingStyle.TANG_MASK);

    companion object {
        val heroPool = arrayOf(
            CLOWNFISH, BLUE_TANG, MOORISH_IDOL, YELLOWFIN_TUNA, PUFFERFISH,
            EMPEROR_ANGEL, LIONFISH, SEA_TURTLE, EAGLE_RAY, MANDARIN, KOHAKU_KOI, MAHI_MAHI
        )
        val schoolPool = arrayOf(BLUE_CHROMIS, SILVER_SARDINE, SUNSET_ANTHIAS)
    }
}

/**
 * Majestic Periodic Ocean Visitors that glide past the window every once in a while:
 * Humpback Whales (with surface water spout!), Orcas, Breaching Bottlenose Dolphin Pods,
 * Spotted Whale Sharks, Giant Oceanic Mantas, and Nocturnal Giant Squid!
 */
enum class OceanVisitorSpecies(
    val id: Int,
    val displayName: String,
    val speedColsPerSec: Float,
    val bodyRole: Int,
    val accentRole: Int,
    val preferredZMin: Float,
    val preferredZMax: Float,
    val canBreach: Boolean,
    val canSpout: Boolean,
    val podSize: Int,
    val lengthFrac: Float,
    val aspectRatio: Float,
    val bodyPlan: BodyPlan,
    val marking: MarkingStyle
) {
    HUMPBACK_WHALE(0, "humpback whale", 3.4f, 1, 3, 0.34f, 0.58f, false, true, 1, 0.460f, 0.26f, BodyPlan.WHALE, MarkingStyle.NONE),
    ORCA(1, "orca whale", 5.2f, 1, 3, 0.24f, 0.52f, false, true, 1, 0.330f, 0.28f, BodyPlan.ORCA, MarkingStyle.NONE),
    DOLPHIN_POD(2, "bottlenose dolphin pod", 9.6f, 1, 3, 0.16f, 0.38f, true, false, 3, 0.195f, 0.30f, BodyPlan.DOLPHIN, MarkingStyle.NONE),
    WHALE_SHARK(3, "whale shark", 3.8f, 1, 2, 0.30f, 0.56f, false, false, 1, 0.380f, 0.26f, BodyPlan.SHARK, MarkingStyle.SPOTS),
    GIANT_MANTA(4, "giant oceanic manta", 4.5f, 3, 1, 0.22f, 0.54f, false, false, 2, 0.265f, 0.48f, BodyPlan.RAY, MarkingStyle.NONE),
    DEEP_SQUID(5, "glowing giant squid", 5.0f, 4, 1, 0.28f, 0.58f, false, false, 1, 0.205f, 0.28f, BodyPlan.SQUID, MarkingStyle.NONE)
}

class OceanVisitorMember(
    var colX: Float = -35f,
    var rowY: Float = 50f,
    var zDepth: Float = 0.6f,
    var vx: Float = 3f,
    var vy: Float = 0f,
    var vz: Float = 0f,
    var pitchDeg: Float = 0f,
    var animPhase: Float = 0f,
    var breachPhase: Float = 0f,     // >0 during parabolic leap out of the ocean!
    var hasBreached: Boolean = false,
    var breachTriggerCol: Float = 30f,
    var wasAboveWater: Boolean = false
)

class OceanVisitorEncounter(
    var active: Boolean = false,
    var species: OceanVisitorSpecies = OceanVisitorSpecies.HUMPBACK_WHALE,
    var vxDir: Float = 1f,
    var memberCount: Int = 1,
    var cooldownTimer: Float = 2.5f, // First ocean giant appears promptly after viewing!
    var spoutTimer: Float = 0f,
    var spoutCol: Float = 0f,
    val members: Array<OceanVisitorMember> = Array(3) { OceanVisitorMember() }
)

enum class PredatorSpecies(
    val id: Int,
    val displayName: String,
    val speedColsPerSec: Float,
    val lengthFrac: Float,
    val aspectRatio: Float,
    val bodyPlan: BodyPlan,
    val marking: MarkingStyle
) {
    BARRACUDA_PIKE(0, "barracuda", 11.2f, 0.140f, 0.22f, BodyPlan.PELAGIC_FAST, MarkingStyle.NONE),
    REEF_SHARK(1, "reef shark", 10.6f, 0.165f, 0.26f, BodyPlan.SHARK, MarkingStyle.NONE),
    SWORDFISH(2, "marlin swordfish", 12.0f, 0.160f, 0.24f, BodyPlan.SWORDFISH, MarkingStyle.NONE),
    ELECTRIC_MORAY(3, "moray eel", 9.8f, 0.152f, 0.18f, BodyPlan.EEL, MarkingStyle.SPOTS)
}

enum class BottomFeederSpecies(
    val id: Int,
    val displayName: String,
    val speedColsPerSec: Float,
    val colorRole: Int,
    val accentRole: Int,
    val lengthFrac: Float,
    val aspectRatio: Float,
    val bodyPlan: BodyPlan,
    val marking: MarkingStyle
) {
    RIVER_CRAB(0, "reef crab", 2.0f, 0, 2, 0.058f, 0.62f, BodyPlan.CRAB, MarkingStyle.NONE),
    CHERRY_SHRIMP(1, "cleaner shrimp", 2.8f, 4, 3, 0.054f, 0.44f, BodyPlan.SHRIMP, MarkingStyle.NONE),
    CORY_CATFISH(2, "sand goby", 2.4f, 2, 1, 0.064f, 0.38f, BodyPlan.GOBY, MarkingStyle.SPOTS),
    APPLE_SNAIL(3, "sea conch", 0.8f, 2, 0, 0.046f, 0.56f, BodyPlan.CONCH, MarkingStyle.NONE)
}

class AsciiFishAgent(
    val id: Int,
    var depthLayer: Int,
    var zDepth: Float = 0.45f,
    var yaw: Float = 0f,           // Continuous 3D heading: 0 = right, PI = left, +PI/2 = deeper, -PI/2 = closer
    var targetYaw: Float = 0f,
    var pitchDeg: Float = 0f,      // Smooth vertical nose banking (-11..+11 deg)
    var vz: Float = 0.0f,
    var isSchooler: Boolean,
    var species: AsciiFishSpecies,
    var colFrac: Float,
    var preferredDepthFrac: Float,
    var colX: Float,
    var rowY: Float,
    var vx: Float,
    var vy: Float,
    var facingDir: Int,
    var speedScale: Float,
    var animPhase: Float,
    var isEvading: Boolean = false,
    var respawnTimer: Float = 0f,
    var offscreenTimer: Float = 0f, // Natural off-window cruise pause before re-entering ocean window
    var pelletsEaten: Int = 0,
    var isElderCrown: Boolean = false,
    var burstPhase: Float = 0f,
    var hoverTimer: Float = 0f,
    var steerTimer: Float = 4.0f,
    var wanderTimer: Float = 3.5f,
    var targetDepthFrac: Float = 0.5f,
    var gulpTimer: Float = 0f,
    var startleTimer: Float = 0f
)

enum class LegendarySpecies(
    val id: Int,
    val displayName: String,
    val speedColsPerSec: Float,
    val lengthFrac: Float,
    val aspectRatio: Float,
    val bodyPlan: BodyPlan,
    val marking: MarkingStyle
) {
    DRAGON_KOI(0, "ryujin sea dragon", 3.8f, 0.162f, 0.34f, BodyPlan.REEF_OVAL, MarkingStyle.KOI_PATCHES),
    GIANT_OARFISH(1, "regal oarfish", 4.2f, 0.205f, 0.16f, BodyPlan.EEL, MarkingStyle.NONE),
    ANCIENT_COELACANTH(2, "living coelacanth", 3.2f, 0.148f, 0.38f, BodyPlan.SHARK, MarkingStyle.SPOTS)
}

class LegendaryAgent(
    var active: Boolean = false,
    var species: LegendarySpecies = LegendarySpecies.DRAGON_KOI,
    var colX: Float = 20f,
    var rowY: Float = 45f,
    var zDepth: Float = 0.22f,
    var vxDir: Float = 1f,
    var animPhase: Float = 0f
)

class PredatorAgent(
    var active: Boolean = false,
    var species: PredatorSpecies = PredatorSpecies.BARRACUDA_PIKE,
    var ambushStyle: Int = 0,
    var colX: Float = -16f,
    var rowY: Float = 25f,
    var zDepth: Float = 0.25f,
    var vx: Float = 10f,
    var vy: Float = 0f,
    var vz: Float = 0f,
    var vxDir: Float = 1f,
    var pitchDeg: Float = 0f,
    var animPhase: Float = 0f,
    var cooldownTimer: Float = 20f,
    var activeDuration: Float = 0f,
    var hasCaughtThisVisit: Boolean = false,
    var willCatchThisVisit: Boolean = true,
    var chompTimer: Float = 0f,
    var stalkTimer: Float = 0f,
    var isRetreating: Boolean = false
)

class KelpStalk(
    var colFrac: Float,
    var heightRows: Int,
    var zDepth: Float,
    var phase: Float,
    var bendOffset: Float = 0f,
    var bendVel: Float = 0f
)

class BloodParticle(
    var active: Boolean = false,
    var isScale: Boolean = false,
    var colX: Float = 0f,
    var rowY: Float = 0f,
    var zDepth: Float = 0.25f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var vz: Float = 0f,
    var age: Float = 0f,
    var maxAge: Float = 2.4f
)

class BottomFeederAgent(
    val id: Int,
    var species: BottomFeederSpecies,
    var colX: Float,
    var rowY: Float,
    var zDepth: Float = 0.35f,
    var preferredTier: Int,
    var zRoamTimer: Float = 0f,
    var zRoamTargetTier: Float = 0f,
    var vx: Float,
    var vy: Float,
    var vxDir: Float,
    var animPhase: Float,
    var pauseTimer: Float = 0f,
    var isSnappingAtPredator: Boolean = false
)

class NightJellyfish(
    var colX: Float,
    var rowY: Float,
    var zDepth: Float = 0.45f,
    var phase: Float
)

class SurfaceInsect(
    var colX: Float = 20f,
    var rowY: Float = 10f,
    var vx: Float = 3.5f,
    var phase: Float = 0f
)

class ShorelineAmphibian(
    var isTurtle: Boolean = false,
    var diveHideTimer: Float = 0f,
    var tongueTimer: Float = 0f,
    var blinkPhase: Float = 0f
)

class SurfaceDrifter(
    var isPaperBoat: Boolean = true,
    var colX: Float = 10f,
    var vx: Float = 1.4f
)

class ShootingStar(
    var active: Boolean = false,
    var colX: Float = 0f,
    var rowY: Float = 0f,
    var vx: Float = 26f,
    var vy: Float = 7f,
    var timer: Float = 12f
)

class SkyWeatherDrop(
    var colX: Float = 0f,
    var rowY: Float = 0f,
    var speedY: Float = 18f,
    var speedX: Float = -3f
)

class AsciiBubble(
    var colX: Float = 0f,
    var rowY: Float = 0f,
    var zDepth: Float = 0.5f,
    var speedY: Float = 3.2f,
    var isBubble: Boolean = true,
    var phase: Float = 0f
)

class FoodPellet(
    var colX: Float = 0f,
    var rowY: Float = 0f,
    var zDepth: Float = 0.30f,
    var isHeavyAlgaeWafer: Boolean = false,
    var restTimer: Float = 0f,
    var active: Boolean = false
)

class Seabird(
    var colX: Float,
    var rowFrac: Float,
    var vx: Float,
    var flapPhase: Float
)

class BioPlankton(
    var active: Boolean = false,
    var colX: Float = 0f,
    var rowY: Float = 0f,
    var zDepth: Float = 0.35f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var age: Float = 0f,
    var maxAge: Float = 1.3f
)

class RainSplash(
    var active: Boolean = false,
    var colX: Float = 0f,
    var age: Float = 0f,
    var maxAge: Float = 0.42f
)

class PondSimulation {

    val maxHeroFish = 9
    val maxSchoolFish = 7
    val totalMaxFish = maxHeroFish + maxSchoolFish

    val fish = Array(totalMaxFish) { idx ->
        val dFrac = 0.10f + ((idx + 0.5f) / totalMaxFish.toFloat()) * 0.78f
        val isSchool = idx >= maxHeroFish
        val dir = if (idx % 2 == 0) 1 else -1
        val initYaw = if (dir > 0) 0f else PI.toFloat()
        AsciiFishAgent(
            id = idx,
            depthLayer = if (isSchool) 0 else if (idx % 2 == 0) 2 else 1,
            zDepth = 0.18f + (idx / totalMaxFish.toFloat()) * 0.68f,
            yaw = initYaw,
            targetYaw = initYaw,
            isSchooler = isSchool,
            species = if (isSchool) {
                AsciiFishSpecies.schoolPool[idx % AsciiFishSpecies.schoolPool.size]
            } else {
                AsciiFishSpecies.heroPool[idx % AsciiFishSpecies.heroPool.size]
            },
            colFrac = ((idx * 37 + 13) % 86 + 7) / 100f,
            preferredDepthFrac = dFrac,
            colX = 12f + idx * 4f,
            rowY = 24f + idx * 5f,
            vx = dir * 4.5f,
            vy = 0f,
            facingDir = dir,
            speedScale = 1.0f,
            animPhase = idx * 1.3f,
            targetDepthFrac = dFrac
        )
    }

    val oceanVisitor = OceanVisitorEncounter()
    val legendary = LegendaryAgent()
    val predator = PredatorAgent()
    val bloodParticles = Array(18) { BloodParticle() }
    var lastBloodSiteCol = -1f
    var lastBloodSiteTimer = 0f

    val maxBottomFeeders = 5
    val bottomFeeders = Array(maxBottomFeeders) { idx ->
        val dir = if (idx % 2 == 0) 1f else -1f
        BottomFeederAgent(
            id = idx,
            species = BottomFeederSpecies.entries[idx % BottomFeederSpecies.entries.size],
            colX = 6f + idx * 14f,
            rowY = 150f - idx,
            preferredTier = idx,
            vx = dir * 2.0f,
            vy = 0f,
            vxDir = dir,
            animPhase = idx * 1.7f
        )
    }

    val nightJellies = arrayOf(
        NightJellyfish(18f, 45f, 0.32f, 0.0f),
        NightJellyfish(58f, 68f, 0.72f, 2.4f)
    )

    val surfaceInsect = SurfaceInsect()
    val shoreAmphibian = ShorelineAmphibian()
    val surfaceDrifter = SurfaceDrifter()
    val shootingStar = ShootingStar()

    var landmarkColFrac = 0.48f

    val weatherDrops = Array(28) { idx ->
        SkyWeatherDrop(
            colX = (idx * 13 % 80).toFloat(),
            rowY = (idx * 7 % 40).toFloat(),
            speedY = 22f + (idx % 4) * 3f,
            speedX = -4.5f
        )
    }

    val bubbles = Array(28) { idx ->
        val z = ((idx * 19) % 100) / 100f
        AsciiBubble(
            colX = (idx * 11 % 75).toFloat(),
            rowY = (20 + (idx * 7 % 60)).toFloat(),
            zDepth = z,
            speedY = if (idx < 12) (4.2f - z * 2.0f) else (0.55f - z * 0.25f),
            isBubble = idx < 12,
            phase = idx * 0.9f
        )
    }

    val pellets = Array(8) { FoodPellet() }
    private var nextPelletIdx = 0

    var simTime = 0f
        private set
    private var unlockSeedCounter = 1L
    private var speciesRotationCounter = 1

    var gridCols = 82
        private set
    var gridRows = 164
        private set
    var waterlineRow = 58
        private set
    private var currentWaterlinePct = 36

    private val maxCols = 112
    private val maxRows = 240
    private val charGrid = CharArray(maxCols * maxRows) { ' ' }
    private val colorGrid = IntArray(maxCols * maxRows)
    private val rowCharScratch = CharArray(maxCols)
    private val singleCharBuf = CharArray(1)

    // 1D Interactive Waterline Wave Heightfield
    private val waveHeight = FloatArray(maxCols)
    private val waveVel = FloatArray(maxCols)
    private val waveNext = FloatArray(maxCols)

    // Reactive swaying underwater kelp stalks
    private val kelpStalks = arrayOf(
        KelpStalk(0.09f, 11, 0.24f, 0.2f),
        KelpStalk(0.24f, 8, 0.62f, 1.5f),
        KelpStalk(0.41f, 9, 0.78f, 2.7f),
        KelpStalk(0.73f, 10, 0.56f, 3.9f),
        KelpStalk(0.90f, 12, 0.20f, 5.1f)
    )

    // School Boids wandering target (flows smoothly across the ocean window)
    private var schoolTargetX = 40f
    private var schoolTargetY = 75f
    private var schoolTargetZ = 0.58f
    private var schoolFlowDir = 1f
    private var schoolTargetTimer = 4f

    // Cached sky & underwater LinearGradients (2 drawRects total instead of ~100 per-row drawRects/frame!)
    private val skyGradientPaint = Paint()
    private var cachedSkySplitRow = -1
    private var cachedSkyTopColor = 0
    private var cachedSkyHorizonColor = 0

    private val waterGradientPaint = Paint()
    private var cachedGradSplitRow = -1
    private var cachedGradHeight = -1
    private var cachedGradTopColor = 0
    private var cachedGradDeepColor = 0
    private var cachedGradMeniscusColor = 0

    // Ocean sky seabirds, distant horizon sailboat & night bioluminescent plankton wake
    private val seabirds = arrayOf(
        Seabird(10f, 0.24f, 4.2f, 0.0f),
        Seabird(44f, 0.36f, -3.4f, 2.1f),
        Seabird(68f, 0.18f, 3.1f, 4.3f)
    )
    private var distantBoatCol = 18f
    private var clamOpenPhase = 0f
    private var clamSnapTimer = 0f
    private var lighthouseFlashTimer = 0f
    private var lanternPulseTimer = 0f
    private var chargingBubbleCooldown = 0f
    private var lastIsOceanBiome = true
    private var lastSceneBiome = SceneBiome.OCEAN
    private val bioPlankton = Array(28) { BioPlankton() }
    private var nextPlanktonIdx = 0

    @Volatile
    var lastResolvedSnapshot: TimeOfDayPalette.TimeSnapshot? = null
        private set
    @Volatile
    var hasHighMotionActivity: Boolean = false
        private set
    private var nextBubbleIdx = 0

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.MONOSPACE
        fontFeatureSettings = "liga 0, calt 0"
        textAlign = Paint.Align.LEFT
        isSubpixelText = true
    }
    private val bgPaint = Paint()
    private val creatureRenderer = MarineCreatureRenderer()
    private val sceneRenderer = OceanSceneRenderer()
    private val aquariumEngine = AquariumBiotopeEngine()

    fun summonAquariumCatNow(): String = aquariumEngine.summonCatVisitNow()

    // Unified Zero-Allocation Depth-Sorted Draw Queue (Painter's Algorithm across ALL creatures & 3D flora!)
    // Kind: 0=Fish, 1=OceanVisitorMember, 2=BottomFeeder, 3=NightJellyfish, 4=Legendary, 5=Predator, 6=Kelp, 7=CoralOutcrop
    private val maxDrawables = 64
    private val drawKind = IntArray(maxDrawables)
    private val drawIdx = IntArray(maxDrawables)
    private val drawZ = FloatArray(maxDrawables)
    private val rainSplashes = Array(10) { RainSplash() }
    private var nextSplashIdx = 0

    private var viewportWidthPx = 1080f
    private var viewportHeightPx = 2200f
    private var cellWidthPx = 13f
    private var cellHeightPx = 22f
    private var baselineOffsetPx = 17f

    private fun refSpanPx(): Float = minOf(viewportWidthPx, viewportHeightPx * 0.68f).coerceAtLeast(240f)

    private fun creatureLengthPx(lengthFrac: Float, zDepth: Float): Float {
        // Refined mobile-window 3D perspective scale (1.06x near glass -> 0.42x deep haze, crisp 2.5x depth zoom!)
        val persp = 1.06f - zDepth.coerceIn(0f, 1f) * 0.64f
        return refSpanPx() * lengthFrac * persp
    }

    private fun spanCols(lengthFrac: Float, zDepth: Float = 0.38f): Float =
        (creatureLengthPx(lengthFrac, zDepth) / cellWidthPx.coerceAtLeast(1f)).coerceAtLeast(2.5f)

    private fun spanRows(lengthFrac: Float, aspectRatio: Float, zDepth: Float = 0.38f): Float =
        (creatureLengthPx(lengthFrac, zDepth) * aspectRatio / cellHeightPx.coerceAtLeast(1f)).coerceAtLeast(1.2f)

    var activeSpeciesSummary: String = "sea turtle, moorish idol, eagle ray, blue chromis"
        private set
    var onHuntOutcomeListener: ((Boolean, String, String) -> Unit)? = null
    var onCrownLevelUpListener: ((String) -> Unit)? = null
    var onLegendarySightingListener: ((String) -> Unit)? = null

    init {
        randomizeEcosystemOnUnlock(System.nanoTime())
    }

    private fun wrapAngle(rad: Float): Float {
        var a = rad
        val twoPi = (2.0 * PI).toFloat()
        val piF = PI.toFloat()
        while (a > piF) a -= twoPi
        while (a < -piF) a += twoPi
        return a
    }

    private fun injectSurfaceRipple(colFloat: Float, impulse: Float) {
        val center = colFloat.roundToInt().coerceIn(1, (gridCols - 2).coerceAtLeast(1))
        waveVel[center] = (waveVel[center] + impulse).coerceIn(-6.5f, 6.5f)
        if (center - 1 >= 0) waveVel[center - 1] = (waveVel[center - 1] + impulse * 0.55f).coerceIn(-6.5f, 6.5f)
        if (center + 1 < gridCols) waveVel[center + 1] = (waveVel[center + 1] + impulse * 0.55f).coerceIn(-6.5f, 6.5f)
    }

    private fun emitMouthBubble(colX: Float, rowY: Float, zDepth: Float) {
        val idx = nextBubbleIdx % 12
        nextBubbleIdx = (nextBubbleIdx + 1) % 12
        val b = bubbles[idx]
        b.colX = colX
        b.rowY = rowY
        b.zDepth = zDepth.coerceIn(0.08f, 0.90f)
        b.speedY = 4.4f - b.zDepth * 1.8f
        b.isBubble = true
    }

    private fun spawnBioPlankton(colX: Float, rowY: Float, zDepth: Float, vx: Float = 0f, vy: Float = -0.5f) {
        val bp = bioPlankton[nextPlanktonIdx]
        nextPlanktonIdx = (nextPlanktonIdx + 1) % bioPlankton.size
        bp.active = true
        bp.colX = colX
        bp.rowY = rowY
        bp.zDepth = zDepth.coerceIn(0.10f, 0.88f)
        bp.vx = vx
        bp.vy = vy
        bp.age = 0f
        bp.maxAge = 1.0f + (nextPlanktonIdx % 5) * 0.18f
    }

    private fun drawCharZeroAlloc(canvas: Canvas, ch: Char, x: Float, y: Float, paint: Paint) {
        singleCharBuf[0] = ch
        canvas.drawText(singleCharBuf, 0, 1, x, y, paint)
    }

    fun loadBundledFont(context: Context) {
        try {
            val tf = ResourcesCompat.getFont(context, R.font.jetbrains_mono_regular)
            if (tf != null) {
                textPaint.typeface = tf
            }
        } catch (_: Throwable) {}
    }

    fun configureGrid(widthPx: Int, heightPx: Int, density: GridDensity, waterlinePct: Int = currentWaterlinePct) {
        if (widthPx <= 0 || heightPx <= 0) return
        viewportWidthPx = widthPx.toFloat()
        viewportHeightPx = heightPx.toFloat()
        currentWaterlinePct = waterlinePct.coerceIn(25, 65)
        val cols = density.targetCols.coerceIn(50, maxCols - 2)
        cellWidthPx = widthPx.toFloat() / cols.toFloat()

        textPaint.textSize = cellWidthPx * 1.66f
        val actualAdvance = textPaint.measureText("M").coerceAtLeast(1f)
        textPaint.textSize = textPaint.textSize * (cellWidthPx / actualAdvance)

        val fm = textPaint.fontMetrics
        cellHeightPx = (fm.descent - fm.ascent + fm.leading).coerceAtLeast(cellWidthPx * 1.42f)
        baselineOffsetPx = -fm.ascent

        gridCols = cols
        gridRows = (heightPx / cellHeightPx).toInt().coerceIn(60, maxRows - 2)
        waterlineRow = (gridRows * (currentWaterlinePct / 100f)).roundToInt().coerceIn(14, gridRows - 26)

        val minR = waterlineRow + 5
        val maxR = gridRows - 8
        val spanR = (maxR - minR).coerceAtLeast(16)
        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            val sw = spanCols(f.species.lengthFrac, f.zDepth)
            f.colX = (f.colFrac * (gridCols - sw - 4f) + 2f).coerceIn(2f, (gridCols - sw - 2f).coerceAtLeast(3f))
            f.rowY = minR + f.preferredDepthFrac * spanR
        }
    }

    fun randomizeEcosystemOnUnlock(seed: Long = System.nanoTime(), preserveElderCrown: Boolean = false) {
        unlockSeedCounter++
        aquariumEngine.randomizeMorphs(seed xor unlockSeedCounter)
        val rand = Random(seed xor (unlockSeedCounter * 0x9E3779B97F4A7C15UL.toLong()))

        // Build a diverse shuffled pool so every hero slot shows a genuinely varied ocean cast!
        val shuffledHeroes = AsciiFishSpecies.heroPool.copyOf()
        for (k in shuffledHeroes.size - 1 downTo 1) {
            val swap = rand.nextInt(k + 1)
            val tmp = shuffledHeroes[k]
            shuffledHeroes[k] = shuffledHeroes[swap]
            shuffledHeroes[swap] = tmp
        }
        val schoolSp = AsciiFishSpecies.schoolPool[rand.nextInt(AsciiFishSpecies.schoolPool.size)]

        activeSpeciesSummary = "${shuffledHeroes[0].displayName}, ${shuffledHeroes[1].displayName}, ${shuffledHeroes[2].displayName}, ${schoolSp.displayName}"
        landmarkColFrac = 0.36f + rand.nextFloat() * 0.28f

        shoreAmphibian.isTurtle = rand.nextBoolean()
        shoreAmphibian.diveHideTimer = 0f
        surfaceDrifter.isPaperBoat = rand.nextBoolean()
        surfaceDrifter.colX = rand.nextFloat() * (gridCols * 0.6f) + 6f

        val minR = waterlineRow + 5
        val maxR = (gridRows - 8).coerceAtLeast(minR + 20)
        val spanR = (maxR - minR).toFloat()

        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            val keepElder = preserveElderCrown && f.isElderCrown
            val isSchool = i >= maxHeroFish
            f.isSchooler = isSchool

            // Spread smoothly across deep ocean window from foreground (0.12) to misty deep (0.86)
            f.zDepth = if (isSchool) {
                0.42f + (i - maxHeroFish) * 0.06f
            } else {
                0.14f + (i.toFloat() / maxHeroFish.toFloat()) * 0.72f
            }
            f.depthLayer = when {
                f.zDepth > 0.64f -> 0
                f.zDepth > 0.32f -> 1
                else -> 2
            }
            if (!keepElder) {
                f.species = if (isSchool) {
                    schoolSp
                } else {
                    shuffledHeroes[i % shuffledHeroes.size]
                }
            }
            val laneFrac = 0.08f + ((i.toFloat() + 0.5f) / totalMaxFish.toFloat()) * 0.80f
            f.preferredDepthFrac = laneFrac.coerceIn(0.08f, 0.88f)
            f.targetDepthFrac = f.preferredDepthFrac
            f.colFrac = 0.08f + ((i * 29 + rand.nextInt(19)) % 82) / 100f

            val sw = spanCols(f.species.lengthFrac, f.zDepth)
            f.colX = (f.colFrac * (gridCols - sw - 2f)).coerceIn(1f, (gridCols - sw - 1f).coerceAtLeast(2f))
            f.rowY = minR + f.preferredDepthFrac * spanR

            val dir = if (i % 2 == 0) 1 else -1
            f.facingDir = dir
            // Slight natural ocean heading angle (slightly angled into or out of the window)
            // True 3D initial ocean headings (some gliding diagonally in from deep, some curving deeper!)
            val baseYaw = when {
                !isSchool && i % 4 == 1 -> -PI.toFloat() * 0.5f + (rand.nextFloat() - 0.5f) * 0.65f
                !isSchool && i % 4 == 3 -> PI.toFloat() * 0.5f + (rand.nextFloat() - 0.5f) * 0.65f
                else -> (if (dir > 0) 0f else PI.toFloat()) + (rand.nextFloat() - 0.5f) * 0.70f
            }
            f.yaw = wrapAngle(baseYaw)
            f.targetYaw = f.yaw
            f.facingDir = if (cos(f.yaw) >= 0f) 1 else -1
            f.pitchDeg = 0f

            f.speedScale = 0.86f + rand.nextFloat() * 0.28f
            val initSpd = f.species.baseSpeedColsPerSec * f.speedScale * (1.10f - f.zDepth * 0.42f)
            f.vx = cos(f.yaw) * initSpd
            f.vy = 0f
            f.vz = sin(f.yaw) * initSpd * 0.088f
            f.animPhase = rand.nextFloat() * 6.28f
            f.isEvading = false
            f.respawnTimer = 0f
            f.offscreenTimer = 0f
            if (!keepElder) {
                f.pelletsEaten = 0
                f.isElderCrown = false
            }
            f.burstPhase = rand.nextFloat() * 6.2831855f
            f.hoverTimer = 0f
            f.steerTimer = 3.0f + rand.nextFloat() * 6.0f
            f.wanderTimer = 3.0f + rand.nextFloat() * 5.0f
            f.gulpTimer = 0f
            f.startleTimer = 0f
        }

        val baseFloor = (gridRows - 2).coerceAtLeast(30)
        for (b in 0 until maxBottomFeeders) {
            val bf = bottomFeeders[b]
            bf.species = when (b) {
                0 -> BottomFeederSpecies.RIVER_CRAB
                1 -> BottomFeederSpecies.CHERRY_SHRIMP
                2 -> BottomFeederSpecies.CORY_CATFISH
                3 -> BottomFeederSpecies.APPLE_SNAIL
                else -> BottomFeederSpecies.entries[rand.nextInt(BottomFeederSpecies.entries.size)]
            }
            bf.preferredTier = b
            bf.rowY = (baseFloor - 1 - b).toFloat()
            bf.colX = 4f + b * ((gridCols - 16).coerceAtLeast(25) / maxBottomFeeders.toFloat())
            val dir = if (b % 2 == 0) 1f else -1f
            bf.vxDir = dir
            bf.vx = dir * bf.species.speedColsPerSec
            bf.vy = 0f
            bf.pauseTimer = 0f
            bf.isSnappingAtPredator = false
        }

        for (bp in bloodParticles) bp.active = false
        lastBloodSiteTimer = 0f
        predator.active = false
        predator.cooldownTimer = 18f + rand.nextFloat() * 18f

        // Schedule an Ocean Megafauna encounter promptly so you see whales & dolphins during normal unlocks!
        if (!oceanVisitor.active) {
            oceanVisitor.cooldownTimer = 2.0f + rand.nextFloat() * 3.0f
        }

        // Rare Legendary Spawn Roll (~7% chance)
        if (rand.nextFloat() < 0.07f) {
            spawnLegendaryCreature(rand)
        } else {
            legendary.active = false
        }
    }

    /**
     * Spawns a periodic majestic Ocean Visitor (Humpback Whale, Orca, Breaching Dolphin Pod,
     * Whale Shark, Giant Oceanic Manta, or Nocturnal Giant Squid) smoothly gliding past the window.
     */
    fun triggerOceanVisitor(seed: Long = System.nanoTime(), isNight: Boolean = false): String {
        val rand = Random(seed)
        val sp = if (isNight && rand.nextFloat() < 0.22f) {
            OceanVisitorSpecies.DEEP_SQUID
        } else {
            // Strong chance for Humpback Whales, Breaching Dolphin Pods, and Orcas!
            val roll = rand.nextFloat()
            when {
                roll < 0.29f -> OceanVisitorSpecies.HUMPBACK_WHALE
                roll < 0.58f -> OceanVisitorSpecies.DOLPHIN_POD
                roll < 0.76f -> OceanVisitorSpecies.ORCA
                roll < 0.89f -> OceanVisitorSpecies.GIANT_MANTA
                else -> OceanVisitorSpecies.WHALE_SHARK
            }
        }

        val fromLeft = rand.nextBoolean()
        val dir = if (fromLeft) 1f else -1f
        val baseZ = sp.preferredZMin + rand.nextFloat() * (sp.preferredZMax - sp.preferredZMin)
        val spriteW = spanCols(sp.lengthFrac, baseZ)
        val minR = (waterlineRow + 5).coerceAtMost(gridRows - 20)
        val maxR = (gridRows - 14).coerceAtLeast(minR + 8)
        val baseRow = when {
            sp.canBreach -> (waterlineRow + 4.5f + rand.nextFloat() * 2.5f).coerceIn(minR.toFloat(), maxR.toFloat())
            sp.canSpout -> (minR + rand.nextFloat() * (maxR - minR) * 0.42f)
            else -> (minR + rand.nextFloat() * (maxR - minR))
        }

        oceanVisitor.active = true
        oceanVisitor.species = sp
        oceanVisitor.vxDir = dir
        oceanVisitor.memberCount = sp.podSize.coerceAtMost(oceanVisitor.members.size)
        oceanVisitor.spoutTimer = 0f

        for (m in 0 until oceanVisitor.memberCount) {
            val mem = oceanVisitor.members[m]
            val spacing = (spriteW * 0.85f + 3f) * m
            // Start right at the window edge so slow giants glide into view within 1-2 seconds!
            mem.colX = if (fromLeft) (-spriteW * 0.75f - 1.5f - spacing) else (gridCols + 1.5f + spacing)
            mem.rowY = (baseRow + m * 1.8f).coerceIn(minR.toFloat(), maxR.toFloat())
            mem.zDepth = (baseZ + m * 0.06f).coerceIn(0.16f, 0.72f)
            val perspSpd = sp.speedColsPerSec * (1.08f - mem.zDepth * 0.30f)
            mem.vx = dir * perspSpd
            mem.vy = 0f
            // Subtle diagonal 3D ocean depth drift for non-breaching giants!
            mem.vz = if (sp.canBreach) 0f else ((rand.nextFloat() - 0.5f) * 0.026f)
            mem.pitchDeg = 0f
            mem.animPhase = m * 1.4f
            mem.breachPhase = 0f
            // Dolphins 1 & 2 always perform staggered leaps above the waves; dolphin 3 joins 50% of the time!
            mem.hasBreached = sp.canBreach && (m == 2 && rand.nextBoolean())
            mem.wasAboveWater = false
            // Stagger where each dolphin in the pod breaches gracefully above the waves!
            mem.breachTriggerCol = if (fromLeft) {
                gridCols * (0.20f + m * 0.17f)
            } else {
                gridCols * (0.80f - m * 0.17f)
            }
        }
        return sp.displayName
    }

    fun spawnLegendaryCreature(rand: Random = Random(System.nanoTime())): String {
        val sp = LegendarySpecies.entries[rand.nextInt(LegendarySpecies.entries.size)]
        legendary.species = sp
        legendary.active = true
        legendary.colX = 8f + rand.nextFloat() * (gridCols - 28).coerceAtLeast(16)
        val minR = (waterlineRow + 9).coerceAtMost(gridRows - 22)
        val maxR = (gridRows - 14).coerceAtLeast(minR + 6)
        legendary.rowY = minR + rand.nextFloat() * (maxR - minR)
        legendary.zDepth = 0.20f
        legendary.vxDir = if (rand.nextBoolean()) 1f else -1f
        legendary.animPhase = 0f
        activeSpeciesSummary = "★ " + sp.displayName + " + " + activeSpeciesSummary
        onLegendarySightingListener?.invoke(sp.displayName)
        return sp.displayName
    }

    fun triggerPredatorVisit(seed: Long = System.nanoTime(), catchProbability: Float = 0.45f) {
        val rand = Random(seed)
        val sp = PredatorSpecies.entries[rand.nextInt(PredatorSpecies.entries.size)]
        predator.species = sp
        predator.active = true
        predator.activeDuration = 0f
        predator.animPhase = 0f
        predator.hasCaughtThisVisit = false
        predator.willCatchThisVisit = rand.nextFloat() < catchProbability
        predator.chompTimer = 0f
        predator.stalkTimer = 0.75f
        predator.isRetreating = false
        predator.pitchDeg = 0f

        val minR = (waterlineRow + 7).coerceAtMost(gridRows - 20)
        val maxR = (gridRows - 12).coerceAtLeast(minR + 8)

        val style = rand.nextInt(3)
        predator.ambushStyle = style

        when (style) {
            1 -> {
                predator.colX = (gridCols * (0.24f + rand.nextFloat() * 0.52f))
                predator.rowY = minR + rand.nextFloat() * (maxR - minR)
                predator.zDepth = 0.94f
                predator.vxDir = if (rand.nextBoolean()) 1f else -1f
                predator.vx = predator.vxDir * (sp.speedColsPerSec * 0.35f)
                predator.vy = 0f
                predator.vz = -0.36f
            }
            2 -> {
                val fromLeft = rand.nextBoolean()
                val diveDown = rand.nextBoolean()
                predator.vxDir = if (fromLeft) 1f else -1f
                predator.colX = if (fromLeft) -16f else (gridCols + 4).toFloat()
                predator.rowY = if (diveDown) minR.toFloat() else maxR.toFloat()
                predator.zDepth = if (diveDown) 0.78f else 0.18f
                predator.vx = predator.vxDir * sp.speedColsPerSec
                predator.vy = if (diveDown) 4.2f else -4.2f
                predator.vz = if (diveDown) -0.16f else 0.16f
            }
            else -> {
                val fromLeft = rand.nextBoolean()
                predator.vxDir = if (fromLeft) 1f else -1f
                predator.colX = if (fromLeft) -16f else (gridCols + 4).toFloat()
                predator.rowY = minR + rand.nextFloat() * (maxR - minR)
                predator.zDepth = 0.16f + rand.nextFloat() * 0.60f
                predator.vx = predator.vxDir * sp.speedColsPerSec
                predator.vy = 0f
                predator.vz = 0f
            }
        }
    }

    private fun spawnBloodPlume(catchCol: Float, catchRow: Float, catchZ: Float = 0.25f) {
        lastBloodSiteCol = catchCol
        lastBloodSiteTimer = 8.0f

        for (idx in bloodParticles.indices) {
            val bp = bloodParticles[idx]
            bp.active = true
            bp.age = 0f
            bp.isScale = (idx >= 14)
            val angle = (idx.toFloat() / 14f) * (2f * PI.toFloat()) + (idx * 0.37f)
            val speed = if (bp.isScale) (1.2f + (idx % 3) * 0.8f) else (0.8f + (idx % 4) * 1.35f)

            bp.colX = catchCol + cos(angle) * 0.8f
            bp.rowY = catchRow + sin(angle) * 0.5f
            bp.zDepth = catchZ.coerceIn(0.08f, 0.88f)
            bp.vx = cos(angle) * speed
            bp.vy = if (bp.isScale) (2.4f + (idx % 2) * 1.1f) else (sin(angle) * speed * 0.65f - 0.35f)
            bp.vz = ((idx % 5) - 2) * 0.08f
            bp.maxAge = if (bp.isScale) 3.4f else (1.8f + (idx % 4) * 0.35f)
        }

        for (f in fish) {
            if (f.respawnTimer > 0f || f.offscreenTimer > 0f) continue
            val dx = f.colX - catchCol
            val dy = f.rowY - catchRow
            val d = hypot(dx, dy)
            if (d < 28f) {
                val fleeYaw = if (dx >= 0f) 0f else PI.toFloat()
                f.targetYaw = fleeYaw
                f.vx = cos(fleeYaw) * (f.species.baseSpeedColsPerSec * 2.1f)
                f.vy = (if (dy >= 0f) 1f else -1f) * 5.8f
                f.isEvading = true
            }
        }
    }

    fun onTouch(screenX: Float, screenY: Float) {
        if (lastSceneBiome == SceneBiome.AQUARIUM) {
            aquariumEngine.onTouch(screenX, screenY)
            return
        }
        val c = (screenX / cellWidthPx).coerceIn(2f, (gridCols - 3).toFloat())
        val r = (screenY / cellHeightPx).coerceIn(2f, (gridRows - 4).toFloat())

        if (!lastIsOceanBiome && abs(c - (gridCols - 9)) < 8f && abs(r - waterlineRow) < 6f && shoreAmphibian.diveHideTimer <= 0f) {
            shoreAmphibian.diveHideTimer = 9.0f
            injectSurfaceRipple((gridCols - 10).toFloat(), 5.2f)
            return
        }

        // Tactile Easter Egg 1: Tap a passing Whale or Orca to trigger an immediate blowhole spout!
        if (oceanVisitor.active && oceanVisitor.species.canSpout) {
            val sp = oceanVisitor.species
            for (m in 0 until oceanVisitor.memberCount) {
                val mem = oceanVisitor.members[m]
                val sw = spanCols(sp.lengthFrac, mem.zDepth)
                val centerCol = mem.colX + sw * 0.5f
                if (abs(c - centerCol) < (sw * 0.56f + 2.0f) && abs(r - mem.rowY) < 6.0f) {
                    val dir = if (oceanVisitor.vxDir >= 0f) 1f else -1f
                    oceanVisitor.spoutTimer = 2.4f
                    oceanVisitor.spoutCol = (centerCol + sw * 0.22f * dir).coerceIn(6f, (gridCols - 7).toFloat())
                    injectSurfaceRipple(oceanVisitor.spoutCol, 4.4f)
                    emitMouthBubble(oceanVisitor.spoutCol - 0.8f, mem.rowY - 1.5f, mem.zDepth)
                    emitMouthBubble(oceanVisitor.spoutCol + 0.8f, mem.rowY - 2.0f, mem.zDepth)
                    return
                }
            }
        }

        // Tactile Easter Egg 2: Tap the coastal Lighthouse to flash a golden beacon sweep!
        if (lastIsOceanBiome && abs(c - (gridCols - 8f)) < 6.5f && abs(r - (waterlineRow - 2.5f)) < 6.0f) {
            lighthouseFlashTimer = 2.8f
            return
        }

        // Tactile Easter Egg 3: Tap the Giant Clam to snap its shell shut with a burst of pearl bubbles!
        val tapFloorRow = (gridRows - 2).toFloat()
        if (lastIsOceanBiome) {
            val clamCol = (gridCols * 0.72f).coerceIn(12f, (gridCols - 14).toFloat())
            if (abs(c - clamCol) < 6.5f && abs(r - (tapFloorRow - 1.5f)) < 5.5f) {
                clamSnapTimer = 2.8f
                emitMouthBubble(clamCol - 1.0f, tapFloorRow - 2.2f, 0.35f)
                emitMouthBubble(clamCol, tapFloorRow - 2.8f, 0.30f)
                emitMouthBubble(clamCol + 1.0f, tapFloorRow - 2.2f, 0.35f)
                return
            }
        }

        // Tactile Easter Egg 4: Tap the underwater Stone Lantern to emit a warm glowing pulse ring!
        val lanternCol = landmarkColFrac * (gridCols - 12) + 6f
        if (abs(c - lanternCol) < 6.0f && abs(r - (tapFloorRow - 2.2f)) < 6.0f) {
            lanternPulseTimer = 1.6f
            emitMouthBubble(lanternCol - 0.6f, tapFloorRow - 3.2f, 0.42f)
            emitMouthBubble(lanternCol + 0.6f, tapFloorRow - 3.6f, 0.42f)
            return
        }

        // Tapping directly on a fish gently startles it into a natural curved burst
        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            if (f.respawnTimer > 0f || f.offscreenTimer > 0f) continue
            val sw = spanCols(f.species.lengthFrac, f.zDepth)
            val centerCol = f.colX + sw * 0.5f
            if (abs(c - centerCol) < (sw * 0.55f + 1.5f) && abs(r - f.rowY) < 2.8f) {
                val pushDir = if (c <= centerCol) 1 else -1
                f.targetYaw = wrapAngle((if (pushDir > 0) 0f else PI.toFloat()) + (if (f.zDepth > 0.45f) -0.25f else 0.25f))
                f.startleTimer = 1.25f
                f.hoverTimer = 0f
                f.vy = (if (r <= f.rowY) 1f else -1f) * 3.5f
                val mouthCol = if (f.facingDir >= 0) f.colX + sw else f.colX
                emitMouthBubble(mouthCol, f.rowY, f.zDepth)
                injectSurfaceRipple(c, 2.2f)
                return
            }
        }

        injectSurfaceRipple(c, 3.6f)

        val p = pellets[nextPelletIdx]
        p.colX = c
        p.rowY = r.coerceAtLeast(waterlineRow.toFloat() + 1f)
        val uSpan = (gridRows - waterlineRow).coerceAtLeast(20).toFloat()
        p.zDepth = (1f - ((p.rowY - waterlineRow) / uSpan)).coerceIn(0.14f, 0.80f)
        p.isHeavyAlgaeWafer = false
        p.restTimer = 0f
        p.active = true
        nextPelletIdx = (nextPelletIdx + 1) % pellets.size
    }

    fun onLongPress(screenX: Float, screenY: Float) {
        if (lastSceneBiome == SceneBiome.AQUARIUM) {
            aquariumEngine.onLongPress(screenX, screenY)
            return
        }
        val c = (screenX / cellWidthPx).coerceIn(4f, (gridCols - 5).toFloat())
        val r = (screenY / cellHeightPx).coerceIn(2f, (gridRows - 4).toFloat())
        injectSurfaceRipple(c, 5.4f)

        val p = pellets[nextPelletIdx]
        p.colX = c
        p.rowY = r.coerceAtLeast(waterlineRow.toFloat() + 1f)
        val uSpan = (gridRows - waterlineRow).coerceAtLeast(20).toFloat()
        p.zDepth = (1f - ((p.rowY - waterlineRow) / uSpan)).coerceIn(0.14f, 0.78f)
        p.isHeavyAlgaeWafer = true
        p.restTimer = 9.0f
        p.active = true
        nextPelletIdx = (nextPelletIdx + 1) % pellets.size
    }

    /**
     * Finger-drag water wake:
     *   - Sends traveling ripples along the meniscus when swiping near/in the water
     *   - Gently parts nearby fish out of your finger's trail
     *   - Leaves a fading bioluminescent plankton glow at dusk/night!
     */
    fun onDrag(screenX: Float, screenY: Float, deltaX: Float = 0f) {
        if (lastSceneBiome == SceneBiome.AQUARIUM) {
            aquariumEngine.onDrag(screenX, screenY, deltaX)
            return
        }
        val c = (screenX / cellWidthPx).coerceIn(2f, (gridCols - 3).toFloat())
        val r = (screenY / cellHeightPx).coerceIn(2f, (gridRows - 4).toFloat())
        if (r >= waterlineRow - 4f) {
            val rippleAmp = if (abs(r - waterlineRow) < 6f) 2.6f else 1.1f
            injectSurfaceRipple(c, rippleAmp)

            val snap = lastResolvedSnapshot
            if (snap != null && (snap.isNight || snap.colors.phaseName == "dusk" || snap.colors.phaseName == "twilight")) {
                val uSpan = (gridRows - waterlineRow).coerceAtLeast(20).toFloat()
                val z = (1f - ((r - waterlineRow) / uSpan)).coerceIn(0.14f, 0.75f)
                spawnBioPlankton(c, r.coerceAtLeast(waterlineRow + 1.5f), z, (deltaX * 0.08f).coerceIn(-3f, 3f), -0.6f)
            }

            // Gently part nearby fish away from your dragging finger
            for (i in 0 until totalMaxFish) {
                val f = fish[i]
                if (f.respawnTimer > 0f || f.offscreenTimer > 0f) continue
                val sw = spanCols(f.species.lengthFrac, f.zDepth)
                val dx = (f.colX + sw * 0.5f) - c
                val dy = f.rowY - r
                if (abs(dx) < 10f && abs(dy) < 6f) {
                    val pushYaw = atan2(if (f.zDepth > 0.5f) -3f else 3f, if (abs(dx) < 1f) 4f else dx)
                    f.targetYaw = pushYaw
                    f.vy += (if (dy >= 0f) 1f else -1f) * 2.2f
                    f.startleTimer = f.startleTimer.coerceAtLeast(0.45f)
                }
            }
        }
    }

    fun updateAndRender(
        canvas: Canvas,
        dt: Float,
        config: WallpaperConfig,
        parallaxX: Float,
        parallaxY: Float,
        isBatterySaver: Boolean,
        isWallpaperMode: Boolean,
        batteryPercent: Int = 85,
        isSystemDark: Boolean = true,
        previewHourOverride: Float? = null,
        isCharging: Boolean = false
    ) {
        val clampedDt = dt.coerceIn(0.001f, 0.05f)
        simTime += clampedDt
        if (simTime >= 86400f) {
            simTime -= 86400f
        }

        if (canvas.width > 0 && canvas.height > 0) {
            viewportWidthPx = canvas.width.toFloat()
            viewportHeightPx = canvas.height.toFloat()
            if (config.waterlinePercent != currentWaterlinePct) {
                configureGrid(canvas.width, canvas.height, config.gridDensity, config.waterlinePercent)
            }
        }

        val snapshot = TimeOfDayPalette.resolve(
            mode = config.timeCycleMode,
            family = config.paletteFamily,
            themeMode = config.themeMode,
            isSystemDark = isSystemDark,
            overrideHour = previewHourOverride
        )
        lastResolvedSnapshot = snapshot
        lastSceneBiome = config.sceneBiome
        if (config.sceneBiome == SceneBiome.AQUARIUM) {
            aquariumEngine.updateAndRender(
                canvas = canvas,
                dt = clampedDt,
                config = config,
                snapshot = snapshot,
                parallaxX = parallaxX,
                parallaxY = parallaxY,
                isBatterySaver = isBatterySaver,
                isWallpaperMode = isWallpaperMode,
                isCharging = isCharging
            )
            hasHighMotionActivity = aquariumEngine.isHighMotion
            return
        }
        val pal = snapshot.colors

        val cols = gridCols
        val rows = gridRows
        val pShiftCols = (parallaxX * 11.5f)
        val pShiftRowsFloat = (parallaxY * 6.5f)
        val pShiftRows = (pShiftRowsFloat * 0.45f).roundToInt()
        val splitRow = (waterlineRow + pShiftRows).coerceIn(14, rows - 24)

        val topCalmRow = if (isWallpaperMode) (rows * 0.12f).toInt() else 0
        val floorRow = rows - 2
        val underwaterRows = (rows - splitRow).coerceAtLeast(1)
        val isTrueBlackOled = (config.paletteFamily == PaletteFamily.AMOLED && !pal.isLightSurface)
        val isOceanBiome = (config.sceneBiome == SceneBiome.OCEAN)
        lastIsOceanBiome = isOceanBiome
        if (clamSnapTimer > 0f) clamSnapTimer = (clamSnapTimer - clampedDt).coerceAtLeast(0f)
        if (lighthouseFlashTimer > 0f) lighthouseFlashTimer = (lighthouseFlashTimer - clampedDt).coerceAtLeast(0f)
        if (lanternPulseTimer > 0f) lanternPulseTimer = (lanternPulseTimer - clampedDt).coerceAtLeast(0f)
        if (isCharging && config.enableBatteryLantern) {
            chargingBubbleCooldown -= clampedDt
            if (chargingBubbleCooldown <= 0f) {
                chargingBubbleCooldown = 2.6f
                val lanternCol = landmarkColFrac * (cols - 12) + 6f
                emitMouthBubble(lanternCol + sin(simTime) * 0.6f, (floorRow - 3.0f).toFloat(), 0.45f)
            }
        }
        val calmScrim = (isWallpaperMode && config.calmIconMode)
        // Unified slow ocean swell current (~38s rhythm) swaying kelp, jellies, bubbles & marine snow together
        val oceanCurrent = sin(simTime * 0.16f) * 0.75f + cos(simTime * 0.09f) * 0.45f

        if (isTrueBlackOled) {
            bgPaint.color = 0xFF000000.toInt()
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), bgPaint)
        } else {
            val splitPx = splitRow * cellHeightPx
            // Cached 1-call Sky LinearGradient (soft horizon glow at dawn/dusk/noon/night!)
            if (cachedSkySplitRow != splitRow ||
                cachedSkyTopColor != pal.skyBg ||
                cachedSkyHorizonColor != pal.skyHorizonBg
            ) {
                cachedSkySplitRow = splitRow
                cachedSkyTopColor = pal.skyBg
                cachedSkyHorizonColor = pal.skyHorizonBg
                val midSky = TimeOfDayPalette.lerpColor(pal.skyBg, pal.skyHorizonBg, 0.35f)
                skyGradientPaint.shader = LinearGradient(
                    0f, 0f,
                    0f, splitPx.coerceAtLeast(1f),
                    intArrayOf(pal.skyBg, midSky, pal.skyHorizonBg),
                    floatArrayOf(0f, 0.65f, 1.0f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), splitPx, skyGradientPaint)

            if (cachedGradSplitRow != splitRow ||
                cachedGradHeight != canvas.height ||
                cachedGradTopColor != pal.waterBgTop ||
                cachedGradDeepColor != pal.waterBgDeep ||
                cachedGradMeniscusColor != pal.meniscusWaveFg
            ) {
                cachedGradSplitRow = splitRow
                cachedGradHeight = canvas.height
                cachedGradTopColor = pal.waterBgTop
                cachedGradDeepColor = pal.waterBgDeep
                cachedGradMeniscusColor = pal.meniscusWaveFg
                val meniscusGlow = TimeOfDayPalette.lerpColor(pal.waterBgTop, pal.meniscusWaveFg, 0.20f)
                val midWater = TimeOfDayPalette.lerpColor(pal.waterBgTop, pal.waterBgDeep, 0.58f)
                waterGradientPaint.shader = LinearGradient(
                    0f, splitPx,
                    0f, canvas.height.toFloat().coerceAtLeast(splitPx + 1f),
                    intArrayOf(meniscusGlow, pal.waterBgTop, midWater, pal.waterBgDeep),
                    floatArrayOf(0f, 0.05f, 0.45f, 1.0f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, splitPx, canvas.width.toFloat(), canvas.height.toFloat(), waterGradientPaint)
        }

        val totalCells = cols * rows
        for (i in 0 until totalCells) {
            charGrid[i] = ' '
            colorGrid[i] = pal.cloudReedFg
        }

        fun setCell(c: Int, r: Int, ch: Char, color: Int) {
            if (c in 0 until cols && r in 0 until rows && ch != ' ') {
                val idx = r * cols + c
                charGrid[idx] = ch
                colorGrid[idx] = color
            }
        }

        fun drawStringAtGrid(c: Int, r: Int, text: String, color: Int) {
            if (r !in 0 until rows) return
            for (i in text.indices) {
                val ch = text[i]
                if (ch != ' ') {
                    setCell(c + i, r, ch, color)
                }
            }
        }

        // =========================================================================
        // 2. ATMOSPHERIC ASCII PARTICLES (Stars, Shooting Star, Aurora, Weather,
        //    Glitter Path, Bubbles & Marine Snow) + PHYSICS UPDATES FOR SCENE
        // =========================================================================
        val pShiftInt = pShiftCols.roundToInt()
        val celestialColFloat = (snapshot.sunArcX * (cols - 12) + 6f + pShiftCols).coerceIn(5f, (cols - 7).toFloat())
        val celestialCol = celestialColFloat.roundToInt()
        val skyHeight = (splitRow - 5).coerceAtLeast(6)
        val celestialRowFloat = ((1f - snapshot.sunArcY) * (skyHeight - 4) + 3f).coerceIn(3f, (splitRow - 5).toFloat())

        if (snapshot.isNight) {
            for (s in 0 until 20) {
                val sc = ((s * 17 + 7 + pShiftInt) % (cols - 2)).coerceAtLeast(1)
                val sr = ((s * 9 + 3) % (splitRow - 4)).coerceAtLeast(1)
                val twinkle = sin(simTime * 2.2f + s * 1.7f)
                val starChar = if (twinkle > 0.35f) '*' else '.'
                setCell(sc, sr, starChar, pal.celestialGlyph)
            }
            if (!shootingStar.active) {
                shootingStar.timer -= clampedDt
                if (shootingStar.timer <= 0f) {
                    shootingStar.active = true
                    shootingStar.colX = (cols * 0.15f)
                    shootingStar.rowY = 3f
                }
            } else {
                shootingStar.colX += shootingStar.vx * clampedDt
                shootingStar.rowY += shootingStar.vy * clampedDt
                val sc = shootingStar.colX.roundToInt()
                val sr = shootingStar.rowY.roundToInt()
                if (sr < splitRow - 4 && sc < cols - 4) {
                    drawStringAtGrid(sc - 3, sr, "---*", pal.fishGold)
                } else {
                    shootingStar.active = false
                    shootingStar.timer = 16f + (sin(simTime) * 0.5f + 0.5f) * 14f
                }
            }
        }

        val effectiveWeather = when (config.weatherMode) {
            WeatherMode.AUTO -> {
                when {
                    snapshot.autoWeatherSlot == 2 -> WeatherMode.RAIN
                    snapshot.isNight && snapshot.autoWeatherSlot == 9 -> WeatherMode.AURORA
                    else -> WeatherMode.CLEAR
                }
            }
            else -> config.weatherMode
        }

        val isRaining = (effectiveWeather == WeatherMode.RAIN)
        if (effectiveWeather == WeatherMode.RAIN || effectiveWeather == WeatherMode.SNOW) {
            val isSnow = effectiveWeather == WeatherMode.SNOW
            val dropCount = if (isBatterySaver) 12 else weatherDrops.size
            for (dIdx in 0 until dropCount) {
                val drop = weatherDrops[dIdx]
                val spdY = if (isSnow) 5.5f else drop.speedY
                val spdX = if (isSnow) sin(simTime * 1.4f + dIdx) * 1.8f else drop.speedX
                drop.rowY += spdY * clampedDt
                drop.colX = (drop.colX + spdX * clampedDt + cols) % cols

                if (drop.rowY >= splitRow) {
                    if (!isSnow) {
                        val sp = rainSplashes[nextSplashIdx]
                        nextSplashIdx = (nextSplashIdx + 1) % rainSplashes.size
                        sp.active = true
                        sp.colX = drop.colX
                        sp.age = 0f
                        sp.maxAge = 0.36f + (dIdx % 3) * 0.08f
                        injectSurfaceRipple(drop.colX, 1.4f)
                    }
                    drop.rowY = 1f
                    drop.colX = ((dIdx * 19 + (simTime * 13f).toInt()) % cols).toFloat()
                }
            }
        }
        for (sIdx in rainSplashes.indices) {
            val sp = rainSplashes[sIdx]
            if (sp.active) {
                sp.age += clampedDt
                if (sp.age >= sp.maxAge) sp.active = false
            }
        }

        // Update 1D spring-propagated waterline wave heightfield
        val wavePhase = (simTime * 3.0f).toInt()
        val waveDamp = exp(-2.2f * clampedDt)
        val tension = 28.0f
        val spread = 14.0f
        for (c in 0 until cols) {
            val leftH = if (c > 0) waveHeight[c - 1] else waveHeight[c]
            val rightH = if (c < cols - 1) waveHeight[c + 1] else waveHeight[c]
            val accel = -tension * waveHeight[c] + spread * (leftH + rightH - 2f * waveHeight[c])
            val nv = (waveVel[c] + accel * clampedDt) * waveDamp
            waveVel[c] = nv.coerceIn(-7f, 7f)
            waveNext[c] = (waveHeight[c] + nv * clampedDt).coerceIn(-2.2f, 2.2f)
        }
        for (c in 0 until cols) {
            waveHeight[c] = waveNext[c]
        }

        // Sun & Moon Glitter Path shimmering on the ocean surface directly beneath the celestial body!
        val glitterBaseCol = celestialCol.coerceIn(5, cols - 6)
        val glitterColor = if (snapshot.isNight) pal.bubbleFg else pal.celestialGlyph
        for (gRow in 1..4) {
            val halfSpan = (5 - gRow).coerceAtLeast(1)
            val rowMix = (1f - gRow * 0.17f) * (if (calmScrim) 0.55f else 0.88f)
            val gCol = TimeOfDayPalette.lerpColor(pal.waterBgTop, glitterColor, rowMix)
            for (off in -halfSpan..halfSpan) {
                val shimmer = sin(simTime * 3.4f + gRow * 1.9f + off * 1.3f)
                if (shimmer > 0.28f) {
                    val gCh = when {
                        gRow <= 2 -> if (shimmer > 0.70f) '~' else '-'
                        else -> if (shimmer > 0.65f) ':' else '·'
                    }
                    setCell(glitterBaseCol + off, splitRow + gRow, gCh, gCol)
                }
            }
        }

        // Update sky & horizon dynamic actors (seabirds, sailboat, clam, frog/turtle, drifter, dragonfly)
        if (isOceanBiome) {
            if (!snapshot.isNight) {
                val birdCount = if (isBatterySaver) 2 else seabirds.size
                for (bIdx in 0 until birdCount) {
                    val bird = seabirds[bIdx]
                    bird.flapPhase = (bird.flapPhase + clampedDt * 2.8f) % 62.831855f
                    bird.colX += bird.vx * clampedDt
                    if (bird.colX > cols + 8f) bird.colX = -6f
                    if (bird.colX < -8f) bird.colX = (cols + 6).toFloat()
                }
            }
            distantBoatCol += 0.48f * clampedDt
            if (distantBoatCol > cols + 6f) distantBoatCol = -8f

            clamOpenPhase = (clamOpenPhase + clampedDt * 0.45f) % 6.2831855f
            val clamColFloat = (cols * 0.72f + pShiftCols * 0.45f).coerceIn(12f, (cols - 14).toFloat())
            if (sin(clamOpenPhase) > 0.65f && sin(clamOpenPhase * 12f) > 0.96f) {
                emitMouthBubble(clamColFloat, (floorRow - 2).toFloat(), 0.42f)
            }
        } else {
            if (shoreAmphibian.diveHideTimer > 0f) {
                shoreAmphibian.diveHideTimer = (shoreAmphibian.diveHideTimer - clampedDt).coerceAtLeast(0f)
            } else {
                shoreAmphibian.blinkPhase = (shoreAmphibian.blinkPhase + clampedDt * 2.0f) % 6.2831855f
                if (shoreAmphibian.tongueTimer > 0f) {
                    shoreAmphibian.tongueTimer = (shoreAmphibian.tongueTimer - clampedDt).coerceAtLeast(0f)
                }
                val rockCol = (cols - 11).toFloat()
                if (!shoreAmphibian.isTurtle && !snapshot.isNight) {
                    val distFly = rockCol - surfaceInsect.colX
                    if (distFly in 2f..8f && shoreAmphibian.tongueTimer <= 0f) {
                        shoreAmphibian.tongueTimer = 0.28f
                    }
                }
            }
            surfaceDrifter.colX += surfaceDrifter.vx * clampedDt
            if (surfaceDrifter.colX > cols - 18) surfaceDrifter.vx = -1.2f
            if (surfaceDrifter.colX < 8f) surfaceDrifter.vx = 1.2f
        }

        // Reactive underwater kelp physics (swaying with oceanCurrent + parting for fish/predators!)
        val activeHero = if (isBatterySaver) (config.heroFishCount - 2).coerceAtLeast(2) else config.heroFishCount
        val activeSchool = if (isBatterySaver) 2 else maxSchoolFish
        val kelpDamp = exp(-3.4f * clampedDt)
        val kelpCount = if (isBatterySaver) 3 else kelpStalks.size
        for (kIdx in 0 until kelpCount) {
            val ks = kelpStalks[kIdx]
            val baseCol = ks.colFrac * cols
            val rootRow = (floorRow - 1 - (ks.zDepth * 3.5f).roundToInt()).coerceIn(splitRow + 10, floorRow - 1)

            var pushForce = oceanCurrent * 2.2f
            for (fIdx in 0 until activeHero) {
                val f = fish[fIdx]
                if (f.respawnTimer > 0f || f.offscreenTimer > 0f) continue
                if (abs(f.zDepth - ks.zDepth) < 0.32f &&
                    abs(f.colX + 4f - baseCol) < 7.5f &&
                    f.rowY > rootRow - ks.heightRows - 2f
                ) {
                    pushForce += f.vx * 0.45f
                }
            }
            if (predator.active && abs(predator.zDepth - ks.zDepth) < 0.38f &&
                abs(predator.colX + 5f - baseCol) < 10f &&
                predator.rowY > rootRow - ks.heightRows - 3f
            ) {
                pushForce += predator.vx * 0.75f
            }

            val kAccel = -14.0f * ks.bendOffset + pushForce
            ks.bendVel = ((ks.bendVel + kAccel * clampedDt) * kelpDamp).coerceIn(-8f, 8f)
            ks.bendOffset = (ks.bendOffset + ks.bendVel * clampedDt).coerceIn(-3.2f, 3.2f)
        }

        // Update Rising Bubbles, Marine Snow & Sinking Pellets / Heavy Algae Wafers
        val activeBubbles = if (isBatterySaver) 10 else bubbles.size
        for (i in 0 until activeBubbles) {
            val b = bubbles[i]
            b.rowY -= b.speedY * clampedDt
            val helixAngle = simTime * 2.1f + b.phase
            b.colX += (cos(helixAngle) * 0.55f + oceanCurrent * 0.35f) * clampedDt
            b.zDepth = (0.50f + sin(helixAngle) * 0.44f).coerceIn(0.04f, 0.94f)

            if (b.rowY <= splitRow + 1) {
                val refFish = fish[i % activeHero.coerceAtLeast(1)]
                val refW = spanCols(refFish.species.lengthFrac, refFish.zDepth)
                val mouthX = if (refFish.facingDir >= 0) refFish.colX + refW - 0.5f else refFish.colX + 0.5f
                b.colX = mouthX.coerceIn(2f, (cols - 2).toFloat())
                b.rowY = if (b.isBubble && refFish.offscreenTimer <= 0f) refFish.rowY else (rows - 4).toFloat()
                b.zDepth = refFish.zDepth
            }

            if (b.zDepth >= 0.18f) {
                val bc = (b.colX + pShiftCols * (0.8f - b.zDepth * 0.6f)).roundToInt()
                val br = b.rowY.roundToInt()
                if (br in (splitRow + 2) until (floorRow - 1) && bc in 0 until cols) {
                    val bChar = if (b.isBubble) {
                        when {
                            b.zDepth < 0.42f -> 'o'
                            b.zDepth < 0.70f -> '°'
                            else -> '·'
                        }
                    } else {
                        if (snapshot.isNight && (i + wavePhase) % 3 == 0) '·' else '.'
                    }
                    val bCol = if (snapshot.isNight && !b.isBubble) {
                        pal.fishRose
                    } else {
                        TimeOfDayPalette.lerpColor(pal.bubbleFg, pal.waterBgDeep, b.zDepth * 0.65f)
                    }
                    setCell(bc, br, bChar, bCol)
                }
            }
        }

        var activeWaferCol = -1f
        var activeWaferZ = 0.35f
        for (i in pellets.indices) {
            val pel = pellets[i]
            if (!pel.active) continue
            val targetFloorAtZ = (floorRow - 0.6f - pel.zDepth * 4.5f)
            val sinkScale = 1.15f - pel.zDepth * 0.45f
            if (pel.isHeavyAlgaeWafer) {
                if (pel.rowY < targetFloorAtZ) {
                    pel.rowY = (pel.rowY + 9.5f * sinkScale * clampedDt).coerceAtMost(targetFloorAtZ)
                } else {
                    pel.restTimer -= clampedDt
                    if (pel.restTimer <= 0f) {
                        pel.active = false
                    }
                }
                if (pel.active) {
                    activeWaferCol = pel.colX
                    activeWaferZ = pel.zDepth
                }
            } else {
                pel.rowY += 3.2f * sinkScale * clampedDt
                if (pel.rowY >= targetFloorAtZ) {
                    pel.active = false
                }
            }
        }

        // Flush atmospheric ASCII particle grid (stars, glitter, weather, bubbles) to Canvas
        for (r in 0 until rows) {
            if (r < topCalmRow && isWallpaperMode && r % 2 == 1) continue
            val yPx = r * cellHeightPx + baselineOffsetPx
            val rowOffset = r * cols

            var c = 0
            while (c < cols) {
                val ch = charGrid[rowOffset + c]
                if (ch == ' ') {
                    c++
                    continue
                }
                val runColor = colorGrid[rowOffset + c]
                val runStart = c
                var runLen = 0
                var lastNonSpaceLen = 0
                while (c < cols) {
                    val curCh = charGrid[rowOffset + c]
                    val curCol = colorGrid[rowOffset + c]
                    if (curCh != ' ' && curCol == runColor) {
                        rowCharScratch[runLen++] = curCh
                        lastNonSpaceLen = runLen
                        c++
                    } else if (curCh == ' ') {
                        rowCharScratch[runLen++] = ' '
                        c++
                    } else {
                        break
                    }
                }
                if (lastNonSpaceLen > 0) {
                    textPaint.color = runColor
                    canvas.drawText(rowCharScratch, 0, lastNonSpaceLen, runStart * cellWidthPx, yPx, textPaint)
                }
            }
        }

        // =========================================================================
        // 3. SCULPTED + MONOSPACE ASCII SCENE LAYER (Matching Fish Aesthetic!)
        // =========================================================================
        val refPx = refSpanPx()
        val waterLinePx = splitRow * cellHeightPx + baselineOffsetPx * 0.35f
        val floorLinePx = floorRow * cellHeightPx + baselineOffsetPx * 0.2f

        // 3A. Sky: Sculpted Aurora, Sun / Lunar Moon, Drifting Clouds, Seabirds, Sailboat, Lighthouse, Rain/Snow & Whale Spout
        if (effectiveWeather == WeatherMode.AURORA) {
            sceneRenderer.drawAuroraCurtains(
                canvas, textPaint, waterLinePx, simTime,
                pal.meniscusWaveFg, pal.fishAqua, pal.fishRose,
                calmScrim, isBatterySaver, isTrueBlackOled
            )
        }

        val celX = celestialColFloat * cellWidthPx
        val celY = celestialRowFloat * cellHeightPx
        val celRadius = refPx * 0.038f
        if (snapshot.isNight) {
            sceneRenderer.drawMoon(
                canvas, textPaint, celX, celY, celRadius, snapshot.lunarPhaseIndex,
                pal.celestialGlyph, pal.skyBg, pal.isLightSurface, isTrueBlackOled, calmScrim
            )
        } else {
            sceneRenderer.drawSun(
                canvas, textPaint, celX, celY, celRadius,
                pal.celestialGlyph, pal.skyBg, simTime, pal.isLightSurface, isTrueBlackOled, calmScrim || isRaining
            )
        }

        val cloudWrapSpan = canvas.width + refPx * 0.36f
        val cloud1X = ((simTime * cellWidthPx * 0.9f + refPx * 0.14f + pShiftCols * cellWidthPx) % cloudWrapSpan) - refPx * 0.18f
        val cloud1Y = (splitRow * 0.32f).coerceIn(2.5f, (splitRow - 4).toFloat()) * cellHeightPx
        sceneRenderer.drawCloud(
            canvas, textPaint, cloud1X, cloud1Y, refPx * 0.15f,
            pal.cloudReedFg, pal.skyBg, pal.isLightSurface, isTrueBlackOled, calmScrim
        )

        val cloud2X = ((simTime * cellWidthPx * 0.55f + canvas.width * 0.58f + pShiftCols * cellWidthPx) % cloudWrapSpan) - refPx * 0.18f
        val cloud2Y = (splitRow * 0.54f).coerceIn(3.5f, (splitRow - 3).toFloat()) * cellHeightPx
        sceneRenderer.drawCloud(
            canvas, textPaint, cloud2X, cloud2Y, refPx * 0.125f,
            pal.cloudReedFg, pal.skyBg, pal.isLightSurface, isTrueBlackOled, calmScrim
        )

        val lhX = ((cols - 8f) + pShiftCols * 0.12f).coerceIn(cols - 12f, cols - 4f) * cellWidthPx
        val isDuskOrNight = snapshot.isNight || pal.phaseName == "dusk" || pal.phaseName == "sunset" || pal.phaseName == "twilight"
        val beaconOn = isOceanBiome && isDuskOrNight && sin(simTime * 1.85f) > 0.15f

        if (isOceanBiome) {
            if (!snapshot.isNight) {
                val birdCount = if (isBatterySaver) 2 else seabirds.size
                for (bIdx in 0 until birdCount) {
                    val bird = seabirds[bIdx]
                    val bx = (bird.colX + pShiftCols * 0.35f) * cellWidthPx
                    val by = (splitRow * bird.rowFrac + sin(bird.flapPhase * 0.45f) * 0.85f).coerceIn(2f, (splitRow - 3.5f)) * cellHeightPx
                    sceneRenderer.drawSeabird(
                        canvas, bx, by, refPx * (0.036f - bIdx * 0.004f),
                        bird.flapPhase, pal.cloudReedFg, calmScrim
                    )
                }
            }
            val boatX = (distantBoatCol + pShiftCols * 0.18f) * cellWidthPx
            val boatColor = TimeOfDayPalette.lerpColor(pal.skyHorizonBg, pal.cloudReedFg, 0.82f)
            sceneRenderer.drawSailboat(
                canvas, textPaint, boatX, waterLinePx, refPx * 0.056f,
                boatColor, pal.fishFoam, pal.skyHorizonBg, simTime, isTrueBlackOled
            )
            sceneRenderer.drawLighthouse(
                canvas, textPaint, lhX, waterLinePx, refPx * 0.070f,
                pal.cloudReedFg, pal.fishGold, pal.skyHorizonBg, beaconOn, simTime, isTrueBlackOled,
                extraFlash = lighthouseFlashTimer > 0f
            )
        } else {
            sceneRenderer.drawShorelineReeds(canvas, cellWidthPx * 4.5f, waterLinePx, refPx * 0.065f, simTime, pal.kelpFg)
            sceneRenderer.drawShorelineReeds(canvas, canvas.width - cellWidthPx * 4.5f, waterLinePx, refPx * 0.065f, simTime + 1.4f, pal.kelpFg)
        }

        // Sculpted Raindrops / Snowflakes & Expanding Water-Surface Splash Crowns
        if (effectiveWeather == WeatherMode.RAIN || effectiveWeather == WeatherMode.SNOW) {
            val isSnow = effectiveWeather == WeatherMode.SNOW
            val dropCount = if (isBatterySaver) 12 else weatherDrops.size
            for (dIdx in 0 until dropCount) {
                val drop = weatherDrops[dIdx]
                if (drop.rowY >= splitRow) continue
                val dx = drop.colX * cellWidthPx
                val dy = drop.rowY * cellHeightPx
                if (isSnow) {
                    sceneRenderer.drawSnowFlake(canvas, textPaint, dx, dy, refPx * 0.0045f, dIdx % 3 == 0, pal.bubbleFg)
                } else {
                    sceneRenderer.drawRainDrop(canvas, dx, dy, refPx * 0.022f, pal.bubbleFg)
                }
            }
        }
        for (sIdx in rainSplashes.indices) {
            val sp = rainSplashes[sIdx]
            if (sp.active) {
                sceneRenderer.drawRainSplashCrown(
                    canvas, sp.colX * cellWidthPx, waterLinePx,
                    (sp.age / sp.maxAge).coerceIn(0f, 1f),
                    refPx * 0.026f, pal.meniscusWaveFg
                )
            }
        }

        // Whale surface blowhole water spout arc!
        if (oceanVisitor.spoutTimer > 0f) {
            oceanVisitor.spoutTimer = (oceanVisitor.spoutTimer - clampedDt).coerceAtLeast(0f)
            val spX = (oceanVisitor.spoutCol + pShiftCols * 0.4f).coerceIn(4f, (cols - 5).toFloat()) * cellWidthPx
            val frac = 1f - (oceanVisitor.spoutTimer / 2.4f).coerceIn(0f, 1f)
            sceneRenderer.drawWhaleSpout(canvas, textPaint, spX, waterLinePx, refPx * 0.105f, frac, pal.bubbleFg)
        }

        // 3B. Underwater Landscape: Abyssal Canyon Ridge, Reef Shelf, Sunbeams, Surface Reflections & Seabed Caustics
        val canyonBaseY = (splitRow + underwaterRows * 0.62f + pShiftRowsFloat * 0.15f).coerceIn(splitRow + 6f, floorRow - 8f) * cellHeightPx
        sceneRenderer.drawSubmergedTerrainRidge(
            canvas, canyonBaseY, refPx * 0.028f, 8.5f, -pShiftCols * 0.04f,
            pal.pebbleFg, pal.waterBgDeep, if (calmScrim) 0.14f else 0.22f, isTrueBlackOled
        )
        val shelfBaseY = (floorRow - 7f + pShiftRowsFloat * 0.38f).coerceIn(splitRow + 10f, floorRow - 4f) * cellHeightPx
        sceneRenderer.drawSubmergedTerrainRidge(
            canvas, shelfBaseY, refPx * 0.020f, 12.0f, -pShiftCols * 0.09f + 1.8f,
            pal.pebbleFg, pal.waterBgDeep, if (calmScrim) 0.22f else 0.34f, isTrueBlackOled
        )

        if (!snapshot.isNight && config.waterClarity > 0.45f) {
            val rayCount = if (isRaining) 2 else if (config.waterClarity > 0.78f) 4 else 2
            sceneRenderer.drawSunbeamRibbons(
                canvas, waterLinePx, floorLinePx, simTime, rayCount,
                pal.sunRayFg, calmScrim || isRaining, pal.isLightSurface, isTrueBlackOled
            )
            sceneRenderer.drawSeabedCaustics(
                canvas, floorLinePx, refPx, simTime, pShiftCols * cellWidthPx,
                pal.sunRayFg, calmScrim, pal.isLightSurface, isRaining, isTrueBlackOled
            )
        }

        // Soft Inverted Surface Reflections beneath the Meniscus (Sun / Moon + Blinking Lighthouse)
        sceneRenderer.drawSurfaceReflections(
            canvas, waterLinePx, celX,
            if (snapshot.isNight) pal.bubbleFg else pal.celestialGlyph,
            lhX, beaconOn || lighthouseFlashTimer > 0f, pal.fishGold,
            refPx, simTime, calmScrim, isTrueBlackOled
        )

        // 3C. Continuous Spring-Wave Meniscus & Zen Pond Floating Elements
        sceneRenderer.drawMeniscusWave(
            canvas, textPaint, cols, cellWidthPx, waterLinePx, cellHeightPx,
            waveHeight, simTime, pal.meniscusWaveFg, pal.waterBgTop, pal.isLightSurface, isTrueBlackOled
        )

        if (!isOceanBiome) {
            val pad1Col = (cols * 0.22f + sin(simTime * 0.6f) * 1.5f + pShiftCols)
            val pad1BobPx = -waveHeight[pad1Col.roundToInt().coerceIn(0, cols - 1)] * cellHeightPx * 0.35f
            sceneRenderer.drawLilyPadAndLotus(
                canvas, textPaint, pad1Col * cellWidthPx, waterLinePx + pad1BobPx, refPx * 0.090f,
                hasLotus = true, simTime, pal.lilyPadFg, pal.lotusFlowerFg, pal.waterBgTop, isTrueBlackOled
            )
            val pad2Col = (cols * 0.62f + cos(simTime * 0.5f) * 1.5f + pShiftCols)
            val pad2BobPx = -waveHeight[pad2Col.roundToInt().coerceIn(0, cols - 1)] * cellHeightPx * 0.35f
            sceneRenderer.drawLilyPadAndLotus(
                canvas, textPaint, pad2Col * cellWidthPx, waterLinePx + pad2BobPx, refPx * 0.078f,
                hasLotus = false, simTime + 1.5f, pal.lilyPadFg, pal.lotusFlowerFg, pal.waterBgTop, isTrueBlackOled
            )

            val rockX = (cols - 10.5f) * cellWidthPx
            sceneRenderer.drawShoreRockAndAmphibian(
                canvas, textPaint, rockX, waterLinePx, refPx * 0.088f,
                isTurtle = shoreAmphibian.isTurtle,
                isBlinking = sin(shoreAmphibian.blinkPhase) > 0.92f,
                isHidden = shoreAmphibian.diveHideTimer > 0f,
                showSplash = shoreAmphibian.diveHideTimer > 8.2f,
                tongueOut = shoreAmphibian.tongueTimer > 0f,
                rockColor = pal.pebbleFg,
                amphColor = pal.lilyPadFg,
                accentColor = pal.fishCoral,
                eyeColor = pal.fishFoam,
                skyBg = pal.skyHorizonBg,
                isTrueBlackOled = isTrueBlackOled
            )

            val driftC = (surfaceDrifter.colX + pShiftCols * 0.4f)
            val driftBobPx = -waveHeight[driftC.roundToInt().coerceIn(0, cols - 1)] * cellHeightPx * 0.35f
            sceneRenderer.drawSurfaceDrifter(
                canvas, driftC * cellWidthPx, waterLinePx + driftBobPx, refPx * 0.052f,
                surfaceDrifter.isPaperBoat,
                if (surfaceDrifter.isPaperBoat) pal.fishFoam else pal.fishCoral,
                pal.skyHorizonBg, isTrueBlackOled
            )
        }

        // 3D. Background Seabed Layer: Cobbles, Giant Clam, Stone Battery Lantern & Food Pellets
        //     (Kelp Stalks & Foreground Coral/Anemone now render in Step 11's Unified 3D Z-Sort so fish weave between them!)
        sceneRenderer.drawSeabedPebbles(
            canvas, textPaint, floorLinePx, pShiftCols * cellWidthPx * 0.65f,
            pal.pebbleFg, pal.waterBgDeep, isTrueBlackOled
        )

        if (isOceanBiome) {
            val clamX = (cols * 0.72f + pShiftCols * 0.45f).coerceIn(12f, (cols - 14).toFloat()) * cellWidthPx
            val clamOpenFrac = if (clamSnapTimer > 0f) 0f else ((sin(clamOpenPhase) - 0.15f) / 0.85f).coerceIn(0f, 1f)
            sceneRenderer.drawGiantClam(
                canvas, textPaint, clamX, floorLinePx - 2f, refPx * 0.072f,
                clamOpenFrac, pal.pebbleFg, pal.fishFoam, pal.waterBgDeep, isTrueBlackOled
            )
        }

        val lmX = (landmarkColFrac * (cols - 12) + 6f + pShiftCols * 0.3f) * cellWidthPx
        val lanternGlowCol = if (!config.enableBatteryLantern || batteryPercent >= 50) {
            pal.fishGold
        } else if (batteryPercent >= 20) {
            pal.fishCoral
        } else {
            pal.bloodCore
        }
        val lowBatBlink = config.enableBatteryLantern && batteryPercent < 20 && (wavePhase % 2 == 1)
        sceneRenderer.drawStoneLantern(
            canvas, textPaint, lmX, floorLinePx, refPx * 0.076f,
            pal.pebbleFg, lanternGlowCol, pal.waterBgDeep, lowBatBlink, isTrueBlackOled,
            pulseFrac = (lanternPulseTimer / 1.6f).coerceIn(0f, 1f),
            isCharging = isCharging && config.enableBatteryLantern,
            simTime = simTime
        )

        // Render active sinking Food Pellets & Heavy Algae Wafers
        for (i in pellets.indices) {
            val pel = pellets[i]
            if (!pel.active) continue
            val pParallax = 0.85f - pel.zDepth * 0.60f
            val px = (pel.colX + pShiftCols * pParallax) * cellWidthPx
            val py = pel.rowY * cellHeightPx + baselineOffsetPx * 0.5f
            val pColor = if (pel.isHeavyAlgaeWafer) {
                TimeOfDayPalette.lerpColor(pal.lilyPadFg, pal.waterBgDeep, pel.zDepth * 0.50f)
            } else {
                TimeOfDayPalette.lerpColor(pal.fishGold, pal.waterBgDeep, pel.zDepth * 0.45f)
            }
            val pRadius = (refPx * (if (pel.isHeavyAlgaeWafer) 0.0095f else 0.0048f) * (1.10f - pel.zDepth * 0.45f)).coerceAtLeast(2.0f)
            sceneRenderer.drawFoodPellet(canvas, textPaint, px, py, pRadius, pel.isHeavyAlgaeWafer, pColor, pal.waterBgDeep, isTrueBlackOled)
        }

        val baseTextSizePx = textPaint.textSize
        val waterSurfacePx = (splitRow + 0.55f) * cellHeightPx

        /**
         * Screen-Proportional Organic Silhouette + Monospace ASCII-Dither Creature Renderer:
         * - Scales naturally from viewport dimensions (`lengthFrac`), independent of grid density
         * - Uses real traveling S-wave spine undulation, smooth 3D yaw foreshortening, and Beer-Lambert fog
         */
        fun drawMarineCreature(
            colFloat: Float,
            rowFloat: Float,
            lengthFrac: Float,
            aspectRatio: Float,
            plan: BodyPlan,
            marking: MarkingStyle,
            bodyColor: Int,
            accentColor: Int,
            eyeColor: Int,
            zDepth: Float = 0.25f,
            inSunbeamShaft: Boolean = false,
            facingRight: Boolean = true,
            cosYaw: Float = if (facingRight) 1f else -1f,
            sinYaw: Float = 0f,
            pitchDeg: Float = 0.0f,
            animPhase: Float = 0f,
            allowAboveWater: Boolean = false,
            isChomping: Boolean = false
        ) {
            val zClamped = zDepth.coerceIn(0.0f, 1.0f)
            val lengthPx = creatureLengthPx(lengthFrac, zClamped)
            val colSpan = lengthPx / cellWidthPx.coerceAtLeast(1f)
            val centerX = (colFloat + colSpan * 0.5f) * cellWidthPx
            val centerY = rowFloat * cellHeightPx + baselineOffsetPx * 0.5f

            val vertFrac = ((rowFloat - splitRow) / underwaterRows.toFloat()).coerceIn(0f, 1f)
            val localWaterBg = if (rowFloat < splitRow) pal.skyBg else TimeOfDayPalette.lerpColor(pal.waterBgTop, pal.waterBgDeep, vertFrac)
            val fogFactor = if (rowFloat < splitRow) {
                (zClamped * 0.22f).coerceIn(0f, 0.28f)
            } else {
                (zClamped * 0.64f + vertFrac * 0.16f).coerceIn(0f, 0.76f)
            }

            creatureRenderer.drawCreature(
                canvas = canvas,
                textPaint = textPaint,
                baseTextSizePx = baseTextSizePx,
                centerX = centerX,
                centerY = centerY,
                lengthPx = lengthPx,
                aspectRatio = aspectRatio,
                plan = plan,
                marking = marking,
                bodyColor = bodyColor,
                accentColor = accentColor,
                eyeColor = eyeColor,
                localWaterBg = localWaterBg,
                fogFactor = fogFactor,
                inSunbeam = inSunbeamShaft,
                sunbeamHighlight = pal.sunbeamHighlight,
                isLightSurface = pal.isLightSurface,
                isTrueBlackOled = isTrueBlackOled,
                isBatterySaver = isBatterySaver,
                facingRight = facingRight,
                cosYaw = cosYaw,
                sinYaw = sinYaw,
                pitchDeg = pitchDeg,
                animPhase = animPhase,
                isChomping = isChomping,
                waterSurfacePx = waterSurfacePx,
                allowAboveWater = allowAboveWater
            )
        }

        fun resolveRoleColor(role: Int): Int = when (role) {
            0 -> pal.fishCoral
            1 -> pal.fishAqua
            2 -> pal.fishGold
            3 -> pal.fishFoam
            else -> pal.fishRose
        }

        // =========================================================================
        // 6. SURFACE-SKIMMING SCULPTED DRAGONFLY / NIGHT FIREFLIES
        // =========================================================================
        surfaceInsect.phase = (surfaceInsect.phase + clampedDt * 5.5f) % 62.831855f
        surfaceInsect.colX += surfaceInsect.vx * clampedDt
        if (surfaceInsect.colX > cols - 8) surfaceInsect.vx = -3.6f
        if (surfaceInsect.colX < 4f) surfaceInsect.vx = 3.6f
        surfaceInsect.rowY = (splitRow - 2.6f) + sin(surfaceInsect.phase * 0.7f) * 0.8f

        val insX = (surfaceInsect.colX + pShiftCols * 0.6f) * cellWidthPx
        val insY = surfaceInsect.rowY * cellHeightPx + baselineOffsetPx * 0.5f
        sceneRenderer.drawSurfaceInsect(
            canvas, textPaint, insX, insY, refPx * 0.036f,
            surfaceInsect.phase, surfaceInsect.vx >= 0f, snapshot.isNight,
            if (snapshot.isNight) pal.fishRose else pal.fishAqua,
            isTrueBlackOled
        )

        // =========================================================================
        // 7. NIGHTTIME BIOLUMINESCENT MOON JELLYFISH (Physics — drawn in unified Z-sort!)
        // =========================================================================
        if (snapshot.isNight) {
            for (j in nightJellies.indices) {
                val jel = nightJellies[j]
                jel.phase = (jel.phase + clampedDt * 2.2f) % 62.831855f
                jel.rowY -= (0.9f + 0.5f * sin(jel.phase)) * clampedDt
                jel.colX += (cos(jel.phase * 0.6f + j) * 0.5f + oceanCurrent * 0.35f) * clampedDt
                if (jel.rowY < splitRow + 6) {
                    jel.rowY = (floorRow - 6).toFloat()
                }
            }
        }

        // =========================================================================
        // 7B. OCEAN MEGAFAUNA VISITORS (Humpback Whales, Orcas, Breaching Dolphins,
        //     Whale Sharks, Giant Mantas & Deep Squid gliding past your window!)
        // =========================================================================
        var highMotion = false
        val minFishRow = (splitRow + 4).toFloat()
        val maxFishRow = (floorRow - 6).toFloat().coerceAtLeast(minFishRow + 12f)
        val underwaterSpan = (maxFishRow - minFishRow).coerceAtLeast(12f)

        if (!oceanVisitor.active) {
            oceanVisitor.cooldownTimer -= clampedDt
            if (oceanVisitor.cooldownTimer <= 0f) {
                triggerOceanVisitor((simTime * 1000f).toLong() + unlockSeedCounter * 31L, snapshot.isNight)
            }
        } else {
            val sp = oceanVisitor.species
            var anyStillOnOrApproaching = false

            for (m in 0 until oceanVisitor.memberCount) {
                val mem = oceanVisitor.members[m]
                val spriteW = spanCols(sp.lengthFrac, mem.zDepth)
                mem.animPhase = (mem.animPhase + clampedDt * (if (sp.canBreach) 4.4f else 1.7f)) % 62.831855f
                mem.colX += mem.vx * clampedDt
                if (mem.vz != 0f) {
                    mem.zDepth = (mem.zDepth + mem.vz * clampedDt).coerceIn(0.18f, 0.90f)
                }

                if (sp.canBreach) {
                    highMotion = true
                    // Trigger parabolic leap above the ocean surface!
                    val crossedTrigger = if (oceanVisitor.vxDir > 0f) {
                        mem.colX >= mem.breachTriggerCol
                    } else {
                        mem.colX <= mem.breachTriggerCol
                    }
                    if (!mem.hasBreached && crossedTrigger) {
                        mem.hasBreached = true
                        mem.breachPhase = 0.01f
                        mem.vy = -22.5f // Graceful high parabolic dolphin leap well above the waves!
                    }

                    if (mem.breachPhase > 0f) {
                        mem.breachPhase += clampedDt
                        // Gravity arcs the dolphin smoothly into the sky and nose-first back into the ocean!
                        mem.vy += 18.5f * clampedDt
                        mem.rowY += mem.vy * clampedDt

                        val isAbove = mem.rowY < splitRow
                        if (isAbove && !mem.wasAboveWater) {
                            // Breaking out of the ocean surface with splash crown & ripples!
                            val splashCol = mem.colX + spriteW * 0.5f
                            injectSurfaceRipple(splashCol, 5.8f)
                            emitMouthBubble(splashCol, splitRow + 2f, mem.zDepth)
                            val spCrown = rainSplashes[nextSplashIdx]
                            nextSplashIdx = (nextSplashIdx + 1) % rainSplashes.size
                            spCrown.active = true; spCrown.colX = splashCol; spCrown.age = 0f; spCrown.maxAge = 0.55f
                        } else if (!isAbove && mem.wasAboveWater) {
                            // Splashing cleanly back into the ocean!
                            val splashCol = mem.colX + spriteW * 0.5f
                            injectSurfaceRipple(splashCol, 6.4f)
                            emitMouthBubble(splashCol, splitRow + 2.5f, mem.zDepth)
                            emitMouthBubble(mem.colX + spriteW * 0.35f, splitRow + 3.5f, mem.zDepth)
                            val spCrown = rainSplashes[nextSplashIdx]
                            nextSplashIdx = (nextSplashIdx + 1) % rainSplashes.size
                            spCrown.active = true; spCrown.colX = splashCol; spCrown.age = 0f; spCrown.maxAge = 0.62f
                        }
                        mem.wasAboveWater = isAbove

                        // Smooth re-leveling once back underwater
                        if (!isAbove && mem.vy > 0f && mem.rowY > splitRow + 6f) {
                            mem.vy *= exp(-4.5f * clampedDt)
                            if (abs(mem.vy) < 0.4f) {
                                mem.breachPhase = 0f
                            }
                        }
                    } else {
                        // Porpoising sine wave underwater near the surface
                        mem.vy = sin(simTime * 2.4f + m * 1.1f) * 2.0f
                        mem.rowY = (mem.rowY + mem.vy * clampedDt).coerceIn(minFishRow, (splitRow + 10f).coerceAtMost(maxFishRow - 2f))
                    }
                } else {
                    // Serene deep-ocean glide for whales, orcas, whale sharks & giant mantas
                    val gentleBob = sin(simTime * 0.75f + m * 1.4f) * 0.55f
                    mem.vy = gentleBob
                    mem.rowY = (mem.rowY + mem.vy * clampedDt).coerceIn(minFishRow + 2f, maxFishRow - 4f)

                    // Reliably trigger whale/orca surface blowhole water spout when crossing mid-window!
                    val centerCol = mem.colX + spriteW * 0.5f
                    if (sp.canSpout && !mem.hasBreached && oceanVisitor.spoutTimer <= 0f &&
                        centerCol in (cols * 0.32f)..(cols * 0.68f)
                    ) {
                        mem.hasBreached = true
                        oceanVisitor.spoutTimer = 2.4f
                        oceanVisitor.spoutCol = if (oceanVisitor.vxDir > 0f) mem.colX + spriteW * 0.80f else mem.colX + spriteW * 0.20f
                        injectSurfaceRipple(oceanVisitor.spoutCol, 3.6f)
                    }
                }

                // Smooth athletic pitch angle for breaching dolphins (up to 42 deg arch!) & gliding whales
                val maxPitch = if (sp.canBreach) 42f else 8f
                val desiredPitch = (atan2(mem.vy, abs(mem.vx).coerceAtLeast(3.0f)) * (180.0 / PI).toFloat() * oceanVisitor.vxDir)
                    .coerceIn(-maxPitch, maxPitch)
                mem.pitchDeg += (desiredPitch - mem.pitchDeg) * (1f - exp(-10.5f * clampedDt))

                val stillVisible = if (oceanVisitor.vxDir > 0f) mem.colX < cols + 10f else mem.colX > -spriteW - 10f
                if (stillVisible) {
                    anyStillOnOrApproaching = true
                    // At night, passing whales/dolphins/mantas stir subtle bioluminescent plankton wakes!
                    if (snapshot.isNight && mem.rowY > splitRow + 1f && (wavePhase + m) % 3 == 0 && sin(simTime * 8f + m) > 0.72f) {
                        val wakeX = if (oceanVisitor.vxDir > 0f) mem.colX + 1.5f else mem.colX + spriteW - 1.5f
                        spawnBioPlankton(wakeX, mem.rowY + (m - 1) * 0.8f, mem.zDepth, -mem.vx * 0.12f, -0.35f)
                    }
                }
            }

            if (!anyStillOnOrApproaching) {
                oceanVisitor.active = false
                oceanVisitor.cooldownTimer = 10f + (sin(simTime * 1.7f) * 0.5f + 0.5f) * 12f
            }
        }

        // =========================================================================
        // 8. BOTTOM-FEEDER CRITTERS
        // =========================================================================
        if (lastBloodSiteTimer > 0f) {
            lastBloodSiteTimer = (lastBloodSiteTimer - clampedDt).coerceAtLeast(0f)
        }

        if (config.enableBottomFeeders) {
            val activeFeeders = if (isBatterySaver) 3 else maxBottomFeeders
            val tiltSlopeStretch = (1.0f + parallaxY * 0.18f).coerceIn(0.82f, 1.22f)
            val minBedRow = (floorRow - 5.2f * tiltSlopeStretch).coerceAtLeast((splitRow + 10).toFloat())
            val maxBedRow = (floorRow - 0.35f)
            val bedSpan = (maxBedRow - minBedRow).coerceAtLeast(2.5f)

            for (b in 0 until activeFeeders) {
                val bf = bottomFeeders[b]
                bf.isSnappingAtPredator = false

                if (bf.zRoamTimer > 0f) {
                    bf.zRoamTimer = (bf.zRoamTimer - clampedDt).coerceAtLeast(0f)
                } else if (sin(simTime * 0.9f + bf.id * 2.7f) > 0.96f) {
                    bf.zRoamTimer = 2.4f
                    bf.zRoamTargetTier = ((bf.preferredTier + 2) % 5).toFloat()
                }
                val activeTier = if (bf.zRoamTimer > 0f) bf.zRoamTargetTier else bf.preferredTier.toFloat()
                val homeTierRow = (maxBedRow - (activeTier / 4.0f) * bedSpan).coerceIn(minBedRow, maxBedRow)

                bf.zDepth = (((maxBedRow - bf.rowY) / bedSpan) * 0.74f + 0.08f).coerceIn(0.06f, 0.86f)

                if (predator.active && predator.rowY > floorRow - 18 &&
                    abs(predator.colX + 5f - bf.colX) < 16f &&
                    abs(predator.zDepth - bf.zDepth) < 0.35f
                ) {
                    bf.isSnappingAtPredator = true
                    predator.rowY = (predator.rowY - 8.5f * clampedDt).coerceAtLeast((splitRow + 6).toFloat())
                }

                val attractCenterCol = when {
                    activeWaferCol > 0f -> activeWaferCol
                    lastBloodSiteTimer > 0f && lastBloodSiteCol > 0f -> lastBloodSiteCol
                    else -> -1f
                }

                val spriteLen = spanCols(bf.species.lengthFrac, bf.zDepth)
                val minC = 2.0f
                val maxC = (cols - spriteLen - 2.0f).coerceAtLeast(minC + 4f)

                val perspSpd = bf.species.speedColsPerSec * (1.12f - bf.zDepth * 0.45f)
                val zCrawlDamp = if (abs(bf.vy) > 0.45f) 0.35f else 1.0f
                var desiredVx = bf.vxDir * perspSpd * zCrawlDamp

                val hopBob = if (bf.species == BottomFeederSpecies.CHERRY_SHRIMP || bf.species == BottomFeederSpecies.CORY_CATFISH) {
                    sin(simTime * 2.8f + bf.id * 1.9f) * 0.55f
                } else {
                    0f
                }
                var desiredVy = (homeTierRow - bf.rowY) * 2.2f + hopBob

                if (attractCenterCol > 0f && !bf.isSnappingAtPredator) {
                    val ringSlotOffset = (b - 2) * 4.5f
                    val targetSlotCol = (attractCenterCol + ringSlotOffset).coerceIn(minC, maxC)
                    val targetWaferRow = (maxBedRow - activeWaferZ * bedSpan).coerceIn(minBedRow, maxBedRow)
                    desiredVy = (targetWaferRow - bf.rowY) * 2.5f
                    val dxSlot = targetSlotCol - bf.colX
                    if (abs(dxSlot) > 1.2f) {
                        bf.vxDir = if (dxSlot >= 0f) 1f else -1f
                        desiredVx = bf.vxDir * perspSpd * 1.45f
                    } else {
                        desiredVx = 0f
                        bf.animPhase = (bf.animPhase + clampedDt * 10f) % 6.2831855f
                    }
                }

                for (otherIdx in 0 until activeFeeders) {
                    if (b == otherIdx) continue
                    val other = bottomFeeders[otherIdx]
                    if (abs(bf.zDepth - other.zDepth) > 0.26f) continue
                    val otherLen = spanCols(other.species.lengthFrac, other.zDepth)

                    val centerA = bf.colX + spriteLen * 0.5f
                    val centerB = other.colX + otherLen * 0.5f
                    val dx = centerA - centerB
                    val dy = bf.rowY - other.rowY

                    val safeX = (spriteLen + otherLen) * 0.55f + 2.0f
                    val safeY = 1.25f

                    if (abs(dx) < safeX && abs(dy) < safeY) {
                        val pushSignY = if (bf.preferredTier > other.preferredTier) -1f else 1f
                        val overlapY = (safeY - abs(dy)) / safeY
                        desiredVy += pushSignY * overlapY * 4.5f

                        val pushSignX = if (dx >= 0f) 1f else -1f
                        desiredVx += pushSignX * ((safeX - abs(dx)) / safeX) * 1.6f
                    }
                }

                if (bf.vxDir > 0f && bf.colX >= maxC - 0.8f) {
                    bf.vxDir = -1f
                    desiredVx = -perspSpd
                } else if (bf.vxDir < 0f && bf.colX <= minC + 0.8f) {
                    bf.vxDir = 1f
                    desiredVx = perspSpd
                }

                val smoothRate = (1f - exp(-7.0f * clampedDt)).coerceIn(0.05f, 1.0f)
                bf.vx += (desiredVx - bf.vx) * smoothRate
                bf.vy += (desiredVy - bf.vy) * smoothRate

                if (!bf.isSnappingAtPredator) {
                    bf.colX = (bf.colX + bf.vx * clampedDt).coerceIn(minC, maxC)
                    bf.rowY = (bf.rowY + bf.vy * clampedDt).coerceIn(minBedRow, maxBedRow)
                }

                val moveSpd = hypot(bf.vx, bf.vy)
                bf.animPhase = (bf.animPhase + clampedDt * (2.2f + moveSpd * 1.6f)) % 6.2831855f
            }
        }

        // =========================================================================
        // 9. PREDATOR STALK, LUNGE, ELDER CROWN INTIMIDATION & JAW-SNAP CATCH
        // =========================================================================
        if (config.huntMode.isEnabled) {
            if (!predator.active) {
                predator.cooldownTimer -= clampedDt
                if (predator.cooldownTimer <= 0f) {
                    triggerPredatorVisit((simTime * 1000f).toLong(), config.huntMode.catchChance)
                }
            } else {
                highMotion = true
                predator.activeDuration += clampedDt
                if (predator.stalkTimer > 0f) {
                    predator.stalkTimer = (predator.stalkTimer - clampedDt).coerceAtLeast(0f)
                }
                val animSpeed = if (predator.stalkTimer > 0f) 3.8f else 8.2f
                predator.animPhase = (predator.animPhase + clampedDt * animSpeed) % 6.2831855f
                if (predator.chompTimer > 0f) {
                    predator.chompTimer = (predator.chompTimer - clampedDt).coerceAtLeast(0f)
                }

                var targetFish: AsciiFishAgent? = null
                var minDist3D = 999f
                if (!predator.hasCaughtThisVisit && !predator.isRetreating) {
                    for (i in 0 until totalMaxFish) {
                        val f = fish[i]
                        if (f.respawnTimer > 0f || f.offscreenTimer > 0f || f.isElderCrown) continue
                        if (!f.isSchooler && i >= activeHero) continue
                        if (f.isSchooler && (i - maxHeroFish) >= activeSchool) continue

                        val ahead = (f.colX - predator.colX) * predator.vxDir
                        if (predator.ambushStyle == 1 || ahead > -4f) {
                            val d2 = hypot(f.colX - predator.colX, (f.rowY - predator.rowY) * 1.3f)
                            val dz = abs(f.zDepth - predator.zDepth) * 24f
                            val d3 = hypot(d2, dz)
                            if (d3 < minDist3D) {
                                minDist3D = d3
                                targetFish = f
                            }
                        }
                    }
                }

                val stalkFactor = if (predator.stalkTimer > 0f) 0.32f else 1.0f
                val lungeMult = if (predator.stalkTimer <= 0f && targetFish != null && minDist3D < 14f && predator.willCatchThisVisit) 1.45f else 1.0f
                val perspSpdScale = 1.15f - predator.zDepth * 0.45f

                if (predator.ambushStyle == 1 && !predator.hasCaughtThisVisit && predator.zDepth <= 0.14f) {
                    predator.isRetreating = true
                }

                if (predator.isRetreating || (predator.hasCaughtThisVisit && predator.chompTimer <= 0f)) {
                    if (predator.ambushStyle == 1) {
                        predator.vz = 0.36f
                        predator.vx = predator.vxDir * predator.species.speedColsPerSec * 0.55f
                        predator.vy *= exp(-2.5f * clampedDt)
                    } else {
                        predator.vx = predator.vxDir * predator.species.speedColsPerSec * perspSpdScale * 1.1f
                        predator.vy *= exp(-2.5f * clampedDt)
                    }
                } else if (targetFish != null) {
                    val tx = targetFish.colX
                    val ty = targetFish.rowY
                    val tz = targetFish.zDepth

                    if (predator.ambushStyle == 1 && predator.zDepth > tz + 0.14f) {
                        val dx = (tx - predator.colX).coerceIn(-6f, 6f)
                        val dy = (ty - predator.rowY).coerceIn(-5f, 5f)
                        predator.vx = dx * 1.6f * stalkFactor
                        predator.vy = dy * 1.6f * stalkFactor
                        predator.vz = -0.38f * lungeMult * stalkFactor
                        if (dx != 0f) predator.vxDir = if (dx >= 0f) 1f else -1f
                    } else {
                        predator.vx = predator.vxDir * predator.species.speedColsPerSec * perspSpdScale * lungeMult * stalkFactor
                        val dy = (ty - predator.rowY).coerceIn(-5.5f, 5.5f)
                        predator.vy = dy * (if (lungeMult > 1f) 2.4f else 1.5f) * stalkFactor
                        val dz = (tz - predator.zDepth).coerceIn(-0.35f, 0.35f)
                        predator.vz = dz * 1.4f
                    }
                }

                predator.colX += predator.vx * clampedDt
                predator.rowY = (predator.rowY + predator.vy * clampedDt).coerceIn(minFishRow, maxFishRow)
                predator.zDepth = (predator.zDepth + predator.vz * clampedDt).coerceIn(0.08f, 0.96f)
                val targetPredPitch = (atan2(predator.vy, abs(predator.vx).coerceAtLeast(4f)) * (180.0 / PI).toFloat() * predator.vxDir).coerceIn(-12f, 12f)
                predator.pitchDeg += (targetPredPitch - predator.pitchDeg) * (1f - exp(-7f * clampedDt))

                if (predator.rowY < minFishRow + 3f && abs(predator.vx) > 4f) {
                    injectSurfaceRipple(predator.colX + 4f, 0.9f)
                }

                val predW = spanCols(predator.species.lengthFrac, predator.zDepth)
                val predatorMouthX = if (abs(predator.vz) > 0.22f) {
                    predator.colX + predW * 0.5f
                } else if (predator.vxDir >= 0f) {
                    predator.colX + predW - 2f
                } else {
                    predator.colX + 2f
                }

                if (!predator.hasCaughtThisVisit && !predator.isRetreating && predator.willCatchThisVisit && targetFish != null) {
                    val fishCenterX = targetFish.colX + spanCols(targetFish.species.lengthFrac, targetFish.zDepth) * 0.5f
                    val contactDist2D = hypot(fishCenterX - predatorMouthX, (targetFish.rowY - predator.rowY) * 1.2f)
                    val zDiff = abs(targetFish.zDepth - predator.zDepth)
                    if (contactDist2D < 3.8f && zDiff < 0.22f) {
                        predator.hasCaughtThisVisit = true
                        predator.chompTimer = 0.60f
                        targetFish.respawnTimer = 14f
                        spawnBloodPlume(fishCenterX, targetFish.rowY, targetFish.zDepth)
                        onHuntOutcomeListener?.invoke(
                            true,
                            predator.species.displayName,
                            targetFish.species.displayName
                        )
                    }
                }

                if ((predator.vxDir > 0f && predator.colX > cols + 8) ||
                    (predator.vxDir < 0f && predator.colX < -20f) ||
                    ((predator.hasCaughtThisVisit || predator.isRetreating) && predator.zDepth >= 0.94f) ||
                    predator.activeDuration > 11f
                ) {
                    if (!predator.hasCaughtThisVisit) {
                        onHuntOutcomeListener?.invoke(false, predator.species.displayName, "")
                    }
                    predator.active = false
                    predator.cooldownTimer = 18f + (cos(simTime) * 0.5f + 0.5f) * 18f
                }
            }
        } else {
            predator.active = false
        }

        // =========================================================================
        // 10. CONTINUOUS 3D YAW-STEERED OCEAN SWIM PHYSICS (OPEN-WINDOW FLOW!)
        // =========================================================================
        schoolTargetTimer -= clampedDt
        if (schoolTargetTimer <= 0f) {
            schoolTargetTimer = 4.5f + (sin(simTime * 1.1f) * 0.5f + 0.5f) * 4.0f
            if (schoolTargetX > cols * 0.82f) schoolFlowDir = -1f
            else if (schoolTargetX < cols * 0.18f) schoolFlowDir = 1f
            else if (sin(simTime * 0.43f) > 0.72f) schoolFlowDir = -schoolFlowDir
            schoolTargetX = (schoolTargetX + schoolFlowDir * (cols * 0.38f)).coerceIn(8f, (cols - 8).toFloat())
            schoolTargetY = minFishRow + underwaterSpan * (0.20f + (cos(simTime * 0.49f) * 0.5f + 0.5f) * 0.60f)
            // Wide 3D depth sweep so schooling baitfish emerge from the deep and weave across the glass!
            schoolTargetZ = 0.18f + (sin(simTime * 0.37f) * 0.5f + 0.5f) * 0.68f
        }

        for (pIdx in pellets.indices) {
            if (pellets[pIdx].active) {
                highMotion = true
                break
            }
        }

        val piF = PI.toFloat()

        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            if (!f.isSchooler && i >= activeHero) continue
            if (f.isSchooler && (i - maxHeroFish) >= activeSchool) continue

            val spriteW = spanCols(f.species.lengthFrac, f.zDepth)

            // Caught-prey respawn
            if (f.respawnTimer > 0f) {
                f.respawnTimer -= clampedDt
                if (f.respawnTimer <= 0f) {
                    f.respawnTimer = 0f
                    reenterOceanWindow(f, minFishRow, underwaterSpan, cols, rotateSpecies = true)
                } else {
                    continue
                }
            }

            // Natural off-screen ocean passage before re-entering the window (with fresh variety!)
            if (f.offscreenTimer > 0f) {
                f.offscreenTimer -= clampedDt
                if (f.offscreenTimer <= 0f) {
                    f.offscreenTimer = 0f
                    reenterOceanWindow(f, minFishRow, underwaterSpan, cols, rotateSpecies = !f.isSchooler && !f.isElderCrown)
                } else {
                    continue
                }
            }

            if (f.gulpTimer > 0f) f.gulpTimer = (f.gulpTimer - clampedDt).coerceAtLeast(0f)
            if (f.startleTimer > 0f) {
                f.startleTimer = (f.startleTimer - clampedDt).coerceAtLeast(0f)
                highMotion = true
            }

            // Slow organic depth & heading exploration (curvilinear 3D ocean wandering!)
            f.wanderTimer -= clampedDt
            if (f.wanderTimer <= 0f) {
                f.wanderTimer = 4.5f + (sin(simTime * 0.9f + f.id * 2.1f) * 0.5f + 0.5f) * 5.0f
                f.targetDepthFrac = (0.10f + (sin(simTime * 0.31f + f.id * 1.73f) * 0.5f + 0.5f) * 0.78f)
            }
            f.preferredDepthFrac += (f.targetDepthFrac - f.preferredDepthFrac) * (clampedDt * 0.22f).coerceAtMost(1f)

            // True 3D Open-Ocean Steering (includes deep-to-glass approaches, glass-to-deep dives, & graceful 3D curves!)
            f.steerTimer -= clampedDt
            if (f.steerTimer <= 0f && !f.isSchooler && !f.isEvading) {
                f.steerTimer = 3.2f + (cos(simTime * 0.8f + f.id * 2.9f) * 0.5f + 0.5f) * 4.6f
                val roll = sin(simTime * 1.37f + f.id * 4.1f)
                val inWindowCenter = f.colX > cols * 0.14f && f.colX < cols * 0.86f - spriteW
                when {
                    // Emerge out of the misty deep toward the glass OR dive back into the deep blue!
                    roll > 0.42f && inWindowCenter -> {
                        val approachBias = if (f.zDepth > 0.52f) -piF * 0.5f else piF * 0.5f
                        val diagNudge = cos(simTime * 1.1f + f.id * 2.3f) * 0.52f
                        f.targetYaw = wrapAngle(approachBias + diagNudge)
                    }
                    // Graceful wide-arc 3D U-turn inside the ocean window (curves smoothly through Z!)
                    roll < -0.58f && inWindowCenter -> {
                        val arcSign = if (f.zDepth > 0.50f) -1f else 1f
                        f.targetYaw = wrapAngle(f.yaw + arcSign * piF * 0.92f)
                    }
                    else -> {
                        // Expressive 3D diagonal ocean cruise (+/- 48 deg into or out of depth!)
                        val baseDirYaw = if (cos(f.yaw) >= 0f) 0f else piF
                        val zAngleBias = when {
                            f.zDepth < 0.20f -> 0.65f  // Curve deeper away from foreground glass
                            f.zDepth > 0.78f -> -0.65f // Curve closer out of deep ocean haze
                            else -> roll * 0.85f
                        }
                        f.targetYaw = wrapAngle(baseDirYaw + zAngleBias)
                    }
                }
            }

            // Soft foreground glass curve (fish bank gracefully parallel instead of hitting the glass!)
            if (f.zDepth < 0.12f && sin(f.targetYaw) < -0.15f) {
                f.targetYaw = if (cos(f.yaw) >= 0f) 0.32f else (piF - 0.32f)
            }

            // Burst-and-glide forward effort (gentle tail flick -> smooth glide)
            f.burstPhase = (f.burstPhase + clampedDt * (1.55f + (f.id % 3) * 0.18f)) % 62.831855f
            val wavePulse = 0.5f + 0.5f * sin(f.burstPhase)
            val burstMult = 0.66f + 0.52f * (wavePulse * wavePulse)

            // Occasional calm reef hover
            if (f.hoverTimer > 0f) {
                f.hoverTimer = (f.hoverTimer - clampedDt).coerceAtLeast(0f)
            } else if (!f.isSchooler && !f.isEvading && f.startleTimer <= 0f &&
                (f.species == AsciiFishSpecies.PUFFERFISH || f.species == AsciiFishSpecies.LIONFISH || f.species == AsciiFishSpecies.MANDARIN) &&
                sin(simTime * 0.5f + f.id * 4.3f) > 0.993f
            ) {
                f.hoverTimer = 1.8f
            }

            val perspSpeedFactor = 1.10f - f.zDepth * 0.42f
            val hoverScale = if (f.hoverTimer > 0f) 0.20f else 1.0f
            val startleBoost = if (f.startleTimer > 0f) 1.75f else 1.0f
            var forwardSpeed = f.species.baseSpeedColsPerSec * f.speedScale * perspSpeedFactor * burstMult * hoverScale * startleBoost

            val targetLaneRow = minFishRow + f.preferredDepthFrac * underwaterSpan
            var desiredVy = (targetLaneRow - f.rowY) * 0.95f + sin(simTime * 1.05f + f.id * 1.9f) * 0.55f

            // True 3D Boids Schooling (parting gracefully around passing whales/mantas, baitballing near Orcas!)
            if (f.isSchooler && !predator.active) {
                var sepX = 0f; var sepY = 0f
                var aliX = 0f; var aliY = 0f
                var cohX = 0f; var cohY = 0f; var cohZ = 0f
                var neighbors = 0
                val orcaNear = oceanVisitor.active && oceanVisitor.species == OceanVisitorSpecies.ORCA
                val cohesionWeight = if (orcaNear) 0.95f else 0.40f
                for (sIdx in maxHeroFish until (maxHeroFish + activeSchool).coerceAtMost(totalMaxFish)) {
                    if (sIdx == i) continue
                    val mate = fish[sIdx]
                    if (mate.respawnTimer > 0f || mate.offscreenTimer > 0f) continue
                    val dx = f.colX - mate.colX
                    val dy = f.rowY - mate.rowY
                    val dSq = dx * dx + dy * dy * 2.2f
                    if (dSq < 360f) {
                        neighbors++
                        aliX += mate.vx
                        aliY += mate.vy
                        cohX += mate.colX
                        cohY += mate.rowY
                        cohZ += mate.zDepth
                        val minSepSq = if (orcaNear) 14f else 26f
                        if (dSq < minSepSq && dSq > 0.01f) {
                            val inv = 1f / dSq
                            sepX += dx * inv * 16f
                            sepY += dy * inv * 12f
                        }
                    }
                }
                if (neighbors > 0) {
                    val invN = 1f / neighbors
                    val steerX = (aliX * invN) * 0.45f + (cohX * invN - f.colX) * cohesionWeight + (schoolTargetX - f.colX).coerceIn(-8f, 8f) * 0.55f + sepX
                    val steerZ = ((cohZ * invN * 0.6f + schoolTargetZ * 0.4f) - f.zDepth) * 14f
                    f.targetYaw = atan2(steerZ, steerX)
                    desiredVy = (aliY * invN) * 0.35f + (cohY * invN - f.rowY) * (if (orcaNear) 1.15f else 0.70f) + (schoolTargetY - f.rowY).coerceIn(-5f, 5f) * 0.45f + sepY
                }

                // Gracefully part like living curtains around any passing Ocean Giant!
                if (oceanVisitor.active) {
                    for (m in 0 until oceanVisitor.memberCount) {
                        val mem = oceanVisitor.members[m]
                        val vW = spanCols(oceanVisitor.species.lengthFrac, mem.zDepth)
                        val vCx = mem.colX + vW * 0.5f
                        val gdx = f.colX - vCx
                        val gdy = f.rowY - mem.rowY
                        val gDist = hypot(gdx, gdy * 1.4f)
                        val giantRadius = (vW * 0.65f + 7f)
                        if (gDist < giantRadius && abs(f.zDepth - mem.zDepth) < 0.36f) {
                            val partUrgency = (1f - gDist / giantRadius).coerceIn(0.2f, 1f)
                            val partSignY = if (gdy >= 0f) 1f else -1f
                            desiredVy += partSignY * (7.5f * partUrgency)
                            f.targetYaw = atan2((f.zDepth - mem.zDepth) * 14f, if (abs(gdx) < 1f) oceanVisitor.vxDir * 5f else gdx)
                            forwardSpeed *= (1f + 0.45f * partUrgency)
                        }
                    }
                }
            }

            // Clownfish naturally love hovering briefly near the foreground sea anemone in Ocean mode!
            if (isOceanBiome && f.species == AsciiFishSpecies.CLOWNFISH && f.hoverTimer <= 0f && !f.isEvading) {
                if (f.colX in 4f..16f && f.rowY > floorRow - 12f && sin(simTime * 0.8f + f.id) > 0.94f) {
                    f.hoverTimer = 2.0f
                }
            }

            // 3D Mouth-Targeted Food Pellet Seeking (steers yaw smoothly in X & Z!)
            val mouthX = if (f.facingDir >= 0) f.colX + spriteW - 0.5f else f.colX + 0.5f
            for (pIdx in pellets.indices) {
                val pel = pellets[pIdx]
                if (pel.active && !pel.isHeavyAlgaeWafer) {
                    val pdx = pel.colX - mouthX
                    val pdy = pel.rowY - f.rowY
                    val pdz = (pel.zDepth - f.zDepth) * 22f
                    val pDist = hypot(pdx, pdy)
                    if (pDist < 24f) {
                        f.hoverTimer = 0f
                        f.targetYaw = atan2(pdz, pdx)
                        forwardSpeed = f.species.baseSpeedColsPerSec * f.speedScale * perspSpeedFactor * 1.42f
                        desiredVy = (pdy / pDist.coerceAtLeast(1f)) * 4.4f
                        if (pDist < 2.8f && abs(f.zDepth - pel.zDepth) < 0.28f) {
                            pel.active = false
                            f.pelletsEaten++
                            f.gulpTimer = 0.45f
                            emitMouthBubble(mouthX, f.rowY - 0.5f, f.zDepth)
                            emitMouthBubble(mouthX + f.facingDir * 0.6f, f.rowY - 1.1f, f.zDepth)
                            if (f.pelletsEaten >= 3 && !f.isElderCrown) {
                                f.isElderCrown = true
                                onCrownLevelUpListener?.invoke(f.species.displayName)
                            }
                        }
                        break
                    }
                }
            }

            // Predator Evasion + Sympathetic Neighbor Panic
            val wasEvading = f.isEvading
            f.isEvading = false
            if (predator.active && !predator.hasCaughtThisVisit && !f.isElderCrown) {
                val pCenterX = predator.colX + 6f
                val dx = (f.colX + spriteW * 0.5f) - pCenterX
                val dy = f.rowY - predator.rowY
                val dist = hypot(dx, dy * 1.4f)
                val threatRadius = 27f

                var neighborPanicking = false
                if (dist in threatRadius..40f) {
                    for (nIdx in 0 until totalMaxFish) {
                        if (nIdx == i) continue
                        val nb = fish[nIdx]
                        if (nb.isEvading && abs(nb.colX - f.colX) < 13f && abs(nb.rowY - f.rowY) < 9f) {
                            neighborPanicking = true
                            break
                        }
                    }
                }

                if ((dist < threatRadius && abs(f.zDepth - predator.zDepth) < 0.44f) || neighborPanicking) {
                    f.isEvading = true
                    f.hoverTimer = 0f
                    val urgency = (1f - dist / (threatRadius + 10f)).coerceIn(0.25f, 1f)
                    val zFlee = (f.zDepth - predator.zDepth) * 18f
                    f.targetYaw = atan2(zFlee, if (abs(dx) < 1f) (if (f.id % 2 == 0) 6f else -6f) else dx)
                    forwardSpeed = (f.species.baseSpeedColsPerSec * f.speedScale * perspSpeedFactor) + 7.6f * urgency

                    val jukeSign = when {
                        f.rowY < minFishRow + 5f -> 1f
                        f.rowY > maxFishRow - 5f -> -1f
                        abs(dy) > 1.0f -> if (dy > 0f) 1f else -1f
                        f.id % 2 == 0 -> -1f
                        else -> 1f
                    }
                    desiredVy = jukeSign * (6.0f * urgency)
                    if (!wasEvading && f.rowY < minFishRow + 4f) {
                        injectSurfaceRipple(f.colX + spriteW * 0.5f, 1.8f)
                    }
                }
            }

            // Smooth Angular Steering (zero instantaneous flips!)
            val agilityMult = if (f.isEvading || f.startleTimer > 0f) 2.2f else 1.0f
            val angleDiff = wrapAngle(f.targetYaw - f.yaw)
            val maxTurn = f.species.turnAgilityRadPerSec * agilityMult * clampedDt
            f.yaw = wrapAngle(f.yaw + angleDiff.coerceIn(-maxTurn, maxTurn))

            val cosYaw = cos(f.yaw)
            val sinYaw = sin(f.yaw)
            if (abs(cosYaw) > 0.12f) {
                f.facingDir = if (cosYaw >= 0f) 1 else -1
            }

            var desiredVx = cosYaw * forwardSpeed
            val desiredVz = sinYaw * forwardSpeed * 0.088f

            // Gentle 3D Fish-to-Fish Separation
            for (j in 0 until totalMaxFish) {
                if (i == j) continue
                val other = fish[j]
                if (other.respawnTimer > 0f || other.offscreenTimer > 0f) continue
                if (!other.isSchooler && j >= activeHero) continue
                if (other.isSchooler && (j - maxHeroFish) >= activeSchool) continue
                if (abs(f.zDepth - other.zDepth) > 0.22f) continue

                val otherW = spanCols(other.species.lengthFrac, other.zDepth)
                val dx = (f.colX + spriteW * 0.5f) - (other.colX + otherW * 0.5f)
                val dy = f.rowY - other.rowY

                val safeX = (spriteW + otherW) * 0.52f + 2.2f
                val fH = spanRows(f.species.lengthFrac, f.species.aspectRatio, f.zDepth)
                val oH = spanRows(other.species.lengthFrac, other.species.aspectRatio, other.zDepth)
                val safeY = (fH + oH) * 0.58f + 1.4f

                if (abs(dx) < safeX && abs(dy) < safeY) {
                    val normY = (safeY - abs(dy)) / safeY
                    val pushSignY = if (dy > 0.05f) 1f else if (dy < -0.05f) -1f else if (f.id > other.id) 1f else -1f
                    desiredVy += pushSignY * normY * 4.2f
                    val normX = (safeX - abs(dx)) / safeX
                    desiredVx += (if (dx >= 0f) 1f else -1f) * normX * 1.4f
                }
            }

            val accelRate = (1f - exp(-4.8f * clampedDt)).coerceIn(0.04f, 1.0f)
            f.vx += (desiredVx - f.vx) * accelRate
            f.vy += (desiredVy - f.vy) * accelRate
            f.vz += (desiredVz - f.vz) * accelRate

            f.colX += f.vx * clampedDt
            f.rowY = (f.rowY + f.vy * clampedDt).coerceIn(minFishRow, maxFishRow)
            f.zDepth = (f.zDepth + f.vz * clampedDt).coerceIn(0.08f, 0.96f)
            f.depthLayer = when {
                f.zDepth > 0.64f -> 0
                f.zDepth > 0.32f -> 1
                else -> 2
            }

            // Smooth organic nose pitch banking (-11..+11 deg)
            val desiredPitch = (atan2(f.vy, abs(f.vx).coerceAtLeast(3.5f)) * (180.0 / PI).toFloat() * f.facingDir)
                .coerceIn(-11f, 11f)
            f.pitchDeg += (desiredPitch - f.pitchDeg) * (1f - exp(-6.5f * clampedDt))

            // Open-Ocean Window Exit: glide past left/right edges OR vanish smoothly into the far deep abyss!
            val offRight = f.colX > cols + 5f && f.vx > 0f
            val offLeft = f.colX < -spriteW - 5f && f.vx < 0f
            val vanishedIntoDeep = !f.isSchooler && f.zDepth >= 0.95f && f.vz > 0.05f
            if (offRight || offLeft || vanishedIntoDeep) {
                if (f.isSchooler) {
                    f.targetYaw = if (offRight) piF else 0f
                    f.colX = f.colX.coerceIn(-spriteW - 4f, cols + 4f)
                } else {
                    f.offscreenTimer = 0.8f + (sin(simTime * 1.7f + f.id * 2.3f) * 0.5f + 0.5f) * 2.4f
                }
            }

            f.colFrac = (f.colX / cols.toFloat()).coerceIn(0.02f, 0.94f)

            if (f.rowY < minFishRow + 1.4f && abs(f.vx) > 2.5f && (wavePhase + f.id) % 4 == 0) {
                injectSurfaceRipple(f.colX + spriteW * 0.5f, 0.45f)
            }

            val actualSpd = hypot(f.vx, f.vy)
            val gulpBoost = if (f.gulpTimer > 0f) 5.5f else 0f
            f.animPhase = (f.animPhase + clampedDt * (2.2f + actualSpd * 0.48f + gulpBoost)) % 62.831855f
        }

        // Update Rare Mythic Legendary Creature physics before unified Z-sort
        if (legendary.active) {
            legendary.animPhase = (legendary.animPhase + clampedDt * 3.8f) % 62.831855f
            legendary.colX += legendary.vxDir * legendary.species.speedColsPerSec * clampedDt
            legendary.rowY = (legendary.rowY + sin(simTime * 1.1f) * 0.9f * clampedDt).coerceIn(minFishRow + 2f, maxFishRow - 2f)

            val legWidth = spanCols(legendary.species.lengthFrac, legendary.zDepth)
            if (legendary.vxDir > 0f && legendary.colX > cols + 6f) {
                legendary.vxDir = -1f
            } else if (legendary.vxDir < 0f && legendary.colX < -legWidth - 6f) {
                legendary.vxDir = 1f
            }
        }

        // =========================================================================
        // 11. UNIFIED BACK-TO-FRONT 3D PAINTER'S ALGORITHM DRAW PASS
        //     Sorts ALL creatures (Fish, Ocean Giants, Bottom Feeders, Jellyfish,
        //     Legendary, Predator) by true zDepth so closer creatures 100% cleanly
        //     occlude any part of deeper creatures behind them!
        // =========================================================================
        var drawCount = 0
        fun enqueueDrawable(kind: Int, idx: Int, z: Float) {
            if (drawCount < maxDrawables) {
                drawKind[drawCount] = kind
                drawIdx[drawCount] = idx
                drawZ[drawCount] = z
                drawCount++
            }
        }

        // Kind 0: Fish (heroes + schooling baitfish)
        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            if (f.respawnTimer > 0f || f.offscreenTimer > 0f) continue
            if (!f.isSchooler && i >= activeHero) continue
            if (f.isSchooler && (i - maxHeroFish) >= activeSchool) continue
            val effectiveZ = if (f.isElderCrown) (f.zDepth * 0.55f) else f.zDepth
            enqueueDrawable(0, i, effectiveZ)
        }
        // Kind 1: Ocean Megafauna Visitors (whales, orcas, dolphins, mantas, whale sharks, giant squid)
        if (oceanVisitor.active) {
            for (m in 0 until oceanVisitor.memberCount) {
                enqueueDrawable(1, m, oceanVisitor.members[m].zDepth)
            }
        }
        // Kind 2: Bottom-Feeder Critters (reef crab, cleaner shrimp, sand goby, sea conch)
        if (config.enableBottomFeeders) {
            val activeFeeders = if (isBatterySaver) 3 else maxBottomFeeders
            for (b in 0 until activeFeeders) {
                enqueueDrawable(2, b, bottomFeeders[b].zDepth)
            }
        }
        // Kind 3: Nighttime Bioluminescent Moon Jellyfish
        if (snapshot.isNight) {
            for (j in nightJellies.indices) {
                enqueueDrawable(3, j, nightJellies[j].zDepth)
            }
        }
        // Kind 4: Mythic Legendary
        if (legendary.active) {
            enqueueDrawable(4, 0, legendary.zDepth)
        }
        // Kind 5: Active Predator
        if (predator.active) {
            enqueueDrawable(5, 0, predator.zDepth)
        }
        // Kind 6: 3D Depth-Layered Reactive Kelp Stalks (fish weave behind close kelp & in front of deep kelp!)
        for (kIdx in 0 until kelpCount) {
            enqueueDrawable(6, kIdx, kelpStalks[kIdx].zDepth)
        }
        // Kind 7: Ocean Biome Foreground Coral & Swaying Sea Anemone Outcrops (z=0.24 left, z=0.28 right)
        if (isOceanBiome) {
            enqueueDrawable(7, 0, 0.24f)
            enqueueDrawable(7, 1, 0.28f)
        }

        // Insertion sort descending by zDepth (farthest/deepest draws first -> closest draws last on top!)
        for (i in 1 until drawCount) {
            val kKind = drawKind[i]
            val kIdx = drawIdx[i]
            val kZ = drawZ[i]
            var j = i - 1
            while (j >= 0 && drawZ[j] < kZ) {
                drawKind[j + 1] = drawKind[j]
                drawIdx[j + 1] = drawIdx[j]
                drawZ[j + 1] = drawZ[j]
                j--
            }
            drawKind[j + 1] = kKind
            drawIdx[j + 1] = kIdx
            drawZ[j + 1] = kZ
        }

        var drewMidgroundCaustics = false

        for (d in 0 until drawCount) {
            val itemKind = drawKind[d]
            val itemIdx = drawIdx[d]
            val itemZ = drawZ[d]

            if (!drewMidgroundCaustics && itemZ <= 0.35f) {
                drewMidgroundCaustics = true
                if (!snapshot.isNight && config.waterClarity > 0.55f) {
                    textPaint.color = pal.sunRayFg
                    for (rayIdx in 0..2) {
                        val baseCol = cols * (0.22f + rayIdx * 0.26f) + sin(simTime * 0.5f + rayIdx) * 2.5f + pShiftCols * 0.65f
                        for (step in 1..5) {
                            val rFloat = splitRow + step * 6.5f + sin(simTime * 1.8f + step) * 0.6f
                            val cFloat = baseCol + step * 3.2f
                            if (rFloat < floorRow - 6) {
                                val ch = if ((step + wavePhase) % 2 == 0) '+' else '/'
                                drawCharZeroAlloc(canvas, ch, cFloat * cellWidthPx, rFloat * cellHeightPx + baselineOffsetPx, textPaint)
                            }
                        }
                    }
                }
            }

            when (itemKind) {
                0 -> {
                    val f = fish[itemIdx]
                    val effectiveZ = itemZ
                    val parallaxMult = 1.05f - effectiveZ * 0.88f
                    val cosYaw = cos(f.yaw)
                    val sinYaw = sin(f.yaw)

                    val sw = spanCols(f.species.lengthFrac, effectiveZ)
                    val drawCol = f.colX + pShiftCols * parallaxMult
                    val parallaxDrawRow = (f.rowY + pShiftRowsFloat * parallaxMult)
                        .coerceIn(minFishRow, maxFishRow)

                    if (drawCol + sw < -2f || drawCol > cols + 2f) continue

                    // 1. Cast Sculpted Soft Seabed Drop-Shadow onto the Sloped Floor
                    val distFromFloor = floorRow - f.rowY
                    if (distFromFloor in 2.0f..46.0f) {
                        val shadowRowFloat = (floorRow - 0.4f - effectiveZ * 3.6f).coerceIn((floorRow - 4.4f), (floorRow - 0.3f))
                        val sunOffsetCols = (distFromFloor * 0.14f)
                        val shadowCx = (drawCol + sw * 0.5f + sunOffsetCols) * cellWidthPx
                        val shadowHalfW = creatureLengthPx(f.species.lengthFrac, effectiveZ) * 0.42f * (0.55f + 0.45f * abs(cosYaw))
                        val shadowMix = (1f - effectiveZ * 0.55f) * (1f - distFromFloor / 58.0f).coerceIn(0.25f, 1f)
                        sceneRenderer.drawCreatureShadow(
                            canvas, shadowCx, shadowRowFloat * cellHeightPx, shadowHalfW,
                            pal.shadowTint, pal.waterBgDeep, shadowMix, isTrueBlackOled
                        )
                    }

                    // 2. Soft Shimmering Underside Meniscus Reflection when swimming near the surface
                    val distFromSurface = f.rowY - splitRow
                    if (distFromSurface in 2.0f..18.0f) {
                        val reflectRowFloat = splitRow + 0.9f + (distFromSurface * 0.08f)
                        val reflectCx = (drawCol + sw * 0.5f) * cellWidthPx
                        val reflectHalfW = creatureLengthPx(f.species.lengthFrac, effectiveZ) * 0.38f
                        val reflectAlphaMix = (1f - distFromSurface / 18.0f) * (1f - effectiveZ * 0.5f) * 0.65f
                        sceneRenderer.drawCreatureSurfaceReflection(
                            canvas, reflectCx, reflectRowFloat * cellHeightPx, reflectHalfW,
                            resolveRoleColor(f.species.primaryRole), pal.waterBgTop, reflectAlphaMix, simTime, isTrueBlackOled
                        )
                    }

                    // 3. Diagonal sunbeam shaft highlight check
                    var inBeam = false
                    if (!snapshot.isNight && config.waterClarity > 0.5f) {
                        val dRow = f.rowY - splitRow
                        for (rayIdx in 0..2) {
                            val beamColAtDepth = cols * (0.18f + rayIdx * 0.28f) + (dRow * 0.5f)
                            if (abs((drawCol + sw * 0.5f) - beamColAtDepth) < 4.5f) {
                                inBeam = true
                                break
                            }
                        }
                    }

                    drawMarineCreature(
                        colFloat = drawCol,
                        rowFloat = parallaxDrawRow,
                        lengthFrac = f.species.lengthFrac,
                        aspectRatio = f.species.aspectRatio,
                        plan = f.species.bodyPlan,
                        marking = f.species.marking,
                        bodyColor = resolveRoleColor(f.species.primaryRole),
                        accentColor = if (f.isElderCrown) pal.fishGold else resolveRoleColor(f.species.finAccentRole),
                        eyeColor = pal.fishFoam,
                        zDepth = effectiveZ,
                        inSunbeamShaft = inBeam,
                        facingRight = cosYaw >= 0f,
                        cosYaw = cosYaw,
                        sinYaw = sinYaw,
                        pitchDeg = f.pitchDeg,
                        animPhase = f.animPhase
                    )

                    if (f.isEvading || f.startleTimer > 0f) {
                        val wakeCol = if (f.facingDir >= 0) drawCol - 1.5f else drawCol + sw + 0.5f
                        textPaint.color = pal.bubbleFg
                        drawCharZeroAlloc(canvas, '~', wakeCol * cellWidthPx, parallaxDrawRow * cellHeightPx + baselineOffsetPx, textPaint)
                    }
                }
                1 -> {
                    val sp = oceanVisitor.species
                    val mem = oceanVisitor.members[itemIdx]
                    val vFacingRight = oceanVisitor.vxDir >= 0f
                    val vParallax = 1.0f - mem.zDepth * 0.85f
                    val vDrawCol = mem.colX + pShiftCols * vParallax
                    val vDrawRow = mem.rowY + pShiftRowsFloat * vParallax
                    val vSw = spanCols(sp.lengthFrac, mem.zDepth)

                    // Majestic sweeping seabed shadow cast by Whales, Orcas, Whale Sharks, Mantas & Dolphins!
                    if (mem.rowY > splitRow) {
                        val vDistFloor = (floorRow - mem.rowY).coerceAtLeast(2f)
                        val vShadowRow = (floorRow - 0.5f - mem.zDepth * 3.4f).coerceIn(floorRow - 4.2f, floorRow - 0.4f)
                        val vShadowCx = (vDrawCol + vSw * 0.5f + vDistFloor * 0.14f) * cellWidthPx
                        val vShadowHalfW = creatureLengthPx(sp.lengthFrac, mem.zDepth) * 0.46f
                        val vShadowMix = (1f - mem.zDepth * 0.45f) * 0.82f
                        sceneRenderer.drawCreatureShadow(
                            canvas, vShadowCx, vShadowRow * cellHeightPx, vShadowHalfW,
                            pal.shadowTint, pal.waterBgDeep, vShadowMix, isTrueBlackOled
                        )
                        val vDistSurf = mem.rowY - splitRow
                        if (vDistSurf in 2.0f..18.0f) {
                            sceneRenderer.drawCreatureSurfaceReflection(
                                canvas, (vDrawCol + vSw * 0.5f) * cellWidthPx,
                                (splitRow + 0.9f + vDistSurf * 0.08f) * cellHeightPx,
                                creatureLengthPx(sp.lengthFrac, mem.zDepth) * 0.40f,
                                resolveRoleColor(sp.bodyRole), pal.waterBgTop,
                                (1f - vDistSurf / 18.0f) * (1f - mem.zDepth * 0.45f) * 0.55f,
                                simTime, isTrueBlackOled
                            )
                        }
                    }

                    // Subtle 3D yaw angle when drifting gently closer/deeper
                    val vSinYaw = (mem.vz * 12f).coerceIn(-0.45f, 0.45f)
                    val vCosYaw = if (vFacingRight) 0.92f else -0.92f
                    drawMarineCreature(
                        colFloat = vDrawCol,
                        rowFloat = vDrawRow,
                        lengthFrac = sp.lengthFrac,
                        aspectRatio = sp.aspectRatio,
                        plan = sp.bodyPlan,
                        marking = sp.marking,
                        bodyColor = resolveRoleColor(sp.bodyRole),
                        accentColor = resolveRoleColor(sp.accentRole),
                        eyeColor = pal.fishFoam,
                        zDepth = mem.zDepth,
                        facingRight = vFacingRight,
                        cosYaw = vCosYaw,
                        sinYaw = vSinYaw,
                        pitchDeg = mem.pitchDeg,
                        animPhase = mem.animPhase,
                        allowAboveWater = sp.canBreach
                    )
                }
                2 -> {
                    val bf = bottomFeeders[itemIdx]
                    val bfParallax = 0.85f - bf.zDepth * 0.60f
                    val bfW = spanCols(bf.species.lengthFrac, bf.zDepth)
                    val drawCol = (bf.colX + pShiftCols * bfParallax).coerceIn(1f, (cols - bfW - 1f).coerceAtLeast(2f))
                    val bfFacingRight = bf.vxDir >= 0f
                    val bfBodyCol = if (bf.isSnappingAtPredator) pal.fishGold else resolveRoleColor(bf.species.colorRole)
                    val bfAccCol = resolveRoleColor(bf.species.accentRole)
                    val bfSinYaw = when {
                        bf.vy > 0.55f -> -0.65f
                        bf.vy < -0.55f -> 0.65f
                        else -> 0f
                    }
                    val bfCosYaw = if (abs(bfSinYaw) > 0.3f) {
                        if (bfFacingRight) 0.72f else -0.72f
                    } else {
                        if (bfFacingRight) 1f else -1f
                    }
                    drawMarineCreature(
                        colFloat = drawCol,
                        rowFloat = bf.rowY,
                        lengthFrac = bf.species.lengthFrac,
                        aspectRatio = bf.species.aspectRatio,
                        plan = bf.species.bodyPlan,
                        marking = bf.species.marking,
                        bodyColor = bfBodyCol,
                        accentColor = bfAccCol,
                        eyeColor = pal.fishFoam,
                        zDepth = bf.zDepth,
                        facingRight = bfFacingRight,
                        cosYaw = bfCosYaw,
                        sinYaw = bfSinYaw,
                        pitchDeg = 0f,
                        animPhase = bf.animPhase,
                        isChomping = bf.isSnappingAtPredator
                    )
                }
                3 -> {
                    val jel = nightJellies[itemIdx]
                    val jelParallax = 0.85f - jel.zDepth * 0.65f
                    drawMarineCreature(
                        colFloat = jel.colX + pShiftCols * jelParallax,
                        rowFloat = (jel.rowY + pShiftRowsFloat * jelParallax).coerceIn(splitRow + 3f, floorRow - 4f),
                        lengthFrac = 0.052f,
                        aspectRatio = 0.85f,
                        plan = BodyPlan.JELLYFISH,
                        marking = MarkingStyle.NONE,
                        bodyColor = pal.fishAqua,
                        accentColor = pal.fishRose,
                        eyeColor = pal.fishFoam,
                        zDepth = jel.zDepth,
                        animPhase = jel.phase
                    )
                }
                4 -> {
                    val legWidth = spanCols(legendary.species.lengthFrac, legendary.zDepth)
                    val legParallax = 1.05f - legendary.zDepth * 0.88f
                    val legDrawCol = legendary.colX + pShiftCols * legParallax
                    val legDrawRow = (legendary.rowY + pShiftRowsFloat * legParallax).coerceIn(minFishRow, maxFishRow)
                    val legFacingRight = legendary.vxDir >= 0f

                    val legDistFloor = floorRow - legendary.rowY
                    if (legDistFloor in 2.0f..56.0f) {
                        val legShadowRow = (floorRow - 0.5f - legendary.zDepth * 3.6f).coerceIn(floorRow - 4.4f, floorRow - 0.4f)
                        val legShadowCx = (legDrawCol + legWidth * 0.5f + legDistFloor * 0.14f) * cellWidthPx
                        sceneRenderer.drawCreatureShadow(
                            canvas, legShadowCx, legShadowRow * cellHeightPx,
                            creatureLengthPx(legendary.species.lengthFrac, legendary.zDepth) * 0.44f,
                            pal.shadowTint, pal.waterBgDeep, (1f - legendary.zDepth * 0.50f) * 0.78f, isTrueBlackOled
                        )
                    }

                    drawMarineCreature(
                        colFloat = legDrawCol,
                        rowFloat = legDrawRow,
                        lengthFrac = legendary.species.lengthFrac,
                        aspectRatio = legendary.species.aspectRatio,
                        plan = legendary.species.bodyPlan,
                        marking = legendary.species.marking,
                        bodyColor = pal.fishGold,
                        accentColor = pal.fishCoral,
                        eyeColor = pal.fishFoam,
                        zDepth = legendary.zDepth,
                        inSunbeamShaft = true,
                        facingRight = legFacingRight,
                        cosYaw = if (legFacingRight) 1f else -1f,
                        sinYaw = 0f,
                        animPhase = legendary.animPhase
                    )

                    val trailCol = if (legFacingRight) legDrawCol - 2.0f else legDrawCol + legWidth + 1.0f
                    textPaint.color = pal.fishGold
                    drawCharZeroAlloc(canvas, if (wavePhase % 2 == 0) '*' else '·', trailCol * cellWidthPx, legDrawRow * cellHeightPx + baselineOffsetPx, textPaint)
                }
                5 -> {
                    val predParallax = 0.85f - predator.zDepth * 0.65f
                    val predDrawCol = predator.colX + pShiftCols * predParallax
                    val predDrawRow = (predator.rowY + pShiftRowsFloat * predParallax).coerceIn(minFishRow, maxFishRow)

                    val pDistFloor = floorRow - predator.rowY
                    if (pDistFloor in 2.0f..52.0f) {
                        val pSw = spanCols(predator.species.lengthFrac, predator.zDepth)
                        val pShadowRow = (floorRow - 0.5f - predator.zDepth * 3.6f).coerceIn((floorRow - 4.4f), (floorRow - 0.4f))
                        val pShadowCx = (predDrawCol + pSw * 0.5f + pDistFloor * 0.14f) * cellWidthPx
                        sceneRenderer.drawCreatureShadow(
                            canvas, pShadowCx, pShadowRow * cellHeightPx,
                            creatureLengthPx(predator.species.lengthFrac, predator.zDepth) * 0.44f,
                            pal.shadowTint, pal.waterBgDeep, (1f - predator.zDepth * 0.52f) * 0.80f, isTrueBlackOled
                        )
                    }

                    val predBodyColor = when {
                        predator.chompTimer > 0f -> pal.bloodCore
                        predator.stalkTimer > 0f -> TimeOfDayPalette.lerpColor(pal.fishDimBg, pal.fishCoral, 0.35f)
                        else -> pal.fishCoral
                    }
                    val predFacingRight = predator.vxDir >= 0f
                    val predHeadingCos = when {
                        abs(predator.vz) > 0.22f -> if (predFacingRight) 0.25f else -0.25f
                        predFacingRight -> 1f
                        else -> -1f
                    }
                    val predHeadingSin = when {
                        predator.vz < -0.20f -> -0.95f
                        predator.vz > 0.20f -> 0.95f
                        else -> 0f
                    }
                    drawMarineCreature(
                        colFloat = predDrawCol,
                        rowFloat = predDrawRow,
                        lengthFrac = predator.species.lengthFrac,
                        aspectRatio = predator.species.aspectRatio,
                        plan = predator.species.bodyPlan,
                        marking = predator.species.marking,
                        bodyColor = predBodyColor,
                        accentColor = pal.fishGold,
                        eyeColor = pal.fishFoam,
                        zDepth = predator.zDepth,
                        facingRight = predFacingRight,
                        cosYaw = predHeadingCos,
                        sinYaw = predHeadingSin,
                        pitchDeg = if (abs(predator.vz) > 0.20f) 0f else predator.pitchDeg,
                        animPhase = predator.animPhase,
                        isChomping = predator.chompTimer > 0f
                    )
                }
                6 -> {
                    // 3D Depth-Layered Reactive Kelp Ribbon
                    val ks = kelpStalks[itemIdx]
                    val baseCol = ks.colFrac * cols
                    val rootRow = (floorRow - 1 - (ks.zDepth * 3.5f).roundToInt()).coerceIn(splitRow + 10, floorRow - 1)
                    val kParallax = 0.85f - ks.zDepth * 0.62f
                    val kelpFade = (ks.zDepth * 0.58f + (if (calmScrim) 0.22f else 0f)).coerceAtMost(0.84f)
                    val kColor = TimeOfDayPalette.lerpColor(pal.kelpFg, pal.waterBgDeep, kelpFade)
                    val maxH = ks.heightRows.coerceAtMost((rootRow - splitRow - 4).coerceAtLeast(3))
                    sceneRenderer.drawKelpRibbon(
                        canvas, textPaint,
                        (baseCol + pShiftCols * kParallax) * cellWidthPx,
                        rootRow * cellHeightPx + baselineOffsetPx * 0.3f,
                        maxH * cellHeightPx,
                        cellWidthPx, simTime, ks.phase, ks.bendOffset,
                        kColor, pal.waterBgDeep, isBatterySaver, isTrueBlackOled
                    )
                }
                7 -> {
                    // 3D Depth-Layered Foreground Coral Reef & Swaying Sea Anemone
                    val coralColor = TimeOfDayPalette.lerpColor(pal.fishCoral, pal.waterBgDeep, if (calmScrim) 0.48f else 0.22f)
                    if (itemIdx == 0) {
                        val anemColor = TimeOfDayPalette.lerpColor(pal.fishRose, pal.waterBgDeep, if (calmScrim) 0.44f else 0.20f)
                        val leftCoralX = (5f + pShiftCols * 0.72f).coerceIn(3f, 12f) * cellWidthPx
                        sceneRenderer.drawCoralCluster(canvas, textPaint, leftCoralX, floorLinePx, refPx * 0.078f, coralColor, pal.waterBgDeep, isTrueBlackOled)
                        sceneRenderer.drawSeaAnemone(
                            canvas, leftCoralX + refPx * 0.078f, floorLinePx, refPx * 0.056f,
                            simTime, oceanCurrent, anemColor, pal.fishGold, pal.waterBgDeep, isTrueBlackOled
                        )
                    } else {
                        val rightCoralX = (cols - 8f + pShiftCols * 0.72f).coerceIn(cols - 13f, cols - 4f) * cellWidthPx
                        sceneRenderer.drawCoralCluster(canvas, textPaint, rightCoralX, floorLinePx, refPx * 0.070f, coralColor, pal.waterBgDeep, isTrueBlackOled)
                    }
                }
            }
        }

        // Blood Plume & Sinking Scales
        val scaleDrag = exp(-2.4f * clampedDt)
        val mistDrag = exp(-4.8f * clampedDt)
        for (idx in bloodParticles.indices) {
            val bp = bloodParticles[idx]
            if (!bp.active) continue
            highMotion = true
            bp.age += clampedDt
            if (bp.age >= bp.maxAge) {
                bp.active = false
                continue
            }
            val drag = if (bp.isScale) scaleDrag else mistDrag
            bp.vx *= drag
            if (!bp.isScale) {
                bp.vy *= drag
            }
            bp.colX += (bp.vx + sin(simTime * 2.5f + idx) * 0.25f) * clampedDt
            bp.rowY = (bp.rowY + bp.vy * clampedDt).coerceIn(minFishRow - 2f, (floorRow - 1).toFloat())
            bp.zDepth = (bp.zDepth + bp.vz * clampedDt).coerceIn(0.06f, 0.92f)

            val lifeFrac = (bp.age / bp.maxAge).coerceIn(0f, 1f)
            val glyph = if (bp.isScale) {
                if ((idx + (bp.age * 6f).toInt()) % 2 == 0) '`' else ','
            } else {
                when {
                    lifeFrac < 0.20f -> if (idx % 3 == 0) '@' else if (idx % 2 == 0) '#' else '%'
                    lifeFrac < 0.55f -> if (idx % 2 == 0) '*' else ':'
                    lifeFrac < 0.80f -> if (idx % 2 == 0) '~' else '.'
                    else -> '·'
                }
            }

            val rawCol = when {
                bp.isScale -> pal.fishGold
                lifeFrac < 0.45f -> pal.bloodCore
                else -> pal.bloodMist
            }
            val bpScale = 1.15f - bp.zDepth * 0.52f
            textPaint.textSize = baseTextSizePx * bpScale
            textPaint.color = TimeOfDayPalette.lerpColor(rawCol, pal.waterBgDeep, bp.zDepth * 0.60f)

            val bpParallax = 0.85f - bp.zDepth * 0.65f
            val bx = (bp.colX + pShiftCols * bpParallax) * cellWidthPx
            val by = (bp.rowY + pShiftRowsFloat * bpParallax) * cellHeightPx + baselineOffsetPx
            drawCharZeroAlloc(canvas, glyph, bx, by, textPaint)
        }
        textPaint.textSize = baseTextSizePx

        // =========================================================================
        // 11B. NIGHT BIOLUMINESCENT PLANKTON WAKES (Finger Drags & Passing Giants!)
        // =========================================================================
        val bioDrag = exp(-3.2f * clampedDt)
        val maxBio = if (isBatterySaver) 12 else bioPlankton.size
        for (pIdx in 0 until maxBio) {
            val bp = bioPlankton[pIdx]
            if (!bp.active) continue
            highMotion = true
            bp.age += clampedDt
            if (bp.age >= bp.maxAge) {
                bp.active = false
                continue
            }
            bp.vx *= bioDrag
            bp.colX += (bp.vx + oceanCurrent * 0.35f) * clampedDt
            bp.rowY = (bp.rowY + bp.vy * clampedDt).coerceIn(splitRow + 1.2f, floorRow - 1.5f)
            val life = (bp.age / bp.maxAge).coerceIn(0f, 1f)
            val ch = when {
                life < 0.30f -> '*'
                life < 0.65f -> '°'
                else -> '·'
            }
            val glowColor = TimeOfDayPalette.lerpColor(pal.fishAqua, pal.waterBgDeep, life * 0.78f)
            val bParallax = 0.90f - bp.zDepth * 0.65f
            textPaint.color = glowColor
            drawCharZeroAlloc(
                canvas,
                ch,
                (bp.colX + pShiftCols * bParallax) * cellWidthPx,
                (bp.rowY + pShiftRowsFloat * bParallax) * cellHeightPx + baselineOffsetPx,
                textPaint
            )
        }

        // =========================================================================
        // 12. EXTREME FOREGROUND GLASS-EDGE BOKEH BUBBLES
        // =========================================================================
        textPaint.textSize = baseTextSizePx * 1.42f
        val bokehColor = TimeOfDayPalette.lerpColor(pal.waterBgTop, pal.bubbleFg, 0.55f)
        textPaint.color = bokehColor
        for (i in 0 until activeBubbles) {
            val b = bubbles[i]
            if (b.zDepth < 0.18f && b.rowY > splitRow + 3 && b.rowY < floorRow - 2) {
                val bx = (b.colX + pShiftCols * 1.25f) * cellWidthPx
                val by = b.rowY * cellHeightPx + baselineOffsetPx
                drawCharZeroAlloc(canvas, if (b.isBubble) 'O' else '°', bx, by, textPaint)
            }
        }
        textPaint.textSize = baseTextSizePx
        hasHighMotionActivity = highMotion
    }

    /**
     * Smoothly re-enters a fish either from beyond the left/right ocean window frame
     * OR emerging straight out of the deep misty ocean toward the glass (~35% chance),
     * rotating in fresh marine species over time so the ocean feels truly three-dimensional!
     */
    private fun reenterOceanWindow(
        f: AsciiFishAgent,
        minFishRow: Float,
        underwaterSpan: Float,
        cols: Int,
        rotateSpecies: Boolean
    ) {
        speciesRotationCounter++
        val hash = ((simTime * 100f).toInt() + f.id * 37 + speciesRotationCounter * 13) and 0x7FFFFFFF
        if (rotateSpecies && (hash % 3 != 0)) {
            f.species = AsciiFishSpecies.heroPool[hash % AsciiFishSpecies.heroPool.size]
        }
        val emergeFromDeep = !f.isSchooler && (hash % 100) < 35
        f.preferredDepthFrac = 0.10f + (((hash / 7) % 78) / 100f)
        f.targetDepthFrac = f.preferredDepthFrac
        f.rowY = minFishRow + f.preferredDepthFrac * underwaterSpan

        if (emergeFromDeep) {
            // Swim out of the deep ocean haze straight toward the window, growing organically as it approaches!
            f.zDepth = 0.94f
            val sw = spanCols(f.species.lengthFrac, f.zDepth)
            val spanX = (cols * 0.64f - sw).coerceAtLeast(6f)
            f.colX = cols * 0.18f + ((hash / 13) % 100) / 100f * spanX
            val angleNudge = (((hash % 11) - 5) / 10f) * 0.55f
            f.yaw = wrapAngle((-PI.toFloat() * 0.5f) + angleNudge)
            f.targetYaw = f.yaw
            f.facingDir = if (cos(f.yaw) >= 0f) 1 else -1
        } else {
            val enterLeft = (hash % 2 == 0)
            f.zDepth = 0.14f + ((hash % 74) / 100f)
            val sw = spanCols(f.species.lengthFrac, f.zDepth)
            f.colX = if (enterLeft) (-sw - 3.5f) else (cols + 3.5f)
            val baseYaw = if (enterLeft) 0f else PI.toFloat()
            val angleNudge = (((hash % 11) - 5) / 10f) * 0.68f
            f.yaw = wrapAngle(baseYaw + angleNudge)
            f.targetYaw = f.yaw
            f.facingDir = if (enterLeft) 1 else -1
        }
        f.pitchDeg = 0f
        val spd = f.species.baseSpeedColsPerSec * f.speedScale * (1.10f - f.zDepth * 0.42f)
        f.vx = cos(f.yaw) * spd
        f.vy = 0f
        f.vz = sin(f.yaw) * spd * 0.088f
        f.steerTimer = 2.8f + (hash % 4)
    }
}

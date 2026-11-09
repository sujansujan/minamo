package com.example.koipond

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

enum class AsciiFishSpecies(
    val id: Int,
    val displayName: String,
    val isHeroMultiLine: Boolean,
    val primaryRole: Int,
    val finAccentRole: Int,
    val baseSpeedColsPerSec: Float,
    val rightFrameA: Array<String>,
    val rightFrameB: Array<String>,
    val leftFrameA: Array<String>,
    val leftFrameB: Array<String>,
    val turnFrame: Array<String>
) {
    NEON_TETRA(
        id = 0,
        displayName = "neon tetra",
        isHeroMultiLine = false,
        primaryRole = 1,
        finAccentRole = 0,
        baseSpeedColsPerSec = 6.8f,
        rightFrameA = arrayOf(">-))°>"),
        rightFrameB = arrayOf("}-))°>"),
        leftFrameA  = arrayOf("<°((-<"),
        leftFrameB  = arrayOf("<°((-{ "),
        turnFrame   = arrayOf(" <°> ")
    ),
    SILVER_MINNOW(
        id = 1,
        displayName = "silver rasbora",
        isHeroMultiLine = false,
        primaryRole = 3,
        finAccentRole = 1,
        baseSpeedColsPerSec = 7.2f,
        rightFrameA = arrayOf(">~))'>"),
        rightFrameB = arrayOf("}~))'>"),
        leftFrameA  = arrayOf("<'((~<"),
        leftFrameB  = arrayOf("<'((~{"),
        turnFrame   = arrayOf(" <'> ")
    ),
    CLOWNFISH(
        id = 2,
        displayName = "clownfish",
        isHeroMultiLine = true,
        primaryRole = 0,
        finAccentRole = 3,
        baseSpeedColsPerSec = 4.6f,
        rightFrameA = arrayOf(
            "   _._   ",
            "><)|)|°> ",
            "   '-'   "
        ),
        rightFrameB = arrayOf(
            "   _._   ",
            "}<)|)|°> ",
            "   '-'   "
        ),
        leftFrameA = arrayOf(
            "   _._   ",
            " <°|(|>< ",
            "   '-'   "
        ),
        leftFrameB = arrayOf(
            "   _._   ",
            " <°|(|<{ ",
            "   '-'   "
        ),
        turnFrame = arrayOf(
            "  _._  ",
            " <|º|> ",
            "  '-'  "
        )
    ),
    BLUE_TANG(
        id = 3,
        displayName = "regal tang",
        isHeroMultiLine = true,
        primaryRole = 1,
        finAccentRole = 2,
        baseSpeedColsPerSec = 5.0f,
        rightFrameA = arrayOf(
            "   .--.  ",
            ">==)))°> ",
            "   '--'  "
        ),
        rightFrameB = arrayOf(
            "   .--.  ",
            "}==)))°> ",
            "   '--'  "
        ),
        leftFrameA = arrayOf(
            "  .--.   ",
            " <°(((==<",
            "  '--'   "
        ),
        leftFrameB = arrayOf(
            "  .--.   ",
            " <°(((=={",
            "  '--'   "
        ),
        turnFrame = arrayOf(
            "  .-.  ",
            " (|º|) ",
            "  '-'  "
        )
    ),
    GOLD_OGON(
        id = 4,
        displayName = "yamabuki ogon",
        isHeroMultiLine = true,
        primaryRole = 2,
        finAccentRole = 3,
        baseSpeedColsPerSec = 4.2f,
        rightFrameA = arrayOf(
            "   _.._   ",
            "><((((º>  ",
            "   '--'   "
        ),
        rightFrameB = arrayOf(
            "   _.._   ",
            "}<((((º>  ",
            "   '--'   "
        ),
        leftFrameA = arrayOf(
            "   _.._   ",
            "  <º))))><",
            "   '--'   "
        ),
        leftFrameB = arrayOf(
            "   _.._   ",
            "  <º))))<{",
            "   '--'   "
        ),
        turnFrame = arrayOf(
            "  _._  ",
            " <(º)> ",
            "  '-'  "
        )
    ),
    MANDARIN(
        id = 5,
        displayName = "mandarin dragonet",
        isHeroMultiLine = true,
        primaryRole = 4,
        finAccentRole = 1,
        baseSpeedColsPerSec = 4.4f,
        rightFrameA = arrayOf(
            "   /\\_   ",
            ">~{#))°> ",
            "   \\/    "
        ),
        rightFrameB = arrayOf(
            "   /\\_   ",
            "}~{#))°> ",
            "   \\/    "
        ),
        leftFrameA = arrayOf(
            "   _/\\   ",
            " <°((#~< ",
            "    \\/   "
        ),
        leftFrameB = arrayOf(
            "   _/\\   ",
            " <°((#~{ ",
            "    \\/   "
        ),
        turnFrame = arrayOf(
            "  /\\  ",
            " {º#} ",
            "  \\/  "
        )
    ),
    KOHAKU_KOI(
        id = 6,
        displayName = "kohaku koi",
        isHeroMultiLine = true,
        primaryRole = 3,
        finAccentRole = 0,
        baseSpeedColsPerSec = 4.0f,
        rightFrameA = arrayOf(
            "   _.--._  ",
            "><((({(º>  ",
            "   '-..-'  "
        ),
        rightFrameB = arrayOf(
            "   _.--._  ",
            "}<((({(º>  ",
            "   '-..-'  "
        ),
        leftFrameA = arrayOf(
            "  _.--._   ",
            "  <º)})))><",
            "  '-..-'   "
        ),
        leftFrameB = arrayOf(
            "  _.--._   ",
            "  <º)})))<{",
            "  '-..-'   "
        ),
        turnFrame = arrayOf(
            "  _.--._  ",
            " <((º))>  ",
            "  '-..-'  "
        )
    ),
    MOORISH_IDOL(
        id = 7,
        displayName = "moorish idol",
        isHeroMultiLine = true,
        primaryRole = 2,
        finAccentRole = 3,
        baseSpeedColsPerSec = 4.3f,
        rightFrameA = arrayOf(
            "    /|   ",
            " >|#|°>  ",
            "    \\|   "
        ),
        rightFrameB = arrayOf(
            "    /)   ",
            " }|#|°>  ",
            "    \\)   "
        ),
        leftFrameA = arrayOf(
            "   |\\    ",
            "  <°|#|< ",
            "   |/    "
        ),
        leftFrameB = arrayOf(
            "   (\\    ",
            "  <°|#|{ ",
            "   (/    "
        ),
        turnFrame = arrayOf(
            "   ||   ",
            "  |º#|  ",
            "   ||   "
        )
    ),
    VEIL_BETTA(
        id = 8,
        displayName = "royal betta",
        isHeroMultiLine = true,
        primaryRole = 4,
        finAccentRole = 2,
        baseSpeedColsPerSec = 3.8f,
        rightFrameA = arrayOf(
            "  ~-\\_   ",
            ">))))º>  ",
            "  ~-/    "
        ),
        rightFrameB = arrayOf(
            "   ~\\_   ",
            "}))))º>  ",
            "   ~/    "
        ),
        leftFrameA = arrayOf(
            "   _/-~  ",
            "  <º((((<",
            "    \\-~  "
        ),
        leftFrameB = arrayOf(
            "   _/~   ",
            "  <º(((({",
            "    \\~   "
        ),
        turnFrame = arrayOf(
            "  ~\\/~  ",
            "  (º)   ",
            "  ~/\\~  "
        )
    );

    companion object {
        val heroPool = arrayOf(CLOWNFISH, BLUE_TANG, GOLD_OGON, MANDARIN, KOHAKU_KOI, MOORISH_IDOL, VEIL_BETTA)
        val schoolPool = arrayOf(NEON_TETRA, SILVER_MINNOW)
    }
}

enum class PredatorSpecies(
    val id: Int,
    val displayName: String,
    val speedColsPerSec: Float,
    val rightFrameA: Array<String>,
    val rightFrameB: Array<String>,
    val rightChomp: Array<String>,
    val leftFrameA: Array<String>,
    val leftFrameB: Array<String>,
    val leftChomp: Array<String>,
    val frontChargeFrame: Array<String>, // Charging straight from the deep abyss (+Z -> -Z) toward camera!
    val rearRetreatFrame: Array<String>  // Swimming away into the deep background (-Z -> +Z)!
) {
    BARRACUDA_PIKE(
        id = 0,
        displayName = "barracuda",
        speedColsPerSec = 11.2f,
        rightFrameA = arrayOf(
            "    _.._     ",
            "><(((===º<   ",
            "    '--'     "
        ),
        rightFrameB = arrayOf(
            "    _.._     ",
            "}<(((===º=   ",
            "    '--'     "
        ),
        rightChomp = arrayOf(
            "    _.._     ",
            "><(((===º={{ ",
            "    '--'     "
        ),
        leftFrameA = arrayOf(
            "     _.._    ",
            "   >º===)))><",
            "     '--'    "
        ),
        leftFrameB = arrayOf(
            "     _.._    ",
            "   =º===)))<{",
            "     '--'    "
        ),
        leftChomp = arrayOf(
            "     _.._    ",
            " }}=º===)))><",
            "     '--'    "
        ),
        frontChargeFrame = arrayOf(
            "   _.--._   ",
            " <((ºVVº))> ",
            "   '-..-'   "
        ),
        rearRetreatFrame = arrayOf(
            "    _.._    ",
            "   (|XX|)   ",
            "    '--'    "
        )
    ),
    REEF_SHARK(
        id = 1,
        displayName = "reef shark",
        speedColsPerSec = 10.6f,
        rightFrameA = arrayOf(
            "     /|___   ",
            "><)))º>==<   ",
            "     \\/      "
        ),
        rightFrameB = arrayOf(
            "     /|___   ",
            "}<)))º>==-   ",
            "     \\/      "
        ),
        rightChomp = arrayOf(
            "     /|___   ",
            "><)))º>=={{  ",
            "     \\/      "
        ),
        leftFrameA = arrayOf(
            "   ___|\\     ",
            "   >==<º(((><",
            "      \\/     "
        ),
        leftFrameB = arrayOf(
            "   ___|\\     ",
            "   -==<º(((<{",
            "      \\/     "
        ),
        leftChomp = arrayOf(
            "   ___|\\     ",
            "  }}==<º(((><",
            "      \\/     "
        ),
        frontChargeFrame = arrayOf(
            "     /|\\     ",
            "  /((ºVº))\\  ",
            "    \\vvv/    "
        ),
        rearRetreatFrame = arrayOf(
            "     /|\\     ",
            "   ((|X|))   ",
            "     \\|/     "
        )
    ),
    SWORDFISH(
        id = 2,
        displayName = "marlin swordfish",
        speedColsPerSec = 12.0f,
        rightFrameA = arrayOf(
            "    /|__      ",
            "><(((º>====-- ",
            "    \\/        "
        ),
        rightFrameB = arrayOf(
            "    /|__      ",
            "}<(((º>====-- ",
            "    \\/        "
        ),
        rightChomp = arrayOf(
            "    /|__      ",
            "><(((º>==={{- ",
            "    \\/        "
        ),
        leftFrameA = arrayOf(
            "      __|\\    ",
            " --====<º(((><",
            "        \\/    "
        ),
        leftFrameB = arrayOf(
            "      __|\\    ",
            " --====<º(((<{",
            "        \\/    "
        ),
        leftChomp = arrayOf(
            "      __|\\    ",
            " -}}===<º(((><",
            "        \\/    "
        ),
        frontChargeFrame = arrayOf(
            "     /|\\     ",
            "  <((º+º))>  ",
            "     \\|/     "
        ),
        rearRetreatFrame = arrayOf(
            "     /|\\     ",
            "    (|X|)    ",
            "     \\|/     "
        )
    ),
    ELECTRIC_MORAY(
        id = 3,
        displayName = "moray eel",
        speedColsPerSec = 9.8f,
        rightFrameA = arrayOf(" ~^~^~==º< "),
        rightFrameB = arrayOf(" ^~^~^==º= "),
        rightChomp  = arrayOf(" ~^~^~==º{{"),
        leftFrameA  = arrayOf(" >º==~^~^~ "),
        leftFrameB  = arrayOf(" =º==^~^~^ "),
        leftChomp   = arrayOf("}}º==~^~^~ "),
        frontChargeFrame = arrayOf(
            "   .~^~.   ",
            "  ((ºVº))  ",
            "   '~v~'   "
        ),
        rearRetreatFrame = arrayOf(
            "   .~^~.   ",
            "   ((x))   "
        )
    )
}

enum class BottomFeederSpecies(
    val id: Int,
    val displayName: String,
    val speedColsPerSec: Float,
    val colorRole: Int,
    val rightFrameA: String,
    val rightFrameB: String,
    val leftFrameA: String,
    val leftFrameB: String,
    val frontCrawl: String, // Crawling down the 3D bank toward the front glass
    val rearClimb: String,  // Climbing up the 3D bank into the deep trench
    val defensiveSnap: String
) {
    RIVER_CRAB(
        id = 0,
        displayName = "river crab",
        speedColsPerSec = 2.0f,
        colorRole = 0,
        rightFrameA = "(\\/)(°_°)(\\/)",
        rightFrameB = "(/)(°_°)(\\)",
        leftFrameA  = "(\\/)(°_°)(\\/)",
        leftFrameB  = "(/)(°_°)(\\)",
        frontCrawl  = "(\\/)(º_º)(\\/)",
        rearClimb   = "(/) (_._) (\\)",
        defensiveSnap = "\\(V)/(°^°)\\(V)/"
    ),
    CHERRY_SHRIMP(
        id = 1,
        displayName = "cherry shrimp",
        speedColsPerSec = 2.8f,
        colorRole = 4,
        rightFrameA = ",=/>°",
        rightFrameB = ".=/>°",
        leftFrameA  = "°<\\=,",
        leftFrameB  = "°<\\=.",
        frontCrawl  = "/(°_°)/",
        rearClimb   = " \\(=)/ ",
        defensiveSnap = "!/>°"
    ),
    CORY_CATFISH(
        id = 2,
        displayName = "cory catfish",
        speedColsPerSec = 2.4f,
        colorRole = 2,
        rightFrameA = ">))º>=",
        rightFrameB = "}))º>~",
        leftFrameA  = "=<º((<",
        leftFrameB  = "~<º(({",
        frontCrawl  = "=(º_º)=",
        rearClimb   = " (|x|) ",
        defensiveSnap = ">))º>!"
    ),
    APPLE_SNAIL(
        id = 3,
        displayName = "golden snail",
        speedColsPerSec = 0.8f,
        colorRole = 2,
        rightFrameA = "@_/",
        rightFrameB = "@_'",
        leftFrameA  = "\\_@",
        leftFrameB  = "'_@",
        frontCrawl  = "(@)",
        rearClimb   = "(_)",
        defensiveSnap = "(@)"
    )
}

class AsciiFishAgent(
    val id: Int,
    var depthLayer: Int,
    var zDepth: Float = 0.4f, // Continuous 3D Z-depth: 0.05 (near glass) .. 0.95 (deep background)
    var baseZ: Float = 0.4f,
    var vz: Float = 0.0f,     // Active Z-axis velocity (- = toward camera, + = away into deep)
    var zSwimTimer: Float = 0f,
    var isSchooler: Boolean,
    var species: AsciiFishSpecies,
    var colFrac: Float,
    var preferredDepthFrac: Float,
    var colX: Float,
    var rowY: Float,
    var vx: Float,
    var vy: Float,
    var facingDir: Int,
    var turnTimer: Float,
    var speedScale: Float,
    var animPhase: Float,
    var isEvading: Boolean = false,
    var respawnTimer: Float = 0f,
    var pelletsEaten: Int = 0,
    var isElderCrown: Boolean = false // Levels up after eating 3 pellets; intimidates predators!
)

/**
 * 3 Rare Mythic "Legendary" Creatures that have a ~6% chance to spawn when unlocking the phone!
 */
enum class LegendarySpecies(
    val id: Int,
    val displayName: String,
    val speedColsPerSec: Float,
    val rightFrameA: Array<String>,
    val rightFrameB: Array<String>,
    val leftFrameA: Array<String>,
    val leftFrameB: Array<String>
) {
    DRAGON_KOI(
        id = 0,
        displayName = "ryujin dragon koi",
        speedColsPerSec = 3.8f,
        rightFrameA = arrayOf(
            "    _/^\\_.._    ",
            "}><((((({(º>==~ ",
            "    '-..--'     "
        ),
        rightFrameB = arrayOf(
            "    _/^\\_.._    ",
            "><(((((({(º>=~- ",
            "    '-..--'     "
        ),
        leftFrameA = arrayOf(
            "    _.._/^\\_    ",
            " ~==<º)}))))><{ ",
            "     '--..-'    "
        ),
        leftFrameB = arrayOf(
            "    _.._/^\\_    ",
            " -~=<º)})))))>< ",
            "     '--..-'    "
        )
    ),
    GIANT_OARFISH(
        id = 1,
        displayName = "regal oarfish",
        speedColsPerSec = 4.2f,
        rightFrameA = arrayOf(
            "   ~^~^~^~^~\\|// ",
            " ~===))))))))º>  ",
            "        '---'    "
        ),
        rightFrameB = arrayOf(
            "   ^~^~^~^~^\\|// ",
            " -===))))))))º>  ",
            "        '---'    "
        ),
        leftFrameA = arrayOf(
            " \\|//~^~^~^~^~   ",
            "  <º((((((((===~ ",
            "    '---'        "
        ),
        leftFrameB = arrayOf(
            " \\|//^~^~^~^~^   ",
            "  <º((((((((===- ",
            "    '---'        "
        )
    ),
    ANCIENT_COELACANTH(
        id = 2,
        displayName = "living coelacanth",
        speedColsPerSec = 3.2f,
        rightFrameA = arrayOf(
            "    _/|_/|_    ",
            "}><((({#{(º>   ",
            "    \\/  \\/    "
        ),
        rightFrameB = arrayOf(
            "    _/|_/|_    ",
            "><(((({#{(º>   ",
            "    \\/  \\/    "
        ),
        leftFrameA = arrayOf(
            "    _|\\_|\\_    ",
            "   <º)}#}))><{ ",
            "    \\/  \\/    "
        ),
        leftFrameB = arrayOf(
            "    _|\\_|\\_    ",
            "   <º)}#})))>< ",
            "    \\/  \\/    "
        )
    )
}

class LegendaryAgent(
    var active: Boolean = false,
    var species: LegendarySpecies = LegendarySpecies.DRAGON_KOI,
    var colX: Float = 20f,
    var rowY: Float = 45f,
    var zDepth: Float = 0.14f,
    var vxDir: Float = 1f,
    var animPhase: Float = 0f
)

class PredatorAgent(
    var active: Boolean = false,
    var species: PredatorSpecies = PredatorSpecies.BARRACUDA_PIKE,
    var ambushStyle: Int = 0, // 0 = Lateral X Sweep, 1 = Deep Abyss +Z -> -Z Charge, 2 = Diagonal 3D Dive/Ascent
    var colX: Float = -16f,
    var rowY: Float = 25f,
    var zDepth: Float = 0.25f,
    var vx: Float = 10f,
    var vy: Float = 0f,
    var vz: Float = 0f,
    var vxDir: Float = 1f,
    var vySpeed: Float = 0f,
    var animPhase: Float = 0f,
    var cooldownTimer: Float = 14f,
    var activeDuration: Float = 0f,
    var hasCaughtThisVisit: Boolean = false,
    var willCatchThisVisit: Boolean = true,
    var chompTimer: Float = 0f
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
    var zDepth: Float = 0.35f, // Coupled to riverbed slope + active Z crawling
    var preferredTier: Int,    // 0..4 vertical tier along the sloped riverbed bank
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

/**
 * Shoreline Frog `(o.o)` or Painted Turtle `_(@)_` perched on a mossy bank rock at the right waterline.
 * Flicks a tongue `---*` when the dragonfly passes close by, and dives with a splash if tapped!
 */
class ShorelineAmphibian(
    var isTurtle: Boolean = false,
    var diveHideTimer: Float = 0f,
    var tongueTimer: Float = 0f,
    var blinkPhase: Float = 0f
)

/**
 * Drifting Origami Paper Boat (`\__/`) or Fallen Maple Leaf (`~*`) gliding across the meniscus.
 */
class SurfaceDrifter(
    var isPaperBoat: Boolean = true,
    var colX: Float = 10f,
    var vx: Float = 1.4f
)

/**
 * Nighttime Shooting Star (`*---`) streaking across the upper sky.
 */
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
    var zDepth: Float = 0.30f, // 3D depth based on tap position!
    var isHeavyAlgaeWafer: Boolean = false,
    var restTimer: Float = 0f,
    var active: Boolean = false
)

class PondSimulation {

    val maxHeroFish = 9
    val maxSchoolFish = 7
    val totalMaxFish = maxHeroFish + maxSchoolFish

    val fish = Array(totalMaxFish) { idx ->
        val dFrac = 0.08f + ((idx + 0.5f) / totalMaxFish.toFloat()) * 0.82f
        val isSchool = idx >= maxHeroFish
        val dir = if (idx % 2 == 0) 1 else -1
        AsciiFishAgent(
            id = idx,
            depthLayer = if (isSchool) 0 else if (idx % 2 == 0) 2 else 1,
            isSchooler = isSchool,
            species = AsciiFishSpecies.entries[idx % AsciiFishSpecies.entries.size],
            colFrac = ((idx * 37 + 13) % 86 + 7) / 100f,
            preferredDepthFrac = dFrac,
            colX = 12f + idx * 4f,
            rowY = 24f + idx * 5f,
            vx = dir * 4.5f,
            vy = 0f,
            facingDir = dir,
            turnTimer = 0f,
            speedScale = 1.0f,
            animPhase = idx * 1.3f
        )
    }

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
    private val bfDrawOrder = IntArray(maxBottomFeeders) { it }

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
    private val fishDrawOrder = IntArray(totalMaxFish) { it }

    val pellets = Array(8) { FoodPellet() }
    private var nextPelletIdx = 0

    var simTime = 0f
        private set
    private var unlockSeedCounter = 1L

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
    private val spriteCharScratch = CharArray(32)
    private val insectDayFrameA = charArrayOf('~', 'o', '~')
    private val insectDayFrameB = charArrayOf('-', 'o', '-')
    private val insectNightFrameA = charArrayOf(' ', '*', ' ')
    private val insectNightFrameB = charArrayOf(' ', '·', ' ')
    private val zSwimAwayHero = arrayOf("  _._  ", " (|X|) ", "  '-'  ")
    private val zSwimAwaySmall = arrayOf(" (x) ")
    private val shadowCharsNear = charArrayOf('.', '=', '=', '=', '.')
    private val shadowCharsFar  = charArrayOf('.', '.', '.')
    private val reflectChars    = charArrayOf('~', '.', '.', '~')

    private fun drawTextZeroAlloc(canvas: Canvas, str: String, x: Float, y: Float, paint: Paint) {
        val len = str.length.coerceAtMost(spriteCharScratch.size)
        for (i in 0 until len) {
            spriteCharScratch[i] = str[i]
        }
        canvas.drawText(spriteCharScratch, 0, len, x, y, paint)
    }

    private fun drawCharZeroAlloc(canvas: Canvas, ch: Char, x: Float, y: Float, paint: Paint) {
        singleCharBuf[0] = ch
        canvas.drawText(singleCharBuf, 0, 1, x, y, paint)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.MONOSPACE
        fontFeatureSettings = "liga 0, calt 0"
        textAlign = Paint.Align.LEFT
        isSubpixelText = true
    }
    private val bgPaint = Paint()

    private var cellWidthPx = 13f
    private var cellHeightPx = 22f
    private var baselineOffsetPx = 17f

    var activeSpeciesSummary: String = "clownfish, kohaku koi, neon tetra"
        private set
    var onHuntOutcomeListener: ((Boolean, String, String) -> Unit)? = null
    var onCrownLevelUpListener: ((String) -> Unit)? = null
    var onLegendarySightingListener: ((String) -> Unit)? = null

    init {
        randomizeEcosystemOnUnlock(System.nanoTime())
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
            val sw = f.species.rightFrameA[0].length
            f.colX = (f.colFrac * (gridCols - sw - 4) + 2f).coerceIn(2f, (gridCols - sw - 2).toFloat())
            f.rowY = minR + f.preferredDepthFrac * spanR
        }
    }

    fun randomizeEcosystemOnUnlock(seed: Long = System.nanoTime()) {
        unlockSeedCounter++
        val rand = Random(seed xor (unlockSeedCounter * 0x9E3779B97F4A7C15UL.toLong()))

        val h1 = AsciiFishSpecies.heroPool[rand.nextInt(AsciiFishSpecies.heroPool.size)]
        var h2 = AsciiFishSpecies.heroPool[rand.nextInt(AsciiFishSpecies.heroPool.size)]
        if (h2 == h1) {
            h2 = AsciiFishSpecies.heroPool[(h1.ordinal + 2) % AsciiFishSpecies.heroPool.size]
        }
        val h3 = AsciiFishSpecies.heroPool[rand.nextInt(AsciiFishSpecies.heroPool.size)]
        val schoolSp = AsciiFishSpecies.schoolPool[rand.nextInt(AsciiFishSpecies.schoolPool.size)]

        activeSpeciesSummary = "${h1.displayName}, ${h2.displayName}, ${schoolSp.displayName}"
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
            val isSchool = i >= maxHeroFish
            f.isSchooler = isSchool
            // Spread fish across continuous 3D Z-depth from 0.08 (near glass) to 0.92 (deep canyon)
            f.baseZ = if (isSchool) {
                0.45f + (i - maxHeroFish) * 0.075f
            } else {
                0.08f + (i.toFloat() / maxHeroFish.toFloat()) * 0.78f
            }
            f.zDepth = f.baseZ
            f.depthLayer = when {
                f.zDepth > 0.64f -> 0
                f.zDepth > 0.32f -> 1
                else -> 2
            }
            f.species = if (isSchool) {
                schoolSp
            } else {
                when (i % 3) {
                    0 -> h1
                    1 -> h2
                    else -> h3
                }
            }
            val laneFrac = 0.06f + ((i.toFloat() + 0.5f) / totalMaxFish.toFloat()) * 0.86f
            f.preferredDepthFrac = laneFrac.coerceIn(0.06f, 0.92f)
            f.colFrac = 0.10f + ((i * 29 + rand.nextInt(17)) % 78) / 100f

            val sw = f.species.rightFrameA[0].length
            f.colX = (f.colFrac * (gridCols - sw - 4) + 2f).coerceIn(2f, (gridCols - sw - 2).toFloat())
            f.rowY = minR + f.preferredDepthFrac * spanR
            val dir = if (i % 2 == 0) 1 else -1
            f.facingDir = dir
            f.speedScale = (if (f.depthLayer == 0) 0.68f else 0.92f) + rand.nextFloat() * 0.22f
            f.vx = dir * f.species.baseSpeedColsPerSec * f.speedScale
            f.vy = 0f
            f.turnTimer = 0f
            f.animPhase = rand.nextFloat() * 6.28f
            f.isEvading = false
            f.respawnTimer = 0f
            f.pelletsEaten = 0
            f.isElderCrown = (i == 0 && rand.nextFloat() < 0.35f) // Occasional Elder Crown boss fish on unlock
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
            // Each bottom feeder gets its own distinct depth tier (0..4) on the sloped riverbed
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
        predator.cooldownTimer = 10f + rand.nextFloat() * 14f

        // Rare Legendary Spawn Roll (~7% chance on every phone unlock!)
        if (rand.nextFloat() < 0.07f) {
            spawnLegendaryCreature(rand)
        } else {
            legendary.active = false
        }
    }

    fun spawnLegendaryCreature(rand: Random = Random(System.nanoTime())): String {
        val sp = LegendarySpecies.entries[rand.nextInt(LegendarySpecies.entries.size)]
        legendary.species = sp
        legendary.active = true
        legendary.colX = 8f + rand.nextFloat() * (gridCols - 28).coerceAtLeast(16)
        val minR = (waterlineRow + 9).coerceAtMost(gridRows - 22)
        val maxR = (gridRows - 14).coerceAtLeast(minR + 6)
        legendary.rowY = minR + rand.nextFloat() * (maxR - minR)
        legendary.zDepth = 0.12f
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

        val minR = (waterlineRow + 7).coerceAtMost(gridRows - 20)
        val maxR = (gridRows - 12).coerceAtLeast(minR + 8)

        // Pick 1 of 3 3D Ambush Axes:
        //   0 = Lateral X Sweep at a random 3D depth plane
        //   1 = Deep Abyss (+Z -> -Z "Jaws" Charge straight out of the background canyon toward the glass!)
        //   2 = Diagonal 3D Dive / Trench Ascent (simultaneous X + Y + Z pursuit!)
        val style = rand.nextInt(3)
        predator.ambushStyle = style

        when (style) {
            1 -> {
                // Deep Abyss Charge (+Z -> -Z): Spawns inside the water column far away at zDepth = 0.94!
                predator.colX = (gridCols * (0.24f + rand.nextFloat() * 0.52f))
                predator.rowY = minR + rand.nextFloat() * (maxR - minR)
                predator.zDepth = 0.94f
                predator.vxDir = if (rand.nextBoolean()) 1f else -1f
                predator.vx = predator.vxDir * (sp.speedColsPerSec * 0.35f)
                predator.vy = 0f
                predator.vz = -0.38f // Charges rapidly toward the front glass!
            }
            2 -> {
                // Diagonal 3D Dive (from upper sunlit surface) or Trench Ascent (from deep floor)
                val fromLeft = rand.nextBoolean()
                val diveDown = rand.nextBoolean()
                predator.vxDir = if (fromLeft) 1f else -1f
                predator.colX = if (fromLeft) -14f else (gridCols + 2).toFloat()
                predator.rowY = if (diveDown) minR.toFloat() else maxR.toFloat()
                predator.zDepth = if (diveDown) 0.78f else 0.14f
                predator.vx = predator.vxDir * sp.speedColsPerSec
                predator.vy = if (diveDown) 4.5f else -4.5f
                predator.vz = if (diveDown) -0.18f else 0.18f
            }
            else -> {
                // Classic Lateral X Sweep at a random Z-depth plane
                val fromLeft = rand.nextBoolean()
                predator.vxDir = if (fromLeft) 1f else -1f
                predator.colX = if (fromLeft) -16f else (gridCols + 2).toFloat()
                predator.rowY = minR + rand.nextFloat() * (maxR - minR)
                predator.zDepth = 0.12f + rand.nextFloat() * 0.65f
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
            val angle = (idx.toFloat() / 14f) * (2f * Math.PI.toFloat()) + (idx * 0.37f)
            val speed = if (bp.isScale) (1.2f + (idx % 3) * 0.8f) else (0.8f + (idx % 4) * 1.35f)

            bp.colX = catchCol + cos(angle) * 0.8f
            bp.rowY = catchRow + sin(angle) * 0.5f
            bp.zDepth = catchZ.coerceIn(0.08f, 0.88f)
            bp.vx = cos(angle) * speed
            bp.vy = if (bp.isScale) (2.4f + (idx % 2) * 1.1f) else (sin(angle) * speed * 0.65f - 0.35f)
            // 3D Z-velocity: some blood/scales drift toward the front glass while others drift deeper!
            bp.vz = ((idx % 5) - 2) * 0.08f
            bp.maxAge = if (bp.isScale) 3.4f else (1.8f + (idx % 4) * 0.35f)
        }

        for (f in fish) {
            if (f.respawnTimer > 0f) continue
            val dx = f.colX - catchCol
            val dy = f.rowY - catchRow
            val d = hypot(dx, dy)
            if (d < 28f) {
                f.facingDir = if (dx >= 0f) 1 else -1
                f.vx = f.facingDir * (f.species.baseSpeedColsPerSec * 2.2f)
                f.vy = (if (dy >= 0f) 1f else -1f) * 6.5f
                f.isEvading = true
            }
        }
    }

    /**
     * Single tap:
     *   - If tapped near the shoreline frog/turtle at the right waterline, makes it dive with a splash!
     *   - Otherwise drops a sinking golden food pellet (`*`) for mid-water fish.
     */
    fun onTouch(screenX: Float, screenY: Float) {
        val c = (screenX / cellWidthPx).coerceIn(2f, (gridCols - 3).toFloat())
        val r = (screenY / cellHeightPx).coerceIn(2f, (gridRows - 4).toFloat())

        // Check if user tapped near the perched shoreline frog/turtle (right bank at waterlineRow)
        if (abs(c - (gridCols - 9)) < 8f && abs(r - waterlineRow) < 6f && shoreAmphibian.diveHideTimer <= 0f) {
            shoreAmphibian.diveHideTimer = 9.0f
            return
        }

        val p = pellets[nextPelletIdx]
        p.colX = c
        p.rowY = r.coerceAtLeast(waterlineRow.toFloat() + 1f)
        // Map tap vertical height to 3D Z-depth (tapping higher near the horizon drops deeper in Z)
        val uSpan = (gridRows - waterlineRow).coerceAtLeast(20).toFloat()
        p.zDepth = (1f - ((p.rowY - waterlineRow) / uSpan)).coerceIn(0.12f, 0.82f)
        p.isHeavyAlgaeWafer = false
        p.restTimer = 0f
        p.active = true
        nextPelletIdx = (nextPelletIdx + 1) % pellets.size
    }

    fun onLongPress(screenX: Float, screenY: Float) {
        val c = (screenX / cellWidthPx).coerceIn(4f, (gridCols - 5).toFloat())
        val r = (screenY / cellHeightPx).coerceIn(2f, (gridRows - 4).toFloat())

        val p = pellets[nextPelletIdx]
        p.colX = c
        p.rowY = r.coerceAtLeast(waterlineRow.toFloat() + 1f)
        val uSpan = (gridRows - waterlineRow).coerceAtLeast(20).toFloat()
        p.zDepth = (1f - ((p.rowY - waterlineRow) / uSpan)).coerceIn(0.12f, 0.78f)
        p.isHeavyAlgaeWafer = true
        p.restTimer = 9.0f
        p.active = true
        nextPelletIdx = (nextPelletIdx + 1) % pellets.size
    }

    fun updateAndRender(
        canvas: Canvas,
        dt: Float,
        config: WallpaperConfig,
        parallaxX: Float,
        parallaxY: Float,
        isBatterySaver: Boolean,
        isWallpaperMode: Boolean,
        batteryPercent: Int = 85
    ) {
        val clampedDt = dt.coerceIn(0.001f, 0.05f)
        simTime += clampedDt
        if (simTime >= 86400f) {
            simTime -= 86400f
        }

        if (config.waterlinePercent != currentWaterlinePct && canvas.width > 0 && canvas.height > 0) {
            configureGrid(canvas.width, canvas.height, config.gridDensity, config.waterlinePercent)
        }

        val snapshot = TimeOfDayPalette.resolve(config.timeCycleMode, config.paletteFamily)
        val pal = snapshot.colors

        val cols = gridCols
        val rows = gridRows
        // Enhanced 3D Window-into-a-Tank Gyro Parallax amplitude (`parallaxX`, `parallaxY`)
        val pShiftCols = (parallaxX * 11.5f)
        val pShiftRowsFloat = (parallaxY * 6.5f)
        val pShiftRows = (pShiftRowsFloat * 0.45f).roundToInt()
        val splitRow = (waterlineRow + pShiftRows).coerceIn(14, rows - 24)

        val topCalmRow = if (isWallpaperMode) (rows * 0.12f).toInt() else 0
        val floorRow = rows - 2
        val subFloorRow = rows - 1
        val underwaterRows = (rows - splitRow).coerceAtLeast(1)
        val isAmoled = (config.paletteFamily == PaletteFamily.AMOLED)

        if (isAmoled) {
            // True #000000 Pitch-Black OLED Fast Path: 90%+ of screen pixels are completely turned off!
            bgPaint.color = 0xFF000000.toInt()
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), bgPaint)
        } else {
            bgPaint.color = pal.skyBg
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), splitRow * cellHeightPx, bgPaint)

            for (r in splitRow until rows) {
                val linearD = (r - splitRow).toFloat() / underwaterRows.toFloat()
                val curveD = 1f - (1f - linearD) * (1f - linearD)
                var rowBg = TimeOfDayPalette.lerpColor(pal.waterBgTop, pal.waterBgDeep, curveD)
                if (r - splitRow <= 3) {
                    val glowFrac = (1f - (r - splitRow) / 4f) * 0.22f
                    rowBg = TimeOfDayPalette.lerpColor(rowBg, pal.meniscusWaveFg, glowFrac)
                }
                bgPaint.color = rowBg
                canvas.drawRect(
                    0f,
                    r * cellHeightPx,
                    canvas.width.toFloat(),
                    (r + 1) * cellHeightPx + 1f,
                    bgPaint
                )
            }
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
        // 2. OVER-WATER SKY: Sun / Moon, Shooting Stars, Aurora Curtains, Weather
        // =========================================================================
        val pShiftInt = pShiftCols.roundToInt()
        val celestialCol = ((snapshot.sunArcX * (cols - 12)).roundToInt() + 6 + pShiftInt).coerceIn(5, cols - 7)
        val skyHeight = (splitRow - 5).coerceAtLeast(6)
        val celestialRow = ((1f - snapshot.sunArcY) * (skyHeight - 4) + 3).roundToInt().coerceIn(3, splitRow - 5)

        if (snapshot.isNight) {
            for (s in 0 until 20) {
                val sc = ((s * 17 + 7 + pShiftInt) % (cols - 2)).coerceAtLeast(1)
                val sr = ((s * 9 + 3) % (splitRow - 4)).coerceAtLeast(1)
                val twinkle = sin(simTime * 2.2f + s * 1.7f)
                val starChar = if (twinkle > 0.35f) '*' else '.'
                setCell(sc, sr, starChar, pal.celestialGlyph)
            }
            when (snapshot.lunarPhaseIndex) {
                0 -> {
                    drawStringAtGrid(celestialCol - 2, celestialRow - 1, " .--. ", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow,     "   ) )", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow + 1, " '--' ", pal.celestialGlyph)
                }
                1 -> {
                    drawStringAtGrid(celestialCol - 2, celestialRow - 1, " .--. ", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow,     "  | ) ", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow + 1, " '--' ", pal.celestialGlyph)
                }
                2 -> {
                    drawStringAtGrid(celestialCol - 2, celestialRow - 1, " .--. ", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow,     " (  ) ", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow + 1, " '--' ", pal.celestialGlyph)
                }
                else -> {
                    drawStringAtGrid(celestialCol - 2, celestialRow - 1, " .--. ", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow,     "( (   ", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow + 1, " '--' ", pal.celestialGlyph)
                }
            }

            // Night Shooting Star (`*---`) streaking across the upper sky
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
        } else {
            val rayPulse = ((simTime * 1.5f).toInt() % 2 == 0)
            if (rayPulse) {
                drawStringAtGrid(celestialCol - 3, celestialRow - 1, " \\ | / ", pal.celestialGlyph)
                drawStringAtGrid(celestialCol - 3, celestialRow,     "--(O)--", pal.celestialGlyph)
                drawStringAtGrid(celestialCol - 3, celestialRow + 1, " / | \\ ", pal.celestialGlyph)
            } else {
                drawStringAtGrid(celestialCol - 3, celestialRow - 1, "  \\|/  ", pal.celestialGlyph)
                drawStringAtGrid(celestialCol - 3, celestialRow,     " -(o)- ", pal.celestialGlyph)
                drawStringAtGrid(celestialCol - 3, celestialRow + 1, "  /|\\  ", pal.celestialGlyph)
            }
        }

        val cloud1Col = ((simTime * 0.9f + 8f + pShiftInt).toInt() % (cols + 14)) - 10
        val cloud1Row = (splitRow * 0.32f).toInt().coerceIn(2, splitRow - 4)
        drawStringAtGrid(cloud1Col, cloud1Row,     "  .--.   ", pal.cloudReedFg)
        drawStringAtGrid(cloud1Col, cloud1Row + 1, "-(    )-.", pal.cloudReedFg)

        val cloud2Col = (((simTime * 0.55f + cols * 0.58f + pShiftInt).toInt()) % (cols + 14)) - 10
        val cloud2Row = (splitRow * 0.54f).toInt().coerceIn(3, splitRow - 3)
        drawStringAtGrid(cloud2Col, cloud2Row, " .-(   )-.", pal.cloudReedFg)

        val effectiveWeather = when (config.weatherMode) {
            WeatherMode.AUTO -> {
                val slot = ((simTime / 45f).toInt() % 8)
                when {
                    slot == 3 -> WeatherMode.RAIN
                    snapshot.isNight && slot == 6 -> WeatherMode.AURORA
                    else -> WeatherMode.CLEAR
                }
            }
            else -> config.weatherMode
        }

        if (effectiveWeather == WeatherMode.AURORA) {
            // Shimmering Aurora Borealis curtains (`~`, `:`, `.`) rippling across the upper sky
            for (c in 3 until cols - 3 step 2) {
                val wave = sin(c * 0.16f + simTime * 1.4f) * 2.5f + cos(c * 0.09f - simTime * 0.9f) * 1.5f
                val baseR = (splitRow * 0.28f + wave).roundToInt().coerceIn(2, splitRow - 6)
                val aCol1 = if ((c / 4) % 2 == 0) pal.meniscusWaveFg else pal.fishAqua
                val aCol2 = pal.fishRose
                setCell(c, baseR, ':', aCol1)
                setCell(c, baseR + 1, '|', aCol1)
                setCell(c, baseR + 2, '.', aCol2)
            }
        } else if (effectiveWeather == WeatherMode.RAIN || effectiveWeather == WeatherMode.SNOW) {
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
                        setCell(drop.colX.roundToInt(), splitRow - 1, '^', pal.meniscusWaveFg)
                    }
                    drop.rowY = 1f
                    drop.colX = ((dIdx * 19 + (simTime * 13f).toInt()) % cols).toFloat()
                } else {
                    val ch = if (isSnow) (if (dIdx % 2 == 0) '*' else '.') else (if (dIdx % 2 == 0) '/' else '`')
                    setCell(drop.colX.roundToInt(), drop.rowY.roundToInt(), ch, pal.bubbleFg)
                }
            }
        }

        val swayLeft = sin(simTime * 1.4f) > 0f
        for (c in 0 until cols) {
            if (c in 2..6 || c in (cols - 7)..(cols - 3)) {
                val reedTop = if ((c + if (swayLeft) 1 else 0) % 2 == 0) '/' else '|'
                setCell(c, splitRow - 2, reedTop, pal.kelpFg)
                setCell(c, splitRow - 1, '|', pal.kelpFg)
            }
        }

        // =========================================================================
        // 3. WATERLINE MENISCUS, LILY PADS, PERCHED FROG/TURTLE & ORIGAMI BOAT
        // =========================================================================
        val wavePhase = (simTime * 3.0f).toInt()
        val wavePattern = "~^~._."
        for (c in 0 until cols) {
            val ch = wavePattern[(c + wavePhase).mod(wavePattern.length)]
            setCell(c, splitRow, ch, pal.meniscusWaveFg)
        }

        val pad1Col = (cols * 0.22f + sin(simTime * 0.6f) * 1.5f + pShiftInt).roundToInt()
        drawStringAtGrid(pad1Col, splitRow, "[_v_]", pal.lilyPadFg)
        drawStringAtGrid(pad1Col + 1, splitRow - 1, "(*)", pal.lotusFlowerFg)

        val pad2Col = (cols * 0.62f + cos(simTime * 0.5f) * 1.5f + pShiftInt).roundToInt()
        drawStringAtGrid(pad2Col, splitRow, "[_v_]", pal.lilyPadFg)

        // 3D Dangling Lily-Pad Root Filaments hanging beneath each surface pad into the water column
        val rootColor = TimeOfDayPalette.lerpColor(pal.waterBgTop, pal.lilyPadFg, 0.52f)
        for (padC in intArrayOf(pad1Col, pad2Col)) {
            for (rStep in 1..4) {
                val rootSway = sin(simTime * 1.4f - rStep * 0.6f + padC * 0.2f)
                val rc = padC + 2 + (if (rootSway > 0.35f) 1 else if (rootSway < -0.35f) -1 else 0)
                val rr = splitRow + rStep
                val rootCh = when (rStep) {
                    1 -> '|'
                    2 -> ':'
                    3 -> if (rootSway >= 0f) ')' else '('
                    else -> '.'
                }
                setCell(rc, rr, rootCh, rootColor)
                if (rStep <= 2) {
                    setCell(rc - 1, rr, ':', rootColor)
                }
            }
        }

        // Perched Shoreline Mossy Rock & Frog `(o.o)` / Turtle `_(@)_` at Right Bank
        val rockCol = cols - 13
        drawStringAtGrid(rockCol, splitRow, "_.-._", pal.pebbleFg)
        if (shoreAmphibian.diveHideTimer > 0f) {
            shoreAmphibian.diveHideTimer = (shoreAmphibian.diveHideTimer - clampedDt).coerceAtLeast(0f)
            if (shoreAmphibian.diveHideTimer > 8.2f) {
                drawStringAtGrid(rockCol + 1, splitRow - 1, "\\^/", pal.meniscusWaveFg)
            }
        } else {
            shoreAmphibian.blinkPhase = (shoreAmphibian.blinkPhase + clampedDt * 2.0f) % 6.2831855f
            if (shoreAmphibian.tongueTimer > 0f) {
                shoreAmphibian.tongueTimer = (shoreAmphibian.tongueTimer - clampedDt).coerceAtLeast(0f)
            }
            val amphSprite = if (shoreAmphibian.isTurtle) {
                "_(@)_"
            } else {
                if (sin(shoreAmphibian.blinkPhase) > 0.92f) "(-.-)" else "(o.o)"
            }
            drawStringAtGrid(rockCol, splitRow - 1, amphSprite, pal.lilyPadFg)

            // If dragonfly hovers close to the frog, flick a tongue `---*`!
            if (!shoreAmphibian.isTurtle && !snapshot.isNight) {
                val distFly = rockCol - surfaceInsect.colX
                if (distFly in 2f..8f && shoreAmphibian.tongueTimer <= 0f) {
                    shoreAmphibian.tongueTimer = 0.28f
                }
                if (shoreAmphibian.tongueTimer > 0f) {
                    drawStringAtGrid(rockCol - 4, splitRow - 1, "*---", pal.fishCoral)
                }
            }
        }

        // Drifting Origami Paper Boat (`\__/`) or Fallen Maple Leaf (`~*`) on the Meniscus
        surfaceDrifter.colX += surfaceDrifter.vx * clampedDt
        if (surfaceDrifter.colX > cols - 18) surfaceDrifter.vx = -1.2f
        if (surfaceDrifter.colX < 8f) surfaceDrifter.vx = 1.2f
        val driftC = (surfaceDrifter.colX + pShiftCols * 0.4f).roundToInt()
        if (surfaceDrifter.isPaperBoat) {
            drawStringAtGrid(driftC, splitRow - 1, " /\\ ", pal.fishFoam)
            drawStringAtGrid(driftC, splitRow,     "\\__/", pal.fishFoam)
        } else {
            drawStringAtGrid(driftC, splitRow, "~*", pal.fishCoral)
        }

        // Receding perspective water-surface shimmer lines just above the meniscus (`splitRow - 2`, `splitRow - 1`)
        val perspHorizonColor = TimeOfDayPalette.lerpColor(pal.skyBg, pal.meniscusWaveFg, 0.38f)
        for (c in 6 until cols - 13 step 4) {
            val shimmerShift = ((c + wavePhase) % 5 == 0)
            if (shimmerShift) {
                setCell(c, splitRow - 2, '·', perspHorizonColor)
            } else if ((c + wavePhase) % 3 == 0) {
                setCell(c, splitRow - 1, '~', perspHorizonColor)
            }
        }

        // Far Abyssal Submerged Canyon Ridge (`z = 0.88`, moves slowly at 0.15x parallax for deep 3D space)
        val canyonBaseRow = (splitRow + (underwaterRows * 0.62f) + pShiftRowsFloat * 0.15f).roundToInt().coerceIn(splitRow + 6, floorRow - 8)
        val canyonColor = TimeOfDayPalette.lerpColor(pal.waterBgDeep, pal.pebbleFg, 0.28f)
        val canyonShift = (pShiftCols * 0.15f).roundToInt()
        val canyonPattern = "__..--~~^^~~--..____..--~~^~~--..__"
        for (c in 0 until cols) {
            val patCh = canyonPattern[(c - canyonShift).mod(canyonPattern.length)]
            val rOff = if (patCh == '^' || patCh == '~') -1 else 0
            setCell(c, canyonBaseRow + rOff, patCh, canyonColor)
        }

        // Midground Submerged Reef Shelf (`z = 0.55`, moves at 0.38x parallax)
        val shelfBaseRow = (floorRow - 7 + pShiftRowsFloat * 0.38f).roundToInt().coerceIn(splitRow + 10, floorRow - 4)
        val shelfColor = TimeOfDayPalette.lerpColor(pal.waterBgDeep, pal.pebbleFg, 0.48f)
        val shelfShift = (pShiftCols * 0.38f).roundToInt()
        val shelfPattern = "  .._.-~-._..      .._.-~-._..  "
        for (c in 0 until cols) {
            val sCh = shelfPattern[(c - shelfShift).mod(shelfPattern.length)]
            if (sCh != ' ') {
                setCell(c, shelfBaseRow, sCh, shelfColor)
            }
        }

        // Deep background volumetric sunbeams (drawn into background grid behind all fish)
        if (!snapshot.isNight && config.waterClarity > 0.45f) {
            val bgRayColor = TimeOfDayPalette.lerpColor(pal.waterBgTop, pal.sunRayFg, 0.38f)
            val rayCount = if (config.waterClarity > 0.78f) 4 else 2
            for (rayIdx in 0 until rayCount) {
                val startCol = (cols * (0.14f + rayIdx * 0.24f) + sin(simTime * 0.35f + rayIdx) * 2f).roundToInt()
                val maxRayDepth = ((rows - splitRow) * 0.72f).toInt()
                for (d in 2 until maxRayDepth step 3) {
                    val rc = startCol + (d / 2)
                    val rr = splitRow + d
                    if (rr < floorRow - 5 && (d + wavePhase) % 2 == 0) {
                        setCell(rc, rr, if (d % 6 == 0) ':' else '/', bgRayColor)
                    }
                }
            }
        }

        // =========================================================================
        // 4. RIVERBED COBBLES & REACTIVE BATTERY-LEVEL STONE LANTERN
        // =========================================================================
        val pebbleUpper = "  .   ..o..   .    .o.   "
        val pebbleMid   = " .o. ..ooO.. .o. _.oOo._ "
        val pebbleLower = "..ooO.._.oOo._..ooO.._..o"
        for (c in 0 until cols) {
            val uCh = pebbleUpper[(c - pShiftInt).mod(pebbleUpper.length)]
            val mCh = pebbleMid[(c - pShiftInt).mod(pebbleMid.length)]
            val lCh = pebbleLower[(c - pShiftInt).mod(pebbleLower.length)]
            if (uCh != ' ') setCell(c, floorRow - 3, uCh, pal.cloudReedFg)
            if (mCh != ' ') setCell(c, floorRow - 1, mCh, pal.pebbleFg)
            setCell(c, floorRow, lCh, pal.pebbleFg)
            setCell(c, subFloorRow, '~', pal.cloudReedFg)
        }

        // 3D Linear-Perspective Floor Grid fanning outward from the central vanishing point
        val perspGridColor = TimeOfDayPalette.lerpColor(pal.waterBgDeep, pal.pebbleFg, 0.36f)
        val centerCol = cols / 2 - pShiftInt / 2
        for (beam in -4..4) {
            for (rOff in 0..4) {
                // Fan out wider as rOff approaches the bottom edge (rOff = 4 is closest to viewer)
                val cPos = centerCol + beam * (6 + rOff * 2)
                val rPos = floorRow - 4 + rOff
                val gridCh = when {
                    beam < -1 -> '/'
                    beam > 1  -> '\\'
                    beam == 0 -> '|'
                    else      -> ':'
                }
                setCell(cPos, rPos, gridCh, perspGridColor)
            }
        }

        // Reactive Battery-Level Sunken Stone Lantern on the Riverbed:
        //   >= 50%: `[o]` glowing sun-gold
        //   20..49%: `[*]` warm coral-amber
        //   < 20%: `[!]` pulsing crimson warning
        val lmCol = (landmarkColFrac * (cols - 12) + 4 + pShiftInt * 0.3f).roundToInt()
        drawStringAtGrid(lmCol, floorRow - 3, " .^. ", pal.pebbleFg)
        val lanternMid = if (!config.enableBatteryLantern) {
            " [o] "
        } else {
            when {
                batteryPercent >= 50 -> " [o] "
                batteryPercent >= 20 -> " [*] "
                else -> if (wavePhase % 2 == 0) " [!] " else " [.] "
            }
        }
        val lanternCol = if (!config.enableBatteryLantern || batteryPercent >= 50) {
            pal.fishGold
        } else if (batteryPercent >= 20) {
            pal.fishCoral
        } else {
            pal.bloodCore
        }
        drawStringAtGrid(lmCol, floorRow - 2, lanternMid, lanternCol)
        drawStringAtGrid(lmCol, floorRow - 1, " /_\\ ", pal.pebbleFg)

        // =========================================================================
        // 5. RISING BUBBLES & SINKING PELLETS / HEAVY ALGAE WAFERS (`[@]`)
        // =========================================================================
        val activeHero = if (isBatterySaver) (config.heroFishCount - 2).coerceAtLeast(2) else config.heroFishCount
        val activeSchool = if (isBatterySaver) 2 else maxSchoolFish

        val activeBubbles = if (isBatterySaver) 10 else bubbles.size
        for (i in 0 until activeBubbles) {
            val b = bubbles[i]
            b.rowY -= b.speedY * clampedDt
            // 3D Corkscrew Helix: x uses cos(helixAngle), zDepth uses sin(helixAngle)
            val helixAngle = simTime * 2.1f + b.phase
            b.colX += cos(helixAngle) * 0.55f * clampedDt
            b.zDepth = (0.50f + sin(helixAngle) * 0.44f).coerceIn(0.04f, 0.94f)

            if (b.rowY <= splitRow + 1) {
                val refFish = fish[i % activeHero.coerceAtLeast(1)]
                b.colX = refFish.colX + 2f
                b.rowY = if (b.isBubble) refFish.rowY else (rows - 4).toFloat()
            }

            // Background & midground bubbles go into the grid; extreme foreground bokeh bubbles (z < 0.18)
            // are drawn after fish at 1.40x scale!
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
                    val wCol = TimeOfDayPalette.lerpColor(pal.lilyPadFg, pal.waterBgDeep, pel.zDepth * 0.55f)
                    drawStringAtGrid(pel.colX.roundToInt() - 1, pel.rowY.roundToInt(), "[@]", wCol)
                }
            } else {
                pel.rowY += 3.2f * sinkScale * clampedDt
                if (pel.rowY >= targetFloorAtZ) {
                    pel.active = false
                } else {
                    val pCol = TimeOfDayPalette.lerpColor(pal.fishGold, pal.waterBgDeep, pel.zDepth * 0.55f)
                    setCell(pel.colX.roundToInt(), pel.rowY.roundToInt(), '*', pCol)
                }
            }
        }

        // Flush static background grid to Canvas
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
                while (c < cols) {
                    val curCh = charGrid[rowOffset + c]
                    val curCol = colorGrid[rowOffset + c]
                    if (curCh != ' ' && curCol == runColor) {
                        rowCharScratch[runLen++] = curCh
                        c++
                    } else if (curCh == ' ') {
                        rowCharScratch[runLen++] = ' '
                        c++
                    } else {
                        break
                    }
                }
                textPaint.color = runColor
                canvas.drawText(rowCharScratch, 0, runLen, runStart * cellWidthPx, yPx, textPaint)
            }
        }

        val baseTextSizePx = textPaint.textSize

        fun drawSubPixelSprite(
            colFloat: Float,
            rowFloat: Float,
            lines: Array<String>,
            bodyColor: Int,
            finColor: Int,
            eyeColor: Int,
            isDimmedLayer: Boolean,
            zDepth: Float = 0.25f,
            inSunbeamShaft: Boolean = false
        ) {
            // 3D Perspective Scale: zDepth = 0.05 (foreground glass -> 1.15x size) .. 0.92 (deep background -> 0.62x size)
            val zClamped = zDepth.coerceIn(0.0f, 1.0f)
            val perspScale = 1.16f - zClamped * 0.54f
            val scaledCellW = cellWidthPx * perspScale
            val scaledCellH = cellHeightPx * perspScale
            textPaint.textSize = baseTextSizePx * perspScale

            // Continuous Beer-Lambert Underwater Fog + Vertical Depth Attenuation
            val vertFrac = ((rowFloat - splitRow) / underwaterRows.toFloat()).coerceIn(0f, 1f)
            val localWaterBg = TimeOfDayPalette.lerpColor(pal.waterBgTop, pal.waterBgDeep, vertFrac)
            val fogFactor = (zClamped * 0.68f + vertFrac * 0.18f).coerceIn(0f, 0.80f)

            // If the fish is swimming through a 3D sunbeam shaft, brighten its dorsal crest & body!
            val litBody = if (inSunbeamShaft) TimeOfDayPalette.lerpColor(bodyColor, pal.fishGold, 0.35f) else bodyColor
            val litFin  = if (inSunbeamShaft) TimeOfDayPalette.lerpColor(finColor, pal.fishFoam, 0.45f) else finColor
            val foggedBody = TimeOfDayPalette.lerpColor(litBody, localWaterBg, fogFactor)
            val foggedFin  = TimeOfDayPalette.lerpColor(litFin, localWaterBg, fogFactor)
            val foggedEye  = TimeOfDayPalette.lerpColor(eyeColor, localWaterBg, (fogFactor * 0.75f))

            val baseX = colFloat * cellWidthPx
            val centerY = rowFloat * cellHeightPx + baselineOffsetPx * 0.5f

            for (lineIdx in lines.indices) {
                val lineOffset = (lineIdx - (lines.size - 1) * 0.5f) * scaledCellH
                val lineY = centerY + lineOffset + baselineOffsetPx * 0.5f
                if (lineY < (splitRow + 1.2f) * cellHeightPx || lineY > (floorRow - 0.3f) * cellHeightPx) continue

                val str = lines[lineIdx]
                for (chIdx in str.indices) {
                    val ch = str[chIdx]
                    if (ch == ' ') continue

                    textPaint.color = when {
                        ch == '°' || ch == 'º' -> foggedEye
                        lines.size >= 3 && (lineIdx == 0 || lineIdx == lines.size - 1) -> foggedFin
                        ch == '|' || ch == '{' || ch == '}' || ch == '#' -> foggedFin
                        else -> foggedBody
                    }
                    singleCharBuf[0] = ch
                    canvas.drawText(singleCharBuf, 0, 1, baseX + chIdx * scaledCellW, lineY, textPaint)
                }
            }
            textPaint.textSize = baseTextSizePx
        }

        // =========================================================================
        // 6. SURFACE-SKIMMING DRAGONFLY / NIGHT FIREFLIES
        // =========================================================================
        surfaceInsect.phase = (surfaceInsect.phase + clampedDt * 5.5f) % 62.831855f
        surfaceInsect.colX += surfaceInsect.vx * clampedDt
        if (surfaceInsect.colX > cols - 8) surfaceInsect.vx = -3.6f
        if (surfaceInsect.colX < 4f) surfaceInsect.vx = 3.6f
        surfaceInsect.rowY = (splitRow - 2.6f) + sin(surfaceInsect.phase * 0.7f) * 0.8f

        val insX = (surfaceInsect.colX + pShiftCols * 0.6f) * cellWidthPx
        val insY = surfaceInsect.rowY * cellHeightPx + baselineOffsetPx
        val insectBuf = if (snapshot.isNight) {
            if (sin(surfaceInsect.phase * 2f) > 0f) insectNightFrameA else insectNightFrameB
        } else {
            if (sin(surfaceInsect.phase * 3f) > 0f) insectDayFrameA else insectDayFrameB
        }
        textPaint.color = if (snapshot.isNight) pal.fishRose else pal.fishAqua
        canvas.drawText(insectBuf, 0, 3, insX, insY, textPaint)

        // =========================================================================
        // 7. NIGHTTIME BIOLUMINESCENT MOON JELLYFISH
        // =========================================================================
        if (snapshot.isNight) {
            for (j in nightJellies.indices) {
                val jel = nightJellies[j]
                jel.phase = (jel.phase + clampedDt * 2.2f) % 62.831855f
                jel.rowY -= (0.9f + 0.5f * sin(jel.phase)) * clampedDt
                jel.colX += cos(jel.phase * 0.6f + j) * 0.5f * clampedDt
                // 3D Depth Pulsing: Jellyfish slowly drift toward and away from the front glass!
                jel.zDepth = (0.48f + 0.36f * sin(jel.phase * 0.25f + j * 2.1f)).coerceIn(0.10f, 0.88f)

                if (jel.rowY < splitRow + 6) {
                    jel.rowY = (floorRow - 6).toFloat()
                }
                val jLines = if (sin(jel.phase) > 0f) {
                    arrayOf(" .--. ", "( °° )", " /||\\ ")
                } else {
                    arrayOf("  ..  ", " (oo) ", "  ||  ")
                }
                val jelParallax = 0.85f - jel.zDepth * 0.65f
                drawSubPixelSprite(
                    colFloat = jel.colX + pShiftCols * jelParallax,
                    rowFloat = jel.rowY,
                    lines = jLines,
                    bodyColor = pal.fishAqua,
                    finColor = pal.fishRose,
                    eyeColor = pal.fishFoam,
                    isDimmedLayer = false,
                    zDepth = jel.zDepth
                )
            }
        }

        // =========================================================================
        // 8. BOTTOM-FEEDER CRITTERS (Algae Wafer Tug-of-War & Crab vs Predator Standoff!)
        // =========================================================================
        if (lastBloodSiteTimer > 0f) {
            lastBloodSiteTimer = (lastBloodSiteTimer - clampedDt).coerceAtLeast(0f)
        }

        if (config.enableBottomFeeders) {
            val activeFeeders = if (isBatterySaver) 3 else maxBottomFeeders
            // Gyroscope vertical tilt (`parallaxY`) subtly expands/compresses the 3D riverbed slope!
            val tiltSlopeStretch = (1.0f + parallaxY * 0.18f).coerceIn(0.82f, 1.22f)
            val minBedRow = (floorRow - 5.2f * tiltSlopeStretch).coerceAtLeast((splitRow + 10).toFloat())
            val maxBedRow = (floorRow - 0.35f)
            val bedSpan = (maxBedRow - minBedRow).coerceAtLeast(2.5f)

            for (b in 0 until activeFeeders) {
                val bf = bottomFeeders[b]
                bf.isSnappingAtPredator = false

                // Periodically crawl up or down the 3D riverbed bank (changing Z-depth tier!)
                if (bf.zRoamTimer > 0f) {
                    bf.zRoamTimer = (bf.zRoamTimer - clampedDt).coerceAtLeast(0f)
                } else if (sin(simTime * 0.9f + bf.id * 2.7f) > 0.96f) {
                    bf.zRoamTimer = 2.4f
                    bf.zRoamTargetTier = ((bf.preferredTier + 2) % 5).toFloat()
                }
                val activeTier = if (bf.zRoamTimer > 0f) bf.zRoamTargetTier else bf.preferredTier.toFloat()
                val homeTierRow = (maxBedRow - (activeTier / 4.0f) * bedSpan).coerceIn(minBedRow, maxBedRow)

                // Couple zDepth directly to where the critter is on the sloped riverbed:
                //   rowY near maxBedRow (bottom glass) -> zDepth = 0.08 (1.14x scale, bright)
                //   rowY near minBedRow (upper terrace) -> zDepth = 0.82 (0.70x scale, deep fog)
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

                val spriteLen = bf.species.rightFrameA.length
                val minC = 2.0f
                val maxC = (cols - spriteLen - 2.0f).coerceAtLeast(minC + 4f)

                // Perspective speed scaling: deeper critters on the upper terrace appear to move slower
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
                    val otherLen = other.species.rightFrameA.length

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

                val smoothRate = (clampedDt * 7.0f).coerceIn(0.05f, 1.0f)
                bf.vx += (desiredVx - bf.vx) * smoothRate
                bf.vy += (desiredVy - bf.vy) * smoothRate

                if (!bf.isSnappingAtPredator) {
                    bf.colX = (bf.colX + bf.vx * clampedDt).coerceIn(minC, maxC)
                    bf.rowY = (bf.rowY + bf.vy * clampedDt).coerceIn(minBedRow, maxBedRow)
                }

                val moveSpd = hypot(bf.vx, bf.vy)
                bf.animPhase = (bf.animPhase + clampedDt * (2.2f + moveSpd * 1.6f)) % 6.2831855f
            }

            // Sort bottom feeders back-to-front by zDepth (deepest on upper terrace drawn first!)
            for (i in 1 until activeFeeders) {
                val key = bfDrawOrder[i]
                val keyZ = bottomFeeders[key].zDepth
                var j = i - 1
                while (j >= 0 && bottomFeeders[bfDrawOrder[j]].zDepth < keyZ) {
                    bfDrawOrder[j + 1] = bfDrawOrder[j]
                    j--
                }
                bfDrawOrder[j + 1] = key
            }

            for (orderIdx in 0 until activeFeeders) {
                val bf = bottomFeeders[bfDrawOrder[orderIdx]]
                val frameB = sin(bf.animPhase) > 0f
                // Display 3D foreshortened Front-Crawl or Rear-Climb sprite when moving steeply along Z!
                val sprite = when {
                    bf.isSnappingAtPredator -> bf.species.defensiveSnap
                    bf.vy > 0.55f -> bf.species.frontCrawl // Crawling down slope toward front glass!
                    bf.vy < -0.55f -> bf.species.rearClimb // Climbing up slope into deep trench!
                    bf.vxDir >= 0f -> if (frameB) bf.species.rightFrameB else bf.species.rightFrameA
                    else -> if (frameB) bf.species.leftFrameB else bf.species.leftFrameA
                }
                val rawColor = when {
                    bf.isSnappingAtPredator -> pal.fishGold
                    bf.species.colorRole == 0 -> pal.fishCoral
                    bf.species.colorRole == 1 -> pal.fishAqua
                    bf.species.colorRole == 2 -> pal.fishGold
                    bf.species.colorRole == 3 -> pal.fishFoam
                    else -> pal.fishRose
                }
                // Apply 3D Perspective Font Scale (`0.70x` on far upper terrace .. `1.14x` near front glass) + Beer-Lambert Fog!
                val bfScale = 1.16f - bf.zDepth * 0.54f
                textPaint.textSize = baseTextSizePx * bfScale
                textPaint.color = TimeOfDayPalette.lerpColor(rawColor, pal.waterBgDeep, bf.zDepth * 0.62f)

                val bfParallax = 0.85f - bf.zDepth * 0.60f
                val bxPx = ((bf.colX + pShiftCols * bfParallax).coerceIn(1f, (cols - sprite.length - 1).toFloat())) * cellWidthPx
                val byPx = bf.rowY * cellHeightPx + baselineOffsetPx
                drawTextZeroAlloc(canvas, sprite, bxPx, byPx, textPaint)
            }
            textPaint.textSize = baseTextSizePx
        }

        // =========================================================================
        // 9. PREDATOR CHASE, ELDER CROWN INTIMIDATION & JAW-SNAP CATCH
        // =========================================================================
        val minFishRow = (splitRow + 4).toFloat()
        val maxFishRow = (floorRow - 6).toFloat().coerceAtLeast(minFishRow + 12f)
        val underwaterSpan = (maxFishRow - minFishRow).coerceAtLeast(12f)

        if (config.enablePredators) {
            if (!predator.active) {
                predator.cooldownTimer -= clampedDt
                if (predator.cooldownTimer <= 0f) {
                    triggerPredatorVisit((simTime * 1000f).toLong(), config.huntMode.catchChance)
                }
            } else {
                predator.activeDuration += clampedDt
                predator.animPhase = (predator.animPhase + clampedDt * 7.5f) % 6.2831855f
                if (predator.chompTimer > 0f) {
                    predator.chompTimer = (predator.chompTimer - clampedDt).coerceAtLeast(0f)
                }

                var targetFish: AsciiFishAgent? = null
                var minDist3D = 999f
                if (!predator.hasCaughtThisVisit) {
                    for (i in 0 until totalMaxFish) {
                        val f = fish[i]
                        if (f.respawnTimer > 0f || f.isElderCrown) continue
                        if (!f.isSchooler && i >= activeHero) continue
                        if (f.isSchooler && (i - maxHeroFish) >= activeSchool) continue

                        // In Ambush Style 1 (+Z -> -Z charge from abyss), predator can lock onto any fish in front of it!
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

                val lungeMult = if (targetFish != null && minDist3D < 14f && predator.willCatchThisVisit) 1.42f else 1.0f
                val perspSpdScale = 1.15f - predator.zDepth * 0.45f

                if (targetFish != null && !predator.hasCaughtThisVisit) {
                    val tx = targetFish.colX
                    val ty = targetFish.rowY
                    val tz = targetFish.zDepth

                    // If charging from the deep abyss (style 1), steer horizontally & vertically toward target while rushing forward in Z!
                    if (predator.ambushStyle == 1 && predator.zDepth > tz + 0.14f) {
                        val dx = (tx - predator.colX).coerceIn(-6f, 6f)
                        val dy = (ty - predator.rowY).coerceIn(-5f, 5f)
                        predator.vx = dx * 1.6f
                        predator.vy = dy * 1.6f
                        predator.vz = -0.38f * lungeMult
                        if (dx != 0f) predator.vxDir = if (dx >= 0f) 1f else -1f
                    } else {
                        // Bank into 3D pursuit across X, Y, and Z!
                        predator.vx = predator.vxDir * predator.species.speedColsPerSec * perspSpdScale * lungeMult
                        val dy = (ty - predator.rowY).coerceIn(-5.5f, 5.5f)
                        predator.vy = dy * (if (lungeMult > 1f) 2.5f else 1.6f)
                        val dz = (tz - predator.zDepth).coerceIn(-0.35f, 0.35f)
                        predator.vz = dz * 1.5f
                    }
                } else if (predator.hasCaughtThisVisit && predator.chompTimer <= 0f) {
                    // After catching prey, predator can bank away into the deep abyss (+Z retreat) or cruise out!
                    if (predator.ambushStyle == 1) {
                        predator.vz = 0.34f
                        predator.vx = predator.vxDir * predator.species.speedColsPerSec * 0.45f
                    }
                }

                predator.colX += predator.vx * clampedDt
                predator.rowY = (predator.rowY + predator.vy * clampedDt).coerceIn(minFishRow, maxFishRow)
                predator.zDepth = (predator.zDepth + predator.vz * clampedDt).coerceIn(0.06f, 0.96f)

                val predatorMouthX = if (abs(predator.vz) > 0.22f) {
                    predator.colX + predator.species.frontChargeFrame[0].length * 0.5f
                } else if (predator.vxDir >= 0f) {
                    predator.colX + predator.species.rightFrameA[0].length - 2f
                } else {
                    predator.colX + 2f
                }

                // 3D Jaw-Snap Catch Check (must match X, Y, AND Z-depth plane!)
                if (!predator.hasCaughtThisVisit && predator.willCatchThisVisit && targetFish != null) {
                    val fishCenterX = targetFish.colX + targetFish.species.rightFrameA[0].length * 0.5f
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

                if ((predator.vxDir > 0f && predator.colX > cols + 6) ||
                    (predator.vxDir < 0f && predator.colX < -18f) ||
                    (predator.hasCaughtThisVisit && predator.zDepth >= 0.94f) ||
                    predator.activeDuration > 11f
                ) {
                    if (!predator.hasCaughtThisVisit) {
                        onHuntOutcomeListener?.invoke(false, predator.species.displayName, "")
                    }
                    predator.active = false
                    predator.cooldownTimer = 14f + (cos(simTime) * 0.5f + 0.5f) * 14f
                }
            }
        } else {
            predator.active = false
        }

        // =========================================================================
        // 10. UPDATE PEACEFUL FISH (Pellet Level-Up into Elder Crown + Smooth Evasion)
        // =========================================================================
        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            if (!f.isSchooler && i >= activeHero) continue
            if (f.isSchooler && (i - maxHeroFish) >= activeSchool) continue

            val spriteW = f.species.rightFrameA[0].length
            val minColBound = 2.0f
            val maxColBound = (cols - spriteW - 2.0f).coerceAtLeast(minColBound + 4f)

            if (f.respawnTimer > 0f) {
                f.respawnTimer -= clampedDt
                if (f.respawnTimer <= 0f) {
                    f.respawnTimer = 0f
                    val enterLeft = (f.id % 2 == 0)
                    f.facingDir = if (enterLeft) 1 else -1
                    f.colX = if (enterLeft) minColBound else maxColBound
                    f.rowY = minFishRow + f.preferredDepthFrac * underwaterSpan
                    f.vx = f.facingDir * f.species.baseSpeedColsPerSec * f.speedScale
                    f.vy = 0f
                } else {
                    continue
                }
            }

            if (f.turnTimer > 0f) {
                f.turnTimer = (f.turnTimer - clampedDt).coerceAtLeast(0f)
            }

            val perspSpeedFactor = 1.12f - f.zDepth * 0.45f
            val cruiseSpd = f.species.baseSpeedColsPerSec * f.speedScale * perspSpeedFactor
            val zForwardDamp = if (f.zSwimTimer > 0f) 0.25f else 1.0f
            var desiredVx = f.facingDir * cruiseSpd * zForwardDamp
            val targetLaneRow = minFishRow + f.preferredDepthFrac * underwaterSpan
            var desiredVy = (targetLaneRow - f.rowY) * 1.4f + sin(simTime * 1.3f + f.id * 1.9f) * 0.75f

            if (f.isSchooler && !predator.active) {
                val leader = fish[maxHeroFish]
                if (f !== leader && leader.respawnTimer <= 0f) {
                    val schoolTargetX = leader.colX - leader.facingDir * ((f.id - maxHeroFish) * 3.5f)
                    val schoolTargetY = leader.rowY + ((f.id % 3) - 1) * 2.2f
                    desiredVx += (schoolTargetX - f.colX).coerceIn(-3f, 3f) * 0.6f
                    desiredVy = (schoolTargetY - f.rowY) * 1.6f
                }
            }

            for (pIdx in pellets.indices) {
                val pel = pellets[pIdx]
                if (pel.active && !pel.isHeavyAlgaeWafer) {
                    val pdx = pel.colX - f.colX
                    val pdy = pel.rowY - f.rowY
                    val pDist = hypot(pdx, pdy)
                    if (pDist < 20f) {
                        val wantDir = if (pdx >= 0f) 1 else -1
                        if (wantDir != f.facingDir && f.turnTimer <= 0f) {
                            f.facingDir = wantDir
                            f.turnTimer = 0.16f
                        }
                        desiredVx = f.facingDir * cruiseSpd * 1.30f
                        desiredVy = (pdy / pDist.coerceAtLeast(1f)) * 4.2f
                        if (pDist < 2.4f) {
                            pel.active = false
                            f.pelletsEaten++
                            if (f.pelletsEaten >= 3 && !f.isElderCrown) {
                                f.isElderCrown = true
                                onCrownLevelUpListener?.invoke(f.species.displayName)
                            }
                        }
                        break
                    }
                }
            }

            f.isEvading = false
            // Elder Crown fish are fearless and never panic when a predator swims by!
            if (predator.active && !predator.hasCaughtThisVisit && !f.isElderCrown) {
                val pCenterX = predator.colX + 6f
                val dx = (f.colX + spriteW * 0.5f) - pCenterX
                val dy = f.rowY - predator.rowY
                val dist = hypot(dx, dy * 1.4f)
                val threatRadius = 26f

                if (dist < threatRadius && abs(f.zDepth - predator.zDepth) < 0.42f) {
                    f.isEvading = true
                    val urgency = (1f - dist / threatRadius).coerceIn(0.2f, 1f)
                    // 3D Z-Axis Evasion: fish also dives deeper or darts toward the front glass away from predator.zDepth!
                    val zPush = if (f.zDepth >= predator.zDepth) 0.28f else -0.28f
                    f.baseZ = (f.baseZ + zPush * urgency * clampedDt).coerceIn(0.08f, 0.90f)

                    val nearLeftWall = f.colX < minColBound + 8f
                    val nearRightWall = f.colX > maxColBound - 8f
                    val fleeDir = when {
                        nearLeftWall -> 1
                        nearRightWall -> -1
                        dx >= 0f -> 1
                        else -> -1
                    }
                    if (fleeDir != f.facingDir && f.turnTimer <= 0f) {
                        f.facingDir = fleeDir
                        f.turnTimer = 0.14f
                    }
                    desiredVx = f.facingDir * (cruiseSpd + 7.5f * urgency)

                    val jukeSign = when {
                        f.rowY < minFishRow + 5f -> 1f
                        f.rowY > maxFishRow - 5f -> -1f
                        abs(dy) > 1.0f -> if (dy > 0f) 1f else -1f
                        f.id % 2 == 0 -> -1f
                        else -> 1f
                    }
                    desiredVy = jukeSign * (6.0f * urgency)
                }
            }

            // True 3D Z-Axis Swimming: fish periodically turn toward the camera glass (vz < 0)
            // or swim deep into the canyon (vz > 0), displaying foreshortened front/back sprites!
            if (f.zSwimTimer > 0f) {
                f.zSwimTimer = (f.zSwimTimer - clampedDt).coerceAtLeast(0f)
                f.baseZ = (f.baseZ + f.vz * clampedDt).coerceIn(0.08f, 0.90f)
                if (f.baseZ <= 0.09f || f.baseZ >= 0.89f) {
                    f.vz = -f.vz
                }
            } else if (!f.isEvading && sin(simTime * 0.75f + f.id * 3.1f) > 0.975f) {
                f.zSwimTimer = 1.6f
                f.vz = if (f.baseZ > 0.48f) -0.22f else 0.22f
            } else {
                f.vz = 0f
            }

            f.zDepth = (f.baseZ + sin(simTime * 0.32f + f.id * 1.7f) * 0.14f).coerceIn(0.06f, 0.92f)
            f.depthLayer = when {
                f.zDepth > 0.64f -> 0
                f.zDepth > 0.32f -> 1
                else -> 2
            }

            for (j in 0 until totalMaxFish) {
                if (i == j) continue
                val other = fish[j]
                if (other.respawnTimer > 0f) continue
                if (!other.isSchooler && j >= activeHero) continue
                if (other.isSchooler && (j - maxHeroFish) >= activeSchool) continue
                // Fish swimming at different 3D Z-depths pass naturally in front of / behind each other!
                if (abs(f.zDepth - other.zDepth) > 0.25f) continue

                val otherW = other.species.rightFrameA[0].length
                val dx = (f.colX + spriteW * 0.5f) - (other.colX + otherW * 0.5f)
                val dy = f.rowY - other.rowY

                val safeX = (spriteW + otherW) * 0.55f + 2.5f
                val safeY = (f.species.rightFrameA.size + other.species.rightFrameA.size) * 0.55f + 1.6f

                if (abs(dx) < safeX && abs(dy) < safeY) {
                    val normY = (safeY - abs(dy)) / safeY
                    val pushSignY = if (dy > 0.05f) 1f else if (dy < -0.05f) -1f else if (f.id > other.id) 1f else -1f
                    desiredVy += pushSignY * normY * 5.2f

                    val normX = (safeX - abs(dx)) / safeX
                    val pushSignX = if (dx >= 0f) 1f else -1f
                    desiredVx += pushSignX * normX * 2.4f
                }
            }

            if (f.facingDir > 0 && f.colX >= maxColBound - 1.2f) {
                f.facingDir = -1
                f.turnTimer = 0.18f
                desiredVx = -cruiseSpd
            } else if (f.facingDir < 0 && f.colX <= minColBound + 1.2f) {
                f.facingDir = 1
                f.turnTimer = 0.18f
                desiredVx = cruiseSpd
            }

            val accelRate = (clampedDt * 6.5f).coerceIn(0.05f, 1.0f)
            f.vx += (desiredVx - f.vx) * accelRate
            f.vy += (desiredVy - f.vy) * accelRate

            f.colX += f.vx * clampedDt
            f.rowY += f.vy * clampedDt

            if (f.colX < minColBound) {
                f.colX = minColBound
                f.facingDir = 1
                if (f.vx < 0f) f.vx = cruiseSpd * 0.6f
            } else if (f.colX > maxColBound) {
                f.colX = maxColBound
                f.facingDir = -1
                if (f.vx > 0f) f.vx = -cruiseSpd * 0.6f
            }
            f.rowY = f.rowY.coerceIn(minFishRow, maxFishRow)
            f.colFrac = (f.colX / cols.toFloat()).coerceIn(0.02f, 0.94f)

            val actualSpd = hypot(f.vx, f.vy)
            f.animPhase = (f.animPhase + clampedDt * (2.6f + actualSpd * 0.45f)) % 62.831855f
        }

        // =========================================================================
        // 11. DRAW 3 DEPTH LAYERS OF FISH + ELDER CROWNS + BLOOD PLUME + PREDATOR
        // =========================================================================
        fun resolveRoleColor(role: Int): Int = when (role) {
            0 -> pal.fishCoral
            1 -> pal.fishAqua
            2 -> pal.fishGold
            3 -> pal.fishFoam
            else -> pal.fishRose
        }

        // Sort fish back-to-front by continuous zDepth (largest zDepth = deepest in background drawn first)
        for (i in 1 until totalMaxFish) {
            val key = fishDrawOrder[i]
            val keyZ = fish[key].zDepth
            var j = i - 1
            while (j >= 0 && fish[fishDrawOrder[j]].zDepth < keyZ) {
                fishDrawOrder[j + 1] = fishDrawOrder[j]
                j--
            }
            fishDrawOrder[j + 1] = key
        }

        var drewMidgroundCaustics = false

        for (orderIdx in 0 until totalMaxFish) {
            val i = fishDrawOrder[orderIdx]
            val f = fish[i]
            if (f.respawnTimer > 0f) continue
            if (!f.isSchooler && i >= activeHero) continue
            if (f.isSchooler && (i - maxHeroFish) >= activeSchool) continue

            // Interleave foreground volumetric sunbeam sparkles right between midground (z > 0.35) and foreground (z <= 0.35) fish
            // so midground fish physically swim BEHIND the bright light shafts while foreground fish swim IN FRONT of them!
            if (!drewMidgroundCaustics && f.zDepth <= 0.35f) {
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

            // 3D Depth-Differential Gyro Parallax (X & Y shift proportional to how close the fish is to the front glass)
            val parallaxMult = 1.05f - f.zDepth * 0.88f
            val useFrameB = sin(f.animPhase) > 0f
            val lines = when {
                f.turnTimer > 0f -> f.species.turnFrame
                f.zSwimTimer > 0f && f.vz < 0f -> f.species.turnFrame
                f.zSwimTimer > 0f && f.vz > 0f -> if (f.isSchooler) zSwimAwaySmall else zSwimAwayHero
                f.facingDir >= 0 -> if (useFrameB) f.species.rightFrameB else f.species.rightFrameA
                else -> if (useFrameB) f.species.leftFrameB else f.species.leftFrameA
            }

            val sw = lines[0].length
            val clampedDrawCol = (f.colX + pShiftCols * parallaxMult)
                .coerceIn(0.5f, (cols - sw - 0.5f).coerceAtLeast(1f))
            val parallaxDrawRow = (f.rowY + pShiftRowsFloat * parallaxMult)
                .coerceIn(minFishRow, maxFishRow)

            // 1. Cast 3D Drop-Shadow onto the Sloped Riverbed when fish is in the lower water column
            val distFromFloor = floorRow - f.rowY
            if (distFromFloor in 2.5f..42.0f) {
                val shadowRowFloat = (floorRow - 0.5f - f.zDepth * 3.8f).coerceIn((floorRow - 4.5f), (floorRow - 0.4f))
                val sunOffsetCols = (distFromFloor * 0.14f)
                val shadowColFloat = (clampedDrawCol + sunOffsetCols + sw * 0.2f).coerceIn(2f, (cols - 6).toFloat())
                textPaint.color = TimeOfDayPalette.lerpColor(pal.waterBgDeep, 0xFF02080C.toInt(), (1f - f.zDepth * 0.6f) * 0.65f)
                val sBuf = if (f.zDepth < 0.45f && !f.isSchooler) shadowCharsNear else shadowCharsFar
                canvas.drawText(sBuf, 0, sBuf.size, shadowColFloat * cellWidthPx, shadowRowFloat * cellHeightPx + baselineOffsetPx, textPaint)
            }

            // 2. Underside Meniscus Reflection ("Snell's Window" Mirror) when fish swims near the surface
            val distFromSurface = f.rowY - splitRow
            if (distFromSurface in 2.5f..18.0f) {
                val reflectRowFloat = splitRow + 1.1f + (distFromSurface * 0.08f)
                val reflectColFloat = (clampedDrawCol + sw * 0.2f).coerceIn(2f, (cols - 6).toFloat())
                val reflectAlphaMix = (1f - distFromSurface / 18.0f) * (1f - f.zDepth * 0.5f) * 0.55f
                textPaint.color = TimeOfDayPalette.lerpColor(
                    pal.waterBgTop,
                    resolveRoleColor(f.species.primaryRole),
                    reflectAlphaMix
                )
                canvas.drawText(reflectChars, 0, reflectChars.size, reflectColFloat * cellWidthPx, reflectRowFloat * cellHeightPx + baselineOffsetPx, textPaint)
            }

            // 3. Check if fish is intersecting a 3D diagonal sunbeam shaft for volumetric dorsal lighting
            var inBeam = false
            if (!snapshot.isNight && config.waterClarity > 0.5f) {
                val dRow = f.rowY - splitRow
                for (rayIdx in 0..2) {
                    val beamColAtDepth = cols * (0.18f + rayIdx * 0.28f) + (dRow * 0.5f)
                    if (abs((clampedDrawCol + sw * 0.5f) - beamColAtDepth) < 4.5f) {
                        inBeam = true
                        break
                    }
                }
            }

            drawSubPixelSprite(
                colFloat = clampedDrawCol,
                rowFloat = parallaxDrawRow,
                lines = lines,
                bodyColor = resolveRoleColor(f.species.primaryRole),
                finColor = if (f.isElderCrown) pal.fishGold else resolveRoleColor(f.species.finAccentRole),
                eyeColor = pal.fishFoam,
                isDimmedLayer = false,
                zDepth = if (f.isElderCrown) (f.zDepth * 0.5f) else f.zDepth,
                inSunbeamShaft = inBeam
            )

            if (f.isElderCrown) {
                val perspScale = 1.16f - (f.zDepth * 0.5f) * 0.54f
                val crownCol = clampedDrawCol + sw * 0.45f * perspScale
                val crownRow = f.rowY - (lines.size * 0.5f * perspScale) - 0.4f
                textPaint.color = pal.fishGold
                drawCharZeroAlloc(canvas, '^', crownCol * cellWidthPx, crownRow * cellHeightPx + baselineOffsetPx, textPaint)
            }

            if (f.isEvading) {
                val wakeCol = if (f.facingDir >= 0) clampedDrawCol - 1.5f else clampedDrawCol + sw + 0.5f
                textPaint.color = pal.bubbleFg
                drawCharZeroAlloc(canvas, '~', wakeCol * cellWidthPx, f.rowY * cellHeightPx + baselineOffsetPx, textPaint)
            }
        }

        // Update & Render Rare Mythic Legendary Creature (if spawned on this unlock!)
        if (legendary.active) {
            legendary.animPhase = (legendary.animPhase + clampedDt * 3.8f) % 62.831855f
            legendary.colX += legendary.vxDir * legendary.species.speedColsPerSec * clampedDt
            legendary.rowY = (legendary.rowY + sin(simTime * 1.1f) * 0.9f * clampedDt).coerceIn(minFishRow + 2f, maxFishRow - 2f)
            legendary.zDepth = (0.14f + 0.10f * sin(simTime * 0.4f)).coerceIn(0.06f, 0.30f)

            val legWidth = legendary.species.rightFrameA[0].length
            if (legendary.vxDir > 0f && legendary.colX > cols - legWidth - 2f) {
                legendary.vxDir = -1f
            } else if (legendary.vxDir < 0f && legendary.colX < 2f) {
                legendary.vxDir = 1f
            }

            val legParallax = 1.05f - legendary.zDepth * 0.88f
            val legDrawCol = (legendary.colX + pShiftCols * legParallax).coerceIn(1f, (cols - legWidth - 1).toFloat())
            val legDrawRow = (legendary.rowY + pShiftRowsFloat * legParallax).coerceIn(minFishRow, maxFishRow)
            val useLegB = sin(legendary.animPhase) > 0f
            val legLines = if (legendary.vxDir >= 0f) {
                if (useLegB) legendary.species.rightFrameB else legendary.species.rightFrameA
            } else {
                if (useLegB) legendary.species.leftFrameB else legendary.species.leftFrameA
            }

            drawSubPixelSprite(
                colFloat = legDrawCol,
                rowFloat = legDrawRow,
                lines = legLines,
                bodyColor = pal.fishGold,
                finColor = pal.fishCoral,
                eyeColor = pal.fishFoam,
                isDimmedLayer = false,
                zDepth = legendary.zDepth,
                inSunbeamShaft = true
            )

            // Shimmering stardust wake (`*`, `·`) trailing behind the Legendary Creature
            val trailCol = if (legendary.vxDir >= 0f) legDrawCol - 2.0f else legDrawCol + legWidth + 1.0f
            textPaint.color = pal.fishGold
            drawCharZeroAlloc(canvas, if (wavePhase % 2 == 0) '*' else '·', trailCol * cellWidthPx, legDrawRow * cellHeightPx + baselineOffsetPx, textPaint)
        }

        // Update & Render 3D Multi-Stage ASCII Blood Plume & Sinking Scales (with Z-depth scaling & fog!)
        for (idx in bloodParticles.indices) {
            val bp = bloodParticles[idx]
            if (!bp.active) continue
            bp.age += clampedDt
            if (bp.age >= bp.maxAge) {
                bp.active = false
                continue
            }
            val drag = if (bp.isScale) 0.96f else 0.92f
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
            val bpScale = 1.16f - bp.zDepth * 0.54f
            textPaint.textSize = baseTextSizePx * bpScale
            textPaint.color = TimeOfDayPalette.lerpColor(rawCol, pal.waterBgDeep, bp.zDepth * 0.60f)

            val bpParallax = 0.85f - bp.zDepth * 0.65f
            val bx = (bp.colX + pShiftCols * bpParallax) * cellWidthPx
            val by = bp.rowY * cellHeightPx + baselineOffsetPx
            drawCharZeroAlloc(canvas, glyph, bx, by, textPaint)
        }
        textPaint.textSize = baseTextSizePx

        // Render Active 3D Predator (Supports Lateral X, Front-Charging +Z -> -Z, Rear-Retreating -Z -> +Z, and Shadow!)
        if (predator.active) {
            val predParallax = 0.85f - predator.zDepth * 0.65f
            val predDrawCol = predator.colX + pShiftCols * predParallax

            // Cast 3D Predator Drop-Shadow onto the riverbed slope!
            val pDistFloor = floorRow - predator.rowY
            if (pDistFloor in 2.0f..48.0f) {
                val pShadowRow = (floorRow - 0.5f - predator.zDepth * 3.8f).coerceIn((floorRow - 4.5f), (floorRow - 0.4f))
                val pShadowCol = (predDrawCol + pDistFloor * 0.14f + 3f).coerceIn(2f, (cols - 7).toFloat())
                textPaint.color = TimeOfDayPalette.lerpColor(pal.waterBgDeep, 0xFF01060A.toInt(), (1f - predator.zDepth * 0.55f) * 0.72f)
                canvas.drawText(shadowCharsNear, 0, shadowCharsNear.size, pShadowCol * cellWidthPx, pShadowRow * cellHeightPx + baselineOffsetPx, textPaint)
            }

            val useFrameB = sin(predator.animPhase) > 0f
            val pLines = when {
                predator.chompTimer > 0f -> if (predator.vxDir >= 0f) predator.species.rightChomp else predator.species.leftChomp
                predator.vz < -0.20f -> predator.species.frontChargeFrame // Charging straight out of the deep abyss toward camera!
                predator.vz > 0.20f  -> predator.species.rearRetreatFrame // Retreating back into the deep abyss!
                predator.vxDir >= 0f -> if (useFrameB) predator.species.rightFrameB else predator.species.rightFrameA
                else -> if (useFrameB) predator.species.leftFrameB else predator.species.leftFrameA
            }
            drawSubPixelSprite(
                colFloat = predDrawCol,
                rowFloat = predator.rowY,
                lines = pLines,
                bodyColor = if (predator.chompTimer > 0f) pal.bloodCore else pal.fishCoral,
                finColor = pal.fishGold,
                eyeColor = pal.fishFoam,
                isDimmedLayer = false,
                zDepth = predator.zDepth
            )
        }

        // =========================================================================
        // 12. EXTREME FOREGROUND GLASS-EDGE BOKEH BUBBLES (`z < 0.18`, `1.42x` scale, `1.25x` parallax)
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
    }
}

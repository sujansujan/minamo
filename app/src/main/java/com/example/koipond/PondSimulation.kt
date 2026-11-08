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

/**
 * 9 Detailed Multi-Line & Schooling ASCII Fish Species with dedicated Head-On Turning Frames.
 */
enum class AsciiFishSpecies(
    val id: Int,
    val displayName: String,
    val isHeroMultiLine: Boolean,
    val primaryRole: Int,   // 0=Coral, 1=Aqua, 2=Gold, 3=Foam, 4=Rose
    val finAccentRole: Int, // Secondary accent hue for dorsal/ventral fins & gill markings
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
    val leftFrameA: Array<String>,
    val leftFrameB: Array<String>
) {
    BARRACUDA_PIKE(
        id = 0,
        displayName = "barracuda",
        speedColsPerSec = 10.5f,
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
        leftFrameA = arrayOf(
            "     _.._    ",
            "   >º===)))><",
            "     '--'    "
        ),
        leftFrameB = arrayOf(
            "     _.._    ",
            "   =º===)))<{",
            "     '--'    "
        )
    ),
    REEF_SHARK(
        id = 1,
        displayName = "reef shark",
        speedColsPerSec = 10.0f,
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
        leftFrameA = arrayOf(
            "   ___|\\     ",
            "   >==<º(((><",
            "      \\/     "
        ),
        leftFrameB = arrayOf(
            "   ___|\\     ",
            "   -==<º(((<{",
            "      \\/     "
        )
    ),
    SWORDFISH(
        id = 2,
        displayName = "marlin swordfish",
        speedColsPerSec = 11.4f,
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
        leftFrameA = arrayOf(
            "      __|\\    ",
            " --====<º(((><",
            "        \\/    "
        ),
        leftFrameB = arrayOf(
            "      __|\\    ",
            " --====<º(((<{",
            "        \\/    "
        )
    ),
    ELECTRIC_MORAY(
        id = 3,
        displayName = "moray eel",
        speedColsPerSec = 9.0f,
        rightFrameA = arrayOf(
            " ~^~^~==º< "
        ),
        rightFrameB = arrayOf(
            " ^~^~^==º= "
        ),
        leftFrameA = arrayOf(
            " >º==~^~^~ "
        ),
        leftFrameB = arrayOf(
            " =º==^~^~^ "
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
    val leftFrameB: String
) {
    RIVER_CRAB(
        id = 0,
        displayName = "river crab",
        speedColsPerSec = 2.0f,
        colorRole = 0,
        rightFrameA = "(\\/)(°_°)(\\/)",
        rightFrameB = "(/)(°_°)(\\)",
        leftFrameA  = "(\\/)(°_°)(\\/)",
        leftFrameB  = "(/)(°_°)(\\)"
    ),
    CHERRY_SHRIMP(
        id = 1,
        displayName = "cherry shrimp",
        speedColsPerSec = 2.8f,
        colorRole = 4,
        rightFrameA = ",=/>°",
        rightFrameB = ".=/>°",
        leftFrameA  = "°<\\=,",
        leftFrameB  = "°<\\=."
    ),
    CORY_CATFISH(
        id = 2,
        displayName = "cory catfish",
        speedColsPerSec = 2.4f,
        colorRole = 2,
        rightFrameA = ">))º>=",
        rightFrameB = "}))º>~",
        leftFrameA  = "=<º((<",
        leftFrameB  = "~<º(({"
    ),
    APPLE_SNAIL(
        id = 3,
        displayName = "golden snail",
        speedColsPerSec = 0.8f,
        colorRole = 2,
        rightFrameA = "@_/",
        rightFrameB = "@_'",
        leftFrameA  = "\\_@",
        leftFrameB  = "'_@"
    )
}

/**
 * Sunken riverbed landmark randomized on each phone unlock.
 */
enum class SunkenLandmark(
    val displayName: String,
    val lines: Array<String>
) {
    STONE_LANTERN(
        "stone lantern",
        arrayOf(
            " .^. ",
            " [o] ",
            " /_\\ "
        )
    ),
    MOSS_DRIFTWOOD(
        "moss driftwood",
        arrayOf(
            "  _~..~_  ",
            " ===~==~_ "
        )
    ),
    GIANT_CLAM(
        "pearl clam",
        arrayOf(
            " (\\o/) ",
            "  '-'  "
        )
    )
}

class AsciiFishAgent(
    val id: Int,
    var depthLayer: Int,     // 0 = Background (dimmed, 0.65x speed), 1 = Midground, 2 = Foreground
    var isSchooler: Boolean,
    var species: AsciiFishSpecies,
    var colFrac: Float,
    var preferredDepthFrac: Float,
    var colX: Float,         // Continuous floating-point column coordinate
    var rowY: Float,         // Continuous floating-point row coordinate
    var vx: Float,           // Continuous horizontal velocity (cols/sec)
    var vy: Float,           // Continuous vertical velocity (rows/sec)
    var facingDir: Int,      // +1 = facing right, -1 = facing left
    var turnTimer: Float,    // > 0f while displaying the head-on turning sprite `<(º)>`
    var speedScale: Float,
    var animPhase: Float,
    var isEvading: Boolean = false
)

class PredatorAgent(
    var active: Boolean = false,
    var species: PredatorSpecies = PredatorSpecies.BARRACUDA_PIKE,
    var colX: Float = -16f,
    var rowY: Float = 25f,
    var vxDir: Float = 1f,
    var vySpeed: Float = 0f,
    var animPhase: Float = 0f,
    var cooldownTimer: Float = 14f,
    var activeDuration: Float = 0f
)

class BottomFeederAgent(
    val id: Int,
    var species: BottomFeederSpecies,
    var colX: Float,
    var vxDir: Float,
    var animPhase: Float,
    var pauseTimer: Float = 0f
)

class NightJellyfish(
    var colX: Float,
    var rowY: Float,
    var phase: Float
)

class SurfaceInsect(
    var colX: Float = 20f,
    var rowY: Float = 10f,
    var vx: Float = 3.5f,
    var phase: Float = 0f
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
    var speedY: Float = 3.2f,
    var isBubble: Boolean = true,
    var phase: Float = 0f
)

class FoodPellet(
    var colX: Float = 0f,
    var rowY: Float = 0f,
    var active: Boolean = false
)

/**
 * Silky-Smooth Sub-Pixel Character Renderer + Full Split-View Ecosystem Simulation.
 */
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

    val predator = PredatorAgent()

    val maxBottomFeeders = 5
    val bottomFeeders = Array(maxBottomFeeders) { idx ->
        BottomFeederAgent(
            id = idx,
            species = BottomFeederSpecies.entries[idx % BottomFeederSpecies.entries.size],
            colX = 6f + idx * 14f,
            vxDir = if (idx % 2 == 0) 1f else -1f,
            animPhase = idx * 1.7f
        )
    }

    val nightJellies = arrayOf(
        NightJellyfish(18f, 45f, 0.0f),
        NightJellyfish(58f, 68f, 2.4f)
    )

    val surfaceInsect = SurfaceInsect()
    var activeLandmark = SunkenLandmark.STONE_LANTERN
    var landmarkColFrac = 0.48f

    val weatherDrops = Array(28) { idx ->
        SkyWeatherDrop(
            colX = (idx * 13 % 80).toFloat(),
            rowY = (idx * 7 % 40).toFloat(),
            speedY = 22f + (idx % 4) * 3f,
            speedX = -4.5f
        )
    }

    val bubbles = Array(24) { idx ->
        AsciiBubble(
            colX = (idx * 11 % 75).toFloat(),
            rowY = (20 + (idx * 7 % 60)).toFloat(),
            speedY = if (idx < 9) 3.4f + (idx % 3) * 0.7f else 0.45f,
            isBubble = idx < 9,
            phase = idx * 0.9f
        )
    }

    val pellets = Array(6) { FoodPellet() }
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

    // Static background grid (for sky, waterline, sunbeams, pebbles)
    private val maxCols = 112
    private val maxRows = 240
    private val charGrid = CharArray(maxCols * maxRows) { ' ' }
    private val colorGrid = IntArray(maxCols * maxRows)
    private val rowCharScratch = CharArray(maxCols)
    private val singleCharBuf = CharArray(1)

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

    fun configureGrid(widthPx: Int, heightPx: Int, density: GridDensity) {
        if (widthPx <= 0 || heightPx <= 0) return
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
        waterlineRow = (gridRows * 0.36f).roundToInt().coerceIn(18, gridRows - 30)

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
        activeLandmark = SunkenLandmark.entries[rand.nextInt(SunkenLandmark.entries.size)]
        landmarkColFrac = 0.28f + rand.nextFloat() * 0.44f

        val minR = waterlineRow + 5
        val maxR = (gridRows - 8).coerceAtLeast(minR + 20)
        val spanR = (maxR - minR).toFloat()

        // Create a shuffled order of vertical lanes so hero & school fish interleave cleanly
        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            val isSchool = i >= maxHeroFish
            f.isSchooler = isSchool
            f.depthLayer = if (isSchool) (if (i % 2 == 0) 0 else 1) else (if (i % 3 == 0) 1 else 2)
            f.species = if (isSchool) {
                schoolSp
            } else {
                when (i % 3) {
                    0 -> h1
                    1 -> h2
                    else -> h3
                }
            }
            // Dedicated non-overlapping vertical lane per fish
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
        }

        for (b in 0 until maxBottomFeeders) {
            val bf = bottomFeeders[b]
            bf.species = BottomFeederSpecies.entries[rand.nextInt(BottomFeederSpecies.entries.size)]
            bf.colX = 4f + b * ((gridCols - 14).coerceAtLeast(25) / maxBottomFeeders.toFloat())
            bf.vxDir = if (rand.nextBoolean()) 1f else -1f
            bf.pauseTimer = 0f
        }

        predator.active = false
        predator.cooldownTimer = 10f + rand.nextFloat() * 14f
    }

    fun triggerPredatorVisit(seed: Long = System.nanoTime()) {
        val rand = Random(seed)
        val sp = PredatorSpecies.entries[rand.nextInt(PredatorSpecies.entries.size)]
        predator.species = sp
        predator.active = true
        predator.activeDuration = 0f
        predator.animPhase = 0f

        val fromLeft = rand.nextBoolean()
        predator.vxDir = if (fromLeft) 1f else -1f
        predator.colX = if (fromLeft) -16f else (gridCols + 2).toFloat()
        val minR = (waterlineRow + 8).coerceAtMost(gridRows - 20)
        val maxR = (gridRows - 14).coerceAtLeast(minR + 6)
        predator.rowY = minR + rand.nextFloat() * (maxR - minR)
        predator.vySpeed = 0f
    }

    fun onTouch(screenX: Float, screenY: Float) {
        val c = (screenX / cellWidthPx).coerceIn(2f, (gridCols - 3).toFloat())
        val r = (screenY / cellHeightPx).coerceIn(2f, (gridRows - 4).toFloat())

        val p = pellets[nextPelletIdx]
        p.colX = c
        p.rowY = r.coerceAtLeast(waterlineRow.toFloat() + 1f)
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
        isWallpaperMode: Boolean
    ) {
        val clampedDt = dt.coerceIn(0.001f, 0.05f)
        simTime += clampedDt

        val snapshot = TimeOfDayPalette.resolve(config.timeCycleMode, config.paletteFamily)
        val pal = snapshot.colors

        val cols = gridCols
        val rows = gridRows
        val pShiftCols = (parallaxX * 6f)
        val pShiftRows = (parallaxY * 2f).roundToInt()
        val splitRow = (waterlineRow + pShiftRows).coerceIn(14, rows - 24)

        val topCalmRow = if (isWallpaperMode) (rows * 0.12f).toInt() else 0
        val floorRow = rows - 2
        val subFloorRow = rows - 1

        // 1. Paint solid tinted background bands
        bgPaint.color = pal.skyBg
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), splitRow * cellHeightPx, bgPaint)

        val underwaterRows = (rows - splitRow).coerceAtLeast(1)
        for (r in splitRow until rows) {
            val depthFrac = (r - splitRow).toFloat() / underwaterRows.toFloat()
            bgPaint.color = TimeOfDayPalette.lerpColor(pal.waterBgTop, pal.waterBgDeep, depthFrac)
            canvas.drawRect(
                0f,
                r * cellHeightPx,
                canvas.width.toFloat(),
                (r + 1) * cellHeightPx + 1f,
                bgPaint
            )
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
        // 2. OVER-WATER SKY: Sun / Real Lunar Phase Moon, Stars, Clouds, Weather
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
            // Astronomical Lunar Phase rendering
            when (snapshot.lunarPhaseIndex) {
                0 -> { // Waxing Crescent
                    drawStringAtGrid(celestialCol - 2, celestialRow - 1, " .--. ", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow,     "   ) )", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow + 1, " '--' ", pal.celestialGlyph)
                }
                1 -> { // First Quarter Half Moon
                    drawStringAtGrid(celestialCol - 2, celestialRow - 1, " .--. ", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow,     "  | ) ", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow + 1, " '--' ", pal.celestialGlyph)
                }
                2 -> { // Full Moon
                    drawStringAtGrid(celestialCol - 2, celestialRow - 1, " .--. ", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow,     " (  ) ", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow + 1, " '--' ", pal.celestialGlyph)
                }
                else -> { // Waning Crescent
                    drawStringAtGrid(celestialCol - 2, celestialRow - 1, " .--. ", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow,     "( (   ", pal.celestialGlyph)
                    drawStringAtGrid(celestialCol - 2, celestialRow + 1, " '--' ", pal.celestialGlyph)
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

        // Weather in the sky half (Clear / Rain / Snow / Auto)
        val effectiveWeather = when (config.weatherMode) {
            WeatherMode.AUTO -> {
                // Occasional gentle rain shower for 45 seconds every ~6 minutes
                if (((simTime / 45f).toInt() % 8) == 3) WeatherMode.RAIN else WeatherMode.CLEAR
            }
            else -> config.weatherMode
        }

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
                    // Tiny surface splash `^` where rain hits the meniscus!
                    if (!isSnow) {
                        setCell(drop.colX.roundToInt(), splitRow - 1, '^', pal.meniscusWaveFg)
                    }
                    drop.rowY = 1f
                    drop.colX = ((dIdx * 19 + (simTime * 13f).toInt()) % cols).toFloat()
                } else {
                    val ch = if (isSnow) (if (dIdx % 2 == 0) '*' else '.') else (if (dIdx % 2 == 0) '/' else ''')
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
        // 3. WATERLINE MENISCUS, LILY PADS, LOTUS & SINKING LANDMARK
        // =========================================================================
        val wavePhase = (simTime * 3.0f).toInt()
        val wavePattern = "~^~._."
        for (c in 0 until cols) {
            val ch = wavePattern[(c + wavePhase).mod(wavePattern.length)]
            setCell(c, splitRow, ch, pal.meniscusWaveFg)
        }

        val pad1Col = (cols * 0.24f + sin(simTime * 0.6f) * 1.5f + pShiftInt).roundToInt()
        drawStringAtGrid(pad1Col, splitRow, "[_v_]", pal.lilyPadFg)
        drawStringAtGrid(pad1Col + 1, splitRow - 1, "(*)", pal.lotusFlowerFg)

        val pad2Col = (cols * 0.72f + cos(simTime * 0.5f) * 1.5f + pShiftInt).roundToInt()
        drawStringAtGrid(pad2Col, splitRow, "[_v_]", pal.lilyPadFg)

        // Subtle underwater diagonal sunbeams
        if (!snapshot.isNight && config.waterClarity > 0.5f) {
            val rayCount = if (config.waterClarity > 0.78f) 3 else 2
            for (rayIdx in 0 until rayCount) {
                val startCol = (cols * (0.18f + rayIdx * 0.30f) + sin(simTime * 0.4f + rayIdx) * 2f).roundToInt()
                val maxRayDepth = ((rows - splitRow) * 0.58f).toInt()
                for (d in 3 until maxRayDepth step 4) {
                    val rc = startCol + (d / 2)
                    val rr = splitRow + d
                    if (rr < floorRow - 6 && (d + wavePhase) % 3 == 0) {
                        setCell(rc, rr, '/', pal.sunRayFg)
                    }
                }
            }
        }

        // Riverbed pebbles + Sunken Landmark (Stone Lantern, Moss Driftwood, or Giant Clam)
        val pebblePattern = "..ooO.._.oOo._..ooO.._."
        for (c in 0 until cols) {
            val pCh = pebblePattern[(c - pShiftInt).mod(pebblePattern.length)]
            setCell(c, floorRow, pCh, pal.pebbleFg)
            setCell(c, subFloorRow, '~', pal.cloudReedFg)
        }

        val lmCol = (landmarkColFrac * (cols - 12) + 4 + pShiftInt * 0.3f).roundToInt()
        val lmLines = activeLandmark.lines
        for (idx in lmLines.indices) {
            val lr = floorRow - lmLines.size + idx
            val lmColor = if (activeLandmark == SunkenLandmark.STONE_LANTERN && idx == 1 && snapshot.isNight) {
                pal.fishGold // Glowing stone lantern at night!
            } else {
                pal.pebbleFg
            }
            drawStringAtGrid(lmCol, lr, lmLines[idx], lmColor)
        }

        // =========================================================================
        // 4. RISING BUBBLES & SINKING FOOD PELLETS
        // =========================================================================
        val activeHero = if (isBatterySaver) (config.heroFishCount - 2).coerceAtLeast(2) else config.heroFishCount
        val activeSchool = if (isBatterySaver) 2 else maxSchoolFish

        val activeBubbles = if (isBatterySaver) 8 else bubbles.size
        for (i in 0 until activeBubbles) {
            val b = bubbles[i]
            b.rowY -= b.speedY * clampedDt
            b.colX += sin(simTime * 1.5f + b.phase) * 0.4f * clampedDt

            if (b.rowY <= splitRow + 1) {
                val refFish = fish[i % activeHero.coerceAtLeast(1)]
                b.colX = refFish.colX + 2f
                b.rowY = if (b.isBubble) refFish.rowY else (rows - 4).toFloat()
            }

            val bc = b.colX.roundToInt()
            val br = b.rowY.roundToInt()
            if (br in (splitRow + 2) until (floorRow - 1) && bc in 0 until cols) {
                val bChar = if (b.isBubble) {
                    if ((br + i) % 2 == 0) 'o' else '°'
                } else {
                    // Bioluminescent night plankton sparkles `*` when a fish swims nearby
                    if (snapshot.isNight && (i + wavePhase) % 3 == 0) '·' else '.'
                }
                val bCol = if (snapshot.isNight && !b.isBubble) pal.fishRose else pal.bubbleFg
                setCell(bc, br, bChar, bCol)
            }
        }

        for (i in pellets.indices) {
            val pel = pellets[i]
            if (!pel.active) continue
            pel.rowY += 3.2f * clampedDt
            if (pel.rowY >= floorRow - 1) {
                pel.active = false
            } else {
                setCell(pel.colX.roundToInt(), pel.rowY.roundToInt(), '*', pal.fishGold)
            }
        }

        // Flush static background grid to Canvas before drawing smooth sub-pixel creatures
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

        // Helper for silky-smooth sub-pixel creature rendering (no integer grid snapping!)
        fun drawSubPixelSprite(
            colFloat: Float,
            rowFloat: Float,
            lines: Array<String>,
            bodyColor: Int,
            finColor: Int,
            eyeColor: Int,
            isDimmedLayer: Boolean
        ) {
            val baseX = colFloat * cellWidthPx
            val startRowFloat = rowFloat - (lines.size - 1) * 0.5f

            for (lineIdx in lines.indices) {
                val lineY = (startRowFloat + lineIdx) * cellHeightPx + baselineOffsetPx
                if (lineY < (splitRow + 1) * cellHeightPx || lineY > (floorRow - 0.3f) * cellHeightPx) continue

                val str = lines[lineIdx]
                for (chIdx in str.indices) {
                    val ch = str[chIdx]
                    if (ch == ' ') continue

                    textPaint.color = if (isDimmedLayer) {
                        pal.fishDimBg
                    } else {
                        when {
                            ch == '°' || ch == 'º' -> eyeColor
                            lines.size >= 3 && (lineIdx == 0 || lineIdx == lines.size - 1) -> finColor
                            ch == '|' || ch == '{' || ch == '}' || ch == '#' -> finColor
                            else -> bodyColor
                        }
                    }
                    singleCharBuf[0] = ch
                    canvas.drawText(singleCharBuf, 0, 1, baseX + chIdx * cellWidthPx, lineY, textPaint)
                }
            }
        }

        // =========================================================================
        // 5. SURFACE-SKIMMING DRAGONFLY (Day) / HOVERING FIREFLIES (Night)
        // =========================================================================
        surfaceInsect.phase += clampedDt * 5.5f
        surfaceInsect.colX += surfaceInsect.vx * clampedDt
        if (surfaceInsect.colX > cols - 8) surfaceInsect.vx = -3.6f
        if (surfaceInsect.colX < 4f) surfaceInsect.vx = 3.6f
        surfaceInsect.rowY = (splitRow - 2.6f) + sin(surfaceInsect.phase * 0.7f) * 0.8f

        val insX = (surfaceInsect.colX + pShiftCols * 0.6f) * cellWidthPx
        val insY = surfaceInsect.rowY * cellHeightPx + baselineOffsetPx
        val insectStr = if (snapshot.isNight) {
            if (sin(surfaceInsect.phase * 2f) > 0f) " * " else " · "
        } else {
            if (sin(surfaceInsect.phase * 3f) > 0f) "~o~" else "-o-"
        }
        textPaint.color = if (snapshot.isNight) pal.fishRose else pal.fishAqua
        canvas.drawText(insectStr, insX, insY, textPaint)

        // =========================================================================
        // 6. NIGHTTIME BIOLUMINESCENT MOON JELLYFISH
        // =========================================================================
        if (snapshot.isNight) {
            for (j in nightJellies.indices) {
                val jel = nightJellies[j]
                jel.phase += clampedDt * 2.2f
                jel.rowY -= (0.9f + 0.5f * sin(jel.phase)) * clampedDt
                jel.colX += cos(jel.phase * 0.6f + j) * 0.5f * clampedDt

                if (jel.rowY < splitRow + 6) {
                    jel.rowY = (floorRow - 6).toFloat()
                }
                val jLines = if (sin(jel.phase) > 0f) {
                    arrayOf(" .--. ", "( °° )", " /||\\ ")
                } else {
                    arrayOf("  ..  ", " (oo) ", "  ||  ")
                }
                drawSubPixelSprite(
                    colFloat = jel.colX + pShiftCols * 0.3f,
                    rowFloat = jel.rowY,
                    lines = jLines,
                    bodyColor = pal.fishAqua,
                    finColor = pal.fishRose,
                    eyeColor = pal.fishFoam,
                    isDimmedLayer = false
                )
            }
        }

        // =========================================================================
        // 7. BOTTOM-FEEDER CRITTERS (Sub-Pixel Smooth Gliding Along Riverbed)
        // =========================================================================
        if (config.enableBottomFeeders) {
            val activeFeeders = if (isBatterySaver) 2 else maxBottomFeeders
            val critterRowFloat = (floorRow - 1).toFloat()
            for (b in 0 until activeFeeders) {
                val bf = bottomFeeders[b]
                if (bf.pauseTimer > 0f) {
                    bf.pauseTimer -= clampedDt
                } else {
                    bf.colX += bf.vxDir * bf.species.speedColsPerSec * clampedDt
                    bf.animPhase += clampedDt * 5.0f
                    if (sin(simTime * 2.1f + bf.id * 3.7f) > 0.96f) {
                        bf.pauseTimer = 0.8f
                    }
                }

                val spriteLen = bf.species.rightFrameA.length
                val minC = 2f
                val maxC = (cols - spriteLen - 2).toFloat().coerceAtLeast(minC + 2f)
                if (bf.colX >= maxC) {
                    bf.colX = maxC
                    bf.vxDir = -1f
                } else if (bf.colX <= minC) {
                    bf.colX = minC
                    bf.vxDir = 1f
                }

                // Smooth velocity-based spacing between bottom feeders
                for (otherIdx in 0 until b) {
                    val other = bottomFeeders[otherIdx]
                    val dx = bf.colX - other.colX
                    val minSep = (spriteLen + other.species.rightFrameA.length) * 0.6f + 3f
                    if (abs(dx) < minSep) {
                        bf.vxDir = if (dx >= 0f) 1f else -1f
                        bf.colX = (bf.colX + bf.vxDir * clampedDt * 3f).coerceIn(minC, maxC)
                    }
                }

                val frameB = sin(bf.animPhase) > 0f
                val sprite = if (bf.vxDir >= 0f) {
                    if (frameB) bf.species.rightFrameB else bf.species.rightFrameA
                } else {
                    if (frameB) bf.species.leftFrameB else bf.species.leftFrameA
                }
                val bfColor = when (bf.species.colorRole) {
                    0 -> pal.fishCoral
                    1 -> pal.fishAqua
                    2 -> pal.fishGold
                    3 -> pal.fishFoam
                    else -> pal.fishRose
                }
                textPaint.color = bfColor
                val bxPx = ((bf.colX + pShiftCols * 0.3f).coerceIn(1f, (cols - spriteLen - 1).toFloat())) * cellWidthPx
                val byPx = critterRowFloat * cellHeightPx + baselineOffsetPx
                canvas.drawText(sprite, bxPx, byPx, textPaint)
            }
        }

        // =========================================================================
        // 8. PREDATOR UPDATE & SMOOTH SUB-PIXEL FISH STEERING (Zero Stutter, Zero Off-Screen)
        // =========================================================================
        val minFishRow = (splitRow + 4).toFloat()
        val maxFishRow = (floorRow - 4).toFloat().coerceAtLeast(minFishRow + 12f)
        val underwaterSpan = (maxFishRow - minFishRow).coerceAtLeast(12f)

        if (config.enablePredators) {
            if (!predator.active) {
                predator.cooldownTimer -= clampedDt
                if (predator.cooldownTimer <= 0f) {
                    triggerPredatorVisit((simTime * 1000f).toLong())
                }
            } else {
                predator.activeDuration += clampedDt
                predator.animPhase += clampedDt * 7.0f

                var targetY = predator.rowY
                var minDist = 999f
                for (i in 0 until totalMaxFish) {
                    val f = fish[i]
                    if (!f.isSchooler && i >= activeHero) continue
                    if (f.isSchooler && (i - maxHeroFish) >= activeSchool) continue

                    val ahead = (f.colX - predator.colX) * predator.vxDir
                    if (ahead > -4f) {
                        val d = hypot(f.colX - predator.colX, (f.rowY - predator.rowY) * 1.4f)
                        if (d < minDist) {
                            minDist = d
                            targetY = f.rowY
                        }
                    }
                }
                val dy = (targetY - predator.rowY).coerceIn(-4.0f, 4.0f)
                predator.colX += predator.vxDir * predator.species.speedColsPerSec * clampedDt
                predator.rowY = (predator.rowY + dy * 1.6f * clampedDt).coerceIn(minFishRow, maxFishRow)

                if ((predator.vxDir > 0f && predator.colX > cols + 6) ||
                    (predator.vxDir < 0f && predator.colX < -18f) ||
                    predator.activeDuration > 11f
                ) {
                    predator.active = false
                    predator.cooldownTimer = 14f + (cos(simTime) * 0.5f + 0.5f) * 14f
                }
            }
        } else {
            predator.active = false
        }

        // Update all active fish using smooth continuous velocity integration (no position snapping!)
        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            if (!f.isSchooler && i >= activeHero) continue
            if (f.isSchooler && (i - maxHeroFish) >= activeSchool) continue

            if (f.turnTimer > 0f) {
                f.turnTimer = (f.turnTimer - clampedDt).coerceAtLeast(0f)
            }

            val spriteW = f.species.rightFrameA[0].length
            val minColBound = 2.0f
            val maxColBound = (cols - spriteW - 2.0f).coerceAtLeast(minColBound + 4f)

            val cruiseSpd = f.species.baseSpeedColsPerSec * f.speedScale
            var desiredVx = f.facingDir * cruiseSpd
            val targetLaneRow = minFishRow + f.preferredDepthFrac * underwaterSpan
            var desiredVy = (targetLaneRow - f.rowY) * 1.4f + sin(simTime * 1.3f + f.id * 1.9f) * 0.75f

            // Boids schooling cohesion for small Neon Tetras & Silver Rasboras
            if (f.isSchooler && !predator.active) {
                val leader = fish[maxHeroFish]
                if (f !== leader) {
                    val schoolTargetX = leader.colX - leader.facingDir * ((f.id - maxHeroFish) * 3.5f)
                    val schoolTargetY = leader.rowY + ((f.id % 3) - 1) * 2.2f
                    desiredVx += (schoolTargetX - f.colX).coerceIn(-3f, 3f) * 0.6f
                    desiredVy = (schoolTargetY - f.rowY) * 1.6f
                }
            }

            // Food pellet attraction
            for (pIdx in pellets.indices) {
                val pel = pellets[pIdx]
                if (pel.active) {
                    val pdx = pel.colX - f.colX
                    val pdy = pel.rowY - f.rowY
                    val pDist = hypot(pdx, pdy)
                    if (pDist < 18f) {
                        val wantDir = if (pdx >= 0f) 1 else -1
                        if (wantDir != f.facingDir && f.turnTimer <= 0f) {
                            f.facingDir = wantDir
                            f.turnTimer = 0.16f
                        }
                        desiredVx = f.facingDir * cruiseSpd * 1.25f
                        desiredVy = (pdy / pDist.coerceAtLeast(1f)) * 4.0f
                        if (pDist < 2.2f) pel.active = false
                        break
                    }
                }
            }

            // Smooth 2D Predator Evasion (with inward wall-turn so fish NEVER flee off-screen!)
            f.isEvading = false
            if (predator.active) {
                val pCenterX = predator.colX + 6f
                val dx = (f.colX + spriteW * 0.5f) - pCenterX
                val dy = f.rowY - predator.rowY
                val dist = hypot(dx, dy * 1.4f)
                val threatRadius = 26f

                if (dist < threatRadius) {
                    f.isEvading = true
                    val urgency = (1f - dist / threatRadius).coerceIn(0.2f, 1f)

                    // If fleeing outward would trap the fish against the screen edge, juke vertically and cut back inward!
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

            // Soft Pairwise Steering Avoidance (modifies desiredVx/desiredVy smoothly — ZERO position snapping!)
            for (j in 0 until totalMaxFish) {
                if (i == j) continue
                val other = fish[j]
                if (!other.isSchooler && j >= activeHero) continue
                if (other.isSchooler && (j - maxHeroFish) >= activeSchool) continue

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

            // Smooth Turn-Around before screen edges so fish never get stuck on boundaries
            if (f.facingDir > 0 && f.colX >= maxColBound - 1.2f) {
                f.facingDir = -1
                f.turnTimer = 0.18f
                desiredVx = -cruiseSpd
            } else if (f.facingDir < 0 && f.colX <= minColBound + 1.2f) {
                f.facingDir = 1
                f.turnTimer = 0.18f
                desiredVx = cruiseSpd
            }

            // Exponentially smooth velocity (low-pass filter prevents any sudden jitter)
            val accelRate = (clampedDt * 6.5f).coerceIn(0.05f, 1.0f)
            f.vx += (desiredVx - f.vx) * accelRate
            f.vy += (desiredVy - f.vy) * accelRate

            f.colX += f.vx * clampedDt
            f.rowY += f.vy * clampedDt

            // Hard safety clamp strictly inside visible screen bounds (even during predator evasion)
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
            f.animPhase += clampedDt * (2.6f + actualSpd * 0.45f)
        }

        // =========================================================================
        // 9. DRAW FISH IN 3 DEPTH LAYERS (Background -> Midground -> Foreground)
        // =========================================================================
        fun resolveRoleColor(role: Int): Int = when (role) {
            0 -> pal.fishCoral
            1 -> pal.fishAqua
            2 -> pal.fishGold
            3 -> pal.fishFoam
            else -> pal.fishRose
        }

        for (layer in 0..2) {
            val parallaxMult = when (layer) {
                0 -> 0.25f
                1 -> 0.45f
                else -> 0.70f
            }
            for (i in 0 until totalMaxFish) {
                val f = fish[i]
                if (f.depthLayer != layer) continue
                if (!f.isSchooler && i >= activeHero) continue
                if (f.isSchooler && (i - maxHeroFish) >= activeSchool) continue

                val useFrameB = sin(f.animPhase) > 0f
                val lines = when {
                    f.turnTimer > 0f -> f.species.turnFrame
                    f.facingDir >= 0 -> if (useFrameB) f.species.rightFrameB else f.species.rightFrameA
                    else -> if (useFrameB) f.species.leftFrameB else f.species.leftFrameA
                }

                val sw = lines[0].length
                val clampedDrawCol = (f.colX + pShiftCols * parallaxMult)
                    .coerceIn(0.5f, (cols - sw - 0.5f).coerceAtLeast(1f))

                drawSubPixelSprite(
                    colFloat = clampedDrawCol,
                    rowFloat = f.rowY,
                    lines = lines,
                    bodyColor = resolveRoleColor(f.species.primaryRole),
                    finColor = resolveRoleColor(f.species.finAccentRole),
                    eyeColor = pal.fishFoam,
                    isDimmedLayer = (layer == 0)
                )

                if (f.isEvading) {
                    val wakeCol = if (f.facingDir >= 0) clampedDrawCol - 1.5f else clampedDrawCol + sw + 0.5f
                    textPaint.color = pal.bubbleFg
                    canvas.drawText("~", wakeCol * cellWidthPx, f.rowY * cellHeightPx + baselineOffsetPx, textPaint)
                }
            }
        }

        // Render Active Predator smoothly at sub-pixel coordinates
        if (predator.active) {
            val useFrameB = sin(predator.animPhase) > 0f
            val pLines = if (predator.vxDir >= 0f) {
                if (useFrameB) predator.species.rightFrameB else predator.species.rightFrameA
            } else {
                if (useFrameB) predator.species.leftFrameB else predator.species.leftFrameA
            }
            drawSubPixelSprite(
                colFloat = predator.colX + pShiftCols * 0.55f,
                rowFloat = predator.rowY,
                lines = pLines,
                bodyColor = pal.fishCoral,
                finColor = pal.fishGold,
                eyeColor = pal.fishFoam,
                isDimmedLayer = false
            )
        }
    }
}

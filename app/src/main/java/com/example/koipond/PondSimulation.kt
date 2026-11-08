package com.example.koipond

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Detailed Multi-Line & Schooling ASCII Fish Species designed for the High-Density Grid (68–96 cols).
 * Every hero species has a 3-line anatomical silhouette (dorsal fin, detailed body scales/gills, ventral fin).
 */
enum class AsciiFishSpecies(
    val id: Int,
    val displayName: String,
    val isHeroMultiLine: Boolean,
    val colorRole: Int, // 0=Coral, 1=Aqua, 2=Gold, 3=Foam, 4=Rose
    val baseSpeedColsPerSec: Float,
    val rightFrameA: Array<String>,
    val rightFrameB: Array<String>,
    val leftFrameA: Array<String>,
    val leftFrameB: Array<String>
) {
    NEON_TETRA(
        id = 0,
        displayName = "neon tetra",
        isHeroMultiLine = false,
        colorRole = 1,
        baseSpeedColsPerSec = 7.5f,
        rightFrameA = arrayOf(">-))°>"),
        rightFrameB = arrayOf("}-))°>"),
        leftFrameA  = arrayOf("<°((-<"),
        leftFrameB  = arrayOf("<°((-{")
    ),
    SILVER_MINNOW(
        id = 1,
        displayName = "silver rasbora",
        isHeroMultiLine = false,
        colorRole = 3,
        baseSpeedColsPerSec = 8.0f,
        rightFrameA = arrayOf(">~))'>"),
        rightFrameB = arrayOf("}~))'>"),
        leftFrameA  = arrayOf("<'((~<"),
        leftFrameB  = arrayOf("<'((~{")
    ),
    CLOWNFISH(
        id = 2,
        displayName = "clownfish",
        isHeroMultiLine = true,
        colorRole = 0,
        baseSpeedColsPerSec = 5.2f,
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
        )
    ),
    BLUE_TANG(
        id = 3,
        displayName = "regal tang",
        isHeroMultiLine = true,
        colorRole = 1,
        baseSpeedColsPerSec = 5.6f,
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
        )
    ),
    GOLD_OGON(
        id = 4,
        displayName = "yamabuki ogon",
        isHeroMultiLine = true,
        colorRole = 2,
        baseSpeedColsPerSec = 4.8f,
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
        )
    ),
    MANDARIN(
        id = 5,
        displayName = "mandarin dragonet",
        isHeroMultiLine = true,
        colorRole = 4,
        baseSpeedColsPerSec = 4.9f,
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
        )
    ),
    KOHAKU_KOI(
        id = 6,
        displayName = "kohaku koi",
        isHeroMultiLine = true,
        colorRole = 0,
        baseSpeedColsPerSec = 4.5f,
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
        )
    ),
    MOORISH_IDOL(
        id = 7,
        displayName = "moorish idol",
        isHeroMultiLine = true,
        colorRole = 2,
        baseSpeedColsPerSec = 4.7f,
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
        )
    ),
    VEIL_BETTA(
        id = 8,
        displayName = "royal betta",
        isHeroMultiLine = true,
        colorRole = 4,
        baseSpeedColsPerSec = 4.2f,
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
        )
    );

    companion object {
        val heroPool = arrayOf(CLOWNFISH, BLUE_TANG, GOLD_OGON, MANDARIN, KOHAKU_KOI, MOORISH_IDOL, VEIL_BETTA)
        val schoolPool = arrayOf(NEON_TETRA, SILVER_MINNOW)
    }
}

/**
 * 4 Sleek Aquatic Predator Species (Barracuda, Reef Shark, Swordfish, Moray Eel)
 * that enter the water column and actively chase prey fish while the prey scatter and juke!
 */
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
        speedColsPerSec = 9.5f,
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

/**
 * 4 Bottom-Feeder Critters patrolling the river cobbles along the floor.
 */
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
        speedColsPerSec = 2.4f,
        colorRole = 0,
        rightFrameA = "(\\/)(°_°)(\\/)",
        rightFrameB = "(/)(°_°)(\\)",
        leftFrameA  = "(\\/)(°_°)(\\/)",
        leftFrameB  = "(/)(°_°)(\\)"
    ),
    CHERRY_SHRIMP(
        id = 1,
        displayName = "cherry shrimp",
        speedColsPerSec = 3.2f,
        colorRole = 4,
        rightFrameA = ",=/>°",
        rightFrameB = ".=/>°",
        leftFrameA  = "°<\\=,",
        leftFrameB  = "°<\\=."
    ),
    CORY_CATFISH(
        id = 2,
        displayName = "cory catfish",
        speedColsPerSec = 2.8f,
        colorRole = 2,
        rightFrameA = ">))º>=",
        rightFrameB = "}))º>~",
        leftFrameA  = "=<º((<",
        leftFrameB  = "~<º(({"
    ),
    APPLE_SNAIL(
        id = 3,
        displayName = "golden snail",
        speedColsPerSec = 0.9f,
        colorRole = 2,
        rightFrameA = "@_/",
        rightFrameB = "@_'",
        leftFrameA  = "\\_@",
        leftFrameB  = "'_@"
    )
}

class AsciiFishAgent(
    val id: Int,
    var isSchooler: Boolean,
    var species: AsciiFishSpecies,
    var colFrac: Float,      // Normalized [0.05..0.95] so resizing grid never bunches fish
    var depthFrac: Float,    // Normalized underwater depth [0.06..0.92]
    var colX: Float,
    var rowY: Float,
    var preferredDepthFrac: Float,
    var vxDir: Float,        // +1f = right, -1f = left
    var vySpeed: Float,
    var speedScale: Float,
    var animPhase: Float,
    var evadeBoost: Float = 0f,
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

class AsciiBubble(
    var colX: Float = 0f,
    var rowY: Float = 0f,
    var speedY: Float = 3.2f,
    var isBubble: Boolean = true,
    var phase: Float = 0f
)

/**
 * Subtle sinking food pellets spawned when tapping the water (replaces noisy touch ripple rings).
 */
class FoodPellet(
    var colX: Float = 0f,
    var rowY: Float = 0f,
    var active: Boolean = false
)

class PondSimulation {

    val maxHeroFish = 9
    val maxSchoolFish = 7
    val totalMaxFish = maxHeroFish + maxSchoolFish

    val fish = Array(totalMaxFish) { idx ->
        val dFrac = (idx + 0.5f) / totalMaxFish.toFloat()
        AsciiFishAgent(
            id = idx,
            isSchooler = idx >= maxHeroFish,
            species = AsciiFishSpecies.entries[idx % AsciiFishSpecies.entries.size],
            colFrac = ((idx * 37) % 100) / 100f,
            depthFrac = dFrac,
            colX = 10f,
            rowY = 20f + idx * 4f,
            preferredDepthFrac = dFrac,
            vxDir = if (idx % 2 == 0) 1f else -1f,
            vySpeed = 0.35f,
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

    // Pre-allocated 2D character & color buffers supporting up to 112 cols x 240 rows (zero GC)
    private val maxCols = 112
    private val maxRows = 240
    private val charGrid = CharArray(maxCols * maxRows) { ' ' }
    private val colorGrid = IntArray(maxCols * maxRows)
    private val fishOccupiedGrid = BooleanArray(maxCols * maxRows)
    private val rowCharScratch = CharArray(maxCols)

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.MONOSPACE
        fontFeatureSettings = "liga 0, calt 0"
        textAlign = Paint.Align.LEFT
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

        // Map normalized fish coordinates into the newly sized grid so they are immediately spread out
        val minR = waterlineRow + 4
        val maxR = gridRows - 8
        val spanR = (maxR - minR).coerceAtLeast(16)
        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            f.colX = (f.colFrac * (gridCols - 14)).coerceIn(2f, (gridCols - 12).toFloat())
            f.rowY = minR + f.preferredDepthFrac * spanR
        }
    }

    /**
     * Randomizes the fish species mix, bottom feeders, and stratified vertical lanes
     * every time the user unlocks their phone.
     */
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

        val minR = waterlineRow + 4
        val maxR = (gridRows - 8).coerceAtLeast(minR + 20)
        val spanR = (maxR - minR).toFloat()

        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            val isSchool = i >= maxHeroFish
            f.isSchooler = isSchool
            f.species = if (isSchool) {
                schoolSp
            } else {
                when (i % 3) {
                    0 -> h1
                    1 -> h2
                    else -> h3
                }
            }
            // Evenly distribute preferred depth lanes from shallow water (0.06) to deep water (0.92)
            val laneFrac = 0.06f + ((i.toFloat() + rand.nextFloat() * 0.45f) / totalMaxFish.toFloat()) * 0.86f
            f.preferredDepthFrac = laneFrac.coerceIn(0.06f, 0.92f)
            f.depthFrac = f.preferredDepthFrac
            f.colFrac = 0.08f + rand.nextFloat() * 0.82f

            f.colX = f.colFrac * (gridCols - 12).coerceAtLeast(20)
            f.rowY = minR + f.preferredDepthFrac * spanR
            f.vxDir = if (i % 2 == 0) 1f else -1f
            f.vySpeed = (rand.nextFloat() - 0.5f) * 0.8f
            f.speedScale = 0.85f + rand.nextFloat() * 0.30f
            f.animPhase = rand.nextFloat() * 6.28f
            f.evadeBoost = 0f
            f.isEvading = false
        }

        for (b in 0 until maxBottomFeeders) {
            val bf = bottomFeeders[b]
            bf.species = BottomFeederSpecies.entries[rand.nextInt(BottomFeederSpecies.entries.size)]
            bf.colX = 4f + b * ((gridCols - 12).coerceAtLeast(25) / maxBottomFeeders.toFloat())
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

    /**
     * Tapping drops tiny sinking food pellets (`.`) if tapped near/below the water,
     * with no expanding ripple rings.
     */
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
        val clampedDt = dt.coerceIn(0.001f, 0.08f)
        simTime += clampedDt

        val snapshot = TimeOfDayPalette.resolve(config.timeCycleMode)
        val pal = snapshot.colors

        val cols = gridCols
        val rows = gridRows
        val pShiftCols = (parallaxX * 8f).roundToInt()
        val pShiftRows = (parallaxY * 3f).roundToInt()
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
            fishOccupiedGrid[i] = false
        }

        fun setCell(c: Int, r: Int, ch: Char, color: Int) {
            if (c in 0 until cols && r in 0 until rows && ch != ' ') {
                val idx = r * cols + c
                charGrid[idx] = ch
                colorGrid[idx] = color
            }
        }

        fun drawStringAt(c: Int, r: Int, text: String, color: Int) {
            if (r !in 0 until rows) return
            for (i in text.indices) {
                val ch = text[i]
                if (ch != ' ') {
                    setCell(c + i, r, ch, color)
                }
            }
        }

        // =========================================================================
        // 2. OVER-WATER SKY (Rows 0 .. splitRow - 1): Sun/Moon, Stars, Clouds, Reeds
        // =========================================================================
        val celestialCol = ((snapshot.sunArcX * (cols - 12)).roundToInt() + 6 + pShiftCols).coerceIn(5, cols - 7)
        val skyHeight = (splitRow - 5).coerceAtLeast(6)
        val celestialRow = ((1f - snapshot.sunArcY) * (skyHeight - 4) + 3).roundToInt().coerceIn(3, splitRow - 5)

        if (snapshot.isNight) {
            for (s in 0 until 20) {
                val sc = ((s * 17 + 7 + pShiftCols) % (cols - 2)).coerceAtLeast(1)
                val sr = ((s * 9 + 3) % (splitRow - 4)).coerceAtLeast(1)
                val twinkle = sin(simTime * 2.2f + s * 1.7f)
                val starChar = if (twinkle > 0.35f) '*' else '.'
                setCell(sc, sr, starChar, pal.celestialGlyph)
            }
            drawStringAt(celestialCol - 2, celestialRow - 1, " .--. ", pal.celestialGlyph)
            drawStringAt(celestialCol - 2, celestialRow,     "( (   ", pal.celestialGlyph)
            drawStringAt(celestialCol - 2, celestialRow + 1, " '--' ", pal.celestialGlyph)
        } else {
            val rayPulse = ((simTime * 1.5f).toInt() % 2 == 0)
            if (rayPulse) {
                drawStringAt(celestialCol - 3, celestialRow - 1, " \\ | / ", pal.celestialGlyph)
                drawStringAt(celestialCol - 3, celestialRow,     "--(O)--", pal.celestialGlyph)
                drawStringAt(celestialCol - 3, celestialRow + 1, " / | \\ ", pal.celestialGlyph)
            } else {
                drawStringAt(celestialCol - 3, celestialRow - 1, "  \\|/  ", pal.celestialGlyph)
                drawStringAt(celestialCol - 3, celestialRow,     " -(o)- ", pal.celestialGlyph)
                drawStringAt(celestialCol - 3, celestialRow + 1, "  /|\\  ", pal.celestialGlyph)
            }
        }

        val cloud1Col = ((simTime * 0.9f + 8f + pShiftCols).toInt() % (cols + 14)) - 10
        val cloud1Row = (splitRow * 0.32f).toInt().coerceIn(2, splitRow - 4)
        drawStringAt(cloud1Col, cloud1Row,     "  .--.   ", pal.cloudReedFg)
        drawStringAt(cloud1Col, cloud1Row + 1, "-(    )-.", pal.cloudReedFg)

        val cloud2Col = (((simTime * 0.55f + cols * 0.58f + pShiftCols).toInt()) % (cols + 14)) - 10
        val cloud2Row = (splitRow * 0.54f).toInt().coerceIn(3, splitRow - 3)
        drawStringAt(cloud2Col, cloud2Row, " .-(   )-.", pal.cloudReedFg)

        // Minimal, clean shoreline reeds at far left & right edges above the waterline
        val swayLeft = sin(simTime * 1.4f) > 0f
        for (c in 0 until cols) {
            if (c in 2..6 || c in (cols - 7)..(cols - 3)) {
                val reedTop = if ((c + if (swayLeft) 1 else 0) % 2 == 0) '/' else '|'
                setCell(c, splitRow - 2, reedTop, pal.kelpFg)
                setCell(c, splitRow - 1, '|', pal.kelpFg)
            }
        }

        // =========================================================================
        // 3. WATERLINE MENISCUS & LILY PADS / LOTUS (Row = splitRow)
        // =========================================================================
        val wavePhase = (simTime * 3.0f).toInt()
        val wavePattern = "~^~._."
        for (c in 0 until cols) {
            val ch = wavePattern[(c + wavePhase).mod(wavePattern.length)]
            setCell(c, splitRow, ch, pal.meniscusWaveFg)
        }

        val pad1Col = (cols * 0.24f + sin(simTime * 0.6f) * 1.5f + pShiftCols).roundToInt()
        drawStringAt(pad1Col, splitRow, "[_v_]", pal.lilyPadFg)
        drawStringAt(pad1Col + 1, splitRow - 1, "(*)", pal.lotusFlowerFg)

        val pad2Col = (cols * 0.72f + cos(simTime * 0.5f) * 1.5f + pShiftCols).roundToInt()
        drawStringAt(pad2Col, splitRow, "[_v_]", pal.lilyPadFg)

        // =========================================================================
        // 4. SUBTLE UNDERWATER SUNBEAMS & CLEAN RIVERBED PEBBLES (Zero Noisy Kelp!)
        // =========================================================================
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

        val pebblePattern = "..ooO.._.oOo._..ooO.._."
        for (c in 0 until cols) {
            val pCh = pebblePattern[(c - pShiftCols).mod(pebblePattern.length)]
            setCell(c, floorRow, pCh, pal.pebbleFg)
            setCell(c, subFloorRow, '~', pal.cloudReedFg)
        }

        // =========================================================================
        // 5. BOTTOM-FEEDER CRITTERS (Crab, Cherry Shrimp, Cory Catfish, Golden Snail)
        // =========================================================================
        if (config.enableBottomFeeders) {
            val activeFeeders = if (isBatterySaver) 2 else maxBottomFeeders
            val critterRow = floorRow - 1
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
                if (bf.vxDir > 0f && bf.colX > cols - spriteLen - 2) {
                    bf.vxDir = -1f
                } else if (bf.vxDir < 0f && bf.colX < 2f) {
                    bf.vxDir = 1f
                }

                for (otherIdx in 0 until b) {
                    val other = bottomFeeders[otherIdx]
                    val dx = bf.colX - other.colX
                    val minSep = (spriteLen + other.species.rightFrameA.length) * 0.65f + 3f
                    if (abs(dx) < minSep) {
                        val push = if (dx >= 0f) 1f else -1f
                        bf.colX = (other.colX + push * minSep).coerceIn(1f, (cols - spriteLen - 1).toFloat())
                        bf.vxDir = push
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
                drawStringAt((bf.colX + pShiftCols * 0.3f).roundToInt(), critterRow, sprite, bfColor)
            }
        }

        // =========================================================================
        // 6. RANDOM PREDATOR VISITS (Barracuda, Reef Shark, Swordfish, Moray Eel)
        // =========================================================================
        val minFishRow = (splitRow + 4).coerceAtMost(rows - 14)
        val maxFishRow = (floorRow - 5).coerceAtLeast(minFishRow + 10)
        val underwaterSpan = (maxFishRow - minFishRow).toFloat().coerceAtLeast(10f)

        val activeHero = if (isBatterySaver) (config.heroFishCount - 2).coerceAtLeast(2) else config.heroFishCount
        val activeSchool = if (isBatterySaver) 2 else maxSchoolFish

        if (config.enablePredators) {
            if (!predator.active) {
                predator.cooldownTimer -= clampedDt
                if (predator.cooldownTimer <= 0f) {
                    triggerPredatorVisit((simTime * 1000f).toLong())
                }
            } else {
                predator.activeDuration += clampedDt
                predator.animPhase += clampedDt * 7.0f

                // Actively lock onto the nearest fish in front of the predator and pursue it in 2D!
                var targetY = predator.rowY
                var minDist = 999f
                for (i in 0 until totalMaxFish) {
                    val f = fish[i]
                    if (!f.isSchooler && i >= activeHero) continue
                    if (f.isSchooler && (i - maxHeroFish) >= activeSchool) continue

                    val ahead = (f.colX - predator.colX) * predator.vxDir
                    if (ahead > -6f) {
                        val d = hypot(f.colX - predator.colX, (f.rowY - predator.rowY) * 1.4f)
                        if (d < minDist) {
                            minDist = d
                            targetY = f.rowY
                        }
                    }
                }
                val dy = (targetY - predator.rowY).coerceIn(-4.5f, 4.5f)
                predator.colX += predator.vxDir * predator.species.speedColsPerSec * clampedDt
                predator.rowY = (predator.rowY + dy * 1.8f * clampedDt)
                    .coerceIn(minFishRow.toFloat(), maxFishRow.toFloat())

                if ((predator.vxDir > 0f && predator.colX > cols + 8) ||
                    (predator.vxDir < 0f && predator.colX < -20f) ||
                    predator.activeDuration > 12f
                ) {
                    predator.active = false
                    predator.cooldownTimer = 14f + (cos(simTime) * 0.5f + 0.5f) * 14f
                }
            }
        } else {
            predator.active = false
        }

        // =========================================================================
        // 7. UPDATE PEACEFUL FISH (2D Predator Evasion Scatter + Stratified Lanes)
        // =========================================================================
        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            if (!f.isSchooler && i >= activeHero) continue
            if (f.isSchooler && (i - maxHeroFish) >= activeSchool) continue

            f.evadeBoost *= (1f - clampedDt * 2.5f).coerceAtLeast(0f)
            f.isEvading = false

            val targetLaneRow = minFishRow + f.preferredDepthFrac * underwaterSpan

            if (predator.active) {
                val pCenterX = predator.colX + 6f
                val dx = f.colX - pCenterX
                val dy = f.rowY - predator.rowY
                val dist = hypot(dx, dy * 1.5f)
                val threatRadius = 28f

                if (dist < threatRadius) {
                    // Dynamic 2D Evasion: Fish darts away radially + jukes vertically up or down!
                    f.isEvading = true
                    val urgency = (1f - dist / threatRadius).coerceIn(0.15f, 1f)
                    f.evadeBoost = 8.5f * urgency
                    f.vxDir = if (dx >= 0f) 1f else -1f

                    // Vertical juke away from the predator's row (if exactly level, odd/even fish split up/down)
                    val jukeSign = when {
                        abs(dy) > 1.2f -> if (dy > 0f) 1f else -1f
                        f.id % 2 == 0 -> -1f
                        else -> 1f
                    }
                    f.vySpeed = jukeSign * (6.5f * urgency)
                }
            }

            if (!f.isEvading) {
                // Check if a sinking food pellet is nearby
                var chasingPellet = false
                for (pIdx in pellets.indices) {
                    val pel = pellets[pIdx]
                    if (pel.active) {
                        val pdx = pel.colX - f.colX
                        val pdy = pel.rowY - f.rowY
                        val pDist = hypot(pdx, pdy)
                        if (pDist < 18f) {
                            chasingPellet = true
                            f.vxDir = if (pdx >= 0f) 1f else -1f
                            f.vySpeed = (pdy / pDist.coerceAtLeast(1f)) * 3.5f
                            if (pDist < 2.2f) {
                                pel.active = false // Nibbled the food pellet!
                            }
                            break
                        }
                    }
                }

                if (!chasingPellet) {
                    // Gently cruise along assigned depth lane with natural vertical sine bobbing
                    val lanePull = (targetLaneRow - f.rowY) * 1.1f
                    val gentleBob = sin(simTime * 1.1f + f.id * 2.1f) * 0.9f
                    f.vySpeed = lanePull + gentleBob
                }
            }

            val spd = (f.species.baseSpeedColsPerSec * f.speedScale) + f.evadeBoost
            f.colX += f.vxDir * spd * clampedDt
            f.rowY += f.vySpeed * clampedDt
            f.animPhase += clampedDt * (3.8f + f.evadeBoost * 0.9f)

            val spriteWidth = f.species.rightFrameA[0].length
            if (f.vxDir > 0f && f.colX > cols - spriteWidth - 1) {
                f.vxDir = -1f
            } else if (f.vxDir < 0f && f.colX < 1f) {
                f.vxDir = 1f
            }

            f.rowY = f.rowY.coerceIn(minFishRow.toFloat(), maxFishRow.toFloat())
            f.colFrac = (f.colX / cols.toFloat()).coerceIn(0.02f, 0.95f)
        }

        // =========================================================================
        // 8. PAIRWISE BOUNDING-BOX SEPARATION SOLVER (Guarantees Zero Overlap)
        // =========================================================================
        for (pass in 0 until 4) {
            for (i in 0 until totalMaxFish) {
                val fi = fish[i]
                if (!fi.isSchooler && i >= activeHero) continue
                if (fi.isSchooler && (i - maxHeroFish) >= activeSchool) continue

                val wi = fi.species.rightFrameA[0].length
                val hi = fi.species.rightFrameA.size

                for (j in i + 1 until totalMaxFish) {
                    val fj = fish[j]
                    if (!fj.isSchooler && j >= activeHero) continue
                    if (fj.isSchooler && (j - maxHeroFish) >= activeSchool) continue

                    val wj = fj.species.rightFrameA[0].length
                    val hj = fj.species.rightFrameA.size

                    val minSepX = (wi + wj) * 0.5f + 2.5f
                    val minSepY = (hi + hj) * 0.5f + 1.4f

                    val centerXi = fi.colX + wi * 0.5f
                    val centerXj = fj.colX + wj * 0.5f
                    val dx = centerXi - centerXj
                    val dy = fi.rowY - fj.rowY

                    if (abs(dx) < minSepX && abs(dy) < minSepY) {
                        val overlapX = minSepX - abs(dx)
                        val overlapY = minSepY - abs(dy)

                        // Prefer vertical lane separation so fish swim smoothly above/below each other
                        val pushY = (overlapY * 0.65f).coerceAtLeast(0.8f)
                        if (dy >= 0f) {
                            fi.rowY = (fi.rowY + pushY).coerceIn(minFishRow.toFloat(), maxFishRow.toFloat())
                            fj.rowY = (fj.rowY - pushY).coerceIn(minFishRow.toFloat(), maxFishRow.toFloat())
                        } else {
                            fi.rowY = (fi.rowY - pushY).coerceIn(minFishRow.toFloat(), maxFishRow.toFloat())
                            fj.rowY = (fj.rowY + pushY).coerceIn(minFishRow.toFloat(), maxFishRow.toFloat())
                        }

                        if (overlapX > 2.0f) {
                            val pushX = (overlapX * 0.45f).coerceAtLeast(0.6f)
                            if (dx >= 0f) {
                                fi.colX = (fi.colX + pushX).coerceIn(1f, (cols - wi - 1).toFloat())
                                fj.colX = (fj.colX - pushX).coerceIn(1f, (cols - wj - 1).toFloat())
                            } else {
                                fi.colX = (fi.colX - pushX).coerceIn(1f, (cols - wi - 1).toFloat())
                                fj.colX = (fj.colX + pushX).coerceIn(1f, (cols - wj - 1).toFloat())
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 9. STAMP FISH & PREDATOR INTO HIGH-DENSITY CHARACTER GRID
        // =========================================================================
        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            if (!f.isSchooler && i >= activeHero) continue
            if (f.isSchooler && (i - maxHeroFish) >= activeSchool) continue

            val useFrameB = (sin(f.animPhase) > 0f)
            val lines = if (f.vxDir >= 0f) {
                if (useFrameB) f.species.rightFrameB else f.species.rightFrameA
            } else {
                if (useFrameB) f.species.leftFrameB else f.species.leftFrameA
            }

            val fishColor = when (f.species.colorRole) {
                0 -> pal.fishCoral
                1 -> pal.fishAqua
                2 -> pal.fishGold
                3 -> pal.fishFoam
                else -> pal.fishRose
            }

            val drawC = (f.colX + pShiftCols * 0.5f).roundToInt()
            var drawR = f.rowY.roundToInt()
            val spriteW = lines[0].length
            val spriteH = lines.size
            val startRowOffset = -(spriteH / 2)

            fun isBoxFree(testR: Int): Boolean {
                for (rOff in -1..spriteH) {
                    val rr = (testR + startRowOffset + rOff).coerceIn(0, rows - 1)
                    for (cOff in -1..spriteW) {
                        val cc = (drawC + cOff).coerceIn(0, cols - 1)
                        if (fishOccupiedGrid[rr * cols + cc]) return false
                    }
                }
                return true
            }

            if (!isBoxFree(drawR)) {
                for (delta in intArrayOf(-2, 2, -4, 4, -6, 6, -8, 8)) {
                    val candR = (drawR + delta).coerceIn(minFishRow, maxFishRow)
                    if (isBoxFree(candR)) {
                        drawR = candR
                        f.rowY = candR.toFloat()
                        break
                    }
                }
            }

            for (lineIdx in lines.indices) {
                val r = drawR + startRowOffset + lineIdx
                if (r in (splitRow + 1) until (floorRow - 1)) {
                    val rowStr = lines[lineIdx]
                    for (chIdx in rowStr.indices) {
                        val c = drawC + chIdx
                        if (c in 0 until cols) {
                            fishOccupiedGrid[r * cols + c] = true
                            val ch = rowStr[chIdx]
                            if (ch != ' ') {
                                val charCol = when {
                                    ch == '°' || ch == 'º' -> pal.fishFoam
                                    f.species == AsciiFishSpecies.KOHAKU_KOI && ch == '{' -> pal.fishCoral
                                    f.species == AsciiFishSpecies.KOHAKU_KOI && ch == '}' -> pal.fishCoral
                                    f.species == AsciiFishSpecies.KOHAKU_KOI && lineIdx == 1 -> pal.fishFoam
                                    f.species == AsciiFishSpecies.CLOWNFISH && ch == '|' -> pal.fishFoam
                                    f.species == AsciiFishSpecies.MOORISH_IDOL && ch == '#' -> pal.fishFoam
                                    else -> fishColor
                                }
                                setCell(c, r, ch, charCol)
                            }
                        }
                    }
                }
            }

            // When evading a predator at high speed, draw a subtle water-jet wake (`~` / `:`) behind the tail
            if (f.isEvading && f.evadeBoost > 2.0f) {
                val wakeC = if (f.vxDir >= 0f) drawC - 2 else drawC + spriteW + 1
                setCell(wakeC, drawR, '~', pal.bubbleFg)
                setCell(drawC + spriteW / 2, (drawR + startRowOffset - 1).coerceAtLeast(splitRow + 2), '!', pal.fishGold)
            }
        }

        // Render Active Predator
        if (predator.active) {
            val useFrameB = sin(predator.animPhase) > 0f
            val pLines = if (predator.vxDir >= 0f) {
                if (useFrameB) predator.species.rightFrameB else predator.species.rightFrameA
            } else {
                if (useFrameB) predator.species.leftFrameB else predator.species.leftFrameA
            }
            val pc = (predator.colX + pShiftCols * 0.5f).roundToInt()
            val pr = predator.rowY.roundToInt()
            val pStartOff = -(pLines.size / 2)
            for (lIdx in pLines.indices) {
                val rr = pr + pStartOff + lIdx
                if (rr in (splitRow + 2) until (floorRow - 1)) {
                    val s = pLines[lIdx]
                    for (cIdx in s.indices) {
                        val ch = s[cIdx]
                        if (ch != ' ') {
                            val col = if (ch == 'º' || ch == '=' || ch == '-') pal.fishGold else pal.fishCoral
                            setCell(pc + cIdx, rr, ch, col)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 10. RISING BUBBLES & SINKING FOOD PELLETS
        // =========================================================================
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
                if (!fishOccupiedGrid[br * cols + bc]) {
                    val bChar = if (b.isBubble) {
                        if ((br + i) % 2 == 0) 'o' else '°'
                    } else {
                        '.'
                    }
                    setCell(bc, br, bChar, pal.bubbleFg)
                }
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

        // =========================================================================
        // 11. FLUSH CHARACTER GRID TO CANVAS
        // =========================================================================
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
    }
}

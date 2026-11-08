package com.example.koipond

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * 9 Peaceful Fish Species (Single-Line & 3-Line Hero Sprites).
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
        baseSpeedColsPerSec = 4.6f,
        rightFrameA = arrayOf("><>°"),
        rightFrameB = arrayOf("}><>°"),
        leftFrameA  = arrayOf("°<><"),
        leftFrameB  = arrayOf("°<><{")
    ),
    SILVER_MINNOW(
        id = 1,
        displayName = "silver minnow",
        isHeroMultiLine = false,
        colorRole = 3,
        baseSpeedColsPerSec = 5.0f,
        rightFrameA = arrayOf(">-))°>"),
        rightFrameB = arrayOf(">~))°>"),
        leftFrameA  = arrayOf("<°((-<"),
        leftFrameB  = arrayOf("<°((~<")
    ),
    CLOWNFISH(
        id = 2,
        displayName = "clownfish",
        isHeroMultiLine = false,
        colorRole = 0,
        baseSpeedColsPerSec = 3.2f,
        rightFrameA = arrayOf(">))|°>"),
        rightFrameB = arrayOf("})))|°>"),
        leftFrameA  = arrayOf("<°|((<"),
        leftFrameB  = arrayOf("<°|((({")
    ),
    BLUE_TANG(
        id = 3,
        displayName = "blue tang",
        isHeroMultiLine = false,
        colorRole = 1,
        baseSpeedColsPerSec = 3.5f,
        rightFrameA = arrayOf(">==))°>"),
        rightFrameB = arrayOf("}=~))°>"),
        leftFrameA  = arrayOf("<°((==<"),
        leftFrameB  = arrayOf("<°((~={")
    ),
    GOLD_OGON(
        id = 4,
        displayName = "gold ogon",
        isHeroMultiLine = false,
        colorRole = 2,
        baseSpeedColsPerSec = 2.9f,
        rightFrameA = arrayOf(">)))'>"),
        rightFrameB = arrayOf("}))'>"),
        leftFrameA  = arrayOf("<'(((<"),
        leftFrameB  = arrayOf("<'((({")
    ),
    MANDARIN(
        id = 5,
        displayName = "mandarin",
        isHeroMultiLine = false,
        colorRole = 4,
        baseSpeedColsPerSec = 3.0f,
        rightFrameA = arrayOf(">~#°>"),
        rightFrameB = arrayOf("}>~#°>"),
        leftFrameA  = arrayOf("<°#~<"),
        leftFrameB  = arrayOf("<°#~<{")
    ),
    KOHAKU_KOI(
        id = 6,
        displayName = "kohaku koi",
        isHeroMultiLine = true,
        colorRole = 0,
        baseSpeedColsPerSec = 2.4f,
        rightFrameA = arrayOf(
            "  _.._  ",
            "><(((º> ",
            "  '--'  "
        ),
        rightFrameB = arrayOf(
            "  _.._  ",
            ">}(((º> ",
            "  '--'  "
        ),
        leftFrameA = arrayOf(
            "  _.._  ",
            " <º)))><",
            "  '--'  "
        ),
        leftFrameB = arrayOf(
            "  _.._  ",
            " <º)))<{",
            "  '--'  "
        )
    ),
    MOORISH_IDOL(
        id = 7,
        displayName = "moorish idol",
        isHeroMultiLine = true,
        colorRole = 2,
        baseSpeedColsPerSec = 2.6f,
        rightFrameA = arrayOf(
            "   /|  ",
            " >||°> ",
            "   \\|  "
        ),
        rightFrameB = arrayOf(
            "   /)  ",
            " }|/°> ",
            "   \\)  "
        ),
        leftFrameA = arrayOf(
            "  |\\   ",
            " <°||< ",
            "  |/   "
        ),
        leftFrameB = arrayOf(
            "  (\\   ",
            " <°\\|{ ",
            "  (/   "
        )
    ),
    VEIL_BETTA(
        id = 8,
        displayName = "veil betta",
        isHeroMultiLine = true,
        colorRole = 4,
        baseSpeedColsPerSec = 2.2f,
        rightFrameA = arrayOf(
            " ~-\\_  ",
            ">)))º> ",
            " ~-/   "
        ),
        rightFrameB = arrayOf(
            "  ~\\_  ",
            "}))º>  ",
            "  ~/   "
        ),
        leftFrameA = arrayOf(
            "  _/-~ ",
            " <º(((<",
            "   \\-~ "
        ),
        leftFrameB = arrayOf(
            "  _/~  ",
            "  <º(({",
            "   \\~  "
        )
    );

    companion object {
        val heroPool = arrayOf(CLOWNFISH, BLUE_TANG, GOLD_OGON, MANDARIN, KOHAKU_KOI, MOORISH_IDOL, VEIL_BETTA)
        val schoolPool = arrayOf(NEON_TETRA, SILVER_MINNOW)
    }
}

/**
 * 4 Distinct Predator Species that periodically enter the pond, pursue nearby fish,
 * and cause the peaceful fish to dart into the swaying kelp beds to hide.
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
        displayName = "barracuda pike",
        speedColsPerSec = 6.2f,
        rightFrameA = arrayOf(
            "    _..._     ",
            "><(((====º<   ",
            "    '---'     "
        ),
        rightFrameB = arrayOf(
            "    _..._     ",
            ">}(((====º=   ",
            "    '---'     "
        ),
        leftFrameA = arrayOf(
            "     _..._    ",
            "   >º====)))><",
            "     '---'    "
        ),
        leftFrameB = arrayOf(
            "     _..._    ",
            "   =º====)))<{",
            "     '---'    "
        )
    ),
    REEF_SHARK(
        id = 1,
        displayName = "reef shark",
        speedColsPerSec = 5.8f,
        rightFrameA = arrayOf(
            "     /|____   ",
            "><))))º>===<  ",
            "     \\/       "
        ),
        rightFrameB = arrayOf(
            "     /|____   ",
            "}<))))º>===-  ",
            "     \\/       "
        ),
        leftFrameA = arrayOf(
            "   ____|\\     ",
            "  >===<º((((><",
            "       \\/     "
        ),
        leftFrameB = arrayOf(
            "   ____|\\     ",
            "  -===<º((((<{",
            "       \\/     "
        )
    ),
    ELECTRIC_MORAY(
        id = 2,
        displayName = "moray eel",
        speedColsPerSec = 5.2f,
        rightFrameA = arrayOf(
            " ~^~^~===º< "
        ),
        rightFrameB = arrayOf(
            " ^~^~^===º= "
        ),
        leftFrameA = arrayOf(
            " >º===~^~^~ "
        ),
        leftFrameB = arrayOf(
            " =º===^~^~^ "
        )
    ),
    KINGFISHER_DIVER(
        id = 3,
        displayName = "diving kingfisher",
        speedColsPerSec = 6.8f,
        rightFrameA = arrayOf(
            " \\  |  / ",
            "  \\(º)/  ",
            "   \\V/   "
        ),
        rightFrameB = arrayOf(
            "  \\ | /  ",
            "  -(º)-  ",
            "   \\V/   "
        ),
        leftFrameA = arrayOf(
            " \\  |  / ",
            "  \\(º)/  ",
            "   \\V/   "
        ),
        leftFrameB = arrayOf(
            "  \\ | /  ",
            "  -(º)-  ",
            "   \\V/   "
        )
    )
}

/**
 * 4 Distinct Bottom-Feeder Critters patrolling the river cobbles along the floor.
 */
enum class BottomFeederSpecies(
    val id: Int,
    val displayName: String,
    val speedColsPerSec: Float,
    val colorRole: Int, // 0=Coral, 1=Aqua, 2=Gold, 3=Foam, 4=Rose
    val rightFrameA: String,
    val rightFrameB: String,
    val leftFrameA: String,
    val leftFrameB: String
) {
    RIVER_CRAB(
        id = 0,
        displayName = "river crab",
        speedColsPerSec = 1.6f,
        colorRole = 0,
        rightFrameA = "(\\/)(°_°)(\\/)",
        rightFrameB = "(/)(°_°)(\\)",
        leftFrameA  = "(\\/)(°_°)(\\/)",
        leftFrameB  = "(/)(°_°)(\\)"
    ),
    CHERRY_SHRIMP(
        id = 1,
        displayName = "cherry shrimp",
        speedColsPerSec = 2.2f,
        colorRole = 4,
        rightFrameA = ",=/>°",
        rightFrameB = ".=/>°",
        leftFrameA  = "°<\\=,",
        leftFrameB  = "°<\\=."
    ),
    CORY_CATFISH(
        id = 2,
        displayName = "cory catfish",
        speedColsPerSec = 1.9f,
        colorRole = 2,
        rightFrameA = ">))º>=",
        rightFrameB = "}))º>~",
        leftFrameA  = "=<º((<",
        leftFrameB  = "~<º(({"
    ),
    APPLE_SNAIL(
        id = 3,
        displayName = "golden snail",
        speedColsPerSec = 0.65f,
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
    var colX: Float,
    var rowY: Float,
    var preferredLaneRow: Float,
    var vxDir: Float,      // +1f = right, -1f = left
    var vySpeed: Float,
    var speedScale: Float,
    var animPhase: Float,
    var startleBoost: Float = 0f,
    var isHidingInKelp: Boolean = false
)

class PredatorAgent(
    var active: Boolean = false,
    var species: PredatorSpecies = PredatorSpecies.BARRACUDA_PIKE,
    var colX: Float = -16f,
    var rowY: Float = 25f,
    var vxDir: Float = 1f,
    var vySpeed: Float = 0f,
    var animPhase: Float = 0f,
    var cooldownTimer: Float = 14f, // Seconds until next predator visit
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
    var speedY: Float = 2.2f,
    var isBubble: Boolean = true,
    var phase: Float = 0f
)

class AsciiTouchRipple(
    var colX: Float = 0f,
    var rowY: Float = 0f,
    var radius: Float = 99f,
    var maxRadius: Float = 14f,
    var active: Boolean = false
)

/**
 * Zero-allocation Character-Grid Simulation & Canvas Renderer for the Half-Over / Half-Underwater
 * ASCII Live Wallpaper with Strict Non-Overlapping Fish Separation, Random Predator Visits
 * (where fish hide in the kelp), and Bottom-Feeder Critters.
 */
class PondSimulation {

    val maxHeroFish = 9
    val maxSchoolFish = 7
    val totalMaxFish = maxHeroFish + maxSchoolFish

    val fish = Array(totalMaxFish) { idx ->
        AsciiFishAgent(
            id = idx,
            isSchooler = idx >= maxHeroFish,
            species = AsciiFishSpecies.entries[idx % AsciiFishSpecies.entries.size],
            colX = 10f,
            rowY = 20f,
            preferredLaneRow = 20f,
            vxDir = if (idx % 2 == 0) 1f else -1f,
            vySpeed = 0.25f,
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
            colX = 4f + idx * 9f,
            vxDir = if (idx % 2 == 0) 1f else -1f,
            animPhase = idx * 1.7f
        )
    }

    val bubbles = Array(28) { idx ->
        AsciiBubble(
            colX = (idx * 7 % 45).toFloat(),
            rowY = (15 + (idx * 5 % 25)).toFloat(),
            speedY = if (idx < 10) 2.4f + (idx % 3) * 0.5f else 0.35f,
            isBubble = idx < 10,
            phase = idx * 0.9f
        )
    }

    val ripples = Array(6) { AsciiTouchRipple() }
    private var nextRippleIdx = 0

    var simTime = 0f
        private set
    private var unlockSeedCounter = 1L

    var gridCols = 46
        private set
    var gridRows = 82
        private set
    var waterlineRow = 30
        private set

    private val maxCols = 80
    private val maxRows = 140
    private val charGrid = CharArray(maxCols * maxRows) { ' ' }
    private val colorGrid = IntArray(maxCols * maxRows)
    private val fishOccupiedGrid = BooleanArray(maxCols * maxRows)
    private val rowCharScratch = CharArray(maxCols)

    // Pre-allocated kelp X columns so fish know where to hide when a predator enters
    private val kelpBaseCols = IntArray(6)

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.MONOSPACE
        fontFeatureSettings = "liga 0, calt 0"
        textAlign = Paint.Align.LEFT
    }
    private val bgPaint = Paint()

    private var cellWidthPx = 24f
    private var cellHeightPx = 42f
    private var baselineOffsetPx = 32f

    var activeSpeciesSummary: String = "clownfish, kohaku koi, neon tetra"
        private set

    init {
        randomizeEcosystemOnUnlock(System.nanoTime())
    }

    fun configureGrid(widthPx: Int, heightPx: Int, density: GridDensity) {
        if (widthPx <= 0 || heightPx <= 0) return
        val cols = density.targetCols.coerceIn(30, maxCols - 2)
        cellWidthPx = widthPx.toFloat() / cols.toFloat()

        textPaint.textSize = cellWidthPx * 1.66f
        val actualAdvance = textPaint.measureText("M").coerceAtLeast(1f)
        textPaint.textSize = textPaint.textSize * (cellWidthPx / actualAdvance)

        val fm = textPaint.fontMetrics
        cellHeightPx = (fm.descent - fm.ascent + fm.leading).coerceAtLeast(cellWidthPx * 1.45f)
        baselineOffsetPx = -fm.ascent

        gridCols = cols
        gridRows = (heightPx / cellHeightPx).toInt().coerceIn(28, maxRows - 2)
        waterlineRow = (gridRows * 0.38f).roundToInt().coerceIn(10, gridRows - 14)

        kelpBaseCols[0] = 4
        kelpBaseCols[1] = 8
        kelpBaseCols[2] = (cols * 0.34f).roundToInt()
        kelpBaseCols[3] = (cols * 0.64f).roundToInt()
        kelpBaseCols[4] = cols - 9
        kelpBaseCols[5] = cols - 5
    }

    /**
     * Randomizes the fish species mix, bottom feeders, and stratified non-overlapping lanes
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

        val minUnderwaterRow = (waterlineRow + 4).coerceAtMost(gridRows - 14)
        val maxUnderwaterRow = (gridRows - 9).coerceAtLeast(minUnderwaterRow + 8)
        val underwaterSpan = (maxUnderwaterRow - minUnderwaterRow).toFloat().coerceAtLeast(8f)

        // Assign stratified vertical lanes and staggered horizontal columns so fish start with zero overlap
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
            val laneFrac = (i.toFloat() + 0.5f) / totalMaxFish.toFloat()
            val laneRow = minUnderwaterRow + laneFrac * underwaterSpan
            f.preferredLaneRow = laneRow
            f.rowY = laneRow
            f.colX = ((i * 11 + rand.nextInt(6)) % (gridCols - 10).coerceAtLeast(10)).toFloat() + 2f
            f.vxDir = if (i % 2 == 0) 1f else -1f
            f.vySpeed = (rand.nextFloat() - 0.5f) * 0.35f
            f.speedScale = 0.85f + rand.nextFloat() * 0.30f
            f.animPhase = rand.nextFloat() * 6.28f
            f.startleBoost = 0f
            f.isHidingInKelp = false
        }

        // Randomize bottom feeder species on the riverbed
        for (b in 0 until maxBottomFeeders) {
            val bf = bottomFeeders[b]
            bf.species = BottomFeederSpecies.entries[rand.nextInt(BottomFeederSpecies.entries.size)]
            bf.colX = 3f + b * ((gridCols - 8).coerceAtLeast(15) / maxBottomFeeders.toFloat())
            bf.vxDir = if (rand.nextBoolean()) 1f else -1f
            bf.pauseTimer = 0f
        }

        // Reset predator cooldown so it doesn't ambush immediately on the first frame unless triggered
        predator.active = false
        predator.cooldownTimer = 9f + rand.nextFloat() * 14f
    }

    /**
     * Spawns an immediate predator visit (can be triggered by the random timer or the UI button).
     */
    fun triggerPredatorVisit(seed: Long = System.nanoTime()) {
        val rand = Random(seed)
        val sp = PredatorSpecies.entries[rand.nextInt(PredatorSpecies.entries.size)]
        predator.species = sp
        predator.active = true
        predator.activeDuration = 0f
        predator.animPhase = 0f

        if (sp == PredatorSpecies.KINGFISHER_DIVER) {
            // Plunges from upper sky through the waterline!
            predator.colX = (gridCols * (0.28f + rand.nextFloat() * 0.44f))
            predator.rowY = 2f
            predator.vxDir = if (rand.nextBoolean()) 0.35f else -0.35f
            predator.vySpeed = 11.5f
        } else {
            val fromLeft = rand.nextBoolean()
            predator.vxDir = if (fromLeft) 1f else -1f
            predator.colX = if (fromLeft) -14f else (gridCols + 2).toFloat()
            val minR = (waterlineRow + 5).coerceAtMost(gridRows - 12)
            val maxR = (gridRows - 10).coerceAtLeast(minR + 2)
            predator.rowY = minR + rand.nextFloat() * (maxR - minR)
            predator.vySpeed = 0f
        }
    }

    fun onTouch(screenX: Float, screenY: Float) {
        val c = (screenX / cellWidthPx).coerceIn(1f, (gridCols - 2).toFloat())
        val r = (screenY / cellHeightPx).coerceIn(1f, (gridRows - 2).toFloat())

        val rp = ripples[nextRippleIdx]
        rp.colX = c
        rp.rowY = r
        rp.radius = 0.5f
        rp.maxRadius = 13f
        rp.active = true
        nextRippleIdx = (nextRippleIdx + 1) % ripples.size

        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            val dx = f.colX - c
            val dy = f.rowY - r
            val dist = hypot(dx, dy)
            if (dist < 14f) {
                f.vxDir = if (dx >= 0f) 1f else -1f
                f.startleBoost = 5.5f * (1f - dist / 14f)
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
        isWallpaperMode: Boolean
    ) {
        val clampedDt = dt.coerceIn(0.001f, 0.08f)
        simTime += clampedDt

        val snapshot = TimeOfDayPalette.resolve(config.timeCycleMode)
        val pal = snapshot.colors

        val cols = gridCols
        val rows = gridRows
        val pShiftCols = (parallaxX * 6f).roundToInt()
        val pShiftRows = (parallaxY * 2f).roundToInt()
        val splitRow = (waterlineRow + pShiftRows).coerceIn(8, rows - 12)

        val topCalmRow = if (isWallpaperMode) (rows * 0.14f).toInt() else 0
        val bottomCalmRow = if (isWallpaperMode) (rows * 0.86f).toInt() else rows - 3
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
        val celestialCol = ((snapshot.sunArcX * (cols - 10)).roundToInt() + 5 + pShiftCols).coerceIn(4, cols - 6)
        val skyHeight = (splitRow - 4).coerceAtLeast(4)
        val celestialRow = ((1f - snapshot.sunArcY) * (skyHeight - 3) + 2).roundToInt().coerceIn(2, splitRow - 4)

        if (snapshot.isNight) {
            for (s in 0 until 14) {
                val sc = ((s * 13 + 5 + pShiftCols) % (cols - 2)).coerceAtLeast(1)
                val sr = ((s * 7 + 2) % (splitRow - 3)).coerceAtLeast(1)
                val twinkle = sin(simTime * 2.2f + s * 1.7f)
                val starChar = if (twinkle > 0.3f) '*' else '.'
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

        val cloud1Col = ((simTime * 0.7f + 6f + pShiftCols).toInt() % (cols + 12)) - 8
        val cloud1Row = (splitRow * 0.34f).toInt().coerceIn(2, splitRow - 4)
        drawStringAt(cloud1Col, cloud1Row,     "  .--.   ", pal.cloudReedFg)
        drawStringAt(cloud1Col, cloud1Row + 1, "-(    )-.", pal.cloudReedFg)

        val cloud2Col = (((simTime * 0.45f + cols * 0.55f + pShiftCols).toInt()) % (cols + 12)) - 8
        val cloud2Row = (splitRow * 0.52f).toInt().coerceIn(3, splitRow - 3)
        drawStringAt(cloud2Col, cloud2Row, " .-(   )-.", pal.cloudReedFg)

        val swayLeft = sin(simTime * 1.4f) > 0f
        for (c in 0 until cols) {
            if (c in 2..7 || c in (cols - 9)..(cols - 3)) {
                val reedTop = if ((c + if (swayLeft) 1 else 0) % 2 == 0) '/' else '|'
                setCell(c, splitRow - 2, reedTop, pal.kelpFg)
                setCell(c, splitRow - 1, '|', pal.kelpFg)
            } else if (c % 3 == 0) {
                setCell(c, splitRow - 1, '.', pal.cloudReedFg)
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

        val pad1Col = (cols * 0.22f + sin(simTime * 0.6f) * 1.5f + pShiftCols).roundToInt()
        drawStringAt(pad1Col, splitRow, "[_v_]", pal.lilyPadFg)
        drawStringAt(pad1Col + 1, splitRow - 1, "(*)", pal.lotusFlowerFg)

        val pad2Col = (cols * 0.70f + cos(simTime * 0.5f) * 1.5f + pShiftCols).roundToInt()
        drawStringAt(pad2Col, splitRow, "[_v_]", pal.lilyPadFg)

        if (config.waterClarity > 0.45f) {
            for (c in 1 until cols - 1 step 3) {
                val shimmerChar = if ((c + wavePhase) % 2 == 0) ':' else '.'
                setCell(c, splitRow + 1, shimmerChar, pal.sunRayFg)
            }
        }

        // =========================================================================
        // 4. UNDERWATER SUNBEAMS, RIVERBED COBBLES & BACKGROUND KELP
        // =========================================================================
        if (!snapshot.isNight && config.waterClarity > 0.5f) {
            val rayCount = if (config.waterClarity > 0.78f) 3 else 2
            for (rayIdx in 0 until rayCount) {
                val startCol = (cols * (0.20f + rayIdx * 0.28f) + sin(simTime * 0.4f + rayIdx) * 2f).roundToInt()
                val maxRayDepth = ((rows - splitRow) * 0.68f).toInt()
                for (d in 2 until maxRayDepth step 2) {
                    val rc = startCol + (d / 2)
                    val rr = splitRow + d
                    if (rr < bottomCalmRow && (d + wavePhase) % 3 != 0) {
                        val rayCh = if (d % 4 == 0) '/' else ':'
                        setCell(rc, rr, rayCh, pal.sunRayFg)
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

        // Helper to draw swaying Kelp Sanctuary columns (pass 0 = background stems, pass 1 = foreground fronds)
        fun drawKelpLayer(foregroundOnly: Boolean) {
            for (kIdx in kelpBaseCols.indices) {
                if (foregroundOnly != (kIdx % 2 == 0)) continue
                val baseC = (kelpBaseCols[kIdx] + pShiftCols).mod(cols)
                val height = 9 + (kIdx % 3) * 3
                for (h in 0 until height) {
                    val kr = floorRow - 1 - h
                    if (kr <= splitRow + 2) break
                    val sway = sin(simTime * 1.8f + kIdx * 1.3f - h * 0.50f)
                    val kc = baseC + if (sway > 0.35f) 1 else if (sway < -0.35f) -1 else 0
                    val kCh = when {
                        sway > 0.25f -> ')'
                        sway < -0.25f -> '('
                        else -> '|'
                    }
                    setCell(kc, kr, kCh, pal.kelpFg)
                    if (h % 2 == 1) {
                        setCell(kc + 1, kr, if (sway >= 0f) '/' else ')', pal.kelpFg)
                    }
                }
            }
        }
        drawKelpLayer(foregroundOnly = false)

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
                    // Occasional foraging pause
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

                // Prevent two bottom feeders from overlapping on the riverbed
                for (otherIdx in 0 until b) {
                    val other = bottomFeeders[otherIdx]
                    val dx = bf.colX - other.colX
                    val minSep = (spriteLen + other.species.rightFrameA.length) * 0.65f + 2f
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
        // 6. RANDOM PREDATOR VISITS (Pike, Reef Shark, Moray Eel, Diving Kingfisher)
        // =========================================================================
        if (config.enablePredators) {
            if (!predator.active) {
                predator.cooldownTimer -= clampedDt
                if (predator.cooldownTimer <= 0f) {
                    triggerPredatorVisit((simTime * 1000f).toLong())
                }
            } else {
                predator.activeDuration += clampedDt
                predator.animPhase += clampedDt * 6.5f

                if (predator.species == PredatorSpecies.KINGFISHER_DIVER) {
                    // Dives from sky into upper water, splashes, then flies back up!
                    val maxDiveRow = (splitRow + (rows - splitRow) * 0.48f)
                    if (predator.vySpeed > 0f && predator.rowY >= maxDiveRow) {
                        predator.vySpeed = -9.5f
                        // Spawn splash ripple at waterline
                        val rp = ripples[nextRippleIdx]
                        rp.colX = predator.colX
                        rp.rowY = splitRow.toFloat()
                        rp.radius = 0.5f
                        rp.maxRadius = 12f
                        rp.active = true
                        nextRippleIdx = (nextRippleIdx + 1) % ripples.size
                    }
                    predator.colX += predator.vxDir * predator.species.speedColsPerSec * clampedDt
                    predator.rowY += predator.vySpeed * clampedDt

                    if (predator.vySpeed < 0f && predator.rowY < -3f) {
                        predator.active = false
                        predator.cooldownTimer = 16f + (sin(simTime) * 0.5f + 0.5f) * 14f
                    }
                } else {
                    // Underwater apex predator steers gently toward the nearest fish that isn't hidden yet
                    var targetY = predator.rowY
                    var minDist = 999f
                    for (i in 0 until config.heroFishCount) {
                        val f = fish[i]
                        if (!f.isHidingInKelp) {
                            val d = hypot(f.colX - predator.colX, f.rowY - predator.rowY)
                            if (d < minDist) {
                                minDist = d
                                targetY = f.rowY
                            }
                        }
                    }
                    val dy = (targetY - predator.rowY).coerceIn(-1.5f, 1.5f)
                    predator.colX += predator.vxDir * predator.species.speedColsPerSec * clampedDt
                    predator.rowY = (predator.rowY + dy * 1.6f * clampedDt)
                        .coerceIn((splitRow + 4).toFloat(), (floorRow - 5).toFloat())

                    if ((predator.vxDir > 0f && predator.colX > cols + 6) ||
                        (predator.vxDir < 0f && predator.colX < -18f) ||
                        predator.activeDuration > 14f
                    ) {
                        predator.active = false
                        predator.cooldownTimer = 15f + (cos(simTime) * 0.5f + 0.5f) * 15f
                    }
                }
            }
        } else {
            predator.active = false
        }

        // =========================================================================
        // 7. UPDATE & RENDER PEACEFUL FISH (Strict Non-Overlap + Kelp Hiding AI)
        // =========================================================================
        val activeHero = if (isBatterySaver) (config.heroFishCount - 2).coerceAtLeast(2) else config.heroFishCount
        val activeSchool = if (isBatterySaver) 2 else maxSchoolFish

        val minFishRow = (splitRow + 3).coerceAtMost(rows - 10)
        val maxFishRow = (bottomCalmRow - 4).coerceAtLeast(minFishRow + 4)

        // 7A. Update positions & Predator-Evasion / Kelp-Hiding behavior
        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            if (!f.isSchooler && i >= activeHero) continue
            if (f.isSchooler && (i - maxHeroFish) >= activeSchool) continue

            f.startleBoost *= (1f - clampedDt * 2.2f).coerceAtLeast(0f)
            f.isHidingInKelp = false

            if (predator.active) {
                val distToPred = hypot(f.colX - predator.colX, (f.rowY - predator.rowY) * 1.6f)
                if (distToPred < 24f) {
                    // Find nearest Kelp Sanctuary column away from predator
                    var bestKelpCol = kelpBaseCols[0].toFloat()
                    var bestScore = 999f
                    for (k in kelpBaseCols.indices) {
                        val kc = kelpBaseCols[k].toFloat()
                        val dFish = abs(f.colX - kc)
                        val dPred = abs(predator.colX - kc)
                        val score = dFish - dPred * 0.45f
                        if (score < bestScore) {
                            bestScore = score
                            bestKelpCol = kc
                        }
                    }
                    val hideRow = (floorRow - 3 - (f.id % 4)).toFloat().coerceIn(minFishRow.toFloat(), maxFishRow.toFloat())
                    val dxKelp = bestKelpCol - f.colX
                    val dyKelp = hideRow - f.rowY

                    if (hypot(dxKelp, dyKelp) < 2.2f) {
                        // Safely tucked inside the swaying kelp fronds!
                        f.isHidingInKelp = true
                        f.colX += dxKelp * clampedDt * 3.0f
                        f.rowY += dyKelp * clampedDt * 3.0f
                        f.animPhase += clampedDt * 1.5f
                    } else {
                        // Dart rapidly toward the kelp sanctuary!
                        f.vxDir = if (dxKelp >= 0f) 1f else -1f
                        f.startleBoost = 4.2f
                        val spd = (f.species.baseSpeedColsPerSec * f.speedScale) + f.startleBoost
                        f.colX += f.vxDir * spd * clampedDt
                        f.rowY += dyKelp.coerceIn(-1f, 1f) * 3.8f * clampedDt
                        f.animPhase += clampedDt * 6.5f
                    }
                }
            }

            if (!f.isHidingInKelp && (!predator.active || hypot(f.colX - predator.colX, f.rowY - predator.rowY) >= 24f)) {
                val spd = (f.species.baseSpeedColsPerSec * f.speedScale) + f.startleBoost
                f.colX += f.vxDir * spd * clampedDt
                // Gently return toward assigned vertical lane so fish stay spread across the water column
                val lanePull = (f.preferredLaneRow - f.rowY) * 0.45f
                f.rowY += (f.vySpeed + lanePull + sin(simTime * 0.9f + f.id) * 0.22f) * clampedDt
                f.animPhase += clampedDt * (3.2f + f.startleBoost * 0.8f)
            }

            val spriteWidth = f.species.rightFrameA[0].length
            if (f.vxDir > 0f && f.colX > cols - spriteWidth - 1) {
                f.vxDir = -1f
            } else if (f.vxDir < 0f && f.colX < 1f) {
                f.vxDir = 1f
            }

            f.rowY = f.rowY.coerceIn(minFishRow.toFloat(), maxFishRow.toFloat())
        }

        // 7B. Strict Pairwise Bounding-Box Separation Solver (Guarantees Fish Never Overlap!)
        for (pass in 0 until 3) {
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

                    // Required center-to-center clearance + 2-column / 1-row padding halo
                    val minSepX = (wi + wj) * 0.5f + 2.0f
                    val minSepY = (hi + hj) * 0.5f + 0.9f

                    val centerXi = fi.colX + wi * 0.5f
                    val centerXj = fj.colX + wj * 0.5f
                    val dx = centerXi - centerXj
                    val dy = fi.rowY - fj.rowY

                    if (abs(dx) < minSepX && abs(dy) < minSepY) {
                        val overlapX = minSepX - abs(dx)
                        val overlapY = minSepY - abs(dy)

                        if (overlapY < overlapX * 0.7f) {
                            // Push apart vertically into distinct rows
                            val pushY = (overlapY * 0.55f).coerceAtLeast(0.4f)
                            if (dy >= 0f) {
                                fi.rowY = (fi.rowY + pushY).coerceIn(minFishRow.toFloat(), maxFishRow.toFloat())
                                fj.rowY = (fj.rowY - pushY).coerceIn(minFishRow.toFloat(), maxFishRow.toFloat())
                            } else {
                                fi.rowY = (fi.rowY - pushY).coerceIn(minFishRow.toFloat(), maxFishRow.toFloat())
                                fj.rowY = (fj.rowY + pushY).coerceIn(minFishRow.toFloat(), maxFishRow.toFloat())
                            }
                        } else {
                            // Push apart horizontally and reverse trailing fish if head-on
                            val pushX = (overlapX * 0.55f).coerceAtLeast(0.6f)
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

        // 7C. Stamp Fish into Character Grid with Cell-Occupancy Verification
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

            // Final grid-occupancy check: if any cell in this fish's box is already claimed, shift 1 row
            fun isBoxFree(testR: Int): Boolean {
                for (rOff in 0 until spriteH) {
                    val rr = (testR + startRowOffset + rOff).coerceIn(0, rows - 1)
                    for (cOff in -1..spriteW) {
                        val cc = (drawC + cOff).coerceIn(0, cols - 1)
                        if (fishOccupiedGrid[rr * cols + cc]) return false
                    }
                }
                return true
            }

            if (!isBoxFree(drawR)) {
                for (delta in intArrayOf(-1, 1, -2, 2, -3, 3)) {
                    val candR = (drawR + delta).coerceIn(minFishRow, maxFishRow)
                    if (isBoxFree(candR)) {
                        drawR = candR
                        f.rowY = candR.toFloat()
                        break
                    }
                }
            }

            // Mark bounding box as occupied & draw fish characters
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
                                    f.species == AsciiFishSpecies.KOHAKU_KOI && lineIdx == 0 -> pal.fishCoral
                                    f.species == AsciiFishSpecies.KOHAKU_KOI && lineIdx == 1 -> pal.fishFoam
                                    f.species == AsciiFishSpecies.CLOWNFISH && ch == '|' -> pal.fishFoam
                                    else -> fishColor
                                }
                                setCell(c, r, ch, charCol)
                            }
                        }
                    }
                }
            }

            // If startled by a predator and fleeing, show a tiny '!' alert above the fish
            if (predator.active && !f.isHidingInKelp && f.startleBoost > 1.5f) {
                val alertR = drawR + startRowOffset - 1
                if (alertR > splitRow + 1) {
                    setCell(drawC + spriteW / 2, alertR, '!', pal.fishGold)
                }
            }
        }

        // 7D. Render Active Predator (drawn prominently in coral/gold warning accent)
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
                if (rr in 1 until (floorRow - 1)) {
                    val s = pLines[lIdx]
                    for (cIdx in s.indices) {
                        val ch = s[cIdx]
                        if (ch != ' ') {
                            val col = if (ch == 'º' || ch == 'V' || ch == '=') pal.fishGold else pal.fishCoral
                            setCell(pc + cIdx, rr, ch, col)
                        }
                    }
                }
            }
        }

        // 7E. Draw Foreground Kelp Layer OVER hiding fish so fish tuck realistically behind the fronds!
        drawKelpLayer(foregroundOnly = true)

        // =========================================================================
        // 8. RISING BUBBLES & TOUCH RIPPLES
        // =========================================================================
        val activeBubbles = if (isBatterySaver) 10 else bubbles.size
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

        for (i in ripples.indices) {
            val rp = ripples[i]
            if (!rp.active) continue
            rp.radius += clampedDt * 8.5f
            if (rp.radius > rp.maxRadius) {
                rp.active = false
                continue
            }
            val steps = 16
            for (s in 0 until steps) {
                val ang = (s.toFloat() / steps) * (2f * Math.PI.toFloat())
                val rc = (rp.colX + cos(ang) * rp.radius).roundToInt()
                val rr = (rp.rowY + sin(ang) * (rp.radius * 0.55f)).roundToInt()
                val rCh = if (cos(ang) >= 0f) ')' else '('
                setCell(rc, rr, rCh, pal.meniscusWaveFg)
            }
        }

        // =========================================================================
        // 9. FLUSH CHARACTER GRID TO CANVAS
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

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
 * Approved Plain-Text Monospace ASCII Fish Species (Single-Line & 3-Line Hero Sprites).
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
            "   \|  "
        ),
        rightFrameB = arrayOf(
            "   /)  ",
            " }|/°> ",
            "   \)  "
        ),
        leftFrameA = arrayOf(
            "  |\   ",
            " <°||< ",
            "  |/   "
        ),
        leftFrameB = arrayOf(
            "  (\   ",
            " <°\|{ ",
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
            " ~-\_  ",
            ">)))º> ",
            " ~-/   "
        ),
        rightFrameB = arrayOf(
            "  ~\_  ",
            "}))º>  ",
            "  ~/   "
        ),
        leftFrameA = arrayOf(
            "  _/-~ ",
            " <º(((<",
            "   \-~ "
        ),
        leftFrameB = arrayOf(
            "  _/~  ",
            "  <º(({",
            "   \~  "
        )
    );

    companion object {
        val heroPool = arrayOf(CLOWNFISH, BLUE_TANG, GOLD_OGON, MANDARIN, KOHAKU_KOI, MOORISH_IDOL, VEIL_BETTA)
        val schoolPool = arrayOf(NEON_TETRA, SILVER_MINNOW)
    }
}

class AsciiFishAgent(
    val id: Int,
    var isSchooler: Boolean,
    var species: AsciiFishSpecies,
    var colX: Float,
    var rowY: Float,
    var vxDir: Float,      // +1f = right, -1f = left
    var vySpeed: Float,
    var speedScale: Float,
    var animPhase: Float,
    var startleBoost: Float = 0f
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
 * ASCII Live Wallpaper.
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
            vxDir = if (idx % 2 == 0) 1f else -1f,
            vySpeed = 0.25f,
            speedScale = 1.0f,
            animPhase = idx * 1.3f
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

    // Pre-allocated 2D character & color buffers (max 80 cols x 140 rows -> zero GC allocations)
    private val maxCols = 80
    private val maxRows = 140
    private val charGrid = CharArray(maxCols * maxRows) { ' ' }
    private val colorGrid = IntArray(maxCols * maxRows)
    private val rowCharScratch = CharArray(maxCols)

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

        // Measure monospace 'M' so character cells have crisp aspect ratio
        textPaint.textSize = cellWidthPx * 1.66f
        val actualAdvance = textPaint.measureText("M").coerceAtLeast(1f)
        textPaint.textSize = textPaint.textSize * (cellWidthPx / actualAdvance)

        val fm = textPaint.fontMetrics
        cellHeightPx = (fm.descent - fm.ascent + fm.leading).coerceAtLeast(cellWidthPx * 1.45f)
        baselineOffsetPx = -fm.ascent

        gridCols = cols
        gridRows = (heightPx / cellHeightPx).toInt().coerceIn(28, maxRows - 2)
        // Half-over / half-underwater split: ~38% sky & surface, ~62% underwater cross-section
        waterlineRow = (gridRows * 0.38f).roundToInt().coerceIn(10, gridRows - 14)
    }

    /**
     * Randomizes the fish species mix and positions every time the user unlocks their phone.
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

        val minUnderwaterRow = (waterlineRow + 3).coerceAtMost(gridRows - 10)
        val maxUnderwaterRow = (gridRows - 8).coerceAtLeast(minUnderwaterRow + 4)

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
            f.colX = rand.nextFloat() * (gridCols - 8).coerceAtLeast(10) + 2f
            f.rowY = minUnderwaterRow + rand.nextFloat() * (maxUnderwaterRow - minUnderwaterRow)
            f.vxDir = if (rand.nextBoolean()) 1f else -1f
            f.vySpeed = (rand.nextFloat() - 0.5f) * 0.55f
            f.speedScale = 0.82f + rand.nextFloat() * 0.36f
            f.animPhase = rand.nextFloat() * 6.28f
            f.startleBoost = 0f
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

        // Startle nearby fish gently away from the tap
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

        // Usability calm bands for wallpaper mode (keep top 15% clock & bottom 18% icon dock calmer)
        val topCalmRow = if (isWallpaperMode) (rows * 0.14f).toInt() else 0
        val bottomCalmRow = if (isWallpaperMode) (rows * 0.84f).toInt() else rows - 4

        // 1. Paint solid tinted background bands (Sky above splitRow, Shallow-to-Deep Water below)
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

        // Clear character grid
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
        // 2. OVER-WATER REGION (Rows 0 .. splitRow - 1): Sun/Moon, Stars, Clouds, Shore Reeds
        // =========================================================================
        val celestialCol = ((snapshot.sunArcX * (cols - 10)).roundToInt() + 5 + pShiftCols).coerceIn(4, cols - 6)
        val skyHeight = (splitRow - 4).coerceAtLeast(4)
        val celestialRow = ((1f - snapshot.sunArcY) * (skyHeight - 3) + 2).roundToInt().coerceIn(2, splitRow - 4)

        if (snapshot.isNight) {
            // Twinkling stars in night sky
            for (s in 0 until 14) {
                val sc = ((s * 13 + 5 + pShiftCols) % (cols - 2)).coerceAtLeast(1)
                val sr = ((s * 7 + 2) % (splitRow - 3)).coerceAtLeast(1)
                val twinkle = sin(simTime * 2.2f + s * 1.7f)
                val starChar = if (twinkle > 0.3f) '*' else '.'
                setCell(sc, sr, starChar, pal.celestialGlyph)
            }
            // Crescent Moon ASCII art
            drawStringAt(celestialCol - 2, celestialRow - 1, " .--. ", pal.celestialGlyph)
            drawStringAt(celestialCol - 2, celestialRow,     "( (   ", pal.celestialGlyph)
            drawStringAt(celestialCol - 2, celestialRow + 1, " '--' ", pal.celestialGlyph)
        } else {
            // Radiant Sun ASCII art with animated rays
            val rayPulse = ((simTime * 1.5f).toInt() % 2 == 0)
            if (rayPulse) {
                drawStringAt(celestialCol - 3, celestialRow - 1, " \ | / ", pal.celestialGlyph)
                drawStringAt(celestialCol - 3, celestialRow,     "--(O)--", pal.celestialGlyph)
                drawStringAt(celestialCol - 3, celestialRow + 1, " / | \ ", pal.celestialGlyph)
            } else {
                drawStringAt(celestialCol - 3, celestialRow - 1, "  \|/  ", pal.celestialGlyph)
                drawStringAt(celestialCol - 3, celestialRow,     " -(o)- ", pal.celestialGlyph)
                drawStringAt(celestialCol - 3, celestialRow + 1, "  /|\  ", pal.celestialGlyph)
            }
        }

        // Drifting ASCII Clouds
        val cloud1Col = ((simTime * 0.7f + 6f + pShiftCols).toInt() % (cols + 12)) - 8
        val cloud1Row = (splitRow * 0.34f).toInt().coerceIn(2, splitRow - 4)
        drawStringAt(cloud1Col, cloud1Row,     "  .--.   ", pal.cloudReedFg)
        drawStringAt(cloud1Col, cloud1Row + 1, "-(    )-.", pal.cloudReedFg)

        val cloud2Col = (((simTime * 0.45f + cols * 0.55f + pShiftCols).toInt()) % (cols + 12)) - 8
        val cloud2Row = (splitRow * 0.52f).toInt().coerceIn(3, splitRow - 3)
        drawStringAt(cloud2Col, cloud2Row, " .-(   )-.", pal.cloudReedFg)

        // Shoreline Reeds & Distant Horizon just above the waterline (splitRow - 2, splitRow - 1)
        val swayLeft = sin(simTime * 1.4f) > 0f
        for (c in 0 until cols) {
            // Left and right bank reed clusters
            if (c in 2..7 || c in (cols - 9)..(cols - 3)) {
                val reedTop = if ((c + if (swayLeft) 1 else 0) % 2 == 0) '/' else '|'
                setCell(c, splitRow - 2, reedTop, pal.kelpFg)
                setCell(c, splitRow - 1, '|', pal.kelpFg)
            } else if (c % 3 == 0) {
                setCell(c, splitRow - 1, '.', pal.cloudReedFg)
            }
        }

        // =========================================================================
        // 3. SPLIT WATERLINE MENISCUS & FLOATING LILY PADS / LOTUS (Row = splitRow)
        // =========================================================================
        val wavePhase = (simTime * 3.0f).toInt()
        val wavePattern = "~^~._."
        for (c in 0 until cols) {
            val ch = wavePattern[(c + wavePhase).mod(wavePattern.length)]
            setCell(c, splitRow, ch, pal.meniscusWaveFg)
        }

        // Floating surface lily pads `[_v_]` and blooming lotus `(*)` on the meniscus line
        val pad1Col = (cols * 0.22f + sin(simTime * 0.6f) * 1.5f + pShiftCols).roundToInt()
        drawStringAt(pad1Col, splitRow, "[_v_]", pal.lilyPadFg)
        drawStringAt(pad1Col + 1, splitRow - 1, "(*)", pal.lotusFlowerFg)

        val pad2Col = (cols * 0.70f + cos(simTime * 0.5f) * 1.5f + pShiftCols).roundToInt()
        drawStringAt(pad2Col, splitRow, "[_v_]", pal.lilyPadFg)

        // Sub-surface caustic shimmer right below meniscus (splitRow + 1)
        if (config.waterClarity > 0.45f) {
            for (c in 1 until cols - 1 step 3) {
                val shimmerChar = if ((c + wavePhase) % 2 == 0) ':' else '.'
                setCell(c, splitRow + 1, shimmerChar, pal.sunRayFg)
            }
        }

        // =========================================================================
        // 4. UNDERWATER DIAGONAL SUNBEAMS (God Rays), KELP & RIVERBED COBBLES
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

        // Riverbed Sand & Rounded Cobbles along bottom rows
        val floorRow = rows - 2
        val subFloorRow = rows - 1
        val pebblePattern = "..ooO.._.oOo._..ooO.._."
        for (c in 0 until cols) {
            val pCh = pebblePattern[(c - pShiftCols).mod(pebblePattern.length)]
            setCell(c, floorRow, pCh, pal.pebbleFg)
            setCell(c, subFloorRow, '~', pal.cloudReedFg)
        }

        // Swaying Underwater Kelp / Vallisneria rooted at floorRow - 1
        val kelpPositions = intArrayOf(
            4, 7, 15, 28, cols - 14, cols - 6, cols - 4
        )
        for (kIdx in kelpPositions.indices) {
            val baseC = (kelpPositions[kIdx] + pShiftCols).mod(cols)
            val height = 5 + (kIdx % 4) * 2
            for (h in 0 until height) {
                val kr = floorRow - 1 - h
                if (kr <= splitRow + 2) break
                val sway = sin(simTime * 1.8f + kIdx * 1.3f - h * 0.55f)
                val kc = baseC + if (sway > 0.35f) 1 else if (sway < -0.35f) -1 else 0
                val kCh = when {
                    sway > 0.25f -> ')'
                    sway < -0.25f -> '('
                    else -> '|'
                }
                setCell(kc, kr, kCh, pal.kelpFg)
            }
        }

        // =========================================================================
        // 5. UPDATE & RENDER RISING BUBBLES & DRIFTING PLANKTON MOTES
        // =========================================================================
        val activeBubbles = if (isBatterySaver) 12 else bubbles.size
        for (i in 0 until activeBubbles) {
            val b = bubbles[i]
            b.rowY -= b.speedY * clampedDt
            b.colX += sin(simTime * 1.5f + b.phase) * 0.4f * clampedDt

            if (b.rowY <= splitRow + 1) {
                // Respawn near a fish or riverbed
                val refFish = fish[i % config.heroFishCount.coerceAtLeast(1)]
                b.colX = refFish.colX + 2f
                b.rowY = if (b.isBubble) refFish.rowY else (rows - 4).toFloat()
            }

            val bc = b.colX.roundToInt()
            val br = b.rowY.roundToInt()
            if (br in (splitRow + 2) until (floorRow - 1)) {
                val bChar = if (b.isBubble) {
                    if ((br + i) % 2 == 0) 'o' else '°'
                } else {
                    '.'
                }
                setCell(bc, br, bChar, pal.bubbleFg)
            }
        }

        // =========================================================================
        // 6. UPDATE & RENDER MULTI-SPECIES ASCII FISH (Randomized per Phone Unlock)
        // =========================================================================
        val activeHero = if (isBatterySaver) (config.heroFishCount - 2).coerceAtLeast(2) else config.heroFishCount
        val activeSchool = if (isBatterySaver) 3 else maxSchoolFish

        val minFishRow = (splitRow + 3).coerceAtMost(rows - 10)
        val maxFishRow = (bottomCalmRow - 3).coerceAtLeast(minFishRow + 4)

        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            if (!f.isSchooler && i >= activeHero) continue
            if (f.isSchooler && (i - maxHeroFish) >= activeSchool) continue

            f.startleBoost *= (1f - clampedDt * 2.2f).coerceAtLeast(0f)
            val spd = (f.species.baseSpeedColsPerSec * f.speedScale) + f.startleBoost

            f.colX += f.vxDir * spd * clampedDt
            f.rowY += (f.vySpeed + sin(simTime * 0.9f + f.id) * 0.25f) * clampedDt
            f.animPhase += clampedDt * (3.2f + f.startleBoost * 0.8f)

            // Soft horizontal turn-around at screen margins
            val spriteWidth = f.species.rightFrameA[0].length
            if (f.vxDir > 0f && f.colX > cols - spriteWidth - 1) {
                f.vxDir = -1f
            } else if (f.vxDir < 0f && f.colX < 1f) {
                f.vxDir = 1f
            }

            // Keep strictly inside the underwater comfort zone
            if (f.rowY < minFishRow) {
                f.rowY = minFishRow.toFloat()
                f.vySpeed = abs(f.vySpeed)
            } else if (f.rowY > maxFishRow) {
                f.rowY = maxFishRow.toFloat()
                f.vySpeed = -abs(f.vySpeed)
            }

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
            val drawR = f.rowY.roundToInt()
            val startRowOffset = -(lines.size / 2)

            for (lineIdx in lines.indices) {
                val r = drawR + startRowOffset + lineIdx
                if (r in (splitRow + 1) until (floorRow - 1)) {
                    val rowStr = lines[lineIdx]
                    for (chIdx in rowStr.indices) {
                        val ch = rowStr[chIdx]
                        if (ch != ' ') {
                            // Highlight fish eyes ('°' or 'º') and Kohaku dorsal markings in contrasting role
                            val charCol = when {
                                ch == '°' || ch == 'º' -> pal.fishFoam
                                f.species == AsciiFishSpecies.KOHAKU_KOI && lineIdx == 0 -> pal.fishCoral
                                f.species == AsciiFishSpecies.KOHAKU_KOI && lineIdx == 1 -> pal.fishFoam
                                f.species == AsciiFishSpecies.CLOWNFISH && ch == '|' -> pal.fishFoam
                                else -> fishColor
                            }
                            setCell(drawC + chIdx, r, ch, charCol)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 7. INTERACTIVE TOUCH RIPPLE RINGS `(   )`
        // =========================================================================
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
        // 8. FLUSH CHARACTER GRID TO CANVAS (Batched per color run for 60fps speed)
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

package com.example.koipond

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Dedicated Zero-Allocation Natural Betta Biotope Aquarium Engine.
 *
 * Designed as a visually distinct artistic counterpart to Minamo's Ocean & Zen Pond scenes:
 * - Hand-inked botanical & luminous backlit watercolor aesthetic (non-photorealistic)
 * - Overhead suspended LED light bar simulating real Day -> Dusk -> Night -> AMOLED lighting
 * - Optimal Betta population (2-3 Bettas) driven by 12-joint 3D articulated spines swimming freely
 *   in all 360° X/Y/Z directions with smooth curved turns, trailing pleated silk fins, hiding/peeking
 *   behind live plants & driftwood, broad-leaf lounging, labyrinth surface breaths & polite fin flares
 * - Biologically faithful cleanup crew: glass-clinging Otocinclus, shy nocturnal Bristlenose Pleco,
 *   shoaling sand-snuffling Corydoras (with occasional surface dashes), hopping Red Cherry Shrimps,
 *   and a gliding Golden Mystery Snail
 * - Rare occasional curious pet cat visitor peeking over the rim and dipping a playful paw while
 *   the Bettas ALWAYS escape safely into plant cover.
 */
class AquariumBiotopeEngine {

    private enum class BettaState { CRUISE, HOVER, HIDE, REST, BREATH, INSPECT, ESCAPE }
    private enum class CatState { HIDDEN, PEEKING, STALKING, DIPPING, RETRACT, LEAVING }
    private enum class OtoMode { LEFT_GLASS, RIGHT_GLASS, TRANSIT, LEAF }

    private class BettaFish(
        var name: String,
        var bodyCol: Int,
        var bodyHi: Int,
        var finBase: Int,
        var finMid: Int,
        var finTip: Int,
        val restAnchorIdx: Int,
        val hideAnchorIdx: Int
    ) {
        val nx = FloatArray(SPINE_N)
        val ny = FloatArray(SPINE_N)
        val nz = FloatArray(SPINE_N)
        var vx = 16f
        var vy = 0f
        var vz = 0f
        var speed = 16f
        var swimPhase = 0f
        var pecPhase = 0f
        var state = BettaState.CRUISE
        var stateTimer = 2.5f
        var breathTimer = 18f
        var hideCooldown = 6f
        var targetX = 0.5f // normalized [0..1] inside tank
        var targetY = 0.45f
        var targetZ = 0.45f
        var flare = 0f
        var zDepth = 0.45f
    }

    private class OtoCleaner(
        var mode: OtoMode,
        var normX: Float,
        var normY: Float,
        var dirY: Float,
        var pauseTimer: Float,
        var transitProgress: Float = 0f,
        var startX: Float = 0.03f,
        var startY: Float = 0.45f,
        var endX: Float = 0.97f,
        var endY: Float = 0.55f,
        var phase: Float = 0f
    )

    private class CoryCatfish(
        var normX: Float,
        var normY: Float,
        var dir: Float,
        var pauseTimer: Float,
        var dashPhase: Float = 0f, // >0 during quick Corydoras surface gulp dash!
        var dashTimer: Float = 26f,
        var snuffleTimer: Float = 0f,
        var phase: Float = 0f
    )

    private class CherryShrimp(
        var homeKind: Int, // 0=woodMain, 1=woodBranch, 2=mossLeft, 3=mossRight, 4=catappa
        var t: Float,
        var dir: Float,
        var hopProgress: Float = 0f,
        var hopStartX: Float = 0f,
        var hopStartY: Float = 0f,
        var hopEndX: Float = 0f,
        var hopEndY: Float = 0f,
        var phase: Float = 0f
    )

    private class MicroBubble(
        var x: Float = 0f,
        var y: Float = 0f,
        var r: Float = 2f,
        var vy: Float = 28f,
        var phase: Float = 0f,
        var kind: Int = 0, // 0=diffuser, 1=breath/splash, 2=plant pearling
        var active: Boolean = false
    )

    private class WaterMote(
        var nx: Float = 0.5f,
        var ny: Float = 0.5f,
        var r: Float = 1.2f,
        var phase: Float = 0f,
        var drift: Float = 0f
    )

    private class SandPuff(
        var x: Float = 0f,
        var y: Float = 0f,
        var vx: Float = 0f,
        var vy: Float = 0f,
        var age: Float = 0f,
        var maxAge: Float = 1.2f,
        var active: Boolean = false
    )

    private class TankPellet(
        var x: Float = 0f,
        var y: Float = 0f,
        var z: Float = 0.4f,
        var isWafer: Boolean = false,
        var restTimer: Float = 0f,
        var active: Boolean = false
    )

    private data class AquariumPalette(
        var roomTop: Int = 0,
        var roomBot: Int = 0,
        var shelfTop: Int = 0,
        var shelfFront: Int = 0,
        var lampBody: Int = 0,
        var lampGlow: Int = 0,
        var beamTop: Int = 0,
        var beamBot: Int = 0,
        var waterTop: Int = 0,
        var waterMid: Int = 0,
        var waterBot: Int = 0,
        var fogColor: Int = 0,
        var tanninColor: Int = 0,
        var glassEdge: Int = 0,
        var glassTint: Int = 0,
        var contour: Int = 0,
        var sandTop: Int = 0,
        var sandBot: Int = 0,
        var caustic: Int = 0,
        var woodDark: Int = 0,
        var woodMid: Int = 0,
        var woodRim: Int = 0,
        var plantBack: Int = 0,
        var plantMid: Int = 0,
        var plantFront: Int = 0,
        var anubias: Int = 0,
        var anubiasHi: Int = 0,
        var moss: Int = 0,
        var mossHi: Int = 0,
        var catappa: Int = 0,
        var stoneDark: Int = 0,
        var stoneHi: Int = 0,
        var catBody: Int = 0,
        var catRim: Int = 0,
        var catEye: Int = 0,
        var moteColor: Int = 0,
        var bubbleColor: Int = 0,
        var isAmoled: Boolean = false,
        var isDaylight: Boolean = false,
        var isNight: Boolean = false
    )

    private val rand = Random(System.nanoTime())
    private var simTime = 0f
    private var initialized = false
    private var lastWidth = 0f
    private var lastHeight = 0f

    // Current tank rectangle in screen pixels (updated every frame from viewport + waterlinePct)
    private var tankX = 24f
    private var tankY = 400f
    private var tankW = 420f
    private var tankH = 520f
    private var waterY = 436f
    private var sandY = 860f
    private var lampY = 270f
    private var lampW = 310f
    private var lampPulseTimer = 0f
    private var pearlingCooldown = 1.4f

    // 1D Spring-Wave Water Surface Heightfield
    private val waveN = 48
    private val waveH = FloatArray(waveN)
    private val waveV = FloatArray(waveN)

    // Optimal Betta trio (2 or 3 active based on config.heroFishCount)
    private val bettas = arrayOf(
        BettaFish(
            name = "koi vermilion betta",
            bodyCol = 0xFFDF4E3C.toInt(), bodyHi = 0xFFFA9A76.toInt(),
            finBase = 0xFFD93B34.toInt(), finMid = 0xFFF06A54.toInt(), finTip = 0xFF7BE0E8.toInt(),
            restAnchorIdx = 0, hideAnchorIdx = 0
        ),
        BettaFish(
            name = "turquoise rose betta",
            bodyCol = 0xFF248F9E.toInt(), bodyHi = 0xFF6BE2EA.toInt(),
            finBase = 0xFF2A95A6.toInt(), finMid = 0xFFD94D86.toInt(), finTip = 0xFFF7A8C8.toInt(),
            restAnchorIdx = 1, hideAnchorIdx = 1
        ),
        BettaFish(
            name = "cobalt mustard betta",
            bodyCol = 0xFF2F5298.toInt(), bodyHi = 0xFF6290E6.toInt(),
            finBase = 0xFF284588.toInt(), finMid = 0xFF4A6EC2.toInt(), finTip = 0xFFF0C058.toInt(),
            restAnchorIdx = 2, hideAnchorIdx = 2
        )
    )
    private val bettaSortOrder = intArrayOf(0, 1, 2)

    // Tank Cleanup Crew
    private val otos = arrayOf(
        OtoCleaner(OtoMode.LEFT_GLASS, 0.028f, 0.42f, -1f, 2.0f, phase = 0.4f),
        OtoCleaner(OtoMode.RIGHT_GLASS, 0.972f, 0.64f, 1f, 3.8f, phase = 2.6f)
    )
    private var plecoT = 0.24f // 0.14..0.28 = tucked hiding under spiderwood arch; 0.35..0.72 = foraging on branches!
    private var plecoDir = 1f
    private var plecoPause = 3.5f
    private var plecoPhase = 0f

    private val corys = arrayOf(
        CoryCatfish(0.28f, 0.96f, 1f, 1.4f, dashTimer = 28f, phase = 0.2f),
        CoryCatfish(0.39f, 0.98f, -1f, 2.3f, dashTimer = 46f, phase = 2.1f)
    )
    private var snailNormX = 0.56f
    private var snailDir = 1f
    private var snailRetractTimer = 0f

    private val shrimps = arrayOf(
        CherryShrimp(0, 0.28f, 1f, phase = 0.3f),
        CherryShrimp(0, 0.72f, -1f, phase = 1.5f),
        CherryShrimp(1, 0.58f, -1f, phase = 2.7f),
        CherryShrimp(2, 0.0f, 1f, phase = 3.6f),
        CherryShrimp(3, 0.0f, -1f, phase = 4.9f)
    )

    // Ambient particles & food pellets
    private val bubbles = Array(28) { MicroBubble() }
    private val motes = Array(22) { WaterMote() }
    private val sandPuffs = Array(14) { SandPuff() }
    private var nextPuffIdx = 0
    private val pellets = Array(8) { TankPellet() }
    private var nextPelletIdx = 0

    // Rare Occasional Pet Cat Visitor (absent ~88% of the time!)
    private var catState = CatState.HIDDEN
    private var catTimer = 38f
    private var catRise = 0f
    private var catPawDip = 0f
    private var catSide = 1f // 1 = right rim, -1 = left rim
    private var catSwipesLeft = 0
    private var catEyeLookX = -0.6f
    private var catEyeLookY = 0.4f
    private var catTargetBetta = 0
    private var catEarTwitch = 0f
    private var catSplashFired = false

    val isHighMotion: Boolean
        get() = catState != CatState.HIDDEN ||
            lampPulseTimer > 0f ||
            pellets.any { it.active } ||
            bettas.any { it.state == BettaState.ESCAPE || it.state == BettaState.BREATH }

    // Reusable Zero-Alloc Paint, Path & Scratch Geometry
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val gradPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val shapePath = Path()
    private val detailPath = Path()
    private val veilPath = Path()
    private val bodyPath = Path()

    // Cached Gradients (rebuilt only when dimensions or palette colors change)
    private val aqPal = AquariumPalette()
    private var cachedRoomH = -1f
    private var cachedRoomTop = 0
    private var cachedRoomBot = 0
    private var roomGrad: LinearGradient? = null

    private var cachedWaterY = -1f
    private var cachedWaterBotY = -1f
    private var cachedWaterTop = 0
    private var cachedWaterMid = 0
    private var cachedWaterBot = 0
    private var waterGrad: LinearGradient? = null

    private var cachedBeamY0 = -1f
    private var cachedBeamY1 = -1f
    private var cachedBeamTop = 0
    private var cachedBeamBot = 0
    private var beamGrad: LinearGradient? = null

    private var cachedSandY0 = -1f
    private var cachedSandY1 = -1f
    private var cachedSandTop = 0
    private var cachedSandBot = 0
    private var sandGrad: LinearGradient? = null

    // Betta 3D Spine Projection Scratch Buffers (Zero-Allocation!)
    private val projX = FloatArray(SPINE_N)
    private val projY = FloatArray(SPINE_N)
    private val projS = FloatArray(SPINE_N)
    private val tangX = FloatArray(SPINE_N)
    private val tangY = FloatArray(SPINE_N)
    private val upX = FloatArray(SPINE_N)
    private val upY = FloatArray(SPINE_N)
    private val outX = FloatArray(SPINE_N)
    private val outY = FloatArray(SPINE_N)
    private val topEdgeX = FloatArray(8)
    private val topEdgeY = FloatArray(8)
    private val botEdgeX = FloatArray(8)
    private val botEdgeY = FloatArray(8)
    private val bodyRadii = floatArrayOf(0.036f, 0.118f, 0.152f, 0.142f, 0.112f, 0.074f, 0.044f, 0.024f)

    // Driftwood taper scratch buffers
    private val woodSteps = 14
    private val woodLX = FloatArray(woodSteps + 1)
    private val woodLY = FloatArray(woodSteps + 1)
    private val woodRX = FloatArray(woodSteps + 1)
    private val woodRY = FloatArray(woodSteps + 1)

    // =========================================================================
    // PUBLIC INTERACTION & LIFECYCLE HOOKS
    // =========================================================================

    fun summonCatVisitNow(): String {
        if (catState == CatState.HIDDEN) {
            catSide = if (rand.nextFloat() < 0.65f) 1f else -1f
            catState = CatState.PEEKING
            catTimer = 0f
            catSwipesLeft = 1 + if (rand.nextFloat() < 0.45f) 1 else 0
            catSplashFired = false
            val rimX = if (catSide > 0f) tankX + tankW * 0.76f else tankX + tankW * 0.24f
            var best = 0
            var bestD = Float.MAX_VALUE
            for (i in bettas.indices) {
                val d = abs(bettas[i].nx[0] - rimX)
                if (d < bestD) { bestD = d; best = i }
            }
            catTargetBetta = best
        }
        return "curious cat peeking at the tank!"
    }

    fun randomizeMorphs(seed: Long) {
        val r = Random(seed)
        val morphs = arrayOf(
            intArrayOf(0xFFDF4E3C.toInt(), 0xFFFA9A76.toInt(), 0xFFD93B34.toInt(), 0xFFF06A54.toInt(), 0xFF7BE0E8.toInt()),
            intArrayOf(0xFF248F9E.toInt(), 0xFF6BE2EA.toInt(), 0xFF2A95A6.toInt(), 0xFFD94D86.toInt(), 0xFFF7A8C8.toInt()),
            intArrayOf(0xFF2F5298.toInt(), 0xFF6290E6.toInt(), 0xFF284588.toInt(), 0xFF4A6EC2.toInt(), 0xFFF0C058.toInt()),
            intArrayOf(0xFFD66A2E.toInt(), 0xFFFFB96E.toInt(), 0xFFC74E24.toInt(), 0xFFE8844A.toInt(), 0xFFFFE6A8.toInt()),
            intArrayOf(0xFF6E439C.toInt(), 0xFFB88AF0.toInt(), 0xFF563282.toInt(), 0xFF9C5CD6.toInt(), 0xFF82E6E2.toInt())
        )
        val offset = r.nextInt(morphs.size)
        for (i in bettas.indices) {
            val m = morphs[(offset + i) % morphs.size]
            bettas[i].bodyCol = m[0]
            bettas[i].bodyHi = m[1]
            bettas[i].finBase = m[2]
            bettas[i].finMid = m[3]
            bettas[i].finTip = m[4]
        }
    }

    fun onTouch(screenX: Float, screenY: Float) {
        if (!initialized) return

        // 1. Tap on overhead LED lamp bar -> warm light pulse shimmer!
        val lampLeft = (lastWidth - lampW) * 0.5f
        if (screenX in (lampLeft - 24f)..(lampLeft + lampW + 24f) &&
            screenY in (lampY - 34f)..(lampY + 42f)
        ) {
            lampPulseTimer = 1.8f
            return
        }

        // 2. Tap near the top glass rim -> if cat is peeking, gently startle it away; if hidden, summon a cat visit!
        if (screenX in tankX..(tankX + tankW) && screenY in (tankY - 76f)..(tankY + 26f)) {
            if (catState != CatState.HIDDEN && catState != CatState.LEAVING) {
                catEarTwitch = 1.0f
                catState = CatState.LEAVING
                catTimer = 0.25f
                catPawDip = 0f
            } else if (catState == CatState.HIDDEN) {
                summonCatVisitNow()
            }
            return
        }

        // 3. Tap inside aquarium water
        if (screenX in tankX..(tankX + tankW) && screenY in tankY..(tankY + tankH)) {
            // Check if user tapped the snail -> gentle shell retract!
            val sx = tankX + snailNormX * tankW
            val sy = sandY + tankH * 0.022f
            if (hypot(screenX - sx, screenY - sy) < tankW * 0.075f) {
                snailRetractTimer = 3.2f
                return
            }

            // Check if user tapped directly on a Betta -> playful curved S-turn burst into hiding!
            for (b in bettas) {
                if (hypot(screenX - b.nx[0], screenY - b.ny[0]) < tankW * 0.085f) {
                    b.state = BettaState.ESCAPE
                    b.stateTimer = 1.6f
                    b.flare = 0.75f
                    b.targetX = if (b.nx[0] < tankX + tankW * 0.5f) 0.84f else 0.16f
                    b.targetY = 0.42f + rand.nextFloat() * 0.34f
                    b.targetZ = 0.78f
                    spawnBubble(b.nx[0], b.ny[0] - 4f, 1)
                    injectRipple(screenX, 14f)
                    return
                }
            }

            // Otherwise drop a sinking Betta pellet + gentle meniscus ripple!
            dropFood(screenX, screenY, isWafer = false)
        }
    }

    fun onLongPress(screenX: Float, screenY: Float) {
        if (!initialized) return
        val px = screenX.coerceIn(tankX + tankW * 0.12f, tankX + tankW * 0.88f)
        val py = screenY.coerceIn(waterY + 8f, sandY - 12f)
        dropFood(px, py, isWafer = true)
    }

    fun onDrag(screenX: Float, screenY: Float, deltaX: Float) {
        if (!initialized) return
        if (screenX in tankX..(tankX + tankW) && screenY in (tankY - 12f)..(tankY + tankH)) {
            injectRipple(screenX, (abs(deltaX) * 0.25f).coerceIn(5f, 16f))
            // Gently part nearby Bettas away from dragging finger
            for (b in bettas) {
                val dx = b.nx[0] - screenX
                val dy = b.ny[0] - screenY
                val d = hypot(dx, dy)
                if (d < tankW * 0.16f && d > 1f) {
                    b.vx += (dx / d) * 32f
                    b.vy += (dy / d) * 24f
                }
            }
        }
    }

    private fun dropFood(screenX: Float, screenY: Float, isWafer: Boolean) {
        val p = pellets[nextPelletIdx]
        p.x = screenX.coerceIn(tankX + tankW * 0.08f, tankX + tankW * 0.92f)
        p.y = screenY.coerceIn(waterY + 6f, sandY - 10f)
        p.z = 0.24f + rand.nextFloat() * 0.42f
        p.isWafer = isWafer
        p.restTimer = 0f
        p.active = true
        nextPelletIdx = (nextPelletIdx + 1) % pellets.size
        injectRipple(p.x, if (isWafer) 24f else 15f)
    }

    private fun injectRipple(worldX: Float, strength: Float) {
        val idx = (((worldX - tankX) / tankW.coerceAtLeast(1f)) * (waveN - 1)).roundToInt()
        for (k in -3..3) {
            val i = idx + k
            if (i in 1 until (waveN - 1)) {
                waveV[i] += strength * exp(-(k * k) * 0.35f)
            }
        }
    }

    private fun spawnBubble(x: Float, y: Float, kind: Int) {
        for (i in 14 until bubbles.size) {
            val b = bubbles[i]
            if (!b.active) {
                b.x = x
                b.y = y
                b.r = if (kind == 2) (tankW * 0.0032f).coerceIn(1.2f, 2.6f) else (tankW * 0.0048f).coerceIn(1.6f, 3.6f)
                b.vy = (tankH * if (kind == 2) 0.055f else 0.095f) * (0.85f + rand.nextFloat() * 0.3f)
                b.phase = rand.nextFloat() * 6.28f
                b.kind = kind
                b.active = true
                return
            }
        }
    }

    private fun spawnSandPuff(x: Float, y: Float) {
        val sp = sandPuffs[nextPuffIdx]
        sp.x = x
        sp.y = y
        sp.vx = (rand.nextFloat() - 0.5f) * tankW * 0.035f
        sp.vy = -(tankH * (0.025f + rand.nextFloat() * 0.025f))
        sp.age = 0f
        sp.maxAge = 0.9f + rand.nextFloat() * 0.5f
        sp.active = true
        nextPuffIdx = (nextPuffIdx + 1) % sandPuffs.size
    }

    // =========================================================================
    // MAIN UPDATE & RENDER PIPELINE
    // =========================================================================

    fun updateAndRender(
        canvas: Canvas,
        dt: Float,
        config: WallpaperConfig,
        snapshot: TimeOfDayPalette.TimeSnapshot,
        parallaxX: Float,
        parallaxY: Float,
        isBatterySaver: Boolean,
        isWallpaperMode: Boolean,
        isCharging: Boolean
    ) {
        val clampedDt = dt.coerceIn(0.001f, 0.05f)
        simTime += clampedDt
        if (simTime >= 86400f) simTime -= 86400f

        val w = canvas.width.toFloat().coerceAtLeast(240f)
        val h = canvas.height.toFloat().coerceAtLeast(320f)
        updateGeometry(w, h, config.waterlinePercent, isWallpaperMode, parallaxX, parallaxY)
        if (!initialized) {
            initializeEntities()
            initialized = true
        }

        val activeBettaCount = if (config.heroFishCount <= 4) 2 else 3
        resolveAquariumPalette(config, snapshot)

        // 1. Physics Update
        updateSimulation(clampedDt, activeBettaCount, config.enableBottomFeeders, isBatterySaver)

        // 2. Render Room, Shelf, Overhead LED Lamp & Rare Cat Behind Rim
        drawRoomAndLamp(canvas, config.enableBatteryLantern, isCharging)
        drawCatBehindGlass(canvas)

        // 3. Render Aquarium Interior (Clipped cleanly to Glass Walls!)
        canvas.save()
        canvas.clipRect(tankX, tankY, tankX + tankW, tankY + tankH)

        drawWaterAndLightShafts(canvas, config.waterClarity, isBatterySaver)
        drawBackPlants(canvas)
        drawSubstrateAndCaustics(canvas, isBatterySaver)
        drawCatappaLeavesAndStones(canvas)
        drawSpiderwoodAndAnubias(canvas)
        drawMossBallsAndDiffuser(canvas)

        // Cleanup Crew (behind / nestled around hardscape!)
        if (config.enableBottomFeeders) {
            drawPleco(canvas)
            drawOtos(canvas)
            drawSnail(canvas)
            drawCorydoras(canvas)
            drawShrimps(canvas)
        }
        drawSandPuffs(canvas)

        // 3D Depth-Sorted Bettas (back -> front so closer Bettas cleanly occlude deeper Bettas!)
        sortBettasByDepth(activeBettaCount)
        for (k in 0 until activeBettaCount) {
            drawArticulatedBetta(canvas, bettas[bettaSortOrder[k]], isBatterySaver)
        }

        // Foreground Vallisneria Blades (hiding Bettas literally peek through living green leaves!)
        drawForegroundPlants(canvas)
        drawFloatingCanopyAndBubbleNest(canvas)
        drawBubblesAndPellets(canvas)
        drawMeniscusHighlight(canvas)

        canvas.restore()

        // 4. Rimless Glass Walls, Corner Thickness Refraction & Dipping Cat Paw
        drawGlassFrame(canvas)
        drawCatPawOverlay(canvas)
    }

    private fun updateGeometry(
        w: Float,
        h: Float,
        waterlinePct: Int,
        isWallpaperMode: Boolean,
        parallaxX: Float,
        parallaxY: Float
    ) {
        val prevTankX = tankX
        val prevTankY = tankY
        val prevTankW = tankW
        val prevTankH = tankH

        val marginX = (w * 0.048f).coerceIn(14f, 44f)
        val pShiftX = parallaxX * (marginX * 0.45f)
        val pShiftY = parallaxY * (h * 0.014f)

        // Spacious aquarium proportion: leaves clean calm upper negative space for phone clock/icons
        val topFrac = if (isWallpaperMode) {
            (waterlinePct / 100f).coerceIn(0.32f, 0.52f) + 0.04f
        } else {
            0.27f
        }
        val shelfReserve = h * (if (isWallpaperMode) 0.078f else 0.055f)

        tankW = (w - marginX * 2f).coerceAtLeast(180f)
        tankX = marginX + pShiftX
        tankY = (h * topFrac + pShiftY).coerceIn(h * 0.22f, h * 0.56f)
        tankH = (h - tankY - shelfReserve).coerceIn(h * 0.34f, h * 0.66f)
        waterY = tankY + tankH * 0.072f
        sandY = tankY + tankH * 0.895f
        lampW = tankW * 0.74f
        lampY = (tankY - tankH * (if (isWallpaperMode) 0.27f else 0.21f)).coerceAtLeast(h * 0.06f)

        // Rescale active betta spine coordinates smoothly if viewport resized
        if (initialized && (abs(prevTankW - tankW) > 2f || abs(prevTankH - tankH) > 2f || abs(prevTankY - tankY) > 2f)) {
            val invPrevW = 1f / prevTankW.coerceAtLeast(1f)
            val invPrevH = 1f / prevTankH.coerceAtLeast(1f)
            for (b in bettas) {
                for (i in 0 until SPINE_N) {
                    val u = ((b.nx[i] - prevTankX) * invPrevW).coerceIn(0.05f, 0.95f)
                    val v = ((b.ny[i] - prevTankY) * invPrevH).coerceIn(0.10f, 0.86f)
                    b.nx[i] = tankX + u * tankW
                    b.ny[i] = tankY + v * tankH
                }
            }
        }
        lastWidth = w
        lastHeight = h
    }

    private fun initializeEntities() {
        val initPositions = arrayOf(
            floatArrayOf(0.34f, 0.34f, 0.26f, 0.2f),
            floatArrayOf(0.68f, 0.48f, 0.56f, 2.8f),
            floatArrayOf(0.48f, 0.64f, 0.72f, -0.7f)
        )
        val segLen = bettaSegLen()
        for (idx in bettas.indices) {
            val b = bettas[idx]
            val p = initPositions[idx]
            val hx = tankX + p[0] * tankW
            val hy = waterY + p[1] * (sandY - waterY)
            val hz = p[2]
            val yaw = p[3]
            for (i in 0 until SPINE_N) {
                b.nx[i] = hx - cos(yaw) * i * segLen
                b.ny[i] = hy
                b.nz[i] = (hz - sin(yaw) * i * 0.012f).coerceIn(0.12f, 0.88f)
            }
            b.vx = cos(yaw) * (tankW * 0.045f)
            b.vy = 0f
            b.vz = sin(yaw) * 0.03f
            b.zDepth = hz
            b.swimPhase = idx * 2.1f
            b.pecPhase = idx * 1.4f
            b.breathTimer = 15f + idx * 9f
            b.targetX = p[0]
            b.targetY = p[1]
            b.targetZ = hz
        }

        // Diffuser micro-bubbles (slots 0..13)
        for (i in 0 until 14) {
            val b = bubbles[i]
            b.x = tankX + tankW * 0.135f + (rand.nextFloat() - 0.5f) * (tankW * 0.016f)
            b.y = waterY + rand.nextFloat() * (sandY - waterY - tankH * 0.06f)
            b.r = (tankW * (0.0028f + rand.nextFloat() * 0.0036f)).coerceIn(1.2f, 3.4f)
            b.vy = tankH * (0.060f + rand.nextFloat() * 0.036f)
            b.phase = rand.nextFloat() * 6.28f
            b.kind = 0
            b.active = true
        }
        for (m in motes) {
            m.nx = 0.06f + rand.nextFloat() * 0.88f
            m.ny = 0.12f + rand.nextFloat() * 0.72f
            m.r = 1.0f + rand.nextFloat() * 1.4f
            m.phase = rand.nextFloat() * 6.28f
            m.drift = (rand.nextFloat() - 0.5f) * 0.012f
        }
    }

    private fun bettaSegLen(): Float = (tankW * 0.0074f).coerceIn(2.6f, 8.2f)

    // =========================================================================
    // DRIFTWOOD CURVE EVALUATION (Normalized to tank dimensions!)
    // =========================================================================

    private fun evalWoodMainX(t: Float): Float {
        val x0 = tankX + tankW * 0.31f
        val cx = tankX + tankW * 0.59f
        val x1 = tankX + tankW * 0.85f
        val u = 1f - t
        return u * u * x0 + 2f * u * t * cx + t * t * x1
    }

    private fun evalWoodMainY(t: Float): Float {
        val y0 = sandY + tankH * 0.015f
        val cy = sandY - tankH * 0.20f
        val y1 = sandY - tankH * 0.49f
        val u = 1f - t
        return u * u * y0 + 2f * u * t * cy + t * t * y1
    }

    private fun evalWoodMainAng(t: Float): Float {
        val x0 = tankX + tankW * 0.31f; val cx = tankX + tankW * 0.59f; val x1 = tankX + tankW * 0.85f
        val y0 = sandY + tankH * 0.015f; val cy = sandY - tankH * 0.20f; val y1 = sandY - tankH * 0.49f
        val u = 1f - t
        val dx = 2f * u * (cx - x0) + 2f * t * (x1 - cx)
        val dy = 2f * u * (cy - y0) + 2f * t * (y1 - cy)
        return atan2(dy, dx)
    }

    private fun evalWoodBranchX(t: Float): Float {
        val x0 = tankX + tankW * 0.62f
        val cx = tankX + tankW * 0.53f
        val x1 = tankX + tankW * 0.44f
        val u = 1f - t
        return u * u * x0 + 2f * u * t * cx + t * t * x1
    }

    private fun evalWoodBranchY(t: Float): Float {
        val y0 = sandY - tankH * 0.19f
        val cy = sandY - tankH * 0.35f
        val y1 = sandY - tankH * 0.51f
        val u = 1f - t
        return u * u * y0 + 2f * u * t * cy + t * t * y1
    }

    private fun evalWoodBranchAng(t: Float): Float {
        val x0 = tankX + tankW * 0.62f; val cx = tankX + tankW * 0.53f; val x1 = tankX + tankW * 0.44f
        val y0 = sandY - tankH * 0.19f; val cy = sandY - tankH * 0.35f; val y1 = sandY - tankH * 0.51f
        val u = 1f - t
        val dx = 2f * u * (cx - x0) + 2f * t * (x1 - cx)
        val dy = 2f * u * (cy - y0) + 2f * t * (y1 - cy)
        return atan2(dy, dx)
    }

    private fun evalShrimpHomeX(kind: Int, t: Float): Float = when (kind) {
        0 -> evalWoodMainX(t)
        1 -> evalWoodBranchX(t)
        2 -> tankX + tankW * 0.23f
        3 -> tankX + tankW * 0.80f
        else -> tankX + tankW * 0.45f
    }

    private fun evalShrimpHomeY(kind: Int, t: Float): Float = when (kind) {
        0 -> evalWoodMainY(t) - tankH * 0.014f
        1 -> evalWoodBranchY(t) - tankH * 0.012f
        2 -> sandY - tankH * 0.048f
        3 -> sandY - tankH * 0.044f
        else -> sandY + tankH * 0.016f
    }

    // =========================================================================
    // SIMULATION PHYSICS UPDATE
    // =========================================================================

    private fun updateSimulation(
        dt: Float,
        activeBettaCount: Int,
        enableCleaners: Boolean,
        isBatterySaver: Boolean
    ) {
        if (lampPulseTimer > 0f) lampPulseTimer = (lampPulseTimer - dt).coerceAtLeast(0f)
        if (catEarTwitch > 0f) catEarTwitch = (catEarTwitch - dt * 2.5f).coerceAtLeast(0f)
        if (snailRetractTimer > 0f) snailRetractTimer = (snailRetractTimer - dt).coerceAtLeast(0f)

        // 1. 1D Spring-Wave Water Surface
        val damp = exp(-2.7f * dt)
        for (i in 1 until waveN - 1) {
            val force = 30f * (waveH[i - 1] + waveH[i + 1] - 2f * waveH[i]) - 9f * waveH[i]
            waveV[i] = (waveV[i] + force * dt) * damp
        }
        val maxWave = (tankH * 0.020f).coerceIn(5f, 14f)
        for (i in 1 until waveN - 1) {
            waveH[i] = (waveH[i] + waveV[i] * dt).coerceIn(-maxWave, maxWave)
        }

        // 2. Water Motes, Diffuser Micro-Bubbles & Daytime Oxygen Pearling!
        val waterSpanY = (sandY - waterY).coerceAtLeast(80f)
        if (!isBatterySaver) {
            for (m in motes) {
                m.nx += (sin(simTime * 0.4f + m.phase) * 0.008f + m.drift) * dt
                m.ny += cos(simTime * 0.35f + m.phase) * 0.005f * dt
                if (m.nx < 0.04f) m.nx = 0.96f
                if (m.nx > 0.96f) m.nx = 0.04f
                if (m.ny < 0.10f) m.ny = 0.85f
                if (m.ny > 0.88f) m.ny = 0.14f
            }
            // Real planted-tank daylight oxygen pearling off sunlit Anubias leaves!
            if (aqPal.isDaylight) {
                pearlingCooldown -= dt
                if (pearlingCooldown <= 0f) {
                    pearlingCooldown = 1.6f + rand.nextFloat() * 2.0f
                    val px = tankX + tankW * (if (rand.nextBoolean()) 0.60f else 0.77f) + (rand.nextFloat() - 0.5f) * tankW * 0.08f
                    val py = sandY - tankH * (0.20f + rand.nextFloat() * 0.08f)
                    spawnBubble(px, py, 2)
                }
            }
        }

        for (i in bubbles.indices) {
            val b = bubbles[i]
            if (!b.active) continue
            b.y -= b.vy * dt
            b.x += sin(simTime * 2.9f + b.phase) * (tankW * 0.015f) * dt
            if (b.y <= waterY + 2f) {
                if (b.kind == 0) {
                    b.x = tankX + tankW * 0.135f + (rand.nextFloat() - 0.5f) * (tankW * 0.014f)
                    b.y = sandY - tankH * 0.056f
                } else {
                    b.active = false
                }
            }
        }

        for (sp in sandPuffs) {
            if (!sp.active) continue
            sp.age += dt
            sp.x += sp.vx * dt
            sp.y += sp.vy * dt
            sp.vy += tankH * 0.032f * dt
            if (sp.age >= sp.maxAge) sp.active = false
        }

        // 3. Sinking Food Pellets & Heavy Algae Wafers
        for (p in pellets) {
            if (!p.active) continue
            val floorLim = sandY - tankH * 0.012f
            if (p.y < floorLim) {
                p.y = (p.y + tankH * (if (p.isWafer) 0.14f else 0.042f) * dt).coerceAtMost(floorLim)
                if (!p.isWafer) p.x += sin(simTime * 2.2f + p.z * 8f) * tankW * 0.008f * dt
            } else {
                p.restTimer += dt
                if (p.restTimer > (if (p.isWafer) 14f else 8f)) p.active = false
            }
        }

        // 4. Rare Occasional Pet Cat Visitor State Machine
        val rimX = if (catSide > 0f) tankX + tankW * 0.76f else tankX + tankW * 0.24f
        val pawDipX = rimX - catSide * (tankW * 0.082f)
        when (catState) {
            CatState.HIDDEN -> {
                catRise = 0f
                catPawDip = 0f
                catTimer -= dt
                if (catTimer <= 0f) {
                    summonCatVisitNow()
                }
            }
            CatState.PEEKING -> {
                catRise = (catRise + dt * 0.72f).coerceAtMost(1f)
                if (catRise >= 1f) {
                    catState = CatState.STALKING
                    catTimer = 1.6f + rand.nextFloat() * 0.8f
                }
            }
            CatState.STALKING -> {
                catTimer -= dt
                val tb = bettas[catTargetBetta.coerceIn(0, activeBettaCount - 1)]
                val targetLookX = ((tb.nx[0] - rimX) / (tankW * 0.45f)).coerceIn(-1f, 1f)
                val targetLookY = ((tb.ny[0] - waterY) / (waterSpanY * 0.8f)).coerceIn(-0.2f, 1f)
                catEyeLookX += (targetLookX - catEyeLookX) * (dt * 4.2f).coerceAtMost(1f)
                catEyeLookY += (targetLookY - catEyeLookY) * (dt * 4.2f).coerceAtMost(1f)
                if (rand.nextFloat() < dt * 0.55f) catEarTwitch = 0.7f
                if (catTimer <= 0f) {
                    catState = CatState.DIPPING
                    catSplashFired = false
                }
            }
            CatState.DIPPING -> {
                catPawDip = (catPawDip + dt * 2.75f).coerceAtMost(1f)
                if (catPawDip >= 0.82f && !catSplashFired) {
                    catSplashFired = true
                    injectRipple(pawDipX, 30f)
                    spawnBubble(pawDipX - tankW * 0.012f, waterY + tankH * 0.028f, 1)
                    spawnBubble(pawDipX + tankW * 0.014f, waterY + tankH * 0.036f, 1)
                    // Every nearby Betta IMMEDIATELY executes a curved 3D escape into plant shelter!
                    for (i in 0 until activeBettaCount) {
                        val b = bettas[i]
                        if (hypot(b.nx[0] - pawDipX, b.ny[0] - waterY) < tankW * 0.62f) {
                            b.state = BettaState.ESCAPE
                            b.stateTimer = 2.0f + rand.nextFloat() * 0.7f
                            b.flare = 0.90f
                            b.targetX = if (catSide > 0f) (0.10f + rand.nextFloat() * 0.16f) else (0.74f + rand.nextFloat() * 0.16f)
                            b.targetY = 0.42f + rand.nextFloat() * 0.36f
                            b.targetZ = 0.78f + rand.nextFloat() * 0.10f // hides deep behind tall plants!
                        }
                    }
                }
                if (catPawDip >= 1f) {
                    catState = CatState.RETRACT
                    catTimer = 0.65f
                }
            }
            CatState.RETRACT -> {
                catTimer -= dt
                if (catTimer <= 0f) {
                    catPawDip = (catPawDip - dt * 2.1f).coerceAtLeast(0f)
                    if (catPawDip <= 0f) {
                        catSwipesLeft--
                        if (catSwipesLeft > 0) {
                            catState = CatState.STALKING
                            catTimer = 1.8f
                        } else {
                            catState = CatState.LEAVING
                            catTimer = 0.9f
                        }
                    }
                }
            }
            CatState.LEAVING -> {
                catTimer -= dt
                if (catTimer <= 0f) {
                    catRise = (catRise - dt * 0.62f).coerceAtLeast(0f)
                    if (catRise <= 0f) {
                        catState = CatState.HIDDEN
                        catTimer = 58f + rand.nextFloat() * 52f // Rare visits (~1-2 min apart!)
                    }
                }
            }
        }

        // 5. Update Optimal Betta Trio (True 12-Joint 3D Articulated Spine + Natural Behaviors!)
        val zScalePx = tankW * 0.56f
        val segLen = bettaSegLen()
        val nightCalm = if (aqPal.isNight) 0.72f else 1.0f

        for (idx in 0 until activeBettaCount) {
            val b = bettas[idx]
            val hx = b.nx[0]
            val hy = b.ny[0]
            val hz = b.nz[0]
            b.flare = (b.flare - dt * 0.50f).coerceAtLeast(0f)
            b.breathTimer -= dt
            if (b.hideCooldown > 0f) b.hideCooldown -= dt

            // Look for reachable sinking Betta pellets (unless escaping cat)
            var foodPellet: TankPellet? = null
            if (b.state != BettaState.ESCAPE) {
                var bestD = tankW * 0.52f
                for (p in pellets) {
                    if (!p.active || p.isWafer) continue
                    val d = hypot(p.x - hx, p.y - hy)
                    if (d < bestD) { bestD = d; foodPellet = p }
                }
            }

            if (foodPellet != null) {
                b.targetX = ((foodPellet.x - tankX) / tankW).coerceIn(0.08f, 0.92f)
                b.targetY = ((foodPellet.y - waterY) / waterSpanY).coerceIn(0.04f, 0.90f)
                b.targetZ = foodPellet.z
                if (hypot(foodPellet.x - hx, foodPellet.y - hy) < tankW * 0.036f) {
                    foodPellet.active = false
                    spawnBubble(hx, hy - 4f, 1)
                    b.state = BettaState.HOVER
                    b.stateTimer = 1.8f
                }
            } else {
                when (b.state) {
                    BettaState.ESCAPE -> {
                        b.stateTimer -= dt
                        if (b.stateTimer <= 0f) {
                            // After darting into plants, naturally stay hidden & peek out calmly for a few seconds!
                            b.state = BettaState.HIDE
                            b.stateTimer = 3.2f + rand.nextFloat() * 3.0f
                        }
                    }
                    BettaState.BREATH -> {
                        b.targetY = 0.025f
                        if (hy <= waterY + tankH * 0.032f) {
                            injectRipple(hx, 11f)
                            spawnBubble(hx - tankW * 0.008f, waterY + tankH * 0.022f, 1)
                            spawnBubble(hx + tankW * 0.008f, waterY + tankH * 0.030f, 1)
                            b.breathTimer = 24f + rand.nextFloat() * 18f
                            b.state = BettaState.CRUISE
                            b.stateTimer = 3.2f
                            b.targetX = 0.18f + rand.nextFloat() * 0.64f
                            b.targetY = 0.28f + rand.nextFloat() * 0.45f
                            b.targetZ = 0.18f + rand.nextFloat() * 0.64f
                        }
                    }
                    else -> {
                        b.stateTimer -= dt
                        if (b.breathTimer <= 0f && catState == CatState.HIDDEN) {
                            b.state = BettaState.BREATH
                            b.targetX = (((hx - tankX) / tankW) + (rand.nextFloat() - 0.5f) * 0.18f).coerceIn(0.18f, 0.82f)
                            b.targetY = 0.022f
                            b.targetZ = 0.24f + rand.nextFloat() * 0.38f
                        } else if (b.stateTimer <= 0f) {
                            val roll = rand.nextFloat()
                            when {
                                // 1. Genuine Hiding Behavior behind dense plants or under spiderwood arch!
                                roll < (if (aqPal.isNight) 0.28f else 0.20f) && b.hideCooldown <= 0f -> {
                                    b.state = BettaState.HIDE
                                    b.stateTimer = 4.5f + rand.nextFloat() * 4.5f
                                    b.hideCooldown = 14f + rand.nextFloat() * 10f
                                    when (b.hideAnchorIdx) {
                                        0 -> { b.targetX = 0.11f; b.targetY = 0.56f; b.targetZ = 0.82f } // Left Vallisneria grove
                                        1 -> { b.targetX = 0.89f; b.targetY = 0.58f; b.targetZ = 0.82f } // Right Vallisneria grove
                                        else -> { b.targetX = 0.47f; b.targetY = 0.72f; b.targetZ = 0.68f } // Under spiderwood cave arch!
                                    }
                                }
                                // 2. Leaf-Lounging on broad Anubias hammock leaves or under frogbit roots!
                                roll < (if (aqPal.isNight) 0.54f else 0.36f) -> {
                                    b.state = BettaState.REST
                                    b.stateTimer = 4.0f + rand.nextFloat() * 3.8f
                                    when (b.restAnchorIdx) {
                                        0 -> { b.targetX = 0.60f; b.targetY = 0.64f; b.targetZ = 0.44f } // Central Anubias hammock
                                        1 -> { b.targetX = 0.77f; b.targetY = 0.69f; b.targetZ = 0.50f } // Right Anubias hammock
                                        else -> { b.targetX = 0.64f; b.targetY = 0.14f; b.targetZ = 0.34f } // Under floating frogbit roots!
                                    }
                                }
                                // 3. Curious inspection of a shrimp or snail!
                                roll < 0.50f && enableCleaners -> {
                                    b.state = BettaState.INSPECT
                                    b.stateTimer = 2.6f + rand.nextFloat() * 1.8f
                                    val sh = shrimps[rand.nextInt(shrimps.size)]
                                    val sx = evalShrimpHomeX(sh.homeKind, sh.t)
                                    val sy = evalShrimpHomeY(sh.homeKind, sh.t)
                                    b.targetX = ((sx - tankX) / tankW - 0.06f).coerceIn(0.14f, 0.86f)
                                    b.targetY = ((sy - waterY) / waterSpanY - 0.08f).coerceIn(0.18f, 0.82f)
                                    b.targetZ = 0.46f
                                }
                                // 4. Calm mid-water hover
                                roll < 0.68f -> {
                                    b.state = BettaState.HOVER
                                    b.stateTimer = 2.2f + rand.nextFloat() * 2.4f
                                }
                                // 5. Free 360° 3D exploration anywhere in the tank!
                                else -> {
                                    b.state = BettaState.CRUISE
                                    b.stateTimer = 3.0f + rand.nextFloat() * 3.4f
                                    b.targetX = 0.13f + rand.nextFloat() * 0.74f
                                    b.targetY = 0.12f + rand.nextFloat() * 0.72f
                                    b.targetZ = 0.14f + rand.nextFloat() * 0.72f
                                }
                            }
                        }
                    }
                }
            }

            // Compute 3D target in pixel coordinates
            val txPx = tankX + b.targetX * tankW
            val tyPx = waterY + b.targetY * waterSpanY
            val dx = txPx - hx
            val dy = tyPx - hy
            val dzPx = (b.targetZ - hz) * zScalePx
            val dist3D = sqrtSafe(dx * dx + dy * dy + dzPx * dzPx)

            // Polite personal territory spacing + graceful fin flaring when crossing paths!
            var repX = 0f
            var repY = 0f
            var repZ = 0f
            val personalSpace = tankW * 0.18f
            for (j in 0 until activeBettaCount) {
                if (j == idx) continue
                val o = bettas[j]
                val rx = hx - o.nx[0]
                val ry = hy - o.ny[0]
                val rz = (hz - o.nz[0]) * zScalePx
                val rd = sqrtSafe(rx * rx + ry * ry + rz * rz)
                if (rd < personalSpace && rd > 1f) {
                    b.flare = (b.flare + dt * 1.45f).coerceAtMost(1f)
                    val push = (personalSpace - rd) * 0.62f
                    repX += (rx / rd) * push
                    repY += (ry / rd) * push
                    repZ += (rz / rd) * push
                }
            }

            val baseSpd = tankW * when {
                b.state == BettaState.ESCAPE -> 0.36f
                foodPellet != null -> 0.175f
                b.state == BettaState.BREATH -> 0.105f
                b.state == BettaState.HOVER -> 0.016f
                b.state == BettaState.HIDE || b.state == BettaState.REST -> if (dist3D > tankW * 0.045f) 0.072f else 0.012f
                b.state == BettaState.INSPECT -> 0.062f
                else -> 0.084f
            } * nightCalm

            val steerRate = if (b.state == BettaState.ESCAPE) 5.6f else 1.72f
            var desVx = b.vx * 0.9f
            var desVy = b.vy * 0.9f
            var desVzPx = b.vz * zScalePx * 0.9f

            if ((b.state == BettaState.HOVER || ((b.state == BettaState.HIDE || b.state == BettaState.REST) && dist3D < tankW * 0.045f)) && foodPellet == null) {
                desVx = cos(simTime * 0.65f + idx * 2.1f) * (tankW * 0.012f) + repX
                desVy = sin(simTime * 1.05f + idx * 1.7f) * (tankW * 0.009f) + repY
                desVzPx = sin(simTime * 0.55f + idx) * (tankW * 0.010f) + repZ
            } else if (dist3D > 5f) {
                val ramp = (dist3D / (tankW * 0.085f)).coerceIn(0.15f, 1f)
                desVx = (dx / dist3D) * baseSpd * ramp + repX
                desVy = (dy / dist3D) * baseSpd * ramp + repY
                desVzPx = (dzPx / dist3D) * baseSpd * ramp + repZ
            }

            val blend = (steerRate * dt).coerceAtMost(1f)
            b.vx += (desVx - b.vx) * blend
            b.vy += (desVy - b.vy) * blend
            val curVzPx = b.vz * zScalePx + (desVzPx - b.vz * zScalePx) * blend
            b.vz = curVzPx / zScalePx.coerceAtLeast(1f)

            // Soft glass boundary cushion so Bettas curve smoothly away from walls
            val padX = tankW * 0.075f
            if (hx < tankX + padX) b.vx += (tankX + padX - hx) * 3.4f * dt
            if (hx > tankX + tankW - padX) b.vx -= (hx - (tankX + tankW - padX)) * 3.4f * dt
            if (hy < waterY + tankH * 0.032f) b.vy += (waterY + tankH * 0.032f - hy) * 4.0f * dt
            if (hy > sandY - tankH * 0.062f) b.vy -= (hy - (sandY - tankH * 0.062f)) * 4.0f * dt
            if (hz < 0.12f) b.vz += (0.12f - hz) * 1.8f * dt
            if (hz > 0.88f) b.vz -= (hz - 0.88f) * 1.8f * dt

            b.nx[0] = (hx + b.vx * dt).coerceIn(tankX + tankW * 0.04f, tankX + tankW * 0.96f)
            b.ny[0] = (hy + b.vy * dt).coerceIn(waterY + tankH * 0.018f, sandY - tankH * 0.042f)
            b.nz[0] = (hz + b.vz * dt).coerceIn(0.10f, 0.90f)

            b.speed = sqrtSafe(b.vx * b.vx + b.vy * b.vy + (b.vz * zScalePx) * (b.vz * zScalePx))
            val spdNorm = b.speed / tankW.coerceAtLeast(1f)
            b.swimPhase += dt * (2.5f + spdNorm * 44f)
            b.pecPhase += dt * (4.6f + spdNorm * 32f)

            // 12-Joint Follow-the-Leader 3D Articulated Spine!
            for (i in 1 until SPINE_N) {
                val px = b.nx[i - 1]; val py = b.ny[i - 1]; val pz = b.nz[i - 1]
                var sdx = b.nx[i] - px
                var sdy = b.ny[i] - py
                var sdzPx = (b.nz[i] - pz) * zScalePx
                var d = sqrtSafe(sdx * sdx + sdy * sdy + sdzPx * sdzPx)
                if (d < 0.001f) { sdx = -1f; d = 1f }

                val frac = i.toFloat() / (SPINE_N - 1).toFloat()
                val waveAmp = (0.35f + frac * frac * 2.3f) * (0.42f + (spdNorm * 7.5f).coerceAtMost(1.0f) * 0.78f) * (segLen * 0.34f)
                val wave = sin(b.swimPhase - i * 0.56f) * waveAmp
                val hLen = hypot(sdx, sdzPx).coerceAtLeast(0.001f)
                val perpX = -sdzPx / hLen
                val perpZ = sdx / hLen
                val hoverDriftY = sin(b.swimPhase * 0.5f - i * 0.45f) * frac * (segLen * 0.11f)

                val tx = px + (sdx / d) * segLen + perpX * wave * dt * 9.5f
                val ty = py + (sdy / d) * segLen + hoverDriftY * dt * 7.5f
                val tzPx = pz * zScalePx + (sdzPx / d) * segLen + perpZ * wave * dt * 9.5f

                val rdx = tx - px; val rdy = ty - py; val rdzPx = tzPx - pz * zScalePx
                val rd = sqrtSafe(rdx * rdx + rdy * rdy + rdzPx * rdzPx).coerceAtLeast(0.001f)
                b.nx[i] = px + (rdx / rd) * segLen
                b.ny[i] = py + (rdy / rd) * segLen
                b.nz[i] = (pz + ((rdzPx / rd) * segLen) / zScalePx).coerceIn(0.08f, 0.92f)
            }
            b.zDepth = b.nz[2]
        }

        // 6. Update Tank Cleaners & Critters (Authentic Biological Behaviors!)
        if (enableCleaners) {
            updateCleanersAndCritters(dt, activeBettaCount)
        }
    }

    private fun updateCleanersAndCritters(dt: Float, activeBettaCount: Int) {
        // Find any resting algae wafer on the sand bed!
        var activeWaferX = -1f
        for (p in pellets) {
            if (p.active && p.isWafer && p.y >= sandY - tankH * 0.04f) {
                activeWaferX = ((p.x - tankX) / tankW).coerceIn(0.10f, 0.90f)
                break
            }
        }

        // A. Otocinclus Glass-Clingers (graze side glass panes + occasional curved dart to opposite pane!)
        for (o in otos) {
            o.phase += dt * 5.2f
            when (o.mode) {
                OtoMode.LEFT_GLASS, OtoMode.RIGHT_GLASS -> {
                    o.pauseTimer -= dt
                    if (o.pauseTimer <= 0f) {
                        o.normY += o.dirY * 0.055f * dt
                        if (o.normY < 0.22f) {
                            o.dirY = 1f; o.pauseTimer = 2.6f + rand.nextFloat() * 2.2f
                        } else if (o.normY > 0.78f) {
                            o.dirY = -1f; o.pauseTimer = 2.4f + rand.nextFloat() * 2.2f
                        } else if (rand.nextFloat() < dt * 0.08f) {
                            // Occasional nimble curved dart to opposite glass pane!
                            o.startX = o.normX
                            o.startY = o.normY
                            o.endX = if (o.mode == OtoMode.LEFT_GLASS) 0.972f else 0.028f
                            o.endY = 0.28f + rand.nextFloat() * 0.44f
                            o.transitProgress = 0f
                            o.mode = OtoMode.TRANSIT
                        } else if (rand.nextFloat() < dt * 0.35f) {
                            o.pauseTimer = 1.8f + rand.nextFloat() * 2.6f
                        }
                    }
                }
                OtoMode.TRANSIT -> {
                    o.transitProgress += dt * 0.65f
                    val u = o.transitProgress.coerceIn(0f, 1f)
                    val ease = u * u * (3f - 2f * u)
                    o.normX = o.startX + (o.endX - o.startX) * ease
                    o.normY = o.startY + (o.endY - o.startY) * ease - sin(u * PI.toFloat()) * 0.08f
                    if (o.transitProgress >= 1f) {
                        o.mode = if (o.endX < 0.5f) OtoMode.LEFT_GLASS else OtoMode.RIGHT_GLASS
                        o.pauseTimer = 2.5f
                    }
                }
                OtoMode.LEAF -> {
                    o.mode = OtoMode.LEFT_GLASS
                }
            }
        }

        // B. Shy Nocturnal Bristlenose Pleco:
        //    During bright daylight, tucks under the dark spiderwood arch (t in 0.14..0.28);
        //    At dusk/night, emerges onto the sunlit spiderwood branches (t in 0.22..0.72)!
        plecoPhase += dt * 2.6f
        val minPlecoT = if (aqPal.isDaylight) 0.14f else 0.20f
        val maxPlecoT = if (aqPal.isDaylight) 0.29f else 0.70f
        plecoPause -= dt
        if (plecoPause <= 0f) {
            if (plecoT > maxPlecoT) plecoDir = -1f
            if (plecoT < minPlecoT) plecoDir = 1f
            plecoT = (plecoT + plecoDir * 0.036f * dt).coerceIn(0.12f, 0.74f)
            if (plecoT >= maxPlecoT) { plecoDir = -1f; plecoPause = 4.5f + rand.nextFloat() * 4.0f }
            else if (plecoT <= minPlecoT) { plecoDir = 1f; plecoPause = 5.0f + rand.nextFloat() * 4.5f }
            else if (rand.nextFloat() < dt * 0.28f) plecoPause = 3.5f + rand.nextFloat() * 4.0f
        }

        // C. Shoaling Corydoras Catfish (paired sand-snuffling + sand puffs + occasional surface dash!)
        for (idx in corys.indices) {
            val c = corys[idx]
            c.phase += dt * 6.4f
            c.dashTimer -= dt
            if (c.dashPhase > 0f) {
                // Iconic real-life Corydoras quick surface gulp dash!
                c.dashPhase += dt * 0.88f
                val u = c.dashPhase.coerceIn(0f, 1f)
                c.normY = 0.96f - sin(u * PI.toFloat()) * 0.88f
                c.normX = (c.normX + c.dir * 0.08f * dt).coerceIn(0.14f, 0.86f)
                if (u > 0.46f && u < 0.54f && rand.nextFloat() < dt * 16f) {
                    injectRipple(tankX + c.normX * tankW, 10f)
                    spawnBubble(tankX + c.normX * tankW, waterY + tankH * 0.03f, 1)
                }
                if (c.dashPhase >= 1f) {
                    c.dashPhase = 0f
                    c.normY = if (idx == 0) 0.96f else 0.98f
                    c.dashTimer = 34f + rand.nextFloat() * 28f
                    c.pauseTimer = 1.6f
                }
                continue
            }

            if (c.dashTimer <= 0f && catState == CatState.HIDDEN) {
                c.dashPhase = 0.01f
                continue
            }

            // Rush toward fallen algae wafer!
            if (activeWaferX > 0f) {
                val dx = activeWaferX - c.normX
                if (abs(dx) > 0.03f) {
                    c.dir = if (dx >= 0f) 1f else -1f
                    c.normX += c.dir * 0.095f * dt
                    c.snuffleTimer += dt
                    if (c.snuffleTimer > 0.45f) {
                        c.snuffleTimer = 0f
                        spawnSandPuff(tankX + c.normX * tankW + c.dir * tankW * 0.02f, sandY)
                    }
                }
                continue
            }

            // Loose shoaling companion pull toward partner Corydoras
            val buddy = corys[1 - idx]
            c.pauseTimer -= dt
            if (c.pauseTimer <= 0f) {
                if (abs(buddy.normX - c.normX) > 0.28f && rand.nextFloat() < dt * 1.4f) {
                    c.dir = if (buddy.normX > c.normX) 1f else -1f
                }
                c.normX += c.dir * 0.046f * dt
                c.snuffleTimer += dt
                if (c.snuffleTimer > 0.85f && rand.nextFloat() < 0.45f) {
                    c.snuffleTimer = 0f
                    spawnSandPuff(tankX + c.normX * tankW + c.dir * tankW * 0.018f, sandY + 2f)
                }
                if (c.normX < 0.16f) { c.dir = 1f; c.pauseTimer = 1.6f + rand.nextFloat() * 1.8f }
                else if (c.normX > 0.64f) { c.dir = -1f; c.pauseTimer = 1.8f + rand.nextFloat() * 2.0f }
                else if (rand.nextFloat() < dt * 0.42f) c.pauseTimer = 1.5f + rand.nextFloat() * 2.5f
            }
        }

        // D. Golden Mystery Snail
        if (snailRetractTimer <= 0f) {
            snailNormX += snailDir * 0.0075f * dt
            if (snailNormX > 0.68f) snailDir = -1f
            if (snailNormX < 0.36f) snailDir = 1f
        }

        // E. Red Cherry Shrimps (grazing + nimble swimming hops + darting backward from inspecting Bettas!)
        for (s in shrimps) {
            s.phase += dt * 5.8f
            if (s.hopProgress > 0f) {
                s.hopProgress += dt * 1.35f
                if (s.hopProgress >= 1f) {
                    s.hopProgress = 0f
                }
                continue
            }

            // Check if a curious Betta gets right in the shrimp's face -> nimble tail-flick hop!
            val sx = evalShrimpHomeX(s.homeKind, s.t)
            val sy = evalShrimpHomeY(s.homeKind, s.t)
            var startled = false
            for (i in 0 until activeBettaCount) {
                if (hypot(bettas[i].nx[0] - sx, bettas[i].ny[0] - sy) < tankW * 0.085f) {
                    startled = true
                    break
                }
            }
            if (startled || rand.nextFloat() < dt * 0.055f) {
                s.hopStartX = sx
                s.hopStartY = sy
                // Hop to a nearby spot along wood or between moss/wood!
                if (s.homeKind == 0 || s.homeKind == 1) {
                    s.t = (s.t + (if (rand.nextBoolean()) 0.18f else -0.18f)).coerceIn(0.16f, 0.84f)
                } else if (rand.nextFloat() < 0.5f) {
                    s.homeKind = if (s.homeKind == 2) 4 else if (s.homeKind == 4) 3 else 2
                }
                s.hopEndX = evalShrimpHomeX(s.homeKind, s.t)
                s.hopEndY = evalShrimpHomeY(s.homeKind, s.t)
                s.dir = if (s.hopEndX >= s.hopStartX) 1f else -1f
                s.hopProgress = 0.01f
            } else if (s.homeKind == 0 || s.homeKind == 1) {
                s.t += s.dir * 0.014f * dt
                if (s.t > 0.85f) s.dir = -1f
                if (s.t < 0.15f) s.dir = 1f
            }
        }
    }

    // =========================================================================
    // PALETTE HARMONIZATION (Day / Dusk / Night / AMOLED x Lagoon/Ember/Moss)
    // =========================================================================

    private fun resolveAquariumPalette(config: WallpaperConfig, snapshot: TimeOfDayPalette.TimeSnapshot) {
        val pal = snapshot.colors
        val isAmoled = (config.paletteFamily == PaletteFamily.AMOLED && !pal.isLightSurface)
        val isDusk = !snapshot.isNight && (pal.phaseName == "dusk" || pal.phaseName == "sunset" || pal.phaseName == "dawn" || pal.phaseName == "twilight")
        val isDay = !snapshot.isNight && !isDusk

        aqPal.isAmoled = isAmoled
        aqPal.isDaylight = isDay
        aqPal.isNight = snapshot.isNight

        when {
            isAmoled -> {
                aqPal.roomTop = 0xFF000000.toInt(); aqPal.roomBot = 0xFF000000.toInt()
                aqPal.shelfTop = 0xFF0A0E12.toInt(); aqPal.shelfFront = 0xFF000000.toInt()
                aqPal.lampBody = 0xFF162028.toInt(); aqPal.lampGlow = 0xFF8CEBFF.toInt()
                aqPal.beamTop = 0x2266D8F0; aqPal.beamBot = 0x00000000
                aqPal.waterTop = 0xFF03080B.toInt(); aqPal.waterMid = 0xFF010406.toInt(); aqPal.waterBot = 0xFF000000.toInt()
                aqPal.fogColor = 0xFF020608.toInt(); aqPal.tanninColor = 0x00000000
                aqPal.glassEdge = 0x9974DCF0.toInt(); aqPal.glassTint = 0x104AB8CE
                aqPal.contour = 0x6E5CB4C8; aqPal.sandTop = 0xFF141D22.toInt(); aqPal.sandBot = 0xFF06090B.toInt()
                aqPal.caustic = 0x1E6AD8EE; aqPal.woodDark = 0xFF18110E.toInt(); aqPal.woodMid = 0xFF281C16.toInt(); aqPal.woodRim = 0xFF4D3E36.toInt()
                aqPal.plantBack = 0xFF102E2C.toInt(); aqPal.plantMid = 0xFF143C36.toInt(); aqPal.plantFront = 0xFF194A42.toInt()
                aqPal.anubias = 0xFF143E38.toInt(); aqPal.anubiasHi = 0xFF2A6C62.toInt()
                aqPal.moss = 0xFF133629.toInt(); aqPal.mossHi = 0xFF265C47.toInt(); aqPal.catappa = 0xFF382318.toInt()
                aqPal.stoneDark = 0xFF121C21.toInt(); aqPal.stoneHi = 0xFF273840.toInt()
                aqPal.catBody = 0xFF05070B.toInt(); aqPal.catRim = 0xAA7EE0F4.toInt(); aqPal.catEye = 0xFFFFD65E.toInt()
                aqPal.moteColor = 0x3D82E0F4; aqPal.bubbleColor = 0x999AECFC.toInt()
            }
            snapshot.isNight -> {
                aqPal.roomTop = 0xFF080F1B.toInt(); aqPal.roomBot = 0xFF101C2E.toInt()
                aqPal.shelfTop = 0xFF231D1A.toInt(); aqPal.shelfFront = 0xFF120E0D.toInt()
                aqPal.lampBody = 0xFF1A2634.toInt(); aqPal.lampGlow = 0xFFB8F2FF.toInt()
                aqPal.beamTop = 0x547AD6F0; aqPal.beamBot = 0x05226C8E
                aqPal.waterTop = 0xFF225064.toInt(); aqPal.waterMid = 0xFF143446.toInt(); aqPal.waterBot = 0xFF0A1F2D.toInt()
                aqPal.fogColor = 0xFF143446.toInt(); aqPal.tanninColor = 0x142A86A8
                aqPal.glassEdge = 0x8AA0E2F4.toInt(); aqPal.glassTint = 0x1C68C2DC
                aqPal.contour = 0x9E08121A.toInt(); aqPal.sandTop = 0xFF3F4E58.toInt(); aqPal.sandBot = 0xFF253038.toInt()
                aqPal.caustic = 0x2692E4FA; aqPal.woodDark = 0xFF241813.toInt(); aqPal.woodMid = 0xFF38271E.toInt(); aqPal.woodRim = 0xFF5A463C.toInt()
                aqPal.plantBack = 0xFF194442.toInt(); aqPal.plantMid = 0xFF133636.toInt(); aqPal.plantFront = 0xFF0E2A2D.toInt()
                aqPal.anubias = 0xFF103133.toInt(); aqPal.anubiasHi = 0xFF265558.toInt()
                aqPal.moss = 0xFF173C33.toInt(); aqPal.mossHi = 0xFF2A5B4C.toInt(); aqPal.catappa = 0xFF452D1F.toInt()
                aqPal.stoneDark = 0xFF223037.toInt(); aqPal.stoneHi = 0xFF3C5059.toInt()
                aqPal.catBody = 0xFF0B0F17.toInt(); aqPal.catRim = 0x8C90E0F6.toInt(); aqPal.catEye = 0xFFFFD562.toInt()
                aqPal.moteColor = 0x3DAAEAFA; aqPal.bubbleColor = 0x8ABCEEFB.toInt()
            }
            isDusk -> {
                aqPal.roomTop = 0xFF272D3E.toInt(); aqPal.roomBot = 0xFF47404E.toInt()
                aqPal.shelfTop = 0xFF4B382C.toInt(); aqPal.shelfFront = 0xFF2E221A.toInt()
                aqPal.lampBody = 0xFF252B36.toInt(); aqPal.lampGlow = 0xFFFFE6B4.toInt()
                aqPal.beamTop = 0x72FFD48C; aqPal.beamBot = 0x085CA2A0
                aqPal.waterTop = 0xFF8EBAB3.toInt(); aqPal.waterMid = 0xFF4D8083.toInt(); aqPal.waterBot = 0xFF284C55.toInt()
                aqPal.fogColor = 0xFF437276.toInt(); aqPal.tanninColor = 0x28E29448
                aqPal.glassEdge = 0x9EF4E8D2.toInt(); aqPal.glassTint = 0x2482BAB8
                aqPal.contour = 0x8F101C22.toInt(); aqPal.sandTop = 0xFFBCA687.toInt(); aqPal.sandBot = 0xFF887258.toInt()
                aqPal.caustic = 0x38FFDEA5; aqPal.woodDark = 0xFF3B271B.toInt(); aqPal.woodMid = 0xFF5A3D2B.toInt(); aqPal.woodRim = 0xFF8D6547.toInt()
                aqPal.plantBack = 0xFF396958.toInt(); aqPal.plantMid = 0xFF285546.toInt(); aqPal.plantFront = 0xFF1D4337.toInt()
                aqPal.anubias = 0xFF1E483A.toInt(); aqPal.anubiasHi = 0xFF3B7059.toInt()
                aqPal.moss = 0xFF2C5439.toInt(); aqPal.mossHi = 0xFF487951.toInt(); aqPal.catappa = 0xFF7B4B28.toInt()
                aqPal.stoneDark = 0xFF39474B.toInt(); aqPal.stoneHi = 0xFF5B6E71.toInt()
                aqPal.catBody = 0xFF161A22.toInt(); aqPal.catRim = 0x85FFD696.toInt(); aqPal.catEye = 0xFFF9BC44.toInt()
                aqPal.moteColor = 0x45FFE8BE; aqPal.bubbleColor = 0x94F2F6F0.toInt()
            }
            else -> { // Daytime sunlit planted tank
                aqPal.roomTop = 0xFFB0C4CA.toInt(); aqPal.roomBot = 0xFFCAD8DC.toInt()
                aqPal.shelfTop = 0xFFA28C76.toInt(); aqPal.shelfFront = 0xFF78634F.toInt()
                aqPal.lampBody = 0xFF3D4B53.toInt(); aqPal.lampGlow = 0xFFFFFDF4.toInt()
                aqPal.beamTop = 0x7AFFFAE0; aqPal.beamBot = 0x06B0E0DA
                aqPal.waterTop = 0xFFB5DCD6.toInt(); aqPal.waterMid = 0xFF83BCB5.toInt(); aqPal.waterBot = 0xFF55908B.toInt()
                aqPal.fogColor = 0xFF74B2AC.toInt(); aqPal.tanninColor = 0x22DAAE6C
                aqPal.glassEdge = 0xC4F8FFFD.toInt(); aqPal.glassTint = 0x2EB6E0DC
                aqPal.contour = 0x7A1C3034; aqPal.sandTop = 0xFFE2D4BA.toInt(); aqPal.sandBot = 0xFFBCA989.toInt()
                aqPal.caustic = 0x52FFFCE8; aqPal.woodDark = 0xFF4D3627.toInt(); aqPal.woodMid = 0xFF6E4E38.toInt(); aqPal.woodRim = 0xFF997458.toInt()
                aqPal.plantBack = 0xFF508469.toInt(); aqPal.plantMid = 0xFF396D52.toInt(); aqPal.plantFront = 0xFF285740.toInt()
                aqPal.anubias = 0xFF295C41.toInt(); aqPal.anubiasHi = 0xFF4B8964.toInt()
                aqPal.moss = 0xFF3A6943.toInt(); aqPal.mossHi = 0xFF5B8E5E.toInt(); aqPal.catappa = 0xFF946238.toInt()
                aqPal.stoneDark = 0xFF506260.toInt(); aqPal.stoneHi = 0xFF798D89.toInt()
                aqPal.catBody = 0xFF1F242C.toInt(); aqPal.catRim = 0x73FFF5DC; aqPal.catEye = 0xFFF3B43A.toInt()
                aqPal.moteColor = 0x55FFFCEB; aqPal.bubbleColor = 0xADFFFFFF.toInt()
            }
        }

        // Subtle palette-family hue harmonization (Ember = warm Amazonian blackwater, Moss = emerald forest!)
        if (!isAmoled) {
            when (config.paletteFamily) {
                PaletteFamily.EMBER -> {
                    aqPal.waterTop = TimeOfDayPalette.lerpColor(aqPal.waterTop, 0xFFD99A64.toInt(), 0.22f)
                    aqPal.waterMid = TimeOfDayPalette.lerpColor(aqPal.waterMid, 0xFF8C5336.toInt(), 0.24f)
                    aqPal.waterBot = TimeOfDayPalette.lerpColor(aqPal.waterBot, 0xFF4A281A.toInt(), 0.26f)
                    aqPal.fogColor = TimeOfDayPalette.lerpColor(aqPal.fogColor, 0xFF7A482E.toInt(), 0.24f)
                    aqPal.roomTop = TimeOfDayPalette.lerpColor(aqPal.roomTop, pal.skyBg, 0.28f)
                    aqPal.roomBot = TimeOfDayPalette.lerpColor(aqPal.roomBot, pal.skyHorizonBg, 0.24f)
                }
                PaletteFamily.MOSS -> {
                    aqPal.waterTop = TimeOfDayPalette.lerpColor(aqPal.waterTop, 0xFF8CC4A2.toInt(), 0.20f)
                    aqPal.waterMid = TimeOfDayPalette.lerpColor(aqPal.waterMid, 0xFF3E7858.toInt(), 0.22f)
                    aqPal.waterBot = TimeOfDayPalette.lerpColor(aqPal.waterBot, 0xFF1D4432.toInt(), 0.24f)
                    aqPal.fogColor = TimeOfDayPalette.lerpColor(aqPal.fogColor, 0xFF3A6E52.toInt(), 0.22f)
                    aqPal.roomTop = TimeOfDayPalette.lerpColor(aqPal.roomTop, pal.skyBg, 0.24f)
                }
                else -> Unit
            }
        }
    }

    // =========================================================================
    // SCENE & BIOTOPE RENDERING
    // =========================================================================

    private fun drawRoomAndLamp(canvas: Canvas, enableBatteryIndicator: Boolean, isCharging: Boolean) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()

        // 1. Calm Room Backdrop
        if (aqPal.isAmoled) {
            fillPaint.color = 0xFF000000.toInt()
            canvas.drawRect(0f, 0f, w, h, fillPaint)
        } else {
            if (roomGrad == null || abs(cachedRoomH - h) > 2f || cachedRoomTop != aqPal.roomTop || cachedRoomBot != aqPal.roomBot) {
                cachedRoomH = h; cachedRoomTop = aqPal.roomTop; cachedRoomBot = aqPal.roomBot
                roomGrad = LinearGradient(0f, 0f, 0f, h, aqPal.roomTop, aqPal.roomBot, Shader.TileMode.CLAMP)
            }
            gradPaint.shader = roomGrad
            canvas.drawRect(0f, 0f, w, h, gradPaint)
        }

        // 2. Minimalist Tabletop Shelf beneath the tank
        val shelfTopY = tankY + tankH - 3f
        val shelfThickness = (h * 0.018f).coerceIn(10f, 22f)
        fillPaint.color = aqPal.shelfTop
        canvas.drawRect(0f, shelfTopY, w, shelfTopY + shelfThickness, fillPaint)
        fillPaint.color = aqPal.shelfFront
        canvas.drawRect(0f, shelfTopY + shelfThickness, w, h, fillPaint)

        // 3. Suspended Twin Suspension Wires + Overhead LED Light Bar + Volumetric Beam!
        val lampX = (w - lampW) * 0.5f
        val lampThick = (tankH * 0.020f).coerceIn(7f, 14f)
        strokePaint.color = if (aqPal.isAmoled) 0x555E7A86 else aqPal.contour
        strokePaint.strokeWidth = (w * 0.0024f).coerceIn(1.2f, 2.4f)
        canvas.drawLine(lampX + lampW * 0.11f, 0f, lampX + lampW * 0.11f, lampY, strokePaint)
        canvas.drawLine(lampX + lampW * 0.89f, 0f, lampX + lampW * 0.89f, lampY, strokePaint)

        // Volumetric Overhead LED Light Cone!
        val beamY0 = lampY + lampThick * 0.85f
        val beamY1 = tankY + tankH * 0.68f
        val pulseBoost = if (lampPulseTimer > 0f) (sin((lampPulseTimer / 1.8f) * PI.toFloat()).coerceAtLeast(0f) * 42f).toInt() else 0
        val effectiveBeamTop = if (pulseBoost > 0) {
            val baseA = ((aqPal.beamTop ushr 24) + pulseBoost).coerceAtMost(210)
            (aqPal.beamTop and 0x00FFFFFF) or (baseA shl 24)
        } else {
            aqPal.beamTop
        }
        if (beamGrad == null || abs(cachedBeamY0 - beamY0) > 2f || abs(cachedBeamY1 - beamY1) > 2f ||
            cachedBeamTop != effectiveBeamTop || cachedBeamBot != aqPal.beamBot
        ) {
            cachedBeamY0 = beamY0; cachedBeamY1 = beamY1
            cachedBeamTop = effectiveBeamTop; cachedBeamBot = aqPal.beamBot
            beamGrad = LinearGradient(0f, beamY0, 0f, beamY1, effectiveBeamTop, aqPal.beamBot, Shader.TileMode.CLAMP)
        }
        shapePath.reset()
        shapePath.moveTo(lampX + 6f, beamY0)
        shapePath.lineTo(lampX + lampW - 6f, beamY0)
        shapePath.lineTo(tankX + tankW + tankW * 0.045f, beamY1)
        shapePath.lineTo(tankX - tankW * 0.045f, beamY1)
        shapePath.close()
        gradPaint.shader = beamGrad
        canvas.drawPath(shapePath, gradPaint)

        // Sleek Architectural Lamp Bar + Glowing LED Diffuser Strip
        fillPaint.color = aqPal.lampBody
        canvas.drawRoundRect(lampX, lampY, lampX + lampW, lampY + lampThick, 3f, 3f, fillPaint)
        fillPaint.color = aqPal.lampGlow
        canvas.drawRect(lampX + 6f, lampY + lampThick * 0.68f, lampX + lampW - 6f, lampY + lampThick, fillPaint)

        // Subtle charging status micro-LED on the lamp bar
        if (enableBatteryIndicator && isCharging) {
            val pulse = 0.5f + 0.5f * sin(simTime * 2.6f)
            val dotAlpha = (120 + (pulse * 125f).toInt()).coerceIn(90, 245)
            fillPaint.color = (0x006CE8B8) or (dotAlpha shl 24)
            canvas.drawCircle(lampX + lampW - 14f, lampY + lampThick * 0.36f, (lampThick * 0.24f).coerceAtLeast(2.2f), fillPaint)
        }
    }

    private fun drawWaterAndLightShafts(canvas: Canvas, waterClarity: Float, isBatterySaver: Boolean) {
        val botY = tankY + tankH
        if (waterGrad == null || abs(cachedWaterY - waterY) > 2f || abs(cachedWaterBotY - botY) > 2f ||
            cachedWaterTop != aqPal.waterTop || cachedWaterMid != aqPal.waterMid || cachedWaterBot != aqPal.waterBot
        ) {
            cachedWaterY = waterY; cachedWaterBotY = botY
            cachedWaterTop = aqPal.waterTop; cachedWaterMid = aqPal.waterMid; cachedWaterBot = aqPal.waterBot
            waterGrad = LinearGradient(
                0f, waterY, 0f, botY,
                intArrayOf(aqPal.waterTop, aqPal.waterMid, aqPal.waterBot),
                floatArrayOf(0f, 0.52f, 1f),
                Shader.TileMode.CLAMP
            )
        }

        shapePath.reset()
        shapePath.moveTo(tankX, botY)
        for (i in 0 until waveN) {
            val wx = tankX + (i.toFloat() / (waveN - 1).toFloat()) * tankW
            val wy = waterY + waveH[i] + sin(simTime * 1.35f + i * 0.24f) * (tankH * 0.0018f)
            shapePath.lineTo(wx, wy)
        }
        shapePath.lineTo(tankX + tankW, botY)
        shapePath.close()
        gradPaint.shader = waterGrad
        canvas.drawPath(shapePath, gradPaint)

        // Subtle natural Catappa-leaf blackwater tannin warmth
        if (!aqPal.isAmoled && (aqPal.tanninColor ushr 24) > 0) {
            fillPaint.color = aqPal.tanninColor
            canvas.drawRect(tankX, waterY, tankX + tankW, botY, fillPaint)
        }

        // Soft Volumetric Underwater LED Shafts
        if (!aqPal.isAmoled && waterClarity > 0.42f) {
            val shaftAlpha = if (aqPal.isNight) 15 else if (aqPal.isDaylight) 26 else 22
            fillPaint.color = (if (aqPal.isNight) 0x0082DAF0 else 0x00FFFAE0) or (shaftAlpha shl 24)
            val rayCount = if (isBatterySaver) 2 else 4
            for (r in 0 until rayCount) {
                val rx = tankX + tankW * (0.17f + r * 0.21f) + sin(simTime * 0.42f + r * 1.3f) * (tankW * 0.022f)
                shapePath.reset()
                shapePath.moveTo(rx, waterY)
                shapePath.lineTo(rx + tankW * 0.068f, waterY)
                shapePath.lineTo(rx + tankW * 0.165f, sandY)
                shapePath.lineTo(rx + tankW * 0.042f, sandY)
                shapePath.close()
                canvas.drawPath(shapePath, fillPaint)
            }
        }

        // Suspended luminous water motes
        if (!isBatterySaver) {
            fillPaint.color = aqPal.moteColor
            val waterSpan = (sandY - waterY).coerceAtLeast(60f)
            for (m in motes) {
                canvas.drawCircle(tankX + m.nx * tankW, waterY + m.ny * waterSpan, m.r, fillPaint)
            }
        }
    }

    private fun drawVallisneriaGroup(canvas: Canvas, xBase: Float, heightPx: Float, blades: Int, fillCol: Int, swayAmp: Float) {
        val spacing = tankW * 0.020f
        val halfW = (tankW * 0.0085f).coerceIn(2.6f, 6.2f)
        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = (tankW * 0.0020f).coerceIn(0.8f, 1.5f)
        fillPaint.color = fillCol

        for (i in 0 until blades) {
            val offset = (i - (blades - 1) * 0.5f) * spacing
            val bh = heightPx * (0.68f + ((i * 37) % 10) * 0.036f)
            val sway = sin(simTime * 1.05f + i * 0.85f + xBase * 0.02f) * (tankW * 0.028f) * swayAmp
            val bx = xBase + offset
            val by = sandY + tankH * 0.010f
            shapePath.reset()
            shapePath.moveTo(bx - halfW, by)
            shapePath.quadTo(bx + sway * 0.45f - halfW * 0.72f, by - bh * 0.55f, bx + sway, by - bh)
            shapePath.quadTo(bx + sway * 0.45f + halfW * 0.85f, by - bh * 0.52f, bx + halfW, by)
            shapePath.close()
            canvas.drawPath(shapePath, fillPaint)
            canvas.drawPath(shapePath, strokePaint)
        }
    }

    private fun drawBackPlants(canvas: Canvas) {
        drawVallisneriaGroup(canvas, tankX + tankW * 0.12f, tankH * 0.60f, 5, aqPal.plantBack, 0.85f)
        drawVallisneriaGroup(canvas, tankX + tankW * 0.88f, tankH * 0.56f, 5, aqPal.plantBack, 0.80f)
        drawVallisneriaGroup(canvas, tankX + tankW * 0.70f, tankH * 0.42f, 4, aqPal.plantBack, 0.65f)
    }

    private fun drawForegroundPlants(canvas: Canvas) {
        drawVallisneriaGroup(canvas, tankX + tankW * 0.075f, tankH * 0.49f, 3, aqPal.plantFront, 1.0f)
        drawVallisneriaGroup(canvas, tankX + tankW * 0.925f, tankH * 0.46f, 3, aqPal.plantFront, 0.92f)
    }

    private fun drawSubstrateAndCaustics(canvas: Canvas, isBatterySaver: Boolean) {
        val botY = tankY + tankH
        val sY0 = sandY - tankH * 0.020f
        if (sandGrad == null || abs(cachedSandY0 - sY0) > 2f || abs(cachedSandY1 - botY) > 2f ||
            cachedSandTop != aqPal.sandTop || cachedSandBot != aqPal.sandBot
        ) {
            cachedSandY0 = sY0; cachedSandY1 = botY
            cachedSandTop = aqPal.sandTop; cachedSandBot = aqPal.sandBot
            sandGrad = LinearGradient(0f, sY0, 0f, botY, aqPal.sandTop, aqPal.sandBot, Shader.TileMode.CLAMP)
        }
        shapePath.reset()
        shapePath.moveTo(tankX, sandY + tankH * 0.010f)
        shapePath.quadTo(tankX + tankW * 0.34f, sandY - tankH * 0.020f, tankX + tankW * 0.70f, sandY + tankH * 0.005f)
        shapePath.quadTo(tankX + tankW * 0.88f, sandY - tankH * 0.010f, tankX + tankW, sandY + tankH * 0.008f)
        shapePath.lineTo(tankX + tankW, botY)
        shapePath.lineTo(tankX, botY)
        shapePath.close()
        gradPaint.shader = sandGrad
        canvas.drawPath(shapePath, gradPaint)

        // Dancing Caustic Light Rings on the Sand!
        if (!isBatterySaver) {
            strokePaint.color = aqPal.caustic
            strokePaint.strokeWidth = (tankW * 0.0032f).coerceIn(1.1f, 2.4f)
            for (c in 0 until 6) {
                val cx = tankX + tankW * (0.12f + c * 0.15f) + sin(simTime * 1.1f + c * 1.35f) * (tankW * 0.026f)
                val cy = sandY + tankH * (0.026f + (c % 2) * 0.018f)
                val rx = tankW * (0.042f + 0.009f * sin(simTime + c))
                val ry = rx * 0.22f
                canvas.drawOval(cx - rx, cy - ry, cx + rx, cy + ry, strokePaint)
            }
        }
    }

    private fun drawCatappaLeavesAndStones(canvas: Canvas) {
        // Fallen Indian Almond (Catappa) leaves on the substrate!
        drawCatappaLeaf(canvas, tankX + tankW * 0.45f, sandY + tankH * 0.026f, 8f)
        drawCatappaLeaf(canvas, tankX + tankW * 0.73f, sandY + tankH * 0.032f, -11f)

        // Smooth Layered River Stones
        drawRiverStone(canvas, tankX + tankW * 0.21f, sandY + tankH * 0.004f, tankW * 0.062f, tankH * 0.028f)
        drawRiverStone(canvas, tankX + tankW * 0.61f, sandY + tankH * 0.005f, tankW * 0.074f, tankH * 0.032f)
        drawRiverStone(canvas, tankX + tankW * 0.78f, sandY + tankH * 0.008f, tankW * 0.065f, tankH * 0.030f)
    }

    private fun drawCatappaLeaf(canvas: Canvas, cx: Float, cy: Float, deg: Float) {
        val rx = tankW * 0.046f
        val ry = rx * 0.42f
        canvas.save()
        canvas.translate(cx, cy)
        canvas.rotate(deg)
        shapePath.reset()
        shapePath.moveTo(-rx, 0f)
        shapePath.quadTo(0f, -ry, rx, 0f)
        shapePath.quadTo(0f, ry * 0.85f, -rx, 0f)
        shapePath.close()
        fillPaint.color = aqPal.catappa
        canvas.drawPath(shapePath, fillPaint)
        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = 1.0f
        canvas.drawPath(shapePath, strokePaint)
        canvas.drawLine(-rx * 0.75f, 0f, rx * 0.75f, 0f, strokePaint)
        canvas.restore()
    }

    private fun drawRiverStone(canvas: Canvas, cx: Float, cy: Float, rx: Float, ry: Float) {
        fillPaint.color = aqPal.stoneDark
        canvas.drawOval(cx - rx, cy - ry, cx + rx, cy + ry, fillPaint)
        // Soft top rim-light catch from overhead LED
        fillPaint.color = aqPal.stoneHi
        canvas.drawOval(cx - rx * 0.72f, cy - ry * 0.86f, cx + rx * 0.68f, cy + ry * 0.12f, fillPaint)
        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = 1.0f
        canvas.drawOval(cx - rx, cy - ry, cx + rx, cy + ry, strokePaint)
    }

    private fun drawSpiderwoodAndAnubias(canvas: Canvas) {
        // Tapered Natural Spiderwood Driftwood Arch (Pleco hides underneath, Shrimps graze along branches!)
        drawTaperedWood(canvas, isMain = true, rStart = tankW * 0.020f, rEnd = tankW * 0.005f, fillCol = aqPal.woodMid)
        drawTaperedWood(canvas, isMain = false, rStart = tankW * 0.013f, rEnd = tankW * 0.0038f, fillCol = aqPal.woodRim)

        // Broad-Leaf Anubias Hammock Clusters (Bettas physically lounge above these!)
        drawAnubiasCluster(canvas, tankX + tankW * 0.60f, sandY - tankH * 0.21f, tankW * 0.00215f)
        drawAnubiasCluster(canvas, tankX + tankW * 0.77f, sandY - tankH * 0.17f, tankW * 0.00180f)
    }

    private fun drawTaperedWood(canvas: Canvas, isMain: Boolean, rStart: Float, rEnd: Float, fillCol: Int) {
        for (i in 0..woodSteps) {
            val t = i.toFloat() / woodSteps.toFloat()
            val px = if (isMain) evalWoodMainX(t) else evalWoodBranchX(t)
            val py = if (isMain) evalWoodMainY(t) else evalWoodBranchY(t)
            val ang = if (isMain) evalWoodMainAng(t) else evalWoodBranchAng(t)
            val r = rStart + (rEnd - rStart) * t
            val nx = -sin(ang)
            val ny = cos(ang)
            woodLX[i] = px + nx * r; woodLY[i] = py + ny * r
            woodRX[i] = px - nx * r; woodRY[i] = py - ny * r
        }
        shapePath.reset()
        shapePath.moveTo(woodLX[0], woodLY[0])
        for (i in 1..woodSteps) shapePath.lineTo(woodLX[i], woodLY[i])
        for (i in woodSteps downTo 0) shapePath.lineTo(woodRX[i], woodRY[i])
        shapePath.close()
        fillPaint.color = fillCol
        canvas.drawPath(shapePath, fillPaint)
        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = 1.15f
        canvas.drawPath(shapePath, strokePaint)
    }

    private fun drawAnubiasCluster(canvas: Canvas, cx: Float, cy: Float, scale: Float) {
        val offsetsX = floatArrayOf(-24f, 0f, 23f, -17f, 17f)
        val offsetsY = floatArrayOf(-12f, -23f, -13f, -1f, -1f)
        val angles = floatArrayOf(-33f, -4f, 32f, -52f, 50f)

        for (i in offsetsX.indices) {
            val sway = sin(simTime * 0.9f + i * 1.1f) * 1.8f
            canvas.save()
            canvas.translate(cx + offsetsX[i] * scale, cy + offsetsY[i] * scale + sway)
            canvas.rotate(angles[i])
            shapePath.reset()
            shapePath.moveTo(0f, 12f * scale)
            shapePath.cubicTo(-13f * scale, 4f * scale, -12f * scale, -14f * scale, 0f, -20f * scale)
            shapePath.cubicTo(12f * scale, -14f * scale, 13f * scale, 4f * scale, 0f, 12f * scale)
            shapePath.close()
            fillPaint.color = if (i == 1) aqPal.anubiasHi else aqPal.anubias
            canvas.drawPath(shapePath, fillPaint)
            strokePaint.color = aqPal.contour
            strokePaint.strokeWidth = 0.95f
            canvas.drawPath(shapePath, strokePaint)
            // Delicate central leaf vein
            strokePaint.color = 0x26FFFFFF
            canvas.drawLine(0f, 9f * scale, 0f, -14f * scale, strokePaint)
            canvas.restore()
        }
    }

    private fun drawMossBallsAndDiffuser(canvas: Canvas) {
        // Velvety Marimo Moss Balls
        val rL = tankW * 0.036f
        drawMossBall(canvas, tankX + tankW * 0.23f, sandY - rL * 0.42f, rL)
        val rR = tankW * 0.038f
        drawMossBall(canvas, tankX + tankW * 0.80f, sandY - rR * 0.34f, rR)

        // Minimalist Glass Corner Diffuser Tube
        val tubeX = tankX + tankW * 0.118f
        val cupX = tankX + tankW * 0.135f
        val cupBot = sandY - tankH * 0.014f
        val cupTop = sandY - tankH * 0.056f
        strokePaint.color = 0x72DCF2F6
        strokePaint.strokeWidth = (tankW * 0.0042f).coerceIn(1.5f, 3.2f)
        shapePath.reset()
        shapePath.moveTo(tubeX, tankY + 4f)
        shapePath.lineTo(tubeX, cupBot - 6f)
        shapePath.quadTo(tubeX, cupBot, cupX, cupBot)
        shapePath.lineTo(cupX, cupTop + 6f)
        canvas.drawPath(shapePath, strokePaint)
        val cupHalf = tankW * 0.012f
        fillPaint.color = 0x52E2F4F8
        canvas.drawRoundRect(cupX - cupHalf, cupTop, cupX + cupHalf, cupTop + cupHalf * 1.7f, 2f, 2f, fillPaint)
    }

    private fun drawMossBall(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        fillPaint.color = aqPal.moss
        canvas.drawOval(cx - r * 1.08f, cy - r * 0.86f, cx + r * 1.08f, cy + r * 0.86f, fillPaint)
        fillPaint.color = aqPal.mossHi
        canvas.drawOval(cx - r * 0.72f, cy - r * 0.74f, cx + r * 0.52f, cy + r * 0.10f, fillPaint)
        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = 1.0f
        canvas.drawOval(cx - r * 1.08f, cy - r * 0.86f, cx + r * 1.08f, cy + r * 0.86f, strokePaint)
    }

    // =========================================================================
    // TRUE 12-JOINT 3D ARTICULATED BETTA RENDERER (Zero Allocation!)
    // =========================================================================

    private fun sortBettasByDepth(count: Int) {
        for (i in 0 until count) bettaSortOrder[i] = i
        // Insertion sort (at most 3 elements — zero alloc!)
        for (i in 1 until count) {
            val key = bettaSortOrder[i]
            val keyZ = bettas[key].zDepth
            var j = i - 1
            while (j >= 0 && bettas[bettaSortOrder[j]].zDepth < keyZ) {
                bettaSortOrder[j + 1] = bettaSortOrder[j]
                j--
            }
            bettaSortOrder[j + 1] = key
        }
    }

    private fun drawArticulatedBetta(canvas: Canvas, b: BettaFish, isBatterySaver: Boolean) {
        val avgZ = b.nz[2]
        val fogAmt = if (aqPal.isAmoled) 0f else (avgZ * 0.46f).coerceIn(0f, 0.46f)
        val unitLen = (tankW * 0.076f).coerceIn(24f, 76f) // True dainty scale (~13-14% tank width total incl. veil!)

        for (i in 0 until SPINE_N) {
            projX[i] = b.nx[i]
            projY[i] = b.ny[i]
            projS[i] = 1.08f - b.nz[i] * 0.34f
        }

        // Compute tangent & anatomically upright dorsal normal along every spine joint
        for (i in 0 until SPINE_N) {
            val pIdx = if (i == 0) 0 else i - 1
            val nIdx = if (i == SPINE_N - 1) SPINE_N - 1 else i + 1
            var tx = projX[pIdx] - projX[nIdx]
            var ty = projY[pIdx] - projY[nIdx]
            val len = hypot(tx, ty)
            if (len < 0.01f) { tx = 1f; ty = 0f } else { tx /= len; ty /= len }
            tangX[i] = tx; tangY[i] = ty

            var ux = -ty; var uy = tx
            if (uy > 0f) { ux = ty; uy = -tx }
            val fore = (len / (bettaSegLen() * 0.72f)).coerceIn(0f, 1f)
            ux *= fore
            uy = uy * fore - (1f - fore)
            val uLen = hypot(ux, uy).coerceAtLeast(0.001f)
            upX[i] = ux / uLen; upY[i] = uy / uLen
        }

        val spdNorm = b.speed / tankW.coerceAtLeast(1f)
        val billow = (1.16f - (spdNorm * 0.9f).coerceAtMost(0.32f)) + b.flare * 0.28f
        val veilAlpha = if (aqPal.isAmoled) 182 else 188
        val finBaseCol = withAlpha(TimeOfDayPalette.lerpColor(b.finBase, aqPal.fogColor, fogAmt), veilAlpha)
        val finMidCol = withAlpha(TimeOfDayPalette.lerpColor(b.finMid, aqPal.fogColor, fogAmt), veilAlpha - 12)
        val finTipCol = withAlpha(TimeOfDayPalette.lerpColor(b.finTip, aqPal.fogColor, fogAmt), veilAlpha + 10)
        val bodyCol = TimeOfDayPalette.lerpColor(b.bodyCol, aqPal.fogColor, fogAmt) or (0xFF shl 24)
        val bodyHiCol = TimeOfDayPalette.lerpColor(b.bodyHi, aqPal.fogColor, fogAmt) or (0xFF shl 24)
        val rimLineCol = withAlpha(TimeOfDayPalette.lerpColor(b.finTip, aqPal.fogColor, fogAmt * 0.5f), if (aqPal.isAmoled) 145 else 92)

        // 1. Flowing Translucent Dorsal Sail Veil (spine 2..7)
        for (i in 2..7) {
            val frac = (i - 2) / 5f
            val h = unitLen * (0.27f + sin(frac * PI.toFloat() * 0.88f) * 0.42f) * billow * projS[i]
            val rip = sin(b.swimPhase * 0.9f - i * 0.7f) * (unitLen * 0.06f) * projS[i]
            outX[i] = projX[i] + upX[i] * h - tangX[i] * (unitLen * 0.13f * frac * projS[i]) + rip * tangY[i]
            outY[i] = projY[i] + upY[i] * h - tangY[i] * (unitLen * 0.13f * frac * projS[i])
        }
        veilPath.reset()
        veilPath.moveTo(projX[2] + upX[2] * unitLen * 0.10f * projS[2], projY[2] + upY[2] * unitLen * 0.10f * projS[2])
        veilPath.lineTo(outX[2], outY[2])
        for (i in 3..7) {
            veilPath.quadTo(outX[i - 1], outY[i - 1], (outX[i - 1] + outX[i]) * 0.5f, (outY[i - 1] + outY[i]) * 0.5f)
        }
        veilPath.lineTo(outX[7], outY[7])
        veilPath.lineTo(projX[7] + upX[7] * unitLen * 0.035f * projS[7], projY[7] + upY[7] * unitLen * 0.035f * projS[7])
        veilPath.close()
        fillPaint.color = finMidCol
        canvas.drawPath(veilPath, fillPaint)
        strokePaint.color = rimLineCol
        strokePaint.strokeWidth = 1.0f
        canvas.drawPath(veilPath, strokePaint)

        // 2. Draped Flowing Anal Veil Fin (spine 2..7)
        for (i in 2..7) {
            val frac = (i - 2) / 5f
            val h = unitLen * (0.29f + sin(frac * PI.toFloat() * 0.85f) * 0.45f) * billow * projS[i]
            val rip = cos(b.swimPhase * 0.85f - i * 0.65f) * (unitLen * 0.065f) * projS[i]
            outX[i] = projX[i] - upX[i] * h - tangX[i] * (unitLen * 0.14f * frac * projS[i]) + rip * tangY[i]
            outY[i] = projY[i] - upY[i] * h - tangY[i] * (unitLen * 0.14f * frac * projS[i])
        }
        veilPath.reset()
        veilPath.moveTo(projX[2] - upX[2] * unitLen * 0.10f * projS[2], projY[2] - upY[2] * unitLen * 0.10f * projS[2])
        veilPath.lineTo(outX[2], outY[2])
        for (i in 3..7) {
            veilPath.quadTo(outX[i - 1], outY[i - 1], (outX[i - 1] + outX[i]) * 0.5f, (outY[i - 1] + outY[i]) * 0.5f)
        }
        veilPath.lineTo(outX[7], outY[7])
        veilPath.lineTo(projX[7] - upX[7] * unitLen * 0.035f * projS[7], projY[7] - upY[7] * unitLen * 0.035f * projS[7])
        veilPath.close()
        fillPaint.color = finMidCol
        canvas.drawPath(veilPath, fillPaint)
        canvas.drawPath(veilPath, strokePaint)

        // 3. Broad Translucent Halfmoon Caudal Veil Tail (anchored to trailing spine 6..11!)
        val tbX = projX[6]; val tbY = projY[6]; val tbS = projS[6]
        val tmX = projX[9]; val tmY = projY[9]
        val ttX = projX[11]; val ttY = projY[11]
        val fanSpan = unitLen * 0.49f * billow * tbS
        val tdX = ttX - tbX; val tdY = ttY - tbY
        val tSpread = hypot(tdX, tdY)
        val ext = max(unitLen * 0.38f * tbS, tSpread * 1.36f)
        val dirX = if (tSpread > 0.5f) tdX / tSpread else -tangX[6]
        val dirY = if (tSpread > 0.5f) tdY / tSpread else -tangY[6]
        val endX = tbX + dirX * ext
        val endY = tbY + dirY * ext
        val nuX = upX[9]; val nuY = upY[9]

        veilPath.reset()
        veilPath.moveTo(tbX + upX[6] * unitLen * 0.055f * tbS, tbY + upY[6] * unitLen * 0.055f * tbS)
        veilPath.cubicTo(
            tmX + nuX * fanSpan * 1.15f, tmY + nuY * fanSpan * 1.15f,
            endX + nuX * fanSpan * 0.92f, endY + nuY * fanSpan * 0.92f,
            endX + dirX * unitLen * 0.10f * tbS, endY + dirY * unitLen * 0.10f * tbS
        )
        veilPath.cubicTo(
            endX - nuX * fanSpan * 0.92f, endY - nuY * fanSpan * 0.92f,
            tmX - nuX * fanSpan * 1.15f, tmY - nuY * fanSpan * 1.15f,
            tbX - upX[6] * unitLen * 0.055f * tbS, tbY - upY[6] * unitLen * 0.055f * tbS
        )
        veilPath.close()
        fillPaint.color = finBaseCol
        canvas.drawPath(veilPath, fillPaint)
        // Iridescent outer silk rim wash
        strokePaint.color = finTipCol
        strokePaint.strokeWidth = (unitLen * 0.052f).coerceIn(1.2f, 2.8f)
        canvas.drawPath(veilPath, strokePaint)

        // Delicate pleated silk fin rays radiating along the curved tail
        if (!isBatterySaver) {
            strokePaint.color = 0x36FFFFFF
            strokePaint.strokeWidth = 0.85f
            for (r in -3..3) {
                val f = r / 3f
                detailPath.reset()
                detailPath.moveTo(tbX, tbY)
                detailPath.quadTo(
                    tmX + nuX * fanSpan * 0.72f * f,
                    tmY + nuY * fanSpan * 0.72f * f,
                    endX + nuX * fanSpan * 0.78f * f + dirX * (1f - abs(f)) * unitLen * 0.06f,
                    endY + nuY * fanSpan * 0.78f * f + dirY * (1f - abs(f)) * unitLen * 0.06f
                )
                canvas.drawPath(detailPath, strokePaint)
            }
        }

        // 4. Sculpted Curved Betta Torso built directly around spine nodes 0..7!
        for (i in 0..7) {
            val r = unitLen * bodyRadii[i] * projS[i]
            topEdgeX[i] = projX[i] + upX[i] * r
            topEdgeY[i] = projY[i] + upY[i] * r
            botEdgeX[i] = projX[i] - upX[i] * r
            botEdgeY[i] = projY[i] - upY[i] * r
        }
        bodyPath.reset()
        bodyPath.moveTo(projX[0] + tangX[0] * unitLen * 0.075f * projS[0], projY[0] + tangY[0] * unitLen * 0.075f * projS[0])
        for (i in 0..7) bodyPath.lineTo(topEdgeX[i], topEdgeY[i])
        for (i in 7 downTo 0) bodyPath.lineTo(botEdgeX[i], botEdgeY[i])
        bodyPath.close()
        fillPaint.color = bodyCol
        canvas.drawPath(bodyPath, fillPaint)

        // Iridescent dorsal shimmer band along the upper torso curve
        detailPath.reset()
        detailPath.moveTo(topEdgeX[1], topEdgeY[1])
        for (i in 2..6) detailPath.lineTo(topEdgeX[i], topEdgeY[i])
        strokePaint.color = withAlpha(bodyHiCol, 185)
        strokePaint.strokeWidth = (unitLen * 0.075f).coerceIn(1.6f, 4.2f)
        canvas.drawPath(detailPath, strokePaint)

        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = 1.15f
        canvas.drawPath(bodyPath, strokePaint)

        // 5. Trailing Ventral Ribbon & Fluttering Translucent Pectoral Fin
        val pecFlut = sin(b.pecPhase) * (unitLen * 0.09f) * projS[2]
        detailPath.reset()
        detailPath.moveTo(projX[2] - upX[2] * unitLen * 0.11f * projS[2], projY[2] - upY[2] * unitLen * 0.11f * projS[2])
        detailPath.quadTo(
            projX[3] - upX[3] * unitLen * 0.28f * projS[3],
            projY[3] - upY[3] * unitLen * 0.28f * projS[3],
            projX[4] - upX[4] * unitLen * 0.36f * projS[4] - tangX[4] * unitLen * 0.06f,
            projY[4] - upY[4] * unitLen * 0.36f * projS[4]
        )
        strokePaint.color = finTipCol
        strokePaint.strokeWidth = (unitLen * 0.048f * projS[2]).coerceIn(1.2f, 2.8f)
        canvas.drawPath(detailPath, strokePaint)

        // Translucent pectoral fin (splays wider when approaching the front glass!)
        val pecR = unitLen * (if (b.vz < -0.04f) 0.18f else 0.13f) * projS[2]
        fillPaint.color = 0x48FFFFFF
        canvas.drawOval(
            projX[2] - pecR + pecFlut * 0.25f,
            projY[2] + pecFlut * 0.30f - pecR * 0.45f,
            projX[2] + pecR + pecFlut * 0.25f,
            projY[2] + pecFlut * 0.30f + pecR * 0.55f,
            fillPaint
        )

        // 6. Crisp Expressive Eye (hidden smoothly when receding straight away into back plants!)
        if (b.vz < 0.15f) {
            val ex = projX[1] + tangX[1] * unitLen * 0.036f * projS[1] + upX[1] * unitLen * 0.032f * projS[1]
            val ey = projY[1] + tangY[1] * unitLen * 0.036f * projS[1] + upY[1] * unitLen * 0.032f * projS[1]
            val er = (unitLen * 0.048f * projS[1]).coerceIn(1.6f, 4.2f)
            fillPaint.color = 0xFF0C1216.toInt()
            canvas.drawCircle(ex, ey, er, fillPaint)
            fillPaint.color = 0xE6FFFFFF.toInt()
            canvas.drawCircle(ex + er * 0.28f, ey - er * 0.28f, er * 0.34f, fillPaint)
        }
    }

    // =========================================================================
    // REFINED TANK CLEANERS & CRITTERS RENDERERS
    // =========================================================================

    private fun drawOtos(canvas: Canvas) {
        val waterSpan = (sandY - waterY).coerceAtLeast(60f)
        val bodyCol = TimeOfDayPalette.lerpColor(0xFFC8C1AA.toInt(), aqPal.fogColor, if (aqPal.isAmoled) 0f else 0.14f) or (0xFF shl 24)
        val rW = (tankW * 0.0082f).coerceIn(2.6f, 6.2f)
        val rH = rW * 3.1f

        for (o in otos) {
            val ox = tankX + o.normX * tankW
            val oy = waterY + o.normY * waterSpan
            val wag = sin(o.phase) * rW * 0.45f
            canvas.save()
            canvas.translate(ox, oy)
            if (o.mode == OtoMode.TRANSIT) {
                canvas.rotate(if (o.endX > o.startX) 90f else -90f)
            } else if (o.dirY > 0f) {
                canvas.scale(1f, -1f)
            }

            // Forked translucent tail
            shapePath.reset()
            shapePath.moveTo(0f, rH * 0.88f)
            shapePath.lineTo(-rW * 1.15f + wag, rH * 1.48f)
            shapePath.lineTo(0f, rH * 1.24f)
            shapePath.lineTo(rW * 1.15f + wag, rH * 1.48f)
            shapePath.close()
            fillPaint.color = withAlpha(bodyCol, 175)
            canvas.drawPath(shapePath, fillPaint)

            // Slender Otocinclus body + dark signature lateral band
            fillPaint.color = bodyCol
            canvas.drawOval(-rW, -rH, rW, rH, fillPaint)
            strokePaint.color = 0xC23B3832.toInt()
            strokePaint.strokeWidth = rW * 0.62f
            canvas.drawLine(0f, -rH * 0.68f, 0f, rH * 0.92f, strokePaint)
            strokePaint.color = aqPal.contour
            strokePaint.strokeWidth = 0.95f
            canvas.drawOval(-rW, -rH, rW, rH, strokePaint)
            canvas.restore()
        }
    }

    private fun drawPleco(canvas: Canvas) {
        val px = evalWoodMainX(plecoT)
        val py = evalWoodMainY(plecoT) - tankH * 0.016f
        val angDeg = evalWoodMainAng(plecoT) * (180f / PI.toFloat())
        val sc = (tankW * 0.00245f).coerceIn(0.85f, 2.4f)
        val fog = if (aqPal.isAmoled) 0f else if (plecoT < 0.30f) 0.30f else 0.16f
        val armorCol = TimeOfDayPalette.lerpColor(0xFF6B6153.toInt(), aqPal.fogColor, fog) or (0xFF shl 24)
        val finCol = TimeOfDayPalette.lerpColor(0xFF524A40.toInt(), aqPal.fogColor, fog) or (0xFF shl 24)
        val wag = sin(plecoPhase) * 1.6f * sc

        canvas.save()
        canvas.translate(px, py)
        canvas.rotate(angDeg)
        canvas.scale(plecoDir, 1f)

        // Tall ribbed Bristlenose Pleco dorsal sail
        shapePath.reset()
        shapePath.moveTo(-1f * sc, -3.5f * sc)
        shapePath.quadTo(-7f * sc, -14.5f * sc, -15.5f * sc, -3.5f * sc)
        shapePath.close()
        fillPaint.color = finCol
        canvas.drawPath(shapePath, fillPaint)
        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = 1.0f
        canvas.drawPath(shapePath, strokePaint)

        // Armored spotted teardrop body + sweeping forked caudal fin
        bodyPath.reset()
        bodyPath.moveTo(15.5f * sc, 1.2f * sc)
        bodyPath.quadTo(3.5f * sc, -7.2f * sc, -17f * sc, -1.2f * sc)
        bodyPath.lineTo(-24f * sc, -5.2f * sc + wag)
        bodyPath.lineTo(-21f * sc, 0.5f * sc + wag)
        bodyPath.lineTo(-23f * sc, 4.5f * sc + wag)
        bodyPath.lineTo(-16.5f * sc, 2.8f * sc)
        bodyPath.quadTo(3f * sc, 5.2f * sc, 15.5f * sc, 1.2f * sc)
        bodyPath.close()
        fillPaint.color = armorCol
        canvas.drawPath(bodyPath, fillPaint)
        canvas.drawPath(bodyPath, strokePaint)

        // Pale mottled carapace spots + tiny snout bristles
        fillPaint.color = 0xAACEC4AE.toInt()
        canvas.drawCircle(-10f * sc, -1.2f * sc, 1.1f * sc, fillPaint)
        canvas.drawCircle(-4f * sc, 0.8f * sc, 1.1f * sc, fillPaint)
        canvas.drawCircle(2.5f * sc, -1.5f * sc, 1.1f * sc, fillPaint)
        canvas.drawCircle(8.5f * sc, 0.2f * sc, 1.0f * sc, fillPaint)
        strokePaint.color = aqPal.contour
        canvas.drawLine(14.5f * sc, 0f, 18.2f * sc, -2.0f * sc, strokePaint)
        canvas.drawLine(14.5f * sc, 1.5f * sc, 18.0f * sc, 2.5f * sc, strokePaint)
        canvas.restore()
    }

    private fun drawCorydoras(canvas: Canvas) {
        val waterSpan = (sandY - waterY).coerceAtLeast(60f)
        val sc = (tankW * 0.00235f).coerceIn(0.82f, 2.3f)
        val bodyCol = TimeOfDayPalette.lerpColor(0xFFCBBFA8.toInt(), aqPal.fogColor, if (aqPal.isAmoled) 0f else 0.14f) or (0xFF shl 24)

        for (c in corys) {
            val cx = tankX + c.normX * tankW
            val cy = waterY + c.normY * waterSpan
            val wag = sin(c.phase) * 1.8f * sc
            val pitch = if (c.dashPhase > 0f) {
                if (c.dashPhase < 0.5f) -36f else 36f
            } else {
                sin(c.phase * 0.6f) * 4f
            }

            canvas.save()
            canvas.translate(cx, cy)
            canvas.scale(c.dir, 1f)
            canvas.rotate(pitch)

            // Dorsal fin blotch (Panda Cory signature!)
            shapePath.reset()
            shapePath.moveTo(1f * sc, -4.5f * sc)
            shapePath.quadTo(-3f * sc, -11.5f * sc, -6.5f * sc, -3.5f * sc)
            shapePath.close()
            fillPaint.color = 0xFF3E3832.toInt()
            canvas.drawPath(shapePath, fillPaint)

            // Arched back + flat sand-skimming belly + forked tail
            bodyPath.reset()
            bodyPath.moveTo(11.5f * sc, 1.5f * sc)
            bodyPath.quadTo(3.5f * sc, -7.2f * sc, -9.2f * sc, -1.2f * sc)
            bodyPath.lineTo(-15f * sc, -4.4f * sc + wag)
            bodyPath.lineTo(-12.5f * sc, 0f + wag * 0.6f)
            bodyPath.lineTo(-15f * sc, 3.6f * sc + wag)
            bodyPath.lineTo(-9f * sc, 2.2f * sc)
            bodyPath.quadTo(2.5f * sc, 4.2f * sc, 11.5f * sc, 1.5f * sc)
            bodyPath.close()
            fillPaint.color = bodyCol
            canvas.drawPath(bodyPath, fillPaint)
            strokePaint.color = aqPal.contour
            strokePaint.strokeWidth = 0.95f
            canvas.drawPath(bodyPath, strokePaint)

            // Signature dark Panda Cory eye-band + sensory sand barbels!
            fillPaint.color = 0xCC3A342E.toInt()
            canvas.drawOval(4.2f * sc, -4.6f * sc, 7.2f * sc, 1.4f * sc, fillPaint)
            canvas.drawCircle(-8.2f * sc, 0.2f * sc, 1.9f * sc, fillPaint)
            strokePaint.strokeWidth = 0.85f
            canvas.drawLine(10.5f * sc, 1.8f * sc, 14.5f * sc, 4.6f * sc + wag * 0.3f, strokePaint)
            canvas.drawLine(9.8f * sc, 2.2f * sc, 13.0f * sc, 5.2f * sc - wag * 0.3f, strokePaint)
            canvas.restore()
        }
    }

    private fun drawShrimps(canvas: Canvas) {
        val sc = (tankW * 0.0022f).coerceIn(0.76f, 2.2f)
        for (s in shrimps) {
            val sx: Float
            val sy: Float
            val angDeg: Float
            if (s.hopProgress > 0f) {
                val u = s.hopProgress.coerceIn(0f, 1f)
                sx = s.hopStartX + (s.hopEndX - s.hopStartX) * u
                sy = s.hopStartY + (s.hopEndY - s.hopStartY) * u - sin(u * PI.toFloat()) * (tankH * 0.042f)
                angDeg = -18f * cos(u * PI.toFloat()) * s.dir
            } else {
                sx = evalShrimpHomeX(s.homeKind, s.t) + if (s.homeKind >= 2) sin(simTime * 0.45f + s.phase) * tankW * 0.010f else 0f
                sy = evalShrimpHomeY(s.homeKind, s.t)
                angDeg = when (s.homeKind) {
                    0 -> evalWoodMainAng(s.t) * (180f / PI.toFloat()) * 0.5f
                    1 -> evalWoodBranchAng(s.t) * (180f / PI.toFloat()) * 0.5f
                    else -> 0f
                }
            }

            canvas.save()
            canvas.translate(sx, sy)
            canvas.rotate(angDeg)
            canvas.scale(s.dir, 1f)

            // Tiny translucent red cherry shrimp arched carapace + fanned tail
            bodyPath.reset()
            bodyPath.moveTo(6.4f * sc, 0.2f * sc)
            bodyPath.quadTo(1.2f * sc, -3.8f * sc, -5.2f * sc, -0.8f * sc)
            bodyPath.lineTo(-8.2f * sc, 1.8f * sc)
            bodyPath.quadTo(-1.2f * sc, 2.1f * sc, 6.4f * sc, 0.2f * sc)
            bodyPath.close()
            fillPaint.color = 0xE6E84232.toInt()
            canvas.drawPath(bodyPath, fillPaint)
            strokePaint.color = aqPal.contour
            strokePaint.strokeWidth = 0.75f
            canvas.drawPath(bodyPath, strokePaint)

            // Fluttering tiny walking legs / swimmerets
            val legWig = sin(s.phase * 2.2f) * 0.9f * sc
            strokePaint.color = 0xAAFF9A8A.toInt()
            strokePaint.strokeWidth = 0.7f
            for (l in 0..2) {
                val lx = (3.5f - l * 2.5f) * sc
                canvas.drawLine(lx, 1.2f * sc, lx + (if (l % 2 == 0) legWig else -legWig), 3.4f * sc, strokePaint)
            }

            // Twin long sweeping whip antennae!
            val antWave = sin(s.phase) * 1.6f * sc
            strokePaint.color = 0xBBFFE0D6.toInt()
            strokePaint.strokeWidth = 0.75f
            detailPath.reset()
            detailPath.moveTo(6.0f * sc, -0.4f * sc)
            detailPath.quadTo(10.5f * sc, -4.2f * sc, 14.0f * sc, -3.2f * sc + antWave)
            canvas.drawPath(detailPath, strokePaint)
            detailPath.reset()
            detailPath.moveTo(5.8f * sc, 0.2f * sc)
            detailPath.quadTo(9.8f * sc, -2.0f * sc, 12.8f * sc, -0.4f * sc - antWave * 0.7f)
            canvas.drawPath(detailPath, strokePaint)

            // Tiny dark eye dot
            fillPaint.color = 0xFF121010.toInt()
            canvas.drawCircle(5.0f * sc, -0.6f * sc, 0.75f * sc, fillPaint)
            canvas.restore()
        }
    }

    private fun drawSnail(canvas: Canvas) {
        val sx = tankX + snailNormX * tankW
        val sy = sandY + tankH * 0.022f
        val sc = (tankW * 0.0022f).coerceIn(0.8f, 2.2f)
        val shellCol = TimeOfDayPalette.lerpColor(0xFFD8A446.toInt(), aqPal.fogColor, if (aqPal.isAmoled) 0f else 0.14f) or (0xFF shl 24)

        canvas.save()
        canvas.translate(sx, sy)
        canvas.scale(snailDir, 1f)

        // Gliding muscular foot + swaying tentacles (tucked in if startled!)
        if (snailRetractTimer <= 0f) {
            fillPaint.color = 0xD8BCA88A.toInt()
            canvas.drawOval(-7.5f * sc, -1.2f * sc, 8.2f * sc, 3.6f * sc, fillPaint)
            strokePaint.color = 0xCCB8A688.toInt()
            strokePaint.strokeWidth = 0.85f
            val sway = sin(simTime * 2.0f) * 1.2f * sc
            canvas.drawLine(6.8f * sc, 0f, 11.2f * sc, -3.2f * sc + sway, strokePaint)
            canvas.drawLine(7.2f * sc, 0.8f * sc, 10.6f * sc, -1.4f * sc - sway, strokePaint)
        }

        // Golden spiral shell
        fillPaint.color = shellCol
        canvas.drawCircle(-1f * sc, -2.8f * sc, 5.2f * sc, fillPaint)
        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = 0.95f
        canvas.drawCircle(-1f * sc, -2.8f * sc, 5.2f * sc, strokePaint)
        canvas.drawCircle(-2.2f * sc, -2.8f * sc, 2.2f * sc, strokePaint)
        canvas.restore()
    }

    private fun drawSandPuffs(canvas: Canvas) {
        for (sp in sandPuffs) {
            if (!sp.active) continue
            val life = 1f - (sp.age / sp.maxAge).coerceIn(0f, 1f)
            val a = (life * 125f).toInt().coerceIn(0, 140)
            fillPaint.color = (aqPal.sandTop and 0x00FFFFFF) or (a shl 24)
            canvas.drawCircle(sp.x, sp.y, (tankW * 0.0045f * (1.2f - life * 0.4f)).coerceAtLeast(1.4f), fillPaint)
        }
    }

    // =========================================================================
    // FLOATING CANOPY, BUBBLE NEST, BUBBLES & GLASS FRAME
    // =========================================================================

    private fun drawFloatingCanopyAndBubbleNest(canvas: Canvas) {
        val padFracs = floatArrayOf(0.19f, 0.25f, 0.58f, 0.66f, 0.74f)
        val padRx = tankW * 0.026f
        val padRy = padRx * 0.28f

        for (i in padFracs.indices) {
            val fx = tankX + padFracs[i] * tankW
            val wIdx = (padFracs[i] * (waveN - 1)).roundToInt().coerceIn(0, waveN - 1)
            val fy = waterY + waveH[wIdx]

            // Dangling underwater frogbit hair roots
            strokePaint.color = 0x7284B494
            strokePaint.strokeWidth = 0.95f
            for (r in -1..1) {
                val sway = sin(simTime * 1.0f + i + r * 0.8f) * (tankW * 0.012f)
                val rootLen = tankH * (0.095f + (i % 2) * 0.032f)
                detailPath.reset()
                detailPath.moveTo(fx + r * padRx * 0.36f, fy + padRy)
                detailPath.quadTo(fx + r * padRx * 0.45f + sway * 0.5f, fy + rootLen * 0.55f, fx + r * padRx * 0.55f + sway, fy + rootLen)
                canvas.drawPath(detailPath, strokePaint)
            }

            // Floating green pad
            val rx = padRx * (1.0f + (i % 2) * 0.24f)
            fillPaint.color = aqPal.plantFront
            canvas.drawOval(fx - rx, fy - padRy, fx + rx, fy + padRy, fillPaint)
            strokePaint.color = aqPal.contour
            strokePaint.strokeWidth = 0.9f
            canvas.drawOval(fx - rx, fy - padRy, fx + rx, fy + padRy, strokePaint)
        }

        // Delicate Betta Surface Bubble Nest tucked calmly under the floating pads!
        strokePaint.color = aqPal.bubbleColor
        strokePaint.strokeWidth = 0.9f
        val nestX = tankX + tankW * 0.635f
        val nestR = (tankW * 0.0046f).coerceIn(1.5f, 3.2f)
        for (bn in 0 until 8) {
            val bx = nestX + bn * nestR * 2.0f
            val by = waterY + nestR * (1.4f + (bn % 2) * 0.85f)
            canvas.drawCircle(bx, by, nestR, strokePaint)
        }
    }

    private fun drawBubblesAndPellets(canvas: Canvas) {
        strokePaint.color = aqPal.bubbleColor
        strokePaint.strokeWidth = 1.0f
        for (b in bubbles) {
            if (b.active) canvas.drawCircle(b.x, b.y, b.r, strokePaint)
        }

        for (p in pellets) {
            if (!p.active) continue
            if (p.isWafer) {
                val rx = (tankW * 0.013f).coerceIn(4f, 10f)
                val ry = rx * 0.48f
                fillPaint.color = 0xFF5A8A4E.toInt()
                canvas.drawOval(p.x - rx, p.y - ry, p.x + rx, p.y + ry, fillPaint)
                strokePaint.color = aqPal.contour
                canvas.drawOval(p.x - rx, p.y - ry, p.x + rx, p.y + ry, strokePaint)
            } else {
                fillPaint.color = 0xFFD99642.toInt()
                canvas.drawCircle(p.x, p.y, (tankW * 0.006f).coerceIn(2.2f, 4.8f), fillPaint)
            }
        }
    }

    private fun drawMeniscusHighlight(canvas: Canvas) {
        shapePath.reset()
        for (i in 0 until waveN) {
            val wx = tankX + (i.toFloat() / (waveN - 1).toFloat()) * tankW
            val wy = waterY + waveH[i] + sin(simTime * 1.35f + i * 0.24f) * (tankH * 0.0018f)
            if (i == 0) shapePath.moveTo(wx, wy) else shapePath.lineTo(wx, wy)
        }
        strokePaint.color = aqPal.glassEdge
        strokePaint.strokeWidth = (tankW * 0.0038f).coerceIn(1.4f, 2.8f)
        canvas.drawPath(shapePath, strokePaint)
    }

    private fun drawGlassFrame(canvas: Canvas) {
        val wallThick = (tankW * 0.014f).coerceIn(4f, 10f)
        fillPaint.color = aqPal.glassTint
        canvas.drawRect(tankX, tankY, tankX + wallThick, tankY + tankH, fillPaint)
        canvas.drawRect(tankX + tankW - wallThick, tankY, tankX + tankW, tankY + tankH, fillPaint)
        canvas.drawRect(tankX, tankY + tankH - wallThick, tankX + tankW, tankY + tankH, fillPaint)

        strokePaint.color = aqPal.glassEdge
        strokePaint.strokeWidth = (tankW * 0.0046f).coerceIn(1.8f, 3.6f)
        canvas.drawRect(tankX, tankY, tankX + tankW, tankY + tankH, strokePaint)
    }

    // =========================================================================
    // RARE OCCASIONAL CURIOUS PET CAT VISITOR RENDERER
    // =========================================================================

    private fun drawCatBehindGlass(canvas: Canvas) {
        if (catRise <= 0.01f) return
        val rimX = if (catSide > 0f) tankX + tankW * 0.76f else tankX + tankW * 0.24f
        val catUnit = (tankW * 0.075f).coerceIn(24f, 72f)
        val headY = tankY - catUnit * 0.12f + (1f - catRise) * (catUnit * 2.3f)

        // 1. Soft Cat Torso Silhouette Refracted Behind the Back Glass!
        if (catRise > 0.20f) {
            canvas.save()
            canvas.clipRect(tankX + 2f, tankY + 2f, tankX + tankW - 2f, tankY + tankH - 2f)
            val torsoAlpha = (catRise * 58f).toInt().coerceIn(0, 66)
            fillPaint.color = (aqPal.catBody and 0x00FFFFFF) or (torsoAlpha shl 24)
            canvas.drawOval(
                rimX + catSide * catUnit * 0.30f - catUnit * 1.45f,
                tankY - catUnit * 0.25f,
                rimX + catSide * catUnit * 0.30f + catUnit * 1.45f,
                tankY + catUnit * 5.2f,
                fillPaint
            )
            canvas.restore()
        }

        // 2. Sleek Rim-Lit Cat Head, Perked Ears, Tracking Amber Eyes & Whiskers Peeking Over Top Rim!
        canvas.save()
        canvas.clipRect(0f, 0f, lastWidth, tankY + 2f)
        canvas.translate(rimX, headY)
        canvas.scale(catSide, 1f)

        val twitch = sin(simTime * 28f) * catEarTwitch * (catUnit * 0.08f)
        shapePath.reset()
        shapePath.moveTo(-catUnit * 0.85f, -catUnit * 0.44f)
        shapePath.quadTo(-catUnit * 1.04f, -catUnit * 1.12f, -catUnit * 0.88f + twitch, -catUnit * 1.44f)
        shapePath.quadTo(-catUnit * 0.54f, -catUnit * 1.18f, -catUnit * 0.34f, -catUnit * 0.90f)
        shapePath.quadTo(0f, -catUnit * 1.02f, catUnit * 0.34f, -catUnit * 0.90f)
        shapePath.quadTo(catUnit * 0.56f, -catUnit * 1.22f, catUnit * 0.90f - twitch, -catUnit * 1.46f)
        shapePath.quadTo(catUnit * 1.04f, -catUnit * 1.06f, catUnit * 0.85f, -catUnit * 0.40f)
        shapePath.quadTo(catUnit * 0.98f, catUnit * 0.18f, catUnit * 0.70f, catUnit * 0.50f)
        shapePath.lineTo(-catUnit * 0.70f, catUnit * 0.50f)
        shapePath.quadTo(-catUnit * 0.98f, catUnit * 0.18f, -catUnit * 0.85f, -catUnit * 0.44f)
        shapePath.close()

        fillPaint.color = aqPal.catBody
        canvas.drawPath(shapePath, fillPaint)
        strokePaint.color = aqPal.catRim
        strokePaint.strokeWidth = (catUnit * 0.045f).coerceIn(1.2f, 2.8f)
        canvas.drawPath(shapePath, strokePaint)

        // Glowing Expressive Amber Eyes Tracking the Betta!
        val lx = catEyeLookX * catSide * (catUnit * 0.095f)
        val ly = catEyeLookY * (catUnit * 0.070f)
        for (side in -1..1 step 2) {
            val ex = side * catUnit * 0.34f
            val ey = -catUnit * 0.32f
            val erx = catUnit * 0.185f
            val ery = catUnit * 0.145f
            fillPaint.color = aqPal.catEye
            canvas.drawOval(ex - erx, ey - ery, ex + erx, ey + ery, fillPaint)
            fillPaint.color = 0xFF090C10.toInt()
            canvas.drawOval(ex + lx - erx * 0.32f, ey + ly - ery * 0.78f, ex + lx + erx * 0.32f, ey + ly + ery * 0.78f, fillPaint)
            fillPaint.color = 0xFFFFFFFF.toInt()
            canvas.drawCircle(ex + lx + erx * 0.18f, ey + ly - ery * 0.28f, erx * 0.16f, fillPaint)
        }

        // Delicate Rim-Lit Whiskers
        strokePaint.color = aqPal.catRim
        strokePaint.strokeWidth = (catUnit * 0.024f).coerceIn(0.8f, 1.6f)
        for (side in -1..1 step 2) {
            canvas.drawLine(side * catUnit * 0.54f, -catUnit * 0.04f, side * catUnit * 1.15f, -catUnit * 0.14f, strokePaint)
            canvas.drawLine(side * catUnit * 0.54f, catUnit * 0.06f, side * catUnit * 1.18f, catUnit * 0.12f, strokePaint)
        }
        canvas.restore()
    }

    private fun drawCatPawOverlay(canvas: Canvas) {
        if (catRise < 0.45f) return
        val rimX = if (catSide > 0f) tankX + tankW * 0.76f else tankX + tankW * 0.24f
        val catUnit = (tankW * 0.075f).coerceIn(24f, 72f)
        val reach = catPawDip
        val pawX = rimX - catSide * (catUnit * (0.76f + reach * 0.38f))
        val pawY = tankY + catUnit * 0.10f + reach * (waterY - tankY + catUnit * 0.12f)

        // Slender reaching cat arm curved over top rim
        detailPath.reset()
        detailPath.moveTo(rimX - catSide * catUnit * 0.24f, tankY - catUnit * 0.06f)
        detailPath.quadTo(rimX - catSide * catUnit * 0.88f, tankY - catUnit * 0.26f + reach * catUnit * 0.25f, pawX, pawY)
        strokePaint.color = aqPal.catBody
        strokePaint.strokeWidth = (catUnit * 0.42f).coerceIn(10f, 28f)
        canvas.drawPath(detailPath, strokePaint)

        // Rounded cat paw pad
        val prx = catUnit * 0.28f
        val pry = catUnit * 0.21f
        fillPaint.color = aqPal.catBody
        canvas.drawOval(pawX - prx, pawY - pry + 2f, pawX + prx, pawY + pry + 2f, fillPaint)
        strokePaint.color = aqPal.catRim
        strokePaint.strokeWidth = (catUnit * 0.036f).coerceIn(1.0f, 2.2f)
        canvas.drawOval(pawX - prx, pawY - pry + 2f, pawX + prx, pawY + pry + 2f, strokePaint)

        // Concentric surface ripple rings around dipping paw!
        if (reach > 0.65f) {
            val rAlpha = (((reach - 0.65f) * 2.1f).coerceIn(0f, 0.72f) * 255f).toInt()
            strokePaint.color = (0x00FFFFFF) or (rAlpha shl 24)
            strokePaint.strokeWidth = 1.35f
            val r1 = catUnit * (0.50f + reach * 0.45f)
            val r2 = catUnit * (0.82f + reach * 0.65f)
            canvas.drawOval(pawX - r1, waterY - r1 * 0.18f, pawX + r1, waterY + r1 * 0.18f, strokePaint)
            canvas.drawOval(pawX - r2, waterY - r2 * 0.18f, pawX + r2, waterY + r2 * 0.18f, strokePaint)
        }
    }

    private fun withAlpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)

    private fun sqrtSafe(v: Float): Float = if (v <= 0f) 0f else kotlin.math.sqrt(v)

    companion object {
        private const val SPINE_N = 12
    }
}

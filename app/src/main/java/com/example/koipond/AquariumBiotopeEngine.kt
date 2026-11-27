package com.example.koipond

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
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
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Zero-Allocation Nature Aquarium × Southeast Asian Betta Biotope Engine (v2.12.0).
 *
 * Highlights:
 * - Takashi Amano Nature Aquarium composition: sloped aquasoil-to-cosmetic-sand valley,
 *   gnarled 5-branch Asian spiderwood centerpiece with Java Moss cushions & shadowed hiding arch,
 *   and 5 distinct aquatic plant species across 4 depth planes (red-tipped Rotala, Vallisneria,
 *   Java Fern, Anubias/Bucephalandra hammocks, bronze Cryptocoryne, Dwarf Hairgrass, Catappa leaves
 *   & alder cones, floating Frogbit/Salvinia with dappled light rays & bubble nest).
 * - Distinct Betta Varieties with genuinely different silhouettes & behaviors:
 *   HALFMOON (180° scalloped fan), CROWNTAIL (dramatic comb-spiked rays), DUMBO_VEIL (huge fluttering
 *   elephant-ear pectorals + drooping veil), and PLAKAT (agile compact Koi fighter), all driven by
 *   12-joint 3D articulated spines swimming freely in 360° X/Y/Z with true depth perspective sizing.
 * - Anatomically detailed 3D Tank Cleaners & Critters in a Unified Painter's Algorithm Z-Sort:
 *   Otocinclus & Bristlenose Pleco cling to FRONT glass (showing ventral belly + pulsing sucker mouth!),
 *   BACK glass (smaller hazed dorsal view!), SIDE glass, and driftwood; articulated Panda Corydoras
 *   forage in 3D depth across the sloped sand; segmented Red Cherry Shrimps hop between depth planes;
 *   and a Golden Mystery Snail climbs front glass, back glass, and stones.
 * - Rare occasional curious pet cat visitor peeking over the rim and dipping a playful paw while
 *   every Betta always darts safely into plant cover.
 */
class AquariumBiotopeEngine {

    private enum class BettaVariety { HALFMOON, CROWNTAIL, DUMBO_VEIL, PLAKAT }
    private enum class BettaState { CRUISE, HOVER, HIDE, REST, BREATH, INSPECT, ESCAPE }
    private enum class CatState { HIDDEN, PEEKING, STALKING, DIPPING, RETRACT, LEAVING }
    private enum class SurfacePlane { FRONT_GLASS, BACK_GLASS, LEFT_GLASS, RIGHT_GLASS, WOOD, STONE, TRANSIT }

    private class BettaFish(
        var variety: BettaVariety,
        var bodyCol: Int,
        var bodyHi: Int,
        var finBase: Int,
        var finMid: Int,
        var finTip: Int,
        var spotCol: Int,
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
        var targetX = 0.5f
        var targetY = 0.45f
        var targetZ = 0.45f
        var flare = 0f
        var zDepth = 0.45f
    }

    private class OtoCleaner(
        var plane: SurfacePlane,
        var normX: Float,
        var normY: Float,
        var zDepth: Float,
        var angleRad: Float, // travel orientation on front/back glass
        var pauseTimer: Float,
        var transitProgress: Float = 0f,
        var startX: Float = 0.03f,
        var startY: Float = 0.45f,
        var startZ: Float = 0.40f,
        var endX: Float = 0.50f,
        var endY: Float = 0.50f,
        var endZ: Float = 0.03f,
        var nextPlane: SurfacePlane = SurfacePlane.FRONT_GLASS,
        var phase: Float = 0f
    )

    private class BristlenosePleco(
        var plane: SurfacePlane = SurfacePlane.WOOD,
        var woodT: Float = 0.24f,
        var woodDir: Float = 1f,
        var normX: Float = 0.44f,
        var normY: Float = 0.68f,
        var zDepth: Float = 0.54f,
        var angleRad: Float = -0.35f,
        var pauseTimer: Float = 3.5f,
        var transitProgress: Float = 0f,
        var startX: Float = 0.44f,
        var startY: Float = 0.68f,
        var startZ: Float = 0.54f,
        var endX: Float = 0.34f,
        var endY: Float = 0.52f,
        var endZ: Float = 0.04f,
        var nextPlane: SurfacePlane = SurfacePlane.FRONT_GLASS,
        var phase: Float = 0f
    )

    private class CoryCatfish(
        var normX: Float,
        var normY: Float,
        var zDepth: Float,
        var targetX: Float,
        var targetZ: Float,
        var dir: Float,
        var pauseTimer: Float,
        var dashPhase: Float = 0f,
        var dashTimer: Float = 28f,
        var snuffleTimer: Float = 0f,
        var phase: Float = 0f
    )

    private class CherryShrimp(
        var homeKind: Int, // 0=woodMain(z=0.52), 1=woodBranch(z=0.56), 2=frontStone(z=0.18), 3=midMoss(z=0.44), 4=backMoss(z=0.78), 5=catappa(z=0.28)
        var t: Float,
        var zDepth: Float,
        var dir: Float,
        var hopProgress: Float = 0f,
        var hopStartX: Float = 0f,
        var hopStartY: Float = 0f,
        var hopStartZ: Float = 0.5f,
        var hopEndX: Float = 0f,
        var hopEndY: Float = 0f,
        var hopEndZ: Float = 0.5f,
        var phase: Float = 0f
    )

    private class MysterySnail(
        var plane: SurfacePlane = SurfacePlane.STONE,
        var normX: Float = 0.56f,
        var normY: Float = 0.96f,
        var zDepth: Float = 0.24f,
        var angleRad: Float = 0f,
        var dir: Float = 1f,
        var retractTimer: Float = 0f,
        var stateTimer: Float = 18f,
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
        var z: Float = 0.35f,
        var isWafer: Boolean = false,
        var restTimer: Float = 0f,
        var active: Boolean = false
    )

    private data class AquariumPalette(
        var roomTop: Int = 0,
        var roomBot: Int = 0,
        var shelfTop: Int = 0,
        var shelfFront: Int = 0,
        var shelfGrain: Int = 0,
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
        var soilBack: Int = 0,
        var sandTop: Int = 0,
        var sandBot: Int = 0,
        var caustic: Int = 0,
        var woodDark: Int = 0,
        var woodMid: Int = 0,
        var woodRim: Int = 0,
        var rotalaStem: Int = 0,
        var rotalaTip: Int = 0,
        var plantBack: Int = 0,
        var plantMid: Int = 0,
        var plantFront: Int = 0,
        var fernDark: Int = 0,
        var fernHi: Int = 0,
        var anubias: Int = 0,
        var anubiasHi: Int = 0,
        var cryptBronze: Int = 0,
        var cryptOlive: Int = 0,
        var moss: Int = 0,
        var mossHi: Int = 0,
        var catappa: Int = 0,
        var stoneDark: Int = 0,
        var stoneMid: Int = 0,
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

    // Tank rectangle in pixels
    private var tankX = 24f
    private var tankY = 400f
    private var tankW = 420f
    private var tankH = 520f
    private var waterY = 436f
    private var sandY = 860f
    private var lampY = 270f
    private var lampW = 310f
    private var lampPulseTimer = 0f
    private var pearlingCooldown = 1.2f

    // 1D Spring-Wave Water Surface Heightfield
    private val waveN = 48
    private val waveH = FloatArray(waveN)
    private val waveV = FloatArray(waveN)

    // 3 Genuinely Distinct Betta Varieties (Different silhouettes, fins & movement!)
    private val bettas = arrayOf(
        // 0: Royal Halfmoon (180° sweeping scalloped silk fan, deliberate majestic swimmer)
        BettaFish(
            variety = BettaVariety.HALFMOON,
            bodyCol = 0xFFDF4E3C.toInt(), bodyHi = 0xFFFA9E78.toInt(),
            finBase = 0xFFD63B34.toInt(), finMid = 0xFFF26C56.toInt(), finTip = 0xFF7BE2EA.toInt(),
            spotCol = 0xFF283A5E.toInt(),
            restAnchorIdx = 0, hideAnchorIdx = 0
        ),
        // 1: Spiky Crowntail (dramatic web-reduced comb-spiked rays, agile explorer!)
        BettaFish(
            variety = BettaVariety.CROWNTAIL,
            bodyCol = 0xFF218696.toInt(), bodyHi = 0xFF6BE4EE.toInt(),
            finBase = 0xFF1E6E82.toInt(), finMid = 0xFFD94A84.toInt(), finTip = 0xFFFFB2D0.toInt(),
            spotCol = 0xFF15384A.toInt(),
            restAnchorIdx = 1, hideAnchorIdx = 1
        ),
        // 2: Dumbo Elephant-Ear Veiltail (huge fluttering luminous pectoral ears + drooping pointed veil!)
        BettaFish(
            variety = BettaVariety.DUMBO_VEIL,
            bodyCol = 0xFF2E4D92.toInt(), bodyHi = 0xFF6B98EE.toInt(),
            finBase = 0xFF264080.toInt(), finMid = 0xFF4C72C6.toInt(), finTip = 0xFFF4C860.toInt(),
            spotCol = 0xFFF5ECE0.toInt(),
            restAnchorIdx = 2, hideAnchorIdx = 2
        )
    )

    // Upgraded 3D Tank Cleaners & Critters (can stick to FRONT glass, BACK glass, SIDE glass & wood!)
    private val otos = arrayOf(
        OtoCleaner(SurfacePlane.FRONT_GLASS, 0.24f, 0.56f, 0.03f, -1.25f, 2.2f, phase = 0.4f),
        OtoCleaner(SurfacePlane.BACK_GLASS, 0.78f, 0.44f, 0.94f, -1.85f, 3.4f, phase = 2.6f)
    )
    private val pleco = BristlenosePleco()
    private val corys = arrayOf(
        CoryCatfish(0.24f, 0.95f, 0.22f, 0.38f, 0.34f, 1f, 1.4f, dashTimer = 30f, phase = 0.2f),
        CoryCatfish(0.36f, 0.96f, 0.48f, 0.22f, 0.26f, -1f, 2.4f, dashTimer = 48f, phase = 2.1f)
    )
    private val snail = MysterySnail()
    private val shrimps = arrayOf(
        CherryShrimp(0, 0.28f, 0.52f, 1f, phase = 0.3f),
        CherryShrimp(0, 0.72f, 0.52f, -1f, phase = 1.5f),
        CherryShrimp(1, 0.58f, 0.56f, -1f, phase = 2.7f),
        CherryShrimp(2, 0.0f, 0.18f, 1f, phase = 3.6f),  // Foreground mossy stone (larger & crisp!)
        CherryShrimp(4, 0.0f, 0.78f, -1f, phase = 4.9f)  // Background moss (smaller & depth-hazed!)
    )

    // Unified Zero-Allocation Depth-Sorted Draw Queue across ALL living creatures & hardscape layers!
    // Kind: 0=Betta, 1=Otocinclus, 2=Pleco, 3=Corydoras, 4=Shrimp, 5=Snail, 6=MidgroundHardscape(z=0.54)
    private val maxEntities = 24
    private val drawKind = IntArray(maxEntities)
    private val drawIdx = IntArray(maxEntities)
    private val drawZ = FloatArray(maxEntities)

    // Ambient particles & food
    private val bubbles = Array(30) { MicroBubble() }
    private val motes = Array(24) { WaterMote() }
    private val sandPuffs = Array(14) { SandPuff() }
    private var nextPuffIdx = 0
    private val pellets = Array(8) { TankPellet() }
    private var nextPelletIdx = 0

    // Rare Occasional Pet Cat Visitor (absent ~88% of the time!)
    private var catState = CatState.HIDDEN
    private var catTimer = 42f
    private var catRise = 0f
    private var catPawDip = 0f
    private var catSide = 1f
    private var catSwipesLeft = 0
    private var catEyeLookX = -0.6f
    private var catEyeLookY = 0.4f
    private var catTargetBetta = 0
    private var catEarTwitch = 0f
    private var catSplashFired = false
    private var catActionIsGlassPat = false
    private var catGlassPatYFrac = 0.42f
    private var catBlinkTimer = 2.4f
    private var catBlinkDur = 0f
    private var catHeadTiltDeg = 0f

    // Living Betta Surface Bubble Nest state (grows when Halfmoon Betta blows fresh bubbles!)
    private var bubbleNestCount = 9
    private var bubbleNestDecayTimer = 28f
    private val floaterBaseFracs = floatArrayOf(0.18f, 0.24f, 0.29f, 0.58f, 0.65f, 0.73f)
    private val floaterLiveFracs = floatArrayOf(0.18f, 0.24f, 0.29f, 0.58f, 0.65f, 0.73f)

    val isHighMotion: Boolean
        get() = catState != CatState.HIDDEN ||
            lampPulseTimer > 0f ||
            pellets.any { it.active } ||
            bettas.any { it.state == BettaState.ESCAPE || it.state == BettaState.BREATH }

    // Reusable Zero-Alloc Paints & Paths
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

    // Cached Gradients
    private val aqPal = AquariumPalette()
    private var cachedRoomH = -1f; private var cachedRoomTop = 0; private var cachedRoomBot = 0
    private var roomGrad: LinearGradient? = null
    private var cachedWaterY = -1f; private var cachedWaterBotY = -1f
    private var cachedWaterTop = 0; private var cachedWaterMid = 0; private var cachedWaterBot = 0
    private var waterGrad: LinearGradient? = null
    private var cachedBeamY0 = -1f; private var cachedBeamY1 = -1f
    private var cachedBeamTop = 0; private var cachedBeamBot = 0
    private var beamGrad: LinearGradient? = null
    private var cachedSandY0 = -1f; private var cachedSandY1 = -1f
    private var cachedSandTop = 0; private var cachedSandBot = 0
    private var sandGrad: LinearGradient? = null
    private var cachedHaloX = -1f; private var cachedHaloY = -1f; private var cachedHaloR = -1f; private var cachedHaloCol = 0
    private var wallHaloGrad: RadialGradient? = null
    private var cachedVigX = -1f; private var cachedVigY = -1f; private var cachedVigR = -1f
    private var interiorVignetteGrad: RadialGradient? = null

    // Betta 3D Spine Scratch Buffers (Zero-Allocation!)
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
    private val bodyRadii = floatArrayOf(0.036f, 0.118f, 0.154f, 0.144f, 0.112f, 0.074f, 0.044f, 0.024f)

    // Tapered driftwood scratch buffers
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
            catActionIsGlassPat = rand.nextFloat() < 0.45f
            catBlinkTimer = 1.6f + rand.nextFloat() * 1.8f
            catBlinkDur = 0f
            catHeadTiltDeg = 0f
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
            // Koi-Vermilion Halfmoon
            intArrayOf(0xFFDF4E3C.toInt(), 0xFFFA9E78.toInt(), 0xFFD63B34.toInt(), 0xFFF26C56.toInt(), 0xFF7BE2EA.toInt(), 0xFF283A5E.toInt()),
            // Turquoise-Rose Crowntail
            intArrayOf(0xFF218696.toInt(), 0xFF6BE4EE.toInt(), 0xFF1E6E82.toInt(), 0xFFD94A84.toInt(), 0xFFFFB2D0.toInt(), 0xFF15384A.toInt()),
            // Cobalt-Mustard Dumbo Veil
            intArrayOf(0xFF2E4D92.toInt(), 0xFF6B98EE.toInt(), 0xFF264080.toInt(), 0xFF4C72C6.toInt(), 0xFFF4C860.toInt(), 0xFFF5ECE0.toInt()),
            // Calico Galaxy Koi Plakat
            intArrayOf(0xFFE66B32.toInt(), 0xFFFFC078.toInt(), 0xFFC94A22.toInt(), 0xFFE8864C.toInt(), 0xFFFFF0C8.toInt(), 0xFF1D3554.toInt()),
            // Orchid-Teal Crowntail / Dumbo
            intArrayOf(0xFF6B429E.toInt(), 0xFFBA8CF2.toInt(), 0xFF533082.toInt(), 0xFF9C5AD6.toInt(), 0xFF82ECE6.toInt(), 0xFFF7E2EE.toInt())
        )
        val varietySets = arrayOf(
            arrayOf(BettaVariety.HALFMOON, BettaVariety.CROWNTAIL, BettaVariety.DUMBO_VEIL),
            arrayOf(BettaVariety.HALFMOON, BettaVariety.PLAKAT, BettaVariety.CROWNTAIL),
            arrayOf(BettaVariety.DUMBO_VEIL, BettaVariety.PLAKAT, BettaVariety.HALFMOON)
        )
        val vSet = varietySets[r.nextInt(varietySets.size)]
        val offset = r.nextInt(morphs.size)
        for (i in bettas.indices) {
            val m = morphs[(offset + i) % morphs.size]
            bettas[i].variety = vSet[i]
            bettas[i].bodyCol = m[0]
            bettas[i].bodyHi = m[1]
            bettas[i].finBase = m[2]
            bettas[i].finMid = m[3]
            bettas[i].finTip = m[4]
            bettas[i].spotCol = m[5]
        }
    }

    fun onTouch(screenX: Float, screenY: Float) {
        if (!initialized) return

        // 1. Tap overhead LED lamp bar -> warm light shimmer pulse
        val lampLeft = (lastWidth - lampW) * 0.5f
        if (screenX in (lampLeft - 24f)..(lampLeft + lampW + 24f) &&
            screenY in (lampY - 34f)..(lampY + 42f)
        ) {
            lampPulseTimer = 1.8f
            return
        }

        // 2. Tap top glass rim -> summon or shoo cat
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

        // 3. Tap inside tank
        if (screenX in tankX..(tankX + tankW) && screenY in tankY..(tankY + tankH)) {
            val waterSpan = (sandY - waterY).coerceAtLeast(60f)
            val sx = tankX + snail.normX * tankW
            val sy = if (snail.plane == SurfacePlane.FRONT_GLASS || snail.plane == SurfacePlane.BACK_GLASS) {
                waterY + snail.normY * waterSpan
            } else {
                sandY + tankH * 0.022f
            }
            if (hypot(screenX - sx, screenY - sy) < tankW * 0.075f) {
                snail.retractTimer = 3.2f
                return
            }

            // Tap a front-glass Otocinclus -> nimble dart away to another pane!
            for (o in otos) {
                val ox = tankX + o.normX * tankW
                val oy = waterY + o.normY * waterSpan
                if (o.plane != SurfacePlane.TRANSIT && hypot(screenX - ox, screenY - oy) < tankW * 0.07f) {
                    startOtoTransit(o)
                    return
                }
            }

            // Tap directly on a Betta -> curved 3D escape into plant shelter!
            for (b in bettas) {
                if (hypot(screenX - b.nx[0], screenY - b.ny[0]) < tankW * 0.085f) {
                    b.state = BettaState.ESCAPE
                    b.stateTimer = 1.6f
                    b.flare = 0.80f
                    b.targetX = if (b.nx[0] < tankX + tankW * 0.5f) 0.86f else 0.14f
                    b.targetY = 0.42f + rand.nextFloat() * 0.34f
                    b.targetZ = 0.80f
                    spawnBubble(b.nx[0], b.ny[0] - 4f, 1)
                    injectRipple(screenX, 14f)
                    return
                }
            }

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
        p.z = 0.20f + rand.nextFloat() * 0.40f
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
                b.r = if (kind == 2) (tankW * 0.0030f).coerceIn(1.2f, 2.5f) else (tankW * 0.0046f).coerceIn(1.6f, 3.5f)
                b.vy = (tankH * if (kind == 2) 0.052f else 0.092f) * (0.85f + rand.nextFloat() * 0.3f)
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
        sp.vy = -(tankH * (0.024f + rand.nextFloat() * 0.024f))
        sp.age = 0f
        sp.maxAge = 0.9f + rand.nextFloat() * 0.5f
        sp.active = true
        nextPuffIdx = (nextPuffIdx + 1) % sandPuffs.size
    }

    /** Unified 3D Depth Perspective Scale: 1.24x right on front glass -> 0.58x against back glass! */
    private fun perspScale(z: Float): Float = (1.24f - z.coerceIn(0f, 1f) * 0.66f).coerceIn(0.56f, 1.26f)

    /** Unified Beer-Lambert water depth haze: 0% on front glass -> ~44% on back glass! */
    private fun depthFog(z: Float): Float = if (aqPal.isAmoled) 0f else (z.coerceIn(0f, 1f) * 0.45f).coerceIn(0f, 0.48f)

    // =========================================================================
    // MAIN UPDATE & UNIFIED 3D DEPTH-SORTED RENDER PIPELINE
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

        // 2. Room, Wood Shelf, Overhead LED Lamp & Rare Cat Behind Glass
        drawRoomAndLamp(canvas, config.enableBatteryLantern, isCharging)
        drawCatBehindGlass(canvas)

        // 3. Aquarium Interior (Clipped cleanly to Glass Walls!)
        canvas.save()
        canvas.clipRect(tankX, tankY, tankX + tankW, tankY + tankH)

        // 3A. Back Water Volume, 3D Glass Interior Perspective, Floating-Linked Dappled Rays & Distant Canopy
        drawWaterAndDappledRays(canvas, config.waterClarity, isBatterySaver)
        drawUndersideMeniscusReflections(canvas, activeBettaCount)
        drawBackgroundFlora(canvas, isBatterySaver)

        // 3B. Sloped Takashi Amano Aquasoil-to-Sand Substrate + Web Caustics + Fish Cast Sand Shadows
        drawGradedSubstrateAndCaustics(canvas, isBatterySaver)
        drawCreatureSandShadows(canvas, activeBettaCount, config.enableBottomFeeders)

        // 3C. Unified Painter's Algorithm Z-Sorted Layer (Back Glass -> Deep Fish -> Hardscape/Wood -> Mid Fish -> Foreground Plants -> Front Glass!)
        var qCount = 0
        fun enqueue(kind: Int, idx: Int, z: Float) {
            if (qCount < maxEntities) {
                drawKind[qCount] = kind
                drawIdx[qCount] = idx
                drawZ[qCount] = z
                qCount++
            }
        }
        // Enqueue Midground Spiderwood + Java Fern + Anubias Hardscape Island at z = 0.54
        enqueue(6, 0, 0.54f)
        // Enqueue Foreground Stones, Cryptocoryne, Catappa Leaves & Hairgrass at z = 0.25
        enqueue(7, 0, 0.25f)
        // Enqueue Foreground Framing Vallisneria at z = 0.11 (Bettas hide right behind these!)
        enqueue(8, 0, 0.11f)

        // Enqueue Active Bettas
        for (i in 0 until activeBettaCount) enqueue(0, i, bettas[i].zDepth)

        // Enqueue Tank Cleaners & Critters (so front-glass Oto/Pleco/Snail draw OVER everything,
        // and back-glass Oto/Pleco/Snail draw BEHIND hardscape & Bettas!)
        if (config.enableBottomFeeders) {
            for (i in otos.indices) enqueue(1, i, otos[i].zDepth)
            enqueue(2, 0, pleco.zDepth)
            for (i in corys.indices) enqueue(3, i, corys[i].zDepth)
            for (i in shrimps.indices) enqueue(4, i, shrimps[i].zDepth)
            enqueue(5, 0, snail.zDepth)
        }

        // Insertion sort descending by Z (back = 1.0 -> front = 0.0, zero allocations!)
        for (i in 1 until qCount) {
            val kK = drawKind[i]; val kI = drawIdx[i]; val kZ = drawZ[i]
            var j = i - 1
            while (j >= 0 && drawZ[j] < kZ) {
                drawKind[j + 1] = drawKind[j]; drawIdx[j + 1] = drawIdx[j]; drawZ[j + 1] = drawZ[j]
                j--
            }
            drawKind[j + 1] = kK; drawIdx[j + 1] = kI; drawZ[j + 1] = kZ
        }

        for (q in 0 until qCount) {
            when (drawKind[q]) {
                0 -> drawArticulatedBetta(canvas, bettas[drawIdx[q]], isBatterySaver)
                1 -> drawOtocinclus(canvas, otos[drawIdx[q]])
                2 -> drawBristlenosePleco(canvas)
                3 -> drawCorydoras(canvas, corys[drawIdx[q]])
                4 -> drawCherryShrimp(canvas, shrimps[drawIdx[q]])
                5 -> drawMysterySnail(canvas)
                6 -> drawMidgroundHardscape(canvas, isBatterySaver)
                7 -> drawForegroundBotanicals(canvas)
                8 -> drawForegroundFramingPlants(canvas)
            }
        }

        // 3D. Floating Frogbit/Salvinia Canopy, Bubble Nest, Diffuser Bubbles, ADA Hardware, Interior Vignette & Meniscus
        drawSandPuffs(canvas)
        drawFloatingCanopyAndBubbleNest(canvas)
        drawBubblesAndPellets(canvas)
        drawAdaGlassHardware(canvas)
        drawInteriorDepthVignette(canvas)
        drawMeniscusHighlight(canvas)

        canvas.restore()

        // 4. Low-Iron Rimless Glass Frame, Diagonal Specular Glint, Swishing Cat Tail & Dipping/Patting Paw
        drawGlassFrame(canvas)
        drawCatTailPeek(canvas)
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
        val prevTankX = tankX; val prevTankY = tankY; val prevTankW = tankW; val prevTankH = tankH

        val marginX = (w * 0.044f).coerceIn(12f, 40f)
        val pShiftX = parallaxX * (marginX * 0.45f)
        val pShiftY = parallaxY * (h * 0.014f)

        val topFrac = if (isWallpaperMode) {
            (waterlinePct / 100f).coerceIn(0.32f, 0.50f) + 0.03f
        } else {
            0.26f
        }
        val shelfReserve = h * (if (isWallpaperMode) 0.075f else 0.052f)

        tankW = (w - marginX * 2f).coerceAtLeast(180f)
        tankX = marginX + pShiftX
        tankY = (h * topFrac + pShiftY).coerceIn(h * 0.21f, h * 0.54f)
        tankH = (h - tankY - shelfReserve).coerceIn(h * 0.36f, h * 0.68f)
        waterY = tankY + tankH * 0.070f
        sandY = tankY + tankH * 0.895f
        lampW = tankW * 0.76f
        lampY = (tankY - tankH * (if (isWallpaperMode) 0.26f else 0.20f)).coerceAtLeast(h * 0.055f)

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
            floatArrayOf(0.32f, 0.34f, 0.24f, 0.2f),
            floatArrayOf(0.66f, 0.48f, 0.54f, 2.8f),
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
            b.vx = cos(yaw) * (tankW * 0.042f)
            b.vy = 0f
            b.vz = sin(yaw) * 0.025f
            b.zDepth = hz
            b.swimPhase = idx * 2.1f
            b.pecPhase = idx * 1.4f
            b.breathTimer = 14f + idx * 9f
            b.targetX = p[0]; b.targetY = p[1]; b.targetZ = hz
        }

        // Diffuser micro-bubbles (slots 0..13)
        for (i in 0 until 14) {
            val b = bubbles[i]
            b.x = tankX + tankW * 0.125f + (rand.nextFloat() - 0.5f) * (tankW * 0.014f)
            b.y = waterY + rand.nextFloat() * (sandY - waterY - tankH * 0.06f)
            b.r = (tankW * (0.0026f + rand.nextFloat() * 0.0034f)).coerceIn(1.2f, 3.2f)
            b.vy = tankH * (0.058f + rand.nextFloat() * 0.036f)
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

    private fun bettaSegLen(): Float = (tankW * 0.0072f).coerceIn(2.5f, 8.0f)

    // =========================================================================
    // SPIDERWOOD & HARDSCAPE GEOMETRY CURVES (Golden-Ratio Amano Slope!)
    // =========================================================================

    private fun evalWoodMainX(t: Float): Float {
        val x0 = tankX + tankW * 0.78f; val cx = tankX + tankW * 0.56f; val x1 = tankX + tankW * 0.28f
        val u = 1f - t
        return u * u * x0 + 2f * u * t * cx + t * t * x1
    }

    private fun evalWoodMainY(t: Float): Float {
        val y0 = sandY - tankH * 0.02f; val cy = sandY - tankH * 0.25f; val y1 = sandY - tankH * 0.44f
        val u = 1f - t
        return u * u * y0 + 2f * u * t * cy + t * t * y1
    }

    private fun evalWoodMainAng(t: Float): Float {
        val x0 = tankX + tankW * 0.78f; val cx = tankX + tankW * 0.56f; val x1 = tankX + tankW * 0.28f
        val y0 = sandY - tankH * 0.02f; val cy = sandY - tankH * 0.25f; val y1 = sandY - tankH * 0.44f
        val u = 1f - t
        return atan2(2f * u * (cy - y0) + 2f * t * (y1 - cy), 2f * u * (cx - x0) + 2f * t * (x1 - cx))
    }

    private fun evalWoodUpperX(t: Float): Float {
        val x0 = tankX + tankW * 0.72f; val cx = tankX + tankW * 0.65f; val x1 = tankX + tankW * 0.50f
        val u = 1f - t
        return u * u * x0 + 2f * u * t * cx + t * t * x1
    }

    private fun evalWoodUpperY(t: Float): Float {
        val y0 = sandY - tankH * 0.08f; val cy = sandY - tankH * 0.34f; val y1 = sandY - tankH * 0.57f
        val u = 1f - t
        return u * u * y0 + 2f * u * t * cy + t * t * y1
    }

    private fun evalWoodUpperAng(t: Float): Float {
        val x0 = tankX + tankW * 0.72f; val cx = tankX + tankW * 0.65f; val x1 = tankX + tankW * 0.50f
        val y0 = sandY - tankH * 0.08f; val cy = sandY - tankH * 0.34f; val y1 = sandY - tankH * 0.57f
        val u = 1f - t
        return atan2(2f * u * (cy - y0) + 2f * t * (y1 - cy), 2f * u * (cx - x0) + 2f * t * (x1 - cx))
    }

    private fun evalWoodRootX(t: Float): Float {
        val x0 = tankX + tankW * 0.68f; val cx = tankX + tankW * 0.50f; val x1 = tankX + tankW * 0.38f
        val u = 1f - t
        return u * u * x0 + 2f * u * t * cx + t * t * x1
    }

    private fun evalWoodRootY(t: Float): Float {
        val y0 = sandY - tankH * 0.10f; val cy = sandY - tankH * 0.12f; val y1 = sandY + tankH * 0.01f
        val u = 1f - t
        return u * u * y0 + 2f * u * t * cy + t * t * y1
    }

    private fun evalWoodRootAng(t: Float): Float {
        val x0 = tankX + tankW * 0.68f; val cx = tankX + tankW * 0.50f; val x1 = tankX + tankW * 0.38f
        val y0 = sandY - tankH * 0.10f; val cy = sandY - tankH * 0.12f; val y1 = sandY + tankH * 0.01f
        val u = 1f - t
        return atan2(2f * u * (cy - y0) + 2f * t * (y1 - cy), 2f * u * (cx - x0) + 2f * t * (x1 - cx))
    }

    private fun evalShrimpHomeX(kind: Int, t: Float): Float = when (kind) {
        0 -> evalWoodMainX(t)
        1 -> evalWoodUpperX(t)
        2 -> tankX + tankW * 0.24f // Foreground mossy stone (z=0.18)
        3 -> tankX + tankW * 0.58f // Midground stone moss (z=0.44)
        4 -> tankX + tankW * 0.84f // Background moss cushion (z=0.78)
        else -> tankX + tankW * 0.38f // Fallen Catappa leaf (z=0.28)
    }

    private fun evalShrimpHomeY(kind: Int, t: Float): Float = when (kind) {
        0 -> evalWoodMainY(t) - tankH * 0.012f
        1 -> evalWoodUpperY(t) - tankH * 0.010f
        2 -> sandY - tankH * 0.036f
        3 -> sandY - tankH * 0.056f
        4 -> sandY - tankH * 0.095f
        else -> sandY + tankH * 0.018f
    }

    private fun evalShrimpHomeZ(kind: Int): Float = when (kind) {
        0 -> 0.52f
        1 -> 0.56f
        2 -> 0.18f
        3 -> 0.44f
        4 -> 0.78f
        else -> 0.28f
    }

    /** Sand floor Y rises smoothly from front glass (z=0) toward back wall (z=1) for Amano depth! */
    private fun sandFloorYAtZ(z: Float): Float = sandY + tankH * 0.025f - z.coerceIn(0f, 1f) * (tankH * 0.095f)

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
        if (snail.retractTimer > 0f) snail.retractTimer = (snail.retractTimer - dt).coerceAtLeast(0f)

        // Live gentle circular lily-pipe drift on the floating frogbit canopy (drives dappled light rays too!)
        for (i in floaterBaseFracs.indices) {
            floaterLiveFracs[i] = (floaterBaseFracs[i] + sin(simTime * 0.34f + i * 0.85f) * 0.015f).coerceIn(0.10f, 0.88f)
        }
        // Gradual natural popping of extra bubble-nest bubbles so Halfmoon Betta keeps refreshing it
        if (bubbleNestCount > 8) {
            bubbleNestDecayTimer -= dt
            if (bubbleNestDecayTimer <= 0f) {
                bubbleNestCount--
                bubbleNestDecayTimer = 26f + rand.nextFloat() * 18f
            }
        }

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

        // 2. Water Motes, Diffuser Micro-Bubbles & Daytime Oxygen Pearling
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
            if (aqPal.isDaylight) {
                pearlingCooldown -= dt
                if (pearlingCooldown <= 0f) {
                    pearlingCooldown = 1.4f + rand.nextFloat() * 1.9f
                    val px = tankX + tankW * (if (rand.nextBoolean()) 0.64f else 0.82f) + (rand.nextFloat() - 0.5f) * tankW * 0.08f
                    val py = sandY - tankH * (0.24f + rand.nextFloat() * 0.14f)
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
                    b.x = tankX + tankW * 0.125f + (rand.nextFloat() - 0.5f) * (tankW * 0.014f)
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

        for (p in pellets) {
            if (!p.active) continue
            val floorLim = sandFloorYAtZ(p.z) - tankH * 0.012f
            if (p.y < floorLim) {
                p.y = (p.y + tankH * (if (p.isWafer) 0.14f else 0.042f) * dt).coerceAtMost(floorLim)
                if (!p.isWafer) p.x += sin(simTime * 2.2f + p.z * 8f) * tankW * 0.008f * dt
            } else {
                p.restTimer += dt
                if (p.restTimer > (if (p.isWafer) 14f else 8f)) p.active = false
            }
        }

        // 3. Rare Cat Visitor State Machine (Water Paw-Dip OR Front-Glass Curiosity Paw-Pat + Blink + Head Tilt!)
        val rimX = if (catSide > 0f) tankX + tankW * 0.76f else tankX + tankW * 0.24f
        val pawDipX = rimX - catSide * (tankW * 0.082f)
        if (catState != CatState.HIDDEN) {
            if (catBlinkDur > 0f) {
                catBlinkDur = (catBlinkDur - dt).coerceAtLeast(0f)
            } else {
                catBlinkTimer -= dt
                if (catBlinkTimer <= 0f) {
                    catBlinkDur = 0.16f
                    catBlinkTimer = 2.4f + rand.nextFloat() * 2.6f
                }
            }
        }
        when (catState) {
            CatState.HIDDEN -> {
                catRise = 0f; catPawDip = 0f; catHeadTiltDeg = 0f
                catTimer -= dt
                if (catTimer <= 0f) summonCatVisitNow()
            }
            CatState.PEEKING -> {
                catRise = (catRise + dt * 0.72f).coerceAtMost(1f)
                if (catRise >= 1f) { catState = CatState.STALKING; catTimer = 1.6f + rand.nextFloat() * 0.8f }
            }
            CatState.STALKING -> {
                catTimer -= dt
                val tb = bettas[catTargetBetta.coerceIn(0, activeBettaCount - 1)]
                val targetLookX = ((tb.nx[0] - rimX) / (tankW * 0.45f)).coerceIn(-1f, 1f)
                val targetLookY = ((tb.ny[0] - waterY) / (waterSpanY * 0.8f)).coerceIn(-0.2f, 1f)
                catEyeLookX += (targetLookX - catEyeLookX) * (dt * 4.2f).coerceAtMost(1f)
                catEyeLookY += (targetLookY - catEyeLookY) * (dt * 4.2f).coerceAtMost(1f)
                val desiredTilt = (targetLookX * -8.5f * catSide).coerceIn(-10f, 10f)
                catHeadTiltDeg += (desiredTilt - catHeadTiltDeg) * (dt * 3.2f).coerceAtMost(1f)
                catGlassPatYFrac = ((tb.ny[0] - waterY) / waterSpanY).coerceIn(0.18f, 0.54f)
                if (rand.nextFloat() < dt * 0.55f) catEarTwitch = 0.7f
                if (catTimer <= 0f) { catState = CatState.DIPPING; catSplashFired = false }
            }
            CatState.DIPPING -> {
                catPawDip = (catPawDip + dt * 2.65f).coerceAtMost(1f)
                if (catPawDip >= 0.82f && !catSplashFired) {
                    catSplashFired = true
                    if (!catActionIsGlassPat) {
                        injectRipple(pawDipX, 30f)
                        spawnBubble(pawDipX - tankW * 0.012f, waterY + tankH * 0.028f, 1)
                        spawnBubble(pawDipX + tankW * 0.014f, waterY + tankH * 0.036f, 1)
                    } else {
                        injectRipple(pawDipX, 8f) // tiny glass vibration ripple!
                    }
                    val patY = if (catActionIsGlassPat) (waterY + catGlassPatYFrac * waterSpanY) else waterY
                    for (i in 0 until activeBettaCount) {
                        val b = bettas[i]
                        if (hypot(b.nx[0] - pawDipX, b.ny[0] - patY) < tankW * 0.64f) {
                            b.state = BettaState.ESCAPE
                            b.stateTimer = 2.0f + rand.nextFloat() * 0.7f
                            b.flare = 0.95f
                            b.targetX = if (catSide > 0f) (0.10f + rand.nextFloat() * 0.16f) else (0.74f + rand.nextFloat() * 0.16f)
                            b.targetY = 0.44f + rand.nextFloat() * 0.34f
                            b.targetZ = 0.80f + rand.nextFloat() * 0.10f
                        }
                    }
                }
                if (catPawDip >= 1f) { catState = CatState.RETRACT; catTimer = if (catActionIsGlassPat) 0.95f else 0.65f }
            }
            CatState.RETRACT -> {
                catTimer -= dt
                if (catTimer <= 0f) {
                    catPawDip = (catPawDip - dt * 2.1f).coerceAtLeast(0f)
                    if (catPawDip <= 0f) {
                        catSwipesLeft--
                        if (catSwipesLeft > 0) {
                            catActionIsGlassPat = !catActionIsGlassPat // Alternate behavior on 2nd swipe!
                            catState = CatState.STALKING; catTimer = 1.8f
                        } else {
                            catState = CatState.LEAVING; catTimer = 0.9f
                        }
                    }
                }
            }
            CatState.LEAVING -> {
                catTimer -= dt
                catHeadTiltDeg *= exp(-3f * dt)
                if (catTimer <= 0f) {
                    catRise = (catRise - dt * 0.62f).coerceAtLeast(0f)
                    if (catRise <= 0f) { catState = CatState.HIDDEN; catTimer = 60f + rand.nextFloat() * 52f }
                }
            }
        }

        // 4. Update Betta Trio (True 3D Articulated Spine + Variety-Specific Speed & Behavior!)
        val zScalePx = tankW * 0.58f
        val segLen = bettaSegLen()
        val nightCalm = if (aqPal.isNight) 0.72f else 1.0f

        for (idx in 0 until activeBettaCount) {
            val b = bettas[idx]
            val hx = b.nx[0]; val hy = b.ny[0]; val hz = b.nz[0]
            b.flare = (b.flare - dt * 0.48f).coerceAtLeast(0f)
            b.breathTimer -= dt
            if (b.hideCooldown > 0f) b.hideCooldown -= dt

            // Variety-authentic swim agility (Plakat is swift & agile; Halfmoon/Dumbo are serene & deliberate!)
            val varietySpdMult = when (b.variety) {
                BettaVariety.PLAKAT -> 1.24f
                BettaVariety.CROWNTAIL -> 1.06f
                BettaVariety.DUMBO_VEIL -> 0.88f
                BettaVariety.HALFMOON -> 0.92f
            }

            var foodPellet: TankPellet? = null
            if (b.state != BettaState.ESCAPE) {
                var bestD = tankW * 0.54f
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
                if (hypot(foodPellet.x - hx, foodPellet.y - hy) < tankW * 0.038f) {
                    foodPellet.active = false
                    spawnBubble(hx, hy - 4f, 1)
                    b.state = BettaState.HOVER; b.stateTimer = 1.8f
                }
            } else {
                when (b.state) {
                    BettaState.ESCAPE -> {
                        b.stateTimer -= dt
                        if (b.stateTimer <= 0f) {
                            b.state = BettaState.HIDE
                            b.stateTimer = 3.2f + rand.nextFloat() * 3.0f
                        }
                    }
                    BettaState.BREATH -> {
                        b.targetY = 0.025f
                        if (hy <= waterY + tankH * 0.034f) {
                            injectRipple(hx, 11f)
                            spawnBubble(hx - tankW * 0.008f, waterY + tankH * 0.022f, 1)
                            spawnBubble(hx + tankW * 0.008f, waterY + tankH * 0.030f, 1)
                            // Halfmoon Betta blows fresh shimmering bubbles into its floating bubble nest!
                            if (idx == 0 && hx > tankX + tankW * 0.50f) {
                                bubbleNestCount = (bubbleNestCount + 2).coerceAtMost(16)
                                bubbleNestDecayTimer = 30f
                            }
                            b.breathTimer = 24f + rand.nextFloat() * 18f
                            b.state = BettaState.CRUISE; b.stateTimer = 3.2f
                            b.targetX = 0.16f + rand.nextFloat() * 0.68f
                            b.targetY = 0.26f + rand.nextFloat() * 0.48f
                            b.targetZ = 0.14f + rand.nextFloat() * 0.70f
                        }
                    }
                    else -> {
                        b.stateTimer -= dt
                        if (b.breathTimer <= 0f && catState == CatState.HIDDEN) {
                            b.state = BettaState.BREATH
                            b.targetX = (((hx - tankX) / tankW) + (rand.nextFloat() - 0.5f) * 0.18f).coerceIn(0.18f, 0.82f)
                            b.targetY = 0.022f
                            b.targetZ = 0.22f + rand.nextFloat() * 0.38f
                        } else if (b.stateTimer <= 0f) {
                            val roll = rand.nextFloat()
                            when {
                                // Hide behind foreground Vallisneria or tucked under spiderwood cave arch!
                                roll < (if (aqPal.isNight) 0.28f else 0.20f) && b.hideCooldown <= 0f -> {
                                    b.state = BettaState.HIDE
                                    b.stateTimer = 4.5f + rand.nextFloat() * 4.5f
                                    b.hideCooldown = 14f + rand.nextFloat() * 10f
                                    when (b.hideAnchorIdx) {
                                        0 -> { b.targetX = 0.10f; b.targetY = 0.56f; b.targetZ = 0.82f }
                                        1 -> { b.targetX = 0.89f; b.targetY = 0.54f; b.targetZ = 0.82f }
                                        else -> { b.targetX = 0.54f; b.targetY = 0.72f; b.targetZ = 0.68f }
                                    }
                                }
                                // Lounge on broad Java Fern / Anubias hammock leaves or under frogbit roots!
                                roll < (if (aqPal.isNight) 0.54f else 0.36f) -> {
                                    b.state = BettaState.REST
                                    b.stateTimer = 4.0f + rand.nextFloat() * 3.8f
                                    when (b.restAnchorIdx) {
                                        0 -> { b.targetX = 0.64f; b.targetY = 0.56f; b.targetZ = 0.44f }
                                        1 -> { b.targetX = 0.44f; b.targetY = 0.64f; b.targetZ = 0.48f }
                                        else -> { b.targetX = 0.62f; b.targetY = 0.14f; b.targetZ = 0.32f }
                                    }
                                }
                                // Curiously inspect a shrimp or front-glass Otocinclus!
                                roll < 0.50f && enableCleaners -> {
                                    b.state = BettaState.INSPECT
                                    b.stateTimer = 2.6f + rand.nextFloat() * 1.8f
                                    val sh = shrimps[rand.nextInt(shrimps.size)]
                                    val sx = evalShrimpHomeX(sh.homeKind, sh.t)
                                    val sy = evalShrimpHomeY(sh.homeKind, sh.t)
                                    b.targetX = ((sx - tankX) / tankW - 0.06f).coerceIn(0.14f, 0.86f)
                                    b.targetY = ((sy - waterY) / waterSpanY - 0.08f).coerceIn(0.16f, 0.82f)
                                    b.targetZ = evalShrimpHomeZ(sh.homeKind).coerceIn(0.16f, 0.76f)
                                }
                                roll < 0.66f -> {
                                    b.state = BettaState.HOVER
                                    b.stateTimer = 2.2f + rand.nextFloat() * 2.4f
                                }
                                else -> {
                                    b.state = BettaState.CRUISE
                                    b.stateTimer = 3.0f + rand.nextFloat() * 3.4f
                                    b.targetX = 0.12f + rand.nextFloat() * 0.76f
                                    b.targetY = 0.12f + rand.nextFloat() * 0.72f
                                    b.targetZ = 0.12f + rand.nextFloat() * 0.76f // Full front-glass <-> back-plant 3D freedom!
                                }
                            }
                        }
                    }
                }
            }

            val txPx = tankX + b.targetX * tankW
            val tyPx = waterY + b.targetY * waterSpanY
            val dx = txPx - hx; val dy = tyPx - hy; val dzPx = (b.targetZ - hz) * zScalePx
            val dist3D = sqrtSafe(dx * dx + dy * dy + dzPx * dzPx)

            var repX = 0f; var repY = 0f; var repZ = 0f
            val personalSpace = tankW * 0.19f
            for (j in 0 until activeBettaCount) {
                if (j == idx) continue
                val o = bettas[j]
                val rx = hx - o.nx[0]; val ry = hy - o.ny[0]; val rz = (hz - o.nz[0]) * zScalePx
                val rd = sqrtSafe(rx * rx + ry * ry + rz * rz)
                if (rd < personalSpace && rd > 1f) {
                    b.flare = (b.flare + dt * 1.45f).coerceAtMost(1f)
                    val push = (personalSpace - rd) * 0.62f
                    repX += (rx / rd) * push; repY += (ry / rd) * push; repZ += (rz / rd) * push
                }
            }

            val baseSpd = tankW * when {
                b.state == BettaState.ESCAPE -> 0.36f
                foodPellet != null -> 0.175f
                b.state == BettaState.BREATH -> 0.105f
                b.state == BettaState.HOVER -> 0.015f
                b.state == BettaState.HIDE || b.state == BettaState.REST -> if (dist3D > tankW * 0.045f) 0.070f else 0.012f
                b.state == BettaState.INSPECT -> 0.060f
                else -> 0.084f
            } * varietySpdMult * nightCalm

            val steerRate = if (b.state == BettaState.ESCAPE) 5.6f else 1.72f
            var desVx = b.vx * 0.9f; var desVy = b.vy * 0.9f; var desVzPx = b.vz * zScalePx * 0.9f

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
            b.vz = (b.vz * zScalePx + (desVzPx - b.vz * zScalePx) * blend) / zScalePx.coerceAtLeast(1f)

            val padX = tankW * 0.075f
            if (hx < tankX + padX) b.vx += (tankX + padX - hx) * 3.4f * dt
            if (hx > tankX + tankW - padX) b.vx -= (hx - (tankX + tankW - padX)) * 3.4f * dt
            if (hy < waterY + tankH * 0.032f) b.vy += (waterY + tankH * 0.032f - hy) * 4.0f * dt
            if (hy > sandY - tankH * 0.068f) b.vy -= (hy - (sandY - tankH * 0.068f)) * 4.0f * dt
            if (hz < 0.10f) b.vz += (0.10f - hz) * 1.8f * dt
            if (hz > 0.88f) b.vz -= (hz - 0.88f) * 1.8f * dt

            b.nx[0] = (hx + b.vx * dt).coerceIn(tankX + tankW * 0.04f, tankX + tankW * 0.96f)
            b.ny[0] = (hy + b.vy * dt).coerceIn(waterY + tankH * 0.018f, sandY - tankH * 0.045f)
            b.nz[0] = (hz + b.vz * dt).coerceIn(0.08f, 0.90f)

            b.speed = sqrtSafe(b.vx * b.vx + b.vy * b.vy + (b.vz * zScalePx) * (b.vz * zScalePx))
            val spdNorm = b.speed / tankW.coerceAtLeast(1f)
            b.swimPhase += dt * (2.5f + spdNorm * 44f)
            b.pecPhase += dt * (if (b.variety == BettaVariety.DUMBO_VEIL) (3.6f + spdNorm * 24f) else (4.8f + spdNorm * 32f))

            for (i in 1 until SPINE_N) {
                val px = b.nx[i - 1]; val py = b.ny[i - 1]; val pz = b.nz[i - 1]
                var sdx = b.nx[i] - px; var sdy = b.ny[i] - py; var sdzPx = (b.nz[i] - pz) * zScalePx
                var d = sqrtSafe(sdx * sdx + sdy * sdy + sdzPx * sdzPx)
                if (d < 0.001f) { sdx = -1f; d = 1f }

                val frac = i.toFloat() / (SPINE_N - 1).toFloat()
                val waveAmp = (0.35f + frac * frac * 2.3f) * (0.42f + (spdNorm * 7.5f).coerceAtMost(1.0f) * 0.78f) * (segLen * 0.34f)
                val wave = sin(b.swimPhase - i * 0.56f) * waveAmp
                val hLen = hypot(sdx, sdzPx).coerceAtLeast(0.001f)
                val perpX = -sdzPx / hLen; val perpZ = sdx / hLen
                val hoverDriftY = sin(b.swimPhase * 0.5f - i * 0.45f) * frac * (segLen * 0.11f)

                val tx = px + (sdx / d) * segLen + perpX * wave * dt * 9.5f
                val ty = py + (sdy / d) * segLen + hoverDriftY * dt * 7.5f
                val tzPx = pz * zScalePx + (sdzPx / d) * segLen + perpZ * wave * dt * 9.5f

                val rdx = tx - px; val rdy = ty - py; val rdzPx = tzPx - pz * zScalePx
                val rd = sqrtSafe(rdx * rdx + rdy * rdy + rdzPx * rdzPx).coerceAtLeast(0.001f)
                b.nx[i] = px + (rdx / rd) * segLen
                b.ny[i] = py + (rdy / rd) * segLen
                b.nz[i] = (pz + ((rdzPx / rd) * segLen) / zScalePx).coerceIn(0.06f, 0.92f)
            }
            b.zDepth = b.nz[2]
        }

        if (enableCleaners) {
            updateCleanersAndCritters(dt, activeBettaCount)
        }
    }

    private fun startOtoTransit(o: OtoCleaner) {
        o.startX = o.normX; o.startY = o.normY; o.startZ = o.zDepth
        val pick = rand.nextFloat()
        when {
            pick < 0.34f -> {
                o.nextPlane = SurfacePlane.FRONT_GLASS
                o.endX = 0.14f + rand.nextFloat() * 0.72f
                o.endY = 0.22f + rand.nextFloat() * 0.56f
                o.endZ = 0.03f
                o.angleRad = -PI.toFloat() * 0.5f + (rand.nextFloat() - 0.5f) * 1.4f
            }
            pick < 0.64f -> {
                o.nextPlane = SurfacePlane.BACK_GLASS
                o.endX = 0.16f + rand.nextFloat() * 0.68f
                o.endY = 0.20f + rand.nextFloat() * 0.52f
                o.endZ = 0.94f
                o.angleRad = -PI.toFloat() * 0.5f + (rand.nextFloat() - 0.5f) * 1.4f
            }
            pick < 0.82f -> {
                o.nextPlane = SurfacePlane.LEFT_GLASS
                o.endX = 0.026f
                o.endY = 0.26f + rand.nextFloat() * 0.48f
                o.endZ = 0.25f + rand.nextFloat() * 0.45f
                o.angleRad = if (rand.nextBoolean()) -PI.toFloat() * 0.5f else PI.toFloat() * 0.5f
            }
            else -> {
                o.nextPlane = SurfacePlane.RIGHT_GLASS
                o.endX = 0.974f
                o.endY = 0.26f + rand.nextFloat() * 0.48f
                o.endZ = 0.25f + rand.nextFloat() * 0.45f
                o.angleRad = if (rand.nextBoolean()) -PI.toFloat() * 0.5f else PI.toFloat() * 0.5f
            }
        }
        o.transitProgress = 0f
        o.plane = SurfacePlane.TRANSIT
    }

    private fun updateCleanersAndCritters(dt: Float, activeBettaCount: Int) {
        var activeWaferX = -1f
        var activeWaferZ = 0.32f
        for (p in pellets) {
            if (p.active && p.isWafer && p.y >= sandFloorYAtZ(p.z) - tankH * 0.05f) {
                activeWaferX = ((p.x - tankX) / tankW).coerceIn(0.10f, 0.90f)
                activeWaferZ = p.z
                break
            }
        }

        // A. Otocinclus Cleaners: cling to FRONT glass, BACK glass, LEFT/RIGHT glass & dart smoothly between them!
        for (o in otos) {
            o.phase += dt * 5.6f
            when (o.plane) {
                SurfacePlane.FRONT_GLASS, SurfacePlane.BACK_GLASS -> {
                    o.pauseTimer -= dt
                    if (o.pauseTimer <= 0f) {
                        val spd = 0.052f
                        o.normX += cos(o.angleRad) * spd * dt
                        o.normY += sin(o.angleRad) * spd * dt
                        if (o.normX < 0.12f || o.normX > 0.88f || o.normY < 0.18f || o.normY > 0.78f) {
                            o.normX = o.normX.coerceIn(0.12f, 0.88f)
                            o.normY = o.normY.coerceIn(0.18f, 0.78f)
                            o.angleRad = atan2(0.48f - o.normY, 0.50f - o.normX) + (rand.nextFloat() - 0.5f) * 0.7f
                            o.pauseTimer = 2.0f + rand.nextFloat() * 2.4f
                        } else if (rand.nextFloat() < dt * 0.085f) {
                            startOtoTransit(o)
                        } else if (rand.nextFloat() < dt * 0.38f) {
                            o.angleRad += (rand.nextFloat() - 0.5f) * 0.85f
                            o.pauseTimer = 1.6f + rand.nextFloat() * 2.6f
                        }
                    }
                }
                SurfacePlane.LEFT_GLASS, SurfacePlane.RIGHT_GLASS -> {
                    o.pauseTimer -= dt
                    if (o.pauseTimer <= 0f) {
                        val dirY = if (sin(o.angleRad) >= 0f) 1f else -1f
                        o.normY += dirY * 0.054f * dt
                        if (o.normY < 0.22f) { o.angleRad = PI.toFloat() * 0.5f; o.pauseTimer = 2.4f }
                        else if (o.normY > 0.78f) { o.angleRad = -PI.toFloat() * 0.5f; o.pauseTimer = 2.4f }
                        else if (rand.nextFloat() < dt * 0.095f) startOtoTransit(o)
                        else if (rand.nextFloat() < dt * 0.34f) o.pauseTimer = 1.8f + rand.nextFloat() * 2.5f
                    }
                }
                SurfacePlane.TRANSIT -> {
                    o.transitProgress += dt * 0.62f
                    val u = o.transitProgress.coerceIn(0f, 1f)
                    val ease = u * u * (3f - 2f * u)
                    o.normX = o.startX + (o.endX - o.startX) * ease
                    o.normY = o.startY + (o.endY - o.startY) * ease - sin(u * PI.toFloat()) * 0.08f
                    o.zDepth = o.startZ + (o.endZ - o.startZ) * ease
                    if (o.transitProgress >= 1f) {
                        o.plane = o.nextPlane
                        o.zDepth = o.endZ
                        o.pauseTimer = 2.2f + rand.nextFloat() * 2.2f
                    }
                }
                else -> o.plane = SurfacePlane.FRONT_GLASS
            }
        }

        // B. Bristlenose Pleco: moves between Spiderwood Arch (daytime hide) & Front/Back Glass (dusk/night grazing!)
        pleco.phase += dt * 2.8f
        when (pleco.plane) {
            SurfacePlane.WOOD -> {
                val minT = if (aqPal.isDaylight) 0.14f else 0.18f
                val maxT = if (aqPal.isDaylight) 0.34f else 0.74f
                pleco.zDepth = 0.54f
                pleco.pauseTimer -= dt
                if (pleco.pauseTimer <= 0f) {
                    if (pleco.woodT > maxT) pleco.woodDir = -1f
                    if (pleco.woodT < minT) pleco.woodDir = 1f
                    pleco.woodT = (pleco.woodT + pleco.woodDir * 0.032f * dt).coerceIn(0.12f, 0.78f)
                    if (pleco.woodT >= maxT || pleco.woodT <= minT) {
                        pleco.woodDir = -pleco.woodDir
                        pleco.pauseTimer = 4.2f + rand.nextFloat() * 3.8f
                    } else if (!aqPal.isDaylight && rand.nextFloat() < dt * 0.045f) {
                        // Venture onto Front Glass or Back Glass to graze algae!
                        val waterSpan = (sandY - waterY).coerceAtLeast(60f)
                        pleco.startX = ((evalWoodMainX(pleco.woodT) - tankX) / tankW).coerceIn(0.1f, 0.9f)
                        pleco.startY = ((evalWoodMainY(pleco.woodT) - waterY) / waterSpan).coerceIn(0.2f, 0.85f)
                        pleco.startZ = 0.54f
                        val toFront = rand.nextFloat() < 0.62f
                        pleco.nextPlane = if (toFront) SurfacePlane.FRONT_GLASS else SurfacePlane.BACK_GLASS
                        pleco.endX = 0.22f + rand.nextFloat() * 0.56f
                        pleco.endY = 0.34f + rand.nextFloat() * 0.42f
                        pleco.endZ = if (toFront) 0.04f else 0.93f
                        pleco.angleRad = -PI.toFloat() * 0.5f + (rand.nextFloat() - 0.5f) * 1.1f
                        pleco.transitProgress = 0f
                        pleco.plane = SurfacePlane.TRANSIT
                    } else if (rand.nextFloat() < dt * 0.25f) {
                        pleco.pauseTimer = 3.5f + rand.nextFloat() * 4.0f
                    }
                }
            }
            SurfacePlane.FRONT_GLASS, SurfacePlane.BACK_GLASS -> {
                pleco.pauseTimer -= dt
                if (pleco.pauseTimer <= 0f) {
                    pleco.normX += cos(pleco.angleRad) * 0.028f * dt
                    pleco.normY += sin(pleco.angleRad) * 0.028f * dt
                    if (pleco.normX < 0.16f || pleco.normX > 0.84f || pleco.normY < 0.24f || pleco.normY > 0.78f) {
                        pleco.normX = pleco.normX.coerceIn(0.16f, 0.84f)
                        pleco.normY = pleco.normY.coerceIn(0.24f, 0.78f)
                        pleco.angleRad = atan2(0.50f - pleco.normY, 0.50f - pleco.normX)
                        pleco.pauseTimer = 3.2f
                    } else if ((aqPal.isDaylight && rand.nextFloat() < dt * 0.20f) || rand.nextFloat() < dt * 0.055f) {
                        // Return calmly to the spiderwood arch!
                        val waterSpan = (sandY - waterY).coerceAtLeast(60f)
                        pleco.startX = pleco.normX; pleco.startY = pleco.normY; pleco.startZ = pleco.zDepth
                        pleco.nextPlane = SurfacePlane.WOOD
                        pleco.woodT = 0.25f
                        pleco.endX = ((evalWoodMainX(pleco.woodT) - tankX) / tankW).coerceIn(0.1f, 0.9f)
                        pleco.endY = ((evalWoodMainY(pleco.woodT) - waterY) / waterSpan).coerceIn(0.2f, 0.85f)
                        pleco.endZ = 0.54f
                        pleco.transitProgress = 0f
                        pleco.plane = SurfacePlane.TRANSIT
                    } else if (rand.nextFloat() < dt * 0.30f) {
                        pleco.angleRad += (rand.nextFloat() - 0.5f) * 0.7f
                        pleco.pauseTimer = 2.6f + rand.nextFloat() * 3.2f
                    }
                }
            }
            SurfacePlane.TRANSIT -> {
                pleco.transitProgress += dt * 0.45f
                val u = pleco.transitProgress.coerceIn(0f, 1f)
                val ease = u * u * (3f - 2f * u)
                pleco.normX = pleco.startX + (pleco.endX - pleco.startX) * ease
                pleco.normY = pleco.startY + (pleco.endY - pleco.startY) * ease
                pleco.zDepth = pleco.startZ + (pleco.endZ - pleco.startZ) * ease
                if (pleco.transitProgress >= 1f) {
                    pleco.plane = pleco.nextPlane
                    pleco.zDepth = pleco.endZ
                    pleco.pauseTimer = 3.0f
                }
            }
            else -> pleco.plane = SurfacePlane.WOOD
        }

        // C. Shoaling Panda Corydoras Catfish (3D foraging across sloped sand: near glass = big, deep = small!)
        for (idx in corys.indices) {
            val c = corys[idx]
            c.phase += dt * 6.5f
            c.dashTimer -= dt
            if (c.dashPhase > 0f) {
                c.dashPhase += dt * 0.88f
                val u = c.dashPhase.coerceIn(0f, 1f)
                c.normY = 0.95f - sin(u * PI.toFloat()) * 0.88f
                c.normX = (c.normX + c.dir * 0.08f * dt).coerceIn(0.14f, 0.86f)
                if (u in 0.46f..0.54f && rand.nextFloat() < dt * 16f) {
                    injectRipple(tankX + c.normX * tankW, 10f)
                    spawnBubble(tankX + c.normX * tankW, waterY + tankH * 0.03f, 1)
                }
                if (c.dashPhase >= 1f) {
                    c.dashPhase = 0f; c.normY = 0.95f
                    c.dashTimer = 34f + rand.nextFloat() * 28f; c.pauseTimer = 1.6f
                }
                continue
            }
            if (c.dashTimer <= 0f && catState == CatState.HIDDEN) { c.dashPhase = 0.01f; continue }

            if (activeWaferX > 0f) {
                c.targetX = activeWaferX
                c.targetZ = activeWaferZ.coerceIn(0.14f, 0.68f)
            } else {
                c.pauseTimer -= dt
                if (c.pauseTimer <= 0f) {
                    val buddy = corys[1 - idx]
                    if (hypot(buddy.normX - c.normX, buddy.zDepth - c.zDepth) > 0.34f && rand.nextFloat() < 0.65f) {
                        c.targetX = (buddy.normX + (rand.nextFloat() - 0.5f) * 0.16f).coerceIn(0.14f, 0.74f)
                        c.targetZ = (buddy.zDepth + (rand.nextFloat() - 0.5f) * 0.18f).coerceIn(0.14f, 0.72f)
                    } else {
                        c.targetX = 0.14f + rand.nextFloat() * 0.62f
                        c.targetZ = 0.14f + rand.nextFloat() * 0.58f
                    }
                    c.pauseTimer = 2.6f + rand.nextFloat() * 3.0f
                }
            }

            val dx = c.targetX - c.normX
            val dz = c.targetZ - c.zDepth
            val d = hypot(dx, dz)
            if (d > 0.022f) {
                val spd = if (activeWaferX > 0f) 0.092f else 0.052f
                c.normX = (c.normX + (dx / d) * spd * dt).coerceIn(0.12f, 0.84f)
                c.zDepth = (c.zDepth + (dz / d) * spd * 0.85f * dt).coerceIn(0.12f, 0.74f)
                if (abs(dx) > 0.01f) c.dir = if (dx >= 0f) 1f else -1f
                c.snuffleTimer += dt
                if (c.snuffleTimer > 0.82f && rand.nextFloat() < 0.48f) {
                    c.snuffleTimer = 0f
                    spawnSandPuff(tankX + c.normX * tankW + c.dir * tankW * 0.018f, sandFloorYAtZ(c.zDepth))
                }
            }
        }

        // D. Golden Mystery Snail (glides on Sand/Stones AND climbs Front Glass or Back Glass!)
        snail.phase += dt * 3.2f
        if (snail.retractTimer <= 0f) {
            snail.stateTimer -= dt
            when (snail.plane) {
                SurfacePlane.FRONT_GLASS, SurfacePlane.BACK_GLASS -> {
                    snail.normX = (snail.normX + cos(snail.angleRad) * 0.0085f * dt).coerceIn(0.14f, 0.86f)
                    snail.normY = (snail.normY + sin(snail.angleRad) * 0.0085f * dt).coerceIn(0.24f, 0.90f)
                    if (snail.normY <= 0.25f) snail.angleRad = PI.toFloat() * 0.5f + (rand.nextFloat() - 0.5f) * 0.6f
                    if (snail.normY >= 0.89f && snail.stateTimer <= 0f) {
                        snail.plane = SurfacePlane.STONE
                        snail.zDepth = 0.24f
                        snail.stateTimer = 22f + rand.nextFloat() * 18f
                    }
                }
                else -> {
                    snail.normX += snail.dir * 0.0075f * dt
                    if (snail.normX > 0.72f) snail.dir = -1f
                    if (snail.normX < 0.26f) snail.dir = 1f
                    if (snail.stateTimer <= 0f) {
                        val toFront = rand.nextFloat() < 0.65f
                        snail.plane = if (toFront) SurfacePlane.FRONT_GLASS else SurfacePlane.BACK_GLASS
                        snail.zDepth = if (toFront) 0.03f else 0.94f
                        snail.normY = 0.88f
                        snail.angleRad = -PI.toFloat() * 0.5f + (rand.nextFloat() - 0.5f) * 0.5f
                        snail.stateTimer = 24f + rand.nextFloat() * 20f
                    }
                }
            }
        }

        // E. Red Cherry Shrimps (3D depth-scaled grazing & hopping across stones, spiderwood & moss!)
        for (s in shrimps) {
            s.phase += dt * 6.0f
            if (s.hopProgress > 0f) {
                s.hopProgress += dt * 1.25f
                val u = s.hopProgress.coerceIn(0f, 1f)
                s.zDepth = s.hopStartZ + (s.hopEndZ - s.hopStartZ) * u
                if (s.hopProgress >= 1f) {
                    s.hopProgress = 0f
                    s.zDepth = evalShrimpHomeZ(s.homeKind)
                }
                continue
            }

            val sx = evalShrimpHomeX(s.homeKind, s.t)
            val sy = evalShrimpHomeY(s.homeKind, s.t)
            var startled = false
            for (i in 0 until activeBettaCount) {
                if (hypot(bettas[i].nx[0] - sx, bettas[i].ny[0] - sy) < tankW * 0.085f) {
                    startled = true; break
                }
            }
            if (startled || rand.nextFloat() < dt * 0.055f) {
                s.hopStartX = sx; s.hopStartY = sy; s.hopStartZ = s.zDepth
                if ((s.homeKind == 0 || s.homeKind == 1) && rand.nextFloat() < 0.6f) {
                    s.t = (s.t + (if (rand.nextBoolean()) 0.20f else -0.20f)).coerceIn(0.16f, 0.84f)
                } else {
                    s.homeKind = rand.nextInt(6)
                    s.t = 0.20f + rand.nextFloat() * 0.60f
                }
                s.hopEndX = evalShrimpHomeX(s.homeKind, s.t)
                s.hopEndY = evalShrimpHomeY(s.homeKind, s.t)
                s.hopEndZ = evalShrimpHomeZ(s.homeKind)
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
                aqPal.shelfTop = 0xFF0A0E12.toInt(); aqPal.shelfFront = 0xFF000000.toInt(); aqPal.shelfGrain = 0xFF151C22.toInt()
                aqPal.lampBody = 0xFF162028.toInt(); aqPal.lampGlow = 0xFF8CEBFF.toInt()
                aqPal.beamTop = 0x2266D8F0; aqPal.beamBot = 0x00000000
                aqPal.waterTop = 0xFF03080B.toInt(); aqPal.waterMid = 0xFF010406.toInt(); aqPal.waterBot = 0xFF000000.toInt()
                aqPal.fogColor = 0xFF020608.toInt(); aqPal.tanninColor = 0x00000000
                aqPal.glassEdge = 0x9974DCF0.toInt(); aqPal.glassTint = 0x104AB8CE
                aqPal.contour = 0x6E5CB4C8
                aqPal.soilBack = 0xFF0B1114.toInt(); aqPal.sandTop = 0xFF151E23.toInt(); aqPal.sandBot = 0xFF06090B.toInt()
                aqPal.caustic = 0x1E6AD8EE
                aqPal.woodDark = 0xFF18110E.toInt(); aqPal.woodMid = 0xFF281C16.toInt(); aqPal.woodRim = 0xFF4F3E35.toInt()
                aqPal.rotalaStem = 0xFF163834.toInt(); aqPal.rotalaTip = 0xFF5A3846.toInt()
                aqPal.plantBack = 0xFF102E2C.toInt(); aqPal.plantMid = 0xFF143C36.toInt(); aqPal.plantFront = 0xFF194A42.toInt()
                aqPal.fernDark = 0xFF123632.toInt(); aqPal.fernHi = 0xFF225C52.toInt()
                aqPal.anubias = 0xFF143E38.toInt(); aqPal.anubiasHi = 0xFF2B6F64.toInt()
                aqPal.cryptBronze = 0xFF3A2820.toInt(); aqPal.cryptOlive = 0xFF223C2C.toInt()
                aqPal.moss = 0xFF133629.toInt(); aqPal.mossHi = 0xFF28604A.toInt(); aqPal.catappa = 0xFF3A2418.toInt()
                aqPal.stoneDark = 0xFF121C21.toInt(); aqPal.stoneMid = 0xFF1C2A31.toInt(); aqPal.stoneHi = 0xFF2B3E47.toInt()
                aqPal.catBody = 0xFF05070B.toInt(); aqPal.catRim = 0xAA7EE0F4.toInt(); aqPal.catEye = 0xFFFFD65E.toInt()
                aqPal.moteColor = 0x3D82E0F4; aqPal.bubbleColor = 0x999AECFC.toInt()
            }
            snapshot.isNight -> {
                aqPal.roomTop = 0xFF070E1A.toInt(); aqPal.roomBot = 0xFF0F1B2C.toInt()
                aqPal.shelfTop = 0xFF241D1A.toInt(); aqPal.shelfFront = 0xFF130F0D.toInt(); aqPal.shelfGrain = 0xFF302621.toInt()
                aqPal.lampBody = 0xFF1A2634.toInt(); aqPal.lampGlow = 0xFFB8F2FF.toInt()
                aqPal.beamTop = 0x567AD6F0; aqPal.beamBot = 0x05226C8E
                aqPal.waterTop = 0xFF215165.toInt(); aqPal.waterMid = 0xFF133447.toInt(); aqPal.waterBot = 0xFF091E2C.toInt()
                aqPal.fogColor = 0xFF143447.toInt(); aqPal.tanninColor = 0x122A86A8
                aqPal.glassEdge = 0x8CA2E4F5.toInt(); aqPal.glassTint = 0x1C68C2DC
                aqPal.contour = 0x9E07121A.toInt()
                aqPal.soilBack = 0xFF1B242B.toInt(); aqPal.sandTop = 0xFF42515C.toInt(); aqPal.sandBot = 0xFF26323A.toInt()
                aqPal.caustic = 0x2694E6FA
                aqPal.woodDark = 0xFF241813.toInt(); aqPal.woodMid = 0xFF3A281F.toInt(); aqPal.woodRim = 0xFF604A3F.toInt()
                aqPal.rotalaStem = 0xFF1D4646.toInt(); aqPal.rotalaTip = 0xFF563B4C.toInt()
                aqPal.plantBack = 0xFF184241.toInt(); aqPal.plantMid = 0xFF143737.toInt(); aqPal.plantFront = 0xFF0E2B2E.toInt()
                aqPal.fernDark = 0xFF123A38.toInt(); aqPal.fernHi = 0xFF245A55.toInt()
                aqPal.anubias = 0xFF103234.toInt(); aqPal.anubiasHi = 0xFF27585B.toInt()
                aqPal.cryptBronze = 0xFF3E2D28.toInt(); aqPal.cryptOlive = 0xFF224236.toInt()
                aqPal.moss = 0xFF173D34.toInt(); aqPal.mossHi = 0xFF2C5F4F.toInt(); aqPal.catappa = 0xFF472E20.toInt()
                aqPal.stoneDark = 0xFF202E35.toInt(); aqPal.stoneMid = 0xFF2E4049.toInt(); aqPal.stoneHi = 0xFF435862.toInt()
                aqPal.catBody = 0xFF0B0F17.toInt(); aqPal.catRim = 0x8E92E2F6.toInt(); aqPal.catEye = 0xFFFFD562.toInt()
                aqPal.moteColor = 0x3DAAEAFA; aqPal.bubbleColor = 0x8ABCEEFB.toInt()
            }
            isDusk -> {
                aqPal.roomTop = 0xFF262D3E.toInt(); aqPal.roomBot = 0xFF48404E.toInt()
                aqPal.shelfTop = 0xFF4D3A2D.toInt(); aqPal.shelfFront = 0xFF2F221A.toInt(); aqPal.shelfGrain = 0xFF614A3B.toInt()
                aqPal.lampBody = 0xFF252B36.toInt(); aqPal.lampGlow = 0xFFFFE6B4.toInt()
                aqPal.beamTop = 0x74FFD48C; aqPal.beamBot = 0x085CA2A0
                aqPal.waterTop = 0xFF90BDB5.toInt(); aqPal.waterMid = 0xFF4E8285.toInt(); aqPal.waterBot = 0xFF284D56.toInt()
                aqPal.fogColor = 0xFF457579.toInt(); aqPal.tanninColor = 0x2AE4964A
                aqPal.glassEdge = 0xA0F6EAD4.toInt(); aqPal.glassTint = 0x2482BAB8
                aqPal.contour = 0x8C101C22.toInt()
                aqPal.soilBack = 0xFF3B3229.toInt(); aqPal.sandTop = 0xFFC2AC8C.toInt(); aqPal.sandBot = 0xFF8C765B.toInt()
                aqPal.caustic = 0x3AFFDEA5
                aqPal.woodDark = 0xFF3A261A.toInt(); aqPal.woodMid = 0xFF5D3F2C.toInt(); aqPal.woodRim = 0xFF946A4B.toInt()
                aqPal.rotalaStem = 0xFF3E6C56.toInt(); aqPal.rotalaTip = 0xFFAA5B4C.toInt()
                aqPal.plantBack = 0xFF396B59.toInt(); aqPal.plantMid = 0xFF295848.toInt(); aqPal.plantFront = 0xFF1D4538.toInt()
                aqPal.fernDark = 0xFF225241.toInt(); aqPal.fernHi = 0xFF3E785D.toInt()
                aqPal.anubias = 0xFF1E4A3B.toInt(); aqPal.anubiasHi = 0xFF3E745C.toInt()
                aqPal.cryptBronze = 0xFF6B4332.toInt(); aqPal.cryptOlive = 0xFF3D5E40.toInt()
                aqPal.moss = 0xFF2D563A.toInt(); aqPal.mossHi = 0xFF4C7F54.toInt(); aqPal.catappa = 0xFF7E4D29.toInt()
                aqPal.stoneDark = 0xFF364448.toInt(); aqPal.stoneMid = 0xFF4B5C60.toInt(); aqPal.stoneHi = 0xFF657A7D.toInt()
                aqPal.catBody = 0xFF161A22.toInt(); aqPal.catRim = 0x88FFD696.toInt(); aqPal.catEye = 0xFFF9BC44.toInt()
                aqPal.moteColor = 0x45FFE8BE; aqPal.bubbleColor = 0x94F2F6F0.toInt()
            }
            else -> { // Crisp Sunlit Nature Aquarium Daytime
                aqPal.roomTop = 0xFFAEC3C9.toInt(); aqPal.roomBot = 0xFFC9D8DC.toInt()
                aqPal.shelfTop = 0xFFA48E78.toInt(); aqPal.shelfFront = 0xFF7A6450.toInt(); aqPal.shelfGrain = 0xFFB59F88.toInt()
                aqPal.lampBody = 0xFF3D4B53.toInt(); aqPal.lampGlow = 0xFFFFFDF4.toInt()
                aqPal.beamTop = 0x7CFFFAE0; aqPal.beamBot = 0x06B0E0DA
                aqPal.waterTop = 0xFFB6DED8.toInt(); aqPal.waterMid = 0xFF82BCB5.toInt(); aqPal.waterBot = 0xFF528E89.toInt()
                aqPal.fogColor = 0xFF72B1AB.toInt(); aqPal.tanninColor = 0x22DCAD6B
                aqPal.glassEdge = 0xC6F8FFFD.toInt(); aqPal.glassTint = 0x2EB6E0DC
                aqPal.contour = 0x781C3034
                aqPal.soilBack = 0xFF4E4337.toInt(); aqPal.sandTop = 0xFFE5D7BE.toInt(); aqPal.sandBot = 0xFFBEA989.toInt()
                aqPal.caustic = 0x54FFFCE8
                aqPal.woodDark = 0xFF4A3324.toInt(); aqPal.woodMid = 0xFF70503A.toInt(); aqPal.woodRim = 0xFFA17B5D.toInt()
                aqPal.rotalaStem = 0xFF4C8664.toInt(); aqPal.rotalaTip = 0xFFD4745E.toInt()
                aqPal.plantBack = 0xFF4F8569.toInt(); aqPal.plantMid = 0xFF386F54.toInt(); aqPal.plantFront = 0xFF275941.toInt()
                aqPal.fernDark = 0xFF2D684B.toInt(); aqPal.fernHi = 0xFF54946C.toInt()
                aqPal.anubias = 0xFF285D42.toInt(); aqPal.anubiasHi = 0xFF4E8E67.toInt()
                aqPal.cryptBronze = 0xFF7E523B.toInt(); aqPal.cryptOlive = 0xFF486F48.toInt()
                aqPal.moss = 0xFF3A6C44.toInt(); aqPal.mossHi = 0xFF609662.toInt(); aqPal.catappa = 0xFF98653A.toInt()
                aqPal.stoneDark = 0xFF4B5D5C.toInt(); aqPal.stoneMid = 0xFF657976.toInt(); aqPal.stoneHi = 0xFF869B96.toInt()
                aqPal.catBody = 0xFF1F242C.toInt(); aqPal.catRim = 0x75FFF5DC; aqPal.catEye = 0xFFF3B43A.toInt()
                aqPal.moteColor = 0x55FFFCEB; aqPal.bubbleColor = 0xB0FFFFFF.toInt()
            }
        }

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
    // ROOM, OVERHEAD LED LAMP & AMANO BIOTOPE LAYERS
    // =========================================================================

    private fun drawRoomAndLamp(canvas: Canvas, enableBatteryIndicator: Boolean, isCharging: Boolean) {
        val w = canvas.width.toFloat()
        val h = canvas.height.toFloat()

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

            // Soft Ambient LED Wall Halo glowing behind the lamp & upper rim (keeps clock space serene!)
            val haloX = w * 0.5f
            val haloY = (lampY + tankY) * 0.52f
            val haloR = (tankW * 0.78f).coerceAtLeast(140f)
            val haloCol = withAlpha(aqPal.lampGlow, if (aqPal.isNight) 38 else if (aqPal.isDaylight) 44 else 52)
            if (wallHaloGrad == null || abs(cachedHaloX - haloX) > 2f || abs(cachedHaloY - haloY) > 2f ||
                abs(cachedHaloR - haloR) > 2f || cachedHaloCol != haloCol
            ) {
                cachedHaloX = haloX; cachedHaloY = haloY; cachedHaloR = haloR; cachedHaloCol = haloCol
                wallHaloGrad = RadialGradient(haloX, haloY, haloR, haloCol, 0x00000000, Shader.TileMode.CLAMP)
            }
            gradPaint.shader = wallHaloGrad
            canvas.drawCircle(haloX, haloY, haloR, gradPaint)
        }

        // Minimalist warm wood tabletop shelf with subtle grain lines, contact shadow & glowing tank light bounce!
        val shelfTopY = tankY + tankH - 2f
        val shelfThick = (h * 0.019f).coerceIn(10f, 22f)
        fillPaint.color = aqPal.shelfTop
        canvas.drawRect(0f, shelfTopY, w, shelfTopY + shelfThick, fillPaint)
        fillPaint.color = aqPal.shelfFront
        canvas.drawRect(0f, shelfTopY + shelfThick, w, h, fillPaint)
        if (!aqPal.isAmoled) {
            strokePaint.color = aqPal.shelfGrain
            strokePaint.strokeWidth = 1.0f
            canvas.drawLine(0f, shelfTopY + shelfThick * 0.48f, w, shelfTopY + shelfThick * 0.48f, strokePaint)
            // Soft tabletop light bounce cast by the illuminated aquarium onto the polished wood shelf!
            fillPaint.color = withAlpha(aqPal.waterTop, if (aqPal.isNight) 44 else 34)
            canvas.drawOval(tankX + tankW * 0.04f, shelfTopY + 1f, tankX + tankW * 0.96f, shelfTopY + shelfThick * 1.15f, fillPaint)
            fillPaint.color = 0x36000000
            canvas.drawRect(tankX + 2f, shelfTopY, tankX + tankW - 2f, shelfTopY + 4.5f, fillPaint)
        }

        // Suspended Twin-Wire Architectural LED Bar + Volumetric Light Cone
        val lampX = (w - lampW) * 0.5f
        val lampThick = (tankH * 0.020f).coerceIn(7f, 14f)
        strokePaint.color = if (aqPal.isAmoled) 0x555E7A86 else aqPal.contour
        strokePaint.strokeWidth = (w * 0.0022f).coerceIn(1.1f, 2.2f)
        canvas.drawLine(lampX + lampW * 0.11f, 0f, lampX + lampW * 0.11f, lampY, strokePaint)
        canvas.drawLine(lampX + lampW * 0.89f, 0f, lampX + lampW * 0.89f, lampY, strokePaint)

        // Small cable mounts on top of LED fixture
        fillPaint.color = aqPal.lampBody
        canvas.drawRect(lampX + lampW * 0.11f - 3f, lampY - 4f, lampX + lampW * 0.11f + 3f, lampY, fillPaint)
        canvas.drawRect(lampX + lampW * 0.89f - 3f, lampY - 4f, lampX + lampW * 0.89f + 3f, lampY, fillPaint)

        val beamY0 = lampY + lampThick * 0.85f
        val beamY1 = tankY + tankH * 0.72f
        val pulseBoost = if (lampPulseTimer > 0f) (sin((lampPulseTimer / 1.8f) * PI.toFloat()).coerceAtLeast(0f) * 44f).toInt() else 0
        val effectiveBeamTop = if (pulseBoost > 0) {
            (aqPal.beamTop and 0x00FFFFFF) or (((aqPal.beamTop ushr 24) + pulseBoost).coerceAtMost(215) shl 24)
        } else aqPal.beamTop

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

        fillPaint.color = aqPal.lampBody
        canvas.drawRoundRect(lampX, lampY, lampX + lampW, lampY + lampThick, 3f, 3f, fillPaint)
        fillPaint.color = aqPal.lampGlow
        canvas.drawRect(lampX + 6f, lampY + lampThick * 0.68f, lampX + lampW - 6f, lampY + lampThick, fillPaint)

        if (enableBatteryIndicator && isCharging) {
            val pulse = 0.5f + 0.5f * sin(simTime * 2.6f)
            fillPaint.color = (0x006CE8B8) or ((120 + (pulse * 125f).toInt()).coerceIn(90, 245) shl 24)
            canvas.drawCircle(lampX + lampW - 14f, lampY + lampThick * 0.36f, (lampThick * 0.24f).coerceAtLeast(2.2f), fillPaint)
        }
    }

    private fun drawWaterAndDappledRays(canvas: Canvas, waterClarity: Float, isBatterySaver: Boolean) {
        val botY = tankY + tankH
        if (waterGrad == null || abs(cachedWaterY - waterY) > 2f || abs(cachedWaterBotY - botY) > 2f ||
            cachedWaterTop != aqPal.waterTop || cachedWaterMid != aqPal.waterMid || cachedWaterBot != aqPal.waterBot
        ) {
            cachedWaterY = waterY; cachedWaterBotY = botY
            cachedWaterTop = aqPal.waterTop; cachedWaterMid = aqPal.waterMid; cachedWaterBot = aqPal.waterBot
            waterGrad = LinearGradient(
                0f, waterY, 0f, botY,
                intArrayOf(aqPal.waterTop, aqPal.waterMid, aqPal.waterBot),
                floatArrayOf(0f, 0.50f, 1f),
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

        if (!aqPal.isAmoled && (aqPal.tanninColor ushr 24) > 0) {
            fillPaint.color = aqPal.tanninColor
            canvas.drawRect(tankX, waterY, tankX + tankW, botY, fillPaint)
        }

        // 3D Interior Back-Glass Perspective Lines (receding rimless silicone corner seams!)
        val backInsetX = tankW * 0.052f
        val backTopY = tankY + tankH * 0.020f
        val backBotY = sandY - tankH * 0.072f
        strokePaint.color = withAlpha(aqPal.glassEdge, if (aqPal.isAmoled) 48 else 62)
        strokePaint.strokeWidth = 1.05f
        // Recessed rear top glass rim + corner perspective diagonals + faint rear vertical silicone seams
        canvas.drawLine(tankX + backInsetX, backTopY, tankX + tankW - backInsetX, backTopY, strokePaint)
        canvas.drawLine(tankX, tankY, tankX + backInsetX, backTopY, strokePaint)
        canvas.drawLine(tankX + tankW, tankY, tankX + tankW - backInsetX, backTopY, strokePaint)
        strokePaint.color = withAlpha(aqPal.glassEdge, if (aqPal.isAmoled) 32 else 42)
        canvas.drawLine(tankX + backInsetX, backTopY, tankX + backInsetX, backBotY, strokePaint)
        canvas.drawLine(tankX + tankW - backInsetX, backTopY, tankX + tankW - backInsetX, backBotY, strokePaint)

        // Dappled Underwater Sun/Moon Shafts linked dynamically to the open gaps between drifting Floating Frogbit!
        if (!aqPal.isAmoled && waterClarity > 0.42f) {
            val shaftAlpha = if (aqPal.isNight) 16 else if (aqPal.isDaylight) 30 else 24
            fillPaint.color = (if (aqPal.isNight) 0x0082DAF0 else 0x00FFFAE0) or (shaftAlpha shl 24)
            // Compute open sunlit windows between the 2 floating frogbit clusters (left gap, center open window, right gap)
            val leftClusterEnd = floaterLiveFracs[2] + 0.032f
            val rightClusterStart = floaterLiveFracs[3] - 0.032f
            val rightClusterEnd = floaterLiveFracs[5] + 0.032f
            val rayStarts = floatArrayOf(0.06f, leftClusterEnd, (leftClusterEnd + rightClusterStart) * 0.5f, rightClusterEnd)
            val rayWidths = floatArrayOf(0.075f, (rightClusterStart - leftClusterEnd) * 0.42f, (rightClusterStart - leftClusterEnd) * 0.36f, 0.085f)
            val rayCount = if (isBatterySaver) 2 else rayStarts.size
            for (r in 0 until rayCount) {
                val rx = tankX + tankW * rayStarts[r]
                val topW = (tankW * rayWidths[r]).coerceIn(tankW * 0.04f, tankW * 0.14f)
                val slant = (rayStarts[r] - 0.48f) * tankW * 0.22f
                shapePath.reset()
                shapePath.moveTo(rx, waterY)
                shapePath.lineTo(rx + topW, waterY)
                shapePath.lineTo(rx + topW * 1.65f + slant, sandY)
                shapePath.lineTo(rx + slant * 0.65f, sandY)
                shapePath.close()
                canvas.drawPath(shapePath, fillPaint)
            }
            // Subtle shaded bands directly beneath the two dense floating Frogbit canopies!
            fillPaint.color = 0x16000000
            val sh1L = tankX + tankW * (floaterLiveFracs[0] - 0.02f)
            val sh1R = tankX + tankW * (floaterLiveFracs[2] + 0.02f)
            canvas.drawRect(sh1L, waterY, sh1R, sandY, fillPaint)
            val sh2L = tankX + tankW * (floaterLiveFracs[3] - 0.02f)
            val sh2R = tankX + tankW * (floaterLiveFracs[5] + 0.02f)
            canvas.drawRect(sh2L, waterY, sh2R, sandY, fillPaint)
        }

        if (!isBatterySaver) {
            fillPaint.color = aqPal.moteColor
            val waterSpan = (sandY - waterY).coerceAtLeast(60f)
            for (m in motes) {
                canvas.drawCircle(tankX + m.nx * tankW, waterY + m.ny * waterSpan, m.r, fillPaint)
            }
        }
    }

    /**
     * Silvery Underside Meniscus Reflection (Total Internal Reflection!)
     * Shows the shimmering water-surface underside plane between rear meniscus & front meniscus,
     * softly mirroring upper-swimming Bettas & floating pads!
     */
    private fun drawUndersideMeniscusReflections(canvas: Canvas, activeBettaCount: Int) {
        if (aqPal.isAmoled) return
        val backInsetX = tankW * 0.052f
        val backWaterY = waterY - tankH * 0.017f
        shapePath.reset()
        shapePath.moveTo(tankX + backInsetX, backWaterY)
        shapePath.lineTo(tankX + tankW - backInsetX, backWaterY)
        shapePath.lineTo(tankX + tankW, waterY)
        shapePath.lineTo(tankX, waterY)
        shapePath.close()
        fillPaint.color = withAlpha(aqPal.lampGlow, if (aqPal.isNight) 28 else 42)
        canvas.drawPath(shapePath, fillPaint)

        // Shimmering inverted reflections of Bettas gliding near the top surface!
        val waterSpan = (sandY - waterY).coerceAtLeast(60f)
        for (i in 0 until activeBettaCount) {
            val b = bettas[i]
            val distSurf = (b.ny[0] - waterY) / waterSpan
            if (distSurf in 0.01f..0.30f) {
                val alpha = (((1f - distSurf / 0.30f) * 58f).toInt()).coerceIn(8, 62)
                val rx = tankW * 0.052f * perspScale(b.zDepth)
                val ry = (tankH * 0.008f).coerceIn(2f, 5.5f)
                val drift = sin(simTime * 2.6f + i * 1.5f) * (rx * 0.14f)
                val ryPos = waterY + distSurf * tankH * 0.032f
                fillPaint.color = withAlpha(b.finMid, alpha)
                canvas.drawOval(b.nx[2] + drift - rx, ryPos - ry, b.nx[2] + drift + rx, ryPos + ry, fillPaint)
            }
        }
    }

    /**
     * Soft Sculpted Sand Drop-Shadows beneath every swimming Betta & Corydoras!
     * Offset subtly away from the overhead LED center and softening with elevation above the sand.
     */
    private fun drawCreatureSandShadows(canvas: Canvas, activeBettaCount: Int, enableCleaners: Boolean) {
        if (aqPal.isAmoled) return
        val centerX = tankX + tankW * 0.5f
        val waterSpan = (sandY - waterY).coerceAtLeast(60f)

        for (i in 0 until activeBettaCount) {
            val b = bettas[i]
            val floorY = sandFloorYAtZ(b.zDepth)
            val heightFrac = ((floorY - b.ny[2]) / waterSpan).coerceIn(0f, 1f)
            val shadowX = b.nx[2] + (b.nx[2] - centerX) * heightFrac * 0.16f
            val rx = tankW * 0.055f * perspScale(b.zDepth) * (1.0f + heightFrac * 0.32f)
            val ry = rx * 0.21f
            val alpha = ((1f - heightFrac * 0.68f) * (1f - b.zDepth * 0.35f) * 52f).toInt().coerceIn(8, 56)
            fillPaint.color = alpha shl 24
            canvas.drawOval(shadowX - rx, floorY - ry, shadowX + rx, floorY + ry, fillPaint)
        }

        if (enableCleaners) {
            for (c in corys) {
                if (c.dashPhase > 0.15f && c.dashPhase < 0.85f) continue
                val cx = tankX + c.normX * tankW
                val floorY = sandFloorYAtZ(c.zDepth)
                val rx = tankW * 0.030f * perspScale(c.zDepth)
                val ry = rx * 0.24f
                fillPaint.color = 0x36000000
                canvas.drawOval(cx - rx, floorY - ry, cx + rx, floorY + ry, fillPaint)
            }
        }
    }

    /** Subtle Radial Depth Vignette & Shadowed Spiderwood Arch Hollow for Backlit Diorama Pop! */
    private fun drawInteriorDepthVignette(canvas: Canvas) {
        if (aqPal.isAmoled) return
        val vx = tankX + tankW * 0.50f
        val vy = tankY + tankH * 0.38f
        val vr = max(tankW, tankH) * 0.78f
        if (interiorVignetteGrad == null || abs(cachedVigX - vx) > 2f || abs(cachedVigY - vy) > 2f || abs(cachedVigR - vr) > 2f) {
            cachedVigX = vx; cachedVigY = vy; cachedVigR = vr
            interiorVignetteGrad = RadialGradient(
                vx, vy, vr,
                intArrayOf(0x00000000, 0x00000000, 0x3A040C12),
                floatArrayOf(0f, 0.58f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        gradPaint.shader = interiorVignetteGrad
        canvas.drawRect(tankX, tankY, tankX + tankW, tankY + tankH, gradPaint)
    }

    /** Minimalist Authentic ADA Nature Aquarium Hardware: Glass Lily-Pipe Outflow & Slim Thermometer! */
    private fun drawAdaGlassHardware(canvas: Canvas) {
        // 1. Curved Glass Lily-Pipe Outflow over the upper-left rim (creates the gentle circular tank flow!)
        val pipeX = tankX + tankW * 0.048f
        val pipeDipY = waterY + tankH * 0.072f
        val glassStroke = withAlpha(aqPal.glassEdge, if (aqPal.isAmoled) 110 else 135)
        strokePaint.color = glassStroke
        strokePaint.strokeWidth = (tankW * 0.008f).coerceIn(2.6f, 5.5f)
        detailPath.reset()
        detailPath.moveTo(pipeX, tankY)
        detailPath.lineTo(pipeX, pipeDipY)
        detailPath.quadTo(pipeX, pipeDipY + tankH * 0.032f, pipeX + tankW * 0.042f, pipeDipY + tankH * 0.022f)
        canvas.drawPath(detailPath, strokePaint)

        // Flared translucent tulip lily-pipe mouth
        val mouthX = pipeX + tankW * 0.056f
        val mouthY = pipeDipY + tankH * 0.018f
        val mrX = tankW * 0.018f; val mrY = tankH * 0.015f
        fillPaint.color = withAlpha(aqPal.glassEdge, 48)
        canvas.drawOval(mouthX - mrX, mouthY - mrY, mouthX + mrX, mouthY + mrY, fillPaint)
        strokePaint.strokeWidth = 1.15f
        canvas.drawOval(mouthX - mrX, mouthY - mrY, mouthX + mrX, mouthY + mrY, strokePaint)

        // 2. Subtle Slim Glass Hang-On Thermometer on upper-right inner glass pane
        val thX = tankX + tankW * 0.948f
        val thTop = tankY + tankH * 0.05f
        val thBot = waterY + tankH * 0.16f
        val thHw = (tankW * 0.0055f).coerceIn(1.8f, 3.8f)
        fillPaint.color = withAlpha(aqPal.glassEdge, 58)
        canvas.drawRoundRect(thX - thHw, thTop, thX + thHw, thBot, thHw, thHw, fillPaint)
        strokePaint.color = withAlpha(aqPal.glassEdge, 125)
        strokePaint.strokeWidth = 0.9f
        canvas.drawRoundRect(thX - thHw, thTop, thX + thHw, thBot, thHw, thHw, strokePaint)
        // Tiny red alcohol bulb + thin column
        fillPaint.color = 0xCCD84C42.toInt()
        canvas.drawCircle(thX, thBot - thHw * 1.1f, thHw * 0.85f, fillPaint)
        strokePaint.color = 0xBBD84C42.toInt()
        strokePaint.strokeWidth = (thHw * 0.45f).coerceAtLeast(0.9f)
        canvas.drawLine(thX, thBot - thHw * 1.5f, thX, thTop + (thBot - thTop) * 0.45f, strokePaint)
    }

    /**
     * Background Flora (z = 0.88..0.92, softly blended into water haze):
     * - Red-tipped Rotala rotundifolia stem forest on the high right aquasoil slope
     * - Tall distant Vallisneria ribbons framing the rear corners
     */
    private fun drawBackgroundFlora(canvas: Canvas, isBatterySaver: Boolean) {
        val fog = depthFog(0.88f)
        val rotStemCol = TimeOfDayPalette.lerpColor(aqPal.rotalaStem, aqPal.fogColor, fog) or (0xFF shl 24)
        val rotTipCol = TimeOfDayPalette.lerpColor(aqPal.rotalaTip, aqPal.fogColor, fog * 0.7f) or (0xFF shl 24)
        val backValCol = TimeOfDayPalette.lerpColor(aqPal.plantBack, aqPal.fogColor, fog * 0.85f) or (0xFF shl 24)

        // 1. Tall distant Vallisneria framing left & rear-right
        drawVallisneriaGroup(canvas, tankX + tankW * 0.13f, tankH * 0.64f, 5, backValCol, 0.75f, 0.85f)
        drawVallisneriaGroup(canvas, tankX + tankW * 0.90f, tankH * 0.62f, 4, backValCol, 0.72f, 0.85f)

        // 2. Red-tipped Rotala rotundifolia stem cluster (classic Amano background forest!)
        val stemFracs = floatArrayOf(0.64f, 0.69f, 0.74f, 0.79f, 0.84f, 0.21f, 0.25f)
        val stemHeights = floatArrayOf(0.54f, 0.62f, 0.68f, 0.65f, 0.58f, 0.46f, 0.40f)
        val stemLimit = if (isBatterySaver) 4 else stemFracs.size
        val leafRx = (tankW * 0.015f).coerceIn(4f, 11f)
        val leafRy = leafRx * 0.44f

        for (s in 0 until stemLimit) {
            val baseX = tankX + tankW * stemFracs[s]
            val baseY = sandY - tankH * 0.055f
            val hPx = tankH * stemHeights[s]
            val sway = sin(simTime * 0.95f + s * 0.9f) * (tankW * 0.022f)

            strokePaint.color = rotStemCol
            strokePaint.strokeWidth = (tankW * 0.0028f).coerceIn(1.0f, 2.0f)
            detailPath.reset()
            detailPath.moveTo(baseX, baseY)
            detailPath.quadTo(baseX + sway * 0.45f, baseY - hPx * 0.55f, baseX + sway, baseY - hPx)
            canvas.drawPath(detailPath, strokePaint)

            // Paired opposite oval Rotala leaves blushing warm rose-copper toward the LED light!
            val pairs = if (isBatterySaver) 6 else 9
            for (p in 1..pairs) {
                val u = p.toFloat() / pairs.toFloat()
                val sx = baseX + sway * (u * u * 0.55f + u * 0.45f)
                val sy = baseY - hPx * u
                val col = TimeOfDayPalette.lerpColor(rotStemCol, rotTipCol, ((u - 0.35f) / 0.65f).coerceIn(0f, 1f))
                fillPaint.color = col
                val lScale = 0.72f + (1f - abs(u - 0.65f)) * 0.38f
                val rx = leafRx * lScale
                val ry = leafRy * lScale
                canvas.save()
                canvas.translate(sx, sy)
                canvas.rotate(-22f)
                canvas.drawOval(-rx * 1.9f, -ry, -rx * 0.1f, ry, fillPaint)
                canvas.rotate(44f)
                canvas.drawOval(rx * 0.1f, -ry, rx * 1.9f, ry, fillPaint)
                canvas.restore()
            }
        }
    }

    /**
     * Sloped Takashi Amano Substrate Cross-Section:
     * - Dark nutrient aquasoil hill rising high on the right/back for optical depth
     * - Sweeping pale cosmetic river sand foreground path with stippled pebbles & dancing web caustics!
     */
    private fun drawGradedSubstrateAndCaustics(canvas: Canvas, isBatterySaver: Boolean) {
        val botY = tankY + tankH

        // 1. Elevated Dark Aquasoil Back-Hill (sloping up toward back-right & back-left corners!)
        shapePath.reset()
        shapePath.moveTo(tankX, sandY - tankH * 0.045f)
        shapePath.quadTo(tankX + tankW * 0.28f, sandY - tankH * 0.015f, tankX + tankW * 0.52f, sandY - tankH * 0.055f)
        shapePath.quadTo(tankX + tankW * 0.78f, sandY - tankH * 0.125f, tankX + tankW, sandY - tankH * 0.105f)
        shapePath.lineTo(tankX + tankW, botY)
        shapePath.lineTo(tankX, botY)
        shapePath.close()
        fillPaint.color = aqPal.soilBack
        canvas.drawPath(shapePath, fillPaint)

        // 2. Foreground Sweeping Cosmetic River Sand Valley
        val sY0 = sandY - tankH * 0.035f
        if (sandGrad == null || abs(cachedSandY0 - sY0) > 2f || abs(cachedSandY1 - botY) > 2f ||
            cachedSandTop != aqPal.sandTop || cachedSandBot != aqPal.sandBot
        ) {
            cachedSandY0 = sY0; cachedSandY1 = botY
            cachedSandTop = aqPal.sandTop; cachedSandBot = aqPal.sandBot
            sandGrad = LinearGradient(0f, sY0, 0f, botY, aqPal.sandTop, aqPal.sandBot, Shader.TileMode.CLAMP)
        }
        shapePath.reset()
        shapePath.moveTo(tankX, sandY + tankH * 0.008f)
        shapePath.quadTo(tankX + tankW * 0.30f, sandY - tankH * 0.032f, tankX + tankW * 0.62f, sandY + tankH * 0.002f)
        shapePath.quadTo(tankX + tankW * 0.84f, sandY - tankH * 0.018f, tankX + tankW, sandY + tankH * 0.010f)
        shapePath.lineTo(tankX + tankW, botY)
        shapePath.lineTo(tankX, botY)
        shapePath.close()
        gradPaint.shader = sandGrad
        canvas.drawPath(shapePath, gradPaint)

        // Subtle dark gravel grain along the sand-to-soil border
        if (!aqPal.isAmoled) {
            fillPaint.color = withAlpha(aqPal.stoneDark, 110)
            for (g in 0 until 14) {
                val gx = tankX + tankW * (0.06f + g * 0.068f)
                val gy = sandY + tankH * (0.005f + ((g * 29) % 5) * 0.007f)
                val gr = (tankW * (0.0032f + (g % 3) * 0.0014f)).coerceIn(1.2f, 3.2f)
                canvas.drawOval(gx - gr * 1.4f, gy - gr * 0.7f, gx + gr * 1.4f, gy + gr * 0.7f, fillPaint)
            }
        }

        // Shimmering Web-like Caustic Light Rings dancing on the pale sand!
        if (!isBatterySaver) {
            strokePaint.color = aqPal.caustic
            strokePaint.strokeWidth = (tankW * 0.0032f).coerceIn(1.1f, 2.3f)
            for (c in 0 until 6) {
                val cx = tankX + tankW * (0.11f + c * 0.155f) + sin(simTime * 1.1f + c * 1.35f) * (tankW * 0.025f)
                val cy = sandY + tankH * (0.024f + (c % 2) * 0.018f)
                val rx = tankW * (0.044f + 0.010f * sin(simTime + c))
                val ry = rx * 0.22f
                canvas.drawOval(cx - rx, cy - ry, cx + rx, cy + ry, strokePaint)
            }
        }
    }

    /**
     * Midground Hardscape Island (z = 0.54):
     * - Layered Seiryu / river stone base with soft contact shadows
     * - Gnarled 3-branch Asian Spiderwood centerpiece with bark grain & Java Moss cushions
     * - Java Fern lance fronds + Broad-leaf Anubias Betta hammocks + Glass Diffuser
     */
    private fun drawMidgroundHardscape(canvas: Canvas, isBatterySaver: Boolean) {
        // 1. Midground Stone Anchor Cluster supporting the Spiderwood Base (Golden-Ratio ~68% right!)
        drawSculptedStone(canvas, tankX + tankW * 0.76f, sandY - tankH * 0.042f, tankW * 0.095f, tankH * 0.052f)
        drawSculptedStone(canvas, tankX + tankW * 0.62f, sandY - tankH * 0.018f, tankW * 0.074f, tankH * 0.038f)
        drawSculptedStone(canvas, tankX + tankW * 0.86f, sandY - tankH * 0.068f, tankW * 0.068f, tankH * 0.040f)

        // 2. Sculpted Multi-Branch Spiderwood Root Arch (forming a natural dark cave underneath!)
        drawTaperedWoodCurve(canvas, 2, tankW * 0.016f, tankW * 0.005f, aqPal.woodDark)
        drawTaperedWoodCurve(canvas, 0, tankW * 0.024f, tankW * 0.0055f, aqPal.woodMid)
        drawTaperedWoodCurve(canvas, 1, tankW * 0.015f, tankW * 0.0040f, aqPal.woodRim)

        // Epiphytic Velvety Java Moss Cushions draped right along the upper curves of the spiderwood!
        val mossT = floatArrayOf(0.22f, 0.42f, 0.64f)
        for (mt in mossT) {
            val mx = evalWoodMainX(mt)
            val my = evalWoodMainY(mt) - tankH * 0.014f
            val mr = tankW * (0.024f + (1f - mt) * 0.012f)
            drawMossCushion(canvas, mx, my, mr)
        }
        drawMossCushion(canvas, tankX + tankW * 0.84f, sandY - tankH * 0.088f, tankW * 0.040f)

        // 3. Java Fern (Microsorum pteropus) wavy emerald lance fronds erupting from driftwood nexus!
        drawJavaFernCluster(canvas, tankX + tankW * 0.72f, sandY - tankH * 0.14f, tankH * 0.24f, isBatterySaver)
        drawJavaFernCluster(canvas, tankX + tankW * 0.54f, sandY - tankH * 0.18f, tankH * 0.17f, isBatterySaver)

        // 4. Broad-Leaf Anubias Hammocks (where Bettas physically lounge!)
        drawAnubiasCluster(canvas, tankX + tankW * 0.64f, sandY - tankH * 0.13f, tankW * 0.00205f)
        drawAnubiasCluster(canvas, tankX + tankW * 0.45f, sandY - tankH * 0.10f, tankW * 0.00175f)

        // 5. Minimalist Glass Corner Diffuser
        val tubeX = tankX + tankW * 0.110f
        val cupX = tankX + tankW * 0.125f
        val cupBot = sandY - tankH * 0.016f
        val cupTop = sandY - tankH * 0.056f
        strokePaint.color = 0x72DCF2F6
        strokePaint.strokeWidth = (tankW * 0.0040f).coerceIn(1.4f, 3.0f)
        shapePath.reset()
        shapePath.moveTo(tubeX, tankY + 4f)
        shapePath.lineTo(tubeX, cupBot - 6f)
        shapePath.quadTo(tubeX, cupBot, cupX, cupBot)
        shapePath.lineTo(cupX, cupTop + 6f)
        canvas.drawPath(shapePath, strokePaint)
        val cupHalf = tankW * 0.011f
        fillPaint.color = 0x52E2F4F8
        canvas.drawRoundRect(cupX - cupHalf, cupTop, cupX + cupHalf, cupTop + cupHalf * 1.6f, 2f, 2f, fillPaint)
    }

    /**
     * Foreground Botanicals (z = 0.25):
     * - Mossy foreground accent stone + Cryptocoryne wendtii bronze/olive rosettes
     * - Dwarf Hairgrass tufts + Fallen Indian Almond (Catappa) leaves + tiny Alder Cones!
     */
    private fun drawForegroundBotanicals(canvas: Canvas) {
        // Foreground left accent river stone + velvety Marimo moss cushion
        drawSculptedStone(canvas, tankX + tankW * 0.22f, sandY - tankH * 0.004f, tankW * 0.068f, tankH * 0.032f)
        drawMossCushion(canvas, tankX + tankW * 0.24f, sandY - tankH * 0.028f, tankW * 0.032f)

        // Bronze/Olive Cryptocoryne wendtii rosettes (native Southeast Asian Betta biotope flora!)
        drawCryptocoryneRosette(canvas, tankX + tankW * 0.30f, sandY + tankH * 0.004f, tankW * 0.0018f)
        drawCryptocoryneRosette(canvas, tankX + tankW * 0.72f, sandY + tankH * 0.006f, tankW * 0.0019f)

        // Fine Dwarf Hairgrass (Eleocharis) tufts along stone borders
        drawHairgrassTuft(canvas, tankX + tankW * 0.16f, sandY + tankH * 0.010f, tankH * 0.072f)
        drawHairgrassTuft(canvas, tankX + tankW * 0.56f, sandY + tankH * 0.004f, tankH * 0.062f)
        drawHairgrassTuft(canvas, tankX + tankW * 0.80f, sandY + tankH * 0.010f, tankH * 0.068f)

        // Fallen Indian Almond (Catappa) leaves + tiny dark Alder Cones on the sand
        drawCatappaLeaf(canvas, tankX + tankW * 0.38f, sandY + tankH * 0.024f, 9f)
        drawCatappaLeaf(canvas, tankX + tankW * 0.64f, sandY + tankH * 0.032f, -12f)
        drawAlderCone(canvas, tankX + tankW * 0.46f, sandY + tankH * 0.030f)
        drawAlderCone(canvas, tankX + tankW * 0.29f, sandY + tankH * 0.028f)
    }

    /** Foreground Framing Plants (z = 0.11 — IN FRONT of hiding & midground Bettas!) */
    private fun drawForegroundFramingPlants(canvas: Canvas) {
        drawVallisneriaGroup(canvas, tankX + tankW * 0.068f, tankH * 0.50f, 4, aqPal.plantFront, 1.0f, 1.0f)
        drawVallisneriaGroup(canvas, tankX + tankW * 0.932f, tankH * 0.47f, 4, aqPal.plantFront, 0.94f, 1.0f)
    }

    private fun drawVallisneriaGroup(
        canvas: Canvas,
        xBase: Float,
        heightPx: Float,
        blades: Int,
        fillCol: Int,
        swayAmp: Float,
        widthScale: Float
    ) {
        val spacing = tankW * 0.019f * widthScale
        val halfW = (tankW * 0.0085f * widthScale).coerceIn(2.4f, 6.2f)
        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = (tankW * 0.0019f).coerceIn(0.8f, 1.4f)
        fillPaint.color = fillCol

        for (i in 0 until blades) {
            val offset = (i - (blades - 1) * 0.5f) * spacing
            val bh = heightPx * (0.68f + ((i * 37) % 10) * 0.036f)
            val sway = sin(simTime * 1.02f + i * 0.85f + xBase * 0.02f) * (tankW * 0.028f) * swayAmp
            val bx = xBase + offset
            val by = sandY + tankH * 0.012f
            shapePath.reset()
            shapePath.moveTo(bx - halfW, by)
            shapePath.quadTo(bx + sway * 0.45f - halfW * 0.72f, by - bh * 0.55f, bx + sway, by - bh)
            shapePath.quadTo(bx + sway * 0.45f + halfW * 0.85f, by - bh * 0.52f, bx + halfW, by)
            shapePath.close()
            canvas.drawPath(shapePath, fillPaint)
            canvas.drawPath(shapePath, strokePaint)
        }
    }

    private fun drawJavaFernCluster(canvas: Canvas, cx: Float, cy: Float, maxLen: Float, isBatterySaver: Boolean) {
        val angles = floatArrayOf(-52f, -26f, -4f, 20f, 44f)
        val count = if (isBatterySaver) 3 else angles.size
        for (i in 0 until count) {
            val len = maxLen * (0.76f + (if (i == 2) 0.24f else (i % 2) * 0.14f))
            val hw = len * 0.155f
            val sway = sin(simTime * 0.95f + i * 1.1f) * 3.5f
            canvas.save()
            canvas.translate(cx, cy)
            canvas.rotate(angles[i] + sway)
            shapePath.reset()
            shapePath.moveTo(0f, 0f)
            shapePath.cubicTo(-hw * 1.1f, -len * 0.30f, -hw * 0.95f, -len * 0.72f, 0f, -len)
            shapePath.cubicTo(hw * 0.95f, -len * 0.72f, hw * 1.1f, -len * 0.30f, 0f, 0f)
            shapePath.close()
            fillPaint.color = if (i % 2 == 0) aqPal.fernHi else aqPal.fernDark
            canvas.drawPath(shapePath, fillPaint)
            strokePaint.color = aqPal.contour
            strokePaint.strokeWidth = 0.9f
            canvas.drawPath(shapePath, strokePaint)
            strokePaint.color = 0x26FFFFFF
            canvas.drawLine(0f, -len * 0.08f, 0f, -len * 0.88f, strokePaint)
            canvas.restore()
        }
    }

    private fun drawAnubiasCluster(canvas: Canvas, cx: Float, cy: Float, scale: Float) {
        val offsetsX = floatArrayOf(-24f, 0f, 23f, -17f, 17f)
        val offsetsY = floatArrayOf(-11f, -22f, -12f, -1f, -1f)
        val angles = floatArrayOf(-33f, -4f, 32f, -52f, 50f)
        for (i in offsetsX.indices) {
            val sway = sin(simTime * 0.9f + i * 1.1f) * 1.6f
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
            strokePaint.strokeWidth = 0.9f
            canvas.drawPath(shapePath, strokePaint)
            strokePaint.color = 0x26FFFFFF
            canvas.drawLine(0f, 9f * scale, 0f, -14f * scale, strokePaint)
            canvas.restore()
        }
    }

    private fun drawCryptocoryneRosette(canvas: Canvas, cx: Float, cy: Float, scale: Float) {
        val angles = floatArrayOf(-64f, -34f, -6f, 26f, 58f)
        for (i in angles.indices) {
            val sway = sin(simTime * 0.85f + i) * 1.8f
            canvas.save()
            canvas.translate(cx, cy)
            canvas.rotate(angles[i] + sway)
            val len = (24f + (i % 2) * 8f) * scale
            val hw = 6.2f * scale
            shapePath.reset()
            shapePath.moveTo(0f, 0f)
            shapePath.quadTo(-hw * 1.2f, -len * 0.45f, 0f, -len)
            shapePath.quadTo(hw * 1.2f, -len * 0.45f, 0f, 0f)
            shapePath.close()
            fillPaint.color = if (i % 2 == 0) aqPal.cryptBronze else aqPal.cryptOlive
            canvas.drawPath(shapePath, fillPaint)
            strokePaint.color = aqPal.contour
            strokePaint.strokeWidth = 0.85f
            canvas.drawPath(shapePath, strokePaint)
            canvas.restore()
        }
    }

    private fun drawHairgrassTuft(canvas: Canvas, cx: Float, cy: Float, heightPx: Float) {
        strokePaint.color = aqPal.mossHi
        strokePaint.strokeWidth = (tankW * 0.0024f).coerceIn(0.9f, 1.8f)
        for (i in -3..3) {
            val spread = i * (tankW * 0.009f)
            val sway = sin(simTime * 1.3f + i * 0.7f + cx * 0.03f) * (tankW * 0.010f)
            val bh = heightPx * (0.70f + (1f - abs(i) / 4f) * 0.35f)
            detailPath.reset()
            detailPath.moveTo(cx + i * 1.8f, cy)
            detailPath.quadTo(cx + spread * 0.5f, cy - bh * 0.55f, cx + spread + sway, cy - bh)
            canvas.drawPath(detailPath, strokePaint)
        }
    }

    private fun drawSculptedStone(canvas: Canvas, cx: Float, cy: Float, rx: Float, ry: Float) {
        // Subtle ambient occlusion contact shadow on sand
        if (!aqPal.isAmoled) {
            fillPaint.color = 0x36000000
            canvas.drawOval(cx - rx * 1.06f, cy + ry * 0.15f, cx + rx * 1.06f, cy + ry * 1.10f, fillPaint)
        }
        // Faceted angular Seiryu / river rock contour (looks organic, not a plain ellipse!)
        shapePath.reset()
        shapePath.moveTo(cx - rx, cy + ry * 0.35f)
        shapePath.quadTo(cx - rx * 0.88f, cy - ry * 0.65f, cx - rx * 0.25f, cy - ry)
        shapePath.quadTo(cx + rx * 0.45f, cy - ry * 0.92f, cx + rx * 0.88f, cy - ry * 0.25f)
        shapePath.quadTo(cx + rx * 1.05f, cy + ry * 0.35f, cx + rx * 0.78f, cy + ry * 0.68f)
        shapePath.quadTo(cx, cy + ry * 0.88f, cx - rx, cy + ry * 0.35f)
        shapePath.close()
        fillPaint.color = aqPal.stoneDark
        canvas.drawPath(shapePath, fillPaint)

        // Midtone facet, sunlit top ridge highlight & delicate Seiryu quartz mineral vein!
        detailPath.reset()
        detailPath.moveTo(cx - rx * 0.75f, cy + ry * 0.08f)
        detailPath.quadTo(cx - rx * 0.25f, cy - ry * 0.92f, cx + rx * 0.58f, cy - ry * 0.58f)
        detailPath.quadTo(cx + rx * 0.20f, cy - ry * 0.05f, cx - rx * 0.75f, cy + ry * 0.08f)
        detailPath.close()
        fillPaint.color = aqPal.stoneHi
        canvas.drawPath(detailPath, fillPaint)

        // Fine hand-inked quartz mineral vein & facet crack
        strokePaint.color = withAlpha(aqPal.glassEdge, if (aqPal.isAmoled) 68 else 86)
        strokePaint.strokeWidth = 0.85f
        canvas.drawLine(cx - rx * 0.32f, cy - ry * 0.72f, cx + rx * 0.14f, cy + ry * 0.42f, strokePaint)
        canvas.drawLine(cx + rx * 0.04f, cy - ry * 0.42f, cx + rx * 0.54f, cy + ry * 0.18f, strokePaint)

        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = 1.0f
        canvas.drawPath(shapePath, strokePaint)
    }

    private fun drawTaperedWoodCurve(canvas: Canvas, curveId: Int, rStart: Float, rEnd: Float, fillCol: Int) {
        for (i in 0..woodSteps) {
            val t = i.toFloat() / woodSteps.toFloat()
            val px = when (curveId) { 0 -> evalWoodMainX(t); 1 -> evalWoodUpperX(t); else -> evalWoodRootX(t) }
            val py = when (curveId) { 0 -> evalWoodMainY(t); 1 -> evalWoodUpperY(t); else -> evalWoodRootY(t) }
            val ang = when (curveId) { 0 -> evalWoodMainAng(t); 1 -> evalWoodUpperAng(t); else -> evalWoodRootAng(t) }
            val r = rStart + (rEnd - rStart) * t
            val nx = -sin(ang); val ny = cos(ang)
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

        // Subtle sunlit upper bark highlight & hand-inked longitudinal bark grain + knot!
        detailPath.reset()
        detailPath.moveTo(woodRX[1], woodRY[1])
        for (i in 2 until woodSteps) detailPath.lineTo(woodRX[i], woodRY[i])
        strokePaint.color = withAlpha(aqPal.woodRim, 155)
        strokePaint.strokeWidth = (rStart * 0.22f).coerceIn(1.0f, 2.6f)
        canvas.drawPath(detailPath, strokePaint)

        // Hand-inked center wood-grain striation line + subtle knot oval on main arch
        detailPath.reset()
        for (i in 1 until woodSteps - 1) {
            val gx = (woodLX[i] * 0.62f + woodRX[i] * 0.38f)
            val gy = (woodLY[i] * 0.62f + woodRY[i] * 0.38f)
            if (i == 1) detailPath.moveTo(gx, gy) else detailPath.lineTo(gx, gy)
        }
        strokePaint.color = withAlpha(aqPal.woodDark, 145)
        strokePaint.strokeWidth = 0.9f
        canvas.drawPath(detailPath, strokePaint)
        if (curveId == 0) {
            val kx = (woodLX[5] + woodRX[5]) * 0.5f
            val ky = (woodLY[5] + woodRY[5]) * 0.5f
            val kr = (rStart * 0.28f).coerceAtLeast(2.2f)
            canvas.drawOval(kx - kr * 1.4f, ky - kr * 0.65f, kx + kr * 1.4f, ky + kr * 0.65f, strokePaint)
        }

        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = 1.05f
        canvas.drawPath(shapePath, strokePaint)
    }

    private fun drawMossCushion(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        fillPaint.color = aqPal.moss
        canvas.drawOval(cx - r * 1.15f, cy - r * 0.72f, cx + r * 1.15f, cy + r * 0.72f, fillPaint)
        fillPaint.color = aqPal.mossHi
        canvas.drawOval(cx - r * 0.78f, cy - r * 0.64f, cx + r * 0.56f, cy + r * 0.08f, fillPaint)
        // Velvety hand-inked moss stipple highlights!
        fillPaint.color = withAlpha(aqPal.anubiasHi, 165)
        for (dot in -2..2) {
            val dx = cx + dot * r * 0.34f
            val dy = cy - r * (0.28f - (abs(dot) % 2) * 0.22f)
            canvas.drawCircle(dx, dy, (r * 0.075f).coerceIn(1.0f, 2.2f), fillPaint)
        }
        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = 0.9f
        canvas.drawOval(cx - r * 1.15f, cy - r * 0.72f, cx + r * 1.15f, cy + r * 0.72f, strokePaint)
    }

    private fun drawCatappaLeaf(canvas: Canvas, cx: Float, cy: Float, deg: Float) {
        val rx = tankW * 0.048f
        val ry = rx * 0.44f
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
        strokePaint.strokeWidth = 0.9f
        canvas.drawPath(shapePath, strokePaint)
        // Midrib + delicate lateral veins
        canvas.drawLine(-rx * 0.78f, 0f, rx * 0.78f, 0f, strokePaint)
        for (v in -1..1) {
            val vx = v * rx * 0.28f
            canvas.drawLine(vx, 0f, vx + rx * 0.16f, -ry * 0.42f, strokePaint)
            canvas.drawLine(vx, 0f, vx + rx * 0.16f, ry * 0.36f, strokePaint)
        }
        canvas.restore()
    }

    private fun drawAlderCone(canvas: Canvas, cx: Float, cy: Float) {
        val r = (tankW * 0.0085f).coerceIn(2.6f, 6.2f)
        fillPaint.color = aqPal.woodDark
        canvas.drawOval(cx - r * 1.25f, cy - r * 0.85f, cx + r * 1.25f, cy + r * 0.85f, fillPaint)
        strokePaint.color = aqPal.woodRim
        strokePaint.strokeWidth = 0.8f
        canvas.drawLine(cx - r * 0.5f, cy - r * 0.6f, cx - r * 0.5f, cy + r * 0.6f, strokePaint)
        canvas.drawLine(cx + r * 0.2f, cy - r * 0.65f, cx + r * 0.2f, cy + r * 0.65f, strokePaint)
    }

    // =========================================================================
    // 4 GENUINELY DISTINCT BETTA VARIETY RENDERERS (3D Articulated Spines!)
    // =========================================================================

    private fun drawArticulatedBetta(canvas: Canvas, b: BettaFish, isBatterySaver: Boolean) {
        val avgZ = b.nz[2]
        val fogAmt = depthFog(avgZ)
        val unitLen = (tankW * 0.074f).coerceIn(24f, 76f)

        for (i in 0 until SPINE_N) {
            projX[i] = b.nx[i]
            projY[i] = b.ny[i]
            projS[i] = perspScale(b.nz[i]) // True 1.24x front -> 0.58x back perspective sizing!
        }

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
        val veilAlpha = if (aqPal.isAmoled) 186 else 192
        val finBaseCol = withAlpha(TimeOfDayPalette.lerpColor(b.finBase, aqPal.fogColor, fogAmt), veilAlpha)
        val finMidCol = withAlpha(TimeOfDayPalette.lerpColor(b.finMid, aqPal.fogColor, fogAmt), veilAlpha - 10)
        val finTipCol = withAlpha(TimeOfDayPalette.lerpColor(b.finTip, aqPal.fogColor, fogAmt), veilAlpha + 14)
        val bodyCol = TimeOfDayPalette.lerpColor(b.bodyCol, aqPal.fogColor, fogAmt) or (0xFF shl 24)
        val bodyHiCol = TimeOfDayPalette.lerpColor(b.bodyHi, aqPal.fogColor, fogAmt) or (0xFF shl 24)
        val spotCol = TimeOfDayPalette.lerpColor(b.spotCol, aqPal.fogColor, fogAmt) or (0xFF shl 24)
        val rimLineCol = withAlpha(TimeOfDayPalette.lerpColor(b.finTip, aqPal.fogColor, fogAmt * 0.5f), if (aqPal.isAmoled) 155 else 102)

        // Variety-specific fin dimensions!
        val dorsalScale = when (b.variety) {
            BettaVariety.HALFMOON -> 1.18f
            BettaVariety.CROWNTAIL -> 1.05f
            BettaVariety.DUMBO_VEIL -> 0.96f
            BettaVariety.PLAKAT -> 0.56f
        }
        val analScale = when (b.variety) {
            BettaVariety.HALFMOON -> 1.15f
            BettaVariety.CROWNTAIL -> 1.06f
            BettaVariety.DUMBO_VEIL -> 1.10f
            BettaVariety.PLAKAT -> 0.52f
        }

        // 1. DORSAL FIN (Distinct per variety: rounded Halfmoon sail / spiky Crowntail comb / sleek Plakat!)
        for (i in 2..7) {
            val frac = (i - 2) / 5f
            val h = unitLen * (0.24f + sin(frac * PI.toFloat() * 0.88f) * 0.38f) * dorsalScale * billow * projS[i]
            val rip = sin(b.swimPhase * 0.9f - i * 0.7f) * (unitLen * 0.055f) * projS[i]
            outX[i] = projX[i] + upX[i] * h - tangX[i] * (unitLen * 0.13f * frac * projS[i]) + rip * tangY[i]
            outY[i] = projY[i] + upY[i] * h - tangY[i] * (unitLen * 0.13f * frac * projS[i])
        }
        drawVarietyEdgeFin(canvas, 2, 7, isUpper = true, b.variety, unitLen, finMidCol, finTipCol, rimLineCol)

        // 2. ANAL FIN (Draped veil / Crowntail spikes / crisp pointed Plakat!)
        for (i in 2..7) {
            val frac = (i - 2) / 5f
            val h = unitLen * (0.26f + sin(frac * PI.toFloat() * 0.85f) * 0.40f) * analScale * billow * projS[i]
            val rip = cos(b.swimPhase * 0.85f - i * 0.65f) * (unitLen * 0.060f) * projS[i]
            outX[i] = projX[i] - upX[i] * h - tangX[i] * (unitLen * 0.14f * frac * projS[i]) + rip * tangY[i]
            outY[i] = projY[i] - upY[i] * h - tangY[i] * (unitLen * 0.14f * frac * projS[i])
        }
        drawVarietyEdgeFin(canvas, 2, 7, isUpper = false, b.variety, unitLen, finMidCol, finTipCol, rimLineCol)

        // 3. CAUDAL TAIL FIN (DRAMATICALLY different per Betta variety!)
        drawVarietyCaudalTail(canvas, b, unitLen, billow, finBaseCol, finMidCol, finTipCol, isBatterySaver)

        // 4. SCULPTED CURVED TORSO around spine 0..7 (+ Koi Plakat calico blotches!)
        val bodyChub = if (b.variety == BettaVariety.PLAKAT) 1.14f else 1.0f
        for (i in 0..7) {
            val r = unitLen * bodyRadii[i] * bodyChub * projS[i]
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

        // Calico Koi patches for PLAKAT (or metallic scale shimmer for Halfmoon/Crowntail!)
        if (b.variety == BettaVariety.PLAKAT) {
            fillPaint.color = spotCol
            canvas.drawCircle(projX[2], projY[2] + upY[2] * unitLen * 0.03f * projS[2], unitLen * 0.075f * projS[2], fillPaint)
            canvas.drawCircle(projX[4], projY[4] - upY[4] * unitLen * 0.02f * projS[4], unitLen * 0.062f * projS[4], fillPaint)
        }
        detailPath.reset()
        detailPath.moveTo(topEdgeX[1], topEdgeY[1])
        for (i in 2..6) detailPath.lineTo(topEdgeX[i], topEdgeY[i])
        strokePaint.color = withAlpha(bodyHiCol, 195)
        strokePaint.strokeWidth = (unitLen * 0.072f).coerceIn(1.5f, 4.0f)
        canvas.drawPath(detailPath, strokePaint)

        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = 1.10f
        canvas.drawPath(bodyPath, strokePaint)

        // 5. VENTRAL RIBBONS & PECTORAL FINS (ENORMOUS luminous ruffled butterfly-wing ears on DUMBO_VEIL!)
        val pecFlut = sin(b.pecPhase) * (unitLen * 0.095f) * projS[2]
        detailPath.reset()
        detailPath.moveTo(projX[2] - upX[2] * unitLen * 0.11f * projS[2], projY[2] - upY[2] * unitLen * 0.11f * projS[2])
        val ventScale = if (b.variety == BettaVariety.PLAKAT) 0.58f else 1.0f
        detailPath.quadTo(
            projX[3] - upX[3] * unitLen * 0.28f * ventScale * projS[3],
            projY[3] - upY[3] * unitLen * 0.28f * ventScale * projS[3],
            projX[4] - upX[4] * unitLen * 0.36f * ventScale * projS[4] - tangX[4] * unitLen * 0.06f,
            projY[4] - upY[4] * unitLen * 0.36f * ventScale * projS[4]
        )
        strokePaint.color = finTipCol
        strokePaint.strokeWidth = (unitLen * 0.048f * projS[2]).coerceIn(1.2f, 2.8f)
        canvas.drawPath(detailPath, strokePaint)

        if (b.variety == BettaVariety.DUMBO_VEIL) {
            // Iconic hyper-enlarged ruffled Dumbo "Elephant Ear" translucent pectoral wings!
            val earLen = unitLen * 0.36f * projS[2]
            val earW = unitLen * 0.24f * projS[2]
            val flap = sin(b.pecPhase) * earLen * 0.38f
            veilPath.reset()
            veilPath.moveTo(projX[2], projY[2] + unitLen * 0.02f * projS[2])
            veilPath.cubicTo(
                projX[2] - tangX[2] * earLen * 0.4f + upX[2] * earW * 0.5f,
                projY[2] - tangY[2] * earLen * 0.4f - earW * 0.6f + flap,
                projX[2] - tangX[2] * earLen * 1.1f,
                projY[2] - tangY[2] * earLen * 1.1f + earW * 0.2f + flap,
                projX[2] - tangX[2] * earLen * 0.85f,
                projY[2] - tangY[2] * earLen * 0.85f + earW * 1.05f + flap * 0.6f
            )
            veilPath.quadTo(
                projX[2] - tangX[2] * earLen * 0.2f,
                projY[2] + earW * 0.72f,
                projX[2], projY[2] + unitLen * 0.02f * projS[2]
            )
            veilPath.close()
            fillPaint.color = withAlpha(spotCol, 175) // Luminous ivory-white ruffled Dumbo ear!
            canvas.drawPath(veilPath, fillPaint)
            strokePaint.color = withAlpha(finTipCol, 165)
            strokePaint.strokeWidth = 1.1f
            canvas.drawPath(veilPath, strokePaint)
        } else {
            val pecR = unitLen * (if (b.vz < -0.04f) 0.18f else 0.13f) * projS[2]
            fillPaint.color = 0x4EFFFFFF
            canvas.drawOval(
                projX[2] - pecR + pecFlut * 0.25f,
                projY[2] + pecFlut * 0.30f - pecR * 0.45f,
                projX[2] + pecR + pecFlut * 0.25f,
                projY[2] + pecFlut * 0.30f + pecR * 0.55f,
                fillPaint
            )
        }

        // 5B. Flaring Operculum Gill-Beard Collar when crossing paths with another Betta or escaping!
        if (b.flare > 0.22f) {
            val beardR = unitLen * (0.09f + b.flare * 0.06f) * projS[1]
            fillPaint.color = withAlpha(spotCol, (b.flare * 215f).toInt().coerceIn(45, 225))
            canvas.drawCircle(
                projX[1] - upX[1] * unitLen * 0.085f * projS[1],
                projY[1] - upY[1] * unitLen * 0.085f * projS[1],
                beardR,
                fillPaint
            )
        }

        // 6. Crisp Expressive Eye
        if (b.vz < 0.16f) {
            val ex = projX[1] + tangX[1] * unitLen * 0.036f * projS[1] + upX[1] * unitLen * 0.032f * projS[1]
            val ey = projY[1] + tangY[1] * unitLen * 0.036f * projS[1] + upY[1] * unitLen * 0.032f * projS[1]
            val er = (unitLen * 0.048f * projS[1]).coerceIn(1.6f, 4.2f)
            fillPaint.color = 0xFF0C1216.toInt()
            canvas.drawCircle(ex, ey, er, fillPaint)
            fillPaint.color = 0xE6FFFFFF.toInt()
            canvas.drawCircle(ex + er * 0.28f, ey - er * 0.28f, er * 0.34f, fillPaint)
        }
    }

    private fun drawVarietyEdgeFin(
        canvas: Canvas,
        startI: Int,
        endI: Int,
        isUpper: Boolean,
        variety: BettaVariety,
        unitLen: Float,
        fillCol: Int,
        tipCol: Int,
        rimCol: Int
    ) {
        val sign = if (isUpper) 1f else -1f
        veilPath.reset()
        veilPath.moveTo(projX[startI] + sign * upX[startI] * unitLen * 0.09f * projS[startI], projY[startI] + sign * upY[startI] * unitLen * 0.09f * projS[startI])
        if (variety == BettaVariety.CROWNTAIL) {
            // Dramatic Crowntail web-reduced comb spikes extending beyond recessed webbing!
            for (i in startI..endI) {
                val baseX = projX[i] + sign * upX[i] * unitLen * 0.06f * projS[i]
                val baseY = projY[i] + sign * upY[i] * unitLen * 0.06f * projS[i]
                val spikeX = baseX + (outX[i] - baseX) * 1.18f
                val spikeY = baseY + (outY[i] - baseY) * 1.18f
                veilPath.lineTo(spikeX, spikeY)
                if (i < endI) {
                    val midBaseX = (projX[i] + projX[i + 1]) * 0.5f
                    val midBaseY = (projY[i] + projY[i + 1]) * 0.5f
                    val midOutX = (outX[i] + outX[i + 1]) * 0.5f
                    val midOutY = (outY[i] + outY[i + 1]) * 0.5f
                    // Deep ~45% scallop web cutout between comb rays!
                    veilPath.lineTo(midBaseX + (midOutX - midBaseX) * 0.52f, midBaseY + (midOutY - midBaseY) * 0.52f)
                }
            }
        } else {
            veilPath.lineTo(outX[startI], outY[startI])
            for (i in (startI + 1)..endI) {
                veilPath.quadTo(outX[i - 1], outY[i - 1], (outX[i - 1] + outX[i]) * 0.5f, (outY[i - 1] + outY[i]) * 0.5f)
            }
            veilPath.lineTo(outX[endI], outY[endI])
        }
        veilPath.lineTo(projX[endI] + sign * upX[endI] * unitLen * 0.035f * projS[endI], projY[endI] + sign * upY[endI] * unitLen * 0.035f * projS[endI])
        veilPath.close()
        fillPaint.color = fillCol
        canvas.drawPath(veilPath, fillPaint)
        // Luminous backlit inner silk highlight wash along fin core!
        if (variety != BettaVariety.CROWNTAIL) {
            detailPath.reset()
            for (i in startI..endI) {
                val mx = projX[i] + (outX[i] - projX[i]) * 0.62f
                val my = projY[i] + (outY[i] - projY[i]) * 0.62f
                if (i == startI) detailPath.moveTo(mx, my) else detailPath.lineTo(mx, my)
            }
            strokePaint.color = withAlpha(tipCol, 95)
            strokePaint.strokeWidth = (unitLen * 0.09f).coerceIn(1.8f, 5.2f)
            canvas.drawPath(detailPath, strokePaint)
        }
        strokePaint.color = if (variety == BettaVariety.CROWNTAIL) tipCol else rimCol
        strokePaint.strokeWidth = if (variety == BettaVariety.CROWNTAIL) 1.25f else 0.95f
        canvas.drawPath(veilPath, strokePaint)
    }

    private fun drawVarietyCaudalTail(
        canvas: Canvas,
        b: BettaFish,
        unitLen: Float,
        billow: Float,
        finBaseCol: Int,
        finMidCol: Int,
        finTipCol: Int,
        isBatterySaver: Boolean
    ) {
        val tbX = projX[6]; val tbY = projY[6]; val tbS = projS[6]
        val tmX = projX[9]; val tmY = projY[9]
        val ttX = projX[11]; val ttY = projY[11]
        val tdX = ttX - tbX; val tdY = ttY - tbY
        val tSpread = hypot(tdX, tdY)
        val dirX = if (tSpread > 0.5f) tdX / tSpread else -tangX[6]
        val dirY = if (tSpread > 0.5f) tdY / tSpread else -tangY[6]
        val nuX = upX[9]; val nuY = upY[9]

        when (b.variety) {
            BettaVariety.CROWNTAIL -> {
                // Iconic Spiky Crowntail: 7 long curved crown rays with deep 45% web cutouts!
                val ext = max(unitLen * 0.44f * tbS, tSpread * 1.45f)
                val fanSpan = unitLen * 0.50f * billow * tbS
                veilPath.reset()
                veilPath.moveTo(tbX + upX[6] * unitLen * 0.05f * tbS, tbY + upY[6] * unitLen * 0.05f * tbS)
                val rays = 7
                for (r in 0 until rays) {
                    val f = (r / (rays - 1f)) * 2f - 1f // -1..+1
                    val rayLen = ext * (1.16f - abs(f) * 0.14f)
                    val rx = tbX + dirX * rayLen + nuX * (fanSpan * f)
                    val ry = tbY + dirY * rayLen + nuY * (fanSpan * f)
                    veilPath.lineTo(rx, ry)
                    if (r < rays - 1) {
                        val fm = ((r + 0.5f) / (rays - 1f)) * 2f - 1f
                        val cutLen = ext * 0.54f
                        veilPath.lineTo(tbX + dirX * cutLen + nuX * (fanSpan * fm * 0.65f), tbY + dirY * cutLen + nuY * (fanSpan * fm * 0.65f))
                    }
                }
                veilPath.lineTo(tbX - upX[6] * unitLen * 0.05f * tbS, tbY - upY[6] * unitLen * 0.05f * tbS)
                veilPath.close()
                fillPaint.color = finBaseCol
                canvas.drawPath(veilPath, fillPaint)
                strokePaint.color = finTipCol
                strokePaint.strokeWidth = (unitLen * 0.044f).coerceIn(1.1f, 2.4f)
                canvas.drawPath(veilPath, strokePaint)
                // Crisp radiating spiky crown needles extending past the tips!
                strokePaint.color = finTipCol
                strokePaint.strokeWidth = (unitLen * 0.032f).coerceIn(1.0f, 2.0f)
                for (r in 0 until rays) {
                    val f = (r / (rays - 1f)) * 2f - 1f
                    val rayLen = ext * (1.26f - abs(f) * 0.14f)
                    canvas.drawLine(tbX, tbY, tbX + dirX * rayLen + nuX * (fanSpan * f * 1.05f), tbY + dirY * rayLen + nuY * (fanSpan * f * 1.05f), strokePaint)
                }
            }
            BettaVariety.DUMBO_VEIL -> {
                // Long graceful asymmetric drooping pointed Veiltail!
                val ext = max(unitLen * 0.54f * tbS, tSpread * 1.62f)
                val fanSpan = unitLen * 0.34f * billow * tbS
                val endX = tbX + dirX * ext
                val endY = tbY + dirY * ext + unitLen * 0.16f * tbS // Natural graceful downward veil drape!
                veilPath.reset()
                veilPath.moveTo(tbX + upX[6] * unitLen * 0.05f * tbS, tbY + upY[6] * unitLen * 0.05f * tbS)
                veilPath.cubicTo(
                    tmX + nuX * fanSpan * 1.10f, tmY + nuY * fanSpan * 1.10f,
                    endX + nuX * fanSpan * 0.55f, endY + nuY * fanSpan * 0.55f,
                    endX + dirX * unitLen * 0.14f * tbS, endY + unitLen * 0.08f * tbS
                )
                veilPath.cubicTo(
                    endX - nuX * fanSpan * 0.95f, endY - nuY * fanSpan * 0.95f,
                    tmX - nuX * fanSpan * 1.15f, tmY - nuY * fanSpan * 1.15f,
                    tbX - upX[6] * unitLen * 0.05f * tbS, tbY - upY[6] * unitLen * 0.05f * tbS
                )
                veilPath.close()
                fillPaint.color = finMidCol
                canvas.drawPath(veilPath, fillPaint)
                strokePaint.color = finTipCol
                strokePaint.strokeWidth = (unitLen * 0.044f).coerceIn(1.1f, 2.4f)
                canvas.drawPath(veilPath, strokePaint)
            }
            BettaVariety.PLAKAT -> {
                // Crisp, compact, rounded athletic wild-type fighter spade/D-fan!
                val ext = max(unitLen * 0.24f * tbS, tSpread * 0.85f)
                val fanSpan = unitLen * 0.28f * billow * tbS
                val endX = tbX + dirX * ext
                val endY = tbY + dirY * ext
                veilPath.reset()
                veilPath.moveTo(tbX + upX[6] * unitLen * 0.055f * tbS, tbY + upY[6] * unitLen * 0.055f * tbS)
                veilPath.cubicTo(
                    tbX + dirX * ext * 0.55f + nuX * fanSpan * 1.15f, tbY + dirY * ext * 0.55f + nuY * fanSpan * 1.15f,
                    endX + nuX * fanSpan * 0.72f, endY + nuY * fanSpan * 0.72f,
                    endX + dirX * unitLen * 0.05f * tbS, endY + dirY * unitLen * 0.05f * tbS
                )
                veilPath.cubicTo(
                    endX - nuX * fanSpan * 0.72f, endY - nuY * fanSpan * 0.72f,
                    tbX + dirX * ext * 0.55f - nuX * fanSpan * 1.15f, tbY + dirY * ext * 0.55f - nuY * fanSpan * 1.15f,
                    tbX - upX[6] * unitLen * 0.055f * tbS, tbY - upY[6] * unitLen * 0.055f * tbS
                )
                veilPath.close()
                fillPaint.color = finBaseCol
                canvas.drawPath(veilPath, fillPaint)
                strokePaint.color = finTipCol
                strokePaint.strokeWidth = (unitLen * 0.045f).coerceIn(1.2f, 2.5f)
                canvas.drawPath(veilPath, strokePaint)
            }
            BettaVariety.HALFMOON -> {
                // Sweeping 180° Symmetrical D-Fan Halfmoon with delicate pleated rays!
                val ext = max(unitLen * 0.42f * tbS, tSpread * 1.40f)
                val fanSpan = unitLen * 0.56f * billow * tbS
                val endX = tbX + dirX * ext
                val endY = tbY + dirY * ext
                veilPath.reset()
                veilPath.moveTo(tbX + upX[6] * unitLen * 0.06f * tbS, tbY + upY[6] * unitLen * 0.06f * tbS)
                veilPath.cubicTo(
                    tmX + nuX * fanSpan * 1.24f, tmY + nuY * fanSpan * 1.24f,
                    endX + nuX * fanSpan * 0.96f, endY + nuY * fanSpan * 0.96f,
                    endX + dirX * unitLen * 0.11f * tbS, endY + dirY * unitLen * 0.11f * tbS
                )
                veilPath.cubicTo(
                    endX - nuX * fanSpan * 0.96f, endY - nuY * fanSpan * 0.96f,
                    tmX - nuX * fanSpan * 1.24f, tmY - nuY * fanSpan * 1.24f,
                    tbX - upX[6] * unitLen * 0.06f * tbS, tbY - upY[6] * unitLen * 0.06f * tbS
                )
                veilPath.close()
                fillPaint.color = finBaseCol
                canvas.drawPath(veilPath, fillPaint)
                strokePaint.color = finTipCol
                strokePaint.strokeWidth = (unitLen * 0.052f).coerceIn(1.3f, 2.8f)
                canvas.drawPath(veilPath, strokePaint)
                if (!isBatterySaver) {
                    strokePaint.color = 0x3AFFFFFF
                    strokePaint.strokeWidth = 0.85f
                    for (r in -3..3) {
                        val f = r / 3f
                        detailPath.reset()
                        detailPath.moveTo(tbX, tbY)
                        detailPath.quadTo(
                            tmX + nuX * fanSpan * 0.78f * f, tmY + nuY * fanSpan * 0.78f * f,
                            endX + nuX * fanSpan * 0.84f * f + dirX * (1f - abs(f)) * unitLen * 0.06f,
                            endY + nuY * fanSpan * 0.84f * f + dirY * (1f - abs(f)) * unitLen * 0.06f
                        )
                        canvas.drawPath(detailPath, strokePaint)
                    }
                }
            }
        }
    }

    // =========================================================================
    // ANATOMICALLY DETAILED 3D CLEANERS & CRITTERS (FRONT GLASS / BACK GLASS / 3D DEPTH!)
    // =========================================================================

    private fun drawOtocinclus(canvas: Canvas, o: OtoCleaner) {
        val waterSpan = (sandY - waterY).coerceAtLeast(60f)
        val ox = tankX + o.normX * tankW
        val oy = waterY + o.normY * waterSpan
        val sc = (tankW * 0.0024f).coerceIn(0.82f, 2.4f) * perspScale(o.zDepth)
        val fog = depthFog(o.zDepth)
        val wag = sin(o.phase) * 2.2f * sc

        val oliveCol = TimeOfDayPalette.lerpColor(0xFFA8A382.toInt(), aqPal.fogColor, fog) or (0xFF shl 24)
        val bellyCol = TimeOfDayPalette.lerpColor(0xFFEDE6D4.toInt(), aqPal.fogColor, fog) or (0xFF shl 24)
        val stripeCol = TimeOfDayPalette.lerpColor(0xFF282622.toInt(), aqPal.fogColor, fog) or (0xFF shl 24)
        val finCol = withAlpha(TimeOfDayPalette.lerpColor(0xFFD6D0BE.toInt(), aqPal.fogColor, fog), 168)

        canvas.save()
        canvas.translate(ox, oy)
        when (o.plane) {
            SurfacePlane.FRONT_GLASS -> {
                // VENTRAL VIEW PRESSED AGAINST FRONT GLASS: Pale belly, pulsing oral sucker disc, splayed anchor fins!
                canvas.rotate(o.angleRad * (180f / PI.toFloat()) + 90f)
                // Splayed translucent pectoral & pelvic anchor fins pressed flat against the glass!
                fillPaint.color = finCol
                strokePaint.color = aqPal.contour
                strokePaint.strokeWidth = 0.85f
                for (side in -1..1 step 2) {
                    shapePath.reset()
                    shapePath.moveTo(side * 3.5f * sc, -3.5f * sc)
                    shapePath.quadTo(side * 12.5f * sc, -1.5f * sc, side * 9.5f * sc, 5.2f * sc)
                    shapePath.quadTo(side * 5.0f * sc, 4.0f * sc, side * 3.2f * sc, 1.5f * sc)
                    shapePath.close()
                    canvas.drawPath(shapePath, fillPaint)
                    canvas.drawPath(shapePath, strokePaint)
                    // Pelvic anchor fins
                    canvas.drawOval(side * 2.2f * sc - 2.6f * sc, 4.5f * sc, side * 2.2f * sc + 2.6f * sc, 9.2f * sc, fillPaint)
                }

                // Forked undulating tail with iconic caudal-base diamond spot
                shapePath.reset()
                shapePath.moveTo(0f, 11.5f * sc)
                shapePath.lineTo(-4.8f * sc + wag, 19.5f * sc)
                shapePath.lineTo(0f + wag * 0.5f, 16.2f * sc)
                shapePath.lineTo(4.8f * sc + wag, 19.5f * sc)
                shapePath.close()
                canvas.drawPath(shapePath, fillPaint)
                canvas.drawPath(shapePath, strokePaint)

                // Dark flank rim + pearly ivory flattened ventral belly
                bodyPath.reset()
                bodyPath.moveTo(0f, -13.5f * sc)
                bodyPath.cubicTo(-6.2f * sc, -11.5f * sc, -5.8f * sc, 3.5f * sc, -1.8f * sc, 12.5f * sc)
                bodyPath.lineTo(1.8f * sc, 12.5f * sc)
                bodyPath.cubicTo(5.8f * sc, 3.5f * sc, 6.2f * sc, -11.5f * sc, 0f, -13.5f * sc)
                bodyPath.close()
                fillPaint.color = stripeCol
                canvas.drawPath(bodyPath, fillPaint)
                fillPaint.color = bellyCol
                canvas.drawOval(-4.2f * sc, -11.5f * sc, 4.2f * sc, 10.2f * sc, fillPaint)

                // Animated Pulsing Round Oral Sucker Disc rasping algae off the front glass!!
                val pulse = 1.0f + sin(o.phase * 2.4f) * 0.14f
                fillPaint.color = 0xFFD9ABA0.toInt()
                canvas.drawOval(-3.1f * sc * pulse, -11.6f * sc, 3.1f * sc * pulse, -7.2f * sc, fillPaint)
                strokePaint.color = 0x998C5E54.toInt()
                strokePaint.strokeWidth = 0.9f
                canvas.drawOval(-1.8f * sc * pulse, -10.6f * sc, 1.8f * sc * pulse, -8.2f * sc, strokePaint)

                // Caudal diamond spot + subtle gill slits
                fillPaint.color = stripeCol
                canvas.drawCircle(0f, 11.8f * sc, 1.8f * sc, fillPaint)
            }
            SurfacePlane.BACK_GLASS -> {
                // DORSAL TOP-DOWN VIEW ON BACK GLASS (smaller & gently depth-hazed!)
                canvas.rotate(o.angleRad * (180f / PI.toFloat()) + 90f)
                fillPaint.color = finCol
                for (side in -1..1 step 2) {
                    shapePath.reset()
                    shapePath.moveTo(side * 3.2f * sc, -3.5f * sc)
                    shapePath.quadTo(side * 11.5f * sc, -1.0f * sc, side * 8.5f * sc, 5.0f * sc)
                    shapePath.lineTo(side * 3.0f * sc, 1.5f * sc)
                    shapePath.close()
                    canvas.drawPath(shapePath, fillPaint)
                }
                shapePath.reset()
                shapePath.moveTo(0f, 11.5f * sc)
                shapePath.lineTo(-4.5f * sc + wag, 19.0f * sc)
                shapePath.lineTo(0f + wag * 0.5f, 16.0f * sc)
                shapePath.lineTo(4.5f * sc + wag, 19.0f * sc)
                shapePath.close()
                canvas.drawPath(shapePath, fillPaint)

                bodyPath.reset()
                bodyPath.moveTo(0f, -13.2f * sc)
                bodyPath.cubicTo(-5.8f * sc, -10.5f * sc, -5.2f * sc, 3.0f * sc, -1.6f * sc, 12.2f * sc)
                bodyPath.lineTo(1.6f * sc, 12.2f * sc)
                bodyPath.cubicTo(5.2f * sc, 3.0f * sc, 5.8f * sc, -10.5f * sc, 0f, -13.2f * sc)
                bodyPath.close()
                fillPaint.color = oliveCol
                canvas.drawPath(bodyPath, fillPaint)
                // Dark mottled dorsal saddles & tiny dorsal eyes
                fillPaint.color = withAlpha(stripeCol, 155)
                canvas.drawOval(-3.2f * sc, -4.0f * sc, 3.2f * sc, 1.2f * sc, fillPaint)
                canvas.drawOval(-2.2f * sc, 4.0f * sc, 2.2f * sc, 7.5f * sc, fillPaint)
                fillPaint.color = 0xFF101214.toInt()
                canvas.drawCircle(-2.8f * sc, -8.5f * sc, 1.0f * sc, fillPaint)
                canvas.drawCircle(2.8f * sc, -8.5f * sc, 1.0f * sc, fillPaint)
            }
            else -> {
                // SIDE-GLASS OR TRANSIT PROFILE VIEW
                if (o.plane == SurfacePlane.TRANSIT) {
                    val ang = atan2(o.endY - o.startY, o.endX - o.startX)
                    canvas.rotate(ang * (180f / PI.toFloat()))
                    if (o.endX < o.startX) canvas.scale(1f, -1f)
                } else {
                    canvas.rotate(if (sin(o.angleRad) >= 0f) 90f else -90f)
                    if (o.plane == SurfacePlane.RIGHT_GLASS) canvas.scale(1f, -1f)
                }
                // Dorsal & forked caudal fin
                fillPaint.color = finCol
                shapePath.reset()
                shapePath.moveTo(1.5f * sc, -4.0f * sc)
                shapePath.quadTo(-2.5f * sc, -10.5f * sc, -6.0f * sc, -3.2f * sc)
                shapePath.close()
                canvas.drawPath(shapePath, fillPaint)

                shapePath.reset()
                shapePath.moveTo(-11f * sc, 0f)
                shapePath.lineTo(-18.5f * sc, -4.5f * sc + wag)
                shapePath.lineTo(-15.5f * sc, wag * 0.4f)
                shapePath.lineTo(-18.5f * sc, 4.5f * sc + wag)
                shapePath.close()
                canvas.drawPath(shapePath, fillPaint)

                // Sleek torpedo body (olive dorsum + pearly belly + crisp black lateral stripe!)
                bodyPath.reset()
                bodyPath.moveTo(12.5f * sc, 0.8f * sc)
                bodyPath.quadTo(3.0f * sc, -6.2f * sc, -11.5f * sc, -1.6f * sc)
                bodyPath.lineTo(-11.5f * sc, 1.8f * sc)
                bodyPath.quadTo(2.5f * sc, 4.8f * sc, 12.5f * sc, 0.8f * sc)
                bodyPath.close()
                fillPaint.color = bellyCol
                canvas.drawPath(bodyPath, fillPaint)
                fillPaint.color = oliveCol
                canvas.drawOval(-8f * sc, -4.8f * sc, 10f * sc, 0.4f * sc, fillPaint)
                strokePaint.color = stripeCol
                strokePaint.strokeWidth = 2.4f * sc
                canvas.drawLine(9.5f * sc, 0.2f * sc, -11.2f * sc, 0.2f * sc, strokePaint)
                fillPaint.color = stripeCol
                canvas.drawCircle(-11.5f * sc, 0.2f * sc, 1.8f * sc, fillPaint)
                fillPaint.color = 0xFF101214.toInt()
                canvas.drawCircle(7.5f * sc, -1.4f * sc, 1.2f * sc, fillPaint)
            }
        }
        canvas.restore()
    }

    private fun drawBristlenosePleco(canvas: Canvas) {
        val waterSpan = (sandY - waterY).coerceAtLeast(60f)
        val px: Float
        val py: Float
        if (pleco.plane == SurfacePlane.WOOD) {
            px = evalWoodMainX(pleco.woodT)
            py = evalWoodMainY(pleco.woodT) - tankH * 0.015f
        } else {
            px = tankX + pleco.normX * tankW
            py = waterY + pleco.normY * waterSpan
        }
        val sc = (tankW * 0.0028f).coerceIn(0.95f, 2.8f) * perspScale(pleco.zDepth)
        val fog = depthFog(pleco.zDepth) + if (pleco.plane == SurfacePlane.WOOD && pleco.woodT < 0.34f) 0.14f else 0f
        val armorCol = TimeOfDayPalette.lerpColor(0xFF4F463C.toInt(), aqPal.fogColor, fog.coerceAtMost(0.55f)) or (0xFF shl 24)
        val bellyCol = TimeOfDayPalette.lerpColor(0xFFD2C4A8.toInt(), aqPal.fogColor, fog.coerceAtMost(0.55f)) or (0xFF shl 24)
        val spotCol = TimeOfDayPalette.lerpColor(0xFFC8B894.toInt(), aqPal.fogColor, fog.coerceAtMost(0.55f)) or (0xFF shl 24)
        val wag = sin(pleco.phase) * 2.4f * sc

        canvas.save()
        canvas.translate(px, py)

        when (pleco.plane) {
            SurfacePlane.FRONT_GLASS -> {
                // SPECTACULAR FRONT-GLASS VENTRAL VIEW: Wide armored head, bushy snout bristles, pulsing suction-cup mouth, spiny spotted anchor wings!
                canvas.rotate(pleco.angleRad * (180f / PI.toFloat()) + 90f)
                // Broad spiny spotted pectoral & pelvic fins pressed flat to glass
                fillPaint.color = armorCol
                strokePaint.color = aqPal.contour
                strokePaint.strokeWidth = 1.0f
                for (side in -1..1 step 2) {
                    shapePath.reset()
                    shapePath.moveTo(side * 6.5f * sc, -4.5f * sc)
                    shapePath.cubicTo(side * 19.5f * sc, -5.0f * sc, side * 19.0f * sc, 5.5f * sc, side * 11.5f * sc, 9.0f * sc)
                    shapePath.quadTo(side * 7.5f * sc, 6.0f * sc, side * 5.2f * sc, 2.5f * sc)
                    shapePath.close()
                    canvas.drawPath(shapePath, fillPaint)
                    canvas.drawPath(shapePath, strokePaint)
                    // Pelvic fins
                    shapePath.reset()
                    shapePath.moveTo(side * 4.2f * sc, 6.5f * sc)
                    shapePath.quadTo(side * 11.5f * sc, 9.0f * sc, side * 7.8f * sc, 14.5f * sc)
                    shapePath.lineTo(side * 3.0f * sc, 11.5f * sc)
                    shapePath.close()
                    canvas.drawPath(shapePath, fillPaint)
                    canvas.drawPath(shapePath, strokePaint)
                }

                // Sweeping armored caudal fin
                shapePath.reset()
                shapePath.moveTo(-2.2f * sc, 18.0f * sc)
                shapePath.lineTo(-6.8f * sc + wag, 27.5f * sc)
                shapePath.quadTo(wag * 0.6f, 24.5f * sc, 6.8f * sc + wag, 27.5f * sc)
                shapePath.lineTo(2.2f * sc, 18.0f * sc)
                shapePath.close()
                canvas.drawPath(shapePath, fillPaint)
                canvas.drawPath(shapePath, strokePaint)

                // Broad spade-shaped armored head + tapering body + pale ventral belly
                bodyPath.reset()
                bodyPath.moveTo(0f, -17.5f * sc)
                bodyPath.cubicTo(-10.5f * sc, -16.5f * sc, -10.5f * sc, 2.0f * sc, -2.5f * sc, 18.5f * sc)
                bodyPath.lineTo(2.5f * sc, 18.5f * sc)
                bodyPath.cubicTo(10.5f * sc, 2.0f * sc, 10.5f * sc, -16.5f * sc, 0f, -17.5f * sc)
                bodyPath.close()
                fillPaint.color = armorCol
                canvas.drawPath(bodyPath, fillPaint)
                canvas.drawPath(bodyPath, strokePaint)

                fillPaint.color = bellyCol
                canvas.drawOval(-6.8f * sc, -13.5f * sc, 6.8f * sc, 12.0f * sc, fillPaint)

                // Prominent Pulsing Fleshy Suction-Cup Oral Disc!!
                val pulse = 1.0f + sin(pleco.phase * 2.2f) * 0.12f
                fillPaint.color = 0xFFD9A696.toInt()
                canvas.drawOval(-5.4f * sc * pulse, -14.2f * sc, 5.4f * sc * pulse, -7.8f * sc, fillPaint)
                strokePaint.color = 0xAA7A4C40.toInt()
                strokePaint.strokeWidth = 1.1f
                canvas.drawOval(-3.4f * sc * pulse, -12.6f * sc, 3.4f * sc * pulse, -9.2f * sc, strokePaint)

                // Iconic Bristlenose Snout Tentacles fanning around the nose!
                strokePaint.color = spotCol
                strokePaint.strokeWidth = 1.15f
                for (b in -3..3) {
                    val bx = b * 2.2f * sc
                    canvas.drawLine(bx, -16.0f * sc, bx * 1.45f, -19.8f * sc - (3 - abs(b)) * 0.7f * sc, strokePaint)
                }
            }
            SurfacePlane.BACK_GLASS -> {
                // DORSAL ARMORED VIEW ON BACK GLASS (smaller & misted!)
                canvas.rotate(pleco.angleRad * (180f / PI.toFloat()) + 90f)
                fillPaint.color = armorCol
                for (side in -1..1 step 2) {
                    shapePath.reset()
                    shapePath.moveTo(side * 6.0f * sc, -4.0f * sc)
                    shapePath.quadTo(side * 18.0f * sc, -2.5f * sc, side * 11.0f * sc, 8.5f * sc)
                    shapePath.lineTo(side * 5.0f * sc, 2.5f * sc)
                    shapePath.close()
                    canvas.drawPath(shapePath, fillPaint)
                }
                shapePath.reset()
                shapePath.moveTo(-2.0f * sc, 17.5f * sc)
                shapePath.lineTo(-6.2f * sc + wag, 26.5f * sc)
                shapePath.lineTo(6.2f * sc + wag, 26.5f * sc)
                shapePath.lineTo(2.0f * sc, 17.5f * sc)
                shapePath.close()
                canvas.drawPath(shapePath, fillPaint)

                bodyPath.reset()
                bodyPath.moveTo(0f, -16.5f * sc)
                bodyPath.cubicTo(-9.8f * sc, -15.0f * sc, -9.2f * sc, 2.0f * sc, -2.2f * sc, 18.0f * sc)
                bodyPath.lineTo(2.2f * sc, 18.0f * sc)
                bodyPath.cubicTo(9.2f * sc, 2.0f * sc, 9.8f * sc, -15.0f * sc, 0f, -16.5f * sc)
                bodyPath.close()
                canvas.drawPath(bodyPath, fillPaint)
                // Pale spotted armored carapace pattern
                fillPaint.color = withAlpha(spotCol, 175)
                for (r in -2..3) {
                    canvas.drawCircle(-3.0f * sc, r * 4.2f * sc, 1.1f * sc, fillPaint)
                    canvas.drawCircle(3.0f * sc, r * 4.2f * sc + 1.5f * sc, 1.1f * sc, fillPaint)
                }
            }
            else -> {
                // SPIDERWOOD OR TRANSIT PROFILE VIEW (tall ribbed dorsal sail + spotted scutes + snout bristles!)
                val angDeg = if (pleco.plane == SurfacePlane.WOOD) {
                    evalWoodMainAng(pleco.woodT) * (180f / PI.toFloat())
                } else {
                    atan2(pleco.endY - pleco.startY, pleco.endX - pleco.startX) * (180f / PI.toFloat())
                }
                canvas.rotate(angDeg)
                if (pleco.plane == SurfacePlane.WOOD) canvas.scale(pleco.woodDir, 1f)
                else if (pleco.endX < pleco.startX) canvas.scale(1f, -1f)

                // Tall ribbed triangular Bristlenose dorsal sail fin
                shapePath.reset()
                shapePath.moveTo(1.5f * sc, -4.5f * sc)
                shapePath.quadTo(-6.5f * sc, -17.5f * sc, -15.5f * sc, -4.0f * sc)
                shapePath.close()
                fillPaint.color = armorCol
                canvas.drawPath(shapePath, fillPaint)
                strokePaint.color = aqPal.contour
                strokePaint.strokeWidth = 1.0f
                canvas.drawPath(shapePath, strokePaint)
                // Dorsal fin rays
                strokePaint.color = withAlpha(spotCol, 140)
                for (r in 0..3) {
                    canvas.drawLine((-1f - r * 3.5f) * sc, -4.2f * sc, (-4.5f - r * 3.2f) * sc, (-12.5f + r * 1.4f) * sc, strokePaint)
                }

                // Armored teardrop torso + crescent tail
                bodyPath.reset()
                bodyPath.moveTo(17.5f * sc, 1.5f * sc)
                bodyPath.quadTo(4.0f * sc, -8.5f * sc, -17.5f * sc, -1.5f * sc)
                bodyPath.lineTo(-26.5f * sc, -6.5f * sc + wag)
                bodyPath.quadTo(-23.5f * sc, wag * 0.5f, -25.5f * sc, 5.5f * sc + wag)
                bodyPath.lineTo(-17.0f * sc, 3.0f * sc)
                bodyPath.quadTo(3.5f * sc, 5.8f * sc, 17.5f * sc, 1.5f * sc)
                bodyPath.close()
                fillPaint.color = armorCol
                canvas.drawPath(bodyPath, fillPaint)
                strokePaint.color = aqPal.contour
                canvas.drawPath(bodyPath, strokePaint)

                // Flared spotted pectoral fin
                shapePath.reset()
                shapePath.moveTo(5.5f * sc, 3.0f * sc)
                shapePath.quadTo(0f, 11.5f * sc, -6.5f * sc, 7.5f * sc)
                shapePath.lineTo(-1.0f * sc, 3.2f * sc)
                shapePath.close()
                fillPaint.color = armorCol
                canvas.drawPath(shapePath, fillPaint)
                canvas.drawPath(shapePath, strokePaint)

                // Pale cream carapace spots + bushy snout bristles
                fillPaint.color = withAlpha(spotCol, 200)
                val sx = floatArrayOf(-12f, -6f, 0f, 6f, 11f, -9f, 3f)
                val sy = floatArrayOf(-1.2f, 1.0f, -2.2f, 0.5f, -1.0f, 1.6f, -0.5f)
                for (k in sx.indices) canvas.drawCircle(sx[k] * sc, sy[k] * sc, 1.15f * sc, fillPaint)
                strokePaint.color = spotCol
                strokePaint.strokeWidth = 1.05f
                canvas.drawLine(15.5f * sc, -0.5f * sc, 20.5f * sc, -3.5f * sc, strokePaint)
                canvas.drawLine(16.2f * sc, 0.8f * sc, 21.2f * sc, -0.5f * sc, strokePaint)
                canvas.drawLine(15.8f * sc, 2.0f * sc, 19.8f * sc, 3.8f * sc, strokePaint)
            }
        }
        canvas.restore()
    }

    private fun drawCorydoras(canvas: Canvas, c: CoryCatfish) {
        val waterSpan = (sandY - waterY).coerceAtLeast(60f)
        val cx = tankX + c.normX * tankW
        val cy = if (c.dashPhase > 0f) {
            waterY + c.normY * waterSpan
        } else {
            sandFloorYAtZ(c.zDepth) - tankH * 0.012f
        }
        val sc = (tankW * 0.0025f).coerceIn(0.85f, 2.6f) * perspScale(c.zDepth)
        val fog = depthFog(c.zDepth)
        val bodyCol = TimeOfDayPalette.lerpColor(0xFFD8CCB4.toInt(), aqPal.fogColor, fog) or (0xFF shl 24)
        val cheekHi = TimeOfDayPalette.lerpColor(0xFFF2E2BC.toInt(), aqPal.fogColor, fog) or (0xFF shl 24)
        val maskCol = TimeOfDayPalette.lerpColor(0xFF28231E.toInt(), aqPal.fogColor, fog) or (0xFF shl 24)
        val finCol = withAlpha(TimeOfDayPalette.lerpColor(0xFFE6DCC8.toInt(), aqPal.fogColor, fog), 178)
        val wag = sin(c.phase) * 2.2f * sc
        val pitch = if (c.dashPhase > 0f) (if (c.dashPhase < 0.5f) -38f else 38f) else (sin(c.phase * 0.65f) * 4.5f)

        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(c.dir, 1f)
        canvas.rotate(pitch)

        // Tall multi-rayed Panda Corydoras dorsal fin with bold black patch!
        shapePath.reset()
        shapePath.moveTo(2.5f * sc, -5.2f * sc)
        shapePath.quadTo(-2.5f * sc, -14.2f * sc, -7.8f * sc, -4.2f * sc)
        shapePath.close()
        fillPaint.color = maskCol
        canvas.drawPath(shapePath, fillPaint)
        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = 0.9f
        canvas.drawPath(shapePath, strokePaint)

        // Forked translucent tail fin
        shapePath.reset()
        shapePath.moveTo(-10.0f * sc, 0f)
        shapePath.lineTo(-17.5f * sc, -5.8f * sc + wag)
        shapePath.lineTo(-14.2f * sc, wag * 0.45f)
        shapePath.lineTo(-17.5f * sc, 4.8f * sc + wag)
        shapePath.close()
        fillPaint.color = finCol
        canvas.drawPath(shapePath, fillPaint)
        canvas.drawPath(shapePath, strokePaint)

        // Arched armored Panda Cory body + flat bottom-skimming belly
        bodyPath.reset()
        bodyPath.moveTo(13.2f * sc, 2.2f * sc)
        bodyPath.cubicTo(9.5f * sc, -6.5f * sc, 0.5f * sc, -8.5f * sc, -10.5f * sc, -1.6f * sc)
        bodyPath.lineTo(-10.5f * sc, 2.4f * sc)
        bodyPath.quadTo(2.5f * sc, 4.8f * sc, 13.2f * sc, 2.2f * sc)
        bodyPath.close()
        fillPaint.color = bodyCol
        canvas.drawPath(bodyPath, fillPaint)
        canvas.drawPath(bodyPath, strokePaint)

        // Shimmering metallic bronze-cream gill cheek + armor scute chevron lines
        fillPaint.color = cheekHi
        canvas.drawOval(1.5f * sc, -3.2f * sc, 7.2f * sc, 2.6f * sc, fillPaint)
        strokePaint.color = withAlpha(aqPal.contour, 75)
        strokePaint.strokeWidth = 0.75f
        for (k in 0..3) {
            val sx = (-1f - k * 2.4f) * sc
            canvas.drawLine(sx, -3.0f * sc, sx - 1.2f * sc, 2.0f * sc, strokePaint)
        }

        // Signature Velvet-Black Panda Eye-Band & Tail-Peduncle Blotch!
        fillPaint.color = maskCol
        canvas.drawOval(4.8f * sc, -5.8f * sc, 8.6f * sc, 1.8f * sc, fillPaint)
        canvas.drawOval(-10.8f * sc, -2.5f * sc, -7.2f * sc, 2.5f * sc, fillPaint)

        // Eye glint + translucent pectoral fin + 3 pairs of twitching sand-foraging barbels!
        fillPaint.color = 0xE6FFFFFF.toInt()
        canvas.drawCircle(7.0f * sc, -2.8f * sc, 0.9f * sc, fillPaint)
        fillPaint.color = finCol
        canvas.drawOval(0f, 1.5f * sc, 6.2f * sc, 5.2f * sc, fillPaint)

        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = 0.9f
        canvas.drawLine(12.2f * sc, 2.0f * sc, 16.8f * sc, 4.8f * sc + wag * 0.25f, strokePaint)
        canvas.drawLine(11.6f * sc, 2.6f * sc, 15.6f * sc, 6.2f * sc - wag * 0.25f, strokePaint)
        canvas.drawLine(10.8f * sc, 2.8f * sc, 14.0f * sc, 6.6f * sc + wag * 0.2f, strokePaint)
        canvas.restore()
    }

    private fun drawCherryShrimp(canvas: Canvas, s: CherryShrimp) {
        val sx: Float
        val sy: Float
        val angDeg: Float
        if (s.hopProgress > 0f) {
            val u = s.hopProgress.coerceIn(0f, 1f)
            sx = s.hopStartX + (s.hopEndX - s.hopStartX) * u
            sy = s.hopStartY + (s.hopEndY - s.hopStartY) * u - sin(u * PI.toFloat()) * (tankH * 0.046f)
            angDeg = -20f * cos(u * PI.toFloat()) * s.dir
        } else {
            sx = evalShrimpHomeX(s.homeKind, s.t) + if (s.homeKind >= 2) sin(simTime * 0.45f + s.phase) * tankW * 0.010f else 0f
            sy = evalShrimpHomeY(s.homeKind, s.t)
            angDeg = when (s.homeKind) {
                0 -> evalWoodMainAng(s.t) * (180f / PI.toFloat()) * 0.45f
                1 -> evalWoodUpperAng(s.t) * (180f / PI.toFloat()) * 0.45f
                else -> 0f
            }
        }

        val sc = (tankW * 0.0023f).coerceIn(0.80f, 2.4f) * perspScale(s.zDepth)
        val fog = depthFog(s.zDepth)
        val rubyCol = withAlpha(TimeOfDayPalette.lerpColor(0xFFE83E2E.toInt(), aqPal.fogColor, fog), 235)
        val rubyHi = withAlpha(TimeOfDayPalette.lerpColor(0xFFFF8C76.toInt(), aqPal.fogColor, fog), 210)

        canvas.save()
        canvas.translate(sx, sy)
        canvas.rotate(angDeg)
        canvas.scale(s.dir, 1f)

        // Fanned Uropod Tail
        shapePath.reset()
        shapePath.moveTo(-6.2f * sc, 0.2f * sc)
        shapePath.lineTo(-10.5f * sc, 1.5f * sc)
        shapePath.lineTo(-9.2f * sc, 3.8f * sc)
        shapePath.lineTo(-5.5f * sc, 1.8f * sc)
        shapePath.close()
        fillPaint.color = rubyHi
        canvas.drawPath(shapePath, fillPaint)

        // Multi-Segment Arched Translucent Ruby Carapace (Head + 4 Abdomen Plates!)
        bodyPath.reset()
        bodyPath.moveTo(7.8f * sc, -0.2f * sc)
        bodyPath.cubicTo(4.2f * sc, -4.6f * sc, -1.8f * sc, -4.4f * sc, -6.8f * sc, 0.4f * sc)
        bodyPath.quadTo(-1.2f * sc, 2.2f * sc, 7.8f * sc, -0.2f * sc)
        bodyPath.close()
        fillPaint.color = rubyCol
        canvas.drawPath(bodyPath, fillPaint)

        // Pale dorsal highlight stripe + 4 crisp carapace segment lines!
        strokePaint.color = rubyHi
        strokePaint.strokeWidth = 1.0f * sc
        detailPath.reset()
        detailPath.moveTo(5.2f * sc, -1.8f * sc)
        detailPath.quadTo(0.8f * sc, -3.5f * sc, -4.5f * sc, -0.8f * sc)
        canvas.drawPath(detailPath, strokePaint)

        strokePaint.color = aqPal.contour
        strokePaint.strokeWidth = 0.72f
        canvas.drawPath(bodyPath, strokePaint)
        for (seg in 0..2) {
            val segX = (2.4f - seg * 2.5f) * sc
            canvas.drawLine(segX, -3.0f * sc, segX - 0.8f * sc, 1.2f * sc, strokePaint)
        }

        // Articulated bent walking legs & fluttering abdominal swimmerets
        val legWig = sin(s.phase * 2.4f) * 1.1f * sc
        strokePaint.color = rubyHi
        strokePaint.strokeWidth = 0.75f * sc
        for (l in 0..3) {
            val lx = (5.0f - l * 2.4f) * sc
            val tipX = lx + (if (l % 2 == 0) legWig else -legWig)
            canvas.drawLine(lx, 1.0f * sc, lx + 0.8f * sc, 2.8f * sc, strokePaint)
            canvas.drawLine(lx + 0.8f * sc, 2.8f * sc, tipX, 4.4f * sc, strokePaint)
        }

        // Twin long sweeping translucent whip antennae!
        val antWave = sin(s.phase) * 1.8f * sc
        strokePaint.color = withAlpha(0xFFFFE4D8.toInt(), if (fog > 0.3f) 140 else 205)
        strokePaint.strokeWidth = 0.78f
        detailPath.reset()
        detailPath.moveTo(7.4f * sc, -0.6f * sc)
        detailPath.quadTo(12.2f * sc, -4.8f * sc, 16.5f * sc, -3.4f * sc + antWave)
        canvas.drawPath(detailPath, strokePaint)
        detailPath.reset()
        detailPath.moveTo(7.0f * sc, 0.2f * sc)
        detailPath.quadTo(11.5f * sc, -2.2f * sc, 14.8f * sc, -0.2f * sc - antWave * 0.7f)
        canvas.drawPath(detailPath, strokePaint)

        // Bright dark stalked eye dot
        fillPaint.color = 0xFF100E0E.toInt()
        canvas.drawCircle(6.2f * sc, -1.2f * sc, 0.88f * sc, fillPaint)
        canvas.restore()
    }

    private fun drawMysterySnail(canvas: Canvas) {
        val waterSpan = (sandY - waterY).coerceAtLeast(60f)
        val sx = tankX + snail.normX * tankW
        val sy = if (snail.plane == SurfacePlane.FRONT_GLASS || snail.plane == SurfacePlane.BACK_GLASS) {
            waterY + snail.normY * waterSpan
        } else {
            sandFloorYAtZ(snail.zDepth) + tankH * 0.008f
        }
        val sc = (tankW * 0.0024f).coerceIn(0.85f, 2.4f) * perspScale(snail.zDepth)
        val fog = depthFog(snail.zDepth)
        val shellCol = TimeOfDayPalette.lerpColor(0xFFDDA846.toInt(), aqPal.fogColor, fog) or (0xFF shl 24)
        val shellDark = TimeOfDayPalette.lerpColor(0xFFA67426.toInt(), aqPal.fogColor, fog) or (0xFF shl 24)
        val footCol = withAlpha(TimeOfDayPalette.lerpColor(0xFFE6D8BC.toInt(), aqPal.fogColor, fog), 222)

        canvas.save()
        canvas.translate(sx, sy)

        if (snail.plane == SurfacePlane.FRONT_GLASS) {
            // FRONT-GLASS VENTRAL VIEW: Translucent rippling muscular foot + tiny pulsing radula mouth + golden shell behind!
            canvas.rotate(snail.angleRad * (180f / PI.toFloat()) + 90f)
            // Golden spiral shell peeking behind the gliding foot
            fillPaint.color = shellCol
            canvas.drawCircle(1.8f * sc, 1.2f * sc, 6.4f * sc, fillPaint)
            strokePaint.color = aqPal.contour; strokePaint.strokeWidth = 0.9f
            canvas.drawCircle(1.8f * sc, 1.2f * sc, 6.4f * sc, strokePaint)

            // Rippling pale translucent muscular foot pressed flat to the front glass!
            fillPaint.color = footCol
            canvas.drawOval(-4.4f * sc, -9.2f * sc, 4.4f * sc, 8.6f * sc, fillPaint)
            strokePaint.color = withAlpha(shellDark, 120)
            canvas.drawOval(-4.4f * sc, -9.2f * sc, 4.4f * sc, 8.6f * sc, strokePaint)

            if (snail.retractTimer <= 0f) {
                // Tiny pulsing rasping radula mouth + long sweeping sensory tentacles!
                val mPulse = 1f + sin(snail.phase * 2.6f) * 0.2f
                fillPaint.color = 0xFFD49A8E.toInt()
                canvas.drawOval(-1.6f * sc * mPulse, -8.2f * sc, 1.6f * sc * mPulse, -6.0f * sc, fillPaint)
                strokePaint.color = footCol
                strokePaint.strokeWidth = 0.95f
                val sway = sin(snail.phase) * 2.0f * sc
                detailPath.reset()
                detailPath.moveTo(-2.2f * sc, -8.8f * sc); detailPath.quadTo(-5.5f * sc, -13.5f * sc, -7.2f * sc + sway, -16.2f * sc)
                canvas.drawPath(detailPath, strokePaint)
                detailPath.reset()
                detailPath.moveTo(2.2f * sc, -8.8f * sc); detailPath.quadTo(5.5f * sc, -13.5f * sc, 7.2f * sc - sway, -16.2f * sc)
                canvas.drawPath(detailPath, strokePaint)
            }
        } else {
            // PROFILE / BACK-GLASS VIEW: Multi-whorl golden-amber logarithmic spiral shell + sweeping tentacles!
            if (snail.plane == SurfacePlane.BACK_GLASS) {
                canvas.rotate(snail.angleRad * (180f / PI.toFloat()) + 90f)
            } else {
                canvas.scale(snail.dir, 1f)
            }
            if (snail.retractTimer <= 0f) {
                fillPaint.color = footCol
                canvas.drawOval(-8.2f * sc, -1.0f * sc, 9.2f * sc, 3.8f * sc, fillPaint)
                strokePaint.color = footCol
                strokePaint.strokeWidth = 0.95f
                val sway = sin(snail.phase) * 1.6f * sc
                detailPath.reset()
                detailPath.moveTo(7.4f * sc, 0f); detailPath.quadTo(11.5f * sc, -2.5f * sc, 14.2f * sc, -4.5f * sc + sway)
                canvas.drawPath(detailPath, strokePaint)
                detailPath.reset()
                detailPath.moveTo(7.8f * sc, 1.0f * sc); detailPath.quadTo(11.2f * sc, -0.5f * sc, 13.0f * sc, -1.8f * sc - sway)
                canvas.drawPath(detailPath, strokePaint)
            }
            // Sculpted multi-whorl golden spiral shell with sepia banding & top highlight
            fillPaint.color = shellCol
            canvas.drawOval(-6.8f * sc, -9.2f * sc, 5.2f * sc, 2.2f * sc, fillPaint)
            fillPaint.color = 0x45FFF8DC
            canvas.drawOval(-4.8f * sc, -8.2f * sc, 2.2f * sc, -3.5f * sc, fillPaint)
            strokePaint.color = shellDark
            strokePaint.strokeWidth = 1.05f
            canvas.drawOval(-6.8f * sc, -9.2f * sc, 5.2f * sc, 2.2f * sc, strokePaint)
            canvas.drawCircle(-2.2f * sc, -3.5f * sc, 3.0f * sc, strokePaint)
            canvas.drawCircle(-3.0f * sc, -3.5f * sc, 1.2f * sc, strokePaint)
        }
        canvas.restore()
    }

    private fun drawSandPuffs(canvas: Canvas) {
        for (sp in sandPuffs) {
            if (!sp.active) continue
            val life = 1f - (sp.age / sp.maxAge).coerceIn(0f, 1f)
            fillPaint.color = (aqPal.sandTop and 0x00FFFFFF) or ((life * 125f).toInt().coerceIn(0, 140) shl 24)
            canvas.drawCircle(sp.x, sp.y, (tankW * 0.0045f * (1.2f - life * 0.4f)).coerceAtLeast(1.4f), fillPaint)
        }
    }

    // =========================================================================
    // FLOATING CANOPY, BUBBLE NEST, BUBBLES, GLASS FRAME & RARE CAT
    // =========================================================================

    private fun drawFloatingCanopyAndBubbleNest(canvas: Canvas) {
        val padRx = tankW * 0.025f
        val padRy = padRx * 0.28f

        for (i in floaterLiveFracs.indices) {
            val frac = floaterLiveFracs[i]
            val fx = tankX + frac * tankW
            val wIdx = (frac * (waveN - 1)).roundToInt().coerceIn(0, waveN - 1)
            val fy = waterY + waveH[wIdx]

            strokePaint.color = 0x7284B494
            strokePaint.strokeWidth = 0.9f
            for (r in -1..1) {
                val sway = sin(simTime * 1.0f + i + r * 0.8f) * (tankW * 0.012f)
                val rootLen = tankH * (0.092f + (i % 2) * 0.036f)
                detailPath.reset()
                detailPath.moveTo(fx + r * padRx * 0.36f, fy + padRy)
                detailPath.quadTo(fx + r * padRx * 0.45f + sway * 0.5f, fy + rootLen * 0.55f, fx + r * padRx * 0.55f + sway, fy + rootLen)
                canvas.drawPath(detailPath, strokePaint)
            }

            val rx = padRx * (0.95f + (i % 2) * 0.28f)
            fillPaint.color = if (i % 2 == 0) aqPal.anubiasHi else aqPal.plantFront
            canvas.drawOval(fx - rx, fy - padRy, fx + rx, fy + padRy, fillPaint)
            strokePaint.color = aqPal.contour
            strokePaint.strokeWidth = 0.85f
            canvas.drawOval(fx - rx, fy - padRy, fx + rx, fy + padRy, strokePaint)
        }

        // Dynamic Living Betta Surface Bubble Nest tucked right under the drifting right Frogbit canopy!
        strokePaint.color = aqPal.bubbleColor
        strokePaint.strokeWidth = 0.88f
        val nestBaseX = tankX + (floaterLiveFracs[3] + 0.025f) * tankW
        val nestR = (tankW * 0.0044f).coerceIn(1.5f, 3.1f)
        val count = bubbleNestCount.coerceIn(6, 16)
        for (bn in 0 until count) {
            val colIdx = bn % 8
            val rowIdx = bn / 8
            val bx = nestBaseX + colIdx * nestR * 1.90f + rowIdx * nestR * 0.9f
            val by = waterY + nestR * (1.2f + (colIdx % 2) * 0.75f + rowIdx * 1.45f)
            canvas.drawCircle(bx, by, nestR * (0.92f + (bn % 3) * 0.12f), strokePaint)
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
            val ps = perspScale(p.z)
            if (p.isWafer) {
                val rx = (tankW * 0.012f * ps).coerceIn(3.8f, 10f)
                val ry = rx * 0.48f
                fillPaint.color = 0xFF5A8A4E.toInt()
                canvas.drawOval(p.x - rx, p.y - ry, p.x + rx, p.y + ry, fillPaint)
                strokePaint.color = aqPal.contour
                canvas.drawOval(p.x - rx, p.y - ry, p.x + rx, p.y + ry, strokePaint)
            } else {
                fillPaint.color = 0xFFD99642.toInt()
                canvas.drawCircle(p.x, p.y, (tankW * 0.0056f * ps).coerceIn(2.0f, 4.8f), fillPaint)
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

        // Subtle diagonal low-iron glass reflection sheen
        if (!aqPal.isAmoled) {
            fillPaint.color = 0x0CFFFFFF
            shapePath.reset()
            shapePath.moveTo(tankX + tankW * 0.08f, tankY + 2f)
            shapePath.lineTo(tankX + tankW * 0.17f, tankY + 2f)
            shapePath.lineTo(tankX + 2f, tankY + tankH * 0.34f)
            shapePath.lineTo(tankX + 2f, tankY + tankH * 0.18f)
            shapePath.close()
            canvas.drawPath(shapePath, fillPaint)
        }

        strokePaint.color = aqPal.glassEdge
        strokePaint.strokeWidth = (tankW * 0.0046f).coerceIn(1.8f, 3.6f)
        canvas.drawRect(tankX, tankY, tankX + tankW, tankY + tankH, strokePaint)
    }

    private fun drawCatBehindGlass(canvas: Canvas) {
        if (catRise <= 0.01f) return
        val rimX = if (catSide > 0f) tankX + tankW * 0.76f else tankX + tankW * 0.24f
        val catUnit = (tankW * 0.075f).coerceIn(24f, 72f)
        val headY = tankY - catUnit * 0.12f + (1f - catRise) * (catUnit * 2.3f)

        if (catRise > 0.20f) {
            canvas.save()
            canvas.clipRect(tankX + 2f, tankY + 2f, tankX + tankW - 2f, tankY + tankH - 2f)
            fillPaint.color = (aqPal.catBody and 0x00FFFFFF) or ((catRise * 58f).toInt().coerceIn(0, 66) shl 24)
            canvas.drawOval(
                rimX + catSide * catUnit * 0.30f - catUnit * 1.45f,
                tankY - catUnit * 0.25f,
                rimX + catSide * catUnit * 0.30f + catUnit * 1.45f,
                tankY + catUnit * 5.2f,
                fillPaint
            )
            canvas.restore()
        }

        canvas.save()
        canvas.clipRect(0f, 0f, lastWidth, tankY + 2f)
        canvas.translate(rimX, headY)
        canvas.scale(catSide, 1f)
        canvas.rotate(catHeadTiltDeg)

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

        val lx = catEyeLookX * catSide * (catUnit * 0.095f)
        val ly = catEyeLookY * (catUnit * 0.070f)
        val isBlinking = catBlinkDur > 0f
        for (side in -1..1 step 2) {
            val ex = side * catUnit * 0.34f
            val ey = -catUnit * 0.32f
            val erx = catUnit * 0.185f; val ery = catUnit * 0.145f
            if (isBlinking) {
                // Slow feline amber eye blink!
                strokePaint.color = aqPal.catEye
                strokePaint.strokeWidth = (catUnit * 0.042f).coerceIn(1.2f, 2.5f)
                canvas.drawLine(ex - erx * 0.9f, ey, ex + erx * 0.9f, ey, strokePaint)
            } else {
                fillPaint.color = aqPal.catEye
                canvas.drawOval(ex - erx, ey - ery, ex + erx, ey + ery, fillPaint)
                fillPaint.color = 0xFF090C10.toInt()
                canvas.drawOval(ex + lx - erx * 0.32f, ey + ly - ery * 0.78f, ex + lx + erx * 0.32f, ey + ly + ery * 0.78f, fillPaint)
                fillPaint.color = 0xFFFFFFFF.toInt()
                canvas.drawCircle(ex + lx + erx * 0.18f, ey + ly - ery * 0.28f, erx * 0.16f, fillPaint)
            }
        }

        strokePaint.color = aqPal.catRim
        strokePaint.strokeWidth = (catUnit * 0.024f).coerceIn(0.8f, 1.6f)
        for (side in -1..1 step 2) {
            canvas.drawLine(side * catUnit * 0.54f, -catUnit * 0.04f, side * catUnit * 1.15f, -catUnit * 0.14f, strokePaint)
            canvas.drawLine(side * catUnit * 0.54f, catUnit * 0.06f, side * catUnit * 1.18f, catUnit * 0.12f, strokePaint)
        }
        canvas.restore()
    }

    /** Slowly swishing feline silhouette tail peeking around the lower side edge of the tank while stalking! */
    private fun drawCatTailPeek(canvas: Canvas) {
        if (catRise < 0.28f) return
        val edgeX = if (catSide > 0f) tankX + tankW else tankX
        val baseY = tankY + tankH - tankH * 0.08f
        val tailLen = (tankW * 0.055f) * catRise
        val swish = sin(simTime * 2.1f) * (tailLen * 0.55f)

        detailPath.reset()
        detailPath.moveTo(edgeX - catSide * 2f, baseY)
        detailPath.cubicTo(
            edgeX + catSide * tailLen * 0.85f, baseY - tailLen * 0.25f,
            edgeX + catSide * (tailLen * 0.95f + swish * 0.35f), baseY - tailLen * 1.15f,
            edgeX + catSide * (tailLen * 0.45f + swish), baseY - tailLen * 1.85f
        )
        strokePaint.color = aqPal.catBody
        strokePaint.strokeWidth = (tankW * 0.022f).coerceIn(6f, 16f)
        canvas.drawPath(detailPath, strokePaint)
        strokePaint.color = withAlpha(aqPal.catRim, 110)
        strokePaint.strokeWidth = 1.0f
        canvas.drawPath(detailPath, strokePaint)
    }

    private fun drawCatPawOverlay(canvas: Canvas) {
        if (catRise < 0.45f) return
        val rimX = if (catSide > 0f) tankX + tankW * 0.76f else tankX + tankW * 0.24f
        val catUnit = (tankW * 0.075f).coerceIn(24f, 72f)
        val reach = catPawDip
        val waterSpan = (sandY - waterY).coerceAtLeast(60f)

        if (catActionIsGlassPat) {
            // MODE B: Front-Glass Curiosity Paw-Pat (soft paw with 4 tiny toe-beans pressed gently to front glass!)
            if (reach <= 0.05f) return
            val patX = rimX - catSide * (catUnit * (0.62f + reach * 0.42f))
            val targetPatY = waterY + catGlassPatYFrac * waterSpan
            val patY = tankY + (targetPatY - tankY) * reach

            detailPath.reset()
            detailPath.moveTo(rimX - catSide * catUnit * 0.22f, tankY - catUnit * 0.04f)
            detailPath.quadTo(rimX - catSide * catUnit * 0.72f, (tankY + patY) * 0.5f, patX, patY)
            strokePaint.color = aqPal.catBody
            strokePaint.strokeWidth = (catUnit * 0.40f).coerceIn(9f, 26f)
            canvas.drawPath(detailPath, strokePaint)

            val pr = catUnit * 0.32f
            fillPaint.color = aqPal.catBody
            canvas.drawCircle(patX, patY, pr, fillPaint)
            strokePaint.color = aqPal.catRim
            strokePaint.strokeWidth = (catUnit * 0.036f).coerceIn(1.0f, 2.2f)
            canvas.drawCircle(patX, patY, pr, strokePaint)

            // Cute soft pink/blush toe-beans & main paw pad pressed against the front glass!
            if (reach > 0.60f) {
                val beanAlpha = (((reach - 0.60f) * 2.5f).coerceIn(0f, 0.88f) * 255f).toInt()
                fillPaint.color = (0x00E8A4A8) or (beanAlpha shl 24)
                canvas.drawOval(patX - pr * 0.42f, patY + pr * 0.02f, patX + pr * 0.42f, patY + pr * 0.56f, fillPaint)
                for (toe in -1..2) {
                    val tx = patX + (toe - 0.5f) * pr * 0.42f
                    val ty = patY - pr * (0.34f - abs(toe - 0.5f) * 0.08f)
                    canvas.drawCircle(tx, ty, pr * 0.16f, fillPaint)
                }
                // Whisper-subtle glass tap shimmer ring!
                val tapA = (((reach - 0.68f) * 2.2f).coerceIn(0f, 0.55f) * 255f).toInt()
                if (tapA > 8) {
                    strokePaint.color = (0x00FFFFFF) or (tapA shl 24)
                    strokePaint.strokeWidth = 1.2f
                    canvas.drawCircle(patX, patY, pr * (1.25f + (reach - 0.68f) * 1.4f), strokePaint)
                }
            }
        } else {
            // MODE A: Top-Rim Surface Water Paw-Dip (creates spring-wave ripples & concentric rings!)
            val pawX = rimX - catSide * (catUnit * (0.76f + reach * 0.38f))
            val pawY = tankY + catUnit * 0.10f + reach * (waterY - tankY + catUnit * 0.12f)

            detailPath.reset()
            detailPath.moveTo(rimX - catSide * catUnit * 0.24f, tankY - catUnit * 0.06f)
            detailPath.quadTo(rimX - catSide * catUnit * 0.88f, tankY - catUnit * 0.26f + reach * catUnit * 0.25f, pawX, pawY)
            strokePaint.color = aqPal.catBody
            strokePaint.strokeWidth = (catUnit * 0.42f).coerceIn(10f, 28f)
            canvas.drawPath(detailPath, strokePaint)

            val prx = catUnit * 0.28f; val pry = catUnit * 0.21f
            fillPaint.color = aqPal.catBody
            canvas.drawOval(pawX - prx, pawY - pry + 2f, pawX + prx, pawY + pry + 2f, fillPaint)
            strokePaint.color = aqPal.catRim
            strokePaint.strokeWidth = (catUnit * 0.036f).coerceIn(1.0f, 2.2f)
            canvas.drawOval(pawX - prx, pawY - pry + 2f, pawX + prx, pawY + pry + 2f, strokePaint)

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
    }

    private fun withAlpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)

    private fun sqrtSafe(v: Float): Float = if (v <= 0f) 0f else sqrt(v)

    companion object {
        private const val SPINE_N = 12
    }
}

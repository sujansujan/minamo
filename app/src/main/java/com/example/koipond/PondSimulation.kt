package com.example.koipond

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * 8 distinct aquatic species with unique body aspect ratios, fin profiles, and shader patterns.
 */
enum class FishSpecies(
    val id: Int,
    val lengthMin: Float,
    val lengthMax: Float,
    val heightAspect: Float, // relative vertical tallness (e.g. Angelfish/Idol > Clownfish > Tetra)
    val finScale: Float,     // dorsal/caudal fin extension multiplier
    val baseSpeed: Float,
    val swimFreq: Float,
    val isSchoolingDarter: Boolean
) {
    CLOWNFISH(0, 0.11f, 0.145f, 1.15f, 1.05f, 0.14f, 4.2f, false),
    BLUE_TANG(1, 0.12f, 0.160f, 1.30f, 1.00f, 0.16f, 4.0f, false),
    MOORISH_IDOL(2, 0.13f, 0.175f, 1.65f, 1.75f, 0.13f, 3.3f, false),
    KOI_CARP(3, 0.14f, 0.185f, 0.95f, 1.15f, 0.12f, 3.1f, false),
    NEON_TETRA(4, 0.065f, 0.088f, 0.85f, 0.90f, 0.21f, 5.8f, true),
    ROYAL_BETTA(5, 0.11f, 0.145f, 1.15f, 1.85f, 0.10f, 2.9f, false),
    MANDARIN_FISH(6, 0.10f, 0.135f, 1.10f, 1.30f, 0.12f, 3.8f, false),
    SILVER_MINNOW(7, 0.060f, 0.082f, 0.78f, 0.85f, 0.23f, 6.2f, true);

    companion object {
        val heroSpecies = arrayOf(CLOWNFISH, BLUE_TANG, MOORISH_IDOL, KOI_CARP, ROYAL_BETTA, MANDARIN_FISH)
        val schoolingSpecies = arrayOf(NEON_TETRA, SILVER_MINNOW)
    }
}

class FishAgent(
    val id: Int,
    var isSmallSchooler: Boolean,
    var speciesId: Int,
    var x: Float,
    var y: Float,           // Normalized screen Y in [-0.82 .. +0.10] (strictly underwater below the split waterline)
    var zDepth: Float,      // 0.12 (foreground crisp) .. 0.85 (deep background)
    var heading: Float,     // 3D yaw angle in XZ plane: 0 = swimming right (+X), PI = swimming left (-X)
    var pitch: Float,       // vertical pitch angle [-0.35 .. +0.35]
    var speed: Float,
    var baseSpeed: Float,
    var bodyLength: Float,  // Properly scaled for phone screens (0.06 .. 0.18 of width)
    var bodyHeight: Float,
    var finScale: Float,
    var patternSeed: Float,
    var swimPhase: Float,
    var swimFreq: Float,
    var smoothedCurvature: Float,
    var targetX: Float,
    var targetY: Float,
    var targetZ: Float,
    var startleBoost: Float = 0f
)

class VallisneriaBlade(
    val rootX: Float,
    val rootY: Float,
    val length: Float,
    val width: Float,
    val baseAngle: Float,
    val phase: Float,
    val hueShift: Float,
    val depthZ: Float
)

class RippleWave(
    var x: Float = 0f,
    var y: Float = 0f,
    var age: Float = 99f,
    var maxDuration: Float = 3.2f,
    var amplitude: Float = 0f
)

class ParticleMote(
    var x: Float,
    var y: Float,
    var z: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var isBubble: Boolean,
    var phase: Float
)

/**
 * Split-view ("Over-Under") Phone Ecosystem Simulation:
 * - Waterline sits at screen NDC Y = +0.22 (top ~39% is scenic sunlit sky, distant hills, and floating surface pads;
 *   bottom ~61% is crystal-clear cross-section underwater world).
 * - Calling randomizeEcosystemOnUnlock() generates a fresh biome mix of fish species and sizes every time
 *   the user unlocks their phone.
 */
class PondSimulation {

    val maxHeroFish = 9
    val maxSchoolFish = 7
    val totalMaxFish = maxHeroFish + maxSchoolFish

    // Waterline in NDC Y [-1..+1]: +0.22 puts the split surface just above center
    val waterlineNdcY = 0.22f

    private var unlockCounter = 1L

    val fish = Array(totalMaxFish) { idx ->
        FishAgent(
            id = idx,
            isSmallSchooler = idx >= maxHeroFish,
            speciesId = idx % 8,
            x = 0f,
            y = -0.3f,
            zDepth = 0.3f,
            heading = 0f,
            pitch = 0f,
            speed = 0.14f,
            baseSpeed = 0.14f,
            bodyLength = 0.13f,
            bodyHeight = 0.13f,
            finScale = 1.0f,
            patternSeed = idx * 1.7f,
            swimPhase = 0f,
            swimFreq = 3.8f,
            smoothedCurvature = 0f,
            targetX = 0.4f,
            targetY = -0.3f,
            targetZ = 0.3f
        )
    }

    // Underwater swaying seagrass & kelp rooted along the sloped riverbed at the bottom
    val vallisneriaBlades = Array(28) { idx ->
        val rand = Random(idx * 4177 + 13)
        val xPos = -0.95f + (idx / 27.0f) * 1.90f + (rand.nextFloat() - 0.5f) * 0.08f
        val depthZ = 0.18f + (idx % 4) * 0.18f
        val rootY = -0.96f + depthZ * 0.12f
        VallisneriaBlade(
            rootX = xPos,
            rootY = rootY,
            length = 0.22f + rand.nextFloat() * 0.28f,
            width = 0.016f + rand.nextFloat() * 0.010f,
            baseAngle = 1.570796f + (rand.nextFloat() - 0.5f) * 0.22f, // Grows upward (+Y)
            phase = rand.nextFloat() * 6.28f,
            hueShift = rand.nextFloat(),
            depthZ = depthZ
        )
    }

    val maxRipples = 8
    val ripples = Array(maxRipples) { RippleWave() }
    private var nextRippleIdx = 0
    private var surfaceDropTimer = 3.0f

    val maxParticles = 48
    val particles = Array(maxParticles) { idx ->
        val r = Random(idx * 6151 + 99)
        val isBubble = idx < 12
        ParticleMote(
            x = (r.nextFloat() - 0.5f) * 1.9f,
            y = -0.92f + r.nextFloat() * 1.08f, // Strictly below waterline
            z = 0.15f + r.nextFloat() * 0.75f,
            vx = (r.nextFloat() - 0.5f) * 0.016f,
            vy = if (isBubble) (0.08f + r.nextFloat() * 0.06f) else (r.nextFloat() - 0.5f) * 0.012f,
            size = if (isBubble) (0.008f + r.nextFloat() * 0.007f) else (0.004f + r.nextFloat() * 0.004f),
            isBubble = isBubble,
            phase = r.nextFloat() * 6.28f
        )
    }

    var simulationTime = 0f
        private set

    init {
        randomizeEcosystemOnUnlock(System.nanoTime())
    }

    /**
     * Picks a fresh, cohesive combination of fish species, sizes, and depths every time
     * the user unlocks or wakes their phone.
     */
    fun randomizeEcosystemOnUnlock(seedInput: Long = System.nanoTime()) {
        unlockCounter++
        val rand = Random(seedInput xor (unlockCounter * 0x9E3779B97F4A7C15UL.toLong()))

        // Pick 2 or 3 hero species for this unlock session so the scene feels curated yet surprising
        val heroPool = FishSpecies.heroSpecies
        val primaryHero = heroPool[rand.nextInt(heroPool.size)]
        val secondaryHero = heroPool[rand.nextInt(heroPool.size)]
        val tertiaryHero = heroPool[rand.nextInt(heroPool.size)]
        val schoolSpecies = FishSpecies.schoolingSpecies[rand.nextInt(FishSpecies.schoolingSpecies.size)]

        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            val isSchooler = i >= maxHeroFish
            f.isSmallSchooler = isSchooler

            val sp = if (isSchooler) {
                schoolSpecies
            } else {
                when (i % 3) {
                    0 -> primaryHero
                    1 -> secondaryHero
                    else -> tertiaryHero
                }
            }

            f.speciesId = sp.id
            f.zDepth = if (isSchooler) {
                0.22f + rand.nextFloat() * 0.45f
            } else {
                0.16f + (i.toFloat() / maxHeroFish.toFloat()) * 0.62f
            }

            // Perspective scale: deeper fish appear naturally smaller
            val depthScale = 1.05f - f.zDepth * 0.42f
            val baseLen = sp.lengthMin + rand.nextFloat() * (sp.lengthMax - sp.lengthMin)
            f.bodyLength = baseLen * depthScale
            f.bodyHeight = f.bodyLength * sp.heightAspect
            f.finScale = sp.finScale

            f.x = (rand.nextFloat() - 0.5f) * 1.45f
            // Keep fish comfortably in the underwater zone [-0.68 .. +0.06] away from bottom dock
            f.y = -0.66f + rand.nextFloat() * 0.72f
            val dirRight = rand.nextBoolean()
            f.heading = if (dirRight) (rand.nextFloat() - 0.5f) * 0.4f else PI.toFloat() + (rand.nextFloat() - 0.5f) * 0.4f
            f.pitch = 0f
            f.baseSpeed = sp.baseSpeed * (0.85f + rand.nextFloat() * 0.30f) * depthScale
            f.speed = f.baseSpeed
            f.swimFreq = sp.swimFreq * (0.9f + rand.nextFloat() * 0.25f)
            f.swimPhase = rand.nextFloat() * 6.28f
            f.patternSeed = rand.nextFloat() * 20f + i * 2.3f
            f.smoothedCurvature = 0f
            f.startleBoost = 0f

            f.targetX = if (dirRight) 0.65f else -0.65f
            f.targetY = (-0.62f + rand.nextFloat() * 0.66f)
            f.targetZ = f.zDepth
        }
    }

    fun spawnRipple(ndcX: Float, ndcY: Float, amplitude: Float, startleFish: Boolean) {
        val r = ripples[nextRippleIdx]
        r.x = ndcX
        r.y = ndcY
        r.age = 0f
        r.maxDuration = 3.2f
        r.amplitude = amplitude
        nextRippleIdx = (nextRippleIdx + 1) % maxRipples

        if (startleFish) {
            for (i in fish.indices) {
                val f = fish[i]
                val dx = f.x - ndcX
                val dy = f.y - ndcY
                val dist = hypot(dx, dy)
                if (dist < 0.55f && dist > 0.001f) {
                    f.targetX = (f.x + (dx / dist) * 0.65f).coerceIn(-0.82f, 0.82f)
                    f.targetY = (f.y + (dy / dist) * 0.35f).coerceIn(-0.68f, 0.06f)
                    f.startleBoost = 0.22f * (1f - dist / 0.55f)
                }
            }
        }
    }

    fun update(
        dt: Float,
        activeHeroCount: Int,
        activeSchoolCount: Int,
        activeParticleCount: Int
    ) {
        val clampedDt = dt.coerceIn(0.001f, 0.05f)
        simulationTime += clampedDt

        // Occasional gentle surface droplet ripple along the waterline
        surfaceDropTimer -= clampedDt
        if (surfaceDropTimer <= 0f) {
            surfaceDropTimer = 3.4f + (sin(simulationTime * 1.9f) * 0.5f + 0.5f) * 3.5f
            val rx = sin(simulationTime * 11.3f) * 0.72f
            spawnRipple(rx, waterlineNdcY, amplitude = 0.45f, startleFish = false)
        }

        for (i in 0 until maxRipples) {
            val r = ripples[i]
            if (r.age < r.maxDuration) {
                r.age += clampedDt
            }
        }

        // Update 3D side-view fish steering inside the underwater volume
        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            if (!f.isSmallSchooler && i >= activeHeroCount) continue
            if (f.isSmallSchooler && (i - maxHeroFish) >= activeSchoolCount) continue

            f.startleBoost *= (1f - clampedDt * 2.0f).coerceAtLeast(0f)

            // Check if close to target waypoint or boundary, pick a smooth new underwater waypoint
            val dxTarget = f.targetX - f.x
            val dyTarget = f.targetY - f.y
            if (hypot(dxTarget, dyTarget) < 0.16f || abs(f.x) > 0.86f) {
                val r1 = sin(simulationTime * 1.37f + i * 4.19f)
                val r2 = cos(simulationTime * 0.91f + i * 2.73f)
                // Gently cruise across the screen horizontally with subtle vertical & depth changes
                f.targetX = if (f.x > 0.25f) {
                    -0.75f + abs(r1) * 0.55f
                } else if (f.x < -0.25f) {
                    0.75f - abs(r1) * 0.55f
                } else {
                    r1 * 0.78f
                }
                // Keep fish below waterline (+0.08) and above bottom icon dock (-0.68)
                f.targetY = (-0.30f + r2 * 0.34f).coerceIn(-0.66f, 0.06f)
            }

            var steerX = dxTarget
            var steerY = dyTarget * 1.35f

            // Mutual separation so fish never overlap unnaturally
            for (j in 0 until totalMaxFish) {
                if (i == j) continue
                val other = fish[j]
                if (!other.isSmallSchooler && j >= activeHeroCount) continue
                if (other.isSmallSchooler && (j - maxHeroFish) >= activeSchoolCount) continue

                if (abs(f.zDepth - other.zDepth) < 0.16f) {
                    val ox = f.x - other.x
                    val oy = f.y - other.y
                    val d = hypot(ox, oy)
                    val minSep = (f.bodyLength + other.bodyLength) * 0.65f
                    if (d < minSep && d > 0.001f) {
                        val push = (minSep - d) / minSep
                        steerX += (ox / d) * push * 0.65f
                        steerY += (oy / d) * push * 0.65f
                    }
                }
            }

            // Hard safety ceiling below the air-water meniscus so fish stay underwater
            if (f.y > 0.07f) {
                steerY -= (f.y - 0.07f) * 4.5f
            } else if (f.y < -0.68f) {
                steerY += (-0.68f - f.y) * 4.5f
            }

            // Desired horizontal yaw (0 = right, PI = left) and subtle vertical pitch
            val desiredYaw = if (steerX >= 0f) 0f else PI.toFloat()
            val yawDiff = normalizeAngle(desiredYaw - f.heading)
            val turnRate = if (f.isSmallSchooler) 3.4f else 2.1f
            val yawStep = (yawDiff * turnRate).coerceIn(-turnRate, turnRate)
            f.heading = normalizeAngle(f.heading + yawStep * clampedDt)

            val desiredPitch = atan2(steerY, abs(steerX).coerceAtLeast(0.15f)).coerceIn(-0.32f, 0.32f)
            f.pitch += (desiredPitch - f.pitch) * (clampedDt * 3.5f).coerceAtMost(1f)

            // Smooth spine C-bend when turning in 3D
            val targetCurve = (yawStep / turnRate).coerceIn(-1f, 1f) * 0.38f
            f.smoothedCurvature += (targetCurve - f.smoothedCurvature) * (clampedDt * 5.0f).coerceAtMost(1f)

            f.speed = f.baseSpeed + f.startleBoost
            val vx = cos(f.heading) * cos(f.pitch) * f.speed
            val vy = sin(f.pitch) * f.speed * 0.85f

            f.x += vx * clampedDt
            f.y = (f.y + vy * clampedDt).coerceIn(-0.74f, 0.09f)

            val freqBoost = 1f + (f.startleBoost / f.baseSpeed.coerceAtLeast(0.05f)) * 1.4f
            f.swimPhase = (f.swimPhase + clampedDt * f.swimFreq * freqBoost) % (200f * PI.toFloat())
        }

        // Update underwater motes & rising air bubbles (bubbles pop at waterlineNdcY)
        for (i in 0 until activeParticleCount.coerceAtMost(maxParticles)) {
            val p = particles[i]
            p.x += (p.vx + 0.008f * sin(simulationTime * 0.8f + p.phase)) * clampedDt
            p.y += p.vy * clampedDt

            if (p.isBubble) {
                if (p.y >= waterlineNdcY - 0.04f) {
                    // Respawn bubble near an active hero fish or riverbed
                    val refFish = fish[i % activeHeroCount.coerceAtLeast(1)]
                    p.x = refFish.x + sin(simulationTime * 3.1f + i) * 0.04f
                    p.y = refFish.y
                }
            } else {
                if (p.x > 1.0f) p.x = -1.0f
                if (p.x < -1.0f) p.x = 1.0f
                if (p.y > waterlineNdcY - 0.05f) p.y = -0.90f
                if (p.y < -0.92f) p.y = waterlineNdcY - 0.08f
            }
        }
    }

    private fun normalizeAngle(angle: Float): Float {
        var a = angle
        while (a > PI.toFloat()) a -= (2f * PI.toFloat())
        while (a < -PI.toFloat()) a += (2f * PI.toFloat())
        return a
    }
}

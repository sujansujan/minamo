package com.example.koipond

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * Pre-allocated state for a single Koi or silver minnow.
 * All fields are primitives so updates produce zero garbage collection allocations.
 */
class FishAgent(
    val id: Int,
    val isMinnow: Boolean,
    var x: Float,
    var y: Float,
    var zDepth: Float,      // 0.18 (shallow) .. 0.65 (deep)
    var heading: Float,     // radians
    var speed: Float,
    val baseSpeed: Float,
    val bodyLength: Float,
    val bodyWidth: Float,
    val patternVariant: Int, // 0=Kohaku, 1=Sanke, 2=Showa, 3=Ogon Gold, 4=Platinum, 5=Tancho, 6=Silver Minnow
    val patternSeed: Float,
    var swimPhase: Float,
    var swimFreq: Float,
    var turnRate: Float,
    var smoothedCurvature: Float,
    var wanderAngle: Float,
    var startleBoost: Float = 0f
)

class LilyPadItem(
    val x: Float,
    val y: Float,
    val radius: Float,
    val baseRotation: Float,
    val notchAngle: Float,
    val swayPhase: Float,
    val hasLotus: Boolean,
    val lotusColorVariant: Int
)

class VallisneriaBlade(
    val rootX: Float,
    val rootY: Float,
    val length: Float,
    val width: Float,
    val baseAngle: Float,
    val phase: Float,
    val hueShift: Float
)

/**
 * Pre-allocated circular buffer for surface ripples (touch + occasional raindrops).
 */
class RippleWave(
    var x: Float = 0f,
    var y: Float = 0f,
    var age: Float = 99f,
    var maxDuration: Float = 3.5f,
    var amplitude: Float = 0f
)

class ParticleMote(
    var x: Float,
    var y: Float,
    var z: Float,
    var vx: Float,
    var vy: Float,
    var vz: Float,
    var size: Float,
    var isBubble: Boolean,
    var phase: Float
)

/**
 * Pure zero-allocation simulation loop for koi steering, touch-evasion, ripples,
 * vallisneria sway, lily pad drift, and underwater particles.
 */
class PondSimulation {

    val maxKoi = 9
    val minnowCount = 5
    val totalMaxFish = maxKoi + minnowCount

    val fish = Array(totalMaxFish) { idx ->
        val isMinnow = idx >= maxKoi
        val rand = Random(idx * 9173 + 19)
        val x = (rand.nextFloat() - 0.5f) * 1.1f
        val y = (rand.nextFloat() - 0.5f) * 1.1f
        val heading = rand.nextFloat() * (2f * PI.toFloat())
        if (isMinnow) {
            FishAgent(
                id = idx,
                isMinnow = true,
                x = x,
                y = y,
                zDepth = 0.16f + rand.nextFloat() * 0.14f,
                heading = heading,
                speed = 0.21f,
                baseSpeed = 0.20f + rand.nextFloat() * 0.05f,
                bodyLength = 0.13f + rand.nextFloat() * 0.025f,
                bodyWidth = 0.11f,
                patternVariant = 6,
                patternSeed = rand.nextFloat() * 10f,
                swimPhase = rand.nextFloat() * 6.28f,
                swimFreq = 6.2f,
                turnRate = 0f,
                smoothedCurvature = 0f,
                wanderAngle = heading
            )
        } else {
            FishAgent(
                id = idx,
                isMinnow = false,
                x = x,
                y = y,
                zDepth = 0.26f + (idx % 4) * 0.09f,
                heading = heading,
                speed = 0.12f,
                baseSpeed = 0.105f + rand.nextFloat() * 0.035f,
                bodyLength = 0.34f + (idx % 3) * 0.045f,
                bodyWidth = 0.32f + (idx % 3) * 0.035f,
                patternVariant = idx % 6,
                patternSeed = idx * 1.73f + 0.41f,
                swimPhase = rand.nextFloat() * 6.28f,
                swimFreq = 2.9f + (idx % 3) * 0.25f,
                turnRate = 0f,
                smoothedCurvature = 0f,
                wanderAngle = heading
            )
        }
    }

    // Lily pads positioned organically while keeping top 15% and bottom 20% calmer
    val lilyPads = arrayOf(
        LilyPadItem(-0.56f,  0.42f, 0.23f, 0.4f, 0.8f, 0.0f, hasLotus = true,  lotusColorVariant = 0),
        LilyPadItem(-0.34f,  0.22f, 0.17f, 1.9f, 2.3f, 1.4f, hasLotus = false, lotusColorVariant = 0),
        LilyPadItem( 0.58f,  0.34f, 0.21f, 3.5f, 4.1f, 2.1f, hasLotus = false, lotusColorVariant = 0),
        LilyPadItem( 0.44f, -0.26f, 0.24f, 4.8f, 5.2f, 3.3f, hasLotus = true,  lotusColorVariant = 1),
        LilyPadItem( 0.64f, -0.44f, 0.16f, 1.1f, 1.5f, 4.7f, hasLotus = false, lotusColorVariant = 0),
        LilyPadItem(-0.60f, -0.34f, 0.19f, 2.7f, 3.0f, 5.4f, hasLotus = false, lotusColorVariant = 0),
        LilyPadItem(-0.42f, -0.48f, 0.14f, 0.9f, 0.4f, 2.8f, hasLotus = false, lotusColorVariant = 0)
    )

    val vallisneriaBlades = Array(26) { idx ->
        val rand = Random(idx * 3571 + 7)
        val cluster = idx % 4
        val clusterX = when (cluster) {
            0 -> -0.68f
            1 ->  0.66f
            2 -> -0.52f
            else -> 0.50f
        }
        val clusterY = when (cluster) {
            0 ->  0.36f
            1 -> -0.18f
            2 -> -0.42f
            else -> 0.48f
        }
        VallisneriaBlade(
            rootX = clusterX + (rand.nextFloat() - 0.5f) * 0.26f,
            rootY = clusterY + (rand.nextFloat() - 0.5f) * 0.26f,
            length = 0.28f + rand.nextFloat() * 0.22f,
            width = 0.024f + rand.nextFloat() * 0.012f,
            baseAngle = -0.65f + (rand.nextFloat() - 0.5f) * 0.55f,
            phase = rand.nextFloat() * 6.28f,
            hueShift = rand.nextFloat()
        )
    }

    val maxRipples = 10
    val ripples = Array(maxRipples) { RippleWave() }
    private var nextRippleIdx = 0
    private var raindropTimer = 2.8f

    val maxParticles = 96
    val particles = Array(maxParticles) { idx ->
        val r = Random(idx * 6151 + 99)
        val isBubble = idx < 10
        ParticleMote(
            x = (r.nextFloat() - 0.5f) * 2.0f,
            y = (r.nextFloat() - 0.5f) * 3.2f,
            z = r.nextFloat(),
            vx = (r.nextFloat() - 0.5f) * 0.018f,
            vy = if (isBubble) (0.045f + r.nextFloat() * 0.035f) else (r.nextFloat() - 0.5f) * 0.018f,
            vz = if (isBubble) 0.09f else (r.nextFloat() - 0.5f) * 0.01f,
            size = if (isBubble) (0.012f + r.nextFloat() * 0.010f) else (0.005f + r.nextFloat() * 0.006f),
            isBubble = isBubble,
            phase = r.nextFloat() * 6.28f
        )
    }

    var simulationTime = 0f
        private set

    fun spawnRipple(worldX: Float, worldY: Float, amplitude: Float, startleFish: Boolean, aspect: Float) {
        val r = ripples[nextRippleIdx]
        r.x = worldX
        r.y = worldY
        r.age = 0f
        r.maxDuration = if (startleFish) 3.8f else 2.8f
        r.amplitude = amplitude
        nextRippleIdx = (nextRippleIdx + 1) % maxRipples

        if (startleFish) {
            for (i in fish.indices) {
                val f = fish[i]
                val dx = f.x - worldX
                val dy = f.y - worldY
                val dist = hypot(dx, dy)
                val influenceRadius = if (f.isMinnow) 0.65f else 0.55f
                if (dist < influenceRadius && dist > 0.001f) {
                    val awayAngle = atan2(dy, dx)
                    val delta = normalizeAngle(awayAngle - f.heading)
                    val strength = (1f - dist / influenceRadius)
                    f.wanderAngle = f.heading + delta * (0.55f + 0.35f * strength)
                    f.startleBoost = (0.18f + 0.14f * strength).coerceAtMost(0.32f)
                }
            }
        }
    }

    fun update(
        dt: Float,
        aspect: Float,
        activeKoiCount: Int,
        activeMinnowCount: Int,
        activeParticleCount: Int
    ) {
        val clampedDt = dt.coerceIn(0.001f, 0.05f)
        simulationTime += clampedDt

        // 1. Update ripples & occasional meditative raindrop
        raindropTimer -= clampedDt
        if (raindropTimer <= 0f) {
            raindropTimer = 3.6f + (sin(simulationTime * 1.7f) * 0.5f + 0.5f) * 4.2f
            val rx = sin(simulationTime * 12.73f) * 0.68f
            val ry = cos(simulationTime * 7.39f) * (0.55f / aspect.coerceAtLeast(0.45f))
            spawnRipple(rx, ry, amplitude = 0.45f, startleFish = false, aspect = aspect)
        }

        for (i in 0 until maxRipples) {
            val r = ripples[i]
            if (r.age < r.maxDuration) {
                r.age += clampedDt
            }
        }

        // World bounds: X in [-1, 1], Y in [-1/aspect, +1/aspect]
        // Usability constraint: Keep top 15% and bottom 20% calmer (fish steer gently away from clock & dock)
        val halfH = 1.0f / aspect.coerceAtLeast(0.4f)
        val safeX = 0.78f
        val safeTopY = halfH * (1.0f - 2.0f * 0.17f)    // stays below top 17%
        val safeBottomY = -halfH * (1.0f - 2.0f * 0.22f) // stays above bottom 22%

        // 2. Update Koi and Minnow steering (wander + separation + boundary avoidance + lily-pad stem avoidance)
        for (i in 0 until totalMaxFish) {
            val f = fish[i]
            if (!f.isMinnow && i >= activeKoiCount) continue
            if (f.isMinnow && (i - maxKoi) >= activeMinnowCount) continue

            // Decay startle boost smoothly
            f.startleBoost *= (1f - clampedDt * 1.8f).coerceAtLeast(0f)

            // Smooth wander perturbation
            val wanderNoise = sin(simulationTime * 0.55f + f.id * 2.17f) * 0.42f +
                cos(simulationTime * 0.23f - f.id * 1.31f) * 0.28f
            f.wanderAngle += wanderNoise * clampedDt * 0.65f

            var steerX = cos(f.wanderAngle) * 0.45f
            var steerY = sin(f.wanderAngle) * 0.45f

            // Separation from other fish (and schooling cohesion for minnows)
            for (j in 0 until totalMaxFish) {
                if (i == j) continue
                val other = fish[j]
                if (!other.isMinnow && j >= activeKoiCount) continue
                if (other.isMinnow && (j - maxKoi) >= activeMinnowCount) continue

                val dx = f.x - other.x
                val dy = f.y - other.y
                val dist = hypot(dx, dy)
                val minSep = (f.bodyLength + other.bodyLength) * 0.58f
                if (dist < minSep && dist > 0.0001f) {
                    val push = ((minSep - dist) / minSep) * 1.65f
                    steerX += (dx / dist) * push
                    steerY += (dy / dist) * push
                } else if (f.isMinnow && other.isMinnow && dist < 0.45f && dist > 0.01f) {
                    // Gentle schooling alignment for silver minnows
                    steerX += cos(other.heading) * 0.18f
                    steerY += sin(other.heading) * 0.18f
                }
            }

            // Soft boundary steering so fish stay out of top 15% clock area and bottom 20% icon dock
            if (f.x > safeX) {
                steerX -= ((f.x - safeX) / 0.25f) * 2.2f
            } else if (f.x < -safeX) {
                steerX += ((-safeX - f.x) / 0.25f) * 2.2f
            }

            if (f.y > safeTopY) {
                steerY -= ((f.y - safeTopY) / 0.25f) * 2.6f
            } else if (f.y < safeBottomY) {
                steerY += ((safeBottomY - f.y) / 0.25f) * 2.6f
            }

            val desiredHeading = atan2(steerY, steerX)
            val angleDiff = normalizeAngle(desiredHeading - f.heading)

            val maxTurnSpeed = if (f.isMinnow) 2.6f else 1.25f
            val angularVel = (angleDiff * 1.6f).coerceIn(-maxTurnSpeed, maxTurnSpeed)

            f.heading = normalizeAngle(f.heading + angularVel * clampedDt)
            f.wanderAngle = normalizeAngle(f.heading + angleDiff * 0.25f)

            // Smooth spine curvature proportional to angular velocity for realistic body C-bend
            val targetCurvature = (angularVel / maxTurnSpeed).coerceIn(-1f, 1f) * 0.42f
            f.smoothedCurvature += (targetCurvature - f.smoothedCurvature) * (clampedDt * 5.0f).coerceAtMost(1f)

            f.speed = f.baseSpeed + f.startleBoost
            val velocityMultiplier = 1f + 0.08f * sin(f.swimPhase * 2f)
            val actualSpeed = f.speed * velocityMultiplier

            f.x += cos(f.heading) * actualSpeed * clampedDt
            f.y += sin(f.heading) * actualSpeed * clampedDt

            // Advance tail undulation phase proportional to current speed
            val freqScale = 1f + (f.startleBoost / f.baseSpeed) * 1.4f
            f.swimPhase = (f.swimPhase + clampedDt * f.swimFreq * freqScale) % (200f * PI.toFloat())
        }

        // 3. Update drifting sediment motes and rising bubbles
        for (i in 0 until activeParticleCount.coerceAtMost(maxParticles)) {
            val p = particles[i]
            p.x += (p.vx + 0.008f * sin(simulationTime * 0.7f + p.phase)) * clampedDt
            p.y += (p.vy + 0.006f * cos(simulationTime * 0.5f + p.phase)) * clampedDt
            p.z += p.vz * clampedDt

            if (p.isBubble) {
                if (p.z > 1.0f || p.y > halfH) {
                    p.z = 0.05f
                    // Respawn bubble near an active koi
                    val refFish = fish[i % activeKoiCount.coerceAtLeast(1)]
                    p.x = refFish.x + (sin(simulationTime * 3.1f + i) * 0.06f)
                    p.y = refFish.y + (cos(simulationTime * 2.7f + i) * 0.06f)
                }
            } else {
                if (p.x > 1.05f) p.x = -1.05f
                if (p.x < -1.05f) p.x = 1.05f
                if (p.y > halfH) p.y = -halfH
                if (p.y < -halfH) p.y = halfH
                if (p.z > 1.0f) p.z = 0.0f
                if (p.z < 0.0f) p.z = 1.0f
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

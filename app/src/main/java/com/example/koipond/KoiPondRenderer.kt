package com.example.koipond

import android.opengl.GLES30
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class KoiPondRenderer(private val simulation: PondSimulation) {

    private var surfaceWidth = 1080
    private var surfaceHeight = 2400
    private var aspect = 2400f / 1080f

    var currentScale = 0.85f
        private set
    private var fboWidth = 0
    private var fboHeight = 0
    private var fboId = 0
    private var fboColorTex = 0

    private var bedProgram = 0
    private var vallisneriaProgram = 0
    private var koiProgram = 0
    private var waterCompositeProgram = 0

    private var bedTimeLoc = -1
    private var bedAspectLoc = -1
    private var bedParallaxLoc = -1
    private var bedSunDirLoc = -1
    private var bedClarityLoc = -1
    private var bedNoiseTexLoc = -1

    private var valTimeLoc = -1
    private var valAspectLoc = -1
    private var valParallaxLoc = -1
    private var valBladeDataLoc = -1
    private var valBladeMetaLoc = -1
    private var valShadowPassLoc = -1
    private var valSunDirLoc = -1

    private var koiTimeLoc = -1
    private var koiAspectLoc = -1
    private var koiParallaxLoc = -1
    private var koiSunDirLoc = -1
    private var koiWorldPosLoc = -1
    private var koiScaleLoc = -1
    private var koiHeadingLoc = -1
    private var koiSwimPhaseLoc = -1
    private var koiCurvatureLoc = -1
    private var koiShadowPassLoc = -1
    private var koiPatternVariantLoc = -1
    private var koiPatternSeedLoc = -1
    private var koiClarityLoc = -1
    private var koiNoiseTexLoc = -1

    private var waterUnderwaterTexLoc = -1
    private var waterNoiseTexLoc = -1
    private var waterTimeLoc = -1
    private var waterAspectLoc = -1
    private var waterParallaxLoc = -1
    private var waterSunDirLoc = -1
    private var waterClarityLoc = -1
    private var waterRipplesLoc = -1
    private var waterParticleCountLoc = -1
    private var waterParticlesLoc = -1

    private var quadMesh: GlMesh? = null
    private var vallisneriaMesh: GlMesh? = null
    private var fishMesh: GlMesh? = null
    private var noiseTexId = 0

    private val rippleUniformArray = FloatArray(10 * 4)
    private val particleUniformArray = FloatArray(48 * 4)
    private val fishOrder = IntArray(simulation.totalMaxFish) { it }

    private var targetParallaxX = 0f
    private var targetParallaxY = 0f
    private var smoothParallaxX = 0f
    private var smoothParallaxY = 0f

    private var smoothedFrameTimeMs = 16.0f
    private var scaleCooldownFrames = 0

    fun onSurfaceCreated() {
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        noiseTexId = ProceduralTextures.createNoiseAndCausticsTexture(256)

        quadMesh = Meshes.createFullscreenQuad()
        vallisneriaMesh = Meshes.createVallisneriaRibbon(22)
        fishMesh = Meshes.createFishMesh(28, 12)

        bedProgram = Shaders.buildProgram(Shaders.POND_BED_VERT, Shaders.POND_BED_FRAG)
        bedTimeLoc = GLES30.glGetUniformLocation(bedProgram, "uTime")
        bedAspectLoc = GLES30.glGetUniformLocation(bedProgram, "uAspect")
        bedParallaxLoc = GLES30.glGetUniformLocation(bedProgram, "uParallax")
        bedSunDirLoc = GLES30.glGetUniformLocation(bedProgram, "uSunDir")
        bedClarityLoc = GLES30.glGetUniformLocation(bedProgram, "uWaterClarity")
        bedNoiseTexLoc = GLES30.glGetUniformLocation(bedProgram, "uNoiseTex")

        vallisneriaProgram = Shaders.buildProgram(Shaders.VALLISNERIA_VERT, Shaders.VALLISNERIA_FRAG)
        valTimeLoc = GLES30.glGetUniformLocation(vallisneriaProgram, "uTime")
        valAspectLoc = GLES30.glGetUniformLocation(vallisneriaProgram, "uAspect")
        valParallaxLoc = GLES30.glGetUniformLocation(vallisneriaProgram, "uParallax")
        valBladeDataLoc = GLES30.glGetUniformLocation(vallisneriaProgram, "uBladeData")
        valBladeMetaLoc = GLES30.glGetUniformLocation(vallisneriaProgram, "uBladeMeta")
        valShadowPassLoc = GLES30.glGetUniformLocation(vallisneriaProgram, "uShadowPass")
        valSunDirLoc = GLES30.glGetUniformLocation(vallisneriaProgram, "uSunDir")

        koiProgram = Shaders.buildProgram(Shaders.KOI_VERT, Shaders.KOI_FRAG)
        koiTimeLoc = GLES30.glGetUniformLocation(koiProgram, "uTime")
        koiAspectLoc = GLES30.glGetUniformLocation(koiProgram, "uAspect")
        koiParallaxLoc = GLES30.glGetUniformLocation(koiProgram, "uParallax")
        koiSunDirLoc = GLES30.glGetUniformLocation(koiProgram, "uSunDir")
        koiWorldPosLoc = GLES30.glGetUniformLocation(koiProgram, "uFishWorldPos")
        koiScaleLoc = GLES30.glGetUniformLocation(koiProgram, "uFishScale")
        koiHeadingLoc = GLES30.glGetUniformLocation(koiProgram, "uFishHeading")
        koiSwimPhaseLoc = GLES30.glGetUniformLocation(koiProgram, "uSwimPhase")
        koiCurvatureLoc = GLES30.glGetUniformLocation(koiProgram, "uCurvature")
        koiShadowPassLoc = GLES30.glGetUniformLocation(koiProgram, "uShadowPass")
        koiPatternVariantLoc = GLES30.glGetUniformLocation(koiProgram, "uPatternVariant")
        koiPatternSeedLoc = GLES30.glGetUniformLocation(koiProgram, "uPatternSeed")
        koiClarityLoc = GLES30.glGetUniformLocation(koiProgram, "uWaterClarity")
        koiNoiseTexLoc = GLES30.glGetUniformLocation(koiProgram, "uNoiseTex")

        waterCompositeProgram = Shaders.buildProgram(
            Shaders.WATER_COMPOSITE_VERT,
            Shaders.WATER_COMPOSITE_FRAG
        )
        waterUnderwaterTexLoc = GLES30.glGetUniformLocation(waterCompositeProgram, "uUnderwaterTex")
        waterNoiseTexLoc = GLES30.glGetUniformLocation(waterCompositeProgram, "uNoiseTex")
        waterTimeLoc = GLES30.glGetUniformLocation(waterCompositeProgram, "uTime")
        waterAspectLoc = GLES30.glGetUniformLocation(waterCompositeProgram, "uAspect")
        waterParallaxLoc = GLES30.glGetUniformLocation(waterCompositeProgram, "uParallax")
        waterSunDirLoc = GLES30.glGetUniformLocation(waterCompositeProgram, "uSunDir")
        waterClarityLoc = GLES30.glGetUniformLocation(waterCompositeProgram, "uWaterClarity")
        waterRipplesLoc = GLES30.glGetUniformLocation(waterCompositeProgram, "uRipples")
        waterParticleCountLoc = GLES30.glGetUniformLocation(waterCompositeProgram, "uParticleCount")
        waterParticlesLoc = GLES30.glGetUniformLocation(waterCompositeProgram, "uParticles")
    }

    fun onSurfaceChanged(width: Int, height: Int, config: WallpaperConfig, isBatterySaver: Boolean) {
        surfaceWidth = width.coerceIn(1, 2160)
        surfaceHeight = height.coerceIn(1, 3840)
        aspect = (surfaceHeight.toFloat() / surfaceWidth.toFloat()).coerceIn(0.5f, 3.0f)

        val initialScale = if (isBatterySaver) 0.60f else config.qualityPreset.maxScale
        currentScale = initialScale.coerceIn(0.60f, 1.00f)
        recreateOffscreenFbo()
    }

    private fun recreateOffscreenFbo() {
        val targetW = (surfaceWidth * currentScale).roundToInt().coerceAtLeast(320)
        val targetH = (surfaceHeight * currentScale).roundToInt().coerceAtLeast(320)
        if (targetW == fboWidth && targetH == fboHeight && fboId != 0) return

        fboWidth = targetW
        fboHeight = targetH

        if (fboColorTex != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(fboColorTex), 0)
            fboColorTex = 0
        }
        if (fboId != 0) {
            GLES30.glDeleteFramebuffers(1, intArrayOf(fboId), 0)
            fboId = 0
        }

        val texArr = IntArray(1)
        GLES30.glGenTextures(1, texArr, 0)
        fboColorTex = texArr[0]
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fboColorTex)
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_RGBA8,
            fboWidth,
            fboHeight,
            0,
            GLES30.GL_RGBA,
            GLES30.GL_UNSIGNED_BYTE,
            null
        )
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)

        val fboArr = IntArray(1)
        GLES30.glGenFramebuffers(1, fboArr, 0)
        fboId = fboArr[0]
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboId)
        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER,
            GLES30.GL_COLOR_ATTACHMENT0,
            GLES30.GL_TEXTURE_2D,
            fboColorTex,
            0
        )
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    fun setParallaxTargets(scrollOffsetX: Float, gyroX: Float, gyroY: Float) {
        targetParallaxX = (scrollOffsetX - 0.5f) * 0.14f + gyroX * 0.05f
        targetParallaxY = gyroY * 0.05f
    }

    fun onTouchWater(screenX: Float, screenY: Float) {
        val ndcX = (screenX / surfaceWidth.toFloat()) * 2f - 1f
        val ndcY = 1f - (screenY / surfaceHeight.toFloat()) * 2f
        simulation.spawnRipple(ndcX, ndcY, amplitude = 1.0f, startleFish = true)
    }

    fun randomizeFishOnUnlock() {
        simulation.randomizeEcosystemOnUnlock(System.nanoTime())
    }

    fun updateDynamicResolution(
        lastFrameCpuGpuMs: Float,
        config: WallpaperConfig,
        isBatterySaver: Boolean
    ) {
        if (isBatterySaver) {
            if (abs(currentScale - 0.60f) > 0.01f) {
                currentScale = 0.60f
                recreateOffscreenFbo()
            }
            return
        }

        smoothedFrameTimeMs = smoothedFrameTimeMs * 0.88f + lastFrameCpuGpuMs * 0.12f
        if (scaleCooldownFrames > 0) {
            scaleCooldownFrames--
            return
        }

        val budgetMs = if (config.targetFps == 60) 15.5f else 28.0f
        val minScale = config.qualityPreset.minScale
        val maxScale = config.qualityPreset.maxScale

        if (smoothedFrameTimeMs > budgetMs && currentScale > minScale + 0.02f) {
            currentScale = (currentScale - 0.05f).coerceAtLeast(minScale)
            recreateOffscreenFbo()
            scaleCooldownFrames = 45
        } else if (smoothedFrameTimeMs < budgetMs * 0.62f && currentScale < maxScale - 0.02f) {
            currentScale = (currentScale + 0.05f).coerceAtMost(maxScale)
            recreateOffscreenFbo()
            scaleCooldownFrames = 60
        }
    }

    fun drawFrame(dt: Float, config: WallpaperConfig, isBatterySaver: Boolean) {
        val activeHero = if (isBatterySaver) (config.koiCount - 2).coerceAtLeast(3) else config.koiCount
        val activeSchool = if (isBatterySaver) 3 else simulation.maxSchoolFish
        val activeParticles = if (isBatterySaver) {
            16
        } else {
            (48 * config.qualityPreset.particleMultiplier).roundToInt().coerceIn(16, 48)
        }

        val lerpFactor = (dt * 6f).coerceIn(0.01f, 1f)
        smoothParallaxX += (targetParallaxX - smoothParallaxX) * lerpFactor
        smoothParallaxY += (targetParallaxY - smoothParallaxY) * lerpFactor

        simulation.update(dt, activeHero, activeSchool, activeParticles)

        val elevRad = (config.sunAngleDeg * PI / 180.0).toFloat()
        val horiz = cos(elevRad)
        val sunX = -0.7071f * horiz
        val sunY =  0.7071f * horiz
        val sunZ =  sin(elevRad)

        val simTime = simulation.simulationTime

        // =========================================================================
        // PASS 1: Render Split-View Backdrop, Underwater Seagrass & Multi-Species Fish
        // =========================================================================
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboId)
        GLES30.glViewport(0, 0, fboWidth, fboHeight)
        GLES30.glDisable(GLES30.GL_BLEND)

        // 1A. Split-View Backdrop (Over-Water Sky & Surface + Underwater Column & Cobble Floor)
        GLES30.glUseProgram(bedProgram)
        GLES30.glUniform1f(bedTimeLoc, simTime)
        GLES30.glUniform1f(bedAspectLoc, aspect)
        GLES30.glUniform2f(bedParallaxLoc, smoothParallaxX, smoothParallaxY)
        GLES30.glUniform3f(bedSunDirLoc, sunX, sunY, sunZ)
        GLES30.glUniform1f(bedClarityLoc, config.waterClarity)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, noiseTexId)
        GLES30.glUniform1i(bedNoiseTexLoc, 0)
        quadMesh?.draw()

        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        // 1B. Sort active fish back-to-front by zDepth (deepest drawn first)
        for (i in 1 until simulation.totalMaxFish) {
            val key = fishOrder[i]
            val keyDepth = simulation.fish[key].zDepth
            var j = i - 1
            while (j >= 0 && simulation.fish[fishOrder[j]].zDepth < keyDepth) {
                fishOrder[j + 1] = fishOrder[j]
                j--
            }
            fishOrder[j + 1] = key
        }

        // 1C. Background Seagrass Blades (zDepth >= 0.45)
        val bladeCount = if (isBatterySaver) 16 else simulation.vallisneriaBlades.size
        GLES30.glUseProgram(vallisneriaProgram)
        GLES30.glUniform1f(valTimeLoc, simTime)
        GLES30.glUniform1f(valAspectLoc, aspect)
        GLES30.glUniform2f(valParallaxLoc, smoothParallaxX, smoothParallaxY)
        GLES30.glUniform3f(valSunDirLoc, sunX, sunY, sunZ)
        GLES30.glUniform1f(valShadowPassLoc, 0.0f)
        for (i in 0 until bladeCount) {
            val b = simulation.vallisneriaBlades[i]
            if (b.depthZ >= 0.45f) {
                GLES30.glUniform4f(valBladeDataLoc, b.rootX, b.rootY, b.length, b.width)
                GLES30.glUniform3f(valBladeMetaLoc, b.baseAngle, b.phase, b.hueShift)
                vallisneriaMesh?.draw()
            }
        }

        // 1D. Multi-Species 3D Side-View Fish
        GLES30.glUseProgram(koiProgram)
        GLES30.glUniform1f(koiTimeLoc, simTime)
        GLES30.glUniform1f(koiAspectLoc, aspect)
        GLES30.glUniform2f(koiParallaxLoc, smoothParallaxX, smoothParallaxY)
        GLES30.glUniform3f(koiSunDirLoc, sunX, sunY, sunZ)
        GLES30.glUniform1f(koiClarityLoc, config.waterClarity)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, noiseTexId)
        GLES30.glUniform1i(koiNoiseTexLoc, 0)

        for (idx in 0 until simulation.totalMaxFish) {
            val fishIdx = fishOrder[idx]
            val f = simulation.fish[fishIdx]
            if (!f.isSmallSchooler && fishIdx >= activeHero) continue
            if (f.isSmallSchooler && (fishIdx - simulation.maxHeroFish) >= activeSchool) continue

            GLES30.glUniform3f(koiWorldPosLoc, f.x, f.y, f.zDepth)
            GLES30.glUniform2f(koiScaleLoc, f.bodyLength, f.bodyHeight)
            GLES30.glUniform1f(koiHeadingLoc, f.heading)
            GLES30.glUniform1f(koiSwimPhaseLoc, f.swimPhase)
            GLES30.glUniform1f(koiCurvatureLoc, f.pitch + f.smoothedCurvature * 0.35f)
            GLES30.glUniform1f(koiShadowPassLoc, f.finScale)
            GLES30.glUniform1i(koiPatternVariantLoc, f.speciesId)
            GLES30.glUniform1f(koiPatternSeedLoc, f.patternSeed)
            fishMesh?.draw()
        }

        // 1E. Foreground Seagrass Blades (zDepth < 0.45) so fish can swim behind foreground plants
        GLES30.glUseProgram(vallisneriaProgram)
        for (i in 0 until bladeCount) {
            val b = simulation.vallisneriaBlades[i]
            if (b.depthZ < 0.45f) {
                GLES30.glUniform4f(valBladeDataLoc, b.rootX, b.rootY, b.length, b.width)
                GLES30.glUniform3f(valBladeMetaLoc, b.baseAngle, b.phase, b.hueShift)
                vallisneriaMesh?.draw()
            }
        }

        // =========================================================================
        // PASS 2: Full-Resolution Meniscus Wave Line, Bubbles, Plankton & Tone Map
        // =========================================================================
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glViewport(0, 0, surfaceWidth, surfaceHeight)
        GLES30.glDisable(GLES30.GL_BLEND)

        GLES30.glUseProgram(waterCompositeProgram)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, fboColorTex)
        GLES30.glUniform1i(waterUnderwaterTexLoc, 0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, noiseTexId)
        GLES30.glUniform1i(waterNoiseTexLoc, 1)

        GLES30.glUniform1f(waterTimeLoc, simTime)
        GLES30.glUniform1f(waterAspectLoc, aspect)
        GLES30.glUniform2f(waterParallaxLoc, smoothParallaxX, smoothParallaxY)
        GLES30.glUniform3f(waterSunDirLoc, sunX, sunY, sunZ)
        GLES30.glUniform1f(waterClarityLoc, config.waterClarity)

        for (i in 0 until simulation.maxRipples) {
            val r = simulation.ripples[i]
            val base = i * 4
            rippleUniformArray[base] = r.x
            rippleUniformArray[base + 1] = r.y
            rippleUniformArray[base + 2] = r.age
            rippleUniformArray[base + 3] = r.amplitude
        }
        GLES30.glUniform4fv(waterRipplesLoc, simulation.maxRipples, rippleUniformArray, 0)

        GLES30.glUniform1i(waterParticleCountLoc, activeParticles)
        for (i in 0 until activeParticles) {
            val p = simulation.particles[i]
            val base = i * 4
            particleUniformArray[base] = p.x
            particleUniformArray[base + 1] = p.y
            particleUniformArray[base + 2] = p.z
            particleUniformArray[base + 3] = if (p.isBubble) -p.size else p.size
        }
        GLES30.glUniform4fv(waterParticlesLoc, activeParticles, particleUniformArray, 0)

        quadMesh?.draw()
    }

    fun release() {
        quadMesh?.release()
        vallisneriaMesh?.release()
        fishMesh?.release()
        quadMesh = null
        vallisneriaMesh = null
        fishMesh = null

        if (noiseTexId != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(noiseTexId), 0)
            noiseTexId = 0
        }
        if (fboColorTex != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(fboColorTex), 0)
            fboColorTex = 0
        }
        if (fboId != 0) {
            GLES30.glDeleteFramebuffers(1, intArrayOf(fboId), 0)
            fboId = 0
        }
        if (bedProgram != 0) GLES30.glDeleteProgram(bedProgram)
        if (vallisneriaProgram != 0) GLES30.glDeleteProgram(vallisneriaProgram)
        if (koiProgram != 0) GLES30.glDeleteProgram(koiProgram)
        if (waterCompositeProgram != 0) GLES30.glDeleteProgram(waterCompositeProgram)
        bedProgram = 0
        vallisneriaProgram = 0
        koiProgram = 0
        waterCompositeProgram = 0
    }
}

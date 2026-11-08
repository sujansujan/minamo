package com.example.koipond

import android.opengl.GLES30
import android.util.Log

/**
 * Contains all GLSL ES 3.0 shaders with detailed mathematical comments explaining
 * the procedural river stones, Snell's law refraction, Dual-layer Voronoi caustics,
 * vertex-shader articulated koi spine bending, and volumetric sun rays.
 */
object Shaders {

    private const val TAG = "KoiPondShaders"

    // =========================================================================================
    // 1. POND BED SHADER (Procedural Gravel, River Stones, Moss, Dappled Canopy Shadows, Caustics)
    // =========================================================================================
    const val POND_BED_VERT = """#version 300 es
precision highp float;
layout(location = 0) in vec2 aPos;
layout(location = 1) in vec2 aUv;

out vec2 vUv;
out vec2 vNdc;

void main() {
    vUv = aUv;
    vNdc = aPos;
    gl_Position = vec4(aPos, 0.0, 1.0);
}
"""

    const val POND_BED_FRAG = """#version 300 es
precision highp float;

in vec2 vUv;
in vec2 vNdc;

uniform float uTime;
uniform float uAspect;
uniform vec2 uParallax;
uniform vec3 uSunDir;        // Normalized 3D sun vector from upper-left
uniform float uWaterClarity; // 0.35 .. 1.0
uniform sampler2D uNoiseTex;

out vec4 fragColor;

// Fast deterministic 2D hash for procedural river cobbles
vec2 hash2(vec2 p) {
    p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
    return fract(sin(p) * 43758.5453123);
}

// Computes smooth Voronoi river pebbles:
// Returns vec4(cellDist, edgeFactor, cellIdHash1, cellIdHash2)
// Plus outputs local 2D pebble normal offset in outNormalXY
vec4 evaluatePebbleLayer(vec2 p, float scale, out vec2 outNormalXY) {
    vec2 uv = p * scale;
    vec2 iuv = floor(uv);
    vec2 fuv = fract(uv);

    float minDist = 8.0;
    float secondDist = 8.0;
    vec2 bestVec = vec2(0.0);
    vec2 bestHash = vec2(0.0);

    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 neighbor = vec2(float(x), float(y));
            vec2 rnd = hash2(iuv + neighbor);
            // Slight elliptical anisotropy for natural water-worn river stones
            vec2 point = neighbor + 0.18 + 0.64 * rnd - fuv;
            point.x *= 1.0 + 0.22 * (rnd.x - 0.5);
            float d = length(point);
            if (d < minDist) {
                secondDist = minDist;
                minDist = d;
                bestVec = -point;
                bestHash = rnd;
            } else if (d < secondDist) {
                secondDist = d;
            }
        }
    }

    float crevice = smoothstep(0.02, 0.16, secondDist - minDist);
    // Dome height of stone falls off quadratically from center
    float domeRadius = 0.46 + 0.18 * bestHash.x;
    float rNorm = clamp(minDist / domeRadius, 0.0, 1.0);
    outNormalXY = (bestVec / max(domeRadius, 0.001)) * crevice;
    float height = sqrt(max(0.0, 1.0 - rNorm * rNorm)) * crevice;
    return vec4(height, crevice, bestHash.x, bestHash.y);
}

// Dual-layer animated caustic pattern projected along the refracted sun vector
float computeCaustics(vec2 worldPos, float time) {
    vec2 uv1 = worldPos * 1.15 + vec2(time * 0.032, -time * 0.024);
    vec2 uv2 = worldPos * 1.48 + vec2(-time * 0.027, time * 0.036);

    vec4 n1 = texture(uNoiseTex, uv1);
    vec4 n2 = texture(uNoiseTex, uv2);

    // Ridged interference between two cellular/FBM layers creates sharp bright caustic webs
    float c1 = 1.0 - abs(n1.r * 2.0 - 1.0);
    float c2 = 1.0 - abs(n2.a * 2.0 - 1.0);
    float combined = pow(c1 * c2, 3.2) * 3.8 + pow(n1.g * n2.g, 2.6) * 1.6;
    return clamp(combined, 0.0, 2.5);
}

void main() {
    // Aspect-corrected world coordinate: X in [-1, 1], Y in [-1/aspect, +1/aspect]
    vec2 worldPos = vec2(vNdc.x, vNdc.y / uAspect) + uParallax * 1.35;

    // Sample fine sand/gravel noise
    vec4 sandNoise = texture(uNoiseTex, worldPos * 2.8);
    vec4 macroNoise = texture(uNoiseTex, worldPos * 0.45);

    // Layer 1: Medium river pebbles
    vec2 normMed;
    vec4 pebbleMed = evaluatePebbleLayer(worldPos + 0.03 * (sandNoise.rg - 0.5), 4.6, normMed);

    // Layer 2: Large hero river cobbles scattered sparsely
    vec2 normLarge;
    vec4 pebbleLarge = evaluatePebbleLayer(worldPos * 0.85 + vec2(1.7, -2.3), 1.85, normLarge);
    float hasLargeStone = step(0.58, pebbleLarge.z) * smoothstep(0.05, 0.25, pebbleLarge.x);

    float stoneHeight = mix(pebbleMed.x * 0.65, pebbleLarge.x, hasLargeStone);
    float creviceMask = mix(pebbleMed.y, pebbleLarge.y, hasLargeStone);
    vec2 stoneNormalXY = mix(normMed, normLarge, hasLargeStone);

    // Construct 3D surface normal for diffuse + specular sunlight on underwater stones
    vec3 N = normalize(vec3(
        stoneNormalXY.x * 0.85 + (sandNoise.b - 0.5) * 0.14,
        stoneNormalXY.y * 0.85 + (sandNoise.a - 0.5) * 0.14,
        1.0
    ));

    // Stone base palette: warm slate, terracotta river stone, basalt, jade-gray, and sandy gravel
    float hVal = mix(pebbleMed.z, pebbleLarge.z, hasLargeStone);
    vec3 slateStone   = vec3(0.22, 0.26, 0.28);
    vec3 warmOchre    = vec3(0.46, 0.36, 0.26);
    vec3 jadePebble   = vec3(0.24, 0.34, 0.30);
    vec3 paleGranite  = vec3(0.52, 0.49, 0.44);

    vec3 stoneAlbedo = mix(slateStone, warmOchre, smoothstep(0.15, 0.45, hVal));
    stoneAlbedo = mix(stoneAlbedo, jadePebble, smoothstep(0.45, 0.72, hVal));
    stoneAlbedo = mix(stoneAlbedo, paleGranite, smoothstep(0.75, 0.95, hVal));

    // Fine mineral speckles & vein bands on stones
    float veins = smoothstep(0.46, 0.52, sin(worldPos.x * 28.0 + worldPos.y * 19.0 + sandNoise.r * 6.0));
    stoneAlbedo = mix(stoneAlbedo, stoneAlbedo * 1.18, veins * 0.35);
    stoneAlbedo *= (0.88 + 0.24 * sandNoise.b);

    // Sandy gravel bed in crevices between stones
    vec3 sandColor = vec3(0.38, 0.33, 0.24) * (0.75 + 0.5 * sandNoise.b);
    vec3 baseAlbedo = mix(sandColor * 0.55, stoneAlbedo, creviceMask);

    // Lush velvety green moss growing on the sunlit upper-left faces of larger stones
    float mossMask = smoothstep(0.32, 0.75, macroNoise.r + 0.25 * hasLargeStone)
                   * smoothstep(0.15, 0.65, N.x * -0.5 + N.y * 0.5 + 0.4)
                   * creviceMask;
    vec3 mossColor = mix(vec3(0.11, 0.26, 0.14), vec3(0.19, 0.38, 0.18), sandNoise.g);
    baseAlbedo = mix(baseAlbedo, mossColor, mossMask * 0.68);

    // Diffuse & wet underwater specular lighting from sunlight (upper-left)
    float NdotL = max(dot(N, uSunDir), 0.0);
    vec3 viewDir = vec3(0.0, 0.0, 1.0);
    vec3 halfVec = normalize(uSunDir + viewDir);
    float spec = pow(max(dot(N, halfVec), 0.0), 28.0) * 0.22 * creviceMask;

    // Dappled overhanging tree-canopy shadows drifting slowly in the breeze
    vec2 canopyUv = worldPos * 0.52 + vec2(sin(uTime * 0.18) * 0.04, cos(uTime * 0.15) * 0.04);
    float canopyNoise = texture(uNoiseTex, canopyUv).r;
    float edgeDist = length(vNdc * vec2(1.0, 0.85));
    float canopyShadow = smoothstep(0.42, 0.72, canopyNoise + edgeDist * 0.28);
    float sunlightTransmit = 1.0 - canopyShadow * 0.52;

    // Project animated caustics along sun refraction offset
    vec2 causticCoord = worldPos - uSunDir.xy * (0.28 * (1.0 - stoneHeight * 0.35));
    float caustics = computeCaustics(causticCoord, uTime) * sunlightTransmit * (0.35 + 0.65 * NdotL);

    // Warm golden sunlight vs cool teal-blue shadow ambient
    vec3 sunColor = vec3(1.05, 0.93, 0.72);
    vec3 shadowAmbient = vec3(0.10, 0.28, 0.34);

    vec3 litColor = baseAlbedo * (shadowAmbient + sunColor * NdotL * 0.88 * sunlightTransmit)
                  + baseAlbedo * sunColor * caustics * 0.62
                  + sunColor * spec * sunlightTransmit;

    // Beer-Lambert underwater depth absorption & depth fog toward pond floor
    float waterDepth = 1.0 - stoneHeight * 0.28 + 0.22 * edgeDist;
    float turbidity = mix(1.45, 0.52, uWaterClarity);
    vec3 deepTealFog = vec3(0.04, 0.16, 0.21);
    float fogFactor = exp(-waterDepth * turbidity);
    litColor = mix(deepTealFog, litColor, fogFactor);

    // Usability zone calming: gently lower contrast in top 15% (clock) and bottom 20% (dock icons)
    float uvY = vUv.y; // 0 = bottom, 1 = top
    float topCalm = smoothstep(0.82, 0.98, uvY);
    float bottomCalm = smoothstep(0.22, 0.02, uvY);
    float calmZone = max(topCalm * 0.35, bottomCalm * 0.42);
    litColor = mix(litColor, deepTealFog * 0.85 + litColor * 0.45, calmZone);

    fragColor = vec4(litColor, 1.0);
}
"""

    // =========================================================================================
    // 2. VALLISNERIA RIBBON SHADER (Swaying Underwater Grass with Out-of-Sync Sine Waves)
    // =========================================================================================
    const val VALLISNERIA_VERT = """#version 300 es
precision highp float;
layout(location = 0) in vec2 aLocalPos; // x in [-1, 1], y in [0, 1] along blade
layout(location = 1) in vec2 aUv;

uniform float uTime;
uniform float uAspect;
uniform vec2 uParallax;
uniform vec4 uBladeData; // x=rootX, y=rootY, z=length, w=width
uniform vec3 uBladeMeta; // x=baseAngle, y=phase, z=hueShift
uniform float uShadowPass; // 1.0 = shadow pass, 0.0 = main lit pass
uniform vec3 uSunDir;

out vec2 vUv;
out float vHueShift;
out float vSwayDeriv;

void main() {
    float v = aUv.y; // 0 at root, 1 at tip
    float phase = uBladeMeta.y;
    float baseAngle = uBladeMeta.x;

    // Out-of-sync traveling wave along blade spine
    float wave = sin(uTime * 1.15 + phase - v * 3.4) * 0.085 * pow(v, 1.35)
               + cos(uTime * 0.62 + phase * 1.7 - v * 2.1) * 0.045 * v;

    float angle = baseAngle + wave;
    vec2 spineDir = vec2(cos(angle), sin(angle));
    vec2 perpDir  = vec2(-spineDir.y, spineDir.x);

    vec2 worldPos = uBladeData.xy
                  + spineDir * (v * uBladeData.z)
                  + perpDir * (aLocalPos.x * uBladeData.w);

    if (uShadowPass > 0.5) {
        // Offset shadow toward pond floor away from upper-left sun
        worldPos -= uSunDir.xy * (0.04 + 0.09 * v);
    }

    // Parallax between floor (1.35) and surface (0.85)
    worldPos += uParallax * mix(1.30, 1.05, v);

    vUv = aUv;
    vHueShift = uBladeMeta.z;
    vSwayDeriv = wave;

    gl_Position = vec4(worldPos.x, worldPos.y * uAspect, 0.0, 1.0);
}
"""

    const val VALLISNERIA_FRAG = """#version 300 es
precision highp float;

in vec2 vUv;
in float vHueShift;
in float vSwayDeriv;

uniform float uShadowPass;
uniform float uTime;
uniform vec3 uSunDir;

out vec4 fragColor;

void main() {
    if (uShadowPass > 0.5) {
        float edgeAlpha = smoothstep(0.0, 0.25, 1.0 - abs(vUv.x * 2.0 - 1.0));
        fragColor = vec4(0.01, 0.05, 0.07, 0.28 * edgeAlpha * (1.0 - vUv.y * 0.35));
        return;
    }

    // Blade rib striations and translucent chlorophyll gradient from root to sunlit tip
    float centerRib = 1.0 - abs(vUv.x * 2.0 - 1.0);
    float striation = 0.88 + 0.12 * sin(vUv.x * 18.0);
    vec3 rootGreen = vec3(0.06, 0.22, 0.15);
    vec3 tipGreen  = mix(vec3(0.18, 0.48, 0.22), vec3(0.28, 0.54, 0.18), vHueShift);

    vec3 col = mix(rootGreen, tipGreen, pow(vUv.y, 0.85)) * striation;
    col += vec3(0.25, 0.32, 0.12) * max(0.0, vSwayDeriv * 4.5) * vUv.y;

    float alpha = smoothstep(0.0, 0.18, centerRib) * (1.0 - 0.15 * pow(vUv.y, 3.0));
    fragColor = vec4(col, alpha * 0.88);
}
"""

    // =========================================================================================
    // 3. KOI & SILVER MINNOW SHADER (Vertex Spine Bending + Procedural Kohaku/Sanke/Ogon Scales)
    // =========================================================================================
    const val KOI_VERT = """#version 300 es
precision highp float;

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec3 aUvPart; // x=u, y=spineT [0..1.2], z=partId

uniform float uTime;
uniform float uAspect;
uniform vec2 uParallax;
uniform vec3 uSunDir;

uniform vec3 uFishWorldPos;   // x, y, zDepth
uniform vec2 uFishScale;      // width, length
uniform float uFishHeading;   // radians
uniform float uSwimPhase;     // accumulated phase
uniform float uCurvature;     // smooth turn bend [-0.45..0.45]
uniform float uShadowPass;    // 1.0 = shadow on pond bed, 0.0 = lit 3D fish body

out vec3 vNormal;
out vec3 vUvPart;
out vec2 vWorldPos;
out float vDepth;

void main() {
    float spineT = aUvPart.y; // 0.0 at snout, 1.0 at tail peduncle, 1.22 at caudal fin tip
    float partId = aUvPart.z; // 0=body, 1=pectoral/ventral, 2=dorsal, 3=caudal

    vec3 localPos = aPos;
    vec3 localNorm = aNormal;

    // -------------------------------------------------------------------------
    // ARTICULATED SPINE BENDING MATH:
    // Anterior head (spineT < 0.2) stays rigid with slight counter-yaw;
    // Posterior trunk & caudal fin follow an increasing quadratic envelope:
    //   envelope(t) = 0.03 + 0.97 * t^1.85
    // combined with smooth turning curvature (C-bend) + traveling sine wave (S-bend).
    // -------------------------------------------------------------------------
    float waveEnvelope = 0.025 + 0.15 * pow(max(spineT, 0.0), 1.85);
    float waveArg = uSwimPhase - spineT * 4.35;
    float sWave = sin(waveArg) * waveEnvelope;
    float cBend = uCurvature * pow(max(spineT - 0.12, 0.0), 1.55) * 0.55;

    float lateralOffset = sWave + cBend;
    // Analytical derivative d(lateralOffset)/d(spineT) rotates vertex normals accurately
    float dWave = -4.35 * cos(waveArg) * waveEnvelope + uCurvature * 0.65 * max(spineT - 0.12, 0.0);

    // Pectoral sculling feather motion
    if (partId > 0.5 && partId < 1.5) {
        float finSide = sign(localPos.x);
        float flap = sin(uTime * 3.1 + finSide * 0.8 + uSwimPhase * 0.5) * 0.035 * aUvPart.x;
        localPos.y += flap;
        localPos.z += abs(flap) * 0.4;
    }

    localPos.x += lateralOffset;

    // Rotate normal in XY plane by spine tangent angle
    float bendAngle = atan(dWave * 0.38);
    float cb = cos(bendAngle);
    float sb = sin(bendAngle);
    localNorm.xy = vec2(
        localNorm.x * cb - localNorm.y * sb,
        localNorm.x * sb + localNorm.y * cb
    );

    // Scale to fish world dimensions
    vec2 scaledXY = vec2(localPos.x * uFishScale.x, localPos.y * uFishScale.y);

    // Mesh snout is at +Y (angle = PI/2); rotate to match uFishHeading
    float rot = uFishHeading - 1.5707963;
    float cr = cos(rot);
    float sr = sin(rot);

    vec2 rotatedXY = vec2(
        scaledXY.x * cr - scaledXY.y * sr,
        scaledXY.x * sr + scaledXY.y * cr
    );

    vec3 worldNormal = normalize(vec3(
        localNorm.x * cr - localNorm.y * sr,
        localNorm.x * sr + localNorm.y * cr,
        localNorm.z
    ));

    vec2 finalWorldXY = uFishWorldPos.xy + rotatedXY;

    if (uShadowPass > 0.5) {
        // Project soft shadow onto pond floor along sunlight direction proportional to fish depth
        float shadowDist = uFishWorldPos.z * 0.34;
        finalWorldXY -= uSunDir.xy * shadowDist;
        finalWorldXY += uParallax * 1.35; // Floor parallax plane
    } else {
        // Fish depth parallax plane between floor (1.35) and surface (0.85)
        float parallaxFactor = mix(0.92, 1.25, uFishWorldPos.z);
        finalWorldXY += uParallax * parallaxFactor;
    }

    vNormal = worldNormal;
    vUvPart = aUvPart;
    vWorldPos = finalWorldXY;
    vDepth = uFishWorldPos.z;

    gl_Position = vec4(finalWorldXY.x, finalWorldXY.y * uAspect, 0.0, 1.0);
}
"""

    const val KOI_FRAG = """#version 300 es
precision highp float;

in vec3 vNormal;
in vec3 vUvPart;
in vec2 vWorldPos;
in float vDepth;

uniform float uTime;
uniform vec3 uSunDir;
uniform float uWaterClarity;
uniform int uPatternVariant; // 0=Kohaku, 1=Taisho Sanke, 2=Showa, 3=Yamabuki Ogon, 4=Platinum, 5=Tancho, 6=Minnow
uniform float uPatternSeed;
uniform float uShadowPass;
uniform sampler2D uNoiseTex;

out vec4 fragColor;

// Procedural domain-warped organic koi markings
float koiPatchNoise(vec2 p, float seed) {
    vec4 n1 = texture(uNoiseTex, p * 0.42 + vec2(seed * 0.17, seed * 0.31));
    vec4 n2 = texture(uNoiseTex, p * 0.85 + (n1.rg - 0.5) * 0.22 - vec2(seed * 0.23, 0.11));
    return mix(n1.r, n2.a, 0.45);
}

void main() {
    float u = vUvPart.x;       // 0..1 across left-to-right flank
    float spineT = vUvPart.y;  // 0..1.22 from nose to tail tip
    float partId = vUvPart.z;  // 0=body, 1=pectoral, 2=dorsal, 3=caudal

    // 1. Soft projected underwater shadow pass
    if (uShadowPass > 0.5) {
        float finFade = (partId > 0.5) ? (1.0 - 0.65 * u) : 1.0;
        float bodyFade = smoothstep(1.22, 0.82, spineT);
        float shadowAlpha = 0.42 * finFade * bodyFade * (1.0 - vDepth * 0.35);
        fragColor = vec4(0.01, 0.06, 0.09, shadowAlpha);
        return;
    }

    vec3 N = normalize(vNormal);

    // 2. Base koi pigment colors
    vec3 pureWhite  = vec3(0.97, 0.96, 0.93);
    vec3 beniOrange = vec3(0.95, 0.27, 0.06); // Deep vermilion-orange Hi pigment
    vec3 sumiBlack  = vec3(0.08, 0.09, 0.11); // Lacquer-black Sumi pigment
    vec3 ogonGold   = vec3(0.96, 0.74, 0.18); // Metallic Yamabuki gold

    vec2 bodyCoord = vec2((u - 0.5) * 2.2, spineT * 3.2);
    float patch1 = koiPatchNoise(bodyCoord, uPatternSeed);
    float patch2 = koiPatchNoise(bodyCoord * 1.45 + vec2(1.9, -0.7), uPatternSeed + 4.3);

    vec3 albedo = pureWhite;
    float metallic = 0.15;

    if (uPatternVariant == 0) {
        // Kohaku: Snow-white body with bold vermilion-orange dorsal saddle patches
        float hiMask = smoothstep(0.46, 0.51, patch1 + 0.12 * (1.0 - abs(u - 0.5) * 2.0));
        albedo = mix(pureWhite, beniOrange, hiMask);
    } else if (uPatternVariant == 1) {
        // Taisho Sanke: White base, orange Hi saddles, plus smaller jet-black Sumi spots
        float hiMask = smoothstep(0.47, 0.52, patch1);
        float sumiMask = smoothstep(0.64, 0.68, patch2) * smoothstep(0.12, 0.20, spineT);
        albedo = mix(pureWhite, beniOrange, hiMask);
        albedo = mix(albedo, sumiBlack, sumiMask);
    } else if (uPatternVariant == 2) {
        // Showa Sanshoku: Rich Sumi black wrapping with bold orange & white markings
        float whiteMask = smoothstep(0.44, 0.49, patch1);
        float hiMask = smoothstep(0.50, 0.55, patch2);
        albedo = mix(sumiBlack, pureWhite, whiteMask);
        albedo = mix(albedo, beniOrange, hiMask);
    } else if (uPatternVariant == 3) {
        // Yamabuki Ogon: Solid metallic golden koi
        albedo = mix(ogonGold, vec3(1.0, 0.86, 0.42), 1.0 - abs(u - 0.5) * 1.5);
        metallic = 0.65;
    } else if (uPatternVariant == 4) {
        // Platinum Ogon / Bekko: Pearlescent silver-white with subtle charcoal stepping stones
        float sumiMask = smoothstep(0.68, 0.72, patch2) * step(0.18, spineT);
        albedo = mix(vec3(0.95, 0.97, 0.99), sumiBlack, sumiMask);
        metallic = 0.55;
    } else if (uPatternVariant == 5) {
        // Tancho: Pure snow white with a prized crimson-orange crown circle on the head
        float crownDist = length(vec2((u - 0.5) * 2.4, (spineT - 0.11) * 4.5));
        float crownMask = 1.0 - smoothstep(0.24, 0.29, crownDist + (patch1 - 0.5) * 0.08);
        albedo = mix(pureWhite, vec3(0.94, 0.18, 0.05), crownMask);
    } else {
        // Silver Minnow: Translucent olive-silver back with bright iridescent lateral stripe
        float lateralStripe = exp(-pow((abs(u - 0.5) - 0.28) * 12.0, 2.0));
        albedo = mix(vec3(0.42, 0.52, 0.48), vec3(0.85, 0.92, 0.96), lateralStripe);
        metallic = 0.75;
    }

    // 3. Procedural overlapping fish scales on main trunk (partId == 0)
    float alpha = 1.0;
    if (partId < 0.5) {
        // Eyes near head flanks (spineT ~ 0.085, u ~ 0.14 and 0.86)
        float eyeDistL = length(vec2((u - 0.14) * 3.5, (spineT - 0.085) * 7.5));
        float eyeDistR = length(vec2((u - 0.86) * 3.5, (spineT - 0.085) * 7.5));
        float eyeDist = min(eyeDistL, eyeDistR);
        if (eyeDist < 0.16) {
            vec3 eyeCol = (eyeDist < 0.09) ? vec3(0.03, 0.03, 0.04) : vec3(0.85, 0.72, 0.32);
            albedo = mix(albedo, eyeCol, smoothstep(0.16, 0.12, eyeDist));
        } else if (spineT > 0.16 && spineT < 0.92) {
            // Overlapping diamond scale lattice
            vec2 scaleUv = vec2(u * 18.0, spineT * 34.0);
            scaleUv.x += 0.5 * mod(floor(scaleUv.y), 2.0);
            vec2 sf = fract(scaleUv) - 0.5;
            float scaleEdge = smoothstep(0.36, 0.48, length(sf * vec2(1.0, 1.25)));
            albedo *= (1.0 - 0.09 * scaleEdge);
            N.xy += sf * 0.12;
            N = normalize(N);
        }
    } else {
        // Translucent silk fin rays on pectoral, dorsal, and caudal fins
        float rayFreq = (partId > 2.5) ? 38.0 : 24.0;
        float rays = 0.78 + 0.22 * sin(u * rayFreq + spineT * 10.0);
        albedo = mix(pureWhite * 0.95, albedo, 0.28) * rays;

        if (partId > 2.5) {
            // Caudal tail feather fade
            float tailTip = clamp((spineT - 0.86) / 0.36, 0.0, 1.0);
            alpha = (1.0 - 0.62 * pow(tailTip, 1.4)) * (0.65 + 0.35 * rays);
        } else {
            // Pectoral / Dorsal feather fade
            alpha = (1.0 - 0.58 * pow(u, 1.3)) * 0.82;
        }
    }

    // 4. Sunlight illumination + animated underwater caustics on the fish's back
    float NdotL = max(dot(N, uSunDir), 0.0);
    vec3 viewDir = vec3(0.0, 0.0, 1.0);
    vec3 halfVec = normalize(uSunDir + viewDir);
    float spec = pow(max(dot(N, halfVec), 0.0), mix(24.0, 48.0, metallic)) * mix(0.28, 0.65, metallic);

    // Subsurface scattering glow at thin fin edges and flanks
    float sss = pow(1.0 - max(N.z, 0.0), 2.2) * 0.35;

    // Caustic ribbons moving across the koi back
    vec2 cUv1 = vWorldPos * 1.25 + vec2(uTime * 0.032, -uTime * 0.024);
    vec2 cUv2 = vWorldPos * 1.55 + vec2(-uTime * 0.027, uTime * 0.036);
    float c1 = 1.0 - abs(texture(uNoiseTex, cUv1).r * 2.0 - 1.0);
    float c2 = 1.0 - abs(texture(uNoiseTex, cUv2).a * 2.0 - 1.0);
    float fishCaustics = pow(c1 * c2, 3.0) * 2.8 * smoothstep(0.1, 0.6, N.z);

    vec3 sunLight = vec3(1.06, 0.94, 0.76);
    vec3 ambientTeal = vec3(0.14, 0.33, 0.39);

    vec3 color = albedo * (ambientTeal + sunLight * (NdotL * 0.85 + sss))
               + albedo * sunLight * fishCaustics * 0.45
               + sunLight * spec;

    // Depth tinting according to how deep the fish is swimming
    float turbidity = mix(1.25, 0.42, uWaterClarity);
    vec3 waterTint = vec3(0.05, 0.20, 0.26);
    color = mix(waterTint, color, exp(-vDepth * turbidity));

    fragColor = vec4(color, alpha);
}
"""

    // =========================================================================================
    // 4. WATER COMPOSITE SHADER (Refraction, Touch Ripples, Sun Sparkle, God Rays, Lily Pads, Lotus)
    // =========================================================================================
    const val WATER_COMPOSITE_VERT = """#version 300 es
precision highp float;
layout(location = 0) in vec2 aPos;
layout(location = 1) in vec2 aUv;

out vec2 vUv;
out vec2 vNdc;

void main() {
    vUv = aUv;
    vNdc = aPos;
    gl_Position = vec4(aPos, 0.0, 1.0);
}
"""

    const val WATER_COMPOSITE_FRAG = """#version 300 es
precision highp float;

in vec2 vUv;
in vec2 vNdc;

uniform sampler2D uUnderwaterTex;
uniform sampler2D uNoiseTex;

uniform float uTime;
uniform float uAspect;
uniform vec2 uParallax;
uniform vec3 uSunDir;
uniform float uWaterClarity;

// Up to 10 active touch/raindrop ripples: vec4(worldX, worldY, age, amplitude)
uniform vec4 uRipples[10];

// 7 Lily pads: vec4(x, y, radius, baseRot), vec4(notchAngle, swayPhase, hasLotus, lotusVariant)
uniform vec4 uLilyPadsA[7];
uniform vec4 uLilyPadsB[7];

// Active particle motes/bubbles: vec4(x, y, z, sizeAndFlag)
uniform int uParticleCount;
uniform vec4 uParticles[48];

out vec4 fragColor;

// Computes water surface height & normal from animated Gerstner-like FBM waves + interactive ripples
vec3 computeWaterNormal(vec2 worldPos) {
    vec2 wUv1 = worldPos * 0.95 + vec2(uTime * 0.028, uTime * 0.019);
    vec2 wUv2 = worldPos * 1.45 - vec2(uTime * 0.022, -uTime * 0.031);
    vec2 wUv3 = worldPos * 2.80 + vec2(-uTime * 0.041, -uTime * 0.025);

    vec4 s1 = texture(uNoiseTex, wUv1);
    vec4 s2 = texture(uNoiseTex, wUv2);
    vec4 s3 = texture(uNoiseTex, wUv3);

    vec2 grad = (s1.rg - 0.5) * 0.045
              + (s2.ba - 0.5) * 0.032
              + (s3.rg - 0.5) * 0.016;

    // Add interactive touch ripples and raindrop rings
    for (int i = 0; i < 10; i++) {
        vec4 rp = uRipples[i];
        if (rp.z < 3.8 && rp.w > 0.001) {
            vec2 delta = worldPos - rp.xy;
            float dist = length(delta);
            float waveSpeed = 0.42;
            float ringRadius = rp.z * waveSpeed;
            float band = dist - ringRadius;
            // Gaussian envelope around expanding ring
            float env = exp(-band * band * 55.0) * exp(-rp.z * 1.05) * rp.w;
            float phase = band * 48.0 - rp.z * 14.0;
            float dHeight = cos(phase) * env * 0.12;
            grad += (delta / max(dist, 0.001)) * dHeight;
        }
    }

    return normalize(vec3(-grad.x, -grad.y, 1.0));
}

// Evaluates a single lily pad + optional 3D layered pink/white lotus flower
vec4 evaluateLilyPadAndLotus(
    vec2 worldPos,
    vec4 padA,
    vec4 padB,
    out float outShadowAlpha
) {
    outShadowAlpha = 0.0;
    float swayX = sin(uTime * 0.55 + padB.y) * 0.018;
    float swayY = cos(uTime * 0.48 + padB.y * 1.3) * 0.018;
    vec2 padCenter = padA.xy + vec2(swayX, swayY) + uParallax * 0.85;
    float radius = padA.z;

    // Quick bounding check
    vec2 rel = worldPos - padCenter;
    float dist = length(rel);

    // Shadow offset onto water/floor along sun direction
    vec2 shadowRel = (worldPos + uSunDir.xy * 0.075) - padCenter;
    float shadowDist = length(shadowRel);
    if (shadowDist < radius * 1.08) {
        float sAng = atan(shadowRel.y, shadowRel.x) - padB.x;
        sAng = atan(sin(sAng), cos(sAng));
        float notchS = smoothstep(0.08, 0.34, abs(sAng));
        float effRadS = radius * mix(0.12, 1.0, notchS);
        outShadowAlpha = (1.0 - smoothstep(effRadS * 0.82, effRadS * 1.05, shadowDist)) * 0.46;
    }

    if (dist > radius * 1.05) {
        return vec4(0.0);
    }

    float angle = atan(rel.y, rel.x);
    float notchDiff = atan(sin(angle - padB.x), cos(angle - padB.x));

    // Characteristic V-shaped botanical cleft in the lily pad leaf
    float notchFactor = smoothstep(0.07, 0.32, abs(notchDiff));
    float wavyRim = 1.0 + 0.025 * sin(angle * 11.0 + padB.y);
    float effectiveRadius = radius * wavyRim * mix(0.08, 1.0, notchFactor);

    float padMask = 1.0 - smoothstep(effectiveRadius - 0.008, effectiveRadius + 0.004, dist);
    if (padMask <= 0.001) {
        return vec4(0.0);
    }

    float rNorm = clamp(dist / max(effectiveRadius, 0.001), 0.0, 1.0);

    // Radial botanical venation from pad navel
    float mainVeins = pow(0.5 + 0.5 * cos(notchDiff * 16.0), 6.0) * (1.0 - rNorm * 0.5) * notchFactor;
    float subVeins  = pow(0.5 + 0.5 * cos(notchDiff * 32.0 + rNorm * 4.0), 8.0) * rNorm * 0.5;

    vec3 padGreenCenter = vec3(0.14, 0.38, 0.19);
    vec3 padGreenMid    = vec3(0.18, 0.46, 0.22);
    vec3 padRimBronze   = vec3(0.36, 0.28, 0.16);
    vec3 veinColor      = vec3(0.34, 0.58, 0.28);

    vec3 padCol = mix(padGreenCenter, padGreenMid, smoothstep(0.0, 0.75, rNorm));
    padCol = mix(padCol, veinColor, (mainVeins * 0.45 + subVeins * 0.25));
    // Upturned reddish-bronze outer rim
    float rimMask = smoothstep(0.84, 0.98, rNorm);
    padCol = mix(padCol, padRimBronze, rimMask * 0.65);

    // Water droplets resting on hydrophobic pad surface
    vec2 dropUv = rel * 18.0;
    float dropDist = length(fract(dropUv) - 0.5);
    float droplet = (1.0 - smoothstep(0.08, 0.12, dropDist)) * step(0.82, sin(floor(dropUv.x) * 17.3 + floor(dropUv.y) * 31.7));

    vec3 padNormal = normalize(vec3(
        rel.x / radius * (0.25 - 0.6 * rimMask),
        rel.y / radius * (0.25 - 0.6 * rimMask),
        1.0
    ));
    float padNdotL = max(dot(padNormal, uSunDir), 0.0);
    padCol = padCol * (vec3(0.18, 0.32, 0.34) + vec3(1.04, 0.94, 0.76) * padNdotL * 0.85)
           + vec3(0.95, 0.98, 1.0) * droplet * 0.45;

    // Optional blooming pink/cream Lotus flower (Nelumbo nucifera) with 3 concentric rings of petals
    if (padB.z > 0.5) {
        vec2 lotusCenter = padCenter + vec2(cos(padA.w), sin(padA.w)) * (radius * 0.18);
        vec2 lRel = worldPos - lotusCenter;
        float lDist = length(lRel);
        float lotusRadius = radius * 0.56;

        if (lDist < lotusRadius) {
            float lAngle = atan(lRel.y, lRel.x) + padA.w + sin(uTime * 0.4 + padB.y) * 0.03;
            float lr = lDist / lotusRadius;

            // Outer, middle, and inner pointed petal rings
            float outerPetals = 0.68 + 0.28 * pow(abs(cos(lAngle * 5.0)), 0.65);
            float midPetals   = 0.48 + 0.24 * pow(abs(cos(lAngle * 5.0 + 0.62)), 0.70);
            float innerPetals = 0.28 + 0.16 * pow(abs(cos(lAngle * 4.0 + 1.15)), 0.75);

            vec3 petalBase = (padB.w < 0.5) ? vec3(0.98, 0.88, 0.91) : vec3(0.99, 0.94, 0.89);
            vec3 petalTip  = (padB.w < 0.5) ? vec3(0.92, 0.36, 0.56) : vec3(0.95, 0.52, 0.64);

            // Soft drop shadow from lotus onto the lily pad
            float lotusPadShadow = 1.0 - smoothstep(lotusRadius * 0.55, lotusRadius * 0.98, lDist);
            padCol *= (1.0 - 0.38 * lotusPadShadow);

            if (lr < outerPetals) {
                float pGrad = clamp(lr / outerPetals, 0.0, 1.0);
                vec3 pCol = mix(petalBase, petalTip, pow(pGrad, 1.4));
                pCol *= (0.75 + 0.35 * max(dot(normalize(vec3(lRel, lotusRadius * 0.5)), uSunDir), 0.0));
                padCol = mix(padCol, pCol, smoothstep(outerPetals, outerPetals - 0.05, lr));
            }
            if (lr < midPetals) {
                float pGrad = clamp(lr / midPetals, 0.0, 1.0);
                vec3 pCol = mix(petalBase * 1.03, petalTip, pow(pGrad, 1.2));
                pCol *= (0.82 + 0.32 * max(dot(normalize(vec3(lRel, lotusRadius * 0.4)), uSunDir), 0.0));
                padCol = mix(padCol, pCol, smoothstep(midPetals, midPetals - 0.04, lr));
            }
            if (lr < innerPetals) {
                float pGrad = clamp(lr / innerPetals, 0.0, 1.0);
                vec3 pCol = mix(petalBase * 1.05, petalTip, pGrad);
                padCol = mix(padCol, pCol, smoothstep(innerPetals, innerPetals - 0.04, lr));
            }
            // Golden receptacle & stamen crown in the center
            if (lr < 0.16) {
                float stamenRing = smoothstep(0.08, 0.15, lr) * (0.5 + 0.5 * sin(lAngle * 24.0));
                vec3 centerGold = mix(vec3(0.95, 0.78, 0.18), vec3(0.98, 0.62, 0.10), stamenRing);
                padCol = mix(padCol, centerGold, smoothstep(0.16, 0.12, lr));
            }
        }
    }

    return vec4(padCol, padMask);
}

void main() {
    vec2 worldPos = vec2(vNdc.x, vNdc.y / uAspect);

    // 1. Animated water normal & Snell's law refraction of the underwater FBO
    vec3 waterNormal = computeWaterNormal(worldPos + uParallax * 0.95);
    vec2 refractOffset = waterNormal.xy * 0.022;
    vec2 refractedUv = clamp(vUv + vec2(refractOffset.x, refractOffset.y * uAspect), 0.002, 0.998);

    // Subtle chromatic dispersion across refracted water ripples
    vec3 underwaterCol;
    underwaterCol.r = texture(uUnderwaterTex, clamp(refractedUv + refractOffset * 0.08, 0.002, 0.998)).r;
    underwaterCol.g = texture(uUnderwaterTex, refractedUv).g;
    underwaterCol.b = texture(uUnderwaterTex, clamp(refractedUv - refractOffset * 0.08, 0.002, 0.998)).b;

    // 2. Drifting underwater sediment motes & rising bubbles catching the sunlight
    vec2 motesWorld = worldPos + uParallax * 1.05;
    for (int i = 0; i < 48; i++) {
        if (i >= uParticleCount) break;
        vec4 pt = uParticles[i];
        vec2 pDelta = motesWorld - pt.xy;
        float pDist = length(pDelta);
        float pSize = abs(pt.w);
        if (pDist < pSize) {
            float radial = 1.0 - pDist / pSize;
            if (pt.w < 0.0) {
                // Rising air bubble with crisp specular rim
                float rim = smoothstep(0.35, 0.85, pDist / pSize) * radial;
                underwaterCol += vec3(0.75, 0.92, 0.98) * rim * 0.65;
            } else {
                // Sunlit drifting sediment mote
                underwaterCol += vec3(1.0, 0.88, 0.58) * (radial * radial) * (0.25 + 0.45 * pt.z);
            }
        }
    }

    // 3. Evaluate floating Lily Pads & Lotus Flowers + their shadows on the water
    vec4 topFoliage = vec4(0.0);
    float totalPadShadow = 0.0;
    for (int i = 0; i < 7; i++) {
        float padShadow;
        vec4 padCol = evaluateLilyPadAndLotus(worldPos, uLilyPadsA[i], uLilyPadsB[i], padShadow);
        totalPadShadow = max(totalPadShadow, padShadow);
        topFoliage = mix(topFoliage, vec4(padCol.rgb, 1.0), padCol.a);
    }

    underwaterCol *= (1.0 - totalPadShadow * (1.0 - topFoliage.a));

    // 4. Fresnel sky reflection + sunlight surface sparkle on water ripples
    vec3 viewDir = vec3(0.0, 0.0, 1.0);
    float NdotV = max(dot(waterNormal, viewDir), 0.0);
    // Schlick Fresnel approximation (F0 = 0.02 for water)
    float fresnel = 0.02 + 0.22 * pow(1.0 - NdotV, 4.0);
    vec3 skyReflectColor = mix(vec3(0.42, 0.64, 0.74), vec3(0.82, 0.89, 0.94), vUv.y);

    vec3 halfVec = normalize(uSunDir + viewDir);
    float NdotH = max(dot(waterNormal, halfVec), 0.0);
    float sunGlint = pow(NdotH, 140.0) * 1.35 + pow(NdotH, 28.0) * 0.14;

    // Calm top 15% and bottom 20% so bright specular glints never obscure clock/icons
    float usabilityCalm = smoothstep(0.04, 0.20, vUv.y) * (1.0 - smoothstep(0.84, 0.96, vUv.y));
    sunGlint *= (0.25 + 0.75 * usabilityCalm);

    vec3 waterSurfaceCol = mix(underwaterCol, skyReflectColor, fresnel)
                         + vec3(1.08, 0.96, 0.78) * sunGlint;

    // Blend floating lily pads and lotus flowers on top of the water surface
    vec3 sceneCol = mix(waterSurfaceCol, topFoliage.rgb, topFoliage.a);

    // 5. Overhanging lakeside maple/bamboo leaves at upper-left & lower-right corners
    // casting soft dappled foreground framing
    vec2 cornerTL = vUv - vec2(0.0, 1.0);
    float distTL = length(cornerTL * vec2(1.2, 1.6));
    if (distTL < 0.42) {
        float ang = atan(cornerTL.y, cornerTL.x) + sin(uTime * 0.75) * 0.04;
        float leafLobes = 0.28 + 0.11 * cos(ang * 9.0) + 0.04 * sin(ang * 27.0);
        float leafMask = 1.0 - smoothstep(leafLobes - 0.02, leafLobes + 0.01, distTL);
        vec3 leafCol = mix(vec3(0.09, 0.24, 0.12), vec3(0.24, 0.44, 0.18), 1.0 - distTL / 0.42);
        sceneCol = mix(sceneCol, leafCol, leafMask * 0.85);
    }

    // 6. Volumetric God Rays (sunbeams entering from upper-left at uSunDir angle)
    // Computed by projecting coordinates perpendicular to the 2D sun ray vector
    // and layering animated low-frequency noise shafts.
    vec2 sun2D = normalize(vec2(-0.72, 0.69)); // Upper-left sun source
    vec2 sunPerp = vec2(-sun2D.y, sun2D.x);
    float alongRay = dot(vNdc - vec2(-0.85, 0.95), -sun2D);
    float acrossRay = dot(vNdc, sunPerp);

    float rayNoise1 = texture(uNoiseTex, vec2(acrossRay * 1.35 + uTime * 0.012, alongRay * 0.15)).r;
    float rayNoise2 = texture(uNoiseTex, vec2(acrossRay * 2.40 - uTime * 0.016, alongRay * 0.22)).a;
    float shaftPattern = pow(rayNoise1 * rayNoise2, 1.65) * 2.4;

    // Attenuate rays with distance from upper-left corner
    float rayFalloff = exp(-max(alongRay, 0.0) * 0.62) * smoothstep(-0.2, 0.8, 1.0 - abs(acrossRay * 0.65));
    vec3 godRayColor = vec3(1.04, 0.91, 0.64) * shaftPattern * rayFalloff * 0.28 * usabilityCalm;

    sceneCol += godRayColor;

    // Subtle filmic ACES-inspired tone mapping for rich highlights and deep teal shadows
    sceneCol = (sceneCol * (2.51 * sceneCol + 0.03)) / (sceneCol * (2.43 * sceneCol + 0.59) + 0.14);

    fragColor = vec4(clamp(sceneCol, 0.0, 1.0), 1.0);
}
"""

    fun buildProgram(vertexSource: String, fragmentSource: String): Int {
        val vs = compileShader(GLES30.GL_VERTEX_SHADER, vertexSource)
        val fs = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
        val program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vs)
        GLES30.glAttachShader(program, fs)
        GLES30.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val info = GLES30.glGetProgramInfoLog(program)
            GLES30.glDeleteProgram(program)
            throw RuntimeException("GLSL link failed: $info")
        }
        GLES30.glDeleteShader(vs)
        GLES30.glDeleteShader(fs)
        return program
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)
        val status = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val info = GLES30.glGetShaderInfoLog(shader)
            Log.e(TAG, "Shader compile error: $info")
            GLES30.glDeleteShader(shader)
            throw RuntimeException("GLSL compile failed: $info")
        }
        return shader
    }
}

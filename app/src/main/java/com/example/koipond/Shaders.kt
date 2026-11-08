package com.example.koipond

import android.opengl.GLES30
import android.util.Log

/**
 * GLSL ES 3.0 shaders for the Split-View ("Half Over / Half Underwater") Phone Live Wallpaper:
 *   1. SPLIT_BACKDROP_FRAG:
 *      - Upper 38% (Above Water): Warm sunlit sky, soft mist, distant mossy boulders & shore reeds,
 *        plus perspective-foreshortened water surface with floating lily pads & lotus blossoms.
 *      - Wave Meniscus Line: Glowing refractive air-water interface with surface ripples.
 *      - Lower 62% (Underwater Cross-Section): Crystal-clear turquoise-teal water column,
 *        diagonal volumetric sunbeams (god rays), caustic light webs, and a sloped 3D river-cobble bed.
 *   2. VALLISNERIA_VERT/FRAG:
 *      - Underwater seagrass & kelp growing upward from the sloped riverbed, swaying in the current.
 *   3. KOI_VERT/FRAG (Multi-Species 3D Side-View Shader):
 *      - Renders 8 distinct species (Clownfish, Blue Tang, Moorish Idol, Koi Carp, Neon Tetra,
 *        Royal Betta, Mandarin Dragonet, and Silver Minnow) with 3D yaw/pitch rotation and spine undulation.
 *   4. WATER_COMPOSITE_FRAG:
 *      - Adds foreground underwater refraction, rising air bubbles, sunlit plankton motes,
 *        the crisp glowing meniscus wave line, and ACES filmic tone mapping.
 */
object Shaders {

    private const val TAG = "MinamoShaders"

    // =========================================================================================
    // 1. SPLIT-VIEW BACKDROP SHADER (Over-Water Sky & Surface + Underwater Column & Riverbed)
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
uniform vec3 uSunDir;
uniform float uWaterClarity;
uniform sampler2D uNoiseTex;

out vec4 fragColor;

vec2 hash2(vec2 p) {
    p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
    return fract(sin(p) * 43758.5453123);
}

// Evaluates 3D rounded river cobbles for the underwater sloped floor
vec4 evaluateRiverCobbles(vec2 uv, out vec2 outNorm) {
    vec2 iuv = floor(uv);
    vec2 fuv = fract(uv);
    float minDist = 8.0;
    float secondDist = 8.0;
    vec2 bestVec = vec2(0.0);
    vec2 bestRnd = vec2(0.0);

    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 nb = vec2(float(x), float(y));
            vec2 rnd = hash2(iuv + nb);
            vec2 pt = nb + 0.18 + 0.64 * rnd - fuv;
            pt.x *= 0.82; // Perspective foreshortening on sloped floor
            float d = length(pt);
            if (d < minDist) {
                secondDist = minDist;
                minDist = d;
                bestVec = -pt;
                bestRnd = rnd;
            } else if (d < secondDist) {
                secondDist = d;
            }
        }
    }
    float crevice = smoothstep(0.02, 0.15, secondDist - minDist);
    float rad = 0.48 + 0.16 * bestRnd.x;
    float rNorm = clamp(minDist / rad, 0.0, 1.0);
    outNorm = (bestVec / rad) * crevice;
    float h = sqrt(max(0.0, 1.0 - rNorm * rNorm)) * crevice;
    return vec4(h, crevice, bestRnd.x, bestRnd.y);
}

// Evaluates animated surface waterline height at horizontal position x
float getWaterlineY(float x, float time) {
    float baseLine = 0.22 - uParallax.y * 0.25;
    float w1 = sin(x * 3.8 + time * 1.35) * 0.016;
    float w2 = cos(x * 7.2 - time * 1.95) * 0.008;
    float w3 = sin(x * 13.0 + time * 2.60) * 0.003;
    return baseLine + w1 + w2 + w3;
}

void main() {
    float x = vNdc.x + uParallax.x * 0.85;
    float y = vNdc.y;
    float waterlineY = getWaterlineY(x, uTime);

    // Sun position in upper-left sky
    vec2 sunPosNdc = vec2(-0.52 + uSunDir.x * 0.25, 0.72 + uSunDir.z * 0.12);

    // =========================================================================
    // REGION A: ABOVE WATER (Over-Water Sky, Distant Lush Bank, Recessed Surface)
    // =========================================================================
    if (y >= waterlineY) {
        float horizonY = waterlineY + 0.16;

        if (y >= horizonY) {
            // 1. Warm Golden-Cyan Natural Sky with Sun Glow & Soft Clouds
            float skyT = clamp((y - horizonY) / (1.0 - horizonY), 0.0, 1.0);
            vec3 zenithSky  = vec3(0.26, 0.56, 0.78);
            vec3 horizonSky = vec3(0.86, 0.91, 0.88);
            vec3 skyCol = mix(horizonSky, zenithSky, pow(skyT, 0.65));

            // Soft drifting clouds
            vec2 cloudUv = vec2(x * 0.35 + uTime * 0.006, skyT * 0.8);
            float cNoise = texture(uNoiseTex, cloudUv).r * 0.6 + texture(uNoiseTex, cloudUv * 2.3).a * 0.4;
            float cloudMask = smoothstep(0.48, 0.78, cNoise) * (1.0 - skyT * 0.5);
            skyCol = mix(skyCol, vec3(1.0, 0.97, 0.92), cloudMask * 0.55);

            // Warm sun disc & atmospheric scattering
            vec2 sunDelta = vec2(vNdc.x - sunPosNdc.x, (vNdc.y - sunPosNdc.y) * uAspect);
            float sunDist = length(sunDelta);
            skyCol += vec3(1.05, 0.88, 0.55) * exp(-sunDist * 3.2) * 0.65;
            skyCol += vec3(1.0, 0.98, 0.90) * smoothstep(0.11, 0.04, sunDist) * 0.75;

            // Distant lush shoreline silhouette & mossy rocks just above horizonY
            float bankNoise = texture(uNoiseTex, vec2(x * 0.28 + 0.4, 0.25)).r;
            float bankHeight = horizonY + 0.035 + bankNoise * 0.085 * (0.6 + 0.4 * abs(vNdc.x));
            if (y < bankHeight) {
                float bGrad = (y - horizonY) / max(bankHeight - horizonY, 0.001);
                vec3 foliageDark = vec3(0.14, 0.28, 0.20);
                vec3 foliageLit  = vec3(0.32, 0.48, 0.26);
                vec3 bankCol = mix(foliageDark, foliageLit, bGrad);
                // Warm golden sun haze over the shoreline
                bankCol = mix(bankCol, horizonSky * 0.95, 0.32);
                skyCol = bankCol;
            }

            // Calm top 15% slightly so the phone lock/home clock stays crisp
            float clockCalm = smoothstep(0.75, 0.98, vUv.y);
            skyCol *= (1.0 - clockCalm * 0.18);

            fragColor = vec4(skyCol, 1.0);
            return;
        } else {
            // 2. Perspective Water Surface Strip (between waterlineY and horizonY)
            // Looking obliquely across the pond surface toward the shoreline
            float surfT = clamp((y - waterlineY) / max(horizonY - waterlineY, 0.001), 0.0, 1.0);
            float perspZ = 1.0 / (0.18 + surfT * 1.4);
            vec2 surfUv = vec2(x * perspZ * 0.45 + uTime * 0.02, perspZ * 0.65 - uTime * 0.03);
            vec4 waveSample = texture(uNoiseTex, surfUv);

            vec3 reflectSky = mix(vec3(0.16, 0.48, 0.56), vec3(0.68, 0.84, 0.86), surfT);
            float rippleGlint = pow(waveSample.r, 5.0) * (1.0 - surfT * 0.5);
            vec3 surfCol = reflectSky + vec3(1.0, 0.94, 0.75) * rippleGlint * 0.45;

            // Perspective-foreshortened floating lily pads on the water surface strip
            vec2 pad1 = vec2(-0.42 + sin(uTime * 0.5) * 0.02, waterlineY + 0.055);
            vec2 pad2 = vec2( 0.46 + cos(uTime * 0.4) * 0.02, waterlineY + 0.075);
            vec2 pad3 = vec2( 0.14 + sin(uTime * 0.6) * 0.015, waterlineY + 0.095);

            // Foreshortened ellipses (wide in X, compressed in Y by 4.2x)
            float dPad1 = length(vec2(x - pad1.x, (y - pad1.y) * 4.2));
            float dPad2 = length(vec2(x - pad2.x, (y - pad2.y) * 4.2));
            float dPad3 = length(vec2(x - pad3.x, (y - pad3.y) * 4.5));

            if (dPad1 < 0.18 || dPad2 < 0.16 || dPad3 < 0.13) {
                vec3 padGreen = mix(vec3(0.16, 0.42, 0.20), vec3(0.32, 0.58, 0.26), waveSample.g);
                surfCol = padGreen;
                // Blooming pink lotus on pad1
                float dLotus = length(vec2(x - pad1.x, (y - (pad1.y + 0.012)) * 2.6));
                if (dLotus < 0.065) {
                    surfCol = mix(vec3(0.96, 0.44, 0.64), vec3(1.0, 0.92, 0.85), 1.0 - dLotus / 0.065);
                }
            }

            fragColor = vec4(surfCol, 1.0);
            return;
        }
    }

    // =========================================================================
    // REGION B: UNDERWATER CROSS-SECTION (Water Column, Sunbeams, Sloped Cobble Bed)
    // =========================================================================
    // Normalized underwater vertical coordinate: 0.0 right at the meniscus -> 1.0 at bottom of screen
    float uDepth = clamp((waterlineY - y) / (waterlineY + 1.0), 0.0, 1.0);

    // Rich tropical / alpine spring water gradient (bright emerald-cyan near surface -> deep marine teal)
    vec3 shallowWater = vec3(0.08, 0.54, 0.62);
    vec3 midWater     = vec3(0.04, 0.34, 0.45);
    vec3 deepWater    = vec3(0.02, 0.14, 0.22);

    vec3 waterCol = mix(shallowWater, midWater, smoothstep(0.0, 0.55, uDepth));
    waterCol = mix(waterCol, deepWater, smoothstep(0.45, 1.0, uDepth));

    // Bright sub-surface Snell's window glow right underneath the waterline
    float snellGlow = exp(-uDepth * 5.5) * (0.45 + 0.25 * sin(x * 5.0 + uTime * 1.4));
    waterCol += vec3(0.35, 0.78, 0.82) * snellGlow;

    // Diagonal Volumetric God Rays penetrating from the sunlit surface ripples downward-right
    float rayCoord = (x + uDepth * 0.48) * 2.2;
    float ray1 = texture(uNoiseTex, vec2(rayCoord * 0.45 + uTime * 0.015, uDepth * 0.15)).r;
    float ray2 = texture(uNoiseTex, vec2(rayCoord * 0.85 - uTime * 0.020, uDepth * 0.22)).a;
    float shafts = pow(ray1 * ray2, 1.55) * 2.6 * exp(-uDepth * 1.35);
    waterCol += vec3(0.55, 0.88, 0.78) * shafts * (0.35 + 0.45 * uWaterClarity);

    // Sloped Underwater Riverbed & Mossy Boulders in the lower 28% (uDepth > 0.68)
    float bedHorizon = 0.68 - 0.05 * cos(x * 2.4) + 0.03 * sin(x * 5.1);
    if (uDepth > bedHorizon) {
        float bedT = clamp((uDepth - bedHorizon) / (1.0 - bedHorizon), 0.0, 1.0);
        // Perspective projection onto the sloped sandy/pebble floor
        float floorZ = 1.0 / (0.25 + bedT * 1.25);
        vec2 floorUv = vec2(x * floorZ * 2.6, floorZ * 3.2);

        vec2 pebbleNorm;
        vec4 cobble = evaluateRiverCobbles(floorUv, pebbleNorm);

        vec3 sandCol   = vec3(0.42, 0.38, 0.28);
        vec3 stoneSlate = vec3(0.24, 0.30, 0.34);
        vec3 stoneWarm  = vec3(0.48, 0.38, 0.28);
        vec3 mossGreen  = vec3(0.12, 0.34, 0.20);

        vec3 stoneCol = mix(stoneSlate, stoneWarm, cobble.z);
        // Velvety moss on upper stone faces
        float mossMask = smoothstep(0.25, 0.75, cobble.w) * smoothstep(0.0, 0.5, pebbleNorm.y + 0.25);
        stoneCol = mix(stoneCol, mossGreen, mossMask * 0.65);

        vec3 bedAlbedo = mix(sandCol * 0.65, stoneCol, cobble.y);

        // Animated underwater caustics projected onto the sloped river stones
        vec2 cUv1 = floorUv * 0.45 + vec2(uTime * 0.04, -uTime * 0.03);
        vec2 cUv2 = floorUv * 0.58 + vec2(-uTime * 0.03, uTime * 0.045);
        float c1 = 1.0 - abs(texture(uNoiseTex, cUv1).r * 2.0 - 1.0);
        float c2 = 1.0 - abs(texture(uNoiseTex, cUv2).a * 2.0 - 1.0);
        float caustics = pow(c1 * c2, 2.8) * 3.2;

        vec3 N = normalize(vec3(pebbleNorm.x, pebbleNorm.y + 0.35, 1.0));
        float diff = max(dot(N, uSunDir), 0.25);
        vec3 litBed = bedAlbedo * (vec3(0.18, 0.42, 0.48) + vec3(0.95, 0.90, 0.72) * diff * 0.75)
                    + bedAlbedo * vec3(0.85, 0.98, 0.90) * caustics * 0.55;

        // Blend riverbed smoothly with water column depth fog
        float bedVisibility = smoothstep(0.0, 0.12, bedT) * mix(0.45, 0.88, bedT) * uWaterClarity;
        waterCol = mix(waterCol, litBed, bedVisibility);
    }

    // Usability calm zone in bottom 18% so home-screen dock icons stay effortless to read
    float dockCalm = smoothstep(0.20, 0.02, vUv.y);
    waterCol = mix(waterCol, deepWater * 0.85, dockCalm * 0.38);

    fragColor = vec4(waterCol, 1.0);
}
"""

    // =========================================================================================
    // 2. VALLISNERIA / SEAGRASS SHADER (Growing Upward from the Underwater Riverbed)
    // =========================================================================================
    const val VALLISNERIA_VERT = """#version 300 es
precision highp float;
layout(location = 0) in vec2 aLocalPos;
layout(location = 1) in vec2 aUv;

uniform float uTime;
uniform float uAspect;
uniform vec2 uParallax;
uniform vec4 uBladeData; // x=rootX, y=rootY, z=length, w=width
uniform vec3 uBladeMeta; // x=baseAngle, y=phase, z=hueShift
uniform float uShadowPass;
uniform vec3 uSunDir;

out vec2 vUv;
out float vHueShift;
out float vSwayDeriv;

void main() {
    float v = aUv.y; // 0 at root on riverbed, 1 at floating tip
    float phase = uBladeMeta.y;
    float baseAngle = uBladeMeta.x;

    // Gentle underwater surge current bending the seagrass horizontally
    float sway = sin(uTime * 1.25 + phase - v * 2.8) * 0.14 * pow(v, 1.35)
               + cos(uTime * 0.68 + phase * 1.5 - v * 1.8) * 0.06 * v;

    float angle = baseAngle + sway;
    vec2 spineDir = vec2(cos(angle), sin(angle));
    vec2 perpDir  = vec2(-spineDir.y, spineDir.x);

    vec2 pos = uBladeData.xy
             + spineDir * (v * uBladeData.z)
             + perpDir * (aLocalPos.x * uBladeData.w);

    pos.x += uParallax.x * 0.95;
    pos.y -= uParallax.y * 0.25;

    vUv = aUv;
    vHueShift = uBladeMeta.z;
    vSwayDeriv = sway;

    gl_Position = vec4(pos.x, pos.y, 0.0, 1.0);
}
"""

    const val VALLISNERIA_FRAG = """#version 300 es
precision highp float;

in vec2 vUv;
in float vHueShift;
in float vSwayDeriv;

uniform float uShadowPass;

out vec4 fragColor;

void main() {
    if (uShadowPass > 0.5) {
        fragColor = vec4(0.0, 0.0, 0.0, 0.0);
        return;
    }
    float rib = 1.0 - abs(vUv.x * 2.0 - 1.0);
    vec3 rootCol = vec3(0.04, 0.18, 0.16);
    vec3 tipCol  = mix(vec3(0.14, 0.48, 0.28), vec3(0.28, 0.56, 0.22), vHueShift);
    vec3 col = mix(rootCol, tipCol, pow(vUv.y, 0.8));
    col += vec3(0.22, 0.36, 0.18) * max(0.0, vSwayDeriv * 3.0) * vUv.y;

    float alpha = smoothstep(0.0, 0.22, rib) * (1.0 - 0.18 * pow(vUv.y, 3.0));
    fragColor = vec4(col, alpha * 0.85);
}
"""

    // =========================================================================================
    // 3. MULTI-SPECIES 3D SIDE-VIEW FISH SHADER (8 Species, Proper Phone Scale, Underwater Caustics)
    // =========================================================================================
    const val KOI_VERT = """#version 300 es
precision highp float;

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec3 aUvPart; // x=vHeight [0=belly..1=back], y=spineT [0..1.25], z=partId

uniform float uTime;
uniform float uAspect;
uniform vec2 uParallax;
uniform vec3 uSunDir;

uniform vec3 uFishWorldPos;   // x, y (in NDC), zDepth [0.12..0.85]
uniform vec2 uFishScale;      // x=bodyLength, y=bodyHeight
uniform float uFishHeading;   // 3D yaw in XZ plane: 0 = facing right (+X), PI = facing left (-X)
uniform float uSwimPhase;     // tail undulation phase
uniform float uCurvature;     // pitch + turn bend
uniform float uShadowPass;    // 1.0 = finScale (re-used uniform slot for zero-overhead fin extension)

out vec3 vNormal;
out vec3 vUvPart;
out vec2 vScreenPos;
out float vDepth;

void main() {
    float vHeight = aUvPart.x;
    float spineT  = aUvPart.y;
    float partId  = aUvPart.z;
    float finMult = max(uShadowPass, 0.6);

    vec3 localPos = aPos;
    vec3 localNorm = aNormal;

    // Amplify dorsal/ventral/caudal fins for veil-tailed species (Moorish Idol, Betta)
    if (partId > 1.5) {
        localPos.y *= mix(1.0, finMult, clamp((spineT - 0.25) * 1.2, 0.0, 1.0));
        if (partId > 2.5) {
            localPos.x *= mix(1.0, finMult * 0.88, clamp((spineT - 0.88) * 2.5, 0.0, 1.0));
        }
    }

    // 3D Side-View Spine Undulation:
    // Fish bends laterally along Z (toward/away from camera) and slightly along X/Y as it swims
    float waveEnv = 0.02 + 0.14 * pow(max(spineT, 0.0), 1.75);
    float waveArg = uSwimPhase - spineT * 4.5;
    float lateralZ = sin(waveArg) * waveEnv;
    float dWaveZ   = -4.5 * cos(waveArg) * waveEnv;

    // Pectoral fin sculling
    if (partId > 0.5 && partId < 1.5) {
        float flap = sin(uTime * 4.5 + uSwimPhase) * 0.05 * vHeight;
        localPos.z += sign(localPos.z) * abs(flap);
        localPos.y += flap * 0.5;
    }

    localPos.z += lateralZ;

    // Rotate vertex normal in XZ plane by spine wave derivative
    float bendAng = atan(dWaveZ * 0.45);
    float cb = cos(bendAng);
    float sb = sin(bendAng);
    localNorm.xz = vec2(
        localNorm.x * cb - localNorm.z * sb,
        localNorm.x * sb + localNorm.z * cb
    );

    // Rotate in 3D XZ plane by uFishHeading (0 = facing right +X, PI = facing left -X)
    // Add slight oblique 3D yaw so we always see both the side profile and a hint of the 3D back/tail
    float yaw = uFishHeading + 0.22 * sin(uSwimPhase * 0.5);
    float cy = cos(yaw);
    float sy = sin(yaw);

    vec3 rotPos = vec3(
        localPos.x * cy - localPos.z * sy,
        localPos.y,
        localPos.x * sy + localPos.z * cy
    );

    vec3 rotNorm = normalize(vec3(
        localNorm.x * cy - localNorm.z * sy,
        localNorm.y,
        localNorm.x * sy + localNorm.z * cy
    ));

    // Apply subtle vertical pitch from uCurvature
    float pitch = uCurvature * 0.55;
    float cp = cos(pitch);
    float sp = sin(pitch);
    vec2 pitchedXY = vec2(
        rotPos.x * cp - rotPos.y * sp,
        rotPos.x * sp + rotPos.y * cp
    );

    // Scale to phone screen proportions (correcting vertical aspect ratio so fish are never stretched)
    vec2 screenOffset = vec2(
        pitchedXY.x * uFishScale.x,
        (pitchedXY.y * uFishScale.y) / uAspect
    );

    float parallaxDepth = mix(1.15, 0.45, uFishWorldPos.z);
    vec2 finalPos = uFishWorldPos.xy + screenOffset + vec2(uParallax.x * parallaxDepth, -uParallax.y * 0.25);

    vNormal = rotNorm;
    vUvPart = aUvPart;
    vScreenPos = finalPos;
    vDepth = uFishWorldPos.z;

    gl_Position = vec4(finalPos.x, finalPos.y, 0.0, 1.0);
}
"""

    const val KOI_FRAG = """#version 300 es
precision highp float;

in vec3 vNormal;
in vec3 vUvPart;
in vec2 vScreenPos;
in float vDepth;

uniform float uTime;
uniform vec3 uSunDir;
uniform float uWaterClarity;
uniform int uPatternVariant; // 0..7 species ID
uniform float uPatternSeed;
uniform sampler2D uNoiseTex;

out vec4 fragColor;

void main() {
    float vHeight = vUvPart.x; // 0.0 = ventral belly, 0.5 = mid-flank, 1.0 = dorsal ridge
    float spineT  = vUvPart.y; // 0.0 = snout, 1.0 = tail base, 1.25 = caudal tip
    float partId  = vUvPart.z; // 0=body, 1=pectoral, 2=dorsal/ventral, 3=caudal

    vec3 N = normalize(vNormal);
    // Ensure two-sided lighting when fish faces left or right
    N.z = abs(N.z) * 0.75 + 0.25;
    N = normalize(N);

    vec4 patNoise = texture(uNoiseTex, vec2(spineT * 1.4 + uPatternSeed * 0.13, vHeight * 1.4));

    vec3 albedo = vec3(0.95);
    float metallic = 0.20;
    float finOpacity = 0.82;

    if (uPatternVariant == 0) {
        // 0: CLOWNFISH (Bright tangerine-orange with 3 snow-white black-bordered vertical bars)
        vec3 orange = vec3(0.98, 0.36, 0.04);
        vec3 white  = vec3(0.98, 0.98, 0.99);
        vec3 black  = vec3(0.05, 0.05, 0.07);

        float wobble = (patNoise.r - 0.5) * 0.035;
        float d1 = abs(spineT - 0.22 + wobble);
        float d2 = abs(spineT - 0.54 + wobble);
        float d3 = abs(spineT - 0.84 + wobble);

        float whiteBar = (1.0 - smoothstep(0.045, 0.060, d1))
                       + (1.0 - smoothstep(0.055, 0.070, d2))
                       + (1.0 - smoothstep(0.035, 0.050, d3));
        float blackRim = (1.0 - smoothstep(0.060, 0.082, d1))
                       + (1.0 - smoothstep(0.070, 0.092, d2))
                       + (1.0 - smoothstep(0.050, 0.070, d3));

        albedo = mix(orange, black, clamp(blackRim, 0.0, 1.0));
        albedo = mix(albedo, white, clamp(whiteBar, 0.0, 1.0));
        if (partId > 0.5) {
            float finTip = smoothstep(0.75, 0.95, max(spineT - 0.2, abs(vHeight - 0.5) * 2.0));
            albedo = mix(orange, black, finTip);
        }
    } else if (uPatternVariant == 1) {
        // 1: REGAL BLUE TANG (Cobalt-sapphire body, dark dorsal oval palette, bright canary-yellow tail)
        vec3 cobalt = vec3(0.06, 0.32, 0.88);
        vec3 yellow = vec3(0.98, 0.86, 0.10);
        vec3 dark   = vec3(0.04, 0.07, 0.14);

        float dorsalBand = smoothstep(0.18, 0.12, abs(vHeight - 0.72)) * step(0.18, spineT) * step(spineT, 0.85);
        albedo = mix(cobalt, dark, dorsalBand * 0.85);
        float yellowTail = smoothstep(0.82, 0.92, spineT);
        albedo = mix(albedo, yellow, yellowTail);
        metallic = 0.35;
    } else if (uPatternVariant == 2) {
        // 2: MOORISH IDOL / BANNERFISH (Bold vertical black, ivory-white, and golden-yellow bands)
        vec3 ivory  = vec3(0.96, 0.96, 0.92);
        vec3 gold   = vec3(0.96, 0.76, 0.12);
        vec3 onyx   = vec3(0.06, 0.07, 0.09);

        float band1 = 1.0 - smoothstep(0.08, 0.11, abs(spineT - 0.24));
        float band2 = 1.0 - smoothstep(0.09, 0.12, abs(spineT - 0.70));
        albedo = mix(ivory, gold, smoothstep(0.45, 0.95, vHeight) * 0.65);
        albedo = mix(albedo, onyx, clamp(band1 + band2, 0.0, 1.0));
        if (partId > 2.5) albedo = onyx;
    } else if (uPatternVariant == 3) {
        // 3: KOHAKU / SANKE KOI (Snow-white body with bold vermilion-red dorsal patches & sumi spots)
        vec3 white = vec3(0.97, 0.96, 0.94);
        vec3 red   = vec3(0.94, 0.24, 0.07);
        vec3 sumi  = vec3(0.08, 0.09, 0.11);

        float hiPatch = smoothstep(0.44, 0.50, patNoise.r + 0.18 * (vHeight - 0.3));
        float sumiSpot = smoothstep(0.72, 0.76, patNoise.a) * step(0.2, spineT);
        albedo = mix(white, red, hiPatch);
        albedo = mix(albedo, sumi, sumiSpot);
    } else if (uPatternVariant == 4) {
        // 4: NEON TETRA (Iridescent electric-cyan lateral stripe + glowing crimson posterior belly)
        vec3 oliveBack = vec3(0.22, 0.38, 0.42);
        vec3 neonCyan  = vec3(0.05, 0.92, 1.00);
        vec3 crimson   = vec3(0.96, 0.12, 0.18);

        float neonStripe = exp(-pow((vHeight - 0.56) * 9.5, 2.0)) * step(spineT, 0.82);
        float redBelly   = smoothstep(0.42, 0.75, spineT) * smoothstep(0.55, 0.20, vHeight);
        albedo = mix(oliveBack, crimson, redBelly);
        albedo = mix(albedo, neonCyan, neonStripe);
        metallic = 0.80;
    } else if (uPatternVariant == 5) {
        // 5: ROYAL BETTA / VEIL-TAIL GOLDFISH (Iridescent ruby-magenta & burnished gold)
        vec3 ruby    = vec3(0.86, 0.12, 0.34);
        vec3 turquoise = vec3(0.10, 0.75, 0.82);
        vec3 gold    = vec3(0.98, 0.68, 0.16);

        albedo = mix(ruby, gold, smoothstep(0.0, 0.65, 1.0 - spineT));
        if (partId > 0.5) {
            float rays = 0.5 + 0.5 * sin(vHeight * 36.0 + spineT * 18.0);
            albedo = mix(ruby, turquoise, rays * 0.65);
            finOpacity = 0.74;
        }
        metallic = 0.55;
    } else if (uPatternVariant == 6) {
        // 6: MANDARIN DRAGONET (Vibrant tangerine & electric teal labyrinth swirls)
        vec3 teal      = vec3(0.06, 0.68, 0.72);
        vec3 tangerine = vec3(0.96, 0.42, 0.08);
        vec3 cobalt    = vec3(0.08, 0.22, 0.78);

        float swirl = sin(spineT * 22.0 + patNoise.r * 6.0) * 0.5 + 0.5;
        albedo = mix(tangerine, teal, smoothstep(0.35, 0.65, swirl));
        albedo = mix(albedo, cobalt, smoothstep(0.75, 0.95, patNoise.g));
    } else {
        // 7: SILVER RASBORA / MINNOW (Translucent champagne-silver with dark tail spot)
        vec3 silver = vec3(0.82, 0.90, 0.94);
        vec3 goldStripe = vec3(0.95, 0.88, 0.58);
        float stripe = exp(-pow((vHeight - 0.52) * 10.0, 2.0));
        albedo = mix(silver * 0.78, goldStripe, stripe);
        metallic = 0.85;
    }

    // Eye rendering on lateral head (spineT ~ 0.11, vHeight ~ 0.58)
    float alpha = 1.0;
    if (partId < 0.5) {
        float eyeDist = length(vec2((spineT - 0.11) * 6.5, (vHeight - 0.58) * 3.8));
        if (eyeDist < 0.18) {
            vec3 eyeCol = (eyeDist < 0.10) ? vec3(0.02, 0.02, 0.03) : vec3(0.92, 0.82, 0.45);
            albedo = mix(albedo, eyeCol, smoothstep(0.18, 0.13, eyeDist));
        } else if (spineT > 0.16 && spineT < 0.88) {
            // Subtle diamond scale shimmer on fish trunk
            vec2 sUv = vec2(spineT * 28.0, vHeight * 14.0);
            sUv.y += 0.5 * mod(floor(sUv.x), 2.0);
            vec2 sf = fract(sUv) - 0.5;
            float rim = smoothstep(0.35, 0.48, length(sf));
            albedo *= (1.0 - 0.08 * rim);
        }
        // Natural counter-shading (slightly brighter silver-white belly)
        albedo = mix(albedo * 1.12, albedo, smoothstep(0.05, 0.45, vHeight));
    } else {
        // Delicate translucent fin rays
        float rayPattern = 0.76 + 0.24 * sin((vHeight + spineT) * 32.0);
        albedo *= rayPattern;
        float edgeFade = (partId > 2.5)
            ? clamp((spineT - 0.90) / 0.35, 0.0, 1.0)
            : abs(vHeight - 0.5) * 1.6;
        alpha = finOpacity * (1.0 - 0.55 * pow(clamp(edgeFade, 0.0, 1.0), 1.4));
    }

    // Underwater directional sunlight from above + caustic shimmer on dorsal ridge
    vec3 lightDir = normalize(vec3(-0.35, 0.82, 0.45));
    float NdotL = max(dot(N, lightDir), 0.0);
    vec3 viewDir = vec3(0.0, 0.0, 1.0);
    vec3 halfVec = normalize(lightDir + viewDir);
    float spec = pow(max(dot(N, halfVec), 0.0), mix(20.0, 52.0, metallic)) * mix(0.25, 0.70, metallic);

    // Animated caustic bands rippling across the upper half of the fish
    float causticWave = pow(0.5 + 0.5 * sin(vScreenPos.x * 18.0 + uTime * 2.8 - vScreenPos.y * 8.0), 3.0);
    float dorsalCaustic = causticWave * smoothstep(0.45, 0.95, vHeight) * (1.0 - vDepth * 0.6);

    vec3 sunCol     = vec3(1.05, 0.96, 0.80);
    vec3 waterAmb   = vec3(0.16, 0.44, 0.52);

    vec3 color = albedo * (waterAmb + sunCol * NdotL * 0.82)
               + albedo * vec3(0.65, 0.98, 0.92) * dorsalCaustic * 0.42
               + sunCol * spec;

    // Beer-Lambert underwater depth perspective (fish further back blend softly into the turquoise water)
    float fogStrength = mix(1.35, 0.55, uWaterClarity);
    vec3 depthFogCol  = vec3(0.04, 0.32, 0.42);
    color = mix(depthFogCol, color, exp(-vDepth * fogStrength));

    fragColor = vec4(color, alpha);
}
"""

    // =========================================================================================
    // 4. SPLIT-VIEW WATER COMPOSITE SHADER (Meniscus Wave Line, Bubbles, Refraction, Tone Map)
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

uniform vec4 uRipples[10];
uniform vec4 uLilyPadsA[7];
uniform vec4 uLilyPadsB[7];
uniform int uParticleCount;
uniform vec4 uParticles[48];

out vec4 fragColor;

float getCompositeWaterlineY(float x, float time) {
    float baseLine = 0.22 - uParallax.y * 0.25;
    float w1 = sin(x * 3.8 + time * 1.35) * 0.016;
    float w2 = cos(x * 7.2 - time * 1.95) * 0.008;
    float w3 = sin(x * 13.0 + time * 2.60) * 0.003;

    // Add interactive touch wave displacement along the meniscus
    float touchDisp = 0.0;
    for (int i = 0; i < 8; i++) {
        vec4 rp = uRipples[i];
        if (rp.z < 3.2 && rp.w > 0.001) {
            float dx = abs(x - rp.x);
            float wave = cos(dx * 28.0 - rp.z * 12.0) * exp(-dx * 4.5) * exp(-rp.z * 1.2);
            touchDisp += wave * 0.022 * rp.w;
        }
    }
    return baseLine + w1 + w2 + w3 + touchDisp;
}

void main() {
    float x = vNdc.x + uParallax.x * 0.85;
    float y = vNdc.y;
    float waterlineY = getCompositeWaterlineY(x, uTime);

    vec2 sampleUv = vUv;

    // Apply gentle underwater current & touch-ripple refraction ONLY below the waterline
    if (y < waterlineY) {
        vec4 nSample = texture(uNoiseTex, vec2(x * 1.2 + uTime * 0.025, y * 1.2 - uTime * 0.02));
        vec2 distort = (nSample.rg - 0.5) * 0.006;

        for (int i = 0; i < 8; i++) {
            vec4 rp = uRipples[i];
            if (rp.z < 3.2 && rp.w > 0.001) {
                vec2 delta = vec2(vNdc.x - rp.x, (vNdc.y - rp.y) * uAspect);
                float dist = length(delta);
                float ring = dist - rp.z * 0.45;
                float env = exp(-ring * ring * 65.0) * exp(-rp.z * 1.1) * rp.w;
                distort += (delta / max(dist, 0.001)) * cos(ring * 45.0) * env * 0.015;
            }
        }
        sampleUv = clamp(vUv + distort, 0.002, 0.998);
    }

    vec3 sceneCol = texture(uUnderwaterTex, sampleUv).rgb;

    // Render underwater rising air bubbles & sunlit plankton motes below the waterline
    if (y < waterlineY) {
        for (int i = 0; i < 48; i++) {
            if (i >= uParticleCount) break;
            vec4 pt = uParticles[i];
            vec2 delta = vec2(vNdc.x - pt.x, (vNdc.y - pt.y) * uAspect * 0.55);
            float d = length(delta);
            float pSize = abs(pt.w);
            if (d < pSize) {
                float rNorm = d / pSize;
                if (pt.w < 0.0) {
                    // Crisp refractive air bubble with specular highlight
                    float rim = smoothstep(0.35, 0.88, rNorm) * (1.0 - rNorm);
                    float glint = smoothstep(0.35, 0.05, length(delta - vec2(-0.28, 0.28) * pSize));
                    sceneCol += vec3(0.75, 0.96, 1.0) * (rim * 1.4 + glint * 0.85);
                } else {
                    // Sunlit golden-cyan plankton mote
                    float glow = (1.0 - rNorm) * (1.0 - rNorm);
                    sceneCol += vec3(0.72, 0.95, 0.85) * glow * 0.35;
                }
            }
        }
    }

    // Crisp Refractive Meniscus Ribbon right at the Half-Over / Half-Underwater Interface
    float meniscusDist = abs(y - waterlineY);
    if (meniscusDist < 0.032) {
        // Thick glass-like total-internal-reflection band where the water meets the camera lens
        float coreRibbon = exp(-pow(meniscusDist / 0.0065, 2.0));
        float softBand   = exp(-pow(meniscusDist / 0.022, 2.0));
        float shimmer    = 0.75 + 0.25 * sin(x * 24.0 + uTime * 3.2);

        vec3 meniscusTint = vec3(0.45, 0.88, 0.94);
        vec3 meniscusHighlight = vec3(0.96, 0.99, 1.00);

        sceneCol = mix(sceneCol, meniscusTint, softBand * 0.48);
        sceneCol += meniscusHighlight * coreRibbon * shimmer * 0.78;
    }

    // Filmic ACES tone mapping for rich natural contrast
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

# Sunlit Koi Pond 4K — Android Live Wallpaper (OpenGL ES 3.0)

A production-quality, real-time 4K procedural Android Live Wallpaper written in Kotlin and OpenGL ES 3.0 (`minSdk 26`, `targetSdk 35`).

## Rendering Pipeline & Performance Budget

1. **Two-Pass Dynamic Resolution Pipeline**:
   - **Pass 1 (Offscreen Underwater FBO, 0.60x–1.00x adaptive scale)**: Renders the procedural gravel and river-stone bed, mossy cobbles, dappled overhanging canopy shadows, dual-layer Voronoi caustics, swaying vallisneria ribbons, soft projected shadows, and depth-sorted 3D articulated koi and silver minnows.
   - **Pass 2 (Native Screen Resolution up to 2160x3840)**: Samples Pass 1 with Snell's law water normal refraction and subtle chromatic dispersion, composites sunlit sediment motes and rising bubbles, renders floating lily pads with 3-ring blooming pink/cream lotus flowers, applies Schlick Fresnel sky reflection, sun surface sparkle, and volumetric god rays.
2. **Zero Per-Frame Heap Allocations**:
   - All fish steering agents, ripple ring buffers, particle motes, insertion-sort depth indices, and GL uniform upload buffers (`FloatArray`) are pre-allocated at initialization.

## Build & Installation Steps

1. Open **Android Studio Ladybug (2024.2.1)** or newer.
2. Select **File → Open** and choose the `KoiPondWallpaper` root folder.
3. Let Gradle sync with **JDK 17** (configured automatically in `app/build.gradle.kts`).
4. Build and install onto a physical Android device (API 26–35):
   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
5. Launch **Koi Pond Settings** from the app drawer, adjust your preferences, and tap **Activate Live Wallpaper**.

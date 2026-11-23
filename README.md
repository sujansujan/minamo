# minamo — Split-View ASCII Ocean Window & Zen Pond Live Wallpaper

`split-view ascii ocean window that shifts with the sun. nothing else.`

## Features
- **Plain-Text Monospace Everything**: Both the live wallpaper and the 3-tab app UI (`[1 pond]`, `[2 set]`, `[3 about]`) are built strictly from JetBrains Mono characters with ligatures disabled (`liga 0, calt 0`), square corners, 1dp hairline borders, and zero Material icons.
- **Solar Light & Dark Mode Following the Sun**: Automatically tracks seasonal sunrise/sunset across a 6-keyframe CIE LAB perceptual curve with a WCAG contrast guard — crisp sunlit Lagoon Light paper by day, warm horizon gradients at dawn/dusk, and starry dark/AMOLED indigo by night, reporting native `WallpaperColors` so Android's clock & launcher text adapt automatically. Includes a live 24-hour solar scrubber in the preview tab.
- **Ocean Window & Zen Pond Biomes**:
  - **Ocean Window**: Open-ocean horizon with sun/moon water glitter paths, soaring seabirds, distant sailboat & blinking night lighthouse, foreground coral reef & swaying anemones, bubbling giant clam, and unified ocean swell currents.
  - **Zen Pond**: Floating lotus lily pads with dangling roots, shoreline mossy rock frog/turtle, and drifting origami paper boat.
- **Natural 3D Swim Physics & Rich Ocean Variety**:
  - Continuous 3D `yaw` heading with smooth horizontal foreshortening (`scaleX`) and gentle pitch banking, open-window edge crossings rotating through **15 distinct species** (green sea turtle, spotted eagle ray, moorish idol, yellowfin tuna, emperor angelfish, reef lionfish, spotted puffer, golden dorado, clownfish, regal tang, mandarin dragonet, kohaku koi, and 3D boid schools of blue chromis, silver sardines & sunset anthias).
  - **Periodic Ocean Giants**: Humpback whales (with surface water spouts), orcas, breaching bottlenose dolphin pods leaping above the 1D spring-wave meniscus, whale sharks, giant oceanic mantas, and nocturnal giant squid — with schooling baitfish parting naturally around passing giants.
- **Interactive & Battery-Efficient**:
  - Tap to drop food or gently startle fish, hold for bottom-feeder algae wafers, and **drag across the water** to send ripples and leave glowing **night bioluminescent plankton wakes**.
  - VSYNC `Choreographer` rendering with adaptive FPS (20fps calm / 30–60fps active / 15fps eco & thermal throttle), cached single-call sky & water `LinearGradient` shaders, 15s palette snapshot caching, zero per-frame allocations, and low-power `TYPE_GAME_ROTATION_VECTOR` hardware-batched parallax.

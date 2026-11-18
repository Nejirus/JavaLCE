# Legacy Console 2.3.3 — playable Overworld port

Target: Minecraft 1.17.1, Forge 37.1.1 and Java 17. Optional shaders:
OptiFine 1.17.1 HD U H1 with LCE Look 2.0.0.

## What changes in this build

- Legacy Overworlds connect the recovered PS4 terrain, biome surfaces and four
  carver passes to the translated population dispatcher and feature kernels.
- Ores, deposits, lakes, dungeons, the eleven tree types, biome plant selectors,
  vegetation, aquatic/coral features, fossils, ice and snow now run in the live
  generator. The PS4 dispatcher supplies one shared population RNG.
- Held maps use the original 128×128 PS4 image and coordinate transform.
  Classic's scale-3 coverage is 108×108 pixels, leaving ten unused pixels per side.
  The earlier custom shrink/inset is removed.
- Finite bounds retain the verified origin-centered PS4 positions for each size,
  independent of seed. Legacy-only map-grid alignment is preserved.
- Deepslate, copper, geodes and bee nests use separate target-game streams after
  the PS4 pass. Nether and End retain Minecraft 1.17.1 generation.

## Install and use

Put `legacy-console-1.17.1-2.3.3.jar` in `mods`. Replace the older version.
Update optional `legacy-join-compat-1.17.1-2.3.3.jar` on the server at the same
time. Keep LCE Look as a ZIP in `shaderpacks`; it is not a mod JAR. OptiFine is
obtained separately and is not redistributed.

Create a new world with **World Type: Legacy**. Choose Classic, Small, Medium
or Large and the desired biome scale. Explicit seeds remain explicit, including
zero. Existing saves load, but old generated chunks keep their previous blocks;
newly generated terrain can meet them at a visible seam.

## Accuracy limits

This is the playable milestone requested before full parity, not a completed
PS4 seed-parity implementation. The translated algorithms have bounded original
executable comparisons. The live world adapter uses Java states, lighting,
collision/support queries, physics, waterlogging, loot and chunk scheduling.
These can change placements and subsequent random consumption. Structure
geometry, clipping, loot and shared-RNG effects are not fully verified.
Some original process-wide feature-cache lifetimes are not reproduced.
Modern additions intentionally change generated blocks. Final all-seed PS4
spawn equality is not proved. New 1.17 structure work is deferred.

The full canonical-state and physics research remains in the source for future
accuracy builds; it is not presented as part of the live world bridge.
See `PS4-PORT-233.md` for source addresses and fixture reproduction, and
`../TESTING.md` for actual validation results and runtime limitations.

# Extend Console UI Size to LCE containers — 2.3.13

The existing Console UI Size setting now applies to inventory, creative,
furnace and storage containers, including single/double chests. Smithing and
grindstone join the same control. The previous PS4 menus and tutorial panels
continue to use their existing transforms.

## Source stops and retained layouts

MinecraftLCE-7thGen revision `acff48c2c7e18406fd6000b56945746ca9a6eb69`,
`Minecraft.Client/Common/UI/UIScene_SettingsUIMenu.cpp`, initializes its
UI-size slider with minimum 1 and maximum 3. The original LCE HUD frame ratios
are the same 2/3, 1 and 4/3 already recovered from mcps4 and used by the mod's
Console UI Size control. `LCE-WIDGETS-ACTIONS.json`, `LCE-DISPLAY-CONTROLLERS.json`
and `PS4-UI-SCALE-2312.json` retain the source references and asset evidence.

This release connects additional Java menus to that existing control; it does
not claim that the original console setting was a separate global menu zoom.
It preserves the currently ported LCE menu layouts and textures. Smithing and
grindstone retain their existing Java/Legacy4J layout adapters, with their
21-unit player inventory pitch matching the LCE inventory/furnace/chest grid.
No replacement artwork or scale stops are introduced.

## Rendering and input

`ConsoleContainerScale` uniformly scales the authored menu rectangle, including
creative's tab strip. It uses the shared scale ratios and the original 5% safe
zone. Enlargement fits within the viewport above the actual footer glyph
bounds. Normal is the prior size and position whenever that rectangle fits.

The transform wraps menu content in the existing model-view stack, so labels,
items, highlights, furnace bars, the inventory player model and smithing preview
stay together. The footer/background remain in screen space. Native hover
text uses the actual pointer position and global GUI scale, matching the PS4
menus' current behavior. The previous PS4 rendering route remains an identity
transform in this adapter, avoiding a second application of the size setting.

Mouse press/release/drag coordinates are converted back into menu space.
Controller navigation, cursor snapping, resize repositioning and quick move
use the transformed on-screen holder rectangles. Slot indexes, crafting and
server transactions retain their existing native implementations.

The inventory, creative and storage classes opt in directly. In the legacy
workstation adapter, only smithing and grindstone opt in; the PS4 replacements
for other workstations continue through their existing source-space renderer.
The HUD remains controlled by HUD Size. The existing slider and assets are
reused, and network protocol 6 / tutorial save version 2 remain unchanged.

## Validation

The full Java 17 / Forge 37.1.1 suite passes 397 tests. New regressions check
shared ratios, unchanged Normal geometry, inverse mouse mapping, transformed
holder bounds, creative tab bounds and double-chest fitting across the supported
framebuffer profiles and smaller GUI windows.

The production-client fixture uses Forge 37.1.1, the supplied OptiFine H1 and
LCE Look 2.0.0. It records seven menus at all three stops, at 720p with shaders
off and 1440p with LCE Look. Checks use actual scaled mouse pickup/replacement,
native creative item selection and tab clicks, and controller navigation targets.
Results and capture hashes are in `NATIVE-2313-CONTAINER-RESULTS.json`.

These captures use a private flat world and synthetic controller state.
Physical controllers and Apple Retina hardware were not tested; the supported
geometry profiles are checked automatically.

# PS4 tutorial, inventory and map interactions — 2.3.9

Reference: [Nejirus/mcps4](https://github.com/Nejirus/mcps4), revision
`37e258085773f11599cf0ffe29119926769554b7`, original `eboot.bin` SHA-256
`c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8e3`.
The original executable is an analysis input and is not redistributed.

## Crafting tutorial input

The three original InfoTasks at registration return PCs `0x99a3d8`,
`0x99a6ee` and `0x99a9d4` require menu action **2 / Square**. The former
Java adapter accepted only actions 0 and 1 through its yes/no handler.

PS4 `InfoTask::handleUIInput`, `0x979160`, checks activation at object `+0x19`,
walks the completedMappings list at `+0x50`, and marks matching action IDs.
`isCompleted`, `0x978ed0`, separately checks the minimum display time before
accepting those recorded inputs. `Ps4InfoInput` translates this mapping state;
the existing 40-tick timing adapter remains in use. The crafting screen
reports Square before cycling its panel, preserving the original menu order.
The keyboard X equivalent reports the same action and is named correctly
in crafting tutorial text.

## Inline glyphs

The PS4 input-formatting functions `0xaf2ce0` and `0xaf3360` produce
middle-aligned HTML images. The single-player paths load **45** at
`0xaf2e90` and `0xaf34a2`, then pass that value for both width and height to
the image formatter. The 30-pixel branches serve multiple local viewports;
this mod has one local viewport. TutorialPopup1080's original panel and text
anchors remain in use.

The adapter previously normalized artwork to 27 source pixels. It now uses
45, preserves the existing layered controller artwork and its aspect ratio,
and expands affected line boxes to fit it. Wrapping and drawing share glyph
advances. This also replaces an unsupported `space` font provider from 2.3.8:
Minecraft 1.17.1's StringSplitter now measures these inline spans explicitly.
No replacement button artwork or Unicode button outlines are introduced.

## Inventory pointer actions

The container pointer logic distinguishes an empty slot, a one-item slot,
a larger stack, and a carried stack. The Java footer now makes the same
distinctions: Take/Quick Move/What's This require a hovered item; Split
requires a stack of at least two and an empty pointer. Placement and swap
prompts respect slot acceptance and stack capacity. What's This still uses
the existing PS4 R2 popup and original English label `0x2fccdffb`.

PS4's native pointer routine is `0x9a2950`; its tooltip label table at
`0xee4b60` is used by `0x9a2700`. The no-carried-item branch selects generic
take for count 1, or take-all plus half-stack for larger stacks; the latter
assigns enum 4 at `0x9a48c8`. The table maps enum 4 to `0x23a35725` and
enum 16 to What's This (`0x2fccdffb`). The readable C++ cross-reference is
`IUIScene_AbstractContainerMenu::onMouseTick` in
MinecraftLCE-7thGen revision `acff48c2c7e18406fd6000b56945746ca9a6eb69`;
it helps identify the carried/hovered variables in the PS4 disassembly.

## Map orientation and edge tracking

PS4 map projection `0x44a610` subtracts map center Z from world Z and divides
by the map scale, without inverting it. Map north therefore decreases Z and
moves the cursor toward the top of the map. Orientation is unchanged.

The original routine compares projected coordinates with **-63 and +63**.
Minecraft 1.17.1 already matches this projection, including byte coordinates,
rotation rounding, and the nearby off-map cursor. The extra mod correction
used 64, changed the decoration after the standard update, and wrote directly
to the decoration map without the standard dirty notification. That competing
writer is removed, including its crafting-menu synchronization call.
Java's normal tracking and packet dirty notifications now remain authoritative.
Finite-world map alignment and Classic's existing PS4 inset are preserved.

## Campfire compatibility

This prompt adapts the actual **Minecraft 1.17.1 ShovelItem::useOn** condition:
the targeted block must be a lit CampfireBlock, a shovel must be held, and the
hit face must not be DOWN. Soul campfires use the same target-version block
class. It uses the existing Use label and L2 glyph. This is a requested 1.17.1
interaction adapter; it is not presented as a newly decompiled PS4 campfire
implementation.

## Reproduction

`docs/reference-tools/ps4/native_tutorial_map239.py` executes the original
bounded InfoTask handler and map projection with host dictionary/profiler
callbacks. Its checked-in fixture contains 30 input cases and 450 near-edge
map cases over every scale, both axes, negative coordinates and rotations.
The map regression invokes Minecraft 1.17.1's projection and compares each
result with that original-code fixture.

Build and tests use Java 17, Minecraft 1.17.1 and Forge 37.1.1. See
`NATIVE-239-INTERACTION-RESULTS.json` for final automated and client results.

The final suite passes **381 tests**. The production-client run with the
supplied OptiFine H1 and LCE Look 2.0.0 passes six interaction checks and
records **60 captures**. The imported tutorial exercises all three Square
steps, an early press retained through the minimum display time, and keyboard
X. Inventory captures cover empty/outside pointers, single/multiple items,
placement and swap. Actual Legacy Small map packets converge to the native
cursor types and projected coordinates on both sides of the 63-pixel limit.
The campfire check displays Use, performs the shovel interaction, verifies
extinguishing, and confirms that Use disappears.

Tutorial layouts cover 1280x720, 1920x1080, 2560x1440, 3840x2160, 1024x768,
1600x1000, 1512x982 and 3024x1964. Further glyph, inventory, map and campfire
captures use 720p and 1440p. Both shaders-off and LCE Look paths are covered.
These are software-controlled framebuffers on Linux/llvmpipe; physical
controller and Apple Retina hardware were not exercised.

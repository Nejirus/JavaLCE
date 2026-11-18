# Xbox 360 artwork, console UI size and tutorial persistence — 2.3.12

## Xbox 360 source correction

Reference: [MinecraftLCE-7thGen](https://github.com/chrisGrando/MinecraftLCE-7thGen),
revision `acff48c2c7e18406fd6000b56945746ca9a6eb69`.

The Xbox 360 prompt families previously read Windows skin SWF exports. Those
exports contain dark Xbox-style buttons, so selecting Xbox 360 still looked
like Xbox One. Both active Xbox 360 families now use the original PNGs from
`Minecraft.Client/Common/Media/Graphics/X360ControllerIcons/`.

`skin_Minecraft.xui` supplies normal A/B/X/Y backgrounds and letter layers,
the tooltip trigger/bumper images, and the navigation-stick display geometry.
Supplementary buttons use the original standalone PNGs and native dimensions.
Twenty-two PNGs are copied byte-for-byte after checking their repository Git
blob hashes. All eighteen glyph roles use these originals. No image is drawn,
recolored or resampled. Higher resolution geometry scales by 3/2 while retaining
the same PNGs. PS3, PS4 and Xbox One artwork retain their previous selection.

`LCE-XBOX360-2312.json` records sources, Git blob IDs, SHA-256 values and geometry.
`docs/reference-tools/lce/export_xbox360_glyphs.py` reproduces the export.

## Independent console menu size

PS4 reference: [Nejirus/mcps4](https://github.com/Nejirus/mcps4), revision
`37e258085773f11599cf0ffe29119926769554b7`, eboot SHA-256
`c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8e3`.

The recovered settings constructor `0xaba940` initializes its size slider with
minimum 1 at `0xabc438`, maximum 3 at `0xabc43d`, and calls `0x9f7980`.
`HUD1080.fui` has ScaleSmall/ScaleNormal/ScaleLarge frames whose HudSmall,
HudNormal and HudLarge transforms are 1, 1.5 and 2. The new option uses those
three numbered stops and their ratios to Normal: 2/3, 1 and 4/3.

The original PS4 option controls HUD frames. Applying the ratios to workstation
menus is the user's requested Java adapter, not a claim that PS4 had a separate
global menu-zoom slider. It leaves the existing HUD Size and global GUI Scale
settings independent. Normal preserves prior menu transforms. Large fits within
the viewport when necessary; tutorial panels retain their upper-right anchor
and fit their text height. Input rectangles, item faces and controller slots
are calculated from the same transformed scene geometry as rendering.

Six images are recovered from the actual `skinHDGraphics.fui` bitmap exports:
track, handle, focus outline, highlight and active/inactive tint. Only BGRA to
RGBA channel conversion is performed. `PS4-UI-SCALE-2312.json` records the source
instructions, frame transforms, symbols, bitmap indexes and image hashes;
`docs/reference-tools/ps4/export_ui_scale.py` reproduces the extraction.

The control affects PS4 crafting/special crafting, stonecutting, loom,
cartography, enchanting, brewing, beacon and trading scenes, plus tutorial and
manual description panels. The HUD and other console menu implementations
retain their existing controls.

## Tutorial menu history

The gameplay stage, flags, crafted items and disc hunt were saved, but the
client's menu-guide indexes and completed-context set were not. Every new
connection cleared them, causing inventory instructions to replay.

Completed contexts and partial task indexes now belong to each player's saved
tutorial progress. Client task advances report their index to the server;
finishing or skipping a guide records completion regardless of the current
gameplay gate. Save/load and the server-to-client state packet preserve both.
Indexes and contexts are validated against the original recovered task graph.
The existing original order, minimum display time, prompts and input handling
are retained; the current unfinished task restarts its display timer on login.

For pre-2.3.12 saves, completed StateChangeTasks prove which inventory/furnace
guides were finished. Those completions migrate without guessing. Other old
menu guides whose state was never saved may play once after upgrading. New
saves retain their completed and partial states. History is scoped per world
and player, so a fresh tutorial still teaches its guides.

The tutorial save format is version 2. Network protocol 6 carries this history;
update the server and modded clients together, plus the optional compatibility
add-on. Clients without the mod still use the existing compatibility route.

## Validation

The full Java 17 / Forge 37.1.1 suite passes 393 tests. Regressions verify source
artwork hashes/geometry, scale stops and eight framebuffer profiles, popup
bounds, HUD independence, menu-history NBT, old-save migration and packet
round-trip behavior. Runtime results with the supplied OptiFine H1 and LCE Look
2.0.0 are recorded in `NATIVE-2312-GUI-RESULTS.json`.

Production-client checks cover both controller families, the new slider, actual
craft/trade buttons and slot pickup/replacement across all eight menus at each stop, and tutorial panels.
They also quit/load an unfinished inventory guide, complete it, quit/load again,
and check that opening inventory does not repeat the completed guide.
The save/reload regression uses a private flat tutorial fixture at the original
inventory gate. Captures include 720p and a 1364×1024 desktop framebuffer
with shaders off, and all eight menus at 1440p with LCE Look. Other supported
framebuffer profiles are covered by geometry regressions. The controller state
is synthetic; physical controllers and Apple Retina hardware were not tested.

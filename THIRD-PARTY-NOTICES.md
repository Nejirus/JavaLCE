# Third-party notices

Version 2.3.8 translates the pinned mcps4 crafting display-mode cycle,
ingredient rows and item use-description bindings at Item+0x6c. Existing
controller glyph display lists and artwork are reused unchanged in tutorial
text; only inline spacing metrics and Java rendering adapters are added.
See `docs/PS4-CRAFTING-INFO-238.md` for native routines and verification.

Version 2.3.7 translates the pinned mcps4 FullTutorial/task graph and block
description bindings, and converts its original Orbis Tutorial.mcs and
Tutorial.pck into the included Java world template. TutorialPopup1080.fui,
the Play Tutorial icon, pointer-panel artwork and English text retain their
original Mojang/4J rights. The source executable and media archive are excluded.
Legacy4J descriptions for newer blocks come from revision
`f49505dab97480f687025f8a54208d2caa3ff86c`; its existing LGPL-3.0 license
is preserved in `licenses/Legacy4J-controls-LICENSE.txt`. See
`docs/PS4-TUTORIAL-237.md` for hashes, recovery methods and Java adapters.

Version 2.3.6 replaces the trading presentation with recovered mcps4
UIScene_TradingMenu code, TradingMenu1080.fui, and original HUD experience-bar
bitmaps from the pinned PS4 revision below. Exporters record source hashes;
`docs/PS4-TRADING-236.md` documents native addresses and Java menu adapters.
Crafting side-tab icon placement now reads both authored VertTab frames.
Version 2.3.5 restored the 2.3.3 general recipe families from the retained
Legacy4J ordering data; the 2.3.4 grouping note below describes that historical
release, rather than the current recipe grouping.

Version 2.3.4 replaces the supported workstation/crafting menus with Java
adaptations of recovered PS4 code and extracted Orbis FUI assets from the same
pinned mcps4 revision below. `docs/PS4-WORKSTATIONS.md` and the exported JSON
manifests document source addresses, hashes, conversion and target adapters.
The general crafting list no longer reads Legacy4J recipe-order data.


Version 2.3.1 keeps the imported PS4 crafting artwork and transforms unchanged,
changes the focused selector render order, and uses the 16 separate Minecraft
dye items instead of the older PS4 dye material substitutes.

Version 2.3.0 replaces the banner, firework and dye crafting interfaces with
Java adaptations of the PS4 crafting implementation and original FUI artwork
from Nejirus/mcps4 at revision `37e258085773f11599cf0ffe29119926769554b7`.
Original artwork and recovered behavior retain their upstream rights; the Java
platform adapters do not imply permission to redistribute upstream game files.
`docs/PS4-CRAFTING.md` and `MCPS4-CRAFTING.json` document provenance. The full
PS4 executable and media archive are excluded. Earlier Legacy4J notes below remain historical provenance for retained
interfaces and unmodded-server fallback screens.

Version 2.0.0 ports this project to Minecraft 1.17.1 / Forge 37.0.103.
Legacy4J revision `80ca31049dd5ea9f7169810f3cadd0aa38114a51` supplies
the creative/crafting ordering and classic loom, cartography, stonecutter,
grindstone and smithing layouts. Its original license remains in `licenses/`.
New Java integration uses native 1.17.1 menus, recipes and inventory actions.
Minecraft 1.17.1's panorama is loaded from the installed game's resources;
no Minecraft or OptiFine executable is included. Earlier version notes below
describe the inherited assets and adaptations, rather than additional tests
or claims of native 1.17.1 compatibility.

Version 1.9.6 reuses the original LCE keyboard face images and adds the two
unchanged LayoutButton_Outline SD/HD bitmaps. LCE-WORLD-CUSTOMIZE.json records
these and the LaunchMoreOptionsMenu scene/code evidence. The flat customizer
translates Legacy4J LegacyFlatWorldScreen, FlatWorldLayerSelector and native
preset integration to Minecraft 1.11.2. Existing upstream notices/licenses apply.
The LCE revision has no layer customizer; no unavailable C++ implementation is
claimed. Hardcore/permanent difficulty locks are the user-requested Java rules.

Version 1.9.5 adds six unchanged menu/list selection bitmaps from the original
SD/HD skinGraphics files. LCE-MENU-BUTTONS.json records pixel hashes, source
movie timelines, colors and the translated FJ_MenuButton/FJ_Base behavior.
The existing source Mojangles7 atlas is reused; upstream rights remain.

Version 1.9.4 records original white field colors and the complete presented-
frame input-gate path in LCE-INPUT-DISPLAY.json. Its display shader translates
the Orbis Renderer::UpdateGamma transfer already used by LCE Look, with new
Java/OpenGL glue. Source input/render libraries are hashed as evidence and are
not included. Existing artwork and its attribution remain unchanged.

Version 1.9.3 reuses the original LCE panel and ListButton_Norm images.
Java omits the console storage-quota footer and repeats the original top
inset below the panels. Original full and fitted geometry are recorded in
LCE-PLAY-LAYOUT.json. No new artwork is introduced or existing image changed.

Version 1.9.2 restores the original unnamed FJ_Panel9Grid layer from LCE’s
LoadOrJoinMenu720/1080 scenes, reusing the previously extracted SD/HD vector
panel images and their source scaling grids. No additional artwork is added.
The complete scene placement is recorded in LCE-PLAY-LAYOUT.json.

Version 1.9.1 adds six original LCE SD/HD Play Game list and arrow bitmaps.
`LCE-PLAY-LAYOUT.json` records their source and decoded-pixel hashes, original
LoadOrJoinMenu and checkbox scene measurements, and translated C++/ActionScript
list/navigation/fade references from the same pinned LCE revision below.
These retain their upstream rights. Existing blue highlight art is unchanged.

Version 1.9.0 adds original LCE SD/HD checkboxes, ticks, sliders, text fields,
horizontal pack-list borders/arrows, vector panels, More Options tabs and default
comparison artwork. `LCE-WIDGETS-ACTIONS.json` records all 43 source images,
original strings, scene measurements and translated C++/ActionScript references
at revision `acff48c2c7e18406fd6000b56945746ca9a6eb69` of
https://github.com/chrisGrando/MinecraftLCE-7thGen . These retain their upstream
rights. Installed Java pack icons are loaded from the user's packs at runtime
and are not replaced with console DLC assets.

Original Java glue and the unused legacy `quiet_horizon.ogg` composition are
under MIT, except the adapted shield controller and associated shield hooks
described below. Imported source and assets retain their upstream rights.
Console interface textures, retained alternate glyphs, menu panorama and
other retained interface artwork are imported from **Legacy4J 1.20.1** by Wilyicaro
(Icaro K. Bomfim) and contributors, at revision
`80ca31049dd5ea9f7169810f3cadd0aa38114a51`.

That branch is distributed under the MIT license (copyright 2023–2024
Icaro K. Bomfim). Its license is preserved in `licenses/Legacy4J-MIT.txt`
and `META-INF/licenses/Legacy4J-MIT.txt` in the JAR. See
https://github.com/Wilyicaro/Legacy-Minecraft/tree/1.20.1 .
`REFERENCE-ASSETS.json` records each imported path and SHA-256. Imported
Legacy4J sound recordings were removed in 1.8.0. The UI layout follows that branch and
Console Edition behavior, with new rendering and input glue for Forge 1.11.2.
Credit also belongs to the upstream asset contributors, including the
PlayStation component artwork contributors credited by Legacy4J.

This JAR also includes:

* Jamepad 2.30.0.0 (Java bindings and native libraries), Apache License 2.0.
  William Hartman and the libGDX Jamepad contributors.
  https://github.com/libgdx/Jamepad
  Published source: https://repo.maven.apache.org/maven2/com/badlogicgames/jamepad/jamepad/2.30.0.0/jamepad-2.30.0.0-sources.jar
* libGDX gdx-jnigen-loader 2.2.0, Apache License 2.0, libGDX contributors.
  https://github.com/libgdx/gdx-jnigen
* SDL2 2.30.0, linked into the Jamepad native libraries, zlib license.
  Copyright Sam Lantinga and SDL contributors. https://libsdl.org/
* SDL_GameControllerDB mapping database, zlib license, SDL community contributors.
  https://github.com/gabomdq/SDL_GameControllerDB

Copies of these licenses are in `licenses/` in the source package and
`META-INF/licenses/` in the built JAR. Gradle's wrapper and ForgeGradle have
their own upstream licenses. Minecraft and Forge dependencies are obtained
from their original repositories and are not bundled into this mod.

MinecraftLCE-7thGen, revision `acff48c2c7e18406fd6000b56945746ca9a6eb69`:
https://github.com/chrisGrando/MinecraftLCE-7thGen . Original Minecraft Console
Edition code and artwork belong to their respective Mojang/4J rights holders.
Version 1.4 translates its creative catalog, tab/page logic, quick-select and
recipe registration behavior, and adapts its container interfaces and chat
fading. Four original creative panel/tab files plus 34 other interface PNGs
are included unchanged. Eighteen PS4 glyph bitmaps are exported losslessly
from the original Orbis skin SWF. `LCE-REFERENCE.json` and `LCE-INTERFACES.json`
record paths and hashes, including source SWF and decoded bitmap hashes.
`LCE-PORT.md` describes the adaptations and distinguishes the user's stricter
world-lock rule from actual LCE's achievements warning.
The earlier current-main Legacy4J reference was superseded by the requested
1.20.1 branch.

The music references C418's original game tracks, already installed by Minecraft
1.11.2. Those recordings and the Minecraft logo are loaded from the game and
are not bundled here. This is an independent Forge 1.11.2 adaptation, not an
official Mojang, 4J Studios, Sony or Legacy4J release.

OptiFine by sp614x is an optional, separately installed runtime dependency.
The integration was checked against the user-supplied 1.11.2 HD U G5 JAR.
Its settings and shader logic run from that installed JAR; no OptiFine bytecode,
assets or JAR is included in this mod or its source archive.

Version 1.6.7 additionally uses the two original Common/Media/Graphics LCE panorama PNGs, copied without modification. Active sleep/death code and skin-movie layout references are recorded in SCENES-REFERENCE.json at the same pinned revision.

Version 1.6.10 additionally exports ten original brewing runtime bitmaps from
`skinGraphicsInGame.swf` and the two original brewing background symbols from
the 720/480 scene movies. Source/export hashes, slot-list and progress-mask
behavior, and the translated LCE alternate-font/book rendering are documented
in `BREWING-REFERENCE.json` and `LCE-PORT.md`. No replacement artwork was drawn.

Version 1.6.11 uses the pinned Legacy4J brewing screen/menu and icon-holder
code as its brewing layout reference. Its original brewing progress and bubble
PNG files are copied unchanged alongside the existing composite, coil, fuel
icon and fuel-bar artwork. `REFERENCE-ASSETS.json` records their hashes.

## LCE startup and boat references (1.6.13)

Original ESRB, Mojang and 4J introduction artwork, the plain Minecraft title
atlas, three lossless loading-panel/bar bitmap exports and eight gameplay tips
come from MinecraftLCE-7thGen revision
`acff48c2c7e18406fd6000b56945746ca9a6eb69`.
`STARTUP-BOAT-REFERENCE.json` records original paths, hashes, bitmap IDs and
text keys. Introduction timings and loading geometry follow its SWF scene
code and C++ UI scenes; boat control/prompt behavior follows Input.cpp,
Minecraft.cpp and Boat.cpp, adapted to native Java boats.
These assets and translated upstream portions retain their upstream rights
and credits to Mojang, 4J Studios and the original contributors. They are not
relicensed by this project's MIT license. No OptiFine binary is bundled.

## Legacy4J shield controls (1.6.15)

Shield control logic is adapted from `LegacyShieldControls`, `MinecraftMixin`
and `LegacyItemUtil` in Legacy4J revision
`f49505dab97480f687025f8a54208d2caa3ff86c`, with its current license preserved
in `licenses/Legacy4J-controls-LICENSE.txt` and the JAR's META-INF/licenses.
The adapted ConsoleShield and associated shield hook portions retain the
upstream LGPL-3.0 license and copyright (C) 2026 Icaro K. Bomfim.
Original authors and contributors retain their rights; the exact code paths
and SHA-256 values are recorded in `CONTROLS-REFERENCE.json`.
Held-hotbar repeat and slot behavior follow the pinned LCE Orbis/PS3/Windows
initialization, Minecraft.cpp and Inventory.cpp recorded in the same manifest.


## Display/HUD and additional controller artwork (1.7.0)

`LCE-DISPLAY-CONTROLLERS.json` records 121 additional original images from the
same pinned LCE repository: 31 HUD PNGs, 18 Xbox 360 PNGs, and 18 glyphs each
losslessly decoded from PS3 and Durango platform SWFs, plus 18 each from the
HD Orbis and HD Durango skins for 1080p and higher. Existing SD Orbis/PS4
exports are unchanged. HUD state/layout and safe-zone behavior follow
`UIScene_HUD.cpp`, HUD720/HUD1080 and their Hud ActionScript; menu key repeat
follows `UIController.cpp/.h`. Java engine integration and later-feature
fallbacks are documented in README.md. No replacement artwork was drawn.

The SDL GameControllerDB by Gabriel Jacobo and contributors is included at
revision `c0f11d40b91f57853ce9714c703e119d8243f91c` under the zlib license,
preserved in `licenses/SDL-GameControllerDB.txt` and the packaged licenses.
https://github.com/mdqinc/SDL_GameControllerDB

## LCE tooltip symbols and layout (1.7.1)

143 original embedded bitmaps are exported losslessly from the pinned Orbis,
PS3, Durango and Windows Xbox skins. Normal FJ_Tooltips frames retain the
original display-list dimensions, layering and padding. UIComponent_Tooltips,
UIScene, FJ_ToolTip/ToolTips and IUIScene_CraftingMenu supply spacing, safe zones
and paired bumper layout. LCE-GLYPHS.json records paths, hashes and adaptations.
Original Mojang/4J artwork rights are unchanged. Java supplies label rasterization.

## LCE prompt bitmap font and descriptions (1.7.2)

The unchanged Mojangles7 TitleUpdate atlas is copied from MinecraftLCE-7thGen
revision `acff48c2c7e18406fd6000b56945746ca9a6eb69`. Its codepoint/cell
metrics and alpha-mask/advance behavior are translated from `UIBitmapFont`
and `UIFontData`, as installed by `UIController::SetupFont`. Original item
use-description bindings and text come from `Tile.cpp`, `Item.cpp` and
`Common/Media/strings.resx`. Crafting panel prompts/cycling follow
`IUIScene_CraftingMenu.cpp`. `LCE-PROMPT-TEXT.json` records exact source,
asset and generated-data hashes. Original ownership and notices remain;
private use does not change upstream licensing.

Version 1.8.0 removes all seven Legacy4J UI recordings. The replacement six
cues originate in the pinned LCE Common/Media/Sound WAVs and MenuSounds.xwb
bank. Original WAVs are copied where available; the other three are decoded
with vgmstream r2168 and trimmed to XAP source frame counts, with XAP sample
rates. Original cue gain/pitch metadata is translated from MenuSounds.xap.
The WAV payloads use .ogg resource names only for Minecraft 1.11's resource
lookup; the runtime detects RIFF and reads PCM, avoiding another lossy encode.
The vgmstream executable is a development tool and is not bundled. Its source
is at https://github.com/vgmstream/vgmstream/tree/r2168 .

The original Common/Media/splashes.txt is copied unchanged. Animated-player,
minecart action, music scheduling and splash behavior are adapted from LCE
C++ as recorded in LCE-GAMEPLAY-PRESENTATION.json. Minecraft's installed music
files and player renderer are used without bundling game code or recordings.

Version 1.8.1 replaces only the focus cue with the active PS4 soundbank event
Minecraft/UI/focus, extracted from PS4_GAME/Sound/Minecraft.msscmp at the same
pinned LCE revision. Its embedded BCF1/Bink recording is decoded once to PCM16
with vgmstream r2168, retaining native rate/count and event pitch bounds.
LCE-AUDIO.json records the whole-bank, embedded-stream and PCM hashes. No Bink
runtime or source bank is bundled. The original 4J intro PNG remains unchanged;
the hold-until-ready presentation is the user's requested adaptation.

The 1.8.2 navigation stop ramp is adapted from DEFAULT_DLRS=32 and
SAMPLE.fade_to_stop in the same pinned repository’s
Minecraft.Client/Orbis/Miles/include/mss.h; its hash is in LCE-AUDIO.json.
The original sound asset remains unchanged.
## Version 2.0.18 reference behavior

Bucket prompts port the original `BucketItem::TestUse` and `Minecraft::GetActionTooltips`
eligibility checks, using native Java 1.17 bucketable entities and fluid containers.
The paper doll uses `Gui.cpp`'s xd=-40 / yd=10 angles, body-rotation cancellation,
and head alignment. Multiplayer sleep announcements use the original BedTile,
ServerPlayer and ClientConnection message rules and original English strings.
Intro confirmation follows UIScene_Intro's single navigation guard and the UI
controller's press/release ownership; the requested resource-readiness gate is
adapted to Java loading. Existing original logo images and Intro720 fades are
retained. Held maps reuse the existing LCE paper and the native 1.11.2 unexplored
pixel formula. Reference details are recorded in `FEATURES-218-REFERENCE.json`.

## Version 2.0.17 HUD messages

Failed-sleep notifications follow the original `BedTile`, `LocalPlayer` and `Gui`
message-history path. Chat backgrounds use the original HUD720/1080
`LabelNBackground` shape fill and full-viewport span. The flat rectangle is drawn
directly from its source colour; no substitute texture is introduced.
`HUD-MESSAGES-REFERENCE.json` records the C++ references, movie hashes, shape and
display-list matrices. Existing rights and notices are retained.

## Version 2.0.16 multiplayer connection presentation

The connection view ports `CGameNetworkManager::StateChange_AnyToJoining`,
`StateChange_AnyToStarting`, `UIScene_ConnectingProgress`, and the Xbox XUI
Cancel Join behavior from the pinned LCE revision. Its square timer retains the
original `skin.swf::fourj.FJ_Timer` display-list positions and all 80 frames of
alpha transforms. The original white rectangle shape is rendered directly.
`LOADING-REFERENCE.json` and `MULTIPLAYER-JOIN-REFERENCE.json` record the source,
stage mapping and hashes. All existing rights and notices are retained.


## Version 2.1.0 world-generation sources

The Legacy Overworld algorithms are Java ports/comparisons of Minecraft / LCE
world-generation code and the PS4 executable in Nejirus/mcps4 revision
37e258085773f11599cf0ffe29119926769554b7, with readable C++ references from
MinecraftLCE-7thGen revision acff48c2c7e18406fd6000b56945746ca9a6eb69.
Earlier Java GenLayer/NoiseGenerator algorithms supplied Java expression references
for shared routines. Original Microsoft/Mojang/4J ownership and terms remain; these
derived algorithms and original third-party assets are not relicensed under the
project's MIT notice. That notice applies to original integration contributions.
No PS4 executable, platform SDK, game JAR, decompiler, or native oracle harness is
bundled. Native-reference outputs are ordinary generated JSON data. Method, input
and reference-file hashes and all target-version adaptations are recorded in
WORLDGEN-PS4-REFERENCE.json and docs/LEGACY-WORLDGEN.md.

## Version 2.2.0 PS4 structures and options

The 450 Overworld structure templates are original Minecraft/Mojang/Microsoft/4J
assets from Common/Media/MediaOrbis.arc in the same pinned mcps4 revision. GZIP
wrappers preserve their original NBT bytes; templates.json records source hashes.
Captured pool, element, biome-filter, crop/path/wear and balanced-seed data retain
their original provenance. Shared procedural block-placement expressions are
adapted from Minecraft Java to the recovered PS4 rules and target APIs. Original
rights and notices remain; these assets and derived expressions are not relicensed
under the project's MIT notice. No PS4 executable or platform SDK is bundled.
The generation notes and WORLDGEN-PS4-REFERENCE.json document the port's limits.

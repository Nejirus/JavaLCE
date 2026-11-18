# Legacy Console for Minecraft 1.17.1 — Release 1.0.0

This is the separate Forge port of the 1.11.2 project. It uses the **PS4 menu panorama**, Java logo, console menus, original LCE interface artwork and sounds, controller layouts, floating HUD, inventory-funded console crafting, fixed-grid map upgrades and world-origin restrictions.

## Release 1.0.0

Release 1.0.0 is the renamed 2.3.18 build, with the same features and fixes.
The mod, compatibility add-on, source archives and main-menu version label
now use 1.0.0. Update both mod jars together. Network protocol remains 8;
LCE Look retains its independent 2.0.0 version.

See [release naming and verification](docs/RELEASE-1.0.0.md).

## Map cursor and player-list style in 2.3.18

Map icons now use PS4's original depth sequence above the terrain image,
fixing a cursor/image depth conflict that could make the cursor flicker while
moving or turning. The first icon uses -0.04 and later icons follow the
source's repeated -0.01 layer step. The map image remains at -0.02.

Server-details player buttons use the same original list artwork and selected
state as world select, with its 30-unit row height. The list fits inside the
recessed frame and still scrolls to the final player.

Update the main mod and optional compatibility add-on to **2.3.18** together.
Network protocol remains **8**; LCE Look remains **2.0.0**.
See [PS4 source evidence and validation](docs/MAP-CURSOR-2318.md).

## Multiplayer colors, list sizing and saved hints in 2.3.17

The server-details player rows fit inside the recessed frame, including narrow
GUI profiles. Host menus wrap from the first enabled row to the last and back
through controller navigation and mouse-wheel scrolling, following Settings.

Multiplayer uses mcps4's first-unused player-slot allocation, original palette
and rectangular name-tag border geometry. Everyone receives the server's slot
assignments; departures leave other players' colors alone, respawn retains the
slot, and a new arrival can reuse a freed slot. Host-menu icons use that same
assignment. Colors belong to the current server session, as on PS4.

Block and general hints already saved their seen history. Ordinary menu guides
now also save partial progress and completion in the local hint profile, so a
reload resumes an unfinished guide and does not repeat a completed one.
Tutorial-world progress continues to save per player in that world. Manual
**What's This?** descriptions remain available.

Update the main mod and optional compatibility add-on to **2.3.17** together.
The network protocol is **8**. LCE Look remains **2.0.0**.
See [PS4 source evidence and validation](docs/PLAYER-COLORS-2317.md).

## Host-option activation fix in 2.3.16

Controller confirm now activates the highlighted host-menu row after navigating,
scrolling or changing pages. This fixes Set Day, Set Night, Set World Spawn Point
and the host/player checkboxes using an outdated cursor position. Keyboard Enter
uses the same activation callback. Host privileges remain required.

Update the main mod and optional compatibility add-on to **2.3.16** together.
See [the reproduction and input-path validation](docs/HOST-ACTIVATION-2316.md).

## Anvil and host-menu layout fixes in 2.3.15

The anvil name is white and centered in its original PS4 text field. Host and
player buttons now use the same renderer and artwork as Settings, with centered
labels. Host sliders use the Settings track, outline, highlight, handle and
tint layers, support pointer dragging, and share the Settings row spacing.

Mouse-wheel scrolling stops on a filled final page. Controller navigation keeps
the selected row visible, and network updates retain the scrolled position.
Select/Back/Share remains the host shortcut; Square stays unbound in pause.

Update the main mod and optional compatibility add-on to **2.3.15** together.
See [layout and validation notes](docs/UI-LAYOUT-2315.md).

## PS4 frontend and host options in 2.3.14

The main menu uses the original mcps4 panorama images and animation positions.
Anvil and furnace use the PS4 scenes, artwork and navigation, including blast
furnaces and smokers through their native Java menus. These screens follow
Console UI Size.

Select/Back/Share opens the PS4 player/host menus. The pause-menu Square
shortcut and its glyph are removed. Host rules, time/weather, spawn points,
teleporting, game modes and player permissions require enabled host privileges,
with every request checked by the server. Player settings are saved per world.

Survival difficulty remains editable and survives reload; only Hardcore locks
it to Hard. Existing game-mode/origin restrictions remain in force.

Update the main mod and optional compatibility add-on to **2.3.14** together;
network protocol is **7**. LCE Look remains **2.0.0**.
See [source and validation notes](docs/PS4-FRONTEND-2314.md).

## Console UI Size for LCE containers in 2.3.13

**Settings → User Interface → Console UI Size** now also scales the LCE
inventory, creative menu, furnace and storage screens, plus smithing and
grindstone. The same three stops apply: 1 Small, 2 Normal and 3 Large.
Smithing and grindstone use the same ratios and their existing 21-unit player
inventory pitch as the LCE containers.

Menu text, holders, items, progress bars and entity previews scale together.
Mouse clicks/dragging and controller holder rectangles use the same transform.
Creative tabs and larger chests fit above the existing footer. Normal retains
its previous size where it fits. HUD Size and the footer remain independent;
native hover tooltips retain the same screen-space behavior as the PS4 menus.

Update the main mod and optional compatibility add-on to **2.3.13**.
Network protocol **6**, saved tutorial history and LCE Look **2.0.0** are retained.
See [source and validation notes](docs/LCE-CONTAINER-SCALE-2313.md).

## Xbox 360 artwork, console UI size and saved guides in 2.3.12

Xbox 360 uses the original LCE Xbox 360 XUI/PNG button artwork throughout
menus, help pages, the HUD and tutorial. The Windows skin is no longer used
for that controller family. Higher resolutions scale the same original images.

**Settings → User Interface → Console UI Size** has three stops. It scales
PS4 crafting, stonecutting, loom, cartography, enchanting, brewing, beacon and
trading menus, plus tutorial/description panels. **HUD Size** stays independent.
The option adapts PS4's original numbered size stops and HUD frame ratios to
these menus; it uses the recovered PS4 slider images. Normal preserves the
existing menu size, and screen-fit limits keep enlarged panels usable.

Tutorial saves retain completed menu guides and partial guide progress per
player. Reloading resumes an unfinished guide at its saved step. Older saves
recover inventory/furnace guide completion when saved gameplay proves it.

Update the mod and optional compatibility add-on to **2.3.12** on the server
and modded clients. The channel protocol is **6** because guide history is now
synchronized. LCE Look remains **2.0.0**. See [source and validation notes](docs/GUI-TUTORIAL-2312.md).

## Tutorial text and flint-and-steel prompts in 2.3.11

Tutorial messages remove original authoring tabs before font layout, fixing
U+0009 placeholders in the trading, anvil and final tutorial instructions.
Paragraph breaks and the selected controller artwork remain intact.

Flint and steel offers **Use** for valid ignition targets. Looking into the
air or at blocked or unsupported fire placement no longer shows that prompt.
Campfire, candle, TNT and creeper interactions retain their applicable prompts.

Update the mod and optional compatibility add-on together to **2.3.11**.
LCE Look stays at **2.0.0**. See [source and validation notes](docs/PS4-TUTORIAL-TABS-FLINT-2311.md).

## Tutorial progression and world options in 2.3.10

The furnace tutorial now completes after glass is collected and advances to
the next lesson. Automatic task completion reports the menu state just as
confirmation prompts do. The PS4 starting-area constraint restricts horizontal
movement and leaves height unrestricted; the incorrect Y=75 ceiling is removed.

Saved tutorial worlds resume without the repeated experimental-generator
warning. Recognition uses the tutorial save marker, including renamed saves.
When creating a Legacy world, World Options shows **Legacy** and hides the
Superflat toggle. Other world types retain their existing Superflat controls.

Update the mod and optional compatibility add-on together to **2.3.10**.
LCE Look stays at **2.0.0**. See [PS4 source and validation notes](docs/PS4-TUTORIAL-FIXES-2310.md).

## Tutorial, inventory and map fixes in 2.3.9

The PS4 crafting tutorial now recognizes **Square** (keyboard **X**) through
all three information panels. Input is remembered while the original minimum
message time elapses. Tutorial buttons use the PS4 45-pixel source size, with
line spacing and wrapping that accommodate the selected controller artwork.

Inventory actions follow the item under the pointer: **Take**, **Quick Move**
and **What's This?** require an item; **Split** requires more than one. Carried
items use placement or swap prompts. **R2** still opens item information.

Map north remains negative Z. The conflicting 64-pixel edge correction has
been removed: the original PS4 routine and Minecraft 1.17.1 both use a
63-pixel threshold. A lit campfire offers **Use** when a held shovel can
extinguish it, following the target game's actual interaction conditions.

Update the mod and optional compatibility add-on together to **2.3.9**.
LCE Look stays at **2.0.0**. See [source and validation notes](docs/PS4-INTERACTIONS-239.md).

## PS4 crafting information and controller glyphs in 2.3.8

In ordinary crafting and stonecutting, press **Square** (keyboard **X**) to
cycle **Inventory → Description → Ingredients → Inventory**, following the
PS4 code. Ingredients show their names, required quantities and missing-item
marks. The selected recipe and normal crafting transactions remain active.

Inventory hover tooltips show the usual item information. **R2 — What's This?**
(keyboard **Page Down**) opens the PS4 upper-right description popup. Optional
Hints can be Off without disabling this manual action.

Tutorial input symbols use the existing PS4, PS3, Xbox 360 or Xbox One artwork
selected under **Controller Glyphs**, including Automatic controller detection.
Keyboard bindings remain text. Update both mod jars to **2.3.8**; LCE Look
stays **2.0.0**. [Source evidence](docs/PS4-CRAFTING-INFO-238.md).

## PS4 tutorial and hints in 2.3.7

**Play Game → Play Tutorial** appears directly below **Create New World**.
Each press creates a fresh copy of mcps4's original PS4 tutorial save. Existing
saves are preserved; resume a tutorial from the saved-world list. The source
starting area, task sequence, menu guides and 12 tagged music-disc hunt are
adapted to Minecraft 1.17.1. The original saved terrain is converted, rather
than regenerated. This does not change ordinary world generation.

Block information uses recovered PS4 descriptions first, with Legacy4J text
for newer blocks. **Settings → Hints** controls optional prompts. Tutorial
instructions remain active inside tutorial saves. Page Down / R2 opens block
information for the item under the pointer in supported inventory menus.

Update the main mod and optional compatibility add-on together to **2.3.7**.
Guided prompts need the main mod on the client. LCE Look remains **2.0.0**.
[Source evidence and Java integration limits](docs/PS4-TUTORIAL-237.md).

## PS4 trading and crafting tabs in 2.3.6

The trading screen uses mcps4's original scene, ten-cell offer row, request and
result holders, level labels, experience bar/preview, scrolling and sold-out
cross. Payments stay in the inventory until a server-validated trade completes.
Traded output that cannot fit drops beside the player, following the PS4 path.
Java 1.17.1 supplies offers, prices and XP; future locked PS4 offers are not
invented from Java packets. The firework, banner and dye side-tab icons use the
original inactive-frame offset, fixing their horizontal alignment.

Update the main mod and optional compatibility add-on together to **2.3.6**.
Protocol 5, 2.3.3 recipe grouping, maps and world generation are unchanged.
[PS4 source evidence and integration details](docs/PS4-TRADING-236.md).

## UI corrections in 2.3.5

Crafting and stonecutting lose the extra upper-right recessed panel. Crafting
restores the working 2.3.3 item families and order while retaining PS4 menu art
and the 1.17 recipes. Beacon labels gain left padding, and netherite appears
before emerald in the payment row. The enchanting book keeps the verified PS4
57-unit scale and uses the actual FUI bounds for its center.

Update the main mod and optional compatibility add-on to **2.3.5**. Protocol 5,
world generation and map margins are unchanged; a new world is not required.
[Implementation notes and PS4 book evidence](docs/PS4-UI-FIXES-235.md).

## PS4 workstation menus in 2.3.4

Crafting, stonecutting, loom, cartography, enchanting, brewing and beacon menus
now use recovered **mcps4** code paths and original PS4 FUI artwork. General
crafting keeps Minecraft 1.17.1 recipes, including the newer copper, deepslate
and honey-related items. Loom supports the 32 original basic patterns, reusable
templates, staged layers, and Java's globe/piglin additions.

Update the main mod and optional Join Compatibility add-on together to
**2.3.4** (network protocol **5**). LCE Look remains **2.0.0**. World generation
and map margins are unchanged from 2.3.3. This menu release does not require
a new world. The inventory-funded stonecutter and loom require the mod on the
server; singleplayer includes that server.

[PS4 source evidence, extraction and target adapters](docs/PS4-WORKSTATIONS.md)
identify what was translated and what still comes from Minecraft 1.17.1.

## Playable PS4 Overworld port in 2.3.3

New **Legacy** worlds use the recovered PS4 biome, terrain, surface and carving
routines, followed by the translated PS4 population sequence: ores, lakes,
dungeons, biome-specific trees, plants, aquatic features, fossils, ice and snow.
The feature calls share the translated population random stream. Original PS4
structure placement uses the existing port's structure pieces.

Maps now use the PS4 full 128×128 image and original coordinate transform.
Classic's ten-pixel inset on each side comes from its 864-block coverage at
scale 3. The earlier custom 100×100 image shrink and extra 14-pixel inset are
removed. Legacy map-grid alignment and finite sampling remain confined to Legacy
worlds. Bounds stay centered at zero for every seed.

Bee nests, deepslate, copper and amethyst geodes use Minecraft 1.17.1 adapters
and separate random streams after PS4 population. Nether and End generation
remain native. New 1.17 structure work is deferred.

**Start a new Legacy world for this generator update.** Existing chunks retain
their terrain; generating new chunks in an older world can create seams.
Update the main mod and optional server compatibility add-on together to 2.3.3.
LCE Look stays at 2.0.0.

This release prioritizes playability. **Full PS4 seed parity is not claimed.**
The world adapter uses Java block states, lighting, physics and loot; structure
geometry/RNG and final spawn still need complete-world comparisons. See the
[2.3.3 release notes](docs/PLAYABLE-233.md), [source evidence](docs/PS4-PORT-233.md)
and [verification](TESTING.md). The older release sections below are retained
as history; their custom map-margin descriptions no longer describe 2.3.3.

## PS4 maps, finite bounds, and spawn in 2.3.2

The Legacy Overworld now uses the recovered PS4 map grid, first-map scale choice,
source-centered enlargement, and empty-chunk handling beyond its finite bounds.
Default and Flat keep their existing map behavior. The held-map image inset,
coordinate text in the paper border, and uniform visual margins are unchanged.
A separate Java adapter keeps one-handed maps inside viewports narrower than 16:9;
it does not change the map image or paper layout.

New Legacy worlds use the translated PS4 biome/grass spawn search and initial
player-placement branch. Existing saved world spawns and player positions are
not rewritten. The finite border was already at PS4's origin-centered limits;
the verified positions are retained for every seed, with no seed-dependent offset.

[Implementation notes and evidence](docs/PS4-MAPS-BOUNDS-SPAWN.md) identify the
functions, instruction fixtures, target-version adapters, and remaining limits.
The translated spawn procedure does not establish complete PS4 seed-to-block
equality for the existing terrain port.

## Installation

1. Create a separate Minecraft **1.17.1** launcher installation and game directory.
2. Install **Forge 37.1.1**, using **Java 17**.
3. Put `legacy-console-1.17.1-1.0.0.jar` in that installation's `mods` folder.
4. For shaders, also install **OptiFine 1.17.1 HD U H1**, then put `LCE-Look-1.17.1-2.0.0.zip` in `shaderpacks` and select it under Graphics → Shaders.

OptiFine is optional for the mod. Its settings appear only when it is present. The uploaded 1.11.2 G5 OptiFine JAR cannot be used on 1.17.1. Obtain H1 from https://optifine.net/downloads . The shaderpack requests Old Lighting ON; OptiFine's Default setting follows the pack.

Install the mod on the server when using console crafting and inventory-funded villager trading. Singleplayer includes its own server. This version uses native 1.17.1 recipes, remainders, menu transactions and server validation.

## Dye ingredients and banner selector fixes in 2.3.1

Banner, firework and dye menus offer only the 16 separate Minecraft dye items.
Bone meal, lapis, cocoa beans and ink sacs cannot substitute for dye in these
crafts. Their ordinary recipes and other uses remain available.

The focused banner selector is now drawn above neighboring squares, keeping
its full border, arrows and highlight visible without changing spacing or art.

## PS4 banner, firework and dye crafting in 2.3.0

These three crafting tabs now use the recovered PS4 crafting state machine and
original `mcps4` FUI menu artwork and transforms. This replaces their Legacy4J
selectors and layout. Banner crafting supports the five PS4 pattern groups,
staging/removing up to six layers, the large preview, original material recipes,
and copying. Personal crafting offers the original shapeless material patterns;
shaped banner patterns require a crafting table. Paper pattern recipes remain
in the native loom.

Fireworks use the PS4 Color/Shape/Effect, Fade and Rocket groups. Triangle adds
an ingredient, Square removes the last added ingredient, and Cross creates the
result. Staged ingredients stay fixed as you browse other choices. Dyeing offers
Armor and Item groups, including leather horse armor, wool, beds and shulker
boxes. The PS4 color order uses Minecraft 1.17.1's 16 separate dye items. Recoloring retains item names and shulker contents.

The original 1920×1080 scene scales uniformly at 720p, 1080p, 1440p and 4K,
and fits narrower 16:10 and 4:3 displays. Java 1.17 item models, localized item
names and native firework/leather color calculations adapt those menus to this
game version. See [PS4 crafting provenance](docs/PS4-CRAFTING.md) for source
addresses, extraction details and the limits of the accuracy comparison.

Update the main mod and optional Join Compatibility add-on together to **2.3.1**.
The crafting network protocol remains 4; clients with an older main mod must update.
The shaderpack remains **2.0.0**.

## Offhand maps and world options in 2.2.1

Offhand maps retain their map texture when another item is held in the main hand
with OptiFine shaders. The map alpha pass prevents OptiFine from flushing other
batched geometry while its texture state is being set up or restored. The paper,
coordinates and faint unexplored checker pixels keep their existing presentation.

Legacy world options now stack World Type, World Size and Biome Scale vertically
at the same width. Find Balanced Seed uses the original checkbox artwork and
controls the blank-seed search when Create New World is pressed. It defaults to
on; turn it off for an ordinary random seed. An explicit seed disables the search
and is preserved, including numeric zero. Canceling the search returns to the
creation menu without starting a world.

## Legacy worlds in 2.2.0

Create World → More World Options → **World Type: Legacy** adds a finite PS4
Overworld: Classic (864), Small (1,024), Medium (3,072), or Large (5,120 blocks).
The Nether and End retain native 1.17.1 generation. **Biome Scale** offers Small,
Medium and Large; **Find Balanced Seed** uses the recovered PS4 category rules.
Seed, size and scale persist through saves and world recreation.

This update removes the visible Java border from modded clients, preserves the
finite ocean and collision boundary, and fixes the surface/tint indexing that
caused chunk-shaped biome seams. It replaces Java structure assembly in Legacy
worlds with PS4 placement and saved piece graphs, 450 original Overworld templates,
recovered village pools/processors, and console procedural structure rules.
It also fixes ship heights at coastal cliffs and old jigsaw wall/name conversions.

**Create a new Legacy world to see the complete changes.** Existing generated
chunks keep their previous terrain and structures. The source and validation are
recorded in [the generation notes](docs/LEGACY-WORLDGEN.md) and
`WORLDGEN-PS4-REFERENCE.json`. Some block placement, loot, mobs and decoration
remain 1.17.1 adaptations; complete PS4 seed-to-block equality is not claimed.

Dedicated worlds accept `level-type=legacy`,
`legacy-world-size=classic|small|medium|large`, and
`legacy-biome-scale=small|medium|large`. Update the mod and optional Join
Compatibility add-on together to 2.2.1. The shaderpack stays at 2.0.0.

## Maps, multiplayer sleep, controls and intro in 2.0.18

Held maps use the 1.11.2 paper presentation: unexplored checker pixels stay
inside the discoverable map square, leaving plain paper around the coordinates.

With this version on the server, the standard all-players sleep rule uses LCE's
chat notices: the sleeper sees “You are sleeping in a bed…”, while others see
the sleeper's name. Java's count/skipping notices are replaced for that rule.
Custom Java sleep percentages retain their own status text. When joining a
server without the mod, its sleep status is displayed in chat.

Crouch to Shield defaults to on, including one upgrade migration for older
profiles. Turning it off afterward is saved normally. Bucket prompts check the
aimed action: Collect for eligible source fluids/cows and water-bucket captures
of fish or axolotls, and Empty for valid fluid placement on a block.

The original intro logo fades work again, and 4J stays visible while loading.
Cross/A advances a logo while loading. Once resources are ready, it skips the
entire intro to the main menu from any logo. That press is consumed until release,
so it cannot also select Play Game. Enter/Space follows the same rule.

The paper doll uses LCE's fixed view and head/body alignment. Source and Java
adaptation details are in `FEATURES-218-REFERENCE.json`.

## Sleep warnings and chat backgrounds in 2.0.17

Failed-sleep warnings, including daytime and nearby-monster warnings, now appear
in chat history as in LCE, clear of the floating HUD. Minecraft's server still
decides whether the player can sleep.

Visible chat rows, including achievement announcements, have backgrounds that
reach both screen edges. The background uses the original HUD artwork's colour
and follows chat opacity and fading. It remains aligned when changing chat scale,
chat width or display aspect ratio. See `HUD-MESSAGES-REFERENCE.json` for the
original C++ message path and HUD movie shapes.

## Multiplayer joining and server details in 2.0.16

Joining a server now shows LCE's connection timer followed by the console loading
view during login and Forge negotiation. The timer uses the original 80-frame,
30 fps LCE animation and positions. The Java logo and 1.17.1 panorama remain.
Cancel Join works with Circle/B, Escape, or a mouse click on its footer prompt.
Connection errors still show their normal explanation.

The server details panel now shows readable Version and Players values instead
of Minecraft text-object diagnostics. Long values fit within the existing panel.
See `LOADING-REFERENCE.json` and `MULTIPLAYER-JOIN-REFERENCE.json` for the source.

## Retina cursor and 4:3 support in 2.0.15

The controller pointer, slot highlight and held item now share the same GUI
position. Native mouse coordinates update immediately during a controller warp,
without waiting for the macOS cursor callback. Window points and framebuffer
pixels are converted separately, including Retina scaling and 16:10 displays.
Container origins and the hovered slot also stay aligned through GUI resizing.
Trackpad/mouse clicks and scrolling switch back to normal mouse input.

Standard 4:3 output follows LCE's 480-line profile and available 720-scene fallback:
menu and loading artwork fits uniformly, crafting shows one current variant,
and the controller pointer uses LCE's 0.6 SD input multiplier. Existing HUD presets
fit within the source five-percent safe margins at small manual GUI scales.
The Java main-menu logo remains shared with the loading screen.

Temurin 17 remains supported. Native checks use Linux/Mesa with Retina metrics
and delayed callbacks simulated; an actual Apple Silicon Mac is not available
for hardware testing. See `CURSOR-ASPECT-REFERENCE.json` for source details.

## Random loading-tip order in 2.0.14

Loading tips now receive random sort keys once per game launch, following LCE's
`InitialiseTips` content-package path. The shared cursor then walks that shuffled
order across world entry, exit and resource reloads. Longer loads still advance
after seven seconds. Completing the list wraps to the same startup order, as in
`GetNextTip`; the next launch initializes a fresh random order.

The eight original compatible gameplay tips remain unchanged. LCE's specially
pinned multiplayer/SD introductory tip is outside this subset, so every included
tip participates in randomization. See `LOADING-REFERENCE.json` for the exact
source configuration and Java comparator adaptation.

## Loading tip fix in 2.0.13

Loading tips now continue across world entry, world exit and resource reloads,
following LCE's app-wide `GetNextTip` cursor. Each new loading screen takes the
next tip; a longer load takes another after seven seconds. Closing the screen
resets its timer, without restarting the list. This fixes short loads repeatedly
showing the first tip. Original tip text, artwork and loading layout are retained.

## World loading and OptiFine reloads

Version 2.0.12 uses LCE's FullscreenProgress720 layout, original progress bar,
original tip panel and seven-second tip rotation while entering or leaving a world.
The same presentation covers OptiFine/resource-pack reloads instead of the red
Mojang screen. The Minecraft logo uses the exact main-menu texture, size and
position. The Java 1.17.1 panorama remains in use.

Minecraft still owns actual loading, saving, completion and error handling. Resource
and spawn-chunk progress use native values; phases without a native aggregate
percentage keep the bar empty. The opening rating/Mojang/4J intro stays unchanged.
The shaderpack stays at 2.0.0. See `LOADING-REFERENCE.json` for source details.

## Updating

Replace the older Legacy Console JAR in `mods`; keep only one version installed. The LCE Look shaderpack stays at **2.0.0** and does not need replacing. Update the optional server add-on alongside the server mod.

2.0.11 corrects villager trading against LCE's trading scene:

- The heading uses the villager's displayed name and the plain item name, with LCE's “villager offers item” wording. Text-component debug strings no longer appear.
- Unaffordable trades and missing required items use the original LCE red slot and warning icon, exported from skinGraphicsInGame.swf.
- Exhausted offers disappear from the displayed list. Remaining offers shift left, and restocked offers return in their original order. The display retains original server offer indices, so filtering cannot buy a different trade.
- Eight cells are always present. Unused cells remain empty on the right, and additional offers scroll horizontally. This eight-cell minimum follows your request; the pinned older C++ snapshot uses seven visible cells.
- The trading inventory stays read-only and controller navigation cannot select its slots. Enchanted-book details remain visible below the trade row.

2.0.10 corrects creative controls and special crafting:

- Creative Cross takes one item into quick select, Triangle takes the item's full stack limit, and Square clears all nine hotbar slots. Matching stacks merge before empty slots, following LCE. Dropping a carried stack outside the menu works again; right mouse click drops one item.
- Base banners move from Decoration to **Banners → Create Banner**, alongside Copy Banner and Decorate Shield. Creating one uses six matching wool and one stick at a crafting table.
- **Dyeing → Dye Item** accepts beds and shulker boxes. Colored beds can be recolored with one dye, preserving their tags. Structures contains only bed creation from wool/planks.
- Honey bottle recipes are hidden from the crafting menu; other honey recipes and normal acquisition remain available.
- The classic loom uses Legacy4J's panel, input-slot icons, pattern positioning and banner rendering. Native loom transactions, template patterns and the six-pattern limit are retained.
- Switching crafting side tabs no longer replays an already completed craft sound.

2.0.9 fixes accidental diagonal movement by porting LCE's movement-stick preprocessing as well as its sprint threshold. LCE applies its 10000/32767 deadzone separately to each movement axis, then scales the remaining travel. Small sideways deflections now stay straight instead of leaking through the earlier radial filter. Wider turns still permit sideways walking, and sprinting still ends below 0.8 processed forward input.

The General option is now named **Legacy Movement**. Its existing saved preference is retained. **Look / Menu Deadzone** keeps your existing look, menu and inventory-cursor response; the gameplay movement filter uses the source LCE values. Turning Legacy Movement off restores the earlier radial gameplay response. See LCE-MOVEMENT-REFERENCE.json for the input initialization, provided-library checksum and normalization rules.

2.0.8 corrects movement, ingredient previews and resource pack selection:

- **Legacy Sprinting** retains sideways input and stops sprinting below LCE's 0.8 forward-input threshold. Turning the movement stick far enough sideways therefore changes to walking. The saved `noSprintStrafe` preference now controls this source behavior; the option is labelled Legacy Sprinting.
- Crafting previews count each ingredient once across the inventory. With two string toward wool's four-string recipe, exactly two squares are red, starting at the first matching squares as in LCE. Both personal/table previews and special-crafting grids use quantity-aware matching.
- Texture packs select independently. The first selected pack has lower priority than the next; later packs override matching resources while keeping earlier packs' other resources. Select a checked pack again to remove it. Default clears the user pack selection. The world load/create selector shows selected marks and priority in its description; Settings also has a Texture Packs list with checkboxes.

2.0.7 explicitly gives the game native input focus when its prepared startup window is revealed in fullscreen. Keyboard and controller input can go to the game without Alt-Tab. The focus request runs once at startup; switching to another application afterward keeps normal focus-loss behavior.

2.0.6 keeps the native game window hidden during OpenGL and resource setup, then reveals it in fullscreen after the first console intro frame has been swapped. This removes the empty black window that could appear at the top left before fullscreen. The hidden context starts at the selected fullscreen dimensions, while retaining the launcher's windowed size for F11. The original rating/Mojang/4J intro and music sequence remain in place.

2.0.5 corrects the 1.17.1 crafting columns. Polished blackstone buttons join the other buttons; Nether fences/gates, sandstone slabs and polished blackstone brick shapes join their reference families. All magenta dye recipes stay with the dyes, matching by output item as newer Legacy4J does. Rockets use the dedicated Fireworks side tab. Honey bottles have a separate Food column after bread, following Legacy4J's food placement. The imported reference listing remains unchanged; the compatibility adapter supplies the missing entries and old recipe names. The shaderpack stays at 2.0.0.

The retained 2.0.4 fix restores Minecraft's native particle sheet order and draws the XP/item-pickup pass last, preventing black debris and smoke during XP collection. Custom particle layers remain available.

Version 2.0.3 also fixed the skipped intro on **Java 17**. The rating/Mojang/4J sequence is presented from Minecraft's main frame loop, avoiding the old loader's `PermittedSubclasses requires ASM9` failure on the loading-screen hook. Music starts at the rating logo and stays on the same native stream through loading; 4J remains until loading finishes. Java's music player is suspended while LCE music is enabled, including a track that was already playing.

The active shader-pack entry is green again. An OptiFine H1 compatibility fix restores block-hit and block-breaking particles: H1's Forge branches updated their sprites but omitted adding the created particles to the engine. The fix keeps H1's original particles, textures, velocities and lifetimes and only applies to that affected H1 path. The shaderpack remains 2.0.0.

The retained 2.0.1 fixes include working OptiFine controls, restored panel backgrounds and continuous music across menu/world transitions. Wool and candle families use separate Decoration columns; waxed copper recipes and the redundant Maps side tab are hidden. Map operations remain available at the cartography table. Java tutorial/recipe tips are suppressed, while chat and advancement messages have dark backings.

Auto-Jump starts **Off** and is available under **Control**. The first 2.0.1 launch applies this default once; a later choice is saved. Controller look is normal by default, with the existing Invert Y option retained.

## Newer Minecraft content

Creative order and crafting categories use the supplied Legacy4J reference. Unknown newer block IDs, enchantments and potion types are filtered through the real 1.17.1 registries. Native recipe-manager data supplies 1.17.1 recipes, including copper and other new blocks.

The loom, stonecutter, cartography table, grindstone and smithing table use Legacy4J's classic workstation layouts with native 1.17.1 menus. Smithing uses the two-input netherite upgrade mechanic; 1.20 templates are not part of 1.17.1. Banner pattern application uses the loom. Banner copying, shield decoration, leather dyes and fireworks remain available through their appropriate crafting paths. Cartography adds the reference map-name field and on-screen keyboard.

## Controllers and scaling

The existing SDL2/Jamepad backend supports Xbox, DualShock 3/4 and DualSense mappings recognized by SDL and the operating system. Select PlayStation 3/4 or Xbox 360/One glyphs in Control options; the help pages follow that choice. Windows DS3 use still needs a compatible system driver. Physical controller and operating-system combinations need hardware verification.

Automatic UI scale targets the source 640×360 canvas: 720p/2, 1080p/3, 1440p/4 and 4K/6. The source HUD size slider and inventory cursor sensitivity remain available. Button size stays 100%, and brightness stays 67%.

## Optional join compatibility

Add `legacy-join-compat-1.17.1-2.3.3.jar` alongside the main mod **on the server** to admit clients without Legacy Console. They use normal Java menus. Matching Legacy Console clients keep console crafting.

The add-on makes only the Legacy Console channel optional and omits its unused crafting-menu registry entry for Forge peers lacking the mod. Other mods' requirements and registry synchronization remain active. It does not supply another mod's missing content.

## Fabric

The mod JAR is **Forge only**. A Fabric port needs separate lifecycle, networking, registry, screen and overlay integrations. Shared recipe/category data, source layouts and shader equations are reusable, but a working Fabric mod is not included. The shaderpack has no Forge dependency; Iris/OptiFabric execution has not been validated.

## Building

Use **JDK 17** and **Gradle 7.3.3**:

```
gradle build
```

The compiler emits Java 16 bytecode. `build/libs` contains the reobfuscated mod and join add-on. `gradle test` runs the included regression tests. See TESTING.md and PORT-1171.md for verification and adaptations; licenses and source notices are retained.

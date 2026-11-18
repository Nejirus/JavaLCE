# 2.3.8 crafting information, glyph and inventory checks

The full automated suite passes **372 tests**. After the final food/armor icon
rendering adapter, **19 focused tests** also pass. They check the original
PS4 counter transitions, unique-ingredient quantities/shortages, overlapping
alternatives, PS4 item-description bindings, all seven glyph-family spacing
resources and token handling. The full suite uses a 1 GiB test heap; an initial
512 MiB run exhausted its heap on the existing large tree-oracle fixture.

The production jars run on Minecraft **1.17.1**, **Forge 37.1.1**, **Java
17.0.21** and the supplied **OptiFine H1**. The integrated fixture cycles Square
through all three information panels, crafts while Ingredients is visible,
and verifies exactly one pickaxe, three planks and two sticks in the server
transaction. Inventory hover retains normal tooltip information without the
PS4 description. Synthetic R2 input opens the actual upper-right information
popup with optional Hints Off.

**80 frame captures** cover the three crafting states at eight physical sizes
(48), inventory R2 information at the same sizes (16), and all explicit glyph
settings at 720p/1440p (16). Every capture asserts framebuffer dimensions and
OptiFine shader state, with shaders OFF and LCE Look 2.0.0. Glyph checks use a
controller name that differs from the chosen setting, then verify Automatic
recognition of PS3, PS4, Xbox 360 and Xbox One names. The glyph stress fixture
uses two unchanged original inventory-guide paragraphs.

The sizes are 1280×720, 1920×1080, 2560×1440, 3840×2160, 1024×768,
1600×1000, 1512×982 and 3024×1964. Selected crafting, inventory and controller
frames are visually reviewed. These are Linux framebuffer and synthetic input
checks; physical Retina/controller hardware and authenticated remote
multiplayer are not verified. World generation and the tutorial save are
unchanged. [Source evidence](docs/PS4-CRAFTING-INFO-238.md) and
[exact artifacts/captures](docs/NATIVE-238-INFORMATION-RESULTS.json) distinguish
native PS4 logic from Java text/recipe/rendering adapters.

# 2.3.7 PS4 tutorial and button verification

The final full automated suite passes **365 tests**, with zero failures, errors or skips.

The production mod and compatibility jars run on Minecraft **1.17.1**, **Forge
37.1.1**, **Java 17.0.21**, and the exact supplied **OptiFine H1**. A real mouse
click on Play Tutorial launches a fresh original-map copy without an extra
backup dialog. Server checks verify spawn, initial health/food/steak, Classic
864-block bounds, Nether scale 3, all 12 original hunt chest coordinates,
source slots and disc tags. Duplicate discs are rejected, completion counters
round-trip through NBT, and the starting-area restriction returns an escaping
player. A real client confirmation packet advances the original welcome task.

**32 captures** cover the Play menu (16) and tutorial popup (16): 720p, 1080p,
1440p, 4K, 1024×768, 1600×1000, 1512×982 and 3024×1964, each with shaders OFF
and LCE Look 2.0.0. The menu matrix precedes the final launch-dialog adapter;
its placement code is unchanged. Tutorial captures use the final jars.
Selected 1440p and 4:3 images were visually reviewed. These are framebuffer
checks, not physical Apple Retina or controller-hardware tests.

An offline audit checks **3,384 complete chunks**, original block-entity/entity
records, and zero remaining unconverted item stacks. The canonical container
extractor reproduces the original decoded GRF JSON exactly. The source graph
has 43 registrations and 45 flattened tasks, with 17 common menu/activity states.

The task/input, original region and modern item conversions, source description
priority, newer-block fallback, menu ordering, disc tags and persistence have
automated regression coverage. See [source/adapter notes](docs/PS4-TUTORIAL-237.md)
and [exact artifacts and captures](docs/NATIVE-237-TUTORIAL-RESULTS.json).

This run does not establish a full player playthrough of every tutorial branch
or complete PS4 gameplay equality. Java ignores some console-only entity
attribute IDs and supplies its own entity behavior. The complete conditional
hint system and every source screenshot token are not claimed as implemented.
Offline skin/cape lookups fail in this test account; the game remains playable.
Ordinary world generation, prior menu fixes and LCE Look are unchanged.

# 2.3.6 PS4 trading and crafting-tab verification

The full automated suite passes **357 tests**. The eight trading/tooltip
regressions also pass after the final tooltip-depth and cost-warning changes.
The original PS4 binary supplies **360 navigation transitions** and **100 XP
preview results**; Java matches their results exactly. Asset checks cover the
original scene transforms, both vertical-tab frames and shipped PNG hashes.

The final production jars are tested on **Minecraft 1.17.1 / Forge 37.1.1**,
**Java 17.0.21**, and the supplied **OptiFine H1**. Real client input reaches
the integrated server for a two-cost mouse trade and an enchanted-book
controller trade. Counts, result NBT, stock and villager XP are checked.
Unavailable stock, missing costs, invalid indices, mismatched NBT and foreign
merchant context are rejected. A full inventory still permits a funded trade:
output drops beside the player. Partial insertion fills the existing stack
and drops precisely the remainder, with no item loss or duplication.

**144 production-client captures** cover ordinary crafting, banner, firework
and dye tabs plus five merchant states: ordinary, sold out, scrolled enchanted
result, wandering trader and empty offers. Each runs at 720p, 1080p, 1440p,
4K, 1024×768, 1600×1000, 1512×982 and 3024×1964, with shaders OFF and
**LCE Look 2.0.0**. The driver asserts actual framebuffer sizes and loaded
shader state. Selected images are visually reviewed for tab placement,
description readability, panel fitting, stock marks and single-payment layout.

See [source and adapter notes](docs/PS4-TRADING-236.md) and the
[exact artifact hashes/capture manifest](docs/NATIVE-236-UI-RESULTS.json).
Java supplies merchant offers and current levels; future locked PS4 offers
and per-offer tiers are not fabricated. Physical Retina/controller hardware
and authenticated remote multiplayer are not part of this Linux framebuffer
and controller-input fixture coverage. World generation is unchanged.

# 2.3.5 UI correction verification

**27 focused tests pass** with no failures, errors or skips. They cover the
restored 2.3.3 recipe families/order, 1.17 grouping adaptations, original PS4
assets/layout bounds and the unchanged native menu arithmetic. The original
book renderer was also executed at four viewport ratios, retaining a combined
57-unit X/Y model scale. See [book evidence](docs/PS4-UI-FIXES-235.md).

The final production jars pass integrated client/server actions for crafting,
stonecutting, enchanting and beacon on **Forge 37.1.1**, **Java 17.0.21**, and
the supplied **OptiFine H1** jar. The beacon accepts and consumes a netherite
ingot and applies the selected Speed/Regeneration powers. Server transaction
checks also retain ordinary crafting, stonecutting batches, reusable banner
templates and wrong-context rejection.

**64 captures** cover these four menus at 720p, 1080p, 1440p, 4K, 1024×768,
1600×1000, 1512×982 and 3024×1964, with shaders OFF and LCE Look 2.0.0.
The test driver asserts actual framebuffer dimensions and shader state.
Selected captures were visually checked for the missing extra panel, restored
columns, beacon label padding, the five payment icons and book placement.
Physical Retina/controller hardware was not tested. The full world-generation
suite was not rerun for this UI-only patch; prior release results follow below.

Exact tested jar hashes and the capture manifest are recorded in
[the runtime record](docs/NATIVE-235-UI-RESULTS.json).

# 2.3.4 PS4 workstation verification

The automated suite passes **348 tests**, with no failures, errors or skips.
The final asset dependency/hash/layout checks were rerun after resolving the
original nested cartography plus-sign display list. Native fixtures compare
401 brewing ticks and arrow fills, eight beacon button states, 2,232 menu
section transitions, and 84 enchantment names against original PS4 routines.
All 182 exported bitmap hashes and the four scenes' display bounds are checked.

The production jars pass client-to-server actions for all seven requested menus
on Minecraft **1.17.1**, **Forge 37.1.1**, **Java 17.0.21**, and the exact supplied
**OptiFine 1.17.1 HD U H1** jar. Checks cover ordinary crafting, stonecutting,
loom template reuse and dye consumption, cartography naming, enchanting and
lapis consumption, pickup/placement at the edge of a scaled brewing holder,
and beacon effect selection/confirmation. Additional server checks exercise
1.17 beehive and waxed-copper recipes, stonecutting batches, reusable templates,
and rejection of recipes sent from the wrong workstation context.

**112 game-frame captures** cover all seven menus at 1280×720, 1920×1080,
2560×1440, 3840×2160, 1024×768, 1600×1000, 1512×982, and 3024×1964,
each with shaders OFF and the supplied LCE Look 2.0.0. The driver asserts actual
framebuffer dimensions and OptiFine shader state. Selected 1440p, 4:3, 16:10
and MacBook-like frames were visually reviewed. These are framebuffer tests;
physical Apple Retina and controller hardware were not tested.

The integrated-server fixture uses disposable flat worlds. It tests the Java
and Forge adapters described in [PS4-WORKSTATIONS.md](docs/PS4-WORKSTATIONS.md),
not complete PS4 gameplay equality. The original book animation/font palette,
all native auxiliary recipe ordering, and authenticated remote multiplayer
remain outside the verified scope. World generation is unchanged from 2.3.3;
its prior validation is retained below.

Earlier test candidates exposed an unsigned-FUI-color loader error and an
unresolved nested display-list asset; both are fixed in the final build.
The private test driver's teleport and double-click timing were corrected
before the final run. Offline skin/cape requests and sound-pool warnings from
the test driver's rapid recipe navigation are environment/automation artifacts.
The final client completes the transaction and display matrix without a crash.
Exact hashes and frame records are in
[the runtime record](docs/NATIVE-234-UI-RESULTS.json).

# 2.3.3 playable release verification

The full suite passes **342 tests**, with zero failures, errors or skips.
The production main and compatibility JARs run on Minecraft **1.17.1**,
**Forge 37.1.1**, **Java 17.0.21** and the exact supplied **OptiFine H1**.
Three population integration tests were rerun after the final profile-data
cleanup. JARs, source JAR and private runtime driver build successfully.

Fresh Small (seed 0), Medium (-987654321), Large (82958734), Classic
(123456789), Default and Flat worlds pass the native integrated-server checks.
Legacy checks cover safe initial player placement, saved-spawn procedure,
all four collision borders, survival ores, modern deepslate/copper, generated
trees and actual log harvesting. Classic runs with structures enabled and
checks a generated village plan. Default and Flat retain their map behavior;
Nether/End retain their native bounds and generation.

Map checks cover 150 creation cases, first/later blank maps, fractional negative
positions, source-centered enlargement, source preservation, locking and finite
edge discovery. Classic is saved, closed and reopened: generator options,
a placed copper block and saved map survive the reload.

Sixteen rendered gameplay frames pass framebuffer and shader-state assertions:
1280×720, 1920×1080, 2560×1440, 3840×2160, 1024×768, 1600×1000, 1512×982 and
3024×1964, each with shaders OFF and LCE Look 2.0.0. Selected frames were visually
reviewed at 1440p, 4:3 and MacBook-like geometry. Physical Retina hardware was
not available. These map/HUD captures do not prove PS4 terrain equality.

The final run has no rejected far-chunk placement logs. Earlier test clients
failed to open GLFW because the sandbox hid the desktop Xauthority file;
the final run used authorized desktop access. An earlier integration candidate
exposed rejected far-chunk writes; the final adapter clips to Java's generation
region before writing. Offline skin/cape connection failures and software-renderer
lag are test-environment observations.

Exact tested JAR/input hashes, world results and frame hashes are recorded in
[the runtime record](docs/NATIVE-233-PLAYABLE-RESULTS.json). Original-code
fixtures cover the translated kernels; the new target palette check accepts
471,579 native writes and 113 distinct states. Full PS4 Overworld seed parity,
original physics/lighting/loot, full structure results and all-seed final spawn
remain unverified. See [release scope](docs/PLAYABLE-233.md).

The hosted-site preview relay timed out. JavaScript source-archive assembly,
checksum/error handling, file sizes, ZIP integrity and links are checked locally;
production download hashes are checked after deployment.

The following sections record earlier releases as history.

# 2.3.2 verification

Target for this update: Minecraft **1.17.1**, **Forge 37.1.1**, JDK **17**,
the exact supplied **OptiFine 1.17.1 HD U H1** JAR, and the supplied
**LCE Look 2.0.0** shaderpack. The existing Forge 37.0.103 / Java 16 port
baseline remains; the project builds on JDK 17 / Gradle 7.3.3 and emits
Java 16 class files.

## Maps, finite bounds, and spawn

`build joinAddonJar sourcesJar` passes **259 tests**, with zero failures,
errors, or skips. This includes nine new checks against original-instruction
fixtures for PS4 map centers, initial scale choice, finite chunk boundaries,
saved spawn selection, and following RNG output, plus native map/border
integration checks. The main and compatibility JARs are reobfuscated.

Both portable reference scripts were rerun against the pinned, SHA-256-checked
PS4 executable. Their output reproduces the supplied fixtures byte for byte:
160 map centers (including negative fractional coordinates and half ties),
two initial-map branches, 160 finite chunk-boundary cases, and 24 controlled
spawn-selection/walk cases. These are isolated instruction executions with
controlled biome and ground inputs, not complete-world captures from a PS4.

The finite border remains at the already correct origin-centered coordinates
for all seeds. Only Legacy Overworld map data and spawn are changed. The supplied
map-image inset, coordinate placement, paper, and texture code are byte-identical
to the 2.3.1 source. A separately documented Java one-handed-pose adapter prevents
clipping below 16:9 without changing the paper layout.

The final production main and compatibility JARs pass native integrated-server
checks in Classic (seed 123456789), Small (0), Medium (-987654321), Large
(82958734), Default (123456789), and Flat (123456789) worlds. Across those six
worlds, 150 scale/coordinate creation cases pass. Further checks cover first and
later maps, fractional negative positions, source-centered enlargement, source
preservation, locking, four border collisions, positive discovered terrain inside
all four finite edges, and untouched pixels outside them. Nether and End keep
their native border sizes. Saved-spawn integration and player landing radius pass
for every Legacy size. These spawn coordinates are Java-port results, not PS4
complete-world captures.

Sixteen actual gameplay frames were captured and visually reviewed: 1280×720,
1920×1080, 2560×1440, 3840×2160, 1024×768, 1600×1000, 1512×982, and 3024×1964,
each with H1 shaders OFF and LCE Look enabled. The one-handed map is complete
at 4:3 and retains its inset and coordinate border. The last two dimensions
exercise MacBook-like geometry; they do not test physical Retina pixel scaling.
All captures wait until native loading/overlays have completed.

Early native checks caught a missing parameter in the viewport-hook descriptor;
it was fixed before the final run. Early captures also exposed that a fixed-delay
QA capture could record a loading overlay, so final capture waits for completed
loading and an actual rendered gameplay frame. The private driver, game files,
OptiFine, saved worlds, PS4 executable, and captures are excluded from downloads.

Native runtime results are recorded in `docs/NATIVE-232-RESULTS.json`. See
[implementation evidence and limits](docs/PS4-MAPS-BOUNDS-SPAWN.md) for the exact
translation scope. Complete PS4 final-terrain and all-seed spawn equality is
not established. Physical Mac Retina hardware, hardware GPUs, and a running
PS4 console were not available for this verification.

The following sections record prior release checks and are retained as history.

## 2.3.1 dye materials and banner selector regression

The complete automated suite passes **250 tests**. Updated PS4 crafting tests
require exactly 16 distinct modern dye items in both color orders. Former dye
materials are rejected for banner plans, wool/bed recoloring and native firework
star/fade recipes. Modern dyes retain existing item names and explosion data.

The selector fix changes render order: draw the row first, then the focused
selector and its item models. Original artwork, slot pitch and transforms are
unchanged. Native QA checks 72 actual border samples per runtime: the first,
middle and last columns at 720p, 1080p, 1440p, 4K, 16:10 and 4:3. A before/after
frame comparison confirms the adjacent square no longer paints over the frame.

Native requests also bypass the selectors to verify 28 obsolete-material dye
requests are rejected without consuming items. The four ordinary recipes that
turn those materials into separate dye items still succeed. Updated full banner,
firework and dye transactions pass with modern dye ingredients.

The 2.3.0 results below describe that release; its old dye aliases are removed
in 2.3.1. Updated native checks use modern black, blue and white dye items.

## 2.3.0 PS4 banner, fireworks and dye crafting

The complete automated suite passes **248 tests**, including 17 new PS4 crafting
checks. These cover source pattern groups/rows, personal/table restrictions,
exact multi-layer allocation and shortages, existing banner NBT, six-layer limits,
invalid patterns/colors/dye material requests, material variants, native firework
shape/fade/flight data, color mixing, wool/bed recoloring, original palette order,
menu transforms and imported artwork channels.

Native QA uses the production JAR on Forge 37.1.1 / Java 17 / OptiFine H1 with
LCE Look 2.0.0, and Forge 37.0.103 / Java 16 without OptiFine. It checks:

- Creating and copying banners; selecting a banner before editing; staging and
  undoing across groups; a six-layer cap; exact ink/material consumption; returning
  to the base group after creating; and full-inventory rollback with no lost items.
- A staged red/red/blue star, preserving the first color when the selector changes;
  one shape, unique glowstone/diamond effects, reverse-order removal, exact
  explosion NBT, fade preservation, three-gunpowder maximum and three-rocket output.
- Recoloring named wool, beds and shulker boxes through the lapis alias, preserving
  item NBT; same-color rejection; and dyeing leather chestplates and horse armor.
- No stale crafting sound on side-tab switches and no controller cursor in these menus.
- Real rendered frames at 1280×720, 1920×1080, 2560×1440, 3840×2160,
  1600×1000 and 1024×768, plus a successful personal 2×2 material-pattern craft.

The portable extraction script reproduces every hash in all 39 crafting resource
files. Release checks verify archive CRCs, Java 16 bytecode and the absence of QA,
Minecraft, OptiFine and PS4 executable/media files in the downloads. These are
source-derived and native Java checks on Linux software OpenGL, not measurements
from a running PS4 console or physical controller hardware. See
[PS4 crafting provenance](docs/PS4-CRAFTING.md) for platform adaptations.

## Automated checks

`gradle build joinAddonJar` completes compilation, Mixin annotation processing, JAR
reobfuscation, add-on reobfuscation and **231 tests**. Tests cover the retained
layout/input/asset regressions, native-registry filtering of the supplied
Legacy4J item data, and world-origin/difficulty/hardcore policy persistence
and migration. They do not measure pixel equivalence with a running console.

## 2.2.1 offhand maps and world options

The new seed-policy regression covers a blank enabled Legacy seed, the checkbox
being off, other world types, and explicit seeds including zero, negative values,
text and whitespace. The complete suite passes with 231 tests and no failures,
errors or skips. Both mod and add-on are reobfuscated successfully.

Native Forge 37.1.1 / Java 17 / OptiFine H1 reproduced an offhand map sampling the
item atlas when the main hand held a block or sword with LCE Look enabled. The
final alpha-state flush guard renders the expected test map with a stone block,
diamond sword, torch or shield, an empty main hand, maps in both hands, and a
two-handed main-hand map. Shaders OFF also renders correctly. The faint blank-map
checker remains confined to its inset image and the coordinate paper remains
plain. LCE Look stays at 2.0.0; this fix is in the mod's OptiFine bridge.

Native 1080p and 720p captures show World Type, World Size and Biome Scale stacked
with matching widths, inside the existing frame. Controller left/right changes
each slider and Cross/A is consumed without changing it. Find Balanced Seed uses
the existing original checkbox/tick sprites. An injected controller Cross press
toggles it without opening a search. Its preference and manual WUBZY/0 seeds
survive leaving and reopening the options; explicit seeds disable the checkbox.
Clearing a manual seed restores the previous checkbox preference.

With a blank seed, Create New World opens the asynchronous search. Cancel returns
to creation without launching. A subsequent completed search creates a Classic,
Small-biome Legacy world with recorded seed 3321319428232652063, present in the
saved generator codec. The chosen seed updates both the input and the backing
creation model before the parent screen is reinitialized.

A final Forge 37.0.103 / Java 16 run without OptiFine also renders the offhand
map correctly beside a stone block and opens the stacked options with a working
checkbox. The optional bridge resolves to a no-op when OptiFine is absent.

These checks use Linux/Mesa software rendering and injected controller samples.
They do not replace physical-controller, hardware-GPU or console comparisons.
The native fixture, saved worlds and OptiFine/game binaries are excluded from
all release archives. Source references are in MAP-WORLD-OPTIONS-221-REFERENCE.json.

## 2.2.0 ocean boundary, biomes, structures and seed options

The thirteen tests added since 2.1.0 cover Medium/Large biome-scale reference vectors,
original balanced-seed categories/scores, finite structure candidates, original
mineshaft graphs, original template hashes/loading, captured village processors,
asymmetric desert/grass surface columns, jigsaw replacement states and shipwrecks.
Exact native comparisons include 24 scale rectangles, six balanced predicates,
36 candidate cases, 546 mineshaft pieces across three graphs and four shipwreck
constructor choice cases. All 450 original Overworld templates load, and every
one of their 41 distinct jigsaw replacement strings parses under 1.17.1. Explicit
regressions cover old wall booleans and the original truncated savanna road name.

Native Forge 37.1.1 / Java 17 / OptiFine H1 checks create a Classic world with
seed 123456789 and a Large world with Medium biome scale and recorded seed
3530508963576622004. Across the two worlds, all thirteen supported structure
kinds produce custom Ps4StructureStart/piece records. Full village footprints and
a full monument footprint generate. A 92-piece village reloads successfully;
Medium biome scale and the optional client biome profile survive a save/reload.
The outside Classic chunk retains exactly 13,824 stone blocks and 2,304 water
blocks, with no bedrock. The client renders that ocean edge without a striped
wall, while the server retains finite collision bounds. Controller sliders clamp
and consume Cross/A, and the asynchronous balanced search returns a seed.

Shipwreck regression tests use WUBZY (numeric seed 82944867), Large world size,
Small biome scale. Nine previously hovering cliff-edge starts now have hull
origins below sea level. Fresh native Forge-only / Java 16 generation checks
include the reported vicinity and a cliff regression: the latter's hull origin
moves from Y=68 to Y=49 while its original mast remains above the waterline.
Ship RNG choices match the original PS4 ship-specific region seed.
The final Forge-only / Java 16 client saves and reloads the WUBZY world,
receives its Legacy profile again, then loads an existing non-Legacy flat world
with the profile cleared. Sampling the
actual rotated footprint is explicitly recorded as a target-runtime correction.

A dedicated Forge 37.0.103 / Java 16 server starts with the matching add-on and
persists seed 0, Small world size, Large biome scale and structures disabled.
Its Overworld border is 1,024; Nether and End borders are 59,999,968. A modded H1
client joins and receives the finite biome profile. A pure vanilla 1.17.1 client
also joins and renders the terrain without any client mod. These are local,
offline authentication tests. Vanilla clients keep their native border/tint
rendering; hiding the wall and PS4 tint lookup require the client mod.

Early native QA caught and corrected a biome-hook descriptor mismatch and an
incompatible LevelRenderer Mixin route on Forge 37.1.1 / Java 17. The final
wall-draw hook uses a narrow Forge ASM transformation and does not alter border
physics or rendering alpha. Harness-only mistakes involving command-centred
coordinates and a shortcut's seed-zero handling were also corrected. They do not
change the normal Create World seed handling. No private QA fixture is shipped.

Full final-block equality with PS4 is not claimed. Procedural block placement,
loot, mobs and some decoration still use target-runtime adapters. Hardware GPU,
physical controller, macOS and console-side visual comparisons were not run for
this update. See the generation notes for the precise boundary of the port.

## 2.1.0 finite Legacy Overworld

Seventeen added tests cover the recovered PS4 world-generation components and
native 1.17.1 integration. JSON vectors come from isolated calls to the original
unencrypted PS4 routines at the pinned mcps4 revision. Exact comparisons cover
RNG/permutations, point and octave noise, 25 biome/Voronoi rectangles, ten complete
finite biome maps (2,601,472 cells), 6,800 density samples, 4,096 surface-noise
samples, 24 default surface columns plus their next RNG output, and 20 single-
stronghold positions across five seeds and all four world sizes. Integration
checks cover generator/biome codecs, explicit seed zero, size bounds, column/
height agreement, native Nether/End preservation and world-space lava lighting.
See `docs/LEGACY-WORLDGEN.md` and `WORLDGEN-PS4-REFERENCE.json` for exact coverage
and the Java adaptations; full PS4 seed-to-block equality is not claimed.

Native Forge 37.1.1 / Java 17 / OptiFine H1 checks use the final production JAR.
A Classic world with seed 123456789 loads with an 864-block Overworld border,
native unbounded Nether/End generators, and the expected located stronghold.
An edge chunk has the finite ocean falloff. The console Create World button
creates a Small world with numeric seed zero; saving and reopening preserve its
generator, size and seed. Recreate retains the Classic preset. Simulated controller
left/right reaches all four size choices, clamps at both ends, and Cross/A does
not accidentally advance the size.

A reload initially failed an overly strict full-block hash assertion: ordinary
water simulation added 27 water blocks while stone, bedrock, lava and terrain
height stayed unchanged. The follow-up persistence check compares solid terrain
separately from fluid updates. Early private launcher checks also exposed test
harness ordering errors (loading before the intro completed, and directly loading
a second world without disconnecting); these were corrected in the private driver.
They did not require production changes.

The final Forge 37.0.103 / Java 16 client without OptiFine also opens the saved
Small world, saves and reloads with an identical solid-terrain hash and preserved
seed/size/codecs, then opens an existing non-Legacy flat world successfully.

A dedicated Forge 37.0.103 / Java 16 server boots with `level-type=legacy`, Small
size and seed zero, with the 2.1.0 compatibility add-on. The server reports border
1024 and the expected stronghold. A pure vanilla 1.17.1 client joins and renders
the generated terrain with no client mod installed. Offline local testing does
not cover account authentication or public-server deployment.

All native checks use Linux/Mesa. Private launchers, analysis executables, QA
fixtures, saved worlds and captures are excluded from the release. No PS4 binary,
Minecraft JAR or OptiFine JAR is distributed in the mod or source archive.

## 2.0.18 maps, multiplayer sleep, bucket prompts, intro and paper doll

Eighteen added regressions cover the 1.11.2 unexplored-map alpha pattern, LCE
paper-doll view constants/body cancellation, bucket source eligibility, older
profile shield migration and persistent later opt-out, original sleep message
wording/new-sleeper tracking, and scene-owned intro confirmations. Intro tests
cover each logo, held input, loading readiness, complete-intro skipping, release
ownership and the original timeline/fades.

Native checks use the final production JAR on Forge 37.1.1 / Java 17 / OptiFine
H1 with the published LCE Look 2.0.0 shader files. A private fixture delays the
native resource-completion notification to inspect the original rating/Mojang
fades and full-opacity 4J hold. After actual loading completes, Cross skips from
the rating card to the main menu. Holding it there cannot activate Play; releasing
and pressing again opens the Play screen. Final pre-swap captures record the
actual presented frames, after queued startup/gamma drawing.

Native bucket checks cover empty air, water/lava sources, flowing water, adult
and baby cows, filled-bucket air and block placement, fish and axolotls. The
prompts use the native reach/ray trace without performing a pickup or placement.
Paper-doll probes at four world yaws record the aligned head/body and fixed
pitch during drawing, followed by restoration of the player's world angles.
Held-map captures inspect plain paper margins, a confined checker region and a
known discovered region. The map-only blend pass retains the old faint alpha
levels that the 1.17 text shader otherwise discards. OptiFine's emulated alpha
state is saved and restored around the same pass. The bucket, four-yaw pose and
held-map checks also pass on Forge 37.0.103 / Java 16 without OptiFine.

Multiplayer sleep checks use a real native bed interaction and a second private
ServerPlayer with an instrumented connection in an integrated world. The sleeper
receives the original self message; the observer receives the original named
message. Once everyone is sleeping, no Java counter/skipping announcement is
added. This is an in-process multiplayer simulation, not a second network client.
A dedicated Java 16 Forge server also boots with the mod and matching join add-on.
Source-format sleep messages require the mod on the server; custom non-100%
sleep rules retain native Java text and time progression remains native.

All captures use Linux/Mesa and simulated controller input. These checks do not
claim pixel identity with a running LCE console, hardware-GPU coverage or a
physical-controller test. Private fixtures, captured frames and saved QA worlds
are excluded from the release. `FEATURES-218-REFERENCE.json` records the pinned
C++ sources, original paper asset, 1.11.2 checker formula and Java adaptations.

## 2.0.17 sleep warnings and full-width chat backgrounds

Six new regressions cover failed-sleep Component keys, unrelated action-bar and
chat messages, the original HUD shape colour with opacity preserved, and both
viewport edges across GUI widths, fractional positions and chat scales.

A native 2.0.16 baseline reproduces the reported problems: a daytime bed warning
exists only in the action bar, and chat backgrounds span X=12..340 in a 640-unit
viewport. With 2.0.17, real server-validated bed interactions deliver the daytime
and nearby-monster warnings into native chat history with their original
translation keys. Both warning backgrounds span X=0..640 with the original
HUD fill RGB 51 and default alpha 127.

Granting a real Stone Age advancement produces the native announcement in chat
with the same full-width background. Native checks cover chat scale 0.5/0.75/1,
reduced chat width, 1280x720, 1440x900 (16:10), 1024x768 (4:3), and focused chat.
Turning console HUD off retains Java's native bed-message path. Normal sleeping
at night and waking through the console sleep screen still work.

These checks use Forge 37.1.1 / Java 17 / OptiFine H1 with Linux/Mesa. Captures
come from the final pre-swap framebuffer; private probes record the actual chat
rectangle after its native pose transform. They do not compare against a running
LCE console or physical hardware. Private fixtures and QA worlds are excluded
from downloads. `HUD-MESSAGES-REFERENCE.json` records the original C++ path and
active HUD720/1080 movie background shapes, colour, transforms and hashes.
Both failed-sleep warnings and full-width backgrounds at reduced chat scale also
pass on Forge 37.0.103 / Java 16 without OptiFine.

## 2.0.16 multiplayer joining and server text

Six new regressions verify styled/sibling server Components, null and empty
status placeholders, the unchanged source timer timeline, its 30 fps loop,
and uniform square size at 720p, 1080p, 1440p, 4K, 16:10 and 4:3.

Native Forge 37.1.1 / Java 17 / OptiFine H1 checks use a real TCP status/login
endpoint and the actual Join Game button. Version and Players render readable
text; login renders the console loading view. Mouse, Escape and simulated
controller Circle cancellation each invoke the native abort callback and close
the socket. Connection refused and unknown-host failures return to the native
disconnect screen with no loading cover. A native ConnectScreen fixture holds
the pre-socket phase for visual inspection of the original timer animation.
The same status, cancellation and failure checks also pass on Forge 37.0.103 /
Java 16 without OptiFine.

A successful join to a dedicated Forge 37.0.103 / Java 16 server with the
production 2.0.16 mod and compatibility add-on reaches the world and retires the
loading cover from both client profiles. A resized 1024x768 native connection
view retains its timer, hidden cursor and working footer mouse cancellation.
Login status remains native; phases without an aggregate
percentage do not invent progress or delay fast connections. Pre-swap framebuffer
captures inspect the join details, login view and timer. The existing native
terrain, save/reload, shuffled-tip and startup presentation are retained.

These checks use Linux/Mesa and a private simulated controller. They do not
measure equivalence against a running LCE console or a physical controller.
No private protocol fixture, QA mod or saved world is included in the release.

## 2.0.15 cursor coordinates and standard aspect profile

Thirteen regressions bring the suite to 170 tests. Coordinate checks cover 16:9,
16:10, 4:3, Retina M1-size framebuffers (3024x1964 and 3456x2234), independently
changing X/Y density, exact Y round trips, legacy bottom-origin compatibility,
outside-window item dropping coordinates and zero-size transitions. Layout checks
cover the 640x480 profile, uniform fallback bounds, original wide anchors, safe
loading bounds, integer GUI factors, the source 0.6 SD cursor multiplier, settings
row visibility and HUD safe bounds at small manual scales.

A baseline 2.0.14 native run reproduces the rendering mismatch while the controller
is active: under delayed native coordinates the virtual pointer is over slot 9,
but rendering uses (0,0) and reports no hovered slot. The corrected run keeps the
highlight, carried-item render position and actual clicked slot aligned under the
same simulated delay and 2x Retina metrics.

Native checks exercise pickup/place-all, splitting/place-one, quick move, a real
Forge mouse-input handoff, inventory resizing while a slot is hovered, chest and
furnace transfers, creative and single-preview crafting. Title, settings, world
list and creation buttons remain within 640x480, 1024x768 and 1280x960 windows.
Captures inspect the retained original artwork. The runtime profiles are Forge
37.1.1 / Java 17 / OptiFine H1 and Forge 37.0.103 / Java 16 without OptiFine;
the final standard-pointer step check measures 0.6 of the wide-profile movement.

These are Linux/Mesa checks with window-point/framebuffer differences and delayed
callbacks simulated. They do not replace testing on an M1 Pro MacBook, a physical
controller or macOS. Private fixtures, simulated metrics and saved QA worlds are
excluded from downloadable artifacts.

## 2.0.14 startup randomization

Three regressions bring the suite to 157 tests. A controlled source-style set of
random signed sort values verifies the resulting tip order, including both
integer extremes. Thirty-two deterministic startup seeds produce complete
permutations and different orders. A counting random source verifies that
scene changes, timers and wraparound reuse the startup order without drawing
more random keys. The five scene/timer tests remain, with an explicit ordered
random-key source where their assertions concern cursor and timing behavior.

The native Forge 37.1.1 / Java 17 / OptiFine H1 probe records the actual startup
permutation and verifies that short loads, the seven-second transition, world
entry, OptiFine reloads and saving all consume that permutation in order. Native
fixtures and captures remain private and are excluded from release artifacts.

## 2.0.13 tip sequence

Five scene-sequence regressions replace the old elapsed-time-only tip-index test,
bringing the suite to 154 tests. They cover eight short loads consuming all eight
tips before wrapping, exact seven-second rotation during a long load, continuing
after the last displayed tip, idle time and timer isolation, adjacent scene starts,
and empty/single-tip lists. The implementation follows the app-wide cursor in
`CMinecraftApp::GetNextTip` and the scene-local timer in `UIScene_FullscreenProgress`.

The new native regression uses Forge 37.1.1 / Java 17 / OptiFine H1.
It exercises repeated short loading screens, a held loading screen
through its timer boundary, actual world entry/exit and OptiFine reloads. The private
pre-swap probe records the selected original tip index and text alongside the
retained progress/cursor/last-frame checks. Native fixtures remain excluded from
release artifacts.

## 2.0.12 loading screens

Five new regressions bring the suite to 150 tests. They verify the authored 720p
progress/tip bounds, proportional 720p–4K layout and wide-screen centering, fixed
texture scale while progress is clipped, native-value clamping, the seven-second
tip timer, and original bitmap dimensions and SHA-256 hashes.

Native checks use Forge 37.1.1 / Java 17 / OptiFine H1 with LCE Look 2.0.0,
and Forge 37.0.103 / Java 16 without OptiFine. A private pre-swap probe records
loading state, native progress, cursor visibility and background samples. The
checks load a real saved world, exercise a native ProgressScreen at 37%, trigger
OptiFine's actual Anisotropic Filtering reload, then use Pause → Exit Game →
Confirm to save and leave. The Forge-only profile uses a native resource reload.
Both profiles must return to their settings/title screens and retain the cover
on the reload's final frame after the native overlay removes itself. Loading
captures inspect the original bar, tip-panel borders and retained Java logo.
No full solid-red Mojang background is present in sampled loading frames; the
native cursor stays hidden. Native save phases expose no aggregate percentage,
so they retain an empty bar rather than reporting simulated progress.

These Linux/Mesa captures and the layout/asset tests do not measure pixel
identity with a console or hardware-GPU behavior. Native QA fixtures and
captures remain private and are excluded from the release.

## 2.0.11 trading display and stock

Seven new unit regressions bring the suite to 145 tests. They cover the eight-cell
minimum, unaffordable offers remaining visible, exhausted leading offers compacting,
original server-index retention, restock order and selector columns, all-exhausted state,
scrolling beyond eight, the source header wording and original bitmap dimensions.

Native checks open a real MerchantMenu, capture the actual heading and enchanted
book details, verify the original warning bitmap in the rendered image, and inspect
server inventory after trading. With raw offer 0 exhausted, the first visible offer
still buys raw offer 1's bread. Restocks restore the original book/bread/torch order.
All exhausted leaves eight empty cells with no trade target. Ten offers scroll horizontally;
after filtering raw offers 0 and 4, the selected raw offer 9 still buys an ender pearl.
The controller cursor remains disabled and inventory cells cannot take focus.

Checks use Forge 37.1.1 / Java 17 / OptiFine H1 with LCE Look 2.0.0 and Forge
37.0.103 / Java 16 without OptiFine. Captures use Linux/Mesa; these do not claim
physical-controller calibration or console pixel equivalence. QA fixtures and saved
worlds remain private and are excluded from downloadable artifacts.

## 2.0.10 creative controls, banner/dye crafting and loom

The suite now has 138 tests. New regressions cover creative single-item pickup,
whole-stack limits (including 16-item stacks), merging before empty slots,
full-hotbar replacement, preserved tags, colored-bed dyeing and malformed
extra-ingredient rejection. The grouping regression now requires honey bottles
and base banners to be hidden from ordinary crafting.

Native Forge checks exercise actual menu actions and inspect server inventory:
Cross adds one then merges a second item, Triangle adds a full stack, Square
clears all nine slots, picking up a hotbar stack works, and outside clicks drop
all or one carried item. Recipe grouping leaves 16 wool/plank bed creation
recipes, with no ordinary banner or honey-bottle outputs. Creating a banner
consumes six wool and a stick. Dye Item consumes a red bed and blue dye and
creates one blue bed. The native sound-event probe hears the successful craft
cue once and does not hear it again on crafting group changes.

Loom checks cover empty-slot artwork, normal pattern selection/result pickup,
one-dye consumption, a flower template and rejection at six applied patterns.
Visual captures inspect the corrected pattern thumbnails and large preview.
A white-pixel regression checks the full-bright preview with shaders enabled;
the GUI draw uses its own texturing pass without mutating world materials.
The runtime profiles are Forge 37.1.1 / Java 17 / OptiFine H1 and Forge
37.0.103 / Java 16 without OptiFine, on Linux/Mesa. These checks do not constitute
physical-controller or console pixel-equivalence testing. The private fixture
and captures are excluded from release archives.

## 2.0.9 LCE movement-axis deadzone

Seven new regressions bring the suite to 131 tests. They cover the source
10000/32767 boundary and first representable step, independent axis scaling,
small lateral deflections while pushing forward, full travel and SDL signs,
the narrower sprint cone, actual PadState construction, and neutral startup
states clearing both dedicated movement axes. Menu/look axis tests remain.

The source initialization in Orbis_Minecraft.cpp and the supplied 4J_Input.a
implementation are checked together; the latter is inspected with symbol-aware
disassembly. The normalization is per axis rather than radial. The game applies
the unchanged 0.8 sprint threshold to that processed forward value.

Native comparisons load the released 2.0.8 JAR and rebuilt 2.0.9 JAR using raw
ControllerState samples passed through the actual PadState constructor,
ConsoleMovement and LocalPlayer.aiStep. Small +/-0.2 lateral deflections during
full forward travel have nonzero sideways movement in 2.0.8 and zero in 2.0.9.
A 32-degree full-circle input remains sprinting in the old build but becomes
walking with the source filter, retaining sideways input. A 30-degree input
still sprints. Camera/menu samples remain nonzero at the same small deflection,
and disabling Legacy Movement restores the old radial gameplay response.

Checks use Forge 37.1.1 / Java 17 / OptiFine H1, plus Forge 37.0.103 / Java 16
without OptiFine. These are injected raw sample tests on Linux/Mesa, not physical
controller calibration. The fixture and the original input-library binary are
not distributed in the release.

## 2.0.8 movement, quantity warnings and pack priority

The build passes 124 unit regressions. New cases cover the exact 0.8 sprint
boundary, two/four string, split inventory stacks, blank grid cells, strict
versus flexible ingredients, repeated gunpowder and exact special-crafting tags.
Pack selection tests cover append priority, removing one pack, reselecting it
above the others, reopening an ordered selection and clearing the user stack.

Native Forge 37.1.1 / Java 17 / OptiFine H1 checks execute actual LocalPlayer.aiStep:
sideways input remains 0.4 at forward 0.916515 while sprinting; sideways +/-0.8
with forward 0.6 switches to walking; exactly 0.8 forward keeps sprinting; pure
sideways input walks. Disabling Legacy Sprinting retains Java's continued sprint
at the tested 0.6 forward impulse. The same direction and shortage smoke checks
run with Forge 37.0.103 / Java 16 without OptiFine.

The actual white_wool_from_string recipe, with two string in inventory, produces
[red, red, clear, clear] in both personal and table previews. Captures verify the
red squares under the string sprites. A server craft request is rejected with
two string; adding two more clears all warnings and crafts one wool, consuming
exactly four string. Native recipe inputs and server validation stay in use.

Two private format-7 pack fixtures share one text resource; Alpha also supplies
a unique resource. Selecting Alpha then Beta keeps both selected, loads Beta's
override and still loads Alpha's unique resource. Reopening the settings list
preserves the order. Removing Alpha keeps Beta; reselecting Alpha produces
[Beta, Alpha] and loads Alpha's override. Default clears the user list while
retaining vanilla and mod_resources. Selection marks and checkboxes are inspected
in native captures. No fixture packs or QA harness code are shipped.

## 2.0.7 initial native input focus

The 2.0.6 production JAR reproduces the input failure with another X11 window
holding focus before launch: the game is fullscreen and Minecraft reports its
window active, but GLFW_FOCUSED is false and real Down/Enter events do not reach
the menu. The prior startup tests explicitly focused the game before sending
F11; they verified sizing but did not catch this missing initial handoff.

The fix explicitly calls GLFW.glfwFocusWindow after the prepared startup window
is shown. It runs only inside the existing one-shot reveal guard. Controller
polling continues to use the actual GLFW focus state.

Cold starts of the production 2.0.7 JAR use Forge 37.1.1 / Java 17 / OptiFine H1,
and Forge 37.0.103 / Java 16 without OptiFine. Another native window holds input
focus before each launch. The game remains hidden until the intro frame is
ready, then becomes visible, fullscreen and natively focused. Actual Down/Enter
keyboard events open Help & Options without a click, Alt-Tab or a game-focus
helper. Deliberately switching focus away afterward leaves it with the other
window, and keyboard input no longer changes the game menu. Regaining focus
and toggling F11 preserves the 1280x720 launcher size and 2560x1440 fullscreen.
The original 113 unit regressions pass. These native checks use Linux/Mesa;
Windows foreground policy and physical controllers need hardware verification.
The focus-control window, event observer and private harness are not shipped.

## 2.0.6 startup window presentation

A native GLFW observer reproduces the old 2.0.5 behavior: a visible 1280x720
window at (0,0), followed by a visible 2560x1440 window, both before the intro
has drawn its first frame. The constructor and first swap are observed directly;
checking only the title screen's fullscreen flag would miss this transition.

GLFW ignores the visibility hint when creating a fullscreen window. The fix
creates a hidden windowed context at the preferred fullscreen dimensions,
keeps the original launcher dimensions for windowed mode, and enters native
fullscreen after the first rendered intro frame has been swapped. Both Forge's
handoff and OptiFine H1's reflected handoff/native fallback are covered.

Cold-start checks use the packaged production JAR with Forge 37.1.1 / Java 17 /
OptiFine H1, and Forge 37.0.103 / Java 16 without OptiFine. The native window
is hidden at creation, and its first visible observation is fullscreen with a
completed intro frame. Rating, Mojang and 4J stages still appear, with rating
music continuing on the same native channel into the title. F11 restores the
1280x720 launcher dimensions, then returns to 2560x1440 fullscreen. A later
resource reload stays visible without replaying the intro. These checks use
Linux/Mesa; Windows driver-specific presentation still needs user confirmation.
The private GLFW observer, launcher and captures are excluded from downloads.

## 2.0.5 crafting-family regression

Eight new regressions cover blackstone button placement, Nether fences/gates,
blackstone-brick aliases, cut/smooth sandstone slabs, all native magenta dye
recipe names, output-based dye matching, dedicated Fireworks ownership and
honey bottle placement in Food. The imported Legacy4J JSON remains byte-for-byte
identical to the pinned reference. The newer supplied Legacy4J list confirms
blackstone buttons after stone, Nether fences/gates after the Overworld woods,
and dye matching by output item. Honey is a later-version adaptation: the
crafting reference omits it, while its creative reference puts it last in Food.

The packaged production JAR runs with Forge 37.1.1 / Java 17 / OptiFine H1.
The live recipe manager yields 697 regular displayed entries; comparison with
the earlier 698-entry catalog confirms that only the duplicated ordinary rocket
entry moved out of the regular columns. All 47 slab outputs, 44 stairs, 9 fences,
8 gates, 21 walls and 10 buttons are in their reference families. No vanilla
fallback columns remain. All five magenta recipes share Decoration's dye column;
honey bottles occupy their own Food column after bread. Both table and personal
crafting screens open with the corrected grouping.

The Fireworks side tab still builds the native Flight 1 recipe and, with one
paper and one gunpowder, crafts three rockets through the normal server request.
Ingredients are consumed and the three rockets appear in inventory. The private
fixture, recipe dumps and QA world remain outside the release. The shaderpack
and interface artwork are unchanged in this update.

## 2.0.4 XP pickup regression

The 2.0.3 production JAR reproduces black coal-ore debris with an XP pickup.
A native particle-pass observer records the pickup (CUSTOM) before TERRAIN_SHEET;
the lightmap texture falls from 7 to 0 before terrain particles draw. Native
Forge 37.1.1 iterates an identity-map key set rather than the original render
order, so this depends on the active particle types and their iteration order.

The fix uses the engine's own RENDER_ORDER for ordinary sheets, retains custom
types and draws the entity-based CUSTOM pass last. Two regression tests also
cover custom types with equal values remaining distinct. Native captures compare
coal debris before and during an XP pickup, including the formerly bad order,
with shaders OFF and with the released LCE Look 2.0.0 pack. The terrain pass
keeps its nonzero lightmap during collection. The same scene passes with
Forge alone, without OptiFine. The controlled pickup is kept
alive by the private fixture for capture; its renderer is the native XP renderer.
That fixture, render-state observer and test world are excluded from downloads.

## Retained 2.0.3 Java 17 and OptiFine repair checks

The uploaded user log identified Java 17 / Forge 37.1.1 / OptiFine H1. The
unchanged 2.0.2 release reproduces the same `PermittedSubclasses requires ASM9`
warning and skips its LoadingOverlay mixin under Java 17. That target is now
removed. The replacement uses Minecraft.setOverlay and the main render loop;
its draw is queued after the native framebuffer blit and uses Minecraft's
GUI projection before the window swaps. The native loading lifecycle and
error handling continue to run.

Cold-start captures cover the original rating/Mojang/4J artwork, with no
native red loading frames. Early OpenAL observations confirm music at the
rating while sound definitions are still loading, and the same instance and
channel at the title. A test explicitly starts Java menu music with LCE music
disabled, then enables LCE music and checks that only its channel remains.
The default Java music scheduler is bypassed while LCE owns playback.
The independent music pump follows the native stopped-channel check; eight
short OGG streams reach EOF and release their channels without refilling a
finished stream.

OptiFine H1's patched ParticleEngine is inspected directly: both its Forge
sprite-update branches construct a TerrainParticle but omit enqueueing it.
The compatibility fix adds that same particle after the successful sprite
update, scoped to H1 and that reflected method. Native checks confirm 64 debris particles for a full stone-block break, one
mining-hit particle and 150 separately spawned flame particles with Shaders
OFF. Presented-frame captures show the debris. The active shader-pack label
is checked both by its runtime green color and a screenshot. The unmodified H1 and Minecraft binaries are not redistributed.

The earlier Forge 37.0.103 / Java 16 baseline remains supported. Current
checks run on Forge 37.1.1 / Java 17 with H1, plus a Forge-only launch.

## Retained 2.0.1 native repair checks

The 2.0.1 startup timing has been superseded by the loading-phase checks above.
The retained first-run control migration saves Auto-Jump Off and tutorial step NONE.

Native menu checks cover brightness dragging, Mipmap Type changing in both
directions, Quality/Video navigation, shader selection, Old Lighting changes,
shader Gamma increments of 1%, mouse dragging, persistence on reopening and
returning through nested shader menus. Auto-Jump can be enabled in Control,
remains enabled on reopening and can be turned off again. Graphics and
Control screenshots confirm the original panel backgrounds.

New recipe regression checks use the actual 1.17.1 registry to place white
wool/recolors in one Decoration column and all candles in a separate column,
and exclude waxed copper outputs from the crafting list. The source listing
bytes are retained; these are compatibility mappings for changed IDs/tags.
Controller pitch tests cover up/down and the explicit inversion option.

The live recipe manager confirms 16 wool recipes and 17 candle recipes in
separate Decoration families, no waxed-copper output in any crafting tab,
and four crafting side tabs. Cartography remains accessible. A native player
turn confirms up/down signs and the inversion option. Explicitly enqueued
Java tutorial/recipe toasts stay suppressed; chat and advancement screenshots
confirm their dark text backgrounds and the retained notification panel.

The music continuity check follows the same SoundInstance, native channel and
OpenAL source from the title through actual world loading, Escape pause,
resume and return to the title. The source stays AL_PLAYING at each point.
Music buffer servicing runs on the native sound executor at 20 Hz independently
of blocked/paused game ticks; playlist choice and source timing are unchanged.
These audio checks use OpenAL's null output, rather than a physical speaker.

## Port baseline native game checks

The following workstation/map/compatibility checks were established for
2.0.0 and retained for the unchanged server mechanics. The 2.0.1 changes to
these paths are client-side grouping and menu integration; the add-on is
rebuilt with matching version metadata and retains its existing protocol.

The reobfuscated release mod was launched in a separate, real Forge 1.17.1
installation with H1, using Java 16 and Mesa software OpenGL on Linux.
The dedicated server also boots with the release mod and join add-on.

The optional add-on was checked with a real vanilla 1.17.1 client and a Forge
37.0.103 client whose mods folder was empty. Both join and open the normal
Java crafting-table screen. A matching Legacy Console + H1 client also joins
and crafts through the console interface: two log-to-planks transactions
produce eight planks and leave two of the original four logs on the server.
The add-on does not bypass another mod's channel or registry requirements.

Native menu transactions checked:

- Personal survival crafting: logs to planks, then repeated stick recipes.
- Crafting table: nine copper ingots to a copper block.
- Smithing: diamond sword and netherite ingot to a netherite sword.
- Stonecutter: stone to chiseled stone bricks, taking the real result.
- Loom: white banner, red dye and a selected pattern to a tagged banner.
- Grindstone: enchanted sword to an unenchanted sword, taking the result.
- Cartography: enlarging, naming and taking a map. Saved map data confirms
  scale 0 remains unchanged, scale 1 uses its fixed-grid center (576,576),
  and the test player's location (600,600) is inside the resulting map.
- Brewing: real block/menu opens with water, nether wart and blaze powder;
  the native brewing block entity retains its fuel requirement.

The creative catalog was inspected for deepslate, copper, amethyst, fireworks,
netherite and Elytra ordering. The title uses the installed 1.17.1 panorama.
Resolution checks exercise native framebuffers at 1280x720 / scale 2,
1920x1080 / scale 3, 2560x1440 / scale 4 and 3840x2160 / scale 6.

Shader programs load in the Overworld, Nether and End. The retained display
gamma bridge, held-map adaptation and original HUD resources are checked in
the same native runtime. See the shaderpack's TESTING.md for its scope.

## Limits

The private QA driver and test worlds are excluded from downloads. The tests
use a software renderer, keyboard/mouse and a development input fixture;
physical controllers, Windows drivers, hardware GPU performance, every
resource pack, and every OptiFine setting combination are not verified.
The mod is Forge-only; Fabric/Iris/OptiFabric execution is not validated.
Native recipe checks cover the examples above, not every 1.17.1 recipe.

Imported source/artwork provenance remains in the reference manifests and
THIRD-PARTY-NOTICES.md. The 1.11.2 baseline is kept separately.

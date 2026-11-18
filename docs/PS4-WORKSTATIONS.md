For 2.3.5, [PS4-UI-FIXES-235.md](PS4-UI-FIXES-235.md) supersedes the general
recipe grouping and adds beacon padding/netherite. The implementation below
describes the 2.3.4 release. The book draw function starts at `0x009e0b10`.

# PS4 workstation port — 2.3.4

This change uses **Nejirus/mcps4**, revision
`37e258085773f11599cf0ffe29119926769554b7`, as its implementation reference.
It does not use the LCE executable as the authority for these menus.
The input `eboot.bin` SHA-256 is
`c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8`.
Source addresses below are virtual addresses in that image. Decompiled control
flow is checked against instructions, especially where the decompiler joins
adjacent functions after a stack guard or PIC call.

## Code and assets

| Menu | Original routines / data | Java implementation |
| --- | --- | --- |
| General crafting | `0x9a8670` default Show All flag; `0x9ac010` input/filter; `0x9ad970` recipe filtering; `0x5a0740` recipe registration; block `+0x14c` and item `+0x28` family fields | `ConsoleCraftingScreen`, `ConsoleRecipeGroups` |
| Stonecutter | Normal crafting type 0, workstation context 2; original CraftingMenu scene and selectors | `ConsoleCraftingScreen`, `StonecuttingRecipes`, `ConsoleContainer.STONECUTTER` |
| Loom | Crafting type 5 setup at `0x9a9338`; `0x9b23c0` groups; BannerPattern static constructor `0x46fd0`; type-5 recipe registration at `0x4e1e0..0x4e299` | `ConsoleSpecialCrafting`, `Ps4BannerPatterns`, `Ps4BannerCrafting` |
| Cartography | `0xa148d0` six display modes; `0x7f6760` illustration visibility; `0xa14330` 30-character keyboard limit; `0xa14fc0` names/status; `0x161d20` result/rename logic; `0x162970` quick move | `Ps4CartographyScreen`, `CartographyMenuMixin`, `CartographyInputSlotMixin` |
| Enchanting | `0xa3c100` accepted menu click before packet; `0xa3c2b0` costs/tooltips; `0x9e17e0` changed-cost name updates; `0x9e1df0` random word selection; `0x9e1f90` 62-word pool; `0x9e0b30` book transforms | `Ps4EnchantingScreen`, `Ps4EnchantmentNames` |
| Brewing | `0xa01ce0` ranges 400/30/20; `0xa12830` progress/bubbles; `0x843d40` nonzero arrow inversion; `0x843d30`, `0x843e70`, `0x843f70` top/bottom/horizontal fill configuration | `ConsoleBrewingScreen`, `Ps4MenuLogic`, `Ps4UiArt` |
| Beacon | `0xa0e310` tiers and confirm state; `0xa0f9d0` selection; `0xa0ef50` payment icons; `0x9df160` four button frames; effect registrations `0x5a320` | `Ps4BeaconScreen`, `Ps4MenuLogic` |

The four inventory menus translate section transitions from `0xa12f70`,
`0xa3bed0`, `0xa146b0`, and `0xa0f5d0`, with the common position operation
`0x9a2610`. `Ps4MenuNavigation` retains those switches and slot tables.
Java's pointer starts the operation at the nearest displayed cell.

`ps4_workstations.json` records the original display lists, transforms, native
English labels, text-field colors, and source/conversion hashes. Its 182 sprite
entries and 27 nine-grid pieces are extracted from the original FUI resources.
BGRA-to-RGBA channel conversion makes Orbis bitmaps usable as Java textures;
no replacement artwork is drawn. The original 1920×1080 roots are brewing
(639,141), cartography (648,158), enchanting (637,218), and beacon (580,173).
Crafting/stonecutting/loom retain CraftingMenu1080's (496.5,170.5) root and the
existing extraction in `ps4_crafting.json`.

The cartography display uses the PS4 illustrations, rather than a Legacy4J map
preview. It accepts empty and filled maps in its first slot. Its rename result
uses Java map NBT and the synchronized server result slot. The server retains
Java's one-item take transaction and quick-move rules. This does not change map
margins, map grids, world bounds, or world generation.

## Loom and newer recipes

There are two native pattern vectors. The first contains the base plus 44
material/template registrations. The second contains **32 loom patterns** with
IDs 101..138 (gaps retained), the same original pattern hashes, and no paper
requirement. Their original type-5 recipes cost one banner and one dye. The
six original paper alternatives add their reusable template requirement.
Four of those paper items exist in Java 1.17; Java has no field-masoned or
bordure-indented paper item. Globe and piglin templates are explicitly marked
Java additions (`ps4_id = -1`). Loom and ordinary crafting pattern requests
are validated separately by the server. Staging reserves all required dyes,
preserves templates, and includes existing layers in the six-layer limit.

General recipe data recovers 419 literal result/category registrations, 195
explicit block-family stores, and 126 explicit item-family stores. Unknown
runtime-generated outputs or inherited fields are omitted from that extraction.
Java's flattened IDs, newer woods, copper, deepslate, candles, honey-related
recipes, and other 1.17 recipes are adapters around the recovered PS4 families.
The Java recipe manager supplies ingredients, outputs, remainders and modded
recipes. Dynamic or new recipes use declared fallback grouping and stable Java
IDs; **complete PS4 auxiliary-value ordering is not claimed**. The old
`crafting_legacy4j.json` file is no longer read by the general crafting screen.

Crafting, stonecutting and loom use server-validated inventory transactions.
The protocol is now **5** because menu context is transmitted explicitly.
Update the main mod and optional server Join Compatibility add-on together.
On servers without the main mod, the pre-existing native-menu fallbacks remain;
the PS4 inventory-funded stonecutter/loom require the mod on the server.

## Target adapters and limits

The existing in-game Java keyboard replaces the PS4 operating-system keyboard,
with the original map-name character limit. Minecraft 1.17.1 supplies item/block
models, fonts, localization, inventory and
network synchronization, enchantment generation, potion recipes, beacon world
effects, and map NBT. These are explicit Forge/Java adapters, not recovered PS4
implementations. The original book transforms use Java's book mesh and existing
opening/page animation. Native rune selection uses the translated shared PS4
random stream and original word pool; the host clock seeds that stream. Its
297×48 FUI viewport is retained. The unavailable texture-pack rune color table
(IDs `0x1c2..0x1c4`) uses the host game's rune palette. Ordinary label and XP-cost
colors come directly from the original FUI fields.

Display fitting is a Java adapter: uniformly fit the original authored scene,
then center it for narrower 4:3 and 16:10 displays. Minecraft GUI coordinates
already incorporate framebuffer/Retina scaling. This is not a claim that the
PS4 executable supported Apple displays. Physical Retina and controller hardware
are not available in the test environment.

World generation is unchanged from 2.3.3. This release does not claim full
PS4 gameplay or world-generation parity.

## Reproduce extraction and native comparisons

Use the pinned original repository inputs; the executable/media archive are not
included in this source distribution. Python dependencies are `capstone` and
`Pillow`. Extract the named original FUI resources and `languages.loc` from
`MediaOrbis.arc` into one directory, then run from the project root:

```sh
python docs/reference-tools/ps4/read_recipe_groups.py --eboot /path/eboot.bin --output src/main/resources/assets/legacyconsole/ps4_recipe_groups.json
python docs/reference-tools/ps4/read_pattern_constructors.py --eboot /path/eboot.bin --output docs/reference-tools/ps4/pattern-constructors-native.json
python docs/reference-tools/ps4/export_crafting.py --input-dir /path/fui --project-root .
python docs/reference-tools/ps4/export_workstations.py --input-dir /path/fui --project-root . --eboot /path/eboot.bin
python docs/reference-tools/ps4/native_workstations.py --eboot /path/eboot.bin --output src/test/resources/ps4/menu-native.json
```

The last command executes isolated original routines with bounded platform
callbacks. It captures 401 brew ticks and arrow fills, eight beacon button
states, 2,232 section transitions, and 84 enchantment names across seven seeds.
It never starts the game or platform SDK. Tests compare these outputs against
Java, validate all shipped sprite hashes and scene bounds, and exercise the
crafting/template transactions. Runtime results are recorded separately in
`NATIVE-234-UI-RESULTS.json` after the client checks complete.

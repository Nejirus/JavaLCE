# PS4 crafting information, tutorial glyphs and What's This — 2.3.8

Primary source is Nejirus/mcps4 revision
`37e258085773f11599cf0ffe29119926769554b7`, executable SHA-256
`c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8`.

## Original crafting behavior

`IUIScene_CraftingMenu::handleKeyDown` at `0x9ac010` handles normal crafting's
Square action in `0x9ac698..0x9ac6b8`: increment the field at `+0xe18` while it
is at most 1, otherwise return to 0. It then updates the multi-panel and footer.
The initial value is 0. The states are **Inventory (0), Description (1),
Ingredients (2)**. The source prompt hashes are `ae01f627` (Show Description),
`63d85f8f` (Show Ingredients), and `ccf37993` (Show Inventory).

The Orbis wrapper `0xa24580` passes that field to the original FUI controller
at `0x7f8d70`. Its actual visibility calls are captured in
`src/test/resources/ps4/crafting-info-native.json`: inventory, hotbar and its
label show only in 0; description shows only in 1; the five ingredient icons,
names and Ingredients label show only in 2. The source recipe count and names
come from `0x9b7930`; `0x9b5a20` updates the selected result name. The shared
normal crafting type covers stonecutting too. Square keeps its separate
remove-ingredient/layer action in the special crafting interfaces.

`Ps4CraftingInformation` translates the counter and unique-ingredient list.
`ConsoleCraftingScreen` retains the selected recipe and server transaction,
while changing only the right-hand information panel. Repeated cells become
one ingredient with its required count. Existing inventory allocation supplies
missing-item marks and keeps overlapping alternatives from spending one stack
twice. The original generic labels (e.g. Planks (any type)) are retained.
Flattened 1.17 planks/wool/slabs use the equivalent live item tags.

The original CraftingMenu1080.fui anchors remain: DescriptionText (548,439),
Ingredient1..5 (548,478/523/568/613/657), corresponding labels
(592,486/531/576/621/665), and IngredientsLabel (535.05,412). Ingredient text
uses the source small black label size 20; normal text uses size 30. Java font
wrapping and flattened recipe alternatives are platform adapters. No extra
upper-right recessed crafting panel is introduced.

`Item` registration `0x386070` stores the name/use-description pair at
`+0x68/+0x6c`. The new exporter recovers 230 explicit registrations and 218
English use-description bindings, including the original tool descriptions.
Blocks retain the PS4 block-description path from 2.3.7. New items can use
Legacy4J text from the same pinned revision already credited by the project.
No new description prose is authored for this release.

## Inventory action

The original PS4 common inventory guide states: “If you want more information
about an item, move the pointer over the item and press
{*CONTROLLER_ACTION_MENU_PAGEDOWN*} .” The source input is R2 and the native
footer string `2fccdffb` is “What's This?”. The existing Page Down/R2 adapter
opens TutorialPopup1080's upper-right information panel. The inventory now
advertises R2 when the hovered stack has a description. Automatic description
insertion into ordinary hover tooltips is removed. Standard names, enchantments
and item lore still come from the game's tooltip. Manual information works
when optional Hints is Off.

## Glyph selection

Tutorial input tokens now use the same `ControllerGlyphs.current()` selection,
`LceGlyphLayout` display lists and `ConsoleTheme.glyph` renderer as other mod
prompts. All original layers, transparent padding and texture paths are kept.
Explicit PS4, PS3, Xbox 360 and Xbox One settings override detection; Automatic
uses the connected controller name. The existing SD/HD family threshold is
retained. No replacement Unicode button shapes or newly drawn button art are
used. Keyboard instructions remain text with the current game bindings.

Invisible font spacing metrics reserve the exact inline image advance before
Minecraft wraps text. `Ps4TutorialText` renders the selected original layered
images into those spaces, including the sticks, triggers and bumpers. These
metrics and the Java font fitting are integration code, not PS4 assets.

## Reproduce

With Python capstone installed and the separately obtained original executable:

```
python docs/reference-tools/ps4/native_crafting_info.py --eboot /path/eboot.bin --output /tmp/crafting-info-native.json
python docs/reference-tools/ps4/export_item_descriptions.py --eboot /path/eboot.bin --english /path/english-full.json --output /tmp/ps4_item_descriptions.json
```

The first tool executes only the bounded original counter instructions and
FUI visibility function, with allocator/string reservation, lookup and Show
callbacks. It does not start the game or console services. LCE source was used
to cross-reference class/field meanings; the PS4 instructions, function calls,
strings and FUI coordinates are the authority for this implementation.
See `TESTING.md` and `NATIVE-238-INFORMATION-RESULTS.json` for validation scope.

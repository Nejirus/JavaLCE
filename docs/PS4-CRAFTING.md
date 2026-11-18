The 2.3.4 loom and general crafting changes are documented in
[PS4-WORKSTATIONS.md](PS4-WORKSTATIONS.md). The description below records the
2.3.0/2.3.1 special crafting implementation; paper patterns now have the PS4
loom interface, together with the separate 32 native basic loom patterns.

# PS4 special crafting — 2.3.1

This release replaces the previous Legacy4J-based banner, fireworks and dyeing
screen. It uses the recovered PS4 menu, not a redraw based on screenshots.

## Reference

- Repository: https://github.com/Nejirus/mcps4
- Revision: `37e258085773f11599cf0ffe29119926769554b7`
- Executable SHA-256: `c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8`
- Scenes: `CraftingMenu1080.fui`, `skinHDGraphicsInGame.fui`.
- Layout and asset hashes: `assets/legacyconsole/ps4_crafting.json`.
- Pattern rows and groups: `assets/legacyconsole/ps4_banner_patterns.json`.

The executable hash above is recorded in `MCPS4-CRAFTING.json`; that manifest
is authoritative. Neither the executable nor its complete media archive ships
with the mod or source download.

## Recovered behavior

The Java screen adapts `IUIScene_CraftingMenu` routines at `0x9a9600` (owned
choices), `0x9ac010` (input), `0x9af470` (select/add banner), `0x9ad6a0`
(remove layer), `0x9aca40` (remove ingredient), `0x9b04a0` (create),
`0x9b23c0` (groups and panel visibility), `0x9b45a0` (selectors),
`0x9bb700` (preview and staged grid), `0x9bb200` (add constraints) and
`0x9bd000` (fixed choices). FUI selector positioning follows `0x7f9ea0`.
The original banner and dye static constructors were evaluated separately;
the game's application entry point was never run.

Banners have Create/Select/Copy, then Stripes, Halves, Shapes, Borders /
Backgrounds / Gradients, and Symbols. The source contains 38 original material
patterns plus six paper-pattern alternatives. `BannerRecipes::addRecipes` at
`0x4d9d0` assigns paper-pattern alternatives to crafting type 5 (loom), rather
than the normal banner crafting type 1. This screen therefore offers the 38
material patterns: 11 stripes, eight halves, ten shapes, five border/background/
gradient patterns and four symbols. Personal crafting retains six shapeless
material patterns across its two editing groups. Banners must be selected before
browsing pattern groups; group navigation then wraps among editing groups.
The source's icon patterns are `cr`, `ld`, `mc`, `bo`, and `cre`, in black.

Triangle stages a layer, Square removes the last layer, and Cross commits all
staged layers. Existing layers count toward the limit of six. Material allocation
reserves every dye and pattern material across the entire staged operation.
Creating returns to the base banner group. Copying retains the original patterned
banner and consumes a blank banner of the same base color.

Fireworks use three groups: Color / Shape / Effect, Star / Fade, and Power / Star.
The shape list retains fire charge, gold nugget, six skull/head variants, and
feather; the effect list is glowstone dust then diamond. One shape and one of
each effect may be added; colors and stars can repeat if the player has enough.
Rockets have one to three gunpowder. Four personal or nine table grid cells
limit additions. Once an ingredient has been added, the existing grid stays
fixed while browsing other choices. Square removes additions in reverse order.

Dyeing has Armor / Color and Item / Color. Item supports wool, beds and shulker
boxes. The original `DyeColor::StaticCtor` (`0x21e8a0`) supplies the white-to-black
color order. Version 2.3.1 uses only the 16 separate dye items, as requested for
Minecraft 1.17.1; the four earlier material substitutes are omitted. Banner
patterns retain the reverse color order with the same 16 separate dyes. An already matching item
color cannot be dyed again through this menu.

## Art and scaling

The authored scene is 1920×1080. It retains the MainPanel transform, original
nine-grid slices, 82-pixel selectors, 70-pixel table ingredient cells, 48-pixel
inventory pitch, 108-pixel result slot, 233-pixel preview, tabs and arrows.
Description-panel visibility follows the source: it is shown for base banner,
fireworks and dyeing groups, and hidden for banner editing's full-width row.
The focused selector is painted after every other square, so its wider frame
and highlight cannot be covered by the next square. Tab icons and selector
highlights are behind the item models. Source FUI BGRA
channels are converted to Java RGBA; no replacement artwork is drawn. Original
PNG payload hashes and converted PNG hashes are recorded separately.

The entire scene scales uniformly using the smaller of width/1920 and
height/1080, centered on other aspect ratios. At 720p and 1080p this uses the
original layout at 2/3 and 1 scale respectively. At 1440p and 4K it scales to
4/3 and 2. Fitting 16:10 and 4:3 preserves the same scene's proportions, rather
than inventing new control positions.

`docs/reference-tools/ps4/export_crafting.py` reproduces the resource extraction
from the two original FUI files and the included constructor records. Run with
Python 3 and Pillow, supplying `--input-dir` and `--project-root`. The parser
and extractor are included; upstream game files are not.

## Java adapters and comparison limits

Minecraft 1.17 renders its own items, banners, resource-pack replacements and
localized item names. Native recipes calculate firework explosion NBT and mixed
leather colors; adapters allow recoloring all wool/bed colors with the 16 separate
Minecraft dye items. Original item names and shulker contents survive recoloring.
The inventory panel is read-only, as in this PS4 crafting scene.

New Java packets validate banner plans against the server's actual inventory
and reserve space before committing. A failed craft consumes nothing. Crafting packets use protocol 4; update the
main mod and optional add-on together to 2.3.1. the optional join add-on keeps the existing
unmodded-client behavior.

This is a source-derived port, with Java platform adapters. Render checks use
Minecraft on Linux software OpenGL. They are not a side-by-side capture from a
PS4 console, and do not establish pixel-for-pixel or physical-controller parity.

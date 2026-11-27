# Release 1.0.6

Item hover panels and the villager trading offer popup now use PS4's runtime
padding. The default textures include original PS4 blocks, items and compatible
entity textures, including the emerald block and witch. The 22 selected ore and
deepslate PNGs present in the supplied Programmer Art Fix rv4 patch override
the corresponding defaults.

Update the main mod and Join Compatibility add-on together to **1.0.6**.
Minecraft remains **1.17.1**, Forge **37.1.1**, Java **17**, protocol **8**.
LCE Look remains **2.0.0**.

## Information panel padding

Source: [Nejirus/mcps4](https://github.com/Nejirus/mcps4), revision
`37e258085773f11599cf0ffe29119926769554b7`. The pinned executable SHA-256 is
`c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8e3`.

The previous implementation used the FUI authoring-time text offset `(3,5)`.
PS4 replaces that offset when assigning text. The original cursor routines
`0x7d57c0` / `0x7d5920` and HTML panel routine `0x864f80` size the panel using
character zero's glyph extent, obtained through `0x863ea0(textField,0)`.
This is a glyph measurement, not the height of the complete multiline tooltip.

For the integer glyph height `g`, the original width is
`round(textWidth + 0.8*g)` and height is `textHeight + g`.
The horizontal text inset is `round(0.35*g)`. The cursor's vertical inset uses
rounding; the trading panel uses integer division by two. The Java translation
preserves this distinction. Existing nine-slice panel pixels are unchanged.
Minecraft's actual first baked glyph, including its selected font, supplies
the compatible glyph extent; the original calculation is then applied in
PS4 canvas units before the menu scale transform.

`capture_pointer_panels.py` executes the original routines with isolated
text/UI callbacks. Its 32 captured layouts include different glyph sizes and
multiline field heights. Both Java layout tests compare against those CPU
observations. The checked fixture is
`src/test/resources/ps4/pointer-panels-native.json`.

## Default resource textures

`export_default_textures.py` reads the literal registrations in the original
PS4 atlas initializer `0xd3e070` and cell constructor `0xd549d0`. It extracts
cells from the original `Common/res/TitleUpdate/res/terrain.png` and `items.png`.
For example, the emerald block's registration at `0xd46ad1` selects column 9,
row 1. The original witch texture comes from `mob/witch.png`.

Java filenames are bound by the original registered name, an explicit registry
rename, or identical visible pixels in the Java/Programmer Art catalog. Texture
colors are copied from mcps4. PNG re-encoding is lossless. Every output and
input has a hash and mapping record in
`assets/legacyconsole/ps4_default_textures.json`.

Some PS4 entity images use alpha unsuitable for Java's cutout renderer. For
these, the existing Java Programmer Art alpha coverage is used only when
every resulting visible RGB pixel exactly matches the original PS4 pixel at
the same UV coordinate. These adapters are explicitly identified in the
manifest. Images with unverified mesh/UV compatibility remain on their existing
Java assets. Console procedural animations are not reimplemented by this update.
Matching Minecraft 1.17.1 animation metadata accompanies imported animated
PNGs in the same resource pack. This fixes the lit blast furnace: its original
16×32 image contains two 16×16 frames; without metadata Java stretched the
whole strip into a single face. Java's animation timing is retained as an
explicit renderer adapter. Fast-leaf variants and console-only
minigame textures do not acquire invented Java mappings.

The supplied pack contributes only its ore and deepslate PNGs, unchanged.
Its regular diamond, lapis and emerald ore files are absent, so those entries
retain their mapped PS4 textures. Deepslate stairs, slabs and walls use the
same imported base block textures through Minecraft's existing models.
At the user's request, nether quartz ore uses PS4's registered `quartz_ore`
cell, and nether gold ore keeps default Java. Java zombified-piglin textures
and geometry are retained. Grass top, grass side, side overlay and snowy
side come from the registered `grass_top`, `grass_side`, `grass_side_overlay`
and `snow_side` cells, respectively. Biome tint still comes from Java.
The author's attribution and terms are preserved in
`docs/resource-pack/ProgrammerArtFix-copyright.txt`.

The textures are part of the mod's default resources. User-selected resource
packs keep their normal higher priority.

## Selected-item information while mining

The original caller `0xc78a90` consumes the inventory change notification
through `0x37b4c0`, then calls the information setter through `0x9dd2c0`.
The setter `0xa4e3f0` restarts the 60-tick display for a supplied nonempty
tooltip. The Java inventory adapter previously treated any item NBT change
as a fresh notification, including ordinary durability loss while mining.

`Ps4SelectedItem` excludes the Java `Damage` field from that notification
comparison. Slot, item type, name and enchantment changes still trigger the
information display. This is an adapter for Java inventory notifications;
it does not claim that PS4 used Java NBT comparisons. Two tests cover repeated
wear, count-only changes and meaningful selection/metadata changes.

## Riding inventories, HUD and controls

The original `HorseInventoryMenu1080.fui` supplies the horse, donkey and llama
menu geometry, labels, equipment placeholders and portrait viewport. The
translated `0xa47400` visibility rules use the actual mount's saddle and armor
capabilities and scale the chested list by its column count divided by five.
`0x809540` removes the unchested list while retaining its authored recess.
Java's synchronized horse menu supplies item transactions, chest contents,
armor, saddles and llama carpets.

`Ps4ContainerLogic.horse` translates `0xa47bd0` section routing, including its
coordinate offsets and visibility branches. `capture_riding_routes.py` executes
that original function with isolated horse/UI callbacks. The test compares
3,312 routes across split mode, chest presence, armor/saddle capabilities and
different chest widths.

The original portrait routine `0x9f6ba0` uses a 0.71 scale multiplier and a
−7.5 foot-anchor divisor; those constants are read from the executable.
Minecraft adapts entity meshes, lighting and rendering to that viewport.
Eight original `skinHDGraphicsHud.fui` sprites and the horse-health/jump-bar
display lists supply the riding HUD. Their transforms share the selected
HUD scale. The previous mounted-living-entity Java fallback caused the
reported HUD size change. Horses now use the console path. R3 dismount,
Triangle inventory and Cross jump use the existing configured glyph family;
jump is conditional on the mount's jump capability. R3 dismount does not leave
the controller's crouch toggle enabled after leaving the mount. Java still
supplies mount simulation, health/invulnerability state and jump charge.

## Witch hood

The original WitchModel constructor `0xd7eff0` first loads `villager_v2`
through the base name getter `0xd84340`, then applies `villager.witch`.
The original `models.bin` base model includes a `helmet` attached to `head`:
position `(−4,−10,−4)`, size `(8,10,8)`, UV `(32,0)`, inflation `0.5`.
Java's WitchModel replaces that part with the pointed hat. A renderer layer
restores the original inherited helmet beside the existing pointed hat,
using PS4's original texture. No new hood geometry was designed.

## Verification

437 automated tests pass, including the original pointer/trading calculations
and existing PS4 map, world, menu and HUD regression tests. The production
client uses Forge 37.1.1 and the supplied OptiFine H1, with shaders off.
The runtime record checks active resource loading against the texture manifest
and captures hover panels in inventory, furnace, enchanting and villager
trading menus. Inventory and trading cover all three Console UI Size stops
and a 4:3 case; furnace and enchanting use their normal UI size. HUD checks
also cover 720p, 1080p, 1440p, 4K, 16:10 and a 3024×1964 framebuffer.
The mining check used the actual selected-item HUD timer while server-authorized
stone mining damaged an enchanted pickaxe nine times; the timer reached zero
and stayed expired. Horse, donkey and llama menus and riding HUD cover all
three size stops at 1440p. The horse fixture retains Java's constructor maximum
of 53 health to exercise multiple rows. No physical controller was attached;
riding action glyph identifiers and their conditional actions were checked.
The final report contains 56 runtime checks and 40 captures. The suite resumed
31 already-passed checks after private fixture corrections; all production
jar hashes were checked before reusing that baseline.
A Retina-shaped Linux framebuffer verifies geometry, not macOS display-density
handling. Final runtime results and image hashes are recorded in
`RELEASE-1.0.6-RESULTS.json`.

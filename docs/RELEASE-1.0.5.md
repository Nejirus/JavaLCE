# Release 1.0.5

Active potion effects now use PS4's HUD tiles and inventory effect bars.
Hovering an item in menus opens the PS4 pointer information panel. Selecting
a hotbar item shows its name and enchantments, with PS4's five-line limit.
The existing R2 “What's This?” block-description action remains available.

Update the main mod and Join Compatibility add-on together to **1.0.5**.
Minecraft remains **1.17.1**, Forge **37.1.1**, Java **17**, protocol **8**.
LCE Look keeps its separate **2.0.0** version.

## PS4 source evidence

Authority: [Nejirus/mcps4](https://github.com/Nejirus/mcps4), revision
`37e258085773f11599cf0ffe29119926769554b7`. The input `eboot.bin` SHA-256 is
`c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8e3`.
No Legacy4J replacement artwork or invented potion artwork is used.

| Feature | PS4 routines and media | Java implementation |
| --- | --- | --- |
| HUD effects | `0xa4f690`, `0xa4f970`, `0x8191c0`; `FJ_MobEffect_Tile_Normal` | `Ps4Effects` |
| Inventory effect bars | `0xa65460`, `0x81db80`, `0x81dfa0`, `0x81e9c0`, `0x8431e0`; `InventoryMenu1080.fui`, `FJ_MobEffect_Normal/Small` | `Ps4Effects`, `Ps4InformationLayout` |
| Pointer item information | `0x9a5ce0`, `0xa0b930`, `0x9c5800`; `FJ_Cursor_Normal`, `FJ_TextContainer`, `FJ_PointerTextPanel` | `Ps4ItemInformation` |
| Hotbar information | `0xc78a90`, `0x9dd2c0`, `0xa4e3f0`, `0x813030`; `HUD1080.fui` `SelectedLabel` | `Ps4ItemInformation`, `Ps4InformationLayout` |

The HUD sorts effects by remaining duration, decreasing, and places beneficial
and harmful effects in separate rows. Its expiration pulse starts below 200
ticks. The effect icons and nine-slice panel pixels were already exported from
`skinHDGraphicsInGame.fui`; this release uses those original images.

Inventory bars use the authored effect group offset, bottom-up anchors,
white names and gray duration text. The original layout compresses spacing
above seven normal bars. Native anchor conversion and positive half-up rounding
are preserved, including the authored seventh anchor's `0.8` to `0` conversion.
Durations use the original minute clamp and zero-padded seconds.

The original pointer widget places its panel at `(24, -31)` relative to the
cursor, with text inset `(3, 5)`. Names, enchantments, lore and other item details
come through Minecraft's item-information API, with their component colors and
formatting preserved. Automatic block-description insertion is not reintroduced.

The native hotbar routine removes empty information entries. More than five
entries become four entries followed by `...`. Its selected-label setter starts
a 60-tick timer. The original 38 fade alpha values are exported from the FUI
timeline, rather than replacing the curve with a linear fade.

## Minecraft adapters

These are translations into Java and Minecraft's renderer, not execution of
the PS4 UI runtime. Minecraft supplies live inventory/effect data, localization,
font metrics and Forge tooltip components. Effects without a matching PS4
icon use their registered Java sprite. No replacement PS4 icon is invented.

The original menu and HUD size controls remain independent. Inventory bars can
use the authored small widget when the right margin needs it. Hover panels
fit the viewport and move to the cursor's other side at an edge. Hotbar text
fits between the HUD and the crosshair; it reserves the actual crosshair and
Java attack indicator extents. If the HUD lift setting leaves no lower space,
the text uses the clear area above the crosshair. This viewport adaptation is
necessary for the requested sizes/aspect ratios and is distinct from the
native fixed-canvas positioning. The FUI fade curve is sampled on Java's
remaining display ticks.

## Verification

`capture_information.py` executes the original bounded PS4 bar layout, selected
label setter and effect pulse routines with isolated data/UI callbacks. It
records 29 effect-count layouts, 28 effect-to-bar mappings, four timer inputs and 11 pulse samples.
`export_information.py` reproduces the scene and timeline metadata from the
pinned media, including input hashes. The native capture ships as
`src/test/resources/ps4/information-native.json`.

The build runs 432 automated tests, including native layout/pulse comparisons
and crosshair clearance across all three HUD sizes, GUI scales, HUD lift values,
720p, 1080p, 1440p, 4K, 4:3, 16:10 and a 3024×1964 framebuffer.
The final production-client test record and capture hashes are in
`RELEASE-1.0.5-RESULTS.json`. Tests use Forge 37.1.1 and the supplied OptiFine H1,
with shaders off. A Retina-shaped Linux framebuffer checks pixel geometry;
it does not substitute for testing macOS's display-density handling.

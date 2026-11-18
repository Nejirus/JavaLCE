# Legacy Console 2.3.18

The map cursor now uses PS4's separate drawing layers above the map image,
fixing the coplanar cursor/image conflict that could make the cursor flicker
while the held map moved. Server-details player rows use the same original
list-button artwork as world select, including the selected state and
30-unit row height. Rows remain within the recessed frame and scroll to the
last available player.

Update the main mod and optional Join Compatibility add-on to 2.3.18 together.
Use Java 17, Minecraft 1.17.1 and Forge 37.1.1. LCE Look remains the supplied
2.0.0 shaderpack. Network protocol remains 8.

## PS4 map layer translation

Reference: Nejirus/mcps4 commit
`37e258085773f11599cf0ffe29119926769554b7`. The original `eboot.bin` SHA-256 is
`c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8`.
The executable is an analysis input and is not included in the source archive.

The earlier renderer port moved Minecraft's map image from -0.01 to PS4's
original -0.02. Minecraft's first decoration already used a -0.02 translation,
so the first cursor and terrain ended up on the same plane. Small numerical
differences during rotation and movement could let the terrain cover parts of
the cursor. The previous map fix addressed the separate competing edge-update
writer; it did not correct this render-depth conflict.

PS4's `0xc6fba0` map renderer uses these layers:

| Original instructions/data | Behavior |
| --- | --- |
| `0xc6fc48..0xc6fce7` | Draw the complete map image at Z = -0.02 |
| `0xc6fdf1`, constant `0xf06968` | Initialize the decoration translation to -0.04 |
| `0xc70058`, `0xc70070` | Pass the current decoration depth to the translation call |
| `0xc700a7..0xc700c0` | Scale icons by (4, 4, 3) |
| `0xc7016e`, constant `0xf0698c` | Use local vertex Z = index × -0.001 |
| `0xc70243..0xc70269`, constant `0xf06994` | Add -0.01 to the decoration translation after each rendered icon |

`MapTextureMixin` now resets the decoration translation at the start of each
map draw and supplies the original sequence to Minecraft's icon translation.
`ConsoleMapRenderer.nextIconLayer` uses repeated float addition, matching the
source rather than substituting a multiply by the icon index. Minecraft's
existing icon scale and local vertex step already match the source.
The first icon therefore starts at -0.04, in front of the -0.02 image; later
icons retain their original separate layers. Held maps and visible item-frame
decorations use the same corrected path.

Map coordinates, north/decreasing Z, finite-world alignment and Classic's
PS4 inset retain their existing implementations. This correction does not
alter map data or save files.

## Original world-select list style

`ConsolePlayScreen` and `ConsoleJoinGame` now share `LceWidgets.listButton`.
It draws the existing original `lce/play/{sd,hd}/list_button` background and
`lce/widgets/{sd,hd}/list_button_selected` artwork. Both original
LoadOrJoinMenu scenes specify a 30-unit row height. The server player list
uses that height and recomputes visible rows and its final scroll offset from
the already-corrected frame interior. Player names remain real server-status
data. No replacement artwork or invented player avatars are added.

## Validation

The full Java suite passes 421 tests. The added native layer regression uses
64 sequential decoration depths captured from the original executable.
`docs/reference-tools/ps4/map_layers.py` executes the isolated original
initialization, Z-argument load, scale and layer-increment ranges with Unicorn;
it also reads the original local vertex-step constant. The captures isolate
Z layering from unrelated coordinate getters and rendering backends.

The published 2.3.17 build lost cursor pixels in 26 of 64 controlled views,
with up to 16 missing white cursor pixels in a view. Its emitted first cursor
and image vertices both measured -0.02.

The release result JSON records the published-build reproduction, actual
renderer vertex depths, controlled cursor visibility sweeps, walking/turning
captures, and player-list viewport checks. The controlled render compares
each cursor with the same cursor rendered without terrain, so missing cursor
pixels can be measured while tilting and rotating the map.

The fixed build passes all 256 tilted/rotated views with zero missing cursor
pixels, plus 256 emitted vertex-depth checks for held and framed maps.
Twenty-four movement captures cover four shader/resolution combinations, and
seven server-list captures verify the original row style and frame bounds.
The Host Options source remains byte-identical to 2.3.17.

Client validation uses Forge 37.1.1 and the exact supplied OptiFine H1, with
shaders OFF and supplied LCE Look 2.0.0. UI viewport checks include 720p,
1080p, 1440p, 4K, 4:3, 16:10 and a Retina-shaped window. These are Linux
software-rendered viewports, not physical Apple display tests.

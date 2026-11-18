# Release 1.0.2 — PS4 inventory and chests

The inventory, single-chest and double-chest screens now use mcps4's original
FUI scenes and artwork. The existing Console UI Size setting controls all three.
Vanilla menu IDs, slot IDs, stack transactions, armor restrictions, offhand
storage and server synchronization are retained through Minecraft 1.17.1's
menu implementation.

The crafting selector overlap was a rendering-state regression in 1.0.1.
Undecorated recipe selectors called the item renderer directly but omitted the
state cleanup used by `ConsoleTheme.item`. Depth testing remained enabled, so
neighboring block models could cover a later selector frame. The slot renderer
now restores lighting and disables depth testing after every item, including
selectors without decorations. The native item dimensions are retained.

## Original PS4 evidence

Source: [Nejirus/mcps4](https://github.com/Nejirus/mcps4), revision
`37e258085773f11599cf0ffe29119926769554b7`; executable SHA-256
`c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8`.

* `ChestMenu1080.fui`: root `(637,229)`, 9×3 chest, 9×3 inventory and 9×1
  hotbar. `ChestLargeMenu1080.fui`: root `(637,134)`, 9×6 chest and original
  lower inventory/hotbar transforms. The Java screen preserves custom titles.
* `InventoryMenu1080.fui`: root `(637,214)`, four armor slots, offhand slot,
  inventory, hotbar, original five empty-equipment icons and character backdrop.
* `FJ_InventoryMenu::setClassicCrafting`, `0x81e150`: the console-crafting
  branch removes the manual grid/result/arrow, hides its label, and adds the
  computed X offset to equipment, character and offhand controls. Executing
  the original routine with these authored controls gives **149**. The port
  translates its rounding and centering expression; it does not guess an offset.
* `UIScene_ContainerMenu::handleSection`, `0xa17a60`, and
  `UIScene_InventoryMenu::handleSection`, `0xa66550`: section transitions,
  including offsets passed to the common `0x9a2610` position operation.
  The native reference contains **2,448** executed visible-section cases.
* Player drawing at `0x9f6f30`, with screen dimensions initialized by
  `0xa00900`, supplies the 1920/1080 scale ratio and 8/9-height foot anchor.
  The original `iggy_player` placeholder is **189×252**. Java's entity renderer
  adapts character models, lighting and look-at animation inside this layout.

`assets/legacyconsole/ps4_workstations.json` includes the original scene hashes,
transforms and equipment display lists. The recessed panel's nine pieces are
extracted from `skinHDGraphicsInGame.fui`; only BGRA→RGBA conversion is applied.
No replacement artwork or new PS4 coordinates are drawn.

`docs/reference-tools/ps4/export_containers.py` reproduces the assets/layout
export. `native_containers.py` executes the original recentering and navigation
instructions with bounded FUI getter/setter callbacks. Captures are in
`src/test/resources/ps4/containers-native.json`.

```sh
PYTHONPATH=/path/to/python-dependencies python docs/reference-tools/ps4/export_containers.py \
  --input-dir /path/to/extracted/fui --project-root .
PYTHONPATH=/path/to/python-dependencies python docs/reference-tools/ps4/native_containers.py \
  --eboot /path/to/mcps4/eboot.bin \
  --assets src/main/resources/assets/legacyconsole/ps4_workstations.json \
  --output src/test/resources/ps4/containers-native.json
```

The export extends the existing workstation/front-end manifest. It expects the
pinned original FUI resources; neither the executable nor full media archive is
included in the download.

`check_selector_frames.py` compares captured frame pixels against the original
frame/highlight alpha masks. Four matching 1440p comparisons found 285 frame
pixels overwritten by the neighboring block in 1.0.1 and zero in 1.0.2.

## Scope and adapters

The mod's existing console crafting mode remains in use: personal crafting opens
the console crafting screen. The manual inventory grid is hidden as in PS4's
console mode. This release does not add a classic-crafting setting or expose
shield decoration in console crafting. Creative item selection keeps its
existing screen, reached through the established creative/crafting route.

ChestMenu-based containers with three or six rows use the corresponding PS4
scene, including ender chests. Other container types retain their existing
adapters; no additional PS4 scene is fabricated for arbitrary row counts.
Minecraft supplies localized titles, item/entity models, fonts and synchronized
inventory transactions. The player's persistent inventory slot positions are
restored on closing the screen.

## Installation and validation

Replace both mod jars with version **1.0.2**. Use Java 17, Minecraft 1.17.1 and
Forge **37.1.1**. Network protocol remains **8**. LCE Look retains its independent
version **2.0.0**; the supplied OptiFine **1.17.1 H1** remains the test version.

Automated tests, native comparisons, production-client checks and selector-image
comparisons are recorded in `RELEASE-1.0.2-RESULTS.json`. Physical controller and
Apple Retina hardware are unavailable; Retina-shaped framebuffers are tested
on Linux. Java model/font rendering is not claimed to be pixel-identical to PS4.

# Release 1.0.1 — PS4 item viewports and banner availability

The carried stack in a PS4 workstation used Minecraft's fixed 16-unit item
viewport. Its slot used the console scene's scaled viewport. Changing Console
UI Size therefore changed the item on placement without changing the item on
the cursor. The carried stack now follows the original PS4 pointer-item area
and the same scene transform as the menu.

Slot rendering now translates the original PS4 rounded-bevel calculation,
instead of deriving item geometry from the highlight's 90% inner face.
Crafting, stonecutting and special-crafting recipe selectors also follow PS4's
disabled-decoration flag: they show the recipe icon without stack numbers or
durability overlays. Inventory and crafting output retain their decorations.

The source supports different sizes for regular slots, enlarged result slots
and the carried stack. Those distinctions remain. Regular icons were already
close to the original size; they are not all reduced by a new arbitrary factor.

The new-banner selector now checks the wool-and-stick recipe, rather than
whether the output banner is already in the inventory. This fixes a red
unavailable background on a craftable banner, and the reverse false available
state after its materials have been consumed.

Banner design controls remain PS4's staged workflow: select a banner, press
Triangle to choose it, select a pattern/color, press Triangle **Add**, then
Cross **Create**. The large preview shows the staged banner. Selecting a pattern
alone does not add it to that preview or enable Create. The reported design and
preview failure was resolved by using Add; no replacement workflow was introduced.

Banner-on-shield crafting is not exposed in the console menu. The supplied
mcps4 build's banner recipe implementations cover pattern application and
duplication; no shield-decoration implementation was recovered. The optional
Java-recipe extension was declined, preserving the requested PS4 menu behavior.

## Source evidence

Authoritative input: [Nejirus/mcps4](https://github.com/Nejirus/mcps4), revision
`37e258085773f11599cf0ffe29119926769554b7`. Executable SHA-256:
`c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8`.

* `FJ_SlotList_Slot::addSlot`, `0x83b9b0`, obtains the slot's X scale and
  rounds it with `0x3c0ea0`. The new custom-draw rectangle is
  `[border, width-border, border, height-border]`. At a 63-unit furnace
  inventory cell, the original executable returns `[3,60,3,60]`, a 57-unit
  item area. The 96-unit result cell returns `[5,91,5,91]`, an 86-unit area.
* `skinHDInGame.fui`, `fourj.Cursor.FJ_Cursor_Normal`, has an
  `iggy_pointerIcon` placeholder at `(-31,-31)`. Its 65×59 shape and original
  transforms produce a 72.00027×71.99988 area. `CursorImage` is a separate
  48×48 pointer bitmap. The carried-item adapter uses the item placeholder.
* `UIScene_CraftingMenu::customDraw`, `0xa21bd0`, disables decorations on
  horizontal and vertical recipe selectors (including `0xa227c1` and
  `0xa22058`). The common item drawing operation is `0xa09560`.
* Stack decorations use the common viewport scale in `0xa09560`; count
  placement in `0xc38b20` uses the familiar right anchor at X+17 and Y+9.
  Java 1.17.1's models, font and item decorators remain the target adapters.
  Font glyph metrics and every newer/modded item model are not claimed to be
  pixel-identical to PS4.
* Banner input in `0x9ac010` routes Triangle to staging (`0x9af470`), Square
  to removal (`0x9ad6a0`) and Cross to crafting (`0x9b04a0`). The latter
  submits the staged vector at IUI offsets `0xc90`–`0xc98`.
  `UIScene_CraftingMenu::customDraw`, slot ID `0x37`, draws the staged banner
  at scene offset `0xe28` (IUI offset `0xca8`) in the original `PreviewSlot`.

`docs/reference-tools/ps4/item_viewports.py` executes the original slot
rectangle and rounding instructions with bounded platform callbacks. Its 12
captured cases and the original FUI placeholder are in
`src/test/resources/ps4/item-viewports-native.json`. Regeneration:

```sh
PYTHONPATH=/path/to/python-dependencies python docs/reference-tools/ps4/item_viewports.py \
  --eboot /path/to/mcps4/eboot.bin \
  --widgets /path/to/extracted/skinHDInGame.fui \
  --output src/test/resources/ps4/item-viewports-native.json
```

The user's Console UI Size setting is a Java menu-zoom adapter. It applies the
recovered item/slot geometry uniformly; PS4's separate original HUD setting is
documented in `GUI-TUTORIAL-2312.md`.

## Installation and validation

Use `legacy-console-1.17.1-1.0.1.jar` with Java 17 / Minecraft 1.17.1 / Forge
37.1.1. Update the optional server compatibility jar to
`legacy-join-compat-1.17.1-1.0.1.jar` together with the main mod. Network
protocol remains 8. LCE Look keeps its independent version 2.0.0.

Native comparisons, automated tests and production-client checks are recorded
in `RELEASE-1.0.1-RESULTS.json`. The client checks use the supplied OptiFine H1
and original LCE Look 2.0.0, including stack pickup/replacement and actual
rendering transforms at each console size. Retina-shaped framebuffers are
tested on Linux; physical Apple Retina hardware is unavailable.

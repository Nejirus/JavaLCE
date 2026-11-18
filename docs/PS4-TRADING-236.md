# 2.3.6: PS4 trading and crafting tab placement

Source: [Nejirus/mcps4](https://github.com/Nejirus/mcps4), revision
`37e258085773f11599cf0ffe29119926769554b7`. This patch uses the PS4 executable
and Orbis FUI assets, rather than LCE's older trading implementation.

## Translated behavior

- `UIScene_TradingMenu`, constructor `0xad19a0`, list setup `0xad24a0`,
  display update `0xad2560`, input `0xad3ce0`, scroll visibility `0xad3a20`.
  The offer row has **10** cells. It keeps exhausted offers and their server
  indices, permits focus on empty cells, stops at the ends, and scrolls one
  position at a time when focus reaches a visible edge.
- Affordability (`0xad5200`) checks both costs. Trade execution (`0xad3ce0`)
  inserts the result and drops any remainder when the inventory is full. The
  Java translation preserves partial insertion and the dropped item/NBT; a
  full inventory no longer disables an otherwise valid offer.
- `FJ_TradingMenu`, `0x833370`, centers the first payment on the result row
  and hides the second holder for a single-payment merchant. Java merchants
  carrying a second cost retain both holders.
- Level names: `0xad55f0`; experience preview: `0xad5730`. The selected trade
  previews current progress plus trade XP divided by the tier's XP range,
  capped at one. Master returns one. Original execution supplies the golden
  results, including the master's return value.
- `FJ_TradingMenu::setSelectedSlot`, `0x832f90`, centers the original 108-unit
  highlight on the 80-unit offer cell. `0x832d50` positions item-detail text
  beside the left/right half of the focused selector. `0x833230` supplies its
  item tooltip details.
- `TradingMenu1080.fui` supplies every main panel, inventory/offer/required/
  result holder, label, arrow, cross and experience-bar transform. The
  experience bar comes from `skinHDHud.fui` and `skinHDGraphicsHud.fui`;
  its empty, white preview and green fill bitmaps retain their source pixels.
  The HTML panel uses the original small text field and the item tooltip
  colors supplied at runtime (`0xad5510`), including the item name.
- `CraftingMenu1080.fui` VertTab frames place the active item holder at
  `(38,25.65)` and inactive holder at `(30,26.05)`. The previous Java code
  reused 38 for both. Both crafting screens now read these authored frames.

`docs/reference-tools/ps4/export_workstations.py` and `export_crafting.py`
reproduce scene transforms, original PNGs and language strings. Source asset
hashes and the exact BGRA-to-RGBA conversion are recorded in the exported JSON.
The source executable/archive are analysis inputs and are not distributed.

## Java 1.17.1 adapters

The Minecraft server remains authoritative for prices (including demand and
reputation), available offers, uses/restocking, item/NBT costs, result insertion,
statistics and merchant XP. TradeRequest validates the live menu and index.
The normal Java SelectTrade packet is deliberately omitted: it automatically
moves costs into hidden input slots, unlike PS4 inventory-funded focus.

Java merchant packets provide unlocked offers and the current merchant level;
they do not provide each offer's original unlock tier or future-tier offers.
The PS4 level-label control therefore shows the synchronized current merchant
level. No future locked trades or per-offer tiers are fabricated. Wandering
traders use the same PS4 layout with their Java progress-bar visibility.
Item models and glyph rasterization are supplied by the installed Java game.
Mouse hit areas, mouse-wheel navigation, result-click activation and uniform
fitting on other display ratios are host input/display adapters.

Crafting families remain the restored 2.3.3/2.3.5 grouping. World generation,
map behavior, beacon payments and other workstation menus are unchanged.

## Verification

The isolated original PS4 routines produced 360 navigation transitions and
100 experience previews. Java tests compare their results exactly. The capture
script and vectors are included under `docs/reference-tools/ps4/` and
`src/test/resources/ps4/trading-native.json`.

Production-client results and artifact hashes are recorded in
`NATIVE-236-UI-RESULTS.json`. Physical Retina/controller hardware is outside the
Linux software-renderer framebuffer and controller-input fixture coverage.

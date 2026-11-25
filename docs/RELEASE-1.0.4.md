# Release 1.0.4

- Wither uses the four original full/half and flashing heart sprites from
  mcps4's `skinHDGraphicsHud.fui`. The effect no longer switches the entire HUD
  to the smaller Java fallback. HUD Size continues to control its geometry.
- The air row remains visible outside water while the player's air refills,
  and disappears when air reaches its maximum.
- Workstation footers use the PS4 pointer-action rules already used by inventory
  and chests (`0x9a2950` / `0x9a2700`). Take, Split, Quick Move and What's This
  depend on the hovered item; Place/Place One/Swap depend on the carried stack,
  slot acceptance and capacity. Select appears over an enabled menu control.
  Outside slots and controls, only Back remains. This also covers smithing,
  grindstone and other retained Java workstation layouts.
- In Legacy worlds, Java's dimension-initialization packet now carries the
  destination dimension's border. Previously `PlayerList.sendLevelInfo` always
  sent the Overworld border, causing a red border warning and client-side block
  placement rejection on the outer End islands. Ordinary Java worlds retain
  their existing behavior. This does not change terrain generation or Legacy
  Overworld limits; the tutorial's existing Nether limit is retained.
- Trade updates send the actual villager's level. Java's server-side
  `MerchantMenu` does not populate its client-only level field; reusing that
  value reset the displayed tier to level 1 and filled the XP bar. Existing PS4
  XP preview arithmetic and Java trade rewards are retained.
- Dimension-specific music is replaced when the dimension changes, before the
  active-track early return. Nether music cannot continue in the Overworld.
  Menu and Overworld retain their existing shared music pool.
- Honey bottles are hidden from console crafting. The waxed copper block,
  stairs, slab, cut and oxidation variants remain hidden as in Release 1.0.3.
  Honeycomb waxing and stonecutting already-waxed copper remain available.

The personal crafting row intentionally ends after the last recipe family.
mcps4's `IUIScene_CraftingMenu` row update at `0x9b6890` stops at the first empty
family. Ten executed native cases cover personal/table contexts with 0, 1, 3,
7 and 12 families. No extra blank entries are added.

PS4 reference: Nejirus/mcps4 revision
`37e258085773f11599cf0ffe29119926769554b7`. The original heart image hashes and
health-frame labels are recorded in `assets/legacyconsole/ps4_status_hud.json`.
`docs/reference-tools/ps4/export_status_hud.py` and `capture_crafting_row.py`
reproduce the asset export and native row audit. Source inputs are not bundled.
Honey/copper filtering is a requested customization for Java's newer items.
Dimension packets, entity air data, trade synchronization and sound-channel
lifecycle are Minecraft 1.17.1 adapters; no replacement PS4 code is claimed for
those host APIs.

Install the main mod and optional Join Compatibility **1.0.4** together.
Network protocol remains **8**. LCE Look remains **2.0.0**.
Build and client validation are recorded in `RELEASE-1.0.4-RESULTS.json`.

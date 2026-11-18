# 2.3.5 UI corrections

This patch keeps the PS4 workstation implementation from 2.3.4 and corrects the
reported layout issues. World generation, map margins and network protocol 5
are unchanged.

- **Crafting and stonecutting:** remove the extra upper-right recessed
  description panel and its duplicate item description. The authored main,
  ingredient and inventory panels remain.
- **Crafting families:** restore `ConsoleRecipeGroups` from the released 2.3.3
  implementation, including its Legacy4J listing and 1.17 adaptations. This is
  the user's requested reference for grouping. Planks/wood, workstations,
  storage, barriers, slabs, stairs and color variants return to their previous
  columns and order. Honey bottles and waxed copper remain available, as in
  2.3.4. The PS4 artwork, selectors and inventory-funded crafting stay in use.
- **Beacon labels:** inset Primary Power and Secondary Power by the original
  recessed panel's 22-unit left border width, so the glyphs start inside the
  panel face. This padding correction follows the user's request.
- **Beacon payment row:** add Minecraft 1.17's netherite ingot before emerald,
  followed by diamond, gold and iron. Five equal cells fit the original row.
  This is an explicit Java-version addition; PS4's original row has four items.
  The Java server accepts and consumes the netherite payment.
- **Enchanting book:** retain the original **57-unit** X/Y model scale. Executing
  the original routine confirms it at four viewport ratios, so this patch does
  not enlarge the book based on appearance alone. Correct its bounds/centering:
  the FUI placeholder is **65×59**, rather than the generic 50×50 slot shape.
  Its transformed authored size is approximately **183×117**. The Java book
  mesh, animation and renderer remain target adapters.

## PS4 book evidence

The actual function entry is `0x009e0b10` in the pinned mcps4 executable,
revision `37e258085773f11599cf0ffe29119926769554b7`. The earlier 2.3.4 notes
referenced an instruction inside that routine (`0x009e0b30`). Original constants
at `0x00ee28f0` and `0x00ee28f4` are -57 and +57. The routine compensates its
local model scale for the surrounding viewport scale; it does not multiply the
57-unit value by the placeholder dimensions.

`EnchantingMenu1080.fui` local definition 2 / shape 0 has vertices bounded by
(0,0)..(65,59), transformed by (2.815338134765625, 1.9828338623046875), at
(39,70) within the scene. Those dimensions now locate the model's center.

`reference-tools/ps4/capture_book_transform.py` executes that original routine
with graphics/platform callbacks. The recorded matrices for viewport ratios
0.5, 1, 4/3 and 2 are in `reference-tools/ps4/book-transform-native.json`.
Only rendering callbacks, the shared stack guard, and the animation tick are
adapted; the book draw arithmetic runs as original machine code. Reproduce it
with the pinned source image:

```sh
python docs/reference-tools/ps4/capture_book_transform.py --eboot /path/eboot.bin --output docs/reference-tools/ps4/book-transform-native.json
```

Verification and exact tested-file hashes are recorded in
`NATIVE-235-UI-RESULTS.json`.

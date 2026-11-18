# PS4 tutorial, hints and Play Tutorial — 2.3.7

Play Game now places **Play Tutorial** immediately below **Create New World**,
followed by saved worlds. It installs a fresh copy in `Tutorial`, `Tutorial-2`,
etc. Existing folders are never replaced. Resume from the saved-world list.

## Source

Primary source: [Nejirus/mcps4](https://github.com/Nejirus/mcps4/tree/37e258085773f11599cf0ffe29119926769554b7),
revision `37e258085773f11599cf0ffe29119926769554b7`.
`MCPS4-TUTORIAL-237.json` records original file hashes and native entry points.

* `FullTutorial` constructor `0x964350`: 43 registered tasks, including the final
  three-part compound (45 sequential tasks), guided crafting/furnace tasks,
  progress flags, original input masks, pre-completion and six area hints.
* Common task constructor `0x999780`: 17 menu/activity states. Original
  Info/Choice, HorseChoice, Controller, Pickup, Craft, UseItem, CompleteUsingItem,
  UseBlock, StateChange, ProgressFlag, FullTutorialActive and UICrafting tasks
  are adapted to Java events, menus and network messages.
* `Tutorial` constructor `0x96d5c0`, hint constructor `0x986450`, and constants
  `0x1403dc0`: 2-second input minimum, 7-second hint display and 14-second delay.
* Native block registry and `Block::setUseDescriptionId` virtual slot `0x370`:
  314 English description bindings. Stone enum initializer `0x67f1f0` supplies
  the seven stone variant bindings. The full registry distinguishes existing
  PS4 blocks without descriptions from blocks added later.
* `TutorialPopup1080.fui`: original pointer-panel position `(1080,75)`,
  description `(1118,105)`, icon holder `(1407,264)`. The original panel and
  hunger icon are reused. Native text ICON payloads select the original item
  IDs (steak 364 / bed 355), converted with Minecraft's own item fixes.
* Canonical `Orbis/Tutorial/Tutorial.mcs` and `Tutorial.pck`, rather than the
  different DLC WorldPack: original terrain, entities, containers and rule data.

Constructor recovery executes isolated original code using Unicorn with
allocator, UTF-16 copy, profile query, registry and named-area adapters. It does
not execute console startup. Captured pointers name registry objects; they do
not emulate the whole game. Decoders and raw constructor snapshots are in
`reference-tools/ps4/tutorial`. LCE task classes are used to cross-reference
method meanings, with the PS4 graph and constants supplying this release's data.

English descriptions for blocks absent from the PS4 catalog come from
[Legacy4J](https://github.com/Wilyicaro/Legacy-Minecraft/tree/f49505dab97480f687025f8a54208d2caa3ff86c),
revision `f49505dab97480f687025f8a54208d2caa3ff86c`: its `.tip` strings and
positive-ID/tag/exclusion overrides are retained. Text is not newly authored.

## Save conversion

The MCS wrapper uses zlib after an 8-byte header and a 144-byte file table.
Console regions contain 16-by-16 chunks with a 32-entry header stride; Java
regions are re-bucketed by saved chunk coordinates. Blocks are two 128-high
XZY slabs, transposed to Java section YZX order. Both nibble arrays and block
bytes are transposed. No replacement terrain is generated in saved chunks.

The intermediate Anvil save passes through Minecraft 1.17.1's own DataFixer.
Mixed old and namespaced entity IDs are normalized separately, and nested
inventory item stacks receive an explicit ITEM_STACK fix. Complete chunks are
marked `full`, with lighting recalculated by Java. The 3,384 original chunks
are retained: 2,916 Overworld, 324 Nether and 144 End. Source saved-data files
are copied. The 16 original PopulateContainer rules supply their authored items
and enchantments; the 12 hunt discs retain their exact `4jdata` values and slots.

The source seed is `110485071283`; saved spawn and initial player position are
`(-15,71,85)`, yaw 180, health 12, food 12, with a steak in slot 10. The source
starting box is `[-36,20] × [0,75] × [62,113]`. Movement toward the box is allowed
when outside it. The area task, or the basic tutorial's original No branch,
releases this restriction. The original task sequence holds day time at 8000
until completion. A world-specific SavedData marker confines tutorial rules to
these imported worlds. Individual task counters, crafting history, flags and
12-disc completion persist with the save.

The Overworld is Classic (864 blocks), centered at zero. A bundled dimension
type retains the original `HellScale=3`; this tutorial's Nether is 288 blocks.
Ordinary Legacy world generation and its Nether/End behavior are unchanged.
The saved template's Creative authoring mode is changed to Survival for playing
the tutorial; commands remain disabled. Original difficulty/lock are retained.

## Minecraft adapters and remaining limits

This is a Java 1.17.1 integration of the recovered tutorial, not a console
runtime. Java supplies physics, lighting, mobs, recipes, furnace timing and
item models. Source menu contexts are mapped to this mod's current menus.
Keyboard/mouse instructions use actual Java bindings. Controller input uses
the mod's existing pad layer. Panel text wraps with Java's font and can grow to
fit it; this is not pixel-identical to Scaleform. Optional hints have a profile
file at `config/legacyconsole-hints.txt`; mandatory tutorial instructions stay
active when Hints is Off. Manual block information remains available.

The covered hint classes are block information, mining hold, tool damage,
swimming, recovered activity/menu guides and the six FullTutorial area hints.
This does not claim an exhaustive port of every conditional PS4 hint class,
all source screenshot tokens, or every auxiliary block-description variant.
Some console-only saved entity attribute IDs are not translated; Java ignores those attributes and uses its defaults. The current language resources are English. Guided prompts require the main
mod on the client; an unmodded compatibility client cannot acknowledge them.
Physical Retina hardware, controller hardware and authenticated remote
multiplayer have not been tested. World generation parity is not changed or
claimed by this release. See `TESTING.md` for the verified runtime scope.

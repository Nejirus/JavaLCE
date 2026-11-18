# Tutorial text and flint-and-steel prompts — 2.3.11

Reference: [Nejirus/mcps4](https://github.com/Nejirus/mcps4), revision
`37e258085773f11599cf0ffe29119926769554b7`, eboot SHA-256
`c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8e3`.

## Tutorial authoring tabs

The original PS4 tutorial strings contain U+0009 tabs in 17 text/prompt
fields: the trading and anvil guides and the final exploration instruction.
The Java component adapter passed those control characters to Minecraft's
font, producing missing-character placeholders.

The PS4 whitespace helper at `0x68a570` contains a tab-removal pass, including
comparisons with 9 at `0x68a7e3` and `0x68a813`. `Ps4TutorialText` now removes
tabs before expanding control tokens or measuring text. This is the targeted
translation of that removal behavior. Existing explicit Java paragraph breaks
and the authored `{*B*}` breaks remain, as do controller glyph spans. The
original task strings and tutorial world assets are unmodified.

## Flint-and-steel Use availability

The held-item adapter previously returned Use unconditionally for flint and
steel. It now requires a block hit and the read-only predicates used by
Minecraft 1.17.1's actual ignition interaction: an unlit, dry campfire/candle/
candle cake, ignitable TNT, or a valid adjacent fire placement. Air misses,
blocked placement and unsupported fire locations do not offer Use.

Creeper ignition is recognized through the entity interaction route. Adventure
placement permissions are checked before item use. TNT's direct block action
retains its position before the item placement permission check. Checking HUD
availability never calls `useOn`, changes blocks, or consumes tool durability.
The real game interaction continues through Minecraft/Forge normally.

The target game's `FlintAndSteelItem.useOn`, `BaseFireBlock.canBePlacedAt`,
`TntBlock.use` and creeper interaction supply these conditions. The candle
support is a Minecraft 1.17.1 adapter, not a claim that PS4 had candles.

## Validation

Eight focused Java tests pass. All 17 affected original fields are checked
with keyboard/controller modes, four artwork families and 720p/1440p glyph
selection; words, paragraph breaks and styled controller spans are retained.
The existing shovel/campfire prompt regression also passes.

The production client uses Forge 37.1.1, the supplied OptiFine H1 and LCE Look
2.0.0. Forty framebuffer captures cover three affected original tutorial
messages and fourteen flint-and-steel target contexts with shaders off and
LCE Look. Assertions verify availability, unchanged tool durability and that
an actual subsequent use still lights a campfire. Full results and hashes
are in `NATIVE-2311-PROMPT-RESULTS.json`.

Captures use a synthetic controller state and controlled ray-hit fixtures in
a private flat test world. Physical controller and Apple Retina hardware were
not tested. The prior world-generation, tutorial progression and world-option
implementations are retained.

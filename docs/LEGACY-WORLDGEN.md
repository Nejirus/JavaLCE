# Legacy world generation — 2.3.3

Create World → More World Options → **World Type: Legacy** adds the finite PS4
Overworld. Choose a **World Size** and **Biome Scale** with left/right on the
controller or drag the sliders. Cross/A selects the control without changing its
value. Small biome scale is the original default.

| World size | Overworld width | Chunk width |
| --- | ---: | ---: |
| Classic | 864 blocks | 54 |
| Small | 1,024 blocks | 64 |
| Medium | 3,072 blocks | 192 |
| Large | 5,120 blocks | 320 |

Large world size is the default. Bounds are centered at zero: Classic covers
blocks -432 through 431 on both axes. The original 32-block terrain falloff meets
an ocean continuation. Outside the playable area, columns contain stone through
Y=53 and water through Y=62, without caves, bedrock or decoration. The boundary
blocks movement, as LCE's invisible collision boxes do. Clients with the mod
hide Java's striped wall; clients without it retain their native border renderer.
Only the Overworld is finite. The 1.17.1 Nether and End keep their native
terrain and normal bounds.

The full-width controls are stacked **World Type → World Size → Biome Scale**.
**Biome Scale: Small / Medium / Large** selects the recovered PS4 layer-factory
settings, including its additional zooms and mushroom-layer ordering. It does
not change world size.

**Find Balanced Seed** is a checkbox, enabled by default for a blank seed. It
starts the search when **Create New World** is selected, using the original biome
categories, proportions and fallback score. Turn it off to use an ordinary random
seed. Supplying a seed disables the checkbox and preserves that seed, including
`0` and text such as `WUBZY`. Canceling the search returns to Create World.
The search tests the original central 200 × 200 biome sample, stops at a balanced
candidate, or returns the best candidate after the original ten-second budget.
Its chosen seed is preserved when the creation screen is reinitialized. Search
results can vary with the number of candidates tested within that time; a chosen
seed always generates deterministically.

Seed, size, scale and the structures switch persist through save/load and
Recreate World. Older Legacy saves default to Small biome scale. Seed 0 works.
The mod must remain installed to open a saved Legacy generator.

For a new dedicated-server world:

```properties
level-type=legacy
legacy-world-size=large
legacy-biome-scale=small
level-seed=123456789
```

`legacy-world-size` accepts `classic`, `small`, `medium`, or `large`;
`legacy-biome-scale` accepts `small`, `medium`, or `large`.
`legacyconsole:legacy` also works for `level-type`. Existing saves use their
stored generator rather than these creation properties. The matching server-side
Join Compatibility add-on permits clients without the mod to join. Update the
main mod and that optional add-on together to the same version.

## Changes from 2.1.0

The surface writer's packed X/Z indexing is corrected. Biome queries during
decoration, weather and client tinting now use the original two-dimensional PS4
Voronoi lookup, including cloned generation-region biome managers. This removes
the mismatched chunk squares, strips and surface/tint boundaries caused by the
old indexing and Java's three-dimensional biome sampling.

Legacy worlds now use their own PS4 structure dispatcher and saved piece graphs,
rather than asking Java's structure features to assemble them. Covered structures
are villages, mineshafts, strongholds, desert and jungle temples, swamp huts,
igloos, ocean monuments, woodland mansions, shipwrecks, ocean ruins, outposts and
buried treasure. Java-only structures such as ruined portals are not selected by
this dispatcher. Locate commands and Eye of Ender positions use these starts.

The port includes 450 original Overworld NBT templates, their source hashes,
351 village-element definitions, 38 weighted pools, and 17 captured processor
lists containing 48 rules. Village assembly follows the PS4 depth-first jigsaw
search, fallback pools, two-dimensional collision tests, depth limit, finite
bounds, terrain projection and shuffle order. Captured rules cover crop
substitutions and maturity, path bridges over water/ice, desert paths and wear.
Template block names, properties, jigsaw replacement strings, block entities and banner colours are converted
to their 1.17.1 equivalents at load time; the bundled source template bytes remain
unchanged, wrapped in GZIP.

**Use a new Legacy world to evaluate these changes.** Existing generated chunks
are not regenerated. An existing world can therefore retain old surfaces or
structures next to newly generated terrain. Back up existing saves before an
upgrade; the update does not rewrite those chunks.

Shipwreck choices now use the recovered ship-specific region seed. Their four
minimum-height samples are taken within the actual rotated target template
footprint, preserving the PS4 sinking depths. Sampling the previous preview
chunk could place ships above water when that neighbouring chunk was a cliff.
Regression cases use WUBZY, Large world, Small biomes. This footprint correction
is a target-runtime adaptation, rather than a claim of identical PS4 placement.

Jigsaw replacement strings are validated separately from palette entries. The
bridge converts old wall-direction booleans, trims original trailing spaces,
and repairs the original savanna terminator's truncated `grass_` road name using
its surrounding path cells. These conversions leave the source NBT unchanged.
All 41 distinct replacement strings parse successfully under 1.17.1.

## References and recovery

[Nejirus/mcps4](https://github.com/Nejirus/mcps4) is pinned to
`37e258085773f11599cf0ffe29119926769554b7`. It contains packaged game files and
an unencrypted PS4 SELF executable, rather than C++ source. Recovery uses the
existing unencrypted segments, exception-frame function boundaries, C++ RTTI,
vtables, targeted Ghidra 11.2.1 decompilation and isolated original routines.
The application entry point, game startup and platform services are never run.
Original templates come from `Common/Media/MediaOrbis.arc` in that revision.

The readable C++ comparison is
[MinecraftLCE-7thGen, acff48c2c7e18406fd6000b56945746ca9a6eb69](https://github.com/chrisGrando/MinecraftLCE-7thGen/tree/acff48c2c7e18406fd6000b56945746ca9a6eb69).
Its random/noise, biome, cave/ravine, finite ocean/collision and shared structure
algorithms provide readable references. **mcps4 is the authority for this port.**
A match to the readable LCE source alone does not establish PS4 parity. Recovered
PS4 routines take precedence where they differ, including the newer jigsaw villages. Shared Java structure placement
expressions are adapted to the target runtime with recovered PS4 placement,
randomization, finite rules and original template assets.

`WORLDGEN-PS4-REFERENCE.json` records revisions, hashes, original addresses,
validation and remaining adaptations. `templates.json` records every bundled
Overworld template's original SHA-256. Executables, Ghidra, analysis projects,
private analysis artifacts, game JARs and platform SDKs are excluded from releases.

## Ported core and direct validation

The core retains the original 48-bit RNG, permutations, noise octave ordering,
coordinate wrapping and rounding; PS4 biome graph and finite byte cache;
5 × 17 × 5 density lattice, single-precision biome weights and interpolation;
32-block ocean falloff; default surface materials and bedrock RNG consumption;
recovered PS4 air and underwater cave/canyon routines; and the recovered single
stronghold position and finite structure-candidate rules.

Private disposable x86-64 oracle processes execute isolated original pure
routines using verified host allocation/profile bindings and captured biome
objects. Ordinary JSON outputs are shipped as test data. Comparisons require
exact equality for the following named components:

* Five RNG/permutation cases, 20 point-noise values and 1,380 octave-region values.
* 25 original biome/Voronoi rectangles and ten complete finite biome-map hashes,
  covering 2,601,472 cells; another 24 rectangles cover Medium/Large biome scales.
* 6,800 density values, 4,096 surface-noise values, sixteen complete base buffers
  (524,288 voxels), and 24 complete default-surface columns plus following RNG.
* 1,440 controlled biome-surface cases plus following RNG and mutable top tiles;
  four complete buffers after shared surface generation and all four carvers.
  Five further canonical carver buffers verify metadata, including 335 writes
  whose tile ID remains unchanged.
* 20 stronghold positions across five seeds and all four sizes.
* 36 finite structure-candidate cases, including negative cells and rejections.
* Three complete mineshaft piece graphs: 546 piece types and bounding boxes in
  original order, plus the following RNG output.
* Six captured balanced-seed predicate/score cases.
* Four original shipwreck constructor choice cases: wood palette, main/beached
  variants and rotation. The synthetic height binding is not a terrain oracle.
* Twelve complete controlled carver buffers, 32,768 voxels each, after the original
  underwater-cave, underwater-canyon, air-cave and air-canyon sequence. Inputs
  cover warm/cold land, oceans, mushroom grass, mixed materials and biome gates.
* 100,124 direct libc `sinf`/`cosf` input pairs, compared by float bits, plus the
  SHA-256 of all 65,536 original Mth sine-table entries. The direct translation
  covers libc's bounded/medium argument paths used by these carvers. Another
  101,085 original double-cosine results validate the badlands math path.

Separately recovered ore/deposit kernels match 200 individual veins, 24 complete
ore-dispatcher passes, 96 submerged patches and 216 lakes, including block-probe/write order
and following RNG. The translated population dispatcher now runs these kernels in Legacy worlds
through an explicit Minecraft/Forge world adapter. See `PLAYABLE-233.md` for
target-version adaptations and the remaining parity limits.

Additional tests load and hash every original template, validate processor
behavior, save/reload custom pieces, check real asymmetric desert/grass surface
columns, and exercise codecs, all world sizes, structure switches, explicit seed
zero, Nether/End preservation and world-space lava lighting.

## Target-runtime adaptations and accuracy limits

This is a port into Minecraft 1.17.1, not a PS4 save importer or a claim that every
PS4 seed produces identical blocks. Procedural structure block-placement helpers,
loot, mobs and twelve small village decoration-feature adapters use target-game
APIs and shared Java expressions. Complete village/stronghold/monument/mansion
piece graphs and final structure blocks have not all been proven equal to the
PS4 executable. The mineshaft graph comparison above does not prove equality of
every placed block or loot item.

Badlands bands, swamp pools, frozen-ocean surfaces and their biome wrappers
now use recovered PS4 routines. General trees, vegetation, ores and non-structure
decoration still use 1.17.1 features. The carver voxel comparisons above do not establish full
Overworld parity. See [PS4-PORT-233.md](PS4-PORT-233.md) for the current development
scope and capture reproduction instructions. Whole-world block output and the
complete population pipeline still require independent PS4 validation.

Chunk storage and native block/entity mechanics remain Java formats. No new
blocks, items or biomes are added to synchronized registries. An optional mod-only
packet supplies the finite profile for client biome tinting and the hidden wall.
The shaderpack and native 1.17.1 title panorama are unchanged by this release.

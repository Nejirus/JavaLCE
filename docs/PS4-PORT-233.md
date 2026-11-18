# PS4 port: 2.3.3 source evidence

The 2.3.3 playable release does not claim full seed parity.
The requested target is PS4 **Overworld** generation in the Legacy world types.
Nether and End generation remain native to Minecraft 1.17.1. The requested
beehives, deepslate and geodes are explicit exceptions to the PS4 baseline.

## Authority

The implementation target is [Nejirus/mcps4](https://github.com/Nejirus/mcps4),
revision `37e258085773f11599cf0ffe29119926769554b7`. Readable LCE code is a
cross-reference. LCE equivalence alone is not evidence of PS4 parity.

The repository supplies packaged PS4 files, including an unencrypted executable,
rather than the original C++ source tree. Java translations follow the recovered
PS4 routines, including their RNG consumption, precision and unusual loop bounds.
Recovery artifacts are checked against bounded execution of the original code.

| File | SHA-256 |
| --- | --- |
| `eboot.bin` | `c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8` |
| `sce_module/libc.prx` | `1936f5700cc590877d9bf493c6a6bb7714e84003dfd6d74a7d77b1869f71d816` |

## Recovered map rendering

Map renderer `0xc6fba0` uses a 128 by 128 image quad at depth `-0.02` and a
coordinate-text transform at depth `-0.06`, with draw origin `(0,0)`. The custom
14-pixel translation, 100/128 image scaling and coordinate-text shrinking have
been removed. Original unexplored palette entries are transparent, as recovered
from `0xc6f870`.

Classic's inset comes from the map data: 108 mapped pixels with ten unexplored
pixels per side. An additional geometry inset would apply that margin twice.
The existing PS4 paper texture is preserved. The map fixture records the original
vertices, texture coordinates, text transform and first four palette entries.

## Recovered carving

The original Overworld `getChunk` routine, `0x508360`, calls underwater caves,
underwater canyons, air caves, then air canyons. This order is retained.

| Component | Original routine |
| --- | --- |
| Air feature chunk seeding | `0x3df5b0` |
| Air cave starts / tunnel / voxel helper | `0x3df290` / `0x3de090` / `0x3deb80` |
| Air canyon starts / tunnel | `0x160280` / `0x15f2a0` |
| Underwater cave chunk seeding / starts / tunnel | `0x714f30` / `0x715d50` / `0x7150a0` |
| Underwater canyon chunk seeding / starts / tunnel | `0x713fd0` / `0x714d80` / `0x714140` |
| Mth sine / cosine | `0x496b30` / `0x496c40` |
| Original libc direct sine / cosine | `0xc73b0` / `0xc1770` |

Air cave/canyon voxel indices, lava thresholds, replacement predicates and biome
gates follow the original routines. Underwater carvers retain their separate
odd-multiplier seeding, float voxel tests, water-neighbor writes, magma/obsidian
choices, and direct libc trigonometry. The original float sine table is bundled;
its initialization uses rounded float arguments and the original PS4 library.

The tests compare 393,216 output voxels from twelve controlled buffers after all four
original carving stages. They include negative coordinates, signed seeds, ocean
and cold-biome gates, mushroom grass and mixed replaceable/unreplaceable blocks.
The fixtures use synthetic world callbacks and deliberately constructed inputs;
they are not PS4 save captures or proof of complete generated-world parity.

The direct math fixture compares 100,124 input pairs by float bits, including
branch boundaries and signs. It covers the bounded/medium argument paths used
by these carvers. The very large argument reduction kernel is not substituted
with JVM trigonometry; unsupported finite inputs are rejected explicitly.

The recovered wet-biome gate reads biome depth, not temperature. Ocean category
10 includes all ocean subclasses. Snow eligibility includes both the raw snow
flag and the frozen-ocean accumulation field. Grass repair reads the biome's
mutable surface top tile. These fields are distinct from the class-family IDs
used by the layer graph.

The Java bridge preserves metadata on untouched voxels and clears it on every
original default-state write. Five additional canonical buffers match all
163,840 states, including 335 mutations whose tile ID stays unchanged. Carving
reconstructs its own chunk's final surface top fields from the original surface
inputs, so Java's separate chunk tasks cannot overwrite them between stages.
The complete surface/carver fixtures also check this interleaving case.

The original Mth table's uncompressed SHA-256 is
`a994dcf0a00f927939600f8f242e634741a8e6e544745851a263bb4ee7206051`.

## Recovered base terrain and biome surfaces

The original noise/density/interpolation pipeline matches 6,800 density doubles,
4,096 surface-noise doubles and sixteen complete base buffers (524,288 voxels).
Biome rectangles and depth/scale fields are controlled inputs. This validates
the named numerical pipeline, rather than independently validating every biome
constructor or a complete PS4 world.

Badlands (`0x46aa40`, band initialization `0x46b130`), swamp (`0x6ca1e0`) and
frozen-ocean (`0x4d7660`, temperature `0x4d7380`) surfaces now use translations of
the original routines. Mountain, giant-taiga and mutated-savanna wrappers also
follow their original branches. Warm/lukewarm seabeds retain the original sand
and depth cutoffs; Ice Spikes uses snow blocks. Java surface builders are no
longer used for these Legacy terrain surfaces.

The frozen-ocean prepass writes the physical X/Z column; the shared surface pass
writes the transposed packed column. The full chunk buffer and shared RNG retain
this ordering. The original first-use cached seed is zero for badlands/frozen
noise initialization. Tests cover fresh generator instances, not every original
global-cache history after switching worlds within one PS4 session.

Original-code fixtures compare 1,440 controlled surface cases, including both
frozen-ocean columns, following RNG and mutable top tile. Four complete buffers
also match after the original shared surface loop and all four carvers. These
use supplied biome/noise/voxel inputs, not PS4 save captures. Badlands double
cosine is checked against 101,085 original libc results by bits.

All 72 registered biome constructors now have an independent original-code
capture. The complete biome initializer and constructors produce climate float
bits, class families, temperature groups, snow/rain helper results and decoration
counts; all 5,184 ordered biome-equality pairs also match. The numeric preset
stores execute as an audited bounded span, returning before the next allocation.
Name-map/registry/default/property storage and nine feature-constructor/deletion
bindings are supplied, so feature geometry does not run during this capture.
The original frozen-ocean ID 10 now reports the Ocean temperature group.
New frozen oceans retain their separate 0.125 snow-accumulation setting, and
warm-ocean configuration includes four sea-pickle attempts. These constructor
captures independently validate the numeric biome values used in the earlier
controlled terrain captures; they do not turn those captures into world saves.

## Recovered population kernels

`OreFeature::place` (`0x503f60`) uses float-rounded endpoints, inclusive vein
steps, fixed-point distance tests and a visited-bitset branch that skips its
index increment after an already-visited voxel. These instructions are retained.
The original stone predicate (`0x504a30`) and StoneType initializer (`0x67f1f0`)
confirm that ordinary stone and unpolished granite/diorite/andesite are eligible.
Two hundred controlled cases match 6,165 block writes, 10,829 ordered block
probes, height/rule queries and following RNG. Forty-two predicate cases match.

The original ore dispatcher (`0x88970`) and settings constructor (`0x1f1940`)
establish all eleven passes, including diorite before granite, exclusive upper
heights, equal/reversed range handling and lapis's triangular height selection.
Twenty-four complete controlled passes match 976 feature calls, 28,491 writes,
44,549 ordered block probes and following RNG. Their original kernels run within
the original dispatcher; world access and legacy state bindings are controlled.

Submerged clay (`0x181400`) and sand/gravel (`0x5c3e70`) kernels match another
96 cases: 1,634 writes, 8,536 ordered block probes, rule queries, return values
and following RNG. Clay's original pre-increment loop visits Y-1 through Y+1;
sand/gravel visits Y-2 through Y+2. Material and block callbacks are controlled.

The lake kernel (`0x3db900`) matches 216 original-code cases: 26,472 writes,
46,828 ordered block probes, light/biome/temperature/rule queries, return values
and following RNG. These cover both liquids, terrain and rule rejections,
mycelium floor repair, lava stone shells, source-water freezing while leaving nonzero fluid levels
unfrozen. The original water-freezing pass checks the Y+4 plane, including
existing source water outside the carved mask. The fixtures use controlled
world/material/light/biome callbacks; the complete PS4 lighting engine and lake
locations are not independently validated by these cases.

The complete population dispatcher (`0x508bb0`) matches 312 controlled cases,
including the two signed odd seed multipliers, all ten structure passes,
village lake suppression, desert identity checks, disabled-lava RNG draws,
dungeons, biome decoration, mobs, ice and snow. Feature callbacks consume a
controlled original `nextLong`; this validates dispatch/RNG flow, not the
remaining feature implementations or complete worlds.

The dungeon kernel (`0x497e40`) matches 264 cases, including 168 completed
rooms: 28,466 writes, 27,100 removals and 138 chest loot seeds. Original chest
facing (`0x1746f0`) matches all 1,024 controlled neighbor/facing combinations.
Material solidity and solid rendering remain separate. Original mob selection
and its static initializer establish Skeleton/Zombie/Zombie/Spider; RTTI
virtuals independently verify the entity keys. The original loot reference is
`minecraft:chests/simple_dungeon`; its contents still need PS4 validation.

Eight plant scatter kernels match 5,376 cases and 17,227 writes, with 15,636
additional original placement/survival rule cases. They cover flowers, tall
grass, dead bushes, lilies, cactus, reeds, double plants and mushrooms. Reeds distinguish
block water from fluid-state water; lily generation uses a block-identity rule
that differs from its survival rule. Cactus/reeds recheck the original base
position for every stack level. Double-plant upper states retain the native
default variant and obtain their displayed type from the lower half at the
Java block bridge. Original plant rules run inside original scatter captures;
the supplied voxel/material/fluid contexts are not complete world saves.

The main biome decorator (`0x87760`) matches 480 full call sequences, totaling
39,383 ordered events. Original constructor (`0x86c00`) count/flag stores
establish defaults. Cases include enabled, zero and negative counts, height
edges, nonzero origin Y, tree prepare/post calls, flower selectors, the ten
unconditional reed attempts and both spring passes. Controlled feature calls
consume original RNG; feature geometry and biome-specific selectors still need
their own ports and integration.

Huge mushrooms (`0x370120`) and melons (`0x4642c0`) match 2,616 cases, 41,435
writes and 219,408 ordered probes. These cover random/forced mushroom types,
the rare doubled height, both update flags, opaque/leaf obstacles, soil
rejections and replaceable plants. Mushroom variant IDs come from original
initializer immediates in `0x3d86e0`; supplied state/property storage preserves
their identity. Mushroom scatter survival includes original mycelium/podzol
exceptions and the strict brightness-below-13 test.

Seven tree kernels (ordinary/jungle, birch/tall birch, pine, swamp, ground bush,
savanna and dark oak) and their shared trunk/fallen-log helpers match 23,712 cases:
441,785 writes and 1,356,299 ordered probes, plus protection/height/support
queries and following RNG. The one-in-80 fallen-log branch, species-specific
vine probabilities, cocoa, hanging-vine length/probe order, existing dirt
variants and each tree's distinct 128/256-block height checks are retained.
There are 768 original replacement-predicate cases and independent native
log/vine/cocoa metadata checks. Those metadata checks caught and corrected
reversed vine attachment bits in the draft state adapter. Feature heights and
states, materials and fallen-log support queries are supplied inputs; biome
selectors, remaining tree kernels and complete original worlds remain pending.

Giant spruce/jungle trees, their shared height/clearance/leaf-disc helpers and
the spruce podzol pass match another 5,760 cases: 549,816 writes and 2,471,634
ordered block probes. The canopy's float-rounded radii, the two distinct leaf
discs, jungle branch rounding and per-column vine draws match original code.
The podzol pass preserves its four fixed centers, five random edge candidates
and vertical soil search. Feature configurations and block/material/property
bindings are controlled; biome selection and complete world integration are
still pending.

Big oak's original constructor, preparation and complete geometry match 2,912
constructor cases and 2,976 placements, including reused feature instances:
382,556 writes and 417,006 ordered probes. Leaf-node graphs, shortened height
limits, branch axes and both following RNG outputs match. The internal RNG is
seeded from the population RNG's `nextLong`, while the first height draw uses
the population RNG itself. Independent original leaf metadata checks cover all
16 species/decay combinations. Voxel/material/property bindings and default
log/leaf states are controlled; this does not validate the complete registry
initializer or whole-world population.

Ordinary spruce (`0x672fb0`) matches 1,408 further cases: 40,632 writes and
234,570 ordered probes. This is a distinct generator from pine, with air/leaf
clearance, farmland support, shared fallen-log/trunk randomness and its own
alternating canopy radius sequence. Cases cover successful/fallen/rejected
trees, protection boxes, height limits and both update modes.

Ten biome tree selectors and their constructors match 8,256 choices, followed
by 1,152 complete selector/prepare/place/post lifecycles: 116,420 writes and
331,861 ordered block probes. All eleven tree types are selected, including
forest/taiga variants and bamboo jungle's distinct chances. Original species
enum initialization, 64 final block-default property assignments and 23 cached
state bindings establish species and decay flags. Giant spruce intentionally
retains its default leaf CHECK_DECAY flag; ordinary spruce/pine clear it.
Base block construction, StateDefinition storage/factories, materials and world
contexts are supplied. This is not full block-registry initialization or
whole-world validation. The translated selectors bind to the recovered tree
kernels through the original prepare/place/post lifecycle.

Flower/grass selectors match 5,916 flower and 2,064 grass cases. Original flower
enum objects establish all twelve names, metadata and selection-table ordering;
original configuration/property getters validate the selected states. Native
seed-2345 biome noise is compared by double bits; separate bound values cover
the strict plains threshold and flower-forest clamp edges. These captures
caught reversed poppy/dandelion aliases in the draft and establish the original
forest lily-of-the-valley choice, blue-orchid substitution, plains tulip order
and extra grass/fern draws. State/property storage and exact boundary noise
inputs are supplied; the ordinary-coordinate cases execute original noise.

Standalone vines match 49,680 attachment cases, all 345 recovered block
identities and 384 complete placements. The original libc RTTI cast runs against
the original Block/ShulkerBox type information; explicit exception comparisons,
above-block checks, face-query order, vine bits, the 128-block ceiling and
blocked-position RNG are preserved. Face-shape virtual results, other block
identities, voxel contexts and property storage remain controlled inputs.

Block/alias instruction recovery independently matches 344 registered IDs/names
to 345 named lookups; air is looked up here but registered elsewhere. This
establishes newer IDs, including natural pumpkin 257, bamboo 319, berry bush
363 and purple shulker box 260. It does not execute the complete block registry.
The native loader now retains imported-data relocation addends so original C++
RTTI vtables can be used. Earlier tree/flora selector captures reproduce with
byte-identical hashes after that correction.

Taiga boulders match 3,840 original BlockBlobFeature cases, including the original
X/Y/Z position iterator, the literal `0.333f` radius multiplier, flags 4, ground
search and the offset draws after every blob, including the last one. Target
default states, radii and voxel contexts are supplied inputs.

Natural pumpkins match 528 full scatters, 384 placement-rule contexts and 16
final default-property assignments. The original state-definition factory
lists only horizontal facing; original metadata getters and the scatter's
property-membership checks establish its facing draw and absence of an age
draw. The default facing is south (metadata 0). The main decorator and its
controlled capture now use recovered natural-pumpkin ID 257; ID 86 denotes
carved pumpkin. World support results, materials and canonical property storage
remain supplied inputs, while original placement-rule/probe/RNG code executes.

Ten biome-specific decoration wrappers match 5,304 complete call sequences and
168,921 ordered events: forests, savannas/mutated savannas, plains, swamps,
taigas, jungles/bamboo jungles, deserts and hills. Original tree selection,
constructors and biome noise run within the captures. Supplied geometry/base
callbacks consume controlled original RNG, with no draw for the RNG-free well.
Cases include the roofed-forest grid, double-flower retries/absolute Y, changed
plains counts, taiga berry/boulder/fern ordering, jungle vines/bamboo, bamboo's
float-rounded density and final extra X draw, and hill emerald/silverfish passes.
Rare cases include both a desert well and fossil in the same pass. Heights,
geometry outcomes, initial counts, variants, temperatures and some exact noise
boundary values are inputs; these sequences are not complete population or
whole-world comparisons. Remaining feature kernels and biome-constructor
settings still need completion before integration.

Bamboo, podzol and sweet berries match 2,408 original kernel runs and 588
placement-rule contexts, with 14,886 writes and 113,073 block probes. Original
property lists, 32 final default-state assignments, metadata getters and the
berry AGE range constructor also run. Coverage includes coordinate-hashed
bamboo heights, tip writes over obstacles, mixed soils, height edges, all four
berry growth stages and podzol's repeated soil probes. Block base construction,
canonical state storage, registry lookups, materials and successful voxel writes
are controlled; these are bounded feature comparisons.

Desert wells match 5,880 original placements, including every foundation
cell with one or two unsupported layers, low heights and descent to the surface.
Original constructor stores, sand/slab variant initializers, the sand predicate
and slab metadata getter establish the bottom sandstone slabs, ordinary-sand
requirement, flowing-water pool, 96-write geometry and absence of RNG draws.
Enum-property/StateDefinition/canonical storage, half-enum identities, block
defaults and world contexts are supplied inputs.

The fossil dispatcher and chunk RNG match 1,728 original call sequences and
624 independent four-value RNG vectors. Original template dimensions are read
from all 16 unchanged fossil assets. The dispatcher retains its square height
scan using rotated width, its inclusive Y=256 clipping box, translated origin,
0.9/0.1 bone/coal integrity and independent chunk RNG. Seed vectors cover signed
world seeds, 32-bit square overflow and unsigned extension. Rotation registry
storage/order, heights and template placement are controlled inputs; geometry
callbacks consume explicit original RNG draws. Those dispatcher-only sequences do not establish whole-fossil parity; the
separate template-geometry comparisons below add original placed-block coverage.

Springs match 7,968 original placement/immediate-tick sequences, with 224
source-block writes and 26,352 ordered block probes. The cases cover horizontal
neighbor combinations, early rejection, stone metadata, protection, water/lava
and the true/tick/false immediate-update flag sequence. The fluid tick itself
and thread-local storage are supplied callbacks; fluid propagation remains to
be translated before integration.

The fossil path through the original template routines and integrity processor
also matches 2,304 complete geometry runs using all 16 unchanged fossil assets.
Comparisons cover bone-axis rotation, ordered writes, following RNG, world probes,
clipping and neighbor notifications, including notifications for original entries
rejected by the integrity processor. The second integrity draw gates replacement
processing and its world probe, while writing still occurs. Original NBT block
order/palette/positions are supplied to the template container, then the original
loader's block sort executes before placement. The original NBT parser is
outside this capture. Rotation registry order, loaded-column status,
canonical property/registry storage and notification effects remain controlled.

The original dynamic-liquid tick and flow helpers now match 5,856 cases and
926,784 ordered events, including 10,096 primary/secondary writes. Coverage
includes finite borders, two fluid layers, source regeneration, falling/decaying
levels, slope-search probes, lava slowdown RNG, drops and water/lava reactions.
The original immediate-update queue processes exactly 100 requested updates and
discards the remainder; 32 replay cases verify that limit. The slope search
checks the lower target face against the current search origin, retaining its
original diagonal probe. Canonical registry/property/deque storage, material/
face/drop classifiers, dimension flags and successful voxel APIs are supplied.
The fire game rule and feature15 are false. World notifications, schedules,
drops and fizz effects are observers except for explicitly requested replay.
Native state-code inputs cover IDs <=255; the full registry and world callback
effects still need integration. This does not yet establish complete spring
propagation in an original world.

The lava ignition helper also matches 4,608 cases, split equally between direct
calls and calls inside the original liquid tick. It retains the ten independent
offset attempts, default-state material checks, unsigned Y test, loaded-chunk
early exit and first-air-neighbor fire placement with flags 11. These comparisons
include following RNG and ordered world events. Game-rule, block-default,
flammability, loaded-chunk and direction-vector inputs are explicit; writes
succeed and downstream fire/world callback effects remain controlled. The Java
liquid port now calls this helper directly when the fire game rule is enabled.

Kelp, seagrass and sea-pickle generation and support rules match 5,880 original
rule calls and 2,800 feature runs: 21,720 writes, 245,949 block probes, effective
fluid/collision queries and following RNG. The original kelp cache contains 16
growth ages. Seagrass retains its busy byte after failure, clears it after the
scatter and checks the world's reentry flag only when already busy. Original
block constructors/getData and integer-property constructors verify metadata;
sea-pickle defaults are supplied storage that its constructor does not overwrite.
Solid-blocking/slab/byte10 classifications, effective-fluid/collision boundaries,
client/reentry flags, property storage and successful writes remain controlled.
These captures do not include complete registry initialization or downstream
notification/waterlogging effects.

Blue-ice growth, ice patches and ice spikes match 3,232 original feature runs,
320,804 writes, 495,655 block probes and following RNG. Coverage includes the
blue-ice neighbor-count limit, patch targets/radii, float taper and edge draws,
rare raised spikes, root gaps and the Y=50 root cutoff. Original shared feature
write flags are retained. Direction storage/order, defaults, canonical/material
storage, patch configuration, sea level and successful writes are supplied;
downstream world effects and full world comparisons remain pending.

The complete iceberg constructor, generator, smoothing and carving routines
match 260 runs, 193,233 writes, 1,025,740 probes and following RNG. The cases
include 26 elliptical shapes, the rare blue-ice choice, snow-layer cleanup and
11 reused-feature placements. Ellipse parameters remain cached when a later
placement selects a round iceberg. The original libc double sine port also
matches 100,146 results by bits; cosine retains its existing independent check.
Canonical/material/default storage, direction storage, sea level, voxel contexts
and successful writes are supplied. These are complete feature geometry runs,
not complete original world or registry captures.

Frozen/warm-ocean and ice-biome wrappers match 5,376 original sequences and
175,488 ordered events, including following RNG. Ice spikes/patches run before
base decoration; frozen-ocean and warm-ocean additions run after it. Warm oceans
retain four coral calls, the separate coral-crust chance, sixteen hanging-coral
calls, twenty sea-anemone calls and the pickle gate/count. Pickle Y is sampled
as an absolute height. Six decorator feature bindings are checked against the
original constructor's vtable assignments. Base/feature callbacks consume a
controlled original nextLong, with supplied heights/outcomes/configuration;
this validates wrapper flow, not combined population geometry.

Hanging coral, sea anemones and the coral generator's floor/side-fan helpers
match 3,720 original support-rule cases and 5,488 kernel runs. Another 64 shared
RNG sequences run hanging coral and desert wells with a retained horizontal
direction array. Together these capture 14,282 writes and 37,206 block probes.
Original property factories, definitions, constructor assignments and metadata
getters verify all seven coral block identities. The full coral-block variant
property has six values, although the generator samples five colors.

Hanging coral swaps failed candidates in the process-wide horizontal direction
array. The Java ports now accept a shared `Ps4DirectionPlane`; desert wells and
liquid source-neighbor scans read the same mutable order. Original fluid ticks
also match 768 cases across all 24 supplied permutations and 983,328 ordered
events. The latter use the fluid harness's controlled boundaries and supplied
permutations, rather than a combined original coral/fluid world. Coral material,
support, collision, effective-fluid, loaded-chunk and canonical storage inputs
remain controlled; writes succeed. The main shape comparison is described
below; these captures do not include coral-crust templates or downstream world
effects.

The main coral feature and all four shape routines now match 14,952 original
runs, 121,280 writes, 198,037 block probes, ordered top/side candidate lists and
following RNG. The comparison includes each low-level cross/diagonal/branch
helper, the deliberate switch fallthrough, the full fan follow-up passes,
same/foreign/dead coral contexts, supported/unsupported terrain, collision
outcomes and height/client boundaries. The repeated diagonal, duplicate base
writes and duplicate side candidates are retained. An existing live coral block
below the starting water cell rejects the placement before any random draw.
World/material/default/property/collision inputs are controlled as above;
coral-crust templates and downstream effects remain separate work.

Coral-crust dispatch and its two offset helpers match 4,336 original sequences,
32,712 template/callback events, 57,846 block probes and following population
RNG. All eleven original crust/outcropping asset IDs and all 24 supplied rotation
registry orders are covered. Rotation uses an integer draw from a value copy
of Random and leaves the original population stream unchanged. `Ps4Random`
supports this operation; it also passes the full main-coral geometry fixture.
The separate template random constructor retains its original 32-bit X term
and unsigned Z-square term. Template placement and marker postprocessing remain
controlled callbacks consuming zero, one or three native nextLong values; their
geometry and world effects are not part of this dispatcher comparison.

The crust marker postprocessor and its scatter/fan helpers match 9,792 original
runs, 21,048 successful writes, 96,794 block probes and following RNG, including
rejected writes. The original repeated side candidates and the draw skipped
after a failed upward write remain intact. The top-fan helper omits the main
coral helper's extra current-state probe; the crust side-fan helper requests
numeric coral-block placement before writing a wall fan. Direction facing bytes
are bound from the original constructor argument stores. Marker positions and
their iteration order are explicit inputs here; original template marker
extraction/hash-map order, template geometry and downstream effects still need
their separate comparisons.

The original marker collector's linked hash-map order now matches 416 captures
and 53,328 returned markers. These include all eleven crust assets at four
rotations and shifted origins, plus filtering, collisions, duplicate keys,
coordinate overflow and rehash boundaries through 1,024 buckets. Position hash
arithmetic, bucket grouping, list splices and first-value retention are
translated from the native container routines. Original template order, block
identities and NBT metadata strings are supplied inputs; NBT/property parsing,
template block geometry and downstream world effects are outside this fixture.

The loader's block sort is translated separately, including median selection,
partitioning, insertion and heap fallback. Its signed packed X/Z comparison
ignores Y, and equal-column order follows the native swaps. All 27 fossil/crust
assets plus synthetic cases match 357 original runs and 45,147 ordered entries.
Both Java template readers now apply this sort. The fossil geometry fixture has
been regenerated with the original sorting routine before placement.

The old `variant=dead_coral` palette string sets dead=true and chooses a color
from Mth's process-wide random stream. The module initializer constructs that
stream with the original clock-based Random constructor, independently of world
seed. Explicit clock returns and preceding shared draws match 784 native runs
and 8,624 palette conversions, including the following RNG. The port accepts
the shared stream rather than inventing a seed or fixed dead-coral color. Full
registry loading and the process-wide history remain integration work.

Crust template placement, native marker collection/hash-map ordering and marker
decoration now also run together against the original executable. The 1,926
comparisons cover all eleven unchanged assets, four rotations, two supplied
cache-load/clock contexts, coordinate overflow, height edges, rejected writes,
unavailable columns and placeholder chunks. They compare 686,712 successful
writes, 1,412,964 block probes, column/chunk checks, neighbor notifications,
block-entity lookups and following population RNG. Paired clock inputs leave
integrity-one placement unchanged; its native branches draw neither template
stream. Canonical
storage, stone/sand mapping, ignored marker mode metadata, NBT fields, registry
rotation order and downstream world effects are controlled inputs. This adds
combined feature evidence; complete world generation remains pending.

Mesa's ore-decorator override runs the base pass and then twenty additional
attempts with its configured gold feature at Y in [32,80). The shared RNG and
feature geometry match 216 native passes, 13,104 feature calls and 233,981 writes.
The extra pass remains active when the base ore counts are zero. The override
binding is checked against the original Mesa constructor's vtable assignment;
world/canonical/configuration/protection inputs remain controlled.

The pinned population mob helper, `0x4a6450`, is a single RET. Another 312
original dispatcher runs leave that helper unmodified and verify that it draws
no RNG. The Legacy generator's generation-time mob hook now follows that empty
helper instead of calling Java's chunk-generation spawner. Other population
features remain controlled in this fixture; ordinary ongoing mob spawning is
outside the helper.

The final population freeze predicate matches 15,340 native comparisons across
all 345 registered block identities, both water IDs and every water level,
height/light boundaries and adjacent float temperatures. It permits source water
at exactly 0.15f and block light at most nine; the native unordered comparison
also retains its NaN behavior. Temperature, light and canonical state storage are
supplied, with query order and block-probe hashes compared. Snow placement is covered separately below; world/biome integration remains
pending.

Snow eligibility, neighbor selection and layer increments match 5,472 original
runs and 1,599 writes. The native rule uses block defaults for eligibility and
actual state for incrementing layers. Fixed N,S,W,E order, origin-chunk height
queries, duplicate temperature/light checks, failed-write returns and maximum
snow-depth gates are retained. Nineteen limit-conversion cases also verify the
native float-to-integer behavior. All 72 biome constructors confirm that only
frozen oceans 48/49 have a nonzero maximum depth, two. Default/material/climate
storage, rule booleans, available chunks, supplied section occupancy and write outcomes are inputs. The
combined snow comparison also executes uncached precipitation-height scans.
Complete registry initialization, compressed storage and downstream physics
remain integration work.

The original precipitation-height routine and highest-section selector match
2,432 contexts and 383,412 events, with two consecutive queries per context.
The scan reads both state layers before testing primary motion blocking, primary
liquid and secondary liquid. Cache byte indexing, the original top-height byte
conversions and repeated scans of invalid entries are retained. Tests compare
complete cache hashes and query/classifier order across supplied section masks,
material combinations, signed coordinates and height edges. The compressed
section accessor's TLS/lock wrapper is supplied as its emptiness result; actual
compressed palette decoding and full world-write effects remain pending.

The chunk-write precipitation-cache invalidation prefix now matches 48,340
native cases across every initial byte and signed coordinate/height boundaries.
It invalidates before the unchanged-voxel test, including writes that return
false. The native comparison takes two controlled early-return paths: fast
null-storage writes and ordinary unchanged-voxel writes with no block entity.
Neither path executes compressed voxel writes, TLS, lighting or neighbor
physics. Direct chunk calls with out-of-range Y verify the helper; the separate
world wrapper rejects those Y values.

A second height comparison now runs native constructor-selected materials and
BlockState material getters through the original height scan for every one of
the 345 registered block identities. It verifies 4,140 supplied column contexts
and 2,886,826 ordered events, including both layers, mixed fluid/solid columns,
repeated cache reads and height-byte overflow. No material classifier callbacks
are supplied in this comparison. Columns, occupancy, cache and prototype state
storage remain controlled; full storage and physics are outside this test.

The pure compressed-section routines now have literal Java translations:
emptiness (`0x35a090`) and raw packed lookup (`0x35bc60`). A shared native
fixture covers 25,792 explicit byte layouts and 1,650,688 cell reads across all
eight header modes, selected/unused palette entries, mixed/uniform bitplanes,
section offsets, header flag 0x1000 and optional outputs. Mixed bitplanes make
the emptiness routine return false even with zero palette values. Its output
is a four-block slice top, and empty sections leave that output untouched.
Subcube zero ignores the header payload offset. Raw lookup preserves the full
16-bit value; the separate TLS wrapper applies a 0x7fff mask.

These raw byte layouts are supplied inputs, including redundant palette cases.
Original compression construction/mutation and locking wrappers are outside the
comparison. The wrapper's third argument is an optional output pointer, not a
layer index; correcting that annotation reproduces the existing height and snow
fixtures byte for byte. Full physical storage integration remains pending.

Secondary storage now follows the original pure lookup (`0x35bf10`) and
wrapper value selection. The two layers share their palette and use separate
index planes; raw mode stores two sets of 64 words. Header flag 0x1000 gates
stored secondary data. A high bit in the primary raw value makes the wrapper
select packed source water (0x90), even when no secondary payload is present.
The primary wrapper separately masks its return with 0x7fff.

The native comparison covers 27,840 supplied byte layouts and 1,781,760 cells,
comparing raw primary, stored secondary and wrapper-selected secondary values.
A host assembly bridge enters the original value-selection body after TLS and
returns at verified exits before TLS cleanup; the original pure helpers run
unchanged. These are bounded routine captures, with explicit raw storage inputs
and no full platform wrapper or world integration claim. The earlier compressed
fixture reproduces byte for byte after sharing its input-loader code.

State-ID selection now follows the native table-first lookup (`0xb84f0`),
including its metadata-conversion fallback. Item encoding (`0x91bc0`) and
decoding (`0x91c50`) remain distinct from chunk state IDs: item metadata starts
at bit 12, while chunk table IDs have metadata in the low four bits. The native
comparison covers 262,144 lookups and 117 signed/overflow encoding contexts,
including cache hits, fallback targets and callback order.

Registry IDs, ownership, metadata converters and cache values are tagged stubs.
The test deliberately enlarges both supplied tables for safe coverage of every
16-bit selector input; original startup allocates 5,856 state slots and 366 block
slots, and its lookup has no initial bounds check. This verifies selection and
arithmetic, not complete canonical state construction or a safe default for
invalid game inputs. Actual property definitions and table population remain
separate work.

The final registry enumeration loop (`0xb7737..0xb77ea`) is also translated.
It skips null vector entries, computes each packed ID from the block registry
ID and metadata getter, stores the ID in the state, then writes the table slot.
Definition order and repeated IDs preserve the original last write. A bounded
native capture verifies 135 supplied contexts, 24,953 stored state fields and
the complete 5,856-slot table in each case, including duplicate metadata and
reused state references. The original definition getter executes; vectors and
node order are supplied inputs. Synthetic vectors for registered block IDs do
not establish their actual property definitions or final runtime ordering.

Canonical definition tables now follow the complete original constructor
(`0xcf850`), property getter (`0xd04f0`) and property setter (`0xd0550`).
Each property's count is rounded to a power of two, with its own bit shift and
mask. Canonical states occupy the resulting sparse table; unused combinations
remain null. Property reads use identity lookup, and missing properties return
null. Setting an equal value returns the existing state; other values use the
original low-16-bit ordinal mask and can select a null table slot.

The native comparison verifies 171 supplied property contexts, 8,709 reads and
61,188 changes, including canonical identity and callback order. The entire
original constructor, Cartesian iterator, temporary containers and state
allocations execute. Property counts/enumerations/ordinals and boxed scalar
hash/equality are supplied, with three hash contexts. They are not actual game
property schemas. The constructor assumes nonempty value enumerations, and the
setter's first search assumes a present property; invalid contexts are outside
this comparison. Actual block property initialization, shapes and complete
registry/world integration remain separate work.

Integer properties now have their own translated constructor/vector behavior,
including inclusive signed ranges and the original index-zero result when no
value matches. A second native pipeline uses real integer properties and typed
boxes through the complete canonical constructor and getter/setter routines.
It supplies no property count/value/index/hash/equality callbacks. The readonly
original int RTTI data is linked explicitly, alongside allocator/string imports.

This comparison covers 120 property contexts and nine combined definitions,
with 347 reads and 2,916 changes. Names exercise UTF16 small-buffer, heap and
surrogate cases; ranges include empty intervals and signed integer endpoints.
The native cached property hash includes type-pointer bits and remains outside
the numeric comparison. These supplied schemas verify the property type and
canonical pipeline; actual per-block property initialization and geometry are
not established by them.

Boolean properties now preserve the original `true`, `false` enumeration order
and typed zero-index fallback. Eight original constructors and 13 mixed
boolean/integer definitions verify 519 reads and 4,810 changes, including
identity-distinct properties with equal names and queries of the wrong scalar
type. The native run uses real property/box methods throughout, original
readonly bool/int RTTI and pure string/allocator imports. These supplied schemas
still do not establish actual per-block initialization or world integration.

The translated enum constructor keeps its input set's iteration order and
compares enum objects by identity. Native comparisons cover 128 supplied ordered
sets and eight mixed enum/boolean/integer definitions, including 221 reads and
2,015 changes. The original wood initializer supplies enum objects, IDs and
UTF16 names; its downstream property factory is intercepted during that
initializer. A separate original constructor run receives explicit linked sets.
Equal IDs/names on separate objects remain distinct. All property/box methods
in the canonical pipeline execute original code.

The pointer-key set used by enum factories is also translated, including
eight-byte FNV hashing, duplicate handling, linked bucket groups and rehashing.
It matches 1,108 native snapshots in 73 explicit opaque-key contexts, through
the 1,025-element growth boundary. Pointer tokens are explicit inputs. These
tests do not supply a PS4 heap layout or establish actual per-block factory
schemas; host allocation-dependent enum order and cached address-based hashes
are excluded from the numeric comparison.

Concrete scalar class definitions now use 136 original property aliases from
131 initializer calls, including five shared alias pairs. Their literal names,
types and integer ranges are extracted from the pinned instructions and UTF16
data without executing startup. Both direct constructors and factory calls are
included. The original methods for 86 selected concrete class vtables construct
1,303 canonical states; 46 translated `getData` virtuals match their metadata.
This includes the inline empty-definition constructor, class-specific crop age
selectors, unused connection flags, snow's layer offset and combined bit fields.

The native pipeline uses real property/box/definition/getter/metadata methods,
with explicit class vtable inputs, zeroed auxiliary owner fields and the initial
canonical default. Static guard locking is supplied for one capture thread.
Shared aliases retain pointer identity and normalize to their lowest global
alias in the records. Post-constructor defaults, `fromData`, geometry, complete
registry-ID associations, final indexing and world integration remain outside
this comparison. The reference export reproduces byte for byte independently.

The corresponding 40 `fromData` virtuals now match 20,848 native conversions:
all 1,303 canonical states are supplied in turn as the starting default, with
every metadata nibble. Tests check canonical return identity, preserved flags
and the unchanged default field. The translation retains distinct boolean
tests, signed division/remainders, frosted-ice clamping and scaffolding's
original two-bit stability mask. Actual post-constructor defaults and complete
registry/world integration remain separate; out-of-contract metadata inputs
are not part of this comparison.

Constructor-stage property defaults now retain the original requested types
and assignment order for 47 concrete scalar classes. Seventy-four constructor
contexts verify 201 mutations/default stores and final canonical metadata.
Native observers dispatch the original mutation body and the verified original
position-independent default-field store. A second invocation without observers
confirms the same final results. State codes and owner vtables are captured
while live, excluding temporary ancestor definitions that may be freed/reused.

These runs use original material/color initialization and scalar properties.
Known constructor GP arguments come from registration code; text arguments are
explicit empty UTF16 strings and unknown/unused GP inputs are zeroed. This
comparison covers the final-class property assignments, not complete physical
constructor attributes. Registration-time changes, empty base-block startup,
complete indexing and world integration remain pending.

Position-dependent temperature matches 10,018 native float-bit results across
all 72 registered biomes, altitude boundaries and large/signed coordinates.
Original biome constructors supply base temperatures and frozen-ocean noise;
shared noise constructor seeds/counts and Simplex numeric constants are read
from the original initialization instructions. The base altitude adjustment and
frozen-ocean virtual share the translated helper, including the existing frozen
surface path. Registry/property storage remains controlled, and actual world
biome lookup is outside this numeric comparison.

Material traits are translated from the isolated native initializers and class
virtuals. The comparison verifies all 40 initialized material identities, six
classes, 2,592 synthetic flag contexts and 9,216 setter steps across all setter
orders. Solidity, motion blocking, light blocking, replacement, flammability,
solid blocking and tool/push fields remain separate. Original color words are
retained as numeric data. The original initializer bodies use allocator calls
only; no game startup services run. The unwritten trailing-byte accessor at
slot 0x80 remains outside this defined-field comparison.

Block material assignments now follow all 345 registered block identities,
including air. Static instruction flow links registrations to constructors and
traces material origins through 213 constructor functions. Native constructor
prefixes independently select the same materials in three supplied color/enum
contexts (1,035 cases), then the complete selected native base constructor stores
the material and the original BlockState getters read it. The 345 blocks share
38 initialized materials; Java preserves that identity within each context.
Only the two base material stores occur in the traced constructor bodies. Color
inputs, unused arguments and prototype state storage are supplied. Full registry
startup and property/default/shape construction remain outside this comparison.

The feature kernels above are connected through `LegacyPopulation` for the
playable release. The dispatcher shares one RNG across structures, lakes,
dungeons and biome decoration. The world adapter still uses Java operations
and the existing structure piece port; complete parity is not established.

## Reproducing original-code captures

On Linux x86-64 with Python 3.12, supply your own files from the pinned mcps4
revision. The script checks both hashes before executing bounded routines:

```sh
python docs/reference-tools/ps4/export_carvers.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-carver-captures

python docs/reference-tools/ps4/export_surfaces.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-surface-captures

python docs/reference-tools/ps4/export_terrain.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-terrain-captures

python docs/reference-tools/ps4/export_ores.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-ore-captures

python docs/reference-tools/ps4/export_mesa_ores.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-mesa-ore-captures

python docs/reference-tools/ps4/export_deposits.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-deposit-captures

python docs/reference-tools/ps4/export_lakes.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-lake-captures

python docs/reference-tools/ps4/export_population.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-population-captures

python docs/reference-tools/ps4/export_integer_properties.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-integer-property-captures

python docs/reference-tools/ps4/export_boolean_properties.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-boolean-property-captures

python docs/reference-tools/ps4/export_enum_properties.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-enum-property-captures

python docs/reference-tools/ps4/export_scalar_block_states.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-scalar-block-state-captures

python docs/reference-tools/ps4/export_scalar_state_decoders.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-scalar-state-decoder-captures

python docs/reference-tools/ps4/export_scalar_constructor_defaults.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-scalar-constructor-default-captures

python docs/reference-tools/ps4/export_state_definitions.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-state-definition-captures

python docs/reference-tools/ps4/export_state_index.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-state-index-captures

python docs/reference-tools/ps4/export_state_ids.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-state-id-captures

python docs/reference-tools/ps4/export_secondary_storage.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-secondary-storage-captures

python docs/reference-tools/ps4/export_compressed_sections.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-compressed-section-captures

python docs/reference-tools/ps4/export_block_heights.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-block-height-captures

python docs/reference-tools/ps4/export_height_invalidation.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-height-invalidation-captures

python docs/reference-tools/ps4/export_block_materials.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-block-material-captures

python docs/reference-tools/ps4/export_materials.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-material-captures

python docs/reference-tools/ps4/export_biome_temperature.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-biome-temperature-captures

python docs/reference-tools/ps4/export_precipitation.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-precipitation-captures

python docs/reference-tools/ps4/export_snow.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-snow-captures

python docs/reference-tools/ps4/export_freezing.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-freezing-captures

python docs/reference-tools/ps4/export_population.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --native-mobs \
  --output-dir /tmp/ps4-population-native-mob-captures

python docs/reference-tools/ps4/export_dungeons.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-dungeon-captures

python docs/reference-tools/ps4/export_flora.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-flora-captures

python docs/reference-tools/ps4/export_decoration.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-decoration-captures

python docs/reference-tools/ps4/export_mushrooms.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-mushroom-captures

python docs/reference-tools/ps4/export_trees.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-tree-captures

python docs/reference-tools/ps4/export_mega_trees.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-mega-tree-captures

python docs/reference-tools/ps4/export_big_trees.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-big-tree-captures

python docs/reference-tools/ps4/export_spruce.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-spruce-captures

python docs/reference-tools/ps4/export_tree_selection.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-tree-selection-captures

python docs/reference-tools/ps4/export_flora_selection.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-flora-selection-captures

python docs/reference-tools/ps4/export_vines.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-vine-captures

python docs/reference-tools/ps4/export_blobs.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-blob-captures

python docs/reference-tools/ps4/export_pumpkins.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-pumpkin-captures

python docs/reference-tools/ps4/export_liquids.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-liquid-captures

python docs/reference-tools/ps4/export_lava_fire.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-lava-fire-captures

python docs/reference-tools/ps4/export_aquatics.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-aquatic-captures

python docs/reference-tools/ps4/export_ice.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-ice-captures

python docs/reference-tools/ps4/export_icebergs.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-iceberg-captures

python docs/reference-tools/ps4/export_double_sine.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-double-sine-captures

python docs/reference-tools/ps4/export_biome_settings.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-biome-settings-captures

python docs/reference-tools/ps4/export_ocean_wrappers.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-ocean-wrapper-captures

python docs/reference-tools/ps4/export_corals.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-coral-captures

python docs/reference-tools/ps4/export_coral_shapes.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-coral-shape-captures

python docs/reference-tools/ps4/export_coral_crust.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --assets src/main/resources/assets/legacyconsole/worldgen/ps4 \
  --output-dir /tmp/ps4-coral-crust-captures

python docs/reference-tools/ps4/export_coral_decoration.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-coral-decoration-captures

python docs/reference-tools/ps4/export_template_markers.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --assets src/main/resources/assets/legacyconsole/worldgen/ps4 \
  --output-dir /tmp/ps4-template-marker-captures

python docs/reference-tools/ps4/export_template_order.py \
  --eboot /path/to/mcps4/eboot.bin \
  --assets src/main/resources/assets/legacyconsole/worldgen/ps4 \
  --output-dir /tmp/ps4-template-order-captures

python docs/reference-tools/ps4/export_coral_palette.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-coral-palette-captures

python docs/reference-tools/ps4/export_coral_templates.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --assets src/main/resources/assets/legacyconsole/worldgen/ps4 \
  --output-dir /tmp/ps4-coral-template-captures

python docs/reference-tools/ps4/export_direction_liquids.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-direction-liquid-captures

python docs/reference-tools/ps4/export_fossil_geometry.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --assets src/main/resources/assets/legacyconsole/worldgen/ps4 \
  --output-dir /tmp/ps4-fossil-geometry-captures

python docs/reference-tools/ps4/export_springs.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-spring-captures

python docs/reference-tools/ps4/export_fossils.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --assets src/main/resources/assets/legacyconsole/worldgen/ps4 \
  --output-dir /tmp/ps4-fossil-captures

python docs/reference-tools/ps4/export_wells.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-well-captures

python docs/reference-tools/ps4/export_tall_vegetation.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-tall-vegetation-captures

python docs/reference-tools/ps4/export_biome_wrappers.py \
  --eboot /path/to/mcps4/eboot.bin \
  --libc /path/to/mcps4/sce_module/libc.prx \
  --output-dir /tmp/ps4-biome-wrapper-captures
```

The optional static block-table extractor additionally requires Capstone 5:

```sh
python docs/reference-tools/ps4/read_block_registry.py \
  --eboot /path/to/mcps4/eboot.bin \
  --output /tmp/block-registry-native.json
```

The helper maps unencrypted segments and applies relocations. It does not call
module startup, the game entry point, or platform services. Only selected math,
noise, density, interpolation, surface, carver, population dispatcher,
ore/deposit/lake/dungeon/plant and selected
configuration/enum initializer entry points run.
Allocation/memory operations, profiling locks, and
the controlled world/block/biome callbacks are supplied by the harness. Unknown
imports terminate capture. The original libc supplies the math functions.

The captures reproduce the corresponding test fixtures, including
`ps4-double-cos.f64.gz`, the two surface fixture files and
`ps4-base-chunks.json.gz`. Terrain replay also checks the existing density and
surface-noise fixtures by double bits. `ps4-sine.f32.gz` reproduces the
production math resource. Original executables,
SDK files, game jars and analysis projects are not bundled.

## Playable integration and remaining accuracy work

`LegacyChunkGenerator.applyBiomeDecoration` now calls the translated
`Ps4Population` dispatcher through `LegacyPopulation`. Registered biome virtual
bindings and used variant fields are captured from original `0x7f900` startup
with the same bounded constructor bindings as the biome-settings capture.
The additional Ice/Mesa selectors are original `0x373dc0`/`0x46a9d0`; they create
a spruce/basic tree without a selector RNG draw. The production profile data is
reproducible with `export_population_profiles.py`.

The target palette accepts all 113 distinct states in 471,579 native feature
writes covered by the new integration test. This verifies palette coverage,
not complete-world parity. Java-only or other-mod states receive explicit
adapter tokens rather than guessed PS4 registry IDs.

The world bridge clips writes to the target generation region (center chunk
plus one neighboring chunk in each direction). Rejected writes do not expand
that region or request recursive chunk generation. It uses Java heightmaps,
light, shape queries, block entities,
loot, scheduled fluid updates and waterlogging. It reconstructs Java leaf
distance and tall-plant state after placement. Original compressed state
storage, canonical block registry and complete original physics are not used by
this live adapter. Structure clipping, graph/geometry, loot and RNG still need
full native comparisons. Stateful feature caches are per population context;
the original global iceberg/direction cache lifetime is not reproduced.
Coral template palette colors use a fixed independent target stream and sorted
cache initialization instead of the original process clock stream.

Modern deepslate, copper, geodes and bee nests run after the PS4 sequence using
separate domain seeds and target 1.17.1 routines. They intentionally change the
resulting world. Bee probabilities are a target adaptation. New 1.17 structure
work remains deferred. Final spawn still needs comparison against complete PS4
world output after population.

See [playable release notes](PLAYABLE-233.md) and [verification](../TESTING.md)
for tested artifacts, runtime checks and remaining limits.

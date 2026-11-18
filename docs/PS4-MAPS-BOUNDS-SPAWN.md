# PS4 maps, finite bounds, and spawn translation — 2.3.2

The map-data, finite-border, and spawn changes apply to the **Legacy Overworld**.
Default, Flat, and the native Nether/End retain their existing world behavior.
The 100×100 map image stays inset at (14,14) in its 128×128 area, with coordinates
in the paper border. Those image, paper, and coordinate transforms are unchanged.

## Evidence

- [PS4 executable reference](https://github.com/Nejirus/mcps4/tree/37e258085773f11599cf0ffe29119926769554b7)
- [LCE C++ reference](https://github.com/chrisGrando/MinecraftLCE-7thGen/tree/acff48c2c7e18406fd6000b56945746ca9a6eb69)
- Baseline: the uploaded `legacy-console-1.17.1-2.3.1-source.zip`.

The PS4 repository supplies an executable, not decompiled C++. Its `eboot.bin`
SHA-256 is `c6089834fe1372854eed8f9acf2cb307f7009aaeb32ee303ce5bf6a1160786c8`.
Functions were identified through RTTI/vtables and callers, disassembled, and
cross-referenced with LCE C++. Java preserves the recovered constants, branch
ordering, rounding, and RNG consumption; platform object, player, chunk, NBT,
and packet operations use Minecraft/Forge APIs. No executable is redistributed.

| Function | PS4 virtual address | Java translation/adaptation |
| --- | --- | --- |
| `EmptyMapItem::use` | `0x225f50` | `EmptyMapItemMixin` |
| `MapItem::create` | `0x442ef0` | `Ps4Maps.center/fresh`, `MapItemMixin` |
| map enlargement | `0x447430` | `Ps4Maps.enlarged`, `MapCrafting` |
| map terrain sampling | `0x443cc0` | finite lookup in `MapItemMixin` |
| `ServerChunkCache::getChunk` | `0xcdfe60` | empty chunk outside finite bounds |
| dimension finite chunk predicate | `0x1fe210` | `Ps4WorldSize` predicates |
| `ServerLevel::setInitialSpawn` | `0xcf98d0` | `Ps4Spawn`, Forge spawn event |
| `BiomeSource::findBiome` | `0x8e130` | `Ps4Spawn.select` |
| `Dimension::isValidSpawn` | `0x1fdbf0` | `Ps4Spawn.validGround` |
| `Level::getTopTile` | `0x3eeb70` | grass check starting at sea level |
| `Dimension::getSpawnYPosition` | `0x1fe060` | saved Legacy world spawn Y=64 |
| `ServerPlayer` spawn branch | `0xd001c1..0xd00575` | `ServerPlayerSpawnMixin` |
| top-solid position scan | `0x3f7ca0` | highest-section scan excluding leaves |

The corresponding LCE files are `Minecraft.World/{MapItem,MapItemSavedData,
Dimension,BiomeSource,Level}.cpp` and `Minecraft.Client/{ServerChunkCache,
ServerLevel,ServerPlayer}.cpp`. Verified PS4 differences take priority.

## Maps

The image remains **128×128 pixels**. Scale `s` covers `128 << s` blocks per axis.
There is no special image format that crops a Classic world to 864 blocks.

At scales 0–2, the center is `floor(position / width) * width + width / 2`.
At scales 3–4, it is `round(position / width) * width`, using C `round(double)`
(negative half ties go away from zero). The import at `0xe6d3b0` resolves to
the `round` NID `nlaojL9hDtA`; Java's different `Math.round` tie rule is not used.
First-map creation uses the original double player position before rounding.

Enlargement uses the source map's center. The original map and its discovered
image remain intact. PS4 chooses scale 3 when the player's map count is zero,
and scale 0 otherwise. The port stores the required zero/nonzero history by
world and player UUID; carrying an existing filled map also registers it.
Java map IDs and saves remain native. PS4's per-player map-ID allocation and
save format are not emulated.

The PS4 finite server chunk source returns empty chunks outside its world;
the map sampler skips them. Previously the Java generator's real outside ocean
chunks could be painted onto maps. The redirect now returns an empty chunk
before requesting that terrain. Existing saved map images are not rewritten
or recentered.

At scale 3, Classic uses 108 of 128 pixels per axis, leaving ten unexplored
pixels on each side. Small fills one map; Medium and Large cover three and five
scale-3 tiles per axis. These are map-data coverage facts. **The separate paper
inset, coordinates, and uniform visual margins remain as supplied in the mod.**

Other sampling uses the existing native 1.17.1 material-color and packet
adapters. This release translates geometry, initial scale choice, enlargement
alignment, and finite lookup. It does not claim every PS4 map decoration or
discovery-timing rule has been translated.

### Narrow Java viewports

Native testing exposed Java's one-handed map clipping at 4:3. A separate client
adapter, `HeldMapViewportMixin`, scales the held pose in camera X/Y by
`min(1, viewportAspect / (16/9))`. This keeps the paper inside a narrow viewport
without changing its inset, coordinates, square proportions, or shader equations.
It applies to one-handed maps in every world type; 16:9 and wider are unchanged.
This is an explicitly identified target-runtime compatibility adjustment, **not
a recovered PS4 constant or a claim of PS4 hand-pose equivalence**. The older LCE
reference has a two-handed map and separate split-screen adjustments, so it does
not supply a directly equivalent one-handed 4:3 pose.

## Where each seed's borders are

The PS4 finite predicate reads chunk coordinates and dimension width. It does
not read seed or spawn. The seed changes terrain inside this fixed region.

| Size | First X/Z block | Last X/Z block | Collision planes |
| --- | ---: | ---: | --- |
| Classic | −432 | 431 | −432, +432 |
| Small | −512 | 511 | −512, +512 |
| Medium | −1536 | 1535 | −1536, +1536 |
| Large | −2560 | 2559 | −2560, +2560 |

The supplied mod already placed its collision border correctly. Those planes
are retained and tested, with no invented seed offset. The finite terrain edge
is also origin-centered. Nether/End borders retain size 59,999,968.

## Spawn

The translated saved-world spawn follows PS4's sequence:

1. Seed `Random` with the saved world seed.
2. Search the 129×129 quart rectangle within 256 blocks of (0,0). Eligible IDs
   are 4, 5, 1, 19, 18, 21, 22, 40, and 41. The bamboo IDs were verified in
   PS4's list initialization at `0x8cee0` and registry initialization `0x7f900`.
3. Preserve the selected-count increment inside `findBiome`'s replacement
   branch. Replacing it with a conventional reservoir changes the RNG sequence.
4. Fall back to (8,8). Check for grass above sea level. PS4 Overworld biome
   subclasses use the default suitability predicate; only the ungenerated
   Void subclass overrides it.
5. Adjust X and then Z by `nextInt(64)-nextInt(64)`. Reset each out-of-bounds
   axis to zero. Preserve the 1,000-adjustment limit and final-check ordering.
6. Save X/Z with Y=64 and angle zero. The port's existing native 1.17.1 bonus
   chest feature still runs at this selected spawn when requested.

Initial player placement follows the recovered PS4 constructor branch:
world-rule spawn radius, border-distance clamp, inclusive square offsets,
finite-bound rejection, top-solid scan ignoring leaves, and the original
20-attempt/nearby-player condition. This PS4 build scans water from absolute
Y=0; the older LCE source starts at ground Y. The PS4 branch is preserved.
Its water globals resolve to `flowing_water` and `water` through registry
initialization at `0xc1520`; both map to native 1.17.1 water.

Player landing uses a transient entity RNG, so its exact coordinate is not
determined by world seed alone. Saved world spawn is deterministic for seed,
world size, biome scale, and generated surface. Beds, anchors, saved player
positions, and other world types retain their native lifecycle.

**This does not establish complete PS4 seed-to-block equality.** The supplied
port already documents target-version adapters for complex surfaces,
decoration, caves, and some structures. Those can affect a ground-dependent
spawn check. Selection logic is tested independently; complete final world/spawn
equality across every seed would require complete terrain parity and matching
PS4 world captures. No such unverified equality is claimed here.

## Reproduce the instruction comparisons

With Python `unicorn` installed, supply the matching executable yourself:

```sh
python docs/reference-tools/ps4/export_maps.py --eboot /path/eboot.bin --output /tmp/maps.json
python docs/reference-tools/ps4/export_spawn.py --eboot /path/eboot.bin --output /tmp/spawn.json
```

Map fixtures contain 160 centers, two first-map branches, and 160 finite-bound
cases. Spawn fixtures execute PS4 biome selection, finite spawn walking, and
`Random::nextInt` for 24 controlled cases. Tests compare every queried
coordinate, final position, and following RNG output. Controlled biome/ground
inputs exercise fallback, clipping, and rejection; they are not fabricated
captures of actual PS4 worlds.

Build and test with JDK 17 / Gradle 7.3.3 using
`./gradlew build joinAddonJar sourcesJar`. See `TESTING.md` for runtime results.

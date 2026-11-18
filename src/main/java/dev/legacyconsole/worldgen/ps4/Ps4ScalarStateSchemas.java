package dev.legacyconsole.worldgen.ps4;

import java.util.*;

/** Concrete scalar class definitions and their original metadata virtuals.
 * Class initialization after the initial definition/default is separate. */
public final class Ps4ScalarStateSchemas {
 private Ps4ScalarStateSchemas(){}
 public record Schema(long vtable,int createMethod,int encoder,int decoder,long virtualProperty,List<Long> properties) {
  public Schema {properties=List.copyOf(properties);}
  public <B> Ps4StateDefinition<B,Ps4Property<?>,Object> create(B owner,Ps4ScalarProperties context){List<Ps4Property<?>> ordered=new ArrayList<>();for(long alias:properties)ordered.add(context.get(alias));return new Ps4StateDefinition<>(owner,ordered,Ps4Property.DEFINITION_VALUES);}
  public <B> Ps4StateDefinition.State<B,Ps4Property<?>,Object> constructorDefault(Ps4StateDefinition<B,Ps4Property<?>,Object> definition,Ps4ScalarProperties context){return Ps4ScalarStateDefaults.initialize(this,definition,context);}
  public int encode(Ps4StateDefinition.State<?,Ps4Property<?>,Object> state,Ps4ScalarProperties context){return Ps4ScalarStateCodecs.encode(encoder,new Ps4ScalarStateCodecs.Access(){public Object value(long alias){return state.value(context.get(alias));}public long virtualProperty(){return Schema.this.virtualProperty;}});}
  public <B> Ps4StateDefinition.State<B,Ps4Property<?>,Object> decode(int data,Ps4StateDefinition.State<B,Ps4Property<?>,Object> initial,Ps4ScalarProperties context){return Ps4ScalarStateCodecs.decode(decoder,data,new Ps4ScalarStateCodecs.DecodeAccess<Ps4StateDefinition.State<B,Ps4Property<?>,Object>>(){public Ps4StateDefinition.State<B,Ps4Property<?>,Object> defaultState(){return initial;}public Ps4StateDefinition.State<B,Ps4Property<?>,Object> with(Ps4StateDefinition.State<B,Ps4Property<?>,Object> state,long alias,Object value){return state.with(context.get(alias),value);}public long virtualProperty(){return Schema.this.virtualProperty;}});}
 }
 private static final Map<Long,Schema> SCHEMAS;
 static {Map<Long,Schema> values=new LinkedHashMap<>();
  add(values,new Schema(0x1206e60L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 8AirBlock
  add(values,new Schema(0x120b858L,0x3ea00,0x40250,0x40150,0L,List.of(0x1409750L,0x1409758L))); // 11BambooBlock
  add(values,new Schema(0x120cac8L,0x44160,0x44110,0x44070,0L,List.of(0x1409798L))); // 19StandingBannerBlock
  add(values,new Schema(0x120def8L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 12BarrierBlock
  add(values,new Schema(0x1210570L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 11BeaconBlock
  add(values,new Schema(0x1211918L,0x64100,0x1ec1b0,0x1ec100,0x1409de8L,List.of(0x1409de8L))); // 13BeetrootBlock
  add(values,new Schema(0x1217e60L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 5Block
  add(values,new Schema(0x121b560L,0xe4e20,0xe4d20,0xe4be0,0L,List.of(0x140bcc0L,0x140bcc8L,0x140bcd0L))); // 17BrewingStandBlock
  add(values,new Schema(0x121c250L,0xeae20,0xeadb0,0xead10,0L,List.of(0x140bd40L))); // 19BubbleColumnSegment
  add(values,new Schema(0x1220a48L,0x159810,0x1597c0,0x159720,0L,List.of(0x140fa70L))); // 11CactusBlock
  add(values,new Schema(0x1220f30L,0x15a340,0x15a2a0,0x15a200,0L,List.of(0x140fa98L))); // 9CakeBlock
  add(values,new Schema(0x1223938L,0x16e340,0x16d380,0x16d280,0L,List.of(0x140fee0L,0x140fee8L))); // 13CauldronBlock
  add(values,new Schema(0x1226bb0L,0x17e3e0,0x17dc90,0x17dbf0,0L,List.of(0x140ffb8L))); // 17ChorusFlowerBlock
  add(values,new Schema(0x1227298L,0x17fd00,0x17f9b0,0x91eb0,0L,List.of(0x1410000L,0x1410008L,0x1410010L,0x1410018L,0x1410020L,0x1410028L))); // 16ChorusPlantBlock
  add(values,new Schema(0x12278c0L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 9ClayBlock
  add(values,new Schema(0x122e600L,0x1b4110,0x1b3f20,0x1b3e60,0L,List.of(0x14107b8L))); // 14ComposterBlock
  add(values,new Schema(0x122f360L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 12ConduitBlock
  add(values,new Schema(0x122ff30L,0x1c67c0,0x1c6920,0x1c6970,0L,List.of(0x14133a8L))); // 5Coral
  add(values,new Schema(0x1230420L,0x1c7000,0x1c7240,0x1c72d0,0L,List.of(0x1413568L,0x1413560L))); // 10CoralBlock
  add(values,new Schema(0x1230908L,0x1ca1e0,0x1ca3e0,0x1ca470,0L,List.of(0x1413588L,0x1413590L))); // 8CoralFan
  add(values,new Schema(0x1230fd0L,0x1cb7b0,0x1cb930,0x1cba00,0L,List.of(0x1413598L,0x14135a0L,0x1413768L))); // 12CoralFanHang
  add(values,new Schema(0x1232658L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 18CraftingTableBlock
  add(values,new Schema(0x1235c20L,0x1ec290,0x1ec1b0,0x1ec100,0x1413960L,List.of(0x1413960L))); // 9CropBlock
  add(values,new Schema(0x1236748L,0x1f6880,0x1f67c0,0x1f6720,0L,List.of(0x1413bd0L))); // 21DaylightDetectorBlock
  add(values,new Schema(0x1236d98L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 13DeadBushBlock
  add(values,new Schema(0x123d070L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 14DragonEggBlock
  add(values,new Schema(0x123ddf0L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 14DriedKelpBlock
  add(values,new Schema(0x123fb80L,0x41c5a0,0x41c550,0x41c4b0,0L,List.of(0x14188e0L))); // 18DynamicLiquidBlock
  add(values,new Schema(0x1246108L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 21EnchantmentTableBlock
  add(values,new Schema(0x124adf8L,0x271860,0x271980,0x2718e0,0L,List.of(0x1415928L))); // 15EndGatewayBlock
  add(values,new Schema(0x124b380L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 14EndPortalBlock
  add(values,new Schema(0x1250a48L,0x2a4730,0x2a46d0,0x2a4630,0L,List.of(0x1416230L))); // 9FarmBlock
  add(values,new Schema(0x1250fe8L,0x2a5de0,0x2a5350,0x91eb0,0L,List.of(0x1416250L,0x1416258L,0x1416268L,0x1416260L))); // 10FenceBlock
  add(values,new Schema(0x1252470L,0x2b1320,0x2b12d0,0x2b1230,0L,List.of(0x1416430L,0x1416438L,0x1416440L,0x1416448L,0x1416450L,0x1416458L))); // 9FireBlock
  add(values,new Schema(0x12597c0L,0x31d030,0x31cab0,0x31cb00,0L,List.of(0x1416a10L))); // 15FrostedIceBlock
  add(values,new Schema(0x125dce0L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 10GlassBlock
  add(values,new Schema(0x125e6e0L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 14Glowstoneblock
  add(values,new Schema(0x125fd60L,0x3369e0,0x336930,0x91eb0,0L,List.of(0x1416be8L))); // 10GrassBlock
  add(values,new Schema(0x1260288L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 14GrassPathBlock
  add(values,new Schema(0x1260770L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 11GravelBlock
  add(values,new Schema(0x1266e50L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 17HardenedClayBlock
  add(values,new Schema(0x126c918L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 8IceBlock
  add(values,new Schema(0x1270048L,0x3c3cf0,0x3c3bb0,0x3c3b10,0L,List.of(0x14181a8L))); // 12JukeboxBlock
  add(values,new Schema(0x12706e0L,0x3c5e40,0x3c5df0,0x3c5d50,0L,List.of(0x14182a8L))); // 9KelpBlock
  add(values,new Schema(0x1273cf0L,0x3dde70,0x3ddf90,0x3ddef0,0L,List.of(0x14183a0L))); // 12LanternBlock
  add(values,new Schema(0x127ba90L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 10MagmaBlock
  add(values,new Schema(0x1280058L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 10MelonBlock
  add(values,new Schema(0x1287928L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 15MobSpawnerBlock
  add(values,new Schema(0x128b798L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 13MushroomBlock
  add(values,new Schema(0x128d040L,0x4a2b00,0x4a2af0,0x91eb0,0L,List.of(0x1419890L))); // 13MyceliumBlock
  add(values,new Schema(0x128d7d8L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 16NetherBrickBlock
  add(values,new Schema(0x128f3f0L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 15NetherrackBlock
  add(values,new Schema(0x128f8d8L,0x4b95d0,0x4b9570,0x4b94d0,0L,List.of(0x1419938L))); // 15NetherWartBlock
  add(values,new Schema(0x12947a0L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 9NoteBlock
  add(values,new Schema(0x12951a0L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 13ObsidianBlock
  add(values,new Schema(0x1298cf0L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 8OreBlock
  add(values,new Schema(0x1299470L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 14PackedIceBlock
  add(values,new Schema(0x12a5d48L,0x1ec290,0x1ec1b0,0x1ec100,0x1413960L,List.of(0x1413960L))); // 11PotatoBlock
  add(values,new Schema(0x12a64b8L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 12PoweredBlock
  add(values,new Schema(0x12aae88L,0x58b910,0x58b8c0,0x58b820,0L,List.of(0x142ecd8L))); // 18PressurePlateBlock
  add(values,new Schema(0x12b1ac0L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 17RedstoneLampBlock
  add(values,new Schema(0x12b1fa8L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 16RedStoneOreBlock
  add(values,new Schema(0x12b2e98L,0x5b43d0,0x5b4380,0x5b42e0,0L,List.of(0x142f6d0L))); // 10ReedsBlock
  add(values,new Schema(0x12b70f0L,0x5cad60,0x5cae00,0x5cae90,0L,List.of(0x142fbc0L,0x142fbc8L))); // 16ScaffoldingBlock
  add(values,new Schema(0x12b9c58L,0x5f5340,0x5f5710,0x5f5670,0L,List.of(0x142fc68L))); // 8SeaGrass
  add(values,new Schema(0x12ba1c0L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 15SeaLanternBlock
  add(values,new Schema(0x12ba6a8L,0x5f7780,0x5f7810,0x5f7a00,0L,List.of(0x142fc88L,0x142fc90L))); // 9SeaPickle
  add(values,new Schema(0x12c8bb0L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 10SlimeBlock
  add(values,new Schema(0x12cafd0L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 9SnowBlock
  add(values,new Schema(0x12cb690L,0x6394f0,0x6394a0,0x6393a0,0L,List.of(0x1430430L))); // 14SnowLayerBlock
  add(values,new Schema(0x12cd4c8L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 13SoulSandBlock
  add(values,new Schema(0x12d0738L,0x672690,0x672640,0x6725a0,0L,List.of(0x1432090L))); // 11SpongeBlock
  add(values,new Schema(0x12d3038L,0x679060,0x678e50,0x678db0,0L,List.of(0x1430158L))); // 17StandingSignBlock
  add(values,new Schema(0x12d35a0L,0x41c5a0,0x41c550,0x41c4b0,0L,List.of(0x14188e0L))); // 17StaticLiquidBlock
  add(values,new Schema(0x12dd198L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 14StructureBlock
  add(values,new Schema(0x12dda88L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 18StructureVoidBlock
  add(values,new Schema(0x12de098L,0x6cc260,0x6cc380,0x6cc2e0,0L,List.of(0x14332d0L))); // 19SweetBerryBushBlock
  add(values,new Schema(0x12e09a0L,0x6e09e0,0x6e0200,0x91eb0,0L,List.of(0x14334e8L,0x14334f0L,0x1433500L,0x14334f8L))); // 14ThinFenceBlock
  add(values,new Schema(0x12e4f88L,0x6eec00,0x6eebb0,0x6eeb10,0L,List.of(0x1433610L))); // 8TntBlock
  add(values,new Schema(0x12e80a0L,0x70b050,0x70a7a0,0x70a650,0L,List.of(0x1433990L,0x1433998L,0x14339a0L,0x14339a8L,0x14339b0L,0x14339b8L,0x14339c8L,0x14339c0L))); // 13TripWireBlock
  add(values,new Schema(0x12eaae8L,0x711eb0,0x711f40,0x711fd0,0L,List.of(0x1433e18L,0x1433e20L))); // 14TurtleEggBlock
  add(values,new Schema(0x12ef910L,0x774940,0x774820,0x774680,0L,List.of(0x1434228L,0x1434230L,0x1434238L,0x1434240L,0x1434248L))); // 9VineBlock
  add(values,new Schema(0x12f2e58L,0x953c0,0x780880,0x91eb0,0L,List.of())); // 14WaterlilyBlock
  add(values,new Schema(0x12f3670L,0x953c0,0x91ec0,0x91eb0,0L,List.of())); // 8WebBlock
  add(values,new Schema(0x12f3b78L,0x783600,0x7835b0,0x783510,0L,List.of(0x1434630L))); // 26WeightedPressurePlateBlock
  add(values,new Schema(0x12f8ac8L,0x7946b0,0x794740,0x7947e0,0L,List.of(0x1434710L,0x1434718L))); // 9WoodBlock
  SCHEMAS=Collections.unmodifiableMap(values);
 }
 private static void add(Map<Long,Schema> values,Schema schema){values.put(schema.vtable(),schema);}
 public static Schema get(long vtable){Schema schema=SCHEMAS.get(vtable);if(schema==null)throw new IllegalArgumentException("Untranslated scalar class vtable: "+Long.toHexString(vtable));return schema;}
 public static Collection<Schema> all(){return SCHEMAS.values();}
}

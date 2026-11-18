package dev.legacyconsole.worldgen.ps4;

import java.util.Arrays;
import java.util.List;

/** PS4 biome properties recovered from Biome::initBiomes (0x7f900), not Java biome IDs.
 * family identifies the biome class used by the layer equality rules; it is not
 * Biome's category field at 0x1ac. The Java keys are used only after the original
 * PS4 layer and density calculations. */
public final class Ps4BiomeData {
 private static final Ps4BiomeData[] VALUES = new Ps4BiomeData[256];
 public final int id, family;
 public final String key;
 public final float depth, scale, temperature;
 private final boolean snowy;
 private Ps4BiomeData(int id,String key,int family,float depth,float scale,float temperature,boolean snow) {
  this.id=id;this.key=key;this.family=family;this.depth=depth;this.scale=scale;this.temperature=temperature;this.snowy=snow;
 }
 private static Ps4BiomeData add(int id,String key,int family,float depth,float scale,float temp,boolean snow) {
  return VALUES[id]=new Ps4BiomeData(id,key,family,depth,scale,temp,snow);
 }
 static {
  add(0,"ocean",10,-1.0f,0.10000000149011612f,0.5f,false);
  add(1,"plains",11,0.125f,0.05000000074505806f,0.800000011920929f,false);
  add(2,"desert",1,0.125f,0.05000000074505806f,2.0f,false);
  add(3,"mountains",2,1.0f,0.5f,0.20000000298023224f,false);
  add(4,"forest",3,0.10000000149011612f,0.20000000298023224f,0.699999988079071f,false);
  add(5,"taiga",17,0.20000000298023224f,0.20000000298023224f,0.25f,false);
  add(6,"swamp",16,-0.20000000298023224f,0.10000000149011612f,0.800000011920929f,false);
  add(7,"river",13,-0.5f,0.0f,0.5f,false);
  add(8,"nether_wastes",4,0.10000000149011612f,0.20000000298023224f,2.0f,false);
  add(9,"the_end",18,0.10000000149011612f,0.20000000298023224f,0.5f,false);
  add(10,"frozen_ocean",10,-1.0f,0.10000000149011612f,0.0f,true);
  add(11,"frozen_river",13,-0.5f,0.0f,0.0f,true);
  add(12,"snowy_tundra",5,0.125f,0.05000000074505806f,0.0f,true);
  add(13,"snowy_mountains",5,0.44999998807907104f,0.30000001192092896f,0.0f,true);
  add(14,"mushroom_fields",8,0.20000000298023224f,0.30000001192092896f,0.8999999761581421f,false);
  add(15,"mushroom_field_shore",8,0.0f,0.02500000037252903f,0.8999999761581421f,false);
  add(16,"beach",0,0.0f,0.02500000037252903f,0.800000011920929f,false);
  add(17,"desert_hills",1,0.44999998807907104f,0.30000001192092896f,2.0f,false);
  add(18,"wooded_hills",3,0.44999998807907104f,0.30000001192092896f,0.699999988079071f,false);
  add(19,"taiga_hills",17,0.44999998807907104f,0.30000001192092896f,0.25f,false);
  add(20,"mountain_edge",2,0.800000011920929f,0.30000001192092896f,0.20000000298023224f,false);
  add(21,"jungle",6,0.10000000149011612f,0.20000000298023224f,0.949999988079071f,false);
  add(22,"jungle_hills",6,0.44999998807907104f,0.30000001192092896f,0.949999988079071f,false);
  add(23,"jungle_edge",6,0.10000000149011612f,0.20000000298023224f,0.949999988079071f,false);
  add(24,"deep_ocean",10,-1.7999999523162842f,0.10000000149011612f,0.5f,false);
  add(25,"stone_shore",15,0.10000000149011612f,0.800000011920929f,0.20000000298023224f,false);
  add(26,"snowy_beach",0,0.0f,0.02500000037252903f,0.05000000074505806f,true);
  add(27,"birch_forest",3,0.10000000149011612f,0.20000000298023224f,0.6000000238418579f,false);
  add(28,"birch_forest_hills",3,0.44999998807907104f,0.30000001192092896f,0.6000000238418579f,false);
  add(29,"dark_forest",3,0.10000000149011612f,0.20000000298023224f,0.699999988079071f,false);
  add(30,"snowy_taiga",17,0.20000000298023224f,0.20000000298023224f,-0.5f,true);
  add(31,"snowy_taiga_hills",17,0.44999998807907104f,0.30000001192092896f,-0.5f,true);
  add(32,"giant_tree_taiga",17,0.20000000298023224f,0.20000000298023224f,0.30000001192092896f,false);
  add(33,"giant_tree_taiga_hills",17,0.44999998807907104f,0.30000001192092896f,0.30000001192092896f,false);
  add(34,"wooded_mountains",2,1.0f,0.5f,0.20000000298023224f,false);
  add(35,"savanna",14,0.125f,0.05000000074505806f,1.2000000476837158f,false);
  add(36,"savanna_plateau",14,1.5f,0.02500000037252903f,1.0f,false);
  add(37,"badlands",7,0.10000000149011612f,0.20000000298023224f,2.0f,false);
  add(38,"wooded_badlands_plateau",7,1.5f,0.02500000037252903f,2.0f,false);
  add(39,"badlands_plateau",7,1.5f,0.02500000037252903f,2.0f,false);
  add(40,"bamboo_jungle",6,0.10000000149011612f,0.20000000298023224f,0.949999988079071f,false);
  add(41,"bamboo_jungle_hills",6,0.10000000149011612f,0.20000000298023224f,0.949999988079071f,false);
  add(42,"warm_ocean",22,-1.0f,0.10000000149011612f,0.5f,false);
  add(43,"deep_warm_ocean",22,-1.7999999523162842f,0.10000000149011612f,0.5f,false);
  add(44,"lukewarm_ocean",21,-1.0f,0.10000000149011612f,0.5f,false);
  add(45,"deep_lukewarm_ocean",21,-1.7999999523162842f,0.10000000149011612f,0.5f,false);
  add(46,"cold_ocean",19,-1.0f,0.10000000149011612f,0.5f,false);
  add(47,"deep_cold_ocean",19,-1.7999999523162842f,0.10000000149011612f,0.5f,false);
  add(48,"frozen_ocean",20,-1.0f,0.10000000149011612f,0.0f,true);
  add(49,"deep_frozen_ocean",20,-1.7999999523162842f,0.10000000149011612f,0.0f,true);
  add(127,"the_void",23,0.10000000149011612f,0.20000000298023224f,0.5f,false);
  add(129,"sunflower_plains",11,0.125f,0.05000000074505806f,0.800000011920929f,false);
  add(130,"desert_lakes",1,0.22499999403953552f,0.25f,2.0f,false);
  add(131,"gravelly_mountains",2,1.0f,0.5f,0.20000000298023224f,false);
  add(132,"flower_forest",3,0.10000000149011612f,0.4000000059604645f,0.699999988079071f,false);
  add(133,"taiga_mountains",17,0.30000001192092896f,0.4000000059604645f,0.25f,false);
  add(134,"swamp_hills",16,-0.10000000149011612f,0.30000001192092896f,0.800000011920929f,false);
  add(140,"ice_spikes",5,0.42500001192092896f,0.45000001788139343f,0.0f,true);
  add(149,"modified_jungle",6,0.20000000298023224f,0.4000000059604645f,0.949999988079071f,false);
  add(151,"modified_jungle_edge",6,0.20000000298023224f,0.4000000059604645f,0.949999988079071f,false);
  add(155,"tall_birch_forest",3,0.20000000298023224f,0.4000000059604645f,0.6000000238418579f,false);
  add(156,"tall_birch_hills",3,0.550000011920929f,0.5f,0.6000000238418579f,false);
  add(157,"dark_forest_hills",3,0.20000000298023224f,0.4000000059604645f,0.699999988079071f,false);
  add(158,"snowy_taiga_mountains",17,0.30000001192092896f,0.4000000059604645f,-0.5f,true);
  add(160,"giant_spruce_taiga",17,0.20000000298023224f,0.20000000298023224f,0.25f,false);
  add(161,"giant_spruce_taiga_hills",17,0.20000000298023224f,0.20000000298023224f,0.25f,false);
  add(162,"modified_gravelly_mountains",2,1.0f,0.5f,0.20000000298023224f,false);
  add(163,"shattered_savanna",14,0.36250001192092896f,1.225000023841858f,1.100000023841858f,false);
  add(164,"shattered_savanna_plateau",14,1.0499999523162842f,1.2125000953674316f,1.0f,false);
  add(165,"eroded_badlands",7,0.10000000149011612f,0.20000000298023224f,2.0f,false);
  add(166,"modified_wooded_badlands_plateau",7,0.44999998807907104f,0.30000001192092896f,2.0f,false);
  add(167,"modified_badlands_plateau",7,0.44999998807907104f,0.30000001192092896f,2.0f,false);
 }
 public static final Ps4BiomeData OCEAN=VALUES[0];
 public static final Ps4BiomeData PLAINS=VALUES[1];
 public static final Ps4BiomeData DESERT=VALUES[2];
 public static final Ps4BiomeData EXTREME_HILLS=VALUES[3];
 public static final Ps4BiomeData FOREST=VALUES[4];
 public static final Ps4BiomeData TAIGA=VALUES[5];
 public static final Ps4BiomeData SWAMPLAND=VALUES[6];
 public static final Ps4BiomeData RIVER=VALUES[7];
 public static final Ps4BiomeData HELL=VALUES[8];
 public static final Ps4BiomeData SKY=VALUES[9];
 public static final Ps4BiomeData FROZEN_OCEAN=VALUES[10];
 public static final Ps4BiomeData FROZEN_RIVER=VALUES[11];
 public static final Ps4BiomeData ICE_PLAINS=VALUES[12];
 public static final Ps4BiomeData ICE_MOUNTAINS=VALUES[13];
 public static final Ps4BiomeData MUSHROOM_ISLAND=VALUES[14];
 public static final Ps4BiomeData MUSHROOM_ISLAND_SHORE=VALUES[15];
 public static final Ps4BiomeData BEACH=VALUES[16];
 public static final Ps4BiomeData DESERT_HILLS=VALUES[17];
 public static final Ps4BiomeData FOREST_HILLS=VALUES[18];
 public static final Ps4BiomeData TAIGA_HILLS=VALUES[19];
 public static final Ps4BiomeData EXTREME_HILLS_EDGE=VALUES[20];
 public static final Ps4BiomeData JUNGLE=VALUES[21];
 public static final Ps4BiomeData JUNGLE_HILLS=VALUES[22];
 public static final Ps4BiomeData JUNGLE_EDGE=VALUES[23];
 public static final Ps4BiomeData DEEP_OCEAN=VALUES[24];
 public static final Ps4BiomeData STONE_BEACH=VALUES[25];
 public static final Ps4BiomeData COLD_BEACH=VALUES[26];
 public static final Ps4BiomeData BIRCH_FOREST=VALUES[27];
 public static final Ps4BiomeData BIRCH_FOREST_HILLS=VALUES[28];
 public static final Ps4BiomeData ROOFED_FOREST=VALUES[29];
 public static final Ps4BiomeData COLD_TAIGA=VALUES[30];
 public static final Ps4BiomeData COLD_TAIGA_HILLS=VALUES[31];
 public static final Ps4BiomeData REDWOOD_TAIGA=VALUES[32];
 public static final Ps4BiomeData REDWOOD_TAIGA_HILLS=VALUES[33];
 public static final Ps4BiomeData EXTREME_HILLS_WITH_TREES=VALUES[34];
 public static final Ps4BiomeData SAVANNA=VALUES[35];
 public static final Ps4BiomeData SAVANNA_PLATEAU=VALUES[36];
 public static final Ps4BiomeData MESA=VALUES[37];
 public static final Ps4BiomeData MESA_ROCK=VALUES[38];
 public static final Ps4BiomeData MESA_CLEAR_ROCK=VALUES[39];
 public static final Ps4BiomeData BAMBOO_JUNGLE=VALUES[40];
 public static final Ps4BiomeData BAMBOO_JUNGLE_HILLS=VALUES[41];
 public static Ps4BiomeData getBiome(int id) { return id>=0&&id<VALUES.length?VALUES[id]:null; }
 public static Ps4BiomeData getBiomeForId(int id) { return getBiome(id); }
 public static int getIdForBiome(Ps4BiomeData biome) { return biome==null?-1:biome.id; }
 public static Ps4BiomeData getMutationForBiome(Ps4BiomeData biome) { return biome==null?null:getBiome(biome.id+128); }
 public boolean isMutation() { return id>=128; }
 public boolean isSnowyBiome() { return snowy; }
 /** Original constructor field +0x1bc is 0.25f for the two frozen-ocean
  * biomes and zero for the other registered biomes; helper 0x852c0 scales it. */
 public int maximumSnowDepth() { return Ps4SnowAndIce.maximumSnowDepth(id==48||id==49?0.25f:0.0f); }
 public int getBiomeClass() { return family; }
 public enum TempCategory { OCEAN,COLD,MEDIUM,WARM }
 // OceanBiome's original virtual also covers the old frozen-ocean ID 10;
 // Layer::isOcean below has a separate, narrower identity predicate.
 public TempCategory getTempCategory() { return isOceanCategory(id)?TempCategory.OCEAN:temperature<.2f?TempCategory.COLD:temperature<1f?TempCategory.MEDIUM:TempCategory.WARM; }
 public static boolean isOcean(int id) { return id==0||id==24||id>=42&&id<=49; }
 /** Biome's stored OCEAN category (0x1ac), used by the carvers. All new ocean
  * constructors pass category 10 to Biome's constructor. This differs from
  * the RTTI-derived family identities used by the biome layers. */
 public static boolean isOceanCategory(int id) { return isOcean(id)||id==10; }
 public static boolean isShallowOcean(int id) { return id==0||id==42||id==44||id==46||id==48; }
 public static List<Ps4BiomeData> values() { return Arrays.stream(VALUES).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toList()); }
}

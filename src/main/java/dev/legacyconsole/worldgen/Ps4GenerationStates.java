package dev.legacyconsole.worldgen;

import java.util.*;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/** Target-game palette for population. This is an explicit 1.17 adaptation,
 * separate from the original canonical PS4 state registry and its codecs. */
public final class Ps4GenerationStates {
 private static final Map<Integer,BlockState> STATES=new HashMap<>();
 private static final Map<BlockState,Integer> ORIGINAL=new IdentityHashMap<>();
 private static final Map<Block,Integer> BLOCK_DEFAULTS=new IdentityHashMap<>();
 private static final Map<BlockState,Integer> TARGET_TOKENS=new IdentityHashMap<>();
 private static final List<BlockState> TARGET_STATES=new ArrayList<>();
 private static final Block[] WOOD_LOGS={Blocks.OAK_LOG,Blocks.SPRUCE_LOG,Blocks.BIRCH_LOG,Blocks.JUNGLE_LOG,Blocks.ACACIA_LOG,Blocks.DARK_OAK_LOG};
 private static final Block[] WOOD_LEAVES={Blocks.OAK_LEAVES,Blocks.SPRUCE_LEAVES,Blocks.BIRCH_LEAVES,Blocks.JUNGLE_LEAVES,Blocks.ACACIA_LEAVES,Blocks.DARK_OAK_LEAVES};
 private static final Block[] FLOWERS={Blocks.POPPY,Blocks.BLUE_ORCHID,Blocks.ALLIUM,Blocks.AZURE_BLUET,Blocks.RED_TULIP,Blocks.ORANGE_TULIP,Blocks.WHITE_TULIP,Blocks.PINK_TULIP,Blocks.OXEYE_DAISY,Blocks.CORNFLOWER,Blocks.LILY_OF_THE_VALLEY};
 private static final Block[] DOUBLE_PLANTS={Blocks.SUNFLOWER,Blocks.LILAC,Blocks.TALL_GRASS,Blocks.LARGE_FERN,Blocks.ROSE_BUSH,Blocks.PEONY};
 private static final Block[] TERRACOTTA={Blocks.WHITE_TERRACOTTA,Blocks.ORANGE_TERRACOTTA,Blocks.MAGENTA_TERRACOTTA,Blocks.LIGHT_BLUE_TERRACOTTA,Blocks.YELLOW_TERRACOTTA,Blocks.LIME_TERRACOTTA,Blocks.PINK_TERRACOTTA,Blocks.GRAY_TERRACOTTA,Blocks.LIGHT_GRAY_TERRACOTTA,Blocks.CYAN_TERRACOTTA,Blocks.PURPLE_TERRACOTTA,Blocks.BLUE_TERRACOTTA,Blocks.BROWN_TERRACOTTA,Blocks.GREEN_TERRACOTTA,Blocks.RED_TERRACOTTA,Blocks.BLACK_TERRACOTTA};
 private static final String[] CORAL={"tube","brain","bubble","fire","horn"};
 private static final int[] TILES={0,1,2,3,4,5,6,7,9,8,11,10,12,13,14,15,16,17,18,20,21,24,31,32,37,38,39,40,43,44,48,49,50,51,52,54,56,60,73,74,78,79,80,81,82,83,85,86,87,88,89,97,99,100,103,106,110,111,127,129,159,161,162,168,172,174,175,179,212,213,216,255,257,258,259,263,264,265,266,267,268,269,270,271,273,319,321,363};
 static{
  for(int tile:TILES)for(int data=0;data<16;data++){
   int raw=tile|(data<<16);BlockState state=convert(tile,data);STATES.put(raw,state);ORIGINAL.putIfAbsent(state,raw);BLOCK_DEFAULTS.putIfAbsent(state.getBlock(),raw);
  }
  // Generation treats all three Java air variants as the original air tile.
  ORIGINAL.put(Blocks.AIR.defaultBlockState(),0);ORIGINAL.put(Blocks.CAVE_AIR.defaultBlockState(),0);ORIGINAL.put(Blocks.VOID_AIR.defaultBlockState(),0);
  ORIGINAL.put(Blocks.KELP_PLANT.defaultBlockState(),258);
 }
 private Ps4GenerationStates(){}
 public static BlockState decode(int state){
  if((state&0x8000)!=0){synchronized(TARGET_STATES){return TARGET_STATES.get(state&0x7fff);}}
  BlockState result=STATES.get(state);if(result==null)throw new IllegalArgumentException("Unmapped PS4 population state "+state);return result;
 }
 public static int encode(BlockState state){
  Integer known=ORIGINAL.get(state);if(known!=null)return known;
  if(state.getBlock() instanceof LeavesBlock){
   for(int i=0;i<WOOD_LEAVES.length;i++)if(state.is(WOOD_LEAVES[i]))return (i<4?18:161)|(((i<4?i:i-4)|(state.getValue(LeavesBlock.PERSISTENT)?4:0))<<16);
  }
  Integer base=BLOCK_DEFAULTS.get(state.getBlock());if(base!=null)return base;
  // Foreign and modern states get adapter-only identities. Do not depend on
  // Forge's globally remapped numeric state IDs or interpret these as PS4 IDs.
  synchronized(TARGET_STATES){
   Integer token=TARGET_TOKENS.get(state);if(token!=null)return token;
   int index=TARGET_STATES.size();if(index>=0x8000)throw new IllegalStateException("Population adapter target palette exhausted");
   token=0x8000|index;TARGET_STATES.add(state);TARGET_TOKENS.put(state,token);return token;
  }
 }
 public static boolean original(int state){return (state&0x8000)==0;}
 public static BlockState doublePlant(int below){return DOUBLE_PLANTS[Math.min(below>>>16&7,5)].defaultBlockState().setValue(DoublePlantBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER);}
 private static BlockState s(Block block){return block.defaultBlockState();}
 private static BlockState properties(BlockState state,String... pairs){for(int i=0;i<pairs.length;i+=2)state=property(state,pairs[i],pairs[i+1]);return state;}
 @SuppressWarnings({"rawtypes","unchecked"}) private static BlockState property(BlockState state,String name,String value){
  Property p=state.getBlock().getStateDefinition().getProperty(name);if(p==null)throw new IllegalArgumentException("Missing target property "+name+" on "+state);
  Comparable parsed=(Comparable)p.getValue(value).orElse(null);if(parsed==null)throw new IllegalArgumentException("Invalid target property "+name+"="+value);return state.setValue(p,parsed);
 }
 private static BlockState coral(int type,boolean dead,String suffix){
  String name="minecraft:"+(dead?"dead_":"")+CORAL[Math.min(type,4)]+"_coral"+suffix;
  var id=new net.minecraft.resources.ResourceLocation(name);if(!net.minecraft.core.Registry.BLOCK.containsKey(id))throw new IllegalArgumentException("Missing target coral "+name);
  BlockState result=net.minecraft.core.Registry.BLOCK.get(id).defaultBlockState();return result.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)?properties(result,"waterlogged","true"):result;
 }
 private static BlockState convert(int id,int m){
  return switch(id){
   case 0,255->s(Blocks.AIR);
   case 1->s(switch(m){case 1->Blocks.GRANITE;case 2->Blocks.POLISHED_GRANITE;case 3->Blocks.DIORITE;case 4->Blocks.POLISHED_DIORITE;case 5->Blocks.ANDESITE;case 6->Blocks.POLISHED_ANDESITE;default->Blocks.STONE;});
   case 2->s(Blocks.GRASS_BLOCK);case 3->s(m==1?Blocks.COARSE_DIRT:m==2?Blocks.PODZOL:Blocks.DIRT);case 4->s(Blocks.COBBLESTONE);
   case 5->s(new Block[]{Blocks.OAK_PLANKS,Blocks.SPRUCE_PLANKS,Blocks.BIRCH_PLANKS,Blocks.JUNGLE_PLANKS,Blocks.ACACIA_PLANKS,Blocks.DARK_OAK_PLANKS}[Math.min(m,5)]);
   case 6->s(new Block[]{Blocks.OAK_SAPLING,Blocks.SPRUCE_SAPLING,Blocks.BIRCH_SAPLING,Blocks.JUNGLE_SAPLING,Blocks.ACACIA_SAPLING,Blocks.DARK_OAK_SAPLING}[Math.min(m&7,5)]);
   case 7->s(Blocks.BEDROCK);
   case 8,9->properties(s(Blocks.WATER),"level",Integer.toString(m));case 10,11->properties(s(Blocks.LAVA),"level",Integer.toString(m));
   case 12->s(m==1?Blocks.RED_SAND:Blocks.SAND);case 13->s(Blocks.GRAVEL);case 14->s(Blocks.GOLD_ORE);case 15->s(Blocks.IRON_ORE);case 16->s(Blocks.COAL_ORE);
   case 17,162->{int wood=id==17?m&3:4+(m&1);yield properties(s(WOOD_LOGS[wood]),"axis",(m&12)==4?"x":(m&12)==8?"z":"y");}
   case 18,161->{int wood=id==18?m&3:4+(m&1);yield properties(s(WOOD_LEAVES[wood]),"persistent",Boolean.toString((m&4)!=0),"distance","1");}
   case 20->s(Blocks.GLASS);case 21->s(Blocks.LAPIS_ORE);case 24->s(m==1?Blocks.CHISELED_SANDSTONE:m==2?Blocks.CUT_SANDSTONE:Blocks.SANDSTONE);
   case 31->s(m==2?Blocks.FERN:m==0?Blocks.DEAD_BUSH:Blocks.GRASS);case 32->s(Blocks.DEAD_BUSH);case 37->s(Blocks.DANDELION);case 38->s(FLOWERS[Math.min(m,10)]);
   case 39->s(Blocks.BROWN_MUSHROOM);case 40->s(Blocks.RED_MUSHROOM);
   case 43,44->{Block slab=switch(m&7){case 1->Blocks.SANDSTONE_SLAB;case 2->Blocks.PETRIFIED_OAK_SLAB;case 3->Blocks.COBBLESTONE_SLAB;case 4->Blocks.BRICK_SLAB;case 5->Blocks.STONE_BRICK_SLAB;case 6->Blocks.NETHER_BRICK_SLAB;case 7->Blocks.QUARTZ_SLAB;default->Blocks.SMOOTH_STONE_SLAB;};yield properties(s(slab),"type",id==43?"double":(m&8)!=0?"top":"bottom");}
   case 48->s(Blocks.MOSSY_COBBLESTONE);case 49->s(Blocks.OBSIDIAN);case 50->s(Blocks.TORCH);case 51->properties(s(Blocks.FIRE),"age",Integer.toString(m));case 52->s(Blocks.SPAWNER);
   case 54->properties(s(Blocks.CHEST),"facing",Direction.from3DDataValue(m>=2&&m<=5?m:2).getName());case 56->s(Blocks.DIAMOND_ORE);
   case 60->properties(s(Blocks.FARMLAND),"moisture",Integer.toString(m&7));case 73,74->s(Blocks.REDSTONE_ORE);
   case 78->properties(s(Blocks.SNOW),"layers",Integer.toString((m&7)+1));case 79->s(Blocks.ICE);case 80->s(Blocks.SNOW_BLOCK);
   case 81->properties(s(Blocks.CACTUS),"age",Integer.toString(m));case 82->s(Blocks.CLAY);case 83->properties(s(Blocks.SUGAR_CANE),"age",Integer.toString(m));case 85->s(Blocks.OAK_FENCE);
   case 86->properties(s(Blocks.CARVED_PUMPKIN),"facing",Direction.from2DDataValue(m&3).getName());case 87->s(Blocks.NETHERRACK);case 88->s(Blocks.SOUL_SAND);case 89->s(Blocks.GLOWSTONE);
   case 97->s(switch(m){case 1->Blocks.INFESTED_COBBLESTONE;case 2->Blocks.INFESTED_STONE_BRICKS;case 3->Blocks.INFESTED_MOSSY_STONE_BRICKS;case 4->Blocks.INFESTED_CRACKED_STONE_BRICKS;case 5->Blocks.INFESTED_CHISELED_STONE_BRICKS;default->Blocks.INFESTED_STONE;});
   case 99,100->{if(m==10||m==15)yield s(Blocks.MUSHROOM_STEM);BlockState b=s(id==99?Blocks.BROWN_MUSHROOM_BLOCK:Blocks.RED_MUSHROOM_BLOCK);yield properties(b,"up",Boolean.toString(m!=0),"down","false","north",Boolean.toString(m>=1&&m<=3),"south",Boolean.toString(m>=7&&m<=9),"west",Boolean.toString(m==1||m==4||m==7),"east",Boolean.toString(m==3||m==6||m==9));}
   case 103->s(Blocks.MELON);
   case 106->properties(s(Blocks.VINE),"south",Boolean.toString((m&1)!=0),"west",Boolean.toString((m&2)!=0),"north",Boolean.toString((m&4)!=0),"east",Boolean.toString((m&8)!=0));
   case 110->s(Blocks.MYCELIUM);case 111->s(Blocks.LILY_PAD);case 127->properties(s(Blocks.COCOA),"age",Integer.toString(Math.min(m>>>2,2)),"facing",Direction.from2DDataValue(m&3).getName());case 129->s(Blocks.EMERALD_ORE);case 159->s(TERRACOTTA[m]);case 168->s(m==1?Blocks.PRISMARINE_BRICKS:m==2?Blocks.DARK_PRISMARINE:Blocks.PRISMARINE);
   case 172->s(Blocks.TERRACOTTA);case 174->s(Blocks.PACKED_ICE);
   case 175->properties(s(DOUBLE_PLANTS[Math.min(m&7,5)]),"half",(m&8)!=0?"upper":"lower");case 179->s(m==1?Blocks.CHISELED_RED_SANDSTONE:m==2?Blocks.CUT_RED_SANDSTONE:Blocks.RED_SANDSTONE);
   case 212->properties(s(Blocks.FROSTED_ICE),"age",Integer.toString(m&3));case 213->s(Blocks.MAGMA_BLOCK);case 216->properties(s(Blocks.BONE_BLOCK),"axis",m==4?"x":m==8?"z":"y");
   case 257->s(Blocks.PUMPKIN);case 258->properties(s(Blocks.KELP),"age",Integer.toString(m));case 259->coral(m&7,(m&8)!=0,"_block");
   case 263->coral(m&7,(m&8)!=0,"");case 264,265->coral(m&7,id==265,"_fan");
   case 266,267,268->{int type=id==268?4:(id-266)*2+(m>>>1&1);String facing=switch(m>>>2&3){case 0->"west";case 1->"east";case 2->"north";default->"south";};yield properties(coral(type,(m&1)!=0,"_wall_fan"),"facing",facing);}
   case 269->s(Blocks.DRIED_KELP_BLOCK);case 270->m==0?s(Blocks.SEAGRASS):properties(s(Blocks.TALL_SEAGRASS),"half",m==1?"upper":"lower");case 271->properties(s(Blocks.SEA_PICKLE),"pickles",Integer.toString((m&3)+1),"waterlogged","true");case 273->s(Blocks.BLUE_ICE);
   case 319->properties(s(Blocks.BAMBOO),"age",Integer.toString(m>>>3&1),"leaves",(m&3)==2?"large":(m&3)==1?"small":"none");case 321->s(Blocks.BAMBOO_SAPLING);case 363->properties(s(Blocks.SWEET_BERRY_BUSH),"age",Integer.toString(m&3));
   default->throw new IllegalArgumentException("Unmapped PS4 population tile "+id);
  };
 }
}

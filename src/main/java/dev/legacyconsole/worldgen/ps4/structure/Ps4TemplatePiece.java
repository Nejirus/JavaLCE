package dev.legacyconsole.worldgen.ps4.structure;

import dev.legacyconsole.worldgen.LegacyChunkGenerator;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/** A persisted placement of an original PS4 template, not a Java jigsaw start. */
public class Ps4TemplatePiece extends StructurePiece {
 private final String name,processor,feature;
 private BlockPos position;
 private final Rotation rotation;
 private final Mirror mirror;
 private final int projection;
 public Ps4TemplatePiece(String name,BlockPos pos,Rotation rotation,Mirror mirror,int projection,String processor,String feature){
  super(Ps4PieceTypes.TEMPLATE,0,name.isEmpty()?new BoundingBox(pos):Ps4Templates.asset(name).template().getBoundingBox(new StructurePlaceSettings().setRotation(rotation).setMirror(mirror),pos));
  this.name=name;this.position=pos;this.rotation=rotation;this.mirror=mirror;this.projection=projection;this.processor=processor;this.feature=feature;
 }
 public Ps4TemplatePiece(ServerLevel level,CompoundTag tag){
  super(Ps4PieceTypes.TEMPLATE,tag);name=tag.getString("Ps4Template");position=new BlockPos(tag.getInt("PX"),tag.getInt("PY"),tag.getInt("PZ"));rotation=Rotation.valueOf(tag.getString("Ps4Rotation"));mirror=Mirror.valueOf(tag.getString("Ps4Mirror"));projection=tag.getInt("Projection");processor=tag.getString("Processor");feature=tag.getString("Feature");
 }
 public String templateName(){return name;}
 public BlockPos position(){return position;}
 public int projection(){return projection;}
 @Override public Rotation getRotation(){return rotation;}
 @Override public void move(int x,int y,int z){super.move(x,y,z);position=position.offset(x,y,z);}
 @Override protected void addAdditionalSaveData(ServerLevel level,CompoundTag tag){tag.putString("Ps4Template",name);tag.putInt("PX",position.getX());tag.putInt("PY",position.getY());tag.putInt("PZ",position.getZ());tag.putString("Ps4Rotation",rotation.name());tag.putString("Ps4Mirror",mirror.name());tag.putInt("Projection",projection);tag.putString("Processor",processor);tag.putString("Feature",feature);}
 @Override public boolean postProcess(WorldGenLevel level,StructureFeatureManager manager,ChunkGenerator generator,Random random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
  if(name.isEmpty()){BlockPos featurePos=project(level,generator,position,position,projection);if(clip.isInside(featurePos))Ps4VillageDecoration.place(feature,level,generator,random,featurePos);return true;}
  var template=Ps4Templates.asset(name).template();
  var settings=new StructurePlaceSettings().setRotation(rotation).setMirror(mirror).setBoundingBox(clip).setKeepLiquids(false)
   .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK).addProcessor(JigsawReplacementProcessor.INSTANCE)
   .addProcessor(new Projection(generator,projection,processor));
  if(processor.startsWith("ps4:integrity/")||processor.startsWith("ps4:ruin/")){
   settings.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR).addProcessor(new BlockRotProcessor(Float.parseFloat(processor.substring(processor.lastIndexOf('/')+1))));
  }
  template.placeInWorld(level,position,position,settings,random,2);
  if(projection==0&&(name.startsWith("village/")||processor.equals("ps4:outpost")))foundation(level,random,clip);

  for(var marker:template.filterBlocks(position,settings,Blocks.STRUCTURE_BLOCK)){
   BlockPos pos=project(level,generator,marker.pos,position,projection);if(clip.isInside(pos))marker(level,pos,marker.nbt==null?"":marker.nbt.getString("metadata"),random);
  }
  return true;
 }
 private static BlockPos project(LevelHeightAccessor level,ChunkGenerator generator,BlockPos pos,BlockPos origin,int projection){return projection==0?pos:pos.offset(0,generator.getBaseHeight(pos.getX(),pos.getZ(),Heightmap.Types.WORLD_SURFACE_WG,level)-origin.getY()-1,0);}
 private static final class Projection extends StructureProcessor {
  private final ChunkGenerator generator;private final int projection;private final String processor;
  Projection(ChunkGenerator generator,int projection,String processor){this.generator=generator;this.projection=projection;this.processor=processor;}
  @Override public StructureTemplate.StructureBlockInfo processBlock(LevelReader level,BlockPos origin,BlockPos pivot,StructureTemplate.StructureBlockInfo original,StructureTemplate.StructureBlockInfo current,StructurePlaceSettings settings){
   BlockPos pos=project(level,generator,current.pos,origin,projection);BlockState state=current.state;
   if(processor.equals("ps4:igloo_closed")&&original.pos.equals(new BlockPos(3,0,5)))state=Blocks.SNOW_BLOCK.defaultBlockState();
   if(processor.startsWith("ps4:shipwreck/"))state=shipwood(state,Integer.parseInt(processor.substring(processor.lastIndexOf('/')+1)));
   boolean desert=generator instanceof LegacyChunkGenerator legacy&&dev.legacyconsole.worldgen.ps4.Ps4BiomeData.getBiome(legacy.source().layers().blockBiome(pos.getX(),pos.getZ())).family==1;
   state=Ps4VillageProcessors.process(processor,state,level.getBlockState(pos),settings.getRandom(pos),desert);
   return new StructureTemplate.StructureBlockInfo(pos,state,current.nbt);
  }
  @Override protected StructureProcessorType<?> getType(){return StructureProcessorType.RULE;}
 }
 private static BlockState shipwood(BlockState state,int palette){
  int[][] colors={{0,1},{3,1},{5,3},{5,1},{1,0},{1,3},{1,5},{0,2},{1,4}};String[] woods={"oak","spruce","birch","jungle","acacia","dark_oak"};String name=Registry.BLOCK.getKey(state.getBlock()).getPath(),old=name.startsWith("dark_oak_")?"dark_oak":name.startsWith("stripped_oak_")||name.startsWith("oak_")?"oak":null;
  if(old==null)return state;String wood=woods[colors[palette][old.equals("oak")?0:1]];String changed=name.replace(old,wood);var block=Registry.BLOCK.get(new ResourceLocation("minecraft",changed));BlockState result=block.defaultBlockState();for(var property:state.getProperties())if(result.hasProperty(property))result=copy(result,state,property);return result;
 }
 private static <T extends Comparable<T>> BlockState copy(BlockState target,BlockState source,net.minecraft.world.level.block.state.properties.Property<T> property){return target.setValue(property,source.getValue(property));}
 private void foundation(WorldGenLevel level,Random random,BoundingBox clip){
  BoundingBox box=getBoundingBox();int floor=box.minY();BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
  for(int x=Math.max(box.minX(),clip.minX());x<=Math.min(box.maxX(),clip.maxX());x++)for(int z=Math.max(box.minZ(),clip.minZ());z<=Math.min(box.maxZ(),clip.maxZ());z++){
   pos.set(x,floor,z);BlockState bottom=level.getBlockState(pos);if(bottom.isAir()||!bottom.getMaterial().isSolid())continue;BlockState fill=bottom.is(Blocks.GRASS_BLOCK)||bottom.is(Blocks.DIRT_PATH)?Blocks.DIRT.defaultBlockState():bottom;
   int depth=8+random.nextInt(4);for(int y=floor-1;y>floor-depth&&y>1;y--){pos.setY(y);BlockState old=level.getBlockState(pos);if(!old.isAir()&&!old.getMaterial().isLiquid())break;if(clip.isInside(pos))level.setBlock(pos,fill,2);}
  }
 }
 private void marker(WorldGenLevel level,BlockPos pos,String metadata,Random random){
  if(metadata.startsWith("Chest")){
   Direction facing=metadata.endsWith("North")?Direction.NORTH:metadata.endsWith("South")?Direction.SOUTH:metadata.endsWith("West")?Direction.WEST:Direction.EAST;
   createChest(level,getBoundingBox(),random,pos,BuiltInLootTables.WOODLAND_MANSION,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,rotation.rotate(facing)));return;
  }
  ResourceLocation loot=switch(metadata){case "mapChest"->BuiltInLootTables.SHIPWRECK_MAP;case "treasureChest"->BuiltInLootTables.SHIPWRECK_TREASURE;case "supplyChest"->BuiltInLootTables.SHIPWRECK_SUPPLY;case "topChest"->BuiltInLootTables.PILLAGER_OUTPOST;case "chest"->name.startsWith("igloo/")?BuiltInLootTables.IGLOO_CHEST:BuiltInLootTables.UNDERWATER_RUIN_SMALL;default->null;};
  if(loot!=null){if(name.startsWith("ruin/")){level.setBlock(pos,Blocks.CHEST.defaultBlockState(),2);if(level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity chest)chest.setLootTable(name.contains("big_")?BuiltInLootTables.UNDERWATER_RUIN_BIG:loot,random.nextLong());}
   else {if(level.getBlockEntity(pos.below()) instanceof RandomizableContainerBlockEntity chest)chest.setLootTable(loot,random.nextLong());level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);}return;}
  EntityType<? extends Mob> type=null;
  if(metadata.startsWith("minecraft:villager_v2"))type=EntityType.VILLAGER;
  else if(metadata.startsWith("minecraft:iron_golem")||metadata.equals("cage"))type=EntityType.IRON_GOLEM;
  else if(metadata.startsWith("minecraft:cat"))type=EntityType.CAT;
  else if(metadata.equals("minecraft:horse"))type=EntityType.HORSE;
  else if(metadata.equals("minecraft:sheep"))type=EntityType.SHEEP;
  else if(metadata.equals("minecraft:pig"))type=EntityType.PIG;
  else if(metadata.equals("minecraft:cow"))type=EntityType.COW;
  else if(metadata.equals("Mage"))type=EntityType.EVOKER;
  else if(metadata.equals("Warrior"))type=EntityType.VINDICATOR;
  else if(metadata.equals("pillager")||metadata.equals("captain"))type=EntityType.PILLAGER;
  if(type!=null){Mob mob=type.create(level.getLevel());if(mob!=null){mob.setPersistenceRequired();mob.moveTo(pos,0,0);mob.finalizeSpawn(level,level.getCurrentDifficultyAt(pos),MobSpawnType.STRUCTURE,null,null);if(metadata.equals("captain")&&mob instanceof net.minecraft.world.entity.monster.PatrollingMonster patrol){patrol.setPatrolLeader(true);patrol.setItemSlot(EquipmentSlot.HEAD,net.minecraft.world.entity.raid.Raid.getLeaderBannerInstance());patrol.setDropChance(EquipmentSlot.HEAD,2.0f);}if(mob instanceof Villager villager)villager.setVillagerData(villager.getVillagerData().setType(VillagerType.byBiome(level.getBiomeName(pos))));level.addFreshEntityWithPassengers(mob);}level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);}
 }
}

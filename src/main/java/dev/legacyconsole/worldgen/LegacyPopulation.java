package dev.legacyconsole.worldgen;

import dev.legacyconsole.worldgen.ps4.*;
import dev.legacyconsole.worldgen.ps4.structure.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.*;

/** Minecraft/Forge adapter for the translated PS4 population sequence.
 * Feature kernels and the shared RNG are original translations. Chunk access,
 * collision/light queries, block entities, loot and updates use 1.17.1 APIs.
 * This adapter deliberately does not claim complete original world physics. */
final class LegacyPopulation implements Ps4Population.Access,Ps4BiomeDecorations.Access,
 Ps4TreeTrunk.Access,Ps4VineRules.Access,Ps4ScatterFeatures.Access,Ps4LakeFeature.Access,
 Ps4DungeonFeature.Access,Ps4SpringFeature.Access,Ps4LiquidFlow.Access,Ps4IceFeatures.Access,Ps4PumpkinFeature.Access,
 Ps4CoralCrustFeature.Access,Ps4CoralCrustDecoration.Access,Ps4CoralTemplate.Access,
 Ps4FossilFeature.Access,Ps4FossilTemplate.Access,Ps4SnowAndIce.SnowAccess {
 private static final Ps4BlockMaterials MATERIALS=new Ps4BlockMaterials();
 private static final Map<Integer,Ps4FossilTemplate> FOSSILS=new ConcurrentHashMap<>();
 private static final Map<Integer,Ps4CoralTemplate> CRUSTS=new ConcurrentHashMap<>();
 // Original crust palette colors use a process-level clock stream. This target
 // adaptation fixes that independent stream so chunk scheduling cannot alter it.
 private static final Ps4Random PALETTE_RANDOM=new Ps4Random(0);
 static{for(int id=177;id<=187;id++)crust(id);}
 private final LegacyChunkGenerator generator;
 private final WorldGenRegion region;
 private final StructureFeatureManager structures;
 private final Map<BlockPos,Integer> written=new HashMap<>();
 private final Set<BlockPos> leaves=new HashSet<>();
 private final Ps4DirectionPlane horizontal=new Ps4DirectionPlane();
 private final Ps4LiquidFlow water=new Ps4LiquidFlow(false,horizontal),lava=new Ps4LiquidFlow(true,horizontal);
 private final Ps4AquaticFeatures.Seagrass seagrass=new Ps4AquaticFeatures.Seagrass();
 private final Ps4IcebergFeature iceberg=new Ps4IcebergFeature();
 private record BeeTree(int x,int z,int biome,List<BlockPos> logs,List<BlockPos> leaves){}
 private final List<BeeTree> beeTrees=new ArrayList<>();
 private List<BlockPos> treeLogs,treeLeaves;
 private Ps4PopulationProfiles.Profile profile;
 private boolean immediate;
 private LegacyPopulation(LegacyChunkGenerator generator,WorldGenRegion region,StructureFeatureManager structures){this.generator=generator;this.region=region;this.structures=structures;}
 static void populate(LegacyChunkGenerator generator,WorldGenRegion region,StructureFeatureManager structures){
  var world=new LegacyPopulation(generator,region,structures);ChunkPos p=region.getCenter();
  Ps4Population.populate(generator.seed(),p.x,p.z,generator.structures(),Ps4Population.DEFAULTS,world);
  world.finishPlants();
  world.modernAdditions();
 }
 private BlockPos pos(int x,int y,int z){return new BlockPos(x,y,z);}
 private boolean inside(int x,int z){return generator.size().containsBlock(x,z);}
 private boolean available(int x,int y,int z){return y>=0&&y<256&&region.hasChunk(x>>4,z>>4);}
 private boolean writable(BlockPos p){ChunkPos center=region.getCenter();return inside(p.getX(),p.getZ())&&p.getY()>=0&&p.getY()<256&&Math.abs((p.getX()>>4)-center.x)<=1&&Math.abs((p.getZ()>>4)-center.z)<=1;}
 private BlockState state(int x,int y,int z){return available(x,y,z)?region.getBlockState(pos(x,y,z)):Blocks.AIR.defaultBlockState();}
 private Ps4Material material(int raw){return MATERIALS.forRegisteredBlock(raw&65535);}
 public int block(int x,int y,int z){
  if(!available(x,y,z))return 0;BlockPos p=pos(x,y,z);Integer raw=written.get(p);
  if(raw!=null&&Ps4GenerationStates.decode(raw).getBlock()==region.getBlockState(p).getBlock())return raw;
  return Ps4GenerationStates.encode(region.getBlockState(p));
 }
 public void setBlock(int x,int y,int z,int raw,int flags){
  if(!available(x,y,z))return;BlockPos p=pos(x,y,z);if(!writable(p))return;
  BlockState converted=Ps4GenerationStates.decode(raw);
  if((raw&65535)==175&&(raw>>>16&8)!=0)converted=Ps4GenerationStates.doublePlant(block(x,y-1,z));
  if(region.setBlock(p,converted,flags|16)){
   written.put(p,raw);
   if(converted.getBlock() instanceof LeavesBlock)leaves.add(p);
   if(treeLogs!=null){int id=raw&65535;if(id==17||id==162)treeLogs.add(p);else if(id==18||id==161)treeLeaves.add(p);}
   if((raw&65535)==258&&state(x,y-1,z).is(Blocks.KELP))region.setBlock(p.below(),Blocks.KELP_PLANT.defaultBlockState(),18);
  }
 }
 public int height(int x,int z){return region.hasChunk(x>>4,z>>4)?region.getHeight(Heightmap.Types.MOTION_BLOCKING,x,z):0;}
 public int height(int x,int y,int z){return height(x,z);}
 public int heightXZ(int x,int z){return height(x,z);}
 public int topSolidOrLiquid(int x,int y,int z){return region.hasChunk(x>>4,z>>4)?region.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,x,z):0;}
 public int seaLevel(){return generator.getSeaLevel();}
 public boolean empty(int x,int y,int z){return (block(x,y,z)&65535)==0;}
 public boolean solid(int s){return Ps4GenerationStates.original(s)?material(s).solid():Ps4GenerationStates.decode(s).getMaterial().isSolid();}
 public boolean liquid(int s){return Ps4GenerationStates.original(s)?material(s).liquid():Ps4GenerationStates.decode(s).getMaterial().isLiquid();}
 public boolean replaceable(int s){return Ps4GenerationStates.original(s)?material(s).replaceable():Ps4GenerationStates.decode(s).getMaterial().isReplaceable();}
 public boolean airMaterial(int s){return Ps4GenerationStates.original(s)?Ps4BlockMaterials.alias(s&65535)==0x14188d0L:Ps4GenerationStates.decode(s).isAir();}
 private boolean alias(int s,long alias){return Ps4GenerationStates.original(s)&&Ps4BlockMaterials.alias(s&65535)==alias;}
 public boolean leavesMaterial(int s){return alias(s,0x14187a0L)||!Ps4GenerationStates.original(s)&&Ps4GenerationStates.decode(s).getBlock() instanceof LeavesBlock;}
 public boolean waterMaterial(int s){return alias(s,0x1418330L);}
 public boolean lavaMaterial(int s){return alias(s,0x1418328L);}
 public boolean iceMaterial(int s){return alias(s,0x14188e8L);}
 public boolean vineMaterial(int s){return alias(s,0x1419000L);}
 public boolean dirtMaterial(int s){return alias(s,0x14188f0L);}
 public boolean grassMaterial(int s){return alias(s,0x1418fe0L);}
 public boolean waterFluid(int x,int y,int z){return state(x,y,z).getFluidState().is(FluidTags.WATER);}
 public boolean solidRender(int s){return Ps4GenerationStates.decode(s).isSolidRender(EmptyBlockGetter.INSTANCE,BlockPos.ZERO);}
 public boolean solidBlocking(int s){return Ps4GenerationStates.original(s)?material(s).solidBlocking():Ps4GenerationStates.decode(s).getMaterial().isSolidBlocking();}
 public boolean solidBlocking(int x,int y,int z,boolean unloaded){return available(x,y,z)?solidBlocking(block(x,y,z)):unloaded;}
 public int rawBrightness(int x,int y,int z){return Math.max(skyLight(x,y,z),blockLight(x,y,z));}
 public int skyLight(int x,int y,int z){return available(x,y,z)?y>=height(x,z)?15:region.getBrightness(LightLayer.SKY,pos(x,y,z)):15;}
 public int blockLight(int x,int y,int z){return available(x,y,z)?region.getBrightness(LightLayer.BLOCK,pos(x,y,z)):0;}
 public float temperature(int x,int y,int z){return Ps4BiomeTemperature.at(biome(x,z),x,y,z);}
 public int biomeTop(int x,int y,int z){return Ps4PopulationProfiles.get(biome(x,z)).top();}
 public int biome(int x,int z){return generator.source().layers().blockBiome(x,z);}
 public double biomeNoise(double x,double z){return Ps4BiomePlants.biomeNoise(x,z);}
 public boolean hasCeiling(){return false;}
 public boolean clientSide(){return false;}
 public boolean slab(int s){return Ps4GenerationStates.decode(s).getBlock() instanceof SlabBlock;}
 public boolean seagrassBelowFlag(int s){return !solidBlocking(s);}
 public boolean seagrassReentryBlocked(){return true;}
 public int effectiveBlock(int x,int y,int z){BlockState state=state(x,y,z);return state.getFluidState().is(FluidTags.WATER)?9|(state.getFluidState().getType() instanceof net.minecraft.world.level.material.FlowingFluid?state.getFluidState().createLegacyBlock().getValue(net.minecraft.world.level.block.LiquidBlock.LEVEL)<<16:0):block(x,y,z);}
 public boolean canPlace(int raw,int x,int y,int z){return available(x,y,z)&&(replaceable(block(x,y,z))||waterFluid(x,y,z));}
 public boolean canPlaceState(int raw,int x,int y,int z){return canPlace(raw,x,y,z);}
 public int faceShape(int s,int x,int y,int z,int direction){return supportsFace(s,direction,0)?0:1;}
 public boolean supportsFace(int s,int direction,int kind){return Ps4GenerationStates.decode(s).isFaceSturdy(EmptyBlockGetter.INSTANCE,BlockPos.ZERO,Direction.from3DDataValue(direction));}
 public Ps4DirectionPlane horizontalDirections(){return horizontal;}
 public boolean hasChunk(int x,int z){return region.hasChunk(x,z);}
 public boolean hasChunk(int x,int z,boolean allowEmpty){return hasChunk(x,z);}
 public boolean columnAvailable(int x,int y,int z){return available(x,y,z);}
 public void notifyNeighbors(int x,int y,int z,int original,int flags){/* Deferred to target chunk postprocessing. */}
 public void templateBlockEntityChanged(int x,int y,int z){var entity=region.getBlockEntity(pos(x,y,z));if(entity!=null)entity.setChanged();}
 public boolean placeCoralBlock(int x,int y,int z,int raw,int flags){if(!canPlace(raw,x,y,z))return false;setBlock(x,y,z,raw,flags);return true;}

 public void populationMode(boolean active){}
 public boolean structure(Ps4Population.Structure kind,Random random,int cx,int cz,boolean villageFlag){
  List<StructureFeature<?>> features=switch(kind){
   case MINESHAFT->List.of(StructureFeature.MINESHAFT);case VILLAGE->List.of(StructureFeature.VILLAGE);case STRONGHOLD->List.of(StructureFeature.STRONGHOLD);
   case SCATTERED->List.of(StructureFeature.DESERT_PYRAMID,StructureFeature.JUNGLE_TEMPLE,StructureFeature.SWAMP_HUT,StructureFeature.IGLOO);
   case MONUMENT->List.of(StructureFeature.OCEAN_MONUMENT);case OCEAN_RUIN->List.of(StructureFeature.OCEAN_RUIN);case MANSION->List.of(StructureFeature.WOODLAND_MANSION);
   case BURIED_TREASURE->List.of(StructureFeature.BURIED_TREASURE);case SHIPWRECK->List.of(StructureFeature.SHIPWRECK);case OUTPOST->List.of(StructureFeature.PILLAGER_OUTPOST);
  };
  BoundingBox clip=new BoundingBox(cx*16,0,cz*16,cx*16+15,255,cz*16+15);boolean[] placed={false};
  for(var feature:features)structures.startsForFeature(SectionPos.of(cx,0,cz),feature).forEach(start->{if(start.isValid()){start.placeInChunk(region,structures,generator,random,clip,new ChunkPos(cx,cz));placed[0]=true;}});
  return placed[0];
 }
 public void finishOutpostChunk(int x,int z){}
 public void lake(Random random,int x,int y,int z,int tile){new Ps4LakeFeature(tile).place(this,random,x,y,z);}
 public void dungeon(Random random,int x,int y,int z){Ps4DungeonFeature.place(this,random,x,y,z);}
 public void decorate(Random random,int id,int x,int z){
  profile=Ps4PopulationProfiles.get(id);var settings=Ps4BiomeSettings.forBiome(id);
  if(profile.decoration()==null)baseDecoration(random,settings,x,0,z);
  else Ps4BiomeDecorations.decorate(profile.decoration(),profile.variant(),Ps4BiomeData.getBiome(id).temperature,this,random,settings,x,0,z);
 }
 public void baseDecoration(Random random,Ps4BiomeDecorator.Settings settings,int x,int y,int z){Ps4BiomeDecorator.decorate(this,random,settings,x,y,z);}
 public void ores(Random random,int x,int y,int z){if(Ps4BiomeData.getBiome(profile.id()).family==7)Ps4OreDecorator.decorateMesa(this,random,x,z);else Ps4OreDecorator.decorate(this,random,x,z);}
 public Ps4BiomeDecorator.Tree tree(Random random){
  var selected=Ps4BiomeTrees.select(profile.tree(),profile.variant(),random);var tree=selected.bind(this);int id=profile.id();
  // Bee placement is deferred until the original population sequence completes.
  return new Ps4BiomeDecorator.Tree(){
   private final List<BlockPos> logs=new ArrayList<>(),foliage=new ArrayList<>();
   public void prepare(){tree.prepare();}
   public boolean place(Ps4BiomeDecorator.Access w,Random r,int x,int y,int z){treeLogs=logs;treeLeaves=foliage;try{return tree.place(w,r,x,y,z);}finally{treeLogs=null;treeLeaves=null;}}
   public void postPlace(Ps4BiomeDecorator.Access w,Random r,int x,int y,int z){tree.postPlace(w,r,x,y,z);int wood=selected.log()>>>16&3;if((selected.log()&65535)==17&&(wood==0||wood==2)&&!logs.isEmpty()&&!foliage.isEmpty())beeTrees.add(new BeeTree(x,z,id,logs,foliage));}
  };
 }
 public Ps4BiomeDecorator.Grass grass(Random random){int raw=Ps4BiomePlants.grass(profile.grass(),random);return (world,r,x,y,z)->Ps4ScatterFeatures.place(Ps4ScatterFeatures.Kind.TALL_GRASS,this,r,x,y,z,raw);}
 public int flower(Random random,int x,int y,int z){return Ps4BiomePlants.flower(profile.flowers(),profile.variant(),random,x,z);}
 public void place(Ps4BiomeDecorator.Feature feature,Random random,int x,int y,int z,int raw){
  switch(feature){
   case SAND->Ps4DepositFeature.sand(this,random,x,y,z,7,12);case GRAVEL->Ps4DepositFeature.sand(this,random,x,y,z,6,13);case CLAY->Ps4DepositFeature.clay(this,random,x,y,z,4);
   case HUGE_MUSHROOM->Ps4HugeMushroomFeature.place(this,random,x,y,z,0,false);
   case FLOWER->Ps4ScatterFeatures.place(Ps4ScatterFeatures.Kind.FLOWER,this,random,x,y,z,raw);
   case DEAD_BUSH->Ps4ScatterFeatures.place(Ps4ScatterFeatures.Kind.DEAD_BUSH,this,random,x,y,z,32);
   case WATERLILY->Ps4ScatterFeatures.place(Ps4ScatterFeatures.Kind.WATERLILY,this,random,x,y,z,111);
   case BROWN_MUSHROOM,RED_MUSHROOM->Ps4ScatterFeatures.place(Ps4ScatterFeatures.Kind.MUSHROOM,this,random,x,y,z,raw);
   case REEDS->Ps4ScatterFeatures.place(Ps4ScatterFeatures.Kind.REEDS,this,random,x,y,z,83);
   case PUMPKIN->Ps4PumpkinFeature.place(this,random,x,y,z);
   case CACTUS->Ps4ScatterFeatures.place(Ps4ScatterFeatures.Kind.CACTUS,this,random,x,y,z,81);
   case KELP->Ps4AquaticFeatures.kelp(this,random,x,y,z);case SEAGRASS->seagrass.place(this,random,x,y,z);
   case WATER_SPRING,LAVA_SPRING->Ps4SpringFeature.place(this,random,x,y,z,raw);
  }
 }
 public boolean decoration(Ps4BiomeDecorations.Feature feature,Random random,int x,int y,int z,int raw){
  return switch(feature){
   case DOUBLE_PLANT->Ps4ScatterFeatures.place(Ps4ScatterFeatures.Kind.DOUBLE_PLANT,this,random,x,y,z,raw);
   case BOULDER->Ps4BlockBlobFeature.place(this,random,x,y,z,0,48);
   case BERRY_BUSH->Ps4BerryBushFeature.place(this,random,x,y,z);case MELON->Ps4MelonFeature.place(this,random,x,y,z);
   case VINES->Ps4VinesFeature.place(this,random,x,y,z);case BAMBOO->Ps4BambooFeature.place(this,random,x,y,z);case PODZOL->Ps4PodzolFeature.place(this,random,x,y,z);
   case DESERT_WELL->Ps4DesertWellFeature.place(this,horizontal,x,y,z);case FOSSIL->Ps4FossilFeature.place(this,x,y,z);
   case HUGE_MUSHROOM->Ps4HugeMushroomFeature.place(this,random,x,y,z,0,false);case SILVERFISH->new Ps4OreFeature(97,9,s->(s&65535)==1).place(this,random,x,y,z);
   case ICEBERG->iceberg.place(this,random,x,y,z);case BLUE_ICE->Ps4IceFeatures.blueIce(this,random,x,y,z);case ICE_SPIKE->Ps4IceFeatures.spike(this,random,x,y,z,false);case ICE_PATCH->Ps4IceFeatures.patch(this,random,x,y,z,4,174);
   case CORAL->Ps4CoralFeatures.coral(this,random,x,y,z);case CORAL_HANG->Ps4CoralFeatures.hanging(this,random,x,y,z);case SEA_ANEMONE->Ps4CoralFeatures.anemone(this,random,x,y,z);case SEA_PICKLES->Ps4AquaticFeatures.pickles(this,random,x,y,z);
   case CORAL_CRUST->Ps4CoralCrustFeature.place(this,(Ps4Random)random,x,y,z,new int[]{0,1,2,3});
  };
 }

 public void removeBlock(int x,int y,int z){setBlock(x,y,z,0,2);}
 public Ps4DungeonFeature.Entity entity(int x,int y,int z){var e=region.getBlockEntity(pos(x,y,z));return e instanceof ChestBlockEntity?Ps4DungeonFeature.Entity.CHEST:e instanceof SpawnerBlockEntity?Ps4DungeonFeature.Entity.SPAWNER:Ps4DungeonFeature.Entity.OTHER;}
 public void chestLoot(int x,int y,int z,String table,long seed){var e=region.getBlockEntity(pos(x,y,z));if(e instanceof RandomizableContainerBlockEntity chest)chest.setLootTable(new ResourceLocation(table),seed);}
 public void spawnerMob(int x,int y,int z,Ps4DungeonFeature.Mob mob){var e=region.getBlockEntity(pos(x,y,z));if(e instanceof SpawnerBlockEntity spawner)spawner.getSpawner().setEntityId(mob==Ps4DungeonFeature.Mob.SKELETON?EntityType.SKELETON:mob==Ps4DungeonFeature.Mob.SPIDER?EntityType.SPIDER:EntityType.ZOMBIE);}
 public int precipitationHeight(int x,int z){return height(x,z);}
 public boolean canFreeze(int x,int y,int z){return Ps4SnowAndIce.canFreeze(this,x,y,z);}
 public void ice(int x,int y,int z){setBlock(x,y,z,79,2);}
 public void snow(int x,int y,int z,boolean checkLight,boolean accumulate){Ps4SnowAndIce.placeSnow(this,x,y,z,checkLight,accumulate);}
 public int blockType(int x,int y,int z){return block(x,y,z)&65535;}
 public int defaultState(int tile){return tile==78?78:tile;}
 public boolean specialSnowSupportMaterial(int s){return leavesMaterial(s)||iceMaterial(s)||dirtMaterial(s);}
 public boolean materialBlocksMotion(int s){return Ps4GenerationStates.original(s)?material(s).blocksMotion():Ps4GenerationStates.decode(s).getMaterial().blocksMotion();}
 public int maximumSnowDepth(int x,int y,int z){return Ps4PopulationProfiles.get(biome(x,z)).maximumSnowDepth();}
 public boolean snowEnabled(){return true;}
 public Integer chunkPrecipitationHeight(int cx,int cz,int x,int y,int z){return hasChunk(cx,cz)?height(x,z):null;}

 public void immediateFluidTicks(boolean enabled){immediate=enabled;}
 public boolean immediateFluidTicks(){return immediate;}
 public void tickFluid(int x,int y,int z,int raw,Random random){((raw&65535)==10?lava:water).tick(this,random,x,y,z,raw);}
 public Ps4LiquidFlow.Pair pairedBlocks(int x,int y,int z){return new Ps4LiquidFlow.Pair(block(x,y,z),secondaryBlock(x,y,z));}
 public int secondaryBlock(int x,int y,int z){int b=block(x,y,z)&65535;return b!=8&&b!=9&&b!=10&&b!=11&&waterFluid(x,y,z)?9:0;}
 public void setSecondary(int x,int y,int z,int raw,int flags){BlockState state=state(x,y,z);if(state.hasProperty(BlockStateProperties.WATERLOGGED)){BlockPos p=pos(x,y,z);if(writable(p))region.setBlock(p,state.setValue(BlockStateProperties.WATERLOGGED,raw!=0),flags|16);}}
 public void removePrimary(int x,int y,int z){removeBlock(x,y,z);}
 public boolean blocksFluid(int raw){return solidBlocking(raw);}
 public boolean canPassFluidFace(int x,int y,int z,int raw,int direction){return !supportsFace(raw,direction,0);}
 public boolean dropsInWater(int raw){return !solid(raw)&&!waterMaterial(raw)&&!airMaterial(raw);}
 public void dropBlock(int x,int y,int z,int raw,int fortune){}
 public void levelEvent(int event,int x,int y,int z,int data){}
 public void fizz(int x,int y,int z){}
 public void scheduleFluid(int x,int y,int z,int tile,int delay){BlockPos p=pos(x,y,z);if(writable(p))region.getLiquidTicks().scheduleTick(p,tile==8?net.minecraft.world.level.material.Fluids.FLOWING_WATER:net.minecraft.world.level.material.Fluids.FLOWING_LAVA,delay);}
 public void notifyFluidNeighbors(int x,int y,int z,int tile,int flags){}
 public int finiteChunkCount(){return generator.size().blocks/16;}
 public boolean hotFlowStep(){return false;}
 public boolean ultraWarm(){return false;}
 public boolean skipLavaWaterReaction(){return false;}
 public boolean lavaStoneContactMaterial(int raw){return alias(raw,0x1418330L)||alias(raw,0x1418ff0L);}
 public boolean fireTickEnabled(){return region.getLevel().getGameRules().getBoolean(GameRules.RULE_DOFIRETICK);}
 public int defaultBlockState(int tile){return defaultState(tile);}
 public boolean flammableMaterial(int s){return Ps4GenerationStates.original(s)&&material(s).flammable();}

 private static InputStream template(int id){String name=Ps4Templates.template(id);return Objects.requireNonNull(LegacyPopulation.class.getResourceAsStream(Ps4Templates.ROOT+"templates/"+name+".nbt"),name);}
 private static Ps4FossilTemplate fossil(int id){return FOSSILS.computeIfAbsent(id,k->{try(var in=template(k)){return Ps4FossilTemplate.read(in);}catch(IOException e){throw new UncheckedIOException(e);}});}
 private static synchronized Ps4CoralTemplate crust(int id){return CRUSTS.computeIfAbsent(id,k->{try(var in=template(k)){return Ps4CoralTemplate.read(in,PALETTE_RANDOM);}catch(IOException e){throw new UncheckedIOException(e);}});}
 public Random chunkRandom(int x,int z,long salt){return Ps4ChunkRandom.create(generator.seed(),x,z,salt);}
 public int rotation(int index){return index;}
 public Ps4FossilFeature.Size templateSize(int template){return fossil(template).size();}
 public int surfaceHeight(int x,int z){return height(x,z);}
 public void placeTemplate(int template,int x,int y,int z,int rotation,Ps4FossilFeature.Clip clip,float integrity,Random random,int flags){fossil(template).place(this,random,x,y,z,rotation,clip,integrity,flags);}
 public void placeCoralTemplate(int template,int x,int y,int z,int rotation,float integrity,int flags){crust(template).place(this,x,y,z,rotation,flags);}
 public void decorateCoralTemplate(int template,int x,int y,int z,int rotation,Ps4Random random){Ps4CoralCrustDecoration.decorate(this,random,crust(template).markers(x,y,z,rotation));}

 private void finishPlants(){
  // PS4 has no Java leaf-distance property. Recompute it after all features so
  // generated trees remain valid and their leaves decay normally when chopped.
  Map<BlockPos,Integer> distances=new HashMap<>();ArrayDeque<BlockPos> queue=new ArrayDeque<>();
  for(BlockPos p:leaves)if(state(p.getX(),p.getY(),p.getZ()).getBlock() instanceof LeavesBlock){
   for(Direction d:Direction.values()){int raw=block(p.getX()+d.getStepX(),p.getY()+d.getStepY(),p.getZ()+d.getStepZ())&65535;if(raw==17||raw==162){distances.put(p,1);queue.add(p);break;}}
  }
  while(!queue.isEmpty()){BlockPos p=queue.removeFirst();int next=distances.get(p)+1;if(next>6)continue;for(Direction d:Direction.values()){BlockPos n=p.relative(d);if(leaves.contains(n)&&distances.getOrDefault(n,7)>next){distances.put(n,next);queue.add(n);}}}
  for(BlockPos p:leaves){BlockState state=region.getBlockState(p);if(state.getBlock() instanceof LeavesBlock)region.setBlock(p,state.setValue(LeavesBlock.DISTANCE,distances.getOrDefault(p,7)),18);}
 }
 private void modernAdditions(){
  ChunkPos center=region.getCenter();int x=center.getMinBlockX(),z=center.getMinBlockZ();BlockPos origin=new BlockPos(x,0,z);
  // Vanilla configured features, with separate domain seeds. They neither draw
  // from nor run between the translated PS4 feature calls. Leave enough room
  // for their geometry to keep the finite outside-ocean continuation intact.
  if(inside(x-16,z-16)&&inside(x+31,z+31)){
   WorldGenLevel bounded=boundedModernWorld();
   net.minecraft.data.worldgen.Features.ORE_DEEPSLATE.place(bounded,generator,Ps4ChunkRandom.create(generator.seed(),center.x,center.z,0x44454550534c4154L),origin);
   net.minecraft.data.worldgen.Features.ORE_COPPER.place(bounded,generator,Ps4ChunkRandom.create(generator.seed(),center.x,center.z,0x434f50504552L),origin);
   net.minecraft.data.worldgen.Features.AMETHYST_GEODE.place(bounded,generator,Ps4ChunkRandom.create(generator.seed(),center.x,center.z,0x47454f4445L),origin);
  }
  for(var tree:beeTrees){
   float chance=tree.biome==1||tree.biome==129||tree.biome==132?.05f:.002f;
   tree.logs.sort(Comparator.comparingInt(BlockPos::getY));tree.leaves.sort(Comparator.comparingInt(BlockPos::getY));
   var random=Ps4ChunkRandom.create(generator.seed(),tree.x,tree.z,0x42454553L);
   new net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator(chance).place(region,(p,s)->{if(writable(p))region.setBlock(p,s,19);},random,tree.logs,tree.leaves);
  }
 }
 private WorldGenLevel boundedModernWorld(){
  // Match by the Java method signature, not its name: production Forge remaps
  // method names. All reads and non-placement operations retain the real region.
  return (WorldGenLevel)java.lang.reflect.Proxy.newProxyInstance(WorldGenLevel.class.getClassLoader(),new Class<?>[]{WorldGenLevel.class},(proxy,method,args)->{
   Class<?>[] types=method.getParameterTypes();
   if(method.getReturnType()==boolean.class&&(types.length==3||types.length==4)&&types[0]==BlockPos.class&&types[1]==BlockState.class&&types[2]==int.class&&!writable((BlockPos)args[0]))return false;
   try{return method.invoke(region,args);}catch(java.lang.reflect.InvocationTargetException error){throw error.getCause();}
  });
 }
}

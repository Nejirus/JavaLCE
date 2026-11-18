package dev.legacyconsole.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.legacyconsole.worldgen.ps4.Ps4Terrain;
import dev.legacyconsole.worldgen.ps4.Ps4Carvers;
import dev.legacyconsole.worldgen.ps4.Ps4BiomeSurfaces;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.core.*;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;

/** Persisted PS4 Overworld generator. Nether/End generators are never replaced. */
public final class LegacyChunkGenerator extends ChunkGenerator {
 public static final Codec<LegacyChunkGenerator> CODEC=RecordCodecBuilder.<LegacyChunkGenerator>create(instance->instance.group(
  RegistryLookupCodec.<Biome>create(Registry.BIOME_REGISTRY).forGetter(generator->generator.source.registry()),
  Codec.LONG.fieldOf("seed").forGetter(generator->generator.seed),
  Ps4WorldSize.CODEC.optionalFieldOf("size",Ps4WorldSize.LARGE).forGetter(generator->generator.size),
  Codec.BOOL.optionalFieldOf("structures",true).forGetter(generator->generator.structures),
  Ps4BiomeScale.CODEC.optionalFieldOf("biome_scale",Ps4BiomeScale.SMALL).forGetter(generator->generator.scale())
 ).apply(instance,LegacyChunkGenerator::new)).stable();
 private static final BlockState AIR=Blocks.AIR.defaultBlockState(),STONE=Blocks.STONE.defaultBlockState(),WATER=Blocks.WATER.defaultBlockState();
 private static final Map<net.minecraft.world.level.block.Block,Integer> SURFACE_VALUES=surfaceValues();
 private final long seed;
 private final Ps4WorldSize size;
 private final boolean structures;
 private final LegacyBiomeSource source;
 private final Ps4Terrain terrain;
 private final Ps4BiomeSurfaces surfaces;
 private volatile ChunkPos stronghold;
 private final dev.legacyconsole.worldgen.ps4.structure.Ps4Structures ps4Structures=new dev.legacyconsole.worldgen.ps4.structure.Ps4Structures(this);
 public dev.legacyconsole.worldgen.ps4.structure.Ps4Structures ps4Structures(){return ps4Structures;}
 // Height/column queries from structure generation often repeat within the same chunk.
 private final ThreadLocal<BaseChunk> lastBase=new ThreadLocal<>();
 private record BaseChunk(int x,int z,byte[] blocks){}
 public LegacyChunkGenerator(Registry<Biome> registry,long seed,Ps4WorldSize size,boolean structures){this(registry,seed,size,structures,Ps4BiomeScale.SMALL);}
 public LegacyChunkGenerator(Registry<Biome> registry,long seed,Ps4WorldSize size,boolean structures,Ps4BiomeScale scale){this(new LegacyBiomeSource(registry,seed,size,scale),seed,size,structures);}
 private LegacyChunkGenerator(LegacyBiomeSource source,long seed,Ps4WorldSize size,boolean structures){
  super(source,source,new StructureSettings(structures),seed);
  this.source=source;this.seed=seed;this.size=size;this.structures=structures;terrain=new Ps4Terrain(seed,size.blocks,source.layers());surfaces=new Ps4BiomeSurfaces(seed);
 }
 public long seed(){return seed;}
 public Ps4WorldSize size(){return size;}
 public Ps4BiomeScale scale(){return source.scale();}
 public boolean structures(){return structures;}
 public LegacyBiomeSource source(){return source;}
 @Override protected Codec<? extends ChunkGenerator> codec(){return CODEC;}
 @Override public LegacyChunkGenerator withSeed(long value){return new LegacyChunkGenerator(source.registry(),value,size,structures,scale());}
 private ChunkPos stronghold(){
  ChunkPos pos=stronghold;if(pos!=null)return pos;
  synchronized(this){if(stronghold==null){int[] location=dev.legacyconsole.worldgen.ps4.Ps4Stronghold.position(seed,size.blocks,source.layers());stronghold=new ChunkPos(location[0],location[1]);}return stronghold;}
 }
 @Override public boolean hasStronghold(ChunkPos pos){return structures&&pos.equals(stronghold());}
 @Override public BlockPos findNearestMapFeature(net.minecraft.server.level.ServerLevel level,net.minecraft.world.level.levelgen.feature.StructureFeature<?> feature,BlockPos pos,int radius,boolean skipKnown){
  return ps4Structures.locate(level,feature,pos,radius,skipKnown);
 }
 @Override public void createStructures(RegistryAccess registries,StructureFeatureManager manager,ChunkAccess chunk,net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager templates,long worldSeed){ps4Structures.createStructures(manager,chunk);}
 @Override public void createReferences(WorldGenLevel level,StructureFeatureManager manager,ChunkAccess chunk){
  if(!structures)return;super.createReferences(level,manager,chunk);ps4Structures.addDistantVillageReferences(manager,chunk);
 }
 @Override public int getGenDepth(){return 256;}
 @Override public int getMinY(){return 0;}
 @Override public int getSeaLevel(){return Ps4Terrain.SEA_LEVEL;}
 @Override public int getSpawnHeight(LevelHeightAccessor level){return getSeaLevel()+1;}
 private byte[] base(int cx,int cz){BaseChunk old=lastBase.get();if(old!=null&&old.x==cx&&old.z==cz)return old.blocks;byte[] data=terrain.baseChunk(cx,cz);lastBase.set(new BaseChunk(cx,cz,data));return data;}
 private static BlockState block(byte id){return id==1?STONE:id==2?WATER:AIR;}
 @Override public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor,StructureFeatureManager structures,ChunkAccess chunk){
  // ChunkStatus owns this chunk exclusively at this stage; no asynchronous mutation escapes.
  int cx=chunk.getPos().x,cz=chunk.getPos().z;byte[] data=base(cx,cz);BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
  Heightmap floor=chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG),surface=chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
  for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<128;y++){
   byte id=data[(x*16+z)*128+y];if(id==0)continue;BlockState state=block(id);
   chunk.setBlockState(pos.set(x,y,z),state,false);floor.update(x,y,z,state);surface.update(x,y,z,state);
  }return CompletableFuture.completedFuture(chunk);
 }
 @Override public int getBaseHeight(int x,int z,Heightmap.Types type,LevelHeightAccessor level){
  byte[] data=base(x>>4,z>>4);int offset=((x&15)*16+(z&15))*128;
  for(int y=127;y>=0;y--)if(type.isOpaque().test(block(data[offset+y])))return y+1;return 0;
 }
 @Override public NoiseColumn getBaseColumn(int x,int z,LevelHeightAccessor level){
  byte[] data=base(x>>4,z>>4);int offset=((x&15)*16+(z&15))*128;BlockState[] states=new BlockState[256];Arrays.fill(states,AIR);
  for(int y=0;y<128;y++)states[y]=block(data[offset+y]);return new NoiseColumn(0,states);
 }
 private static BlockState surfaceState(int state){
  return switch(state&65535){
   case 0->AIR;case 1->STONE;case 2->Blocks.GRASS_BLOCK.defaultBlockState();case 3->(state>>>16)==1?Blocks.COARSE_DIRT.defaultBlockState():(state>>>16)==2?Blocks.PODZOL.defaultBlockState():Blocks.DIRT.defaultBlockState();
   case 7->Blocks.BEDROCK.defaultBlockState();case 8,9->WATER;case 10,11->Blocks.LAVA.defaultBlockState();case 12->(state>>>16)==1?Blocks.RED_SAND.defaultBlockState():Blocks.SAND.defaultBlockState();
   case 13->Blocks.GRAVEL.defaultBlockState();case 24->Blocks.SANDSTONE.defaultBlockState();case 49->Blocks.OBSIDIAN.defaultBlockState();case 78->Blocks.SNOW.defaultBlockState();case 79->Blocks.ICE.defaultBlockState();case 80->Blocks.SNOW_BLOCK.defaultBlockState();case 110->Blocks.MYCELIUM.defaultBlockState();case 111->Blocks.LILY_PAD.defaultBlockState();
   case 159->terracotta(state>>>16);case 172->Blocks.TERRACOTTA.defaultBlockState();case 174->Blocks.PACKED_ICE.defaultBlockState();case 179->Blocks.RED_SANDSTONE.defaultBlockState();case 213->Blocks.MAGMA_BLOCK.defaultBlockState();
   default->throw new IllegalArgumentException("Unknown PS4 surface tile: "+state);
  };
 }
 private static BlockState terracotta(int color){
  return (switch(color&15){
   case 0->Blocks.WHITE_TERRACOTTA;case 1->Blocks.ORANGE_TERRACOTTA;case 2->Blocks.MAGENTA_TERRACOTTA;case 3->Blocks.LIGHT_BLUE_TERRACOTTA;
   case 4->Blocks.YELLOW_TERRACOTTA;case 5->Blocks.LIME_TERRACOTTA;case 6->Blocks.PINK_TERRACOTTA;case 7->Blocks.GRAY_TERRACOTTA;
   case 8->Blocks.LIGHT_GRAY_TERRACOTTA;case 9->Blocks.CYAN_TERRACOTTA;case 10->Blocks.PURPLE_TERRACOTTA;case 11->Blocks.BLUE_TERRACOTTA;
   case 12->Blocks.BROWN_TERRACOTTA;case 13->Blocks.GREEN_TERRACOTTA;case 14->Blocks.RED_TERRACOTTA;default->Blocks.BLACK_TERRACOTTA;
  }).defaultBlockState();
 }
 private static Map<net.minecraft.world.level.block.Block,Integer> surfaceValues(){
  Map<net.minecraft.world.level.block.Block,Integer> result=new IdentityHashMap<>();
  for(int id:new int[]{0,1,2,3,7,9,11,12,13,24,49,78,79,80,110,111,172,174,179,213})result.put(surfaceState(id).getBlock(),id);
  for(int value:new int[]{3|(1<<16),3|(2<<16),12|(1<<16)})result.put(surfaceState(value).getBlock(),value);
  for(int color=0;color<16;color++)result.put(terracotta(color).getBlock(),159|(color<<16));
  return Collections.unmodifiableMap(result);
 }
 @Override public void buildSurfaceAndBedrock(WorldGenRegion region,ChunkAccess chunk){
  int cx=chunk.getPos().x,cz=chunk.getPos().z;Random random=new Random((long)cx*341873128712L+(long)cz*132897987541L);
  if(terrain.outside(chunk.getPos().getMinBlockX(),chunk.getPos().getMinBlockZ()))return;
  double[] noise=terrain.surfaceNoise(cx,cz);int[] ids=source.layers().blockArea(cx*16,cz*16,16,16);BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();int[][] columns=new int[256][128];
  for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<128;y++){
   BlockState state=chunk.getBlockState(pos.set(x,y,z));columns[x*16+z][y]=state.is(STONE.getBlock())?1:state.is(WATER.getBlock())?9:0;
  }
  // Original replaceSurface (0x508110) order and packed axes. Frozen ocean's
  // preliminary pass can modify a different column, so retain the whole buffer
  // across calls rather than regenerating each column independently.
  surfaces.chunk(columns,random,cx,cz,ids,noise);
  for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<128;y++)
   chunk.setBlockState(pos.set(x,y,z),surfaceState(columns[x*16+z][y]),false);
 }
 private static int carverState(BlockState state){return state.isAir()?0:SURFACE_VALUES.getOrDefault(state.getBlock(),127);}
 @Override public void applyCarvers(long worldSeed,BiomeManager manager,ChunkAccess chunk,GenerationStep.Carving step){
  if(step!=GenerationStep.Carving.AIR||terrain.outside(chunk.getPos().getMinBlockX(),chunk.getPos().getMinBlockZ()))return;
  int[] blocks=new int[32768];BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
  for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<128;y++){
   BlockState state=chunk.getBlockState(pos.set(x,y,z));
   blocks[(x*16+z)*128+y]=carverState(state);
  }
  int[] before=blocks.clone();
  int[] biomeIds=source.layers().blockArea(chunk.getPos().getMinBlockX(),chunk.getPos().getMinBlockZ(),16,16);
  int[] topTiles=Ps4BiomeSurfaces.chunkTopTiles(biomeIds,terrain.surfaceNoise(chunk.getPos().x,chunk.getPos().z));
  java.util.function.IntBinaryOperator biomeAt=(x,z)->(x>>4)==chunk.getPos().x&&(z>>4)==chunk.getPos().z?biomeIds[(x&15)+(z&15)*16]:source.layers().blockBiome(x,z);
  Ps4Carvers.carve(seed,chunk.getPos().x,chunk.getPos().z,blocks,biomeAt,(x,z)->topTiles[biomeAt.applyAsInt(x,z)]);
  for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=0;y<128;y++){
   int i=(x*16+z)*128+y;if(blocks[i]==before[i])continue;
   BlockState state=blocks[i]==0?Blocks.CAVE_AIR.defaultBlockState():surfaceState(blocks[i]);
   chunk.setBlockState(pos.set(x,y,z),state,false);
   // PS4's flowing-water tile shares Java's source-water block state;
   // postprocessing schedules its fluid update after chunk generation.
   if(blocks[i]==8)chunk.markPosForPostprocessing(pos);
  }
 }
 @Override public net.minecraft.util.random.WeightedRandomList<net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData> getMobsAt(Biome biome,StructureFeatureManager manager,net.minecraft.world.entity.MobCategory category,BlockPos pos){
  var spawns=net.minecraftforge.common.world.StructureSpawnManager.getStructureSpawns(manager,category,pos);if(spawns!=null)return spawns;
  if(category==net.minecraft.world.entity.MobCategory.UNDERGROUND_WATER_CREATURE&&manager.getStructureAt(pos,false,net.minecraft.world.level.levelgen.feature.StructureFeature.OCEAN_MONUMENT).isValid())return net.minecraft.world.level.levelgen.feature.StructureFeature.OCEAN_MONUMENT.getSpecialUndergroundWaterAnimals();
  return super.getMobsAt(biome,manager,category,pos);
 }
 @Override public void spawnOriginalMobs(WorldGenRegion region){
  // mcps4 0x4a6450, called by Overworld postProcess, is a single RET.
 }
 @Override public void applyBiomeDecoration(WorldGenRegion region,StructureFeatureManager manager){
  ChunkPos center=region.getCenter();if(!terrain.outside(center.getMiddleBlockX(),center.getMiddleBlockZ()))LegacyPopulation.populate(this,region,manager);
 }
}

package dev.legacyconsole.worldgen;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.util.Properties;
import net.minecraft.core.*;
import net.minecraft.resources.*;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.junit.*;import static org.junit.Assert.*;

public class LegacyWorldgenTest {
 @BeforeClass public static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();LegacyWorldgen.register();}
 private WorldGenSettings vanilla(RegistryAccess registries){Properties p=new Properties();p.setProperty("level-seed","123456789");return WorldGenSettings.create(registries,p);}
 @Test public void allSizesRoundTripAndOnlyOverworldChanges(){
  for(Ps4WorldSize size:Ps4WorldSize.values()){
   RegistryAccess.RegistryHolder registries=RegistryAccess.builtin();WorldGenSettings base=vanilla(registries),legacy=LegacyWorldgen.create(registries,base,size,true,false);
   assertTrue(legacy.overworld() instanceof LegacyChunkGenerator);
   assertSame(base.dimensions().get(LevelStem.NETHER),legacy.dimensions().get(LevelStem.NETHER));
   assertSame(base.dimensions().get(LevelStem.END),legacy.dimensions().get(LevelStem.END));
   var write=RegistryWriteOps.create(JsonOps.INSTANCE,registries);
   JsonElement json=ChunkGenerator.CODEC.encodeStart(write,legacy.overworld()).getOrThrow(false,fail->{});
   var read=RegistryReadOps.create(JsonOps.INSTANCE,new SimpleReloadableResourceManager(PackType.SERVER_DATA),registries);
   JsonElement all=WorldGenSettings.CODEC.encodeStart(write,legacy).getOrThrow(false,fail->{});
   var decoded=WorldGenSettings.CODEC.parse(read,all);assertEquals(com.mojang.serialization.Lifecycle.stable(),decoded.lifecycle());assertTrue(decoded.result().isPresent());
   LegacyChunkGenerator copy=(LegacyChunkGenerator)ChunkGenerator.CODEC.parse(read,json).getOrThrow(false,fail->{});
   assertEquals(size,copy.size());assertEquals(base.seed(),copy.seed());assertTrue(copy.structures());
   assertEquals("bamboo_jungle",dev.legacyconsole.worldgen.ps4.Ps4BiomeData.getBiome(40).key);
   assertEquals(copy.source().getNoiseBiome(0,0,0),copy.source().getNoiseBiome(0,63,0));
   assertEquals(987654321L,copy.withSeed(987654321L).seed());assertEquals(size,copy.withSeed(987654321L).size());
  }
 }
 @Test public void chunkHeightQueriesAndLavaLightSourcesUseTheCorrectWorldChunk(){
  RegistryAccess.RegistryHolder registries=RegistryAccess.builtin();LegacyChunkGenerator generator=(LegacyChunkGenerator)LegacyWorldgen.create(registries,vanilla(registries),Ps4WorldSize.CLASSIC,false,false).overworld();
  var height=new net.minecraft.world.level.LevelHeightAccessor(){public int getHeight(){return 256;}public int getMinBuildHeight(){return 0;}};
  var chunk=new net.minecraft.world.level.chunk.ProtoChunk(new net.minecraft.world.level.ChunkPos(4,-6),net.minecraft.world.level.chunk.UpgradeData.EMPTY,height);
  generator.fillFromNoise(Runnable::run,null,chunk).join();
  for(int x=0;x<16;x+=5)for(int z=0;z<16;z+=5){
   int expected=0;var column=generator.getBaseColumn(64+x,-96+z,height);
   for(int y=0;y<256;y++){
    var pos=new net.minecraft.core.BlockPos(64+x,y,-96+z);assertEquals(chunk.getBlockState(pos),column.getBlockState(pos));
    if(!column.getBlockState(pos).isAir())expected=y+1;
   }
   assertEquals(expected,generator.getBaseHeight(64+x,-96+z,net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,height));
  }
  generator.applyCarvers(123456789L,null,chunk,GenerationStep.Carving.AIR);
  var lights=chunk.getLights().collect(java.util.stream.Collectors.toList());assertFalse("Fixture must contain cave lava",lights.isEmpty());
  for(var pos:lights){assertEquals(4,pos.getX()>>4);assertEquals(-6,pos.getZ()>>4);}
 }
 @Test public void mixedDesertAndGrassColumnsFollowTheBiomeAtTheirActualWorldPosition(){
  var registry=RegistryAccess.builtin().registryOrThrow(Registry.BIOME_REGISTRY);var generator=new LegacyChunkGenerator(registry,123456789L,Ps4WorldSize.SMALL,false);
  int checked=0,desert=0,grass=0;
  for(int cx=-15;cx<=15&&checked<6;cx++)for(int cz=-15;cz<=15&&checked<6;cz++){
   int[] ids=generator.source().layers().blockArea(cx*16,cz*16,16,16);boolean common=true,sand=false,green=false,asymmetric=false;
   for(int x=0;x<16;x++)for(int z=0;z<16;z++){int id=ids[x+z*16];if(!java.util.Set.of(1,2,4,5,16,17,18,19,27,28,35,36,129,130).contains(id))common=false;if(id==2||id==17||id==130)sand=true;else if(id!=16)green=true;if(ids[x+z*16]!=ids[z+x*16])asymmetric=true;}
   if(!common||!sand||!green||!asymmetric)continue;
   var chunk=new net.minecraft.world.level.chunk.ProtoChunk(new net.minecraft.world.level.ChunkPos(cx,cz),net.minecraft.world.level.chunk.UpgradeData.EMPTY,dev.legacyconsole.worldgen.ps4.structure.Ps4Structures.HEIGHT);generator.fillFromNoise(Runnable::run,null,chunk).join();generator.buildSurfaceAndBedrock(null,chunk);checked++;
   for(int x=0;x<16;x++)for(int z=0;z<16;z++){
    int id=ids[x+z*16];if(id==16)continue;int y=generator.getBaseHeight(cx*16+x,cz*16+z,net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG,dev.legacyconsole.worldgen.ps4.structure.Ps4Structures.HEIGHT)-1;if(y<64)continue;
    var state=chunk.getBlockState(new net.minecraft.core.BlockPos(cx*16+x,y,cz*16+z));if(id==2||id==17||id==130){assertFalse("Desert column must not get its transposed neighbour's grass",state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK));if(state.is(net.minecraft.world.level.block.Blocks.SAND))desert++;}
    else if(state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK))grass++;
   }
  }
  assertTrue("Fixture must contain asymmetric biome boundaries",checked>0);assertTrue(desert>0);assertTrue(grass>0);
 }
 @Test public void consoleSeedZeroIsDeterministicInsteadOfBecomingARandomSeed(){
  RegistryAccess.RegistryHolder registries=RegistryAccess.builtin();WorldGenSettings base=LegacyWorldgen.explicitSeed(vanilla(registries),"0");assertEquals(0,base.seed());
  assertEquals(0,((LegacyChunkGenerator)LegacyWorldgen.create(registries,base,Ps4WorldSize.CLASSIC,true,false).overworld()).seed());
 }
 @Test public void invalidSizeDoesNotSilentlyChangeSavedWorldGeometry(){
  assertFalse(Ps4WorldSize.CODEC.parse(JsonOps.INSTANCE,new JsonPrimitive("unlimited")).result().isPresent());
 }
 @Test public void structuresSwitchSurvivesCodec(){
  RegistryAccess.RegistryHolder registries=RegistryAccess.builtin();LegacyChunkGenerator g=(LegacyChunkGenerator)LegacyWorldgen.create(registries,vanilla(registries),Ps4WorldSize.CLASSIC,false,false).overworld();
  assertFalse(g.structures());assertFalse(g.hasStronghold(new net.minecraft.world.level.ChunkPos(0,0)));
 }
}

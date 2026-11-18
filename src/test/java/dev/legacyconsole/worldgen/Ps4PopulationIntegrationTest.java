package dev.legacyconsole.worldgen;

import com.google.gson.*;
import dev.legacyconsole.worldgen.ps4.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import net.minecraft.world.level.block.*;
import org.junit.*;
import static org.junit.Assert.*;

public class Ps4PopulationIntegrationTest {
 @BeforeClass public static void bootstrap(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
 @Test public void originalRegisteredBiomesBindToImplementedSelectors()throws Exception{
  try(var input=getClass().getResourceAsStream("/worldgen/ps4-biome-settings.json.gz");var gzip=new GZIPInputStream(input);var reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){
   JsonArray biomes=new JsonParser().parse(reader).getAsJsonObject().getAsJsonArray("biomes");int count=0;
   for(var entry:biomes){var b=entry.getAsJsonObject();int id=b.get("id").getAsInt();var p=Ps4PopulationProfiles.get(id);assertEquals(id,p.id());assertEquals(b.get("maximum_snow_depth").getAsInt(),p.maximumSnowDepth());assertEquals(b.get("top").getAsInt(),p.top());assertNotNull(Ps4BiomeSettings.forBiome(id));count++;}
   assertEquals(72,count);
  }
  assertEquals(Ps4BiomeTrees.Biome.MUTATED_FOREST,Ps4PopulationProfiles.get(155).tree());
  assertEquals(Ps4BiomeTrees.Biome.MESA,Ps4PopulationProfiles.get(38).tree());
  assertEquals(3,Ps4PopulationProfiles.get(29).variant());assertEquals(2,Ps4PopulationProfiles.get(161).variant());assertEquals(1,Ps4PopulationProfiles.get(140).variant());
 }
 @Test public void iceAndMesaSelectorsMatchAdditionalOriginalCalls()throws Exception{
  try(var input=getClass().getResourceAsStream("/assets/legacyconsole/worldgen/ps4/population-profiles.json");var reader=new InputStreamReader(input,StandardCharsets.UTF_8)){
   var cases=new JsonParser().parse(reader).getAsJsonObject().getAsJsonArray("additionalTreeSelections");assertEquals(12,cases.size());
   for(var e:cases){var c=e.getAsJsonObject();Random r=new Ps4Random(c.get("seed").getAsLong());var selected=Ps4BiomeTrees.select(Ps4BiomeTrees.Biome.valueOf(c.get("biome").getAsString()),0,r);var expected=c.getAsJsonObject("selection");
    assertEquals(expected.get("kind").getAsString(),selected.kind().name());assertEquals(expected.get("base_height").getAsInt(),selected.baseHeight());assertEquals(expected.get("log").getAsInt(),selected.log());assertEquals(expected.get("leaves").getAsInt(),selected.leaves());assertEquals(c.get("random_after").getAsLong(),r.nextLong());
   }
  }
 }
 @Test public void nativeFeatureWritePalettesHavePlayableTargetStates()throws Exception{
  Set<Integer> states=new HashSet<>();int writes=0;
  for(String name:List.of("ores","flora","tree-selection","lakes","corals","aquatics","icebergs","fossil-geometry")){
   try(var input=getClass().getResourceAsStream("/worldgen/ps4-"+name+".json.gz");var gzip=new GZIPInputStream(input);var reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){
    var fixture=new JsonParser().parse(reader).getAsJsonObject();for(var section:fixture.entrySet())if(section.getValue().isJsonArray())for(var e:section.getValue().getAsJsonArray())if(e.isJsonObject()&&e.getAsJsonObject().has("writes"))for(var write:e.getAsJsonObject().getAsJsonArray("writes")){
     int raw=write.getAsJsonArray().get(3).getAsInt();states.add(raw);writes++;assertNotNull("Original write "+name+" "+raw,Ps4GenerationStates.decode(raw));
    }
   }
  }
  assertTrue(writes>100000);assertTrue(states.size()>100);
  assertSame(Blocks.WATER,Ps4GenerationStates.decode(9).getBlock());assertEquals(0,Ps4GenerationStates.encode(Blocks.CAVE_AIR.defaultBlockState()));
  assertSame(Blocks.TALL_SEAGRASS,Ps4GenerationStates.decode(270|(2<<16)).getBlock());
  assertEquals(1,(int)Ps4GenerationStates.decode(18).getValue(LeavesBlock.DISTANCE));
  var modern=Blocks.AMETHYST_BLOCK.defaultBlockState();int token=Ps4GenerationStates.encode(modern);assertFalse(Ps4GenerationStates.original(token));assertSame(modern,Ps4GenerationStates.decode(token));
 }
}

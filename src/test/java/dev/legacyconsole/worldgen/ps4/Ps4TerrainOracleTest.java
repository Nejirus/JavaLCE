package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import dev.legacyconsole.worldgen.ps4.layers.Ps4Layers;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4TerrainOracleTest {
 @Test public void originalPs4CompleteBaseBuffersMatch()throws Exception{
  JsonArray cases;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-base-chunks.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){
   cases=new JsonParser().parse(reader).getAsJsonObject().getAsJsonArray("cases");
  }
  assertEquals(16,cases.size());
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();long seed=c.get("seed").getAsLong();int width=c.get("width").getAsInt(),cx=c.getAsJsonArray("chunk").get(0).getAsInt(),cz=c.getAsJsonArray("chunk").get(1).getAsInt();
   byte[] expected=new byte[32768];for(int i=0;i<expected.length;i++)expected[i]=c.getAsJsonArray("blocks").get(i).getAsByte();
   assertArrayEquals("original PS4 base seed="+seed+" width="+width+" chunk="+cx+","+cz,expected,new Ps4Terrain(seed,width,new Ps4Layers(seed,width)).baseChunk(cx,cz));
  }
 }
 @Test public void originalPs4DensityFieldsMatchBitForBit() throws Exception {
  JsonArray cases=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/worldgen/ps4-terrain-vectors.json"),StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("cases");
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();long seed=c.get("seed").getAsLong();int width=c.get("width").getAsInt();
   Ps4Terrain terrain=new Ps4Terrain(seed,width,new Ps4Layers(seed,width));int[] ids=new int[100];for(int i=0;i<100;i++)ids[i]=c.getAsJsonArray("biomes").get(i).getAsInt();
   int cx=c.getAsJsonArray("chunk").get(0).getAsInt(),cz=c.getAsJsonArray("chunk").get(1).getAsInt();double[] got=terrain.densities(cx,cz,ids);
   for(int i=0;i<got.length;i++)assertEquals("seed="+seed+" chunk="+cx+","+cz+" density="+i,c.getAsJsonArray("values").get(i).getAsDouble(),got[i],0);
  }
 }
 @Test public void originalPs4SurfaceNoiseMatchesBitForBit() throws Exception {
  JsonArray cases=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/worldgen/ps4-surface-vectors.json"),StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("cases");
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();long seed=c.get("seed").getAsLong();Ps4Terrain terrain=new Ps4Terrain(seed,864,new Ps4Layers(seed,864));
   int cx=c.getAsJsonArray("chunk").get(0).getAsInt(),cz=c.getAsJsonArray("chunk").get(1).getAsInt();double[] got=terrain.surfaceNoise(cx,cz);
   for(int i=0;i<got.length;i++)assertEquals("seed="+seed+" surface="+i,c.getAsJsonArray("values").get(i).getAsDouble(),got[i],0);
  }
 }
 @Test public void finiteTerrainHasAnOceanEdgeAndBuildSpaceAbove128(){
  for(int width:new int[]{864,1024,3072,5120}){
   Ps4Terrain terrain=new Ps4Terrain(0,width,new Ps4Layers(0,width));
   assertEquals(128,terrain.edgeFalloff(-width/2,0),0);assertEquals(128,terrain.edgeFalloff(width/2-1,0),0);
   assertEquals(0,terrain.edgeFalloff(-width/2+32,0),0);assertFalse(terrain.outside(width/2-1,0));assertTrue(terrain.outside(width/2,0));
   byte[] blocks=terrain.baseChunk(-width/32,0);for(int z=0;z<16;z++)for(int y=0;y<63;y++)assertEquals(y<=53?1:2,blocks[z*128+y]);
  }
 }
}

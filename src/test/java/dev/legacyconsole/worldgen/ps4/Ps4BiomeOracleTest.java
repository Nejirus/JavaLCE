package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import dev.legacyconsole.worldgen.ps4.layers.Ps4Layers;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4BiomeOracleTest {
 @Test public void matchesOriginalPs4FiniteBiomesAndVoronoi() throws Exception {
  JsonArray cases=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/worldgen/ps4-biomes-vectors.json"),StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("cases");
  Map<String,Ps4Layers> worlds=new HashMap<>();
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();long seed=c.get("seed").getAsLong();int width=c.get("width").getAsInt();
   Ps4Layers world=worlds.computeIfAbsent(seed+":"+width,k->new Ps4Layers(seed,width));
   JsonArray rectangle=c.getAsJsonArray("rectangle");int x=rectangle.get(0).getAsInt(),z=rectangle.get(1).getAsInt(),w=rectangle.get(2).getAsInt(),h=rectangle.get(3).getAsInt();
   int[] got=c.get("space").getAsString().equals("quart")?world.quartArea(x,z,w,h):world.blockArea(x,z,w,h);
   int[] want=new int[w*h];for(int i=0;i<want.length;i++)want[i]=c.getAsJsonArray("values").get(i).getAsInt();
   assertArrayEquals("seed="+seed+" width="+width+" "+c.get("space")+" "+rectangle,want,got);
  }
 }
 @Test public void completeFiniteBiomeMapsMatchOriginalPs4Hashes()throws Exception{
  JsonArray cases=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/worldgen/ps4-biome-hashes-vectors.json"),StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("cases");
  for(JsonElement element:cases){JsonObject c=element.getAsJsonObject();long seed=c.get("seed").getAsLong();int width=c.get("width").getAsInt(),q=width/4;int[] ids=new Ps4Layers(seed,width).quartArea(-q/2,-q/2,q,q);byte[] data=new byte[ids.length];for(int i=0;i<ids.length;i++)data[i]=(byte)ids[i];
   String hash=com.google.common.io.BaseEncoding.base16().lowerCase().encode(java.security.MessageDigest.getInstance("SHA-256").digest(data));
   assertEquals("seed="+seed+" width="+width,c.get("sha256").getAsString(),hash);
  }
 }
 @Test public void nativeMetadataDoesNotUseJavaBiomeIdsOrBambooHillDepth(){
  assertEquals("warm_ocean",Ps4BiomeData.getBiome(42).key);
  assertEquals("bamboo_jungle",Ps4BiomeData.getBiome(40).key);
  assertEquals(.1f,Ps4BiomeData.getBiome(41).depth,0);
  assertTrue(Ps4BiomeData.isOcean(48));assertFalse(Ps4BiomeData.isOcean(10));
 }
}

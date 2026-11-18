package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.IntBinaryOperator;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4BiomeSurfacesOracleTest {
 private JsonArray cases(String name)throws Exception{
  try(InputStream source=getClass().getResourceAsStream("/worldgen/"+name);GZIPInputStream gzip=new GZIPInputStream(source);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){
   return new JsonParser().parse(reader).getAsJsonObject().getAsJsonArray("cases");
  }
 }
 private static int[] ints(JsonArray values){int[] result=new int[values.size()];for(int i=0;i<result.length;i++)result[i]=values.get(i).getAsInt();return result;}
 @Test public void originalBiomeSurfaceColumnsAndRngMatch()throws Exception{
  Map<Long,Ps4BiomeSurfaces> worlds=new HashMap<>();JsonArray cases=cases("ps4-biome-surfaces.json.gz");assertEquals(1440,cases.size());
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();long seed=c.get("seed").getAsLong();int id=c.get("biome").getAsInt(),x=c.get("x").getAsInt(),z=c.get("z").getAsInt();
   int[] column=ints(c.getAsJsonArray("before")),ice=(x&15)==(z&15)?column:ints(c.getAsJsonArray("ice_before"));
   Random random=new Random(c.get("random_seed").getAsLong());Ps4BiomeSurfaces surface=worlds.computeIfAbsent(seed,Ps4BiomeSurfaces::new);
   surface.column(column,ice,random,id,x,z,c.get("noise").getAsDouble());String label="seed="+seed+" biome="+id+" at="+x+","+z+" noise="+c.get("noise");
   assertArrayEquals(label,ints(c.getAsJsonArray("after")),column);assertArrayEquals(label+" ice column",ints(c.getAsJsonArray("ice_after")),ice);
   assertEquals(label+" biome top field",c.get("top_tile_after").getAsInt(),surface.topTile(id));
   assertEquals(label+" subsequent RNG",c.get("random_after").getAsDouble(),random.nextDouble(),0);
  }
 }
 @Test public void originalWholeSurfaceAndCarverSequenceMatches()throws Exception{
  JsonArray cases=cases("ps4-surface-chunks.json.gz");assertEquals(4,cases.size());
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();long seed=c.get("seed").getAsLong();int cx=c.getAsJsonArray("chunk").get(0).getAsInt(),cz=c.getAsJsonArray("chunk").get(1).getAsInt();
   int[] input=ints(c.getAsJsonArray("before")),biomes=ints(c.getAsJsonArray("biomes")),got=new int[32768];int[][] columns=new int[256][128];double[] noise=new double[256];
   for(int i=0;i<256;i++){System.arraycopy(input,i*128,columns[i],0,128);noise[i]=c.getAsJsonArray("noise").get(i).getAsDouble();}
   Random random=new Random((long)cx*341873128712L+(long)cz*132897987541L);Ps4BiomeSurfaces surface=new Ps4BiomeSurfaces(seed);surface.chunk(columns,random,cx,cz,biomes,noise);
   for(int i=0;i<256;i++)System.arraycopy(columns[i],0,got,i*128,128);
   String label="original PS4 surface seed="+seed+" chunk="+cx+","+cz;assertArrayEquals(label,ints(c.getAsJsonArray("after")),got);
   assertEquals(label+" subsequent RNG",c.get("random_after").getAsDouble(),random.nextDouble(),0);
   int[] topTiles=Ps4BiomeSurfaces.chunkTopTiles(biomes,noise);
   for(var top:c.getAsJsonObject("top_tiles").entrySet()){
    assertEquals(label+" top tile "+top.getKey(),top.getValue().getAsInt(),surface.topTile(Integer.parseInt(top.getKey())));
    assertEquals(label+" local top tile "+top.getKey(),top.getValue().getAsInt(),topTiles[Integer.parseInt(top.getKey())]);
   }
   // Simulate a different chunk changing the shared biome field while the
   // current chunk waits for Java's separate carver task.
   surface.column(new int[128],new Random(0),3,0,0,topTiles[3]==1?0:100);
   assertNotEquals(label+" interleaved biome field",topTiles[3],surface.topTile(3));
   IntBinaryOperator biomeAt=(x,z)->(x>>4)==cx&&(z>>4)==cz?biomes[(x&15)+(z&15)*16]:1;
   Ps4Carvers.carve(seed,cx,cz,got,biomeAt,(x,z)->topTiles[biomeAt.applyAsInt(x,z)]);
   assertArrayEquals(label+" followed by all four carvers",ints(c.getAsJsonArray("carved")),got);
  }
 }
 @Test public void originalCarverMetadataWritesMatch()throws Exception{
  JsonArray cases=cases("ps4-carver-states.json.gz");assertEquals(5,cases.size());int total=0;
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();long seed=c.get("seed").getAsLong();
   int cx=c.getAsJsonArray("chunk").get(0).getAsInt(),cz=c.getAsJsonArray("chunk").get(1).getAsInt();
   int[] before=ints(c.getAsJsonArray("before")),got=before.clone();
   Ps4Carvers.carve(seed,cx,cz,got,(x,z)->c.get("biome").getAsInt(),(x,z)->c.get("top_tile").getAsInt());
   String label="original canonical carver seed="+seed+" chunk="+cx+","+cz;
   assertArrayEquals(label,ints(c.getAsJsonArray("after")),got);
   int changed=0;for(int i=0;i<got.length;i++)if(got[i]!=before[i]&&(got[i]&65535)==(before[i]&65535))changed++;
   assertEquals(label+" metadata-only writes",c.get("metadata_only_changes").getAsInt(),changed);total+=changed;
  }
  assertTrue("original fixture exercises same-ID default-state writes",total>100);
 }
}

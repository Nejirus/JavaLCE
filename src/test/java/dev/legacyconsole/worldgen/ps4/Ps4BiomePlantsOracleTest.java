package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4BiomePlantsOracleTest {
 @Test public void originalFlowerGrassChoicesNoiseAndPropertyConversionMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-flora-selection.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  JsonArray flowers=fixture.getAsJsonArray("flowers"),grass=fixture.getAsJsonArray("grass");assertEquals(5916,flowers.size());assertEquals(2064,grass.size());
  Ps4SurfaceNoise noise=new Ps4SurfaceNoise(new Random(2345),1);Set<Integer> chosen=new HashSet<>();
  for(JsonElement element:flowers){
   JsonObject c=element.getAsJsonObject();var kind=Ps4BiomePlants.Flowers.valueOf(c.get("kind").getAsString());Random random=new Random(c.get("seed").getAsLong());JsonArray pos=c.getAsJsonArray("position");int variant=c.get("variant").getAsInt();
   int state=c.get("noise_override").isJsonNull()?Ps4BiomePlants.flower(kind,variant,random,pos.get(0).getAsInt(),pos.get(2).getAsInt()):Ps4BiomePlants.flowerWithNoise(kind,variant,random,c.get("noise_override").getAsDouble());
   String label="original flower "+kind+" variant="+variant+" seed="+c.get("seed")+" pos="+pos+" noise="+c.get("noise_override");
   assertEquals(label,c.get("state").getAsInt(),state);assertEquals(label+" configured canonical state",c.get("configured_state").getAsInt(),state);assertEquals(label+" native metadata",c.get("native_data").getAsInt(),state>>>16);
   assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());chosen.add(state);
   if(c.get("noise_override").isJsonNull())for(JsonElement query:c.getAsJsonArray("noise_queries")){JsonArray q=query.getAsJsonArray();assertEquals(label+" original biome noise bits",Double.doubleToRawLongBits(q.get(2).getAsDouble()),Double.doubleToRawLongBits(noise.getValue(q.get(0).getAsDouble(),q.get(1).getAsDouble())));}
  }
  assertEquals("all twelve flower types selected",12,chosen.size());
  for(JsonElement element:grass){JsonObject c=element.getAsJsonObject();Random random=new Random(c.get("seed").getAsLong());int state=Ps4BiomePlants.grass(Ps4BiomePlants.Grass.valueOf(c.get("kind").getAsString()),random);String label="original grass "+c.get("kind")+" seed="+c.get("seed");assertEquals(label,c.get("state").getAsInt(),state);assertEquals(label+" native metadata",c.get("native_data").getAsInt(),state>>>16);assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());}
 }
}

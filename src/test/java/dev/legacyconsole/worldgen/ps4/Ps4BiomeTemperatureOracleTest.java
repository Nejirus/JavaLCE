package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4BiomeTemperatureOracleTest {
 @Test public void originalRegisteredTemperatureVirtualsNoiseAndAltitudeMatchByBits()throws Exception{
  JsonObject fixture;try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-biome-temperature.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  Set<Integer> biomes=new HashSet<>();Set<String> entries=new HashSet<>();int cases=0;
  for(JsonElement e:fixture.getAsJsonArray("biomes")){JsonObject b=e.getAsJsonObject();biomes.add(b.get("id").getAsInt());entries.add(b.get("virtual").getAsString());assertEquals("original constructor temperature",Integer.parseUnsignedInt(b.get("temperature_bits").getAsString(),16),Float.floatToRawIntBits(Ps4BiomeData.getBiome(b.get("id").getAsInt()).temperature));}
  for(JsonElement e:fixture.getAsJsonArray("cases")){
   JsonObject c=e.getAsJsonObject();int id=c.get("biome").getAsInt();JsonArray p=c.getAsJsonArray("position");float actual=Ps4BiomeTemperature.at(id,p.get(0).getAsInt(),p.get(1).getAsInt(),p.get(2).getAsInt());assertEquals("original biome temperature "+id+" at "+p,Integer.parseUnsignedInt(c.get("bits").getAsString(),16),Float.floatToRawIntBits(actual));cases++;
  }
  assertEquals(72,biomes.size());assertEquals(Set.of("0x85390","0x4d7380"),entries);assertEquals(10018,cases);
 }
}

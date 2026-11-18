package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4CoralPaletteOracleTest {
 @Test public void originalClockSeededSharedMathStreamAndDeadCoralPaletteMatch()throws Exception{
  JsonObject fixture;try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-coral-palette.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  Set<Integer> states=new HashSet<>();int conversions=0;
  for(JsonElement e:fixture.getAsJsonArray("cases")){
   JsonObject c=e.getAsJsonObject();Ps4Random random=Ps4Random.fromClockTick(c.get("clock_tick").getAsLong());
   for(JsonElement prefix:c.getAsJsonArray("prefix")){if(prefix.getAsJsonPrimitive().isString())random.nextDouble();else random.nextInt(prefix.getAsInt());}
   for(JsonElement expected:c.getAsJsonArray("states")){int actual=Ps4CoralPalette.deadCoral(random);assertEquals("original shared palette stream "+c.get("clock_tick")+" prefix="+c.get("prefix"),expected.getAsInt(),actual);states.add(actual);conversions++;}
   assertEquals("original following shared RNG",c.get("random_after").getAsLong(),random.nextLong());
  }
  assertEquals(784,fixture.getAsJsonArray("cases").size());assertEquals(Set.of(259|(8<<16),259|(9<<16),259|(10<<16),259|(11<<16),259|(12<<16)),states);assertEquals(8624,conversions);
 }
}

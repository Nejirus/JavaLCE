package dev.legacyconsole.worldgen.ps4.layers;

import com.google.gson.*;
import dev.legacyconsole.worldgen.ps4.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4BiomeSettingsOracleTest {
 @Test public void originalRegisteredBiomesClimateClassesAndDecorationCountsMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-biome-settings.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  JsonArray biomes=fixture.getAsJsonArray("biomes");assertEquals(72,biomes.size());assertEquals(biomes.size(),Ps4BiomeData.values().size());Set<Integer> ids=new HashSet<>();
  for(JsonElement e:biomes){
   JsonObject c=e.getAsJsonObject();int id=c.get("id").getAsInt();ids.add(id);String label="original registered biome "+id+" "+c.get("class");Ps4BiomeData b=Ps4BiomeData.getBiome(id);assertNotNull(label,b);
   JsonObject bits=c.getAsJsonObject("float_bits");assertEquals(label+" depth",Integer.parseUnsignedInt(bits.get("depth").getAsString(),16),Float.floatToRawIntBits(b.depth));assertEquals(label+" scale",Integer.parseUnsignedInt(bits.get("scale").getAsString(),16),Float.floatToRawIntBits(b.scale));assertEquals(label+" temperature",Integer.parseUnsignedInt(bits.get("temperature").getAsString(),16),Float.floatToRawIntBits(b.temperature));
   assertEquals(label+" snow flag",c.get("snow").getAsBoolean(),b.isSnowyBiome());assertEquals(label+" class family",c.get("family").getAsInt(),b.getBiomeClass());assertEquals(label+" temperature virtual",c.get("temperature_group").getAsInt(),b.getTempCategory().ordinal());assertEquals(label+" ocean category",c.get("category").getAsInt()==10,Ps4BiomeData.isOceanCategory(id));
   assertEquals(label+" maximum snow depth",c.get("maximum_snow_depth").getAsInt(),b.maximumSnowDepth());
   var s=Ps4BiomeSettings.forBiome(id);JsonObject expected=c.getAsJsonObject("settings"),actual=new JsonObject();String[] names={"waterlilies","trees","flowers","grass","deadBushes","mushrooms","reeds","cacti","gravel","sand","clay","hugeMushrooms","kelp","seaPickles","seagrass"};int[] values={s.waterlilies(),s.trees(),s.flowers(),s.grass(),s.deadBushes(),s.mushrooms(),s.reeds(),s.cacti(),s.gravel(),s.sand(),s.clay(),s.hugeMushrooms(),s.kelp(),s.seaPickles(),s.seagrass()};
   for(int i=0;i<names.length;i++)actual.addProperty(names[i],values[i]);actual.addProperty("extraTreeChance",s.extraTreeChance());actual.addProperty("springs",s.springs());assertEquals(label+" decoration constructor",expected,actual);
  }
  JsonArray equal=fixture.getAsJsonArray("equality");assertEquals(5184,equal.size());
  for(JsonElement e:equal){JsonObject c=e.getAsJsonObject();int a=c.get("first").getAsInt(),b=c.get("second").getAsInt();assertEquals("original biome equality "+a+","+b,c.get("result").getAsBoolean(),GenLayer.biomesEqualOrMesaPlateau(a,b));}
  assertEquals(Ps4BiomeData.TempCategory.OCEAN,Ps4BiomeData.FROZEN_OCEAN.getTempCategory());
  for(int id:new int[]{-1,50,128,168,256}){assertFalse(ids.contains(id));try{Ps4BiomeSettings.forBiome(id);fail("unregistered biome "+id);}catch(IllegalArgumentException expected){}}
 }
}

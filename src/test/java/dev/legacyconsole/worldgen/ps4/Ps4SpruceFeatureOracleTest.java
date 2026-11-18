package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4SpruceFeatureOracleTest {
 @Test public void originalSpruceTrunksAndCanopiesMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-spruce.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(1408,cases.size());int placed=0,rejected=0,fallen=0;
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();var world=new Ps4TreeFeatureOracleTest.World(c);Random random=new Random(c.get("seed").getAsLong());int[] p=world.origin;
   boolean result=Ps4SpruceFeature.place(world,random,p[0],p[1],p[2],c.get("base_height").getAsInt(),c.get("log").getAsInt(),c.get("leaves").getAsInt(),c.get("updates").getAsBoolean());
   String label="original spruce seed="+c.get("seed")+" profile="+world.profile+" position="+c.get("position");
   assertEquals(label+" result",c.get("result").getAsBoolean(),result);Ps4FloraOracleTest.compare(label,c,world);
   assertEquals(label+" support queries",c.getAsJsonArray("support_queries"),world.supportQueries);assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());
   if(result)placed++;else rejected++;
   if(result&&world.writes.size()>0){boolean leaves=false;for(JsonElement row:world.writes)if((row.getAsJsonArray().get(3).getAsInt()&65535)==18)leaves=true;if(!leaves)fallen++;}
  }
  assertTrue("ordinary, rejected and fallen spruce covered",placed>100&&rejected>100&&fallen>0);
 }
}

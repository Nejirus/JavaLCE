package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4MegaTreeFeatureOracleTest {
 @Test public void originalGiantTreesAndGroundPostprocessMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-mega-trees.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){
   fixture=new JsonParser().parse(reader).getAsJsonObject();
  }
  JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(5760,cases.size());int writes=0,placed=0,rejected=0,podzol=0;
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();var world=new Ps4TreeFeatureOracleTest.World(c);Random random=new Random(c.get("seed").getAsLong());int[] p=world.origin;
   boolean updates=c.get("updates").getAsBoolean();Boolean result;
   if(c.get("kind").getAsString().equals("mega_pine_post")){Ps4MegaPineFeature.postPlace(world,random,p[0],p[1],p[2],updates);result=null;}
   else if(c.get("kind").getAsString().equals("mega_jungle"))result=Ps4MegaJungleFeature.place(world,random,p[0],p[1],p[2],c.get("base_height").getAsInt(),c.get("extra_range").getAsInt(),c.get("log").getAsInt(),c.get("leaves").getAsInt(),updates);
   else result=Ps4MegaPineFeature.place(world,random,p[0],p[1],p[2],c.get("base_height").getAsInt(),c.get("extra_range").getAsInt(),c.get("log").getAsInt(),c.get("leaves").getAsInt(),c.get("tall").getAsBoolean(),updates);
   String label="original "+c.get("kind")+" seed="+c.get("seed")+" profile="+world.profile+" pos="+c.get("position")+" tall="+c.get("tall")+" updates="+updates;
   assertEquals(label+" result",c.get("result").isJsonNull()?null:c.get("result").getAsBoolean(),result);Ps4FloraOracleTest.compare(label,c,world);
   assertEquals(label+" support queries",c.getAsJsonArray("support_queries"),world.supportQueries);
   assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());
   writes+=world.writes.size();if(Boolean.TRUE.equals(result))placed++;if(Boolean.FALSE.equals(result))rejected++;
   for(JsonElement w:world.writes)if(w.getAsJsonArray().get(3).getAsInt()==(3|(2<<16)))podzol++;
  }
  assertTrue("placement, rejection and podzol coverage",placed>100&&rejected>100&&podzol>10000);assertTrue("substantial giant tree geometry coverage",writes>100000);
 }
}

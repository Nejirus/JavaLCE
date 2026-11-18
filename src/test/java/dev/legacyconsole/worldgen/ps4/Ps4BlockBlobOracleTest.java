package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4BlockBlobOracleTest {
 @Test public void originalBouldersAndPositionIteratorMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-blobs.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(3840,cases.size());int placed=0,rejected=0,writes=0;
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();World world=new World(c);Random random=new Random(c.get("seed").getAsLong());int[] p=world.origin;
   boolean result=Ps4BlockBlobFeature.place(world,random,p[0],p[1],p[2],c.get("radius").getAsInt(),c.get("state").getAsInt());String label="original boulder seed="+c.get("seed")+" profile="+world.profile+" pos="+c.get("position")+" radius="+c.get("radius");
   assertEquals(label+" result",c.get("result").getAsBoolean(),result);Ps4FloraOracleTest.compare(label,c,world);assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());if(result)placed++;else rejected++;writes+=world.writes.size();
  }
  assertTrue("placement, rejection and geometry coverage",placed>100&&rejected>100&&writes>100000);
 }
 private static class World extends Ps4TreeFeatureOracleTest.World {
  World(JsonObject c)throws Exception{super(c);}
  @Override int initial(int x,int y,int z){if(profile.equals("empty"))return 0;if(profile.equals("sand")&&y==origin[1]-1)return 12;return super.initial(x,y,z);}
 }
}

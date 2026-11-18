package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4PumpkinOracleTest {
 @Test public void originalNaturalPumpkinPropertiesPlacementAndRngMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-pumpkins.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  assertEquals(257,fixture.get("target_id").getAsInt());assertEquals(1,fixture.getAsJsonArray("properties").size());assertEquals("horizontal_facing",fixture.getAsJsonArray("properties").get(0).getAsJsonObject().get("property").getAsString());
  for(JsonElement e:fixture.getAsJsonArray("defaults")){JsonObject c=e.getAsJsonObject();assertEquals(257,c.get("state").getAsInt());assertEquals(0,c.get("native_data").getAsInt());}
  assertEquals(384,fixture.getAsJsonArray("rules").size());assertEquals(528,fixture.getAsJsonArray("cases").size());int writes=0,accepted=0,rejected=0;Set<Integer> facings=new HashSet<>();
  for(JsonElement e:fixture.getAsJsonArray("rules")){
   JsonObject c=e.getAsJsonObject();World world=new World(c);boolean result=Ps4PumpkinFeature.mayPlace(world,0,64,0);String label="original pumpkin mayPlace current="+c.get("current")+" below="+c.get("below")+" support="+c.get("support")+" air="+c.get("nonreplaceable_air");
   assertEquals(label,c.get("result").getAsBoolean(),result);Ps4FloraOracleTest.compare(label,c,world);assertEquals(label+" support queries",c.getAsJsonArray("support_queries"),world.pumpkinSupport);if(result)accepted++;else rejected++;
  }
  for(JsonElement e:fixture.getAsJsonArray("cases")){
   JsonObject c=e.getAsJsonObject();World world=new World(c);Random random=new Random(c.get("seed").getAsLong());int[] p=world.origin;
   boolean result=Ps4PumpkinFeature.place(world,random,p[0],p[1],p[2]);String label="original natural pumpkins seed="+c.get("seed")+" profile="+world.profile+" pos="+c.get("position");
   assertEquals(label+" result",c.get("result").getAsBoolean(),result);Ps4FloraOracleTest.compare(label,c,world);assertEquals(label+" support queries",c.getAsJsonArray("support_queries"),world.pumpkinSupport);assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());
   JsonArray properties=c.getAsJsonArray("property_queries");assertEquals(label+" original property membership checks",world.writes.size()*2,properties.size());
   for(int i=0;i<world.writes.size();i++){
    int state=world.writes.get(i).getAsJsonArray().get(3).getAsInt();JsonArray facing=properties.get(i*2).getAsJsonArray(),age=properties.get(i*2+1).getAsJsonArray();
    assertEquals(257,facing.get(0).getAsInt());assertEquals("horizontal_facing",facing.get(1).getAsString());assertTrue(facing.get(2).getAsBoolean());assertEquals(state,age.get(0).getAsInt());assertEquals("berry_age",age.get(1).getAsString());assertFalse(age.get(2).getAsBoolean());facings.add(state>>>16);
   }
   writes+=world.writes.size();
  }
  assertTrue("accepted and rejected support/material cases",accepted>10&&rejected>100);assertTrue("successful scatter coverage",writes>500);assertEquals(Set.of(0,1,2,3),facings);
 }
 private static class World extends Ps4TreeFeatureOracleTest.World implements Ps4PumpkinFeature.Access {
  final JsonObject input;final JsonArray pumpkinSupport=new JsonArray();
  World(JsonObject c)throws Exception{super(c);input=c;}
  @Override int initial(int x,int y,int z){if(profile.equals("pumpkin_rules"))return x==0&&z==0?y==64?input.get("current").getAsInt():y==63?input.get("below").getAsInt():0:0;if(profile.equals("replaceable")&&y==origin[1]&&Math.floorMod(x+z,3)==0)return 31|(2<<16);return super.initial(x,y,z);}
  @Override public boolean replaceable(int state){boolean nonreplace=profile.equals("pumpkin_rules")?input.get("nonreplaceable_air").getAsBoolean():profile.equals("non_replaceable_air");return !(nonreplace&&(state&65535)==0)&&super.replaceable(state);}
  public boolean supportsFace(int state,int direction,int supportType){boolean result=profile.equals("pumpkin_rules")?input.get("support").getAsBoolean():!profile.equals("unsupported")&&solidRender(state);JsonArray row=new JsonArray();row.add(state);row.add(direction);row.add(supportType);row.add(result);pumpkinSupport.add(row);return result;}
 }
}

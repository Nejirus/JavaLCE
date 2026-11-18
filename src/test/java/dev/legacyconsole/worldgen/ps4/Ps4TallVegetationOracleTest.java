package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4TallVegetationOracleTest {
 @Test public void originalBambooPodzolAndBerryRulesGeometryAndRngMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-tall-vegetation.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  assertEquals(4,fixture.get("berry_age_count").getAsInt());assertEquals(2,fixture.getAsJsonObject("property_counts").get("319").getAsInt());assertEquals(1,fixture.getAsJsonObject("property_counts").get("363").getAsInt());
  assertEquals(32,fixture.getAsJsonArray("default_state_cases").size());
  for(JsonElement e:fixture.getAsJsonArray("default_state_cases")){JsonObject c=e.getAsJsonObject();assertEquals(c.get("base").getAsInt()&65535,c.get("state").getAsInt());assertEquals(0,c.get("native_data").getAsInt());}
  for(JsonElement e:fixture.getAsJsonArray("data_cases")){JsonObject c=e.getAsJsonObject();assertEquals(c.get("state").getAsInt()>>>16,c.get("native_data").getAsInt());}
  assertEquals(588,fixture.getAsJsonArray("rules").size());int accepted=0,rejected=0;
  for(JsonElement e:fixture.getAsJsonArray("rules")){
   JsonObject c=e.getAsJsonObject();World world=new World(c);String kind=c.get("kind").getAsString();boolean result=kind.equals("bamboo")?Ps4BambooFeature.mayPlace(world,0,64,0):Ps4BerryBushFeature.mayPlace(world,0,64,0);
   String label="original "+kind+" placement current="+c.get("current")+" below="+c.get("below");assertEquals(label,c.get("result").getAsBoolean(),result);Ps4FloraOracleTest.compare(label,c,world);if(result)accepted++;else rejected++;
  }
  assertEquals(2408,fixture.getAsJsonArray("cases").size());int writes=0,blockedTips=0;Set<Integer> ages=new HashSet<>();Set<String> successes=new HashSet<>(),failures=new HashSet<>();
  for(JsonElement e:fixture.getAsJsonArray("cases")){
   JsonObject c=e.getAsJsonObject();World world=new World(c);Random random=new Random(c.get("seed").getAsLong());int[] p=world.origin;String kind=c.get("kind").getAsString();
   boolean result=switch(kind){case "bamboo"->Ps4BambooFeature.place(world,random,p[0],p[1],p[2]);case "podzol"->Ps4PodzolFeature.place(world,random,p[0],p[1],p[2]);case "berries"->Ps4BerryBushFeature.place(world,random,p[0],p[1],p[2]);default->throw new AssertionError(kind);};
   String label="original "+kind+" seed="+c.get("seed")+" profile="+world.profile+" pos="+c.get("position")+" roof="+c.get("roof");
   assertEquals(label+" result",c.get("result").getAsBoolean(),result);Ps4FloraOracleTest.compare(label,c,world);assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());
   if(world.writes.size()>0)successes.add(kind);if(!result)failures.add(kind);writes+=world.writes.size();
   if(kind.equals("berries")){
    JsonArray queries=c.getAsJsonArray("property_queries");assertEquals(label,world.writes.size()*2,queries.size());
    for(int i=0;i<world.writes.size();i++){
     JsonArray facing=queries.get(i*2).getAsJsonArray(),age=queries.get(i*2+1).getAsJsonArray();assertEquals("horizontal_facing",facing.get(1).getAsString());assertFalse(facing.get(2).getAsBoolean());assertEquals("berry_age",age.get(1).getAsString());assertTrue(age.get(2).getAsBoolean());
     ages.add(world.writes.get(i).getAsJsonArray().get(3).getAsInt()>>>16);
    }
   }
   if(kind.equals("bamboo")&&world.profile.equals("roof"))for(JsonElement row:world.writes){JsonArray w=row.getAsJsonArray();if(w.get(1).getAsInt()==p[1]+world.roof&&w.get(3).getAsInt()==(319|(10<<16)))blockedTips++;}
  }
  assertTrue("both placement-rule outcomes",accepted>50&&rejected>100);assertEquals(Set.of("bamboo","podzol","berries"),successes);assertEquals(Set.of("bamboo","podzol"),failures);assertEquals(Set.of(0,1,2,3),ages);assertTrue("tip overwrites original obstacles",blockedTips>20);assertTrue("substantial geometry coverage",writes>5000);
 }
 private static class World extends Ps4TreeFeatureOracleTest.World {
  final JsonObject input;final int roof;
  World(JsonObject c)throws Exception{super(c);input=c;roof=c.has("roof")?c.get("roof").getAsInt():256;}
  @Override int initial(int x,int y,int z){
   if(profile.equals("vegetation_rules"))return x==0&&z==0?y==64?input.get("current").getAsInt():y==63?input.get("below").getAsInt():0:0;
   if(profile.equals("roof")&&x==origin[0]&&z==origin[2]&&y==origin[1]+roof)return 1;
   if(profile.equals("mixed_soil")&&y==origin[1]-1)return new int[]{2,3,3|(2<<16),12,13,110,1,60}[Math.floorMod(x+z,8)];
   if(profile.equals("empty"))return 0;
   if(profile.equals("replaceable")&&y==origin[1]&&Math.floorMod(x+z,3)==0)return 31|(2<<16);
   return super.initial(x,y,z);
  }
  @Override public boolean replaceable(int state){int tile=state&65535;return tile==319||tile==320||tile==321||tile==363||super.replaceable(state);}
 }
}

package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4AquaticOracleTest {
 @Test public void originalAquaticStatesSupportRulesGeometryAndRngMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-aquatics.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  assertEquals(16,fixture.getAsJsonObject("property_counts").get("kelp_age").getAsInt());assertEquals(3,fixture.getAsJsonObject("property_counts").get("sea_grass").getAsInt());assertEquals(4,fixture.getAsJsonObject("property_counts").get("pickle_count").getAsInt());assertEquals(2,fixture.getAsJsonObject("property_counts").get("pickle_flange").getAsInt());
  assertEquals(48,fixture.getAsJsonArray("default_state_cases").size());
  for(JsonElement e:fixture.getAsJsonArray("default_state_cases")){JsonObject c=e.getAsJsonObject();int base=c.get("base").getAsInt(),state=c.get("state").getAsInt();assertEquals((base&65535)==271?base:base&65535,state);assertEquals(state>>>16,c.get("native_data").getAsInt());}
  for(JsonElement e:fixture.getAsJsonArray("data_cases")){JsonObject c=e.getAsJsonObject();assertEquals(c.get("state").getAsInt()>>>16,c.get("native_data").getAsInt());}
  JsonArray cached=fixture.getAsJsonArray("kelp_cached_states");assertEquals(16,cached.size());for(int i=0;i<16;i++)assertEquals(258|(i<<16),cached.get(i).getAsInt());
  JsonArray rules=fixture.getAsJsonArray("rules"),cases=fixture.getAsJsonArray("cases");assertEquals(5880,rules.size());assertEquals(2800,cases.size());
  int accepted=0,rejected=0,writes=0;Set<Integer> ages=new HashSet<>(),grassTypes=new HashSet<>(),pickleCounts=new HashSet<>();Set<String> successes=new HashSet<>();boolean retainedBusy=false,blockedReentry=false;
  for(JsonElement e:rules){
   JsonObject c=e.getAsJsonObject();World w=new World(c);String kind=c.get("kind").getAsString();boolean result=switch(kind){case "kelp"->Ps4AquaticFeatures.mayPlaceKelp(w,0,64,0);case "seagrass"->Ps4AquaticFeatures.mayPlaceSeagrass(w,c.get("state").getAsInt(),0,64,0,c.get("skip").getAsBoolean());case "pickles"->Ps4AquaticFeatures.canStayPickle(w,0,64,0);default->throw new AssertionError(kind);};
   String label="original aquatic rule "+c;assertEquals(label,c.get("result").getAsBoolean(),result);compare(label,c,w);if(result)accepted++;else rejected++;
  }
  for(JsonElement e:cases){
   JsonObject c=e.getAsJsonObject();World w=new World(c);int[] p=w.origin;String kind=c.get("kind").getAsString();Random random=new Random(c.get("seed").getAsLong());Ps4AquaticFeatures.Seagrass grass=new Ps4AquaticFeatures.Seagrass(c.get("busy").getAsBoolean());
   boolean result=switch(kind){case "kelp"->Ps4AquaticFeatures.kelp(w,random,p[0],p[1],p[2]);case "seagrass"->grass.place(w,random,p[0],p[1],p[2]);case "pickles"->Ps4AquaticFeatures.pickles(w,random,p[0],p[1],p[2]);default->throw new AssertionError(kind);};
   String label="original aquatic "+kind+" seed="+c.get("seed")+" pos="+c.get("position")+" profile="+w.profile+" placement="+c.get("placement")+" busy="+c.get("busy")+" reentry="+c.get("reentry");
   assertEquals(label+" result",c.get("result").getAsBoolean(),result);compare(label,c,w);assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());
   if(kind.equals("seagrass")){assertEquals(label+" busy byte",c.get("busy_after").getAsBoolean(),grass.placing());if(!result&&grass.placing())retainedBusy=true;if(c.get("busy").getAsBoolean()&&c.get("reentry").getAsBoolean()&&w.readCount==0)blockedReentry=true;}
   if(w.writes.size()>0)successes.add(kind);writes+=w.writes.size();
   for(JsonElement row:w.writes){JsonArray b=row.getAsJsonArray();int state=b.get(3).getAsInt();switch(state&65535){case 258->ages.add(state>>>16);case 270->grassTypes.add(state>>>16);case 271->pickleCounts.add(state>>>16);default->fail("unexpected aquatic state "+state);}}
  }
  assertTrue("both support-rule outcomes",accepted>100&&rejected>100);assertEquals(Set.of("kelp","seagrass","pickles"),successes);assertTrue("substantial geometry coverage",writes>10000);assertTrue("kelp growth-age coverage",ages.size()>10);assertEquals(Set.of(0,1,2),grassTypes);assertEquals(Set.of(0,1,2,3),pickleCounts);assertTrue("failed placements retain the busy byte",retainedBusy);assertTrue("busy reentry can reject before probing the world",blockedReentry);
 }
 private static void compare(String label,JsonObject expected,World actual)throws Exception{
  try{Ps4FloraOracleTest.compare(label,expected,actual);assertEquals(label+" effective fluid queries",expected.getAsJsonArray("effective_queries"),actual.effectiveQueries);assertEquals(label+" collision placement queries",expected.getAsJsonArray("placement_queries"),actual.placementQueries);}
  catch(AssertionError error){JsonObject d=new JsonObject();d.add("case",expected);d.add("actual_writes",actual.writes);d.add("actual_probes",actual.probes);d.add("actual_effective_queries",actual.effectiveQueries);d.add("actual_placement_queries",actual.placementQueries);Files.writeString(Path.of("build/ps4-aquatic-mismatch.json"),d.toString());throw error;}
 }
 private static JsonArray row(int... values){JsonArray r=new JsonArray();for(int value:values)r.add(value);return r;}
 private static class World extends Ps4FloraOracleTest.World implements Ps4AquaticFeatures.Access {
  final JsonObject input;final JsonArray effectiveQueries=new JsonArray(),placementQueries=new JsonArray(),probes=new JsonArray();
  World(JsonObject c)throws Exception{super(c);input=c;}
  @Override int initial(int x,int y,int z){
   if(profile.equals("aquatic_rule"))return x==0&&z==0?y==64?input.get("current").getAsInt():y==63?input.get("below").getAsInt():y==65?input.get("above").getAsInt():0:0;
   if(y<0||y>=256||profile.equals("air"))return 0;
   if(profile.equals("column_kelp")&&x==origin[0]&&y==origin[1]-1&&z==origin[2])return 258|(14<<16);
   if(profile.equals("column_pickle")&&x==origin[0]&&y==origin[1]-1&&z==origin[2])return 271;
   int floor=origin[1]-4+(profile.equals("steps")?Math.floorMod(x*7+z*3,5):0);
   if(y<floor)return 1;
   if(y==floor)return switch(profile){case "ice_floor"->79;case "magma"->213;case "soul"->88;case "glass"->20;case "top_slabs"->44|(8<<16);case "bottom_slabs"->44;case "mixed"->new int[]{1,3,12,13,20,44|(8<<16),213,88}[Math.floorMod(x+z,8)];default->1;};
   if(y>floor+(profile.equals("shallow")?2:24))return 0;
   if(profile.equals("flowing"))return 8|(Math.floorMod(x+z,10)<<16);
   if(profile.equals("waterlogged")&&Math.floorMod(x+z,3)==0)return 20;
   return 9;
  }
  @Override public int block(int x,int y,int z){int value=super.block(x,y,z);probes.add(row(x,y,z,value));return value;}
  public boolean clientSide(){return input.has("client")&&input.get("client").getAsBoolean();}
  public boolean solidBlocking(int state){if(profile.equals("aquatic_rule"))return input.get("blocking").getAsBoolean();int id=state&65535;return !NONSOLID.contains(id)&&!Set.of(18,161,258,270,271,44,126,182,205,312).contains(id);}
  public boolean slab(int state){return Set.of(44,126,182,205,312).contains(state&65535);}
  public boolean seagrassBelowFlag(int state){return profile.equals("aquatic_rule")&&input.get("flag").getAsBoolean();}
  public int effectiveBlock(int x,int y,int z){int value=profile.equals("aquatic_rule")?input.get("effective").getAsInt():state(x,y,z);if(profile.equals("waterlogged")&&value==20)value=9;effectiveQueries.add(row(x,y,z,value));return value;}
  public boolean canPlace(int state,int x,int y,int z){String mode=input.get("placement").getAsString();boolean result=mode.equals("all")||mode.equals("checker")&&Math.floorMod(x+y+z,3)!=0;JsonArray q=row(x,y,z,state);q.add(result);placementQueries.add(q);return result;}
  public boolean seagrassReentryBlocked(){return input.get("reentry").getAsBoolean();}
 }
}

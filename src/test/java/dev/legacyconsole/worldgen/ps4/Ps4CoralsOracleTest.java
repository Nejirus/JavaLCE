package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4CoralsOracleTest {
 @Test public void originalCoralPropertiesRulesKernelsAndSharedDirectionHistoryMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-corals.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  Map<String,Integer> counts=Map.of("plant",5,"block",6,"block_dead",2,"fan",5,"fan_direction",2,"hang",2,"hang_direction",4,"hang_dead",2);
  for(JsonElement e:fixture.getAsJsonArray("properties")){JsonObject p=e.getAsJsonObject();assertEquals(counts.get(p.get("key").getAsString()).intValue(),p.get("count").getAsInt());}
  assertEquals(112,fixture.getAsJsonArray("default_state_cases").size());
  for(JsonElement e:fixture.getAsJsonArray("default_state_cases")){
   JsonObject c=e.getAsJsonObject();int base=c.get("base").getAsInt(),id=base&65535,state=c.get("state").getAsInt();
   int expected=id==259?id:id==263?base:id<266?id|(11<<16):id|(8<<16);
   assertEquals(expected,state);assertEquals(id==263?(state>>>16&7):state>>>16,c.get("native_data").getAsInt());
  }
  for(JsonElement e:fixture.getAsJsonArray("data_cases")){JsonObject c=e.getAsJsonObject();assertEquals(c.get("state").getAsInt()>>>16,c.get("native_data").getAsInt());}
  Map<Integer,Set<String>> definitions=Map.of(259,Set.of("block","block_dead"),263,Set.of("plant"),264,Set.of("fan","fan_direction"),265,Set.of("fan","fan_direction"),266,Set.of("hang","hang_direction","hang_dead"),267,Set.of("hang","hang_direction","hang_dead"),268,Set.of("hang","hang_direction","hang_dead"));
  for(JsonElement e:fixture.getAsJsonArray("definition_inputs")){JsonObject c=e.getAsJsonObject();Set<String> actual=new HashSet<>();for(JsonElement p:c.getAsJsonArray("properties"))actual.add(p.getAsString());assertEquals(definitions.get(c.get("tile").getAsInt()),actual);}
  int writes=0,accepted=0,rejected=0,changedPlanes=0;Set<Integer> tiles=new HashSet<>();Set<String> successes=new HashSet<>();
  for(JsonElement e:fixture.getAsJsonArray("rules")){
   JsonObject c=e.getAsJsonObject();World w=new World(c);boolean result=c.get("kind").getAsString().equals("plant")?Ps4CoralFeatures.canStayPlant(w,0,64,0):Ps4CoralFeatures.canStayFan(w,0,64,0);
   String label="original coral rule "+c;assertEquals(label,c.get("result").getAsBoolean(),result);compare(label,c,w);if(result)accepted++;else rejected++;
  }
  for(JsonElement e:fixture.getAsJsonArray("cases")){
   JsonObject c=e.getAsJsonObject();World w=new World(c);Random random=new Random(c.get("seed").getAsLong());String kind=c.get("kind").getAsString();
   String label="original coral "+kind+" seed="+c.get("seed")+" profile="+w.profile+" pos="+c.get("position")+" plane="+c.get("plane")+" placement="+c.get("placement")+" loaded="+c.get("loaded");
   Boolean result=place(w,random,kind,c.has("direction")?c.get("direction").getAsInt():2);
   if(result!=null)assertEquals(label+" result",c.get("result").getAsBoolean(),result.booleanValue());
   compare(label,c,w);assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());
   if(!Arrays.equals(w.plane.snapshot(),array(c.getAsJsonArray("plane"))))changedPlanes++;
   if(w.writes.size()>0)successes.add(kind);writes+=w.writes.size();for(JsonElement row:w.writes)tiles.add(row.getAsJsonArray().get(3).getAsInt()&65535);
  }
  for(JsonElement e:fixture.getAsJsonArray("sequences")){
   JsonObject sequence=e.getAsJsonObject();Random random=new Random(sequence.get("seed").getAsLong());Ps4DirectionPlane plane=new Ps4DirectionPlane();int index=0;
   for(JsonElement step:sequence.getAsJsonArray("steps")){
    JsonObject c=step.getAsJsonObject();World w=new World(c,plane);String label="original shared coral/well sequence "+sequence.get("seed")+" step="+(index++);
    assertArrayEquals(label+" preceding shared plane",array(c.getAsJsonArray("plane")),plane.snapshot());
    assertEquals(label+" result",c.get("result").getAsBoolean(),place(w,random,c.get("kind").getAsString(),2).booleanValue());compare(label,c,w);
   }
   assertEquals("shared sequence following RNG",sequence.get("random_after").getAsLong(),random.nextLong());
  }
  assertTrue("support and rejection branches",accepted>100&&rejected>100);assertTrue("shared plane swaps",changedPlanes>100);assertTrue("actual placements",writes>500);assertEquals(Set.of("hang","anemone","fan","side_fan"),successes);assertEquals(Set.of(263,264,266,267,268),tiles);
 }
 private static Boolean place(World w,Random random,String kind,int direction){
  int[] p=w.origin;return switch(kind){case "anemone"->Ps4CoralFeatures.anemone(w,random,p[0],p[1],p[2]);case "hang"->Ps4CoralFeatures.hanging(w,random,p[0],p[1],p[2]);case "fan"->{Ps4CoralFeatures.fan(w,random,p[0],p[1],p[2]);yield null;}case "side_fan"->{Ps4CoralFeatures.sideFan(w,random,p[0],p[1],p[2],direction);yield null;}case "well"->Ps4DesertWellFeature.place(w,w.plane,p[0],p[1],p[2]);default->throw new AssertionError(kind);};
 }
 static void compare(String label,JsonObject c,World w)throws Exception{
  try{
   Ps4FloraOracleTest.compare(label,c,w);assertEquals(label+" effective queries",c.getAsJsonArray("effective_queries"),w.effectiveQueries);assertEquals(label+" placement queries",c.getAsJsonArray("placement_queries"),w.placementQueries);assertEquals(label+" state placement queries",c.getAsJsonArray("state_placement_queries"),w.statePlacementQueries);assertEquals(label+" face support queries",c.getAsJsonArray("face_queries"),w.faceQueries);assertEquals(label+" chunk queries",c.getAsJsonArray("chunk_queries"),w.chunkQueries);assertArrayEquals(label+" cached plane",array(c.getAsJsonArray("plane_after")),w.plane.snapshot());
  }catch(AssertionError error){JsonObject d=new JsonObject();d.add("case",c);d.add("actual_probes",w.probes);d.add("actual_writes",w.writes);d.add("actual_effective_queries",w.effectiveQueries);d.add("actual_placement_queries",w.placementQueries);d.add("actual_face_queries",w.faceQueries);Files.writeString(Path.of("build/ps4-coral-mismatch.json"),d.toString());throw error;}
 }
 private static int[] array(JsonArray values){int[] a=new int[values.size()];for(int i=0;i<a.length;i++)a[i]=values.get(i).getAsInt();return a;}
 private static JsonArray row(int... values){JsonArray r=new JsonArray();for(int value:values)r.add(value);return r;}
 static class World extends Ps4FloraOracleTest.World implements Ps4CoralFeatures.Access {
  final JsonObject input;final Ps4DirectionPlane plane;final JsonArray effectiveQueries=new JsonArray(),placementQueries=new JsonArray(),statePlacementQueries=new JsonArray(),faceQueries=new JsonArray(),chunkQueries=new JsonArray(),probes=new JsonArray();
  World(JsonObject c)throws Exception{this(c,c.has("plane")?new Ps4DirectionPlane(array(c.getAsJsonArray("plane"))):new Ps4DirectionPlane());}
  World(JsonObject c,Ps4DirectionPlane plane)throws Exception{super(c);input=c;this.plane=plane;}
  @Override int initial(int x,int y,int z){
   if(profile.equals("coral_rule"))return x==0&&z==0?y==63?input.get("below").getAsInt():y==65?input.get("above").getAsInt():0:0;
   int ox=origin[0],oy=origin[1],oz=origin[2],dx=x-ox,dz=z-oz;
   if(profile.equals("well_sand"))return y>oy?0:y==oy?12:1;
   if(profile.equals("air")||y<0||y>=256)return 0;
   int floor=profile.startsWith("anemone_")?oy-1:profile.startsWith("hang_")?oy:oy-2;if(profile.equals("steps"))floor+=Math.floorMod(x*3+z*7,5)-2;
   if(y<floor)return 1;
   if(y==floor){
    if(profile.startsWith("hang_")&&(x!=ox||z!=oz)){
     if(profile.equals("hang_dry"))return 0;if(profile.equals("hang_north")&&(dx!=0||dz!=-1))return 1;if(profile.equals("hang_east")&&(dx!=1||dz!=0))return 1;if(profile.equals("hang_checker")&&Math.floorMod(x+z,3)==0)return 1;return 9;
    }
    return profile.endsWith("dirt")?3:profile.endsWith("live")?259|(2<<16):profile.endsWith("dead")?259|(10<<16):1;
   }
   if(y>=64)return 0;if(profile.equals("hang_dry"))return 0;if(profile.equals("hang_north")&&(dx!=0||dz!=-1))return 1;if(profile.equals("hang_east")&&(dx!=1||dz!=0))return 1;if(profile.equals("hang_checker")&&Math.floorMod(x+z,3)==0)return 1;
   if(profile.equals("flowing"))return 8|(Math.floorMod(x+z,10)<<16);if(profile.equals("waterlogged")&&Math.floorMod(x+z,3)==0)return 20;return 9;
  }
  @Override public int block(int x,int y,int z){int value=super.block(x,y,z);probes.add(row(x,y,z,value));return value;}
  public boolean clientSide(){return input.has("client")&&input.get("client").getAsBoolean();}
  public boolean solidBlocking(int state){return !NONSOLID.contains(state&65535)&&!Set.of(18,161,258,270,271,44,126,182,205,312).contains(state&65535);}
  public boolean slab(int state){return Set.of(44,126,182,205,312).contains(state&65535);}
  public boolean seagrassBelowFlag(int state){return false;}
  public boolean seagrassReentryBlocked(){return false;}
  public Ps4DirectionPlane horizontalDirections(){return plane;}
  public int effectiveBlock(int x,int y,int z){int value=profile.equals("coral_rule")?input.get("effective").getAsInt():state(x,y,z);if(profile.equals("waterlogged")&&value==20)value=9;effectiveQueries.add(row(x,y,z,value));return value;}
  public boolean canPlace(int state,int x,int y,int z){boolean result=placement(x,y,z);JsonArray q=row(x,y,z,state);q.add(result);placementQueries.add(q);return result;}
  public boolean canPlaceState(int state,int x,int y,int z){boolean result=placement(x,y,z);JsonArray q=row(x,y,z,state);q.add(result);statePlacementQueries.add(q);return result;}
  private boolean placement(int x,int y,int z){String mode=input.get("placement").getAsString();return mode.equals("all")||mode.equals("checker")&&Math.floorMod(x+y+z,3)!=0;}
  public boolean supportsFace(int state,int direction,int kind){boolean result=profile.equals("coral_rule")?input.get("face_support").getAsBoolean():solidBlocking(state);JsonArray q=row(state,direction,kind);q.add(result);faceQueries.add(q);return result;}
  public boolean hasChunk(int x,int z){String mode=input.get("loaded").getAsString();boolean result=mode.equals("all")||mode.equals("checker")&&Math.floorMod(x+z,3)!=0;JsonArray q=row(x,z);q.add(result);chunkQueries.add(q);return result;}
 }
}

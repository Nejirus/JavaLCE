package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4DesertWellOracleTest {
 @Test public void originalWellGeometryConstructorAndSandPredicateMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-wells.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  assertEquals(16,fixture.getAsJsonArray("configurations").size());
  for(JsonElement e:fixture.getAsJsonArray("configurations")){JsonObject c=e.getAsJsonObject();assertEquals(new JsonParser().parse("[65580,24,8]"),c.get("states"));assertEquals(1,c.get("slab_native_data").getAsInt());}
  for(JsonElement e:fixture.getAsJsonArray("predicate_cases")){JsonObject c=e.getAsJsonObject();assertEquals(c.get("state").getAsInt()==12,c.get("result").getAsBoolean());}
  assertEquals(5880,fixture.getAsJsonArray("cases").size());int accepted=0,rejected=0,writes=0;
  for(JsonElement e:fixture.getAsJsonArray("cases")){
   JsonObject c=e.getAsJsonObject();World world=new World(c);int[] p=world.origin;Random random=new Random(c.get("seed").getAsLong());boolean result=Ps4DesertWellFeature.place(world,p[0],p[1],p[2]);
   String label="original well profile="+world.profile+" pos="+c.get("position")+" surface="+c.get("surface_offset")+" holes="+c.get("holes");assertEquals(label,c.get("result").getAsBoolean(),result);Ps4FloraOracleTest.compare(label,c,world);assertEquals(label+" no RNG draw",c.get("random_after").getAsLong(),random.nextLong());
   if(result){accepted++;assertEquals(label+" geometry write count",96,world.writes.size());}else rejected++;writes+=world.writes.size();
  }
  assertTrue("success/rejection/geometry coverage",accepted>1000&&rejected>1000&&writes>100000);
 }
 private record Hole(int x,int y,int z){}
 private static class World extends Ps4TreeFeatureOracleTest.World {
  final int offset;final Set<Hole> holes=new HashSet<>();
  World(JsonObject c)throws Exception{super(c);offset=c.get("surface_offset").getAsInt();for(JsonElement e:c.getAsJsonArray("holes")){JsonArray p=e.getAsJsonArray();holes.add(new Hole(p.get(0).getAsInt(),p.get(1).getAsInt(),p.get(2).getAsInt()));}}
  @Override int initial(int x,int y,int z){
   int surface=origin[1]+offset;
   if(y==surface)return switch(profile){case "red_sand"->12|(1<<16);case "stone"->1;case "dirt"->3;default->12;};
   if(y>surface||profile.equals("empty_below"))return 0;
   return profile.equals("holes")&&holes.contains(new Hole(x-origin[0],y-surface,z-origin[2]))?0:1;
  }
 }
}

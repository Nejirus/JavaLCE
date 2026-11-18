package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4IceOracleTest {
 @Test public void originalIceGrowthFloatGeometryRootsAndRngMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-ice.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(3232,cases.size());int writes=0;Set<String> successes=new HashSet<>(),failures=new HashSet<>();boolean tall=false,roots=false;
  for(JsonElement e:cases){
   JsonObject c=e.getAsJsonObject();World w=new World(c);int[] p=w.origin;String kind=c.get("kind").getAsString();Random random=new Random(c.get("seed").getAsLong());
   boolean result=switch(kind){case "blue"->Ps4IceFeatures.blueIce(w,random,p[0],p[1],p[2]);case "patch"->Ps4IceFeatures.patch(w,random,p[0],p[1],p[2],c.get("size").getAsInt(),c.get("state").getAsInt());case "spike"->Ps4IceFeatures.spike(w,random,p[0],p[1],p[2],c.get("updates").getAsBoolean());default->throw new AssertionError(kind);};
   String label="original ice "+kind+" seed="+c.get("seed")+" pos="+c.get("position")+" profile="+w.profile+" size="+c.get("size")+" state="+c.get("state")+" updates="+c.get("updates");
   assertEquals(label+" result",c.get("result").getAsBoolean(),result);
   try{Ps4FloraOracleTest.compare(label,c,w);assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());}
   catch(AssertionError error){JsonObject d=new JsonObject();d.add("case",c);d.add("actual_writes",w.writes);d.add("actual_probes",w.probes);Files.writeString(Path.of("build/ps4-ice-mismatch.json"),d.toString());throw error;}
   if(w.writes.size()>0)successes.add(kind);if(!result)failures.add(kind);writes+=w.writes.size();
   if(kind.equals("spike"))for(JsonElement row:w.writes){int y=row.getAsJsonArray().get(1).getAsInt();if(y>p[1]+20)tall=true;if(y==51)roots=true;}
  }
  assertEquals(Set.of("blue","patch","spike"),successes);assertEquals(Set.of("blue","patch","spike"),failures);assertTrue("substantial geometry coverage",writes>100000);assertTrue("rare raised spike",tall);assertTrue("roots reach the original floor cutoff",roots);
 }
 private static JsonArray row(int... values){JsonArray r=new JsonArray();for(int value:values)r.add(value);return r;}
 private static class World extends Ps4FloraOracleTest.World implements Ps4IceFeatures.Access {
  final JsonObject input;final String kind;final JsonArray probes=new JsonArray();
  World(JsonObject c)throws Exception{super(c);input=c;kind=c.get("kind").getAsString();}
  @Override int initial(int x,int y,int z){
   int dx=x-origin[0],dy=y-origin[1],dz=z-origin[2];
   if(kind.equals("blue")){
    if(profile.equals("air"))return 0;if(profile.equals("flowing"))return 8;
    if(profile.equals("ice_below")&&dx==0&&dy==-1&&dz==0)return 174;
    if(Set.of("ice_above","current_air","mixed","crowded").contains(profile)&&dx==0&&dy==1&&dz==0)return 174;
    if(profile.equals("ice_side")&&dx==1&&dy==0&&dz==0)return 174;
    if(profile.equals("blue_only")&&dx==1&&dy==0&&dz==0)return 273;
    if(profile.equals("current_air")&&dx==0&&dy==0&&dz==0)return 0;
    if(profile.equals("crowded")&&dx>=1&&dx<=3&&dz>=1&&dz<=3&&dy>=-1&&dy<=1)return dx==2&&dy==0&&dz==2?9:273;
    if(profile.equals("mixed")&&Math.floorMod(x*7+y*11+z*3,7)==0)return new int[]{0,1,8,9,79,174,273}[Math.floorMod(x+y+z,7)];
    return 9;
   }
   if(y<0||y>=256||profile.equals("air"))return 0;
   int floor=origin[1]-4+(profile.equals("buried")?3:0);
   if(profile.equals("overhang")&&y==origin[1]+2&&Math.abs(dx)+Math.abs(dz)<3)return 1;
   if(y>floor)return 0;
   if(y==floor)return switch(profile){case "dirt"->3;case "grass"->2;case "ice"->79;case "stone"->1;case "snow_layers"->78;case "water"->9;default->80;};
   if(profile.equals("snow_only"))return 80;
   if(profile.equals("root_holes")&&Math.floorMod(x*7+y*3+z*11,5)==0)return 0;
   if(profile.equals("mixed"))return new int[]{0,1,3,3|(2<<16),79,80,174,9}[Math.floorMod(x*7+y*3+z*11,8)];
   return 3;
  }
  @Override public int block(int x,int y,int z){int value=super.block(x,y,z);probes.add(row(x,y,z,value));return value;}
  @Override public int seaLevel(){return input.get("sea_level").getAsInt();}
 }
}

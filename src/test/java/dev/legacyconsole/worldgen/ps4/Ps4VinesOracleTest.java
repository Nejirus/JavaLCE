package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4VinesOracleTest {
 private static final int[][] DIRECTIONS={{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
 @Test public void originalVineAttachmentExceptionsGeometryAndRngMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-vines.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  assertEquals(345,fixture.getAsJsonArray("exceptions").size());assertEquals(49680,fixture.getAsJsonArray("rules").size());assertEquals(384,fixture.getAsJsonArray("cases").size());
  for(JsonElement element:fixture.getAsJsonArray("exceptions")){JsonObject c=element.getAsJsonObject();assertEquals("original identity/RTTI exception "+c.get("name"),c.get("result").getAsBoolean(),Ps4VineRules.exception(c.get("state").getAsInt()));}
  int accepted=0,rejected=0,writes=0;
  for(JsonElement element:fixture.getAsJsonArray("rules")){
   JsonObject c=element.getAsJsonObject();World world=new World(c);int direction=c.get("direction").getAsInt();
   boolean result=Ps4VineRules.mayPlaceOnFace(world,0,64,0,direction);String label="original vine rule neighbor="+c.get("neighbor")+" above="+c.get("above")+" direction="+direction+" side="+c.get("side_shape")+" top="+c.get("top_shape");
   assertEquals(label,c.get("result").getAsBoolean(),result);Ps4FloraOracleTest.compare(label,c,world);assertEquals(label+" face queries",c.getAsJsonArray("face_queries"),world.faceQueries);if(result)accepted++;else rejected++;
  }
  for(JsonElement element:fixture.getAsJsonArray("cases")){
   JsonObject c=element.getAsJsonObject();World world=new World(c);Random random=new Random(c.get("seed").getAsLong());int[] p=world.origin;
   boolean result=Ps4VinesFeature.place(world,random,p[0],p[1],p[2]);String label="original vines seed="+c.get("seed")+" profile="+world.profile+" pos="+c.get("position");
   assertEquals(label+" result",c.get("result").getAsBoolean(),result);Ps4FloraOracleTest.compare(label,c,world);assertEquals(label+" face queries",c.getAsJsonArray("face_queries"),world.faceQueries);assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());writes+=world.writes.size();
  }
  assertTrue("attachment acceptance, rejection and placed vines covered",accepted>1000&&rejected>10000&&writes>1000);
 }
 private static class World extends Ps4TreeFeatureOracleTest.World implements Ps4VineRules.Access {
  final JsonObject input;final JsonArray faceQueries=new JsonArray();
  World(JsonObject c)throws Exception{super(c);input=c;}
  @Override int initial(int x,int y,int z){
   if(profile.equals("vine_rules")){
    int dir=input.get("direction").getAsInt();int[] d=DIRECTIONS[dir^1];
    if(x==d[0]&&y==64+d[1]&&z==d[2])return input.get("neighbor").getAsInt();
    return x==0&&y==65&&z==0?input.get("above").getAsInt():0;
   }
   if(y<0||y>=256)return 0;int ox=origin[0],oy=origin[1],oz=origin[2];
   if(Set.of("stone_wall","leaf_wall","glass_wall","trapdoor_wall","shulker_wall").contains(profile)&&z==oz+1&&x-ox>=-4&&x-ox<=4)return switch(profile){case "stone_wall"->1;case "leaf_wall"->18;case "glass_wall"->20;case "trapdoor_wall"->274;default->260;};
   if(profile.equals("mixed_walls")&&(Math.floorMod(x-ox+3,7)==0||Math.floorMod(z-oz+2,5)==0))return new int[]{1,18,20,33,260,274}[Math.floorMod(x*3+z+y,6)];
   if(profile.equals("old_vines")&&x==ox&&z==oz&&y<oy+5)return 106;
   return super.initial(x,y,z);
  }
  public int faceShape(int state,int x,int y,int z,int direction){
   int value;
   if(profile.equals("vine_rules")){
    int dir=input.get("direction").getAsInt();int[] d=DIRECTIONS[dir^1];value=x==0&&y==65&&z==0&&direction==1?input.get("top_shape").getAsInt():x==d[0]&&y==64+d[1]&&z==d[2]&&direction==dir?input.get("side_shape").getAsInt():1;
   }else value=Set.of(0,8,9,10,11,50,75,76,106).contains(state&65535)?1:0;
   JsonArray row=new JsonArray();for(int v:new int[]{x,y,z,direction,value})row.add(v);faceQueries.add(row);return value;
  }
 }
}

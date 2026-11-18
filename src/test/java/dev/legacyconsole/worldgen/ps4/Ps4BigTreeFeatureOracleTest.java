package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4BigTreeFeatureOracleTest {
 @Test public void originalBigOakConstructorGeometryAndBothRandomStreamsMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-big-trees.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(2912,cases.size());int successes=0,rejections=0,writes=0,shortened=0,reused=0;
  Field internalField=Ps4BigTreeFeature.class.getDeclaredField("random");internalField.setAccessible(true);
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject(),ctor=c.getAsJsonObject("constructor");
   assertEquals(0,ctor.get("height_limit").getAsInt());assertEquals(0,ctor.get("trunk_height").getAsInt());
   assertEquals(0.618,ctor.get("attenuation").getAsDouble(),0);assertEquals(0.381,ctor.get("branch_slope").getAsDouble(),0);
   assertEquals(1,ctor.get("trunk_size").getAsInt());assertEquals(12,ctor.get("height_range").getAsInt());assertEquals(4,ctor.get("leaf_distance").getAsInt());
   assertEquals(1.0,ctor.get("width_scale").getAsDouble(),0);assertEquals(1.0,ctor.get("leaf_density").getAsDouble(),0);
   var tree=new Ps4BigTreeFeature(c.get("updates").getAsBoolean(),c.get("log").getAsInt(),c.get("leaves").getAsInt());
   if(c.get("prepared").getAsBoolean())tree.prepare();
   JsonArray placements=c.getAsJsonArray("placements");if(placements.size()>1)reused++;
   for(JsonElement placement:placements){
    JsonObject p=placement.getAsJsonObject();World world=new World(p);Random random=new Random(p.get("seed").getAsLong());int[] pos=world.origin;
    String label="original big oak seed="+p.get("seed")+" profile="+world.profile+" pos="+p.get("position")+" prepared="+c.get("prepared");
    boolean result=tree.place(world,random,pos[0],pos[1],pos[2]);assertEquals(label+" result",p.get("result").getAsBoolean(),result);
    Ps4FloraOracleTest.compare(label,p,world);assertEquals(label+" following population RNG",p.get("random_after").getAsLong(),random.nextLong());
    assertEquals(label+" following internal RNG",p.get("internal_random_after").getAsLong(),((Random)internalField.get(tree)).nextLong());
    JsonObject config=p.getAsJsonObject("config");assertEquals(label+" height limit",config.get("height_limit").getAsInt(),tree.heightLimit());
    assertEquals(label+" trunk height",config.get("trunk_height").getAsInt(),tree.trunkHeight());assertEquals(label+" leaf distance",config.get("leaf_distance").getAsInt(),tree.leafDistance());
    JsonArray nodes=new JsonArray();for(int[] node:tree.leafNodesForValidation()){JsonArray row=new JsonArray();for(int v:node)row.add(v);nodes.add(row);}assertEquals(label+" leaf node graph",p.getAsJsonArray("nodes"),nodes);
    if(result)successes++;else rejections++;if(tree.heightLimit()==7)shortened++;writes+=world.writes.size();
   }
  }
  assertTrue("successful, rejected, shortened and reused features",successes>100&&rejections>100&&shortened>20&&reused==32);assertTrue("large geometry coverage",writes>100000);
  for(JsonElement element:fixture.getAsJsonArray("leaf_data_cases")){JsonObject c=element.getAsJsonObject();assertEquals("native leaf species/decay metadata",c.get("state").getAsInt()>>>16,c.get("native_data").getAsInt());}
 }
 private static class World extends Ps4TreeFeatureOracleTest.World {
  World(JsonObject c)throws Exception{super(c);}
  @Override int initial(int x,int y,int z){
   int ox=origin[0],oy=origin[1],oz=origin[2];
   if(profile.equals("high_obstacle")&&x==ox&&y==oy+7&&z==oz)return 1;
   if(profile.equals("off_axis")&&y>=oy+3&&y<oy+9&&x==ox+2&&z==oz+1)return 1;
   if(profile.equals("sky_obstacle")&&y==oy+8&&Math.abs(x-ox)<=5&&Math.abs(z-oz)<=5)return 20;
   return super.initial(x,y,z);
  }
 }
}

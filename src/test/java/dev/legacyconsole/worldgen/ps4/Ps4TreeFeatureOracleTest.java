package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4TreeFeatureOracleTest {
 @Test public void originalTreeKernelsAndSharedHelpersMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-trees.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){
   fixture=new JsonParser().parse(reader).getAsJsonObject();
  }
  JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(23712,cases.size());int writes=0,success=0,rejection=0,fallen=0;Set<Integer> types=new HashSet<>();
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();World world=new World(c);Random random=new Random(c.get("seed").getAsLong());int[] p=world.origin;int log=c.get("log").getAsInt(),leaves=c.get("leaves").getAsInt();boolean updates=c.get("updates").getAsBoolean();
   int height=c.get("base_height").getAsInt();String kind=c.get("kind").getAsString();
   Boolean result=switch(kind){
    case "pine"->Ps4PineFeature.place(world,random,p[0],p[1],p[2],height,log,leaves,updates);
    case "ground_bush"->Ps4GroundBushFeature.place(world,random,p[0],p[1],p[2],log,leaves,updates);
    case "trunk"->Ps4TreeTrunk.place(world,random,p[0],p[1],p[2],height,log,c.get("jungle").getAsBoolean(),updates);
    case "basic"->Ps4TreeFeature.place(world,random,p[0],p[1],p[2],height,log,leaves,c.get("jungle").getAsBoolean(),updates);
    case "birch"->Ps4BirchFeature.place(world,random,p[0],p[1],p[2],height,log,leaves,c.get("tall").getAsBoolean(),updates);
    case "swamp"->Ps4SwampTreeFeature.place(world,random,p[0],p[1],p[2],height,log,leaves,updates);
    case "savanna"->Ps4SavannaTreeFeature.place(world,random,p[0],p[1],p[2],height,log,leaves,updates);
    case "roof"->Ps4RoofTreeFeature.place(world,random,p[0],p[1],p[2],height,log,leaves,updates);
    case "fallen"->{Ps4TreeTrunk.fallen(world,random,p[0],p[1],p[2],height,log,updates);yield null;}
    default->throw new AssertionError(kind);
   };
   String label="original "+c.get("kind")+" seed="+c.get("seed")+" profile="+world.profile+" pos="+c.get("position")+" updates="+updates;
   assertEquals(label+" result",c.get("result").isJsonNull()?null:c.get("result").getAsBoolean(),result);Ps4FloraOracleTest.compare(label,c,world);
   assertEquals(label+" support queries",c.getAsJsonArray("support_queries"),world.supportQueries);
   assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());
   writes+=world.writes.size();if(result==null)fallen++;else if(result)success++;else rejection++;
   for(JsonElement row:world.writes)types.add(row.getAsJsonArray().get(3).getAsInt()&65535);
  }
  assertTrue("success, rejection and fallen-log coverage",success>100&&rejection>100&&fallen>100);assertTrue("substantial geometry coverage",writes>10000);assertTrue("vines and both mushrooms covered",types.containsAll(Set.of(106,39,40)));
  JsonObject first=cases.get(0).getAsJsonObject();World world=new World(first);JsonArray predicates=fixture.getAsJsonArray("predicates");assertEquals(768,predicates.size());
  for(JsonElement element:predicates){JsonObject c=element.getAsJsonObject();int state=c.get("state").getAsInt();assertEquals("original tree replacement predicate "+state,c.get("result").getAsBoolean(),Ps4TreeRules.canGrowInto(world,state));}
  for(JsonElement element:fixture.getAsJsonArray("log_data_cases")){JsonObject c=element.getAsJsonObject();assertEquals("original log species/axis metadata",c.get("native_data").getAsInt(),c.get("state").getAsInt()>>>16);}
  for(JsonElement element:fixture.getAsJsonArray("plant_data_cases")){JsonObject c=element.getAsJsonObject();assertEquals("original vine/cocoa metadata",c.get("native_data").getAsInt(),c.get("state").getAsInt()>>>16);}
 }
 static class World extends Ps4FloraOracleTest.World implements Ps4TreeTrunk.Access {
  final JsonArray supportQueries=new JsonArray();
  World(JsonObject c)throws Exception{super(c);}
  @Override int initial(int x,int y,int z){
   int ox=origin[0],oy=origin[1],oz=origin[2];
   if(Set.of("shallow_water","deep_water","flowing_surface").contains(profile)){
    if(y<0||y>=256)return 0;int depth=profile.equals("deep_water")?3:1;
    return y>=oy?0:y>=oy-depth?profile.equals("flowing_surface")?8:9:y==oy-depth-1?2:1;
   }
   if(profile.equals("stone_obstacle")&&x==ox&&y==oy+2&&z==oz)return 1;
   if(profile.equals("trunk_obstacle")&&x==ox&&y==oy+1&&z==oz)return 17;
   if(profile.equals("glass")&&x==ox&&y==oy+1&&z==oz)return 20;
   if(profile.equals("leaves")&&y>=oy&&y<oy+16&&Math.abs(x-ox)<=3&&Math.abs(z-oz)<=3)return 18;
   if(profile.equals("vine_column")&&x==ox&&z==oz&&y>=oy&&y<oy+5)return 106;
   if(profile.equals("sapling_column")&&x==ox&&z==oz&&y>=oy&&y<oy+5)return 6;
   return super.initial(x,y,z);
  }
  @Override public boolean intersectsGenerationRule(int x0,int y0,int z0,int x1,int y1,int z1){JsonArray row=new JsonArray();for(int value:new int[]{x0,y0,z0,x1,y1,z1})row.add(value);ruleQueries.add(row);return x0<=origin[0]&&origin[0]<=x1;}
  @Override public int height(int x,int z){JsonArray row=new JsonArray();row.add(x);row.add(z);heightQueries.add(row);return origin[1]+(profile.equals("raised")?2:0);}
  public boolean vineMaterial(int state){return (state&65535)==106;}
  public boolean dirtMaterial(int state){int id=state&65535;return id==3||id==60;}
  public boolean grassMaterial(int state){int id=state&65535;return id==2||id==110||id==170;}
  public boolean solidBlocking(int x,int y,int z,boolean whenUnloaded){boolean result=profile.equals("wide_gaps")?false:profile.equals("gaps")?Math.floorMod(x+z,3)==0:solidRender(state(x,y,z));JsonArray row=new JsonArray();row.add(x);row.add(y);row.add(z);row.add(whenUnloaded);row.add(result);supportQueries.add(row);return result;}
 }
}

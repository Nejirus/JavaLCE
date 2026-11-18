package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4PopulationOracleTest {
 @Test public void originalPopulationSequenceAndSharedRandomMatch()throws Exception{
  verifyPopulation("ps4-population.json.gz",false);
 }
 @Test public void originalEmptyMobHelperPreservesSharedPopulationRandom()throws Exception{
  verifyPopulation("ps4-population-native-mobs.json.gz",true);
 }
 private void verifyPopulation(String fixture,boolean nativeMobs)throws Exception{
  JsonArray cases;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/"+fixture);
      GZIPInputStream gzip=new GZIPInputStream(input);
      Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){
   cases=new JsonParser().parse(reader).getAsJsonObject().getAsJsonArray("cases");
  }
  assertEquals(312,cases.size());int water=0,lava=0,dungeons=0;
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();long seed=c.get("seed").getAsLong();
   int cx=c.getAsJsonArray("chunk").get(0).getAsInt(),cz=c.getAsJsonArray("chunk").get(1).getAsInt();
   String label="original population seed="+seed+" chunk="+cx+","+cz+" mode="+c.get("mode");
   Random initial=Ps4Population.random(seed,cx,cz);
   assertEquals(label+" first long",c.getAsJsonArray("initial_longs").get(0).getAsLong(),initial.nextLong());
   assertEquals(label+" second long",c.getAsJsonArray("initial_longs").get(1).getAsLong(),initial.nextLong());
   assertEquals(label+" initial double",Double.doubleToRawLongBits(c.get("initial_double").getAsDouble()),Double.doubleToRawLongBits(initial.nextDouble()));
   JsonObject settings=c.getAsJsonObject("settings");Set<Ps4Population.Structure> enabled=EnumSet.noneOf(Ps4Population.Structure.class);
   for(JsonElement name:settings.getAsJsonArray("structures"))enabled.add(Ps4Population.Structure.valueOf(name.getAsString()));
   var config=new Ps4Population.Settings(enabled,settings.get("water_lakes").getAsBoolean(),settings.get("water_chance").getAsInt(),settings.get("lava_lakes").getAsBoolean(),settings.get("lava_chance").getAsInt(),settings.get("dungeons").getAsBoolean(),settings.get("dungeon_count").getAsInt());
   if(c.get("mode").getAsString().equals("default"))assertEquals(label+" native default settings",Ps4Population.DEFAULTS,config);
   World world=nativeMobs?new World(c):new ControlledMobWorld(c);
   Random after=Ps4Population.populate(seed,cx,cz,c.get("structures").getAsBoolean(),config,world);
   JsonArray expected=c.getAsJsonArray("events");assertEquals(label+" event count",expected.size(),world.events.size());
   // String comparison retains every integer bit, including long draws above
   // the exact-integer range of double-based JSON number comparisons.
   for(int i=0;i<expected.size();i++)assertEquals(label+" event "+i,expected.get(i).toString(),world.events.get(i).toString());
   assertEquals(label+" following long 1",c.getAsJsonArray("after_longs").get(0).getAsLong(),after.nextLong());
   assertEquals(label+" following long 2",c.getAsJsonArray("after_longs").get(1).getAsLong(),after.nextLong());
   assertEquals(label+" following double",Double.doubleToRawLongBits(c.get("after_double").getAsDouble()),Double.doubleToRawLongBits(after.nextDouble()));
   assertEquals(label+" final mode",c.get("mode_after").getAsBoolean(),world.active);assertFalse(world.active);assertEquals(2,world.modeChanges);
   for(JsonElement event:world.events){JsonObject e=event.getAsJsonObject();if(e.get("event").getAsString().equals("dungeon"))dungeons++;if(e.get("event").getAsString().equals("lake")){if(e.get("tile").getAsInt()==9)water++;else lava++;}}
  }
  assertTrue("original cases exercise both lake branches and dungeon attempts",water>0&&lava>0&&dungeons>100);
 }
 private static class World implements Ps4Population.Access {
  final JsonArray events=new JsonArray();final int biome,sea;final String mode;boolean active;int modeChanges;
  World(JsonObject c){biome=c.get("biome").getAsInt();sea=c.get("sea_level").getAsInt();mode=c.get("mode").getAsString();}
  public void populationMode(boolean value){assertNotEquals("population mode transition",active,value);active=value;modeChanges++;}
  void event(String name,Object... values){
   assertTrue("callback executes within population mode",active);JsonObject event=new JsonObject();event.addProperty("event",name);
   for(int i=0;i<values.length;i+=2){String key=(String)values[i];Object value=values[i+1];if(value instanceof JsonElement j)event.add(key,j);else if(value instanceof Number n)event.addProperty(key,n);else if(value instanceof Boolean b)event.addProperty(key,b);else event.addProperty(key,(String)value);}
   events.add(event);
  }
  public int biome(int x,int z){event("biome","position",array(x,0,z));return biome;}
  public boolean structure(Ps4Population.Structure kind,Random random,int x,int z,boolean flag){event("structure","kind",kind.name(),"chunk",array(x,z),"village_flag",flag,"draw",random.nextLong());return kind==Ps4Population.Structure.VILLAGE&&mode.equals("village");}
  public void finishOutpostChunk(int x,int z){event("outpost_chunk","chunk",array(x,z),"create",true);event("finish_outpost");}
  public void lake(Random random,int x,int y,int z,int tile){event("lake","position",array(x,y,z),"tile",tile,"draw",random.nextLong());}
  public void dungeon(Random random,int x,int y,int z){event("dungeon","position",array(x,y,z),"draw",random.nextLong());}
  public void decorate(Random random,int id,int x,int z){event("decorate","biome",id,"position",array(x,0,z),"draw",random.nextLong());}
  public int seaLevel(){return sea;}
  public int precipitationHeight(int x,int z){event("height_chunk","chunk",array(x>>4,z>>4));return 64+((x&15)*3+(z&15))%4;}
  public boolean canFreeze(int x,int y,int z){event("can_freeze","position",array(x,y,z));return ((x+z)&3)==0;}
  public void ice(int x,int y,int z){event("ice","position",array(x,y,z),"state",79,"flags",2);}
  public void snow(int x,int y,int z,boolean checkLight,boolean accumulate){event("snow","position",array(x,y,z),"check_light",checkLight,"accumulate",accumulate);}
 }
 private static final class ControlledMobWorld extends World {
  ControlledMobWorld(JsonObject c){super(c);}
  public void spawnMobs(Random random,int id,int x,int z,int width,int depth){event("mobs","biome",id,"area",array(x,z,width,depth),"draw",random.nextLong());}
 }
 private static JsonArray array(int... values){JsonArray result=new JsonArray();for(int value:values)result.add(value);return result;}
}

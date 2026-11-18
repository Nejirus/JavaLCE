package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4LavaFireOracleTest {
 @Test public void originalLavaIgnitionAndIntegratedFlowRngMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-lava-fire.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  Map<Integer,Integer> defaults=new HashMap<>();Set<Integer> flammable=new HashSet<>();for(JsonElement e:fixture.getAsJsonArray("default_inputs")){JsonObject c=e.getAsJsonObject();defaults.put(c.get("tile").getAsInt(),c.get("state").getAsInt());if(c.get("flammable").getAsBoolean())flammable.add(c.get("tile").getAsInt());}
  JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(4608,cases.size());int writes=0,integrated=0;Set<Integer> heights=new HashSet<>();
  for(JsonElement e:cases){
   JsonObject c=e.getAsJsonObject();World w=new World(c,defaults,flammable);w.random=new Random(c.get("seed").getAsLong());int[] p=w.origin;
   if(c.get("integrated").getAsBoolean()){w.flow.tick(w,w.random,p[0],p[1],p[2],10);integrated++;}else Ps4LavaFireTick.tick(w,w.random,p[0],p[1],p[2]);
   String label="original lava ignition seed="+c.get("seed")+" pos="+c.get("position")+" profile="+w.profile+" enabled="+c.get("fire_enabled")+" loaded="+c.get("loaded")+" integrated="+c.get("integrated");
   JsonArray effects=new JsonArray();for(JsonElement event:w.events)if(!Set.of("primary","secondary","pair","face","chunk").contains(event.getAsJsonArray().get(0).getAsString()))effects.add(event);
   assertEquals(label+" effects",c.getAsJsonArray("effects"),effects);assertEquals(label+" probe/event count",c.get("event_count").getAsInt(),w.events.size());
   byte[] digest=MessageDigest.getInstance("SHA-256").digest(w.events.toString().getBytes(StandardCharsets.UTF_8));StringBuilder hex=new StringBuilder();for(byte b:digest)hex.append(String.format(Locale.ROOT,"%02x",b&255));if(!c.get("event_sha256").getAsString().equals(hex.toString())){JsonObject d=new JsonObject();d.add("case",c);d.add("actual_events",w.events);java.nio.file.Files.writeString(java.nio.file.Path.of("build/ps4-lava-fire-event-mismatch.json"),d.toString());}assertEquals(label+" ordered probes/events",c.get("event_sha256").getAsString(),hex.toString());assertEquals(label+" following RNG",c.get("random_after").getAsLong(),w.random.nextLong());
   for(JsonElement event:effects){JsonArray row=event.getAsJsonArray();if(row.get(0).getAsString().equals("set_primary")&&row.get(4).getAsInt()==51){writes++;heights.add(row.get(2).getAsInt());assertEquals("original ignition write flags",11,row.get(5).getAsInt());}}
  }
  assertEquals(2304,integrated);assertTrue("substantial ignition geometry and height-edge coverage",writes>500&&heights.stream().anyMatch(y->y<0)&&heights.stream().anyMatch(y->y>=256));
 }
 private static class World extends Ps4LiquidFlowOracleTest.World implements Ps4LavaFireTick.Access {
  final Map<Integer,Integer> defaults;final Set<Integer> flammable;
  World(JsonObject c,Map<Integer,Integer> defaults,Set<Integer> flammable)throws Exception{super(c);this.defaults=defaults;this.flammable=flammable;}
  @Override int initial(int x,int y,int z){
   Integer supplied=initialPrimary.get(List.of(x,y,z));if(supplied!=null)return supplied;
   int ox=origin[0],oy=origin[1],oz=origin[2];if(x==ox&&y==oy&&z==oz)return 10;
   if(profile.equals("air"))return 0;
   if(profile.equals("blocked_above")&&x==ox&&y==oy+1&&z==oz)return 1;
   if(profile.equals("leaf_shell"))return y>=oy&&y<=oy+1?18:0;
   if(profile.equals("mixed"))return new int[]{0,1,17,18,31,51}[Math.floorMod(x*7+y*3+z*11,6)];
   return y<oy?profile.equals("metadata_wood")?17|(3<<16):17:0;
  }
  public boolean fireTickEnabled(){return input.get("fire_enabled").getAsBoolean();}
  public int defaultBlockState(int tile){return defaults.getOrDefault(tile,tile);}
  public boolean flammableMaterial(int state){return flammable.contains(state&65535);}
  public boolean hasChunk(int x,int z,boolean allowEmpty){String loaded=input.get("loaded").getAsString();boolean result=loaded.equals("all")||loaded.equals("alternating")&&((x^z)&1)==0;JsonArray row=row("chunk",x,z);row.add(allowEmpty);row.add(result);return result;}
 }
}

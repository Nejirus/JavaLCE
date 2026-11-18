package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4LiquidFlowOracleTest {
 @Test public void originalFluidLayersSlopeSearchRngAndBoundedQueueMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-liquids.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(5856,cases.size());int writes=0,secondary=0,replays=0;Set<String> effects=new HashSet<>();boolean lavaRng=false;
  for(JsonElement e:cases){
   JsonObject c=e.getAsJsonObject();World w=new World(c);w.random=new Random(c.get("seed").getAsLong());int[] p=w.origin;int tile=c.get("kind").getAsString().equals("lava")?10:8;
   w.flow.tick(w,w.random,p[0],p[1],p[2],tile|(c.get("level").getAsInt()<<16));
   String label="original liquid "+c.get("kind")+" seed="+c.get("seed")+" pos="+c.get("position")+" level="+c.get("level")+" profile="+w.profile+" hot="+c.get("hot_step")+" warm="+c.get("ultra_warm")+" faces="+c.get("closed_faces")+" primary="+c.get("primary")+" secondary="+c.get("secondary")+" replay="+c.get("replay");
   JsonArray actualEffects=new JsonArray();for(JsonElement event:w.events){String kind=event.getAsJsonArray().get(0).getAsString();if(!Set.of("primary","secondary","pair","face").contains(kind))actualEffects.add(event);}
   assertEquals(label+" effects",c.getAsJsonArray("effects"),actualEffects);assertEquals(label+" probe/event count",c.get("event_count").getAsInt(),w.events.size());
   byte[] digest=MessageDigest.getInstance("SHA-256").digest(w.events.toString().getBytes(StandardCharsets.UTF_8));StringBuilder hex=new StringBuilder();for(byte b:digest)hex.append(String.format(Locale.ROOT,"%02x",b&255));if(!c.get("event_sha256").getAsString().equals(hex.toString())){JsonObject diagnostic=new JsonObject();diagnostic.add("case",c);diagnostic.add("actual_events",w.events);java.nio.file.Files.writeString(java.nio.file.Path.of("build/ps4-liquid-event-mismatch.json"),diagnostic.toString());}assertEquals(label+" ordered probes/events",c.get("event_sha256").getAsString(),hex.toString());
   long after=w.random.nextLong();assertEquals(label+" following RNG",c.get("random_after").getAsLong(),after);if(tile==10&&after!=new Random(c.get("seed").getAsLong()).nextLong())lavaRng=true;
   int scheduled=0;for(JsonElement event:actualEffects){String kind=event.getAsJsonArray().get(0).getAsString();effects.add(kind);if(kind.equals("set_primary"))writes++;if(kind.equals("set_secondary"))secondary++;if(kind.equals("schedule"))scheduled++;}
   if(c.get("replay").getAsBoolean()){assertEquals(label+" original immediate queue limit",100,scheduled);replays++;}
  }
  assertTrue("geometry and two-layer coverage",writes>1000&&secondary>20);assertEquals(32,replays);assertTrue("lava slowdown consumes the original RNG",lavaRng);assertTrue("flow/removal/drop/reaction coverage",effects.containsAll(Set.of("set_primary","set_secondary","remove","schedule","notify","drop","fizz","level_event")));
 }
 static class World extends Ps4FloraOracleTest.World implements Ps4LiquidFlow.Access {
  final JsonObject input;final JsonArray events=new JsonArray();final Map<List<Integer>,Integer> initialPrimary=new HashMap<>(),secondary=new HashMap<>();final Ps4LiquidFlow flow;Random random;
  World(JsonObject c)throws Exception{
   super(c);input=c;Ps4DirectionPlane plane=new Ps4DirectionPlane();if(c.has("plane")){JsonArray a=c.getAsJsonArray("plane");plane=new Ps4DirectionPlane(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt(),a.get(3).getAsInt());}flow=new Ps4LiquidFlow(c.get("kind").getAsString().equals("lava"),plane);for(JsonElement e:c.getAsJsonArray("primary")){JsonArray p=e.getAsJsonArray();initialPrimary.put(List.of(p.get(0).getAsInt(),p.get(1).getAsInt(),p.get(2).getAsInt()),p.get(3).getAsInt());}for(JsonElement e:c.getAsJsonArray("secondary")){JsonArray p=e.getAsJsonArray();secondary.put(List.of(p.get(0).getAsInt(),p.get(1).getAsInt(),p.get(2).getAsInt()),p.get(3).getAsInt());}initialPrimary.putIfAbsent(List.of(origin[0],origin[1],origin[2]),(c.get("kind").getAsString().equals("lava")?10:8)|(c.get("level").getAsInt()<<16));
  }
  @Override int initial(int x,int y,int z){return initialPrimary.getOrDefault(List.of(x,y,z),profile.equals("empty")?0:y<origin[1]?1:0);}
  JsonArray row(String kind,int... values){JsonArray row=new JsonArray();row.add(kind);for(int value:values)row.add(value);events.add(row);return row;}
  @Override public int block(int x,int y,int z){int value=super.block(x,y,z);row("primary",x,y,z,value);return value;}
  public Ps4LiquidFlow.Pair pairedBlocks(int x,int y,int z){int a=state(x,y,z),b=secondary.getOrDefault(List.of(x,y,z),0);row("pair",x,y,z,a,b);return new Ps4LiquidFlow.Pair(a,b);}
  public int secondaryBlock(int x,int y,int z){int value=secondary.getOrDefault(List.of(x,y,z),0);row("secondary",x,y,z,value);return value;}
  @Override public void setBlock(int x,int y,int z,int state,int flags){super.setBlock(x,y,z,state,flags);row("set_primary",x,y,z,state,flags);}
  public void setSecondary(int x,int y,int z,int state,int flags){secondary.put(List.of(x,y,z),state);row("set_secondary",x,y,z,state,flags);}
  public void removePrimary(int x,int y,int z){super.setBlock(x,y,z,0,0);row("remove",x,y,z);}
  public boolean blocksFluid(int state){return solidRender(state);}
  public boolean canPassFluidFace(int x,int y,int z,int state,int direction){boolean result=!input.get("closed_faces").getAsBoolean();row("face",x,y,z,state&65535,direction).add(result);return result;}
  public boolean dropsInWater(int state){return input.get("drop_plants").getAsBoolean()&&Set.of(31,37,38,39,40,50,106).contains(state&65535);}
  public void dropBlock(int x,int y,int z,int state,int fortune){row("drop",x,y,z,state,fortune);}
  public void levelEvent(int event,int x,int y,int z,int data){row("level_event",event,x,y,z,data);}
  public void fizz(int x,int y,int z){row("fizz",x,y,z);}
  public void scheduleFluid(int x,int y,int z,int tile,int delay){row("schedule",x,y,z,tile,delay);if(input.get("replay").getAsBoolean()){int next=secondary.getOrDefault(List.of(x,y,z),0);if(next==0)next=state(x,y,z);flow.tick(this,random,x,y,z,next);}}
  public void notifyFluidNeighbors(int x,int y,int z,int tile,int flags){row("notify",x,y,z,tile,flags);}
  public int finiteChunkCount(){return input.get("chunk_count").getAsInt();}
  public boolean hotFlowStep(){return input.get("hot_step").getAsBoolean();}
  public boolean ultraWarm(){return input.get("ultra_warm").getAsBoolean();}
  public boolean immediateFluidTicks(){return input.get("immediate").getAsBoolean();}
  public boolean fireTickEnabled(){return false;} // Supplied doFireTick=false in original captures.
  public int defaultBlockState(int tile){return tile;}
  public boolean flammableMaterial(int state){throw new AssertionError("Disabled fire tick must not query flammability");}
  public boolean hasChunk(int x,int z,boolean allowEmpty){throw new AssertionError("Disabled fire tick must not query loaded chunks");}
  public boolean skipLavaWaterReaction(){return false;} // Supplied feature15=false.
  public boolean lavaStoneContactMaterial(int state){return waterMaterial(state);} // Second supplied material identity is unbound.
 }
}

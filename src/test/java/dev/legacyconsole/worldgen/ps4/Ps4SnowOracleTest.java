package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4SnowOracleTest {
 @Test public void originalSnowRulesDefaultsActualIncrementsNeighborSelectionAndFailedWritesMatch()throws Exception{
  JsonObject fixture;try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-snow.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  assertEquals(78,fixture.get("snow_default").getAsInt());for(int i=0;i<8;i++)assertEquals(i,fixture.getAsJsonArray("snow_data").get(i).getAsInt());
  Map<Integer,JsonObject> defaults=new HashMap<>();for(JsonElement e:fixture.getAsJsonArray("default_inputs")){JsonObject d=e.getAsJsonObject();defaults.put(d.get("tile").getAsInt(),d);}
  for(JsonElement e:fixture.getAsJsonArray("maximum_depth_cases")){JsonObject c=e.getAsJsonObject();assertEquals("original maximum snow depth "+c.get("bits"),c.get("result").getAsInt(),Ps4SnowAndIce.maximumSnowDepth(Float.intBitsToFloat((int)Long.parseLong(c.get("bits").getAsString(),16))));}
  int writes=0,rejectedWrites=0;Set<String> kinds=new HashSet<>();
  for(JsonElement e:fixture.getAsJsonArray("cases")){
   JsonObject c=e.getAsJsonObject();World w=new World(c,defaults);JsonArray p=c.getAsJsonArray("position");int x=p.get(0).getAsInt(),y=p.get(1).getAsInt(),z=p.get(2).getAsInt();String kind=c.get("kind").getAsString();boolean actual;int depth=0;
   switch(kind){
    case "rule"->{var query=Ps4SnowAndIce.canSnowAt(w,x,y,z,c.get("check_height").getAsBoolean(),c.get("accumulate").getAsBoolean());actual=query.allowed();depth=query.depth();}
    case "state"->{assertEquals("original actual snow layer increment",c.get("state_result").getAsInt(),Ps4SnowAndIce.snowState(w,x,y,z,1,c.get("accumulate").getAsBoolean()));actual=true;}
    case "full"->actual=Ps4SnowAndIce.placeSnow(w,x,y,z,c.get("check_height").getAsBoolean(),c.get("accumulate").getAsBoolean());
    default->throw new AssertionError(kind);
   }
   String label="original snow "+kind+" position="+p+" blocks="+c.get("blocks")+" temperature="+c.get("temperature_bits")+" cap="+c.get("cap")+" accumulate="+c.get("accumulate")+" check="+c.get("check_height")+" chunk="+c.get("chunk_present")+" cached="+c.get("cached_height");
   try{assertEquals(label,c.get("result").getAsBoolean(),actual);assertEquals(label+" depth",c.get("depth").getAsInt(),depth);assertEquals(label+" queries",c.getAsJsonArray("events"),w.events);assertEquals(label+" height cache",c.get("cache_sha256").getAsString(),hex(MessageDigest.getInstance("SHA-256").digest(w.cache)));assertEquals(label+" writes",c.getAsJsonArray("writes"),w.writes);assertEquals(label+" outcomes",c.getAsJsonArray("write_attempts"),w.attempts);assertEquals(label+" block count",c.get("read_count").getAsInt(),w.readCount);assertEquals(label+" block order",c.get("read_sha256").getAsString(),hex(w.hash.digest()));}
   catch(AssertionError error){JsonObject d=new JsonObject();d.add("case",c);d.add("actual_events",w.events);d.add("actual_writes",w.writes);d.add("actual_probes",w.probes);Files.writeString(Path.of("build/ps4-snow-mismatch.json"),d.toString());throw error;}
   kinds.add(kind);writes+=w.writes.size();for(JsonElement a:w.attempts)if(!a.getAsJsonArray().get(5).getAsBoolean())rejectedWrites++;
  }
  assertEquals(Set.of("rule","state","full"),kinds);assertEquals(5472,fixture.getAsJsonArray("cases").size());assertTrue(writes>100&&rejectedWrites>0);
 }
 private record Pos(int x,int y,int z){}
 private static String hex(byte[] bytes){StringBuilder s=new StringBuilder();for(byte b:bytes)s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();}
 private static JsonArray row(Object... values){JsonArray r=new JsonArray();for(Object v:values)if(v instanceof Number n)r.add(n);else if(v instanceof Boolean b)r.add(b);else r.add(v.toString());return r;}
 private static class World implements Ps4SnowAndIce.SnowAccess {
  final byte[] cache=new byte[256];final JsonObject input;final Map<Integer,JsonObject> defaults;final Map<Pos,Integer> voxels=new HashMap<>();final JsonArray events=new JsonArray(),writes=new JsonArray(),attempts=new JsonArray(),probes=new JsonArray();final MessageDigest hash=MessageDigest.getInstance("SHA-256");int readCount;
  World(JsonObject c,Map<Integer,JsonObject> defaults)throws Exception{input=c;this.defaults=defaults;Arrays.fill(cache,(byte)c.get("cached_height").getAsInt());for(JsonElement e:c.getAsJsonArray("blocks")){JsonArray b=e.getAsJsonArray();voxels.put(new Pos(b.get(0).getAsInt(),b.get(1).getAsInt(),b.get(2).getAsInt()),b.get(3).getAsInt());}}
  public float temperature(int x,int y,int z){events.add(row("biome",x,y,z));events.add(row("temperature",x,y,z,input.get("temperature_bits").getAsString()));return Float.intBitsToFloat((int)Long.parseLong(input.get("temperature_bits").getAsString(),16));}
  public int blockLight(int x,int y,int z){int light=input.get("light").getAsInt();events.add(row("light",0,x,y,z,light));return light;}
  public int block(int x,int y,int z){int state=voxels.getOrDefault(new Pos(x,y,z),0);hash.update(ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN).putInt(x).putInt(y).putInt(z).putInt(state).array());probes.add(row(x,y,z,state));readCount++;return state;}
  public int blockType(int x,int y,int z){return block(x,y,z)&65535;}
  public int defaultState(int tile){return tile==78?78|(input.get("default_snow_meta").getAsInt()<<16):defaults.get(tile).get("state").getAsInt();}
  public boolean solidRender(int state){return defaults.get(state&65535).get("solid_render").getAsBoolean();}
  public boolean specialSnowSupportMaterial(int state){return defaults.get(state&65535).get("special_material").getAsBoolean();}
  public boolean materialBlocksMotion(int state){return defaults.get(state&65535).get("blocks_motion").getAsBoolean();}
  public int maximumSnowDepth(int x,int y,int z){return input.get("maximum_depth").getAsInt();}
  public boolean snowEnabled(){boolean enabled=input.get("enabled").getAsBoolean();events.add(row("rule",18,enabled));return enabled;}
  public Integer chunkPrecipitationHeight(int cx,int cz,int x,int y,int z){boolean present=input.get("chunk_present").getAsBoolean();events.add(row("chunk",cx,cz,present));return present?Ps4PrecipitationHeight.height(new Ps4PrecipitationHeight.Access(){
   public int cachedHeight(int index){return cache[index]&255;}
   public void cacheHeight(int index,int value){cache[index]=(byte)value;}
   public boolean sectionEmpty(int sectionY){boolean empty=(input.get("section_mask").getAsInt()&(1<<(sectionY/16)))==0;events.add(row("section",sectionY,empty));return empty;}
   public int primary(int px,int py,int pz){return block(px,py,pz);}
   public int secondary(int px,int py,int pz){return 0;}
   public boolean blocksMotion(int state){return defaults.get(state&65535).get("blocks_motion").getAsBoolean();}
   public boolean liquid(int state){return defaults.get(state&65535).get("liquid").getAsBoolean();}
  },x,z):null;}
  public void setBlock(int x,int y,int z,int state,int flags){boolean accepted=input.get("write_mode").getAsString().equals("all");attempts.add(row(x,y,z,state,flags,accepted));if(accepted){writes.add(row(x,y,z,state,flags));voxels.put(new Pos(x,y,z),state);}}
 }
}

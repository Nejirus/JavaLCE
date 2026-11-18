package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4StateIndexOracleTest {
 @Test public void originalDefinitionOrderNullSkipsIdStoresAndTableCollisionsMatch()throws Exception{
  JsonObject fixture;try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-state-index.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  int count=0,stateIds=0;
  for(JsonElement element:fixture.getAsJsonArray("cases")){
   JsonObject c=element.getAsJsonObject();World world=new World();Map<Integer,State> values=new LinkedHashMap<>();List<Block> blocks=new ArrayList<>();
   for(JsonElement value:c.getAsJsonArray("values")){JsonObject v=value.getAsJsonObject();values.put(v.get("token").getAsInt(),new State(v.get("token").getAsInt(),v.get("data").getAsInt()));}
   for(JsonElement value:c.getAsJsonArray("blocks")){JsonObject b=value.getAsJsonObject();List<State> states=new ArrayList<>();for(JsonElement token:b.getAsJsonArray("states"))states.add(token.isJsonNull()?null:values.get(token.getAsInt()));blocks.add(new Block(blocks.size(),b.get("id").getAsInt(),states));}
   Ps4StateIds.index(world,blocks);JsonArray actual=new JsonArray();for(State state:values.values()){JsonArray row=new JsonArray();row.add(state.token);row.add(state.id);actual.add(row);}String label="original state indexing "+c.get("name");assertEquals(label+" state ID stores",c.getAsJsonArray("state_ids"),actual);
   MessageDigest table=MessageDigest.getInstance("SHA-256");for(State state:world.table)write(table,state==null?-1:state.token);assertEquals(label+" complete table",c.get("table_sha256").getAsString(),hex(table.digest()));assertEquals(label+" metadata/registry query order",c.get("events_sha256").getAsString(),hex(world.events.digest()));count++;stateIds+=values.size();
  }
  assertEquals(135,count);assertEquals(24953,stateIds);
 }
 private static void write(MessageDigest digest,int... values){for(int value:values)for(int i=0;i<4;i++)digest.update((byte)(value>>>(i*8)));}
 private static String hex(byte[] bytes){char[] out=new char[bytes.length*2],digits="0123456789abcdef".toCharArray();for(int i=0;i<bytes.length;i++){out[i*2]=digits[(bytes[i]>>>4)&15];out[i*2+1]=digits[bytes[i]&15];}return new String(out);}
 private static class State {final int token,data;int id=0x12345678;State(int token,int data){this.token=token;this.data=data;}}
 private record Block(int slot,int id,List<State> states){}
 private static class World implements Ps4StateIds.MutableAccess<State,Block> {
  final State[] table=new State[5856];final MessageDigest events;
  World()throws Exception{events=MessageDigest.getInstance("SHA-256");}
  public Iterable<State> states(Block block){return block.states;}
  public void storeStateId(State state,int id){state.id=id;}
  public void cacheState(int id,State state){table[id]=state;}
  public int blockId(Block block){write(events,1,block.slot,block.id);return block.id;}
  public int data(Block block,State state){write(events,2,block.slot,state.data);return state.data;}
  public State cachedState(int id){throw new AssertionError("indexing queried cache");}
  public Block blockAt(int index){throw new AssertionError("indexing queried block index");}
  public State fromData(Block block,int data){throw new AssertionError("indexing used metadata conversion");}
  public Block blockOf(State state){throw new AssertionError("indexing queried state ownership");}
 }
}

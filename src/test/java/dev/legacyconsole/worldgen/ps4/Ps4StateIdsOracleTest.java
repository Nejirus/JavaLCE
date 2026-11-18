package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4StateIdsOracleTest {
 @Test public void originalTableFirstLookupFallbackAndDistinctItemCodecsMatch()throws Exception{
  JsonObject fixture;try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-state-ids.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  World world=new World();int lookups=0,encodings=0;MessageDigest digest=MessageDigest.getInstance("SHA-256");
  for(JsonElement context:fixture.getAsJsonArray("contexts")){
   world.cache.clear();for(JsonElement element:context.getAsJsonObject().getAsJsonArray("cache")){JsonObject entry=element.getAsJsonObject();int slot=entry.get("slot").getAsInt();world.cache.put(entry.get("id").getAsInt(),new Value(1,new Block(slot,slot),entry.get("data").getAsInt()));}
   for(JsonElement element:context.getAsJsonObject().getAsJsonArray("ranges")){
    JsonObject range=element.getAsJsonObject();int start=range.get("start").getAsInt();
    for(String operation:new String[]{"table","item"}){
     digest.reset();world.events.reset();for(int id=start;id<start+256;id++){
      Value value=operation.equals("table")?Ps4StateIds.byId(world,id):Ps4StateIds.fromItem(world,id);write(digest,value.kind,value.block.slot,value.data);lookups++;
     }
     String label="original "+operation+" selector start="+start+" cache="+world.cache.size();assertEquals(label,range.get(operation+"_sha256").getAsString(),hex(digest.digest()));assertEquals(label+" conversion calls",range.get(operation+"_events_sha256").getAsString(),hex(world.events.digest()));
    }
   }
  }
  for(JsonElement element:fixture.getAsJsonArray("encodings")){
   JsonObject c=element.getAsJsonObject();Block block=new Block(0,c.get("registry_id").getAsInt());Value state=new Value(2,block,c.get("data").getAsInt());String label="original item encode ID="+block.id+" data="+state.data;world.events.reset();assertEquals(label,c.get("item_value").getAsInt(),Ps4StateIds.item(world,state));assertEquals(label+" query order",c.get("item_events_sha256").getAsString(),hex(world.events.digest()));world.events.reset();assertEquals(label+" block base",c.get("block_base").getAsInt(),Ps4StateIds.blockBase(world,block));assertEquals(label+" base query order",c.get("base_events_sha256").getAsString(),hex(world.events.digest()));encodings++;
  }
  assertEquals(262144,lookups);assertEquals(117,encodings);
 }
 private static void write(MessageDigest digest,int... values){for(int value:values)for(int i=0;i<4;i++)digest.update((byte)(value>>>(i*8)));}
 private static String hex(byte[] bytes){char[] out=new char[bytes.length*2],digits="0123456789abcdef".toCharArray();for(int i=0;i<bytes.length;i++){out[i*2]=digits[(bytes[i]>>>4)&15];out[i*2+1]=digits[bytes[i]&15];}return new String(out);}
 private record Block(int slot,int id){}
 private record Value(int kind,Block block,int data){}
 private static class World implements Ps4StateIds.Access<Value,Block> {
  final Map<Integer,Value> cache=new HashMap<>();final MessageDigest events;
  World()throws Exception{events=MessageDigest.getInstance("SHA-256");}
  public Value cachedState(int id){return cache.get(id);}
  public Block blockAt(int index){return new Block(index,index);}
  public Value fromData(Block block,int data){write(events,0,block.slot,data);return new Value(0,block,data);}
  public Block blockOf(Value state){return state.block;}
  public int blockId(Block block){write(events,1,block.slot,block.id);return block.id;}
  public int data(Block block,Value state){write(events,2,block.slot,state.data);return state.data;}
 }
}

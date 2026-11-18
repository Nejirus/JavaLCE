package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4BlockHeightsOracleTest {
 @Test public void originalConstructorMaterialsAndStateGettersFeedBothHeightLayers()throws Exception{
  int cases=0,events=0;Set<Integer> primaryIds=new HashSet<>(),secondaryIds=new HashSet<>();Ps4BlockMaterials materials=new Ps4BlockMaterials();
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-block-heights.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);JsonReader reader=new JsonReader(new InputStreamReader(gzip,StandardCharsets.UTF_8))){
   reader.beginObject();while(reader.hasNext()){
    if(!reader.nextName().equals("cases")){reader.skipValue();continue;}
    reader.beginArray();while(reader.hasNext()){
     JsonObject c=new JsonParser().parse(reader).getAsJsonObject();World world=new World(c,materials);JsonArray pos=c.getAsJsonArray("position"),actual=new JsonArray();int x=pos.get(0).getAsInt(),z=pos.get(2).getAsInt();for(int i=0;i<2;i++){JsonArray result=new JsonArray();result.add(x);result.add(Ps4PrecipitationHeight.height(world,x,z));result.add(z);actual.add(result);}
     String label="original material height primary="+c.get("primary")+" secondary="+c.get("secondary");assertEquals(label,c.getAsJsonArray("results"),actual);assertEquals(label+" event count",c.get("events").getAsInt(),world.events);assertEquals(label+" query order",c.get("event_sha256").getAsString(),hex(world.eventHash.digest()));assertEquals(label+" complete cache",c.get("cache_sha256").getAsString(),hex(MessageDigest.getInstance("SHA-256").digest(world.cache)));primaryIds.add(c.getAsJsonArray("primary").get(1).getAsInt());secondaryIds.add(c.getAsJsonArray("secondary").get(1).getAsInt());cases++;events+=world.events;
    }reader.endArray();
   }reader.endObject();
  }
  assertEquals(4140,cases);assertEquals(2886826,events);assertEquals(345,primaryIds.size());assertEquals(primaryIds,secondaryIds);
 }
 private static String hex(byte[] bytes){char[] out=new char[bytes.length*2],digits="0123456789abcdef".toCharArray();for(int i=0;i<bytes.length;i++){out[i*2]=digits[(bytes[i]>>>4)&15];out[i*2+1]=digits[bytes[i]&15];}return new String(out);}
 private static class World implements Ps4PrecipitationHeight.Access {
  final JsonObject input;final Ps4BlockMaterials materials;final byte[] cache=new byte[256];final MessageDigest eventHash;int events;
  World(JsonObject input,Ps4BlockMaterials materials)throws Exception{this.input=input;this.materials=materials;eventHash=MessageDigest.getInstance("SHA-256");Arrays.fill(cache,(byte)128);JsonArray pos=input.getAsJsonArray("position");cache[((pos.get(2).getAsInt()&15)<<4)|(pos.get(0).getAsInt()&15)]=(byte)input.get("cached").getAsInt();}
  private void event(int a,int b,int c,int d,int e){events++;for(int value:new int[]{a,b,c,d,e})for(int i=0;i<4;i++)eventHash.update((byte)(value>>>(i*8)));}
  public int cachedHeight(int index){return cache[index]&255;}
  public void cacheHeight(int index,int value){cache[index]=(byte)value;}
  public boolean sectionEmpty(int y){boolean empty=(input.get("section_mask").getAsInt()&(1<<(y/16)))==0;event(0,y,empty?1:0,0,0);return empty;}
  private int sample(int layer,int x,int y,int z){JsonArray profile=input.getAsJsonArray(layer==1?"primary":"secondary");int value=y<=profile.get(0).getAsInt()?profile.get(1).getAsInt():0;event(layer,x,y,z,value);return value;}
  public int primary(int x,int y,int z){return sample(1,x,y,z);}
  public int secondary(int x,int y,int z){return sample(2,x,y,z);}
  public boolean blocksMotion(int state){return materials.forRegisteredBlock(state).blocksMotion();}
  public boolean liquid(int state){return materials.forRegisteredBlock(state).liquid();}
 }
}

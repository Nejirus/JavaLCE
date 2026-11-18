package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4FreezingOracleTest {
 @Test public void originalFreezeTemperatureHeightLightIdentityLevelsAndQueryOrderMatch()throws Exception{
  JsonObject fixture;try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-freezing.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  assertEquals("3e19999a",fixture.get("temperature_threshold_bits").getAsString());int accepted=0,rejected=0;
  for(JsonElement e:fixture.getAsJsonArray("cases")){
   JsonObject c=e.getAsJsonObject();World w=new World(c);JsonArray p=c.getAsJsonArray("position");boolean actual=Ps4SnowAndIce.canFreeze(w,p.get(0).getAsInt(),p.get(1).getAsInt(),p.get(2).getAsInt());String label="original freezing "+c.get("state")+" temperature="+c.get("temperature_bits")+" light="+c.get("light")+" position="+p;
   assertEquals(label,c.get("result").getAsBoolean(),actual);assertEquals(label+" queries",c.getAsJsonArray("events"),w.events);assertEquals(label+" block reads",c.get("read_count").getAsInt(),w.readCount);assertEquals(label+" block read order",c.get("read_sha256").getAsString(),hex(w.hash.digest()));if(actual)accepted++;else rejected++;
  }
  assertTrue(accepted>100&&rejected>1000);assertTrue(fixture.getAsJsonArray("registered_ids").size()>300);assertTrue(fixture.getAsJsonArray("cases").size()>15000);
 }
 private static String hex(byte[] bytes){StringBuilder s=new StringBuilder();for(byte b:bytes)s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();}
 private static JsonArray row(Object... values){JsonArray r=new JsonArray();for(Object v:values)if(v instanceof Number n)r.add(n);else r.add(v.toString());return r;}
 private static class World implements Ps4SnowAndIce.Access {
  final JsonObject input;final JsonArray events=new JsonArray();final MessageDigest hash=MessageDigest.getInstance("SHA-256");int readCount;
  World(JsonObject c)throws Exception{input=c;}
  public float temperature(int x,int y,int z){events.add(row("biome",x,y,z));events.add(row("temperature",x,y,z,input.get("temperature_bits").getAsString()));return Float.intBitsToFloat((int)Long.parseLong(input.get("temperature_bits").getAsString(),16));}
  public int blockLight(int x,int y,int z){int light=input.get("light").getAsInt();events.add(row("light",0,x,y,z,light));return light;}
  public int block(int x,int y,int z){int state=input.get("state").getAsInt();ByteBuffer b=ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN).putInt(x).putInt(y).putInt(z).putInt(state);hash.update(b.array());readCount++;return state;}
 }
}

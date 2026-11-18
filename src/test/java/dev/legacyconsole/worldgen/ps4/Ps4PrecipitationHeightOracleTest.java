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

public class Ps4PrecipitationHeightOracleTest {
 @Test public void originalTwoLayerScanHighestSectionAndRepeatedCacheReadsMatch()throws Exception{
  int cases=0,events=0;Set<Integer> results=new HashSet<>();
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-precipitation.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);JsonReader reader=new JsonReader(new InputStreamReader(gzip,StandardCharsets.UTF_8))){
   reader.beginObject();while(reader.hasNext()){
    if(!reader.nextName().equals("cases")){reader.skipValue();continue;}
    reader.beginArray();while(reader.hasNext()){
     JsonObject c=new JsonParser().parse(reader).getAsJsonObject();World w=new World(c);JsonArray p=c.getAsJsonArray("position"),actual=new JsonArray();int x=p.get(0).getAsInt(),z=p.get(2).getAsInt();
     for(int i=0;i<2;i++){int height=Ps4PrecipitationHeight.height(w,x,z);actual.add(row(x,height,z));results.add(height);}
     String label="original precipitation pos="+p+" cache="+c.get("cached")+" sections="+c.get("section_mask")+" primary="+c.get("primary")+" secondary="+c.get("secondary");assertEquals(label+" repeated heights",c.getAsJsonArray("results"),actual);assertEquals(label+" queries/classifiers",c.getAsJsonArray("events"),w.events);assertEquals(label+" complete cache mutation",c.get("cache_sha256").getAsString(),hex(MessageDigest.getInstance("SHA-256").digest(w.cache)));cases++;events+=w.events.size();
    }reader.endArray();
   }reader.endObject();
  }
  assertEquals(2432,cases);assertEquals(383412,events);assertTrue(results.containsAll(Set.of(0,254,255)));
 }
 private static String hex(byte[] bytes){StringBuilder s=new StringBuilder();for(byte b:bytes)s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();}
 private static JsonArray row(Object... values){JsonArray r=new JsonArray();for(Object v:values)if(v instanceof Number n)r.add(n);else if(v instanceof Boolean b)r.add(b);else r.add(v.toString());return r;}
 private static class World implements Ps4PrecipitationHeight.Access {
  final JsonObject input;final JsonArray events=new JsonArray();final byte[] cache=new byte[256];
  World(JsonObject c){input=c;Arrays.fill(cache,(byte)128);JsonArray p=c.getAsJsonArray("position");cache[((p.get(2).getAsInt()&15)<<4)|(p.get(0).getAsInt()&15)]=(byte)c.get("cached").getAsInt();}
  public int cachedHeight(int index){return cache[index]&255;}
  public void cacheHeight(int index,int value){cache[index]=(byte)value;}
  public boolean sectionEmpty(int y){boolean empty=(input.get("section_mask").getAsInt()&(1<<(y/16)))==0;events.add(row("section",y,empty));return empty;}
  private int sample(String layer,int x,int y,int z){JsonArray profile=input.getAsJsonArray(layer);int value=y<=profile.get(0).getAsInt()?profile.get(1).getAsInt():0;events.add(row(layer,x,y,z,value));return value;}
  public int primary(int x,int y,int z){return sample("primary",x,y,z);}
  public int secondary(int x,int y,int z){return sample("secondary",x,y,z);}
  public boolean blocksMotion(int state){boolean value=(state&1)!=0;events.add(row("blocks_motion",state,value));return value;}
  public boolean liquid(int state){boolean value=(state&2)!=0;events.add(row("liquid",state,value));return value;}
 }
}

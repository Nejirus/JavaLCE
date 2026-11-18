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

public class Ps4HeightInvalidationOracleTest {
 @Test public void originalChunkWriteInvalidationMatchesIncludingUnchangedWriteFailures()throws Exception{
  int count=0,failedWrites=0,successfulWrites=0;Set<Integer> initialBytes=new HashSet<>();MessageDigest digest=MessageDigest.getInstance("SHA-256");
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-height-invalidation.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);JsonReader reader=new JsonReader(new InputStreamReader(gzip,StandardCharsets.UTF_8))){
   reader.beginObject();while(reader.hasNext()){
    if(!reader.nextName().equals("cases")){reader.skipValue();continue;}
    reader.beginArray();while(reader.hasNext()){
     JsonObject c=new JsonParser().parse(reader).getAsJsonObject();JsonArray pos=c.getAsJsonArray("position");int x=pos.get(0).getAsInt(),y=pos.get(1).getAsInt(),z=pos.get(2).getAsInt(),cached=c.get("cached").getAsInt(),index=((z&15)<<4)|(x&15);Cache cache=new Cache();cache.values[index]=(byte)cached;
     Ps4PrecipitationHeight.invalidateBeforeWrite(cache,x,y,z);String label="original chunk write pos="+pos+" cache="+cached+" fast="+c.get("fast");assertEquals(label,c.get("cache_after").getAsInt(),cache.values[index]&255);assertEquals(label+" complete cache",c.get("cache_sha256").getAsString(),hex(digest.digest(cache.values)));initialBytes.add(cached);if(c.get("write_result").getAsBoolean())successfulWrites++;else failedWrites++;count++;
    }reader.endArray();
   }reader.endObject();
  }
  assertEquals(48340,count);assertEquals(24170,failedWrites);assertEquals(24170,successfulWrites);assertEquals(256,initialBytes.size());
 }
 private static String hex(byte[] bytes){char[] out=new char[bytes.length*2],digits="0123456789abcdef".toCharArray();for(int i=0;i<bytes.length;i++){out[i*2]=digits[(bytes[i]>>>4)&15];out[i*2+1]=digits[bytes[i]&15];}return new String(out);}
 private static class Cache implements Ps4PrecipitationHeight.Access {
  final byte[] values=new byte[256];Cache(){Arrays.fill(values,(byte)128);}
  public int cachedHeight(int index){return values[index]&255;}
  public void cacheHeight(int index,int value){values[index]=(byte)value;}
  public boolean sectionEmpty(int y){throw new AssertionError("invalidation queried sections");}
  public int primary(int x,int y,int z){throw new AssertionError("invalidation queried primary blocks");}
  public int secondary(int x,int y,int z){throw new AssertionError("invalidation queried secondary blocks");}
  public boolean blocksMotion(int state){throw new AssertionError("invalidation queried materials");}
  public boolean liquid(int state){throw new AssertionError("invalidation queried materials");}
 }
}

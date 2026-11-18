package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4SecondaryStorageOracleTest {
 @Test public void originalStoredSecondaryAndPrimaryHighBitWaterSelectionMatch()throws Exception{
  int cases=0,samples=0,waterShortcuts=0,differentLayers=0;Ps4CompressedSectionsOracleTest.Storage storage=new Ps4CompressedSectionsOracleTest.Storage();MessageDigest digest=MessageDigest.getInstance("SHA-256");
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-secondary-storage.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);JsonReader reader=new JsonReader(new InputStreamReader(gzip,StandardCharsets.UTF_8))){
   reader.beginObject();while(reader.hasNext()){
    if(!reader.nextName().equals("cases")){reader.skipValue();continue;}
    reader.beginArray();while(reader.hasNext()){
     JsonObject c=new JsonParser().parse(reader).getAsJsonObject();storage.load(c);JsonArray origin=c.getAsJsonArray("read_origin");int ox=origin.get(0).getAsInt(),oy=origin.get(1).getAsInt(),oz=origin.get(2).getAsInt();digest.reset();
     for(int x=0;x<4;x++)for(int z=0;z<4;z++)for(int y=0;y<4;y++){
      int primary=Ps4CompressedSections.value(storage,ox+x,oy+y,oz+z,null),stored=Ps4CompressedSections.storedSecondary(storage,ox+x,oy+y,oz+z),selected=Ps4CompressedSections.secondary(storage,ox+x,oy+y,oz+z);for(int value:new int[]{primary,stored,selected}){digest.update((byte)value);digest.update((byte)(value>>>8));}if(selected!=stored)waterShortcuts++;if(primary!=stored)differentLayers++;samples++;
     }
     assertEquals("original secondary lookup case="+cases+" origin="+origin,c.get("read_sha256").getAsString(),hex(digest.digest()));assertEquals(64,c.get("samples").getAsInt());cases++;
    }reader.endArray();
   }reader.endObject();
  }
  assertEquals(27840,cases);assertEquals(1781760,samples);assertTrue(waterShortcuts>0);assertTrue(differentLayers>0);
 }
 private static String hex(byte[] bytes){char[] out=new char[bytes.length*2],digits="0123456789abcdef".toCharArray();for(int i=0;i<bytes.length;i++){out[i*2]=digits[(bytes[i]>>>4)&15];out[i*2+1]=digits[bytes[i]&15];}return new String(out);}
}

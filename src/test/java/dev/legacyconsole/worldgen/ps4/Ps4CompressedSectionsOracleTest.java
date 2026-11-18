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

public class Ps4CompressedSectionsOracleTest {
 @Test public void originalCompressedHeaderModesPalettesBitplanesAndOptionalTopMatch()throws Exception{
  int cases=0,unallocated=0;Set<Integer> modes=new HashSet<>();Storage storage=new Storage();
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-compressed-sections.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);JsonReader reader=new JsonReader(new InputStreamReader(gzip,StandardCharsets.UTF_8))){
   reader.beginObject();while(reader.hasNext()){
    if(!reader.nextName().equals("cases")){reader.skipValue();continue;}
    reader.beginArray();while(reader.hasNext()){
     JsonObject c=new JsonParser().parse(reader).getAsJsonObject();storage.load(c);int[] top={0x12345678};int sectionY=c.get("section_y").getAsInt();boolean result=Ps4CompressedSections.empty(storage,sectionY,c.get("has_output").getAsBoolean()?value->top[0]=value:null);String label="original compressed section case="+cases+" y="+sectionY+" allocation="+c.get("allocated")+" offset="+c.get("section_offset")+" output="+c.get("has_output");assertEquals(label+" emptiness",c.get("empty").getAsBoolean(),result);assertEquals(label+" optional upper Y",c.get("reported_top").getAsInt(),top[0]);for(JsonElement header:c.getAsJsonArray("headers"))modes.add(header.getAsInt()>>>13);if(c.get("allocated").getAsInt()==0)unallocated++;cases++;
    }reader.endArray();
   }reader.endObject();
  }
  assertEquals(25792,cases);assertEquals(64,unallocated);assertEquals(Set.of(0,1,2,3,4,5,6,7),modes);
 }
 @Test public void originalPackedValuesAndOptionalConstantByteMatchEverySubcubeCell()throws Exception{
  int cases=0,samples=0;Storage storage=new Storage();MessageDigest digest=MessageDigest.getInstance("SHA-256");
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-compressed-sections.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);JsonReader reader=new JsonReader(new InputStreamReader(gzip,StandardCharsets.UTF_8))){
   reader.beginObject();while(reader.hasNext()){
    if(!reader.nextName().equals("cases")){reader.skipValue();continue;}
    reader.beginArray();while(reader.hasNext()){
     JsonObject c=new JsonParser().parse(reader).getAsJsonObject();storage.load(c);JsonArray origin=c.getAsJsonArray("read_origin");int ox=origin.get(0).getAsInt(),oy=origin.get(1).getAsInt(),oz=origin.get(2).getAsInt();boolean output=c.get("has_output").getAsBoolean();digest.reset();
     for(int x=0;x<4;x++)for(int z=0;z<4;z++)for(int y=0;y<4;y++){
      int[] constant={0xa5};int value=Ps4CompressedSections.value(storage,ox+x,oy+y,oz+z,output?flag->constant[0]=flag:null);digest.update((byte)value);digest.update((byte)(value>>>8));digest.update((byte)constant[0]);digest.update((byte)(constant[0]>>>8));samples++;
     }
     assertEquals("original packed lookup case="+cases+" origin="+origin+" output="+output,c.get("read_sha256").getAsString(),hex(digest.digest()));assertEquals(64,c.get("read_samples").getAsInt());cases++;
    }reader.endArray();
   }reader.endObject();
  }
  assertEquals(25792,cases);assertEquals(1650688,samples);
 }
 private static String hex(byte[] bytes){char[] out=new char[bytes.length*2],digits="0123456789abcdef".toCharArray();for(int i=0;i<bytes.length;i++){out[i*2]=digits[(bytes[i]>>>4)&15];out[i*2+1]=digits[bytes[i]&15];}return new String(out);}
 static final class Storage implements Ps4CompressedSections.Access {
  final byte[] raw=new byte[65536],allocated=new byte[16];final int[] offsets=new int[16];
  void load(JsonObject input){
   Arrays.fill(raw,(byte)0);Arrays.fill(allocated,(byte)0);Arrays.fill(offsets,0);int section=input.get("section_y").getAsInt()/16,offset=input.get("section_offset").getAsInt();allocated[section]=(byte)input.get("allocated").getAsInt();offsets[section]=offset;int base=offset*256,index=0;
   for(JsonElement element:input.getAsJsonArray("headers")){int value=element.getAsInt();raw[base+index++]=(byte)value;raw[base+index++]=(byte)(value>>>8);}
   for(JsonElement element:input.getAsJsonArray("patches")){JsonArray patch=element.getAsJsonArray();int start=base+patch.get(0).getAsInt();String hex=patch.get(1).getAsString();for(int i=0;i<hex.length();i+=2)raw[start+i/2]=(byte)((Character.digit(hex.charAt(i),16)<<4)|Character.digit(hex.charAt(i+1),16));}
  }
  public boolean allocated(int section){return allocated[section]!=0;}
  public int sectionOffset(int section){return offsets[section];}
  public int unsignedByte(int offset){return raw[offset]&255;}
 }
}

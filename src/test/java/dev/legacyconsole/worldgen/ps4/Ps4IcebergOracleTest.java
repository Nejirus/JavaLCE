package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4IcebergOracleTest {
 @Test public void originalCompleteIcebergShapeCleanupCarvingReuseAndRngMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-icebergs.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  Ps4IcebergFeature feature=new Ps4IcebergFeature();fields("constructor",fixture.getAsJsonObject("constructor_fields"),feature.snapshot());JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(260,cases.size());int writes=0,ellipses=0,reuses=0,blue=0,air=0,water=0;
  for(JsonElement e:cases){
   JsonObject c=e.getAsJsonObject();World w=new World(c);int[] p=w.origin;if(!c.get("reuse").getAsBoolean())feature=new Ps4IcebergFeature();else reuses++;
   String label="original iceberg seed="+c.get("seed")+" pos="+c.get("position")+" sea="+c.get("sea_level")+" profile="+w.profile+" reuse="+c.get("reuse");fields(label+" before",c.getAsJsonObject("before"),feature.snapshot());Random random=new Random(c.get("seed").getAsLong());
   boolean result=feature.place(w,random,p[0],p[1],p[2]);assertEquals(label+" result",c.get("result").getAsBoolean(),result);fields(label+" after",c.getAsJsonObject("after"),feature.snapshot());
   try{Ps4FloraOracleTest.compare(label,c,w);assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());}
   catch(AssertionError error){JsonObject d=new JsonObject();d.add("case",c);d.add("actual_writes",w.writes);d.add("actual_probes",w.probes);Files.writeString(Path.of("build/ps4-iceberg-mismatch.json"),d.toString());throw error;}
   writes+=w.writes.size();if(feature.snapshot().ellipse())ellipses++;if(feature.snapshot().state()==273)blue++;
   for(JsonElement row:w.writes){int state=row.getAsJsonArray().get(3).getAsInt();if(state==0)air++;if(state==9)water++;}
  }
  assertTrue("both shape branches",ellipses>10&&ellipses<cases.size()-10);assertTrue("retained feature state",reuses>5);assertTrue("rare blue-ice choice",blue>0);assertTrue("substantial geometry",writes>100000);assertTrue("smoothing and above-water carving",air>1000);assertTrue("underwater carving",water>1000);
 }
 private static void fields(String label,JsonObject c,Ps4IcebergFeature.State s){assertEquals(label+" snow",c.get("snow").getAsBoolean(),s.snow());assertEquals(label+" ellipse",c.get("ellipse").getAsBoolean(),s.ellipse());assertEquals(label+" angle",Long.parseUnsignedLong(c.get("angle_bits").getAsString(),16),s.angleBits());assertEquals(label+" major",c.get("major").getAsInt(),s.major());assertEquals(label+" minor",c.get("minor").getAsInt(),s.minor());assertEquals(label+" inset",c.get("inset").getAsInt(),s.inset());assertEquals(label+" state",c.get("state").isJsonNull()?0:c.get("state").getAsInt(),s.state());}
 private static JsonArray row(int... values){JsonArray r=new JsonArray();for(int value:values)r.add(value);return r;}
 private static class World extends Ps4FloraOracleTest.World implements Ps4IceFeatures.Access {
  final int sea;final JsonArray probes=new JsonArray();
  World(JsonObject c)throws Exception{super(c);sea=c.get("sea_level").getAsInt();}
  @Override int initial(int x,int y,int z){
   if(profile.equals("empty"))return 0;if(profile.equals("stone"))return 1;
   if(profile.equals("mixed"))return new int[]{0,1,3,2,8,9,79,78,80,174,273}[Math.floorMod(x*7+y*3+z*11,11)];
   if(profile.equals("caps")){if(y==sea+1)return 78;if(y==sea)return Math.floorMod(x+z,3)==0?80:174;}
   return y<=sea?9:0;
  }
  @Override public int block(int x,int y,int z){int value=super.block(x,y,z);probes.add(row(x,y,z,value));return value;}
  @Override public int seaLevel(){return sea;}
 }
}

package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4CoralCrustOracleTest {
 @Test public void originalCrustDispatchOffsetsCopiedRandomAndTemplateStreamsMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-coral-crust.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  assertEquals(44,fixture.getAsJsonArray("offsets").size());
  for(JsonElement e:fixture.getAsJsonArray("offsets")){JsonObject c=e.getAsJsonObject();assertArrayEquals(array(c.getAsJsonArray("offset")),Ps4CoralCrustFeature.offset(c.get("kind").getAsString().equals("outcropping"),c.get("index").getAsInt(),c.get("rotation").getAsInt()));}
  assertEquals(11,fixture.getAsJsonArray("templates").size());for(int i=0;i<5;i++)assertEquals(177+i,fixture.getAsJsonArray("crust_ids").get(i).getAsInt());for(int i=0;i<6;i++)assertEquals(182+i,fixture.getAsJsonArray("outcropping_ids").get(i).getAsInt());
  Set<Integer> templates=new HashSet<>();Set<String> orders=new HashSet<>();int accepted=0,rejected=0;
  for(JsonElement e:fixture.getAsJsonArray("cases")){
   JsonObject c=e.getAsJsonObject();World w=new World(c);Ps4Random random=new Ps4Random(c.get("seed").getAsLong());int[] p=w.origin;
   boolean result=Ps4CoralCrustFeature.place(w,random,p[0],p[1],p[2],array(c.getAsJsonArray("rotation_order")));
   String label="original coral crust seed="+c.get("seed")+" pos="+c.get("position")+" profile="+w.profile+" order="+c.get("rotation_order")+" geometry="+c.get("geometry_draws")+" post="+c.get("post_draws");
   assertEquals(label+" result",c.get("result").getAsBoolean(),result);Ps4CoralsOracleTest.compare(label,c,w);assertEquals(label+" template calls and independent random draws",c.getAsJsonArray("events"),w.events);assertEquals(label+" following population RNG",c.get("random_after").getAsLong(),random.nextLong());
   if(result)accepted++;else rejected++;orders.add(c.get("rotation_order").toString());for(JsonElement event:w.events){JsonArray r=event.getAsJsonArray();if(r.get(0).getAsString().equals("template"))templates.add(r.get(1).getAsInt());}
  }
  assertEquals(4336,fixture.getAsJsonArray("cases").size());assertEquals(11,templates.size());assertEquals(24,orders.size());assertTrue(accepted>100&&rejected>100);
 }
 private static int[] array(JsonArray values){int[] result=new int[values.size()];for(int i=0;i<result.length;i++)result[i]=values.get(i).getAsInt();return result;}
 private static class World extends Ps4CoralsOracleTest.World implements Ps4CoralCrustFeature.Access {
  final JsonArray events=new JsonArray();
  World(JsonObject c)throws Exception{super(c);}
  @Override int initial(int x,int y,int z){
   if(!profile.startsWith("crust_"))return super.initial(x,y,z);
   if(profile.equals("crust_air")||y<0||y>=256)return 0;
   int floor=profile.equals("crust_shallow")?52:profile.equals("crust_deep")?30:48;
   if(profile.equals("crust_steps"))floor+=(int)Math.floorMod(3L*x+7L*z,7)-3;
   if(y<floor)return 1;if(y==floor)return profile.equals("crust_live")?259:profile.equals("crust_dead")?259|(8<<16):1;return y<64?9:0;
  }
  private JsonArray event(String kind,int... values){JsonArray row=new JsonArray();row.add(kind);for(int value:values)row.add(value);events.add(row);return row;}
  public void placeCoralTemplate(int id,int x,int y,int z,int rotation,float integrity,int flags){
   event("template",id);JsonArray row=event("place",id,x,y,z,rotation);row.add(integrity);row.add(flags);Ps4Random random=Ps4TemplateRandom.fromPosition(x,z);JsonArray draws=new JsonArray();for(int i=0;i<input.get("geometry_draws").getAsInt();i++)draws.add(random.nextLong());row.add(draws);
  }
  public void decorateCoralTemplate(int id,int x,int y,int z,int rotation,Ps4Random random){JsonArray row=event("post",id,x,y,z,rotation),draws=new JsonArray();for(int i=0;i<input.get("post_draws").getAsInt();i++)draws.add(random.nextLong());row.add(draws);}
 }
}

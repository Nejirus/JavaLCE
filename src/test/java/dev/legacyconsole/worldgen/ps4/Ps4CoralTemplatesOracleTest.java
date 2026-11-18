package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4CoralTemplatesOracleTest {
 @Test public void originalSortedTemplatesAndCompleteCrustFeatureMatchInSharedWorld()throws Exception{
  Map<Integer,String> paths=new HashMap<>();List<Map<Integer,Ps4CoralTemplate>> contexts=new ArrayList<>();int cases=0,writes=0,complete=0;Set<Integer> placed=new HashSet<>();boolean rejected=false,unavailable=false;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-coral-templates.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);JsonReader reader=new JsonReader(new InputStreamReader(gzip,StandardCharsets.UTF_8))){
   reader.beginObject();
   while(reader.hasNext()){
    switch(reader.nextName()){
     case "templates"->{JsonArray templates=new JsonParser().parse(reader).getAsJsonArray();for(JsonElement e:templates){JsonObject t=e.getAsJsonObject();paths.put(t.get("id").getAsInt(),t.get("path").getAsString());}}
     case "contexts"->{JsonArray values=new JsonParser().parse(reader).getAsJsonArray();for(JsonElement e:values){
      JsonObject c=e.getAsJsonObject();Ps4Random shared=Ps4Random.fromClockTick(c.get("clock_tick").getAsLong());Map<Integer,Ps4CoralTemplate> templates=new HashMap<>();
      for(JsonElement id:c.getAsJsonArray("load_order")){int index=id.getAsInt();try(InputStream nbt=getClass().getResourceAsStream("/assets/legacyconsole/worldgen/ps4/templates/"+paths.get(index)+".nbt")){templates.put(index,Ps4CoralTemplate.read(nbt,shared));}}
      for(JsonElement palette:c.getAsJsonArray("palettes")){JsonObject p=palette.getAsJsonObject();Set<Integer> expected=new HashSet<>(),actual=new HashSet<>();for(JsonElement s:p.getAsJsonArray("states"))expected.add(s.getAsInt());for(var b:templates.get(p.get("id").getAsInt()).blocks())actual.add(b.state());assertEquals("original resolved crust palette",expected,actual);}
      assertEquals("original following Mth stream",c.get("math_random_after").getAsLong(),shared.nextLong());contexts.add(templates);
     }}
     case "cases"->{reader.beginArray();while(reader.hasNext()){
      JsonObject c=new JsonParser().parse(reader).getAsJsonObject();World w=new World(c,contexts.get(c.get("context").getAsInt()));int[] p=w.origin;
      if(c.get("kind").getAsString().equals("geometry")){int id=c.get("template").getAsInt();w.templates.get(id).place(w,p[0],p[1],p[2],c.get("rotation").getAsInt(),2);placed.add(id);}
      else{Ps4Random random=new Ps4Random(c.get("seed").getAsLong());boolean result=Ps4CoralCrustFeature.place(w,random,p[0],p[1],p[2],array(c.getAsJsonArray("rotation_order")));assertEquals("complete crust return",c.get("result").getAsBoolean(),result);assertEquals("complete crust following population RNG",c.get("random_after").getAsLong(),random.nextLong());complete++;}
      String label="original complete crust "+c.get("kind")+" context="+c.get("context")+" template="+c.get("template")+" seed="+c.get("seed")+" position="+c.get("position")+" profile="+w.profile+" writes="+c.get("write_mode")+" columns="+c.get("column_mode")+" empty="+c.get("empty_mode");
      try{
       Ps4CoralsOracleTest.compare(label,c,w);assertEquals(label+" write outcomes",c.getAsJsonArray("write_attempts"),w.attempts);assertEquals(label+" column availability",c.getAsJsonArray("column_queries"),w.columns);assertEquals(label+" chunk lookup",c.getAsJsonArray("template_chunk_queries"),w.templateChunks);assertEquals(label+" placeholder chunks",c.getAsJsonArray("empty_queries"),w.emptyChunks);assertEquals(label+" notifications",c.getAsJsonArray("notifications"),w.notifications);assertEquals(label+" block entity lookup",c.getAsJsonArray("entity_queries"),w.entities);assertEquals(label+" template lookup order",c.getAsJsonArray("events"),w.events);
      }catch(AssertionError error){JsonObject d=new JsonObject();d.add("case",c);d.add("actual_writes",w.writes);d.add("actual_attempts",w.attempts);d.add("actual_columns",w.columns);d.add("actual_notifications",w.notifications);d.add("actual_probes",w.probes);Files.writeString(Path.of("build/ps4-coral-template-mismatch.json"),d.toString());throw error;}
      for(JsonElement a:w.attempts)if(!a.getAsJsonArray().get(5).getAsBoolean())rejected=true;for(JsonElement a:w.columns)if(!a.getAsJsonArray().get(3).getAsBoolean())unavailable=true;cases++;writes+=w.writes.size();
     }reader.endArray();}
     default->reader.skipValue();
    }
   }
   reader.endObject();
  }
  assertEquals(1926,cases);assertEquals(2,contexts.size());assertEquals(11,placed.size());assertTrue(complete>500&&writes>100000);assertTrue(rejected&&unavailable);
 }
 private static int[] array(JsonArray values){int[] result=new int[values.size()];for(int i=0;i<result.length;i++)result[i]=values.get(i).getAsInt();return result;}
 private static JsonArray row(int... values){JsonArray r=new JsonArray();for(int value:values)r.add(value);return r;}
 private static class World extends Ps4CoralsOracleTest.World implements Ps4CoralTemplate.Access,Ps4CoralCrustFeature.Access,Ps4CoralCrustDecoration.Access {
  final Map<Integer,Ps4CoralTemplate> templates;final JsonArray attempts=new JsonArray(),columns=new JsonArray(),templateChunks=new JsonArray(),emptyChunks=new JsonArray(),notifications=new JsonArray(),entities=new JsonArray(),events=new JsonArray();
  World(JsonObject c,Map<Integer,Ps4CoralTemplate> templates)throws Exception{super(c);this.templates=templates;}
  @Override int initial(int x,int y,int z){
   if(profile.equals("crust_air")||y<0||y>=256)return 0;int floor=profile.equals("crust_shallow")?52:profile.equals("crust_deep")?30:48;if(profile.equals("crust_steps"))floor+=(int)Math.floorMod(3L*x+7L*z,7)-3;
   if(y<floor)return 1;if(y==floor)return profile.equals("crust_live")?259:profile.equals("crust_dead")?259|(8<<16):1;return y<64?9:0;
  }
  @Override public void setBlock(int x,int y,int z,int state,int flags){placeCoralBlock(x,y,z,state,flags);}
  public boolean placeCoralBlock(int x,int y,int z,int state,int flags){String mode=input.get("write_mode").getAsString();boolean accepted=mode.equals("all")||mode.equals("checker")&&Math.floorMod(x+y+z,3)!=0;JsonArray a=row(x,y,z,state,flags);a.add(accepted);attempts.add(a);if(accepted)super.setBlock(x,y,z,state,flags);return accepted;}
  public boolean columnAvailable(int x,int y,int z){String mode=input.get("column_mode").getAsString();boolean accepted=mode.equals("all")||mode.equals("checker")&&Math.floorMod((long)x+z,3)!=0;JsonArray c=row(x,y,z);c.add(accepted);columns.add(c);if(!accepted)return false;int cx=x>>4,cz=z>>4;templateChunks.add(row(cx,cz));String emptyMode=input.get("empty_mode").getAsString();boolean empty=emptyMode.equals("all")||emptyMode.equals("checker")&&Math.floorMod((long)cx+cz,3)==0;JsonArray e=row(cx,cz);e.add(empty);emptyChunks.add(e);return !empty;}
  public void notifyNeighbors(int x,int y,int z,int tile,int flags){notifications.add(row(x,y,z,tile,flags));}
  public void templateBlockEntityChanged(int x,int y,int z){entities.add(row(x,y,z));}
  public void placeCoralTemplate(int id,int x,int y,int z,int rotation,float integrity,int flags){assertEquals(1f,integrity,0f);JsonArray e=new JsonArray();e.add("template");e.add(id);events.add(e);templates.get(id).place(this,x,y,z,rotation,flags);}
  public void decorateCoralTemplate(int id,int x,int y,int z,int rotation,Ps4Random random){Ps4CoralCrustDecoration.decorate(this,random,templates.get(id).markers(x,y,z,rotation));}
 }
}

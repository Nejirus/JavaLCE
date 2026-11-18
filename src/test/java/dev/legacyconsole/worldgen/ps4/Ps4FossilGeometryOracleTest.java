package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4FossilGeometryOracleTest {
 @Test public void originalFossilTemplateBlocksIntegrityProbesAndNotificationsMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-fossil-geometry.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  Map<Integer,Ps4FossilTemplate> templates=new HashMap<>();for(JsonElement e:fixture.getAsJsonArray("template_inputs")){JsonObject c=e.getAsJsonObject();try(InputStream input=getClass().getResourceAsStream("/assets/legacyconsole/worldgen/ps4/templates/"+c.get("path").getAsString()+".nbt")){templates.put(c.get("id").getAsInt(),Ps4FossilTemplate.read(input));}}
  for(JsonElement e:fixture.getAsJsonArray("bone_axis_data")){JsonObject c=e.getAsJsonObject();assertEquals(c.get("state").getAsInt()>>>16,c.get("native_data").getAsInt());}
  for(JsonElement e:fixture.getAsJsonArray("palette_inputs")){JsonObject c=e.getAsJsonObject();Set<Integer> expected=new HashSet<>(),actual=new HashSet<>();for(JsonElement s:c.getAsJsonArray("states"))expected.add(s.getAsInt());for(var block:templates.get(c.get("id").getAsInt()).blocks())actual.add(block.state());assertEquals("original canonical fossil palette",expected,actual);}
  JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(2304,cases.size());int writes=0,notifications=0;Set<Integer> states=new HashSet<>();
  for(JsonElement e:cases){
   JsonObject c=e.getAsJsonObject();World world=new World(c,templates);int[] p=world.origin;boolean result=Ps4FossilFeature.place(world,p[0],p[1],p[2]);
   String label="original fossil geometry seed="+c.get("world_seed")+" pos="+c.get("position")+" heights="+world.heights+" voxels="+world.voxels+" rotations="+c.get("rotation_order");
   assertEquals(label,c.get("result").getAsBoolean(),result);Ps4FloraOracleTest.compare(label,c,world);assertEquals(label+" ordered calls",c.getAsJsonArray("events"),world.events);assertEquals(label+" neighbor notifications",c.getAsJsonArray("notification_queries"),world.notifications);assertEquals(label+" no fossil block entities",0,c.getAsJsonArray("entity_queries").size());assertEquals(label+" following chunk RNG",c.get("chunk_random_after").getAsLong(),world.random.nextLong());assertEquals(label+" population RNG untouched",c.get("population_random_after").getAsLong(),new Random(c.get("population_seed").getAsLong()).nextLong());
   writes+=world.writes.size();notifications+=world.notifications.size();for(JsonElement row:world.writes)states.add(row.getAsJsonArray().get(3).getAsInt());
  }
  assertTrue("substantial full original template geometry",writes>50000&&notifications>100000);assertEquals(Set.of(16,216,216|(4<<16),216|(8<<16)),states);
 }
 private static class World extends Ps4FloraOracleTest.World implements Ps4FossilFeature.Access,Ps4FossilTemplate.Access {
  final long seed;final String heights,voxels;final JsonArray order,events=new JsonArray(),notifications=new JsonArray();final Map<Integer,Ps4FossilTemplate> templates;Random random;
  World(JsonObject c,Map<Integer,Ps4FossilTemplate> templates)throws Exception{super(c);seed=c.get("world_seed").getAsLong();heights=c.get("height_profile").getAsString();voxels=c.get("voxel_profile").getAsString();order=c.getAsJsonArray("rotation_order");this.templates=templates;}
  @Override int initial(int x,int y,int z){return switch(voxels){case "air"->0;case "water"->9;case "mixed"->new int[]{1,3,9,8|(3<<16),10,16,216|(8<<16),0}[Math.floorMod(x+y+z,8)];default->1;};}
  public Random chunkRandom(int x,int z,long salt){chunk(x,z);JsonArray row=new JsonArray();row.add("chunk_rng");row.add(salt);events.add(row);return random=Ps4ChunkRandom.create(seed,x,z,salt);}
  private void chunk(int x,int z){JsonArray row=new JsonArray();row.add("chunk");row.add(x);row.add(z);events.add(row);}
  public boolean columnAvailable(int x,int y,int z){chunk(x>>4,z>>4);return true;}
  public int rotation(int index){return order.get(index).getAsInt();}
  public Ps4FossilFeature.Size templateSize(int id){JsonArray row=new JsonArray();row.add("template");row.add(id);events.add(row);return templates.get(id).size();}
  public int surfaceHeight(int x,int z){int value=switch(heights){case "flat"->64;case "zero"->0;case "high"->300;default->32+((x*3+z)&31);};JsonArray row=new JsonArray();row.add("height");row.add(x);row.add(z);row.add(value);events.add(row);return value;}
  public void placeTemplate(int id,int x,int y,int z,int rotation,Ps4FossilFeature.Clip clip,float integrity,Random random,int flags){templates.get(id).place(this,random,x,y,z,rotation,clip,integrity,flags);}
  public void notifyNeighbors(int x,int y,int z,int tile,int flags){JsonArray row=new JsonArray();for(int value:new int[]{x,y,z,tile,flags})row.add(value);notifications.add(row);}
 }
}

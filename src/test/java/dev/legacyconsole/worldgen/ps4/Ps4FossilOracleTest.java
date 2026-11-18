package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.GZIPInputStream;
import net.minecraft.nbt.NbtIo;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4FossilOracleTest {
 private JsonObject fixture()throws Exception{
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-fossils.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){return new JsonParser().parse(reader).getAsJsonObject();}
 }
 @Test public void originalChunkRandomOverflowAndUnsignedExtensionsMatch()throws Exception{
  JsonArray cases=fixture().getAsJsonArray("random_cases");assertEquals(624,cases.size());
  for(JsonElement e:cases){JsonObject c=e.getAsJsonObject();Random random=Ps4ChunkRandom.create(c.get("world_seed").getAsLong(),c.get("chunk_x").getAsInt(),c.get("chunk_z").getAsInt(),c.get("salt").getAsLong());for(JsonElement value:c.getAsJsonArray("values"))assertEquals("original chunk RNG "+c,value.getAsLong(),random.nextLong());}
 }
 @Test public void originalFossilSelectionCoordinatesAndIndependentRngMatch()throws Exception{
  JsonObject fixture=fixture();Map<Integer,Ps4FossilFeature.Size> sizes=new HashMap<>();
  for(JsonElement e:fixture.getAsJsonArray("template_inputs")){
   JsonObject c=e.getAsJsonObject();JsonArray size=c.getAsJsonArray("size");sizes.put(c.get("id").getAsInt(),new Ps4FossilFeature.Size(size.get(0).getAsInt(),size.get(1).getAsInt(),size.get(2).getAsInt()));
   String resource="/assets/legacyconsole/worldgen/ps4/templates/"+c.get("path").getAsString()+".nbt";byte[] bytes;try(InputStream in=getClass().getResourceAsStream(resource)){bytes=in.readAllBytes();}
   StringBuilder digest=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))digest.append(String.format(Locale.ROOT,"%02x",b&255));assertEquals("original fossil asset hash",c.get("sha256").getAsString(),digest.toString());
   try(InputStream in=new ByteArrayInputStream(bytes)){var actual=NbtIo.readCompressed(in).getList("size",3);for(int i=0;i<3;i++)assertEquals("original NBT dimensions",size.get(i).getAsInt(),actual.getInt(i));}
  }
  assertEquals(16,sizes.size());JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(1728,cases.size());Set<Integer> templates=new HashSet<>(),rotations=new HashSet<>();int probes=0;
  for(JsonElement e:cases){
   JsonObject c=e.getAsJsonObject();World world=new World(c,sizes);JsonArray p=c.getAsJsonArray("position");boolean result=Ps4FossilFeature.place(world,p.get(0).getAsInt(),p.get(1).getAsInt(),p.get(2).getAsInt());
   String label="original fossil seed="+c.get("world_seed")+" pos="+p+" heights="+world.profile+" rotations="+c.get("rotation_order")+" geometry draws="+world.draws;
   assertEquals(label,c.get("result").getAsBoolean(),result);assertEquals(label+" ordered calls",c.getAsJsonArray("events"),world.events);assertEquals(label+" following chunk RNG",c.get("chunk_random_after").getAsLong(),world.random.nextLong());assertEquals(label+" population RNG untouched",c.get("population_random_after").getAsLong(),new Random(c.get("population_seed").getAsLong()).nextLong());
   for(JsonElement event:world.events){JsonArray row=event.getAsJsonArray();if(row.get(0).getAsString().equals("place")){templates.add(row.get(1).getAsInt());rotations.add(row.get(5).getAsInt());}else if(row.get(0).getAsString().equals("height"))probes++;}
  }
  assertEquals(16,templates.size());assertEquals(Set.of(0,1,2,3),rotations);assertTrue("substantial original height-scan coverage",probes>50000);
 }
 private static class World implements Ps4FossilFeature.Access {
  final long seed;final int draws;final String profile;final JsonArray order,events=new JsonArray();final Map<Integer,Ps4FossilFeature.Size> sizes;Random random;
  World(JsonObject c,Map<Integer,Ps4FossilFeature.Size> sizes){seed=c.get("world_seed").getAsLong();draws=c.get("geometry_draws").getAsInt();profile=c.get("height_profile").getAsString();order=c.getAsJsonArray("rotation_order");this.sizes=sizes;}
  public Random chunkRandom(int x,int z,long salt){JsonArray chunk=new JsonArray();chunk.add("chunk");chunk.add(x);chunk.add(z);events.add(chunk);JsonArray rng=new JsonArray();rng.add("chunk_rng");rng.add(salt);events.add(rng);return random=Ps4ChunkRandom.create(seed,x,z,salt);}
  public int rotation(int index){return order.get(index).getAsInt();}
  public Ps4FossilFeature.Size templateSize(int id){JsonArray row=new JsonArray();row.add("template");row.add(id);events.add(row);return sizes.get(id);}
  public int surfaceHeight(int x,int z){int value=switch(profile){case "flat"->64;case "zero"->0;case "high"->300;default->32+((x*3+z)&31);};JsonArray row=new JsonArray();row.add("height");row.add(x);row.add(z);row.add(value);events.add(row);return value;}
  public void placeTemplate(int id,int x,int y,int z,int rotation,Ps4FossilFeature.Clip clip,float integrity,Random random,int flags){
   JsonArray row=new JsonArray();row.add("place");for(int value:new int[]{id,x,y,z,rotation,clip.minX(),clip.minY(),clip.minZ(),clip.maxX(),clip.maxY(),clip.maxZ()})row.add(value);row.add((double)integrity);row.add(flags);JsonArray consumed=new JsonArray();for(int i=0;i<draws;i++)consumed.add(random.nextLong());row.add(consumed);events.add(row);
  }
 }
}

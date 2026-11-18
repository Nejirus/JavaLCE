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

public class Ps4DungeonFeatureOracleTest {
 private JsonObject fixture(String name)throws Exception{
  try(InputStream input=getClass().getResourceAsStream("/worldgen/"+name);GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){
   return new JsonParser().parse(reader).getAsJsonObject();
  }
 }
 @Test public void originalDungeonGeometryEntitiesAndRngMatch()throws Exception{
  JsonObject fixture=fixture("ps4-dungeons.json.gz");JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(264,cases.size());
  assertEquals("original loot reference",fixture.get("loot_table").getAsString(),Ps4DungeonFeature.LOOT_TABLE);
  for(var mob:Ps4DungeonFeature.Mob.values())assertEquals("original RTTI/entity key "+mob,fixture.getAsJsonObject("entity_types").getAsJsonObject(mob.javaName).get("key").getAsInt(),mob.ps4Type);
  int completed=0,rejected=0,loots=0;Set<Integer> mobs=new HashSet<>();
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();long seed=c.get("seed").getAsLong();int[] position=ints(c.getAsJsonArray("position"));
   World world=new World(c.get("profile").getAsString(),position);Random random=new Random(seed);
   boolean result=Ps4DungeonFeature.place(world,random,position[0],position[1],position[2]);
   String label="original dungeon seed="+seed+" profile="+world.profile+" pos="+c.get("position");
   assertEquals(label+" result",c.get("result").getAsBoolean(),result);
   exact(label+" write/remove/entity/loot/mob order",c.getAsJsonArray("operations"),world.operations);
   assertEquals(label+" writes",c.getAsJsonArray("writes"),world.writes);
   assertEquals(label+" removals",c.getAsJsonArray("removals"),world.removals);
   assertEquals(label+" read count",c.get("read_count").getAsInt(),world.readCount);
   assertEquals(label+" read order",c.get("read_sha256").getAsString(),hex(world.hash.digest()));
   exact(label+" entity queries",c.getAsJsonArray("entity_queries"),world.entities);
   exact(label+" loot seeds",c.getAsJsonArray("loot_seeds"),world.loots);
   exact(label+" mob choices",c.getAsJsonArray("spawner_mobs"),world.mobs);
   assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());
   if(result)completed++;else rejected++;loots+=world.loots.size();for(JsonElement mob:world.mobs)mobs.add(mob.getAsJsonArray().get(3).getAsInt());
  }
  assertTrue("fixture exercises success, rejection and chest loot",completed>50&&rejected>50&&loots>50);
  assertEquals("fixture exercises all three recovered mobs",3,mobs.size());
 }
 @Test public void originalChestFacingMatchesEveryControlledNeighborCombination()throws Exception{
  JsonArray cases=fixture("ps4-chest-facing.json.gz").getAsJsonArray("cases");assertEquals(1024,cases.size());
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();World world=new World("orient",new int[]{0,64,0});world.neighbors=ints(c.getAsJsonArray("neighbors"));
   int state=Ps4DungeonFeature.chestState(world,0,64,0,c.get("facing").getAsInt());
   String label="original chest neighbors="+c.get("neighbors")+" facing="+c.get("facing");
   assertEquals(label,c.get("state").getAsInt(),state);
   assertEquals(label+" native ChestBlock::getData",c.get("native_data").getAsInt(),state>>>16);
   assertEquals(label+" read count",c.get("read_count").getAsInt(),world.readCount);
   assertEquals(label+" read order",c.get("read_sha256").getAsString(),hex(world.hash.digest()));
  }
 }
 private record Pos(int x,int y,int z){}
 private static final class World implements Ps4DungeonFeature.Access {
  private static final int[][] DELTAS={{0,0,-1},{1,0,0},{0,0,1},{-1,0,0}};
  final String profile;final int[] origin;int[] neighbors;
  final Map<Pos,Integer> changed=new HashMap<>();
  final JsonArray writes=new JsonArray(),removals=new JsonArray(),entities=new JsonArray(),loots=new JsonArray(),mobs=new JsonArray(),operations=new JsonArray();
  final MessageDigest hash=MessageDigest.getInstance("SHA-256");final ByteBuffer probe=ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);int readCount;
  World(String profile,int[] origin)throws Exception{this.profile=profile;this.origin=origin;}
  int initial(int x,int y,int z){
   int dx=x-origin[0],dy=y-origin[1],dz=z-origin[2];
   if(profile.equals("orient")){for(int i=0;i<4;i++)if(dx==DELTAS[i][0]&&dy==0&&dz==DELTAS[i][2])return neighbors[i];return 0;}
   if(profile.equals("floor_hole")&&dy==-1||profile.equals("ceiling_hole")&&dy==4||profile.equals("unsupported")&&dy==-2)return 0;
   if(profile.equals("many_holes")&&(dy==0||dy==1))return 0;
   if(profile.equals("no_holes"))return 1;
   if(profile.equals("old_chests")&&dy==0&&(dx==0&&dz==1||dx==1&&dz==0))return 54|(2<<16);
   if(profile.equals("old_spawner")&&dx==0&&dy==0&&dz==0)return 52;
   boolean tunnel=dz==0&&(dy==0||dy==1)&&(!profile.equals("one_hole")||dx>=0);
   if(profile.equals("four_holes"))tunnel|=dx==0&&(dy==0||dy==1);
   return tunnel?0:1;
  }
  int state(int x,int y,int z){return changed.getOrDefault(new Pos(x,y,z),initial(x,y,z));}
  public int block(int x,int y,int z){int state=state(x,y,z);probe.clear();probe.putInt(x).putInt(y).putInt(z).putInt(state);hash.update(probe.array());readCount++;return state;}
  public boolean solid(int state){int id=state&65535;return id!=0&&id!=8&&id!=9&&id!=10&&id!=11;}
  public boolean solidRender(int state){int id=state&65535;return solid(state)&&id!=20&&id!=52&&id!=54;}
  public void setBlock(int x,int y,int z,int state,int flags){writes.add(array(x,y,z,state,flags));operations.add(operation("set",x,y,z,state,flags));changed.put(new Pos(x,y,z),state);}
  public void removeBlock(int x,int y,int z){removals.add(array(x,y,z));operations.add(operation("remove",x,y,z));changed.put(new Pos(x,y,z),0);}
  public Ps4DungeonFeature.Entity entity(int x,int y,int z){
   int id=state(x,y,z)&65535,kind=profile.equals("wrong_entities")?0:id==54?0x1e00201:id==52?0x1200201:0;
   entities.add(array(x,y,z,kind));operations.add(operation("entity",x,y,z,kind));
   return kind==0x1e00201?Ps4DungeonFeature.Entity.CHEST:kind==0x1200201?Ps4DungeonFeature.Entity.SPAWNER:Ps4DungeonFeature.Entity.OTHER;
  }
  public void chestLoot(int x,int y,int z,String table,long seed){assertEquals(Ps4DungeonFeature.LOOT_TABLE,table);loots.add(array(x,y,z,seed));operations.add(operation("loot",x,y,z,seed));}
  public void spawnerMob(int x,int y,int z,Ps4DungeonFeature.Mob mob){mobs.add(array(x,y,z,mob.ps4Type));operations.add(operation("mob",x,y,z,mob.ps4Type));}
  public int height(int x,int z){return 128;}
  public int seaLevel(){return 63;}
 }
 private static int[] ints(JsonArray array){int[] result=new int[array.size()];for(int i=0;i<result.length;i++)result[i]=array.get(i).getAsInt();return result;}
 private static JsonArray array(long... values){JsonArray result=new JsonArray();for(long value:values)result.add(value);return result;}
 private static JsonArray operation(String name,long... values){JsonArray result=new JsonArray();result.add(name);for(long value:values)result.add(value);return result;}
 private static void exact(String label,JsonArray expected,JsonArray actual){assertEquals(label+" count",expected.size(),actual.size());for(int i=0;i<expected.size();i++)assertEquals(label+" #"+i,expected.get(i).toString(),actual.get(i).toString());}
 private static String hex(byte[] data){StringBuilder result=new StringBuilder();for(byte value:data)result.append(String.format(Locale.ROOT,"%02x",value&255));return result.toString();}
}

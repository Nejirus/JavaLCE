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

public class Ps4FloraOracleTest {
 private JsonArray fixture(String name)throws Exception{
  try(InputStream input=getClass().getResourceAsStream("/worldgen/"+name);GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){
   return new JsonParser().parse(reader).getAsJsonObject().getAsJsonArray("cases");
  }
 }
 @Test public void originalScatterSurvivalWritesAndRngMatch()throws Exception{
  JsonArray cases=fixture("ps4-flora.json.gz");assertEquals(5376,cases.size());
  int writes=0;Set<String> successes=new HashSet<>();
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();World world=new World(c);long seed=c.get("seed").getAsLong();Random random=new Random(seed);
   var kind=Ps4ScatterFeatures.Kind.valueOf(c.get("kind").getAsString().toUpperCase(Locale.ROOT));
   String label=kind+" seed="+seed+" profile="+world.profile+" pos="+c.get("position");
   boolean result=Ps4ScatterFeatures.place(kind,world,random,world.origin[0],world.origin[1],world.origin[2],c.get("state").getAsInt());
   assertEquals(label+" result",c.get("result").getAsBoolean(),result);
   compare(label,c,world);
   assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());
   writes+=world.writes.size();if(world.writes.size()>0)successes.add(kind.name());
  }
  assertEquals("all eight kernels place blocks",8,successes.size());assertTrue("substantial successful placement coverage",writes>5000);
 }
 @Test public void originalPlantPlacementAndSurvivalRulesMatch()throws Exception{
  JsonArray cases=fixture("ps4-plant-rules.json.gz");assertEquals(15636,cases.size());
  int accepted=0,rejected=0;
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();World world=new World(c);int state=c.get("state").getAsInt();
   for(JsonElement row:c.getAsJsonArray("blocks")){int[] b=ints(row.getAsJsonArray());world.changed.put(new Pos(b[0],b[1],b[2]),b[3]);}
   for(JsonElement row:c.getAsJsonArray("fluids")){int[] b=ints(row.getAsJsonArray());world.fluids.add(new Pos(b[0],b[1],b[2]));}
   boolean result=c.get("kind").getAsString().equals("may_place")?Ps4PlantRules.mayPlace(world,state&65535,world.origin[0],world.origin[1],world.origin[2]):Ps4PlantRules.canStay(world,state,world.origin[0],world.origin[1],world.origin[2]);
   String label="original "+c.get("kind")+" state="+state+" pos="+c.get("position")+" blocks="+c.get("blocks")+" fluids="+c.get("fluids");
   assertEquals(label,c.get("result").getAsBoolean(),result);compare(label,c,world);
   if(result)accepted++;else rejected++;
  }
  assertTrue("both rule outcomes covered",accepted>100&&rejected>100);
 }
 static void compare(String label,JsonObject expected,World actual){
  assertEquals(label+" writes",expected.getAsJsonArray("writes"),actual.writes);
  assertEquals(label+" block probe count",expected.get("read_count").getAsInt(),actual.readCount);
  assertEquals(label+" block probe order",expected.get("read_sha256").getAsString(),hex(actual.hash.digest()));
  assertEquals(label+" fluid queries",expected.getAsJsonArray("fluid_queries"),actual.fluidQueries);
  assertEquals(label+" protection queries",expected.getAsJsonArray("rule_queries"),actual.ruleQueries);
  assertEquals(label+" height queries",expected.getAsJsonArray("height_queries"),actual.heightQueries);
  assertEquals(label+" light queries",expected.getAsJsonArray("light_queries"),actual.lightQueries);
 }
 private record Pos(int x,int y,int z){}
 static class World implements Ps4ScatterFeatures.Access {
  static final Set<Integer> NONSOLID=Set.of(0,8,9,10,11,31,32,37,38,39,40,83,111,175),REPLACEABLE=Set.of(0,8,9,10,11,31,32,37,38,39,40,83,175);
  final String profile;final int[] origin;final boolean ceiling;final int brightness;
  final Map<Pos,Integer> changed=new HashMap<>();final Set<Pos> fluids=new HashSet<>();
  final JsonArray writes=new JsonArray(),fluidQueries=new JsonArray(),ruleQueries=new JsonArray(),heightQueries=new JsonArray(),lightQueries=new JsonArray();
  final MessageDigest hash=MessageDigest.getInstance("SHA-256");final ByteBuffer probe=ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);int readCount;
  World(JsonObject c)throws Exception{profile=c.has("profile")?c.get("profile").getAsString():"rule";origin=ints(c.getAsJsonArray("position"));ceiling=c.has("ceiling")&&c.get("ceiling").getAsBoolean();brightness=c.has("brightness")?c.get("brightness").getAsInt():profile.equals("bright")?15:12;}
  int initial(int x,int y,int z){
   if(profile.equals("rule")||y<0||y>=256)return 0;
   int surface=origin[1]-1;
   if(profile.equals("canopy")&&y>=surface+1&&y<=surface+3)return 18;
   if(y>surface)return 0;if(y<surface)return 1;
   return switch(profile){
    case "water"->9;case "flowing"->8;case "flowing_level"->8|(1<<16);case "ice"->79;case "packed_ice"->174;
    case "sand","wet_sand","waterlogged","frosted","lava_side"->Math.floorMod(x+z,4)==0?switch(profile){case "wet_sand"->9;case "waterlogged"->20;case "frosted"->212;case "lava_side"->10;default->12;}:12;
    case "terracotta"->(x&1)!=0?172:159|(3<<16);case "dirt"->3|(1<<16);case "podzol"->3|(2<<16);case "mycelium"->110;case "farmland"->60|(7<<16);case "stone"->1;default->2;
   };
  }
  int state(int x,int y,int z){return changed.getOrDefault(new Pos(x,y,z),initial(x,y,z));}
  public int block(int x,int y,int z){int state=state(x,y,z);probe.clear();probe.putInt(x).putInt(y).putInt(z).putInt(state);hash.update(probe.array());readCount++;return state;}
  public void setBlock(int x,int y,int z,int state,int flags){writes.add(array(x,y,z,state,flags));changed.put(new Pos(x,y,z),state);}
  public boolean solid(int state){return !NONSOLID.contains(state&65535);}
  public boolean solidRender(int state){int id=state&65535;return !NONSOLID.contains(id)&&id!=18&&id!=20&&id!=52&&id!=54&&id!=161;}
  public int rawBrightness(int x,int y,int z){lightQueries.add(array(x,y,z,brightness));return brightness;}
  public boolean liquid(int state){int id=state&65535;return id==8||id==9||id==10||id==11;}
  public boolean replaceable(int state){return REPLACEABLE.contains(state&65535);}
  public boolean airMaterial(int state){return (state&65535)==0;}
  public boolean leavesMaterial(int state){int id=state&65535;return id==18||id==161;}
  public boolean waterMaterial(int state){int id=state&65535;return id==8||id==9;}
  public boolean lavaMaterial(int state){int id=state&65535;return id==10||id==11;}
  public boolean iceMaterial(int state){int id=state&65535;return id==79||id==212;}
  public boolean waterFluid(int x,int y,int z){int id=state(x,y,z)&65535;boolean water=profile.equals("rule")?fluids.contains(new Pos(x,y,z)):id==8||id==9||profile.equals("waterlogged")&&id==20;fluidQueries.add(array(x,y,z,water?1:0));return water;}
  public boolean hasCeiling(){return ceiling;}
  public boolean hasGenerationRules(){return profile.equals("protected");}
  public boolean intersectsGenerationRule(int x0,int y0,int z0,int x1,int y1,int z1){ruleQueries.add(array(x0,y0,z0,x1,y1,z1));return x0<=origin[0]+8&&origin[0]+8<=x1;}
  public int height(int x,int z){heightQueries.add(array(x,z));return 256;}
  public int seaLevel(){return 63;}
 }
 private static int[] ints(JsonArray array){int[] result=new int[array.size()];for(int i=0;i<result.length;i++)result[i]=array.get(i).getAsInt();return result;}
 private static JsonArray array(long... values){JsonArray result=new JsonArray();for(long value:values)result.add(value);return result;}
 private static String hex(byte[] data){StringBuilder result=new StringBuilder();for(byte value:data)result.append(String.format(Locale.ROOT,"%02x",value&255));return result.toString();}
}

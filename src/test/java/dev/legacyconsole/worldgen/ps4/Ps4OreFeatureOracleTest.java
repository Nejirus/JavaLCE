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

public class Ps4OreFeatureOracleTest {
 private JsonObject fixture()throws Exception{
  return fixture("ps4-ores.json.gz");
 }
 private JsonObject fixture(String name)throws Exception{
  try(InputStream input=getClass().getResourceAsStream("/worldgen/"+name);
      GZIPInputStream gzip=new GZIPInputStream(input);
      Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){
   return new JsonParser().parse(reader).getAsJsonObject();
  }
 }

 @Test public void originalStonePredicateMatches()throws Exception{
  JsonArray cases=fixture().getAsJsonArray("predicate_cases");assertEquals(42,cases.size());
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();int state=c.get("state").getAsInt();
   assertEquals("stone predicate state="+state,c.get("result").getAsBoolean(),Ps4OreFeature.isNaturalStone(state));
  }
 }

 @Test public void originalOreWritesReadsRulesAndRngMatch()throws Exception{
  JsonArray cases=fixture().getAsJsonArray("cases");assertEquals(200,cases.size());
  int writes=0,reads=0,heightQueries=0,ruleQueries=0;
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();JsonArray position=c.getAsJsonArray("position");
   long seed=c.get("seed").getAsLong();int size=c.get("size").getAsInt();
   int x=position.get(0).getAsInt(),y=position.get(1).getAsInt(),z=position.get(2).getAsInt();
   String label="original PS4 ore seed="+seed+" size="+size+" profile="+c.get("profile").getAsString();
   World world=new World(c,x);Random random=new Random(seed);
   assertEquals(label,c.get("result").getAsBoolean(),new Ps4OreFeature(c.get("state").getAsInt(),size).place(world,random,x,y,z));
   assertEquals(label+" writes",c.getAsJsonArray("writes"),world.writes);
   assertEquals(label+" read count",c.get("read_count").getAsInt(),world.readCount);
   assertEquals(label+" read order",c.get("read_sha256").getAsString(),hex(world.readHash.digest()));
   assertEquals(label+" height queries",c.getAsJsonArray("height_queries"),world.heightQueries);
   assertEquals(label+" rule queries",c.getAsJsonArray("rule_queries"),world.ruleQueries);
   assertEquals(label+" following RNG",Double.doubleToRawLongBits(c.get("random_after").getAsDouble()),Double.doubleToRawLongBits(random.nextDouble()));
   writes+=world.writes.size();reads+=world.readCount;heightQueries+=world.heightQueries.size();ruleQueries+=world.ruleQueries.size();
  }
  assertTrue("fixture includes successful veins",writes>1000);
  assertTrue("fixture includes block probes",reads>1000);
  assertTrue("fixture exercises above-sea-level height rejection",heightQueries>100);
  assertTrue("fixture exercises generation-rule intersections",ruleQueries>100);
 }

 @Test public void originalOreDecoratorOrderPositionsBlocksAndRngMatch()throws Exception{
  verifyDecorators("ps4-ore-decoration.json.gz",false,24);
 }
 @Test public void originalMesaOreOverrideAndSharedFeatureRngMatch()throws Exception{
  verifyDecorators("ps4-mesa-ores.json.gz",true,216);
 }
 private void verifyDecorators(String name,boolean mesa,int count)throws Exception{
  JsonArray cases=fixture(name).getAsJsonArray("cases");assertEquals(count,cases.size());
  int attempts=0;
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();long seed=c.get("seed").getAsLong();
   int x=c.getAsJsonArray("origin").get(0).getAsInt(),z=c.getAsJsonArray("origin").get(1).getAsInt();
   List<Ps4OreDecorator.Vein> veins=new ArrayList<>();
   for(JsonElement entry:c.getAsJsonArray("settings")){
    JsonObject v=entry.getAsJsonObject();
    veins.add(new Ps4OreDecorator.Vein(v.get("name").getAsString(),v.get("state").getAsInt(),v.get("size").getAsInt(),v.get("attempts").getAsInt(),v.get("minimum").getAsInt(),v.get("maximum").getAsInt(),v.get("triangular").getAsBoolean()));
   }
   if(c.get("mode").getAsString().equals("default"))assertEquals("original default settings",veins,Ps4OreDecorator.DEFAULTS);
   String label="original PS4 "+(mesa?"Mesa ":"")+"ore decorator seed="+seed+" mode="+c.get("mode").getAsString()+" origin="+x+","+z;
   World world=new World(c,x);Random random=new Random(seed);JsonArray calls=new JsonArray();
   Ps4OreDecorator.Placement placement=(vein,rng,bx,by,bz)->{
    calls.add(array(vein.state(),vein.size(),bx,by,bz));
    new Ps4OreFeature(vein.state(),vein.size()).place(world,rng,bx,by,bz);
   };
   if(mesa)Ps4OreDecorator.decorateMesa(veins,random,x,z,placement);else Ps4OreDecorator.decorate(veins,random,x,z,placement);
   assertEquals(label+" feature calls",c.getAsJsonArray("feature_calls"),calls);
   assertEquals(label+" writes",c.getAsJsonArray("writes"),world.writes);
   assertEquals(label+" read count",c.get("read_count").getAsInt(),world.readCount);
   assertEquals(label+" read order",c.get("read_sha256").getAsString(),hex(world.readHash.digest()));
   assertEquals(label+" height queries",c.getAsJsonArray("height_queries"),world.heightQueries);
   assertEquals(label+" protection queries",c.getAsJsonArray("rule_queries"),world.ruleQueries);
   assertEquals(label+" following RNG",Double.doubleToRawLongBits(c.get("random_after").getAsDouble()),Double.doubleToRawLongBits(random.nextDouble()));
   attempts+=calls.size();
  }
  assertTrue("fixture exercises complete default ore passes",attempts>500);
 }

 @Test public void originalClaySandAndGravelDepositsMatch()throws Exception{
  JsonArray cases=fixture("ps4-deposits.json.gz").getAsJsonArray("cases");assertEquals(96,cases.size());int writes=0;
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();long seed=c.get("seed").getAsLong();
   JsonArray pos=c.getAsJsonArray("position");int x=pos.get(0).getAsInt(),y=pos.get(1).getAsInt(),z=pos.get(2).getAsInt();
   World world=new World(c,x);Random random=new Random(seed);
   boolean clay=c.get("kind").getAsString().equals("clay");int radius=c.get("maximum_radius").getAsInt();
   boolean result=clay?Ps4DepositFeature.clay(world,random,x,y,z,radius):Ps4DepositFeature.sand(world,random,x,y,z,radius,c.get("state").getAsInt());
   String label="original deposit "+c.get("kind").getAsString()+" seed="+seed+" profile="+world.profile+" x="+x;
   assertEquals(label+" result",c.get("result").getAsBoolean(),result);
   assertEquals(label+" writes",c.getAsJsonArray("writes"),world.writes);
   assertEquals(label+" read count",c.get("read_count").getAsInt(),world.readCount);
   assertEquals(label+" read order",c.get("read_sha256").getAsString(),hex(world.readHash.digest()));
   assertEquals(label+" rule queries",c.getAsJsonArray("rule_queries"),world.ruleQueries);
   assertEquals(label+" following RNG",Double.doubleToRawLongBits(c.get("random_after").getAsDouble()),Double.doubleToRawLongBits(random.nextDouble()));
   writes+=world.writes.size();
  }
  assertTrue("fixture exercises submerged patch writes",writes>1000);
 }

 private record Pos(int x,int y,int z){}
 private static final class World implements Ps4OreFeature.Access {
  private static final int[] MIXED={1,1|(1<<16),1|(2<<16),1|(3<<16),1|(4<<16),1|(5<<16),1|(6<<16),2,3,13,0,49,7};
  private static final int[] DEPOSITS={3,3|(1<<16),3|(2<<16),2,82,13,1,0};
  final Map<Pos,Integer> changed=new HashMap<>();
  final JsonArray writes=new JsonArray(),heightQueries=new JsonArray(),ruleQueries=new JsonArray();
  final MessageDigest readHash;
  final ByteBuffer probe=ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
  final String profile;final int height,anchorX;final boolean deposit;int readCount;
  World(JsonObject c,int x)throws Exception{
   profile=c.get("profile").getAsString();height=c.has("height")?c.get("height").getAsInt():128;deposit=c.has("kind");anchorX=x;readHash=MessageDigest.getInstance("SHA-256");
  }
  public int block(int x,int y,int z){
   int initial=y<0||y>=256||profile.equals("air")?0:profile.equals("mixed")?MIXED[Math.floorMod(x*17+y*11+z*31,MIXED.length)]:1;
   if(deposit)initial=profile.equals("dry")?3:y>=64?profile.equals("flowing")?8:9:DEPOSITS[Math.floorMod(x*17+y*11+z*31,DEPOSITS.length)];
   int state=changed.getOrDefault(new Pos(x,y,z),initial);
   probe.clear();probe.putInt(x).putInt(y).putInt(z).putInt(state);readHash.update(probe.array());readCount++;return state;
  }
  public void setBlock(int x,int y,int z,int state,int flags){writes.add(array(x,y,z,state,flags));changed.put(new Pos(x,y,z),state);}
  public int height(int x,int z){heightQueries.add(array(x,z));return height;}
  public int seaLevel(){return 63;}
  public boolean hasGenerationRules(){return profile.equals("rules")||profile.equals("protected");}
  public boolean intersectsGenerationRule(int x0,int y0,int z0,int x1,int y1,int z1){ruleQueries.add(array(x0,y0,z0,x1,y1,z1));return profile.equals("protected")||x0<=anchorX+8&&anchorX+8<=x1;}
 }
 private static JsonArray array(int... values){JsonArray result=new JsonArray();for(int value:values)result.add(value);return result;}
 private static String hex(byte[] data){StringBuilder result=new StringBuilder();for(byte value:data)result.append(String.format(Locale.ROOT,"%02x",value&255));return result.toString();}
}

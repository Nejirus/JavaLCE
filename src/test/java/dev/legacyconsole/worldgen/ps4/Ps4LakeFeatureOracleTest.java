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

public class Ps4LakeFeatureOracleTest {
 @Test public void originalLakesWritesQueriesAndRngMatch()throws Exception{
  JsonArray cases;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-lakes.json.gz");
      GZIPInputStream gzip=new GZIPInputStream(input);
      Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){
   cases=new JsonParser().parse(reader).getAsJsonObject().getAsJsonArray("cases");
  }
  assertEquals(216,cases.size());int successes=0,failures=0,writes=0,ice=0,mycelium=0;
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();long seed=c.get("seed").getAsLong();
   JsonArray pos=c.getAsJsonArray("position");int x=pos.get(0).getAsInt(),y=pos.get(1).getAsInt(),z=pos.get(2).getAsInt();
   World world=new World(c,x,z);Random random=new Random(seed);
   boolean result=new Ps4LakeFeature(c.get("tile").getAsInt()).place(world,random,x,y,z);
   String label="original PS4 lake seed="+seed+" profile="+world.profile+" tile="+c.get("tile")+" pos="+pos;
   assertEquals(label+" result",c.get("result").getAsBoolean(),result);
   assertEquals(label+" writes",c.getAsJsonArray("writes"),world.writes);
   assertEquals(label+" read count",c.get("read_count").getAsInt(),world.readCount);
   assertEquals(label+" read order",c.get("read_sha256").getAsString(),hex(world.readHash.digest()));
   assertEquals(label+" rule queries",c.getAsJsonArray("rule_queries"),world.ruleQueries);
   assertEquals(label+" light queries",c.getAsJsonArray("light_queries"),world.lightQueries);
   assertEquals(label+" biome queries",c.getAsJsonArray("biome_queries"),world.biomeQueries);
   assertEquals(label+" temperature queries",c.getAsJsonArray("temperature_queries"),world.temperatureQueries);
   assertEquals(label+" following RNG",Double.doubleToRawLongBits(c.get("random_after").getAsDouble()),Double.doubleToRawLongBits(random.nextDouble()));
   if(result)successes++;else failures++;
   for(JsonElement write:world.writes){int state=write.getAsJsonArray().get(3).getAsInt();if(state==79)ice++;if(state==110)mycelium++;}
   writes+=world.writes.size();
  }
  assertTrue("fixture includes both completed and rejected lakes",successes>50&&failures>50);
  assertTrue("fixture exercises lake writes",writes>10000);
  assertTrue("fixture exercises freezing existing source water",ice>0);
  assertTrue("fixture exercises mushroom-biome floor repair",mycelium>0);
 }
 private record Pos(int x,int y,int z){}
 private static final class World implements Ps4LakeFeature.Access {
  final String profile;final int originX,originZ;
  final Map<Pos,Integer> changed=new HashMap<>();
  final JsonArray writes=new JsonArray(),ruleQueries=new JsonArray(),lightQueries=new JsonArray(),biomeQueries=new JsonArray(),temperatureQueries=new JsonArray();
  final MessageDigest readHash=MessageDigest.getInstance("SHA-256");
  final ByteBuffer probe=ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
  int readCount;
  World(JsonObject c,int x,int z)throws Exception{profile=c.get("profile").getAsString();originX=x;originZ=z;}
  public int block(int x,int y,int z){
   int initial;
   if(profile.equals("empty"))initial=0;
   else if(profile.equals("flooded")&&y>=64&&y<80)initial=9;
   else if(profile.equals("leaking")&&Math.floorMod(x*17+y*11+z*31,7)==0)initial=0;
   else if((profile.equals("cold")||profile.equals("flowing")||profile.equals("bright"))&&y==63
      &&(x-originX+8==0||x-originX+8==15)&&(z-originZ+8==0||z-originZ+8==15))initial=profile.equals("flowing")?8|(1<<16):9;
   else initial=y>=64?0:y==63?3|(1<<16):y==62?3|(2<<16):1;
   int state=changed.getOrDefault(new Pos(x,y,z),initial);
   probe.clear();probe.putInt(x).putInt(y).putInt(z).putInt(state);readHash.update(probe.array());readCount++;return state;
  }
  public void setBlock(int x,int y,int z,int state,int flags){writes.add(array(x,y,z,state,flags));changed.put(new Pos(x,y,z),state);}
  public boolean solid(int state){int id=state&65535;return id!=0&&!liquid(state);}
  public boolean liquid(int state){int id=state&65535;return id==8||id==9||id==10||id==11;}
  public int skyLight(int x,int y,int z){lightQueries.add(array(15,x,y,z));return 15;}
  public int blockLight(int x,int y,int z){lightQueries.add(array(0,x,y,z));return profile.equals("bright")?15:0;}
  public int biomeTop(int x,int y,int z){biomeQueries.add(array(x,y,z));return profile.equals("mushroom")?110:2;}
  public float temperature(int x,int y,int z){biomeQueries.add(array(x,y,z));temperatureQueries.add(array(x,y,z));return profile.equals("cold")||profile.equals("flowing")||profile.equals("bright")?0:.8f;}
  public int height(int x,int z){return 128;}
  public int seaLevel(){return 63;}
  public boolean hasGenerationRules(){return profile.equals("protected");}
  public boolean intersectsGenerationRule(int x0,int y0,int z0,int x1,int y1,int z1){ruleQueries.add(array(x0,y0,z0,x1,y1,z1));return true;}
 }
 private static JsonArray array(int... values){JsonArray result=new JsonArray();for(int value:values)result.add(value);return result;}
 private static String hex(byte[] data){StringBuilder result=new StringBuilder();for(byte value:data)result.append(String.format(Locale.ROOT,"%02x",value&255));return result.toString();}
}

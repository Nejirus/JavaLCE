package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4MushroomGeometryOracleTest {
 @Test public void originalHugeMushroomAndMelonGeometryAndRngMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-mushrooms-melons.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){
   fixture=new JsonParser().parse(reader).getAsJsonObject();
  }
  JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(2616,cases.size());int huge=0,melon=0,writes=0;Set<Integer> flags=new HashSet<>(),types=new HashSet<>();boolean doubled=false;
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();World world=new World(c);Random random=new Random(c.get("seed").getAsLong());int[] p=world.origin;boolean mushroom=c.get("kind").getAsString().equals("huge");
   boolean result=mushroom?Ps4HugeMushroomFeature.place(world,random,p[0],p[1],p[2],c.get("forced").getAsInt(),c.get("updates").getAsBoolean()):Ps4MelonFeature.place(world,random,p[0],p[1],p[2]);
   String label="original "+c.get("kind")+" seed="+c.get("seed")+" profile="+world.profile+" pos="+c.get("position")+" forced="+c.get("forced")+" updates="+c.get("updates");
   assertEquals(label+" result",c.get("result").getAsBoolean(),result);Ps4FloraOracleTest.compare(label,c,world);
   assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());
   if(mushroom)huge++;else melon++;writes+=world.writes.size();
   for(JsonElement w:world.writes){JsonArray row=w.getAsJsonArray();int state=row.get(3).getAsInt();flags.add(row.get(4).getAsInt());types.add(state&65535);if(mushroom&&row.get(1).getAsInt()>p[1]+6)doubled=true;}
  }
  assertEquals(2256,huge);assertEquals(360,melon);assertTrue("both update modes",flags.containsAll(Set.of(2,3)));assertEquals(Set.of(99,100,103),types);assertTrue("rare doubled mushrooms covered",doubled);assertTrue("successful geometry coverage",writes>10000);
 }
 private static final class World extends Ps4FloraOracleTest.World {
  World(JsonObject c)throws Exception{super(c);}
  @Override int initial(int x,int y,int z){
   int ox=origin[0],oy=origin[1],oz=origin[2];
   if(profile.equals("blocked")&&x==ox+1&&y==oy+4&&z==oz+1)return 17;
   if(profile.equals("trunk_obstacle")&&x==ox&&y==oy+1&&z==oz)return 17;
   if(profile.equals("glass")&&x==ox&&y==oy+1&&z==oz)return 20;
   if(profile.equals("leaves")&&y>=oy&&y<oy+16&&Math.abs(x-ox)<=3&&Math.abs(z-oz)<=3)return 18;
   if(profile.equals("replaceable")&&y==oy&&Math.floorMod(x+z,3)==0)return 31|(2<<16);
   return super.initial(x,y,z);
  }
 }
}

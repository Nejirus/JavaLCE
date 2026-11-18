package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4SpringOracleTest {
 @Test public void originalSpringProbesAndImmediateTickOrderMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-springs.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(7968,cases.size());int writes=0,accepted=0,rejected=0;Set<Integer> fluids=new HashSet<>(),draws=new HashSet<>();
  for(JsonElement e:cases){
   JsonObject c=e.getAsJsonObject();World world=new World(c);Random random=new Random(c.get("seed").getAsLong());int[] p=world.origin;boolean result=Ps4SpringFeature.place(world,random,p[0],p[1],p[2],c.get("target").getAsInt());
   String label="original spring seed="+c.get("seed")+" pos="+c.get("position")+" top="+c.get("above")+" bottom="+c.get("below")+" current="+c.get("current")+" sides="+c.get("sides")+" protection="+c.get("protected")+" reject="+c.get("reject_rule");
   assertEquals(label,c.get("result").getAsBoolean(),result);Ps4FloraOracleTest.compare(label,c,world);assertEquals(label+" lifecycle",c.getAsJsonArray("lifecycle"),world.lifecycle);assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());assertFalse(label+" immediate flag cleared",world.immediate);
   if(result)accepted++;else rejected++;writes+=world.writes.size();if(world.writes.size()>0){fluids.add(c.get("target").getAsInt());draws.add(c.get("tick_draws").getAsInt());}
  }
  assertEquals("original writes",224,writes);assertEquals(608,accepted);assertEquals(7360,rejected);assertEquals(Set.of(8,10),fluids);assertEquals(Set.of(0,1,3),draws);
 }
 private static class World extends Ps4FloraOracleTest.World implements Ps4SpringFeature.Access {
  final JsonObject input;final JsonArray lifecycle=new JsonArray();final Map<List<Integer>,Integer> context=new HashMap<>();boolean immediate;
  World(JsonObject c)throws Exception{
   super(c);input=c;int x=origin[0],y=origin[1],z=origin[2];context.put(List.of(x,y+1,z),c.get("above").getAsInt());context.put(List.of(x,y-1,z),c.get("below").getAsInt());context.put(List.of(x,y,z),c.get("current").getAsInt());int[][] sides={{-1,0},{1,0},{0,-1},{0,1}};for(int i=0;i<4;i++)context.put(List.of(x+sides[i][0],y,z+sides[i][1]),c.getAsJsonArray("sides").get(i).getAsInt());
  }
  @Override int initial(int x,int y,int z){return context.getOrDefault(List.of(x,y,z),0);}
  @Override public boolean hasGenerationRules(){return input.get("protected").getAsBoolean();}
  @Override public boolean intersectsGenerationRule(int x0,int y0,int z0,int x1,int y1,int z1){JsonArray row=new JsonArray();for(int v:new int[]{x0,y0,z0,x1,y1,z1})row.add(v);ruleQueries.add(row);return input.get("reject_rule").getAsBoolean();}
  @Override public void setBlock(int x,int y,int z,int state,int flags){super.setBlock(x,y,z,state,flags);JsonArray row=new JsonArray();row.add("set");for(int v:new int[]{x,y,z,state,flags})row.add(v);lifecycle.add(row);}
  public void immediateFluidTicks(boolean enabled){immediate=enabled;JsonArray row=new JsonArray();row.add("immediate");row.add(9);row.add(enabled?1:0);lifecycle.add(row);}
  public void tickFluid(int x,int y,int z,int state,Random random){JsonArray row=new JsonArray();row.add("tick");for(int v:new int[]{x,y,z,state})row.add(v);row.add(immediate);JsonArray values=new JsonArray();for(int i=0;i<input.get("tick_draws").getAsInt();i++)values.add(random.nextLong());row.add(values);lifecycle.add(row);}
 }
}

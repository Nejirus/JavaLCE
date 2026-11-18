package dev.legacyconsole.worldgen.ps4;
import com.google.gson.*;
import dev.legacyconsole.worldgen.ps4.layers.Ps4Layers;
import java.io.*;import java.nio.charset.StandardCharsets;import java.util.*;
import org.junit.Test;import static org.junit.Assert.*;
public class Ps4BiomeScaleOracleTest {
 @Test public void allPs4ScaleBranchesMatchNativeQuartAndVoronoiRectangles()throws Exception{
  JsonArray cases=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/worldgen/ps4-biome-scale-vectors.json"),StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("cases");Map<String,Ps4Layers> worlds=new HashMap<>();
  for(var e:cases){var c=e.getAsJsonObject();long seed=c.get("seed").getAsLong();int width=c.get("width").getAsInt(),scale=c.get("scale").getAsInt();var world=worlds.computeIfAbsent(seed+":"+width+":"+scale,k->new Ps4Layers(seed,width,scale));var q=c.getAsJsonArray("rectangle");int x=q.get(0).getAsInt(),z=q.get(1).getAsInt(),w=q.get(2).getAsInt(),h=q.get(3).getAsInt();int[] got=c.get("space").getAsString().equals("quart")?world.quartArea(x,z,w,h):world.blockArea(x,z,w,h),want=new int[w*h];for(int i=0;i<want.length;i++)want[i]=c.getAsJsonArray("values").get(i).getAsInt();assertArrayEquals(c.toString().substring(0,100),want,got);}
 }
 @Test public void originalBalancedThresholdsAndCategoryAggregationMatch()throws Exception{
  var data=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/worldgen/ps4-balanced-vectors.json"),StandardCharsets.UTF_8)).getAsJsonObject();var reps=data.getAsJsonArray("representatives");
  for(var e:data.getAsJsonArray("cases")){var c=e.getAsJsonObject();int[] ids=new int[c.get("length").getAsInt()];int off=0;for(int i=0;i<8;i++){int count=c.getAsJsonArray("counts").get(i).getAsInt();Arrays.fill(ids,off,off+count,reps.get(i).getAsInt());off+=count;}var result=Ps4BalancedSeed.evaluate(ids);assertEquals(c.get("match").getAsBoolean(),result.matches());for(int i=0;i<8;i++)assertEquals(c.getAsJsonArray("groups").get(i).getAsFloat(),result.fractions()[i],0);}
 }
 @Test public void searchCanBeCanceledBeforeSampling(){try{Ps4BalancedSeed.find(new Random(1),dev.legacyconsole.worldgen.Ps4WorldSize.CLASSIC,dev.legacyconsole.worldgen.Ps4BiomeScale.SMALL,()->true,n->{});fail();}catch(java.util.concurrent.CancellationException expected){}}
 @Test public void balancedCreationRequiresItsCheckboxAndLeavesAllExplicitSeedsAlone(){
  assertTrue(Ps4BalancedSeed.shouldSearch(true,true,""));
  assertFalse(Ps4BalancedSeed.shouldSearch(true,false,""));
  assertFalse(Ps4BalancedSeed.shouldSearch(false,true,""));
  for(String seed:new String[]{"0","-1","WUBZY"," "})assertFalse(seed,Ps4BalancedSeed.shouldSearch(true,true,seed));
 }
}

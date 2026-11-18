package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4OceanWrappersOracleTest {
 @Test public void originalOceanIceCoordinatesCountsCallOrderAndRngMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-ocean-wrappers.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(5376,cases.size());assertEquals(6,fixture.getAsJsonArray("feature_bindings").size());Set<Ps4BiomeDecorations.Feature> reached=EnumSet.noneOf(Ps4BiomeDecorations.Feature.class);int events=0,absolutePickles=0;
  for(JsonElement e:cases){
   JsonObject c=e.getAsJsonObject();var w=new Ps4BiomeDecorationsOracleTest.World(c);int[] p=w.origin;Random random=new Random(c.get("seed").getAsLong());
   var s=new Ps4BiomeDecorator.Settings(0,0,.1F,2,1,0,0,0,0,1,3,1,0,0,c.get("sea_pickles").getAsInt(),0,true);
   var after=Ps4BiomeDecorations.decorate(w.kind,w.variant,0.0F,w,random,s,p[0],p[1],p[2]);
   String label="original ocean/ice wrapper "+w.kind+" seed="+c.get("seed")+" variant="+w.variant+" origin="+c.get("origin")+" height="+w.heightProfile+" outcome="+w.outcome+" pickles="+s.seaPickles();
   assertEquals(label+" ordered events",c.getAsJsonArray("events"),w.events);assertEquals(label+" no noise calls",c.getAsJsonArray("noise_queries"),w.noiseQueries);assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());assertEquals(label+" retained settings",s,after);
   if(w.kind==Ps4BiomeDecorations.Biome.ICE&&w.variant!=0)assertEquals(label+" base last","base",w.events.get(w.events.size()-1).getAsJsonArray().get(0).getAsString());else assertEquals(label+" base first","base",w.events.get(0).getAsJsonArray().get(0).getAsString());
   for(JsonElement row:w.events){JsonArray a=row.getAsJsonArray();if(a.get(0).getAsString().equals("feature")&&a.get(1).getAsString().equals("SEA_PICKLES")&&w.heightProfile.equals("zero")){assertEquals(label+" absolute pickle Y",0,a.get(3).getAsInt());absolutePickles++;}}
   events+=w.events.size();reached.addAll(w.features);
  }
  assertEquals(EnumSet.of(Ps4BiomeDecorations.Feature.ICEBERG,Ps4BiomeDecorations.Feature.BLUE_ICE,Ps4BiomeDecorations.Feature.ICE_SPIKE,Ps4BiomeDecorations.Feature.ICE_PATCH,Ps4BiomeDecorations.Feature.CORAL,Ps4BiomeDecorations.Feature.CORAL_CRUST,Ps4BiomeDecorations.Feature.CORAL_HANG,Ps4BiomeDecorations.Feature.SEA_ANEMONE,Ps4BiomeDecorations.Feature.SEA_PICKLES),reached);assertTrue("substantial wrapper coverage",events>100000);assertTrue("zero-height pickle samples across shifted origins",absolutePickles>50);
 }
}

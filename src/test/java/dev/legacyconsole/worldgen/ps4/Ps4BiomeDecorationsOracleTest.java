package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4BiomeDecorationsOracleTest {
 @Test public void originalBiomeWrappersSelectorsFeatureOrderAndRngMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-biome-wrappers.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(5304,cases.size());Set<Ps4BiomeDecorations.Feature> features=EnumSet.noneOf(Ps4BiomeDecorations.Feature.class);int events=0,wellAndFossil=0;
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();World world=new World(c);Random random=new Random(c.get("seed").getAsLong());int[] p=world.origin;
   var settings=Ps4BiomeDecorations.decorate(world.kind,world.variant,c.get("temperature").getAsFloat(),world,random,Ps4BiomeDecorator.Settings.defaults(),p[0],p[1],p[2]);
   String label="original biome wrapper "+world.kind+" variant="+world.variant+" seed="+c.get("seed")+" origin="+c.get("origin")+" height="+world.heightProfile+" outcome="+world.outcome+" noise="+c.get("noise_override");
   assertEquals(label+" ordered events",c.getAsJsonArray("events"),world.events);assertEquals(label+" original noise queries/bits",c.getAsJsonArray("noise_queries"),world.noiseQueries);
   assertEquals(label+" flowers",c.get("flowers_after").getAsInt(),settings.flowers());assertEquals(label+" grass",c.get("grass_after").getAsInt(),settings.grass());assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());
   features.addAll(world.features);events+=world.events.size();if(world.features.containsAll(Set.of(Ps4BiomeDecorations.Feature.DESERT_WELL,Ps4BiomeDecorations.Feature.FOSSIL)))wellAndFossil++;
  }
  assertEquals("all supplied features of these ten wrappers reached",EnumSet.of(Ps4BiomeDecorations.Feature.DOUBLE_PLANT,Ps4BiomeDecorations.Feature.BOULDER,Ps4BiomeDecorations.Feature.BERRY_BUSH,Ps4BiomeDecorations.Feature.MELON,Ps4BiomeDecorations.Feature.VINES,Ps4BiomeDecorations.Feature.BAMBOO,Ps4BiomeDecorations.Feature.PODZOL,Ps4BiomeDecorations.Feature.DESERT_WELL,Ps4BiomeDecorations.Feature.FOSSIL,Ps4BiomeDecorations.Feature.HUGE_MUSHROOM,Ps4BiomeDecorations.Feature.SILVERFISH),features);assertTrue("large sequence and simultaneous rare-branch coverage",events>100000&&wellAndFossil>=3);
 }
 static class World implements Ps4BiomeDecorations.Access {
  final JsonObject input;final Ps4BiomeDecorations.Biome kind;final int variant;final int[] origin=new int[3];final String heightProfile,outcome;
  final JsonArray events=new JsonArray(),noiseQueries=new JsonArray();final Set<Ps4BiomeDecorations.Feature> features=EnumSet.noneOf(Ps4BiomeDecorations.Feature.class);
  static final Ps4SurfaceNoise NOISE=new Ps4SurfaceNoise(new Random(2345),1);
  World(JsonObject c){input=c;kind=Ps4BiomeDecorations.Biome.valueOf(c.get("kind").getAsString());variant=c.get("variant").getAsInt();for(int i=0;i<3;i++)origin[i]=c.getAsJsonArray("origin").get(i).getAsInt();heightProfile=c.get("height_profile").getAsString();outcome=c.get("outcome").getAsString();}
  static JsonArray row(Object... values){JsonArray row=new JsonArray();for(Object v:values)if(v==null)row.add(JsonNull.INSTANCE);else if(v instanceof JsonElement e)row.add(e);else if(v instanceof Number n)row.add(new JsonPrimitive(n));else if(v instanceof Boolean b)row.add(b);else row.add(new JsonPrimitive(v.toString()));return row;}
  int heightAt(int x,int z){return switch(heightProfile){case "zero"->0;case "one"->1;case "flat"->64;default->32+((3*x+z)&31);};}
  boolean outcome(long draw){return outcome.equals("always")||outcome.equals("mixed")&&(draw&1)!=0;}
  public int height(int x,int y,int z){int h=heightAt(x,z);events.add(row("height",x,y,z,h));return h;}
  public int heightXZ(int x,int z){int h=heightAt(x,z);events.add(row("height_xz",x,z,h));return h;}
  public double biomeNoise(double x,double z){double value=input.get("noise_override").isJsonNull()?NOISE.getValue(x,z):input.get("noise_override").getAsDouble();noiseQueries.add(row(x,z,value));return value;}
  public void baseDecoration(Random random,Ps4BiomeDecorator.Settings settings,int x,int y,int z){events.add(row("base",x,y,z,settings.flowers(),settings.grass(),random.nextLong()));}
  public boolean decoration(Ps4BiomeDecorations.Feature feature,Random random,int x,int y,int z,int state){features.add(feature);Long draw=feature==Ps4BiomeDecorations.Feature.DESERT_WELL?null:random.nextLong();events.add(row("feature",feature.name(),x,y,z,state,draw));return outcome(draw==null?0:draw);}
  public int block(int x,int y,int z){int value=Math.floorMod(x+y+z,3)==0?1:3;events.add(row("block",x,y,z,value));return value;}
  public void setBlock(int x,int y,int z,int state,int flags){events.add(row("set",x,y,z,state,flags));}
  public Ps4BiomeDecorator.Tree tree(Random random){
   Ps4BiomeTrees.Biome biome=Ps4BiomeTrees.Biome.FOREST;var selection=Ps4BiomeTrees.select(biome,variant,random);JsonObject descriptor=new JsonObject();
   descriptor.addProperty("kind",selection.kind().name());descriptor.addProperty("base_height",selection.baseHeight());descriptor.addProperty("extra_range",selection.extraRange());descriptor.addProperty("log",selection.log());descriptor.addProperty("leaves",selection.leaves());descriptor.addProperty("jungle",selection.jungle());descriptor.addProperty("tall",selection.tall());descriptor.addProperty("updates",selection.updates());
   return new Ps4BiomeDecorator.Tree(){
    public void prepare(){events.add(row("prepare",descriptor));}
    public boolean place(Ps4BiomeDecorator.Access world,Random random,int x,int y,int z){long draw=random.nextLong();events.add(row("tree",descriptor,x,y,z,draw));return outcome(draw);}
    public void postPlace(Ps4BiomeDecorator.Access world,Random random,int x,int y,int z){if(selection.kind()==Ps4BiomeTrees.Kind.MEGA_PINE)events.add(row("post",descriptor,x,y,z,random.nextLong()));}
   };
  }
  public void ores(Random random,int x,int y,int z){throw new AssertionError("Unexpected main-decoration call");}
  public int topSolidOrLiquid(int x,int y,int z){throw new AssertionError("Unexpected main-decoration call");}
  public boolean empty(int x,int y,int z){throw new AssertionError("Unexpected main-decoration call");}
  public Ps4BiomeDecorator.Grass grass(Random random){throw new AssertionError("Unexpected main-decoration call");}
  public int flower(Random random,int x,int y,int z){throw new AssertionError("Unexpected main-decoration call");}
  public boolean airMaterial(int state){throw new AssertionError("Unexpected main-decoration call");}
  public void place(Ps4BiomeDecorator.Feature feature,Random random,int x,int y,int z,int state){throw new AssertionError("Unexpected main-decoration call");}
 }
}

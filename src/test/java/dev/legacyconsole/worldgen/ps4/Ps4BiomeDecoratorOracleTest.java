package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4BiomeDecoratorOracleTest {
 @Test public void originalConstructorDefaultsAndCompleteMainSequenceMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-biome-decoration.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){
   fixture=new JsonParser().parse(reader).getAsJsonObject();
  }
  assertEquals("actual original constructor defaults",settings(fixture.getAsJsonObject("defaults")),Ps4BiomeDecorator.Settings.defaults());
  JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(480,cases.size());
  Set<String> placed=new HashSet<>();int events=0;
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();World world=new World(c.get("height_profile").getAsString());Random random=new Random(c.get("seed").getAsLong());int[] p=ints(c.getAsJsonArray("origin"));
   Ps4BiomeDecorator.decorate(world,random,settings(c.getAsJsonObject("settings")),p[0],p[1],p[2]);
   String label="original decorator seed="+c.get("seed")+" mode="+c.get("mode")+" height="+world.profile+" origin="+c.get("origin");
   JsonArray expected=c.getAsJsonArray("events");assertEquals(label+" call count",expected.size(),world.events.size());
   for(int i=0;i<expected.size();i++)assertEquals(label+" call #"+i,expected.get(i).toString(),world.events.get(i).toString());
   assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());
   placed.addAll(world.placed);events+=world.events.size();
  }
  assertEquals("all sixteen dispatched feature types exercised",16,placed.size());assertTrue("substantial sequence coverage",events>30000);
 }
 private static Ps4BiomeDecorator.Settings settings(JsonObject o){return new Ps4BiomeDecorator.Settings(i(o,"waterlilies"),i(o,"trees"),o.get("extraTreeChance").getAsFloat(),i(o,"flowers"),i(o,"grass"),i(o,"deadBushes"),i(o,"mushrooms"),i(o,"reeds"),i(o,"cacti"),i(o,"gravel"),i(o,"sand"),i(o,"clay"),i(o,"hugeMushrooms"),i(o,"kelp"),i(o,"seagrass"),o.get("springs").getAsBoolean());}
 private static int i(JsonObject o,String key){return o.get(key).getAsInt();}
 private static int[] ints(JsonArray a){int[] result=new int[a.size()];for(int n=0;n<result.length;n++)result[n]=a.get(n).getAsInt();return result;}
 private static final class World implements Ps4BiomeDecorator.Access {
  final String profile;final JsonArray events=new JsonArray();final Set<String> placed=new HashSet<>();
  World(String profile){this.profile=profile;}
  void event(Object... values){JsonArray row=new JsonArray();for(Object value:values)if(value instanceof String s)row.add(s);else if(value instanceof Boolean b)row.add(b);else row.add((Number)value);events.add(row);}
  public void ores(Random random,int x,int y,int z){event("ores",random.nextLong());}
  public int height(int x,int y,int z){int h=switch(profile){case "zero"->0;case "negative"->-32;case "one"->1;case "flat"->64;default->32+((3*x+z)&31);};event("height",x,y,z,h);return h;}
  public int topSolidOrLiquid(int x,int y,int z){int h=62+((x^z)&3);event("top",x,y,z,h);return h;}
  public boolean empty(int x,int y,int z){boolean empty=y>=62+((x^z)&3)+1;event("empty",x,y,z,empty);return empty;}
  boolean place(String name,Random random,int x,int y,int z,int state){long value=random.nextLong();event("place",name,x,y,z,state,value);return (value&1)!=0;}
  public void place(Ps4BiomeDecorator.Feature feature,Random random,int x,int y,int z,int state){String name=feature.name().toLowerCase(Locale.ROOT);placed.add(name);place(name,random,x,y,z,state);}
  public Ps4BiomeDecorator.Tree tree(Random random){int selection=random.nextInt(3);event("tree",selection);String name="tree"+selection;return new Ps4BiomeDecorator.Tree(){
   public void prepare(){event("prepare",name);}
   public boolean place(Ps4BiomeDecorator.Access world,Random r,int x,int y,int z){return World.this.place(name,r,x,y,z,0);}
   public void postPlace(Ps4BiomeDecorator.Access world,Random r,int x,int y,int z){event("post",name,x,y,z,r.nextLong());}
  };}
  public Ps4BiomeDecorator.Grass grass(Random random){int selection=random.nextInt(2);event("grass",selection);return (world,r,x,y,z)->place("grass"+selection,r,x,y,z,31|((selection+1)<<16));}
  public int flower(Random random,int x,int y,int z){int state=new int[]{0,37,38,38|(8<<16)}[random.nextInt(4)];event("flower",x,y,z,state);return state;}
  public boolean airMaterial(int state){return (state&65535)==0;}
 }
}

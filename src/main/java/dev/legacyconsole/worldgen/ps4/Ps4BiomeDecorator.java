package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** BiomeDecorator::decorateMain, mcps4 0x87760. Biome-specific selectors and
 * feature implementations are supplied explicitly, preserving their shared
 * population RNG and virtual-call order. */
public final class Ps4BiomeDecorator {
 public enum Feature {SAND,CLAY,GRAVEL,HUGE_MUSHROOM,FLOWER,DEAD_BUSH,WATERLILY,
  BROWN_MUSHROOM,RED_MUSHROOM,REEDS,PUMPKIN,CACTUS,KELP,SEAGRASS,WATER_SPRING,LAVA_SPRING}
 public record Settings(int waterlilies,int trees,float extraTreeChance,int flowers,int grass,
  int deadBushes,int mushrooms,int reeds,int cacti,int gravel,int sand,int clay,
  int hugeMushrooms,int kelp,int seaPickles,int seagrass,boolean springs){
  /** Main-decoration configuration with no separate warm-ocean pickle attempts. */
  public Settings(int waterlilies,int trees,float extraTreeChance,int flowers,int grass,int deadBushes,int mushrooms,int reeds,int cacti,int gravel,int sand,int clay,int hugeMushrooms,int kelp,int seagrass,boolean springs){this(waterlilies,trees,extraTreeChance,flowers,grass,deadBushes,mushrooms,reeds,cacti,gravel,sand,clay,hugeMushrooms,kelp,0,seagrass,springs);}
  /** Original constructor 0x86c00; its packed constants are 0xe74780/88. */
  public static Settings defaults(){return new Settings(0,0,.1f,2,1,0,0,0,0,1,3,1,0,0,0,true);}
 }
 public interface Tree {
  void prepare();
  boolean place(Access world,Random random,int x,int y,int z);
  void postPlace(Access world,Random random,int x,int y,int z);
 }
 public interface Grass {void place(Access world,Random random,int x,int y,int z);}
 public interface Access {
  void ores(Random random,int originX,int originY,int originZ);
  int height(int x,int y,int z);
  int topSolidOrLiquid(int x,int y,int z);
  boolean empty(int x,int y,int z);
  Tree tree(Random random);
  Grass grass(Random random);
  /** A legacy flower state; material-air selections skip the scatter call. */
  int flower(Random random,int x,int y,int z);
  boolean airMaterial(int state);
  void place(Feature feature,Random random,int x,int y,int z,int state);
 }
 private Ps4BiomeDecorator(){}

 public static void decorate(Access world,Random random,Settings s,int x,int y,int z){
  world.ores(random,x,y,z);
  for(int i=0;i<s.sand;i++){int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8;world.place(Feature.SAND,random,wx,world.topSolidOrLiquid(wx,y,wz),wz,12);}
  for(int i=0;i<s.clay;i++){int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8;world.place(Feature.CLAY,random,wx,world.topSolidOrLiquid(wx,y,wz),wz,82);}
  for(int i=0;i<s.gravel;i++){int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8;world.place(Feature.GRAVEL,random,wx,world.topSolidOrLiquid(wx,y,wz),wz,13);}
  int trees=s.trees+(random.nextFloat()<s.extraTreeChance?1:0);
  for(int i=0;i<trees;i++){
   int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8;
   Tree tree=world.tree(random);tree.prepare();int wy=world.height(wx,y,wz);
   if(tree.place(world,random,wx,wy,wz))tree.postPlace(world,random,wx,wy,wz);
  }
  for(int i=0;i<s.hugeMushrooms;i++){int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8;world.place(Feature.HUGE_MUSHROOM,random,wx,world.height(wx,y,wz),wz,0);}
  for(int i=0;i<s.flowers;i++){
   int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8,h=world.height(wx,y,wz);
   if(h>-32){int wy=y+random.nextInt(h+32),state=world.flower(random,wx,wy,wz);if(!world.airMaterial(state))world.place(Feature.FLOWER,random,wx,wy,wz,state);}
  }
  for(int i=0;i<s.grass;i++){
   int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8,h=world.height(wx,y,wz);
   if(h>0){int wy=y+random.nextInt(h*2);world.grass(random).place(world,random,wx,wy,wz);}
  }
  for(int i=0;i<s.seagrass;i++){int wx=x+random.nextInt(16),wz=z+random.nextInt(16);world.place(Feature.SEAGRASS,random,wx,y+54,wz,0);}
  for(int i=0;i<s.deadBushes;i++)randomHeight(world,random,Feature.DEAD_BUSH,x,y,z,32);
  for(int i=0;i<s.waterlilies;i++){
   int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8,h=world.height(wx,y,wz);
   if(h>0){int wy=y+random.nextInt(h*2);while(wy>0&&world.empty(wx,wy-1,wz))wy--;world.place(Feature.WATERLILY,random,wx,wy,wz,111);}
  }
  for(int i=0;i<s.mushrooms;i++){
   if(random.nextInt(4)==0){int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8;world.place(Feature.BROWN_MUSHROOM,random,wx,world.height(wx,y,wz),wz,39);}
   if(random.nextInt(8)==0)randomHeight(world,random,Feature.RED_MUSHROOM,x,y,z,40);
  }
  if(random.nextInt(4)==0)randomHeight(world,random,Feature.BROWN_MUSHROOM,x,y,z,39);
  if(random.nextInt(8)==0)randomHeight(world,random,Feature.RED_MUSHROOM,x,y,z,40);
  if(random.nextInt(18)==0)for(int i=0;i<s.kelp;i++){int wx=x+random.nextInt(35),wz=z+random.nextInt(35);world.place(Feature.KELP,random,wx,y+43,wz,0);}
  for(int i=0;i<s.reeds;i++)randomHeight(world,random,Feature.REEDS,x,y,z,83);
  for(int i=0;i<10;i++)randomHeight(world,random,Feature.REEDS,x,y,z,83);
  if(random.nextInt(32)==0)randomHeight(world,random,Feature.PUMPKIN,x,y,z,257);
  for(int i=0;i<s.cacti;i++)randomHeight(world,random,Feature.CACTUS,x,y,z,81);
  if(s.springs){
   for(int i=0;i<50;i++){int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8,wy=y+random.nextInt(random.nextInt(120)+8);world.place(Feature.WATER_SPRING,random,wx,wy,wz,8);}
   for(int i=0;i<20;i++){int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8,wy=y+random.nextInt(random.nextInt(random.nextInt(112)+8)+8);world.place(Feature.LAVA_SPRING,random,wx,wy,wz,10);}
  }
 }
 private static void randomHeight(Access world,Random random,Feature feature,int x,int y,int z,int state){
  int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8,h=world.height(wx,y,wz);
  if(h>0)world.place(feature,random,wx,y+random.nextInt(h*2),wz,state);
 }
}

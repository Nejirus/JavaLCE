package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** Recovered biome-specific decoration around the shared BiomeDecorator pass. */
public final class Ps4BiomeDecorations {
 public enum Biome {FOREST,SAVANNA,MUTATED_SAVANNA,PLAINS,SWAMP,TAIGA,JUNGLE,BAMBOO_JUNGLE,DESERT,HILLS,FROZEN_OCEAN,WARM_OCEAN,ICE}
 public enum Feature {DOUBLE_PLANT,BOULDER,BERRY_BUSH,MELON,VINES,BAMBOO,PODZOL,DESERT_WELL,FOSSIL,HUGE_MUSHROOM,SILVERFISH,ICEBERG,BLUE_ICE,ICE_SPIKE,ICE_PATCH,CORAL,CORAL_CRUST,CORAL_HANG,SEA_ANEMONE,SEA_PICKLES}
 public interface Access extends Ps4BiomeDecorator.Access {
  void baseDecoration(Random random,Ps4BiomeDecorator.Settings settings,int x,int y,int z);
  boolean decoration(Feature feature,Random random,int x,int y,int z,int state);
  int heightXZ(int x,int z);
  double biomeNoise(double x,double z);
  int block(int x,int y,int z);
  void setBlock(int x,int y,int z,int state,int flags);
 }
 private Ps4BiomeDecorations(){}
 public static Ps4BiomeDecorator.Settings decorate(Biome biome,int variant,float temperature,Access world,Random random,Ps4BiomeDecorator.Settings settings,int x,int y,int z){
  switch(biome){
   case FOREST->{
    if(variant==3)roofGrid(world,random,x,y,z);
    int count=random.nextInt(5)-3+(variant==1?2:0);
    for(int i=0;i<count;i++){
     int state=175|(new int[]{1,4,5}[random.nextInt(3)]<<16);
     for(int attempt=0;attempt<5;attempt++){
      int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8,wy=random.nextInt(world.height(wx,y,wz)+32);
      if(world.decoration(Feature.DOUBLE_PLANT,random,wx,wy,wz,state))break;
     }
    }
    world.baseDecoration(random,settings,x,y,z);
   }
   case SAVANNA->{doublePlants(world,random,x,y,z,7,2);world.baseDecoration(random,settings,x,y,z);}
   case MUTATED_SAVANNA->world.baseDecoration(random,settings,x,y,z);
   case PLAINS->{
    if(world.biomeNoise((double)(x+8)/200.0,(double)(z+8)/200.0)>=-0.8){settings=plantCounts(settings,4,10);doublePlants(world,random,x,y,z,7,2);}else settings=plantCounts(settings,15,5);
    if((variant&255)!=0)doublePlants(world,random,x,y,z,10,0);
    world.baseDecoration(random,settings,x,y,z);
   }
   case SWAMP->{world.baseDecoration(random,settings,x,y,z);if(random.nextInt(64)==0)world.decoration(Feature.FOSSIL,random,x,y,z,0);}
   case TAIGA->{
    if(variant==1||variant==2){
     for(int i=random.nextInt(3);i>0;i--){int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8;world.decoration(Feature.BOULDER,random,wx,world.height(wx,y,wz),wz,0);}
    }else{
     int group=0.2<=(double)temperature?(temperature<1.0f?2:3):1;
     if(group!=1||random.nextInt(12)==0){int wz=z+random.nextInt(16),wx=x+random.nextInt(16);world.decoration(Feature.BERRY_BUSH,random,wx,world.height(wx,y,wz),wz,363);}
    }
    doublePlants(world,random,x,y,z,7,3);world.baseDecoration(random,settings,x,y,z);
   }
   case JUNGLE->{
    world.baseDecoration(random,settings,x,y,z);melon(world,random,x,y,z);vines(world,random,x,y,z);
    for(int i=0;i<16;i++){
     int wx=x+random.nextInt(16),wz=z+random.nextInt(16),height=world.height(wx,y,wz),wy=y+(height>0?random.nextInt(height*2):0);
     world.decoration(Feature.BAMBOO,random,wx,wy,wz,0);
    }
   }
   case BAMBOO_JUNGLE->{
    world.baseDecoration(random,settings,x,y,z);
    double density=((double)(float)world.biomeNoise((double)(x/80),(double)(z/80))+0.3)*160.0;
    int count=(int)density;if((float)count<(float)density)count++;count=Math.min(Math.max(count,15),160);
    int dx=random.nextInt(16);
    for(int i=0;i<count;i++){
     int wx=x+dx,wz=z+random.nextInt(16),wy=y+world.heightXZ(wx,wz);
     if(world.decoration(Feature.BAMBOO,random,wx,wy,wz,0)&&random.nextFloat()*1.0f<0.2f)world.decoration(Feature.PODZOL,random,wx,wy,wz,0);
     dx=random.nextInt(16);
    }
    int wx=x+dx+8,wz=z+random.nextInt(16)+8,height=world.height(wx,y,wz),wy=y+(height>0?random.nextInt(height*2):0);
    world.decoration(Feature.MELON,random,wx,wy,wz,0);vines(world,random,x,y,z);
   }
   case DESERT->{
    world.baseDecoration(random,settings,x,y,z);
    if(random.nextInt(1000)==0){int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8;world.decoration(Feature.DESERT_WELL,random,wx,world.height(wx,y,wz)+1,wz,0);}
    if(random.nextInt(64)==0)world.decoration(Feature.FOSSIL,random,x,y,z,0);
   }
   case HILLS->{
    world.baseDecoration(random,settings,x,y,z);
    int emeralds=3+random.nextInt(6);
    for(int i=0;i<emeralds;i++){
     int wx=x+random.nextInt(16),wy=y+random.nextInt(28)+4,wz=z+random.nextInt(16);
     if((world.block(wx,wy,wz)&65535)==1)world.setBlock(wx,wy,wz,129,2);
    }
    for(int i=0;i<7;i++){int wx=x+random.nextInt(16),wy=y+random.nextInt(64),wz=z+random.nextInt(16);world.decoration(Feature.SILVERFISH,random,wx,wy,wz,97);}
   }
   case FROZEN_OCEAN->{
    world.baseDecoration(random,settings,x,y,z);
    if(random.nextInt(8)==0){int wx=x+random.nextInt(8)+12,wz=z+random.nextInt(8)+12;world.decoration(Feature.ICEBERG,random,wx,world.height(wx,y,wz),wz,0);}
    int count=random.nextInt(20);
    for(int i=0;i<count;i++){int wx=x+random.nextInt(16)+8,wy=y+random.nextInt(32)+30,wz=z+random.nextInt(16)+8;world.decoration(Feature.BLUE_ICE,random,wx,wy,wz,0);}
   }
   case ICE->{
    if((variant&255)!=0){
     for(int i=0;i<3;i++){int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8;world.decoration(Feature.ICE_SPIKE,random,wx,world.height(wx,y,wz),wz,0);}
     for(int i=0;i<2;i++){int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8;world.decoration(Feature.ICE_PATCH,random,wx,world.height(wx,y,wz),wz,0);}
    }
    world.baseDecoration(random,settings,x,y,z);
   }
   case WARM_OCEAN->{
    world.baseDecoration(random,settings,x,y,z);
    for(int i=0;i<4;i++){int wx=x+random.nextInt(16),wz=z+random.nextInt(16);world.decoration(Feature.CORAL,random,wx,y+45,wz,0);}
    if(random.nextInt(3)==0){int wx=x+random.nextInt(10),wz=z+random.nextInt(10);world.decoration(Feature.CORAL_CRUST,random,wx,y+45,wz,0);}
    for(int i=0;i<16;i++){int wx=x+random.nextInt(16),wz=z+random.nextInt(16);world.decoration(Feature.CORAL_HANG,random,wx,y+62,wz,0);}
    for(int i=0;i<20;i++){int wx=x+random.nextInt(16),wz=z+random.nextInt(16);world.decoration(Feature.SEA_ANEMONE,random,wx,y+62,wz,0);}
    if(random.nextInt(6)==0)for(int i=0;i<settings.seaPickles();i++){
     int wx=x+random.nextInt(24),wz=z+random.nextInt(24),height=world.heightXZ(wx,wz),wy=height==0?0:random.nextInt(height*2);
     // MutableBlockPos::setY replaces Y; this is an absolute sampled height.
     world.decoration(Feature.SEA_PICKLES,random,wx,wy,wz,0);
    }
   }
  }
  return settings;
 }
 private static void roofGrid(Access world,Random random,int x,int y,int z){
  for(int gx=0;gx<4;gx++)for(int gz=0;gz<4;gz++){
   int wx=x+random.nextInt(3)+gx*4+9,wz=z+random.nextInt(3)+gz*4+9,wy=world.height(wx,y,wz);
   if(random.nextInt(20)==0)world.decoration(Feature.HUGE_MUSHROOM,random,wx,wy,wz,0);
   else{var tree=world.tree(random);tree.prepare();if(tree.place(world,random,wx,wy,wz))tree.postPlace(world,random,wx,wy,wz);}
  }
 }
 private static void doublePlants(Access world,Random random,int x,int y,int z,int count,int variant){
  for(int i=0;i<count;i++){int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8,wy=y+random.nextInt(world.height(wx,y,wz)+32);world.decoration(Feature.DOUBLE_PLANT,random,wx,wy,wz,175|(variant<<16));}
 }
 private static void melon(Access world,Random random,int x,int y,int z){int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8,height=world.height(wx,y,wz),wy=y+(height>0?random.nextInt(height*2):0);world.decoration(Feature.MELON,random,wx,wy,wz,0);}
 private static void vines(Access world,Random random,int x,int y,int z){for(int i=0;i<50;i++){int wx=x+random.nextInt(16)+8,wz=z+random.nextInt(16)+8;world.decoration(Feature.VINES,random,wx,y+64,wz,0);}}
 private static Ps4BiomeDecorator.Settings plantCounts(Ps4BiomeDecorator.Settings s,int flowers,int grass){return new Ps4BiomeDecorator.Settings(s.waterlilies(),s.trees(),s.extraTreeChance(),flowers,grass,s.deadBushes(),s.mushrooms(),s.reeds(),s.cacti(),s.gravel(),s.sand(),s.clay(),s.hugeMushrooms(),s.kelp(),s.seaPickles(),s.seagrass(),s.springs());}
}

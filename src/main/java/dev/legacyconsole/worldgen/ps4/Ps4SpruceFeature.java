package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** SpruceFeature::place, mcps4 0x672fb0. */
public final class Ps4SpruceFeature {
 private Ps4SpruceFeature(){}
 public static boolean place(Ps4TreeTrunk.Access world,Random random,int x,int y,int z,int baseHeight,int log,int leaves,boolean updates){
  int height=baseHeight+random.nextInt(4),bare=random.nextInt(2),maximumRadius=2+random.nextInt(2);
  if(y<1||y+height>255)return false;
  if(world.hasGenerationRules()&&world.intersectsGenerationRule(x-maximumRadius,y-1,z-maximumRadius,x+maximumRadius,y+height,z+maximumRadius))return false;
  for(int level=0;level<=height+1;level++){
   int radius=level>bare?maximumRadius:0,wy=y+level;
   for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++){
    if(wy<0||wy>=256)return false;
    int old=world.block(wx,wy,wz);if(!world.airMaterial(old)&&!world.leavesMaterial(old))return false;
   }
  }
  int soil=world.block(x,y-1,z)&65535;
  if(soil!=2&&soil!=3&&soil!=60||y>=255-height)return false;
  Ps4TreeRules.dirt(world,x,y-1,z,updates);
  int trunkHeight=height-random.nextInt(3);
  if(!Ps4TreeTrunk.place(world,random,x,y,z,trunkHeight,log,false,updates))return true;
  int radius=random.nextInt(2),threshold=1,reset=0,flags=updates?3:2;
  for(int layer=0;layer<height-bare;layer++){
   int wy=y+height-layer;
   for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++){
    if(radius>0&&Math.abs(wx-x)==radius&&Math.abs(wz-z)==radius)continue;
    if(!world.solidRender(world.block(wx,wy,wz)))world.setBlock(wx,wy,wz,leaves,flags);
   }
   if(radius<threshold)radius++;else{radius=reset;reset=1;threshold=Math.min(threshold+1,maximumRadius);}
  }
  return true;
 }
}

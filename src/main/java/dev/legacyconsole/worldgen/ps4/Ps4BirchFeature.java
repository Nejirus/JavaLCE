package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** BirchFeature::place, mcps4 0x8f390. */
public final class Ps4BirchFeature {
 private Ps4BirchFeature(){}
 public static boolean place(Ps4TreeTrunk.Access world,Random random,int x,int y,int z,int baseHeight,int log,int leaves,boolean tall,boolean updates){
  int height=baseHeight+random.nextInt(3);if(tall)height+=random.nextInt(7);
  if(y<1||y+height>255)return false;
  for(int wy=y;wy<=y+height+1;wy++){
   int radius=wy==y?0:1;if(wy>=y+height-1)radius=2;
   for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++)if(wy<0||wy>=256||!Ps4TreeRules.canGrowInto(world,world.block(wx,wy,wz)))return false;
  }
  int ground=world.block(x,y-1,z)&65535;
  if(ground!=2&&ground!=3&&ground!=60||y>=255-height)return false;
  if(world.hasGenerationRules()&&world.intersectsGenerationRule(x-3,y-1,z-3,x+3,y+height,z+3))return false;
  Ps4TreeRules.dirt(world,x,y-1,z,updates);
  if(!Ps4TreeTrunk.place(world,random,x,y,z,height,log,false,updates))return true;
  int flags=updates?3:2;
  for(int wy=y+height-3;wy<=y+height;wy++){
   int dy=wy-y-height,radius=1-dy/2;
   for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++){
    if(Math.abs(wx-x)==radius&&Math.abs(wz-z)==radius&&(random.nextInt(2)==0||dy==0))continue;
    int state=world.block(wx,wy,wz);if(world.airMaterial(state)||world.leavesMaterial(state))world.setBlock(wx,wy,wz,leaves,flags);
   }
  }
  return true;
 }
}

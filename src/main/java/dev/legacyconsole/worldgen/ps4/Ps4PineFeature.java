package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** PineFeature::place, mcps4 0x5345c0. Its original 128-block generation
 * ceiling differs from the 256-block checks of several other tree features. */
public final class Ps4PineFeature {
 private Ps4PineFeature(){}
 public static boolean place(Ps4PlantRules.Access world,Random random,int x,int y,int z,int baseHeight,int log,int leaves,boolean updates){
  int height=baseHeight+random.nextInt(5),trunk=height-random.nextInt(2)-3,topRadius=1+random.nextInt(height-trunk+1);
  if(y<1||y+height>127)return false;
  if(world.hasGenerationRules()&&world.intersectsGenerationRule(x-topRadius,y-1,z-topRadius,x+topRadius,y+height,z+topRadius))return false;
  for(int wy=y;wy<=y+height+1;wy++){
   int radius=wy-y<trunk?0:topRadius;
   for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++){
    if(wy<0||wy>=128)return false;
    if(!Ps4TreeRules.canGrowInto(world,world.block(wx,wy,wz)))return false;
   }
  }
  int ground=world.block(x,y-1,z)&65535;
  if(ground!=2&&ground!=3||y>=127-height)return false;
  Ps4TreeRules.dirt(world,x,y-1,z,updates);
  int radius=0,flags=updates?3:2;
  for(int wy=y+height;wy>=y+trunk;wy--){
   for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++){
    if(radius>0&&Math.abs(wx-x)==radius&&Math.abs(wz-z)==radius)continue;
    if(!world.solidRender(world.block(wx,wy,wz)))world.setBlock(wx,wy,wz,leaves,flags);
   }
   if(radius>=1&&wy==y+trunk+1)radius--;else if(radius<topRadius)radius++;
  }
  for(int level=0;level<height-1;level++){
   int state=world.block(x,y+level,z);
   if(world.airMaterial(state)||world.leavesMaterial(state))world.setBlock(x,y+level,z,log,flags);
  }
  return true;
 }
}

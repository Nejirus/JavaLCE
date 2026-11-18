package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** SwampTreeFeature::place, mcps4 0x6ca560 (128-block ceiling). */
public final class Ps4SwampTreeFeature {
 private static final int[][] VINES={{-1,0,8},{1,0,2},{0,-1,1},{0,1,4}};
 private Ps4SwampTreeFeature(){}
 public static boolean place(Ps4PlantRules.Access world,Random random,int x,int y,int z,int baseHeight,int log,int leaves,boolean updates){
  int height=baseHeight+random.nextInt(4);
  while(world.waterMaterial(world.block(x,y-1,z)))y--;
  if(y<1||y+height>=128)return false;
  if(world.hasGenerationRules()&&world.intersectsGenerationRule(x-3,y-1,z-3,x+3,y+height,z+3))return false;
  for(int wy=y;wy<=y+height+1;wy++){
   int radius=wy==y?0:1;if(wy>=y+height-1)radius=3;
   for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++){
    if(wy<0||wy>=128)return false;
    int old=world.block(wx,wy,wz),id=old&65535;
    if(!world.airMaterial(old)&&!world.leavesMaterial(old)&&!((id==8||id==9)&&wy<=y))return false;
   }
  }
  int ground=world.block(x,y-1,z)&65535;
  if(ground!=2&&ground!=3||y>=127-height)return false;
  Ps4TreeRules.dirt(world,x,y-1,z,updates);int flags=updates?3:2;
  for(int wy=y+height-3;wy<=y+height;wy++){
   int dy=wy-y-height,radius=2-dy/2;
   for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++){
    if(Math.abs(wx-x)==radius&&Math.abs(wz-z)==radius&&(random.nextInt(2)==0||dy==0))continue;
    if(!world.solidRender(world.block(wx,wy,wz)))world.setBlock(wx,wy,wz,leaves,flags);
   }
  }
  for(int level=0;level<height;level++){
   int old=world.block(x,y+level,z),id=old&65535;
   if(world.airMaterial(old)||world.leavesMaterial(old)||id==8||id==9)world.setBlock(x,y+level,z,log,flags);
  }
  for(int wy=y+height-3;wy<=y+height;wy++){
   int radius=2-(wy-y-height)/2;
   for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++)if(world.leavesMaterial(world.block(wx,wy,wz)))
    for(int[] d:VINES)if(random.nextInt(4)==0&&world.airMaterial(world.block(wx+d[0],wy,wz+d[1])))Ps4TreeFeature.hangingVine(world,wx+d[0],wy,wz+d[1],d[2],updates);
  }
  return true;
 }
}

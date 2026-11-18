package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** SavannaTreeFeature::place, mcps4 0x5c6ec0. */
public final class Ps4SavannaTreeFeature {
 private static final int[][] HORIZONTAL={{0,-1},{1,0},{0,1},{-1,0}};
 private Ps4SavannaTreeFeature(){}
 public static boolean place(Ps4PlantRules.Access world,Random random,int x,int y,int z,int baseHeight,int log,int leaves,boolean updates){
  int height=baseHeight+random.nextInt(3)+random.nextInt(3);
  if(y<1||y+height>=256)return false;
  for(int wy=y;wy<=y+height+1;wy++){
   int radius=wy==y?0:1;if(wy>=y+height-1)radius=2;
   for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++)if(wy<0||wy>=256||!Ps4TreeRules.canGrowInto(world,world.block(wx,wy,wz)))return false;
  }
  int ground=world.block(x,y-1,z)&65535;
  if(ground!=2&&ground!=3||y>=255-height)return false;
  Ps4TreeRules.dirt(world,x,y-1,z,updates);
  int direction=random.nextInt(4),bend=height-random.nextInt(4),remaining=3-random.nextInt(3);
  int wx=x,wz=z,topY=0,flags=updates?3:2;int[] d=HORIZONTAL[direction];
  for(int level=0;level<height;level++){
   if(level>=bend-1&&remaining>0){wx+=d[0];wz+=d[1];remaining--;}
   int wy=y+level,old=world.block(wx,wy,wz);
   if(world.airMaterial(old)||world.leavesMaterial(old)){world.setBlock(wx,wy,wz,log,flags);topY=wy;}
  }
  disk(world,wx,topY,wz,3,leaves,flags);
  square(world,wx,topY+1,wz,leaves,flags);
  leaf(world,wx+2,topY+1,wz,leaves,flags);leaf(world,wx-2,topY+1,wz,leaves,flags);
  leaf(world,wx,topY+1,wz+2,leaves,flags);leaf(world,wx,topY+1,wz-2,leaves,flags);
  int second=random.nextInt(4);
  if(second!=direction){
   int start=bend-2-random.nextInt(2),length=1+random.nextInt(3);wx=x;wz=z;topY=0;d=HORIZONTAL[second];
   for(int level=start;level<height&&length>0;level++,length--){
    if(level<=0)continue;
    wx+=d[0];wz+=d[1];int wy=y+level,old=world.block(wx,wy,wz);
    if(world.airMaterial(old)||world.leavesMaterial(old)){world.setBlock(wx,wy,wz,log,flags);topY=wy;}
   }
   if(topY>0){disk(world,wx,topY,wz,2,leaves,flags);square(world,wx,topY+1,wz,leaves,flags);}
  }
  return true;
 }
 private static void disk(Ps4PlantRules.Access world,int x,int y,int z,int radius,int leaves,int flags){for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)if(Math.abs(dx)!=radius||Math.abs(dz)!=radius)leaf(world,x+dx,y,z+dz,leaves,flags);}
 private static void square(Ps4PlantRules.Access world,int x,int y,int z,int leaves,int flags){for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)leaf(world,x+dx,y,z+dz,leaves,flags);}
 private static void leaf(Ps4PlantRules.Access world,int x,int y,int z,int leaves,int flags){int old=world.block(x,y,z);if(world.airMaterial(old)||world.leavesMaterial(old))world.setBlock(x,y,z,leaves,flags);}
}

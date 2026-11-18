package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** TreeFeature 0x6f5eb0, its validation 0x6f5b60, and hanging vines 0x6f6950. */
public final class Ps4TreeFeature {
 private static final int[][] VINES={{-1,0,8},{1,0,2},{0,-1,1},{0,1,4}};
 // Horizontal plane N,E,S,W and CocoaBlock's original horizontal data.
 private static final int[][] COCOA={{0,-1,2},{1,0,3},{0,1,0},{-1,0,1}};
 private Ps4TreeFeature(){}
 public static boolean place(Ps4TreeTrunk.Access world,Random random,int x,int y,int z,int baseHeight,int log,int leaves,boolean jungle,boolean updates){
  int height=baseHeight+random.nextInt(3);
  if(!validate(world,x,y,z,height,updates))return false;
  if(!Ps4TreeTrunk.place(world,random,x,y,z,height,log,jungle,updates))return true;
  int flags=updates?3:2;
  for(int wy=y+height;wy>=y+height-3;wy--){
   int dy=wy-y-height,radius=1-dy/2;
   for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++){
    if(Math.abs(wx-x)==radius&&Math.abs(wz-z)==radius&&(random.nextInt(2)==0||dy==0))continue;
    int state=world.block(wx,wy,wz);
    if(world.airMaterial(state)||world.leavesMaterial(state)||world.vineMaterial(state))world.setBlock(wx,wy,wz,leaves,flags);
   }
  }
  if(jungle){
   for(int wy=y+height-3;wy<=y+height;wy++){
    int radius=2-(wy-y-height)/2;
    for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++)if(world.leavesMaterial(world.block(wx,wy,wz)))
     for(int[] d:VINES)if(random.nextInt(4)==0&&world.airMaterial(world.block(wx+d[0],wy,wz+d[1])))hangingVine(world,wx+d[0],wy,wz+d[1],d[2],updates);
   }
   if(random.nextInt(5)==0&&height>5)for(int row=0;row<2;row++)for(int[] d:COCOA)if(random.nextInt(4-row)==0){int age=random.nextInt(3);world.setBlock(x-d[0],y+height-5+row,z-d[1],127|((age*4+d[2])<<16),flags);}
  }
  return true;
 }
 public static boolean validate(Ps4PlantRules.Access world,int x,int y,int z,int height,boolean updates){
  if(y<1||y+height>=256)return false;
  if(world.hasGenerationRules()&&world.intersectsGenerationRule(x-2,y-1,z-2,x+2,y+height,z+2))return false;
  for(int wy=y;wy<=y+height+1;wy++){
   int radius=wy==y?0:1;if(wy>=y+height-1)radius=2;
   for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++)if(wy<0||wy>=256||!Ps4TreeRules.canGrowInto(world,world.block(wx,wy,wz)))return false;
  }
  int ground=world.block(x,y-1,z)&65535;
  if(ground!=2&&ground!=3&&ground!=60||y>=255-height)return false;
  Ps4TreeRules.dirt(world,x,y-1,z,updates);return true;
 }
 public static void hangingVine(Ps4PlantRules.Access world,int x,int y,int z,int attachment,boolean updates){
  int state=106|(attachment<<16),flags=updates?3:2;world.setBlock(x,y,z,state,flags);
  int remaining=4;
  // The original probes below the last segment even after using the limit.
  while(world.airMaterial(world.block(x,--y,z))&&remaining>0){world.setBlock(x,y,z,state,flags);remaining--;}
 }
}

package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** Shared giant-tree helpers recovered from mcps4 0x4523a0..0x4529b0. */
public final class Ps4MegaTreeRules {
 private Ps4MegaTreeRules(){}
 public static int height(Random random,int baseHeight,int extraRange){int height=baseHeight+random.nextInt(3);if(extraRange>1)height+=random.nextInt(extraRange);return height;}
 public static boolean validate(Ps4PlantRules.Access world,int x,int y,int z,int height,boolean updates){
  if(y<1||y+height>255)return false;
  for(int level=0;level<=height+1;level++){
   int radius=level==0?1:2;
   for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
    int wy=y+level;if(wy<0||wy>=256||!Ps4TreeRules.canGrowInto(world,world.block(x+dx,wy,z+dz)))return false;
   }
  }
  int ground=world.block(x,y-1,z)&65535;
  if(ground!=2&&ground!=3||y<=1)return false;
  Ps4TreeRules.dirt(world,x,y-1,z,updates);Ps4TreeRules.dirt(world,x+1,y-1,z,updates);
  Ps4TreeRules.dirt(world,x,y-1,z+1,updates);Ps4TreeRules.dirt(world,x+1,y-1,z+1,updates);return true;
 }
 /** 0x452730: union of four integer circles around the 2x2 trunk. */
 public static void wideLeaves(Ps4PlantRules.Access world,int x,int y,int z,int radius,int leaves,boolean updates){
  int squared=radius*radius;
  for(int dx=-radius;dx<=radius+1;dx++)for(int dz=-radius;dz<=radius+1;dz++)
   if(dx*dx+dz*dz<=squared||(dx-1)*(dx-1)+dz*dz<=squared||dx*dx+(dz-1)*(dz-1)<=squared||(dx-1)*(dx-1)+(dz-1)*(dz-1)<=squared)leaf(world,x+dx,y,z+dz,leaves,updates);
 }
 /** 0x4528a0: a single integer circle for a branch. */
 public static void roundLeaves(Ps4PlantRules.Access world,int x,int y,int z,int radius,int leaves,boolean updates){for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)if(dx*dx+dz*dz<=radius*radius)leaf(world,x+dx,y,z+dz,leaves,updates);}
 public static void leaf(Ps4PlantRules.Access world,int x,int y,int z,int leaves,boolean updates){int old=world.block(x,y,z);if(world.airMaterial(old)||world.leavesMaterial(old))world.setBlock(x,y,z,leaves,updates?3:2);}
 public static void trunk(Ps4PlantRules.Access world,int x,int y,int z,int log,boolean updates){int old=world.block(x,y,z);if(world.airMaterial(old)||world.leavesMaterial(old))world.setBlock(x,y,z,log,updates?3:2);}
}

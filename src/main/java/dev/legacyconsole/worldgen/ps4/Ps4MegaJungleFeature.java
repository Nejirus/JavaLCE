package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** MegaJungleTreeFeature::place, mcps4 0x450720. */
public final class Ps4MegaJungleFeature {
 private Ps4MegaJungleFeature(){}
 public static boolean place(Ps4PlantRules.Access world,Random random,int x,int y,int z,int baseHeight,int extraRange,int log,int leaves,boolean updates){
  int height=Ps4MegaTreeRules.height(random,baseHeight,extraRange);
  if(!Ps4MegaTreeRules.validate(world,x,y,z,height,updates))return false;
  for(int layer=0;layer<3;layer++)Ps4MegaTreeRules.wideLeaves(world,x,y+height-layer,z,3+layer,leaves,updates);
  int flags=updates?3:2;
  for(int branch=y+height-2-random.nextInt(4);branch>y+height/2;branch-=2+random.nextInt(4)){
   float angle=random.nextFloat()*6.2831855f,cos=Ps4Math.cos(angle),sin=Ps4Math.sin(angle);
   int endX=x,endZ=z;
   for(int step=0;step<5;step++){
    endX=x+(int)(cos*(float)step+1.5f);endZ=z+(int)(sin*(float)step+1.5f);
    world.setBlock(endX,branch-3+step/2,endZ,log,flags);
   }
   int extra=random.nextInt(2);
   for(int wy=branch-1-extra;wy<=branch;wy++)Ps4MegaTreeRules.roundLeaves(world,endX,wy,endZ,1-(wy-branch),leaves,updates);
  }
  for(int level=0;level<height;level++){
   column(world,random,x,y+level,z,log,level,updates,-1,0,8,0,-1,1);
   if(level<height-1){
    column(world,random,x+1,y+level,z,log,level,updates,1,0,2,0,-1,1);
    column(world,random,x+1,y+level,z+1,log,level,updates,1,0,2,0,1,4);
    column(world,random,x,y+level,z+1,log,level,updates,-1,0,8,0,1,4);
   }
  }
  return true;
 }
 private static void column(Ps4PlantRules.Access world,Random random,int x,int y,int z,int log,int level,boolean updates,int dx0,int dz0,int side0,int dx1,int dz1,int side1){
  if(!Ps4TreeRules.canGrowInto(world,world.block(x,y,z)))return;
  int flags=updates?3:2;world.setBlock(x,y,z,log,flags);
  if(level>0){vine(world,random,x+dx0,y,z+dz0,side0,flags);vine(world,random,x+dx1,y,z+dz1,side1,flags);}
 }
 private static void vine(Ps4PlantRules.Access world,Random random,int x,int y,int z,int side,int flags){if(random.nextInt(3)>0&&(world.block(x,y,z)&65535)==0)world.setBlock(x,y,z,106|(side<<16),flags);}
}

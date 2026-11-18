package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** AbstractTreeFeature's trunk/vine routine 0x1c560 and fallen logs 0x1cb80.
 * The caller uses the return value to decide whether to add the canopy. */
public final class Ps4TreeTrunk {
 public interface Access extends Ps4PlantRules.Access {
  boolean vineMaterial(int state);
  boolean dirtMaterial(int state);
  boolean grassMaterial(int state);
  boolean solidBlocking(int x,int y,int z,boolean whenUnloaded);
 }
 // Direction::Plane::HORIZONTAL: N,E,S,W. OldLog's axis metadata is Z=8,X=4.
 private static final int[][] HORIZONTAL={{0,-1},{1,0},{0,1},{-1,0}};
 private static final int[][] VINES={{-1,0,8},{1,0,2},{0,-1,1},{0,1,4}};
 private Ps4TreeTrunk(){}
 public static boolean place(Access world,Random random,int x,int y,int z,int height,int log,boolean jungle,boolean updates){
  int standing=random.nextInt(80);
  boolean birch=(log>>>16&3)==2;
  float probability=birch?0f:.75f;
  if(standing!=0&&!birch)probability=jungle?1f/3f:random.nextInt(12)==0?1f:0f;
  if(standing==0){fallen(world,random,x,y,z,height,log,updates);height=1;}
  int flags=updates?3:2;
  for(int level=0;level<height;level++){
   int state=world.block(x,y+level,z);
   if(world.airMaterial(state)||world.leavesMaterial(state)||world.vineMaterial(state)||world.dirtMaterial(state)||world.grassMaterial(state)){
    world.setBlock(x,y+level,z,log,flags);
    if(probability!=0f)for(int[] d:VINES)if(random.nextFloat()<probability&&(world.block(x+d[0],y+level,z+d[1])&65535)==0)world.setBlock(x+d[0],y+level,z+d[1],106|(d[2]<<16),flags);
   }
  }
  return standing!=0;
 }
 public static void fallen(Access world,Random random,int x,int y,int z,int height,int log,boolean updates){
  int[] direction=HORIZONTAL[random.nextInt(4)];int distance=random.nextInt(2)+2;
  int sx=x+direction[0]*distance,sz=z+direction[1]*distance,sy=world.height(sx,sz);
  if(sy>y+1||height<=2)return;
  int wx=sx,wz=sz,gaps=0;
  for(int level=0;level<height-2;level++){
   wx+=direction[0];wz+=direction[1];
   if(!world.airMaterial(world.block(wx,sy,wz)))return;
   if(world.solidBlocking(wx,sy-1,wz,false))gaps=0;else{if(gaps>1)return;gaps++;}
  }
  wx=sx;wz=sz;int flags=updates?3:2,rotated=(log&~(12<<16))|((direction[0]==0?8:4)<<16);
  for(int level=0;level<height-2;level++){
   wx+=direction[0];wz+=direction[1];world.setBlock(wx,sy,wz,rotated,flags);
   if(random.nextInt(10)==0&&world.airMaterial(world.block(wx,sy+1,wz)))world.setBlock(wx,sy+1,wz,random.nextFloat()<.5f?39:40,flags);
  }
 }
}

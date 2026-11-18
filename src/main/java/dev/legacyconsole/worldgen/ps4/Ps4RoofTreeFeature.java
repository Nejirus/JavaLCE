package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** RoofTreeFeature::place 0x5bfc60 and clearance helper 0x5c1550. */
public final class Ps4RoofTreeFeature {
 private static final int[][] HORIZONTAL={{0,-1},{1,0},{0,1},{-1,0}};
 private Ps4RoofTreeFeature(){}
 public static boolean place(Ps4PlantRules.Access world,Random random,int x,int y,int z,int baseHeight,int log,int leaves,boolean updates){
  int height=baseHeight+random.nextInt(3)+random.nextInt(2);
  if(y<1||y+height>=256)return false;
  int ground=world.block(x,y-1,z)&65535;
  if(ground!=2&&ground!=3||y>=255-height||!clearance(world,x,y,z,height))return false;
  Ps4TreeRules.dirt(world,x,y-1,z,updates);Ps4TreeRules.dirt(world,x+1,y-1,z,updates);
  Ps4TreeRules.dirt(world,x,y-1,z+1,updates);Ps4TreeRules.dirt(world,x+1,y-1,z+1,updates);
  int[] direction=HORIZONTAL[random.nextInt(4)];int bend=height-random.nextInt(4),remaining=2-random.nextInt(3);
  int cx=x,cz=z,top=y+height-1,flags=updates?3:2;
  for(int level=0;level<height;level++){
   if(level>=bend&&remaining>0){cx+=direction[0];cz+=direction[1];remaining--;}
   int wy=y+level,old=world.block(cx,wy,cz);
   if(world.airMaterial(old)||world.leavesMaterial(old)){
    trunk(world,cx,wy,cz,log,flags);trunk(world,cx+1,wy,cz,log,flags);
    trunk(world,cx,wy,cz+1,log,flags);trunk(world,cx+1,wy,cz+1,log,flags);top=wy;
   }
  }
  for(int dx=-2;dx<=0;dx++)for(int dz=-2;dz<=0;dz++){
   mirrored(world,cx,top-1,cz,dx,dz,leaves,flags);
   if((dx>-2||dz==0)&&(dz!=-2||dx!=-1))mirrored(world,cx,top+1,cz,dx,dz,leaves,flags);
  }
  if(random.nextBoolean()){
   leaf(world,cx,top+2,cz,leaves,flags);leaf(world,cx+1,top+2,cz,leaves,flags);
   leaf(world,cx+1,top+2,cz+1,leaves,flags);leaf(world,cx,top+2,cz+1,leaves,flags);
  }
  for(int dx=-3;dx<=4;dx++)for(int dz=-3;dz<=4;dz++)
   if((dx!=-3||dz!=-3)&&(dx!=-3||dz!=4)&&(dx!=4||dz!=-3)&&(dx!=4||dz!=4)&&(Math.abs(dx)<3||Math.abs(dz)<3))leaf(world,cx+dx,top,cz+dz,leaves,flags);
  for(int dx=-1;dx<=2;dx++)for(int dz=-1;dz<=2;dz++)if((dx<0||dx>1||dz<0||dz>1)&&random.nextInt(3)==0){
   int length=random.nextInt(3)+2;
   // Native branch trunks use the original origin; their leaves use the
   // final bent trunk center. Retain these distinct coordinate expressions.
   for(int level=0;level<length;level++)trunk(world,x+dx,top-1-level,z+dz,log,flags);
   for(int lx=-1;lx<=1;lx++)for(int lz=-1;lz<=1;lz++)leaf(world,cx+dx+lx,top,cz+dz+lz,leaves,flags);
   for(int lx=-2;lx<=2;lx++)for(int lz=-2;lz<=2;lz++)if(Math.abs(lx)!=2||Math.abs(lz)!=2)leaf(world,cx+dx+lx,top-1,cz+dz+lz,leaves,flags);
  }
  return true;
 }
 private static boolean clearance(Ps4PlantRules.Access world,int x,int y,int z,int height){
  for(int level=0;level<=height+1;level++){
   int radius=level==0?0:1;if(level>=height-1)radius=2;
   for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++)if(!Ps4TreeRules.canGrowInto(world,world.block(wx,y+level,wz)))return false;
  }
  return true;
 }
 private static void mirrored(Ps4PlantRules.Access world,int x,int y,int z,int dx,int dz,int leaves,int flags){
  leaf(world,x+dx,y,z+dz,leaves,flags);leaf(world,x+1-dx,y,z+dz,leaves,flags);
  leaf(world,x+dx,y,z+1-dz,leaves,flags);leaf(world,x+1-dx,y,z+1-dz,leaves,flags);
 }
 private static void leaf(Ps4PlantRules.Access world,int x,int y,int z,int leaves,int flags){if(world.airMaterial(world.block(x,y,z)))world.setBlock(x,y,z,leaves,flags);}
 private static void trunk(Ps4PlantRules.Access world,int x,int y,int z,int log,int flags){if(Ps4TreeRules.canGrowInto(world,world.block(x,y,z)))world.setBlock(x,y,z,log,flags);}
}

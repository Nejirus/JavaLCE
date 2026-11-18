package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** Original submerged clay/sand/gravel patches (0x181400 / 0x5c3e70).
 * The feature kernel is ready for the shared PS4 population dispatcher. */
public final class Ps4DepositFeature {
 private Ps4DepositFeature(){}
 public static boolean clay(Ps4OreFeature.Access world,Random random,int x,int y,int z,int maximumRadius){
  if(!water(world.block(x,y,z)))return false;
  int radius=random.nextInt(maximumRadius-2)+2;
  for(int bx=x-radius;bx<=x+radius;bx++)for(int bz=z-radius;bz<=z+radius;bz++){
   int dx=bx-x,dz=bz-z;if(dx*dx+dz*dz>radius*radius)continue;
   // Original pre-increment loop visits Y-1, Y and Y+1.
   for(int by=y-1;by<=y+1;by++){
    int id=world.block(bx,by,bz)&65535;
    if(id==3||id==82)world.setBlock(bx,by,bz,82,2);
   }
  }
  return true;
 }
 public static boolean sand(Ps4OreFeature.Access world,Random random,int x,int y,int z,int maximumRadius,int state){
  if(!water(world.block(x,y,z)))return false;
  int radius=random.nextInt(maximumRadius-2)+2;
  if(world.hasGenerationRules()&&world.intersectsGenerationRule(x-radius,y-2,z-radius,x+radius,y+2,z+radius))return false;
  for(int bx=x-radius;bx<=x+radius;bx++)for(int bz=z-radius;bz<=z+radius;bz++){
   int dx=bx-x,dz=bz-z;if(dx*dx+dz*dz>radius*radius)continue;
   for(int by=y-2;by<=y+2;by++){
    int id=world.block(bx,by,bz)&65535;
    if(id==3||id==2)world.setBlock(bx,by,bz,state,2);
   }
  }
  return true;
 }
 private static boolean water(int state){int id=state&65535;return id==8||id==9;}
}

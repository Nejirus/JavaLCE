package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** PodzolAreaFeature::place (0x5548a0), used after successful bamboo. */
public final class Ps4PodzolFeature {
 private Ps4PodzolFeature(){}
 public static boolean place(Ps4PlantRules.Access world,Random random,int x,int y,int z){
  int radius=random.nextInt(4)+1,count=0;
  for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++){
   int dx=wx-x,dz=wz-z;
   if(dx*dx+dz*dz<=radius*radius&&((world.block(wx,y-1,wz)&65535)==3||(world.block(wx,y-1,wz)&65535)==2)){
    world.setBlock(wx,y-1,wz,3|(2<<16),2);count++;
   }
  }
  return count>0;
 }
}

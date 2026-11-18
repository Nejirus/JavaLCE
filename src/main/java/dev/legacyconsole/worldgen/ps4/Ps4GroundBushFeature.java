package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** GroundBushFeature::place, mcps4 0x33b160. */
public final class Ps4GroundBushFeature {
 private Ps4GroundBushFeature(){}
 public static boolean place(Ps4PlantRules.Access world,Random random,int x,int y,int z,int log,int leaves,boolean updates){
  int old=world.block(x,y,z);
  while((world.airMaterial(old)||world.leavesMaterial(old))&&y>0)old=world.block(x,--y,z);
  int ground=world.block(x,y,z)&65535;
  if(ground==3||ground==2){
   int base=++y,flags=updates?3:2;world.setBlock(x,y,z,log,flags);
   for(int wy=base;wy<=base+2;wy++){
    int radius=2-(wy-base);
    for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++){
     if(Math.abs(wx-x)==radius&&Math.abs(wz-z)==radius&&random.nextInt(2)==0)continue;
     int state=world.block(wx,wy,wz);
     if(world.airMaterial(state)||world.leavesMaterial(state))world.setBlock(wx,wy,wz,leaves,flags);
    }
   }
  }
  return true;
 }
}

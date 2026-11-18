package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** MelonFeature::place 0x4642c0 and Block::mayPlace 0x94050. */
public final class Ps4MelonFeature {
 private Ps4MelonFeature(){}
 public static boolean place(Ps4PlantRules.Access world,Random random,int x,int y,int z){
  for(int attempt=0;attempt<64;attempt++){
   int wx=x+random.nextInt(8)-random.nextInt(8),wy=y+random.nextInt(4)-random.nextInt(4),wz=z+random.nextInt(8)-random.nextInt(8);
   if(world.replaceable(world.block(wx,wy,wz))&&(world.block(wx,wy-1,wz)&65535)==2)world.setBlock(wx,wy,wz,103,2);
  }
  return true;
 }
}

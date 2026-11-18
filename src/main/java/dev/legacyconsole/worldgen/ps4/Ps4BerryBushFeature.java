package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** Sweet-berry configuration of BushFeature::place (0x172300).
 * Its original AGE property has four values, including fully grown berries. */
public final class Ps4BerryBushFeature {
 private Ps4BerryBushFeature(){}
 /** SweetBerryBushBlock::mayPlace (0x6cb360), then BushBlock::mayPlace. */
 public static boolean mayPlace(Ps4PlantRules.Access world,int x,int y,int z){
  int below=world.block(x,y-1,z);
  return !world.airMaterial(below)&&!world.lavaMaterial(below)&&Ps4PlantRules.mayPlace(world,363,x,y,z);
 }
 public static boolean place(Ps4PlantRules.Access world,Random random,int x,int y,int z){
  for(int attempt=0;attempt<64;attempt++){
   int wx=x+random.nextInt(8)-random.nextInt(8),wy=y+random.nextInt(4)-random.nextInt(4),wz=z+random.nextInt(8)-random.nextInt(8);
   if((world.block(wx,wy,wz)&65535)==0&&(world.block(wx,wy-1,wz)&65535)==2&&mayPlace(world,wx,wy,wz))world.setBlock(wx,wy,wz,363|(random.nextInt(4)<<16),2);
  }
  return true;
 }
}

package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** Natural-pumpkin configuration of BushFeature::place (0x172300).
 * The original registry assigns natural pumpkin ID 257 and carved pumpkin 86. */
public final class Ps4PumpkinFeature {
 public interface Access extends Ps4PlantRules.Access {
  /** Original BlockState::supportsFace arguments, independent of position. */
  boolean supportsFace(int state,int direction,int supportType);
 }
 private static final int[] FACING_DATA={2,3,0,1}; // original N,E,S,W plane
 private Ps4PumpkinFeature(){}
 public static boolean place(Access world,Random random,int x,int y,int z){
  for(int attempt=0;attempt<64;attempt++){
   int wx=x+random.nextInt(8)-random.nextInt(8),wy=y+random.nextInt(4)-random.nextInt(4),wz=z+random.nextInt(8)-random.nextInt(8);
   if((world.block(wx,wy,wz)&65535)==0&&(world.block(wx,wy-1,wz)&65535)==2&&mayPlace(world,wx,wy,wz))world.setBlock(wx,wy,wz,257|(FACING_DATA[random.nextInt(4)]<<16),2);
  }
  return true;
 }
 /** PumpkinBlock::mayPlace, 0x58ff60. The fallback air probe is distinct. */
 public static boolean mayPlace(Access world,int x,int y,int z){
  int below=world.block(x,y-1,z);
  if(!world.replaceable(world.block(x,y,z))&&(world.block(x,y,z)&65535)!=0)return false;
  return world.supportsFace(below,1,2);
 }
}

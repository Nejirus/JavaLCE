package dev.legacyconsole.worldgen.ps4;

/** Shared AbstractTreeFeature helpers recovered from mcps4. */
public final class Ps4TreeRules {
 private Ps4TreeRules(){}
 /** 0x1c390 tests the block's default material and these six identities. */
 public static boolean canGrowInto(Ps4PlantRules.Access world,int state){
  int id=state&65535;
  return world.airMaterial(state)||world.leavesMaterial(state)||id==2||id==3||id==17||id==162||id==6||id==106;
 }
 /** 0x1c430 leaves existing dirt variants unchanged. */
 public static void dirt(Ps4PlantRules.Access world,int x,int y,int z,boolean updates){
  if((world.block(x,y,z)&65535)!=3)world.setBlock(x,y,z,3,updates?3:2);
 }
}

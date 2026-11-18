package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** SpringFeature::place (0x672c40) and Level::instaTick (0x3fe550).
 * The fluid-tick callback must supply the recovered PS4 update implementation. */
public final class Ps4SpringFeature {
 public interface Access extends Ps4OreFeature.Access {
  void immediateFluidTicks(boolean enabled);
  void tickFluid(int x,int y,int z,int state,Random random);
 }
 private static final int[][] SIDES={{-1,0},{1,0},{0,-1},{0,1}};
 private Ps4SpringFeature(){}
 public static boolean place(Access world,Random random,int x,int y,int z,int state){
  if(world.hasGenerationRules()&&world.intersectsGenerationRule(x,y,z,x,y,z))return false;
  if((world.block(x,y+1,z)&65535)!=1||(world.block(x,y-1,z)&65535)!=1)return false;
  if((world.block(x,y,z)&65535)!=0&&(world.block(x,y,z)&65535)!=1)return false;
  int stone=0;
  for(int[] side:SIDES){int id=world.block(x+side[0],y,z+side[1])&65535;if(id==1)stone++;else if(id!=0)return false;}
  if(stone==3){
   world.setBlock(x,y,z,state,2);
   world.immediateFluidTicks(true);world.tickFluid(x,y,z,state,random);world.immediateFluidTicks(false);
  }
  return true;
 }
}

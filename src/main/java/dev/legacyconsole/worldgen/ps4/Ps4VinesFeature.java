package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** VinesFeature::place, mcps4 0x775260; its upper limit is 128. */
public final class Ps4VinesFeature {
 private static final int[] HORIZONTAL={2,5,3,4};
 private Ps4VinesFeature(){}
 public static boolean place(Ps4VineRules.Access world,Random random,int x,int y,int z){
  while(y<128){
   if((world.block(x,y,z)&65535)!=0){x+=random.nextInt(4)-random.nextInt(4);z+=random.nextInt(4)-random.nextInt(4);}
   else for(int i=0;i<4;i++)if(Ps4VineRules.mayPlaceOnFace(world,x,y,z,HORIZONTAL[i])){world.setBlock(x,y,z,106|((1<<i)<<16),2);break;}
   y++;
  }
  return true;
 }
}

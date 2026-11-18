package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** Literal scatter kernels recovered from the mcps4 executable. The caller
 * supplies the population RNG; these features never reseed it. */
public final class Ps4ScatterFeatures {
 public interface Access extends Ps4PlantRules.Access {boolean hasCeiling();}
 public enum Kind {
  FLOWER(0x2ea4c0),TALL_GRASS(0x6d3a90),DEAD_BUSH(0x1f72d0),
  WATERLILY(0x7813d0),CACTUS(0x159aa0),REEDS(0x5b48a0),DOUBLE_PLANT(0x215bf0),MUSHROOM(0x139230);
  public final int entrypoint;
  Kind(int entrypoint){this.entrypoint=entrypoint;}
 }
 private static final int[][] REED_WATER={{-1,0},{1,0},{0,-1},{0,1}};
 private Ps4ScatterFeatures(){}

 /** State is the flower/grass state or the lower double-plant variant. */
 public static boolean place(Kind kind,Access world,Random random,int x,int y,int z,int state){
  if(kind==Kind.FLOWER&&world.hasGenerationRules()
     &&world.intersectsGenerationRule(x-8,y-4,z-8,x+8,y+4,z+8))return false;
  if(kind==Kind.TALL_GRASS||kind==Kind.DEAD_BUSH){
   int old=world.block(x,y,z);
   while((world.airMaterial(old)||world.leavesMaterial(old))&&y>0)old=world.block(x,--y,z);
  }
  int attempts=switch(kind){case TALL_GRASS->128;case DEAD_BUSH->4;case WATERLILY,CACTUS->10;case REEDS->20;default->64;};
  boolean placed=false;
  for(int attempt=0;attempt<attempts;attempt++){
   int wx,wy,wz;
   if(kind==Kind.REEDS){wx=x+random.nextInt(4)-random.nextInt(4);wy=y;wz=z+random.nextInt(4)-random.nextInt(4);}
   else{wx=x+random.nextInt(8)-random.nextInt(8);wy=y+random.nextInt(4)-random.nextInt(4);wz=z+random.nextInt(8)-random.nextInt(8);}
   if(kind==Kind.REEDS&&world.hasGenerationRules()&&world.intersectsGenerationRule(wx,wy,wz,wx,wy,wz))continue;
   if((world.block(wx,wy,wz)&65535)!=0)continue;
   switch(kind){
    case FLOWER,MUSHROOM->{if((wy<127||!world.hasCeiling())&&Ps4PlantRules.canStay(world,state,wx,wy,wz))world.setBlock(wx,wy,wz,state,2);}
    case TALL_GRASS->{if(Ps4PlantRules.canStay(world,state,wx,wy,wz))world.setBlock(wx,wy,wz,state,0x12);}
    case DEAD_BUSH->{if(Ps4PlantRules.canStay(world,32,wx,wy,wz))world.setBlock(wx,wy,wz,32,2);}
    case WATERLILY->{if(Ps4PlantRules.mayPlace(world,111,wx,wy,wz))world.setBlock(wx,wy,wz,111,2);}
    case DOUBLE_PLANT->{
     if((!world.hasCeiling()||wy<126)&&Ps4PlantRules.mayPlace(world,175,wx,wy,wz)){
      world.setBlock(wx,wy,wz,175|(state>>>16&7)<<16,2);
      // The native upper state has no copied variant; its lower half supplies it.
      world.setBlock(wx,wy+1,wz,175|(8<<16),2);placed=true;
     }
    }
    case CACTUS->{
     int height=random.nextInt(random.nextInt(3)+1)+1;
     for(int level=0;level<height;level++)if(Ps4PlantRules.canStay(world,81,wx,wy,wz))world.setBlock(wx,wy+level,wz,81,2);
    }
    case REEDS->{
     boolean water=false;
     for(int[] d:REED_WATER)if(world.waterMaterial(world.block(wx+d[0],wy-1,wz+d[1]))){water=true;break;}
     if(water){
      int height=random.nextInt(random.nextInt(3)+1)+2;
      for(int level=0;level<height;level++)if(Ps4PlantRules.canStay(world,83,wx,wy,wz))world.setBlock(wx,wy+level,wz,83,2);
     }
    }
   }
  }
  return kind!=Kind.DOUBLE_PLANT||placed;
 }
}

package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** Recovered biome flower/grass selectors; these consume the population RNG. */
public final class Ps4BiomePlants {
 public enum Flowers {DEFAULT,FOREST,PLAINS,SWAMP}
 public enum Grass {DEFAULT,TAIGA,JUNGLE,BAMBOO_JUNGLE}
 private static final Ps4SurfaceNoise NOISE=new Ps4SurfaceNoise(new Random(2345),1);
 private Ps4BiomePlants(){}
 public static double biomeNoise(double x,double z){return NOISE.getValue(x,z);}
 public static int grass(Grass biome,Random random){
  int variant=switch(biome){case DEFAULT->1;case TAIGA->random.nextInt(5)>0?2:1;case JUNGLE,BAMBOO_JUNGLE->random.nextInt(4)==0?2:1;};
  return 31|(variant<<16);
 }
 public static int flower(Flowers biome,int variant,Random random,int x,int z){
  double noise=biome==Flowers.FOREST&&variant==1?NOISE.getValue((double)x/48.0,(double)z/48.0):biome==Flowers.PLAINS?NOISE.getValue((double)x/200.0,(double)z/200.0):0;
  return flowerWithNoise(biome,variant,random,noise);
 }
 static int flowerWithNoise(Flowers biome,int variant,Random random,double noise){
  return switch(biome){
   case DEFAULT->random.nextInt(3)>0?37:38;
   case SWAMP->red(1);
   case FOREST->{
    if(variant!=1)yield random.nextBoolean()?red(10):37;
    double clamped=Math.min(Math.max((noise+1.0)*0.5,0),0.9999);
    int index=(int)(clamped*12.0);
    yield index==2?38:index==0?37:red(index-1);
   }
   case PLAINS->{
    int draw=random.nextInt(4);
    if(noise< -0.8)yield red(switch(draw){case 0->5;case 1->4;case 2->7;default->6;});
    if(draw==0)yield 37;
    yield switch(random.nextInt(4)){case 0->38;case 1->red(3);case 2->red(8);default->red(9);};
   }
  };
 }
 private static int red(int variant){return 38|(variant<<16);}
}

package dev.legacyconsole.worldgen.ps4;

import java.util.Random;
import dev.legacyconsole.worldgen.ps4.layers.Ps4Layers;

/** PS4 StrongholdFeature::selectPositions (0x68c130). This build places one
 * stronghold and retries up to 30 times, using the console's compact-world radius. */
public final class Ps4Stronghold {
 private Ps4Stronghold(){}
 public static int[] position(long seed,int width,Ps4Layers biomes){
  Random random=new Random(seed);double angle=random.nextDouble()*Math.PI*2;int x=0,z=0;
  for(int attempt=0;attempt<30;attempt++){
   double distance=(random.nextDouble()+1.25)*(width/16<72?3+random.nextInt(4):32);
   x=(int)Math.floor(Math.cos(angle)*distance+.5);z=(int)Math.floor(Math.sin(angle)*distance+.5);
   int minX=((x*16+8)-112)>>2,minZ=((z*16+8)-112)>>2;
   int maxX=((x*16+8)+112)>>2,maxZ=((z*16+8)+112)>>2;
   int w=maxX-minX+1,h=maxZ-minZ+1;int[] ids=biomes.quartArea(minX,minZ,w,h);
   int found=0,selectedX=0,selectedZ=0;
   for(int i=0;i<ids.length;i++){
    Ps4BiomeData biome=Ps4BiomeData.getBiome(ids[i]);
    if(biome!=null&&biome.id<42&&biome.depth>0&&(found==0||random.nextInt(found+1)==0)){
     selectedX=(minX+i%w)*4;selectedZ=(minZ+i/w)*4;found++;
    }
   }
   double nextAngle=random.nextDouble()*Math.PI*2/3;
   if(found>0)return new int[]{selectedX>>4,selectedZ>>4};angle=nextAngle;
  }
  return new int[]{x,z};
 }
}

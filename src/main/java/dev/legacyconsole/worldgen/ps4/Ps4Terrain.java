package dev.legacyconsole.worldgen.ps4;

import dev.legacyconsole.worldgen.ps4.layers.Ps4Layers;
import java.util.Random;

/** PS4 OverworldLevelSource density and interpolation. The console creates 128-block
 * terrain within a 256-block build space. Noise construction order and float rounding
 * are part of the saved generator format; do not substitute Java's current noises. */
public final class Ps4Terrain {
 public static final int SEA_LEVEL=63, TERRAIN_HEIGHT=128;
 private static final float COORD=684.4119873046875f;
 private static final float[] WEIGHTS={
  3.4921515f,4.38529f,4.8795004f,4.38529f,3.4921515f,
  4.38529f,6.741998f,9.128709f,6.741998f,4.38529f,
  4.8795004f,9.128709f,22.36068f,9.128709f,4.8795004f,
  4.38529f,6.741998f,9.128709f,6.741998f,4.38529f,
  3.4921515f,4.38529f,4.8795004f,4.38529f,3.4921515f};
 private final Ps4PerlinNoise low,high,blend,depth;
 private final Ps4SurfaceNoise surface;
 private final Ps4Layers biomes;
 private final int width;
 public Ps4Terrain(long seed,int width,Ps4Layers biomes){
  this.width=width;this.biomes=biomes;Random random=new Random(seed);
  low=new Ps4PerlinNoise(random,16);high=new Ps4PerlinNoise(random,16);blend=new Ps4PerlinNoise(random,8);
  surface=new Ps4SurfaceNoise(random,4);
  new Ps4PerlinNoise(random,10);depth=new Ps4PerlinNoise(random,16);new Ps4PerlinNoise(random,8);
 }
 public double[] densities(int chunkX,int chunkZ){
  return densities(chunkX,chunkZ,biomes.quartArea(chunkX*4-2,chunkZ*4-2,10,10));
 }
 public double[] densities(int chunkX,int chunkZ,int[] ids){
  if(ids.length!=100)throw new IllegalArgumentException("PS4 biome neighborhood must be 10x10");
  int x=chunkX*4,z=chunkZ*4;
  double[] d=depth.region(x,10,z,5,1,5,200,1,200);
  double[] b=blend.region(x,0,z,5,17,5,(double)(COORD/80f),(double)(COORD/160f),(double)(COORD/80f));
  double[] l=low.region(x,0,z,5,17,5,COORD,COORD,COORD),h=high.region(x,0,z,5,17,5,COORD,COORD,COORD);
  double[] out=new double[425];float[] weights=new float[5],depths=new float[5],scales=new float[5];
  for(int xx=0;xx<5;xx++)for(int zz=0;zz<5;zz++){
   float scaleSum=0,depthSum=0,weightSum=0;float center=Ps4BiomeData.getBiome(ids[xx+2+(zz+2)*10]).depth;
   for(int dx=0;dx<5;dx++){
    for(int dz=0;dz<5;dz++){
     Ps4BiomeData biome=Ps4BiomeData.getBiome(ids[xx+dx+(zz+dz)*10]);
     depths[dz]=biome.depth;scales[dz]=biome.scale;
     weights[dz]=WEIGHTS[dx+dz*5]/(biome.depth+2f);if(biome.depth>center)weights[dz]*=.5f;
    }
    // PS4 compiler's unrolled five-neighbor accumulation order (single precision).
    scaleSum=scales[4]*weights[4]+(scales[3]*weights[3]+((scales[1]*weights[1]+(scales[0]*weights[0]+scaleSum))+scales[2]*weights[2]));
    depthSum=depths[4]*weights[4]+(depths[3]*weights[3]+((depths[1]*weights[1]+(depths[0]*weights[0]+depthSum))+depths[2]*weights[2]));
    weightSum=weights[4]+(weights[3]+((weights[0]+weightSum)+weights[1]+weights[2]));
   }
   float scale=(scaleSum/weightSum)*.9f+.1f;
   float biomeDepth=((depthSum/weightSum)*4f-1f)*.125f;
   double extra=d[xx*5+zz]/8000;
   if(extra<0)extra*=-.3;extra=extra*3-2;
   if(extra>=0)extra=Math.min(1,extra)*.125;
   else extra=(Math.max(-1,extra*.5)/1.4)*.5;
   double centerY=(extra*.2+(double)biomeDepth)*8.5*.125*4+8.5;
   for(int y=0;y<17;y++){
    int index=(xx*5+zz)*17+y;
    double slope=((y-centerY)*12*128*.00390625)/(double)scale;if(slope<0)slope*=4;
    double factor=(b[index]/10+1)*.5;
    double density=clampedLerp(l[index]/512,h[index]/512,factor)-slope;
    if(y>=14){double fade=(double)((float)(y-13)/3f);density=(1-fade)*density+fade*-10;}
    out[index]=density;
   }
  }
  return out;
 }
 private static double clampedLerp(double a,double b,double t){return t<0?a:t>1?b:a+t*(b-a);}
 /** x,z in world blocks; PS4's nearest outer edge is at -width/2 and width/2-1. */
 public int edgeDistance(int x,int z){
  int min=-width/2,max=width/2-1;
  return Math.min(Math.min(Math.abs(x-min),Math.abs(x-max)),Math.min(Math.abs(z-min),Math.abs(z-max)));
 }
 public float edgeFalloff(int x,int z){int distance=edgeDistance(x,z);return distance<32?(32-distance)*.03125f*128f:0;}
 public boolean outside(int x,int z){return x< -width/2||z< -width/2||x>=width/2||z>=width/2;}
 /** 0=air, 1=stone, 2=water, ordered (localX*16+localZ)*128+Y. */
 public byte[] baseChunk(int chunkX,int chunkZ){
  double[] grid=densities(chunkX,chunkZ);byte[] out=new byte[16*16*128];
  for(int cx=0;cx<4;cx++)for(int cz=0;cz<4;cz++)for(int cy=0;cy<16;cy++){
   int i=(cx*5+cz)*17+cy,j=((cx+1)*5+cz)*17+cy;
   double a=grid[i],b=grid[i+17],c=grid[j],d=grid[j+17];
   double ay=(grid[i+1]-a)*.125,by=(grid[i+18]-b)*.125,cyy=(grid[j+1]-c)*.125,dy=(grid[j+18]-d)*.125;
   for(int yy=0;yy<8;yy++){
    double aa=a,bb=b,ax=(c-a)*.25,bx=(d-b)*.25;int y=cy*8+yy;
    for(int xx=0;xx<4;xx++){
     double step=(bb-aa)*.25,value=step+(aa-step);int lx=cx*4+xx;
     for(int zz=0;zz<4;zz++){
      int lz=cz*4+zz,wx=chunkX*16+lx,wz=chunkZ*16+lz;
      byte block=value>edgeFalloff(wx,wz)?(byte)1:y<SEA_LEVEL?(byte)2:0;
      if(edgeDistance(wx,wz)==0||outside(wx,wz))block=y<=SEA_LEVEL-10?(byte)1:y<SEA_LEVEL?(byte)2:0;
      out[(lx*16+lz)*128+y]=block;value+=step;
     }aa+=ax;bb+=bx;
    }a+=ay;b+=by;c+=cyy;d+=dy;
   }
  }return out;
 }
 public double[] surfaceNoise(int chunkX,int chunkZ){return surface.getRegion(null,chunkX*16,chunkZ*16,16,16,.0625,.0625,1);}
}

package dev.legacyconsole.worldgen.ps4.layers;

import dev.legacyconsole.worldgen.ps4.Ps4BiomeData;

/** Original PS4 Layer::getDefaultLayers graph (0x3e0d20), including the finite cache.
 * No Minecraft runtime globals or shared IntCache are used by these calculations. */
public final class Ps4Layers {
 private final FiniteCache cache;
 private final GenLayer block;
 public Ps4Layers(long seed,int width) {this(seed,width,0);}
 public Ps4Layers(long seed,int width,int scale) {
  if(scale<0||scale>2)throw new IllegalArgumentException("PS4 biome scale: "+scale);
  if(width!=864&&width!=1024&&width!=3072&&width!=5120) throw new IllegalArgumentException("PS4 world width: "+width);
  GenLayer[] n=new GenLayer[54];
  n[33]=new GenLayerIsland(1L);
  n[32]=new GenLayerFuzzyZoom(2000L, n[33]);
  n[31]=new GenLayerAddIsland(1L, n[32]);
  n[30]=new GenLayerZoom(2001L, n[31]);
  n[29]=new GenLayerAddIsland(2L, n[30]);
  n[28]=new GenLayerAddIsland(50L, n[29]);
  n[27]=new GenLayerAddIsland(70L, n[28]);
  n[26]=new GenLayerRemoveTooMuchOcean(2L, n[27]);
  n[25]=new GenLayerAddSnow(2L, n[26]);
  n[24]=new GenLayerAddIsland(3L, n[25]);
  n[23]=new GenLayerEdge(2L, n[24], GenLayerEdge.Mode.values()[0]);
  n[22]=new GenLayerEdge(2L, n[23], GenLayerEdge.Mode.values()[1]);
  n[21]=new GenLayerEdge(3L, n[22], GenLayerEdge.Mode.values()[2]);
  n[20]=new GenLayerZoom(2002L, n[21]);
  n[19]=new GenLayerZoom(2003L, n[20]);
  n[18]=new GenLayerAddIsland(4L, n[19]);
  n[17]=new DeepOcean(4L, scale==0?n[18]:new GenLayerAddMushroomIsland(5L,n[18]));
  n[16]=new BiomeInit(200L, n[17]);
  n[15]=new RareSpot(1001L, n[16], 21, 40, 10);
  GenLayer scaled=n[15];for(int i=0;i<scale;i++)scaled=new GenLayerZoom(1000L+i,scaled);
  n[14]=new GenLayerBiomeEdge(1000L, scaled);
  n[36]=new GenLayerRiverInit(100L, n[17]);
  n[35]=new GenLayerZoom(1000L, n[36]);
  n[34]=new GenLayerZoom(1001L, n[35]);
  n[13]=new GenLayerHills(1000L, n[14], n[34]);
  n[12]=new RareSpot(1001L, n[13], 1, 129, 57);
  n[11]=new GenLayerZoom(1000L, n[12]);
  n[10]=new GenLayerAddIsland(3L, n[11]);
  n[9]=scale==0?new GenLayerAddMushroomIsland(5L, n[10]):n[10];
  n[8]=new GenLayerZoom(1001L, n[9]);
  n[7]=scale==0?new GrowMushroom(5L, n[8]):n[8];
  n[6]=new GenLayerShore(1000L, n[7]);
  n[5]=new GenLayerZoom(1002L, n[6]);
  n[4]=new GenLayerZoom(1003L, n[5]);
  n[3]=new GenLayerSmooth(1000L, n[4]);
  n[44]=new GenLayerZoom(1000L, n[36]);
  n[43]=new GenLayerZoom(1001L, n[44]);
  n[42]=new GenLayerZoom(1000L, n[43]);
  n[41]=new GenLayerZoom(1001L, n[42]);
  n[40]=new GenLayerZoom(1002L, n[41]);
  n[39]=new GenLayerZoom(1003L, n[40]);
  n[38]=new GenLayerRiver(1L, n[39]);
  n[37]=new GenLayerSmooth(1000L, n[38]);
  n[2]=new GenLayerRiverMix(100L, n[3], n[37]);
  n[52]=new OceanTemperature(2L, n[26]);
  n[51]=new OceanEdge(2L, n[52]);
  n[50]=new GenLayerZoom(2001L, n[51]);
  n[49]=new GenLayerZoom(2002L, n[50]);
  n[48]=new GenLayerZoom(2003L, n[49]);
  n[47]=new GenLayerZoom(2004L, n[48]);
  n[46]=new GenLayerZoom(2005L, n[47]);
  n[45]=new GenLayerZoom(2006L, n[46]);
  n[1]=new OceanMix(100L, n[2], n[45]);
  n[0]=new FiniteCache(1L, n[1], width);
  n[53]=new GenLayerVoronoiZoom(10L, n[0]);
  n[53].initWorldGenSeed(seed);
  cache=(FiniteCache)n[0];block=n[53];
 }
 public int quartBiome(int x,int z) { return cache.biome(x,z); }
 public int[] quartArea(int x,int z,int width,int height) { return cache.getInts(x,z,width,height); }
 public synchronized int[] blockArea(int x,int z,int width,int height) { return block.getInts(x,z,width,height); }
 public int blockBiome(int x,int z) { return blockArea(x,z,1,1)[0]; }

 private abstract static class Single extends GenLayer {
  Single(long seed,GenLayer input) { super(seed);parent=input; }
 }
 private static final class BiomeInit extends Single {
  private static final int[][] POOLS={{2,2,2,35,35,1},{4,29,3,1,27,6},{4,3,5,1},{12,12,12,30}};
  BiomeInit(long salt,GenLayer p) { super(salt,p); }
  public int[] getInts(int x,int z,int width,int height) {
   int[] in=parent.getInts(x,z,width,height),out=new int[width*height];
   for(int zz=0;zz<height;zz++)for(int xx=0;xx<width;xx++) {
    initChunkSeed(x+xx,z+zz);int index=xx+zz*width,id=in[index],special=(id&3840)>>8;id&=-3841;
    if(Ps4BiomeData.isOcean(id)||id==14)out[index]=id;
    else if(id==1&&special>0)out[index]=nextInt(3)==0?39:38;
    else if(id==2&&special>0)out[index]=21;
    else if(id==3&&special>0)out[index]=32;
    else if(id>=1&&id<=4){int[] pool=POOLS[id-1];out[index]=pool[nextInt(pool.length)];}
    else out[index]=14;
   }return out;
  }
 }
 private static final class RareSpot extends Single {
  private final int from,to,chance;
  RareSpot(long salt,GenLayer p,int from,int to,int chance){super(salt,p);this.from=from;this.to=to;this.chance=chance;}
  public int[] getInts(int x,int z,int w,int h){
   int[] in=parent.getInts(x-1,z-1,w+2,h+2),out=new int[w*h];
   for(int zz=0;zz<h;zz++)for(int xx=0;xx<w;xx++){
    initChunkSeed(x+xx,z+zz);int id=in[xx+1+(zz+1)*(w+2)];out[xx+zz*w]=nextInt(chance)==0&&id==from?to:id;
   }return out;
  }
 }
 private static final class DeepOcean extends Single {
  DeepOcean(long salt,GenLayer p){super(salt,p);}
  public int[] getInts(int x,int z,int w,int h){
   int[] in=parent.getInts(x-1,z-1,w+2,h+2),out=new int[w*h];int stride=w+2;
   for(int zz=0;zz<h;zz++)for(int xx=0;xx<w;xx++){
    int i=xx+1+(zz+1)*stride,id=in[i];
    if(Ps4BiomeData.isShallowOcean(id)&&Ps4BiomeData.isShallowOcean(in[i-stride])&&Ps4BiomeData.isShallowOcean(in[i+stride])&&Ps4BiomeData.isShallowOcean(in[i-1])&&Ps4BiomeData.isShallowOcean(in[i+1]))id=id==0?24:id+1;
    out[xx+zz*w]=id;
   }return out;
  }
 }
 private static final class GrowMushroom extends Single {
  GrowMushroom(long salt,GenLayer p){super(salt,p);}
  public int[] getInts(int x,int z,int w,int h){
   int[] in=parent.getInts(x-1,z-1,w+2,h+2),out=new int[w*h];int stride=w+2;
   for(int zz=0;zz<h;zz++)for(int xx=0;xx<w;xx++){
    int i=xx+(zz*stride);out[xx+zz*w]=in[i]==14||in[i+2]==14||in[i+2*stride]==14||in[i+2*stride+2]==14?14:in[i+stride+1];
   }return out;
  }
 }
 private static final class OceanTemperature extends Single {
  OceanTemperature(long salt,GenLayer p){super(salt,p);}
  public int[] getInts(int x,int z,int w,int h){
   int[] out=new int[w*h];
   for(int zz=0;zz<h;zz++)for(int xx=0;xx<w;xx++){
    initChunkSeed(x+xx,z+zz);float t=nextInt(100)/100f;out[xx+zz*w]=t<.075f?42:t<.4f?44:t<.675f?0:t<.95f?46:48;
   }return out;
  }
 }
 private static final class OceanEdge extends Single {
  OceanEdge(long salt,GenLayer p){super(salt,p);}
  public int[] getInts(int x,int z,int w,int h){
   int[] in=parent.getInts(x-1,z-1,w+2,h+2),out=new int[w*h];int stride=w+2;
   for(int zz=0;zz<h;zz++)for(int xx=0;xx<w;xx++){
    int i=xx+1+(zz+1)*stride,id=in[i],opposite=id==42?48:id==48?42:-1;
    out[xx+zz*w]=opposite!=-1&&(in[i-stride]==opposite||in[i+stride]==opposite||in[i-1]==opposite||in[i+1]==opposite)?0:id;
   }return out;
  }
 }
 private static final class OceanMix extends GenLayer {
  private final GenLayer land,ocean;
  OceanMix(long salt,GenLayer land,GenLayer ocean){super(salt);this.land=land;this.ocean=ocean;}
  public void initWorldGenSeed(long seed){land.initWorldGenSeed(seed);ocean.initWorldGenSeed(seed);super.initWorldGenSeed(seed);}
  public int[] getInts(int x,int z,int w,int h){
   int[] a=land.getInts(x,z,w,h),b=ocean.getInts(x,z,w,h);
   for(int i=0;i<a.length;i++)if(Ps4BiomeData.isOcean(a[i])&&Ps4BiomeData.isOcean(b[i])){
    int id=b[i];if(a[i]==24)id=id==0?24:id==44||id==46||id==48?id+1:id;a[i]=id;
   }return a;
  }
 }
 private static final class FiniteCache extends Single {
  private final int min,size;
  private volatile byte[] values;
  FiniteCache(long salt,GenLayer p,int width){super(salt,p);size=width/4;min=-size/2;}
  private byte[] values(){
   byte[] v=values;if(v!=null)return v;
   synchronized(this){
    if(values==null){int[] original=parent.getInts(min,min,size,size);v=new byte[original.length];for(int i=0;i<v.length;i++)v[i]=(byte)original[i];values=v;parent=null;}
    return values;
   }
  }
  int biome(int x,int z){return values()[Math.max(0,Math.min(size-1,z-min))*size+Math.max(0,Math.min(size-1,x-min))]&255;}
  public int[] getInts(int x,int z,int w,int h){int[] out=new int[w*h];for(int zz=0;zz<h;zz++)for(int xx=0;xx<w;xx++)out[xx+zz*w]=biome(x+xx,z+zz);return out;}
 }
}

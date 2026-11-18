package dev.legacyconsole.worldgen.ps4;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicIntegerArray;

/** Recovered mcps4 special surfaces. Values use legacy IDs with metadata in
 * bits 16..31, matching Ps4Surface. Coordinates are the original surface-call
 * coordinates; the PS4 packed column axes are transposed at the chunk bridge. */
public final class Ps4BiomeSurfaces {
 private static final int RED_SAND=12|(1<<16),ORANGE=159|(1<<16),TERRACOTTA=172;
 private static final Ps4SurfaceNoise WETLAND=new Ps4SurfaceNoise(new Random(2345),1);
 static double wetlandNoise(double x,double z){return WETLAND.getValue(x,z);}
 private final long seed;
 private volatile Mesa mesa;
 private volatile Frozen frozen;
 private final AtomicIntegerArray topTiles=new AtomicIntegerArray(256);
 public Ps4BiomeSurfaces(long seed){this.seed=seed;for(int id=0;id<256;id++)topTiles.set(id,defaultTopTile(id));}
 /** Biome's top tile field, which the original air carvers read for grass repair.
  * The mountain, giant-taiga and mutated-savanna surface wrappers update it. */
 public int topTile(int biome){return topTiles.get(biome);}
 public static int defaultTopTile(int id){
  if(id==140)return 80;
  if(id==25)return 1;
  if(id==14||id==15)return 110;
  var data=Ps4BiomeData.getBiome(id);
  return id==2||id==17||id==130||id==16||id==26||data!=null&&data.family==7?12:2;
 }
 private static boolean changesTopTile(int biome){
  return biome==3||biome==20||biome==34||biome==131||biome==162||biome==32||biome==33||biome==160||biome==161||biome==163||biome==164;
 }
 /** Top fields at the end of this chunk's original 0x508110 surface pass.
  * Java schedules surfaces/carvers as separate tasks. Reconstruct the fields
  * from this chunk's inputs so another chunk cannot overwrite them between
  * those tasks. Every affected biome present here gets its own last write. */
 public static int[] chunkTopTiles(int[] biomes,double[] noise){
  if(biomes.length!=256||noise.length!=256)throw new IllegalArgumentException("PS4 surface pass requires 16x16 inputs");
  int[] top=new int[256];for(int id=0;id<top.length;id++)top[id]=defaultTopTile(id);
  for(int i=0;i<256;i++)if(changesTopTile(biomes[i]))top[biomes[i]]=Ps4Surface.materials(biomes[i],noise[i])[0]&65535;
  return top;
 }
 /** Original replaceSurface call order (0x508110), on physical X-major columns.
  * Biomes and noise are the original flattened 16 by 16 inputs. */
 public void chunk(int[][] columns,Random random,int cx,int cz,int[] biomes,double[] noise){
  if(columns.length!=256||biomes.length!=256||noise.length!=256)throw new IllegalArgumentException("PS4 surface pass requires 16x16 inputs");
  for(int x=0;x<16;x++)for(int z=0;z<16;z++)
   column(columns[z*16+x],columns[x*16+z],random,biomes[z+x*16],cx*16+x,cz*16+z,noise[x*16+z]);
 }
 public void column(int[] blocks,Random random,int biome,int x,int z,double noise){
  column(blocks,blocks,random,biome,x,z,noise);
 }
 /** Frozen ocean's first pass addresses the untransposed packed column;
  * its final shared surface pass addresses the transposed column. */
 public void column(int[] blocks,int[] frozenBlocks,Random random,int biome,int x,int z,double noise){
  if(blocks.length!=128)throw new IllegalArgumentException("PS4 surface column must be 128 high");
  if(frozenBlocks.length!=128)throw new IllegalArgumentException("PS4 frozen column must be 128 high");
  if(changesTopTile(biome))
   topTiles.set(biome,Ps4Surface.materials(biome,noise)[0]&65535);
  if(Ps4BiomeData.getBiome(biome).family==7){mesa().column(blocks,random,biome,x,z,noise);return;}
  if(biome==48||biome==49){frozen().column(blocks,frozenBlocks,random,biome,x,z,noise);return;}
  if(biome==6||biome==134){
   // SwampBiome::buildSurface, 0x6ca1e0.
   double wet=WETLAND.getValue(x*.25,z*.25);
   if(wet>0)for(int y=127;y>=0;y--){
    if((blocks[y]&65535)==0)continue;
    if(y==62&&(blocks[y]&65535)!=9){blocks[y]=9;if(wet<.12)blocks[y+1]=111;}break;
   }
  }
  Ps4Surface.column(blocks,random,biome,noise);
 }
 private Mesa mesa(){
  Mesa value=mesa;if(value!=null)return value;
  synchronized(this){if(mesa==null)mesa=new Mesa(seed);return mesa;}
 }
 private Frozen frozen(){
  Frozen value=frozen;if(value!=null)return value;
  synchronized(this){if(frozen==null)frozen=new Frozen();return frozen;}
 }
 private static final class Mesa {
  final int[] bands=new int[64];final Ps4SurfaceNoise bandNoise,pillars,roofs;
  Mesa(long seed){
   // MesaBiome::generateBands, 0x46b130. Noise construction consumes the
   // same RNG before the first band is selected.
   Arrays.fill(bands,TERRACOTTA);Random random=new Random(seed);bandNoise=new Ps4SurfaceNoise(random,1);
   for(int y=0;y<64;y++){y+=random.nextInt(5)+1;if(y<64)bands[y]=ORANGE;}
   stripes(random,4,1);stripes(random,12,2);stripes(random,14,1);
   int count=random.nextInt(3)+3,y=0;
   for(int i=0;i<count;i++){
    y+=4+random.nextInt(16);
    if(y<64){
     bands[y]=159;
     if(y>1&&random.nextBoolean())bands[y-1]=159|(8<<16);
     if(y<63&&random.nextBoolean())bands[y+1]=159|(8<<16);
    }
   }
   // Original constructors initialize the cached seed to zero. On the
   // first world, pillar/roof initialization precedes updating that cache.
   Random pillarRandom=new Random(0);pillars=new Ps4SurfaceNoise(pillarRandom,4);roofs=new Ps4SurfaceNoise(pillarRandom,1);
  }
  private void stripes(Random random,int color,int minLength){
   int count=random.nextInt(4)+2;
   for(int i=0;i<count;i++){
    int length=random.nextInt(3)+minLength,start=random.nextInt(64);
    for(int j=0;j<length&&start+j<64;j++)bands[start+j]=159|(color<<16);
   }
  }
  private int band(int x,int y){
   double n=bandNoise.getValue(x*.001953125,x*.001953125);
   int offset=(int)Math.floor(n+n+.5);return bands[(offset+y+64)%64];
  }
  // MesaBiome::buildSurface, 0x46aa40.
  void column(int[] blocks,Random random,int biome,int x,int z,double noise){
   boolean bryce=biome==165,forest=biome==38||biome==166;
   int pillarHeight=0;
   if(bryce){
    int sx=(x&~15)|(z&15),sz=(z&~15)|(x&15);
    double value=Math.min(pillars.getValue(sx*.25,sz*.25),Math.abs(noise));
    if(value>0)pillarHeight=(int)(Math.min(Math.ceil(Math.abs(roofs.getValue(sx*.001953125,sz*.001953125))*50)+14,value*value*2.5)+64);
   }
   int under=159,thickness=(int)(noise/3+3+random.nextDouble()*.25),remaining=-1,stones=0;boolean sand=false;
   double c=Ps4Math.cosDouble((noise/3)*(double)(float)Math.PI);
   for(int y=127;y>=0;y--){
    if(y<pillarHeight&&(blocks[y]&65535)==0)blocks[y]=1;
    if(y<=random.nextInt(2)+1){blocks[y]=7;continue;}
    if(stones>14&&!bryce)continue;
    int block=blocks[y]&65535;
    if(block==0){remaining=-1;continue;}if(block!=1)continue;
    if(remaining==-1){
     if(thickness<=0)under=1;else if(y>=59&&y<=64)under=159;
     remaining=Math.max(0,y-63)+thickness;
     if(y<62){blocks[y]=(under&65535)==159?ORANGE:under;sand=false;}
     else if(forest&&y>thickness*2+86){blocks[y]=c>0?3|(1<<16):2;sand=false;}
     else if(y>thickness+66){blocks[y]=y>=64&&y<128?(c>0?TERRACOTTA:band(x,y)):ORANGE;sand=false;}
     else{blocks[y]=RED_SAND;sand=true;}
    }else if(remaining>0){remaining--;blocks[y]=sand?ORANGE:band(x,y);}
    stones++;
   }
  }
 }
 private static final class Frozen {
  final Ps4SurfaceNoise icebergs,roofs;
  Frozen(){Random random=new Random(0);icebergs=new Ps4SurfaceNoise(random,4);roofs=new Ps4SurfaceNoise(random,1);}
  float temperature(int x,int y,int z){
   return Ps4BiomeTemperature.frozen(x,y,z);
  }
  // OceanFrozenBiome::buildSurface, 0x4d7660.
  void column(int[] blocks,int[] iceBlocks,Random random,int biome,int x,int z,double noise){
   float value=(float)Math.min(icebergs.getValue(x*.1,z*.1),Math.abs(noise)),lower=0;int upper=0;
   if(value>1.8f){
    float roof=Math.abs((float)roofs.getValue(x*.9765625,z*.9765625));
    float height=Math.min((float)Math.ceil(roof*40)+14,value*value*.8f);
    if(temperature(x,63,z)>.1f)height-=2;
    lower=64-height-7;upper=(int)(height+64);
   }
   int snowLimit=random.nextInt(4)+2,snowLine=63+18+random.nextInt(10),snow=0;
   boolean waterCutoff=lower!=0;int minWater=(int)lower;
   for(int y=127;y>=0;y--){
    if(y<=random.nextInt(2)+1){iceBlocks[y]=7;continue;}
    int block=iceBlocks[y]&65535;
    if(y<upper&&block==0&&random.nextFloat()>.01f){iceBlocks[y]=174;block=174;}
    else if(waterCutoff&&y>minWater&&(block==8||block==9)&&y<63&&random.nextFloat()>.15f){iceBlocks[y]=174;block=174;}
    if(y>snowLine&&snow<=snowLimit&&block==174){iceBlocks[y]=80;snow++;}
   }
   Ps4Surface.column(blocks,random,biome,noise,y->temperature(x,y,z));
  }
 }
}

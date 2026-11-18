package dev.legacyconsole.worldgen.ps4;

import java.util.Random;
import java.util.function.IntToDoubleFunction;

/** Biome::buildSurface (PS4 0x855e0), with the original two/three-layer bedrock,
 * 128 random bedrock checks and console underwater/gravel depth. Values are legacy
 * tile IDs, with metadata in bits 16..31; no Java registry IDs enter this algorithm. */
public final class Ps4Surface {
 private Ps4Surface(){}
 public static void column(int[] blocks,Random random,int biome,double noise){
  column(blocks,random,biome,noise,y->Ps4BiomeData.getBiome(biome).temperature);
 }
 static void column(int[] blocks,Random random,int biome,double noise,IntToDoubleFunction temperature){
  if(blocks.length!=128)throw new IllegalArgumentException("PS4 surface column must be 128 high");
  int[] materials=materials(biome,noise);int originalTop=materials[0],originalUnder=materials[1],top=originalTop,under=originalUnder;
  int thickness=(int)(noise/3+3+random.nextDouble()*.25),remaining=-1;
  for(int y=127;y>=0;y--){
   if(y<=random.nextInt(2)+1){blocks[y]=7;continue;}
   int id=blocks[y]&65535;
   if(id==0){remaining=-1;continue;}
   if(id!=1)continue;
   if(remaining==-1){
    if(thickness<=0){top=0;under=1;}
    else if(y>=59&&y<=64){top=originalTop;under=originalUnder;}
    if(y<63&&(top&65535)==0)top=temperature.applyAsDouble(y)<.15f?79:9;
    remaining=thickness;
    if(y>=62)blocks[y]=top;
    // Warm/shallow-lukewarm ocean overrides in Biome::initBiomes
    // (0x7fbed..0x7ff3f): bottom tile sand, depth 3 or 7, plus one.
    else if(y<(biome==42||biome==43||biome==44?59:55)){top=0;under=1;blocks[y]=biome>=42&&biome<=45?12:13;}
    else blocks[y]=under;
   }else if(remaining>0){
    remaining--;blocks[y]=under;
    if(remaining==0&&thickness>1&&(under&65535)==12){remaining=random.nextInt(4)+Math.max(0,y-63);under=(under>>>16)==1?179:24;}
   }
  }
 }
 static int[] materials(int id,double noise){
  int top=2,under=3;
  if(id==2||id==17||id==130||id==16||id==26){top=12;under=12;}
  else if(id==25){top=1;under=1;}
  else if(id==14||id==15)top=110;
  else if(id==140)top=80; // IceBiome constructor 0x37378c..0x3737b8.
  else if(id==3||id==20||id==34||id==131||id==162){
   int type=id==34?1:id==131||id==162?2:0;
   if((noise< -1||noise>2)&&type==2){top=13;under=13;}
   else if(noise>1&&type!=1){top=1;under=1;}
  }else if(id==32||id==33||id==160||id==161){
   if(noise>1.75)top=3|(1<<16);else if(noise> -.95)top=3|(2<<16);
  }else if(id==163||id==164){
   if(noise>1.75){top=1;under=1;}else if(noise> -.5)top=3|(1<<16);
  }
  return new int[]{top,under};
 }
}

package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** HugeMushroomFeature::place, mcps4 0x370120. */
public final class Ps4HugeMushroomFeature {
 private Ps4HugeMushroomFeature(){}
 /** forcedTile is 0 (random), 99 (brown), or 100 (red). The feature's
  * constructor update flag controls writes independently of mushroom type. */
 public static boolean place(Ps4PlantRules.Access world,Random random,int x,int y,int z,int forcedTile,boolean updates){
  int tile=forcedTile==0?(random.nextBoolean()?99:100):forcedTile;
  int height=random.nextInt(3)+4;if(random.nextInt(12)==0)height*=2;
  if(y<1||y+height>=255)return false;
  for(int wy=y;wy<=y+height+1;wy++){
   int radius=wy>y+3?3:0;
   for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++){
    if(wy<0||wy>=256)return false;
    int old=world.block(wx,wy,wz);if(!world.airMaterial(old)&&!world.leavesMaterial(old))return false;
   }
  }
  int below=world.block(x,y-1,z)&65535;if(below!=3&&below!=2&&below!=110)return false;
  int top=y+height,low=tile==100?top-3:top,flags=updates?3:2;
  for(int wy=low;wy<=top;wy++){
   int radius=tile==99?3:wy<top?2:1;
   for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++){
    int data=5+(wx==x+radius?1:0)-(wx==x-radius?1:0)+(wz==z+radius?3:0)-(wz==z-radius?3:0);
    if(tile==99||wy<top){
     if((wx==x-radius||wx==x+radius)&&(wz==z-radius||wz==z+radius))continue;
     if(wx==x-radius+1&&wz==z-radius||wx==x-radius&&wz==z-radius+1)data=1;
     if(wx==x+radius-1&&wz==z-radius||wx==x+radius&&wz==z-radius+1)data=3;
     if(wx==x-radius+1&&wz==z+radius||wx==x-radius&&wz==z+radius-1)data=7;
     if(wx==x+radius-1&&wz==z+radius||wx==x+radius&&wz==z+radius-1)data=9;
    }
    if(data==5&&wy<top)data=0;
    if(data!=0||y>=top-1)if(!world.solidRender(world.block(wx,wy,wz)))world.setBlock(wx,wy,wz,tile|(data<<16),flags);
   }
  }
  for(int level=0;level<height;level++)if(!world.solidRender(world.block(x,y+level,z)))world.setBlock(x,y+level,z,tile|(10<<16),flags);
  return true;
 }
}

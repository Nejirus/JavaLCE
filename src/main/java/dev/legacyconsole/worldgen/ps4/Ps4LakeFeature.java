package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** LakeFeature::place, mcps4 0x3db900. Legacy states use metadata in bits
 * 16..31. Light, material and biome queries belong to the world bridge. */
public final class Ps4LakeFeature {
 public interface Access extends Ps4OreFeature.Access {
  boolean solid(int state);
  boolean liquid(int state);
  int skyLight(int x,int y,int z);
  int blockLight(int x,int y,int z);
  int biomeTop(int x,int y,int z);
  float temperature(int x,int y,int z);
 }
 private final int state;
 public Ps4LakeFeature(int tile){this.state=tile&65535;}
 public boolean place(Access world,Random random,int x,int y,int z){
  x-=8;z-=8;
  while(y>5&&(world.block(x,y,z)&65535)==0)y--;
  if(y<5)return false;
  y-=4;
  if(world.hasGenerationRules()&&world.intersectsGenerationRule(x,y,z,x+16,y+8,z+16))return false;
  boolean[] shape=new boolean[2048];
  int count=random.nextInt(4)+4;
  for(int part=0;part<count;part++){
   double width=random.nextDouble()*6+3,height=random.nextDouble()*4+2,depth=random.nextDouble()*6+3;
   double offsetX=random.nextDouble()*((16-width)-2)+1;
   double halfX=width*.5,halfY=height*.5,halfZ=depth*.5;
   double cy=halfY+random.nextDouble()*((8-height)-4)+2;
   double cz=halfZ+random.nextDouble()*((16-depth)-2)+1;
   double cx=halfX+offsetX;
   for(int bx=1;bx<15;bx++){
    double dx=(bx-cx)/halfX;dx*=dx;
    for(int bz=1;bz<15;bz++){
     double dz=(bz-cz)/halfZ;dz*=dz;
     for(int by=1;by<7;by++){
      double dy=(by-cy)/halfY;dy*=dy;
      // The first unrolled Y comparison retains a different addition order.
      if((by==1?(dz+dx)+dy:dz+(dx+dy))<1)shape[index(bx,by,bz)]=true;
     }
    }
   }
  }
  for(int bx=0;bx<16;bx++)for(int bz=0;bz<16;bz++)for(int by=0;by<8;by++){
   if(!boundary(shape,bx,by,bz))continue;
   int old=world.block(x+bx,y+by,z+bz);
   if(by>=4){if(world.liquid(old))return false;}
   else if(!world.solid(old)&&(world.block(x+bx,y+by,z+bz)&65535)!=(state&65535))return false;
  }
  for(int bx=0;bx<16;bx++)for(int bz=0;bz<16;bz++)for(int by=0;by<8;by++)
   if(shape[index(bx,by,bz)])world.setBlock(x+bx,y+by,z+bz,by<4?state:0,2);
  for(int bx=0;bx<16;bx++)for(int bz=0;bz<16;bz++)for(int by=4;by<8;by++){
   if(!shape[index(bx,by,bz)])continue;
   int wx=x+bx,wy=y+by-1,wz=z+bz;
   if((world.block(wx,wy,wz)&65535)==3&&world.skyLight(wx,wy+1,wz)>0)
    world.setBlock(wx,wy,wz,world.biomeTop(wx,wy,wz)==110?110:2,2);
  }
  if((state&65535)==10||(state&65535)==11){
   for(int bx=0;bx<16;bx++)for(int bz=0;bz<16;bz++)for(int by=0;by<8;by++){
    if(!boundary(shape,bx,by,bz)||by>=4&&random.nextInt(2)==0)continue;
    if(world.solid(world.block(x+bx,y+by,z+bz)))world.setBlock(x+bx,y+by,z+bz,1,2);
   }
  }
  if((state&65535)==8||(state&65535)==9){
   for(int bx=0;bx<16;bx++)for(int bz=0;bz<16;bz++){
    int wx=x+bx,wy=y+4,wz=z+bz;
    if(!(world.temperature(wx,wy,wz)<=.15f)||wy<0||wy>=256||world.blockLight(wx,wy,wz)>=10)continue;
    int old=world.block(wx,wy,wz),id=old&65535;
    if((id==8||id==9)&&old>>>16==0)world.setBlock(wx,wy,wz,79,2);
   }
  }
  return true;
 }
 private static int index(int x,int y,int z){return (x*16+z)*8+y;}
 private static boolean boundary(boolean[] shape,int x,int y,int z){
  int p=index(x,y,z);
  return !shape[p]&&(x<15&&shape[p+128]||x>0&&shape[p-128]||z<15&&shape[p+8]||z>0&&shape[p-8]||y<7&&shape[p+1]||y>0&&shape[p-1]);
 }
}

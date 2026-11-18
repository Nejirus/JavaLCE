package dev.legacyconsole.worldgen.ps4;

import java.util.Random;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;

/** mcps4 underwater cave/canyon routines (0x714f30/0x713fd0).
 * These use their own chunk seeds, direct libc float trigonometry, and
 * float voxel tests. The air carvers have different rules. */
final class Ps4UnderwaterCarvers {
 private Ps4UnderwaterCarvers(){}
 private static final float PI=0x1.921fb6p1f;
 static void carve(long seed,int cx,int cz,byte[] blocks,IntBinaryOperator biomes,IntConsumer writes){
  new Caves(biomes,writes).apply(seed,cx,cz,blocks);
  new Canyon(biomes,writes).apply(seed,cx,cz,blocks);
 }
 private static boolean canReplace(int id){
  return switch(id){case 0,1,2,3,8,9,10,11,12,13,24,49,110,159,172,179->true;default->false;};
 }
 private static float signedFloat(Random random){return random.nextFloat()-random.nextFloat();}
 private static int clamp(int value,int min,int max){return Math.min(max,Math.max(min,value));}
 private static int floor(double value){return (int)Math.floor(value);}
 /** Only an enclosed voxel becomes still water. Adjacent air receives flowing
  * water instead, in the original +X,-X,+Z,-Z order. */
 private static void water(byte[] blocks,int x,int z,int y,IntConsumer writes){
  int p=(x<<11)|(z<<7)|y;boolean open=false;
  if(x<15&&blocks[p+2048]==0){Ps4Carvers.write(blocks,p+2048,8,writes);open=true;}
  if(x>0&&blocks[p-2048]==0){Ps4Carvers.write(blocks,p-2048,8,writes);open=true;}
  if(z<15&&blocks[p+128]==0){Ps4Carvers.write(blocks,p+128,8,writes);open=true;}
  if(z>0&&blocks[p-128]==0){Ps4Carvers.write(blocks,p-128,8,writes);open=true;}
  if(!open)Ps4Carvers.write(blocks,p,9,writes);
 }
 private abstract static class Large {
  final Random random=new Random();final IntBinaryOperator biomes;final IntConsumer writes;
  Large(IntBinaryOperator biomes,IntConsumer writes){this.biomes=biomes;this.writes=writes;}
  void apply(long seed,int cx,int cz,byte[] blocks){
   random.setSeed(seed);int a=random.nextInt(),b=random.nextInt();
   long ax=(a-(a>>31))|1,bz=(b-(b>>31))|1;
   for(int x=cx-8;x<=cx+8;x++)for(int z=cz-8;z<=cz+8;z++){
    random.setSeed(((long)x*ax+(long)z*bz)^seed);addFeature(x,z,cx,cz,blocks);
   }
  }
  boolean ocean(int x,int z){return Ps4BiomeData.isOceanCategory(biomes.applyAsInt(x,z));}
  abstract void addFeature(int x,int z,int cx,int cz,byte[] blocks);
 }
 private static final class Caves extends Large {
  Caves(IntBinaryOperator biomes,IntConsumer writes){super(biomes,writes);}
  void addFeature(int x,int z,int cx,int cz,byte[] blocks){
   int count=random.nextInt(random.nextInt(random.nextInt(40)+1)+1);
   if(random.nextInt(15)!=0)count=0;
   for(int i=0;i<count;i++){
    double z0=z*16+random.nextInt(16),y0=random.nextInt(random.nextInt(120)+8),x0=x*16+random.nextInt(16);
    int tunnels=1;
    if(random.nextInt(4)==0){
     long roomSeed=random.nextLong();float thickness=random.nextFloat()*6+1;
     tunnel(roomSeed,cx,cz,blocks,x0,y0,z0,thickness,0,0,-1,-1,.5);
     tunnels+=random.nextInt(4);
    }
    for(int j=0;j<tunnels;j++){
     float yaw=random.nextFloat()*PI*2,pitch=((random.nextFloat()-.5f)*2)*.125f;
     float thickness=random.nextFloat()*2+random.nextFloat();
     tunnel(random.nextLong(),cx,cz,blocks,x0,y0,z0,thickness,yaw,pitch,0,0,1);
    }
   }
  }
  /** UnderwaterCaveFeature::addTunnel, 0x7150a0. */
  void tunnel(long seed,int cx,int cz,byte[] blocks,double x,double y,double z,float thickness,float yaw,float pitch,int step,int distance,double verticalScale){
   int startX=floor(x),startZ=floor(z);Random random=new Random(seed);
   if(distance<=0)distance=112-random.nextInt(28);
   boolean room=step==-1;if(room)step=distance/2;
   int split=random.nextInt(distance/2)+distance/4;
   float damping=random.nextInt(6)==0?.92f:.7f;
   float midX=cx*16+8,midZ=cz*16+8,pitchChange=0,yawChange=0,reach=thickness+2+16;
   for(;step<distance;step++){
    float radius=Ps4Math.sinDirect(step*PI/distance);
    float pc=Ps4Math.cosDirect(pitch);
    x+=(double)(pc*Ps4Math.cosDirect(yaw));y+=(double)Ps4Math.sinDirect(pitch);z+=(double)(pc*Ps4Math.sinDirect(yaw));
    float pDelta=signedFloat(random)*random.nextFloat(),yDelta=signedFloat(random)*random.nextFloat();
    yaw=yaw+yawChange*.1f;pitch=pitch*damping+pitchChange*.1f;
    yawChange=yawChange*.75f+yDelta*4;pitchChange=pitchChange*.9f+(pDelta+pDelta);
    if(!room){
     if(thickness>1&&step==split){
      long childSeed=random.nextLong();float width=random.nextFloat()*.5f+.5f;
      tunnel(childSeed,cx,cz,blocks,x,y,z,width,yaw-PI/2,pitch/3,step,distance,1);
      childSeed=random.nextLong();width=random.nextFloat()*.5f+.5f;
      tunnel(childSeed,cx,cz,blocks,x,y,z,width,yaw+PI/2,pitch/3,step,distance,1);return;
     }
     if(random.nextInt(4)==0)continue;
    }
    float dx=(float)x-midX,dz=(float)z-midZ,remaining=distance-step;
    if(dx*dx+dz*dz-remaining*remaining>reach*reach)return;
    radius=radius*thickness+1.5f;float twice=radius+radius;
    if(x<(double)(midX-16-twice)||z<(double)(midZ-16-twice)||x>(double)(midX+16+twice)||z>(double)(midZ+16+twice))continue;
    float height=radius*(float)verticalScale;
    int x0=Math.max(0,floor(x-radius)-cx*16-1),x1=Math.min(16,floor(x+radius)-cx*16+1);
    int z0=Math.max(0,floor(z-radius)-cz*16-1),z1=Math.min(16,floor(z+radius)-cz*16+1);
    int y1=clamp(floor(y+height)+1,1,120),y0=clamp(floor(y-height)-1,1,y1);
    boolean wrote=false;
    for(int xx=x0;xx<x1;xx++){
     float nx=((xx+cx*16+.5f)-(float)x)/radius;nx*=nx;
     for(int zz=z0;zz<z1;zz++){
      float nz=((zz+cz*16+.5f)-(float)z)/radius;nz*=nz;
      if(nx+nz>=1)continue;
      for(int upper=y1;upper>y0;upper--){
       int yy=upper-1;float ny=((yy+.5f)-(float)y)/height;
       if((double)ny<=-.7||upper>62||ny*ny+nx+nz>=1)continue;
       int p=(xx<<11)|(zz<<7)|yy;if(!canReplace(blocks[p]&255))continue;
       if(!wrote&&!ocean(startX,startZ))return;
       if(upper==10)Ps4Carvers.write(blocks,p,random.nextFloat()<.25f?213:49,writes);
       else if(upper<10)Ps4Carvers.write(blocks,p,11,writes);
       else water(blocks,xx,zz,yy,writes);
       wrote=true;
      }
     }
    }
    if(room)break;
   }
  }
 }
 private static final class Canyon extends Large {
  final float[] widths=new float[128];
  Canyon(IntBinaryOperator biomes,IntConsumer writes){super(biomes,writes);}
  void addFeature(int x,int z,int cx,int cz,byte[] blocks){
   if(random.nextInt(20)!=0)return;
   double x0=x*16+random.nextInt(16),y0=random.nextInt(random.nextInt(40)+8)+20;
   if(random.nextInt()<1)y0+=15;
   double z0=z*16+random.nextInt(16);
   float yaw=random.nextFloat()*PI*2,pitch=((random.nextFloat()-.5f)*2)*.125f;
   float thickness=random.nextFloat()*3+random.nextFloat()*3;
   if(random.nextFloat()<.05f)thickness+=thickness;
   tunnel(random.nextLong(),cx,cz,blocks,x0,y0,z0,thickness,yaw,pitch,0,0,3);
  }
  /** UnderwaterCanyonFeature::addTunnel, 0x714140. */
  void tunnel(long seed,int cx,int cz,byte[] blocks,double x,double y,double z,float thickness,float yaw,float pitch,int step,int distance,double verticalScale){
   int startX=floor(x),startZ=floor(z);Random random=new Random(seed);
   if(distance<=0)distance=112-random.nextInt(28);
   boolean room=step==-1;if(room)step=distance/2;
   float f=1;for(int i=0;i<128;i++){if(i==0||random.nextInt(3)==0)f=random.nextFloat()*random.nextFloat()+1;widths[i]=f*f;}
   float midX=cx*16+8,midZ=cz*16+8,pitchChange=0,yawChange=0,reach=thickness+2+16;
   for(;step<distance;step++){
    float radius=Ps4Math.sinDirect(step*PI/distance),w=random.nextFloat(),h=random.nextFloat();
    float pc=Ps4Math.cosDirect(pitch);
    x+=(double)(Ps4Math.cosDirect(yaw)*pc);y+=(double)Ps4Math.sinDirect(pitch);z+=(double)(Ps4Math.sinDirect(yaw)*pc);
    float pDelta=signedFloat(random)*random.nextFloat(),yDelta=signedFloat(random)*random.nextFloat();
    if(room||random.nextInt(4)!=0){
     float dx=(float)x-midX,dz=(float)z-midZ,remaining=distance-step;
     if(dx*dx+dz*dz-remaining*remaining>reach*reach)return;
     radius=radius*thickness+1.5f;float horizontal=radius*(w*.25f+.75f),twice=horizontal+horizontal;
     if(x>=(double)(midX-16-twice)&&z>=(double)(midZ-16-twice)&&x<=(double)(midX+16+twice)&&z<=(double)(midZ+16+twice)){
      float height=radius*(float)verticalScale*(h*.25f+.75f);
      int x0=Math.max(0,floor(x-horizontal)-cx*16-1),x1=Math.min(16,floor(x+horizontal)-cx*16+1);
      int z0=Math.max(0,floor(z-horizontal)-cz*16-1),z1=Math.min(16,floor(z+horizontal)-cz*16+1);
      int y1=clamp(floor(y+height)+1,1,120),y0=clamp(floor(y-height)-1,1,y1);
      boolean wrote=false;
      for(int xx=x0;xx<x1;xx++){
       float nx=((xx+cx*16+.5f)-(float)x)/horizontal;
       for(int zz=z0;zz<z1;zz++){
        float nz=((zz+cz*16+.5f)-(float)z)/horizontal,horizontalDistance=nz*nz+nx*nx;
        if(horizontalDistance>=1)continue;
        for(int upper=y1;upper>y0;upper--){
         int yy=upper-1;if(upper>62)continue;
         float ny=((yy+.5f)-(float)y)/height;
         if(ny*ny/6+horizontalDistance*widths[upper-2]>=1)continue;
         int p=(xx<<11)|(zz<<7)|yy;if(!canReplace(blocks[p]&255))continue;
         if(!wrote&&!ocean(startX,startZ))return;
         if(upper<11)Ps4Carvers.write(blocks,p,random.nextFloat()<.25f?213:49,writes);
         else water(blocks,xx,zz,yy,writes);
         wrote=true;
        }
       }
      }
      if(room)break;
     }
    }
    yaw=yaw+yawChange*.05f;pitch=pitch*.7f+pitchChange*.05f;
    yawChange=yawChange*.5f+yDelta*4;pitchChange=pitchChange*.8f+(pDelta+pDelta);
   }
  }
 }
}

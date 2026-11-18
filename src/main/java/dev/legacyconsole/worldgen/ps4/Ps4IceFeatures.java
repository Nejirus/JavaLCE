package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** mcps4 BlueIceFeature 0xd1300, IcePatchFeature 0x374370 and
 * IceSpikeFeature 0x3745d0. World writes retain original flags and order. */
public final class Ps4IceFeatures {
 public interface Access extends Ps4PlantRules.Access {int seaLevel();}
 private static final int[][] DIRECTIONS={{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
 private Ps4IceFeatures(){}
 public static boolean blueIce(Access world,Random random,int x,int y,int z){
  if(y>=world.seaLevel())return false;
  if((world.block(x,y,z)&65535)!=9&&(world.block(x,y-1,z)&65535)!=9)return false;
  boolean packed=false;
  for(int i=1;i<6;i++){int[] d=DIRECTIONS[i];if((world.block(x+d[0],y+d[1],z+d[2])&65535)==174){packed=true;break;}}
  if(!packed)return false;
  world.setBlock(x,y,z,273,2);
  for(int attempt=0;attempt<200;attempt++){
   int dy=random.nextInt(5)-random.nextInt(6),radius=3;if(dy<2)radius=dy/2+3;
   if(radius<1)continue;
   int wx=x+random.nextInt(radius)-random.nextInt(radius),wy=y+dy,wz=z+random.nextInt(radius)-random.nextInt(radius);
   int state=world.block(wx,wy,wz),id=state&65535;
   if(!world.airMaterial(state)&&id!=9&&id!=174&&id!=79)continue;
   int neighbors=0;
   for(int[] d:DIRECTIONS){if((world.block(wx+d[0],wy+d[1],wz+d[2])&65535)==273)neighbors++;if(neighbors>4)break;}
   if(neighbors>=1&&neighbors<=4)world.setBlock(wx,wy,wz,273,2);
  }
  return true;
 }
 public static boolean patch(Access world,Random random,int x,int y,int z,int size,int state){
  boolean empty=(world.block(x,y,z)&65535)==0;
  if(empty&&y>2){do{y--;empty=(world.block(x,y,z)&65535)==0;if(!empty)break;}while(y>2);}
  if((world.block(x,y,z)&65535)!=80)return false;
  int radius=2+random.nextInt(size-2);
  for(int wx=x-radius;wx<=x+radius;wx++)for(int wz=z-radius;wz<=z+radius;wz++){
   int dx=wx-x,dz=wz-z;if(dx*dx+dz*dz>radius*radius)continue;
   for(int wy=y-1;wy<=y+1;wy++){int id=world.block(wx,wy,wz)&65535;if(id==3||id==80||id==79)world.setBlock(wx,wy,wz,state,2);}
  }
  return true;
 }
 private static boolean spikeReplaceable(Access world,int state,boolean roots){int id=state&65535;return world.airMaterial(state)||id==3||id==80||id==79||roots&&id==174;}
 public static boolean spike(Access world,Random random,int x,int y,int z,boolean updates){
  boolean empty=(world.block(x,y,z)&65535)==0;
  if(empty&&y>2){do{y--;empty=(world.block(x,y,z)&65535)==0;if(!empty)break;}while(y>2);}
  if((world.block(x,y,z)&65535)!=80)return false;
  y+=random.nextInt(4);int height=random.nextInt(4)+7,radius=height/4+random.nextInt(2),flags=updates?3:2;
  if(radius>1&&random.nextInt(60)==0)y+=10+random.nextInt(30);
  for(int level=0;level<height;level++){
   float width=(1.0F-(float)level/(float)height)*(float)radius;int bound=(int)width;if((float)bound<width)bound++;
   for(int dx=-bound;dx<=bound;dx++)for(int dz=-bound;dz<=bound;dz++){
    float fx=(float)Math.abs(dx)-0.25F,fz=(float)Math.abs(dz)-0.25F;
    if((dx!=0||dz!=0)&&fx*fx+fz*fz>width*width)continue;
    if((dx==-bound||dx==bound||dz==-bound||dz==bound)&&random.nextFloat()>0.75F)continue;
    if(spikeReplaceable(world,world.block(x+dx,y+level,z+dz),false))world.setBlock(x+dx,y+level,z+dz,174,flags);
    if(bound>=2&&level!=0&&spikeReplaceable(world,world.block(x+dx,y-level,z+dz),false))world.setBlock(x+dx,y-level,z+dz,174,flags);
   }
  }
  int rootRadius=Math.max(0,Math.min(1,radius-1));
  for(int dx=-rootRadius;dx<=rootRadius;dx++)for(int dz=-rootRadius;dz<=rootRadius;dz++){
   int wy=y-1,remaining=Math.abs(dx)==1&&Math.abs(dz)==1?random.nextInt(5):50;
   while(wy>50){
    if(!spikeReplaceable(world,world.block(x+dx,wy,z+dz),true))break;
    world.setBlock(x+dx,wy,z+dz,174,flags);wy--;remaining--;
    if(remaining<=0){wy-=random.nextInt(5)+1;remaining=random.nextInt(5);}
   }
  }
  return true;
 }
}

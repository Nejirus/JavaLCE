package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** BlockBlobFeature::place, mcps4 0xb85d0, used for taiga boulders. */
public final class Ps4BlockBlobFeature {
 private Ps4BlockBlobFeature(){}
 public static boolean place(Ps4PlantRules.Access world,Random random,int x,int y,int z,int baseRadius,int state){
  boolean ground=false;
  while(y>3){
   if((world.block(x,y-1,z)&65535)!=0){int below=world.block(x,y-1,z)&65535;if(below==2||below==3||below==1){ground=true;break;}}
   y--;
  }
  if(!ground)return false;
  if(baseRadius<0)return true;
  for(int round=0;round<3;round++){
   int rx=baseRadius+random.nextInt(2),ry=baseRadius+random.nextInt(2),rz=baseRadius+random.nextInt(2);
   float radius=(float)(rx+ry+rz)*0.333f+0.5f;
   // Original BlockPos iterator advances X, then Y, then Z.
   for(int wz=z-rz;wz<=z+rz;wz++)for(int wy=y-ry;wy<=y+ry;wy++)for(int wx=x-rx;wx<=x+rx;wx++){
    double dx=(double)wx-(double)x,dy=(double)wy-(double)y,dz=(double)wz-(double)z;
    if(dx*dx+dy*dy+dz*dz<=(double)(radius*radius))world.setBlock(wx,wy,wz,state,4);
   }
   x+=random.nextInt(baseRadius*2+2)-baseRadius-1;y-=random.nextInt(2);z+=random.nextInt(baseRadius*2+2)-baseRadius-1;
  }
  return true;
 }
}

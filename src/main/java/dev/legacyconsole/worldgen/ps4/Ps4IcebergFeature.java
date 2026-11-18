package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** IcebergFeature, mcps4 0x372190..0x3736bf. The original feature retains
 * ellipse parameters between placements, including subsequent round icebergs. */
public final class Ps4IcebergFeature {
 private boolean snow,ellipse;
 private double angle=(double)(float)Math.PI;
 private int major,minor,inset,state;
 record State(boolean snow,boolean ellipse,long angleBits,int major,int minor,int inset,int state){}
 State snapshot(){return new State(snow,ellipse,Double.doubleToRawLongBits(angle),major,minor,inset,state);}
 private static int ceil(float value){return (int)Math.ceil(value);}
 private static boolean ice(int id){return id==174||id==80||id==273;}
 private static boolean carved(int id){return ice(id)||id==78;}

 public boolean place(Ps4IceFeatures.Access world,Random random,int x,int ignoredY,int z){
  snow=random.nextDouble()>0.7;ellipse=random.nextDouble()>0.8;
  if(ellipse){double a=random.nextDouble();angle=(a+a)*(double)(float)Math.PI;major=15-inset-random.nextInt(4);minor=3+random.nextInt(3);}
  state=random.nextDouble()>0.01?174:273;
  int y=world.seaLevel(),height=random.nextInt(ellipse?4:11)+3+(ellipse?3:0);
  if(!ellipse&&random.nextDouble()>0.9)height+=7+random.nextInt(15);
  int underwater=Math.min(height+random.nextInt(11),18),width=Math.min(height+random.nextInt(7)-random.nextInt(5),11),bound=ellipse?major:11;
  for(int dx=-bound;dx<bound;dx++)for(int dz=-bound;dz<bound;dz++)for(int level=0;level<height;level++){
   int radius;
   if(ellipse)radius=ceil((1.0F-((float)level*(float)level)/(float)height)*(float)width*0.5F);
   else{
    float taper=(3.5F-random.nextFloat())*(float)height,ratio;
    if(height>random.nextInt(5)+15){int value=level<random.nextInt(6)+3?level/2:level;ratio=(float)value/(taper*0.4F);}
    else ratio=((float)level*(float)level)/taper;
    radius=ceil((1.0F-ratio)*(float)width*0.5F);
   }
   if(ellipse||dx<radius)generate(world,random,x,y,z,height,dx,level,dz,radius,bound);
  }
  smooth(world,x,y,z,width,height);
  for(int dx=-bound;dx<bound;dx++)for(int dz=-bound;dz<bound;dz++)for(int level=-1;level>-underwater;level--){
   int horizontal=ellipse?ceil((1.0F-((float)level*(float)level)/((float)underwater*8.0F))*(float)bound):bound;
   int radius=ceil((1.0F-(float)-level/((random.nextFloat()*0.5F+1.0F)*(float)underwater))*(float)width*0.5F);
   if(dx<radius)generate(world,random,x,y,z,underwater,dx,level,dz,radius,horizontal);
  }
  if(random.nextDouble()>(ellipse?0.1:0.7))carve(world,random,x,y,z,width,height);
  return true;
 }
 /** 0x3727b0, including its per-cell shape noise and edge rejection draws. */
 private void generate(Ps4IceFeatures.Access world,Random random,int x,int y,int z,int height,int dx,int level,int dz,int radius,int bound){
  double distance;
  if(ellipse){
   int adjusted=minor;if(level>0&&height-level<4)adjusted=minor+height-level-4;
   double cos=Ps4Math.cosDouble(angle),sin=Ps4Math.sinDouble(angle);
   double a=(cos*(double)dx-sin*(double)dz)/(double)bound,b=(sin*(double)dx+(double)dz*cos)/(double)adjusted;
   distance=a*a+b*b-1.0;
  }else{
   float noise=Math.max(0.2F,Math.min(0.8F,random.nextFloat()));
   distance=((double)dx*(double)dx+(double)((noise*10.0F)/(float)radius)+(double)dz*(double)dz)-(double)radius*(double)radius;
  }
  if(!(distance<0.0))return;
  if((ellipse?distance> -0.5:distance>(double)(-random.nextInt(3)-6))&&random.nextDouble()>0.9)return;
  placeBlock(world,random,x+dx,y+level,z+dz,height-level,height);
 }
 /** 0x373230: snow eligibility consumes RNG even when the final state is ice. */
 private void placeBlock(Ps4IceFeatures.Access world,Random random,int x,int y,int z,int depth,int height){
  int current=world.block(x,y,z),id=current&65535;
  if(!world.airMaterial(current)&&id!=80&&id!=79&&id!=9&&id!=8)return;
  boolean white=!ellipse||random.nextDouble()>0.05;
  int target=state;
  if(snow&&id!=9){int bound=Math.max(1,height/(ellipse?3:2));if((double)height*0.6+(double)random.nextInt(bound)>=(double)depth&&white)target=80;}
  world.setBlock(x,y,z,target,2);
 }
 /** 0x372a30, before the underwater half is generated. */
 private void smooth(Ps4IceFeatures.Access world,int x,int y,int z,int width,int height){
  int bound=ellipse?major:width/2;
  for(int dx=-bound;dx<=bound;dx++)for(int dz=-bound;dz<=bound;dz++)for(int level=0;level<=height;level++){
   int wx=x+dx,wy=y+level,wz=z+dz,id=world.block(wx,wy,wz)&65535;
   if(!carved(id))continue;
   if(world.airMaterial(world.block(wx,wy-1,wz))){world.setBlock(wx,wy,wz,0,2);world.setBlock(wx,wy+1,wz,0,2);}
   else if(ice(id)){
    int west=world.block(wx-1,wy,wz)&65535,east=world.block(wx+1,wy,wz)&65535,north=world.block(wx,wy,wz-1)&65535,south=world.block(wx,wy,wz+1)&65535;
    int missing=(ice(west)?0:1)+(ice(east)?0:1)+(ice(north)?0:1)+(ice(south)?0:1);
    if(missing>2)world.setBlock(wx,wy,wz,0,2);
   }
  }
 }
 /** 0x372e50, selecting carve offsets and the two vertical passes. */
 private void carve(Ps4IceFeatures.Access world,Random random,int x,int y,int z,int width,int height){
  boolean negativeX=random.nextBoolean(),negativeZ=random.nextBoolean();int half=width/2,bound=Math.max(1,half-2),cx=random.nextInt(bound);
  if(random.nextBoolean())cx=half+1-random.nextInt(Math.max(1,width-half-1));
  int cz=random.nextInt(bound);if(random.nextBoolean())cz=half+1-random.nextInt(Math.max(1,width-half-1));
  if(ellipse){cx=random.nextInt(Math.max(1,major-5));cz=cx;}
  cx*=negativeX?-1:1;cz*=negativeZ?-1:1;
  double rotation;if(ellipse)rotation=angle+(double)(float)(Math.PI/2.0);else{double a=random.nextDouble();rotation=(a+a)*(double)(float)Math.PI;}
  for(int level=0;level<height-3;level++){
   float taper=(3.5F-random.nextFloat())*(float)height,ratio;
   if(height>random.nextInt(5)+15){int value=level<random.nextInt(6)+3?level/2:level;ratio=(float)value/(taper*0.4F);}
   else ratio=((float)level*(float)level)/taper;
   int radius=ceil((1.0F-ratio)*(float)width*0.5F);
   cut(world,x,y,z,radius,level,false,cx,cz,rotation);
  }
  int level=-1;
  while(random.nextInt(5)-height<level){
   int radius=ceil((1.0F-(float)-level/((random.nextFloat()*0.5F+1.0F)*(float)height))*(float)width*0.5F);
   cut(world,x,y,z,radius,level,true,cx,cz,rotation);level--;
  }
 }
 /** 0x3733b0, with the original asymmetric loop endpoints and snow cleanup. */
 private void cut(Ps4IceFeatures.Access world,int x,int y,int z,int radius,int level,boolean underwater,int cx,int cz,double rotation){
  int extra=major/3,bound=radius+extra+1;
  if(bound<=0)return;
  int shortRadius=Math.min(3,radius-3)-1+minor/2;
  double cos=Ps4Math.cosDouble(rotation),sin=Ps4Math.sinDouble(rotation);
  for(int dx=-bound;dx<bound;dx++)for(int dz=-bound;dz<bound;dz++){
   double a=(cos*(double)(dx-cx)-sin*(double)(dz-cz))/(double)bound,b=(sin*(double)(dx-cx)+cos*(double)(dz-cz))/(double)shortRadius;
   if(!(a*a+b*b-1.0<0.0))continue;
   int wx=x+dx,wy=y+level,wz=z+dz;if(!carved(world.block(wx,wy,wz)&65535))continue;
   world.setBlock(wx,wy,wz,underwater?9:0,2);
   if(!underwater&&(world.block(wx,wy+1,wz)&65535)==78)world.setBlock(wx,wy+1,wz,0,2);
  }
 }
}

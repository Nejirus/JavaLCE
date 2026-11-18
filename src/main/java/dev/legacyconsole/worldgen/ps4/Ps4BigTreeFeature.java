package dev.legacyconsole.worldgen.ps4;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** BigTreeFeature, recovered from mcps4 0x7e020..0x7f340.
 * Instances retain their height limit, as the original feature does. */
public final class Ps4BigTreeFeature {
 private final boolean updates;
 private final int defaultLog,defaultLeaves;
 private final Random random=new Random(0);
 private final List<int[]> nodes=new ArrayList<>();
 private int heightLimit,height,leafDistance=4;
 private int x,y,z;
 private Ps4TreeTrunk.Access world;

 public Ps4BigTreeFeature(boolean updates,int defaultLog,int defaultLeaves){this.updates=updates;this.defaultLog=defaultLog;this.defaultLeaves=defaultLeaves;}
 /** Original prepare (0x7f040). */
 public void prepare(){leafDistance=5;}
 public boolean place(Ps4TreeTrunk.Access world,Random populationRandom,int x,int y,int z){
  this.world=world;this.x=x;this.y=y;this.z=z;
  random.setSeed(populationRandom.nextLong());
  if(heightLimit==0)heightLimit=5+populationRandom.nextInt(12);
  if(!validLocation())return false;
  leafNodes();
  for(int[] node:nodes)leafNode(node);
  limb(x,y,z,x,y+height,z);
  for(int[] node:nodes)if((x!=node[0]||node[3]!=node[1]||z!=node[2])&&(double)heightLimit*0.2<=(double)(node[3]-y))limb(x,node[3],z,node[0],node[1],node[2]);
  return true;
 }
 private boolean validLocation(){
  int below=world.block(x,y-1,z)&65535;
  if(below!=3&&below!=2&&below!=60)return false;
  int distance=checkLine(x,y,z,x,y+heightLimit-1,z);
  if(distance==-1)return true;
  if(distance<=5)return false;
  heightLimit=distance;return true;
 }
 private void leafNodes(){
  height=Math.min((int)((double)heightLimit*0.618),heightLimit-1);
  double density=(double)heightLimit/13.0;
  int attempts=Math.max(1,(int)(density*density+1.382)),top=y+height;
  nodes.clear();nodes.add(new int[]{x,y+heightLimit-leafDistance,z,top});
  for(int level=heightLimit-leafDistance;level>=0;level--){
   if((float)heightLimit*0.3f>(float)level)continue;
   float half=(float)heightLimit*0.5f,difference=half-(float)level,radius=0;
   if(difference==0)radius=half*0.5f;
   else if(Math.abs(difference)<half)radius=(float)Math.sqrt(half*half-difference*difference)*0.5f;
   for(int attempt=0;attempt<attempts;attempt++){
    double distance=((double)random.nextFloat()+0.328)*(double)radius;
    float draw=random.nextFloat(),angle=(draw+draw)*3.1415927f;
    int nx=floor((double)x+((double)Ps4Math.sin(angle)*distance+0.5));
    int ny=floor((double)y+((double)level-1.0));
    int nz=floor(((double)Ps4Math.cos(angle)*distance+0.5)+(double)z);
    if(checkLine(nx,ny,nz,nx,ny+leafDistance,nz)!=-1)continue;
    int dx=x-nx,dz=z-nz;
    float horizontal=(float)Math.sqrt((float)(dz*dz+dx*dx));
    double branch=(double)ny-(double)horizontal*0.381;
    int branchY=(double)top<branch?top:(int)branch;
    if(checkLine(x,branchY,z,nx,ny,nz)==-1)nodes.add(new int[]{nx,ny,nz,branchY});
   }
  }
 }
 private int checkLine(int x0,int y0,int z0,int x1,int y1,int z1){
  int dx=x1-x0,dy=y1-y0,dz=z1-z0,steps=Math.max(Math.max(Math.abs(dx),Math.abs(dy)),Math.abs(dz));
  if(steps<1)return -1;
  float sx=(float)dx/(float)steps,sy=(float)dy/(float)steps,sz=(float)dz/(float)steps;
  for(int step=0;step<=steps;step++){
   int px=floor((double)((float)step*sx+0.5f)+(double)x0),py=floor((double)((float)step*sy+0.5f)+(double)y0),pz=floor((double)((float)step*sz+0.5f)+(double)z0);
   if(!Ps4TreeRules.canGrowInto(world,world.block(px,py,pz)))return step;
  }
  return -1;
 }
 private void leafNode(int[] node){
  int leaves=defaultLeaves&~(8<<16);
  for(int level=0;level<leafDistance;level++)leafPlane(node[0],node[1]+level,node[2],level==0||level==leafDistance-1?2.0f:3.0f,leaves);
 }
 private void leafPlane(int cx,int cy,int cz,float radius,int leaves){
  int extent=(int)((double)radius+0.618);
  for(int dx=-extent;dx<=extent;dx++)for(int dz=-extent;dz<=extent;dz++){
   double a=(double)Math.abs(dx)+0.5,b=(double)Math.abs(dz)+0.5;
   if(a*a+b*b<=(double)(radius*radius)){
    int old=world.block(cx+dx,cy,cz+dz);
    if(world.airMaterial(old)||world.leavesMaterial(old))world.setBlock(cx+dx,cy,cz+dz,leaves,updates?3:2);
   }
  }
 }
 private void limb(int x0,int y0,int z0,int x1,int y1,int z1){
  int dx=x1-x0,dy=y1-y0,dz=z1-z0,steps=Math.max(Math.max(Math.abs(dx),Math.abs(dy)),Math.abs(dz));
  float sx=(float)dx/(float)steps,sy=(float)dy/(float)steps,sz=(float)dz/(float)steps;
  for(int step=0;step<=steps;step++){
   int px=floor((double)((float)step*sx+0.5f)+(double)x0),py=floor((double)((float)step*sy+0.5f)+(double)y0),pz=floor((double)((float)step*sz+0.5f)+(double)z0);
   int ax=Math.abs(px-x0),az=Math.abs(pz-z0),axis=Math.max(ax,az)<1?0:az<=ax?4:8;
   world.setBlock(px,py,pz,(defaultLog&~(12<<16))|(axis<<16),updates?3:2);
  }
 }
 /** BlockPos(double,double,double), 0x744fe0: SSE indefinite on invalid input. */
 private static int floor(double value){double f=Math.floor(value);return !Double.isFinite(f)||f<Integer.MIN_VALUE||f>Integer.MAX_VALUE?Integer.MIN_VALUE:(int)f;}
 int heightLimit(){return heightLimit;}
 int trunkHeight(){return height;}
 int leafDistance(){return leafDistance;}
 List<int[]> leafNodesForValidation(){return nodes;}
}

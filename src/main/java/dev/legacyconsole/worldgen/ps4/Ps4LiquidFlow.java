package dev.legacyconsole.worldgen.ps4;

import java.util.ArrayDeque;
import java.util.Random;

/** DynamicLiquidBlock tick/flow (0x221e90, 0x2215c0, 0x222050..0x222890).
 * World callbacks retain the original primary/secondary liquid layers and
 * notification/scheduling boundaries. Block classifications are supplied. */
public final class Ps4LiquidFlow {
 public record Pair(int primary,int secondary){public int effective(){return secondary==0?primary:secondary;}}
 public interface Access extends Ps4LavaFireTick.Access {
  Pair pairedBlocks(int x,int y,int z);
  int secondaryBlock(int x,int y,int z);
  void setSecondary(int x,int y,int z,int state,int flags);
  void removePrimary(int x,int y,int z);
  boolean blocksFluid(int state);
  boolean canPassFluidFace(int x,int y,int z,int state,int direction);
  boolean dropsInWater(int state);
  void dropBlock(int x,int y,int z,int state,int fortune);
  void levelEvent(int event,int x,int y,int z,int data);
  void fizz(int x,int y,int z);
  void scheduleFluid(int x,int y,int z,int tile,int delay);
  void notifyFluidNeighbors(int x,int y,int z,int tile,int flags);
  int finiteChunkCount();
  boolean hotFlowStep();
  boolean ultraWarm();
  boolean immediateFluidTicks();
  boolean skipLavaWaterReaction();
  /** Original material identities 0x1418330 and 0x1418ff0. */
  boolean lavaStoneContactMaterial(int state);
 }
 private record Pending(Access world,int x,int y,int z,int state,Random random){}
 private static final int[][] FLOW={{-1,0,5},{1,0,4},{0,-1,3},{0,1,2}};
 private final boolean lava;
 private final int tile;
 private final Ps4DirectionPlane horizontal;
 private final ArrayDeque<Pending> pending=new ArrayDeque<>();
 private boolean processing;
 public Ps4LiquidFlow(boolean lava){this(lava,new Ps4DirectionPlane());}
 public Ps4LiquidFlow(boolean lava,Ps4DirectionPlane horizontal){this.lava=lava;this.horizontal=horizontal;tile=lava?10:8;}

 public void tick(Access world,Random random,int x,int y,int z,int state){
  Pending event=new Pending(world,x,y,z,state,random);
  if(!world.immediateFluidTicks()){tickMain(event);return;}
  pending.addLast(event);if(processing)return;processing=true;
  for(int count=0;count<100&&!pending.isEmpty();count++)tickMain(pending.removeFirst());
  pending.clear();processing=false;
 }
 private boolean same(Access world,int state){return lava?world.lavaMaterial(state):world.waterMaterial(state);}
 private int depth(Access world,int x,int y,int z){int state=world.pairedBlocks(x,y,z).effective();return same(world,state)?state>>>16:-1;}
 private int delay(Access world){return lava?world.ultraWarm()?10:30:5;}
 private boolean inside(Access world,int x,int z){int count=world.finiteChunkCount(),half=count/2,cx=(x>>4)+half,cz=(z>>4)+half;return cx>=0&&cx<count&&cz>=0&&cz<count;}

 /** 0x222290: target/source face checks, with the original early probes. */
 private boolean blocked(Access w,int x,int y,int z,int fromX,int fromY,int fromZ,int direction){
  int target=w.block(x,y,z);if(w.blocksFluid(target))return true;
  if(!w.canPassFluidFace(x,y,z,target,direction))return true;
  int source=w.block(fromX,fromY,fromZ);return !w.canPassFluidFace(fromX,fromY,fromZ,source,direction^1);
 }
 private void makeStill(Access w,int x,int y,int z,boolean secondary){
  int effective=w.pairedBlocks(x,y,z).effective(),id=effective&65535;
  int still=id==8?9:id==10?11:id;int state=still|(effective&0xffff0000);
  id=(secondary?w.secondaryBlock(x,y,z):w.block(x,y,z))&65535;
  if(id==10)secondary=false;else if(id!=8)return;
  if(secondary)w.setSecondary(x,y,z,state,18);else w.setBlock(x,y,z,state,18);
 }

 private void tickMain(Pending event){
  Access w=event.world;Random random=event.random;int x=event.x,y=event.y,z=event.z;
  int current=w.block(x,y,z);boolean currentSame=same(w,current);int level=depth(w,x,y,z),step=lava&&!w.hotFlowStep()?2:1;
  if(lava)Ps4LavaFireTick.tick(w,random,x,y,z);int tickDelay=delay(w);
  if(level>0){
   int minimum=-100,sources=0;
   for(int i=0;i<4;i++){int d=horizontal.get(i),next=depth(w,x+Ps4DirectionPlane.dx(d),y,z+Ps4DirectionPlane.dz(d));if(next>=0){if(next==0)sources++;if(next>=8)next=0;if(minimum<0||next<minimum)minimum=next;}}
   int next=minimum+step;if(minimum<0||next>=8)next=-1;
   int above=depth(w,x,y+1,z);if(above>=0)next=above>=8?above:above+8;
   if(sources==1&&depth(w,x,y+1,z)==0)sources++;
   if(sources>1&&!lava){int below=w.block(x,y-1,z);if(w.solid(below)||same(w,below)&&(below>>>16)==0)next=0;}
   int scheduled=tickDelay;
   if(lava&&level<next&&level<8&&next<8&&random.nextInt(4)!=0)scheduled=tickDelay*4;
   if(next==level)makeStill(w,x,y,z,!currentSame);
   else if(next<0){if(currentSame)w.removePrimary(x,y,z);else w.setSecondary(x,y,z,0,3);}
   else{
    int state=(event.state&65535)|(next<<16);if(currentSame||lava)w.setBlock(x,y,z,state,2);else w.setSecondary(x,y,z,state,2);
    w.scheduleFluid(x,y,z,tile,scheduled);w.notifyFluidNeighbors(x,y,z,tile,0);
   }
   level=next;
  }else makeStill(w,x,y,z,!currentSame);
  boolean down=false;
  if(inside(w,x,z)){int below=w.pairedBlocks(x,y-1,z).effective();down=!same(w,below)&&!w.lavaMaterial(below)&&!blocked(w,x,y-1,z,x,y,z,1);}
  if(down){
   if(lava&&!w.skipLavaWaterReaction()){
    int below=w.block(x,y-1,z);if(w.lavaStoneContactMaterial(below)){w.setBlock(x,y-1,z,w.defaultBlockState(1),3);w.fizz(x,y-1,z);return;}
   }
   flowTo(w,x,y-1,z,x,y,z,1,level>=8?level:level+8);return;
  }
  if(level<0||level!=0&&!blocked(w,x,y-1,z,x,y,z,1))return;
  boolean[] directions=flowDirections(w,x,y,z);int next=level>=8?1:level+step;if(next>=8)return;
  for(int i=0;i<4;i++)if(directions[i])flowTo(w,x+FLOW[i][0],y,z+FLOW[i][1],x,y,z,FLOW[i][2],next);
 }

 /** 0x2223e0 and recursive 0x222600. */
 private boolean[] flowDirections(Access w,int x,int y,int z){
  int[] costs={1000,1000,1000,1000};int minimum=1000;
  for(int i=0;i<4;i++){
   int nx=x+FLOW[i][0],nz=z+FLOW[i][1];
   if(!blocked(w,nx,y,nz,x,y,z,FLOW[i][2])){
    int meta=w.block(nx,y,nz)>>>16;int target=w.block(nx,y,nz);
    if(meta!=0||!same(w,target))costs[i]=blocked(w,nx,y-1,nz,x,y,z,1)?slopeDistance(w,nx,y,nz,1,i):0;
   }
   if(costs[i]<minimum)minimum=costs[i];
  }
  boolean[] result=new boolean[4];for(int i=0;i<4;i++)result[i]=costs[i]==minimum;return result;
 }
 private int slopeDistance(Access w,int x,int y,int z,int distance,int entered){
  int minimum=1000;
  for(int i=0;i<4;i++){
   if(i==(entered^1))continue;int nx=x+FLOW[i][0],nz=z+FLOW[i][1];
   if(blocked(w,nx,y,nz,x,y,z,FLOW[i][2]))continue;
   int target=w.block(nx,y,nz);if(same(w,target)&&(target>>>16)==0)continue;
   if(!blocked(w,nx,y-1,nz,x,y,z,1))return distance;
   if(distance<(lava?2:4))minimum=Math.min(minimum,slopeDistance(w,nx,y,nz,distance+1,i));
  }
  return minimum;
 }

 /** 0x222050 and the final 0x41c930 layer selection. */
 private void flowTo(Access w,int x,int y,int z,int fromX,int fromY,int fromZ,int direction,int level){
  if(!inside(w,x,z))return;int effective=w.pairedBlocks(x,y,z).effective();if(same(w,effective)||w.lavaMaterial(effective)||blocked(w,x,y,z,fromX,fromY,fromZ,direction))return;
  int target=w.block(x,y,z);boolean secondary=false;
  if(!w.airMaterial(target)){
   if(lava)w.fizz(x,y,z);
   else if(w.dropsInWater(target)){
    int state=w.block(x,y,z);w.levelEvent(2001,x,y,z,(state&65535)+((state>>>16)<<12));w.dropBlock(x,y,z,w.block(x,y,z),0);
   }else secondary=true;
  }
  int state=tile|(level<<16);if(secondary)w.setSecondary(x,y,z,state,3);else w.setBlock(x,y,z,state,3);
  w.scheduleFluid(x,y,z,tile,delay(w));
 }
}

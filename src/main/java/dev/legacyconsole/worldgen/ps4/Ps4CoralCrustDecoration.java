package dev.legacyconsole.worldgen.ps4;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Original crust marker decoration and scatters (0x1c8940..0x1c95e0).
 * Marker order comes from the original template marker collector. */
public final class Ps4CoralCrustDecoration {
 public interface Access extends Ps4CoralFeatures.Access {
  /** Feature::setBlock result, including rejected/unchanged world writes. */
  boolean placeCoralBlock(int x,int y,int z,int state,int flags);
 }
 public record Marker(int x,int y,int z){}
 static final class Candidates {
  final List<Ps4CoralShapes.Pos> tops=new ArrayList<>();
  final List<Ps4CoralShapes.Side> sides=new ArrayList<>();
 }
 private Ps4CoralCrustDecoration(){}
 private static boolean water(Access w,int x,int y,int z){return w.waterMaterial(w.block(x,y,z));}
 private static boolean blocking(Access w,int x,int y,int z){return w.solidBlocking(w.block(x,y,z));}
 private static int facing(int d){return d==2?2:d==5?1:d==3?3:0;}

 /** 0x1c8940. The color draw occurs even for an empty marker collection. */
 public static void decorate(Access w,Random random,List<Marker> markers){
  Candidates candidates=new Candidates();int color=random.nextInt(5);
  for(Marker marker:markers){int x=marker.x,y=marker.y,z=marker.z;if(blocking(w,x,y-1,z)){if(blocking(w,x,y,z))y++;scatter(w,random,x,y,z,color,candidates);}}
  for(Ps4CoralShapes.Pos p:candidates.tops)fan(w,random,p.x(),p.y(),p.z());
  for(Ps4CoralShapes.Side side:candidates.sides){Ps4CoralShapes.Pos p=side.position();sideFan(w,random,p.x(),p.y(),p.z(),side.direction());}
 }

 /** 0x1c8bd0. Native failed-write branches and duplicate candidates remain. */
 static void scatter(Access w,Random random,int x,int y,int z,int color,Candidates candidates){
  int state=259|(color<<16);
  if(random.nextInt(4)==0){
   if(!w.canPlace(259,x,y,z))return;
   for(int d:new int[]{5,2,4,3}){int nx=x+Ps4DirectionPlane.dx(d),nz=z+Ps4DirectionPlane.dz(d);if((w.block(nx,y,nz)&65535)!=259)w.placeCoralBlock(nx,y,nz,state,2);}
   w.placeCoralBlock(x,y,z,state,2);
   if(w.placeCoralBlock(x,y+1,z,state,2)&&random.nextInt(4)==0)candidates.tops.add(new Ps4CoralShapes.Pos(x,y+2,z));
   return;
  }
  int length=1+random.nextInt(3);if(!water(w,x,y,z))return;
  w.placeCoralBlock(x,y,z,state,2);int axis=random.nextInt(2);
  for(int i=0;i<length;i++){
   boolean negative=random.nextBoolean();int d=axis==0?negative?4:5:negative?3:2;
   x+=Ps4DirectionPlane.dx(d);z+=Ps4DirectionPlane.dz(d);
   if(!w.canPlace(259,x,y,z)||(w.block(x,y,z)&65535)==259||!w.placeCoralBlock(x,y,z,state,2))break;
   Ps4CoralShapes.Pos p=new Ps4CoralShapes.Pos(x,y,z);
   if(random.nextInt(4)==0)candidates.tops.add(p.up(1));
   if(random.nextBoolean())candidates.sides.add(new Ps4CoralShapes.Side(p,d^1));
   if(random.nextBoolean())candidates.sides.add(new Ps4CoralShapes.Side(p,d^1));
   if(random.nextInt(3)==0)candidates.sides.add(new Ps4CoralShapes.Side(p,d^1));
   axis^=1;
  }
 }

 /** 0x1c9430. Unlike the main coral helper, no extra current-state probe. */
 static void fan(Access w,Random random,int x,int y,int z){
  if(!water(w,x,y,z)||water(w,x,y-1,z)||!Ps4CoralFeatures.canStayPlant(w,x,y,z))return;
  int type=random.nextInt(5),orientation=random.nextInt(2);w.placeCoralBlock(x,y,z,264|((type|(orientation<<3))<<16),2);
 }

 /** 0x1c95e0/0x1c9790/0x1c9860. The original numeric placement request is
  * coral block 259, followed by a wall-fan state write. */
 static void sideFan(Access w,Random random,int x,int y,int z,int excluded){
  int id=w.block(x,y,z)&65535,d=Ps4CoralFeatures.otherDirection(random,excluded);
  int nx=x+Ps4DirectionPlane.dx(d),nz=z+Ps4DirectionPlane.dz(d);boolean wet=water(w,nx,y,nz);
  if(id!=259&&id!=1||!wet||!w.canPlace(259,nx,y,nz))return;
  int type=random.nextInt(2),group=random.nextInt(3);int state=(266+group)|(((group==2?0:type*2)+facing(d)*4)<<16);w.placeCoralBlock(nx,y,nz,state,2);
 }
}

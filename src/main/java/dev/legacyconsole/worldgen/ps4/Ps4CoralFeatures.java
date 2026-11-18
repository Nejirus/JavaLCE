package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** Translated mcps4 coral placement routines. Canonical metadata keeps the
 * original coral type, orientation and dead-property layouts. */
public final class Ps4CoralFeatures {
 public interface Access extends Ps4AquaticFeatures.Access {
  Ps4DirectionPlane horizontalDirections();
  boolean supportsFace(int state,int direction,int kind);
  boolean canPlaceState(int state,int x,int y,int z);
  boolean hasChunk(int chunkX,int chunkZ);
 }
 private Ps4CoralFeatures(){}
 private static boolean water(Access w,int x,int y,int z){return w.waterMaterial(w.block(x,y,z));}
 private static boolean blocking(Access w,int x,int y,int z){return w.solidBlocking(w.block(x,y,z));}
 private static boolean loaded(Access w,int x,int y,int z){return y>=0&&y<256&&w.hasChunk(x>>4,z>>4);}
 private static int facing(int direction){return direction==2?2:direction==5?1:direction==3?3:0;}
 private static boolean halfSlab(int id){return id==44||id==126||id==182||id==205||id==312;}

 /** CoralFeature::place, 0x1cc130, including its complete shape dispatcher
  * and ordered floor/side-fan follow-up passes. */
 public static boolean coral(Access w,Random random,int x,int y,int z){
  if(w.clientSide())return true;
  while(y<59){if(water(w,x,y,z))break;y++;}
  if(y>=59||!blocking(w,x,y-1,z)||!water(w,x,y,z))return false;
  int below=w.block(x,y-1,z);if((below&65535)==259&&(below>>>16&8)==0)return false;
  Ps4CoralShapes shape=new Ps4CoralShapes(w,random,259|(random.nextInt(5)<<16));
  shape.dispatch(new Ps4CoralShapes.Pos(x,y,z));
  for(Ps4CoralShapes.Pos p:shape.tops)fan(w,random,p.x(),p.y(),p.z());
  for(Ps4CoralShapes.Side side:shape.sides){Ps4CoralShapes.Pos p=side.position();sideFan(w,random,p.x(),p.y(),p.z(),side.direction());}
  return true;
 }

 /** Coral::canStay/validSoil, 0x1c6420/0x1c65c0. */
 public static boolean canStayPlant(Access w,int x,int y,int z){
  int below=w.block(x,y-1,z);
  if(!w.canPlace(263,x,y,z)||w.airMaterial(w.block(x,y+1,z)))return false;
  if(w.waterMaterial(w.effectiveBlock(x,y-1,z))){int level=w.effectiveBlock(x,y-1,z)>>>16;if(level>=1&&level<=7)return false;}
  boolean top=w.slab(below)&&(below>>>16&8)!=0;
  // The support callback is evaluated even when the slab already supports it.
  return w.supportsFace(below,1,0)|top;
 }

 /** CoralFan::canStay/validSoil, 0x1390c0/0x1c9e90. */
 public static boolean canStayFan(Access w,int x,int y,int z){
  w.block(x,y-1,z);int below=w.block(x,y-1,z);
  if(w.supportsFace(below,1,0))return true;
  return halfSlab(below&65535)&&(below>>>16&8)!=0;
 }

 /** SeaAnemoneFeature::place, 0x5f4f30. */
 public static boolean anemone(Access w,Random random,int x,int y,int z){
  if(w.clientSide())return true;
  while(!blocking(w,x,y-1,z)){if(y<49)break;y--;}
  if(!water(w,x,y,z)||!blocking(w,x,y-1,z)||y<=48||!w.canPlace(259,x,y,z)||!loaded(w,x,y,z))return false;
  int state;
  if(random.nextInt(100)<75)state=263|(random.nextInt(5)<<16);
  else{int type=random.nextInt(5),orientation=random.nextInt(2);state=264|((type|(orientation<<3))<<16);}
  w.setBlock(x,y,z,state,2);return true;
 }

 /** CoralHangFeature::place, 0x1d1a50. Failed water candidates swap entries
  * in the shared cached array, including the final candidate. */
 public static boolean hanging(Access w,Random random,int x,int y,int z){
  if(w.clientSide())return true;
  if(!blocking(w,x,y,z)&&y>50){do{y--;boolean solid=blocking(w,x,y,z);if(y<51||solid)break;}while(true);}
  if(!blocking(w,x,y,z))return false;
  int id=w.block(x,y,z)&65535;if(id!=259&&id!=1)return false;
  Ps4DirectionPlane plane=w.horizontalDirections();int direction=2;
  for(int remaining=4;remaining>0;remaining--){
   int index=random.nextInt(remaining),chosen=plane.get(index);
   if(water(w,x+Ps4DirectionPlane.dx(chosen),y,z+Ps4DirectionPlane.dz(chosen))){direction=chosen;break;}
   plane.swap(index,remaining-1);
  }
  int wx=x+Ps4DirectionPlane.dx(direction),wz=z+Ps4DirectionPlane.dz(direction);
  if(!water(w,wx,y,wz)||!w.canPlace(266,wx,y,wz))return false;
  int type=random.nextInt(2),dead=random.nextBoolean()?1:0;
  int state=266|((type*2+facing(direction)*4+dead)<<16);
  if(loaded(w,wx,y,wz))w.setBlock(wx,y,wz,state,2);
  return true;
 }

 /** CoralFeature top-fan helper, 0x1cc550. */
 public static void fan(Access w,Random random,int x,int y,int z){
  if(!water(w,x,y,z)||water(w,x,y-1,z))return;
  w.block(x,y,z);
  if(!canStayPlant(w,x,y,z))return;
  int type=random.nextInt(5),orientation=random.nextInt(2);
  w.setBlock(x,y,z,264|((type|(orientation<<3))<<16),2);
 }

 /** CoralFeature::randomOtherDirection, 0x1cf0f0. This uses separate immutable
  * cached three-direction arrays, not Direction::Plane::HORIZONTAL. */
 public static int otherDirection(Random random,int direction){
  int index=random.nextInt(3);
  if(direction==2)return new int[]{5,3,4}[index];
  if(direction==3)return new int[]{5,2,4}[index];
  if(direction==5)return new int[]{2,3,4}[index];
  return new int[]{5,3,2}[index];
 }

 /** CoralFeature side-fan helper/state selection, 0x1cc720/0x1cf2c0. */
 public static void sideFan(Access w,Random random,int x,int y,int z,int excluded){
  int id=w.block(x,y,z)&65535,direction=otherDirection(random,excluded);
  int wx=x+Ps4DirectionPlane.dx(direction),wz=z+Ps4DirectionPlane.dz(direction);
  boolean wet=water(w,wx,y,wz);
  if(id!=259&&id!=1||!wet||!w.canPlaceState(266|(8<<16),wx,y,wz))return;
  int type=random.nextInt(2),group=random.nextInt(3);
  int state=(266+group)|(((group==2?0:type*2)+facing(direction)*4)<<16);
  w.setBlock(wx,y,wz,state,2);
 }
}

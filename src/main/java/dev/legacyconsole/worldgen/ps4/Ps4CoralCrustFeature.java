package dev.legacyconsole.worldgen.ps4;

import java.util.ArrayList;
import java.util.List;

/** CoralCrustFeature dispatch/offsets (mcps4 0x1c7650/0x1c84b0/0x1c8650).
 * Template geometry and marker postprocessing are explicit shared-world calls. */
public final class Ps4CoralCrustFeature {
 public interface Access extends Ps4CoralFeatures.Access {
  void placeCoralTemplate(int template,int x,int y,int z,int rotation,float integrity,int flags);
  void decorateCoralTemplate(int template,int x,int y,int z,int rotation,Ps4Random random);
 }
 private record Candidate(int variant,int rotation,Ps4CoralShapes.Pos position){}
 private static final int[][][] CRUST={
  {{6,0,6},{-7,0,6},{-7,0,-7},{6,0,-7}},
  {{6,0,6},{-6,0,6},{-7,0,-7},{6,0,-7}},
  {{4,0,4},{-4,0,4},{-4,0,-4},{4,0,-4}},
  {{4,0,4},{-4,0,4},{-4,0,-4},{4,0,-4}},
  {{3,0,6},{-7,0,3},{-2,0,-7},{7,0,-3}}
 };
 private static final int[][][] OUTCROP={
  {{-5,0,-2},{3,0,-5},{5,0,2},{-2,0,5}},
  {{-7,-2,-3},{3,-2,-6},{6,-2,3},{-2,-2,5}},
  {{-4,0,-3},{3,0,-4},{4,0,3},{-3,0,4}},
  {{-3,0,-3},{3,0,-3},{3,0,3},{-3,0,3}},
  {{-2,0,-2},{2,0,-2},{2,0,2},{-2,0,2}},
  {{-5,0,-5},{6,0,-6},{5,0,5},{-5,0,5}}
 };
 private Ps4CoralCrustFeature(){}
 static int[] offset(boolean outcropping,int index,int rotation){return (outcropping?OUTCROP:CRUST)[index][rotation].clone();}
 private static Ps4CoralShapes.Pos add(Ps4CoralShapes.Pos p,int[] d){return p.offset(d[0],d[1],d[2]);}
 private static Ps4CoralShapes.Pos subtract(Ps4CoralShapes.Pos p,int[] d){return p.offset(-d[0],-d[1],-d[2]);}
 private static boolean blocking(Access w,Ps4CoralShapes.Pos p){return w.solidBlocking(w.block(p.x(),p.y(),p.z()));}
 private static boolean water(Access w,Ps4CoralShapes.Pos p){return w.waterMaterial(w.block(p.x(),p.y(),p.z()));}
 private static void place(Access w,Ps4Random random,int id,Ps4CoralShapes.Pos p,int rotation){w.placeCoralTemplate(id,p.x(),p.y(),p.z(),rotation,1f,2);w.decorateCoralTemplate(id,p.x(),p.y(),p.z(),rotation,random);}

 /** Rotation order comes from the original registry context. */
 public static boolean place(Access w,Ps4Random random,int x,int y,int z,int[] rotationOrder){
  if(w.clientSide())return true;
  int variant=random.nextInt(5),rotation=rotationOrder[random.peekInt(4)];
  Ps4CoralShapes.Pos p=add(new Ps4CoralShapes.Pos(x,y,z),CRUST[variant][rotation]);
  while(p.y()<54){if(water(w,p))break;p=p.up(1);}
  if(p.y()>=54||!blocking(w,p.up(-1))||!water(w,p))return false;
  int below=w.block(p.x(),p.y()-1,p.z());if((below&65535)==259&&(below>>>16&8)==0)return false;
  List<Candidate> candidates=new ArrayList<>();candidates.add(new Candidate(variant,rotation,subtract(p,CRUST[variant][rotation])));
  if(random.nextInt(4)==0){int index=random.nextInt(6);place(w,random,182+index,add(p,OUTCROP[index][rotation]),rotation);return true;}
  int count=4+random.nextInt(5);
  for(int i=0;i<count;i++){
   int nextVariant=random.nextInt(5),nextRotation=rotationOrder[random.peekInt(4)];
   int dx=3+random.nextInt(9);if(random.nextBoolean())dx=-dx;
   int dz=3+random.nextInt(9);if(random.nextBoolean())dz=-dz;
   Ps4CoralShapes.Pos shifted=p.offset(dx,0,dz),sample=add(shifted,CRUST[nextVariant][nextRotation]);
   if(blocking(w,sample.up(-1)))candidates.add(new Candidate(nextVariant,nextRotation,shifted));
  }
  if(candidates.size()==1){int index=random.nextInt(6);place(w,random,182+index,add(p,OUTCROP[index][rotation]),rotation);}
  else for(Candidate candidate:candidates)place(w,random,177+candidate.variant,candidate.position,candidate.rotation);
  return true;
 }
}

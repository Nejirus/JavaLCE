package dev.legacyconsole.worldgen.ps4;

import java.util.BitSet;
import java.util.Objects;
import java.util.Random;
import java.util.function.IntPredicate;

/** OreFeature::place, mcps4 0x503f60. States are legacy tile IDs with metadata
 * in bits 16..31. This is the recovered feature kernel; the population caller
 * must supply the original shared RNG and generation order. */
public final class Ps4OreFeature {
 public interface Access {
  int block(int x,int y,int z);
  void setBlock(int x,int y,int z,int state,int flags);
  int height(int x,int z);
  int seaLevel();
  default boolean hasGenerationRules(){return false;}
  default boolean intersectsGenerationRule(int x0,int y0,int z0,int x1,int y1,int z1){return false;}
 }

 private final int state,size;
 private final IntPredicate replacement;

 public Ps4OreFeature(int state,int size){this(state,size,Ps4OreFeature::isNaturalStone);}

 /** 0x504a30 and StoneType initializer 0x67f1f0: ordinary stone and the
  * unpolished granite/diorite/andesite variants permit ore replacement. */
 public static boolean isNaturalStone(int state){
  int meta=state>>>16;
  return (state&65535)==1&&(meta==0||meta==1||meta==3||meta==5);
 }

 public Ps4OreFeature(int state,int size,IntPredicate replacement){
  if(size<=0)throw new IllegalArgumentException("PS4 ore vein size must be positive");
  this.state=state;this.size=size;this.replacement=Objects.requireNonNull(replacement);
 }

 public boolean place(Access world,Random random,int x,int y,int z){
  float angle=random.nextFloat()*(float)Math.PI;
  double x0=(float)(x+8)+Ps4Math.sin(angle)*(float)size*.125f;
  double x1=(float)(x+8)-Ps4Math.sin(angle)*(float)size*.125f;
  double z0=(float)(z+8)+Ps4Math.cos(angle)*(float)size*.125f;
  double z1=(float)(z+8)-Ps4Math.cos(angle)*(float)size*.125f;
  double y0=y-2+random.nextInt(3),y1=y-2+random.nextInt(3);
  double margin=((double)size*.0625*2+1)*.5;
  int baseX=floor(Math.min(x0,x1)-margin);
  int baseY=floor(Math.min(y0,y1)-margin);
  int baseZ=floor(Math.min(z0,z1)-margin);
  int maxX=floor(Math.max(x0,x1)+margin);
  int maxY=floor(Math.max(y0,y1)+margin);
  int maxZ=floor(Math.max(z0,z1)+margin);
  int height=maxY-baseY+1,depth=maxZ-baseZ+1;
  BitSet visited=new BitSet((maxX-baseX+1)*height*depth);
  boolean checkRules=world.hasGenerationRules()&&world.intersectsGenerationRule(baseX,baseY,baseZ,maxX,maxY,maxZ);
  int seaLevel=world.seaLevel();
  // The original loop includes size, and rounds the interpolation fraction
  // to float before using it in the double interpolation.
  for(int step=0;step<=size;step++){
   float fraction=(float)step/(float)size;
   double cx=(double)fraction*(x1-x0)+x0;
   double cy=(double)fraction*(y1-y0)+y0;
   double cz=(double)fraction*(z1-z0)+z0;
   double spread=random.nextDouble()*(double)size*.0625;
   double radius=((double)(Ps4Math.sin(fraction*(float)Math.PI)+1f)*spread+1)*.5;
   int minX=floor(cx-radius),minY=floor(cy-radius),minZ=floor(cz-radius);
   int endX=floor(cx+radius),endY=floor(cy+radius),endZ=floor(cz+radius);
   if(checkRules&&world.intersectsGenerationRule(minX,minY,minZ,endX,endY,endZ))continue;
   // This optimization uses the first endpoint's Y, not this sphere's center.
   if((double)seaLevel<y0&&world.height(minX,minZ)<minY&&world.height(endX,minZ)<minY
      &&world.height(minX,endZ)<minY&&world.height(endX,endZ)<minY)continue;
   int radiusSquared=(int)(radius*radius*65536.);
   int qx=(int)(((double)minX+.5-cx)*256.);
   int initialQy=(int)(((double)minY+.5-cy)*256.);
   int initialQz=(int)(((double)minZ+.5-cz)*256.);
   for(int bx=minX;bx<=endX;bx++,qx+=256){
    if(qx*qx>=radiusSquared)continue;
    int qy=initialQy;
    for(int by=minY;by<=endY;by++,qy+=256){
     int xy=qx*qx+qy*qy;
     if(xy>=radiusSquared)continue;
     int index=(minZ-baseZ)+((bx-baseX)*height+by-baseY)*depth;
     int qz=initialQz;
     for(int bz=minZ;bz<=endZ;bz++,qz+=256){
      if(qz*qz+xy<radiusSquared){
       // 0x504885 jumps past the bit-index increment when already visited.
       // Retain that branch rather than substituting a normal voxel bitset.
       if(visited.get(index))continue;
       visited.set(index);
       int old=world.block(bx,by,bz);
       if((old&65535)!=0&&replacement.test(old))world.setBlock(bx,by,bz,state,0x74);
      }
      index++;
     }
    }
   }
  }
  return true;
 }

 private static int floor(double value){return (int)Math.floor(value);}
}

package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** FossilFeature::place, mcps4 0x2f6ef0. Template geometry is supplied through
 * Access; this dispatcher never consumes the population RNG. */
public final class Ps4FossilFeature {
 public record Size(int x,int y,int z){}
 public record Clip(int minX,int minY,int minZ,int maxX,int maxY,int maxZ){}
 public interface Access {
  Random chunkRandom(int chunkX,int chunkZ,long salt);
  /** Ordinal in the original rotation registry at the selected index. */
  int rotation(int index);
  Size templateSize(int template);
  int surfaceHeight(int x,int z);
  void placeTemplate(int template,int x,int y,int z,int rotation,Clip clip,float integrity,Random random,int flags);
 }
 private static final int[] BONES={3,4,5,6,11,12,13,14},COAL={7,8,9,10,15,16,17,18};
 private Ps4FossilFeature(){}
 public static boolean place(Access world,int x,int y,int z){
  Random random=world.chunkRandom(x>>4,z>>4,987234911L);
  int rotation=world.rotation(random.nextInt(4)),index=random.nextInt(8),bone=BONES[index],coal=COAL[index];
  Size size=world.templateSize(bone);world.templateSize(coal);
  int width=rotation==1||rotation==3?size.z:size.x,depth=rotation==1||rotation==3?size.x:size.z;
  int offsetX=random.nextInt(16-width),offsetZ=random.nextInt(16-depth),minimum=256;
  // The original inner loop uses width again, even for rectangular templates.
  for(int dx=0;dx<width;dx++)for(int dz=0;dz<width;dz++)minimum=Math.min(minimum,world.surfaceHeight(x+offsetX+dx,z+offsetZ+dz));
  int wy=Math.max(minimum-15-random.nextInt(10),10)+y,wx=x+offsetX,wz=z+offsetZ;
  switch(rotation){case 1->wx+=size.z-1;case 2->{wx+=size.x-1;wz+=size.z-1;}case 3->wz+=size.x-1;default->{}}
  int chunkX=x>>4,chunkZ=z>>4;Clip clip=new Clip(chunkX*16,0,chunkZ*16,chunkX*16+15,256,chunkZ*16+15);
  world.placeTemplate(bone,wx,wy,wz,rotation,clip,0.9f,random,20);world.placeTemplate(coal,wx,wy,wz,rotation,clip,0.1f,random,20);
  return true;
 }
}

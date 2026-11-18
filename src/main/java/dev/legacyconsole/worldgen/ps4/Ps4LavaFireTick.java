package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** LiquidBlock lava ignition helper, mcps4 0x41c980. */
public final class Ps4LavaFireTick {
 public interface Access extends Ps4PlantRules.Access {
  boolean fireTickEnabled();
  int defaultBlockState(int tile);
  boolean flammableMaterial(int state);
  boolean hasChunk(int chunkX,int chunkZ,boolean allowEmpty);
 }
 private static final int[][] DIRECTIONS={{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
 private Ps4LavaFireTick(){}
 public static void tick(Access world,Random random,int x,int y,int z){
  if(!world.fireTickEnabled())return;
  int above=world.block(x,y+1,z)&65535;
  if(!world.airMaterial(world.defaultBlockState(above))&&!world.flammableMaterial(world.defaultBlockState(above)))return;
  for(int attempt=0;attempt<10;attempt++){
   int wx=x+random.nextInt(3)-1,wy=y+random.nextInt(3)-1,wz=z+random.nextInt(3)-1;
   // Original unsigned comparison: negative and Y>=256 skip this chunk check.
   if(Integer.compareUnsigned(wy,256)<0&&!world.hasChunk(wx>>4,wz>>4,true))return;
   if(world.flammableMaterial(world.defaultBlockState(world.block(wx,wy,wz)&65535))){
    for(int[] d:DIRECTIONS){
     int nx=wx+d[0],ny=wy+d[1],nz=wz+d[2];
     if(world.airMaterial(world.defaultBlockState(world.block(nx,ny,nz)&65535))){world.setBlock(nx,ny,nz,world.defaultBlockState(51),11);return;}
    }
   }
  }
 }
}

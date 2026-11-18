package dev.legacyconsole.worldgen.ps4;

/** DesertWellFeature constructor/placement, mcps4 0x1fb400/0x1fa530.
 * Placement consumes no random values. */
public final class Ps4DesertWellFeature {
 private static final int SANDSTONE=24,SLAB=44|(1<<16),FLOWING_WATER=8;
 private Ps4DesertWellFeature(){}
 public static boolean place(Ps4PlantRules.Access world,int x,int y,int z){
  return place(world,new Ps4DirectionPlane(),x,y,z);
 }
 public static boolean place(Ps4PlantRules.Access world,Ps4DirectionPlane directions,int x,int y,int z){
  if((world.block(x,y,z)&65535)==0&&y>2){do{y--;if((world.block(x,y,z)&65535)!=0)break;}while(y>2);}
  // The original BlockStatePredicate requires ordinary sand, excluding red.
  if(world.block(x,y,z)!=12)return false;
  for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)if((world.block(x+dx,y-1,z+dz)&65535)==0&&(world.block(x+dx,y-2,z+dz)&65535)==0)return false;
  for(int dy=-1;dy<=0;dy++)for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)world.setBlock(x+dx,y+dy,z+dz,SANDSTONE,2);
  world.setBlock(x,y,z,FLOWING_WATER,2);
  for(int i=0;i<4;i++){int d=directions.get(i);world.setBlock(x+Ps4DirectionPlane.dx(d),y,z+Ps4DirectionPlane.dz(d),FLOWING_WATER,2);}
  for(int dx=-2;dx<=2;dx++){
   if(dx==-2||dx==2){for(int dz=-2;dz<=2;dz++)world.setBlock(x+dx,y+1,z+dz,SANDSTONE,2);}
   else{world.setBlock(x+dx,y+1,z-2,SANDSTONE,2);world.setBlock(x+dx,y+1,z+2,SANDSTONE,2);}
  }
  world.setBlock(x+2,y+1,z,SLAB,2);world.setBlock(x-2,y+1,z,SLAB,2);world.setBlock(x,y+1,z+2,SLAB,2);world.setBlock(x,y+1,z-2,SLAB,2);
  for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)world.setBlock(x+dx,y+4,z+dz,dx==0&&dz==0?SANDSTONE:SLAB,2);
  for(int dy=1;dy<=3;dy++){
   world.setBlock(x-1,y+dy,z-1,SANDSTONE,2);world.setBlock(x-1,y+dy,z+1,SANDSTONE,2);world.setBlock(x+1,y+dy,z-1,SANDSTONE,2);world.setBlock(x+1,y+dy,z+1,SANDSTONE,2);
  }
  return true;
 }
}

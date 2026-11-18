package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** MonsterRoomFeature::place (0x497e40), chest facing (0x1746f0), and mob
 * selection (0x498a40). Legacy states retain metadata in bits 16..31. */
public final class Ps4DungeonFeature {
 public static final String LOOT_TABLE="minecraft:chests/simple_dungeon";
 public enum Entity {CHEST,SPAWNER,OTHER}
 public enum Mob {
  SKELETON(0x5d540441,"skeleton"),ZOMBIE(0x5d550040,"zombie"),SPIDER(0x5d560040,"spider");
  public final int ps4Type;public final String javaName;
  Mob(int ps4Type,String javaName){this.ps4Type=ps4Type;this.javaName=javaName;}
 }
 public interface Access extends Ps4OreFeature.Access {
  boolean solid(int state);
  boolean solidRender(int state);
  void removeBlock(int x,int y,int z);
  Entity entity(int x,int y,int z);
  void chestLoot(int x,int y,int z,String table,long seed);
  void spawnerMob(int x,int y,int z,Mob mob);
 }
 // Direction::Plane::HORIZONTAL (0x200f70): north, east, south, west.
 private static final int[] HORIZONTAL={2,5,3,4};
 private Ps4DungeonFeature(){}

 public static boolean place(Access world,Random random,int x,int y,int z){
  int rx=random.nextInt(2)+2,rz=random.nextInt(2)+2,holes=0;
  for(int dx=-rx-1;dx<=rx+1;dx++)for(int dy=-1;dy<=4;dy++)for(int dz=-rz-1;dz<=rz+1;dz++){
   int wx=x+dx,wy=y+dy,wz=z+dz;
   boolean solid=world.solid(world.block(wx,wy,wz));
   if((dy==-1||dy==4)&&!solid)return false;
   if((dx==-rx-1||dx==rx+1||dz==-rz-1||dz==rz+1)&&dy==0
      &&empty(world,wx,wy,wz)&&empty(world,wx,wy+1,wz))holes++;
  }
  if(holes<1||holes>5)return false;
  for(int dx=-rx-1;dx<=rx+1;dx++)for(int dy=3;dy>=-1;dy--)for(int dz=-rz-1;dz<=rz+1;dz++){
   int wx=x+dx,wy=y+dy,wz=z+dz;
   if(dx==-rx-1||dx==rx+1||dz==-rz-1||dz==rz+1||dy==-1){
    if(wy>=0&&!world.solid(world.block(wx,wy-1,wz)))world.removeBlock(wx,wy,wz);
    else if(world.solid(world.block(wx,wy,wz))&&(world.block(wx,wy,wz)&65535)!=54)
     world.setBlock(wx,wy,wz,dy==-1&&random.nextInt(4)!=0?48:4,2);
   }else if((world.block(wx,wy,wz)&65535)!=54)world.removeBlock(wx,wy,wz);
  }
  for(int chest=0;chest<2;chest++)for(int attempt=0;attempt<3;attempt++){
   int wx=x+random.nextInt(rx*2+1)-rx,wz=z+random.nextInt(rz*2+1)-rz;
   if(!empty(world,wx,y,wz))continue;
   int walls=0;
   for(int direction:HORIZONTAL)if(world.solid(world.block(wx+dx(direction),y,wz+dz(direction))))walls++;
   if(walls!=1)continue;
   int state=chestState(world,wx,y,wz,2);
   world.setBlock(wx,y,wz,state,2);
   if(world.entity(wx,y,wz)==Entity.CHEST)world.chestLoot(wx,y,wz,LOOT_TABLE,random.nextLong());
   break;
  }
  world.setBlock(x,y,z,52,2);
  if(world.entity(x,y,z)==Entity.SPAWNER)world.spawnerMob(x,y,z,pickMob(random));
  return true;
 }

 public static Mob pickMob(Random random){return switch(random.nextInt(4)){case 0->Mob.SKELETON;case 3->Mob.SPIDER;default->Mob.ZOMBIE;};}

 /** ChestBlock::recheckFacing, 0x1746f0. Solid-render checks are distinct
  * from the material-solid checks used to select a chest location. */
 public static int chestState(Access world,int x,int y,int z,int facing){
  int wall=-1;
  for(int direction:HORIZONTAL){
   int old=world.block(x+dx(direction),y,z+dz(direction));
   if((old&65535)==54)return 54|(facing<<16);
   if(world.solidRender(old)){if(wall!=-1){wall=-1;break;}wall=direction;}
  }
  if(wall!=-1)return 54|(opposite(wall)<<16);
  if(opaque(world,x,y,z,facing))facing=opposite(facing);
  if(opaque(world,x,y,z,facing))facing=clockwise(facing);
  if(opaque(world,x,y,z,facing))facing=opposite(facing);
  return 54|(facing<<16);
 }
 private static boolean empty(Access world,int x,int y,int z){return (world.block(x,y,z)&65535)==0;}
 private static boolean opaque(Access world,int x,int y,int z,int facing){return world.solidRender(world.block(x+dx(facing),y,z+dz(facing)));}
 private static int opposite(int direction){return direction^1;}
 private static int clockwise(int direction){return switch(direction){case 2->5;case 5->3;case 3->4;case 4->2;default->throw new IllegalArgumentException("Chest facing must be horizontal");};}
 private static int dx(int direction){return direction==4?-1:direction==5?1:0;}
 private static int dz(int direction){return direction==2?-1:direction==3?1:0;}
}

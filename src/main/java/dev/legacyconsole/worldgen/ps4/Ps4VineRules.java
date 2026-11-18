package dev.legacyconsole.worldgen.ps4;

/** VineBlock::mayPlaceOnFace (0x772800) and its exceptions (0x7729e0). */
public final class Ps4VineRules {
 public interface Access extends Ps4PlantRules.Access {
  /** Original BlockFaceShape enum value; SOLID is zero. */
  int faceShape(int state,int x,int y,int z,int direction);
 }
 private static final int[][] DIRECTIONS={{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
 private Ps4VineRules(){}
 public static boolean mayPlaceOnFace(Access world,int x,int y,int z,int direction){
  if(direction==0||direction==1)return false;
  int above=world.block(x,y+1,z),id=above&65535;int[] opposite=DIRECTIONS[direction^1];
  int nx=x+opposite[0],ny=y+opposite[1],nz=z+opposite[2],neighbor=world.block(nx,ny,nz);
  if(world.faceShape(neighbor,nx,ny,nz,direction)!=0||exception(neighbor))return false;
  if(id==0||id==9||id==106||id==50||id==76||id==75)return true;
  above=world.block(x,y+1,z);
  return world.faceShape(above,x,y+1,z,1)==0&&!exception(above);
 }
 /** IDs independently recovered from the original registration and alias code. */
 static boolean exception(int state){
  int id=state&65535;
  return id>=219&&id<=234||id==260||id==138||id==118||id==347||id==20||id==95||id==33||id==29||id==34||id==96||id>=274&&id<=278;
 }
}

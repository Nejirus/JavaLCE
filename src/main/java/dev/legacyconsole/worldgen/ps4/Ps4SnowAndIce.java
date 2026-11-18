package dev.legacyconsole.worldgen.ps4;

/** Original Level rules used by the final population snow/ice pass. */
public final class Ps4SnowAndIce {
 public interface Access {
  float temperature(int x,int y,int z);
  int blockLight(int x,int y,int z);
  int block(int x,int y,int z);
 }
 public interface SnowAccess extends Access {
  int blockType(int x,int y,int z);
  int defaultState(int originalTile);
  boolean solidRender(int state);
  /** Original material identity test against 0x14187a0/0x14188e8/0x14188f0. */
  boolean specialSnowSupportMaterial(int state);
  boolean materialBlocksMotion(int state);
  /** Biome::getMaximumSnowDepth (0x852c0) on the biome obtained for this position. */
  int maximumSnowDepth(int x,int y,int z);
  boolean snowEnabled();
  /** Original origin-chunk lookup and precipitation height at the candidate;
   * null denotes a missing chunk. The caller intentionally supplies the origin chunk. */
  Integer chunkPrecipitationHeight(int chunkX,int chunkZ,int x,int y,int z);
  void setBlock(int x,int y,int z,int state,int flags);
 }
 public record SnowQuery(boolean allowed,int depth){}
 private static final SnowQuery NO_SNOW=new SnowQuery(false,0);
 private Ps4SnowAndIce(){}
 /** Level::canFreeze (0x3dcbc0). The native ordered 'above' comparison
  * rejects temperatures greater than 0.15f; equality and NaN pass that gate. */
 public static boolean canFreeze(Access world,int x,int y,int z){
  if(world.temperature(x,y,z)>0.15f||y<0||y>255)return false;
  if(world.blockLight(x,y,z)>9)return false;
  int state=world.block(x,y,z),id=state&65535;return (id==8||id==9)&&(state>>>16&15)==0;
 }

 /** 0x3fe7a0 with the non-null depth output used by the population caller.
  * The original deliberately reads default states for snow eligibility. */
 public static SnowQuery canSnowAt(SnowAccess world,int x,int y,int z,boolean checkHeight,boolean accumulate){
  if(world.temperature(x,y,z)>=0.15f)return NO_SNOW;
  if(world.blockLight(x,y,z)>11||checkHeight&&(y<0||y>255))return NO_SNOW;
  int tile=world.blockType(x,y,z);
  if(tile==78){
   int layers=layers(world.defaultState(78));if(layers+1==8)return NO_SNOW;
   if(accumulate&&!belowSnowLimit(world,x,y,z))return NO_SNOW;
   return new SnowQuery(true,layers+2);
  }
  if(tile!=0)return NO_SNOW;
  int below=world.blockType(x,y-1,z),belowDefault=world.defaultState(below);
  if(below==78){if(layers(world.defaultState(78))+1!=8)return NO_SNOW;if(accumulate&&!belowSnowLimit(world,x,y,z))return NO_SNOW;}
  else{
   if(checkHeight&&(below==79||below==174||below==208))return NO_SNOW;
   if(!world.solidRender(world.defaultState(below))&&!world.specialSnowSupportMaterial(belowDefault))return NO_SNOW;
   if(!world.materialBlocksMotion(belowDefault))return NO_SNOW;
  }
  return new SnowQuery(true,1);
 }
 private static int layers(int state){return (state>>>16&7)+1;}
 /** Biome helper 0x852c0, including CVTTSS2SI's invalid-result value. */
 public static int maximumSnowDepth(float configured){float value=configured*8.0f;return Float.isNaN(value)||value>=0x1.0p31f||value< -0x1.0p31f?Integer.MIN_VALUE:(int)value;}
 private static boolean belowSnowLimit(SnowAccess world,int x,int y,int z){
  int limit=world.maximumSnowDepth(x,y,z);if(limit<1)return true;
  int total=0,remaining=limit+1;
  do{
   int tile=world.blockType(x,y,z);
   if(tile==78)total+=layers(world.defaultState(78))+1;
   else if(tile==80)total+=8;else break;
   y--;
  }while(--remaining>0);
  return total<limit;
 }
 /** 0x6386a0. This helper reads the actual snow state before incrementing. */
 static int snowState(SnowAccess world,int x,int y,int z,int added,boolean accumulate){
  int tile=world.blockType(x,y,z),state=world.block(x,y,z);
  if(tile==78){if(accumulate)added=Math.min(8,added+layers(state));}
  else state=world.defaultState(78);
  return (state&65535)|((added-1)<<16);
 }
 /** Level::trySnow (0x3fede0). Fixed N,S,W,E order and the original
  * origin-chunk height query are retained. Rejected world writes still return true. */
 public static boolean placeSnow(SnowAccess world,int x,int y,int z,boolean checkHeight,boolean spread){
  if(!world.snowEnabled())return false;
  SnowQuery current=canSnowAt(world,x,y,z,checkHeight,true);int half=current.depth/2,depth=current.depth,sx=x,sz=z;
  if(spread)for(int direction:new int[]{2,3,4,5}){
   int nx=x+Ps4DirectionPlane.dx(direction),nz=z+Ps4DirectionPlane.dz(direction);
   SnowQuery candidate=canSnowAt(world,nx,y,nz,checkHeight,true);
   if(candidate.allowed&&candidate.depth<depth){
    Integer height=world.chunkPrecipitationHeight(x>>4,z>>4,nx,y,nz);
    if(height!=null&&height==y||candidate.depth<half){sx=nx;sz=nz;depth=candidate.depth;}
   }
  }
  if(sx!=x||sz!=z||current.allowed){world.setBlock(sx,y,sz,snowState(world,sx,y,sz,1,true),2);return true;}
  return false;
 }
}

package dev.legacyconsole.worldgen.ps4;

/** LevelChunk precipitation-height cache and scan (0x3f7870/0x3f7da0). */
public final class Ps4PrecipitationHeight {
 public interface Access {
  int cachedHeight(int index);
  void cacheHeight(int index,int unsignedByte);
  /** Original primary-layer section-empty query at the supplied section Y. */
  boolean sectionEmpty(int sectionY);
  int primary(int x,int y,int z);
  int secondary(int x,int y,int z);
  boolean blocksMotion(int state);
  boolean liquid(int state);
 }
 private Ps4PrecipitationHeight(){}
 /** Prefix of LevelChunk.setTile (0x40afce). This runs before the write's
  * unchanged-state test, including when that test returns false. World-level
  * Y validation belongs to the caller and is not part of this chunk helper. */
 public static void invalidateBeforeWrite(Access world,int x,int y,int z){
  int index=((z&15)<<4)|(x&15);
  if((world.cachedHeight(index)&255)-1<=y)world.cacheHeight(index,255);
 }
 static int highestSection(Access world){for(int y=240;y>=16;y-=16)if(!world.sectionEmpty(y))return y+15;return 0;}
 public static int height(Access world,int x,int z){
  int index=((z&15)<<4)|(x&15),cached=world.cachedHeight(index)&255;if(cached!=255)return cached;
  int y=highestSection(world)+15;
  for(;y>=0;y--){
   if(y>=256)continue;
   int primary=world.primary(x,y,z),secondary=world.secondary(x,y,z);
   if(world.blocksMotion(primary)||world.liquid(primary)||world.liquid(secondary)){
    int value=y+1;if(value==255)value=254;value&=255;world.cacheHeight(index,value);return value;
   }
  }
  world.cacheHeight(index,255);return 255;
 }
}

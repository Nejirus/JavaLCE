package dev.legacyconsole.worldgen.ps4;

/** Recovered PS4 plant placement rules. Material identity and the fluid state
 * are separate inputs, as they are in the original block implementation. */
public final class Ps4PlantRules {
 public interface Access extends Ps4OreFeature.Access {
  boolean solid(int state);
  boolean liquid(int state);
  boolean replaceable(int state);
  boolean airMaterial(int state);
  boolean leavesMaterial(int state);
  boolean waterMaterial(int state);
  boolean lavaMaterial(int state);
  boolean iceMaterial(int state);
  boolean waterFluid(int x,int y,int z);
  default boolean solidRender(int state){throw new UnsupportedOperationException("PS4 solid-render classification required");}
  default int rawBrightness(int x,int y,int z){throw new UnsupportedOperationException("PS4 light input required");}
 }
 private static final int[][] HORIZONTAL={{0,-1},{1,0},{0,1},{-1,0}};
 private Ps4PlantRules(){}

 /** BushBlock::mayPlace 0x138df0, including each block's soil virtual. */
 public static boolean mayPlace(Access world,int tile,int x,int y,int z){
  if(tile==37||tile==38){int current=world.block(x,y,z);if(world.waterMaterial(current)||world.lavaMaterial(current))return false;}
  if(!world.replaceable(world.block(x,y,z)))return false;
  int below=world.block(x,y-1,z);
  if(!validSoil(world,tile,below))return false;
  if(tile==39||tile==40)return canStay(world,tile,x,y,z);
  return tile!=175||(world.block(x,y+1,z)&65535)==0;
 }

 /** 0x138ed0, 0x1f6f00 and 0x780740. Lily generation deliberately uses
  * this block-identity rule, which differs from its canStay water-level rule. */
 public static boolean validSoil(Access world,int tile,int below){
  int id=below&65535;
  return switch(tile){
   case 32->id==12||id==172||id==159||id==3;
   case 111->id==9||world.iceMaterial(below);
   case 39,40->world.solidRender(below);
   default->id==2||id==3||id==60;
  };
 }

 /** Bush/TallGrass 0x1390c0/0x6d2d70, Cactus 0x159560, Reeds 0x5b3f80,
  * Waterlily 0x780790 and DoublePlant 0x213d50. */
 public static boolean canStay(Access world,int state,int x,int y,int z){
  int tile=state&65535;
  if(tile==39||tile==40){
   if(y<0||y>=256)return false;
   int below=world.block(x,y-1,z),id=below&65535;
   return id==110||id==3&&(below>>>16)==2||world.rawBrightness(x,y,z)<13&&world.solidRender(below);
  }
  if(tile==81){
   for(int[] d:HORIZONTAL){int neighbor=world.block(x+d[0],y,z+d[1]);if(world.solid(neighbor)||world.lavaMaterial(neighbor))return false;}
   int below=world.block(x,y-1,z)&65535;
   return (below==81||below==12)&&!world.liquid(world.block(x,y+1,z));
  }
  if(tile==83){
   int below=world.block(x,y-1,z)&65535;
   if(below==83)return true;
   int current=world.block(x,y,z)&65535;
   if(current!=83&&current!=0||below!=2&&below!=3&&below!=12)return false;
   for(int[] d:HORIZONTAL){
    int neighbor=world.block(x+d[0],y-1,z+d[1]);
    // Both lookups occur even if the block material is already water.
    boolean fluid=world.waterFluid(x+d[0],y-1,z+d[1]);
    if(world.waterMaterial(neighbor)||fluid||(neighbor&65535)==212)return true;
   }
   return false;
  }
  if(tile==111){
   if(y<0||y>=256)return false;
   int below=world.block(x,y-1,z);
   return world.waterMaterial(below)&&(below>>>16)==0||world.iceMaterial(below);
  }
  if(tile==175){
   if((state>>>16&8)!=0)return (world.block(x,y-1,z)&65535)==175;
   if((world.block(x,y+1,z)&65535)!=175)return false;
  }
  return validSoil(world,tile,world.block(x,y-1,z));
 }
}

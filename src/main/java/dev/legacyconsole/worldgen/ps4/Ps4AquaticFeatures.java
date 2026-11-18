package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** Original mcps4 kelp, seagrass and sea-pickle generation and support rules.
 * State/material/collision inputs belong to the world bridge, not a Java plant
 * survival rule. Canonical states retain the original numeric metadata. */
public final class Ps4AquaticFeatures {
 public interface Access extends Ps4PlantRules.Access {
  boolean clientSide();
  boolean solidBlocking(int state);
  boolean slab(int state);
  /** Original Block byte at +0x0a, tested by SeaGrassBlock::mayPlace. */
  boolean seagrassBelowFlag(int state);
  /** Secondary state when present, otherwise primary (Level +0x100/+0x108). */
  int effectiveBlock(int x,int y,int z);
  boolean canPlace(int state,int x,int y,int z);
  /** Original Level byte at +0x220, queried only on seagrass reentry. */
  boolean seagrassReentryBlocked();
 }
 private Ps4AquaticFeatures(){}
 private static boolean water(Access world,int x,int y,int z){return world.waterMaterial(world.block(x,y,z));}
 private static boolean blocking(Access world,int x,int y,int z){return world.solidBlocking(world.block(x,y,z));}
 private static int offset(Random random){return random.nextInt(8)-random.nextInt(8);}
 private static boolean excludedKelpSoil(int id){return id==213||id==88||id==85||id==190||id==113||id==188||id==189||id==191||id==192;}
 private static boolean halfSlab(int id){return id==44||id==126||id==182||id==205||id==312;}

 /** KelpBlock::mayPlace, 0x3c59b0. */
 public static boolean mayPlaceKelp(Access world,int x,int y,int z){
  int below=world.block(x,y-1,z),id=below&65535;
  if(excludedKelpSoil(id))return false;
  if(world.waterMaterial(world.effectiveBlock(x,y,z))){int level=world.effectiveBlock(x,y,z)>>>16;if(level>=1&&level<=7)return false;}
  if(id==258&&world.waterMaterial(world.effectiveBlock(x,y,z)))return true;
  return (world.solidBlocking(below)||world.slab(below)&&(below>>>16&8)!=0)&&world.canPlace(258,x,y,z);
 }

 /** KelpFeature::place, 0x3c64e0. The cached states contain ages 0..15. */
 public static boolean kelp(Access world,Random random,int x,int y,int z){
  if(world.clientSide())return true;
  while(!blocking(world,x,y,z)){if((world.block(x,y,z)&65535)==258||y<1)break;y--;}
  if(!blocking(world,x,y,z))return false;
  for(int attempt=0;attempt<10;attempt++){
   int dz=offset(random),dx=offset(random),wx=x+dx,wy=y,wz=z+dz;
   while(!water(world,wx,wy,wz)&&(world.block(wx,wy,wz)&65535)!=0)wy++;
   if((world.block(wx,wy,wz)&65535)==0||water(world,wx,wy-1,wz)||(world.block(wx,wy,wz)&65535)==258)continue;
   int depth=0;while(true){boolean wet=water(world,wx,wy+depth,wz);if(depth>15||!wet)break;depth++;}
   if(depth<3)continue;
   int height=depth>3?3+random.nextInt(depth-3):3;
   if(!mayPlaceKelp(world,wx,wy,wz))continue;
   world.setBlock(wx,wy,wz,258|((15-height)<<16),50);
   for(int i=1;i<height;i++){
    if(!water(world,wx,wy+i,wz)||!mayPlaceKelp(world,wx,wy+i,wz))break;
    world.setBlock(wx,wy+i,wz,258|((15-height+i)<<16),50);
   }
  }
  return true;
 }

 /** SeaGrassBlock::mayPlace overloads, 0x5f58a0 and 0x5f5ae0. */
 public static boolean mayPlaceSeagrass(Access world,int state,int x,int y,int z,boolean skipPlacement){
  if((world.block(x,y,z)&65535)==270){
   if(!world.canPlace(state,x,y,z))return false;
   int type=state>>>16;
   if(type==2){int above=world.block(x,y+1,z);return (above&65535)==270&&(above>>>16)==1&&blocking(world,x,y-1,z);}
   if(type==1){if(!world.canPlace(state,x,y,z))return false;int below=world.block(x,y-1,z);return (below&65535)==270&&(below>>>16)==2;}
  }
  int below=world.block(x,y-1,z),id=below&65535;
  if(id==213||world.seagrassBelowFlag(below)||id==88)return false;
  boolean supported=blocking(world,x,y-1,z);
  if(!supported||skipPlacement)return supported;
  if(!world.canPlace(270,x,y,z)||!world.waterMaterial(world.effectiveBlock(x,y,z)))return false;
  int level=world.effectiveBlock(x,y,z)>>>16;return level==0||level>7;
 }

 /** SeaGrassFeature::place, 0x5f6730; the original retains its busy byte after
  * an unsuccessful attempt and clears it only after reaching the scatter. */
 public static final class Seagrass {
  private boolean placing;
  public Seagrass(){this(false);}
  Seagrass(boolean placing){this.placing=placing;}
  public boolean placing(){return placing;}
  public boolean place(Access world,Random random,int x,int y,int z){
   if(placing&&world.seagrassReentryBlocked())return false;
   placing=true;
   boolean supported=blocking(world,x,y,z);
   if(!supported&&y>0){do{y--;supported=blocking(world,x,y,z);if(y<1)break;}while(!supported);}
   if(!blocking(world,x,y,z))return false;
   int mode=random.nextInt(20)==0?1:random.nextInt(20)==0?2:0;
   int count=mode==1?14:8;
   for(int attempt=0;attempt<count;attempt++){
    int dz=offset(random),dx=offset(random),wx=x+dx,wy=y,wz=z+dz;
    while(!water(world,wx,wy,wz)&&(world.block(wx,wy,wz)&65535)!=0)wy++;
    if((world.block(wx,wy,wz)&65535)==0||!water(world,wx,wy,wz)||!mayPlaceSeagrass(world,270,wx,wy,wz,true))continue;
    if(mode!=0){int below=world.block(wx,wy-1,wz)&65535;if(below==79||below==273||below==174)continue;}
    if(mode==2&&water(world,wx,wy+2,wz)&&water(world,wx,wy+1,wz)&&random.nextInt(9)!=0){
     world.setBlock(wx,wy,wz,270|(2<<16),18);world.setBlock(wx,wy+1,wz,270|(1<<16),18);
    }else world.setBlock(wx,wy,wz,270,18);
   }
   placing=false;return true;
  }
 }

 /** SeaPickleBlock::canStay/validSoil, 0x5f7310 and 0x5f7400. */
 public static boolean canStayPickle(Access world,int x,int y,int z){
  int below=world.block(x,y-1,z),id=world.block(x,y-1,z)&65535;
  if(id==20||id==95)return true;
  if((world.block(x,y-1,z)&65535)==0||water(world,x,y-1,z))return false;
  if(halfSlab(below&65535)&&(below>>>16&8)==0)return false;
  return world.solidBlocking(below);
 }

 /** SeaPickleFeature::place, 0x5f8c60; the state count is sampled in [0,4). */
 public static boolean pickles(Access world,Random random,int x,int y,int z){
  if(world.clientSide())return true;
  while(!blocking(world,x,y,z)){if((world.block(x,y,z)&65535)==271||y<1)break;y--;}
  if(!blocking(world,x,y,z))return false;
  for(int attempt=0;attempt<4;attempt++){
   int dz=random.nextInt(6),dx=random.nextInt(6),wx=x+dx,wy=y,wz=z+dz,count=random.nextInt(4);
   while(!water(world,wx,wy,wz)&&(world.block(wx,wy,wz)&65535)!=0)wy++;
   if((world.block(wx,wy,wz)&65535)!=0&&(world.block(wx,wy,wz)&65535)!=271&&canStayPickle(world,wx,wy,wz))world.setBlock(wx,wy,wz,271|(count<<16),2);
  }
  return true;
 }
}

package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** MegaPineTreeFeature::place (0x4517f0) and podzol postprocess (0x451c40). */
public final class Ps4MegaPineFeature {
 private Ps4MegaPineFeature(){}
 public static boolean place(Ps4PlantRules.Access world,Random random,int x,int y,int z,int baseHeight,int extraRange,int log,int leaves,boolean longCanopy,boolean updates){
  int height=Ps4MegaTreeRules.height(random,baseHeight,extraRange);
  if(!Ps4MegaTreeRules.validate(world,x,y,z,height,updates))return false;
  int depth=random.nextInt(5)+(longCanopy?baseHeight:3),previous=0;
  for(int wy=y+height-depth;wy<=y+height;wy++){
   int remaining=y+height-wy,radius=(int)Math.floor((float)remaining/(float)depth*3.5f);
   int extra=remaining>0&&radius==previous&&(wy&1)==0?1:0;
   Ps4MegaTreeRules.wideLeaves(world,x,wy,z,radius+extra,leaves,updates);previous=radius;
  }
  for(int level=0;level<height;level++){
   Ps4MegaTreeRules.trunk(world,x,y+level,z,log,updates);
   if(level<height-1){Ps4MegaTreeRules.trunk(world,x+1,y+level,z,log,updates);Ps4MegaTreeRules.trunk(world,x+1,y+level,z+1,log,updates);Ps4MegaTreeRules.trunk(world,x,y+level,z+1,log,updates);}
  }
  return true;
 }
 public static void postPlace(Ps4PlantRules.Access world,Random random,int x,int y,int z,boolean updates){
  podzolArea(world,x-1,y,z-1,updates);podzolArea(world,x+2,y,z-1,updates);
  podzolArea(world,x-1,y,z+2,updates);podzolArea(world,x+2,y,z+2,updates);
  for(int i=0;i<5;i++){int value=random.nextInt(64),dx=value&7,dz=value>>3;if(dx==0||dx==7||dz==0||dz==7)podzolArea(world,x+dx-3,y,z+dz-3,updates);}
 }
 private static void podzolArea(Ps4PlantRules.Access world,int x,int y,int z,boolean updates){for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)if(Math.abs(dx)!=2||Math.abs(dz)!=2)podzolColumn(world,x+dx,y,z+dz,updates);}
 private static void podzolColumn(Ps4PlantRules.Access world,int x,int y,int z,boolean updates){
  for(int dy=2;dy>=-3;dy--){int old=world.block(x,y+dy,z),id=old&65535;if(id==2||id==3){world.setBlock(x,y+dy,z,3|(2<<16),updates?3:2);return;}if(!world.airMaterial(old)&&dy<0)return;}
 }
}

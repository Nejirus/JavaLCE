package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** BambooFeature::place (0x40e40), with BambooBlock::mayPlace (0x3fb40).
 * Canonical metadata is leaves in bits 0..1 and the original age in bit 3. */
public final class Ps4BambooFeature {
 private Ps4BambooFeature(){}
 public static boolean mayPlace(Ps4PlantRules.Access world,int x,int y,int z){
  if(world.liquid(world.block(x,y,z)))return false;
  int below=world.block(x,y-1,z)&65535;
  return below==319||below==321||below==2||below==3||below==12||below==110||below==13||below==31||below==175;
 }
 public static boolean place(Ps4PlantRules.Access world,Random random,int x,int y,int z){
  if((world.block(x,y,z)&65535)!=0||!mayPlace(world,x,y,z))return false;
  int below=world.block(x,y-1,z)&65535;
  if(below!=2&&below!=3)return false;
  // BlockPos::hashCode (0xbffe0) performs all arithmetic in 32 bits.
  int hash=x*0x2fc20f^z*0x6ebfff5;
  hash=(hash*0x285b825+11)*hash;
  int limit=random.nextInt(((hash>>>24)%5)|8)+4,wy=y,count=-1;
  while((world.block(x,wy,z)&65535)==0){
   world.setBlock(x,wy,z,319|(8<<16),2);wy++;
   if(++count>=limit)break;
  }
  if(wy-y>=3){
   world.setBlock(x,wy,z,319|(10<<16),2);
   world.setBlock(x,wy-1,z,319|(10<<16),2);
   world.setBlock(x,wy-2,z,319|(9<<16),2);
  }
  return true;
 }
}

package dev.legacyconsole.worldgen.ps4;

/** StructurePlaceSettings::getRandom position-seeded path (mcps4 0x6c2390).
 * These defaults are used when no random is supplied and the stored seed is -1. */
public final class Ps4TemplateRandom {
 private Ps4TemplateRandom(){}
 public static Ps4Random fromPosition(int x,int z){
  long seed=(long)z*0x5f24f+Integer.toUnsignedLong(z*z)*0x4307a7L+(long)((x*0x4c1906+0x5ac0db)*x);
  return new Ps4Random(seed^0x3ad8025fL);
 }
}

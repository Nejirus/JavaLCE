package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** LevelChunk::getRandom, mcps4 0x40fd80. The two square terms retain
 * the original 32-bit overflow and unsigned extension. */
public final class Ps4ChunkRandom {
 private Ps4ChunkRandom(){}
 public static Random create(long worldSeed,int x,int z,long salt){
  long seed=worldSeed+(long)x*0x5ac0dbL+Integer.toUnsignedLong(x*x*0x4c1906)+(long)z*0x5f24fL+Integer.toUnsignedLong(z*z)*0x4307a7L;
  return new Random(seed^salt);
 }
}

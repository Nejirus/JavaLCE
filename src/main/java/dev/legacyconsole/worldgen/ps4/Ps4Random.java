package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** Native 48-bit uniform Random stream, including integer draws from a value
 * copy. The original Rotation::getRandom takes its Random argument by value. */
public final class Ps4Random extends Random {
 private static final long serialVersionUID=1L;
 private static final long MULTIPLIER=0x5deece66dL,MASK=(1L<<48)-1;
 private long uniformSeed;
 public Ps4Random(long seed){super(seed);}
 /** Random's original default constructor (0x596c00), given the return
  * value of the PS4 clock call at 0xaf1949. Mth's shared stream uses this
  * constructor during module initialization; it is not a world-seed stream. */
 public static Ps4Random fromClockTick(long tick){return new Ps4Random(tick+0xd8b55fac9decL);}
 @Override public synchronized void setSeed(long seed){super.setSeed(seed);uniformSeed=(seed^MULTIPLIER)&MASK;}
 @Override protected synchronized int next(int bits){uniformSeed=(uniformSeed*MULTIPLIER+11)&MASK;return (int)(uniformSeed>>>(48-bits));}
 /** Preview an integer draw without advancing this stream. No Gaussian draw
  * or Gaussian-cache copy is involved in the recovered by-value caller. */
 public synchronized int peekInt(int bound){Ps4Random copy=new Ps4Random(0);copy.uniformSeed=uniformSeed;return copy.nextInt(bound);}
}

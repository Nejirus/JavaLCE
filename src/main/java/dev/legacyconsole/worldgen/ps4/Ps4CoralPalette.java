package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** CoralBlock's legacy variant="dead_coral" conversion (0x1c73d0).
 * This is the original parser's special case, not the whole NBT palette loader. */
public final class Ps4CoralPalette {
 private Ps4CoralPalette(){}
 /** The original first sets dead=true, then takes a color from Mth's
  * shared stream. The stream is shared across palette loads and other users. */
 public static int deadCoral(Random sharedMathRandom){return 259|((8|sharedMathRandom.nextInt(5))<<16);}
}

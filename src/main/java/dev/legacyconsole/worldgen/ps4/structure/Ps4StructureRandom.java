package dev.legacyconsole.worldgen.ps4.structure;
import java.util.*;
/** Original LargeFeature stream and the console's nextInt(i) shuffles. */
public final class Ps4StructureRandom {
 private Ps4StructureRandom(){}
 public static Random start(long seed,int x,int z){Random source=new Random(seed);long a=source.nextLong(),b=source.nextLong();Random result=new Random((long)x*a^(long)z*b^seed);result.nextInt();return result;}
 public static Random region(long seed,int x,int z,long salt){return new Random((long)x*341873128712L+(long)z*132897987541L+seed+salt);}
 public static <T> void shuffle(List<T> values,Random random){for(int i=values.size()-1;i>0;i--)Collections.swap(values,i,random.nextInt(i));}
}

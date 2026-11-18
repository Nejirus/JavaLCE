package dev.legacyconsole.worldgen.ps4;

import dev.legacyconsole.worldgen.*;
import dev.legacyconsole.worldgen.ps4.layers.Ps4Layers;
import java.util.*;
import java.util.function.*;
import java.util.concurrent.CancellationException;

/** PS4 BiomeSource::findSeed (0x8e300) and category predicate (0x8e640). */
public final class Ps4BalancedSeed {
 private static final int[] CATEGORY={5,2,4,1,2,1,3,6,8,1,5,6,0,0,7,7,2,4,2,1,1,3,3,3,5,1,0,2,2,2,0,0,1,1,1,4,4,4,4,4,3,3,5,5,5,5,5,5,5,5,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,2,2,4,1,2,1,3,8,8,8,8,8,0,8,8,8,8,8,8,8,8,3,8,3,8,8,8,2,2,2,0,8,1,1,1,4,4,4,4,4};
 private Ps4BalancedSeed(){}
 /** LCE leaves explicit seeds alone; balanced search belongs to blank-seed creation. */
 public static boolean shouldSearch(boolean legacy,boolean enabled,String seed){return legacy&&enabled&&seed.isEmpty();}
 public record Balance(boolean matches,float score,float[] fractions){}
 public static Balance evaluate(int[] ids){
  float[] groups=new float[8];
  for(int id:ids){int group=id>=0&&id<CATEGORY.length?CATEGORY[id]:8;if(group<8)groups[group]++;}
  for(int i=0;i<8;i++)groups[i]/=ids.length;
  boolean matches=groups[7]>=.001f&&groups[6]<=.1f&&groups[5]>=.01f&&groups[5]<=.25f;
  for(int i=0;i<5;i++)matches&=groups[i]>=.01f;
  float score=(4*groups[0]+4*groups[1]+4*groups[2]+4*groups[3]+4*groups[4]+groups[5]+4*groups[6]+4*groups[7])*.125f;
  return new Balance(matches,score,groups);
 }
 public record Result(long seed,int attempts,boolean matched){}
 public static Result find(Random random,Ps4WorldSize size,Ps4BiomeScale scale,BooleanSupplier canceled,IntConsumer progress){
  long started=System.nanoTime();float best=0;long selected=0;int attempts=0;
  do {
   if(canceled.getAsBoolean()||Thread.currentThread().isInterrupted())throw new CancellationException();
   long seed=random.nextLong();Ps4Layers layers=new Ps4Layers(seed,size.blocks,scale.ordinal());
   // Original PS4 search excludes the 32-block edge of its central Classic area.
   Balance balance=evaluate(layers.quartArea(-100,-100,200,200));attempts++;progress.accept(attempts);
   if(balance.matches)return new Result(seed,attempts,true);
   if(balance.score>best){best=balance.score;selected=seed;}
  }while(System.nanoTime()-started<=10_000_000_000L);
  return new Result(selected,attempts,false);
 }
}

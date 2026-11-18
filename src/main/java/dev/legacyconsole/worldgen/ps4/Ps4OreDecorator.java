package dev.legacyconsole.worldgen.ps4;

import java.util.List;
import java.util.Random;

/** BiomeDecorator::generateOres, mcps4 0x88970. The caller supplies the shared
 * population RNG; this routine does not reseed it. Awaiting population wiring. */
public final class Ps4OreDecorator {
 public record Vein(String name,int state,int size,int attempts,int minimum,int maximum,boolean triangular){}
 @FunctionalInterface public interface Placement {
  void place(Vein vein,Random random,int x,int y,int z);
 }
 // Original default settings constructor 0x1f1940. The source's diorite pass
 // precedes granite, despite their reverse order in the configuration object.
 public static final List<Vein> DEFAULTS=List.of(
  new Vein("dirt",3,33,10,0,128,false),
  new Vein("gravel",13,33,8,0,128,false),
  new Vein("diorite",1|(3<<16),33,10,0,80,false),
  new Vein("granite",1|(1<<16),33,10,0,80,false),
  new Vein("andesite",1|(5<<16),33,10,0,80,false),
  new Vein("coal",16,17,20,0,128,false),
  new Vein("iron",15,9,20,0,64,false),
  new Vein("gold",14,9,2,0,32,false),
  new Vein("redstone",73,8,8,0,16,false),
  new Vein("diamond",56,8,1,0,16,false),
  new Vein("lapis",21,7,1,16,16,true)
 );

 private Ps4OreDecorator(){}

 public static void decorate(Ps4OreFeature.Access world,Random random,int originX,int originZ){
  decorate(DEFAULTS,random,originX,originZ,(vein,rng,x,y,z)->new Ps4OreFeature(vein.state(),vein.size()).place(world,rng,x,y,z));
 }

 /** Mesa's decorator override (0x46ba10) runs the base ore pass, then
  * twenty attempts with the same configured gold feature at Y in [32,80). */
 public static void decorateMesa(Ps4OreFeature.Access world,Random random,int originX,int originZ){
  decorateMesa(DEFAULTS,random,originX,originZ,(vein,rng,x,y,z)->new Ps4OreFeature(vein.state(),vein.size()).place(world,rng,x,y,z));
 }
 public static void decorateMesa(List<Vein> veins,Random random,int originX,int originZ,Placement placement){
  decorate(veins,random,originX,originZ,placement);
  Vein gold=veins.stream().filter(vein->vein.name().equals("gold")).findFirst().orElseThrow(()->new IllegalArgumentException("Original Mesa decorator requires its configured gold feature"));
  decorate(List.of(new Vein(gold.name(),gold.state(),gold.size(),20,32,80,false)),random,originX,originZ,placement);
 }

 /** Triangular veins use minimum as center and maximum as spread, as in the
  * original lapis settings. Other veins use an exclusive upper height. */
 public static void decorate(List<Vein> veins,Random random,int originX,int originZ,Placement placement){
  for(Vein vein:veins){
   int minimum=vein.minimum(),maximum=vein.maximum();
   if(!vein.triangular()){
    if(maximum<minimum){int swap=minimum;minimum=maximum;maximum=swap;}
    else if(maximum==minimum){if(minimum>=255)minimum--;else maximum++;}
   }
   for(int attempt=0;attempt<vein.attempts();attempt++){
    int x=originX+random.nextInt(16);
    int y=vein.triangular()?random.nextInt(maximum)+minimum-maximum+random.nextInt(maximum):minimum+random.nextInt(maximum-minimum);
    int z=originZ+random.nextInt(16);
    placement.place(vein,random,x,y,z);
   }
  }
 }
}

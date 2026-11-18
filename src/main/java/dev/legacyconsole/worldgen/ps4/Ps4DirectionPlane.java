package dev.legacyconsole.worldgen.ps4;

/** Mutable cached Direction::Plane::HORIZONTAL array (mcps4 0x200f70).
 * A population context shares this object between features and liquid ticks.
 * CoralHangFeature swaps entries in the original process-wide array. */
public final class Ps4DirectionPlane {
 private final int[] directions;
 public Ps4DirectionPlane(){this(2,5,3,4);}
 public Ps4DirectionPlane(int... directions){
  if(directions.length!=4)throw new IllegalArgumentException("Four horizontal directions required");
  int bits=0;for(int d:directions){if(d<2||d>5||(bits&(1<<d))!=0)throw new IllegalArgumentException("Invalid horizontal permutation");bits|=1<<d;}
  this.directions=directions.clone();
 }
 public int get(int index){return directions[index];}
 public int[] snapshot(){return directions.clone();}
 void swap(int first,int second){int value=directions[first];directions[first]=directions[second];directions[second]=value;}
 public static int dx(int direction){return direction==4?-1:direction==5?1:0;}
 public static int dz(int direction){return direction==2?-1:direction==3?1:0;}
}

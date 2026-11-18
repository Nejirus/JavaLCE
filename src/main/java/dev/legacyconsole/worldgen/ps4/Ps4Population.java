package dev.legacyconsole.worldgen.ps4;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** OverworldLevelSource::postProcess, mcps4 0x508bb0. Feature implementations
 * receive one shared RNG. Platform world operations are supplied by the caller. */
public final class Ps4Population {
 public enum Structure {MINESHAFT,VILLAGE,STRONGHOLD,SCATTERED,MONUMENT,OCEAN_RUIN,MANSION,BURIED_TREASURE,SHIPWRECK,OUTPOST}
 public record Settings(Set<Structure> structures,boolean waterLakes,int waterChance,boolean lavaLakes,int lavaChance,boolean dungeons,int dungeonCount){
  public Settings {structures=Set.copyOf(structures);}
 }
 public static final Settings DEFAULTS=new Settings(EnumSet.allOf(Structure.class),true,4,true,80,true,8);
 private static final List<Structure> ORDER=List.of(Structure.values());
 public interface Access {
  void populationMode(boolean active);
  int biome(int x,int z);
  boolean structure(Structure kind,Random random,int chunkX,int chunkZ,boolean villageFlag);
  void finishOutpostChunk(int chunkX,int chunkZ);
  void lake(Random random,int x,int y,int z,int tile);
  void dungeon(Random random,int x,int y,int z);
  void decorate(Random random,int biome,int originX,int originZ);
  /** The pinned PS4 helper 0x4a6450 is a single RET and consumes no RNG.
   * Explicit overrides are available to isolated dispatcher comparisons. */
  default void spawnMobs(Random random,int biome,int x,int z,int width,int depth){}
  int seaLevel();
  int precipitationHeight(int x,int z);
  boolean canFreeze(int x,int y,int z);
  void ice(int x,int y,int z);
  void snow(int x,int y,int z,boolean checkLight,boolean accumulate);
 }
 private Ps4Population(){}

 public static Ps4Random random(long seed,int chunkX,int chunkZ){
  Ps4Random random=new Ps4Random(seed);
  long x=random.nextLong(),z=random.nextLong();
  x=(x+(x>>>63))|1L;z=(z+(z>>>63))|1L;
  random.setSeed(((long)chunkX*x+(long)chunkZ*z)^seed);
  return random;
 }

 public static Random populate(long seed,int chunkX,int chunkZ,boolean structures,Settings settings,Access world){
  world.populationMode(true);
  try{
   int originX=chunkX<<4,originZ=chunkZ<<4;
   int biome=world.biome(originX+16,originZ+16);
   Random random=random(seed,chunkX,chunkZ);
   boolean village=false;
   if(structures){
    for(Structure kind:ORDER)if(settings.structures().contains(kind)){
     boolean placed=world.structure(kind,random,chunkX,chunkZ,kind==Structure.VILLAGE);
     if(kind==Structure.VILLAGE)village=placed;
    }
    world.finishOutpostChunk(chunkX,chunkZ);
   }
   // These two comparisons are biome identity, not desert-family membership.
   if(biome!=2&&biome!=17&&!village&&settings.waterLakes()){
    if(random.nextInt(settings.waterChance())==0){
     int x=originX+random.nextInt(16)+8,y=random.nextInt(128),z=originZ+random.nextInt(16)+8;
     world.lake(random,x,y,z,9);
    }
    village=false;
   }
   if(!village){
    // The native routine draws before checking whether lava lakes are enabled.
    if(random.nextInt(settings.lavaChance()/10)==0&&settings.lavaLakes()){
     int x=originX+random.nextInt(16)+8;
     int y=random.nextInt(random.nextInt(120)+8);
     int z=originZ+random.nextInt(16)+8;
     if(y<world.seaLevel()||random.nextInt(settings.lavaChance()/8)==0)world.lake(random,x,y,z,11);
    }
   }
   if(settings.dungeons())for(int i=0;i<settings.dungeonCount();i++){
    int x=originX+random.nextInt(16)+8,y=random.nextInt(128),z=originZ+random.nextInt(16)+8;
    world.dungeon(random,x,y,z);
   }
   world.decorate(random,biome,originX,originZ);
   world.spawnMobs(random,biome,originX+8,originZ+8,16,16);
   for(int x=0;x<16;x++)for(int z=0;z<16;z++){
    int wx=originX+8+x,wz=originZ+8+z,wy=world.precipitationHeight(wx,wz);
    if(world.canFreeze(wx,wy-1,wz))world.ice(wx,wy-1,wz);
    world.snow(wx,wy,wz,true,true);
   }
   return random;
  }finally{world.populationMode(false);}
 }
}

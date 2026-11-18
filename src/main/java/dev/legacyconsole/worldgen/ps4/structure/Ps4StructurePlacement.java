package dev.legacyconsole.worldgen.ps4.structure;

import dev.legacyconsole.worldgen.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import net.minecraft.world.level.ChunkPos;

/** PS4 FjIsFeatureChunk (0x2dbea0), region seeding and finite-world filters. */
public final class Ps4StructurePlacement {
 public static final Set<Integer> VILLAGE=Set.of(1,129,35,12,5,19,30,31,2);
 public static final Set<Integer> COAST=Set.of(15,16,26,0,24,46,47,48,49,44,45,42);
 public static final Set<Integer> OCEAN=Set.of(0,10,24,42,43,44,45,46,47,48,49);
 private final LegacyChunkGenerator generator;
 private final Map<String,Map<Long,Optional<ChunkPos>>> cells=new ConcurrentHashMap<>();
 public Ps4StructurePlacement(LegacyChunkGenerator generator){this.generator=generator;}
 public boolean within(int x,int z,int margin){int half=generator.size().blocks/32;return x>=margin-half&&z>=margin-half&&x<half-margin&&z<half-margin;}
 public boolean viable(int chunkX,int chunkZ,int radius,Set<Integer> allowed){
  int x=chunkX*16+8,z=chunkZ*16+8,minX=(x-radius)>>2,minZ=(z-radius)>>2;
  int width=((x+radius)>>2)-minX+1,depth=((z+radius)>>2)-minZ+1;
  for(int id:generator.source().layers().quartArea(minX,minZ,width,depth))if(!allowed.contains(id))return false;
  return true;
 }
 public ChunkPos grid(int x,int z,int spacing,int separation,long salt){
  int cellX=Math.floorDiv(x,spacing),cellZ=Math.floorDiv(z,spacing);Random random=Ps4StructureRandom.region(generator.seed(),cellX,cellZ,salt);
  return new ChunkPos(cellX*spacing+random.nextInt(spacing-separation),cellZ*spacing+random.nextInt(spacing-separation));
 }
 public boolean village(int x,int z){int spacing=generator.size().blocks/16<128?16:22;return within(x,z,0)&&grid(x,z,spacing,7,10387312).equals(new ChunkPos(x,z))&&viable(x,z,generator.scale().ordinal(),VILLAGE);}
 public boolean scattered(int x,int z){int spacing=generator.size().blocks/16<128?16:32;return within(x,z,0)&&grid(x,z,spacing,8,14357617).equals(new ChunkPos(x,z));}
 /** A cell has at most one feature: the first distinct in-bounds sample whose biome filter passes. */
 public ChunkPos finite(String key,int x,int z,int attempts,int normalSpacing,int separation,long salt,BiPredicate<Integer,Integer> filter){
  boolean compact=generator.size().blocks/16<128;int spacing=compact?32:normalSpacing,count=compact?attempts*3/2:attempts;
  int cellX=Math.floorDiv(x,spacing)*spacing,cellZ=Math.floorDiv(z,spacing)*spacing;
  return cells.computeIfAbsent(key,k->new ConcurrentHashMap<>()).computeIfAbsent(ChunkPos.asLong(cellX,cellZ),k->Optional.ofNullable(candidate(generator.seed(),generator.size().blocks/16,cellX,cellZ,spacing,separation,count,salt,filter))).orElse(null);
 }
 public static ChunkPos candidate(long seed,int worldChunks,int originX,int originZ,int spacing,int separation,int attempts,long salt,BiPredicate<Integer,Integer> filter){
  int bound=spacing-separation,limit=Math.min(attempts,bound*bound),min=3-worldChunks/2,max=worldChunks/2-3;
  Random random=Ps4StructureRandom.region(seed,originX,originZ,salt);Set<Long> seen=new HashSet<>();
  while(seen.size()<limit){int x=originX+random.nextInt(bound),z=originZ+random.nextInt(bound);if(!seen.add(ChunkPos.asLong(x,z)))continue;if(x>=min&&x<max&&z>=min&&z<max&&filter.test(x,z))return new ChunkPos(x,z);}
  return null;
 }
}

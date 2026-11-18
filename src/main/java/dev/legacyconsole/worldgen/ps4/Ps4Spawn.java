package dev.legacyconsole.worldgen.ps4;

import java.util.Random;
import java.util.Set;
import java.util.function.BiPredicate;
import dev.legacyconsole.worldgen.LegacyChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/** Java translation of PS4 ServerLevel::setInitialSpawn (0xcf98d0) and
 * BiomeSource::findBiome (0x8e130). See docs/PS4-MAPS-BOUNDS-SPAWN.md. */
public final class Ps4Spawn {
 private static final Set<Integer> BIOMES=Set.of(4,5,1,19,18,21,22,40,41);
 private Ps4Spawn(){}
 public static BlockPos find(ServerLevel level,LegacyChunkGenerator generator){
  int[] ids=generator.source().layers().quartArea(-64,-64,129,129);
  int[] pos=select(new Random(generator.seed()),generator.size().blocks,ids,(x,z)->validGround(level,x,z));
  return new BlockPos(pos[0],64,pos[1]);
 }
 /** The selected-count increment deliberately stays inside the replacement branch,
  * exactly as in the PS4 function; replacing it with a different reservoir changes seeds. */
 public static int[] select(Random random,int width,int[] biomes,BiPredicate<Integer,Integer> valid){
  if(biomes.length!=129*129)throw new IllegalArgumentException("Spawn search requires the 129x129 PS4 quart rectangle");
  int x=8,z=8,selected=0;
  for(int i=0;i<biomes.length;i++)if(BIOMES.contains(biomes[i])&&(selected==0||random.nextInt(selected+1)==0)){
   x=(-64+i%129)*4;z=(-64+i/129)*4;selected++;
  }
  int attempts=0,min=-width/2,max=width/2;
  while(!valid.test(x,z)){
   x+=random.nextInt(64)-random.nextInt(64);if(x>=max||x<min)x=0;
   z+=random.nextInt(64)-random.nextInt(64);if(z>=max||z<min)z=0;
   if(++attempts==1000)break;
  }
  return new int[]{x,z};
 }
 /** PS4 Dimension::isValidSpawn (0x1fdbf0) / Level::getTopTile (0x3eeb70). */
 private static boolean validGround(ServerLevel level,int x,int z){
  BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos(x,level.getSeaLevel(),z);
  while(pos.getY()<level.getMaxBuildHeight()-1&&!level.isEmptyBlock(pos.above()))pos.move(0,1,0);
  return level.getBlockState(pos).is(Blocks.GRASS_BLOCK);
 }
}

package dev.legacyconsole.worldgen.ps4.structure;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
/** PS4 buried chest: centre of the chunk, three to five blocks below the surface (0x138630). */
public final class Ps4TreasurePiece extends StructurePiece {
 private boolean placed;
 public Ps4TreasurePiece(ChunkPos chunk){super(Ps4PieceTypes.TREASURE,0,new BoundingBox(chunk.getMinBlockX()+8,0,chunk.getMinBlockZ()+8,chunk.getMinBlockX()+9,255,chunk.getMinBlockZ()+9));}
 public Ps4TreasurePiece(ServerLevel level,CompoundTag tag){super(Ps4PieceTypes.TREASURE,tag);placed=tag.getBoolean("Placed");}
 @Override protected void addAdditionalSaveData(ServerLevel level,CompoundTag tag){tag.putBoolean("Placed",placed);}
 @Override public boolean postProcess(WorldGenLevel level,StructureFeatureManager manager,ChunkGenerator generator,Random random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
  if(placed)return true;int x=boundingBox.minX(),z=boundingBox.minZ(),y=level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,x,z)-3-random.nextInt(3);BlockPos pos=new BlockPos(x,Math.max(2,y),z);while(pos.getY()>2&&level.isEmptyBlock(pos.below()))pos=pos.below();if(clip.isInside(pos))placed=createChest(level,clip,random,pos,BuiltInLootTables.BURIED_TREASURE,null);return true;
 }
}

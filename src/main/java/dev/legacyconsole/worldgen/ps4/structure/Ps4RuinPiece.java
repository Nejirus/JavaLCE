package dev.legacyconsole.worldgen.ps4.structure;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;

/** PS4 OceanRuin 0x4f8020/0x4f86f0/0x4f8d10 and the original template-index tables. */
public final class Ps4RuinPiece extends StructurePiece {
 private final boolean large,warm;private final Rotation rotation;
 public Ps4RuinPiece(int x,int z,boolean large,boolean warm,Rotation rotation){super(Ps4PieceTypes.RUIN,0,new BoundingBox(x,0,z,x+(large?15:6),255,z+(large?15:6)));this.large=large;this.warm=warm;this.rotation=rotation;}
 public Ps4RuinPiece(ServerLevel level,CompoundTag tag){super(Ps4PieceTypes.RUIN,tag);large=tag.getBoolean("Large");warm=tag.getBoolean("Warm");rotation=Rotation.valueOf(tag.getString("Ps4Rotation"));}
 @Override protected void addAdditionalSaveData(ServerLevel level,CompoundTag tag){tag.putBoolean("Large",large);tag.putBoolean("Warm",warm);tag.putString("Ps4Rotation",rotation.name());}
 private static final int[][] COLD_SMALL={{153,156,159,162,165,168,171,174},{154,157,160,163,166,169,172,175},{155,158,161,164,167,170,173,176}};
 private static final int[][] COLD_LARGE={{134,135,136,137},{142,143,144,145},{138,139,140,141}};
 @Override public boolean postProcess(WorldGenLevel level,StructureFeatureManager manager,ChunkGenerator generator,Random random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){
  int size=large?16:7,x=boundingBox.minX(),z=boundingBox.minZ();if(x<clip.minX()||x>clip.maxX()||z<clip.minZ()||z>clip.maxZ())return true;
  int sum=0,count=0;for(int bx=x;bx<x+size;bx++)for(int bz=z;bz<z+size;bz++){sum+=Math.max(63,level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,bx,bz));count++;}int y=sum/count-2,min=512,deep=0;
  BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
  for(int bx=x;bx<x+size;bx++)for(int bz=z;bz<z+size;bz++){int bottom=y-1;while(bottom>1){var state=level.getBlockState(pos.set(bx,bottom,bz));if(!state.isAir()&&!state.getMaterial().isLiquid())break;bottom--;}min=Math.min(min,bottom);if(bottom<=y-4)deep++;}
  if(y-1-min>=3&&deep>size+4)y=min+3;
  for(int bx=x;bx<x+size;bx++)for(int bz=z;bz<z+size;bz++)for(int by=y-1;by>1;by--){pos.set(bx,by,bz);var state=level.getBlockState(pos);if(!state.isAir()&&!state.getMaterial().isLiquid())break;if(clip.isInside(pos)&&random.nextInt(100)<82)level.setBlock(pos,Blocks.SAND.defaultBlockState(),2);}
  BlockPos origin=new BlockPos(x+(rotation==Rotation.CLOCKWISE_90||rotation==Rotation.CLOCKWISE_180?size-1:0),y,z+(rotation==Rotation.COUNTERCLOCKWISE_90||rotation==Rotation.CLOCKWISE_180?size-1:0));
  if(warm){int id=(large?146:150)+random.nextInt(large?4:3);place(id,large?.9f:.8f,origin,level,manager,generator,random,clip,chunk,pivot);}
  else {int choice=random.nextInt(large?4:8);int[][] ids=large?COLD_LARGE:COLD_SMALL;for(int i=0;i<3;i++)place(ids[i][choice],i==0?(large?.9f:.8f):i==1?.7f:.5f,origin,level,manager,generator,random,clip,chunk,pivot);}
  return true;
 }
 private void place(int id,float integrity,BlockPos pos,WorldGenLevel level,StructureFeatureManager manager,ChunkGenerator generator,Random random,BoundingBox clip,ChunkPos chunk,BlockPos pivot){new Ps4TemplatePiece(Ps4Templates.template(id),pos,rotation,Mirror.NONE,0,"ps4:ruin/"+integrity,"").postProcess(level,manager,generator,random,clip,chunk,pivot);}
}

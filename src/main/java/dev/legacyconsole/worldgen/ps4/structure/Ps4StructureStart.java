package dev.legacyconsole.worldgen.ps4.structure;

import dev.legacyconsole.worldgen.LegacyChunkGenerator;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

/** Saves the console piece graph under the existing Minecraft feature key. */
public final class Ps4StructureStart extends StructureStart<NoneFeatureConfiguration> {
 private final Ps4Structures.Kind kind;
 private Ps4StructureStart(Ps4Structures.Kind kind,ChunkPos pos,int references,long seed){super((StructureFeature)kind.feature,pos,references,seed);this.kind=kind;}
 public static Ps4StructureStart generate(LegacyChunkGenerator generator,Ps4Structures.Kind kind,ChunkPos pos){
  var start=new Ps4StructureStart(kind,pos,0,generator.seed());start.build(generator,Ps4StructureRandom.start(generator.seed(),pos.x,pos.z));return start;
 }
 @Override public void generatePieces(RegistryAccess registry,ChunkGenerator generator,StructureManager templates,ChunkPos pos,Biome biome,NoneFeatureConfiguration config,LevelHeightAccessor height){}
 private void build(LegacyChunkGenerator generator,Random rng){
  ChunkPos pos=getChunkPos();int x=pos.getMinBlockX(),z=pos.getMinBlockZ();
  switch(kind){
   case VILLAGE -> pieces.addAll(Ps4Village.generate(generator,Ps4Structures.HEIGHT,rng,pos.x,pos.z));
   case MINESHAFT -> {
    // Eligibility consumes the same LargeFeature stream before the room constructor.
    rng.nextDouble();rng.nextInt(80);
    int biome=generator.source().layers().blockBiome(x+8,z+8);boolean mesa=dev.legacyconsole.worldgen.ps4.Ps4BiomeData.getBiome(biome).family==7;
    var root=new Ps4MineShaftPieces.MineShaftRoom(0,rng,x+2,z+2,mesa?MineshaftFeature.Type.MESA:MineshaftFeature.Type.NORMAL);
    addPiece(root);root.addChildren(root,this,rng);
    if(mesa){BoundingBox box=getBoundingBox();offsetPiecesVertically(63+5-box.maxY()+box.getYSpan()/2);}else moveBelowSeaLevel(63,0,rng,10);
   }
   case STRONGHOLD -> {synchronized(Ps4StrongholdPieces.class){try{Ps4StrongholdPieces.finiteWorld(generator.size().blocks);
    do{pieces.clear();Ps4StrongholdPieces.resetPieces();var root=new Ps4StrongholdPieces.StartPiece(rng,x+2,z+2);addPiece(root);root.addChildren(root,this,rng);
     while(!root.pendingChildren.isEmpty()){var child=root.pendingChildren.remove(rng.nextInt(root.pendingChildren.size()));child.addChildren(root,this,rng);}
     if(root.portalRoomPiece!=null)break;
    }while(true);
    moveBelowSeaLevel(63,0,rng,10);
   }finally{Ps4StrongholdPieces.clearFiniteWorld();}}}
   case DESERT_PYRAMID -> addPiece(new Ps4DesertPyramidPiece(rng,x,z));
   case JUNGLE_PYRAMID -> addPiece(new Ps4JunglePyramidPiece(rng,x,z));
   case SWAMP_HUT -> addPiece(new Ps4SwamplandHutPiece(rng,x,z));
   case MONUMENT -> {
    Random base=new Random(generator.seed());long a=base.nextLong(),b=base.nextLong();rng.setSeed((long)pos.x*a^(long)pos.z*b^generator.seed());
    addPiece(new Ps4OceanMonumentPieces.MonumentBuilding(rng,x+8-29,z+8-29,Direction.from2DDataValue(rng.nextInt(4))));
   }
   case MANSION -> {
    int initial=rng.nextInt(4);Rotation rotation=Rotation.values()[initial];int half=generator.size().blocks/32;
    for(int i=0;i<4;i++){Rotation trial=Rotation.values()[(initial+i)%4];boolean fits=true;for(int dx:new int[]{-7,0})for(int dz:new int[]{-3,3}){BlockPos corner=net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.transform(new BlockPos(dx,1,dz),Mirror.NONE,trial,BlockPos.ZERO).offset(pos.x,0,pos.z);if(corner.getX() < -half||corner.getX()>=half||corner.getZ() < -half||corner.getZ()>=half)fits=false;}if(fits){rotation=trial;break;}}
    int dx=rotation==Rotation.CLOCKWISE_90||rotation==Rotation.CLOCKWISE_180?2:12,dz=rotation==Rotation.NONE||rotation==Rotation.CLOCKWISE_90?12:2;
    int ground=Integer.MAX_VALUE;for(int bx:new int[]{7,dx})for(int bz:new int[]{7,dz})ground=Math.min(ground,generator.getBaseHeight(x+bx,z+bz,Heightmap.Types.OCEAN_FLOOR_WG,Ps4Structures.HEIGHT));
    if(ground<60)return;
    List<Ps4WoodlandMansionPieces.WoodlandMansionPiece> generated=new ArrayList<>();Ps4WoodlandMansionPieces.generateMansion(null,new BlockPos(x+8,ground+1,z+8),rotation,generated,rng);pieces.addAll(generated);
   }
   default -> pieces.addAll(Ps4TemplateStructures.generate(generator,kind,pos,rng));
  }
 }
 @Override public CompoundTag createTag(ServerLevel level,ChunkPos pos){CompoundTag tag=super.createTag(level,pos);if(isValid())tag.putBoolean("LegacyPS4",true);return tag;}
 public static StructureStart<?> load(ServerLevel level,CompoundTag tag,long seed){
  Ps4Structures.Kind kind=Ps4Structures.Kind.forFeature(Registry.STRUCTURE_FEATURE.get(new ResourceLocation(tag.getString("id"))));
  if(kind==null)throw new IllegalArgumentException("Unknown PS4 structure "+tag.getString("id"));
  var result=new Ps4StructureStart(kind,new ChunkPos(tag.getInt("ChunkX"),tag.getInt("ChunkZ")),tag.getInt("references"),seed);
  for(Tag value:tag.getList("Children",10)){CompoundTag child=(CompoundTag)value;StructurePieceType type=Registry.STRUCTURE_PIECE.get(new ResourceLocation(child.getString("id")));if(type==null)throw new IllegalArgumentException("Unknown PS4 piece "+child.getString("id"));result.addPiece(type.load(level,child));}
  if(level.getChunkSource().getGenerator() instanceof LegacyChunkGenerator generator)generator.ps4Structures().adopt(result);
  return result;
 }
 @Override public void placeInChunk(WorldGenLevel level,StructureFeatureManager manager,ChunkGenerator generator,Random random,BoundingBox clip,ChunkPos chunk){
  super.placeInChunk(level,manager,generator,random,clip,chunk);
  if(kind==Ps4Structures.Kind.MANSION&&isValid()){
   BoundingBox box=getBoundingBox();int floor=box.minY();BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
   for(int x=clip.minX();x<=clip.maxX();x++)for(int z=clip.minZ();z<=clip.maxZ();z++){
    pos.set(x,floor,z);if(!box.isInside(pos)||level.getBlockState(pos).isAir())continue;boolean part=false;for(var piece:pieces)if(piece.getBoundingBox().isInside(pos)){part=true;break;}if(!part)continue;
    for(int y=floor-1;y>1;y--){pos.setY(y);var state=level.getBlockState(pos);if(!state.isAir()&&!state.getMaterial().isLiquid())break;level.setBlock(pos,Blocks.COBBLESTONE.defaultBlockState(),2);}
   }
  }
 }
}

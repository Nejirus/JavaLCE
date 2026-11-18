package dev.legacyconsole.worldgen.ps4.structure;

import dev.legacyconsole.worldgen.LegacyChunkGenerator;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;

/** PS4 template structure assembly; original template IDs and random call order. */
final class Ps4TemplateStructures {
 private Ps4TemplateStructures(){}
 static List<StructurePiece> generate(LegacyChunkGenerator generator,Ps4Structures.Kind kind,ChunkPos chunk,Random random){
  List<StructurePiece> pieces=new ArrayList<>();
  switch(kind){case IGLOO->igloo(generator,chunk,random,pieces);case SHIPWRECK->shipwreck(generator,chunk,Ps4StructureRandom.region(generator.seed(),chunk.x,chunk.z,14357617),pieces);case OUTPOST->outpost(generator,chunk,random,pieces);case OCEAN_RUIN->ruin(generator,chunk,random,pieces);case TREASURE->pieces.add(new Ps4TreasurePiece(chunk));default->throw new IllegalArgumentException(kind.name());}return pieces;
 }
 private static int ground(LegacyChunkGenerator generator,int x,int z){return Math.max(63,generator.getBaseHeight(x,z,Heightmap.Types.WORLD_SURFACE_WG,Ps4Structures.HEIGHT));}
 private static Ps4TemplatePiece template(String name,BlockPos pos,Rotation rotation,String processor){return new Ps4TemplatePiece(name,pos,rotation,Mirror.NONE,0,processor,"");}
 private static BlockPos rotate(BlockPos pos,Rotation rotation){return StructureTemplate.transform(pos,Mirror.NONE,rotation,BlockPos.ZERO);}
 private static void igloo(LegacyChunkGenerator generator,ChunkPos chunk,Random random,List<StructurePiece> pieces){
  Rotation rotation=Rotation.values()[random.nextInt(4)];boolean basement=random.nextDouble()<.5;int sections=random.nextInt(8)+4;
  BlockPos horizontal=new BlockPos(chunk.getMinBlockX()+2,0,chunk.getMinBlockZ()+2);var box=Ps4Templates.asset("igloo/igloo_top").template().getBoundingBox(new StructurePlaceSettings().setRotation(rotation),horizontal);
  int sum=0,count=0;for(int x=box.minX();x<=box.maxX();x++)for(int z=box.minZ();z<=box.maxZ();z++){sum+=ground(generator,x,z);count++;}BlockPos top=horizontal.atY(sum/count-1);
  pieces.add(template("igloo/igloo_top",top,rotation,basement?"":"ps4:igloo_closed"));
  if(basement){for(int i=0;i<sections;i++)pieces.add(template("igloo/igloo_middle",top.offset(rotate(new BlockPos(2,-3-i*3,4),rotation)),rotation,""));pieces.add(template("igloo/igloo_bottom",top.offset(rotate(new BlockPos(0,-sections*3-6,-2),rotation)),rotation,""));}
 }
 private static final int[] BEACHED={0,4,5,6,7,8,9,10,14,15,16,17,18,19};
 private static void shipwreck(LegacyChunkGenerator generator,ChunkPos chunk,Random random,List<StructurePiece> pieces){
  int palette=random.nextInt(9),main=random.nextInt(20),beached=BEACHED[random.nextInt(14)];Rotation rotation=Rotation.values()[random.nextInt(4)];
  var a=Ps4Templates.asset(Ps4Templates.template(114+main)).template().getSize();var b=Ps4Templates.asset(Ps4Templates.template(114+beached)).template().getSize();int extent=Math.max(Math.max(a.getX(),a.getZ()),Math.max(b.getX(),b.getZ()));
  int x=chunk.getMinBlockX()-2,z=chunk.getMinBlockZ()-2,biome=generator.source().layers().blockBiome(x,z);boolean beach=biome==16||biome==26;
  BlockPos origin=new BlockPos(x+(rotation==Rotation.CLOCKWISE_90||rotation==Rotation.CLOCKWISE_180?extent-1:0),0,z+(rotation==Rotation.COUNTERCLOCKWISE_90||rotation==Rotation.CLOCKWISE_180?extent-1:0));
  var piece=template(Ps4Templates.template(114+(beach?beached:main)),origin,rotation,"ps4:shipwreck/"+palette);
  // Keep the PS4 four-point minimum and sinking depths. The target template is
  // already rotated/translated: sample its real footprint, rather than rounding
  // the pre-translation -2 origin into the previous chunk (which can be a cliff).
  var box=piece.getBoundingBox();int height=Integer.MAX_VALUE;
  for(int dx:new int[]{7,12})for(int dz:new int[]{7,12})height=Math.min(height,generator.getBaseHeight(Math.min(box.minX()+dx,box.maxX()),Math.min(box.minZ()+dz,box.maxZ()),Heightmap.Types.OCEAN_FLOOR_WG,Ps4Structures.HEIGHT));
  boolean inverted=main>0&&main<14&&((0x1c07>>(main-1))&1)!=0;
  piece.move(0,height-(beach||inverted?4:2),0);pieces.add(piece);
 }
 private static int outpostGround(LegacyChunkGenerator generator,BoundingBox box,boolean accessory){
  int x=box.getCenter().getX(),z=box.getCenter().getZ(),dx=box.getXSpan()/2,dz=box.getZSpan()/2;
  int[] heights={ground(generator,x,z),ground(generator,x+dx,z),ground(generator,x-dx,z),ground(generator,x,z+dz),ground(generator,x,z-dz)};int min=Arrays.stream(heights).min().orElse(63),max=Arrays.stream(heights).max().orElse(63);
  return accessory&&max-min>=9?Integer.MIN_VALUE:max-min<9?(min+max)/2:max-4;
 }
 private static void outpost(LegacyChunkGenerator generator,ChunkPos chunk,Random random,List<StructurePiece> pieces){
  Rotation rotation=Rotation.values()[random.nextInt(4)];BlockPos origin=new BlockPos(chunk.getMinBlockX(),0,chunk.getMinBlockZ());var tower=template("pillageroutpost/watchtower",origin,rotation,"ps4:outpost");int y=outpostGround(generator,tower.getBoundingBox(),false);tower.move(0,y,0);pieces.add(tower);pieces.add(template("pillageroutpost/watchtower_overgrown",origin.atY(y),rotation,"ps4:integrity/0.05"));
  int centerX=tower.getBoundingBox().minX()+8,centerZ=tower.getBoundingBox().minZ()+8;List<BlockPos> candidates=new ArrayList<>();
  for(int[] offset:new int[][]{{16,16},{-16,16},{16,-16},{-16,-16}})candidates.add(new BlockPos(centerX+offset[0]+random.nextInt(9)-4,0,centerZ+offset[1]+random.nextInt(9)-4));
  String[] names={"feature_cage1","feature_cage2","feature_logs","feature_targets","feature_tent1","feature_tent2"};
  for(var pos:candidates)if(random.nextFloat()<.9f){var rot=Rotation.values()[random.nextInt(4)];var piece=template("pillageroutpost/"+names[random.nextInt(6)],pos,rot,"ps4:outpost");int height=outpostGround(generator,piece.getBoundingBox(),true);if(height!=Integer.MIN_VALUE){piece.move(0,height,0);pieces.add(piece);}}
 }
 private static int between(Random random,int min,int max){return min+random.nextInt(max-min+1);}
 private static void ruin(LegacyChunkGenerator generator,ChunkPos chunk,Random random,List<StructurePiece> pieces){
  int x=chunk.getMinBlockX(),z=chunk.getMinBlockZ(),biome=generator.source().layers().blockBiome(x,z);boolean warm=biome==42||biome==43||biome==44||biome==45;boolean large=random.nextFloat()<=.3f;
  var root=new Ps4RuinPiece(x,z,large,warm,Rotation.values()[random.nextInt(4)]);pieces.add(root);
  if(large&&random.nextFloat()<=.9f){
   List<BlockPos> candidates=new ArrayList<>();candidates.add(new BlockPos(x-16+between(random,1,8),0,z+16+between(random,1,7)));candidates.add(new BlockPos(x-16+between(random,1,8),0,z+between(random,1,7)));candidates.add(new BlockPos(x-16+between(random,1,8),0,z-16+between(random,4,8)));candidates.add(new BlockPos(x+between(random,1,7),0,z+16+between(random,1,7)));candidates.add(new BlockPos(x+between(random,1,7),0,z-16+between(random,4,6)));candidates.add(new BlockPos(x+16+between(random,1,7),0,z+16+between(random,3,8)));candidates.add(new BlockPos(x+16+between(random,1,7),0,z+between(random,1,7)));candidates.add(new BlockPos(x+16+between(random,1,7),0,z-16+between(random,4,8)));
   int count=between(random,4,8);for(int i=0;i<count&&!candidates.isEmpty();i++){int selected=random.nextInt(candidates.size());BlockPos pos=candidates.get(selected);candidates.set(selected,candidates.get(candidates.size()-1));candidates.remove(candidates.size()-1);BoundingBox box=new BoundingBox(pos.getX(),0,pos.getZ(),pos.getX()+6,6,pos.getZ()+6);if(box.intersects(root.getBoundingBox()))continue;if(!new Ps4StructurePlacement(generator).viable(pos.getX()>>4,pos.getZ()>>4,7,Ps4StructurePlacement.OCEAN))continue;pieces.add(new Ps4RuinPiece(pos.getX(),pos.getZ(),false,warm,Rotation.values()[random.nextInt(4)]));}
  }
 }
}

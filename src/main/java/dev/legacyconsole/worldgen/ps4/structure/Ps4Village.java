package dev.legacyconsole.worldgen.ps4.structure;

import dev.legacyconsole.worldgen.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.*;

/** PS4 0x754540 / 0x5579a0 / 0x558130. Depth-first expansion and 2D occupancy. */
public final class Ps4Village {
 private static final Set<Integer> EXCLUDED=Set.of(3,34,131,162,48,49,0,24,44,45,42,43,46,47,13);
 private final LegacyChunkGenerator generator;private final LevelHeightAccessor height;private final Random random;
 private final List<Ps4TemplatePiece> pieces=new ArrayList<>();
 private final Map<Ps4TemplatePiece,Ps4Templates.Element> elements=new IdentityHashMap<>();
 private Ps4Village(LegacyChunkGenerator generator,LevelHeightAccessor height,Random random){this.generator=generator;this.height=height;this.random=random;}
 public static List<Ps4TemplatePiece> generate(LegacyChunkGenerator generator,LevelHeightAccessor height,Random random,int chunkX,int chunkZ){return new Ps4Village(generator,height,random).start(chunkX,chunkZ);}
 private int ground(int x,int z){return Math.max(63,generator.getBaseHeight(x,z,Heightmap.Types.WORLD_SURFACE_WG,height));}
 private List<Ps4TemplatePiece> start(int cx,int cz){
  int biome=generator.source().layers().blockBiome(cx*16+8,cz*16+8);int family=dev.legacyconsole.worldgen.ps4.Ps4BiomeData.getBiome(biome).family;
  String style=biome==2?"desert":biome==35?"savanna":biome==12?"snowy":Set.of(5,19,30,31).contains(biome)?"taiga":"plains";
  Rotation rotation=Rotation.values()[random.nextInt(4)];var pool=Ps4Templates.pool("minecraft:village/"+style+"/town_centers");if(pool==null||pool.weighted().isEmpty())return pieces;
  var element=pool.weighted().get(random.nextInt(pool.weighted().size()));BlockPos origin=new BlockPos(cx*16+8,0,cz*16+8);BoundingBox box=Ps4Templates.box(element,origin,rotation);
  int centerX=box.getCenter().getX(),centerZ=box.getCenter().getZ();long sum=0;int count=0;
  int minX=Math.max(0,(centerX&15)-1),maxX=Math.min(14,(centerX&15)+1),minZ=Math.max(0,(centerZ&15)-1),maxZ=Math.min(14,(centerZ&15)+1);
  for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++){int value=generator.getBaseHeight((centerX&~15)+x,(centerZ&~15)+z,Heightmap.Types.WORLD_SURFACE_WG,height);if(value>0){sum+=value;count++;}}
  int y=Math.max(63,count==0?63:(int)(sum/count))-box.minY()-1;origin=origin.offset(0,y,0);
  Ps4TemplatePiece first=piece(element,origin,rotation);pieces.add(first);elements.put(first,element);expand(first,0,6);return List.copyOf(pieces);
 }
 private Ps4TemplatePiece piece(Ps4Templates.Element element,BlockPos pos,Rotation rotation){return new Ps4TemplatePiece(element.template(),pos,rotation,Mirror.NONE,element.projection(),element.processor(),element.feature()?element.id():"");}
 private void expand(Ps4TemplatePiece parent,int depth,int maxDepth){
  if(depth>maxDepth)return;var element=elements.get(parent);var joints=Ps4Templates.joints(element,parent.position(),parent.getRotation());Ps4StructureRandom.shuffle(joints,random);List<BoundingBox> internal=new ArrayList<>();
  for(var joint:joints){var pool=Ps4Templates.pool(joint.pool());if(pool==null)continue;var fallback=Ps4Templates.pool(pool.fallback());
   if(depth!=maxDepth&&tryPool(parent,joint,pool,internal,depth,maxDepth))continue;
   if(fallback!=null)tryPool(parent,joint,fallback,internal,depth,maxDepth);
  }
 }
 private boolean tryPool(Ps4TemplatePiece parent,Ps4Templates.Joint joint,Ps4Templates.Pool pool,List<BoundingBox> internal,int depth,int maxDepth){
  List<Ps4Templates.Element> candidates=new ArrayList<>(pool.weighted());Ps4StructureRandom.shuffle(candidates,random);
  BoundingBox parentBox=parent.getBoundingBox();BlockPos target=joint.pos().relative(joint.facing());boolean inside=parentBox.isInside(target);int ground=ground(joint.pos().getX(),joint.pos().getZ());
  for(var element:candidates){if(element.empty())return true;
   List<Rotation> rotations=new ArrayList<>(Arrays.asList(Rotation.values()));Ps4StructureRandom.shuffle(rotations,random);
   for(var rotation:rotations){var childJoints=Ps4Templates.joints(element,BlockPos.ZERO,rotation);Ps4StructureRandom.shuffle(childJoints,random);
    for(var child:childJoints){if(joint.facing()!=child.facing().getOpposite()||!joint.attachment().equals(child.attachment()))continue;
     BlockPos pos=target.subtract(child.pos());BoundingBox box=Ps4Templates.box(element,pos,rotation);
     int delta=joint.pos().getY()-parentBox.minY()-child.pos().getY()+joint.facing().getStepY();
     int desired=parent.projection()==0&&element.projection()==0?parentBox.minY()+delta:ground-1+delta;
     pos=pos.offset(0,desired-box.minY(),0);box=Ps4Templates.box(element,pos,rotation);
     if(!within(box)||excludedCorners(box))continue;
     if(inside){if(box.minX()<parentBox.minX()||box.maxX()>parentBox.maxX()||box.minZ()<parentBox.minZ()||box.maxZ()>parentBox.maxZ()||overlap(box,internal))continue;internal.add(box);}
     else {boolean collision=false;for(var old:pieces)if(intersects(box,old.getBoundingBox())){collision=true;break;}if(collision)continue;}
     var placed=piece(element,pos,rotation);pieces.add(placed);elements.put(placed,element);expand(placed,depth+1,maxDepth);return true;
    }
   }
  }return false;
 }
 private boolean excludedCorners(BoundingBox box){for(int x:new int[]{box.minX(),box.maxX()})for(int z:new int[]{box.minZ(),box.maxZ()})if(EXCLUDED.contains(generator.source().layers().blockBiome(x,z)))return true;return false;}
 private boolean within(BoundingBox box){int min=-generator.size().blocks/2,max=generator.size().blocks/2-1;return box.minX()>=min&&box.maxX()<=max&&box.minZ()>=min&&box.maxZ()<=max;}
 private static boolean intersects(BoundingBox a,BoundingBox b){return a.intersects(b.minX(),b.minZ(),b.maxX(),b.maxZ());}
 private static boolean overlap(BoundingBox box,List<BoundingBox> others){for(var other:others)if(intersects(box,other))return true;return false;}
}

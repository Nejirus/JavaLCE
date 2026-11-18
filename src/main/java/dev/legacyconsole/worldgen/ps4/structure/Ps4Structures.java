package dev.legacyconsole.worldgen.ps4.structure;

import com.google.gson.*;
import dev.legacyconsole.worldgen.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.*;

/** Console feature dispatch. Java's biome-configured structure generators are not invoked. */
public final class Ps4Structures {
 public static final LevelHeightAccessor HEIGHT=new LevelHeightAccessor(){public int getMinBuildHeight(){return 0;}public int getHeight(){return 256;}};
 public enum Kind {
  MINESHAFT(StructureFeature.MINESHAFT),VILLAGE(StructureFeature.VILLAGE),STRONGHOLD(StructureFeature.STRONGHOLD),
  DESERT_PYRAMID(StructureFeature.DESERT_PYRAMID),JUNGLE_PYRAMID(StructureFeature.JUNGLE_TEMPLE),SWAMP_HUT(StructureFeature.SWAMP_HUT),IGLOO(StructureFeature.IGLOO),
  MONUMENT(StructureFeature.OCEAN_MONUMENT),MANSION(StructureFeature.WOODLAND_MANSION),SHIPWRECK(StructureFeature.SHIPWRECK),OCEAN_RUIN(StructureFeature.OCEAN_RUIN),TREASURE(StructureFeature.BURIED_TREASURE),OUTPOST(StructureFeature.PILLAGER_OUTPOST);
  public final StructureFeature<?> feature;Kind(StructureFeature<?> feature){this.feature=feature;}
  public static Kind forFeature(StructureFeature<?> feature){for(var kind:values())if(kind.feature==feature)return kind;return null;}
 }
 private record Filter(Set<Integer> primary,int radius,Set<Integer> secondary,int secondRadius){}
 private static final Map<String,Filter> FILTERS=filters();
 private static Map<String,Filter> filters(){
  try(var in=Objects.requireNonNull(Ps4Structures.class.getResourceAsStream(Ps4Templates.ROOT+"structure-filters.json"))){var json=new JsonParser().parse(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();Map<String,Filter> result=new HashMap<>();
   for(var entry:json.entrySet()){var obj=entry.getValue().getAsJsonObject();Set<Integer> a=new HashSet<>(),b=new HashSet<>();for(var id:obj.getAsJsonArray("primary"))a.add(id.getAsInt());for(var id:obj.getAsJsonArray("secondary"))b.add(id.getAsInt());result.put(entry.getKey(),new Filter(Set.copyOf(a),obj.get("primary_radius").getAsInt(),Set.copyOf(b),obj.get("secondary_radius").getAsInt()));}return Map.copyOf(result);
  }catch(IOException error){throw new UncheckedIOException(error);}
 }
 private final LegacyChunkGenerator generator;
 private final Ps4StructurePlacement placement;
 private final Map<Kind,Map<Long,StructureStart<?>>> plans=new ConcurrentHashMap<>();
 public Ps4Structures(LegacyChunkGenerator generator){this.generator=generator;placement=new Ps4StructurePlacement(generator);}
 private boolean filter(String name,int x,int z){Filter f=FILTERS.get(name);int radius=f.radius,second=f.secondRadius;if(name.equals("treasure")){radius=generator.size()==Ps4WorldSize.MEDIUM?2:generator.size()==Ps4WorldSize.LARGE?4:0;second=generator.size()==Ps4WorldSize.MEDIUM?24:generator.size()==Ps4WorldSize.LARGE?32:16;}return placement.viable(x,z,radius,f.primary)&&placement.viable(x,z,second,f.secondary);}
 public boolean eligible(Kind kind,int x,int z){
  if(!generator.structures()||!placement.within(x,z,0))return false;
  return switch(kind){
   case STRONGHOLD -> generator.hasStronghold(new ChunkPos(x,z));
   case MINESHAFT -> {var random=Ps4StructureRandom.start(generator.seed(),x,z);yield random.nextDouble()<0.004&&random.nextInt(80)<Math.max(Math.abs(x),Math.abs(z));}
   case VILLAGE -> placement.village(x,z);
   case DESERT_PYRAMID,JUNGLE_PYRAMID,SWAMP_HUT,IGLOO -> placement.scattered(x,z)&&scatteredKind(x,z)==kind;
   case MONUMENT -> new ChunkPos(x,z).equals(placement.finite("monument",x,z,40,32,5,10387313,(a,b)->filter("monument",a,b)));
   case MANSION -> new ChunkPos(x,z).equals(placement.finite("mansion",x,z,40,80,6,10387319,(a,b)->filter("mansion",a,b)));
   case OUTPOST -> new ChunkPos(x,z).equals(placement.finite("outpost",x,z,40,50,6,165745296,(a,b)->placement.viable(a,b,4,Ps4StructurePlacement.VILLAGE)&&placement.viable(a,b,32,Ps4StructurePlacement.VILLAGE)));
   case TREASURE -> new ChunkPos(x,z).equals(placement.finite("treasure",x,z,40,4,2,16842397,(a,b)->filter("treasure",a,b)));
   case SHIPWRECK -> {boolean candidate=new ChunkPos(x,z).equals(placement.finite("shipwreck",x,z,20,10,5,14357617,(a,b)->placement.viable(a,b,5,Ps4StructurePlacement.COAST)&&placement.viable(a,b,10,Ps4StructurePlacement.COAST)));if(candidate)for(int a=x-5;a<=x+4&&candidate;a++)for(int b=z-5;b<=z+4;b++)if(eligible(Kind.MONUMENT,a,b)){candidate=false;break;}yield candidate;}
   case OCEAN_RUIN -> placement.grid(x,z,32,5,14357617).equals(new ChunkPos(x,z))&&placement.viable(x,z,8,Ps4StructurePlacement.OCEAN);
  };
 }
 private Kind scatteredKind(int x,int z){return switch(generator.source().layers().blockBiome(x*16+8,z*16+8)){case 2,17->Kind.DESERT_PYRAMID;case 21,22->Kind.JUNGLE_PYRAMID;case 6->Kind.SWAMP_HUT;case 12,30->Kind.IGLOO;default->null;};}
 public StructureStart<?> plan(Kind kind,int x,int z){
  return plans.computeIfAbsent(kind,k->new ConcurrentHashMap<>()).computeIfAbsent(ChunkPos.asLong(x,z),k->eligible(kind,x,z)?Ps4StructureStart.generate(generator,kind,new ChunkPos(x,z)):StructureStart.INVALID_START);
 }
 public void adopt(Ps4StructureStart loaded){Kind kind=Kind.forFeature(loaded.getFeature());plans.computeIfAbsent(kind,k->new ConcurrentHashMap<>()).put(loaded.getChunkPos().toLong(),loaded);}
 public void createStructures(StructureFeatureManager manager,ChunkAccess chunk){
  if(!generator.structures())return;ChunkPos pos=chunk.getPos();for(Kind kind:Kind.values()){var start=plan(kind,pos.x,pos.z);if(start.isValid())manager.setStartForFeature(SectionPos.bottomOf(chunk),kind.feature,start,chunk);}
 }
 /** Bounding search distance derived from eight connected templates (root plus depth-six fallback). */
 public static int villageReach(){return VillageReach.VALUE;}
 private static final class VillageReach {private static final int VALUE=calculate();private static int calculate(){int max=1;for(var element:Ps4Templates.elements())if(!element.template().isEmpty()){var size=Ps4Templates.asset(element.template()).template().getSize();max=Math.max(max,Math.max(size.getX(),size.getZ()));}return (8*max+31)/16;}}
 public List<StructureStart<?>> villagesNear(int x,int z){
  List<StructureStart<?>> result=new ArrayList<>();if(!generator.structures())return result;int radius=villageReach(),spacing=generator.size().blocks/16<128?16:22;
  for(int a=Math.floorDiv(x-radius,spacing);a<=Math.floorDiv(x+radius,spacing);a++)for(int b=Math.floorDiv(z-radius,spacing);b<=Math.floorDiv(z+radius,spacing);b++){
   ChunkPos pos=placement.grid(a*spacing,b*spacing,spacing,7,10387312);var start=plan(Kind.VILLAGE,pos.x,pos.z);if(start.isValid()&&start.getBoundingBox().intersects(x*16,z*16,x*16+15,z*16+15))result.add(start);
  }return result;
 }
 public void addDistantVillageReferences(StructureFeatureManager manager,ChunkAccess chunk){for(var start:villagesNear(chunk.getPos().x,chunk.getPos().z))manager.addReferenceForFeature(SectionPos.bottomOf(chunk),Kind.VILLAGE.feature,start.getChunkPos().toLong(),chunk);}
 public BlockPos locate(ServerLevel level,StructureFeature<?> feature,BlockPos origin,int radius,boolean skipKnown){
  Kind kind=Kind.forFeature(feature);if(kind==null||!generator.structures())return null;
  int half=generator.size().blocks/32;List<ChunkPos> candidates=new ArrayList<>();
  for(int x=-half;x<half;x++)for(int z=-half;z<half;z++)if(eligible(kind,x,z))candidates.add(new ChunkPos(x,z));
  candidates.sort(Comparator.comparingDouble(pos->pos.getMiddleBlockPosition(64).distSqr(origin)));
  for(var pos:candidates){var start=plan(kind,pos.x,pos.z);if(!start.isValid())continue;if(skipKnown){var loaded=level.getChunk(pos.x,pos.z,ChunkStatus.STRUCTURE_STARTS).getStartForFeature(feature);if(loaded==null||!loaded.canBeReferenced())continue;loaded.addReference();}return pos.getMiddleBlockPosition(64);}
  return null;
 }
}

package dev.legacyconsole.worldgen;

import com.google.gson.*;
import dev.legacyconsole.worldgen.ps4.structure.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.Heightmap;
import org.junit.*;
import static org.junit.Assert.*;

public class Ps4StructuresTest {
 @BeforeClass public static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();Ps4PieceTypes.initialize();}
 private JsonElement json(String path)throws IOException {try(var in=Objects.requireNonNull(getClass().getResourceAsStream(path))){return new JsonParser().parse(new InputStreamReader(in,StandardCharsets.UTF_8));}}
 @Test public void finiteCandidatesMatchOriginalPs4RoutineIncludingNegativeCellsAndRejectedSamples()throws Exception {
  for(var value:json("/worldgen/ps4-structure-placement-vectors.json").getAsJsonObject().getAsJsonArray("cases")){var c=value.getAsJsonObject();int filter=c.get("filter").getAsInt();var origin=c.getAsJsonArray("origin");
   var result=Ps4StructurePlacement.candidate(c.get("seed").getAsLong(),c.get("worldChunks").getAsInt(),origin.get(0).getAsInt(),origin.get(1).getAsInt(),c.get("spacing").getAsInt(),c.get("separation").getAsInt(),c.get("attempts").getAsInt(),c.get("salt").getAsLong(),(x,z)->filter==0||filter==1&&Math.floorMod(x*x+z*3,7)==0||filter==2&&x<0&&z>=0);
   var matches=c.getAsJsonArray("matches");assertTrue(matches.size()<=1);if(matches.size()==0)assertNull(c.toString(),result);else assertEquals(c.toString(),new ChunkPos(matches.get(0).getAsJsonArray().get(0).getAsInt(),matches.get(0).getAsJsonArray().get(1).getAsInt()),result);
  }
 }
 @Test public void everyShippedTemplateRetainsOriginalBytesAndLoadsAllBlockStates()throws Exception {
  var entries=json(Ps4Templates.ROOT+"templates.json").getAsJsonArray();assertEquals(450,entries.size());
  for(var value:entries){var entry=value.getAsJsonObject();String name=entry.get("path").getAsString();byte[] raw;try(var in=new java.util.zip.GZIPInputStream(Objects.requireNonNull(getClass().getResourceAsStream(Ps4Templates.ROOT+"templates/"+name+".nbt")))){raw=in.readAllBytes();}
   assertEquals(name,entry.get("source_sha256").getAsString(),hex(MessageDigest.getInstance("SHA-256").digest(raw)));
   CompoundTag original=NbtIo.read(new java.io.DataInputStream(new ByteArrayInputStream(raw)));
   for(Tag b:original.getList("blocks",10)){var nbt=((CompoundTag)b).getCompound("nbt");if(nbt.contains("final_state")){
    String converted=Ps4TemplateBridge.finalState(nbt.getString("final_state"));
    new net.minecraft.commands.arguments.blocks.BlockStateParser(new com.mojang.brigadier.StringReader(converted),false).parse(false);
   }}
   var asset=Ps4Templates.asset(name);assertTrue(name,asset.template().getSize().getX()>0);assertTrue(name,asset.template().getSize().getZ()>0);
  }
 }
 @Test public void jigsawReplacementsConvertOriginalWallPropertiesAndTruncatedRoadName(){
  assertEquals("minecraft:cobblestone_wall[east=none,north=none,south=none,up=true,waterlogged=false]",Ps4TemplateBridge.finalState("minecraft:cobblestone_wall[east=false,north=false,south=false,up=true,waterlogged=false]"));
  assertEquals("minecraft:dirt_path",Ps4TemplateBridge.finalState("minecraft:grass_"));
  assertEquals("minecraft:dirt_path",Ps4TemplateBridge.finalState("minecraft:grass_path "));
 }
 private static String hex(byte[] bytes){StringBuilder result=new StringBuilder();for(byte b:bytes)result.append(String.format("%02x",b&255));return result.toString();}
 @Test public void savedPiecesUseOurTypesAndPreserveTheirTemplateAndRotation()throws Exception {
  var piece=new Ps4TemplatePiece("village/plains/town_centers/plains_fountain_01",new BlockPos(-50,65,20),Rotation.CLOCKWISE_90,Mirror.NONE,0,"minecraft:cobblestone_building_low_decay","");
  CompoundTag tag=piece.createTag(null);assertEquals("legacyconsole:ps4_template",tag.getString("id"));var copy=new Ps4TemplatePiece(null,tag);assertEquals(piece.templateName(),copy.templateName());assertEquals(piece.position(),copy.position());assertEquals(piece.getRotation(),copy.getRotation());assertEquals(piece.getBoundingBox().toString(),copy.getBoundingBox().toString());
 }
 @Test public void capturedCropRulesProduceMatureCropsAndPathsBridgeWaterWithoutFillingAir(){
  assertEquals(17,Ps4VillageProcessors.count());
  var random=new Random(0);var wheat=Blocks.WHEAT.defaultBlockState();var carrot=Blocks.CARROTS.defaultBlockState();var air=Blocks.AIR.defaultBlockState();
  assertEquals(((CropBlock)Blocks.POTATOES).getStateForAge(7),Ps4VillageProcessors.process("minecraft:potato_potato_farm",wheat,air,random,false));
  assertEquals(((CropBlock)Blocks.POTATOES).getStateForAge(7),Ps4VillageProcessors.process("minecraft:potato_potato_farm",carrot,air,random,false));
  assertEquals(((CropBlock)Blocks.BEETROOTS).getStateForAge(3),Ps4VillageProcessors.process("minecraft:wheat_beet_farm",carrot,air,random,false));
  assertEquals(Blocks.OAK_PLANKS.defaultBlockState(),Ps4VillageProcessors.process("minecraft:plains_path_decay",Blocks.DIRT_PATH.defaultBlockState(),Blocks.WATER.defaultBlockState(),random,false));
  assertEquals(air,Ps4VillageProcessors.process("minecraft:plains_path_decay",Blocks.DIRT_PATH.defaultBlockState(),air,random,false));
  assertEquals(Blocks.SMOOTH_SANDSTONE.defaultBlockState(),Ps4VillageProcessors.process("minecraft:plains_path_decay",Blocks.DIRT_PATH.defaultBlockState(),Blocks.SAND.defaultBlockState(),random,true));
 }
 @Test public void consolePlansNeverUseJavaStructurePieceTypes(){
  var registry=RegistryAccess.builtin().registryOrThrow(Registry.BIOME_REGISTRY);var generator=new LegacyChunkGenerator(registry,123456789L,Ps4WorldSize.CLASSIC,true);int count=0;
  for(var kind:Ps4Structures.Kind.values())for(int x=-27;x<27;x++)for(int z=-27;z<27;z++)if(generator.ps4Structures().eligible(kind,x,z)){
   var plan=generator.ps4Structures().plan(kind,x,z);if(!plan.isValid())continue;count++;for(var piece:plan.getPieces())assertEquals(kind.name(),"legacyconsole",Registry.STRUCTURE_PIECE.getKey(piece.getType()).getNamespace());
  }assertTrue("Fixture must generate several console structures",count>3);
 }
}

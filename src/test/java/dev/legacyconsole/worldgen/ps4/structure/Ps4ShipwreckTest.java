package dev.legacyconsole.worldgen.ps4.structure;
import com.google.gson.*;
import dev.legacyconsole.worldgen.*;
import java.io.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import org.junit.*;
import static org.junit.Assert.*;
public class Ps4ShipwreckTest {
 @BeforeClass public static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();Ps4PieceTypes.initialize();}
 private LegacyChunkGenerator generator(long seed){return new LegacyChunkGenerator(RegistryAccess.builtin().registryOrThrow(Registry.BIOME_REGISTRY),seed,Ps4WorldSize.LARGE,true);}
 @Test public void shipVariantsWoodPalettesAndRotationsMatchOriginalPs4Constructor()throws Exception {
  try(var in=getClass().getResourceAsStream("/worldgen/ps4-shipwreck-choice-vectors.json")){
   for(var value:new JsonParser().parse(new InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("cases")){
    var c=value.getAsJsonObject();var chunk=c.getAsJsonArray("chunk");int x=chunk.get(0).getAsInt(),z=chunk.get(1).getAsInt();var generator=generator(c.get("seed").getAsLong());
    var piece=(Ps4TemplatePiece)Ps4TemplateStructures.generate(generator,Ps4Structures.Kind.SHIPWRECK,new ChunkPos(x,z),new Random(0)).get(0);
    int biome=generator.source().layers().blockBiome(x*16-2,z*16-2);boolean beach=biome==16||biome==26;
    assertEquals(c.toString(),Ps4Templates.template(114+c.get(beach?"beached":"main").getAsInt()),piece.templateName());
    assertEquals(c.toString(),Rotation.values()[c.get("rotation").getAsInt()],piece.getRotation());
    assertEquals(c.toString(),"ps4:shipwreck/"+c.get("palette").getAsInt(),piece.createTag(null).getString("Processor"));
   }
  }
 }
 @Test public void wubzyShipwrecksAtCliffEdgesDoNotFloatAboveTheOcean(){
  var generator=generator("WUBZY".hashCode());
  // These starts floated at Y=64..68 while the actual hull stood over sea floor.
  for(int[] chunk:new int[][]{{-110,-107},{-86,-97},{-76,102},{-60,4},{14,62},{20,110},{91,24},{94,-129},{123,12}}){
   var start=generator.ps4Structures().plan(Ps4Structures.Kind.SHIPWRECK,chunk[0],chunk[1]);assertTrue(Arrays.toString(chunk),start.isValid());
   assertTrue(Arrays.toString(chunk)+" "+start.getBoundingBox(),start.getBoundingBox().minY()<63);
  }
 }
}

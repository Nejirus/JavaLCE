package dev.legacyconsole.worldgen.ps4.structure;
import com.google.gson.*;
import java.io.*;
import java.util.Random;
import net.minecraft.core.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.junit.*;import static org.junit.Assert.*;
public class Ps4MineshaftGraphOracleTest {
 @BeforeClass public static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();Ps4PieceTypes.initialize();}
 private static class Plan extends StructureStart<NoneFeatureConfiguration>{
  Plan(){super((StructureFeature)StructureFeature.MINESHAFT,new ChunkPos(0,0),0,0);}
  @Override public void generatePieces(RegistryAccess a,ChunkGenerator b,StructureManager c,ChunkPos d,Biome e,NoneFeatureConfiguration f,LevelHeightAccessor g){}
  void below(Random random){moveBelowSeaLevel(63,0,random,10);}
  void mesa(){var box=getBoundingBox();offsetPiecesVertically(63+5-box.maxY()+box.getYSpan()/2);}
 }
 @Test public void completeRecursiveGraphsMatchOriginalPs4ForNormalAndMesaMineshafts()throws Exception {
  try(var in=getClass().getResourceAsStream("/worldgen/ps4-mineshaft-graph-vectors.json")){
   var cases=new JsonParser().parse(new InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("mineshafts");
   for(var value:cases){var c=value.getAsJsonObject();Random random=new Random(c.get("seed").getAsLong());var pos=c.getAsJsonArray("chunk");boolean mesa=c.get("mesa").getAsInt()==1;Plan plan=new Plan();var root=new Ps4MineShaftPieces.MineShaftRoom(0,random,pos.get(0).getAsInt()*16+2,pos.get(1).getAsInt()*16+2,mesa?MineshaftFeature.Type.MESA:MineshaftFeature.Type.NORMAL);plan.addPiece(root);root.addChildren(root,plan,random);if(mesa)plan.mesa();else plan.below(random);
    var expected=c.getAsJsonArray("pieces");assertEquals("seed "+c.get("seed"),expected.size(),plan.getPieces().size());
    for(int i=0;i<expected.size();i++){var e=expected.get(i).getAsJsonObject();var actual=plan.getPieces().get(i);assertEquals("piece "+i,"ps4_"+e.get("type").getAsString(),Registry.STRUCTURE_PIECE.getKey(actual.getType()).getPath());var box=actual.getBoundingBox();int[] axes={box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ()};for(int a=0;a<6;a++)assertEquals("seed "+c.get("seed")+" piece "+i+" axis "+a,e.getAsJsonArray("box").get(a).getAsInt(),axes[a]);}
    assertEquals("RNG state after complete graph",c.get("next32").getAsInt(),random.nextInt());
   }
  }
 }
}

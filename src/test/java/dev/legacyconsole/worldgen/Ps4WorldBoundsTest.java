package dev.legacyconsole.worldgen;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.border.WorldBorder;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4WorldBoundsTest {
 @Test public void chunkLimitsMatchPs4DimensionInstructions(){
  JsonObject data=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/ps4-map-bounds-vectors.json"),StandardCharsets.UTF_8)).getAsJsonObject();
  for(JsonElement element:data.getAsJsonArray("chunkBounds")){
   JsonObject v=element.getAsJsonObject();Ps4WorldSize size=java.util.Arrays.stream(Ps4WorldSize.values()).filter(s->s.blocks==v.get("width").getAsInt()).findFirst().orElseThrow();
   int x=v.get("chunkX").getAsInt(),z=v.get("chunkZ").getAsInt();boolean expected=v.get("inside").getAsBoolean();
   assertEquals(v.toString(),expected,size.containsChunk(x,z));assertEquals(expected,size.containsBlock(x*16,z*16));assertEquals(expected,size.containsBlock(x*16+15,z*16+15));
  }
 }
 @Test public void nativeBorderUsesTheConfirmedFirstAndLastBlocks(){
  for(Ps4WorldSize size:Ps4WorldSize.values()){
   WorldBorder border=new WorldBorder();border.setCenter(200,-400);border.setSize(50);
   LegacyWorldgen.applyBorder(border,size);
   assertEquals(size.minBlock(),border.getMinX(),0);assertEquals(size.minBlock(),border.getMinZ(),0);
   assertEquals(size.maxBlockExclusive(),border.getMaxX(),0);assertEquals(size.maxBlockExclusive(),border.getMaxZ(),0);
   assertTrue(border.isWithinBounds(new BlockPos(size.minBlock(),64,0)));assertTrue(border.isWithinBounds(new BlockPos(size.maxBlockExclusive()-1,64,0)));
   assertFalse(border.isWithinBounds(new BlockPos(size.minBlock()-1,64,0)));assertFalse(border.isWithinBounds(new BlockPos(size.maxBlockExclusive(),64,0)));
  }
 }
}

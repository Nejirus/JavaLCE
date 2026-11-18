package dev.legacyconsole.client;
import static org.junit.Assert.*;
import org.junit.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
public class BucketPromptTest {
 @BeforeClass public static void bootstrap(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
 @Test public void sourceWaterAndLavaAreCollectableButFlowingLevelsAreNot(){
  for(Block liquid:new Block[]{Blocks.WATER,Blocks.LAVA}) {
   assertTrue(LceBucketPrompts.canCollect(liquid.defaultBlockState()));
   assertFalse(LceBucketPrompts.canCollect(liquid.defaultBlockState().setValue(LiquidBlock.LEVEL,5)));
  }
 }
 @Test public void nativeWaterloggedBlocksAndPowderSnowUseReadOnlyPickupEligibility(){
  var dry=Blocks.OAK_STAIRS.defaultBlockState();var wet=dry.setValue(BlockStateProperties.WATERLOGGED,true);
  assertFalse(LceBucketPrompts.canCollect(dry));assertTrue(LceBucketPrompts.canCollect(wet));
  assertTrue(LceBucketPrompts.canCollect(Blocks.POWDER_SNOW.defaultBlockState()));
  assertFalse(LceBucketPrompts.canCollect(Blocks.STONE.defaultBlockState()));
 }
}

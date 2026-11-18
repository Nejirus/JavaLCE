package dev.legacyconsole.client;

import net.minecraft.core.Direction;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import org.junit.*;
import static org.junit.Assert.*;

public class CampfirePromptTest {
 @BeforeClass public static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
 @Test public void onlyLitCampfiresAndReachableFacesOfferShovelUse(){
  for(var block:new CampfireBlock[]{(CampfireBlock)Blocks.CAMPFIRE,(CampfireBlock)Blocks.SOUL_CAMPFIRE})for(boolean lit:new boolean[]{false,true})for(var face:Direction.values()){
   var state=block.defaultBlockState().setValue(CampfireBlock.LIT,lit);
   assertEquals(lit&&face!=Direction.DOWN,LceActionPrompts.shovelUse(state,face,new ItemStack(Items.DIAMOND_SHOVEL)));
   assertFalse(LceActionPrompts.shovelUse(state,face,new ItemStack(Items.DIAMOND_PICKAXE)));
   assertFalse(LceActionPrompts.shovelUse(state,face,ItemStack.EMPTY));
  }
  assertFalse(LceActionPrompts.shovelUse(Blocks.STONE.defaultBlockState(),Direction.UP,new ItemStack(Items.IRON_SHOVEL)));
 }
}

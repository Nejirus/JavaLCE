package dev.legacyconsole.crafting;

import static org.junit.Assert.*;
import org.junit.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.inventory.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.TextComponent;

public class BedDyeRecipeTest {
  @BeforeClass public static void bootstrap(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
  private CraftingContainer grid() {
    return new CraftingContainer(new AbstractContainerMenu(null,-1){
      @Override public boolean stillValid(Player p){return false;}
    },2,2);
  }
  @Test public void recolorsExistingColoredBedAndPreservesName() {
    CraftingContainer grid=grid();ItemStack bed=new ItemStack(Items.RED_BED);
    bed.setHoverName(new TextComponent("My Bed"));grid.setItem(0,bed);grid.setItem(3,new ItemStack(Items.BLUE_DYE));
    CraftingRecipe recipe=DynamicRecipes.bedDyeRecipe(grid);assertNotNull(recipe);
    assertTrue(recipe.matches(grid,null));ItemStack result=recipe.assemble(grid);
    assertEquals(Items.BLUE_BED,result.getItem());assertEquals("My Bed",result.getHoverName().getString());
    assertEquals(1,result.getCount());assertEquals(Items.RED_BED,bed.getItem());
  }
  @Test public void rejectsExtraItemsAndDuplicateBedOrDye() {
    CraftingContainer grid=grid();grid.setItem(0,new ItemStack(Items.WHITE_BED));grid.setItem(1,new ItemStack(Items.RED_DYE));
    for(Item extra:new Item[]{Items.STICK,Items.WHITE_BED,Items.RED_DYE}){
      grid.setItem(2,new ItemStack(extra));assertNull(DynamicRecipes.bedDyeRecipe(grid));
    }
    grid.setItem(2,ItemStack.EMPTY);grid.setItem(1,ItemStack.EMPTY);assertNull(DynamicRecipes.bedDyeRecipe(grid));
  }
}

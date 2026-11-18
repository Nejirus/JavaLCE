package dev.legacyconsole.crafting;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;

/** PS4 custom ingredient grids, with native 1.17 recipe/NBT adapters. */
public final class DynamicRecipes {
  private DynamicRecipes() {}

  static CraftingContainer grid(int size) {
    return new CraftingContainer(new AbstractContainerMenu(null,-1) {
      @Override public boolean stillValid(Player player){return false;}
    },size,size);
  }
  public static RecipeCatalog.Entry entry(List<ItemStack> inputs,Level level,boolean table) {
    int size=table?3:2;if(inputs.size()!=size*size)return null;
    CraftingContainer grid=grid(size);List<RecipeCatalog.Ingredient> cells=new ArrayList<>();boolean any=false;
    for(int i=0;i<inputs.size();i++) {
      ItemStack item=Ps4BannerPatterns.unit(inputs.get(i));grid.setItem(i,item);
      cells.add(new RecipeCatalog.Ingredient(item,true));any|=!item.isEmpty();
    }
    if(!any || sameColorItem(grid))return null;
    for(CraftingRecipe recipe:level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING))
      if(recipe.matches(grid,level)) {
        ItemStack output=recipe.assemble(grid);
        if(!output.isEmpty())return new RecipeCatalog.Entry(recipe,output,size,size,false,cells);
      }
    CraftingRecipe custom=bedDyeRecipe(grid);
    if(custom==null)custom=woolDyeRecipe(grid);
    if(custom!=null)return new RecipeCatalog.Entry(custom,custom.assemble(grid),size,size,false,cells);
    return null;
  }
  static boolean sameColorItem(CraftingContainer grid) {
    ItemStack item=ItemStack.EMPTY;DyeColor dye=null;
    for(int i=0;i<grid.getContainerSize();i++) {
      ItemStack input=grid.getItem(i);if(input.isEmpty())continue;
      DyeColor color=Ps4Dyes.color(input);
      if(color!=null){if(dye!=null)return false;dye=color;}
      else if(item.isEmpty())item=input;else return false;
    }
    if(item.isEmpty() || dye==null)return false;
    Block block=item.getItem() instanceof BlockItem b?b.getBlock():null;
    if(block instanceof BedBlock bed)return bed.getColor()==dye;
    if(block instanceof ShulkerBoxBlock shulker)return shulker.getColor()==dye;
    return Ps4Dyes.woolColor(item)==dye;
  }
  static CraftingRecipe bedDyeRecipe(CraftingContainer grid){return recolor(grid,true);}
  static CraftingRecipe woolDyeRecipe(CraftingContainer grid){return recolor(grid,false);}
  private static CraftingRecipe recolor(CraftingContainer grid,boolean bed) {
    ItemStack inputItem=ItemStack.EMPTY,dyeItem=ItemStack.EMPTY;DyeColor color=null;
    for(int i=0;i<grid.getContainerSize();i++) {
      ItemStack input=grid.getItem(i);if(input.isEmpty())continue;
      boolean target=bed?input.getItem() instanceof BedItem:
          Ps4Dyes.woolColor(input)!=null;
      if(target && inputItem.isEmpty())inputItem=input;
      else if(Ps4Dyes.color(input)!=null && dyeItem.isEmpty()){dyeItem=input;color=Ps4Dyes.color(input);}
      else return null;
    }
    if(inputItem.isEmpty() || color==null || sameColorItem(grid))return null;
    ItemStack result=new ItemStack(Registry.ITEM.get(new ResourceLocation(color.getName()+(bed?"_bed":"_wool"))));
    if(inputItem.hasTag())result.setTag(inputItem.getTag().copy());
    return new ShapelessRecipe(new ResourceLocation("legacyconsole",bed?"dye_bed":"dye_wool"),"",result,
        NonNullList.of(Ingredient.EMPTY,Ingredient.of(inputItem.getItem()),Ingredient.of(dyeItem.getItem())));
  }
}

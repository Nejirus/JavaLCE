package dev.legacyconsole.crafting;

import java.util.*;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Adapts synchronized 1.17 stonecutting recipes to PS4's inventory-funded crafting mode. */
public final class StonecuttingRecipes {
  private StonecuttingRecipes() {}
  public static List<RecipeCatalog.Entry> entries(Level level) {
    List<RecipeCatalog.Entry> result=new ArrayList<>();
    for(StonecutterRecipe source:level.getRecipeManager().getAllRecipesFor(RecipeType.STONECUTTING)) {
      var cells=new ArrayList<RecipeCatalog.Ingredient>();
      for(var ingredient:source.getIngredients())cells.add(new RecipeCatalog.Ingredient(Arrays.asList(ingredient.getItems())));
      if(cells.size()==1&&!source.getResultItem().isEmpty())result.add(new RecipeCatalog.Entry(new Adapter(source),1,1,true,cells));
    }
    return result;
  }
  public static RecipeCatalog.Entry get(long key,Level level) {
    return entries(level).stream().filter(e->e.key==key).findFirst().orElse(null);
  }
  public static boolean isStonecutting(RecipeCatalog.Entry entry){return entry.recipe instanceof Adapter;}
  private record Adapter(StonecutterRecipe source) implements CraftingRecipe {
    @Override public boolean matches(CraftingContainer input,Level level){return source.matches(input,level);}
    @Override public ItemStack assemble(CraftingContainer input){return source.assemble(input);}
    @Override public boolean canCraftInDimensions(int width,int height){return width>0&&height>0;}
    @Override public ItemStack getResultItem(){return source.getResultItem();}
    @Override public ResourceLocation getId(){return source.getId();}
    @Override public RecipeSerializer<?> getSerializer(){return source.getSerializer();}
    @Override public NonNullList<Ingredient> getIngredients(){return source.getIngredients();}
    @Override public String getGroup(){return source.getGroup();}
  }
}

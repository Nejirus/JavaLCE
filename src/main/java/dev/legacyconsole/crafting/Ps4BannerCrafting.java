package dev.legacyconsole.crafting;

import java.util.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import static dev.legacyconsole.crafting.Ps4BannerPatterns.*;

/** PS4's staged banner transaction, adapted to 1.17 inventory storage. */
public final class Ps4BannerCrafting {
  private Ps4BannerCrafting() {}
  public static final class Plan {
    public final ItemStack output;
    public final List<RecipeCatalog.Ingredient> ingredients;
    public final List<Item> catalysts;
    private Plan(ItemStack output,List<RecipeCatalog.Ingredient> ingredients,List<Item> catalysts) {
      this.output=output;this.ingredients=List.copyOf(ingredients);this.catalysts=List.copyOf(catalysts);
    }
    public int[] allocate(List<ItemStack> inventory) {
      for(Item catalyst:catalysts)if(inventory.stream().noneMatch(s -> s.is(catalyst)))return null;
      boolean[][] matches=new boolean[ingredients.size()][inventory.size()];int[] counts=new int[inventory.size()];
      for(int j=0;j<inventory.size();j++) {
        counts[j]=inventory.get(j).getCount();
        for(int i=0;i<ingredients.size();i++)matches[i][j]=ingredients.get(i).matches(inventory.get(j));
      }
      return IngredientAllocator.allocate(matches,counts);
    }
  }
  public static Plan plan(ItemStack base,List<Layer> layers,boolean table) {
    return plan(base,layers,table,false);
  }
  public static Plan plan(ItemStack base,List<Layer> layers,boolean table,boolean loom) {
    if(!(base.getItem() instanceof BannerItem) || layers.isEmpty() || layers.size()>6 || count(base)+layers.size()>6)
      return null;
    List<RecipeCatalog.Ingredient> cost=new ArrayList<>();List<Item> catalysts=new ArrayList<>();
    cost.add(new RecipeCatalog.Ingredient(unit(base),true));ItemStack output=unit(base);
    for(Layer layer:layers) {
      Pattern pattern=get(layer.recipe());
      if(pattern==null || !pattern.fits(table,loom) || layer.color()<0 || layer.color()>15)return null;
      output=apply(output,pattern,layer.color());if(output.isEmpty())return null;
      Item dye=layer.material();if(dye==Items.AIR)return null;
      for(int i=0;i<pattern.dyes();i++)cost.add(new RecipeCatalog.Ingredient(new ItemStack(dye),false));
      if(pattern.special!=null) {
        if(pattern.reusable) {if(!catalysts.contains(pattern.material()))catalysts.add(pattern.material());}
        else cost.add(new RecipeCatalog.Ingredient(new ItemStack(pattern.material()),false));
      }
    }
    return new Plan(output,cost,catalysts);
  }
  public static void craft(Player player,ConsoleContainer menu,ItemStack base,List<Layer> layers,int batches) {
    Plan plan=plan(base,layers,menu.table,menu.kind==ConsoleContainer.LOOM);
    if(menu.kind==ConsoleContainer.STONECUTTER){menu.result(NativeCrafting.INVALID,0);return;}
    if(plan==null || batches<1 || batches>64 || !menu.stillValid(player)) {menu.result(NativeCrafting.INVALID,0);return;}
    if(!menu.getCarried().isEmpty()) {menu.result(NativeCrafting.CURSOR,0);return;}
    int total=0,status=NativeCrafting.MISSING;
    for(int n=0;n<batches;n++) {
      List<ItemStack> inventory=NativeCrafting.copies(player.getInventory().items);
      int[] slots=plan.allocate(inventory);
      if(slots==null){status=NativeCrafting.MISSING;break;}
      for(int slot:slots)inventory.get(slot).shrink(1);
      if(!NativeCrafting.insert(inventory,plan.output.copy())){status=NativeCrafting.FULL;break;}
      NativeCrafting.awardCustom(player,plan.output,plan.ingredients);
      for(int i=0;i<inventory.size();i++)player.getInventory().items.set(i,inventory.get(i));
      player.getInventory().setChanged();total++;status=NativeCrafting.OK;
    }
    menu.result(total>0?NativeCrafting.OK:status,total);
  }
}

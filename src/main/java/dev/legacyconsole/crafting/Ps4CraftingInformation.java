package dev.legacyconsole.crafting;

import java.util.*;
import net.minecraft.world.item.*;

/** IUIScene_CraftingMenu +0xe18, input 0x9ac698 and ingredients 0x9b7930. */
public final class Ps4CraftingInformation {
 public static final int INVENTORY=0,DESCRIPTION=1,INGREDIENTS=2;
 public static int next(int mode){return mode<=1?mode+1:0;}
 public static String prompt(int mode){return switch(mode){case DESCRIPTION->"63d85f8f";case INGREDIENTS->"ccf37993";default->"ae01f627";};}
 public record Line(ItemStack icon,List<ItemStack> alternatives,int required,boolean missing){}
 /** The console lists unique ingredients, with their recipe quantity and shortage. */
 public static List<Line> ingredients(RecipeCatalog.Entry recipe,List<ItemStack> inventory){
  if(recipe==null)return List.of();
  boolean[] missing=recipe.missingCells(inventory);var positions=new LinkedHashMap<String,List<Integer>>();
  for(int i=0;i<recipe.cells.size();i++){var cell=recipe.cells.get(i);if(!cell.alternatives.isEmpty())positions.computeIfAbsent(cell.signature(),key->new ArrayList<>()).add(i);}
  var result=new ArrayList<Line>();
  for(var indices:positions.values()){
   var ingredient=recipe.cells.get(indices.get(0));ItemStack icon=inventory.stream().filter(ingredient::matches).findFirst().orElse(ingredient.alternatives.get(0)).copy();
   icon.setCount(indices.size());boolean absent=indices.stream().anyMatch(i->missing[i]);
   result.add(new Line(icon,ingredient.alternatives,indices.size(),absent));
  }
  return List.copyOf(result);
 }
 private Ps4CraftingInformation(){}
}

package dev.legacyconsole.client.ui;

import dev.legacyconsole.client.Ps4Descriptions;
import java.util.ArrayList;
import java.util.Collections;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** PS4 container pointer tooltips: derive actions from the hovered slot and carried stack. */
public final class Ps4InventoryActions {
 public static String[] prompts(Slot slot,ItemStack carried,Player player){
  var out=new ArrayList<String>();
  if(slot!=null&&slot.isActive()){
   var stack=slot.getItem();boolean item=!stack.isEmpty();
   if(carried.isEmpty()){
    if(item&&slot.mayPickup(player)){
     Collections.addAll(out,"cross","Take");
     if(stack.getCount()>1)Collections.addAll(out,"square","Split");
    }
   }else if(slot.mayPlace(carried)){
    boolean same=item&&ItemStack.isSame(stack,carried)&&ItemStack.tagMatches(stack,carried);
    int remaining=slot.getMaxStackSize(carried)-(same?stack.getCount():0);
    if(item&&!same){if(slot.mayPickup(player))Collections.addAll(out,"cross","Swap");}
    else if(remaining>0){
     Collections.addAll(out,"cross","Place");
     if(carried.getCount()>1&&remaining>1)Collections.addAll(out,"square","Place One");
    }
   }
   if(item&&slot.mayPickup(player))Collections.addAll(out,"triangle","Quick Move");
   Collections.addAll(out,"circle","Back");
   if(item&&!Ps4Descriptions.get(stack).isEmpty())Collections.addAll(out,"right_trigger",Ps4CraftingArt.label("2fccdffb"));
  }else Collections.addAll(out,"circle","Back");
  return out.toArray(String[]::new);
 }
 private Ps4InventoryActions(){}
}

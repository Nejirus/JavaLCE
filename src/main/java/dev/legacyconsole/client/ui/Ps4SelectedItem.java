package dev.legacyconsole.client.ui;

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Java inventory notification adapter for PS4 0xc78a90 / 0x37b4c0. */
public final class Ps4SelectedItem {
  private Ps4SelectedItem() {}
  public static boolean changed(int slot,int previousSlot,ItemStack item,ItemStack previous) {
    if(slot!=previousSlot||item.isEmpty()!=previous.isEmpty()||item.getItem()!=previous.getItem())return true;
    if(ItemStack.tagMatches(item,previous))return false;
    // PS4 consumes an inventory selection/change notification. Tool wear does
    // not make a new selection. Java stores wear inside the general item tag.
    return !Objects.equals(withoutWear(item),withoutWear(previous));
  }
  private static CompoundTag withoutWear(ItemStack item) {
    if(!item.hasTag())return null;
    var tag=item.getTag().copy();tag.remove("Damage");return tag.isEmpty()?null:tag;
  }
}

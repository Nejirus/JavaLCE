package dev.legacyconsole.client.ui;

import java.util.List;
import net.minecraft.world.item.ItemStack;

/** IUIScene_CreativeMenu::getEmptyInventorySlot / handleSlotListClicked. */
final class LceCreativeHotbar {
  private LceCreativeHotbar() {}

  static int take(List<ItemStack> hotbar, ItemStack icon, boolean fullStack, int previousSlot) {
    if (icon.isEmpty()) return -1;
    ItemStack taken = icon.copy();
    taken.setCount(fullStack ? taken.getMaxStackSize() : 1);
    int target = -1;
    for (int i = 0; i < 9; i++) {
      ItemStack old = hotbar.get(i);
      if (!old.isEmpty() && old.sameItem(taken) && ItemStack.tagMatches(old, taken)
          && old.getCount() + taken.getCount() <= old.getMaxStackSize()) {
        target = i;
        taken.grow(old.getCount());
        break;
      }
    }
    if (target < 0)
      for (int i = 0; i < 9; i++)
        if (hotbar.get(i).isEmpty()) { target = i; break; }
    // LCE retains the last quick-select slot when the bar has no room.
    if (target < 0) target = Math.floorMod(previousSlot, 9);
    hotbar.set(target, taken);
    return target;
  }
}

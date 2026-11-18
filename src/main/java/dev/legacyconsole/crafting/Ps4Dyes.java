package dev.legacyconsole.crafting;

import java.util.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

/** PS4 color ordering, using Minecraft 1.17's sixteen separate dye items. */
public final class Ps4Dyes {
  private Ps4Dyes() {}
  public static DyeColor color(ItemStack stack) {
    if(stack.getItem() instanceof DyeItem dye)return dye.getDyeColor();
    return null;
  }
  public static DyeColor woolColor(ItemStack stack) {
    for(DyeColor color:DyeColor.values())
      if(stack.is(Registry.ITEM.get(new ResourceLocation(color.getName()+"_wool"))))return color;
    return null;
  }
  public static ItemStack[] choices() {
    List<ItemStack> result=new ArrayList<>();
    for(DyeColor color:DyeColor.values())result.add(new ItemStack(DyeItem.byColor(color)));
    return result.toArray(ItemStack[]::new);
  }
  public static ItemStack[] bannerChoices() {
    List<ItemStack> result=new ArrayList<>();
    for(int i=0;i<16;i++) {
      DyeColor color=DyeColor.byId(15-i);
      result.add(new ItemStack(DyeItem.byColor(color)));
    }
    return result.toArray(ItemStack[]::new);
  }
  public static Item material(int color,String item) {
    if(color<0||color>15)return Items.AIR;
    if(item==null || item.isEmpty())return DyeItem.byColor(DyeColor.byId(color));
    ResourceLocation key=ResourceLocation.tryParse(item);
    if(key==null || !Registry.ITEM.containsKey(key))return Items.AIR;
    Item material=Registry.ITEM.get(key);
    return color(new ItemStack(material))==DyeColor.byId(color)?material:Items.AIR;
  }
}

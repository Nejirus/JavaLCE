package dev.legacyconsole.client.ui;
import com.google.gson.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.nbt.TagParser;
final class ConsoleItemNames {
 static ItemStack resolve(JsonElement element){
  if(element==null||element.isJsonNull())return ItemStack.EMPTY;
  String name=element.isJsonPrimitive()?element.getAsString():element.getAsJsonObject().get("item").getAsString();
  if(name.equals("short_grass")||name.equals("minecraft:short_grass"))name="minecraft:grass";
  if(name.startsWith("#"))return ItemStack.EMPTY;
  ResourceLocation id=ResourceLocation.tryParse(name);if(id==null||!Registry.ITEM.containsKey(id))return ItemStack.EMPTY;
  ItemStack stack=new ItemStack(Registry.ITEM.get(id));
  if(element.isJsonObject()&&element.getAsJsonObject().has("nbt"))try{stack.setTag(TagParser.parseTag(element.getAsJsonObject().get("nbt").getAsString()));}catch(com.mojang.brigadier.exceptions.CommandSyntaxException e){throw new IllegalArgumentException("Invalid reference item tag for "+name,e);}
  if(stack.hasTag()){
   var tag=stack.getTag();
   if(tag.contains("Potion",8)&&!Registry.POTION.containsKey(new ResourceLocation(tag.getString("Potion"))))return ItemStack.EMPTY;
   for(String list:new String[]{"Enchantments","StoredEnchantments"})for(var enchantment:tag.getList(list,10)){
    var enchantmentId=ResourceLocation.tryParse(((net.minecraft.nbt.CompoundTag)enchantment).getString("id"));
    if(enchantmentId==null||!Registry.ENCHANTMENT.containsKey(enchantmentId))return ItemStack.EMPTY;
   }
  }
  return stack;
 }
}

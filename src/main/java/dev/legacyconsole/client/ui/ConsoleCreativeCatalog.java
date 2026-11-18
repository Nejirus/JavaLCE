package dev.legacyconsole.client.ui;
import com.google.gson.*;import java.io.*;import java.nio.charset.StandardCharsets;import java.util.*;
import net.minecraft.client.Minecraft;import net.minecraft.core.*;import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;import net.minecraft.world.item.enchantment.*;import net.minecraft.nbt.CompoundTag;
public final class ConsoleCreativeCatalog {
 public static final class Tab {public final String title,icon;public final List<ItemStack> items=new ArrayList<>();Tab(String title,String icon){this.title=title;this.icon=icon;}}
 private static String key(ItemStack s){return s.save(new CompoundTag()).toString();}
 public static List<Tab> build(){
  String[] names={"Building Blocks","Decorations","Redstone & Transport","Materials","Food","Tools, Weapons & Armour","Brewing","Miscellaneous"};
  String[] icons={"structures","decoration","mechanisms","materials","food","tools","brewing","misc"};
  List<Tab> tabs=new ArrayList<>();Set<String> seen=new HashSet<>();
  for(int i=0;i<8;i++)tabs.add(new Tab(names[i],icons[i]));
  try(Reader reader=new InputStreamReader(Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation("legacyconsole","creative_legacy4j.json")).getInputStream(),StandardCharsets.UTF_8)){
   int index=0;for(var entry:new JsonParser().parse(reader).getAsJsonObject().entrySet()){
    Tab tab=tabs.get(index++);for(JsonElement element:entry.getValue().getAsJsonObject().getAsJsonArray("listing")){ItemStack stack=ConsoleItemNames.resolve(element);if(!stack.isEmpty()&&seen.add(key(stack)))tab.items.add(stack);}
   }
  }catch(IOException e){throw new IllegalStateException("Cannot read Legacy4J creative order",e);}
  for(CreativeModeTab original:CreativeModeTab.TABS){
   if(original==null||original==CreativeModeTab.TAB_SEARCH||original==CreativeModeTab.TAB_INVENTORY||original==CreativeModeTab.TAB_HOTBAR)continue;
   int target=original==CreativeModeTab.TAB_BUILDING_BLOCKS?0:original==CreativeModeTab.TAB_DECORATIONS?1:original==CreativeModeTab.TAB_REDSTONE||original==CreativeModeTab.TAB_TRANSPORTATION?2:original==CreativeModeTab.TAB_MATERIALS?3:original==CreativeModeTab.TAB_FOOD?4:original==CreativeModeTab.TAB_TOOLS||original==CreativeModeTab.TAB_COMBAT?5:original==CreativeModeTab.TAB_BREWING?6:7;
   NonNullList<ItemStack> fallback=NonNullList.create();original.fillItemList(fallback);
   for(ItemStack stack:fallback)if(!stack.isEmpty()&&seen.add(key(stack)))tabs.get(target).items.add(stack.copy());
  }
  for(Item special:new Item[]{Items.FIREWORK_ROCKET,Items.FIREWORK_STAR})if(seen.add(key(new ItemStack(special))))tabs.get(7).items.add(new ItemStack(special));
  for(Tab tab:tabs)tab.items.removeIf(s->s.is(Items.ELYTRA));
  int after=0;for(int i=0;i<tabs.get(5).items.size();i++)if(tabs.get(5).items.get(i).getItem() instanceof ArmorItem)after=i+1;
  tabs.get(5).items.add(after,new ItemStack(Items.ELYTRA));
  return tabs;
 }
}

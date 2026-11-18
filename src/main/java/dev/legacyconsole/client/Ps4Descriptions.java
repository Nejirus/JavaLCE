package dev.legacyconsole.client;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import dev.legacyconsole.tutorial.Ps4TutorialScript;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.*;

/** Original Block::setUseDescriptionId bindings, followed by Legacy4J's newer blocks. */
public final class Ps4Descriptions {
 private static final Map<Block,String> PS4=new HashMap<>();
 private static final Set<Block> PRESENT=new HashSet<>();
 private static final Map<Item,String> ITEM_PS4=new HashMap<>();
 private static final Set<Item> PRESENT_ITEMS=new HashSet<>();
 private static final Map<String,String> ITEM_FALLBACK=read("legacy4j_item_tips.json",new TypeToken<Map<String,String>>(){}.getType());
 private static final Map<String,String> FALLBACK=read("legacy4j_block_tips.json",new TypeToken<Map<String,String>>(){}.getType());
 private static final JsonArray OVERRIDES=read("legacy4j_tip_overrides.json",JsonArray.class);
 private static boolean loaded;
 private static <T>T read(String file,java.lang.reflect.Type type){try(var in=new InputStreamReader(Ps4Descriptions.class.getResourceAsStream("/assets/legacyconsole/"+file),StandardCharsets.UTF_8)){return new Gson().fromJson(in,type);}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 private static synchronized void load(){if(loaded)return;
  JsonArray catalog=read("ps4_block_catalog.json",JsonArray.class);for(JsonElement entry:catalog){var row=entry.getAsJsonObject();for(int aux=0;aux<16;aux++){Block block=block(row.get("native_id").getAsInt(),row.get("name").getAsString(),aux);if(block!=Blocks.AIR)PRESENT.add(block);}}
  JsonArray descriptions=read("ps4_block_descriptions.json",JsonArray.class);
  for(JsonElement element:descriptions){JsonObject row=element.getAsJsonObject();for(int aux=0;aux<16;aux++){Block block=block(row.get("native_id").getAsInt(),row.get("name").getAsString(),aux);if(block!=Blocks.AIR)PS4.putIfAbsent(block,row.get("text").getAsString());}}
  JsonArray variants=read("ps4_stone_descriptions.json",JsonArray.class);for(JsonElement element:variants){var row=element.getAsJsonObject();PS4.put(block(row.get("native_id").getAsInt(),"stone",row.get("aux").getAsInt()),row.get("text").getAsString());}
  JsonObject items=read("ps4_item_descriptions.json",JsonObject.class);for(JsonElement element:items.getAsJsonArray("items")){var row=element.getAsJsonObject();for(Item item:Ps4TutorialScript.resolve(row.get("name").getAsString(),-1)){PRESENT_ITEMS.add(item);if(row.has("text"))ITEM_PS4.put(item,row.get("text").getAsString());}}
  loaded=true;
 }
 private static Block block(int id,String name,int aux){
  if(id>255||id==38)try{return dev.legacyconsole.worldgen.Ps4GenerationStates.decode(id|(aux<<16)).getBlock();}catch(IllegalArgumentException notPopulationState){}
  Tag tag;if(id<=255)tag=(Tag)BlockStateData.getTag(id<<4|aux).convert(NbtOps.INSTANCE).getValue();
  else{var state=new CompoundTag();
   if(name.startsWith("stripped_log_"))name="stripped_"+name.substring(13)+((aux&12)==12?"_wood":"_log");
   if(name.startsWith("standing_sign_"))name=name.substring(14)+"_sign";if(name.startsWith("wall_sign_"))name=name.substring(10)+"_wall_sign";
   name=switch(name){case "smoothstone"->"smooth_stone";case "prismarine_bricks_stairs"->"prismarine_brick_stairs";case "end_brick_stairs"->"end_stone_brick_stairs";case "smooth_andesite_stairs"->"polished_andesite_stairs";case "smooth_diorite_stairs"->"polished_diorite_stairs";case "smooth_granite_stairs"->"polished_granite_stairs";case "blastfurnace","lit_blastfurnace"->"blast_furnace";case "lit_smoker"->"smoker";case "stonecutter2"->"stonecutter";case "sea_grass"->"seagrass";case "bubble_column_segment"->"bubble_column";case "wood"->new String[]{"oak","spruce","birch","jungle","acacia","dark_oak"}[Math.min(aux&7,5)]+"_wood";default->name;};
   state.putString("Name","minecraft:"+name);tag=state;}
  var state=(CompoundTag)DataFixers.getDataFixer().update(References.BLOCK_STATE,new Dynamic<Tag>(NbtOps.INSTANCE,tag),1519,2730).getValue();
  ResourceLocation location=ResourceLocation.tryParse(state.getString("Name"));return location==null?Blocks.AIR:Registry.BLOCK.get(location);
 }
 private static boolean matches(JsonObject rule,String field,Block block){
  var values=new ArrayList<String>();if(rule.has(field)&&rule.get(field).isJsonPrimitive())values.add(rule.get(field).getAsString());if(rule.has(field+"s"))for(JsonElement value:rule.getAsJsonArray(field+"s"))values.add(value.getAsString());boolean positive=false;
  ResourceLocation actual=field.equals("block")?Registry.BLOCK.getKey(block):Registry.ITEM.getKey(block.asItem());
  for(String value:values){if(value.startsWith("!")&&actual.equals(new ResourceLocation(value.substring(1))))return false;
   if(value.startsWith("#")){ResourceLocation tag=new ResourceLocation(value.substring(1));positive|=field.equals("block")?net.minecraft.tags.BlockTags.getAllTags().getTagOrEmpty(tag).contains(block):net.minecraft.tags.ItemTags.getAllTags().getTagOrEmpty(tag).contains(block.asItem());}
   else if(!value.startsWith("!"))positive|=actual.equals(new ResourceLocation(value));
  }return positive;
 }
 public static String get(BlockState state){load();Block block=state.getBlock();String text=PS4.get(block);if(text!=null)return text;if(PRESENT.contains(block))return "";
  for(JsonElement element:OVERRIDES){var rule=element.getAsJsonObject();if((matches(rule,"block",block)||matches(rule,"item",block))&&rule.has("tip")&&rule.get("tip").isJsonObject()&&rule.getAsJsonObject("tip").has("translate")){String key=rule.getAsJsonObject("tip").get("translate").getAsString();if(FALLBACK.containsKey(key))return FALLBACK.get(key);}}
  return FALLBACK.getOrDefault(block.getDescriptionId()+".tip","");}
 public static String get(ItemStack stack){
  if(stack.isEmpty())return "";if(stack.getItem() instanceof BlockItem block)return get(block.getBlock().defaultBlockState());load();
  Item item=stack.getItem();if(ITEM_PS4.containsKey(item))return ITEM_PS4.get(item);if(PRESENT_ITEMS.contains(item))return "";
  for(JsonElement element:OVERRIDES){var rule=element.getAsJsonObject();var ids=new ArrayList<String>();if(rule.has("item")&&rule.get("item").isJsonPrimitive())ids.add(rule.get("item").getAsString());if(rule.has("items"))for(JsonElement id:rule.getAsJsonArray("items"))ids.add(id.getAsString());
   var actual=Registry.ITEM.getKey(item);boolean match=false,excluded=false;for(String id:ids){if(id.startsWith("!"))excluded|=actual.equals(new ResourceLocation(id.substring(1)));else if(id.startsWith("#"))match|=net.minecraft.tags.ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(id.substring(1))).contains(item);else match|=actual.equals(new ResourceLocation(id));}
   if(match&&!excluded&&rule.has("tip")&&rule.get("tip").isJsonObject()&&rule.getAsJsonObject("tip").has("translate")){String key=rule.getAsJsonObject("tip").get("translate").getAsString();if(ITEM_FALLBACK.containsKey(key))return ITEM_FALLBACK.get(key);}
  }return ITEM_FALLBACK.getOrDefault(item.getDescriptionId()+".tip","");
 }
 public static boolean fromPs4(ItemStack item){load();return item.getItem() instanceof BlockItem block?PS4.containsKey(block.getBlock()):ITEM_PS4.containsKey(item.getItem());}
 private Ps4Descriptions(){}
}

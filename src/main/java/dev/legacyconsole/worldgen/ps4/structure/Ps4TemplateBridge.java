package dev.legacyconsole.worldgen.ps4.structure;

import java.util.*;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.Property;

/** Converts PS4's mixed pre/post-flattening palette to equivalent 1.17.1 states. */
public final class Ps4TemplateBridge {
 private Ps4TemplateBridge(){}
 private static String value(CompoundTag props,String key,String fallback){String v=props.getString(key);return v.isEmpty()?fallback:v;}
 /** Jigsaw replacements are block-state strings, separate from the NBT palette. */
 public static String finalState(String source){
  String text=source.trim();int bracket=text.indexOf('[');String name=bracket<0?text:text.substring(0,bracket);
  // Original savanna terminator_05 truncates grass_path to grass_. Its other
  // entrance replacement and the surrounding road cells are grass_path.
  if(name.equals("minecraft:grass_"))name="minecraft:grass_path";
  CompoundTag state=new CompoundTag(),properties=new CompoundTag();state.putString("Name",name);
  if(bracket>=0){if(!text.endsWith("]"))throw new IllegalArgumentException("Malformed PS4 replacement: "+source);
   for(String pair:text.substring(bracket+1,text.length()-1).split(",")){if(pair.isBlank())continue;String[] parts=pair.split("=",2);if(parts.length!=2)throw new IllegalArgumentException("Malformed PS4 replacement: "+source);properties.putString(parts[0].trim(),parts[1].trim());}
  }
  state.put("Properties",properties);block(state);
  List<String> entries=new ArrayList<>();for(String key:new TreeSet<>(properties.getAllKeys()))entries.add(key+"="+properties.getString(key));
  return state.getString("Name")+(entries.isEmpty()?"":"["+String.join(",",entries)+"]");
 }
 public static void block(CompoundTag tag){
  String name=tag.getString("Name").replace("minecraft:","");CompoundTag p=tag.getCompound("Properties");String variant=value(p,"variant","oak");
  name=switch(name){
   case "planks"->variant+"_planks";
   case "wooden_slab","double_wooden_slab"->{p.putString("type",name.startsWith("double")?"double":value(p,"half","bottom"));yield variant+"_slab";}
   case "stone_slab","double_stone_slab"->{if(!p.contains("variant"))yield name;p.putString("type",name.startsWith("double")?"double":value(p,"half","bottom"));yield switch(variant){case "stone"->"smooth_stone_slab";case "stone_brick"->"stone_brick_slab";case "brick"->"brick_slab";case "wood_old"->"petrified_oak_slab";default->variant+"_slab";};}
   case "wool","carpet"->value(p,"color","white").replace("silver","light_gray")+"_"+name;
   case "leaves2"->{p.putString("persistent",p.getString("decayable").equals("false")?"true":"false");p.putString("distance","7");yield variant+"_leaves";}
   case "log2"->variant+(p.getString("axis").equals("none")?"_wood":"_log");
   case "waterlily"->"lily_pad";case "melon_block"->"melon";case "fence"->"oak_fence";
   case "wall_banner","illager_captain_wall_banner"->"white_wall_banner";
   case "sapling"->value(p,"type","oak")+"_sapling";
   case "monster_egg"->switch(variant){case "chiseled_brick"->"infested_chiseled_stone_bricks";case "mossy_brick"->"infested_mossy_stone_bricks";case "stone_brick"->"infested_stone_bricks";default->"infested_"+variant;};
   case "mob_spawner"->"spawner";case "web"->"cobweb";case "coral_block"->"dead_tube_coral_block";
   case "trapdoor"->"oak_trapdoor";case "wooden_door"->"oak_door";
   case "stonebrick"->switch(variant){case "chiseled_stonebrick"->"chiseled_stone_bricks";case "cracked_stonebrick"->"cracked_stone_bricks";case "mossy_stonebrick"->"mossy_stone_bricks";default->"stone_bricks";};
   case "wall_sign"->"oak_wall_sign";case "bed"->"red_bed";case "grass_path"->"dirt_path";
   case "flower_pot"->{String contents=p.getString("contents");yield contents.isEmpty()||contents.equals("empty")?"flower_pot":"potted_"+contents;}
   case "grass"->p.contains("snowy")?"grass_block":"grass";
   case "torch"->p.contains("facing")&&!p.getString("facing").equals("up")?"wall_torch":"torch";
   case "redstone_torch"->p.contains("facing")&&!p.getString("facing").equals("up")?"redstone_wall_torch":"redstone_torch";
   default->name;
  };
  if(name.equals("jigsaw")&&p.contains("facing")){String facing=p.getString("facing");p.putString("orientation",facing.equals("up")?"up_north":facing.equals("down")?"down_north":facing+"_up");}
  if(name.endsWith("_wall"))for(String side:List.of("north","east","south","west")){String v=p.getString(side);if(v.equals("true")||v.equals("false"))p.putString(side,v.equals("true")?"low":"none");}
  if(name.equals("sand")&&p.getString("variant").equals("red_sand"))name="red_sand";
  if(name.equals("stone_stairs")&&!p.contains("waterlogged"))name="cobblestone_stairs";
  ResourceLocation id=new ResourceLocation("minecraft",name);if(!Registry.BLOCK.containsKey(id))throw new IllegalArgumentException("Unmapped PS4 block: "+tag);
  var definition=Registry.BLOCK.get(id).getStateDefinition();
  for(String key:new HashSet<>(p.getAllKeys())){Property<?> property=definition.getProperty(key);if(property==null){p.remove(key);continue;}if(property.getValue(p.getString(key)).isEmpty())throw new IllegalArgumentException("Unmapped PS4 property "+id+" "+key+"="+p.getString(key));}
  tag.putString("Name",id.toString());tag.put("Properties",p);
 }
 public static void blockEntity(CompoundTag tag){
  if(!tag.contains("id"))return;String id=tag.getString("id");tag.putString("id",switch(id){case "Chest"->"minecraft:chest";case "Furnace"->"minecraft:furnace";case "Banner"->"minecraft:banner";case "Sign"->"minecraft:sign";case "FlowerPot"->"minecraft:flower_pot";case "MobSpawner"->"minecraft:mob_spawner";case "Cauldron"->"minecraft:brewing_stand";default->id;});
 }
 public static void entity(CompoundTag tag){String id=tag.getString("id");tag.putString("id",switch(id){case "Villager"->"minecraft:villager";case "ZombieVillager"->"minecraft:zombie_villager";default->id;});}
}

package dev.legacyconsole.tutorial;

import com.google.gson.Gson;
import com.mojang.serialization.Dynamic;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.*;

/** Authored task order/arguments recovered from mcps4's FullTutorial constructor. */
public final class Ps4TutorialScript {
 public static final Ps4TutorialScript SOURCE=read("tasks.json",Ps4TutorialScript.class);
 public static final List<Task> GAMEPLAY=flatten(SOURCE.gameplay);
 public List<Task> gameplay;public Map<Integer,List<Task>> states,guided_states;
 public List<AreaHint> area_hints;
 public static final class AreaHint {public String text,description_hash;public double[] bounds;public int display_state,complete_state;public boolean contains(double x,double y,double z){return x>=bounds[0]&&y>=bounds[1]&&z>=bounds[2]&&x<=bounds[3]&&y<=bounds[4]&&z<=bounds[5];}}
 public static final int MINIMUM_TICKS=40,HINT_TICKS=140,HINT_DELAY_TICKS=280;
 private static final Map<String,Set<Item>> ITEMS=new HashMap<>();
 private static final Map<Integer,ItemStack> NUMERIC_ITEMS=new HashMap<>();
 public static final class Selector {public String name;public int aux;}
 public static final class Task {
  public String kind,text="",prompt="",description_hash,prompt_hash;
  public boolean precompletion,minimum_time;public int required=1,state,flag,operation,no_action,yes_action,completion_action,selection_type,group;public String selected_item;
  public int[] actions=new int[0],completion_masks=new int[0];public String block;
  public List<Selector> items=List.of();public List<Task> children=List.of();
  public List<String> variants=List.of();
  public boolean matches(ItemStack stack){if(stack.isEmpty())return false;for(Selector selector:items)if(resolve(selector.name,selector.aux).contains(stack.getItem()))return true;return false;}
 }
 public static <T>T read(String file,Class<T> type){
  try(var in=new InputStreamReader(Ps4TutorialScript.class.getResourceAsStream("/assets/legacyconsole/tutorial/"+file),StandardCharsets.UTF_8)){return new Gson().fromJson(in,type);}
  catch(Exception e){throw new ExceptionInInitializerError(e);}
 }
 public static List<Task> flatten(List<Task> input){var result=new ArrayList<Task>();for(Task t:input)if(t.kind.equals("ProcedureCompoundTask"))result.addAll(flatten(t.children));else result.add(t);return List.copyOf(result);}
 /** Java's flattening fixes supply item variants; console-only spelling is translated explicitly. */
 public static synchronized Set<Item> resolve(String source,int auxiliary){
  String key=source+":"+auxiliary;if(ITEMS.containsKey(key))return ITEMS.get(key);
  var result=new HashSet<Item>();String name=source;
  if(name.startsWith("stripped_log_"))name="stripped_"+name.substring(13)+"_log";
  if(name.equals("smoothstone"))name="smooth_stone";
  int from=auxiliary<0?0:auxiliary,to=auxiliary<0?15:auxiliary;
  for(int data=from;data<=to;data++){
   CompoundTag old=new CompoundTag();old.putString("id","minecraft:"+name);old.putByte("Count",(byte)1);old.putShort("Damage",(short)data);
   Tag fixed=DataFixers.getDataFixer().update(References.ITEM_STACK,new Dynamic<Tag>(NbtOps.INSTANCE,old),1343,2730).getValue();
   String id=((CompoundTag)fixed).getString("id");ResourceLocation location=ResourceLocation.tryParse(id);
   if(location!=null&&Registry.ITEM.containsKey(location)){Item item=Registry.ITEM.get(location);if(item!=Items.AIR)result.add(item);}
  }
  Set<Item> immutable=Set.copyOf(result);ITEMS.put(key,immutable);return immutable;
 }
 public static int areaIndex(){for(int i=0;i<GAMEPLAY.size();i++)if(GAMEPLAY.get(i).kind.equals("AreaTask"))return i;throw new IllegalStateException("Missing source tutorial area task");}
 /** Tutorial text's ICON payload is the original numeric item ID. */
 public static synchronized ItemStack icon(int id){return NUMERIC_ITEMS.computeIfAbsent(id,key->{CompoundTag old=new CompoundTag();old.putShort("id",(short)(int)key);old.putShort("Damage",(short)0);old.putByte("Count",(byte)1);Tag fixed=DataFixers.getDataFixer().update(References.ITEM_STACK,new Dynamic<Tag>(NbtOps.INSTANCE,old),0,2730).getValue();return ItemStack.of((CompoundTag)fixed);}).copy();}
 public static int finalIndex(){return areaIndex()+1;}
}

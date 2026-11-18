package dev.legacyconsole.tutorial;

import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Tutorial's task counters, pre-completion and ChoiceTask's jump-to-last action. */
public final class Ps4TutorialProgress {
 public int stage,flags;public int[] counts=new int[Ps4TutorialScript.GAMEPLAY.size()];
 public boolean[] done=new boolean[counts.length];public long activated;
 public final BitSet discs=new BitSet(12);
 public final Map<String,Integer> crafted=new HashMap<>();
 public final Map<Integer,Integer> menus=new HashMap<>();
 public final Set<Integer> completedMenus=new HashSet<>();
 public static int menuLength(int context){
  if(!Ps4TutorialScript.SOURCE.states.containsKey(context)&&!Ps4TutorialScript.SOURCE.guided_states.containsKey(context))return -1;
  return Ps4TutorialScript.flatten(Ps4TutorialScript.SOURCE.states.getOrDefault(context,List.of())).size()+Ps4TutorialScript.flatten(Ps4TutorialScript.SOURCE.guided_states.getOrDefault(context,List.of())).size();
 }
 public boolean menuProgress(int context,int index,boolean complete){
  int length=menuLength(context);if(length<0||index<0||index>length)return false;
  int previous=menus.getOrDefault(context,0);int next=complete?length:Math.max(previous,index);
  boolean changed=next!=previous;if(changed)menus.put(context,next);
  if(complete)changed|=completedMenus.add(context);return changed;
 }
 public Ps4TutorialScript.Task current(){return stage<done.length?Ps4TutorialScript.GAMEPLAY.get(stage):null;}
 public boolean restricted(){return stage<Ps4TutorialScript.areaIndex();}
 public void advance(long tick){
  while(stage<done.length){var task=current();if(task.kind.equals("ProgressFlagTask")){flags|=task.flag;done[stage]=true;}if(task.kind.equals("AreaTask"))done[stage]=true;if(!done[stage])break;stage++;activated=tick;}
 }
 public boolean acknowledge(boolean yes,long tick){
  var task=current();if(task==null||tick-activated<Ps4TutorialScript.MINIMUM_TICKS||!Set.of("InfoTask","ChoiceTask").contains(task.kind))return false;
  if(task.kind.equals("InfoTask")&&task.actions.length>0&&Arrays.stream(task.actions).noneMatch(action->action==(yes?0:1)))return false;
  if(task.kind.equals("ChoiceTask")&&!yes&&task.no_action==3)stage=Ps4TutorialScript.finalIndex();
  else {done[stage]=true;stage++;}activated=tick;advance(tick);return true;
 }
 public void event(String type,ItemStack item,long tick){
  if(type.equals("CraftTask")&&!item.isEmpty())crafted.merge(net.minecraft.core.Registry.ITEM.getKey(item.getItem()).toString(),1,Integer::sum);
  for(int i=stage;i<done.length;i++){
   var task=Ps4TutorialScript.GAMEPLAY.get(i);if(i!=stage&&!task.precompletion)continue;
   if(!task.kind.equals(type)||!task.matches(item))continue;
   counts[i]++;if(counts[i]>=task.required)done[i]=true;
  }advance(tick);
 }
 public void pickup(ItemStack item,int inventoryCount,long tick){
  for(int i=stage;i<done.length;i++){var t=Ps4TutorialScript.GAMEPLAY.get(i);if(t.kind.equals("PickupTask")&&(i==stage||t.precompletion)&&t.matches(item)&&inventoryCount>=t.required)done[i]=true;}advance(tick);
 }
 public void controls(int actionMask,long tick){
  var task=current();if(task==null||!task.kind.equals("ControllerTask"))return;
  if(task.minimum_time&&tick-activated<Ps4TutorialScript.MINIMUM_TICKS)return;
  for(int i=0;i<task.actions.length;i++)if((actionMask&(1<<(task.actions[i]-25)))!=0)counts[stage]|=1<<i;
  boolean complete=counts[stage]==(1<<task.actions.length)-1;
  for(int mask:task.completion_masks)complete|=counts[stage]==mask;
  if(complete&&(!task.minimum_time||tick-activated>=Ps4TutorialScript.MINIMUM_TICKS)){done[stage]=true;advance(tick);}
 }
 public CompoundTag save(){var tag=new CompoundTag();tag.putInt("Stage",stage);tag.putInt("Flags",flags);tag.putIntArray("Counts",counts);int[] completed=new int[done.length];for(int i=0;i<done.length;i++)completed[i]=done[i]?1:0;tag.putIntArray("Done",completed);tag.putLongArray("Discs",discs.toLongArray());var made=new CompoundTag();crafted.forEach(made::putInt);tag.put("Crafted",made);var menu=new CompoundTag();menus.forEach((context,index)->menu.putInt(Integer.toString(context),index));tag.put("MenuProgress",menu);tag.putIntArray("CompletedMenus",completedMenus.stream().mapToInt(Integer::intValue).sorted().toArray());return tag;}
 public static Ps4TutorialProgress load(CompoundTag tag,long tick){var p=new Ps4TutorialProgress();p.stage=Math.max(0,Math.min(p.done.length,tag.getInt("Stage")));p.flags=tag.getInt("Flags");int[] counts=tag.getIntArray("Counts"),done=tag.getIntArray("Done");System.arraycopy(counts,0,p.counts,0,Math.min(counts.length,p.counts.length));for(int i=0;i<Math.min(done.length,p.done.length);i++)p.done[i]=done[i]!=0;p.discs.or(BitSet.valueOf(tag.getLongArray("Discs")));var made=tag.getCompound("Crafted");for(String name:made.getAllKeys())p.crafted.put(name,made.getInt(name));var menu=tag.getCompound("MenuProgress");for(String name:menu.getAllKeys())try{p.menuProgress(Integer.parseInt(name),menu.getInt(name),false);}catch(NumberFormatException ignored){}
  for(int context:tag.getIntArray("CompletedMenus"))p.menuProgress(context,0,true);
  // Earlier builds saved gameplay completion, but omitted the menu guide state.
  // A completed StateChangeTask proves that its whole menu guide finished.
  if(!tag.contains("MenuProgress")&&!tag.contains("CompletedMenus"))for(int i=0;i<p.done.length;i++)if(p.done[i]){var task=Ps4TutorialScript.GAMEPLAY.get(i);if(task.kind.equals("StateChangeTask"))p.menuProgress(task.state,0,true);}
  p.activated=tick;return p;}
}

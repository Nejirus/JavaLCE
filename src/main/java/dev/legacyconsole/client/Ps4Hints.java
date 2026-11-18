package dev.legacyconsole.client;

import dev.legacyconsole.LegacyConsole;
import dev.legacyconsole.client.input.*;
import dev.legacyconsole.client.ui.*;
import dev.legacyconsole.network.*;
import dev.legacyconsole.tutorial.Ps4TutorialScript;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

/** Client input/presentation adapters for PS4 Tutorial and LookAtBlockHint. */
public final class Ps4Hints {
 private static boolean tutorial;private static int stage,discs,ticks,activated,lastContext=-1,menuIndex;
 private static int flags;private static boolean[] completed=new boolean[0];
 private static Map<String,Integer> crafted=Map.of();
 private static float yaw,pitch;private static int selected=-1,shownUntil,nextHint;
 private static List<Ps4TutorialScript.Task> menuTasks=List.of();
 private static final dev.legacyconsole.tutorial.Ps4InfoInput menuInput=new dev.legacyconsole.tutorial.Ps4InfoInput();
 private static final Map<Integer,Integer> menuProgress=new HashMap<>();private static int menuBaseCount;
 private static final Map<Integer,List<Ps4TutorialScript.Task>> menuHistory=new HashMap<>();
 private static final Set<Integer> menusSeen=new HashSet<>();
 private static final Set<String> hintsSeen=new HashSet<>();private static boolean loaded;
 private static final Ps4MenuHintHistory menuHints=new Ps4MenuHintHistory(hintsSeen);
 private static String hint="",hintTitle="";private static ItemStack hintItem=ItemStack.EMPTY;
 private static boolean detailed;
 private static final Map<String,String> GENERIC=Ps4TutorialScript.read("generic_hints.json",java.util.HashMap.class);
 private static net.minecraft.world.level.block.Block mining;private static net.minecraft.core.BlockPos miningPos;private static int mineRepeats,damage;private static Item held=Items.AIR;
 private static final Path PROFILE=net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get().resolve("legacyconsole-hints.txt");
 private Ps4Hints(){}
 public static void clear(){tutorial=false;stage=discs=0;lastContext=-1;menuTasks=List.of();menuInput.clear();menuHistory.clear();menuProgress.clear();menusSeen.clear();menuIndex=0;hint="";shownUntil=0;selected=-1;detailed=false;}
 public static void tutorial(TutorialState state){if(!tutorial){menusSeen.clear();menuProgress.clear();menuHistory.clear();activated=ticks;}tutorial=true;if(stage!=state.stage){stage=state.stage;activated=ticks;}discs=state.discs;flags=state.flags;completed=state.done;crafted=state.crafted;state.menus.forEach((context,index)->menuProgress.merge(context,index,Math::max));menusSeen.addAll(state.completedMenus);if(lastContext>=0&&menusSeen.contains(lastContext))menuIndex=menuTasks.size();}
 private static void load(){if(loaded)return;loaded=true;try{if(Files.isRegularFile(PROFILE))hintsSeen.addAll(Files.readAllLines(PROFILE));}catch(java.io.IOException e){LegacyConsole.log.warn("Cannot read hint profile",e);}}
 private static void saveProfile(){try{Files.write(PROFILE,new TreeSet<>(hintsSeen));}catch(java.io.IOException e){LegacyConsole.log.warn("Cannot save hint profile",e);}}
 private static void seen(String id){load();if(hintsSeen.add(id))saveProfile();}
 private static boolean menuCompleted(int context){load();return menusSeen.contains(context)||!tutorial&&menuHints.completed(context);}
 private static int savedMenuProgress(int context){load();return tutorial?menuProgress.getOrDefault(context,0):Math.max(menuProgress.getOrDefault(context,0),menuHints.progress(context));}
 private static void generic(String key){load();if(!ClientConfig.hints||current()!=null||ticks<nextHint||hintsSeen.contains(key))return;hint=GENERIC.get(key);hintTitle="";hintItem=ItemStack.EMPTY;shownUntil=ticks+Ps4TutorialScript.HINT_TICKS;nextHint=ticks+Ps4TutorialScript.HINT_DELAY_TICKS;seen(key);}
 private static Ps4TutorialScript.Task gameplay(){return tutorial&&stage<Ps4TutorialScript.GAMEPLAY.size()?Ps4TutorialScript.GAMEPLAY.get(stage):null;}
 private static Ps4TutorialScript.Task current(){if(menuIndex<menuTasks.size())return menuTasks.get(menuIndex);var mc=Minecraft.getInstance();return mc.screen==null||mc.screen instanceof dev.legacyconsole.port.GuiContainer||mc.screen instanceof AbstractContainerScreen?gameplay():null;}
 private static void send(int action,int mask,int context){LegacyConsole.NETWORK.sendToServer(new TutorialInput(action,mask,context));}
 public static boolean acknowledge(boolean yes){
  if(detailed){detailed=false;shownUntil=0;return true;}
  var task=current();if(task==null||!Set.of("InfoTask","ChoiceTask","HorseChoiceTask").contains(task.kind))return false;
  if(task.kind.equals("InfoTask")&&menuIndex<menuTasks.size())return uiInput(yes?0:1);
  if(ticks-activated<Ps4TutorialScript.MINIMUM_TICKS)return false;
  if(task.kind.equals("InfoTask")&&task.actions.length>0&&java.util.Arrays.stream(task.actions).noneMatch(a->a==(yes?0:1)))return false;
  if(menuIndex<menuTasks.size()){
   if(!yes&&(task.kind.equals("ChoiceTask")||task.kind.equals("HorseChoiceTask")))menuIndex=menuTasks.size();else menuIndex++;
   activated=ticks;
   rememberMenu();
   finishMenu();
   if(!yes&&task.kind.equals("InfoTask")){var mc=Minecraft.getInstance();if(mc.player!=null)mc.player.closeContainer();mc.setScreen(null);}
  }else if(tutorial)send(yes?0:1,0,0);
  return true;
 }
 /** PS4 InfoTask::handleUIInput (0x979160): record each requested mapping;
  * isCompleted (0x978ed0) waits for the minimum display time independently.
  * The crafting screen must still receive Square to update its information panel. */
 public static boolean uiInput(int action){
  if(menuIndex>=menuTasks.size())return false;
  var task=menuTasks.get(menuIndex);if(!task.kind.equals("InfoTask")||!menuInput.press(task,action))return false;
  finishInfo();return true;
 }
 private static boolean finishInfo(){
  if(menuIndex>=menuTasks.size())return false;var task=menuTasks.get(menuIndex);
  if(!task.kind.equals("InfoTask")||!menuInput.completed(task,ticks-activated))return false;
  menuIndex++;activated=ticks;menuInput.clear();rememberMenu();
  finishMenu();
  if(java.util.Arrays.stream(task.actions).anyMatch(a->a==1)){var mc=Minecraft.getInstance();if(mc.player!=null)mc.player.closeContainer();mc.setScreen(null);}
  return true;
 }
 /** The final child can complete without a button (e.g. smelting glass). */
 private static void finishMenu(){
  if(lastContext>=0&&!menuTasks.isEmpty()&&menuIndex>=menuTasks.size())completedMenu(lastContext);
 }
 private static void rememberMenu(){if(lastContext<0)return;Integer previous=menuProgress.put(lastContext,menuIndex);if(!Objects.equals(previous,menuIndex)){if(tutorial)send(4,menuIndex,lastContext);else{load();if(menuHints.remember(lastContext,menuIndex))saveProfile();}}}
 private static void completedMenu(int context){if(menusSeen.add(context)){if(tutorial)send(3,0,context);else{load();if(menuHints.complete(context))saveProfile();}}}
 /** A pre-completable final craft may be acknowledged by the server after the
  * player closes the menu. Retain that activated task until its history arrives. */
 private static void finishPendingMenus(){
  if(!tutorial)return;
  for(var entry:menuHistory.entrySet()){
   int context=entry.getKey(),index=menuProgress.getOrDefault(context,0);var tasks=entry.getValue();
   if(context==lastContext||index!=tasks.size()-1)continue;var task=tasks.get(index);
   if(task.precompletion&&task.kind.equals("CraftTask")&&crafted(task)){menuProgress.put(context,tasks.size());completedMenu(context);}
  }
 }
 public static boolean controller(PadState state,PadState previous){
  if(!state.connected)return false;if(state.held(PadState.R2)&&!previous.held(PadState.R2)&&moreInfo())return true;if(detailed){if((state.held(PadState.CROSS)&&!previous.held(PadState.CROSS))||(state.held(PadState.CIRCLE)&&!previous.held(PadState.CIRCLE)))acknowledge(false);return true;}var task=current();if(task==null)return false;
  if(state.held(PadState.CROSS)&&Set.of("InfoTask","ChoiceTask","HorseChoiceTask").contains(task.kind)){if(!previous.held(PadState.CROSS))acknowledge(true);return true;}
  if(state.held(PadState.CIRCLE)&&!previous.held(PadState.CIRCLE))return acknowledge(false);
  return false;
 }
 private static int context(Minecraft mc){
  if(mc.player!=null&&mc.player.containerMenu instanceof net.minecraft.world.inventory.AbstractFurnaceMenu)return 4;
  if(mc.player!=null&&mc.player.containerMenu instanceof net.minecraft.world.inventory.HorseInventoryMenu)return 30;
  if(mc.screen instanceof ConsoleSpecialCrafting special&&special.tutorialType()==2)return 36;
  if(mc.screen instanceof ConsoleCraftingScreen craft)return craft.tutorialTable()?3:2;
  if(mc.screen instanceof ConsoleSpecialCrafting||mc.screen instanceof CraftingScreen)return 2;
  if(mc.screen instanceof ConsoleInventory||mc.screen instanceof InventoryScreen)return mc.player!=null&&mc.player.isCreative()?14:1;
  if(mc.screen instanceof ConsoleBrewingScreen||mc.screen instanceof BrewingStandScreen)return 18;
  if(mc.screen instanceof Ps4EnchantingScreen||mc.screen instanceof EnchantmentScreen)return 20;
  if(mc.screen instanceof ConsoleMerchantScreen||mc.screen instanceof MerchantScreen)return 25;
  if(mc.screen instanceof Ps4AnvilScreen||mc.screen instanceof ConsoleUtilityScreen||mc.screen instanceof AnvilScreen)return 27;
  if(mc.screen instanceof Ps4BeaconScreen||mc.screen instanceof BeaconScreen)return 34;
  if(mc.screen instanceof AbstractFurnaceScreen)return 4;
  if(mc.screen instanceof HorseInventoryScreen)return 30;
  if(mc.player!=null&&mc.player.getVehicle() instanceof AbstractMinecart)return 5;
  if(mc.player!=null&&mc.player.getVehicle() instanceof Boat)return 6;
  if(mc.player!=null&&mc.player.fishing!=null)return 7;
  if(mc.player!=null&&mc.player.isSleeping())return 9;
  if(mc.player!=null&&mc.player.getVehicle() instanceof net.minecraft.world.entity.animal.horse.AbstractHorse)return 29;
  if(mc.player!=null&&mc.player.getMainHandItem().is(Items.NAME_TAG))return 8;
  return -1;
 }
 private static boolean crafted(Ps4TutorialScript.Task task){
  int count=0;for(var entry:crafted.entrySet()){Item item=net.minecraft.core.Registry.ITEM.get(new net.minecraft.resources.ResourceLocation(entry.getKey()));if(task.matches(new ItemStack(item)))count+=entry.getValue();}return count>=task.required;
 }
 /** FullTutorialActiveTask, ProgressFlagTask and UICraftingTask have no confirm button. */
 private static void menuTick(Minecraft mc){
  for(int guard=0;guard<100&&menuIndex<menuTasks.size();guard++){
   if(finishInfo())continue;
   if(tutorial&&gameplay()==null&&menuIndex>=menuBaseCount){menuIndex=menuTasks.size();break;}
   var task=menuTasks.get(menuIndex);boolean complete=false;
   if(task.kind.equals("FullTutorialActiveTask")){if(gameplay()==null&&task.completion_action!=0){menuIndex=menuTasks.size();break;}complete=true;}
   else if(task.kind.equals("ProgressFlagTask"))complete=(flags&task.flag)==task.flag;
   else if(task.kind.equals("CraftTask"))complete=crafted(task);
   else if(task.kind.equals("UICraftingTask")&&mc.screen instanceof ConsoleCraftingScreen craft)complete=task.selection_type==0?craft.tutorialGroup()==task.group:Ps4TutorialScript.resolve(task.selected_item,-1).contains(craft.tutorialItem().getItem());
   if(!complete)break;menuIndex++;activated=ticks;
  }
  if(lastContext>=0)rememberMenu();
  finishMenu();
 }
 public static final class Events {
  @SubscribeEvent public void tick(TickEvent.ClientTickEvent event){
   if(event.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();ticks++;if(mc.player==null||mc.level==null)return;
   int context=context(mc);if(context!=lastContext){if(lastContext>=0){rememberMenu();if(!menuTasks.isEmpty())menuHistory.put(lastContext,menuTasks);}lastContext=context;menuIndex=0;menuTasks=List.of();menuInput.clear();menuBaseCount=0;
    if((tutorial||ClientConfig.hints)&&context>=0&&!menuCompleted(context)){
     var tasks=new ArrayList<Ps4TutorialScript.Task>();tasks.addAll(Ps4TutorialScript.SOURCE.states.getOrDefault(context,!tutorial&&context==3?Ps4TutorialScript.SOURCE.states.getOrDefault(2,List.of()):List.of()));menuBaseCount=Ps4TutorialScript.flatten(tasks).size();
     if(tutorial)tasks.addAll(Ps4TutorialScript.SOURCE.guided_states.getOrDefault(context,List.of()));menuTasks=Ps4TutorialScript.flatten(tasks);menuIndex=Math.min(menuTasks.size(),savedMenuProgress(context));
    }activated=ticks;
    if(tutorial&&menuTasks.isEmpty()&&context>=0)send(3,0,context);
   }
   menuTick(mc);
   finishPendingMenus();
   if(tutorial){
    int mask=0;float dy=mc.player.getYRot()-yaw,dp=mc.player.getXRot()-pitch;
    if(mc.player.input.forwardImpulse>0)mask|=1<<(25-25);if(mc.player.input.forwardImpulse<0)mask|=1<<(26-25);
    if(mc.player.input.leftImpulse>0)mask|=1<<(27-25);if(mc.player.input.leftImpulse<0)mask|=1<<(28-25);
    if(dy<-.01)mask|=1<<(29-25);if(dy>.01)mask|=1<<(30-25);if(dp<-.01)mask|=1<<(31-25);if(dp>.01)mask|=1<<(32-25);
    if(mc.options.keyJump.isDown())mask|=1<<(33-25);if(mc.options.keyAttack.isDown())mask|=1<<(35-25);
    int slot=mc.player.getInventory().selected;if(selected>=0&&slot!=selected)mask|=1<<((slot<selected?36:37)-25);
    if(context==1||context==14)mask|=1<<(38-25);if(context==2||context==3)mask|=1<<(41-25);
    yaw=mc.player.getYRot();pitch=mc.player.getXRot();selected=slot;
    if(mask!=0)send(2,mask,0);
    var task=gameplay();if(task!=null&&task.kind.equals("StateChangeTask")&&context==task.state&&menusSeen.contains(context))send(3,0,context);
   }
   load();ItemStack tool=mc.player.getMainHandItem();if(tool.getItem()==held&&tool.getDamageValue()>damage)generic("7138c68b");held=tool.getItem();damage=tool.getDamageValue();if(mc.player.isUnderWater())generic("b49859e1");
   if(miningPos!=null&&mineRepeats>0&&mc.level.getBlockState(miningPos).isAir())seen("a353e300");
   if(mc.screen==null&&current()==null&&ticks>=nextHint&&(tutorial||ClientConfig.hints))for(var area:Ps4TutorialScript.SOURCE.area_hints){String id="area:"+area.description_hash+java.util.Arrays.toString(area.bounds);if((area.complete_state>=0?menuCompleted(area.complete_state):hintsSeen.contains(id))||!area.contains(mc.player.getX(),mc.player.getY(),mc.player.getZ()))continue;hint=area.text;hintTitle="";hintItem=ItemStack.EMPTY;shownUntil=ticks+Ps4TutorialScript.HINT_TICKS;nextHint=ticks+Ps4TutorialScript.HINT_DELAY_TICKS;if(area.complete_state<0)seen(id);break;}
   if(!ClientConfig.hints||mc.screen!=null||current()!=null||ticks<nextHint)return;
   if(mc.hitResult instanceof BlockHitResult hit){var block=mc.level.getBlockState(hit.getBlockPos());ItemStack item=new ItemStack(block.getBlock());String id=block.getBlock().getDescriptionId();
    if(!hintsSeen.contains(id)){String description=Ps4Descriptions.get(block);if(!description.isEmpty()){hint=description;hintTitle=block.getBlock().getName().getString();hintItem=item;shownUntil=ticks+Ps4TutorialScript.HINT_TICKS;nextHint=ticks+Ps4TutorialScript.HINT_DELAY_TICKS;seen(id);}}
   }
  }
  @SubscribeEvent public void mine(PlayerInteractEvent.LeftClickBlock event){var mc=Minecraft.getInstance();if(event.getPlayer()!=mc.player||!event.getWorld().isClientSide())return;var block=event.getWorld().getBlockState(event.getPos()).getBlock();if(block==mining)mineRepeats++;else mineRepeats=0;mining=block;miningPos=event.getPos();if(mineRepeats>20)generic("a353e300");}
  @SubscribeEvent public void key(InputEvent.KeyInputEvent event){if(event.getAction()==GLFW.GLFW_PRESS&&event.getKey()==GLFW.GLFW_KEY_ENTER)acknowledge(true);}
  @SubscribeEvent public void menuKey(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event){if(event.getKeyCode()==GLFW.GLFW_KEY_PAGE_DOWN&&moreInfo())event.setCanceled(true);else if(event.getKeyCode()==GLFW.GLFW_KEY_ENTER&&acknowledge(true))event.setCanceled(true);else if(event.getKeyCode()==GLFW.GLFW_KEY_ESCAPE&&acknowledge(false))event.setCanceled(true);}
  @SubscribeEvent public void overlay(RenderGameOverlayEvent.Post event){if(event.getType()==RenderGameOverlayEvent.ElementType.ALL&&Minecraft.getInstance().screen==null)render();}
  @SubscribeEvent public void screen(GuiScreenEvent.DrawScreenEvent.Post event){render();}
 }
 private static void render(){
  var mc=Minecraft.getInstance();if(mc.player==null||mc.options.hideGui)return;var task=current();
  if(detailed){Ps4TutorialPopup.draw(hintTitle,hint,"",hintItem);return;}
  if(task!=null){String prompt=ticks-activated>=Ps4TutorialScript.MINIMUM_TICKS?task.prompt:"";String text=task.text;
   if(task.kind.equals("HorseChoiceTask")&&!task.variants.isEmpty()){var horse=mc.player.getVehicle();int variant=horse instanceof net.minecraft.world.entity.animal.horse.Llama?3:horse instanceof net.minecraft.world.entity.animal.horse.Mule?2:horse instanceof net.minecraft.world.entity.animal.horse.Donkey?1:0;text=task.variants.get(variant);}
   if(text.isEmpty()&&task.kind.equals("StateChangeTask"))for(int i=stage-1;i>=0;i--){String prior=Ps4TutorialScript.GAMEPLAY.get(i).text;if(!prior.isEmpty()){text=prior;break;}}
   if(!text.isEmpty())Ps4TutorialPopup.draw("",text,prompt,ItemStack.EMPTY);}
  else if(ClientConfig.hints&&ticks<shownUntil)Ps4TutorialPopup.draw(hintTitle,hint,"",hintItem);
  if(tutorial&&mc.screen==null)Ps4TutorialPopup.goal("You have found "+discs+" of 12 Music Discs!");
 }
 public static String text(String source){return Ps4TutorialText.component(source).getString();}
 public static net.minecraft.network.chat.MutableComponent component(String source){return Ps4TutorialText.component(source);}
 public static boolean canDescribe(net.minecraft.world.inventory.Slot slot){return slot!=null&&!slot.getItem().isEmpty()&&!Ps4Descriptions.get(slot.getItem()).isEmpty();}
 private static boolean moreInfo(){var mc=Minecraft.getInstance();if(!(mc.screen instanceof AbstractContainerScreen<?> container)||mc.screen instanceof ConsoleCraftingScreen||mc.screen instanceof ConsoleSpecialCrafting)return false;var slot=container.getSlotUnderMouse();if(slot==null||slot.getItem().isEmpty())return false;String description=Ps4Descriptions.get(slot.getItem());if(description.isEmpty())return false;hint=description;hintTitle=slot.getItem().getHoverName().getString();hintItem=slot.getItem().copy();detailed=true;return true;}
}

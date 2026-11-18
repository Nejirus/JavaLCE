package dev.legacyconsole.tutorial;

import dev.legacyconsole.LegacyConsole;
import dev.legacyconsole.network.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** World-scoped adaptation of FullTutorial, AreaConstraint and the source GRF rules. */
public final class Ps4TutorialWorld extends SavedData {
 public static final String ID="legacyconsole_ps4_tutorial";
 private boolean tutorial;
 private final Map<UUID,Ps4TutorialProgress> progress=new HashMap<>();
 private final Map<UUID,net.minecraft.world.phys.Vec3> positions=new HashMap<>();
 private final Map<UUID,Integer> sent=new HashMap<>();
 private static final Map<Integer,Item> DISC_ITEMS=new HashMap<>();
 public static final class Rule {public String type;public Map<String,String> attributes;public List<Rule> children=List.of();}
 private static final List<Rule> RULES=List.of(Ps4TutorialScript.read("rules.json",Rule[].class));
 public static Ps4TutorialWorld get(ServerLevel level){return level.getServer().overworld().getDataStorage().computeIfAbsent(Ps4TutorialWorld::load,Ps4TutorialWorld::new,ID);}
 public static Ps4TutorialWorld load(CompoundTag tag){var data=new Ps4TutorialWorld();data.tutorial=tag.getBoolean("PS4Tutorial");CompoundTag players=tag.getCompound("Players");for(String id:players.getAllKeys())data.progress.put(UUID.fromString(id),Ps4TutorialProgress.load(players.getCompound(id),0));if(tag.getInt("Version")<2)data.setDirty();return data;}
 @Override public CompoundTag save(CompoundTag tag){tag.putInt("Version",2);tag.putBoolean("PS4Tutorial",tutorial);var players=new CompoundTag();progress.forEach((id,p)->players.put(id.toString(),p.save()));tag.put("Players",players);return tag;}
 public boolean enabled(){return tutorial;}
 private static Rule rule(String kind){for(Rule root:RULES)for(Rule n:root.children)if(n.type.equals(kind))return n;throw new IllegalStateException("Missing PS4 rule "+kind);}
 private static double number(Map<String,String> attrs,String key){return Double.parseDouble(attrs.get(key));}
 public Ps4TutorialProgress player(ServerPlayer player){return progress.get(player.getUUID());}
 public void join(ServerPlayer player){
  if(!tutorial)return;long tick=player.level.getGameTime();var state=progress.get(player.getUUID());
  if(state==null){
   state=new Ps4TutorialProgress();state.activated=tick;progress.put(player.getUUID(),state);Rule init=rule("UpdatePlayer");var a=init.attributes;
   player.connection.teleport(number(a,"spawnX"),number(a,"spawnY"),number(a,"spawnZ"),(float)number(a,"yRot"),0);
   player.setHealth((float)number(a,"health"));player.getFoodData().setFoodLevel((int)number(a,"food"));
   for(Rule item:init.children){var s=item.attributes;ItemStack stack=legacyItem(Integer.parseInt(s.get("itemId")),Integer.parseInt(s.getOrDefault("auxValue","0")),Integer.parseInt(s.getOrDefault("quantity","1")));player.getInventory().setItem(Integer.parseInt(s.get("slot")),stack);}
   player.getInventory().setChanged();setDirty();
  }else state.activated=tick;
  positions.put(player.getUUID(),player.position());sync(player,state,true);
 }
 private static ItemStack legacyItem(int id,int aux,int count){CompoundTag old=new CompoundTag();old.putShort("id",(short)id);old.putShort("Damage",(short)aux);old.putByte("Count",(byte)count);Tag tag=net.minecraft.util.datafix.DataFixers.getDataFixer().update(net.minecraft.util.datafix.fixes.References.ITEM_STACK,new com.mojang.serialization.Dynamic<Tag>(NbtOps.INSTANCE,old),0,2730).getValue();return ItemStack.of((CompoundTag)tag);}
 private static synchronized void discs(){if(!DISC_ITEMS.isEmpty())return;for(Rule r:rule("CompleteAll").children){var a=r.attributes;DISC_ITEMS.put(Integer.parseInt(a.get("dataTag")),legacyItem(Integer.parseInt(a.get("itemId")),0,1).getItem());}}
 /** CollectItem compares both the item and its original 4jdata tag. */
 public boolean collectDisc(Ps4TutorialProgress state,ItemStack item){discs();if(!item.hasTag())return false;int id=item.getTag().getInt("4jdata");if(id<1||id>12||state.discs.get(id-1)||item.getItem()!=DISC_ITEMS.get(id))return false;state.discs.set(id-1);setDirty();return true;}
 public void input(ServerPlayer player,int action,int mask,int context){
  if(!tutorial)return;var state=this.player(player);if(state==null)return;long tick=player.level.getGameTime();
  if(action==0||action==1){if(!player.isUnderWater())state.acknowledge(action==0,tick);}
  else if(action==2)state.controls(mask&0x1ffff,tick);
  else if(action==3){if(Ps4TutorialProgress.menuLength(context)<0)return;state.menuProgress(context,0,true);var task=state.current();if(task!=null&&task.kind.equals("StateChangeTask")&&task.state==context){state.done[state.stage]=true;state.advance(tick);}}
  else if(action==4){if(!state.menuProgress(context,mask,false))return;}
  setDirty();sync(player,state,false);
 }
 private void sync(ServerPlayer player,Ps4TutorialProgress state,boolean force){
  if(!LegacyConsole.NETWORK.present(player))return;
  int signature=Objects.hash(state.stage,state.flags,state.discs,state.crafted,state.menus,state.completedMenus,Arrays.hashCode(state.done));if(!force&&Objects.equals(sent.get(player.getUUID()),signature))return;
  sent.put(player.getUUID(),signature);LegacyConsole.NETWORK.sendTo(player,new TutorialState(state));
 }
 public void event(ServerPlayer player,String type,ItemStack item){if(!tutorial)return;var p=this.player(player);if(p==null)return;p.event(type,item,player.level.getGameTime());setDirty();sync(player,p,false);}
 /** AreaConstraint permits movement back toward the original box when outside it. */
 public static boolean permits(double ox,double oy,double oz,double x,double y,double z){
  var requested=new net.minecraft.world.phys.Vec3(x,y,z);
  return constrain(new net.minecraft.world.phys.Vec3(ox,oy,oz),requested).equals(requested);
 }
 private static net.minecraft.world.phys.Vec3 constrain(net.minecraft.world.phys.Vec3 from,net.minecraft.world.phys.Vec3 to){
  Rule area=null;for(Rule root:RULES)for(Rule r:root.children)if(r.type.equals("NamedArea")&&r.attributes.get("name").equals("tutorialArea"))area=r;
  if(area==null)throw new IllegalStateException("Missing original tutorial area");var a=area.attributes;
  return Ps4TutorialBounds.constrain(from,to,number(a,"x0"),number(a,"z0"),number(a,"x1"),number(a,"z1"));
 }
 public void tick(ServerPlayer player){
  var state=this.player(player);if(state==null){join(player);state=this.player(player);}if(state==null)return;
  int previousStage=state.stage;var old=positions.getOrDefault(player.getUUID(),player.position());
  if(state.restricted()&&player.level.dimension()==Level.OVERWORLD){var requested=player.position();var allowed=constrain(old,requested);if(!allowed.equals(requested))player.connection.teleport(allowed.x,allowed.y,allowed.z,player.getYRot(),player.getXRot());}
  positions.put(player.getUUID(),player.position());
  var use=state.current();if(use!=null&&use.kind.equals("UseBlockTask")&&((use.block.equals("crafting_table")&&player.containerMenu instanceof dev.legacyconsole.crafting.ConsoleContainer menu&&menu.kind==dev.legacyconsole.crafting.ConsoleContainer.TABLE)||(use.block.equals("furnace")&&player.containerMenu instanceof net.minecraft.world.inventory.FurnaceMenu))){state.done[state.stage]=true;state.advance(player.level.getGameTime());}
  for(ItemStack item:player.getInventory().items){collectDisc(state,item);if(item.isEmpty())continue;var task=state.current();if(task!=null&&task.kind.equals("PickupTask")&&task.matches(item)){int count=0;for(ItemStack held:player.getInventory().items)if(task.matches(held))count+=held.getCount();state.pickup(item,count,player.level.getGameTime());}}
  if(previousStage!=state.stage)setDirty();sync(player,state,false);
 }
 public static final class Events {
  @SubscribeEvent public void joined(PlayerEvent.PlayerLoggedInEvent e){if(e.getPlayer() instanceof ServerPlayer p)get(p.getLevel()).join(p);}
  @SubscribeEvent public void tick(TickEvent.ServerTickEvent e){if(e.phase!=TickEvent.Phase.END)return;var server=net.minecraftforge.fmllegacy.server.ServerLifecycleHooks.getCurrentServer();if(server==null)return;var data=get(server.overworld());if(!data.tutorial)return;boolean finished=true;for(ServerPlayer p:server.getPlayerList().getPlayers()){data.tick(p);var progress=data.player(p);if(progress!=null&&progress.current()!=null)finished=false;}if(!server.getPlayerList().getPlayers().isEmpty()){boolean day=server.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);if(day!=finished)server.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(finished,server);if(!finished)server.overworld().setDayTime(8000);}}
  @SubscribeEvent public void crafted(PlayerEvent.ItemCraftedEvent e){if(e.getPlayer() instanceof ServerPlayer p)get(p.getLevel()).event(p,"CraftTask",e.getCrafting());}
  @SubscribeEvent public void smelted(PlayerEvent.ItemSmeltedEvent e){if(e.getPlayer() instanceof ServerPlayer p)get(p.getLevel()).event(p,"CraftTask",e.getSmelting());}
  @SubscribeEvent public void eaten(LivingEntityUseItemEvent.Finish e){if(e.getEntityLiving() instanceof ServerPlayer p)get(p.getLevel()).event(p,"CompleteUsingItemTask",e.getItem());}
  @SubscribeEvent public void placed(BlockEvent.EntityPlaceEvent e){if(!e.isCanceled()&&e.getEntity() instanceof ServerPlayer p)get(p.getLevel()).event(p,"UseItemTask",new ItemStack(e.getPlacedBlock().getBlock()));}
 }
}

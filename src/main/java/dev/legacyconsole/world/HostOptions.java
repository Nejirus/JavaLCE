package dev.legacyconsole.world;

import dev.legacyconsole.LegacyConsole;
import dev.legacyconsole.network.HostState;
import java.util.UUID;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;

/** PS4 host/player option IDs from 0xa57c30/0xa5d010, over Java server APIs. */
public final class HostOptions extends SavedData {
 public static final int OPEN=-1, TELEPORT_TO=200, TELEPORT_HERE=201;
 public static final String ID="legacyconsole_host_options";
 private final CompoundTag players=new CompoundTag();
 private boolean tnt=true;
 // Native control IDs 0..9,18; the gap contains the action/slider controls.
 private static final GameRules.Key<GameRules.BooleanValue>[] RULES=new GameRules.Key[]{
  GameRules.RULE_DOFIRETICK,null,GameRules.RULE_DAYLIGHT,GameRules.RULE_KEEPINVENTORY,
  GameRules.RULE_DOMOBSPAWNING,GameRules.RULE_MOBGRIEFING,GameRules.RULE_DOMOBLOOT,
  GameRules.RULE_DOBLOCKDROPS,GameRules.RULE_NATURAL_REGENERATION,GameRules.RULE_WEATHER_CYCLE};
 public static HostOptions get(MinecraftServer server){return server.overworld().getDataStorage().computeIfAbsent(HostOptions::load,HostOptions::new,ID);}
 public static HostOptions load(CompoundTag tag){HostOptions h=new HostOptions();h.tnt=!tag.contains("TNTExplodes")||tag.getBoolean("TNTExplodes");h.players.merge(tag.getCompound("Players"));return h;}
 @Override public CompoundTag save(CompoundTag tag){tag.putInt("Version",1);tag.putBoolean("TNTExplodes",tnt);tag.put("Players",players.copy());return tag;}
 public static boolean enabled(MinecraftServer server){return server!=null&&server.overworld()!=null&&!server.isHardcore()&&server.getWorldData().getAllowCommands()&&!WorldPolicy.get(server.overworld()).locked();}
 public static boolean owner(ServerPlayer p){return p.getServer().isSingleplayerOwner(p.getGameProfile())||p.getServer().isDedicatedServer()&&p.hasPermissions(4);}
 private CompoundTag flags(UUID id){return players.getCompound(id.toString());}
 private void store(UUID id,CompoundTag flags){players.put(id.toString(),flags);setDirty();}
 private boolean moderator(ServerPlayer p){return owner(p)||flags(p.getUUID()).getBoolean("moderator");}
 public static boolean allowed(ServerPlayer p,String key){if(!enabled(p.getServer()))return true;return !get(p.getServer()).flags(p.getUUID()).contains(key)||get(p.getServer()).flags(p.getUUID()).getBoolean(key);}
 public static boolean noExhaustion(Player p){return p instanceof ServerPlayer server&&enabled(server.getServer())&&get(server.getServer()).flags(p.getUUID()).getBoolean("exhaustion");}
 public static boolean mayRequest(boolean enabled,boolean moderator,boolean self,boolean grant,int action){
  if(!enabled)return false;
  if(action==OPEN)return true;
  if(action>=0&&action<=18||action==TELEPORT_TO||action==TELEPORT_HERE)return moderator||grant&&(action==TELEPORT_TO||action==TELEPORT_HERE);
  return action>=100&&action<=112&&(moderator||self&&grant&&(action==107||action==108||action==109));
 }
 public static void request(ServerPlayer sender,int action,int value,UUID targetId){
  MinecraftServer server=sender.getServer();HostOptions h=get(server);
  if(enabled(server))h.apply(sender,action,value,targetId);
  LegacyConsole.NETWORK.sendTo(sender,new HostState(h.snapshot(sender)));
 }
 private void apply(ServerPlayer sender,int action,int value,UUID targetId){
  MinecraftServer server=sender.getServer();boolean admin=moderator(sender);
  ServerPlayer target=targetId==null?sender:server.getPlayerList().getPlayer(targetId);
  boolean self=target!=null&&target.getUUID().equals(sender.getUUID());
  CompoundTag own=flags(sender.getUUID());String grant=action==107?"allowFly":action==108?"allowExhaustion":action==109?"allowInvisible":"teleport";
  if(!mayRequest(true,admin,self,own.getBoolean(grant),action))return;
  if(action>=0&&action<RULES.length&&RULES[action]!=null){server.getGameRules().getRule(RULES[action]).set(value!=0,server);return;}
  if(action==1){tnt=value!=0;setDirty();return;}
  switch(action){
   case 12->{for(var level:server.getAllLevels())level.setDayTime(6000);}
   case 13->{for(var level:server.getAllLevels())level.setDayTime(18000);}
   case 14->server.overworld().setDefaultSpawnPos(sender.blockPosition(),sender.getYRot());
   case 15->{if(value>=0&&value<=3)server.setDifficulty(Difficulty.byId(value),true);}
   case 16->{if(value>=0&&value<=2){GameType mode=GameType.byId(value);server.setDefaultGameType(mode);for(var p:server.getPlayerList().getPlayers())p.setGameMode(mode);}}
   case 17->{if(value>=0&&value<=2)server.overworld().setWeatherParameters(value==0?6000:0,value==0?0:6000,value!=0,value==2);}
   case 18->server.getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(value!=0,server);
   case TELEPORT_TO,TELEPORT_HERE->{
    if(target==null||self)return;ServerPlayer from=action==TELEPORT_TO?sender:target,to=action==TELEPORT_TO?target:sender;
    from.teleportTo(to.getLevel(),to.getX(),to.getY(),to.getZ(),to.getYRot(),to.getXRot());
   }
   default->{if(target!=null&&action>=100&&action<=112)playerOption(sender,target,action-100,value,admin,self);}
  }
 }
 private void playerOption(ServerPlayer sender,ServerPlayer target,int id,int value,boolean admin,boolean self){
  CompoundTag f=flags(target.getUUID());boolean on=value!=0;
  if(id<5){if(!admin||owner(target))return;f.putBoolean(new String[]{"build","doors","containers","attackPlayers","attackAnimals"}[id],on);}
  else if(id==5){if(!owner(sender)||owner(target))return;f.putBoolean("moderator",on);}
  else if(id==6){if(!admin)return;f.putBoolean("teleport",on);}
  else if(id>=7&&id<=9){
   String permission=new String[]{"allowFly","allowExhaustion","allowInvisible"}[id-7];
   String active=new String[]{"fly","exhaustion","invisible"}[id-7];
   if(self){if(!admin&&!f.getBoolean(permission))return;f.putBoolean(active,on);}
   else{if(!admin)return;f.putBoolean(permission,on);if(id==7||!on)f.putBoolean(active,on);}
  }else if(id==10){if(!admin||owner(target)||self)return;target.connection.disconnect(new net.minecraft.network.chat.TranslatableComponent("multiplayer.disconnect.kicked"));return;}
  else if(id==11){if(!admin||value<0||value>2)return;target.setGameMode(GameType.byId(value));return;}
  else if(id==12){if(!admin)return;target.setRespawnPosition(target.getLevel().dimension(),target.blockPosition(),target.getYRot(),true,false);return;}
  store(target.getUUID(),f);abilities(target);
 }
 private void abilities(ServerPlayer p){
  CompoundTag f=flags(p.getUUID());boolean enabled=enabled(p.getServer());
  if(f.contains("fly")){
   boolean mayfly=enabled?f.getBoolean("fly"):p.isCreative()||p.isSpectator();
   var a=p.getAbilities();if(a.mayfly!=mayfly||!mayfly&&a.flying){a.mayfly=mayfly;if(!mayfly)a.flying=false;p.onUpdateAbilities();}
  }
  if(f.contains("invisible"))p.setInvisible(enabled&&f.getBoolean("invisible")||p.hasEffect(MobEffects.INVISIBILITY)||p.isSpectator());
 }
 public CompoundTag snapshot(ServerPlayer viewer){
  MinecraftServer server=viewer.getServer();CompoundTag tag=new CompoundTag();tag.putBoolean("enabled",enabled(server));
  if(!enabled(server))return tag;
  tag.putBoolean("owner",owner(viewer));tag.putBoolean("admin",moderator(viewer));tag.putUUID("viewer",viewer.getUUID());
  for(int i=0;i<RULES.length;i++)if(RULES[i]!=null)tag.putBoolean("rule"+i,server.getGameRules().getBoolean(RULES[i]));
  tag.putBoolean("rule1",tnt);tag.putBoolean("rule18",server.getGameRules().getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN));
  tag.putInt("difficulty",server.getWorldData().getDifficulty().getId());tag.putInt("mode",server.getDefaultGameType().getId());
  tag.putInt("weather",server.overworld().isThundering()?2:server.overworld().isRaining()?1:0);
  ListTag list=new ListTag();for(var p:server.getPlayerList().getPlayers()){
   CompoundTag item=flags(p.getUUID()).copy();item.putUUID("uuid",p.getUUID());item.putString("name",p.getGameProfile().getName());
   item.putInt("colorIndex",Ps4PlayerColors.index(p));item.putBoolean("owner",owner(p));item.putInt("mode",p.gameMode.getGameModeForPlayer().getId());
   item.putBoolean("fly",p.getAbilities().mayfly);list.add(item);
  }tag.put("players",list);return tag;
 }
 public static final class Events {
  @SubscribeEvent public void tick(TickEvent.ServerTickEvent e){if(e.phase!=TickEvent.Phase.END)return;var s=ServerLifecycleHooks.getCurrentServer();if(s!=null&&s.overworld()!=null){var h=get(s);for(var p:s.getPlayerList().getPlayers())h.abilities(p);}}
  @SubscribeEvent public void explosion(ExplosionEvent.Start e){if(!e.getWorld().isClientSide&&e.getWorld().getServer()!=null&&enabled(e.getWorld().getServer())&&!get(e.getWorld().getServer()).tnt){var source=e.getExplosion().getExploder();if(source instanceof PrimedTnt||source instanceof MinecartTNT)e.setCanceled(true);}}
  @SubscribeEvent public void breakBlock(BlockEvent.BreakEvent e){if(e.getPlayer() instanceof ServerPlayer p&&!allowed(p,"build"))e.setCanceled(true);}
  @SubscribeEvent public void place(BlockEvent.EntityPlaceEvent e){if(e.getEntity() instanceof ServerPlayer p&&!allowed(p,"build"))e.setCanceled(true);}
  @SubscribeEvent public void use(PlayerInteractEvent.RightClickBlock e){
   if(!(e.getPlayer() instanceof ServerPlayer p))return;var b=e.getWorld().getBlockState(e.getPos()).getBlock();
   boolean door=b instanceof DoorBlock||b instanceof TrapDoorBlock||b instanceof FenceGateBlock||b instanceof ButtonBlock||b instanceof LeverBlock;
   boolean container=e.getWorld().getBlockEntity(e.getPos()) instanceof MenuProvider;
   if(door&&!allowed(p,"doors")||container&&!allowed(p,"containers")||!door&&!container&&!allowed(p,"build"))e.setCanceled(true);
   else if(!allowed(p,"build"))e.setUseItem(net.minecraftforge.eventbus.api.Event.Result.DENY);
  }
  @SubscribeEvent public void attack(AttackEntityEvent e){if(e.getPlayer() instanceof ServerPlayer p&&(e.getTarget() instanceof Player&&!allowed(p,"attackPlayers")||e.getTarget() instanceof Animal&&!allowed(p,"attackAnimals")||(e.getTarget() instanceof net.minecraft.world.entity.decoration.HangingEntity||e.getTarget() instanceof net.minecraft.world.entity.decoration.ArmorStand)&&!allowed(p,"build")))e.setCanceled(true);}
  @SubscribeEvent public void ranged(net.minecraftforge.event.entity.living.LivingAttackEvent e){if(e.getSource().getEntity() instanceof ServerPlayer p&&(e.getEntityLiving() instanceof Player&&!allowed(p,"attackPlayers")||e.getEntityLiving() instanceof Animal&&!allowed(p,"attackAnimals")))e.setCanceled(true);}
  @SubscribeEvent public void entityUse(PlayerInteractEvent.EntityInteract e){if(e.getPlayer() instanceof ServerPlayer p&&(e.getTarget() instanceof MenuProvider&&!allowed(p,"containers")||(e.getTarget() instanceof net.minecraft.world.entity.decoration.HangingEntity||e.getTarget() instanceof net.minecraft.world.entity.decoration.ArmorStand)&&!allowed(p,"build")))e.setCanceled(true);}
  @SubscribeEvent public void useItem(PlayerInteractEvent.RightClickItem e){
   if(!(e.getPlayer() instanceof ServerPlayer p)||allowed(p,"build"))return;var item=e.getItemStack().getItem();
   if(item instanceof BlockItem||item instanceof BucketItem||item instanceof SolidBucketItem||item instanceof FlintAndSteelItem||item instanceof FireChargeItem||item instanceof SpawnEggItem||item instanceof ArmorStandItem||item instanceof EndCrystalItem||item instanceof BoatItem||item instanceof MinecartItem)e.setCanceled(true);
  }
 }
}

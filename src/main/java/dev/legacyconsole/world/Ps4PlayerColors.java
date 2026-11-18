package dev.legacyconsole.world;

import dev.legacyconsole.LegacyConsole;
import dev.legacyconsole.network.PlayerColorState;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** mcps4 cc3a66..cc3b7f slot allocation and cd1a00's original name-tag palette. */
public final class Ps4PlayerColors {
 public static final int SLOTS=16;
 private static final int[] PALETTE={0xff000000,0xff33cc33,0xffcc3333,0xff3333cc,
  0xffcc33cc,0xffcc6633,0xffcccc33,0xff33dccc,0,0,0,0,0,0,0,0};
 private static final Map<MinecraftServer,Slots> SERVERS=new WeakHashMap<>();
 private Ps4PlayerColors(){}
 public static int nameTagColor(int slot){return slot>=0&&slot<SLOTS?PALETTE[slot]:0xff000000;}
 public static int firstFree(Collection<Integer> occupied){
  boolean[] used=new boolean[SLOTS];for(int i:occupied)if(i>=0&&i<SLOTS)used[i]=true;
  for(int i=0;i<SLOTS;i++)if(!used[i])return i;
  return 0;
 }
 public static final class Slots {
  private final Map<UUID,Integer> players=new LinkedHashMap<>();
  public int join(UUID id){return players.computeIfAbsent(id,key->firstFree(players.values()));}
  public void leave(UUID id){players.remove(id);}
  public int index(UUID id){return players.getOrDefault(id,-1);}
  public Map<UUID,Integer> snapshot(){return Map.copyOf(players);}
 }
 private static Slots slots(MinecraftServer server){return SERVERS.computeIfAbsent(server,key->new Slots());}
 public static int index(ServerPlayer player){return slots(player.getServer()).join(player.getUUID());}
 public static CompoundTag snapshot(MinecraftServer server){
  CompoundTag state=new CompoundTag();ListTag list=new ListTag();
  for(var e:slots(server).snapshot().entrySet()){
   CompoundTag p=new CompoundTag();p.putUUID("uuid",e.getKey());p.putInt("index",e.getValue());list.add(p);
  }
  state.put("players",list);return state;
 }
 private static void broadcast(MinecraftServer server,UUID excluded){
  var packet=new PlayerColorState(snapshot(server));
  for(var p:server.getPlayerList().getPlayers())if(!p.getUUID().equals(excluded)&&LegacyConsole.NETWORK.present(p))LegacyConsole.NETWORK.sendTo(p,packet);
 }
 public static final class Events {
  @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getPlayer() instanceof ServerPlayer p){
   for(var online:p.getServer().getPlayerList().getPlayers())index(online);
   index(p);broadcast(p.getServer(),null);
  }}
  @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getPlayer() instanceof ServerPlayer p){slots(p.getServer()).leave(p.getUUID());broadcast(p.getServer(),p.getUUID());}}
  @SubscribeEvent public void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getPlayer() instanceof ServerPlayer p){index(p);if(LegacyConsole.NETWORK.present(p))LegacyConsole.NETWORK.sendTo(p,new PlayerColorState(snapshot(p.getServer())));}}
 }
}

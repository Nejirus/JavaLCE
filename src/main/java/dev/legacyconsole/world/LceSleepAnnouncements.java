package dev.legacyconsole.world;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.*;
import net.minecraft.world.level.GameRules;

/** BedTile / ServerPlayer::displayClientMessage broadcast one message per new sleeper. */
public final class LceSleepAnnouncements {
 private static final Map<ServerLevel,Set<UUID>> PREVIOUS=new WeakHashMap<>();
 private static final Map<String,String> TEXT=read();
 private LceSleepAnnouncements() {}
 public static boolean update(ServerLevel level) {
  // Native 1.17 servers may choose a percentage unavailable in LCE; retain that rule and its text.
  if(level.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE)!=100){PREVIOUS.remove(level);return false;}
  List<ServerPlayer> eligible=level.players().stream().filter(p->!p.isSpectator()).toList();
  Set<UUID> sleeping=new HashSet<>();for(var p:eligible)if(p.isSleeping())sleeping.add(p.getUUID());
  Set<UUID> before=PREVIOUS.put(level,sleeping);
  if(!level.canSleepThroughNights()||level.getServer().isSingleplayer()&&!level.getServer().isPublished()
      ||sleeping.isEmpty()||sleeping.size()==eligible.size())return true;
  Set<UUID> added=newSleepers(before,sleeping,eligible.size());
  for(var sleeper:eligible)if(added.contains(sleeper.getUUID())) {
   for(var recipient:level.players()) {
    String text=message(recipient==sleeper,sleeper.getDisplayName().getString());
    recipient.displayClientMessage(new TextComponent(text),false);
   }
  }
  return true;
 }
 public static Set<UUID> newSleepers(Set<UUID> before,Set<UUID> current,int eligible) {
  if(current.isEmpty()||current.size()>=eligible)return Set.of();
  Set<UUID> added=new HashSet<>(current);if(before!=null)added.removeAll(before);return added;
 }
 public static String message(boolean self,String name) {
  return self?TEXT.get("IDS_TILE_BED_MESLEEP"):TEXT.get("IDS_TILE_BED_PLAYERSLEEP").replace("%s",name);
 }
 private static Map<String,String> read() {
  try(var in=new InputStreamReader(LceSleepAnnouncements.class.getResourceAsStream(
      "/assets/legacyconsole/sleep_messages.json"),StandardCharsets.UTF_8)) {
   return new Gson().fromJson(in,new com.google.gson.reflect.TypeToken<Map<String,String>>(){}.getType());
  }catch(Exception e){throw new ExceptionInInitializerError(e);}
 }
}

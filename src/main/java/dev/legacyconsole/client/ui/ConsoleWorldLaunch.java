package dev.legacyconsole.client.ui;
import net.minecraft.client.Minecraft;import net.minecraft.world.Difficulty;import net.minecraft.world.level.GameType;
import dev.legacyconsole.world.WorldPolicy;
public final class ConsoleWorldLaunch {
 private static String folder;private static GameType mode;private static Difficulty difficulty;private static Boolean fire;
 public static void prepare(String save,GameType type,Difficulty level){folder=save;mode=type;difficulty=level;fire=null;}
 public static void prepare(String save,GameType type,Difficulty level,boolean fireSpreads){prepare(save,type,level);fire=fireSpreads;}
 public static void clear(){folder=null;fire=null;}
 public static void tick(Minecraft mc){if(folder==null||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;var server=mc.getSingleplayerServer();String actual=server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).normalize().getFileName().toString();if(!folder.equals(actual)){clear();return;}
  var id=mc.player.getUUID();var selected=mode;var diff=difficulty;var fireRule=fire;clear();server.execute(()->{var policy=WorldPolicy.get(server.overworld());if(policy.locked()){policy.enforce(server);if(!policy.difficultyLocked())server.setDifficulty(diff,true);return;}var p=server.getPlayerList().getPlayer(id);if(p!=null&&!(server.isHardcore()&&p.isSpectator()))p.setGameMode(selected);server.setDifficulty(diff,true);if(fireRule!=null)server.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOFIRETICK).set(fireRule,server);});
 }
}

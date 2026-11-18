package dev.legacyconsole.world;
import java.io.*;import java.util.*;
import net.minecraft.nbt.*;import net.minecraft.world.Difficulty;import net.minecraft.world.level.*;
import net.minecraft.world.level.saveddata.SavedData;import net.minecraft.world.level.storage.*;
import net.minecraft.server.MinecraftServer;import net.minecraft.server.level.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;import net.minecraftforge.event.TickEvent;import net.minecraftforge.event.world.WorldEvent;import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import dev.legacyconsole.port.WorldInfo;
public final class WorldPolicy extends SavedData {
 public static final String ID="legacyconsole_origin";
 private int mode=GameType.SURVIVAL.getId(),difficulty=Difficulty.NORMAL.getId();
 private boolean privileges,hardcore,fire=true,extended;
 private record Creation(String folder,WorldPolicy policy){}private static volatile Creation pending;
 public WorldPolicy(){}public WorldPolicy(String ignored){}
 private WorldPolicy(GameType mode,boolean privileges,boolean hardcore,Difficulty difficulty,boolean fire){this.mode=mode.getId();this.privileges=privileges;this.hardcore=hardcore;this.difficulty=(hardcore?Difficulty.HARD:difficulty).getId();this.fire=fire;extended=true;setDirty();}
 public static void prepareCreation(String folder,GameType mode,boolean privileges,boolean hardcore,Difficulty difficulty,boolean fire){pending=new Creation(folder,new WorldPolicy(mode,privileges,hardcore,difficulty,fire));}
 public static boolean locked(net.minecraft.server.MinecraftServer server){return server!=null&&!server.isDedicatedServer()&&server.overworld()!=null&&get(server.overworld()).locked();}
 public boolean locked(){return hardcore||mode==GameType.SURVIVAL.getId()&&!privileges;}public boolean hardcore(){return hardcore;}public Difficulty difficulty(){return Difficulty.byId(difficulty);}public GameType createdMode(){return GameType.byId(mode);}
 public static WorldPolicy load(CompoundTag tag){WorldPolicy policy=new WorldPolicy();policy.readFromNBT(tag);return policy;}
 public void readFromNBT(CompoundTag tag){mode=tag.getInt("CreatedGameType");privileges=tag.getBoolean("CreatedWithHostPrivileges");extended=tag.contains("CreatedDifficulty",99);if(extended){difficulty=tag.getInt("CreatedDifficulty");hardcore=tag.getBoolean("CreatedHardcore");fire=tag.getBoolean("CreatedFireSpreads");}}
 @Override public CompoundTag save(CompoundTag tag){tag.putInt("Version",2);tag.putInt("CreatedGameType",mode);tag.putBoolean("CreatedWithHostPrivileges",privileges);tag.putInt("CreatedDifficulty",difficulty);tag.putBoolean("CreatedHardcore",hardcore);tag.putBoolean("CreatedFireSpreads",fire);return tag;}
 public CompoundTag writeToNBT(CompoundTag tag){return save(tag);}
 private void extend(boolean hard,Difficulty diff,boolean fire){if(!extended){hardcore=hard;difficulty=(hard?Difficulty.HARD:diff).getId();this.fire=fire;extended=true;setDirty();}}
 public boolean difficultyLocked(){return hardcore;}
 public void enforce(WorldInfo info){info.setDifficultyLocked(difficultyLocked());if(!locked())return;info.setGameMode(GameType.SURVIVAL);info.setAllowCommands(false);info.setHardcore(hardcore);if(hardcore)info.setDifficulty(Difficulty.HARD);info.getGameRules().setOrCreateGameRule("doFireTick",Boolean.toString(fire));}
 public void enforce(MinecraftServer server){if(server.isDedicatedServer())return;WorldData data=server.getWorldData();data.setDifficultyLocked(difficultyLocked());if(!locked())return;
  if(data instanceof PrimaryLevelData primary&&(data.getGameType()!=GameType.SURVIVAL||data.getAllowCommands()||hardcore&&data.getDifficulty()!=Difficulty.HARD||data.isHardcore()!=hardcore)){
   LevelSettings old=primary.getLevelSettings();primary.settings=new LevelSettings(old.levelName(),GameType.SURVIVAL,hardcore,hardcore?Difficulty.HARD:data.getDifficulty(),false,old.gameRules(),old.getDataPackConfig());
  }
  if(hardcore&&data.getDifficulty()!=Difficulty.HARD)server.setDifficulty(Difficulty.HARD,true);data.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(fire,server);
  server.getPlayerList().setAllowCheatsForAllPlayers(false);
  for(ServerPlayer player:server.getPlayerList().getPlayers())if(player.gameMode.getGameModeForPlayer()!=GameType.SURVIVAL&&!(hardcore&&player.isSpectator()))player.setGameMode(GameType.SURVIVAL);
 }
 public static WorldPolicy get(ServerLevel level){ServerLevel root=level.getServer().overworld();WorldData data=root.getServer().getWorldData();WorldPolicy policy=root.getDataStorage().computeIfAbsent(WorldPolicy::load,()->{
   Creation p=pending;String folder=root.getServer().getWorldPath(LevelResource.ROOT).normalize().getFileName().toString();
   if(p!=null&&p.folder.equals(folder)){pending=null;return p.policy;}
   return new WorldPolicy(data.getGameType(),data.getAllowCommands(),data.isHardcore(),data.getDifficulty(),data.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK));
  },ID);policy.extend(data.isHardcore(),data.getDifficulty(),data.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK));return policy;}
 public static WorldPolicy read(File folder,WorldInfo info){File file=new File(folder,"data/"+ID+".dat");WorldPolicy policy=null;
  if(file.isFile())try{policy=load(NbtIo.readCompressed(file).getCompound("data"));}catch(IOException e){throw new IllegalStateException("Cannot read saved world origin",e);}
  if(policy==null)policy=new WorldPolicy(info.getGameType(),info.areCommandsAllowed(),info.isHardcoreModeEnabled(),info.getDifficulty(),info.getGameRules().getBoolean("doFireTick"));
  policy.extend(info.isHardcoreModeEnabled(),info.getDifficulty(),info.getGameRules().getBoolean("doFireTick"));return policy;
 }
 public static void preserve(File folder,WorldPolicy policy){File dir=new File(folder,"data");if(!dir.isDirectory()&&!dir.mkdirs())throw new IllegalStateException("Cannot create world data directory");File file=new File(dir,ID+".dat");if(!file.exists()){policy.setDirty();policy.save(file);}else try{WorldPolicy existing=load(NbtIo.readCompressed(file).getCompound("data"));if(!existing.extended){existing.extend(policy.hardcore,policy.difficulty(),policy.fire);existing.save(file);}}catch(IOException e){throw new IllegalStateException("Cannot migrate world origin",e);}}
 public static final class Enforcer {
  @SubscribeEvent public void loaded(WorldEvent.Load event){if(event.getWorld() instanceof ServerLevel level&&!level.getServer().isDedicatedServer()&&level.dimension()==Level.OVERWORLD)get(level).enforce(level.getServer());}
  @SubscribeEvent public void tick(TickEvent.ServerTickEvent event){if(event.phase!=TickEvent.Phase.END)return;MinecraftServer server=ServerLifecycleHooks.getCurrentServer();if(server!=null&&!server.isDedicatedServer()&&server.overworld()!=null)get(server.overworld()).enforce(server);}
  @SubscribeEvent public void command(CommandEvent event){MinecraftServer server=event.getParseResults().getContext().getSource().getServer();if(server==null||server.isDedicatedServer()||server.overworld()==null||!get(server.overworld()).locked())return;
   String raw=event.getParseResults().getReader().getString().trim().replaceFirst("^/","");String name=raw.split(" +",2)[0];if(Set.of("gamemode","defaultgamemode","publish","op","deop").contains(name)||name.equals("difficulty")&&get(server.overworld()).difficultyLocked()||raw.matches("gamerule +doFireTick +.*")){event.setCanceled(true);event.getParseResults().getContext().getSource().sendFailure(new net.minecraft.network.chat.TextComponent("This world's original Survival/Hardcore rules are locked."));}
  }
 }
}

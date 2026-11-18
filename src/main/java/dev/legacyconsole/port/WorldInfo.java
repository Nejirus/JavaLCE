package dev.legacyconsole.port;
import java.io.*;import java.nio.file.*;
import net.minecraft.nbt.*;import net.minecraft.world.Difficulty;import net.minecraft.world.level.GameType;
public final class WorldInfo {
 private final CompoundTag root,data;private final File folder;
 public WorldInfo(File folder)throws IOException{this.folder=folder;root=NbtIo.readCompressed(new File(folder,"level.dat"));data=root.getCompound("Data");}
 public GameType getGameType(){return GameType.byId(data.getInt("GameType"));}public void setGameMode(GameType mode){data.putInt("GameType",mode.getId());}
 public boolean isHardcoreModeEnabled(){return data.getBoolean("hardcore");}public void setHardcore(boolean value){data.putBoolean("hardcore",value);}
 public boolean areCommandsAllowed(){return data.getBoolean("allowCommands");}public void setAllowCommands(boolean value){data.putBoolean("allowCommands",value);}
 public Difficulty getDifficulty(){return Difficulty.byId(data.getByte("Difficulty"));}public void setDifficulty(Difficulty value){data.putByte("Difficulty",(byte)value.getId());}
 public boolean isDifficultyLocked(){return data.getBoolean("DifficultyLocked");}public void setDifficultyLocked(boolean b){data.putBoolean("DifficultyLocked",b);}
 public String getWorldName(){return data.getString("LevelName");}
 public CompoundTag generatorTag(){return data.getCompound("WorldGenSettings");}
 public long getSeed(){return data.getCompound("WorldGenSettings").getLong("seed");}
 public Rules getGameRules(){return new Rules(data.getCompound("GameRules"));}
 public static final class Rules{private final CompoundTag tag;Rules(CompoundTag tag){this.tag=tag;}public boolean getBoolean(String key){return Boolean.parseBoolean(tag.getString(key));}public void setOrCreateGameRule(String key,String value){tag.putString(key,value);}}
 public void save(){try{File tmp=new File(folder,"level.dat_new");NbtIo.writeCompressed(root,tmp);net.minecraft.Util.safeReplaceFile(new File(folder,"level.dat"),tmp,new File(folder,"level.dat_old"));}catch(IOException e){throw new IllegalStateException("Cannot save selected world options",e);}}
}

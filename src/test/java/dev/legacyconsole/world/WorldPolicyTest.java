package dev.legacyconsole.world;

import static org.junit.Assert.*;

import java.io.File;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import dev.legacyconsole.port.WorldInfo;
import net.minecraft.nbt.*;
import java.io.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

public class WorldPolicyTest {
  @BeforeClass public static void version(){net.minecraft.SharedConstants.tryDetectVersion();}
  @Rule public TemporaryFolder files = new TemporaryFolder();

  private WorldInfo info(GameType type,boolean cheats)throws Exception {
    File folder=files.newFolder();CompoundTag root=new CompoundTag(),data=new CompoundTag(),rules=new CompoundTag();data.putInt("GameType",type.getId());data.putBoolean("allowCommands",cheats);data.putByte("Difficulty",(byte)2);rules.putString("doFireTick","true");data.put("GameRules",rules);root.put("Data",data);NbtIo.writeCompressed(root,new File(folder,"level.dat"));return new WorldInfo(folder);
  }

  @Test
  public void survivalWithoutPrivilegesStaysLockedAfterEditsAndReload() throws Exception {
    File folder = files.newFolder();
    WorldInfo info = info(GameType.SURVIVAL, false);
    WorldPolicy origin = WorldPolicy.read(folder, info);
    assertTrue(origin.locked());
    WorldPolicy.preserve(folder, origin);
    info.setGameMode(GameType.CREATIVE);
    info.setAllowCommands(true);
    WorldPolicy loaded = WorldPolicy.read(folder, info);
    assertTrue(loaded.locked());
    assertEquals(GameType.SURVIVAL, loaded.createdMode());
    WorldPolicy.preserve(folder, WorldPolicy.read(files.newFolder(), info));
    assertTrue(WorldPolicy.read(folder, info).locked());
  }

  @Test
  public void creativeOriginsRemainUnlockedWhenLaterSavedInSurvival() throws Exception {
    File folder = files.newFolder();
    WorldInfo info = info(GameType.CREATIVE, true);
    WorldPolicy.preserve(folder, WorldPolicy.read(folder, info));
    info.setGameMode(GameType.SURVIVAL);
    info.setAllowCommands(false);
    assertFalse(WorldPolicy.read(folder, info).locked());
    assertEquals(GameType.CREATIVE, WorldPolicy.read(folder, info).createdMode());
  }

  @Test
  public void survivalCreatedWithPrivilegesIsNotRetroactivelyLocked() throws Exception {
    File folder = files.newFolder();
    WorldInfo info = info(GameType.SURVIVAL, true);
    WorldPolicy.preserve(folder, WorldPolicy.read(folder, info));
    info.setAllowCommands(false);
    assertFalse(WorldPolicy.read(folder, info).locked());
  }

  @Test public void hardcoreForcesSurvivalHardAndRemovesPrivilegesAcrossReload() throws Exception {
    File folder=files.newFolder();WorldInfo info=info(GameType.CREATIVE,true);
    info.setHardcore(true);info.setDifficulty(Difficulty.EASY);
    WorldPolicy policy=WorldPolicy.read(folder,info);assertTrue(policy.locked());
    WorldPolicy.preserve(folder,policy);info.setHardcore(false);info.setDifficulty(Difficulty.PEACEFUL);
    WorldPolicy.read(folder,info).enforce(info);
    assertTrue(info.isHardcoreModeEnabled());assertEquals(GameType.SURVIVAL,info.getGameType());
    assertEquals(Difficulty.HARD,info.getDifficulty());assertTrue(info.isDifficultyLocked());assertFalse(info.areCommandsAllowed());
  }

  @Test public void survivalDifficultyRemainsEditableWhileHostRulesStayLocked() throws Exception {
    File folder=files.newFolder();WorldInfo info=info(GameType.SURVIVAL,false);
    info.setDifficulty(Difficulty.EASY);info.getGameRules().setOrCreateGameRule("doFireTick","false");
    WorldPolicy.preserve(folder,WorldPolicy.read(folder,info));
    info.setDifficulty(Difficulty.HARD);info.setDifficultyLocked(true);info.setAllowCommands(true);
    info.getGameRules().setOrCreateGameRule("doFireTick","true");
    WorldPolicy.read(folder,info).enforce(info);
    assertEquals(Difficulty.HARD,info.getDifficulty());assertFalse(info.isDifficultyLocked());
    assertFalse(info.areCommandsAllowed());assertFalse(info.getGameRules().getBoolean("doFireTick"));
  }

  @Test public void versionOneMigrationCapturesCurrentDifficultyOnceWithoutChangingOrigin() throws Exception {
    File folder=files.newFolder();File target=new File(folder,"data/"+WorldPolicy.ID+".dat");target.getParentFile().mkdirs();
    CompoundTag old=new CompoundTag();old.putInt("Version",1);old.putInt("CreatedGameType",0);old.putBoolean("CreatedWithHostPrivileges",false);
    CompoundTag root=new CompoundTag();root.put("data",old);
    try(OutputStream out=new FileOutputStream(target)) {NbtIo.writeCompressed(root,out);}
    WorldInfo info=info(GameType.SURVIVAL,false);info.setDifficulty(Difficulty.EASY);
    WorldPolicy policy=WorldPolicy.read(folder,info);WorldPolicy.preserve(folder,policy);
    info.setDifficulty(Difficulty.HARD);WorldPolicy loaded=WorldPolicy.read(folder,info);
    assertTrue(loaded.locked());assertEquals(GameType.SURVIVAL,loaded.createdMode());assertEquals(Difficulty.EASY,loaded.difficulty());
  }
}

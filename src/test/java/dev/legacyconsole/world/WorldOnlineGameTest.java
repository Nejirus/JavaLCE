package dev.legacyconsole.world;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class WorldOnlineGameTest {
  @Rule public TemporaryFolder directory = new TemporaryFolder();
  private Path world(String name) throws Exception { return directory.newFolder(name).toPath(); }

  @Test public void separateWorldsKeepOppositeChoices() throws Exception {
    Path first=world("first"), second=world("second");
    WorldOnlineGame.write(first,true);
    WorldOnlineGame.write(second,false);
    assertTrue(WorldOnlineGame.read(first));
    assertFalse(WorldOnlineGame.read(second));
  }

  @Test public void changingEitherWorldDoesNotChangeTheOther() throws Exception {
    Path first=world("first"), second=world("second");
    WorldOnlineGame.write(first,true);
    WorldOnlineGame.write(second,true);
    WorldOnlineGame.write(first,false);
    assertFalse(WorldOnlineGame.read(first));
    assertTrue(WorldOnlineGame.read(second));
    WorldOnlineGame.write(second,false);
    assertFalse(WorldOnlineGame.read(first));
    assertFalse(WorldOnlineGame.read(second));
  }

  @Test public void anUnconfiguredWorldStartsOfflineWithoutInheritingAnotherWorld() throws Exception {
    Path first=world("first"), second=world("second");
    WorldOnlineGame.write(first,true);
    assertFalse(WorldOnlineGame.read(second));
    assertFalse(Files.exists(second.resolve("data")));
  }

  @Test public void theChoiceTravelsWithARenamedSave() throws Exception {
    Path first=world("first"), renamed=directory.getRoot().toPath().resolve("renamed");
    WorldOnlineGame.write(first,true);
    Files.move(first,renamed);
    assertTrue(WorldOnlineGame.read(renamed));
  }

  @Test public void savingTheChoiceLeavesLevelDataAndOtherWorldDataAlone() throws Exception {
    Path world=world("first");
    Path level=world.resolve("level.dat"), other=world.resolve("data/another.dat");
    Files.createDirectories(other.getParent());
    Files.writeString(level,"existing level");Files.writeString(other,"existing data");
    WorldOnlineGame.write(world,true);WorldOnlineGame.write(world,false);
    assertEquals("existing level",Files.readString(level));
    assertEquals("existing data",Files.readString(other));
    try(var files=Files.list(other.getParent())) { assertEquals(2,files.count()); }
  }

  @Test public void writingDoesNotCreateAPhantomWorldBeforeCreation() throws Exception {
    Path missing=directory.getRoot().toPath().resolve("missing");
    assertThrows(IllegalStateException.class,()->WorldOnlineGame.write(missing,true));
    assertFalse(Files.exists(missing));
  }
}

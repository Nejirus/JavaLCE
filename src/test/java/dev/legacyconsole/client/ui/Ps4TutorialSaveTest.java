package dev.legacyconsole.client.ui;

import java.nio.file.*;
import net.minecraft.nbt.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class Ps4TutorialSaveTest {
 @Rule public TemporaryFolder temp=new TemporaryFolder();
 @Test public void saveRecognitionUsesThePersistentMarkerInsteadOfTheFolderName()throws Exception{
  Path ordinary=temp.newFolder("Tutorial").toPath();assertFalse(ConsoleTutorialWorld.isTutorialSave(ordinary));
  Path imported=temp.newFolder("Renamed-world").toPath();Files.createDirectory(imported.resolve("data"));
  CompoundTag root=new CompoundTag(),data=new CompoundTag();root.put("data",data);
  var marker=imported.resolve("data/legacyconsole_ps4_tutorial.dat").toFile();NbtIo.writeCompressed(root,marker);assertFalse(ConsoleTutorialWorld.isTutorialSave(imported));
  data.putBoolean("PS4Tutorial",true);NbtIo.writeCompressed(root,marker);assertTrue(ConsoleTutorialWorld.isTutorialSave(imported));
 }
}

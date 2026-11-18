package dev.legacyconsole.client.ui;

import dev.legacyconsole.LegacyConsole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Each press starts a fresh copy of mcps4's original Tutorial.mcs world. */
final class ConsoleTutorialWorld {
  private static final String TEMPLATE="/assets/legacyconsole/tutorial/ps4-tutorial.zip";
  private ConsoleTutorialWorld() {}

  static void launch(Screen parent) {
    Minecraft game=Minecraft.getInstance();
    ProgressScreen progress=new ProgressScreen(false);
    progress.progressStart(new TextComponent("Play Tutorial"));
    progress.progressStage(new TextComponent("Creating World"));
    game.setScreen(progress);
    Path saves=game.getLevelSource().getBaseDir();
    CompletableFuture.supplyAsync(()->{
      try {return install(saves);}
      catch(IOException failure) {throw new java.io.UncheckedIOException(failure);}
    }).whenComplete((folder,failure)->game.execute(()->{
      if(failure==null){
        load(folder);
      }
      else {
        LegacyConsole.log.error("Unable to create PS4 tutorial world",failure);
        game.setScreen(new AlertScreen(()->game.setScreen(parent),
            new TextComponent("Unable to create tutorial world"),
            new TextComponent("The tutorial could not be copied to the saves folder. See the log for details.")));
      }
    }));
  }

  static void load(String folder){
    Minecraft game=Minecraft.getInstance();
    boolean tutorial=isTutorialSave(game.getLevelSource().getBaseDir().resolve(folder));
    game.loadLevel(folder);
    // The bundled tutorial's registered Legacy generator has an experimental
    // lifecycle in Java. Recognize its saved marker on resume as well as creation.
    // The saved-world version warning remains in ConsolePlayScreen.loadWorld.
    if(tutorial&&game.screen instanceof net.minecraft.client.gui.screens.BackupConfirmScreen screen)
      ((dev.legacyconsole.mixin.BackupConfirmScreenAccess)screen).legacy$listener().proceed(false,false);
  }

  static boolean isTutorialSave(Path folder){
    Path marker=folder.resolve("data").resolve(dev.legacyconsole.tutorial.Ps4TutorialWorld.ID+".dat");
    if(!Files.isRegularFile(marker))return false;
    try{return net.minecraft.nbt.NbtIo.readCompressed(marker.toFile()).getCompound("data").getBoolean("PS4Tutorial");}
    catch(IOException error){LegacyConsole.log.warn("Cannot read tutorial save marker",error);return false;}
  }

  /** Reserve the destination atomically; existing saves are never replaced. */
  static String install(Path saves) throws IOException {
    Files.createDirectories(saves);
    Path destination=null;
    for(int suffix=1;destination==null;suffix++) {
      Path candidate=saves.resolve(suffix==1?"Tutorial":"Tutorial-"+suffix);
      try {destination=Files.createDirectory(candidate);}
      catch(FileAlreadyExistsException occupied) { /* Select the next unused name. */ }
    }
    try(InputStream resource=ConsoleTutorialWorld.class.getResourceAsStream(TEMPLATE)) {
      if(resource==null)throw new IOException("Missing original PS4 tutorial template");
      try(ZipInputStream zip=new ZipInputStream(resource)) {
        ZipEntry entry;
        while((entry=zip.getNextEntry())!=null) {
          Path target=destination.resolve(entry.getName()).normalize();
          if(!target.startsWith(destination))throw new IOException("Invalid tutorial entry: "+entry.getName());
          if(entry.isDirectory())Files.createDirectories(target);
          else {Files.createDirectories(target.getParent());Files.copy(zip,target);}
        }
      }
      if(!Files.isRegularFile(destination.resolve("level.dat")))throw new IOException("Incomplete tutorial template");
      return destination.getFileName().toString();
    } catch(IOException | RuntimeException failure) {
      try(var paths=Files.walk(destination)) {
        for(Path path:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(path);
      } catch(IOException cleanup) {failure.addSuppressed(cleanup);}
      throw failure;
    }
  }
}

package dev.legacyconsole.world;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;

/** The launch preference belongs to the save, rather than the client profile. */
public final class WorldOnlineGame {
  private static final String FILE = "legacyconsole_online.dat";

  private WorldOnlineGame() {}

  public static boolean read(Path world) {
    Path file = world.resolve("data").resolve(FILE);
    if (!Files.isRegularFile(file)) return false;
    try {
      return NbtIo.readCompressed(file.toFile()).getCompound("data").getBoolean("OnlineGame");
    } catch (IOException failure) {
      throw new IllegalStateException("Cannot read this world's Online Game setting", failure);
    }
  }

  public static void write(Path world, boolean online) {
    Path temporary = null;
    try {
      if (!Files.isDirectory(world)) throw new IOException("World save does not exist: " + world);
      Path directory = world.resolve("data");
      Files.createDirectories(directory);
      Path target = directory.resolve(FILE);
      temporary = Files.createTempFile(directory, "legacyconsole_online-", ".tmp");
      CompoundTag root = new CompoundTag(), data = new CompoundTag();
      data.putBoolean("OnlineGame", online);
      root.put("data", data);
      NbtIo.writeCompressed(root, temporary.toFile());
      try {
        Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException unsupported) {
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException failure) {
      throw new IllegalStateException("Cannot save this world's Online Game setting", failure);
    } finally {
      if (temporary != null) try { Files.deleteIfExists(temporary); } catch (IOException ignored) {}
    }
  }
}

package dev.legacyconsole.network;
/** A separately installed server add-on opts into the missing-client path. */
public final class JoinCompatibility {
  private JoinCompatibility() {}
  public static boolean enabled() {return net.minecraftforge.fml.ModList.get().isLoaded("legacyjoincompat");}
}

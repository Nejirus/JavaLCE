package dev.legacyconsole.client.ui;

/** Scalar menu logic translated from mcps4's Orbis executable (see PS4-WORKSTATIONS.md). */
public final class Ps4MenuLogic {
  private Ps4MenuLogic() {}

  /** UIScene_BrewingStandMenu::tick, 0x00a12830; ranges set by 0x00a01ce0. */
  public static float brewingArrow(int remaining) { return progress(remaining, 0, 400); }
  /** FJ_BrewingArrow::setProgress, 0x00843d40: zero stays zero; other values are inverted. */
  public static float brewingArrowFill(float progress) { return progress == 0 ? 0 : 1-progress; }
  public static float brewingFuel(int fuel) { return progress(fuel, 0, 20); }
  public static int brewingBubbles(int remaining) {
    int step = (remaining / 2) % 7;
    return step >= 1 && step <= 6 ? 30 - (step * 5 - 5) : 0;
  }
  public static float progress(int value, int min, int max) {
    return max == min ? 0 : (float)(value-min)/(max-min);
  }

  /** 0x00a14330 passes 0x1e as the platform keyboard's character limit. */
  public static final int MAP_NAME_LIMIT = 30;

  /** UIScene_CartographyMenu::GetMode, 0x00a148d0; visibility is applied by 0x007f6760. */
  public static int cartographyMode(boolean filledMap, boolean emptyMapMaterial,
      boolean glassMaterial, boolean paperMaterial, boolean materialEmpty, boolean hasResult) {
    if (filledMap && emptyMapMaterial) return 2;
    if (filledMap && glassMaterial) return 3;
    if (filledMap && paperMaterial) return 4;
    if (!hasResult) return 0;
    return filledMap && materialEmpty ? 5 : 1;
  }

  /** FJ_BeaconEffectButton state selection, 0x009df160. */
  public static int beaconButtonFrame(boolean enabled, boolean selected, boolean focused) {
    return !enabled ? 0 : selected ? 3 : focused ? 2 : 1;
  }
}

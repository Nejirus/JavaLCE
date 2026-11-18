package dev.legacyconsole.client.loading;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/** LCE's startup random-key sort, shared GetNextTip cursor and scene timer. */
public final class LceLoadingTips {
  private record Tip(int index, int sortValue) {}
  private final int[] order;
  private int next, displayed = -1;
  private long changed;
  private boolean active;

  public LceLoadingTips(int count) {
    this(count, new Random());
  }

  LceLoadingTips(int count, Random random) {
    // InitialiseTips: assign one random sort value per tip, then sort once at startup.
    // LCE pins its multiplayer/SD introductory tip; that tip is not in our compatible subset.
    Tip[] tips = new Tip[Math.max(0, count)];
    for (int i = 0; i < tips.length; i++) tips[i] = new Tip(i, random.nextInt());
    // Comparing ints directly avoids overflow in the C++ subtracting comparator.
    Arrays.sort(tips, Comparator.comparingInt(Tip::sortValue));
    order = new int[tips.length];
    for (int i = 0; i < tips.length; i++) order[i] = tips[i].index();
  }

  /** A new loading scene consumes the next tip, even if the last scene was brief. */
  public int begin(long nowMillis) {
    active = true;
    changed = nowMillis;
    return displayed = takeNext();
  }

  public int current(long nowMillis) {
    if (!active) return -1;
    if (nowMillis - changed >= LceLoadingLayout.TIP_INTERVAL_MS) {
      displayed = takeNext();
      changed = nowMillis;
    }
    return displayed;
  }

  /** Retire the scene timer without resetting the app's cursor. */
  public void end() {
    active = false;
  }

  private int takeNext() {
    if (order.length == 0) return -1;
    int tip = order[next];
    next = (next + 1) % order.length;
    return tip;
  }
}

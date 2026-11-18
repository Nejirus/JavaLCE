package dev.legacyconsole.client.loading;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.*;

public class LceLoadingTipsTest {
  private static final class Keys extends Random {
    private final int[] values;
    private int calls;
    Keys(int... values) { this.values = values; }
    @Override public int nextInt() { return values[calls++]; }
  }

  private LceLoadingTips ordered() {
    return new LceLoadingTips(8, new Keys(0,1,2,3,4,5,6,7));
  }

  private List<Integer> cycle(LceLoadingTips tips) {
    List<Integer> shown = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      shown.add(tips.begin(i * 1000));
      tips.end();
    }
    return shown;
  }

  @Test public void startupUsesLceRandomSortValuesIncludingBothIntegerExtremes() {
    var tips = new LceLoadingTips(8,
        new Keys(17,Integer.MAX_VALUE,-5,Integer.MIN_VALUE,900,-500,123,-1));
    assertEquals(List.of(3,5,2,7,0,6,4,1), cycle(tips));
  }

  @Test public void independentStartupSeedsProduceDifferentCompletePermutations() {
    Set<List<Integer>> orders = new HashSet<>();
    for (long seed = 0; seed < 32; seed++) {
      var order = cycle(new LceLoadingTips(8, new Random(seed)));
      assertEquals(Set.of(0,1,2,3,4,5,6,7), new HashSet<>(order));
      orders.add(order);
    }
    assertTrue("Startup must not retain the same fixed order", orders.size() > 1);
  }

  @Test public void scenesTimersAndWrapReuseTheStartupOrderWithoutDrawingMoreRandomKeys() {
    var keys = new Keys(17,Integer.MAX_VALUE,-5,Integer.MIN_VALUE,900,-500,123,-1);
    var tips = new LceLoadingTips(8, keys);
    assertEquals(8, keys.calls);
    var first = cycle(tips);
    assertEquals(first, cycle(tips));
    assertEquals(3, tips.begin(10000));
    assertEquals(5, tips.current(17000));
    tips.end();
    assertEquals(2, tips.begin(18000));
    assertEquals(8, keys.calls);
  }

  @Test public void shortLoadsAdvanceInsteadOfRestartingTheFirstTip() {
    var tips = ordered();
    for (int i = 0; i < 8; i++) {
      assertEquals(i, tips.begin(i * 10000));
      assertEquals(i, tips.current(i * 10000 + 500));
      tips.end();
    }
    assertEquals(0, tips.begin(80000));
  }

  @Test public void longLoadsRotateAtSevenSecondsAndNextSceneContinuesAfterLastTip() {
    var tips = ordered();
    assertEquals(0, tips.begin(1000));
    assertEquals(0, tips.current(7999));
    assertEquals(1, tips.current(8000));
    assertEquals(1, tips.current(14999));
    assertEquals(2, tips.current(15000));
    tips.end();
    assertEquals(3, tips.begin(16000));
    assertEquals(3, tips.current(22999));
    assertEquals(4, tips.current(23000));
  }

  @Test public void idleTimeDoesNotConsumeTipsOrLeakATimerIntoTheNextLoad() {
    var tips = ordered();
    assertEquals(-1, tips.current(0));
    tips.begin(0);
    tips.end();
    assertEquals(-1, tips.current(900000));
    assertEquals(1, tips.begin(900001));
    assertEquals(1, tips.current(907000));
    assertEquals(2, tips.current(907001));
  }

  @Test public void sceneChangesConsumeTheNextTipWithoutAnIdleFrame() {
    var tips = ordered();
    assertEquals(0, tips.begin(0));
    assertEquals(1, tips.begin(100));
    assertEquals(2, tips.begin(200));
    assertEquals(2, tips.current(201));
  }

  @Test public void emptyAndSingleTipListsRemainSafe() {
    var empty = new LceLoadingTips(0);
    assertEquals(-1, empty.begin(0));
    assertEquals(-1, empty.current(7000));
    var one = new LceLoadingTips(1);
    assertEquals(0, one.begin(0));
    assertEquals(0, one.current(7000));
    one.end();
    assertEquals(0, one.begin(8000));
  }
}

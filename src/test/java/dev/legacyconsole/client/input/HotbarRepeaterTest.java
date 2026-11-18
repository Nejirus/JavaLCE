package dev.legacyconsole.client.input;

import static org.junit.Assert.*;

import net.minecraft.world.entity.player.Inventory;
import org.junit.Test;

public class HotbarRepeaterTest {
  @org.junit.BeforeClass public static void bootstrap(){net.minecraft.SharedConstants.tryDetectVersion(); net.minecraft.server.Bootstrap.bootStrap();}
  private static final long MS = 1_000_000;

  @Test
  public void immediateThenOriginalThreeHundredAndTwoHundredMillisecondRepeat() {
    HotbarRepeater repeat = new HotbarRepeater();
    assertEquals(-1, repeat.tick(0, false, true, true));
    assertEquals(0, repeat.tick(299 * MS, false, true, true));
    assertEquals(-1, repeat.tick(300 * MS, false, true, true));
    assertEquals(0, repeat.tick(499 * MS, false, true, true));
    assertEquals(-1, repeat.tick(500 * MS, false, true, true));
    assertEquals(-1, repeat.tick(700 * MS, false, true, true));
  }

  @Test
  public void nativeInventoryWrapsBothWaysAndLeftWinsSimultaneousRepeats() {
    HotbarRepeater repeat = new HotbarRepeater();
    Inventory inventory = new Inventory(null);
    inventory.selected = 0;
    inventory.swapPaint(repeat.tick(0, true, true, true));
    assertEquals(8, inventory.selected);
    inventory.swapPaint(repeat.tick(300 * MS, true, true, true));
    assertEquals(7, inventory.selected);
    repeat.tick(310 * MS, false, false, true);
    inventory.selected = 8;
    inventory.swapPaint(repeat.tick(320 * MS, false, true, true));
    assertEquals(0, inventory.selected);
  }

  @Test
  public void releaseAndDirectionChangeAreImmediateAndLagDoesNotQueueSteps() {
    HotbarRepeater repeat = new HotbarRepeater();
    assertEquals(1, repeat.tick(0, true, false, true));
    assertEquals(-1, repeat.tick(50 * MS, false, true, true));
    assertEquals(0, repeat.tick(60 * MS, false, false, true));
    assertEquals(-1, repeat.tick(70 * MS, false, true, true));
    assertEquals(-1, repeat.tick(2000 * MS, false, true, true));
    assertEquals(0, repeat.tick(2001 * MS, false, true, true));
  }

  @Test
  public void MenusDisconnectAndFocusGuardsRequireReleaseBeforeNewGameplayScroll() {
    HotbarRepeater repeat = new HotbarRepeater();
    assertEquals(1, repeat.tick(0, true, false, true));
    assertEquals(0, repeat.tick(10 * MS, true, false, false));
    assertEquals(0, repeat.tick(1000 * MS, true, false, true));
    repeat.tick(1010 * MS, false, false, true);
    assertEquals(1, repeat.tick(1020 * MS, true, false, true));
    repeat.clear();
    assertEquals(-1, repeat.tick(1030 * MS, false, true, true));
  }
}

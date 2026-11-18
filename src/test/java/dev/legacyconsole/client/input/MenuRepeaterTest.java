package dev.legacyconsole.client.input;

import static org.junit.Assert.*;
import org.junit.Test;

public class MenuRepeaterTest {
  @Test public void usesLceMillisecondsRatherThanMinecraftTickCounts() {
    MenuRepeater repeat = new MenuRepeater();
    assertTrue(repeat.tick(0, PadState.DOWN));
    assertFalse(repeat.tick(299_999_999L, PadState.DOWN));
    assertTrue(repeat.tick(300_000_000L, PadState.DOWN));
    assertFalse(repeat.tick(399_999_999L, PadState.DOWN));
    assertTrue(repeat.tick(400_000_000L, PadState.DOWN));
    assertTrue(repeat.tick(401_000_000L, PadState.UP));
    assertFalse(repeat.tick(500_000_000L, PadState.UP));
    assertFalse(repeat.tick(510_000_000L, 0));
    assertTrue(repeat.tick(511_000_000L, PadState.UP));
  }

  @Test public void slowFramesDoNotQueueMultipleActionsAndSubmenusResetRepeat() {
    MenuRepeater repeat = new MenuRepeater();
    repeat.tick(0, PadState.DOWN);
    assertTrue(repeat.tick(2_000_000_000L, PadState.DOWN));
    assertFalse(repeat.tick(2_000_000_000L, PadState.DOWN));
    repeat.clear();
    assertTrue(repeat.tick(2_000_000_001L, PadState.DOWN));
  }
}

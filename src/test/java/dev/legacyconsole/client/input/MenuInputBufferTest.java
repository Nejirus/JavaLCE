package dev.legacyconsole.client.input;

import static org.junit.Assert.*;

import org.junit.Test;

public class MenuInputBufferTest {
  @Test
  public void countdownAdvancesOnlyOnPresentedFramesNotWorldTicks() {
    MenuInputBuffer buffer = new MenuInputBuffer();
    buffer.arm(true);
    for (int tick = 0; tick < 100; tick++) assertTrue(buffer.tick(false));
    for (int frame = 0; frame < 10; frame++) {
      buffer.framePresented();
      assertTrue(buffer.tick(false));
    }
    buffer.framePresented();
    assertFalse(buffer.tick(false));
    assertFalse(buffer.blocksJump());
  }

  @Test
  public void holdingResumeCannotBecomeAJumpAfterTheCountdown() {
    MenuInputBuffer buffer = new MenuInputBuffer();
    buffer.arm(true);
    for (int tick = 0; tick < 60; tick++) {
      buffer.tick(true);
      buffer.framePresented();
      assertTrue(buffer.blocksJump());
    }
    buffer.tick(false);
    assertFalse(buffer.blocksJump());
    buffer.tick(true);
    assertFalse(buffer.blocksJump());
  }

  @Test
  public void loadingAnotherWorldClearsTheMenuDelayAndHeldConfirm() {
    MenuInputBuffer buffer = new MenuInputBuffer();
    buffer.arm(true);
    buffer.clear();
    assertFalse(buffer.tick(true));
    assertFalse(buffer.blocksJump());
  }
}

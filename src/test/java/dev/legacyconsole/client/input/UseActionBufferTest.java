package dev.legacyconsole.client.input;

import static org.junit.Assert.*;

import org.junit.Test;

public class UseActionBufferTest {
  @Test
  public void heldWakePressCannotUseItemsForTwoSeconds() {
    UseActionBuffer buffer = new UseActionBuffer();
    buffer.arm();
    for (int tick = 0; tick < 40; tick++) assertTrue(buffer.tick(true));
    assertFalse(buffer.tick(true));
  }

  @Test
  public void releaseAllowsANewDeliberatePressWithoutWaiting() {
    UseActionBuffer buffer = new UseActionBuffer();
    buffer.arm();
    assertTrue(buffer.tick(true));
    assertFalse(buffer.tick(false));
    assertFalse(buffer.tick(true));
  }

  @Test
  public void actualWakeRestartsTheDelayAfterTheRequest() {
    UseActionBuffer buffer = new UseActionBuffer();
    buffer.arm();
    for (int tick = 0; tick < 30; tick++) buffer.tick(true);
    buffer.arm();
    for (int tick = 0; tick < 40; tick++) assertTrue(buffer.tick(true));
    assertFalse(buffer.tick(true));
  }

  @Test
  public void worldChangeDoesNotCarryTheOldDelay() {
    UseActionBuffer buffer = new UseActionBuffer();
    buffer.arm();
    buffer.clear();
    assertFalse(buffer.tick(true));
  }
}

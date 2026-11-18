package dev.legacyconsole.client.input;

import static org.junit.Assert.*;

import org.junit.Test;

public class BoatUseControlTest {
  @Test
  public void holdingSailWhileBoardingCannotImmediatelyExit() {
    BoatUseControl input = new BoatUseControl();
    input.tick(false, true, true);
    input.tick(true, true, true);
    assertFalse(input.dismount());
    assertTrue(input.blocksUse());
    input.tick(true, false, true);
    input.tick(true, true, true);
    assertTrue(input.dismount());
  }

  @Test
  public void exitCannotBecomeAnItemUseWhileHeld() {
    BoatUseControl input = new BoatUseControl();
    input.tick(true, false, true);
    input.tick(true, true, true);
    for (int i = 0; i < 200; i++) {
      input.tick(false, true, true);
      assertTrue(input.blocksUse());
      assertFalse(input.dismount());
    }
    input.tick(false, false, true);
    assertFalse(input.blocksUse());
  }

  @Test
  public void menuInputAndWorldChangesCannotLeakExit() {
    BoatUseControl input = new BoatUseControl();
    input.tick(true, true, false);
    input.tick(true, true, true);
    assertFalse(input.dismount());
    input.clear();
    input.tick(false, false, true);
    assertFalse(input.blocksUse());
    input.tick(true, true, true);
    assertTrue(input.dismount());
  }

  @Test
  public void analogInputScalesNativeForwardReverseAndTurningThrust() {
    assertEquals(.01f, ConsoleBoat.acceleration(0, .25f), .000001f);
    assertEquals(-.0025f, ConsoleBoat.acceleration(0, -.5f), .000001f);
    assertEquals(.002f, ConsoleBoat.acceleration(.4f, 0), .000001f);
    assertEquals(.002f, ConsoleBoat.acceleration(-.4f, 0), .000001f);
    assertEquals(0, ConsoleBoat.acceleration(0, 0), 0);
  }
}

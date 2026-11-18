package dev.legacyconsole.client;

import static org.junit.Assert.*;

import org.junit.Test;

public class LcePaperDollStateTest {
  @Test
  public void ordinaryWalkingDoesNotShowDollButSprintLingersForThirtyFrames() {
    LcePaperDollState state = new LcePaperDollState();
    assertFalse(state.frame(true, false, false, false));
    assertTrue(state.frame(true, false, true, false));
    for (int i = 0; i < 29; i++) assertTrue(state.frame(true, false, false, false));
    assertFalse(state.frame(true, false, false, false));
  }

  @Test
  public void flightUsesFiveFramesAndDisablingGuiClearsHistory() {
    LcePaperDollState state = new LcePaperDollState();
    assertTrue(state.frame(true, true, false, false));
    assertTrue(state.frame(true, false, false, true));
    for (int i = 0; i < 4; i++) assertTrue(state.frame(true, false, false, false));
    assertFalse(state.frame(true, false, false, false));
    assertTrue(state.frame(true, true, false, false));
    assertFalse(state.frame(false, true, true, true));
    assertFalse(state.frame(true, false, false, false));
  }
}

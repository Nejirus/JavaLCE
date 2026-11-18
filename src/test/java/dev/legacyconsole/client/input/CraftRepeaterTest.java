package dev.legacyconsole.client.input;

import static org.junit.Assert.*;

import org.junit.Test;

public final class CraftRepeaterTest {
  @Test
  public void quickTapDoesNotRepeat() {
    CraftRepeater r = new CraftRepeater();
    r.press(7, 0);
    for (int i = 1; i < 4; i++) assertFalse(r.repeat(true, 7, i));
    assertFalse(r.repeat(false, 7, 4));
    assertFalse(r.repeat(true, 7, 20));
  }

  @Test
  public void sustainedHoldWaitsFourHundredMillisecondsThenRepeatsEveryHundred() {
    CraftRepeater r = new CraftRepeater();
    r.press(7, 20);
    assertFalse(r.repeat(true, 7, 27));
    assertTrue(r.repeat(true, 7, 28));
    assertFalse(r.repeat(true, 7, 29));
    assertTrue(r.repeat(true, 7, 30));
  }

  @Test
  public void changingRecipeCancelsUntilNewPress() {
    CraftRepeater r = new CraftRepeater();
    r.press(7, 0);
    assertFalse(r.repeat(true, 8, 11));
    assertFalse(r.repeat(true, 7, 12));
    r.press(8, 13);
    assertTrue(r.repeat(true, 8, 23));
  }

  @Test
  public void closingOrLosingFocusCancels() {
    CraftRepeater r = new CraftRepeater();
    r.press(7, 0);
    r.cancel();
    assertFalse(r.repeat(true, 7, 30));
  }
}

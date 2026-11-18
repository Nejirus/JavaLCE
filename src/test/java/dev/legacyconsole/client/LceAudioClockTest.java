package dev.legacyconsole.client;

import static org.junit.Assert.*;

import org.junit.Test;

public class LceAudioClockTest {
  @Test
  public void twentySixtyAndTwoFortyHzCallersAllAdvanceSixHundredSourceFramesInTenSeconds() {
    for (int hz : new int[] {20, 60, 240}) {
      LceAudioClock clock = new LceAudioClock();
      assertEquals(0, clock.updates(0));
      int frames = 0;
      for (int i = 1; i <= hz * 10; i++) frames += clock.updates(i * 1_000_000_000L / hz);
      assertEquals(hz + " Hz", 600, frames);
    }
  }

  @Test
  public void slowFramesCatchUpWithoutMakingMusicWaitLongerOnASlowRenderer() {
    LceAudioClock clock = new LceAudioClock();
    clock.updates(0);
    assertEquals(30, clock.updates(500_000_000L));
    assertEquals(0, clock.updates(500_000_001L));
    assertEquals(30, clock.updates(1_000_000_000L));
  }

  @Test
  public void longLoadingStallIsBoundedByTheWholeMaximumDelay() {
    LceAudioClock clock = new LceAudioClock();
    clock.updates(1);
    assertEquals(3600, clock.updates(120_000_000_001L));
  }
}

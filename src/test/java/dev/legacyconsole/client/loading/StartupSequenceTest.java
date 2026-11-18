package dev.legacyconsole.client.loading;

import static org.junit.Assert.*;

import org.junit.Test;

public class StartupSequenceTest {
  @Test
  public void completedOrSkippedIntroHoldsTheOriginal4JLogo() {
    assertEquals(2, StartupSequence.logoStage(StartupSequence.DURATION_MS, false));
    assertEquals(2, StartupSequence.logoStage(60000, false));
    assertEquals(2, StartupSequence.logoStage(300000, false));
    assertEquals(2, StartupSequence.logoStage(0, true));
    assertEquals(3, StartupSequence.stage(StartupSequence.DURATION_MS)); // fallback loop still ends
  }

  @Test
  public void finalLogoFadesInButDoesNotFadeOutWhileLoadingContinues() {
    assertEquals(0, StartupSequence.opacity(11834), 0);
    assertEquals(1, StartupSequence.opacity(12134), 0);
    assertEquals(1, StartupSequence.opacity(15134), 0);
    assertEquals(1, StartupSequence.opacity(60000), 0);
  }

  @Test
  public void sourceLabelsAdvanceAtThirtyFramesPerSecond() {
    assertEquals(0, StartupSequence.stage(0));
    assertEquals(0, StartupSequence.stage(8699));
    assertEquals(1, StartupSequence.stage(8700));
    assertEquals(1, StartupSequence.stage(11833));
    assertEquals(2, StartupSequence.stage(11834));
    assertEquals(2, StartupSequence.stage(15166));
    assertEquals(3, StartupSequence.stage(StartupSequence.DURATION_MS));
  }

  @Test
  public void sourceNineFrameFadesReturnToWhiteBetweenLogos() {
    assertEquals(0, StartupSequence.opacity(0), 0);
    assertEquals(1, StartupSequence.opacity(300), 0);
    assertEquals(0, StartupSequence.opacity(8667), 0);
    assertEquals(0, StartupSequence.opacity(8700), 0);
    assertEquals(1, StartupSequence.opacity(9000), 0);
    assertEquals(0, StartupSequence.opacity(11800), 0);
    assertEquals(0, StartupSequence.opacity(11834), 0);
  }
}

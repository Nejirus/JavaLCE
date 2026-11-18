package dev.legacyconsole.client;

import static org.junit.Assert.*;

import java.util.Random;
import org.junit.Test;

public class LceUiSoundsTest {
  @Test
  public void originalCueGainsReplaceArbitraryGlobalVolume() {
    assertEquals(Math.pow(10, -16.7 / 20), LceUiSounds.gain("open"), .000001);
    assertEquals(LceUiSounds.gain("open"), LceUiSounds.gain("confirm"), 0);
    assertEquals(1, LceUiSounds.gain("focus"), 0);
    assertEquals(Math.pow(10, -12.0 / 20), LceUiSounds.gain("craft"), .000001);
  }

  @Test
  public void focusPitchHasSourceRangeAndOtherCuesHaveNoPitchVariation() {
    Random random = new Random(17);
    for (int i = 0; i < 100; i++) {
      float pitch = LceUiSounds.pitch("focus", random);
      assertTrue(pitch >= .943874f && pitch <= 1.059463f);
      assertEquals(1, LceUiSounds.pitch("craft", random), 0);
    }
  }

  @Test
  public void focusUsesTheActiveMilesPitchBoundsWithoutTheOldXboxBasePitch() {
    Random minimum =
        new Random() {
          @Override
          public float nextFloat() {
            return 0;
          }
        };
    Random maximum =
        new Random() {
          @Override
          public float nextFloat() {
            return 1;
          }
        };
    assertEquals(.943874f, LceUiSounds.pitch("focus", minimum), .000001f);
    assertEquals(1.059463f, LceUiSounds.pitch("focus", maximum), .000001f);
  }
}

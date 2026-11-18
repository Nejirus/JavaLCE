package dev.legacyconsole.client.input;

import static org.junit.Assert.*;

import org.junit.Test;

public class InputMathTest {
  @Test public void sourceSprintThresholdIncludesPointEight(){
    assertTrue(InputMath.sprintForwardEnough(.8f));
    assertFalse(InputMath.sprintForwardEnough(.7999f));
    assertFalse(InputMath.sprintForwardEnough(0));
    assertFalse(InputMath.sprintForwardEnough(-1));
  }
  @Test public void turningTooFarSidewaysDropsBelowSprintThreshold(){
    float[] shallow=InputMath.radial(.4f,(float)Math.sqrt(.84),.18f);
    float[] wide=InputMath.radial(.8f,.6f,.18f);
    assertTrue(shallow[0]>0);assertTrue(InputMath.sprintForwardEnough(shallow[1]));
    assertTrue(wide[0]>0);assertFalse(InputMath.sprintForwardEnough(wide[1]));
  }

  @Test
  public void driftingStickIsNeutral() {
    assertArrayEquals(new float[] {0, 0}, InputMath.radial(.08f, -.07f, .18f), 0);
  }

  @Test
  public void diagonalInputIsNormalizedAndFullTravelRemainsAvailable() {
    float[] diagonal = InputMath.radial(1, 1, .18f);
    assertEquals(1, Math.sqrt(diagonal[0] * diagonal[0] + diagonal[1] * diagonal[1]), .0001);
    assertArrayEquals(new float[] {-1, 0}, InputMath.radial(-1, 0, .18f), .0001f);
  }

  @Test
  public void responseIsContinuousAtDeadzone() {
    assertArrayEquals(new float[] {.5f, 0}, InputMath.radial(.6f, 0, .2f), .0001f);
  }

  @org.junit.Test
  public void sdlAxesUseUpPositiveWithoutFlippingHorizontalMovement() {
    org.junit.Assert.assertArrayEquals(new float[] {0, 1}, InputMath.sdlStick(0, -1, .18f), .0001f);
    org.junit.Assert.assertArrayEquals(new float[] {0, -1}, InputMath.sdlStick(0, 1, .18f), .0001f);
    org.junit.Assert.assertArrayEquals(new float[] {1, 0}, InputMath.sdlStick(1, 0, .18f), .0001f);
    org.junit.Assert.assertArrayEquals(
        new float[] {-1, 0}, InputMath.sdlStick(-1, 0, .18f), .0001f);
  }
}

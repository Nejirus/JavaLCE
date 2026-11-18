package dev.legacyconsole.client.input;
import static org.junit.Assert.*;
import org.junit.Test;
public class LceMovementFilterTest {
 @Test public void smallSideDeflectionsStayStraightWhenPushingForward(){
  for(float side:new float[]{-.3f,-.2f,-.1f,0,.1f,.2f,.3f}){
   float[] move=InputMath.lceMovementStick(side,-1);
   assertEquals(0,move[0],0);assertEquals(1,move[1],0);
  }
 }
 @Test public void exactSourceDeadzoneAndFirstStepMatchIntegerRange(){
  float boundary=10000f/32767f,step=10001f/32767f;
  assertEquals(0,InputMath.lceMovementAxis(boundary),0);
  assertEquals(0,InputMath.lceMovementAxis(-boundary),0);
  assertEquals(1f/22767,InputMath.lceMovementAxis(step),.0000001f);
  assertEquals(-1f/22767,InputMath.lceMovementAxis(-step),.0000001f);
 }
 @Test public void axesAreIndependentRatherThanRadiallyRescaled(){
  assertEquals(InputMath.lceMovementStick(.1f,-.5f)[1],InputMath.lceMovementStick(.8f,-.5f)[1],0);
  assertEquals(1,InputMath.lceMovementStick(.4f,-1)[1],0);
 }
 @Test public void widerPhysicalAngleWalksWhileSourceBoundaryStillUsesPointEight(){
  float[] at30=InputMath.lceMovementStick(.5f,-(float)Math.cos(Math.toRadians(30)));
  float[] at32=InputMath.lceMovementStick((float)Math.sin(Math.toRadians(32)),-(float)Math.cos(Math.toRadians(32)));
  assertTrue(InputMath.sprintForwardEnough(at30[1]));
  assertFalse(InputMath.sprintForwardEnough(at32[1]));
  assertTrue(at32[0]>0); // Walk sideways; do not restore blanket strafe blocking.
 }
 @Test public void fullTravelAndSdlDirectionArePreserved(){
  assertArrayEquals(new float[]{0,1},InputMath.lceMovementStick(0,-1),0);
  assertArrayEquals(new float[]{0,-1},InputMath.lceMovementStick(0,1),0);
  assertArrayEquals(new float[]{1,0},InputMath.lceMovementStick(1,0),0);
  assertArrayEquals(new float[]{-1,0},InputMath.lceMovementStick(-1,0),0);
 }
}

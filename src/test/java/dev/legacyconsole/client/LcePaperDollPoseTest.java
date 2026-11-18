package dev.legacyconsole.client;
import static org.junit.Assert.*;
import org.junit.Test;
public class LcePaperDollPoseTest {
 @Test public void fixedSourceMouseOffsetsGiveTheAuthoredCameraAngles(){
  assertEquals(-15.707963f,LcePaperDollPose.YAW,.0001f);assertEquals(4.8995733f,LcePaperDollPose.PITCH,.0001f);
 }
 @Test public void worldBodyYawIsCanceledWithoutChangingCapeOrientationFields(){
  for(float body:new float[]{-360,-135,0,45,90,180,360})
   assertEquals(-LcePaperDollPose.YAW,LcePaperDollPose.cancelBodyYaw(body)+180-body,.0001f);
 }
}

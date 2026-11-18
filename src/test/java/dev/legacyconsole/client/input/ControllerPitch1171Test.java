package dev.legacyconsole.client.input;
import static org.junit.Assert.*;
import org.junit.Test;
public class ControllerPitch1171Test {
 @Test public void stickUpLooksUpWithDefaultControls(){float up=InputMath.sdlStick(0,-1,.18f)[1];assertTrue(InputMath.pitch(up,170,.02f,false)<0);}
 @Test public void stickDownLooksDownWithDefaultControls(){float up=InputMath.sdlStick(0,1,.18f)[1];assertTrue(InputMath.pitch(up,170,.02f,false)>0);}
 @Test public void invertOptionReversesPitch(){assertEquals(-InputMath.pitch(1,170,.02f,false),InputMath.pitch(1,170,.02f,true),.0001f);}
}

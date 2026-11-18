package dev.legacyconsole.client.input;

import static org.junit.Assert.*;
import com.studiohartman.jamepad.ControllerState;
import java.lang.reflect.*;
import org.junit.Test;

public class PadStateTest {
  private ControllerState raw(String name) throws Exception {
    Constructor<ControllerState> constructor = ControllerState.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    ControllerState state = constructor.newInstance();
    for (String field : new String[] {"isConnected", "a", "b", "x", "y", "lb", "rb",
        "start", "back", "leftStickClick", "rightStickClick", "dpadDown"}) {
      Field f = ControllerState.class.getField(field);
      f.setAccessible(true);
      f.setBoolean(state, true);
    }
    Field type = ControllerState.class.getField("controllerType");
    type.setAccessible(true);
    type.set(state, name);
    for (String field : new String[] {"leftStickX", "rightStickX", "leftTrigger", "rightTrigger"}) {
      Field f = ControllerState.class.getField(field);
      f.setAccessible(true);
      f.setFloat(state, 1);
    }
    Field vertical = ControllerState.class.getField("leftStickY");
    vertical.setAccessible(true);
    vertical.setFloat(state, -1);
    return state;
  }

  @Test public void physicalFamiliesKeepIdenticalLceButtonPositionsAndStickDirections() throws Exception {
    for (String family : new String[] {"PS3 Controller", "PS4 Controller", "DualSense", "Xbox 360", "Xbox One"}) {
      PadState pad = new PadState(raw(family));
      assertTrue(pad.connected);
      assertTrue(pad.lx > 0 && pad.ly > 0 && pad.rx > 0);
      for (int button : new int[] {PadState.CROSS, PadState.CIRCLE, PadState.SQUARE,
          PadState.TRIANGLE, PadState.L1, PadState.R1, PadState.L2, PadState.R2,
          PadState.OPTIONS, PadState.SHARE, PadState.L3, PadState.R3, PadState.DOWN})
        assertTrue(family + ":" + button, pad.held(button));
    }
  }

  @Test public void waitingForNeutralPreservesDetectionButCannotDispatchHeldStartupInputs() throws Exception {
    PadState pad = new PadState(raw("PS3 Controller")).neutral();
    assertTrue(pad.connected);
    assertEquals("PS3 Controller", pad.name);
    assertEquals(0, pad.buttons);
    assertEquals(0, pad.lx + pad.ly + pad.rx + pad.ry, 0);
    assertSame(PadState.EMPTY, PadState.EMPTY.neutral());
  }
  @Test public void gameplayUsesLceAxisFilteringWhileLookAndMenusKeepTheirFilter() throws Exception {
    ControllerState state=raw("DualSense");
    for(String name:new String[]{"leftStickX","rightStickX"}){
      Field f=ControllerState.class.getField(name);f.setAccessible(true);f.setFloat(state,.2f);
    }
    PadState pad=new PadState(state);
    assertEquals(0,pad.movementX,0);assertEquals(1,pad.movementY,0);
    assertTrue(pad.lx>0);assertTrue(pad.rx>0);
  }
  @Test public void neutralStartupStateAlsoClearsMovementAxes() throws Exception {
    PadState pad=new PadState(raw("PS4 Controller")).neutral();
    assertEquals(0,pad.movementX+pad.movementY,0);
    assertEquals(0,PadState.EMPTY.movementX+PadState.EMPTY.movementY,0);
  }

}

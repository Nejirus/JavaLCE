package dev.legacyconsole.client.input;

import static org.junit.Assert.*;
import org.junit.Test;

public class CursorCoordinatesTest {
  @Test public void guiRoundTripsAtNativeAndRetinaSizesWithoutAnAspectAssumption() {
    int[][] sizes={{1920,1080,1920,1080,640,360},{1920,1200,960,600,640,400},
        {3024,1964,1512,982,756,491},{3456,2234,1728,1117,692,447},
        {1280,960,640,480,640,480},{1024,768,1024,768,1024,768}};
    for(int[] s:sizes) {
      var c=new CursorCoordinates(s[0],s[1],s[2],s[3],s[4],s[5]);
      for(double x:new double[]{1,23.5,s[4]/2.0,s[4]-2})
        for(double y:new double[]{1,8.5,s[5]/2.0,s[5]-2}) {
          var window=c.toWindow(x,y);var gui=c.toGui(window.x(),window.y());
          assertEquals(x,gui.x(),1e-9);assertEquals(y,gui.y(),1e-9);
        }
    }
  }

  @Test public void retinaUsesLogicalWindowPointsAndDoesNotDoubleScaleTheWarp() {
    var c=new CursorCoordinates(3024,1964,1512,982,756,491);
    var point=c.toWindow(235.5,210.5);
    assertEquals(471,point.x(),0);assertEquals(421,point.y(),0);
    var gui=c.toGui(point.x(),point.y());
    assertEquals(210.5,gui.y(),0);
  }

  @Test public void xAndYDensityCanChangeIndependentlyDuringAWindowTransition() {
    var c=new CursorCoordinates(2560,1600,1280,1000,640,400);
    var point=c.toWindow(320,200);
    assertEquals(640,point.x(),0);assertEquals(500,point.y(),0);
    assertEquals(new CursorCoordinates.Point(320,200),c.toGui(point.x(),point.y()));
  }

  @Test public void legacyBottomOriginIsConfinedToTheCompatibilityApi() {
    var c=new CursorCoordinates(1920,1200,960,600,640,400);
    assertEquals(new CursorCoordinates.Point(0,1199),c.toLegacy(0,0));
    assertEquals(new CursorCoordinates.Point(1918,1),c.toLegacy(959,599));
    assertEquals(new CursorCoordinates.Point(959,599),c.legacyToWindow(1918,1));
    var gui=c.toGui(352.5,315.75);
    assertEquals(235,gui.x(),0);assertEquals(210.5,gui.y(),0);
  }

  @Test public void outsideCoordinatesStayOutsideSoNativeItemDroppingStillWorks() {
    var c=new CursorCoordinates(1920,1200,960,600,640,400);
    var point=c.toWindow(-12.5,420);
    assertEquals(new CursorCoordinates.Point(-12.5,420),c.toGui(point.x(),point.y()));
  }

  @Test public void minimizedDimensionsDoNotDivideByZero() {
    var c=new CursorCoordinates(0,0,0,0,0,0);
    assertEquals(new CursorCoordinates.Point(1,1),c.toWindow(1,1));
    assertEquals(new CursorCoordinates.Point(1,1),c.toGui(1,1));
  }
}

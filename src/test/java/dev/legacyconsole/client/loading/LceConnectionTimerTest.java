package dev.legacyconsole.client.loading;

import static org.junit.Assert.*;
import java.security.MessageDigest;
import org.junit.Test;

public class LceConnectionTimerTest {
  @Test public void copiedTimelineIsTheOriginalEightyFrameDisplayList() throws Exception {
    try(var in=getClass().getResourceAsStream("/assets/legacyconsole/connecting_timer.json")) {
      byte[] hash=MessageDigest.getInstance("SHA-256").digest(in.readAllBytes());
      StringBuilder hex=new StringBuilder();for(byte b:hash)hex.append(String.format("%02x",b&255));
      assertEquals("e3cbbd19b293923bc5ef5f0819f86fa867a290a2dd0facaf92fa84f052a2ca55",hex.toString());
    }
    var squares=LceConnectionTimer.squares(1280,720,0);
    assertEquals(2,squares.size());
    assertEquals(new LceLoadingLayout.Bounds(565,363.1f,42,42),squares.get(0).bounds());
    assertEquals(256,squares.get(0).opacity());
    assertEquals(new LceLoadingLayout.Bounds(565,417.1f,42,42),squares.get(1).bounds());
    assertEquals(169,squares.get(1).opacity());
  }
  @Test public void animationUsesSourceThirtyFpsAndLoopsWithoutAnArtificialLoadingDelay() {
    assertEquals(0,LceConnectionTimer.frame(-10));
    assertEquals(0,LceConnectionTimer.frame(33));
    assertEquals(1,LceConnectionTimer.frame(34));
    assertEquals(79,LceConnectionTimer.frame(2666));
    assertEquals(0,LceConnectionTimer.frame(2667));
    assertEquals(0,LceConnectionTimer.frame(8000));
    for(long time:new long[]{0,300,900,1600,2600})
      assertEquals(LceConnectionTimer.squares(1280,720,time),LceConnectionTimer.squares(1280,720,time+8000));
  }
  @Test public void sourceSquareAndSpacingStayUniformOnWideRetinaAndFourByThreeViews() {
    for(int[] size:new int[][]{{640,360},{1280,720},{1920,1080},{2560,1440},{3840,2160},{640,480},{1440,900}}) {
      float scale=dev.legacyconsole.client.ui.LceViewport.scale(size[0],size[1]);
      for(var square:LceConnectionTimer.squares(size[0],size[1],900)) {
        assertEquals(42*scale,square.bounds().width(),.0001f);
        assertEquals(square.bounds().width(),square.bounds().height(),.0001f);
        assertTrue(square.opacity()>0&&square.opacity()<=256);
        assertTrue(square.bounds().x()>=0&&square.bounds().y()+square.bounds().height()<size[1]);
      }
    }
  }
}

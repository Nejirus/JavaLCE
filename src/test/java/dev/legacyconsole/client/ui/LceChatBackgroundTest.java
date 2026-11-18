package dev.legacyconsole.client.ui;

import static org.junit.Assert.*;
import org.junit.Test;

public class LceChatBackgroundTest {
  @Test public void normalChatTranslationReachesBothViewportEdges() {
    assertEquals(new LceChatBackground.Span(-16,624),LceChatBackground.span(640,16,1));
    assertEquals(new LceChatBackground.Span(-32,1248),LceChatBackground.span(640,16,.5f));
  }
  @Test public void reducedChatScaleAndFractionalPlacementNeverLeaveAnEdgeGap() {
    for(int width:new int[]{320,640,683,853,1280,1920})
      for(float scale:new float[]{.1f,.5f,.75f,1})
        for(float origin:new float[]{0,12,16,16.25f,32.5f}) {
          var span=LceChatBackground.span(width,origin,scale);
          float left=span.left()*scale+origin,right=span.right()*scale+origin;
          assertTrue(left<=.001f);assertTrue(left>=-scale-.001f);
          assertTrue(right>=width-.001f);assertTrue(right<=width+scale+.001f);
        }
  }
  @Test public void originalHudShapeColourPreservesNativeOpacityAndFading() {
    assertEquals(0x7f333333,LceChatBackground.color(0x7f000000));
    assertEquals(0xff333333,LceChatBackground.color(0xff000000));
    assertEquals(0x20333333,LceChatBackground.color(0x20000000));
    assertEquals(0x00333333,LceChatBackground.color(0));
  }
}

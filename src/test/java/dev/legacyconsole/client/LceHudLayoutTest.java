package dev.legacyconsole.client;

import static org.junit.Assert.*;
import dev.legacyconsole.client.ui.ConsoleDisplayScale;
import org.junit.Test;

public class LceHudLayoutTest {
  @Test public void allThreeSizesUseOriginalSdAndHdTimelinePlacements() throws Exception {
    com.google.gson.JsonObject source;
    try (java.io.Reader reader = new java.io.InputStreamReader(getClass().getResourceAsStream(
        "/assets/legacyconsole/lce_widget_layout.json"), java.nio.charset.StandardCharsets.UTF_8)) {
      source = new com.google.gson.JsonParser().parse(reader).getAsJsonObject();
    }
    String[] names = {"HudSmall", "HudNormal", "HudLarge"};
    for (int displayHeight : new int[] {720, 1080, 1440, 2160}) {
      int referenceHeight = displayHeight < 1080 ? 720 : 1080;
      com.google.gson.JsonArray nodes = source.getAsJsonObject("HUD"+referenceHeight+".swf").getAsJsonArray("root");
      for (int size=0;size<3;size++) {
        LceHudLayout actual = new LceHudLayout(640,360,displayHeight,displayHeight/360,0,size);
        com.google.gson.JsonArray matrix=null;
        for (com.google.gson.JsonElement element : nodes) {
          com.google.gson.JsonObject node=element.getAsJsonObject();
          if (node.get("name").getAsString().equals(names[size])) matrix=node.getAsJsonArray("matrix");
        }
        assertNotNull(matrix);
        assertEquals(matrix.get(5).getAsFloat()/referenceHeight*360-18,actual.rootY,.001f);
        assertEquals(matrix.get(0).getAsFloat()/1.5f,actual.factor,.001f);
        assertEquals(273*actual.factor,actual.width,.001f);
        assertEquals(320,actual.x+actual.width/2,.001f);
        assertEquals(30*actual.factor,actual.slotX(1)-actual.slotX(0),.001f);
        assertTrue(actual.hotbarY+31.5f*actual.factor<342);
      }
    }
  }

  @Test public void consoleAutoKeepsAllRequestedResolutionsOnTheSameCanvas() {
    int[][] cases = {{1280,720,2}, {1920,1080,3}, {2560,1440,4}, {3840,2160,6}};
    for (int[] c : cases) {
      int scale = ConsoleDisplayScale.automaticFactor(c[0],c[1]);
      assertEquals(c[2], scale);
      assertEquals(640, c[0] / scale);
      assertEquals(360, c[1] / scale);
    }
    assertEquals(1, ConsoleDisplayScale.automaticFactor(854,480));
    assertEquals(2, ConsoleDisplayScale.automaticFactor(1280,1024));
  }

  @Test public void hudUsesSourceNormalSceneAndSafeZoneCoordinates() {
    LceHudLayout sd = new LceHudLayout(640,360,720,2,0);
    LceHudLayout hd = new LceHudLayout(640,360,1080,3,0);
    assertEquals(480, sd.rootY * 2, .001f);
    assertEquals(721, hd.rootY * 3, .001f);
    assertEquals(273, LceHudLayout.WIDTH, 0);
    assertEquals(282, sd.hotbarY, .001f);
    assertEquals(30, sd.slotX(1) - sd.slotX(0), 0);
    assertEquals(24, 16 * LceHudLayout.PIXEL, 0);
    LceHudLayout uhd = new LceHudLayout(640,360,2160,6,0);
    assertEquals(hd.rootY, uhd.rootY, .001f);
    assertEquals(sd.hotbarY - 10, new LceHudLayout(640,360,720,2,10).hotbarY, .001f);
  }
}

package dev.legacyconsole.client.ui;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConsoleOptionLayoutTest {
  @Test public void checkboxPitchMatchesBothOriginalOptionsAndUiScenes() throws Exception {
    com.google.gson.JsonObject source;
    try(java.io.Reader reader=new java.io.InputStreamReader(getClass().getResourceAsStream(
        "/assets/legacyconsole/play_layout.json"),java.nio.charset.StandardCharsets.UTF_8)) {
      source=new com.google.gson.JsonParser().parse(reader).getAsJsonObject();
    }
    for(int units:new int[]{2,3}) {
      com.google.gson.JsonObject scene=source.getAsJsonObject("scenes").getAsJsonObject(units==3?"1080":"720");
      for(String group:new String[]{"settings_ui","settings_options"}) {
        java.util.List<Float> positions=new java.util.ArrayList<>();
        for(com.google.gson.JsonElement element:scene.getAsJsonArray(group)) {
          com.google.gson.JsonObject node=element.getAsJsonObject();
          String name=node.get("name").getAsString();
          if(name.equals(group.equals("settings_ui")?"DisplayHUD":"ViewBob")
              || name.equals(group.equals("settings_ui")?"DisplayHand":"ShowHints"))
            positions.add(node.getAsJsonArray("matrix").get(5).getAsFloat()/units);
        }
        int pitch=group.equals("settings_ui")?ConsoleOptionLayout.UI_CHECKBOX_PITCH:ConsoleOptionLayout.CHECKBOX_PITCH;
        assertEquals(positions.get(1)-positions.get(0),pitch,.001f);
        ConsoleOptionLayout actual=new ConsoleOptionLayout(640,360,new boolean[]{true,true},0,0,0,pitch);
        assertEquals(pitch,actual.rowY(1)-actual.rowY(0));assertEquals(12,actual.rowHeight(0));
      }
    }
  }

  @Test public void mixedHeightScrollingKeepsEverySelectionVisibleAndHitAreasSeparate() {
    boolean[] boxes=new boolean[34];
    for(int i=0;i<boxes.length;i++)boxes[i]=i%7>1;
    for(int[] screen:new int[][]{{640,360},{427,240}})
      for(int selected=0;selected<boxes.length;selected++)
        for(int first=0;first<boxes.length;first++) {
          ConsoleOptionLayout actual=new ConsoleOptionLayout(screen[0],screen[1],boxes,0,first,selected,15);
          assertTrue(actual.first<=selected && selected<actual.first+actual.rows);
          for(int i=actual.first;i<actual.first+actual.rows-1;i++)
            assertTrue(actual.rowY(i)+actual.rowHeight(i)<actual.rowY(i+1));
          int last=actual.first+actual.rows-1;
          assertTrue(actual.rowY(last)+actual.rowHeight(last)+8<=screen[1]-28);
          assertEquals(225,actual.w-16);
        }
  }

  @Test
  public void settingsMatchHelpButtonSizePitchAndOriginAtBoth1440pScales() {
    for (int[] resolution : new int[][] {{640, 360}, {427, 240}})
      for (int count : new int[] {1, 4, 6, 8, 12, 20, 100}) {
        ConsoleOptionLayout list = new ConsoleOptionLayout(resolution[0], resolution[1], count, 0);
        assertEquals(225, list.w - 16);
        assertEquals(Math.round(resolution[1] * 250f / 720f), list.y + 8);
        assertTrue(list.x >= 0 && list.x + list.w <= resolution[0]);
        assertTrue(list.y + list.h <= resolution[1] - 28);
        assertEquals(20, ConsoleOptionLayout.HEIGHT);
        assertEquals(25, ConsoleOptionLayout.PITCH);
        assertTrue(list.rows > 0 && list.rows <= count);
        assertEquals(Math.min(count, resolution[1] == 360 ? 8 : 5), list.rows);
      }
  }

  @Test
  public void nativeConfirmMessagesReserveSpaceWithoutShrinkingButtons() {
    for (int[] resolution : new int[][] {{640, 360}, {427, 240}}) {
      ConsoleOptionLayout dialog = new ConsoleOptionLayout(resolution[0], resolution[1], 2, 65);
      assertEquals(225, dialog.w - 16);
      assertEquals(2, dialog.rows);
      assertTrue(dialog.y + dialog.h <= resolution[1] - 28);
    }
  }
}

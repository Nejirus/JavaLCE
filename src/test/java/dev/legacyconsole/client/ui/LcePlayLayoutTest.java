package dev.legacyconsole.client.ui;

import static org.junit.Assert.*;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class LcePlayLayoutTest {
  @Test public void bothPanelsAndListsUseOriginalSdAndHdScenePlacements() throws Exception {
    JsonObject source;
    try(Reader reader=new InputStreamReader(getClass().getResourceAsStream(
        "/assets/legacyconsole/play_layout.json"),StandardCharsets.UTF_8)) {
      source=new JsonParser().parse(reader).getAsJsonObject();
    }
    for(int height:new int[]{720,1080,1440,2160}) {
      int units=height>=1080?3:2;
      LcePlayLayout actual=new LcePlayLayout(640,360,height);
      JsonArray nodes=source.getAsJsonObject("scenes").getAsJsonObject(units==3?"1080":"720").getAsJsonArray("root");
      for(int c=0;c<2;c++) {
        for(JsonElement element:nodes) {
          JsonObject node=element.getAsJsonObject();String name=node.has("name")?node.get("name").getAsString():"";
          JsonArray matrix=node.getAsJsonArray("matrix");
          if(name.equals(c==0?"SavesListPanel":"JoinListPanel")) {
            assertEquals(matrix.get(4).getAsFloat()/units,actual.panels[c][0],.001f);
            assertEquals(matrix.get(5).getAsFloat()/units,actual.panels[c][1],.001f);
            assertEquals(matrix.get(0).getAsFloat()*(units==3?66:44)/units,actual.panels[c][2],.001f);
          }
          if(name.equals(c==0?"SavesList":"JoinList")) {
            assertEquals(matrix.get(4).getAsFloat()/units,actual.lists[c][0],.001f);
            assertEquals(matrix.get(5).getAsFloat()/units,actual.lists[c][1],.001f);
            assertEquals(matrix.get(0).getAsFloat()*50/units,actual.lists[c][2],.001f);
          }
        }
        assertTrue(actual.lists[c][0]>actual.panels[c][0]);
        assertTrue(actual.lists[c][1]+5*actual.rowHeight<actual.panels[c][1]+actual.panels[c][3]);
      }
      assertEquals(5,actual.rows);assertEquals(30,actual.rowHeight,0);
      assertEquals(.5f,actual.alpha(false,1_000_000_000L),0);
      assertEquals(1,actual.alpha(true,1_000_000_000L),0);
    }
  }

  @Test public void unnamedRaisedBackingContainsBothRecessesWithSymmetricSourceInsets() throws Exception {
    JsonObject source;
    try(Reader reader=new InputStreamReader(getClass().getResourceAsStream(
        "/assets/legacyconsole/play_layout.json"),StandardCharsets.UTF_8)) {
      source=new JsonParser().parse(reader).getAsJsonObject();
    }
    for(int height:new int[]{720,1080,1440,2160}) {
      int units=height>=1080?3:2;
      JsonObject scene=source.getAsJsonObject("scenes").getAsJsonObject(units==3?"1080":"720");
      JsonObject backing=scene.getAsJsonArray("root").get(0).getAsJsonObject();
      assertFalse(backing.has("name"));
      assertEquals(1,backing.get("depth").getAsInt());
      assertEquals("FJ_Panel9Grid",backing.get("class").getAsString());
      for(int[] size:new int[][]{{640,360},{427,240},{320,240}}) {
        LcePlayLayout layout=new LcePlayLayout(size[0],size[1],height);
        float[] outer=layout.background;
        JsonArray matrix=backing.getAsJsonArray("matrix");
        float nativeSize=units==3?159:96;
        assertEquals(60*layout.scale+(size[0]-640*layout.scale)/2,outer[0],.001f);
        assertEquals(matrix.get(0).getAsFloat()*nativeSize/units*layout.scale,outer[2],.001f);
        // Preserve the full original geometry in provenance; Java omits its storage footer.
        assertEquals(matrix.get(3).getAsFloat()*nativeSize/units,
            scene.getAsJsonArray("background").get(3).getAsFloat(),.001f);
        for(float[] recess:layout.panels) {
          assertTrue(outer[0]<recess[0]);assertTrue(outer[1]<recess[1]);
          assertTrue(outer[0]+outer[2]>recess[0]+recess[2]);
          assertTrue(outer[1]+outer[3]>recess[1]+recess[3]);
          float topInset=recess[1]-outer[1];
          float bottomInset=outer[1]+outer[3]-recess[1]-recess[3];
          assertEquals(12*layout.scale,topInset,.001f);
          assertEquals(topInset,bottomInset,.001f);
        }
        assertTrue(outer[0]>=0 && outer[0]+outer[2]<=size[0]);
        assertTrue(outer[1]+outer[3]<=size[1]-28);
      }
    }
  }

  @Test public void twoPanelsStayInsideTheScreenWithManualGuiScales() {
    for(int[] screen:new int[][]{{640,360},{427,240},{320,240},{854,480}}) {
      LcePlayLayout layout=new LcePlayLayout(screen[0],screen[1],1440);
      for(float[] panel:layout.panels) {
        assertTrue(panel[0]>=0);assertTrue(panel[0]+panel[2]<=screen[0]);
        assertTrue(panel[1]>=0);assertTrue(panel[1]+panel[3]<=screen[1]-28);
      }
      assertTrue(layout.panels[0][0]+layout.panels[0][2]<layout.panels[1][0]);
    }
  }
}

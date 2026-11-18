package dev.legacyconsole.client.ui;

import static org.junit.Assert.*;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.junit.Test;

public class Ps4WorkstationArtTest {
  private static String hex(byte[] data){StringBuilder s=new StringBuilder();for(byte b:data)s.append(String.format("%02x",b&255));return s.toString();}
  @Test public void originalScenesFitEverySupportedDisplayShape() {
    String[] scenes={"BrewingStandMenu","CartographyMenu","EnchantingMenu","BeaconMenu"};
    float[][] roots={{639,141},{648,158},{637,218},{580,173}};
    int[][] sizes={{1280,720},{1920,1080},{2560,1440},{3840,2160},{1024,768},{1600,1000},{1512,982},{3024,1964}};
    for(int i=0;i<scenes.length;i++) {
      assertArrayEquals(roots[i],Ps4UiArt.DATA.scenes.get(scenes[i]).root,0);
      for(var node:Ps4UiArt.DATA.scenes.get(scenes[i]).background)
        assertTrue(node.symbol,Ps4UiArt.DATA.sprites.containsKey(node.symbol)||Ps4UiArt.DATA.panels.containsKey(node.symbol));
      for(int[] size:sizes) {
        var art=new Ps4UiArt(scenes[i],size[0],size[1]);
        var panel=art.panelBounds();
        assertTrue(panel.x()>=0&&panel.y()>=0);
        assertTrue(panel.x()+panel.w()<=size[0]&&panel.y()+panel.h()<=size[1]);
        var last=art.cell("inventoryList",8,2,9,3);
        assertTrue(last.x()+last.w()<=panel.x()+panel.w());
        assertTrue(last.y()+last.h()<=panel.y()+panel.h());
        assertEquals(Math.min(size[0]/1920f,size[1]/1080f),art.scale,0);
      }
    }
  }
  @Test public void unsignedNativeTypographySurvivesTheRuntimeGsonParser() {
    assertEquals(0xff80eb20,Ps4UiArt.textColor("FJ_Label_Enchanting"));
    assertEquals(0xffc6110f,Ps4UiArt.textColor("FJ_Label_Enchanting_Inactive"));
    assertEquals(1,Ps4UiArt.DATA.typography.get("FJ_Label_Enchanting").alignment);
    assertEquals("Rename Map",Ps4UiArt.text("cc04e002"));
    assertEquals("Show All Recipes",Ps4UiArt.text("2fddc05c"));
  }
  @Test public void shippedSpritesMatchTheSourceConversionManifest()throws Exception {
    JsonObject manifest;
    try(var r=new InputStreamReader(getClass().getResourceAsStream("/assets/legacyconsole/ps4_workstations.json"),StandardCharsets.UTF_8)) {
      manifest=new JsonParser().parse(r).getAsJsonObject();
    }
    assertEquals(213,manifest.getAsJsonObject("sprites").size());
    for(var entry:manifest.getAsJsonObject("sprites").entrySet()) {
      var data=entry.getValue().getAsJsonObject();
      try(var in=getClass().getResourceAsStream("/assets/legacyconsole/textures/ps4/workstations/"+data.get("texture").getAsString()+".png")) {
        assertNotNull(entry.getKey(),in);
        assertEquals(entry.getKey(),data.get("sha256").getAsString(),hex(MessageDigest.getInstance("SHA-256").digest(in.readAllBytes())));
      }
    }
  }
}

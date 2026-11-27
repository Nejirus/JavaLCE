package dev.legacyconsole.client.ui;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4PointerPanelLayoutTest {
  private void check(String kind)throws Exception {
    try(var reader=new InputStreamReader(getClass().getResourceAsStream("/ps4/pointer-panels-native.json"),StandardCharsets.UTF_8)){
      var data=new Gson().fromJson(reader,JsonObject.class);
      for(var entry:data.getAsJsonArray("cases")){
        var c=entry.getAsJsonObject();if(!c.get("kind").getAsString().equals(kind))continue;
        var panel=Ps4PointerPanelLayout.layout(c.get("width").getAsFloat(),c.get("height").getAsFloat(),
            c.get("glyph_height").getAsFloat(),kind.equals("trading"));
        assertEquals(c.toString(),c.get("panel_width").getAsFloat(),panel.width(),0);
        assertEquals(c.toString(),c.get("panel_height").getAsFloat(),panel.height(),0);
        assertEquals(c.toString(),c.get("inset_x").getAsFloat(),panel.textX(),0);
        assertEquals(c.toString(),c.get("inset_y").getAsFloat(),panel.textY(),0);
      }
    }
  }
  @Test public void cursorPaddingMatchesOriginalCpuRoutine()throws Exception {check("cursor");}
  @Test public void tradingPaddingMatchesOriginalCpuRoutine()throws Exception {check("trading");}
}

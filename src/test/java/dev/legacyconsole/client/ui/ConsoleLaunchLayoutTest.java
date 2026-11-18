package dev.legacyconsole.client.ui;

import static org.junit.Assert.*;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import dev.legacyconsole.port.GuiButton;
import org.junit.Test;

public class ConsoleLaunchLayoutTest {
  @Test public void controlsFollowOriginalFullyOpenHdSceneCoordinates() throws Exception {
    JsonObject source;
    try (Reader reader=new InputStreamReader(getClass().getResourceAsStream(
        "/assets/legacyconsole/lce_widget_layout.json"),StandardCharsets.UTF_8)) {
      source=new JsonParser().parse(reader).getAsJsonObject();
    }
    for (boolean create : new boolean[] {true,false}) {
      ConsoleLaunchLayout layout=new ConsoleLaunchLayout(640,360,create);
      JsonObject sprites=source.getAsJsonObject(create?"CreateWorldMenu1080.swf":"LoadMenu1080.swf").getAsJsonObject("sprites");
      JsonArray frames=sprites.getAsJsonArray(create?"CreateWorldMenu1080_fla.Panel_3":"Panel");
      JsonArray nodes=frames.get(frames.size()-1).getAsJsonObject().getAsJsonArray("nodes");
      String[][] controls={{"mode","GameModeToggle"},{"difficulty","Difficulty"},
          {"packs","TexturePackSelector"},{"more","MoreOptions"},
          {"launch",create?"NewWorld":"LoadSettings"}};
      for(String[] control:controls) {
        float[] r=LceWidgets.exact(layout.place(new GuiButton(0,0,0,""),control[0]));
        JsonArray matrix=null;
        for(JsonElement e:nodes) {
          JsonObject node=e.getAsJsonObject();
          if(node.has("name") && node.get("name").getAsString().equals(control[1]))matrix=node.getAsJsonArray("matrix");
        }
        assertNotNull(matrix);
        assertEquals(layout.x+matrix.get(4).getAsFloat()/3,r[0],.001f);
        assertEquals(layout.y+matrix.get(5).getAsFloat()/3,r[1],.001f);
        if(control[0].equals("difficulty"))assertEquals(matrix.get(0).getAsFloat()*300,r[2],.001f);
      }
      assertTrue(layout.sidebarX>layout.x+layout.w-12);
      assertTrue(layout.sidebarX+layout.sidebarW<640);
      assertTrue(layout.sidebarY+layout.sidebarH<332);
    }
  }
}

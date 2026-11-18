package dev.legacyconsole.client.ui;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4ContainerLogicTest {
  private JsonObject reference()throws Exception {
    try(var r=new InputStreamReader(getClass().getResourceAsStream("/ps4/containers-native.json"),StandardCharsets.UTF_8)) {
      return new JsonParser().parse(r).getAsJsonObject();
    }
  }
  @Test public void matchesNativeInventoryRecentering()throws Exception {
    var data=reference();var inputs=data.getAsJsonObject("layout_inputs");
    var armor=inputs.getAsJsonArray("EquipmentHolder");var player=inputs.getAsJsonArray("iggy_player");
    float dx=Ps4ContainerLogic.equipmentOffset(armor.get(0).getAsFloat(),armor.get(1).getAsFloat(),
        player.get(0).getAsFloat(),player.get(1).getAsFloat(),inputs.getAsJsonArray("BackgroundPanel").get(1).getAsFloat());
    for(var event:data.getAsJsonArray("layout")) {
      var row=event.getAsJsonArray();if(row.get(0).getAsString().equals("offset_x"))assertEquals(row.get(2).getAsFloat(),dx,0);
    }
  }
  @Test public void matchesOriginalInventoryAndChestSectionInstructions()throws Exception {
    var nav=reference().getAsJsonObject("navigation");int count=0;
    for(String name:new String[]{"inventory","chest"})for(var item:nav.getAsJsonArray(name)) {
      var row=item.getAsJsonArray();int split=row.get(0).getAsInt(),s=row.get(1).getAsInt(),d=row.get(2).getAsInt();
      var r=name.equals("inventory")?Ps4ContainerLogic.inventory(s,d,row.get(3).getAsInt(),row.get(4).getAsInt(),split!=0):Ps4ContainerLogic.chest(s,d,split!=0);
      assertEquals(name+row,row.get(5).getAsInt(),r.section());assertEquals(row.get(6).getAsInt(),r.xOffset());assertEquals(row.get(7).getAsInt(),r.yOffset());count++;
    }
    assertEquals(2448,count);
  }
}

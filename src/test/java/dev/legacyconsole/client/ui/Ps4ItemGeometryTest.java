package dev.legacyconsole.client.ui;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4ItemGeometryTest {
  @Test public void matchesOriginalSlotInstructionsAndAuthoredPointerAtEveryZoom() throws Exception {
    JsonObject data;
    try(var reader=new InputStreamReader(getClass().getResourceAsStream("/ps4/item-viewports-native.json"),StandardCharsets.UTF_8)) {
      data=new JsonParser().parse(reader).getAsJsonObject();
    }
    for(var node:data.getAsJsonArray("slot_cases")) {
      var row=node.getAsJsonObject();
      for(float zoom:new float[]{2f/9,1f/3,4f/9,2f/3,1,4f/3,2}) {
        float[] actual=Ps4ItemGeometry.viewport(10,20,row.get("width").getAsFloat()*zoom,row.get("height").getAsFloat()*zoom,zoom);
        var expected=row.getAsJsonArray("rectangle");
        assertEquals(10+expected.get(0).getAsFloat()*zoom,actual[0],.0001f);
        assertEquals(20+expected.get(2).getAsFloat()*zoom,actual[1],.0001f);
        assertEquals((expected.get(1).getAsFloat()-expected.get(0).getAsFloat())*zoom,actual[2],.0001f);
        assertEquals((expected.get(3).getAsFloat()-expected.get(2).getAsFloat())*zoom,actual[3],.0001f);
      }
    }
    var pointer=data.getAsJsonObject("pointer_item");var matrix=pointer.getAsJsonArray("matrix");var bounds=pointer.getAsJsonArray("shape_bounds");
    for(float zoom:new float[]{2f/3,1,4f/3,2}) {
      float[] carried=Ps4ItemGeometry.carried(100,200,zoom);
      assertEquals(100+matrix.get(4).getAsFloat()*zoom,carried[0],.0001f);
      assertEquals(200+matrix.get(5).getAsFloat()*zoom,carried[1],.0001f);
      assertEquals((bounds.get(1).getAsFloat()-bounds.get(0).getAsFloat())*matrix.get(0).getAsFloat()*zoom,carried[2],.0001f);
      assertEquals((bounds.get(3).getAsFloat()-bounds.get(2).getAsFloat())*matrix.get(3).getAsFloat()*zoom,carried[3],.0001f);
    }
  }
}

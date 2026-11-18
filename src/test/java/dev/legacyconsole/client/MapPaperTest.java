package dev.legacyconsole.client;
import static org.junit.Assert.*;
import org.junit.Test;
import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
public class MapPaperTest {
 private JsonObject original(){return new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/ps4-map-render-vectors.json"),StandardCharsets.UTF_8)).getAsJsonObject();}
 @Test public void unexploredPixelsMatchOriginalPs4Lut(){
  JsonArray entries=original().getAsJsonArray("unexploredLutEntries");
  for(int y=0;y<128;y++)for(int x=0;x<128;x++)
   assertEquals(entries.get((x+y)&3).getAsInt(),ConsoleMapRenderer.unexploredPixel(x,y));
 }
 @Test public void originalMapAndCoordinateDepthsArePreserved(){
  JsonObject original=original();
  for(JsonElement vertex:original.getAsJsonArray("mapQuadXYZUV"))
   assertEquals(vertex.getAsJsonArray().get(2).getAsFloat(),ConsoleMapRenderer.IMAGE_DEPTH,0);
  assertEquals(original.getAsJsonArray("coordinateTransforms").get(0).getAsJsonObject().getAsJsonArray("values").get(2).getAsFloat(),ConsoleMapRenderer.TEXT_DEPTH,0);
 }
 @Test public void originalDecorationLayerCallsArePreserved(){
  var nativeCalls=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/ps4/map-layers-native.json"),StandardCharsets.UTF_8)).getAsJsonObject();
  float layer=ConsoleMapRenderer.ICON_DEPTH;
  for(var value:nativeCalls.getAsJsonArray("icons")){
   var row=value.getAsJsonObject();
   assertEquals(row.get("translation_z").getAsFloat(),layer,0);
   float local=nativeCalls.get("index_vertex_step").getAsFloat()*row.get("index").getAsInt();
   float scale=nativeCalls.getAsJsonArray("icon_scale").get(2).getAsFloat();
   assertTrue("Cursor must be in front of the map image",layer+scale*local<ConsoleMapRenderer.IMAGE_DEPTH);
   layer=ConsoleMapRenderer.nextIconLayer(layer);assertEquals(row.get("next_layer").getAsFloat(),layer,0);
  }
 }
}

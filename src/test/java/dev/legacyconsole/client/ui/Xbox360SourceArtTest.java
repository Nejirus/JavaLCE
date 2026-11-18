package dev.legacyconsole.client.ui;
import static org.junit.Assert.*;
import com.google.gson.*;
import java.nio.file.*;
import java.security.MessageDigest;

import org.junit.Test;
public class Xbox360SourceArtTest {
 private static String hex(byte[] bytes){var s=new StringBuilder();for(byte b:bytes)s.append(String.format("%02x",b&255));return s.toString();}
 @Test public void bothResolutionSetsUseOriginalXbox360PngsAndXuiGeometry()throws Exception{
  var ref=new JsonParser().parse(Files.readString(Path.of("LCE-XBOX360-2312.json"))).getAsJsonObject();
  assertEquals(22,ref.getAsJsonArray("assets").size());
  for(var entry:ref.getAsJsonArray("assets")){var a=entry.getAsJsonObject();assertTrue(a.get("source").getAsString().contains("X360ControllerIcons/"));assertEquals(a.get("sha256").getAsString(),hex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(Path.of(a.get("destination").getAsString())))));}
  for(int height:new int[]{720,1080,1440,2160}){
   var family=LceGlyphLayout.family("xbox360",height);float scale=height<1080?1:1.5f;
   for(var entry:ref.getAsJsonObject("glyphs").entrySet()){
    var expected=entry.getValue().getAsJsonObject();var glyph=family.glyph(entry.getKey());
    for(int i=0;i<4;i++)assertEquals(expected.getAsJsonArray("bounds").get(i).getAsFloat()*scale,glyph.bounds[i],.001f);
    assertEquals(expected.getAsJsonArray("layers").size(),glyph.layers.length);
    for(int i=0;i<glyph.layers.length;i++){var layer=expected.getAsJsonArray("layers").get(i).getAsJsonObject();assertEquals(layer.get("path").getAsString(),glyph.layers[i].path);assertTrue(glyph.layers[i].path.startsWith("lce/prompts/xbox360/"));}
   }
   assertFalse(family.glyph("left_trigger").layers[0].path.equals(LceGlyphLayout.family("xboxone",height).glyph("left_trigger").layers[0].path));
  }
 }
}

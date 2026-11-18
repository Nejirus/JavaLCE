package dev.legacyconsole.client.ui;
import static org.junit.Assert.*;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.junit.Test;
public class Ps4FrontendTest {
 private static String hex(byte[] data){StringBuilder s=new StringBuilder();for(byte b:data)s.append(String.format("%02x",b&255));return s.toString();}
 @Test public void panoramaRetainsBothOriginalJpegsAndTimeline()throws Exception{
  JsonObject data;try(var r=new InputStreamReader(getClass().getResourceAsStream("/assets/legacyconsole/ps4_panorama.json"),StandardCharsets.UTF_8)){data=new JsonParser().parse(r).getAsJsonObject();}
  for(String layer:new String[]{"day","night"}){
   var record=data.getAsJsonObject("images").getAsJsonObject(layer);
   try(var in=getClass().getResourceAsStream("/assets/legacyconsole/textures/ps4/panorama/"+layer+".jpg")){
    byte[] bytes=in.readAllBytes();assertEquals(0xff,bytes[0]&255);assertEquals(0xd8,bytes[1]&255);
    assertEquals(record.get("sha256").getAsString(),hex(MessageDigest.getInstance("SHA-256").digest(bytes)));
   }
   assertEquals(4100,data.getAsJsonObject("frames").getAsJsonArray(layer).size());
   assertEquals(0,Ps4Panorama.offset(0,layer.equals("night")),0);
   assertEquals(-1.5f,Ps4Panorama.offset(1/30.0,layer.equals("night")),0);
   assertEquals(-6150,Ps4Panorama.offset(4099/30.0,layer.equals("night")),0);
   assertEquals(0,Ps4Panorama.offset(4100/30.0,layer.equals("night")),0);
  }
 }
 @Test public void newScenesFitAllStopsAndAspectRatios(){
  int previous=dev.legacyconsole.client.ClientConfig.consoleUiSize;
  try{for(int size=0;size<3;size++){dev.legacyconsole.client.ClientConfig.consoleUiSize=size;
   for(int[] screen:new int[][]{{1280,720},{1920,1080},{2560,1440},{3840,2160},{1024,768},{1600,1000},{1512,982},{3024,1964}})
    for(String name:new String[]{"AnvilMenu","FurnaceMenu","InGameHostOptions","InGameInfoMenu","InGamePlayerOptions","InGameTeleportMenu"}){
     var art=new Ps4UiArt(name,screen[0],screen[1]);var r=art.panelBounds();assertTrue(name,r.x()>=0&&r.y()>=0&&r.x()+r.w()<=screen[0]+.01&&r.y()+r.h()<=screen[1]+.01);
    }
  }}finally{dev.legacyconsole.client.ClientConfig.consoleUiSize=previous;}
 }
}

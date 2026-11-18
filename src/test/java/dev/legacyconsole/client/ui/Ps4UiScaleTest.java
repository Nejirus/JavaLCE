package dev.legacyconsole.client.ui;
import static org.junit.Assert.*;
import com.google.gson.*;
import dev.legacyconsole.client.ClientConfig;
import java.nio.file.*;
import org.junit.Test;
public class Ps4UiScaleTest {
 @Test public void stopsAndRatiosMatchTheOriginalPs4Frames()throws Exception{
  var d=new JsonParser().parse(Files.readString(Path.of("PS4-UI-SCALE-2312.json"))).getAsJsonObject();
  assertEquals("[1,2,3]",d.getAsJsonArray("stops").toString());
  for(int i=0;i<3;i++)assertEquals(d.getAsJsonArray("factors").get(i).getAsFloat(),Ps4UiScale.factor(i),.000001f);
  assertEquals(2f/3,Ps4UiScale.factor(-1),0);assertEquals(4f/3,Ps4UiScale.factor(5),0);
 }
 @Test public void menuSizesKeepNormalTransformsAndDoNotAlterHudSize(){
  int original=ClientConfig.consoleUiSize,hud=ClientConfig.hudSize;
  try{
   for(int[] frame:new int[][]{{1280,720},{1920,1080},{2560,1440},{3840,2160},{1024,768},{1600,1000},{1512,982},{3024,1964}}){
    int width=frame[0]/2,height=frame[1]/2;float base=Math.min(width/1920f,height/1080f);
    for(String scene:new String[]{"BrewingStandMenu","EnchantingMenu","CartographyMenu","BeaconMenu","TradingMenu"}){
     float previous=0;
     for(int size=0;size<3;size++){ClientConfig.consoleUiSize=size;var art=new Ps4UiArt(scene,width,height);var bounds=art.panelBounds();assertTrue(bounds.x()>=0);assertTrue(bounds.y()>=0);assertTrue(bounds.x()+bounds.w()<=width+.01);assertTrue(bounds.y()+bounds.h()<=height+.01);assertTrue(art.scale>previous);previous=art.scale;if(size==1)assertEquals(base,art.scale,0);}
    }
    for(int size=0;size<3;size++){ClientConfig.consoleUiSize=size;var art=new Ps4CraftingArt(width,height);assertTrue(art.scale()>0);if(size==1)assertEquals(base,art.scale(),0);}
    assertEquals(hud,ClientConfig.hudSize);
   }
  }finally{ClientConfig.consoleUiSize=original;}
 }
 @Test public void popupRemainsAnchoredAndFitsLongTextAtEveryStop(){
  for(int[] frame:new int[][]{{640,360},{512,384},{756,491},{1512,982}})for(int size=0;size<3;size++)for(float h:new float[]{315,600,1000}){
   var p=Ps4UiScale.popup(frame[0],frame[1],size,h);float left=p.x()+1080*p.scale(),top=p.y()+75*p.scale();assertTrue(left>=0);assertTrue(top>=0);assertTrue(left+750*p.scale()<=frame[0]+.001);assertTrue(top+h*p.scale()<=frame[1]+.001);
  }
 }
}

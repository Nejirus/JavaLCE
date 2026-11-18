package dev.legacyconsole.client.ui;
import static org.junit.Assert.*;
import org.junit.Test;
public class ConsoleContainerScaleTest {
 @Test public void normalRetainsExistingLayoutAndSharedStopsUseThePs4Ratios(){
  for(int size=0;size<3;size++){
   var t=ConsoleContainerScale.fit(640,360,212,65,215,217,320,size);
   assertEquals(Ps4UiScale.factor(size),t.scale(),.000001f);
   if(size==1){assertEquals(0,t.offsetX(),0);assertEquals(0,t.offsetY(),0);}
  }
 }
 @Test public void supportedWindowsKeepMenuBoundsOnscreenAndMapInputBackToSlots(){
  int[][] windows={{640,360},{640,360},{640,360},{640,360},{1024,768},{800,500},{756,491},{756,491},{320,240}};
  float[][] panels={{215,217,0},{214,215,0},{215,207,0},{215,270,0},{321,212,36},{207,215,0}};
  for(int[] window:windows)for(float[] panel:panels)for(int size=0;size<3;size++){
   int w=window[0],h=window[1];float left=(w-panel[0])/2,top=Math.max(panel[2],(h-panel[1])/2-6)-panel[2],height=panel[1]+panel[2],footer=h-39;
   var t=ConsoleContainerScale.fit(w,h,left,top,panel[0],height,footer,size);float[] r=t.rectangle(left,top,panel[0],height);
   assertTrue(r[0]>=w*.05f-.001);assertTrue(r[1]>=h*.05f-.001);assertTrue(r[0]+r[2]<=w*.95f+.001);assertTrue(r[1]+r[3]<=footer+.001);
   for(float x:new float[]{left+10,left+panel[0]/2,left+panel[0]-10})for(float y:new float[]{top+10,top+height/2,top+height-10}){
    assertEquals(x,t.menuX(t.screenX(x)),.001);assertEquals(y,t.menuY(t.screenY(y)),.001);
   }
  }
 }
 @Test public void creativeTabsAndDoubleChestFitBeforeTheUnscaledFooter(){
  for(float[] bounds:new float[][]{{159,48,321,248},{212,39,215,270}}){
   var t=ConsoleContainerScale.fit(640,360,bounds[0],bounds[1],bounds[2],bounds[3],320,2);var r=t.rectangle(bounds[0],bounds[1],bounds[2],bounds[3]);
   assertTrue(t.scale()>1);assertTrue(t.scale()<Ps4UiScale.factor(2));assertTrue(r[1]>=18-.001);assertTrue(r[1]+r[3]<=320+.001);
  }
 }
 @Test public void controllerHolderRectanglesFollowTheSameTransformAsMouseCoordinates(){
  var t=ConsoleContainerScale.fit(640,360,212,65,215,217,320,2);var r=t.rectangle(227,182,21,21);
  assertEquals(21*t.scale(),r[2],0);assertEquals(237.5,t.menuX(r[0]+r[2]/2),.001);assertEquals(192.5,t.menuY(r[1]+r[3]/2),.001);
  var identity=ConsoleContainerScale.IDENTITY;assertArrayEquals(new float[]{10,20,30,40},identity.rectangle(10,20,30,40),0);assertEquals(10,identity.menuX(10),0);
 }
}

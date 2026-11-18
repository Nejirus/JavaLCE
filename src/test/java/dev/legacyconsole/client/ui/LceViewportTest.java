package dev.legacyconsole.client.ui;

import static org.junit.Assert.*;
import org.junit.Test;
import dev.legacyconsole.client.loading.LceLoadingLayout;
import dev.legacyconsole.client.LceHudLayout;

public class LceViewportTest {
  @Test public void standardProfileUsesFourByThreeAndKeepsSixteenByTenWide() {
    for(int[] size:new int[][]{{640,480},{800,600},{1024,768},{1280,960},{1600,1200}})
      assertTrue(LceViewport.standard(size[0],size[1]));
    for(int[] size:new int[][]{{1280,720},{1920,1080},{1280,800},{1920,1200},{3024,1964}})
      assertFalse(LceViewport.standard(size[0],size[1]));
  }

  @Test public void fallbackSceneFitsUniformlyInsideTheStandardViewport() {
    assertEquals(.5f,LceViewport.scale(640,480),0);
    assertEquals(0,LceViewport.x(640,480,0),0);
    assertEquals(60,LceViewport.y(640,480,0),0);
    assertEquals(640,LceViewport.x(640,480,1280),0);
    assertEquals(420,LceViewport.y(640,480,720),0);
    assertEquals(88,LceViewport.y(640,480,56),0);
    assertEquals(185,LceViewport.y(640,480,250),0);
  }

  @Test public void wideSceneAnchorsRetainTheirExistingCoordinates() {
    for(int[] size:new int[][]{{640,360},{640,400},{756,491},{427,240}})
      for(float y:new float[]{56,176,250,300,374,390,510})
        assertEquals(size[1]*y/720f,LceViewport.y(size[0],size[1],y),.0001f);
  }

  @Test public void standardLoadingPanelsStayInsideTheFivePercentSafeArea() {
    for(int[] size:new int[][]{{640,480},{800,600},{1024,768},{320,240}}) {
      for(var box:new LceLoadingLayout.Bounds[]{LceLoadingLayout.bar(size[0],size[1]),LceLoadingLayout.tips(size[0],size[1])}) {
        assertTrue(box.x()>=size[0]*.05f);assertTrue(box.x()+box.width()<=size[0]*.95f);
        assertTrue(box.y()>=size[1]*.05f);assertTrue(box.y()+box.height()<=size[1]*.95f);
      }
    }
  }

  @Test public void standardProfilesAndRetinaKeepAnIntegerGuiScale() {
    int[][] cases={{640,480,1},{800,600,1},{1280,960,2},{1920,1440,3},
        {2560,1920,4},{1920,1200,3},{3024,1964,4},{3456,2234,5}};
    for(int[] c:cases)assertEquals(c[2],ConsoleDisplayScale.automaticFactor(c[0],c[1]));
  }

  @Test public void standardCursorUsesTheSourceSdInputScale() {
    assertEquals(.6f,LceViewport.cursorInputScale(640,480),0);
    assertEquals(.6f,LceViewport.cursorInputScale(800,600),0);
    assertEquals(1,LceViewport.cursorInputScale(640,360),0);
    assertEquals(1,LceViewport.cursorInputScale(640,400),0);
  }

  @Test public void standardSettingsRowsAndHudRemainInsideTheWindow() {
    for(int[] size:new int[][]{{640,480},{800,600},{320,240}}) {
      for(int selected=0;selected<30;selected++) {
        var options=new ConsoleOptionLayout(size[0],size[1],new boolean[30],0,0,selected,15);
        assertTrue(options.x>=0&&options.x+options.w<=size[0]);
        assertTrue(options.y+options.h<=size[1]-28);
        assertTrue(selected>=options.first&&selected<options.first+options.rows);
      }
      for(int hudSize=0;hudSize<3;hudSize++) {
        var hud=new LceHudLayout(size[0],size[1],960,2,0,hudSize);
        assertTrue(hud.x+.001f>=size[0]*.05f);assertTrue(hud.x+hud.width<=size[0]*.95f+.001f);
        assertTrue(hud.hotbarY+31.5f*hud.factor<=size[1]*.95f+.001f);
      }
    }
  }
}

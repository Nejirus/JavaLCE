package dev.legacyconsole.client.ui;

import dev.legacyconsole.client.ClientConfig;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4ContainerArtTest {
  @Test public void originalScenesAndEquipmentUseRecoveredTransforms() {
    assertArrayEquals(new float[]{637,229},Ps4UiArt.DATA.scenes.get("ChestMenu").root,0);
    assertArrayEquals(new float[]{637,134},Ps4UiArt.DATA.scenes.get("ChestLargeMenu").root,0);
    var scene=Ps4UiArt.DATA.scenes.get("InventoryMenu");
    assertArrayEquals(new float[]{637,214},scene.root,0);
    assertArrayEquals(new float[]{0,189,0,252},scene.player_bounds,0);
    assertEquals(5,scene.empty_icons.size());
    assertEquals(38,scene.controls.get("armorList").matrix[4],0);
    assertEquals(120,scene.controls.get("iggy_player").matrix[4],0);
    assertEquals(332,scene.controls.get("shieldList").matrix[4],0);
    assertEquals(2,scene.objects.get("PlayerBackground").size());
    assertTrue(Ps4UiArt.DATA.panels.containsKey("FJ_PanelRecess"));
  }
  @Test public void chestAndInventoryFitAllConsoleSizesAndFramebufferProfiles() {
    int previous=ClientConfig.consoleUiSize;
    try {
      for(int size=0;size<3;size++)for(String name:new String[]{"ChestMenu","ChestLargeMenu","InventoryMenu"})
      for(int[] dimensions:new int[][]{{1280,720},{1920,1080},{2560,1440},{3840,2160},{1024,768},{1600,1000},{1512,982},{3024,1964}}) {
        ClientConfig.consoleUiSize=size;
        var art=new Ps4UiArt(name,dimensions[0],dimensions[1]);var r=art.panelBounds();
        assertTrue(name+r,r.x()>=0&&r.y()>=0&&r.x()+r.w()<=dimensions[0]&&r.y()+r.h()<=dimensions[1]);
        assertEquals(63*art.scale,art.cell("hotbarList",0,0,9,1).w(),.01);
        assertEquals(63*art.scale,art.cell("inventoryList",0,0,9,3).h(),.01);
      }
    } finally {ClientConfig.consoleUiSize=previous;}
  }
}

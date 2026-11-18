package dev.legacyconsole.client.ui;

import static org.junit.Assert.*;
import java.security.MessageDigest;
import java.util.*;
import javax.imageio.ImageIO;
import org.junit.Test;

public class Ps4CraftingArtTest {
  @Test public void sourceTransformsFitSupportedDisplayShapes() {
    assertArrayEquals(new float[]{496.5f,170.5f},Ps4CraftingArt.DATA.root,0);
    for(int[] size:new int[][]{{1280,720},{1920,1080},{2560,1440},{3840,2160},{1600,1000},{1024,768}}) {
      var art=new Ps4CraftingArt(size[0],size[1]);
      var inventory=art.control("Inventory");var output=art.control("CraftingOutput");
      assertEquals(432*art.scale(),inventory.w(),.01);
      assertEquals(144*art.scale(),inventory.h(),.01);
      assertEquals(108*art.scale(),output.w(),.01);
      assertTrue(inventory.x()+inventory.w()<size[0]);assertTrue(inventory.y()+inventory.h()<size[1]);
      assertEquals(Math.min(size[0]/1920f,size[1]/1080f),art.scale(),0);
    }
  }
  @Test public void usesOriginalPs4BannerTabIcons() {
    assertEquals(List.of("cr","ld","mc","bo","cre"),Ps4CraftingArt.DATA.tab_patterns);
    assertEquals("Borders, Backgrounds and Gradients",Ps4CraftingArt.label("f8e4117f"));
    assertEquals("Ingredients",Ps4CraftingArt.label("cb14bd42"));
  }
  @Test public void originalSlotAndScrollArtUsesCorrectChannels() throws Exception {
    try(var in=getClass().getResourceAsStream("/assets/legacyconsole/textures/lce/ps4/crafting/iconholderred_1x1border.png")) {
      var image=ImageIO.read(in);int pixel=image.getRGB(10,10);
      assertTrue((pixel>>16&255)>(pixel&255));assertEquals(20,image.getWidth());
    }
    assertEquals(108,Ps4CraftingArt.DATA.sprites.get("Craft_Highlight_L_Small").size[0],0);
    assertEquals(388,Ps4CraftingArt.DATA.sprites.get("Crafting_3SlotLargeV").size[1],0);
    assertEquals(48,Ps4CraftingArt.DATA.sprites.get("scrollUp").size[0],0);
  }
}

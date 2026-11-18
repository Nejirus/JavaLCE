package dev.legacyconsole.client.ui;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4TradingTest {
  private JsonObject nativeData()throws Exception{
    try(var in=new InputStreamReader(getClass().getResourceAsStream("/ps4/trading-native.json"),StandardCharsets.UTF_8)){
      return new Gson().fromJson(in,JsonObject.class);
    }
  }
  @Test public void navigationMatches360OriginalPs4Transitions()throws Exception {
    for(var value:nativeData().getAsJsonArray("navigation")) {
      var row=value.getAsJsonArray();int count=row.get(0).getAsInt(),offset=row.get(1).getAsInt(),col=row.get(2).getAsInt();
      var list=new Ps4TradeList();list.update(count);
      list.choose(offset+9);list.choose(offset+col);
      list.choose(list.selected()+(row.get(3).getAsInt()==6?1:-1));
      assertEquals(row.toString(),row.get(4).getAsInt(),list.offset());
      assertEquals(row.toString(),row.get(5).getAsInt(),list.column());
    }
  }
  @Test public void xpPreviewMatches100OriginalPs4Results()throws Exception {
    for(var value:nativeData().getAsJsonArray("experience")) {
      var r=value.getAsJsonObject();
      assertEquals(r.toString(),r.get("bits").getAsInt(),Float.floatToRawIntBits(Ps4TradeList.preview(
          r.get("level").getAsInt(),r.get("current").getAsFloat(),r.get("xp").getAsInt(),r.get("range").getAsInt())));
    }
  }
  @Test public void noOffersStillHasTenSelectableEmptyCells() {
    var list=new Ps4TradeList();list.update(0);list.choose(9);
    assertEquals(9,list.column());assertEquals(-1,list.serverIndex());assertFalse(list.right());assertFalse(list.left());
  }
  @Test public void offerRefreshKeepsServerIndicesIncludingExhaustedOffers() {
    var list=new Ps4TradeList();list.update(13);list.choose(11);list.update(13);
    assertEquals(11,list.serverIndex());assertEquals(2,list.offset());
    list.update(2);assertEquals(9,list.selected());assertEquals(-1,list.serverIndex());assertEquals(0,list.offset());
    list.choose(1);assertEquals(1,list.serverIndex());
  }
  @Test public void tradingUsesAuthoredSceneAndHudAssets() {
    var scene=Ps4UiArt.DATA.scenes.get("TradingMenu");
    assertArrayEquals(new float[]{531,240},scene.root,0);
    assertArrayEquals(new float[]{16.000061f,0,0,1.6000061f,26,127},scene.controls.get("TradingBar").matrix,.0001f);
    assertEquals(177.65f,Ps4UiArt.DATA.small_label.bounds[1],.0001f);
    assertEquals(3,scene.experience.size());assertEquals(4,scene.objects.size());
    assertEquals("experience_bar_full_white",scene.experience.get(1).symbol);
    assertEquals("Novice",Ps4UiArt.text(Ps4TradeList.levelKey(0)));
    assertEquals("Master",Ps4UiArt.text(Ps4TradeList.levelKey(4)));
    assertNull(Ps4TradeList.levelKey(5));
  }
  @Test public void verticalTabsUseTheirSeparateAuthoredIconOffsets() {
    var on=Ps4CraftingArt.DATA.vertical_tab_icons.get("on");var off=Ps4CraftingArt.DATA.vertical_tab_icons.get("off");
    assertEquals(38,on[4],0);assertEquals(30,off[4],0);
    assertEquals(25.65,on[5],.0001);assertEquals(26.05,off[5],.0001);
    for(int i=1;i<=4;i++) {
      var active=Ps4CraftingArt.DATA.controls.get("TabV_"+i);var inactive=Ps4CraftingArt.DATA.controls.get("TabV_"+i+"_Off");
      assertEquals(-75,active[4]+on[4],.0001);assertEquals(-69,inactive[4]+off[4],.0001);
    }
  }
}

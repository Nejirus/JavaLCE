package dev.legacyconsole.client.ui;

import com.google.gson.*;
import dev.legacyconsole.client.LceHudLayout;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4InformationLayoutTest {
  private JsonObject nativeData()throws Exception{
    try(var r=new InputStreamReader(getClass().getResourceAsStream("/ps4/information-native.json"),StandardCharsets.UTF_8)){
      return new Gson().fromJson(r,JsonObject.class);
    }
  }
  @Test public void matchesOriginalEffectStackForEverySupportedEffectCount()throws Exception{
    for(var e:nativeData().getAsJsonArray("layouts")){
      var c=e.getAsJsonObject();int count=c.get("count").getAsInt();var ys=c.getAsJsonArray("y");
      for(int i=0;i<count;i++)assertEquals("count="+count+" row="+i,ys.get(i).getAsFloat(),Ps4InformationLayout.effectY(i,count,567,473,.8f),0);
    }
  }
  @Test public void matchesOriginalShortDurationPulse()throws Exception{
    for(var e:nativeData().getAsJsonArray("pulses")){
      var c=e.getAsJsonObject();assertEquals("ticks="+c.get("ticks"),c.get("alpha").getAsFloat(),Ps4InformationLayout.effectAlpha(c.get("ticks").getAsInt()),.00003f);
    }
  }
  @Test public void nativeSelectionStartsSixtyTicksOnlyForNonemptyText()throws Exception{
    for(var e:nativeData().getAsJsonArray("selections")){
      var c=e.getAsJsonObject();assertEquals(c.get("ticks").getAsInt(),c.get("length").getAsInt()==0?0:Ps4InformationLayout.SELECTED_TICKS);
    }
  }
  @Test public void exportedEffectOrderMatchesTheOriginalBarSelectionRoutine()throws Exception{
    for(var e:nativeData().getAsJsonArray("ordering")){
      var c=e.getAsJsonObject();assertEquals(c.get("slot").getAsInt(),Ps4Information.DATA.effect_order.get(c.get("effect_id").getAsString()).intValue());
    }
  }
  @Test public void fiveLinePopupsStayBelowCrosshairAtAllHudSizesAndDisplayProfiles(){
    int[][] profiles={{1280,720},{1920,1080},{2560,1440},{3840,2160},{1280,960},{1440,900},{3024,1964}};
    for(var p:profiles)for(int gui:new int[]{1,2,3,4,6}){
      int width=(p[0]+gui-1)/gui,height=(p[1]+gui-1)/gui;
      if(width<320||height<180)continue;
      for(int size=0;size<3;size++)for(int lift:new int[]{0,20,50})for(int lines=1;lines<=5;lines++){
        var hud=new LceHudLayout(width,height,p[1],gui,lift,size);
        float base=Math.min(width/1920f,height/1080f),bottom=hud.rootY-16*hud.factor+27*base;
        float safe=Ps4InformationLayout.crosshairClearance(p[1],size)+3*base;
        var r=Ps4InformationLayout.hotbar(width,height,bottom,3*base*Ps4UiScale.factor(size),550,lines,Ps4InformationLayout.crosshairClearance(p[1],size));
        assertTrue(r.y()>=height/2f+safe-.001f||r.y()+r.height()<=height/2f-safe+.001f);assertTrue(r.x()>=0);
        assertTrue(r.x()+r.width()<=width);assertTrue(r.y()+r.height()<=bottom+.001f);
        assertTrue(r.scale()>0);
      }
    }
  }
  @Test public void durationUsesPs4MinuteClampAndSeconds(){
    assertEquals("0:00",Ps4InformationLayout.duration(0));assertEquals("0:59",Ps4InformationLayout.duration(1199));
    assertEquals("1:00",Ps4InformationLayout.duration(1200));assertEquals("99:01",Ps4InformationLayout.duration(6001*20));
  }
}

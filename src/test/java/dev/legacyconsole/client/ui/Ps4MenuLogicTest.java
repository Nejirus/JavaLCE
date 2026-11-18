package dev.legacyconsole.client.ui;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;
public class Ps4MenuLogicTest {
 @Test public void cartographyModeUsesNativePriorityAndOutputPresence(){
  assertEquals(0,Ps4MenuLogic.cartographyMode(false,false,false,false,true,false));
  assertEquals(1,Ps4MenuLogic.cartographyMode(false,false,false,false,true,true));
  assertEquals(2,Ps4MenuLogic.cartographyMode(true,true,false,false,false,false));
  assertEquals(3,Ps4MenuLogic.cartographyMode(true,false,true,false,false,false));
  assertEquals(4,Ps4MenuLogic.cartographyMode(true,false,false,true,false,false));
  assertEquals(0,Ps4MenuLogic.cartographyMode(true,false,false,false,true,false));
  assertEquals(5,Ps4MenuLogic.cartographyMode(true,false,false,false,true,true));
 }
 @Test public void brewingArrowFillMatchesOriginalOverride(){for(var e:nativeData().getAsJsonArray("brewing")){
  var row=e.getAsJsonObject();assertEquals(row.get("arrow_fill_bits").getAsInt(),
   Float.floatToRawIntBits(Ps4MenuLogic.brewingArrowFill(Ps4MenuLogic.brewingArrow(row.get("remaining").getAsInt()))));
 }}
 private static JsonObject nativeData(){try(var r=new InputStreamReader(Ps4MenuLogicTest.class.getResourceAsStream("/ps4/menu-native.json"),StandardCharsets.UTF_8)){return new JsonParser().parse(r).getAsJsonObject();}catch(IOException e){throw new AssertionError(e);}}
 @Test public void brewingProgressMatchesAllOriginalTicks(){for(var e:nativeData().getAsJsonArray("brewing")){var row=e.getAsJsonObject();int tick=row.get("remaining").getAsInt();assertEquals(row.get("arrow_bits").getAsInt(),Float.floatToRawIntBits(Ps4MenuLogic.brewingArrow(tick)));assertEquals(row.get("bubble_value").getAsInt(),Ps4MenuLogic.brewingBubbles(tick));assertEquals(row.get("bubble_bits").getAsInt(),Float.floatToRawIntBits(Ps4MenuLogic.progress(Ps4MenuLogic.brewingBubbles(tick),0,30)));assertEquals(row.get("fuel_bits").getAsInt(),Float.floatToRawIntBits(Ps4MenuLogic.brewingFuel(row.get("fuel").getAsInt())));}}
 @Test public void beaconButtonFramesMatchOriginal(){for(var e:nativeData().getAsJsonArray("beacon_buttons")){var r=e.getAsJsonObject();assertEquals(r.get("frame").getAsInt(),Ps4MenuLogic.beaconButtonFrame(r.get("enabled").getAsBoolean(),r.get("selected").getAsBoolean(),r.get("focused").getAsBoolean()));}}
 @Test public void sectionRoutingMatchesOriginal(){for(var menu:nativeData().getAsJsonObject("navigation").entrySet())for(var e:menu.getValue().getAsJsonArray()){var r=e.getAsJsonArray();boolean split=r.get(0).getAsInt()!=0;int s=r.get(1).getAsInt(),d=r.get(2).getAsInt(),x=r.get(3).getAsInt();var route=switch(menu.getKey()){case "brewing"->Ps4MenuNavigation.brewing(s,d,x,split);case "enchanting"->Ps4MenuNavigation.enchanting(s,d,x,split);case "cartography"->Ps4MenuNavigation.cartography(s,d,x,split);case "beacon"->Ps4MenuNavigation.beacon(s,d,x,split,true);case "anvil"->Ps4MenuNavigation.anvil(s,d,x,split);case "furnace"->Ps4MenuNavigation.furnace(s,d,x,split);default->throw new AssertionError();};String context=menu.getKey()+" "+r;assertEquals(context,r.get(4).getAsInt(),route.section());assertEquals(context,r.get(5).getAsInt(),route.xOffset());assertEquals(context,r.get(6).getAsInt(),route.yOffset());}}
 @Test public void enchantingNamesMatchOriginalWordSelections()throws Exception {
  JsonArray words;try(var reader=new InputStreamReader(getClass().getResourceAsStream("/assets/legacyconsole/ps4_enchantment_names.json"),StandardCharsets.UTF_8)){words=new JsonParser().parse(reader).getAsJsonObject().getAsJsonArray("words");}
  java.util.Map<String,Ps4EnchantmentNames> generators=new java.util.HashMap<>();
  for(var e:nativeData().getAsJsonArray("enchantment_names")) {
   var r=e.getAsJsonObject();String seed=r.get("seed").getAsString();
   var generator=generators.computeIfAbsent(seed,key->new Ps4EnchantmentNames(Long.parseUnsignedLong(key)));
   java.util.List<String> expected=new java.util.ArrayList<>();for(var index:r.getAsJsonArray("indices"))expected.add(words.get(index.getAsInt()).getAsString());
   assertEquals(seed+" draw "+r.get("draw"),String.join(" ",expected),generator.next());
  }
 }
}

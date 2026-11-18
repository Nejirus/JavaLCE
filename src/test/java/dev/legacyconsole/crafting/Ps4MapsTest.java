package dev.legacyconsole.crafting;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4MapsTest {
 private JsonObject vectors(){return new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/ps4-map-bounds-vectors.json"),StandardCharsets.UTF_8)).getAsJsonObject();}
 @Test public void allScalesAndNegativeHalfTilesMatchPs4Instructions(){
  for(JsonElement entry:vectors().getAsJsonArray("mapCenters")){
   JsonObject v=entry.getAsJsonObject();int scale=v.get("scale").getAsInt();
   MapItemSavedData data=Ps4Maps.fresh(v.get("x").getAsDouble(),v.get("z").getAsDouble(),(byte)scale,true,true,Level.OVERWORLD);
   assertEquals(v.toString(),v.get("centerX").getAsInt(),data.x);assertEquals(v.toString(),v.get("centerZ").getAsInt(),data.z);
   assertEquals(16384,data.colors.length);assertTrue(data.trackingPosition);assertTrue(data.unlimitedTracking);
   MapItemSavedData restored=MapItemSavedData.load(data.save(new CompoundTag()));
   assertEquals(data.x,restored.x);assertEquals(data.z,restored.z);assertEquals(data.scale,restored.scale);
  }
 }
 @Test public void enlargementUsesOriginalMapAreaAndKeepsTheSourceImage(){
  MapItemSavedData source=Ps4Maps.fresh(-512,512,(byte)2,true,true,Level.OVERWORLD);source.colors[0]=44;
  MapItemSavedData result=Ps4Maps.enlarged(source,source.scaled(1));
  assertEquals(0,result.x);assertEquals(1024,result.z);assertEquals(3,result.scale);assertTrue(result.unlimitedTracking);
  assertEquals(44,source.colors[0]);assertEquals(0,result.colors[0]);assertEquals(-256,source.x);
 }
 @Test public void classicMapKeepsItsPs4MarginsAndSmallMapFitsTheWorld(){
  for(var size:dev.legacyconsole.worldgen.Ps4WorldSize.values()){
   int inside=0;
   for(int pixel=0;pixel<128;pixel++)if(size.containsBlock((pixel-64)*8,0))inside++;
   assertEquals(size==dev.legacyconsole.worldgen.Ps4WorldSize.CLASSIC?108:128,inside);
  }
 }
 @Test public void firstMapChoiceMatchesPs4AndSurvivesReloadPerPlayer(){
  var history=new LegacyMapHistory();UUID a=new UUID(0,1),b=new UUID(0,2);
  JsonArray choices=vectors().getAsJsonArray("initialScales");
  assertEquals(choices.get(0).getAsJsonObject().get("scale").getAsInt(),history.nextScale(a));
  history.opened(a);var restored=LegacyMapHistory.load(history.save(new CompoundTag()));
  assertEquals(choices.get(1).getAsJsonObject().get("scale").getAsInt(),restored.nextScale(a));
  assertEquals(3,restored.nextScale(b));
 }
 @Test public void vanillaGridRemainsAvailableAndHasItsOwnOrigin(){
  assertNull(Ps4Maps.size(null));
  assertEquals(448,MapItemSavedData.createFresh(0,0,(byte)3,true,false,Level.OVERWORLD).x);
  assertEquals(0,Ps4Maps.fresh(0,0,(byte)3,true,false,Level.OVERWORLD).x);
 }
}

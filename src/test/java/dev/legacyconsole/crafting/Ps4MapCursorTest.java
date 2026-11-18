package dev.legacyconsole.crafting;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.maps.*;
import net.minecraft.network.chat.Component;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4MapCursorTest {
 @Test public void javaProjectionAlreadyMatches450OriginalPs4NearEdgeCases()throws Exception{
  var fixture=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/ps4/tutorial-map-239-native.json"),StandardCharsets.UTF_8)).getAsJsonObject();
  var method=MapItemSavedData.class.getDeclaredMethod("addDecoration",MapDecoration.Type.class,LevelAccessor.class,String.class,double.class,double.class,double.class,Component.class);method.setAccessible(true);
  for(var row:fixture.getAsJsonArray("map_cases")){
   var v=row.getAsJsonObject();var map=Ps4Maps.fresh(-512,512,v.get("scale").getAsByte(),true,true,Level.OVERWORLD);
   // Use a fixed center independent of finite-world map-center selection.
   var tag=map.save(new net.minecraft.nbt.CompoundTag());tag.putInt("xCenter",-512);tag.putInt("zCenter",512);map=MapItemSavedData.load(tag);
   method.invoke(map,MapDecoration.Type.PLAYER,null,"player",v.get("x").getAsDouble(),v.get("z").getAsDouble(),v.get("yaw").getAsDouble(),null);
   var icon=map.decorations.get("player");assertNotNull(v.toString(),icon);
   assertEquals(v.toString(),v.get("type").getAsString(),icon.getType().name());assertEquals(v.toString(),v.get("iconX").getAsInt(),icon.getX());assertEquals(v.toString(),v.get("iconZ").getAsInt(),icon.getY());assertEquals(v.toString(),v.get("rotation").getAsInt(),icon.getRot()&15);
  }
 }
}

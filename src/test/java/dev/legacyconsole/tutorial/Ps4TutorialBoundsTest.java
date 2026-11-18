package dev.legacyconsole.tutorial;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.world.phys.Vec3;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4TutorialBoundsTest {
 private Vec3 vector(JsonArray a){return new Vec3(a.get(0).getAsDouble(),a.get(1).getAsDouble(),a.get(2).getAsDouble());}
 @Test public void movementMatchesOriginalPs4CorrectionIncludingUnrestrictedHeight(){
  var data=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/ps4/tutorial-bounds-2310-native.json"),StandardCharsets.UTF_8)).getAsJsonObject();
  assertFalse(data.get("restrict_vertical").getAsBoolean());
  for(var row:data.getAsJsonArray("cases")){
   var c=row.getAsJsonObject();var actual=Ps4TutorialBounds.constrain(vector(c.getAsJsonArray("origin")),vector(c.getAsJsonArray("target")),-36,62,20,113);
   assertEquals(c.toString(),vector(c.getAsJsonArray("corrected")),actual);
  }
 }
 @Test public void playerCanJumpAboveTheAuthoredAreaAndSlideAlongItsEdge(){
  assertTrue(Ps4TutorialWorld.permits(-15,74.8,85,-15,76,85));
  assertTrue(Ps4TutorialWorld.permits(-36,100,85,-36,110,86));
  assertEquals(new Vec3(20,80,90),Ps4TutorialBounds.constrain(new Vec3(19,74,85),new Vec3(22,80,90),-36,62,20,113));
 }
}

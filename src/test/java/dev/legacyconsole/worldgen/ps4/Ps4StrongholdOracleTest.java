package dev.legacyconsole.worldgen.ps4;
import com.google.gson.*;import dev.legacyconsole.worldgen.ps4.layers.Ps4Layers;
import java.io.*;import java.nio.charset.StandardCharsets;
import org.junit.Test;import static org.junit.Assert.*;
public class Ps4StrongholdOracleTest {
 @Test public void originalPs4StrongholdLocationsMatchIncludingCompactWorlds()throws Exception{
  JsonArray cases=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/worldgen/ps4-stronghold-vectors.json"),StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("cases");
  for(JsonElement el:cases){JsonObject c=el.getAsJsonObject();long seed=c.get("seed").getAsLong();int width=c.get("width").getAsInt();int[] pos=Ps4Stronghold.position(seed,width,new Ps4Layers(seed,width));
   assertArrayEquals("seed="+seed+" width="+width,new int[]{c.getAsJsonArray("chunk").get(0).getAsInt(),c.getAsJsonArray("chunk").get(1).getAsInt()},pos);
   assertTrue(Math.abs(pos[0])+7<width/32);assertTrue(Math.abs(pos[1])+7<width/32);
  }
 }
}

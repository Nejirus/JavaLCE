package dev.legacyconsole.worldgen.ps4;
import com.google.gson.*;
import java.io.*;import java.nio.charset.StandardCharsets;import java.util.Random;
import org.junit.Test;import static org.junit.Assert.*;
public class Ps4SurfaceOracleTest {
 @Test public void originalPs4SurfaceColumnsAndRandomConsumptionMatch()throws Exception{
  JsonArray cases=new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/worldgen/ps4-default-surface-vectors.json"),StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("cases");
  for(JsonElement element:cases){JsonObject c=element.getAsJsonObject();int[] got=new int[128],want=new int[128];for(int y=0;y<128;y++){got[y]=c.getAsJsonArray("before").get(y).getAsInt();want[y]=c.getAsJsonArray("after").get(y).getAsInt();}
   Random random=new Random(1171);Ps4Surface.column(got,random,c.get("biome").getAsInt(),c.get("noise").getAsDouble());
   assertArrayEquals("biome "+c.get("biome")+" noise "+c.get("noise"),want,got);assertEquals(c.get("random_after").getAsDouble(),random.nextDouble(),0);
  }
 }
}

package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4DirectionLiquidsOracleTest {
 @Test public void originalLiquidProbesRespectEveryCachedHorizontalPermutation()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-direction-liquids.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  JsonArray cases=fixture.getAsJsonArray("cases");assertEquals(768,cases.size());Set<String> planes=new HashSet<>();
  for(JsonElement e:cases){
   JsonObject c=e.getAsJsonObject();Ps4LiquidFlowOracleTest.World w=new Ps4LiquidFlowOracleTest.World(c);w.random=new Random(c.get("seed").getAsLong());int[] p=w.origin;int tile=c.get("kind").getAsString().equals("lava")?10:8;
   w.flow.tick(w,w.random,p[0],p[1],p[2],tile|(c.get("level").getAsInt()<<16));
   String label="original liquid plane="+c.get("plane")+" seed="+c.get("seed")+" kind="+c.get("kind")+" level="+c.get("level")+" primary="+c.get("primary");
   assertEquals(label+" event count",c.get("event_count").getAsInt(),w.events.size());byte[] hash=MessageDigest.getInstance("SHA-256").digest(w.events.toString().getBytes(StandardCharsets.UTF_8));StringBuilder hex=new StringBuilder();for(byte b:hash)hex.append(String.format(Locale.ROOT,"%02x",b&255));assertEquals(label+" ordered effects and probes",c.get("event_sha256").getAsString(),hex.toString());assertEquals(label+" following RNG",c.get("random_after").getAsLong(),w.random.nextLong());planes.add(c.get("plane").toString());
  }
  assertEquals(24,planes.size());
 }
}

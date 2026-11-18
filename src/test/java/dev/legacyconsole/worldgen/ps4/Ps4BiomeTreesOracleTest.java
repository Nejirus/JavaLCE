package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4BiomeTreesOracleTest {
 @Test public void originalSelectorsConstructorsAndSelectedGeometryMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-tree-selection.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  JsonArray cases=fixture.getAsJsonArray("cases"),geometry=fixture.getAsJsonArray("geometry");assertEquals(8256,cases.size());assertEquals(1152,geometry.size());
  Set<Ps4BiomeTrees.Kind> kinds=new HashSet<>();int writes=0,success=0,rejection=0;
  for(String section:List.of("cases","geometry"))for(JsonElement element:fixture.getAsJsonArray(section)){
   JsonObject c=element.getAsJsonObject();Random random=new Random(c.get("seed").getAsLong());
   var biome=Ps4BiomeTrees.Biome.valueOf(c.get("biome").getAsString());var selection=Ps4BiomeTrees.select(biome,c.get("variant").getAsInt(),random);kinds.add(selection.kind());
   JsonObject s=c.getAsJsonObject("selection");String label="original selector "+biome+" variant="+c.get("variant")+" seed="+c.get("seed");
   assertEquals(label+" kind",s.get("kind").getAsString(),selection.kind().name());assertEquals(label+" height",s.get("base_height").getAsInt(),selection.baseHeight());
   assertEquals(label+" extra range",s.get("extra_range").getAsInt(),selection.extraRange());assertEquals(label+" log",s.get("log").getAsInt(),selection.log());assertEquals(label+" leaves",s.get("leaves").getAsInt(),selection.leaves());
   assertEquals(label+" jungle",s.get("jungle").getAsBoolean(),selection.jungle());assertEquals(label+" tall",s.get("tall").getAsBoolean(),selection.tall());assertEquals(label+" updates",s.get("updates").getAsBoolean(),selection.updates());
   if(c.has("position")){
    var world=new World(c);int[] p=world.origin;var tree=selection.bind(world);tree.prepare();boolean result=tree.place(null,random,p[0],p[1],p[2]);if(result)tree.postPlace(null,random,p[0],p[1],p[2]);
    assertEquals(label+" placement",c.get("result").getAsBoolean(),result);Ps4FloraOracleTest.compare(label,c,world);assertEquals(label+" support",c.getAsJsonArray("support_queries"),world.supportQueries);
    writes+=world.writes.size();if(result)success++;else rejection++;
   }
   assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());
  }
  assertEquals("all eleven tree types selected",EnumSet.allOf(Ps4BiomeTrees.Kind.class),kinds);assertTrue("selected placement and rejection coverage",writes>100000&&success>100&&rejection>100);
  for(JsonElement element:fixture.getAsJsonArray("default_state_cases")){JsonObject c=element.getAsJsonObject();int state=c.get("state").getAsInt();assertEquals("original canonical default metadata",state>>>16,c.get("native_data").getAsInt());int tile=c.get("base").getAsInt()&65535;assertEquals("original constructor overwrites all supplied metadata",tile|((tile==18||tile==161?8:0)<<16),state);}
 }
 private static class World extends Ps4TreeFeatureOracleTest.World {
  World(JsonObject c)throws Exception{super(c);}
  @Override int initial(int x,int y,int z){if(profile.equals("high_obstacle")&&x==origin[0]&&y==origin[1]+7&&z==origin[2])return 1;return super.initial(x,y,z);}
 }
}

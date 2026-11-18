package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4CoralDecorationOracleTest {
 @Test public void originalMarkerDecorationScattersFansAndFailedWritesMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-coral-decoration.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  Set<String> covered=new HashSet<>();boolean failedWrite=false,duplicateSide=false;int writes=0;
  assertEquals(6,fixture.getAsJsonArray("direction_facing_inputs").size());for(JsonElement e:fixture.getAsJsonArray("direction_facing_inputs")){JsonObject c=e.getAsJsonObject();assertEquals(c.get("ordinal").getAsInt(),c.get("facing").getAsInt());}
  for(JsonElement e:fixture.getAsJsonArray("cases")){
   JsonObject c=e.getAsJsonObject();World w=new World(c);Ps4Random random=new Ps4Random(c.get("seed").getAsLong());int[] p=w.origin;Ps4CoralCrustDecoration.Candidates candidates=new Ps4CoralCrustDecoration.Candidates();String kind=c.get("kind").getAsString();
   switch(kind){case "scatter"->Ps4CoralCrustDecoration.scatter(w,random,p[0],p[1],p[2],c.get("color").getAsInt(),candidates);case "fan"->Ps4CoralCrustDecoration.fan(w,random,p[0],p[1],p[2]);case "side_fan"->Ps4CoralCrustDecoration.sideFan(w,random,p[0],p[1],p[2],c.get("direction").getAsInt());case "markers"->{List<Ps4CoralCrustDecoration.Marker> markers=new ArrayList<>();for(JsonElement m:c.getAsJsonArray("markers")){JsonArray a=m.getAsJsonArray();markers.add(new Ps4CoralCrustDecoration.Marker(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt()));}Ps4CoralCrustDecoration.decorate(w,random,markers);}default->throw new AssertionError(kind);}
   String label="original crust decoration "+kind+" seed="+c.get("seed")+" pos="+c.get("position")+" profile="+w.profile+" color="+c.get("color")+" direction="+c.get("direction")+" write_mode="+c.get("write_mode")+" placement="+c.get("placement")+" markers="+c.get("markers");
   Ps4CoralsOracleTest.compare(label,c,w);assertEquals(label+" write outcomes",c.getAsJsonArray("write_attempts"),w.attempts);assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());
   JsonArray tops=new JsonArray(),sides=new JsonArray();for(Ps4CoralShapes.Pos q:candidates.tops)tops.add(row(q.x(),q.y(),q.z()));for(Ps4CoralShapes.Side s:candidates.sides){Ps4CoralShapes.Pos q=s.position();sides.add(row(q.x(),q.y(),q.z(),s.direction()));}assertEquals(label+" top candidates",c.getAsJsonArray("top_positions"),tops);assertEquals(label+" side candidates",c.getAsJsonArray("side_positions"),sides);
   for(JsonElement a:w.attempts)if(!a.getAsJsonArray().get(5).getAsBoolean())failedWrite=true;Set<String> unique=new HashSet<>();for(JsonElement a:sides)if(!unique.add(a.toString()))duplicateSide=true;covered.add(kind);writes+=w.writes.size();
  }
  assertEquals(9792,fixture.getAsJsonArray("cases").size());assertEquals(Set.of("scatter","fan","side_fan","markers"),covered);assertEquals(21048,writes);assertTrue("native rejected writes",failedWrite);assertTrue("native duplicate candidates",duplicateSide);
 }
 private static JsonArray row(int... values){JsonArray row=new JsonArray();for(int v:values)row.add(v);return row;}
 private static class World extends Ps4CoralShapesOracleTest.World implements Ps4CoralCrustDecoration.Access {
  final JsonArray attempts=new JsonArray();World(JsonObject c)throws Exception{super(c);}
  public boolean placeCoralBlock(int x,int y,int z,int state,int flags){String mode=input.get("write_mode").getAsString();boolean accepted=mode.equals("all")||mode.equals("checker")&&Math.floorMod(x+y+z,3)!=0;JsonArray a=row(x,y,z,state,flags);a.add(accepted);attempts.add(a);if(accepted)setBlock(x,y,z,state,flags);return accepted;}
 }
}

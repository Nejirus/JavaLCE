package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4CoralShapesOracleTest {
 @Test public void originalCoralShapeWritesCandidateListsQueriesAndRngMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-coral-shapes.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  Set<String> covered=new HashSet<>();int writes=0;
  for(JsonElement e:fixture.getAsJsonArray("cases")){
   JsonObject c=e.getAsJsonObject();String kind=c.get("kind").getAsString();
   World w=new World(c);Random random=new Ps4Random(c.get("seed").getAsLong());Ps4CoralShapes shape=new Ps4CoralShapes(w,random,c.get("state").getAsInt());Ps4CoralShapes.Pos p=new Ps4CoralShapes.Pos(w.origin[0],w.origin[1],w.origin[2]);
   Boolean result=null;
   switch(kind){case "cross"->shape.cross(p,c.get("radius").getAsInt(),c.get("increasing").getAsBoolean(),c.get("chance").getAsFloat());case "diagonal"->shape.diagonal(p,c.get("radius").getAsInt(),c.get("increasing").getAsBoolean(),c.get("chance").getAsFloat());case "patch"->shape.patch(p);case "shape0"->shape.shape0(p);case "shape1"->shape.shape1(p);case "rare"->shape.rare(p);case "branch"->shape.branch(p,c.get("direction").getAsInt(),c.get("short").getAsBoolean());case "dispatch"->shape.dispatch(p);case "full"->result=Ps4CoralFeatures.coral(w,random,p.x(),p.y(),p.z());default->throw new AssertionError(kind);}
   String label="original coral shape "+kind+" seed="+c.get("seed")+" pos="+c.get("position")+" profile="+w.profile+" state="+c.get("state")+" radius="+c.get("radius")+" increasing="+c.get("increasing")+" chance="+c.get("chance")+" placement="+c.get("placement");
   JsonArray tops=new JsonArray(),sides=new JsonArray();for(Ps4CoralShapes.Pos q:shape.tops)tops.add(row(q.x(),q.y(),q.z()));for(Ps4CoralShapes.Side s:shape.sides){Ps4CoralShapes.Pos q=s.position();sides.add(row(q.x(),q.y(),q.z(),s.direction()));}
   try{if(result!=null)assertEquals(label+" result",c.get("result").getAsBoolean(),result.booleanValue());Ps4CoralsOracleTest.compare(label,c,w);assertEquals(label+" top candidates",c.getAsJsonArray("top_positions"),tops);assertEquals(label+" side candidates",c.getAsJsonArray("side_positions"),sides);assertEquals(label+" following RNG",c.get("random_after").getAsLong(),random.nextLong());}
   catch(AssertionError error){JsonObject d=new JsonObject();d.add("case",c);d.add("actual_probes",w.probes);d.add("actual_writes",w.writes);d.add("actual_top_positions",tops);d.add("actual_side_positions",sides);d.add("actual_state_placement_queries",w.statePlacementQueries);Files.writeString(Path.of("build/ps4-coral-shape-mismatch.json"),d.toString());throw error;}
   covered.add(kind);writes+=w.writes.size();
  }
  assertEquals(14952,fixture.getAsJsonArray("cases").size());assertEquals(Set.of("cross","diagonal","patch","branch","shape0","shape1","rare","dispatch","full"),covered);assertEquals(121280,writes);
 }
 private static JsonArray row(int... values){JsonArray row=new JsonArray();for(int v:values)row.add(v);return row;}
 static class World extends Ps4CoralsOracleTest.World {
  final int shapeState;
  World(JsonObject c)throws Exception{super(c);shapeState=c.has("state")?c.get("state").getAsInt():259|(c.get("color").getAsInt()<<16);}
  @Override int initial(int x,int y,int z){
   if(!profile.startsWith("shape_"))return super.initial(x,y,z);
   if(profile.equals("shape_air")||y<0||y>=256)return 0;
   int floor=origin[1]-(profile.equals("shape_unsupported")?10:1);if(profile.equals("shape_terrain"))floor+=Math.floorMod(x*3+z*7,5)-2;
   if(y<=floor)return 1;
   if(Set.of("shape_matching","shape_foreign","shape_dead").contains(profile)&&Math.floorMod(x+3*y+7*z,5)==0){int color=shapeState>>>16&7;if(profile.equals("shape_foreign"))color=(color+1)%5;return 259|((color|(profile.equals("shape_dead")?8:0))<<16);}
   if(profile.equals("shape_obstacle")&&Math.floorMod(3*x+y+5*z,7)==0)return 1;
   return y<64?9:0;
  }
 }
}

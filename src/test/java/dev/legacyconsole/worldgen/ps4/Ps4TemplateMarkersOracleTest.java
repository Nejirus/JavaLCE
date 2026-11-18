package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4TemplateMarkersOracleTest {
 @Test public void originalTemplateMarkerTransformsFilteringDuplicatesAndHashMapOrderMatch()throws Exception{
  JsonObject fixture;
  try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-template-markers.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  Map<Integer,JsonObject> templates=new HashMap<>();for(JsonElement e:fixture.getAsJsonArray("templates")){JsonObject t=e.getAsJsonObject();templates.put(t.get("id").getAsInt(),t);}
  Set<Long> bucketCounts=new HashSet<>();int originalAssets=0,markers=0;for(JsonObject t:templates.values())if(t.get("path").getAsString().startsWith("coralcrust/"))originalAssets++;
  for(JsonElement e:fixture.getAsJsonArray("cases")){
   JsonObject c=e.getAsJsonObject(),t=templates.get(c.get("template").getAsInt());int rotation=c.get("rotation").getAsInt();JsonArray origin=c.getAsJsonArray("origin");int ox=origin.get(0).getAsInt(),oy=origin.get(1).getAsInt(),oz=origin.get(2).getAsInt();List<Ps4TemplateMarkers.Marker> input=new ArrayList<>();
   for(JsonElement b:t.getAsJsonArray("blocks")){
    JsonArray row=b.getAsJsonArray();if(row.get(3).getAsInt()!=255||row.get(4).isJsonNull())continue;
    int x=row.get(0).getAsInt(),y=row.get(1).getAsInt(),z=row.get(2).getAsInt();int rx=switch(rotation){case 1->-z;case 2->-x;case 3->z;default->x;};int rz=switch(rotation){case 1->x;case 2->-z;case 3->-x;default->z;};input.add(new Ps4TemplateMarkers.Marker(ox+rx,oy+y,oz+rz,row.get(4).getAsString()));
   }
   JsonArray actual=new JsonArray();for(Ps4TemplateMarkers.Marker m:Ps4TemplateMarkers.order(input)){JsonArray row=new JsonArray();row.add(m.x());row.add(m.y());row.add(m.z());row.add(m.metadata());actual.add(row);}
   String label="original marker map "+t.get("path")+" rotation="+rotation+" origin="+origin;assertEquals(label,c.getAsJsonArray("markers"),actual);bucketCounts.add(c.get("bucket_count").getAsLong());markers+=actual.size();
  }
  assertEquals(11,originalAssets);assertEquals(416,fixture.getAsJsonArray("cases").size());assertEquals(Set.of(8L,64L,512L,1024L),bucketCounts);assertTrue(markers>10000);
 }
}

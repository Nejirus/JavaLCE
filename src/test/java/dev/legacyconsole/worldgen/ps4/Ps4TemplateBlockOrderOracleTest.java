package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4TemplateBlockOrderOracleTest {
 @Test public void originalSortEqualColumnsMedianPartitionsInsertionAndHeapMatch()throws Exception{
  JsonObject fixture;try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-template-order.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  int assets=0,heapCases=0;
  for(JsonElement e:fixture.getAsJsonArray("cases")){
   JsonObject c=e.getAsJsonObject();JsonArray positions=c.getAsJsonArray("positions");List<Integer> indices=new ArrayList<>();for(int i=0;i<positions.size();i++)indices.add(i);
   List<Integer> actual=Ps4TemplateBlockOrder.order(indices,index->{JsonArray p=positions.get(index).getAsJsonArray();return Ps4TemplateBlockOrder.key(p.get(0).getAsInt(),p.get(2).getAsInt());},c.get("ideal").getAsInt());
   JsonArray order=new JsonArray();for(int index:actual)order.add(index);assertEquals("original template sort "+c.get("name")+" ideal="+c.get("ideal"),c.getAsJsonArray("order"),order);
   if(c.get("name").getAsString().startsWith("coralcrust/")||c.get("name").getAsString().startsWith("fossils/"))assets++;if(positions.size()>32&&c.get("ideal").getAsInt()==0)heapCases++;
  }
  assertEquals(27,assets);assertTrue(heapCases>10);
 }
}

package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4ScalarStateDecodersOracleTest {
 private JsonObject fixture()throws Exception{try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-scalar-state-decoders.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){return new JsonParser().parse(reader).getAsJsonObject();}}
 @Test public void originalClassFromDataMatchesAllCanonicalDefaultsAndEveryMetadataNibble()throws Exception{
  Ps4ScalarProperties context=new Ps4ScalarProperties();int classes=0,defaults=0,queries=0;Set<Integer> decoders=new HashSet<>();
  for(JsonElement element:fixture().getAsJsonArray("definitions")){
   JsonObject c=element.getAsJsonObject();var schema=Ps4ScalarStateSchemas.get(Long.decode(c.get("vtable").getAsString()));Object owner=new Object();Ps4StateDefinition<Object,Ps4Property<?>,Object> definition=schema.create(owner,context);String label="native decoder "+c.get("decoder")+" class "+c.get("vtable");assertEquals(label+" source virtual",Integer.decode(c.get("decoder").getAsString()).intValue(),schema.decoder());assertTrue(label+" original decoder leaves default field unchanged",c.get("default_field_unchanged").getAsBoolean());JsonArray inputs=c.getAsJsonArray("queries");
   for(JsonElement row:c.getAsJsonArray("outputs")){
    JsonArray expected=row.getAsJsonArray();int beforeCode=expected.get(0).getAsInt();var before=definition.state(beforeCode);JsonArray afterCodes=expected.get(1).getAsJsonArray(),same=expected.get(2).getAsJsonArray();
    for(int i=0;i<inputs.size();i++){int data=inputs.get(i).getAsInt();var after=schema.decode(data,before,context);String query=label+" default "+beforeCode+" metadata "+data;assertSame(query+" canonical result",definition.state(afterCodes.get(i).getAsInt()),after);assertEquals(query+" retained pointer identity",same.get(i).getAsBoolean(),after==before);assertEquals(query+" unchanged input default",beforeCode,before.bits());queries++;}defaults++;
   }
   classes++;decoders.add(schema.decoder());
  }
  assertEquals(86,classes);assertEquals(1303,defaults);assertEquals(20848,queries);assertEquals(40,decoders.size());
 }
}

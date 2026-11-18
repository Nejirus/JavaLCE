package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4IntegerPropertyOracleTest {
 private JsonObject fixture()throws Exception{try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-integer-properties.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){return new JsonParser().parse(reader).getAsJsonObject();}}
 @Test public void originalIntegerConstructorUtf16NamesRangesVectorsAndIndexZeroFallbackMatch()throws Exception{
  int cases=0;
  for(JsonElement element:fixture().getAsJsonArray("properties")){
   JsonObject c=element.getAsJsonObject();Ps4IntegerProperty property=new Ps4IntegerProperty(c.get("name").getAsString(),c.get("minimum").getAsInt(),c.get("maximum").getAsInt());String label="original integer property "+c.get("name")+" "+c.get("minimum")+".."+c.get("maximum");assertEquals(label+" stored UTF16 name",c.get("stored_name").getAsString(),property.name());assertEquals(label+" count",c.get("count").getAsInt(),property.count());JsonArray possible=new JsonArray(),indexed=new JsonArray();for(int value:property.possibleValues())possible.add(value);for(int i=0;i<property.count();i++)indexed.add(property.valueByIndex(i));assertEquals(label+" possible values",c.getAsJsonArray("possible_values"),possible);assertEquals(label+" indexed values",c.getAsJsonArray("indexed_values"),indexed);
   JsonArray queries=c.getAsJsonArray("queries"),indices=c.getAsJsonArray("indices");for(int i=0;i<queries.size();i++)assertEquals(label+" index of "+queries.get(i),indices.get(i).getAsInt(),property.indexOf(queries.get(i).getAsInt()));cases++;
  }
  assertEquals(120,cases);
 }
 @Test public void realNativeIntegerPropertiesFeedCanonicalDefinitionGetterAndSetterPipeline()throws Exception{
  int cases=0,getters=0,setters=0;
  for(JsonElement element:fixture().getAsJsonArray("definitions")){
   JsonObject c=element.getAsJsonObject();List<Ps4IntegerProperty> properties=new ArrayList<>();for(JsonElement p:c.getAsJsonArray("properties")){JsonObject spec=p.getAsJsonObject();properties.add(new Ps4IntegerProperty(spec.get("name").getAsString(),spec.get("minimum").getAsInt(),spec.get("maximum").getAsInt()));}
   Object owner=new Object();Ps4StateDefinition<Object,Ps4IntegerProperty,Integer> definition=new Ps4StateDefinition<>(owner,properties,Ps4IntegerProperty.DEFINITION_VALUES);String label="real native integer definition "+c.get("properties");assertEquals(label,c.get("capacity").getAsInt(),definition.capacity());for(int i=0;i<properties.size();i++){assertEquals(label+" count",c.getAsJsonArray("counts").get(i).getAsInt(),properties.get(i).count());assertEquals(label+" shift",c.getAsJsonArray("shifts").get(i).getAsInt(),definition.shift(i));assertEquals(label+" mask",c.getAsJsonArray("masks").get(i).getAsInt(),definition.mask(i));}
   JsonArray codes=new JsonArray();boolean owners=true;for(var state:definition.states())if(state==null)codes.add(JsonNull.INSTANCE);else{codes.add(state.bits());owners&=state.owner()==owner;}assertEquals(label+" actual sparse canonical states",c.getAsJsonArray("state_codes"),codes);assertEquals(label+" owner links",c.get("owner_matches").getAsBoolean(),owners);Ps4IntegerProperty missing=new Ps4IntegerProperty("missing",0,1);
   for(JsonElement query:c.getAsJsonArray("getters")){JsonArray q=query.getAsJsonArray();int code=q.get(0).getAsInt(),property=q.get(1).getAsInt();Integer actual=definition.state(code).value(property==properties.size()?missing:properties.get(property));if(q.get(2).isJsonNull())assertNull(label+" absent property",actual);else assertEquals(label+" actual typed value",Integer.valueOf(q.get(2).getAsInt()),actual);getters++;}
   for(JsonElement query:c.getAsJsonArray("setters")){JsonArray q=query.getAsJsonArray();var before=definition.state(q.get(0).getAsInt());var after=before.with(properties.get(q.get(1).getAsInt()),q.get(2).getAsInt());assertEquals(label+" original property conversion/mutation",q.get(3).getAsInt(),after==null?-1:after.bits());assertEquals(label+" canonical identity",q.get(4).getAsBoolean(),after==before);setters++;}cases++;
  }
  assertEquals(9,cases);assertEquals(347,getters);assertEquals(2916,setters);
 }
}

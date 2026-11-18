package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4BooleanPropertyOracleTest {
 private JsonObject fixture()throws Exception{try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-boolean-properties.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){return new JsonParser().parse(reader).getAsJsonObject();}}
 private Object scalar(JsonElement value){if(value.isJsonNull())return null;JsonPrimitive p=value.getAsJsonPrimitive();if(p.isBoolean())return p.getAsBoolean();return p.getAsInt();}
 private JsonElement json(Object value){if(value==null)return JsonNull.INSTANCE;if(value instanceof Boolean)return new JsonPrimitive((Boolean)value);return new JsonPrimitive((Integer)value);}
 @Test public void originalBooleanConstructorHasTrueFirstAndTypedIndexZeroFallback()throws Exception{
  int cases=0;
  for(JsonElement element:fixture().getAsJsonArray("properties")){
   JsonObject c=element.getAsJsonObject();Ps4BooleanProperty property=new Ps4BooleanProperty(c.get("name").getAsString());String label="original boolean property "+c.get("name");assertEquals(label+" UTF16 name",c.get("stored_name").getAsString(),property.name());assertEquals(label+" count",c.get("count").getAsInt(),property.count());JsonArray possible=new JsonArray(),indexed=new JsonArray();for(Boolean value:property.possibleValues())possible.add(value);for(int i=0;i<property.count();i++)indexed.add(property.valueByIndex(i));assertEquals(label+" possible values",c.getAsJsonArray("possible_values"),possible);assertEquals(label+" indexed values",c.getAsJsonArray("indexed_values"),indexed);JsonArray queries=c.getAsJsonArray("queries"),indices=c.getAsJsonArray("indices");for(int i=0;i<queries.size();i++)assertEquals(label+" typed index "+queries.get(i),indices.get(i).getAsInt(),property.indexOf(scalar(queries.get(i))));cases++;
  }
  assertEquals(8,cases);
 }
 @Test public void realNativeMixedBooleanIntegerCanonicalStatesAndMutationsMatch()throws Exception{
  int cases=0,getters=0,setters=0;
  for(JsonElement element:fixture().getAsJsonArray("definitions")){
   JsonObject c=element.getAsJsonObject();List<Ps4Property<?>> properties=new ArrayList<>();for(JsonElement item:c.getAsJsonArray("properties")){JsonObject spec=item.getAsJsonObject();String name=spec.get("name").getAsString();properties.add(spec.get("type").getAsString().equals("boolean")?new Ps4BooleanProperty(name):new Ps4IntegerProperty(name,spec.get("minimum").getAsInt(),spec.get("maximum").getAsInt()));}
   Object owner=new Object();Ps4StateDefinition<Object,Ps4Property<?>,Object> definition=new Ps4StateDefinition<>(owner,properties,Ps4Property.DEFINITION_VALUES);String label="native mixed definition "+c.get("properties");assertEquals(label+" capacity",c.get("capacity").getAsInt(),definition.capacity());for(int i=0;i<properties.size();i++){assertEquals(label+" count",c.getAsJsonArray("counts").get(i).getAsInt(),properties.get(i).count());assertEquals(label+" shift",c.getAsJsonArray("shifts").get(i).getAsInt(),definition.shift(i));assertEquals(label+" mask",c.getAsJsonArray("masks").get(i).getAsInt(),definition.mask(i));}
   JsonArray codes=new JsonArray();boolean owners=true;for(var state:definition.states())if(state==null)codes.add(JsonNull.INSTANCE);else{codes.add(state.bits());owners&=state.owner()==owner;}assertEquals(label+" sparse canonical states",c.getAsJsonArray("state_codes"),codes);assertEquals(label+" owners",c.get("owner_matches").getAsBoolean(),owners);Ps4Property<?> missing=new Ps4BooleanProperty("missing");
   for(JsonElement query:c.getAsJsonArray("getters")){JsonArray q=query.getAsJsonArray();int property=q.get(1).getAsInt();Object actual=definition.state(q.get(0).getAsInt()).value(property==properties.size()?missing:properties.get(property));assertEquals(label+" typed getter",q.get(2),json(actual));getters++;}
   for(JsonElement query:c.getAsJsonArray("setters")){JsonArray q=query.getAsJsonArray();var before=definition.state(q.get(0).getAsInt());var after=before.with(properties.get(q.get(1).getAsInt()),scalar(q.get(2)));assertEquals(label+" typed mutation",q.get(3).getAsInt(),after==null?-1:after.bits());assertEquals(label+" canonical identity",q.get(4).getAsBoolean(),after==before);setters++;}cases++;
  }
  assertEquals(13,cases);assertEquals(519,getters);assertEquals(4810,setters);
 }
}

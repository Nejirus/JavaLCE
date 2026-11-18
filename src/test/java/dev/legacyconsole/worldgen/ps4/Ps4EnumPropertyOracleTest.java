package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4EnumPropertyOracleTest {
 private JsonObject fixture()throws Exception{try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-enum-properties.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){return new JsonParser().parse(reader).getAsJsonObject();}}
 private static final class Values {
  final Map<Integer,Ps4EnumProperty.Value> values=new HashMap<>();final Map<Ps4EnumProperty.Value,Integer> identities=new IdentityHashMap<>();
  Values(JsonObject fixture){for(JsonElement element:fixture.getAsJsonArray("enum_values")){JsonObject spec=element.getAsJsonObject();int identity=spec.get("identity").getAsInt();var value=new Ps4EnumProperty.Value(spec.get("id").getAsInt(),spec.get("name").getAsString());values.put(identity,value);identities.put(value,identity);}}
  List<Ps4EnumProperty.Value> ordered(JsonArray ids){List<Ps4EnumProperty.Value> result=new ArrayList<>();for(JsonElement id:ids)result.add(values.get(id.getAsInt()));return result;}
  Object scalar(JsonElement element){if(element.isJsonNull())return null;if(element.isJsonObject())return values.get(element.getAsJsonObject().get("enum").getAsInt());JsonPrimitive p=element.getAsJsonPrimitive();if(p.isBoolean())return p.getAsBoolean();return p.getAsInt();}
  JsonElement json(Object value){if(value==null)return JsonNull.INSTANCE;if(value instanceof Ps4EnumProperty.Value){JsonObject result=new JsonObject();result.addProperty("enum",identities.get(value));return result;}if(value instanceof Boolean)return new JsonPrimitive((Boolean)value);return new JsonPrimitive((Integer)value);}
 }
 @Test public void originalPointerHashSetDuplicatesBucketGroupsAndRehashOrdersMatch()throws Exception{
  int cases=0,snapshots=0;
  for(JsonElement element:fixture().getAsJsonArray("pointer_sets")){
   JsonObject c=element.getAsJsonObject();Ps4PointerSet set=new Ps4PointerSet();JsonArray keys=c.getAsJsonArray("keys"),expected=c.getAsJsonArray("snapshots");int next=0;
   for(int index=0;index<keys.size();index++){
    boolean inserted=set.add(Long.parseUnsignedLong(keys.get(index).getAsString(),16));
    if(next<expected.size()&&expected.get(next).getAsJsonObject().get("prefix").getAsInt()==index+1){JsonObject e=expected.get(next++).getAsJsonObject();String label="original pointer set case "+cases+" prefix "+(index+1);assertEquals(label+" inserted",e.get("inserted").getAsBoolean(),inserted);assertEquals(label+" size",e.get("size").getAsInt(),set.size());assertEquals(label+" buckets",e.get("buckets").getAsInt(),set.bucketCount());JsonArray ordered=new JsonArray();for(long value:set.values())ordered.add(String.format(Locale.ROOT,"%016x",value));assertEquals(label+" actual native order",e.getAsJsonArray("order"),ordered);snapshots++;}
   }
   assertEquals(expected.size(),next);cases++;
  }
  assertEquals(73,cases);assertEquals(1108,snapshots);
 }
 @Test public void originalEnumConstructorKeepsOrderedValuesAndPointerIdentity()throws Exception{
  JsonObject f=fixture();Values values=new Values(f);int cases=0;
  for(JsonElement element:f.getAsJsonArray("properties")){
   JsonObject c=element.getAsJsonObject();Ps4EnumProperty property=new Ps4EnumProperty(c.get("name").getAsString(),values.ordered(c.getAsJsonArray("identities")));String label="original enum property "+c.get("name")+" "+c.get("identities");assertEquals(label+" name",c.get("stored_name").getAsString(),property.name());assertEquals(label+" count",c.get("count").getAsInt(),property.count());JsonArray possible=new JsonArray(),indexed=new JsonArray();for(var value:property.possibleValues())possible.add(values.json(value));for(int i=0;i<property.count();i++)indexed.add(values.json(property.valueByIndex(i)));assertEquals(label+" ordered possible values",c.getAsJsonArray("possible_values"),possible);assertEquals(label+" indexed values",c.getAsJsonArray("indexed_values"),indexed);JsonArray queries=c.getAsJsonArray("queries"),indices=c.getAsJsonArray("indices");for(int i=0;i<queries.size();i++)assertEquals(label+" identity index "+queries.get(i),indices.get(i).getAsInt(),property.indexOf(values.scalar(queries.get(i))));cases++;
  }
  assertEquals(128,cases);
 }
 @Test public void realNativeEnumBooleanIntegerCanonicalDefinitionsAndMutationsMatch()throws Exception{
  JsonObject f=fixture();Values values=new Values(f);int cases=0,getters=0,setters=0;
  for(JsonElement element:f.getAsJsonArray("definitions")){
   JsonObject c=element.getAsJsonObject();List<Ps4Property<?>> properties=new ArrayList<>();for(JsonElement item:c.getAsJsonArray("properties")){JsonObject spec=item.getAsJsonObject();String name=spec.get("name").getAsString();switch(spec.get("type").getAsString()){case "enum":properties.add(new Ps4EnumProperty(name,values.ordered(spec.getAsJsonArray("identities"))));break;case "boolean":properties.add(new Ps4BooleanProperty(name));break;default:properties.add(new Ps4IntegerProperty(name,spec.get("minimum").getAsInt(),spec.get("maximum").getAsInt()));}}
   Object owner=new Object();Ps4StateDefinition<Object,Ps4Property<?>,Object> definition=new Ps4StateDefinition<>(owner,properties,Ps4Property.DEFINITION_VALUES);String label="native enum definition "+c.get("properties");assertEquals(label+" capacity",c.get("capacity").getAsInt(),definition.capacity());for(int i=0;i<properties.size();i++){assertEquals(label+" count",c.getAsJsonArray("counts").get(i).getAsInt(),properties.get(i).count());assertEquals(label+" shift",c.getAsJsonArray("shifts").get(i).getAsInt(),definition.shift(i));assertEquals(label+" mask",c.getAsJsonArray("masks").get(i).getAsInt(),definition.mask(i));}
   JsonArray codes=new JsonArray();boolean owners=true;for(var state:definition.states())if(state==null)codes.add(JsonNull.INSTANCE);else{codes.add(state.bits());owners&=state.owner()==owner;}assertEquals(label+" canonical codes",c.getAsJsonArray("state_codes"),codes);assertEquals(label+" owners",c.get("owner_matches").getAsBoolean(),owners);Ps4Property<?> missing=new Ps4BooleanProperty("missing");
   for(JsonElement query:c.getAsJsonArray("getters")){JsonArray q=query.getAsJsonArray();int property=q.get(1).getAsInt();Object actual=definition.state(q.get(0).getAsInt()).value(property==properties.size()?missing:properties.get(property));assertEquals(label+" typed getter",q.get(2),values.json(actual));getters++;}
   for(JsonElement query:c.getAsJsonArray("setters")){JsonArray q=query.getAsJsonArray();var before=definition.state(q.get(0).getAsInt());var after=before.with(properties.get(q.get(1).getAsInt()),values.scalar(q.get(2)));assertEquals(label+" typed mutation",q.get(3).getAsInt(),after==null?-1:after.bits());assertEquals(label+" canonical identity",q.get(4).getAsBoolean(),after==before);setters++;}cases++;
  }
  assertEquals(8,cases);assertEquals(221,getters);assertEquals(2015,setters);
 }
}

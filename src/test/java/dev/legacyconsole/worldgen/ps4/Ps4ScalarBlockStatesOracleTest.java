package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4ScalarBlockStatesOracleTest {
 private JsonObject fixture()throws Exception{try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-scalar-block-states.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){return new JsonParser().parse(reader).getAsJsonObject();}}
 private static long address(JsonElement value){return Long.decode(value.getAsString());}
 private static JsonElement json(Object value){if(value==null)return JsonNull.INSTANCE;if(value instanceof Boolean)return new JsonPrimitive((Boolean)value);return new JsonPrimitive((Integer)value);}
 @Test public void actualInitializerConstantsTypesAndSharedGlobalPropertyIdentityMatch()throws Exception{
  Ps4ScalarProperties context=new Ps4ScalarProperties();int cases=0;Set<Ps4Property<?>> identities=Collections.newSetFromMap(new IdentityHashMap<>());
  for(JsonElement element:fixture().getAsJsonArray("properties")){
   JsonObject c=element.getAsJsonObject();Ps4Property<?> property=context.get(address(c.get("alias")));String label="actual scalar initializer "+c.get("alias");assertSame(label+" copied global identity",context.get(address(c.get("identity"))),property);assertEquals(label+" name",c.get("name").getAsString(),property.name());assertEquals(label+" type",c.get("type").getAsString().equals("boolean"),property instanceof Ps4BooleanProperty);assertEquals(label+" count",c.get("count").getAsInt(),property.count());JsonArray possible=new JsonArray();for(Object value:property.possibleValues())possible.add(json(value));assertEquals(label+" original vector",c.getAsJsonArray("values"),possible);identities.add(property);cases++;
  }
  assertEquals(136,cases);assertEquals(136,context.aliases().size());assertEquals(131,identities.size());
 }
 @Test public void originalConcreteClassDefinitionsAndMetadataVirtualsMatchEveryCanonicalState()throws Exception{
  Ps4ScalarProperties context=new Ps4ScalarProperties();int classes=0,states=0,encodings=0;Set<Integer> encoders=new HashSet<>();
  for(JsonElement element:fixture().getAsJsonArray("definitions")){
   JsonObject c=element.getAsJsonObject();var schema=Ps4ScalarStateSchemas.get(address(c.get("vtable")));String label="native scalar class "+c.get("class");assertEquals(label+" original definition virtual",(int)address(c.get("method")),schema.createMethod());assertEquals(label+" original metadata virtual",(int)address(c.get("get_data")),schema.encoder());assertEquals(label+" crop property virtual",c.get("virtual_property").isJsonNull()?0:address(c.get("virtual_property")),schema.virtualProperty());JsonArray aliases=new JsonArray();for(long alias:schema.properties())aliases.add("0x"+Long.toHexString(alias));assertEquals(label+" original ordered properties",c.getAsJsonArray("properties"),aliases);
   Object owner=new Object();Ps4StateDefinition<Object,Ps4Property<?>,Object> definition=schema.create(owner,context);assertEquals(label+" capacity",c.get("capacity").getAsInt(),definition.capacity());for(int i=0;i<schema.properties().size();i++){assertEquals(label+" shift",c.getAsJsonArray("shifts").get(i).getAsInt(),definition.shift(i));assertEquals(label+" mask",c.getAsJsonArray("masks").get(i).getAsInt(),definition.mask(i));}
   JsonArray codes=new JsonArray();boolean owners=true;for(var state:definition.states())if(state==null)codes.add(JsonNull.INSTANCE);else{codes.add(state.bits());owners&=state.owner()==owner;}assertEquals(label+" canonical sparse table",c.getAsJsonArray("state_codes"),codes);assertEquals(label+" original owner links",c.get("owner_matches").getAsBoolean(),owners);assertEquals(label+" initial canonical default",c.get("default_code").getAsInt(),definition.defaultState().bits());
   for(JsonElement row:c.getAsJsonArray("values")){JsonArray expected=row.getAsJsonArray();var state=definition.state(expected.get(0).getAsInt());JsonArray values=new JsonArray();for(long alias:schema.properties())values.add(json(state.value(context.get(alias))));assertEquals(label+" original typed property reads",expected.get(1).getAsJsonArray(),values);states++;}
   for(JsonElement row:c.getAsJsonArray("data")){JsonArray expected=row.getAsJsonArray();assertEquals(label+" original getData",expected.get(1).getAsInt(),schema.encode(definition.state(expected.get(0).getAsInt()),context));encodings++;}
   encoders.add(schema.encoder());classes++;
  }
  assertEquals(86,classes);assertEquals(86,Ps4ScalarStateSchemas.all().size());assertEquals(1303,states);assertEquals(1303,encodings);assertEquals(46,encoders.size());
 }
}

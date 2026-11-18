package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4ScalarConstructorDefaultsOracleTest {
 private JsonObject fixture()throws Exception{try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-scalar-constructor-defaults.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){return new JsonParser().parse(reader).getAsJsonObject();}}
 private static Object scalar(JsonElement value){JsonPrimitive p=value.getAsJsonPrimitive();if(p.isBoolean())return p.getAsBoolean();return p.getAsInt();}
 @Test public void originalConstructorDefaultProgramsAndFinalCanonicalValuesMatch()throws Exception{
  Ps4ScalarProperties context=new Ps4ScalarProperties();Set<Long> classes=new HashSet<>();int constructors=0,operations=0;
  for(JsonElement element:fixture().getAsJsonArray("cases")){
   JsonObject c=element.getAsJsonObject();var schema=Ps4ScalarStateSchemas.get(Long.decode(c.get("vtable").getAsString()));Object owner=new Object();Ps4StateDefinition<Object,Ps4Property<?>,Object> definition=schema.create(owner,context);var state=definition.defaultState();String label="native constructor "+c.get("name")+" "+c.get("constructor");assertEquals(label+" initial canonical state",c.get("initial").getAsInt(),state.bits());assertTrue(label+" observation agrees with unobserved invocation",c.get("observer_matches_unobserved").getAsBoolean());List<Ps4ScalarStateDefaults.Assignment> program=Ps4ScalarStateDefaults.assignments(schema);int step=0,stored=state.bits();
   for(JsonElement row:c.getAsJsonArray("operations")){
    JsonObject operation=row.getAsJsonObject();if(operation.get("kind").getAsString().equals("with")){var assignment=program.get(step++);assertEquals(label+" original property alias",Long.decode(operation.get("alias").getAsString()).longValue(),assignment.alias());assertEquals(label+" original typed request",scalar(operation.get("value")),assignment.value());assertEquals(label+" original call return address",Long.decode(operation.get("return_pc").getAsString()).longValue(),assignment.returnPc());assertEquals(label+" original input state",operation.get("before").getAsInt(),state.bits());state=state.with(context.get(assignment.alias()),assignment.value());assertEquals(label+" original canonical mutation",operation.get("after").getAsInt(),state.bits());}else{assertEquals(label+" original default before store",operation.get("before").getAsInt(),stored);stored=state.bits();assertEquals(label+" original default after store",operation.get("after").getAsInt(),stored);}operations++;
   }
   assertEquals(label+" complete assignment program",program.size(),step);var actual=schema.constructorDefault(definition,context);assertSame(label+" final canonical identity",state,actual);assertEquals(label+" original final default",c.get("default").getAsInt(),actual.bits());assertEquals(label+" original final metadata",c.get("metadata").getAsInt(),schema.encode(actual,context));classes.add(schema.vtable());constructors++;
  }
  assertEquals(74,constructors);assertEquals(47,classes.size());assertEquals(201,operations);
 }
}

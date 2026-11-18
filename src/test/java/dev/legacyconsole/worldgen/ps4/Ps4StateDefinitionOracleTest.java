package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4StateDefinitionOracleTest {
 @Test public void originalCompleteDefinitionConstructorAndCanonicalPropertyOperationsMatch()throws Exception{
  JsonObject fixture;try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-state-definitions.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  int cases=0,getters=0,setters=0;Set<String> hashContexts=new HashSet<>();
  for(JsonElement element:fixture.getAsJsonArray("cases")){
   JsonObject c=element.getAsJsonObject();List<Property> properties=new ArrayList<>();for(JsonElement p:c.getAsJsonArray("properties")){JsonObject property=p.getAsJsonObject();List<Integer> indices=new ArrayList<>();for(JsonElement index:property.getAsJsonArray("indices"))indices.add(index.getAsInt());properties.add(new Property(properties.size(),property.get("count").getAsInt(),indices));}
   Object owner=new Object();Values values=new Values();Ps4StateDefinition<Object,Property,Integer> definition=new Ps4StateDefinition<>(owner,properties,values);String label="original definition case="+cases+" props="+c.get("properties")+" hash="+c.get("hash_mode");assertEquals(label+" capacity",c.get("capacity").getAsInt(),definition.capacity());
   for(int i=0;i<properties.size();i++){assertEquals(label+" shift "+i,c.getAsJsonArray("shifts").get(i).getAsInt(),definition.shift(i));assertEquals(label+" mask "+i,c.getAsJsonArray("masks").get(i).getAsInt(),definition.mask(i));}
   JsonArray codes=new JsonArray();boolean ownerMatches=true;for(var state:definition.states())if(state==null)codes.add(JsonNull.INSTANCE);else{codes.add(state.bits());ownerMatches&=state.owner()==owner;}assertEquals(label+" sparse states",c.getAsJsonArray("state_codes"),codes);assertEquals(label+" owner",c.get("owner_matches").getAsBoolean(),ownerMatches);assertEquals(label+" default",c.get("default_code").getAsInt(),definition.defaultState()==null?-1:definition.defaultState().bits());
   Property missing=new Property(properties.size(),1,List.of(0));values.record=true;
   for(JsonElement query:c.getAsJsonArray("getters")){
    JsonArray q=query.getAsJsonArray();int code=q.get(0).getAsInt(),property=q.get(1).getAsInt();Integer actual=definition.state(code).value(property==properties.size()?missing:properties.get(property));if(q.get(2).isJsonNull())assertNull(label+" missing property",actual);else assertEquals(label+" get code="+code+" property="+property,Integer.valueOf(q.get(2).getAsInt()),actual);getters++;
   }
   for(JsonElement query:c.getAsJsonArray("setters")){
    JsonArray q=query.getAsJsonArray();int code=q.get(0).getAsInt(),property=q.get(1).getAsInt(),value=q.get(2).getAsInt();var before=definition.state(code);var after=before.with(properties.get(property),value);assertEquals(label+" mutation code="+code+" property="+property+" value="+value,q.get(3).getAsInt(),after==null?-1:after.bits());assertEquals(label+" canonical identity",q.get(4).getAsBoolean(),after==before);setters++;
   }
   assertEquals(label+" query/equality order",c.get("events_sha256").getAsString(),hex(values.events.digest()));hashContexts.add(c.get("hash_mode").getAsString());cases++;
  }
  assertEquals(171,cases);assertEquals(8709,getters);assertEquals(61188,setters);assertEquals(Set.of("zero","ordinal","salted"),hashContexts);
 }
 private record Property(int id,int count,List<Integer> indices){}
 private static class Values implements Ps4StateDefinition.Properties<Property,Integer> {
  final MessageDigest events;boolean record;
  Values()throws Exception{events=MessageDigest.getInstance("SHA-256");}
  private void event(int a,int b,int c){if(record)for(int value:new int[]{a,b,c})for(int i=0;i<4;i++)events.update((byte)(value>>>(i*8)));}
  public int count(Property property){return property.count;}
  public Iterable<Integer> possibleValues(Property property){return property.indices;}
  public Integer valueByIndex(Property property,int index){event(0,property.id,index);return index;}
  public int indexOf(Property property,Integer value){event(1,property.id,value);return value;}
  public boolean same(Integer first,Integer second){event(2,first,second);return first.intValue()==second.intValue();}
 }
 private static String hex(byte[] bytes){char[] out=new char[bytes.length*2],digits="0123456789abcdef".toCharArray();for(int i=0;i<bytes.length;i++){out[i*2]=digits[(bytes[i]>>>4)&15];out[i*2+1]=digits[bytes[i]&15];}return new String(out);}
}

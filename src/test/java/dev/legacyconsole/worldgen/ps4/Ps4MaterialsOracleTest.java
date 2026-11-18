package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4MaterialsOracleTest {
 @Test public void originalMaterialInitializersVirtualOverridesFlagsAndSetterOrdersMatch()throws Exception{
  JsonObject fixture;try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-materials.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  Map<Long,Ps4Material> initialized=Ps4Materials.initialize();Map<Ps4Material.Type,Ps4Material.Color> colors=new EnumMap<>(Ps4Material.Type.class);assertEquals(40,initialized.size());
  for(JsonElement e:fixture.getAsJsonArray("materials")){
   JsonObject c=e.getAsJsonObject();Ps4Material material=initialized.get(Long.decode(c.get("alias").getAsString()));assertNotNull("original material identity",material);assertEquals(type(c.get("class").getAsString()),material.type());compare("original material "+c.get("alias"),c,material);colors.putIfAbsent(material.type(),material.color());
  }
  int steps=0;Set<Ps4Material.Type> types=new HashSet<>();Set<String> orders=new HashSet<>();
  for(JsonElement e:fixture.getAsJsonArray("cases")){
   JsonObject c=e.getAsJsonObject();Ps4Material.Type type=type(c.get("class").getAsString());Ps4Material material=new Ps4Material(type,colors.get(type),c.get("flags").getAsInt(),c.get("push_reaction").getAsInt());String label="original material "+type+" flags="+c.get("flags")+" push="+c.get("push_reaction");compare(label,c.getAsJsonObject("initial"),material);List<String> order=new ArrayList<>();
   for(JsonElement s:c.getAsJsonArray("steps")){
    JsonObject step=s.getAsJsonObject();String setter=step.get("setter").getAsString();Ps4Material returned=switch(setter){case "0x38"->material.setTranslucent();case "0x40"->material.requireTool();case "0x48"->material.setFlammable();case "0x58"->material.setReplaceable();default->throw new AssertionError(setter);};assertEquals(label+" original setter returns this",step.get("returned_this").getAsBoolean(),returned==material);compare(label+" setter "+setter,step,material);order.add(setter);steps++;
   }
   if(!order.isEmpty())orders.add(order.toString());types.add(type);
  }
  assertEquals(2592,fixture.getAsJsonArray("cases").size());assertEquals(9216,steps);assertEquals(6,types.size());assertEquals(24,orders.size());
 }
 private static Ps4Material.Type type(String name){return switch(name){case "8Material"->Ps4Material.Type.BASE;case "11GasMaterial"->Ps4Material.Type.GAS;case "14LiquidMaterial"->Ps4Material.Type.LIQUID;case "18DecorationMaterial"->Ps4Material.Type.DECORATION;case "14PortalMaterial"->Ps4Material.Type.PORTAL;case "11WebMaterial"->Ps4Material.Type.WEB;default->throw new AssertionError(name);};}
 private static void compare(String label,JsonObject expected,Ps4Material m){
  JsonObject f=new JsonObject();f.addProperty("flammable",m.flammable());f.addProperty("replaceable",m.replaceable());f.addProperty("translucent",m.translucent());f.addProperty("tool_not_required",m.toolNotRequired());f.addProperty("push_reaction",m.pushReaction());f.addProperty("color_code",m.color().code());f.addProperty("color_ordinal",m.color().ordinal());assertEquals(label+" stored fields",expected.getAsJsonObject("fields"),f);
  JsonObject v=new JsonObject();v.addProperty("0x10",m.liquid());v.addProperty("0x18",m.nonSolid());v.addProperty("0x20",m.solid());v.addProperty("0x28",m.blocksLight());v.addProperty("0x30",m.blocksMotion());v.addProperty("0x50",m.flammable());v.addProperty("0x60",m.replaceable());v.addProperty("0x68",m.solidBlocking());v.addProperty("0x70",m.toolNotRequired());v.addProperty("0x78",m.pushReaction());assertEquals(label+" virtual dispatch",expected.getAsJsonObject("virtuals"),v);
 }
}

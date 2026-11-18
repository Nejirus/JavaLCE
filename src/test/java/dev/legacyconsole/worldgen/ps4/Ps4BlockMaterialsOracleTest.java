package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4BlockMaterialsOracleTest {
 @Test public void registeredBlockConstructorSelectionsStoresAndStateMaterialGettersMatch()throws Exception{
  JsonObject fixture;try(InputStream input=getClass().getResourceAsStream("/worldgen/ps4-block-materials.json.gz");GZIPInputStream gzip=new GZIPInputStream(input);Reader reader=new InputStreamReader(gzip,StandardCharsets.UTF_8)){fixture=new JsonParser().parse(reader).getAsJsonObject();}
  int count=0;Set<Integer> ids=new HashSet<>();Set<Long> aliases=new HashSet<>();Ps4BlockMaterials previous=null;
  for(JsonElement context:fixture.getAsJsonArray("contexts")){
   Ps4BlockMaterials materials=new Ps4BlockMaterials();Map<Long,Ps4Material> identities=new HashMap<>();
   for(JsonElement element:context.getAsJsonObject().getAsJsonArray("blocks")){
    JsonObject row=element.getAsJsonObject();int id=row.get("id").getAsInt();long alias=Long.decode(row.get("material_alias").getAsString());String label="native block "+id+" "+row.get("name").getAsString()+" color context "+context.getAsJsonObject().get("supplied_color_ordinal");
    assertEquals(label+" original constructor material selection/store/state getter",alias,Ps4BlockMaterials.alias(id));Ps4Material material=materials.forRegisteredBlock(id);assertSame(label,materials.byAlias(alias),material);
    Ps4Material same=identities.putIfAbsent(alias,material);if(same!=null)assertSame(label+" material identity shared by blocks",same,material);if(previous!=null)assertNotSame(label+" context owns material initialization",previous.forRegisteredBlock(id),material);
    JsonObject virtuals=new JsonObject();virtuals.addProperty("0x10",material.liquid());virtuals.addProperty("0x18",material.nonSolid());virtuals.addProperty("0x20",material.solid());virtuals.addProperty("0x28",material.blocksLight());virtuals.addProperty("0x30",material.blocksMotion());virtuals.addProperty("0x50",material.flammable());virtuals.addProperty("0x60",material.replaceable());virtuals.addProperty("0x68",material.solidBlocking());virtuals.addProperty("0x70",material.toolNotRequired());virtuals.addProperty("0x78",material.pushReaction());assertEquals(label+" original material virtuals",row.getAsJsonObject("virtuals"),virtuals);
    ids.add(id);aliases.add(alias);count++;
   }
   previous=materials;
  }
  assertEquals(1035,count);assertEquals(345,ids.size());assertTrue(ids.contains(0));assertTrue(ids.contains(365));assertEquals(38,aliases.size());assertEquals(213,fixture.getAsJsonArray("constructor_functions").size());
 }
}

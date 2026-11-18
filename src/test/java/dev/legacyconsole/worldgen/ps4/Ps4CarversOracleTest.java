package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4CarversOracleTest {
 @Test public void originalPs4CarverPipelineMatchesEveryVoxel()throws Exception{
  Map<String,byte[]> files=new HashMap<>();
  try(ZipInputStream zip=new ZipInputStream(getClass().getResourceAsStream("/worldgen/ps4-carvers.zip"))){
   for(ZipEntry e;(e=zip.getNextEntry())!=null;)files.put(e.getName(),zip.readAllBytes());
  }
  JsonArray cases=new JsonParser().parse(new String(files.get("manifest.json"),StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("cases");
  for(JsonElement element:cases){
   JsonObject c=element.getAsJsonObject();long seed=c.get("seed").getAsLong();
   int cx=c.getAsJsonArray("chunk").get(0).getAsInt(),cz=c.getAsJsonArray("chunk").get(1).getAsInt();
   String profile=c.get("biomes").getAsString();
   byte[] got=files.get(c.get("input").getAsString()).clone(),want=files.get(c.get("native").getAsString());
   Ps4Carvers.carve(seed,cx,cz,got,(x,z)->profile.equals("mosaic")?new int[]{0,1,12,14,24,44,49,1}[Math.floorMod((x>>4)+3*(z>>4),8)]:Integer.parseInt(profile));
   assertArrayEquals("original PS4 seed="+seed+" chunk="+cx+","+cz+" biomes="+profile,want,got);
  }
 }
}

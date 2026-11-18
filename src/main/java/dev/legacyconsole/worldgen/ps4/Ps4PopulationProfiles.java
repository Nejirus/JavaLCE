package dev.legacyconsole.worldgen.ps4;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Original registered biome virtual bindings, not inferred Java biome families. */
public final class Ps4PopulationProfiles {
 public record Profile(int id,int variant,Ps4BiomeTrees.Biome tree,Ps4BiomePlants.Grass grass,Ps4BiomePlants.Flowers flowers,Ps4BiomeDecorations.Biome decoration,int maximumSnowDepth,int top){}
 private static final Map<Integer,Profile> PROFILES=load();
 private Ps4PopulationProfiles(){}
 public static Profile get(int id){return Objects.requireNonNull(PROFILES.get(id),"Unregistered PS4 population biome "+id);}
 private static Map<Integer,Profile> load(){
  try(var in=Objects.requireNonNull(Ps4PopulationProfiles.class.getResourceAsStream("/assets/legacyconsole/worldgen/ps4/population-profiles.json"))){
   Map<Integer,Profile> result=new LinkedHashMap<>();
   for(var entry:new JsonParser().parse(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("profiles")){
    var p=entry.getAsJsonObject();int id=p.get("id").getAsInt();String decoration=p.get("decoration").getAsString();
    result.put(id,new Profile(id,p.get("variant").getAsInt(),Ps4BiomeTrees.Biome.valueOf(p.get("tree").getAsString()),Ps4BiomePlants.Grass.valueOf(p.get("grass").getAsString()),Ps4BiomePlants.Flowers.valueOf(p.get("flowers").getAsString()),decoration.equals("DEFAULT")?null:Ps4BiomeDecorations.Biome.valueOf(decoration),p.get("maximumSnowDepth").getAsInt(),p.get("top").getAsInt()));
   }
   return Map.copyOf(result);
  }catch(IOException e){throw new UncheckedIOException(e);}
 }
}

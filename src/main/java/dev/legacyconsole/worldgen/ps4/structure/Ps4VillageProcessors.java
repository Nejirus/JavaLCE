package dev.legacyconsole.worldgen.ps4.structure;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/** Original PS4 rule registry (0x716680), including mature crops and path bridges. */
public final class Ps4VillageProcessors {
 private record Predicate(String kind,BlockState block,float chance,int biome) {
  boolean test(BlockState state,Random random,boolean desert){return kind.equals("always")||state.is(block.getBlock())&&(!kind.equals("block_biome")||biome==1&&desert)&&(!kind.equals("block_random")||random.nextFloat()<chance);}
 }
 private record Rule(Predicate input,Predicate location,BlockState output){}
 private static final Map<String,List<Rule>> RULES=load();
 private Ps4VillageProcessors(){}
 private static Predicate predicate(JsonObject json){return new Predicate(json.get("kind").getAsString(),json.has("input")?state(json.getAsJsonObject("input")):Blocks.AIR.defaultBlockState(),json.has("chance")?json.get("chance").getAsFloat():1,json.has("biome")?json.get("biome").getAsInt():0);}
 private static BlockState state(JsonObject json){
  String name=json.get("block").getAsString();int data=json.get("data").getAsInt();name=switch(name){case "grass"->"grass_block";case "grass_path"->"dirt_path";case "flowing_water"->"water";case "planks"->"oak_planks";case "sandstone"->data==2?"smooth_sandstone":data==1?"chiseled_sandstone":"sandstone";default->name;};
  ResourceLocation id=new ResourceLocation("minecraft",name);if(!Registry.BLOCK.containsKey(id))throw new IllegalArgumentException("Unmapped PS4 rule output "+json);BlockState state=Registry.BLOCK.get(id).defaultBlockState();if(state.getBlock() instanceof CropBlock crop)state=crop.getStateForAge(Math.min(data,crop.getMaxAge()));return state;
 }
 private static Map<String,List<Rule>> load(){
  try(var in=Objects.requireNonNull(Ps4VillageProcessors.class.getResourceAsStream(Ps4Templates.ROOT+"village-processors.json"))){var data=new JsonParser().parse(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();Map<String,List<Rule>> result=new HashMap<>();for(var entry:data.entrySet()){List<Rule> rules=new ArrayList<>();for(var value:entry.getValue().getAsJsonArray()){var rule=value.getAsJsonObject();rules.add(new Rule(predicate(rule.getAsJsonObject("input")),predicate(rule.getAsJsonObject("location")),state(rule.getAsJsonObject("output"))));}result.put(entry.getKey(),List.copyOf(rules));}return Map.copyOf(result);}catch(IOException error){throw new UncheckedIOException(error);}
 }
 public static BlockState process(String processor,BlockState input,BlockState existing,Random random,boolean desert){
  for(var rule:RULES.getOrDefault(processor,List.of()))if(rule.input.test(input,random,desert)&&rule.location.test(existing,random,desert))return rule.output;return input;
 }
 public static int count(){return RULES.size();}
}

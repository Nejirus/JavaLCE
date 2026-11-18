package dev.legacyconsole.client.ui;
import java.util.*;import com.google.gson.*;
import net.minecraft.core.Registry;import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;import net.minecraft.world.level.block.*;
import net.minecraft.world.level.biome.*;import net.minecraft.world.level.levelgen.*;import net.minecraft.world.level.levelgen.flat.*;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.client.gui.screens.PresetFlatWorldScreen;
public final class FlatWorldModel {
 final Info info=new Info();
 static final class Info {
  final List<FlatLayerInfo> layers=new ArrayList<>();final Map<String,Map<String,String>> features=new LinkedHashMap<>();
  int biome=BuiltinRegistries.BIOME.getId(BuiltinRegistries.BIOME.getOrThrow(Biomes.PLAINS));
  Map<String,Map<String,String>> getWorldFeatures(){return features;}int getBiome(){return biome;}void setBiome(int id){biome=id;}
 }
 FlatWorldModel(String preset){preset(preset);}
 void preset(String value){
  info.layers.clear();info.features.clear();
  if(value!=null&&value.trim().startsWith("{")){
   JsonObject root=new JsonParser().parse(value).getAsJsonObject();
   for(JsonElement e:root.getAsJsonArray("layers")){JsonObject l=e.getAsJsonObject();ResourceLocation id=new ResourceLocation(l.get("block").getAsString());if(!Registry.BLOCK.containsKey(id))throw new IllegalArgumentException("Unknown layer block "+id);info.layers.add(new FlatLayerInfo(l.get("height").getAsInt(),Registry.BLOCK.get(id)));}
   ResourceLocation biome=new ResourceLocation(root.get("biome").getAsString());info.biome=BuiltinRegistries.BIOME.getId(BuiltinRegistries.BIOME.get(biome));
   for(JsonElement feature:root.getAsJsonArray("features"))info.features.put(feature.getAsString(),new HashMap<>());
  }else{
   FlatLevelGeneratorSettings defaults=FlatLevelGeneratorSettings.getDefault(BuiltinRegistries.BIOME);
   FlatLevelGeneratorSettings settings=value==null||value.isEmpty()?defaults:PresetFlatWorldScreen.fromString(BuiltinRegistries.BIOME,value,defaults);
   info.layers.addAll(settings.getLayersInfo());info.biome=BuiltinRegistries.BIOME.getId(settings.getBiome());
   if(settings.structureSettings().structureConfig().containsKey(StructureFeature.VILLAGE))info.features.put("village",new HashMap<>());
  }
  if(info.layers.isEmpty()||height()>256||info.layers.stream().anyMatch(l->l.getHeight()<1))throw new IllegalArgumentException("Flat world must contain 1–256 layers of blocks");
 }
 int size(){return info.layers.size();}int height(){return info.layers.stream().mapToInt(FlatLayerInfo::getHeight).sum();}
 FlatLayerInfo layer(int topIndex){return info.layers.get(size()-1-topIndex);}int available(int index,boolean edit){return 256-height()+(edit?layer(index).getHeight():0);}
 void put(int index,boolean edit,int height,Block block,int metadata){if(block==null||height<1||height>available(index,edit))throw new IllegalArgumentException("Layer exceeds world height");FlatLayerInfo l=new FlatLayerInfo(height,block);if(edit)info.layers.set(size()-1-index,l);else info.layers.add(size()-index,l);}
 boolean remove(int index){if(size()<=1)return false;info.layers.remove(size()-1-index);return true;}
 void move(int from,int to){if(from>=0&&to>=0&&from<size()&&to<size())Collections.swap(info.layers,size()-1-from,size()-1-to);}
 void toggle(String feature){if(info.features.containsKey(feature))info.features.remove(feature);else info.features.put(feature,new HashMap<>());}
 String preset(){
  JsonObject root=new JsonObject();JsonArray layers=new JsonArray();for(FlatLayerInfo layer:info.layers){JsonObject l=new JsonObject();l.addProperty("block",Registry.BLOCK.getKey(layer.getBlockState().getBlock()).toString());l.addProperty("height",layer.getHeight());layers.add(l);}root.add("layers",layers);
  root.addProperty("biome",BuiltinRegistries.BIOME.getKey(BuiltinRegistries.BIOME.byId(info.biome)).toString());JsonArray features=new JsonArray();info.features.keySet().forEach(features::add);root.add("features",features);return root.toString();
 }
 public static FlatLevelGeneratorSettings nativeSettings(String preset,Registry<Biome> biomes){
  FlatWorldModel model=new FlatWorldModel(preset);Map<StructureFeature<?>,StructureFeatureConfiguration> structures=new HashMap<>();
  Map<String,List<StructureFeature<?>>> names=new LinkedHashMap<>();names.put("village",List.of(StructureFeature.VILLAGE));names.put("mineshaft",List.of(StructureFeature.MINESHAFT));names.put("oceanmonument",List.of(StructureFeature.OCEAN_MONUMENT));names.put("biome_1",List.of(StructureFeature.DESERT_PYRAMID,StructureFeature.JUNGLE_TEMPLE,StructureFeature.SWAMP_HUT,StructureFeature.IGLOO));
  names.forEach((name,list)->{if(model.info.features.containsKey(name))for(StructureFeature<?> type:list)structures.put(type,StructureSettings.DEFAULTS.get(type));});
  StructureSettings structureSettings=new StructureSettings(model.info.features.containsKey("stronghold")?Optional.of(new StrongholdConfiguration(32,3,128)):Optional.empty(),structures);
  FlatLevelGeneratorSettings result=new FlatLevelGeneratorSettings(structureSettings,biomes);result.getLayersInfo().addAll(model.info.layers);
  ResourceLocation biome=BuiltinRegistries.BIOME.getKey(BuiltinRegistries.BIOME.byId(model.info.biome));result.setBiome(()->biomes.get(biome));
  if(model.info.features.containsKey("decoration"))result.setDecoration();if(model.info.features.containsKey("lake"))result.setAddLakes();result.updateLayers();return result;
 }
 static String biomeName(Biome biome){ResourceLocation id=BuiltinRegistries.BIOME.getKey(biome);return net.minecraft.client.resources.language.I18n.get("biome."+id.getNamespace()+"."+id.getPath());}
}

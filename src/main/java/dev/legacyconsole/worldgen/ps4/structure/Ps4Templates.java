package dev.legacyconsole.worldgen.ps4.structure;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/** Original PS4 media templates and captured registry/pool data. */
public final class Ps4Templates {
 public static final String ROOT="/assets/legacyconsole/worldgen/ps4/";
 private static final Map<String,Asset> CACHE=new ConcurrentHashMap<>();
 public record Joint(BlockPos pos,Direction facing,String attachment,String pool){}
 public record Asset(StructureTemplate template,List<Joint> joints){}
 public record Element(String id,String template,String processor,int projection,boolean feature){public boolean empty(){return template.isEmpty()&&!feature;}}
 public record Pool(String fallback,List<Element> weighted){}
 private static final Map<String,Element> ELEMENTS=loadElements();
 private static final Map<String,Pool> POOLS=loadPools();
 private Ps4Templates(){}
 private static JsonObject json(String name){try(var in=Objects.requireNonNull(Ps4Templates.class.getResourceAsStream(ROOT+name),name)){return new JsonParser().parse(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject();}catch(IOException error){throw new UncheckedIOException(error);}}
 private static Map<String,Element> loadElements(){
  Map<String,Element> result=new LinkedHashMap<>();
  for(var e:json("village-elements.json").entrySet()){var data=e.getValue().getAsJsonObject();boolean feature=data.has("kind");result.put(e.getKey(),new Element(e.getKey(),data.has("template")?data.get("template").getAsString():"",data.has("processor")?data.get("processor").getAsString():"",data.has("projection")?data.get("projection").getAsInt():0,feature));}
  return Collections.unmodifiableMap(result);
 }
 private static Map<String,Pool> loadPools(){
  Map<String,Pool> result=new LinkedHashMap<>();
  for(var e:json("village-pools.json").getAsJsonArray("pools")){var p=e.getAsJsonObject();List<Element> weighted=new ArrayList<>();for(var m:p.getAsJsonArray("members")){var data=m.getAsJsonObject();var element=Objects.requireNonNull(ELEMENTS.get(data.get("element").getAsString()));for(int i=0;i<data.get("weight").getAsInt();i++)weighted.add(element);}result.put(p.get("name").getAsString(),new Pool(p.get("fallback").getAsString(),List.copyOf(weighted)));}
  return Collections.unmodifiableMap(result);
 }
 public static Pool pool(String name){return POOLS.get(name);}
 public static Collection<Pool> pools(){return POOLS.values();}
 public static Collection<Element> elements(){return ELEMENTS.values();}
 public static String template(int id){return TemplateNames.VALUES.get(id);}
 private static final class TemplateNames {static final Map<Integer,String> VALUES=load();private static Map<Integer,String> load(){try(var in=Objects.requireNonNull(Ps4Templates.class.getResourceAsStream(ROOT+"templates.json"))){Map<Integer,String> result=new HashMap<>();for(var entry:new JsonParser().parse(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonArray()){var data=entry.getAsJsonObject();result.put(data.get("index").getAsInt(),data.get("path").getAsString());}return Map.copyOf(result);}catch(IOException error){throw new UncheckedIOException(error);}}}
 public static Asset asset(String name){return CACHE.computeIfAbsent(name,Ps4Templates::read);}
 public static Asset read(String name){
  try(var in=Objects.requireNonNull(Ps4Templates.class.getResourceAsStream(ROOT+"templates/"+name+".nbt"),name)){
   CompoundTag tag=NbtIo.readCompressed(in);ListTag palette=tag.getList("palette",10);
   Map<Integer,String> banners=new HashMap<>();for(int i=0;i<palette.size();i++){String id=palette.getCompound(i).getString("Name");if(id.equals("minecraft:wall_banner")||id.equals("minecraft:illager_captain_wall_banner"))banners.put(i,id);}
   for(Tag value:palette)Ps4TemplateBridge.block((CompoundTag)value);
   for(Tag value:tag.getList("entities",10))Ps4TemplateBridge.entity(((CompoundTag)value).getCompound("nbt"));
   List<Joint> joints=new ArrayList<>();
   for(Tag value:tag.getList("blocks",10)){var block=(CompoundTag)value;var state=palette.getCompound(block.getInt("state"));var nbt=block.getCompound("nbt");Ps4TemplateBridge.blockEntity(nbt);
    String oldBanner=banners.get(block.getInt("state"));
    if(oldBanner!=null){if(oldBanner.equals("minecraft:wall_banner")){
     CompoundTag colored=state.copy();colored.putString("Name","minecraft:"+net.minecraft.world.item.DyeColor.byId(15-nbt.getInt("Base")).getName()+"_wall_banner");block.putInt("state",palette.size());palette.add(colored);nbt.remove("Base");
     for(Tag pattern:nbt.getList("Patterns",10)){var p=(CompoundTag)pattern;p.putInt("Color",15-p.getInt("Color"));}
    }else nbt.put("Patterns",net.minecraft.world.entity.raid.Raid.getLeaderBannerInstance().getOrCreateTagElement("BlockEntityTag").getList("Patterns",10).copy());}

    if(nbt.contains("final_state"))nbt.putString("final_state",Ps4TemplateBridge.finalState(nbt.getString("final_state")));
    if(state.getString("Name").equals("minecraft:jigsaw")){ListTag p=block.getList("pos",3);Direction facing=Direction.byName(state.getCompound("Properties").getString("orientation").split("_")[0]);joints.add(new Joint(new BlockPos(p.getInt(0),p.getInt(1),p.getInt(2)),facing,nbt.getString("attachement_type"),nbt.getString("target_pool")));}
   }
   StructureTemplate template=new StructureTemplate();template.load(tag);return new Asset(template,List.copyOf(joints));
  }catch(IOException error){throw new UncheckedIOException("Cannot load original PS4 template "+name,error);}
 }
 public static BoundingBox box(Element element,BlockPos pos,Rotation rotation){return element.feature?new BoundingBox(pos):asset(element.template).template.getBoundingBox(new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings().setRotation(rotation),pos);}
 public static List<Joint> joints(Element element,BlockPos pos,Rotation rotation){
  if(element.feature)return new ArrayList<>(List.of(new Joint(pos,Direction.DOWN,"minecraft:bottom","minecraft:empty")));
  List<Joint> result=new ArrayList<>();for(var j:asset(element.template).joints)result.add(new Joint(StructureTemplate.transform(j.pos,Mirror.NONE,rotation,BlockPos.ZERO).offset(pos),rotation.rotate(j.facing),j.attachment,j.pool));return result;
 }
}

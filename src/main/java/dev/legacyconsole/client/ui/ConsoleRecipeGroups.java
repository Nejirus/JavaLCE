package dev.legacyconsole.client.ui;

import dev.legacyconsole.crafting.RecipeCatalog;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.CandleBlock;

/** Restored 2.3.3 recipe families and ordering, as requested for the PS4 menu. */
final class ConsoleRecipeGroups {
 static final String[] NAMES={"Structures","Tools & Weapons","Food","Armour","Mechanisms","Transport","Decoration"};
 static final String[] ICONS={"structures","tools","food","armour","mechanisms","transport","decoration"};
 static final class Group {final int id;final List<RecipeCatalog.Entry> recipes=new ArrayList<>();Group(int id){this.id=id;}}
 record Position(int group,int rank){}
 private static final int RANK_STEP=16;
 private static final Map<String,Position> ORDER=new LinkedHashMap<>(),FAMILIES=new LinkedHashMap<>();
 private static final Map<ResourceLocation,Position> DYES=new LinkedHashMap<>();
 private static final Map<ResourceLocation,Position> TAGS=new LinkedHashMap<>();
 private static boolean loaded;
 private static String normalize(String id){return id.contains(":")?id:"minecraft:"+id;}
 private static void group(JsonElement recipes,int id){
  List<JsonElement> values=recipes.isJsonArray()?new ArrayList<>():List.of(recipes);
  if(recipes.isJsonArray())recipes.getAsJsonArray().forEach(values::add);
  int rank=0;
  for(JsonElement e:values)if(e.isJsonPrimitive()){
   String name=e.getAsString();Position pos=new Position(id,rank++*RANK_STEP);
   if(name.startsWith("#"))TAGS.putIfAbsent(new ResourceLocation(name.substring(1)),pos);
   else ORDER.putIfAbsent(normalize(name),pos);
  }
 }
 static void load(Reader reader){
  ORDER.clear();FAMILIES.clear();TAGS.clear();DYES.clear();int tab=0;
  for(var entry:new JsonParser().parse(reader).getAsJsonObject().entrySet()){
   JsonElement listing=entry.getValue().getAsJsonObject().get("listing");int family=0;
   if(listing.isJsonObject())for(var g:listing.getAsJsonObject().entrySet()){
    int id=tab*100+family++;FAMILIES.put(entry.getKey()+"/"+g.getKey(),new Position(id,0));group(g.getValue(),id);
   }else for(JsonElement e:listing.getAsJsonArray()){
    int id=tab*100+family++;
    String name=e.isJsonObject()?e.getAsJsonObject().get("group").getAsString():e.getAsString();
    FAMILIES.put(entry.getKey()+"/"+name,new Position(id,0));
    group(e.isJsonObject()?e.getAsJsonObject().get("recipes"):e,id);
   }
   tab++;
  }
  adapt1171();
  loaded=true;
 }
 // Keep the imported reference intact; translate its missing/renamed 1.17.1
 // entries into the existing columns rather than an unrelated catch-all.
 private static void after(String anchor,String... recipes){
  Position p=ORDER.get(normalize(anchor));if(p==null)return;
  for(int i=0;i<recipes.length;i++)ORDER.putIfAbsent(normalize(recipes[i]),new Position(p.group,p.rank+i+1));
 }
 private static void alias(String reference,String recipe){
  Position p=ORDER.get(normalize(reference));if(p!=null)ORDER.putIfAbsent(normalize(recipe),p);
 }
 private static void adapt1171(){
  // The newer Legacy4J listing places blackstone after stone, and Nether
  // fences/gates after the Overworld woods, before the stone barriers.
  after("stone_button","polished_blackstone_button");
  after("cherry_fence","crimson_fence","warped_fence");
  after("cherry_fence_gate","crimson_fence_gate","warped_fence_gate");
  for(String shape:List.of("slab","stairs","wall"))alias("blackstone_brick_"+shape,"polished_blackstone_brick_"+shape);
  after("sandstone_slab","cut_sandstone_slab","smooth_sandstone_slab");
  after("red_sandstone_slab","cut_red_sandstone_slab","smooth_red_sandstone_slab");
  // Modern Legacy4J matches dyes by output item, not by recipe ID. Derive
  // that same color order from the pinned listing, including its alium typo.
  Position dye=FAMILIES.get("decoration/dye");
  if(dye!=null)for(DyeColor color:DyeColor.values()){
   String id=normalize(color.getName()+"_dye");
   for(var e:ORDER.entrySet())if(e.getValue().group==dye.group&&(e.getKey().equals(id)||e.getKey().startsWith(id+"_from_"))){
    DYES.put(new ResourceLocation(id),e.getValue());break;
   }
  }
 }
 private static void load(){if(loaded)return;
  try(Reader reader=new InputStreamReader(Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation("legacyconsole","crafting_legacy4j.json")).getInputStream(),StandardCharsets.UTF_8)){load(reader);}
  catch(IOException e){throw new IllegalStateException("Cannot read Legacy4J recipe order",e);}
 }
 static boolean shown(ItemStack output){return shown(output,false);}
 private static boolean shown(ItemStack output,boolean stonecutting){
  // Wax copper in the world; omit its recipe variants from console crafting.
  // Stonecutting keeps recipes for copper that has already been waxed.
  return !(output.getItem() instanceof BannerItem)&&!output.is(Items.FIREWORK_ROCKET)
      &&!output.is(Items.HONEY_BOTTLE)
      &&(stonecutting||!(output.getItem() instanceof BlockItem block
          &&HoneycombItem.WAX_OFF_BY_BLOCK.get().containsKey(block.getBlock())));
 }
 static boolean shown(RecipeCatalog.Entry entry){
  return shown(entry.output,dev.legacyconsole.crafting.StonecuttingRecipes.isStonecutting(entry))
      &&(!(entry.output.getItem() instanceof BedItem)||entry.recipe instanceof net.minecraft.world.item.crafting.ShapedRecipe);
 }
 private static Position position(RecipeCatalog.Entry e){return position(e.recipe.getId(),e.output,e.category);}
 static Position position(ResourceLocation recipe,ItemStack output,RecipeCatalog.Category category){
  load();Item item=output.getItem();String name=item.getRegistryName().getPath();
  // Legacy4J's later dye_* recipe IDs differ from 1.17.1. Match the output
  // family so string-to-white-wool and all recolors stay in the same column.
  Position wool=FAMILIES.get("decoration/wool");
  if(wool!=null&&name.endsWith("_wool"))return new Position(wool.group,DyeColor.byName(name.substring(0,name.length()-5),DyeColor.WHITE).getId());
  Position candle=FAMILIES.get("decoration/candle");
  if(candle!=null&&item instanceof BlockItem block&&block.getBlock() instanceof CandleBlock)
   return new Position(candle.group,name.equals("candle")?0:1+DyeColor.byName(name.substring(0,name.length()-7),DyeColor.WHITE).getId());
  Position dye=DYES.get(item.getRegistryName());if(dye!=null)return dye;
  Position exact=ORDER.get(recipe.toString());if(exact!=null)return exact;
  Position byItem=ORDER.get(item.getRegistryName().toString());if(byItem!=null)return byItem;
  for(var tag:TAGS.entrySet())if(ItemTags.getAllTags().getTagOrEmpty(tag.getKey()).contains(item))return tag.getValue();
  int tab=category==RecipeCatalog.Category.FOOD?2:item instanceof ArmorItem?3:category==RecipeCatalog.Category.TOOLS||category==RecipeCatalog.Category.GEAR?1:category==RecipeCatalog.Category.LOGIC?(name.contains("rail")||name.contains("minecart")||name.contains("boat")?5:4):item instanceof BlockItem?0:6;
  return new Position(tab*100+90,0);
 }
 static int family(RecipeCatalog.Entry e){return position(e).group;}
 static List<Group> groups(Collection<RecipeCatalog.Entry> entries,int tab,boolean table){
  SortedMap<Integer,Group> groups=new TreeMap<>();for(RecipeCatalog.Entry e:entries){if(!e.fits(table)||!shown(e))continue;int id=family(e);if(tab<0||id/100==tab)groups.computeIfAbsent(id,Group::new).recipes.add(e);}
  for(Group g:groups.values())g.recipes.sort(Comparator.comparingInt((RecipeCatalog.Entry e)->position(e).rank).thenComparing(e->e.recipe.getId().toString()));
  return new ArrayList<>(groups.values());
 }
}

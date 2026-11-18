package dev.legacyconsole.crafting;

import static org.junit.Assert.*;
import java.util.*;
import net.minecraft.world.item.*;
import org.junit.*;

public class Ps4CraftingInformationTest {
 @BeforeClass public static void bootstrap(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
 @Test public void cycleMatchesOriginalPs4MachineCode()throws Exception{
  try(var reader=new java.io.InputStreamReader(getClass().getResourceAsStream("/ps4/crafting-info-native.json"),java.nio.charset.StandardCharsets.UTF_8)){
   var data=new com.google.gson.JsonParser().parse(reader).getAsJsonObject();for(var element:data.getAsJsonArray("transitions")){var row=element.getAsJsonObject();assertEquals(row.get("after").getAsInt(),Ps4CraftingInformation.next(row.get("before").getAsInt()));}
  }
 }
 @Test public void repeatedIngredientsKeepTheirRequiredQuantityAndSharedShortage(){
  var string=new RecipeCatalog.Ingredient(new ItemStack(Items.STRING));
  var recipe=new RecipeCatalog.Entry(null,new ItemStack(Items.WHITE_WOOL),2,2,false,List.of(string,string,string,string));
  var lines=Ps4CraftingInformation.ingredients(recipe,List.of(new ItemStack(Items.STRING,3)));assertEquals(1,lines.size());assertEquals(4,lines.get(0).required());assertEquals(4,lines.get(0).icon().getCount());assertTrue(lines.get(0).missing());
  assertFalse(Ps4CraftingInformation.ingredients(recipe,List.of(new ItemStack(Items.STRING,2),new ItemStack(Items.STRING,2))).get(0).missing());
 }
 @Test public void anAlternativeDoesNotStealTheOnlyStrictIngredient(){
  var oak=new RecipeCatalog.Ingredient(new ItemStack(Items.OAK_PLANKS));var wood=new RecipeCatalog.Ingredient(List.of(new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.BIRCH_PLANKS)));
  var recipe=new RecipeCatalog.Entry(null,new ItemStack(Items.STICK),2,1,false,List.of(oak,wood));
  var lines=Ps4CraftingInformation.ingredients(recipe,List.of(new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.BIRCH_PLANKS)));assertEquals(2,lines.size());assertTrue(lines.stream().noneMatch(Ps4CraftingInformation.Line::missing));
 }
 @Test public void descriptionsUseActualPs4ItemBindings(){
  assertEquals("Deals more damage than by hand.",dev.legacyconsole.client.Ps4Descriptions.get(new ItemStack(Items.WOODEN_SWORD)));
  assertEquals("Required to mine stone-related blocks and ore.",dev.legacyconsole.client.Ps4Descriptions.get(new ItemStack(Items.DIAMOND_PICKAXE)));
 }
}

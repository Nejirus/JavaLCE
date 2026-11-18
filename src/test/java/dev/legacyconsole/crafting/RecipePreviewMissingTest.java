package dev.legacyconsole.crafting;

import static org.junit.Assert.*;
import java.util.*;
import net.minecraft.world.item.*;
import org.junit.*;

public class RecipePreviewMissingTest {
 @BeforeClass public static void bootstrap(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
 private RecipeCatalog.Ingredient string(){return new RecipeCatalog.Ingredient(new ItemStack(Items.STRING));}
 private List<RecipeCatalog.Ingredient> wool(){return List.of(string(),string(),string(),string());}
 @Test public void twoStringsForWoolMarkFirstTwoSquaresMissing(){
  assertArrayEquals(new boolean[]{true,true,false,false},RecipeCatalog.missingIngredients(wool(),List.of(new ItemStack(Items.STRING,2))));
 }
 @Test public void splitStacksCountAcrossTheWholeInventory(){
  assertArrayEquals(new boolean[]{true,true,false,false},RecipeCatalog.missingIngredients(wool(),List.of(new ItemStack(Items.STRING),new ItemStack(Items.STRING))));
  assertArrayEquals(new boolean[4],RecipeCatalog.missingIngredients(wool(),List.of(new ItemStack(Items.STRING,2),new ItemStack(Items.STRING,2))));
 }
 @Test public void emptyGridCellsRemainClear(){
  var empty=new RecipeCatalog.Ingredient(ItemStack.EMPTY);
  assertArrayEquals(new boolean[]{false,true,false,false},RecipeCatalog.missingIngredients(List.of(empty,string(),string(),empty),List.of(new ItemStack(Items.STRING))));
 }
 @Test public void alternativesLeaveTheStrictIngredientAvailable(){
  var strict=new RecipeCatalog.Ingredient(new ItemStack(Items.OAK_PLANKS));
  var flexible=new RecipeCatalog.Ingredient(List.of(new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.BIRCH_PLANKS)));
  assertArrayEquals(new boolean[2],RecipeCatalog.missingIngredients(List.of(strict,flexible),List.of(new ItemStack(Items.OAK_PLANKS),new ItemStack(Items.BIRCH_PLANKS))));
 }
 @Test public void fireworksPreviewDoesNotSpendOnePowderThreeTimes(){
  var powder=new ItemStack(Items.GUNPOWDER);
  assertArrayEquals(new boolean[]{true,false,true,false},RecipeCatalog.missingGridItems(List.of(powder,new ItemStack(Items.PAPER),powder,powder),List.of(new ItemStack(Items.GUNPOWDER),new ItemStack(Items.PAPER))));
 }
 @Test public void specialCraftingKeepsExactItemTags(){
  var named=new ItemStack(Items.WHITE_BANNER);named.setHoverName(new net.minecraft.network.chat.TextComponent("Original"));
  assertArrayEquals(new boolean[]{true},RecipeCatalog.missingGridItems(List.of(named),List.of(new ItemStack(Items.WHITE_BANNER))));
  assertArrayEquals(new boolean[]{false},RecipeCatalog.missingGridItems(List.of(named),List.of(named.copy())));
 }
}

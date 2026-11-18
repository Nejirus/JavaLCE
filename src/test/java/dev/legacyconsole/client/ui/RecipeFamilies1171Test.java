package dev.legacyconsole.client.ui;
import static org.junit.Assert.*;
import org.junit.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import dev.legacyconsole.crafting.RecipeCatalog;

public class RecipeFamilies1171Test {
 @BeforeClass public static void setup()throws Exception{
  net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  try(Reader r=new InputStreamReader(RecipeFamilies1171Test.class.getResourceAsStream("/assets/legacyconsole/crafting_legacy4j.json"),StandardCharsets.UTF_8)){ConsoleRecipeGroups.load(r);}
 }
 private ConsoleRecipeGroups.Position at(Item item){return ConsoleRecipeGroups.position(new ResourceLocation(item.getRegistryName().getPath()),new ItemStack(item),RecipeCatalog.Category.BLOCKS);}
 private ConsoleRecipeGroups.Position recipe(String id,Item output){return ConsoleRecipeGroups.position(new ResourceLocation(id),new ItemStack(output),RecipeCatalog.Category.OTHER);}
 private void sameFamily(Item anchor,Item... outputs){for(Item output:outputs)assertEquals(output.getRegistryName().toString(),at(anchor).group(),at(output).group());}
 private void between(Item first,Item middle,Item last){sameFamily(first,middle,last);assertTrue(at(first).rank()<at(middle).rank());assertTrue(at(middle).rank()<at(last).rank());}
 @Test public void planksAndWoodRestoreThePreviousSharedColumn(){
  sameFamily(Items.OAK_PLANKS,Items.BIRCH_PLANKS,Items.SPRUCE_PLANKS,Items.CRIMSON_PLANKS,
   Items.WARPED_PLANKS,Items.OAK_WOOD,Items.STRIPPED_OAK_WOOD,Items.WARPED_HYPHAE);
  assertTrue(at(Items.OAK_PLANKS).rank()<at(Items.BIRCH_PLANKS).rank());
  assertTrue(at(Items.BIRCH_PLANKS).rank()<at(Items.SPRUCE_PLANKS).rank());
 }
 @Test public void workstationsAndStorageRestoreTheirPreviousFamilies(){
  sameFamily(Items.CRAFTING_TABLE,Items.FURNACE,Items.BLAST_FURNACE,Items.SMOKER,
   Items.ENCHANTING_TABLE,Items.BREWING_STAND,Items.CARTOGRAPHY_TABLE,Items.LOOM,Items.STONECUTTER);
  sameFamily(Items.CHEST,Items.TRAPPED_CHEST,Items.ENDER_CHEST,Items.BARREL,Items.SHULKER_BOX);
  assertNotEquals(at(Items.CRAFTING_TABLE).group(),at(Items.CHEST).group());
  sameFamily(Items.STICK,Items.LADDER,Items.SCAFFOLDING);
 }
 @Test public void visibleModernCopperKeepsThePreviousShapeFamilies(){
  sameFamily(Items.COPPER_BLOCK,Items.CUT_COPPER,Items.WAXED_COPPER_BLOCK,Items.WAXED_OXIDIZED_CUT_COPPER);
  sameFamily(Items.CUT_COPPER_STAIRS,Items.WAXED_CUT_COPPER_STAIRS,Items.WAXED_OXIDIZED_CUT_COPPER_STAIRS);
  sameFamily(Items.CUT_COPPER_SLAB,Items.WAXED_CUT_COPPER_SLAB,Items.WAXED_OXIDIZED_CUT_COPPER_SLAB);
 }
 @Test public void whiteWoolAndDyeRecipesShareDecorationColumn(){
  assertEquals(6,at(Items.WHITE_WOOL).group()/100);
  assertEquals(at(Items.WHITE_WOOL).group(),at(Items.RED_WOOL).group());
  assertEquals(at(Items.WHITE_WOOL).group(),at(Items.BLACK_WOOL).group());
  assertTrue(at(Items.WHITE_WOOL).rank()<at(Items.RED_WOOL).rank());
 }
 @Test public void plainAndColoredCandlesHaveTheirOwnDecorationColumn(){
  assertEquals(6,at(Items.CANDLE).group()/100);
  assertEquals(at(Items.CANDLE).group(),at(Items.RED_CANDLE).group());
  assertTrue(at(Items.CANDLE).rank()<at(Items.RED_CANDLE).rank());
  assertNotEquals(at(Items.CANDLE).group(),at(Items.WHITE_WOOL).group());
 }
 @Test public void waxedCopperOutputsRemainAvailableWithRestoredGroups(){
  for(Item i:new Item[]{Items.WAXED_COPPER_BLOCK,Items.WAXED_CUT_COPPER_STAIRS,Items.WAXED_OXIDIZED_CUT_COPPER_SLAB})assertTrue(ConsoleRecipeGroups.shown(new ItemStack(i)));
  assertTrue(ConsoleRecipeGroups.shown(new ItemStack(Items.CUT_COPPER_STAIRS)));
  assertTrue(ConsoleRecipeGroups.shown(new ItemStack(Items.COPPER_BLOCK)));
 }
 @Test public void blackstoneButtonFollowsStoneInButtonColumn(){
  assertEquals(4,at(Items.POLISHED_BLACKSTONE_BUTTON).group()/100);
  between(Items.STONE_BUTTON,Items.POLISHED_BLACKSTONE_BUTTON,Items.OAK_BUTTON);
  sameFamily(Items.STONE_BUTTON,Items.CRIMSON_BUTTON,Items.WARPED_BUTTON);
 }
 @Test public void netherFencesAndGatesJoinTheirRespectiveFamilies(){
  sameFamily(Items.OAK_FENCE,Items.CRIMSON_FENCE,Items.WARPED_FENCE);
  sameFamily(Items.OAK_FENCE_GATE,Items.CRIMSON_FENCE_GATE,Items.WARPED_FENCE_GATE);
  assertNotEquals(at(Items.CRIMSON_FENCE).group(),at(Items.CRIMSON_FENCE_GATE).group());
  between(Items.DARK_OAK_FENCE,Items.CRIMSON_FENCE,Items.COBBLESTONE_WALL);
  assertTrue(at(Items.CRIMSON_FENCE).rank()<at(Items.WARPED_FENCE).rank());
 }
 @Test public void blackstoneBrickShapesUseReferencePositions(){
  between(Items.POLISHED_BLACKSTONE_SLAB,Items.POLISHED_BLACKSTONE_BRICK_SLAB,Items.GRANITE_SLAB);
  between(Items.POLISHED_BLACKSTONE_STAIRS,Items.POLISHED_BLACKSTONE_BRICK_STAIRS,Items.GRANITE_STAIRS);
  between(Items.POLISHED_BLACKSTONE_WALL,Items.POLISHED_BLACKSTONE_BRICK_WALL,Items.GRANITE_WALL);
  assertNotEquals(at(Items.POLISHED_BLACKSTONE_BRICK_SLAB).group(),at(Items.POLISHED_BLACKSTONE_BRICK_STAIRS).group());
 }
 @Test public void sandstoneSlabVariantsStayWithSlabsAndTheirMaterial(){
  sameFamily(Items.SANDSTONE_SLAB,Items.CUT_STANDSTONE_SLAB,Items.SMOOTH_SANDSTONE_SLAB,Items.CUT_RED_SANDSTONE_SLAB,Items.SMOOTH_RED_SANDSTONE_SLAB);
  between(Items.SANDSTONE_SLAB,Items.CUT_STANDSTONE_SLAB,Items.RED_SANDSTONE_SLAB);
  between(Items.SANDSTONE_SLAB,Items.SMOOTH_SANDSTONE_SLAB,Items.RED_SANDSTONE_SLAB);
  between(Items.RED_SANDSTONE_SLAB,Items.CUT_RED_SANDSTONE_SLAB,Items.QUARTZ_SLAB);
  between(Items.RED_SANDSTONE_SLAB,Items.SMOOTH_RED_SANDSTONE_SLAB,Items.QUARTZ_SLAB);
 }
 @Test public void allMagentaRecipeIdsUseSameDyeColumnAndColorPosition(){
  for(String id:new String[]{"magenta_dye_from_allium","magenta_dye_from_lilac","magenta_dye_from_blue_red_white_dye","magenta_dye_from_blue_red_pink","magenta_dye_from_purple_and_pink"}){
   assertEquals(at(Items.MAGENTA_DYE),recipe(id,Items.MAGENTA_DYE));
   assertEquals(at(Items.RED_DYE).group(),recipe(id,Items.MAGENTA_DYE).group());
  }
  between(Items.PURPLE_DYE,Items.MAGENTA_DYE,Items.BLUE_DYE);
 }
 @Test public void dyeFamiliesMatchOutputsWhenRecipeNamesDiffer(){
  for(Item dye:new Item[]{Items.YELLOW_DYE,Items.RED_DYE,Items.WHITE_DYE,Items.PINK_DYE,Items.ORANGE_DYE,Items.LIME_DYE,Items.BROWN_DYE,Items.BLACK_DYE,Items.GRAY_DYE,Items.LIGHT_GRAY_DYE,Items.LIGHT_BLUE_DYE,Items.CYAN_DYE,Items.PURPLE_DYE,Items.MAGENTA_DYE,Items.BLUE_DYE}){
   assertEquals(at(Items.RED_DYE).group(),recipe("test:other_dye_recipe",dye).group());
   assertEquals(at(dye),recipe("test:other_dye_recipe",dye));
  }
 }
 @Test public void rocketsUseDedicatedFireworksScreenInsteadOfDecorationFallback(){
  assertFalse(ConsoleRecipeGroups.shown(new ItemStack(Items.FIREWORK_ROCKET)));
  assertTrue(ConsoleRecipeGroups.shown(new ItemStack(Items.GUNPOWDER)));
  assertTrue(ConsoleRecipeGroups.shown(new ItemStack(Items.PAPER)));
 }
 @Test public void honeyRecipesRemainAvailableWithRestoredGroups(){
  assertTrue(ConsoleRecipeGroups.shown(new ItemStack(Items.HONEY_BOTTLE)));
  assertTrue(ConsoleRecipeGroups.shown(new ItemStack(Items.HONEY_BLOCK)));
  assertTrue(ConsoleRecipeGroups.shown(new ItemStack(Items.SUGAR)));
 }
 @Test public void baseBannersUseDedicatedBannerScreen(){
  for(Item banner:new Item[]{Items.WHITE_BANNER,Items.RED_BANNER,Items.BLACK_BANNER})
   assertFalse(ConsoleRecipeGroups.shown(new ItemStack(banner)));
  assertTrue(ConsoleRecipeGroups.shown(new ItemStack(Items.FLOWER_BANNER_PATTERN)));
 }
}

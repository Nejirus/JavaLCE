package dev.legacyconsole.crafting;

import static org.junit.Assert.*;
import static dev.legacyconsole.crafting.Ps4BannerPatterns.*;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import org.junit.*;

public class Ps4SpecialCraftingTest {
  @BeforeClass public static void bootstrap(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
  private ItemStack item(Item item,int count){return new ItemStack(item,count);}
  private ItemStack banner(){return item(Items.WHITE_BANNER,1);}
  private CraftingContainer grid(Item... items){CraftingContainer g=DynamicRecipes.grid(3);for(int i=0;i<items.length;i++)g.setItem(i,new ItemStack(items[i]));return g;}
  @Test public void originalPatternGroupsAndRowsAreRetained() {
    assertEquals(44,all().stream().filter(p->p.ps4_id>=0&&!p.loom).count());assertEquals(38,all().stream().filter(p->p.fits(true)).count());
    assertEquals(11,group(1,true).size());assertEquals(8,group(2,true).size());
    assertArrayEquals(new String[]{"# #"," # ","#*#"},get("cr").shape);
    assertArrayEquals(new String[]{"###","#*#","###"},get("bo").shape);
    assertEquals("enchanted_golden_apple",get("moj").special);
    for(var p:all())if(p.reusable)assertFalse("Loom-only recipe in crafting "+p.source_hash,p.fits(true));
  }
  @Test public void originalLoomPatternsUseOneDyeWithoutATemplate() {
    assertEquals(32,all().stream().filter(p->p.loom).count());
    assertEquals(11,group(1,true,true).size());assertEquals(8,group(2,true,true).size());
    var pattern=get("loom:cr");assertEquals(105,pattern.ps4_id);assertEquals(1,pattern.dyes());
    var plan=Ps4BannerCrafting.plan(banner(),List.of(new Layer("loom:cr",14)),true,true);
    assertNotNull(plan);assertNotNull(plan.allocate(List.of(banner(),item(Items.RED_DYE,1))));
    assertNull(plan.allocate(List.of(banner())));assertTrue(plan.catalysts.isEmpty());
    assertNull(Ps4BannerCrafting.plan(banner(),List.of(new Layer("loom:cr",14)),true));
    assertNull(Ps4BannerCrafting.plan(banner(),List.of(new Layer("cr",14)),true,true));
  }
  @Test public void loomTemplateIsRequiredAndReservedAcrossLayers() {
    var plan=Ps4BannerCrafting.plan(banner(),List.of(new Layer("flop",14),new Layer("flop",14)),true,true);
    assertNotNull(plan);assertEquals(List.of(Items.FLOWER_BANNER_PATTERN),plan.catalysts);
    assertNull(plan.allocate(List.of(banner(),item(Items.RED_DYE,2))));
    assertNull(plan.allocate(List.of(banner(),item(Items.RED_DYE,1),item(Items.FLOWER_BANNER_PATTERN,1))));
    var costs=plan.allocate(List.of(banner(),item(Items.RED_DYE,2),item(Items.FLOWER_BANNER_PATTERN,1)));
    assertNotNull(costs);assertEquals(3,costs.length);for(int slot:costs)assertNotEquals(2,slot);
  }
  @Test public void personalCraftingOnlyHasShapelessPatterns() {
    assertTrue(group(1,false).isEmpty());assertTrue(group(2,false).isEmpty());assertTrue(group(3,false).isEmpty());
    assertEquals(2,group(4,false).size());assertEquals(4,group(5,false).size());
    assertNull(Ps4BannerCrafting.plan(banner(),List.of(new Layer("cr",0)),false));
    assertNotNull(Ps4BannerCrafting.plan(banner(),List.of(new Layer("cre",0)),false));
  }
  @Test public void reservesAllDyesAcrossMultipleLayers() {
    var plan=Ps4BannerCrafting.plan(banner(),List.of(new Layer("cr",14),new Layer("cr",14)),true);
    assertNotNull(plan);assertNull(plan.allocate(List.of(banner(),item(Items.RED_DYE,9))));
    int[] allocated=plan.allocate(List.of(banner(),item(Items.RED_DYE,10)));assertNotNull(allocated);assertEquals(11,allocated.length);
    assertEquals(2,count(plan.output));assertEquals(0,count(banner()));
  }
  @Test public void existingBannerNbtAndNameArePreservedWithoutMutation() {
    ItemStack old=apply(banner(),get("bs"),1);old.setHoverName(new TextComponent("Named banner"));
    var plan=Ps4BannerCrafting.plan(old,List.of(new Layer("bo",2)),true);
    assertEquals(2,count(plan.output));assertEquals(1,count(old));assertEquals("Named banner",plan.output.getHoverName().getString());
    assertNull(plan.allocate(List.of(banner(),item(Items.MAGENTA_DYE,8))));
    assertNotNull(plan.allocate(List.of(old,item(Items.MAGENTA_DYE,8))));
  }
  @Test public void sixLayerLimitIncludesExistingPatterns() {
    ItemStack old=banner();for(int i=0;i<5;i++)old=apply(old,get("bs"),i);
    assertNotNull(Ps4BannerCrafting.plan(old,List.of(new Layer("bo",1)),true));
    assertNull(Ps4BannerCrafting.plan(old,List.of(new Layer("bo",1),new Layer("cr",1)),true));
    assertTrue(apply(apply(old,get("bs"),1),get("bs"),1).isEmpty());
  }
  @Test public void rejectsUnknownPatternsColoursAndDyeSpoofing() {
    for(var layer:List.of(new Layer("unknown",0),new Layer("cr",-1),new Layer("cr",16),new Layer("cr",0,"minecraft:diamond"),new Layer("cr",0,"minecraft:red_dye")))
      assertNull(Ps4BannerCrafting.plan(banner(),List.of(layer),true));
    assertNull(Ps4BannerCrafting.plan(item(Items.STICK,1),List.of(new Layer("cr",0)),true));
    assertNull(Ps4BannerCrafting.plan(banner(),List.of(),true));
  }
  @Test public void specialPatternReservesOriginalMaterial() {
    var plan=Ps4BannerCrafting.plan(banner(),List.of(new Layer("moj",14)),false);
    assertNull(plan.allocate(List.of(banner(),item(Items.RED_DYE,1),item(Items.GOLDEN_APPLE,1))));
    assertNotNull(plan.allocate(List.of(banner(),item(Items.RED_DYE,1),item(Items.ENCHANTED_GOLDEN_APPLE,1))));
  }
  @Test public void colourOrderContainsSixteenModernDyes() {
    ItemStack[] choices=Ps4Dyes.choices();assertEquals(16,choices.length);
    assertEquals(Items.WHITE_DYE,choices[0].getItem());assertEquals(Items.ORANGE_DYE,choices[1].getItem());
    assertEquals(Items.BLACK_DYE,choices[15].getItem());
    ItemStack[] banners=Ps4Dyes.bannerChoices();assertEquals(16,banners.length);
    assertEquals(Items.BLACK_DYE,banners[0].getItem());assertEquals(Items.WHITE_DYE,banners[15].getItem());
    assertEquals(16,Arrays.stream(choices).map(ItemStack::getItem).distinct().count());
    assertEquals(16,Arrays.stream(banners).map(ItemStack::getItem).distinct().count());
    for(ItemStack dye:choices)assertTrue(dye.getItem() instanceof DyeItem);
    for(ItemStack dye:banners)assertTrue(dye.getItem() instanceof DyeItem);
    var plan=Ps4BannerCrafting.plan(banner(),List.of(new Layer("mc",11,"minecraft:blue_dye")),true);
    assertNotNull(plan.allocate(List.of(banner(),item(Items.BLUE_DYE,1))));
    assertNull(plan.allocate(List.of(banner(),item(Items.LAPIS_LAZULI,1))));
  }
  @Test public void oldMaterialsCannotBeSentAsBannerDyes() {
    String[] materials={"bone_meal","lapis_lazuli","cocoa_beans","ink_sac"};int[] colors={0,11,12,15};
    for(int i=0;i<materials.length;i++) {
      var plan=Ps4BannerCrafting.plan(banner(),List.of(new Layer("mc",colors[i],"minecraft:"+materials[i])),true);
      assertNull(materials[i],plan);
    }
  }
  @Test public void oldMaterialsCannotRecolorItemsOrFireworks() {
    for(Item material:new Item[]{Items.BONE_MEAL,Items.LAPIS_LAZULI,Items.COCOA_BEANS,Items.INK_SAC}) {
      assertNull(Ps4Dyes.color(new ItemStack(material)));
      assertNull(DynamicRecipes.woolDyeRecipe(grid(Items.RED_WOOL,material)));
      assertNull(DynamicRecipes.bedDyeRecipe(grid(Items.RED_BED,material)));
      assertFalse(new FireworkStarRecipe(new ResourceLocation("test:star")).matches(grid(Items.GUNPOWDER,material),null));
      ItemStack star=new FireworkStarRecipe(new ResourceLocation("test:star")).assemble(grid(Items.GUNPOWDER,Items.RED_DYE));
      CraftingContainer fade=grid(material);fade.setItem(1,star);
      assertFalse(new FireworkStarFadeRecipe(new ResourceLocation("test:fade")).matches(fade,null));
    }
  }
  @Test public void recolorsWoolAndBedWithModernDyesAndKeepsNbt() {
    CraftingContainer grid=grid(Items.RED_WOOL,Items.BLUE_DYE);grid.getItem(0).setHoverName(new TextComponent("Test"));
    var recipe=DynamicRecipes.woolDyeRecipe(grid);assertNotNull(recipe);assertTrue(recipe.matches(grid,null));
    assertEquals(Items.BLUE_WOOL,recipe.assemble(grid).getItem());assertEquals("Test",recipe.assemble(grid).getHoverName().getString());
    grid=grid(Items.RED_BED,Items.WHITE_DYE);assertEquals(Items.WHITE_BED,DynamicRecipes.bedDyeRecipe(grid).assemble(grid).getItem());
  }
  @Test public void sameColourItemDoesNotWasteDye() {
    for(Item item:new Item[]{Items.RED_BED,Items.RED_WOOL,Items.RED_SHULKER_BOX}) {
      assertTrue(DynamicRecipes.sameColorItem(grid(item,Items.RED_DYE)));
      assertFalse(DynamicRecipes.sameColorItem(grid(item,Items.BLUE_DYE)));
    }
    assertFalse(DynamicRecipes.sameColorItem(grid(Items.SHULKER_BOX,Items.RED_DYE)));
  }
  @Test public void recoloringRejectsExtraItemsAndDuplicateBases() {
    assertNull(DynamicRecipes.woolDyeRecipe(grid(Items.RED_WOOL,Items.BLUE_DYE,Items.STICK)));
    assertNull(DynamicRecipes.woolDyeRecipe(grid(Items.RED_WOOL,Items.RED_WOOL,Items.BLUE_DYE)));
    assertNull(DynamicRecipes.bedDyeRecipe(grid(Items.RED_BED,Items.RED_DYE)));
  }
  @Test public void fireworkStarRetainsShapeColourAndEffects() {
    CraftingContainer grid=grid(Items.GUNPOWDER,Items.BLUE_DYE,Items.RED_DYE,Items.FEATHER,Items.GLOWSTONE_DUST,Items.DIAMOND);
    var recipe=new FireworkStarRecipe(new ResourceLocation("test:star"));
    assertTrue(recipe.matches(grid,null));var explosion=recipe.assemble(grid).getTagElement("Explosion");
    assertEquals(4,explosion.getByte("Type"));assertTrue(explosion.getBoolean("Flicker"));assertTrue(explosion.getBoolean("Trail"));
    assertArrayEquals(new int[]{DyeColor.BLUE.getFireworkColor(),DyeColor.RED.getFireworkColor()},explosion.getIntArray("Colors"));
    grid.setItem(6,item(Items.FIRE_CHARGE,1));assertFalse(recipe.matches(grid,null));
  }
  @Test public void fadingStarPreservesExistingExplosion() {
    CraftingContainer grid=grid(Items.GUNPOWDER,Items.RED_DYE,Items.DIAMOND);
    ItemStack star=new FireworkStarRecipe(new ResourceLocation("test:star")).assemble(grid);
    grid=grid(Items.WHITE_DYE);grid.setItem(1,star);
    var recipe=new FireworkStarFadeRecipe(new ResourceLocation("test:fade"));
    assertTrue(recipe.matches(grid,null));var result=recipe.assemble(grid).getTagElement("Explosion");
    assertTrue(result.getBoolean("Trail"));assertArrayEquals(new int[]{DyeColor.RED.getFireworkColor()},result.getIntArray("Colors"));
    assertArrayEquals(new int[]{DyeColor.WHITE.getFireworkColor()},result.getIntArray("FadeColors"));
  }
  @Test public void rocketsRetainFlightAndAllExplosions() {
    var starRecipe=new FireworkStarRecipe(new ResourceLocation("test:star"));
    ItemStack red=starRecipe.assemble(grid(Items.GUNPOWDER,Items.RED_DYE));
    ItemStack blue=starRecipe.assemble(grid(Items.GUNPOWDER,Items.BLUE_DYE));
    CraftingContainer grid=grid(Items.PAPER,Items.GUNPOWDER,Items.GUNPOWDER,Items.GUNPOWDER);grid.setItem(4,red);grid.setItem(5,blue);
    var recipe=new FireworkRocketRecipe(new ResourceLocation("test:rocket"));assertTrue(recipe.matches(grid,null));
    var output=recipe.assemble(grid);assertEquals(3,output.getCount());assertEquals(3,output.getTagElement("Fireworks").getByte("Flight"));
    assertEquals(2,output.getTagElement("Fireworks").getList("Explosions",10).size());
    grid.setItem(6,item(Items.GUNPOWDER,1));assertFalse(recipe.matches(grid,null));
  }
}

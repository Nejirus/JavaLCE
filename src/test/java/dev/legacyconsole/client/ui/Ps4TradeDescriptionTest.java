package dev.legacyconsole.client.ui;

import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import org.junit.*;
import static org.junit.Assert.*;

public class Ps4TradeDescriptionTest {
 @BeforeClass public static void bootstrap(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
 @Test public void resultNameRetainsItsRuntimeTooltipColor(){
  var stack=new ItemStack(Items.BREAD);var description=TradeDescription.details(stack,null);
  assertFalse(description.isEmpty());assertEquals(stack.getHoverName().getString(),description.get(0).text());
  assertEquals(0xffffff,description.get(0).color());
 }
 @Test public void enchantedBookRetainsNameRarityAndStoredEnchantmentText(){
  var stack=EnchantedBookItem.createForEnchantment(new EnchantmentInstance(Enchantments.SHARPNESS,3));
  var source=stack.getTooltipLines(null,TooltipFlag.Default.NORMAL);var description=TradeDescription.details(stack,null);
  assertEquals(source.get(0).getString(),description.get(0).text());
  assertEquals(source.get(0).getStyle().getColor().getValue(),description.get(0).color());
  assertTrue(description.stream().anyMatch(line->line.text().contains("Sharpness III")));
 }
}

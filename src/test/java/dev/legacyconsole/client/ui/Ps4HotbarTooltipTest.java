package dev.legacyconsole.client.ui;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.junit.Test;
import static org.junit.Assert.*;

public class Ps4HotbarTooltipTest {
  private Component modifier(String kind, String amount, String attribute) {
    return new TextComponent(" ").append(new TranslatableComponent("attribute.modifier."+kind,
        amount,new TranslatableComponent("attribute.name.generic."+attribute)));
  }

  @Test public void keepsPs4DamageWhileRemovingJavaSlotHeadingsAndAttackSpeed() {
    var lines=Ps4HotbarTooltip.lines(List.of(new TextComponent("Diamond Sword"),
        new TextComponent(""),new TranslatableComponent("item.modifiers.mainhand"),
        modifier("equals.0","7","attack_damage"),modifier("equals.0","1.6","attack_speed")));
    assertEquals(2,lines.size());
    assertTrue(lines.get(1).getString().startsWith("+7.0 "));
    assertEquals(ChatFormatting.BLUE.getColor(),lines.get(1).getStyle().getColor().getValue(),0);
  }

  @Test public void keepsArmorButOmitsToughnessAndLuckAsInTheNativeAttributeWhitelist() {
    var lines=Ps4HotbarTooltip.lines(List.of(modifier("plus.0","8","armor"),
        modifier("plus.0","2","armor_toughness"),modifier("plus.0","1","luck")));
    assertEquals(1,lines.size());assertTrue(lines.get(0).getString().startsWith("+8.0 "));
  }

  @Test public void translatesSignedDecimalsAndIntegerPercentages() {
    var lines=Ps4HotbarTooltip.lines(List.of(modifier("take.0","2.5","movement_speed"),
        modifier("plus.1","12.5","max_health")));
    assertTrue(lines.get(0).getString().startsWith("-2.5 "));
    assertEquals(ChatFormatting.RED.getColor(),lines.get(0).getStyle().getColor().getValue(),0);
    assertTrue(lines.get(1).getString().startsWith("+12% "));
  }

  @Test public void preservesLoreAndEnchantmentComponentsWithoutTextMatching() {
    var lore=new TextComponent("When in Main Hand: a custom name").withStyle(ChatFormatting.ITALIC);
    var enchantment=new TranslatableComponent("enchantment.minecraft.sharpness").withStyle(ChatFormatting.GRAY);
    var result=Ps4HotbarTooltip.lines(List.of(lore,enchantment));
    assertSame(lore,result.get(0));assertSame(enchantment,result.get(1));
  }

  @Test public void replacesTheFifthEntryWithEllipsisAtTheNativeThreshold() {
    var result=Ps4HotbarTooltip.lines(List.of(new TextComponent("Name"),new TextComponent("One"),
        new TextComponent("Two"),new TextComponent("Three"),new TextComponent("Four")));
    assertEquals(5,result.size());assertEquals("...",result.get(4).getString());
  }
}

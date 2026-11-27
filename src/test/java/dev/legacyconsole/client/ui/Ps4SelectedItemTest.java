package dev.legacyconsole.client.ui;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.*;
import static org.junit.Assert.*;

public class Ps4SelectedItemTest {
  @BeforeClass public static void bootstrap(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
  @Test public void repeatedMiningWearDoesNotRestartSelectedInformation(){
    var previous=new ItemStack(Items.DIAMOND_PICKAXE);previous.enchant(Enchantments.BLOCK_EFFICIENCY,5);
    var current=previous.copy();
    for(int damage=1;damage<100;damage++){
      current.setDamageValue(damage);assertFalse(Ps4SelectedItem.changed(0,0,current,previous));
    }
    assertTrue(Ps4SelectedItem.changed(1,0,current,previous));
  }
  @Test public void selectionAndEnchantmentChangesStillShowThePopup(){
    var previous=new ItemStack(Items.DIAMOND_PICKAXE);var current=previous.copy();
    current.enchant(Enchantments.BLOCK_EFFICIENCY,3);assertTrue(Ps4SelectedItem.changed(0,0,current,previous));
    current=previous.copy();current.setHoverName(new TextComponent("Named pickaxe"));assertTrue(Ps4SelectedItem.changed(0,0,current,previous));
    assertTrue(Ps4SelectedItem.changed(0,0,new ItemStack(Items.IRON_PICKAXE),previous));
    assertTrue(Ps4SelectedItem.changed(0,0,ItemStack.EMPTY,previous));
    previous=new ItemStack(Items.COBBLESTONE,1);current=new ItemStack(Items.COBBLESTONE,64);
    assertFalse(Ps4SelectedItem.changed(0,0,current,previous));
  }
}

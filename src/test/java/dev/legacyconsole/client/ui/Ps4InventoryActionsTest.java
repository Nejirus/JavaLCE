package dev.legacyconsole.client.ui;

import java.util.*;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import org.junit.*;
import static org.junit.Assert.*;

public class Ps4InventoryActionsTest {
 @BeforeClass public static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
 private List<String> actions(int count,ItemStack carried){var items=new SimpleContainer(1);if(count>0)items.setItem(0,new ItemStack(Items.STONE,count));return Arrays.asList(Ps4InventoryActions.prompts(new Slot(items,0,0,0),carried,null));}
 @Test public void emptySlotsAndOutsideHaveOnlyBack(){assertEquals(List.of("circle","Back"),actions(0,ItemStack.EMPTY));assertArrayEquals(new String[]{"circle","Back"},Ps4InventoryActions.prompts(null,ItemStack.EMPTY,null));}
 @Test public void splitRequiresTwoOrMoreItems(){
  for(int count:new int[]{1,2,64}){var a=actions(count,ItemStack.EMPTY);assertTrue(a.containsAll(List.of("Take","Quick Move","What's This?")));assertEquals(count>1,a.contains("Split"));}
 }
 @Test public void carryingUsesPlacementInsteadOfTakeOrSplit(){
  var empty=actions(0,new ItemStack(Items.STONE,4));assertTrue(empty.containsAll(List.of("Place","Place One")));assertFalse(empty.contains("Quick Move"));assertFalse(empty.contains("What's This?"));
  var full=actions(64,new ItemStack(Items.STONE,4));assertFalse(full.contains("Place"));assertFalse(full.contains("Split"));assertFalse(full.contains("Take"));
  assertTrue(actions(1,new ItemStack(Items.DIRT)).contains("Swap"));assertFalse(actions(0,new ItemStack(Items.STONE)).contains("Place One"));
 }
}

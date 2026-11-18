package dev.legacyconsole.client.ui;

import static org.junit.Assert.*;
import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.TextComponent;
import org.junit.*;

public class LceCreativeHotbarTest {
  @BeforeClass public static void bootstrap() {
    net.minecraft.SharedConstants.tryDetectVersion(); net.minecraft.server.Bootstrap.bootStrap();
  }
  private List<ItemStack> bar() { return new ArrayList<>(Collections.nCopies(9, ItemStack.EMPTY)); }
  @Test public void crossTakesOneRegardlessOfCatalogDisplayCount() {
    List<ItemStack> bar = bar(); ItemStack icon = new ItemStack(Items.STONE, 64);
    assertEquals(0, LceCreativeHotbar.take(bar, icon, false, 8));
    assertEquals(1, bar.get(0).getCount()); assertEquals(64, icon.getCount());
  }
  @Test public void triangleUsesItemStackLimitAndFindsRoomForWholeStack() {
    List<ItemStack> bar = bar(); bar.set(0, new ItemStack(Items.STONE, 2));
    assertEquals(1, LceCreativeHotbar.take(bar, new ItemStack(Items.STONE), true, 0));
    assertEquals(64, bar.get(1).getCount()); assertEquals(2, bar.get(0).getCount());
    assertEquals(2, LceCreativeHotbar.take(bar, new ItemStack(Items.SNOWBALL), true, 0));
    assertEquals(16, bar.get(2).getCount());
  }
  @Test public void oneItemMergesBeforeUsingEarlierEmptySlot() {
    List<ItemStack> bar = bar(); bar.set(4, new ItemStack(Items.STONE, 63));
    assertEquals(4, LceCreativeHotbar.take(bar, new ItemStack(Items.STONE), false, 0));
    assertEquals(64, bar.get(4).getCount()); assertTrue(bar.get(0).isEmpty());
  }
  @Test public void fullBarUsesPreviousQuickSelectAndPreservesTags() {
    List<ItemStack> bar = bar(); for (int i=0;i<9;i++)bar.set(i,new ItemStack(Items.DIRT,64));
    ItemStack named=new ItemStack(Items.STONE); named.setHoverName(new TextComponent("Named"));
    assertEquals(6,LceCreativeHotbar.take(bar,named,false,6));
    assertEquals("Named",bar.get(6).getHoverName().getString()); assertEquals(1,bar.get(6).getCount());
    assertEquals(64,bar.get(5).getCount());
  }
}

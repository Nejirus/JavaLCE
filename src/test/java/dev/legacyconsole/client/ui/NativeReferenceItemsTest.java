package dev.legacyconsole.client.ui;
import static org.junit.Assert.*;
import org.junit.*;
import com.google.gson.*;
public class NativeReferenceItemsTest {
 @BeforeClass public static void nativeRegistry(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
 private net.minecraft.world.item.ItemStack item(String id,String nbt){JsonObject value=new JsonObject();value.addProperty("item",id);if(nbt!=null)value.addProperty("nbt",nbt);return ConsoleItemNames.resolve(value);}
 @Test public void futureBlocksDoNotBecomeAirOrMissingModels(){assertTrue(item("minecraft:cherry_planks",null).isEmpty());assertFalse(item("minecraft:deepslate",null).isEmpty());assertFalse(item("minecraft:copper_block",null).isEmpty());}
 @Test public void futureEnchantmentsAreNotBlankBooks(){assertTrue(item("minecraft:enchanted_book","{StoredEnchantments:[{id:'minecraft:swift_sneak',lvl:3s}]}").isEmpty());assertFalse(item("minecraft:enchanted_book","{StoredEnchantments:[{id:'minecraft:mending',lvl:1s}]}").isEmpty());}
 @Test public void futurePotionEffectsAreNotUncraftablePotions(){assertTrue(item("minecraft:potion","{Potion:'minecraft:wind_charged'}").isEmpty());assertFalse(item("minecraft:potion","{Potion:'minecraft:long_swiftness'}").isEmpty());}
}

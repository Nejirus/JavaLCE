package dev.legacyconsole.port;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.item.trading.Merchant;
public class GuiMerchant extends GuiContainer {
 public GuiMerchant(MerchantScreen source){super(source.getMenu());}
 public Merchant getMerchant(){return dev.legacyconsole.crafting.NativeTrading.merchant((net.minecraft.world.inventory.MerchantMenu)inventorySlots);}
}

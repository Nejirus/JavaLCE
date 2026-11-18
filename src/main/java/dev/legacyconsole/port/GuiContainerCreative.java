package dev.legacyconsole.port;
import net.minecraft.client.Minecraft;import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.entity.player.Player;import net.minecraft.world.inventory.*;import net.minecraft.world.item.CreativeModeTab;
public final class GuiContainerCreative extends CreativeModeInventoryScreen {
 public final AbstractContainerMenu inventorySlots;public ContainerListener listener;
 public GuiContainerCreative(Player player){super(player);inventorySlots=getMenu();}
 public void setWorldAndResolution(Minecraft mc,int width,int height){init(mc,width,height);for(Class<?> type=CreativeModeInventoryScreen.class;type!=null;type=type.getSuperclass())for(var f:type.getDeclaredFields())if(ContainerListener.class.isAssignableFrom(f.getType()))try{f.setAccessible(true);listener=(ContainerListener)f.get(this);}catch(IllegalAccessException e){throw new IllegalStateException(e);}}
 private void setCurrentCreativeTab(CreativeModeTab tab){selectTab(tab);}
 private void handleMouseClick(Slot slot,int id,int button,ClickType type){var actual=Minecraft.getInstance().player.containerMenu;getMenu().setCarried(actual.getCarried());super.slotClicked(slot,id,button,type);actual.setCarried(getMenu().getCarried());}
 public void onGuiClosed(){super.removed();}
}

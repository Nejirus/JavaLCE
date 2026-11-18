package dev.legacyconsole.client.ui;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.inventory.ChestMenu;

/** Original ChestMenu1080 / ChestLargeMenu1080 with Java's synchronized chest transactions. */
public final class Ps4ChestScreen extends Ps4ContainerScreen {
  private final int rows;
  private final String title;
  public Ps4ChestScreen(AbstractContainerScreen<?> original) {
    super(original,((ChestMenu)original.getMenu()).getRowCount()==6?"ChestLargeMenu":"ChestMenu");
    rows=((ChestMenu)inventorySlots).getRowCount();title=original.getTitle().getString();
    if(rows!=3&&rows!=6)throw new IllegalArgumentException("PS4 chest scenes have three or six rows");
  }
  @Override protected void layoutControls() {
    for(int i=0;i<rows*9;i++)place(i,art.cell("containerList",i%9,i/9,9,rows));
    navigationGrid(2,"containerList",9,rows);
  }
  @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my) {
    art.backgrounds();art.label(title,"chestLabel");art.label(I18n.get("container.inventory"),"inventoryLabel");
    drawHolders(mx,my);
  }
  @Override protected String[] actionPrompts() {
    return Ps4InventoryActions.prompts(getSlotUnderMouse(),inventorySlots.getCarried(),mc.player);
  }
}

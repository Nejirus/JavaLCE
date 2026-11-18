package dev.legacyconsole.client.ui;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.inventory.BrewingStandMenu;

/** PS4 BrewingStandMenu1080.fui and UIScene_BrewingStandMenu progress logic. */
public final class ConsoleBrewingScreen extends Ps4ContainerScreen {
  private final BrewingStandMenu stand;

  public ConsoleBrewingScreen(AbstractContainerScreen<?> original) {
    super(original,"BrewingStandMenu");
    stand=(BrewingStandMenu)inventorySlots;
  }
  @Override protected void layoutControls() {
    place(0,"Bottle1"); place(1,"Bottle2"); place(2,"Bottle3");
    place(3,"Ingredient"); place(4,"BrewingPlate");
    for(int i=0;i<5;i++)navigationSlot(0x2b+i,i);
  }
  @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my) {
    art.backgrounds();
    art.label(I18n.get("container.brewing"),"BrewingStandText");
    art.label(I18n.get("container.inventory"),"inventoryLabel");
    drawHolders(mx,my);
    if(!inventorySlots.getSlot(4).hasItem())art.emptyIcon("BrewingBlazePowderIcon");
    art.progress("BrewingArrow", "BrewingArrow_Off", "BrewingArrow_On",
        Ps4MenuLogic.brewingArrowFill(Ps4MenuLogic.brewingArrow(stand.getBrewingTicks())),false,false);
    art.progress("BrewingBubbles", "BrewingBubbles_Off", "BrewingBubbles_On",
        Ps4MenuLogic.progress(Ps4MenuLogic.brewingBubbles(stand.getBrewingTicks()),0,30),false);
    art.progress("BrewingPlateBar", "BrewingPlateEmpty", "BrewingPlateFull",
        Ps4MenuLogic.brewingFuel(stand.getFuel()),true);
  }
}

package dev.legacyconsole.client.ui;

import dev.legacyconsole.port.MenuData;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;

/** UIScene_FurnaceMenu, 0xa02ed0/0xa406b0; native Java smelting transactions. */
public final class Ps4FurnaceScreen extends Ps4ContainerScreen {
 private final MenuData values;
 private final String caption;
 public Ps4FurnaceScreen(AbstractContainerScreen<?> source){
  super(source,"FurnaceMenu");values=new MenuData(inventorySlots);caption=source.getTitle().getString();
 }
 @Override protected void layoutControls(){
  place(0,"Ingredient");place(1,"Fuel");place(2,"Result");
  navigationSlot(6,0);navigationSlot(7,1);navigationSlot(8,2);
 }
 static float fraction(int value,int maximum){return maximum<=0?0:Math.max(0,Math.min(1,value/(float)maximum));}
 @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my){
  art.backgrounds();art.label(caption,"Furnace_text");art.label(I18n.get("container.inventory"),"inventoryLabel");
  art.label(I18n.get("legacyconsole.ps4.ingredient"),"Ingredient_Label");art.label(I18n.get("legacyconsole.ps4.fuel"),"Fuel_Label");
  art.progress("FurnaceArrow","Arrow_Off","Arrow_On",fraction(values.getField(2),values.getField(3)),true);
  art.progress("FurnaceFire","Flame_Off","Flame_On",fraction(values.getField(0),values.getField(1)),false);
  drawHolders(mx,my);
 }
}

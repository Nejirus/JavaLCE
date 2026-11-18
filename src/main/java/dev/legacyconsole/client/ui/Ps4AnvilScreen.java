package dev.legacyconsole.client.ui;

import dev.legacyconsole.port.GuiButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;

/** UIScene_AnvilMenu, 0xa036f0/0xa0c970/0xa0dbc0 and original AnvilMenu1080. */
public final class Ps4AnvilScreen extends Ps4ContainerScreen {
 public static final int NAME_LIMIT=31;
 private String name="",signature="";
 public Ps4AnvilScreen(AbstractContainerScreen<?> source){super(source,"AnvilMenu");}
 @Override protected void layoutControls(){
  place(0,"Ingredient");place(1,"Ingredient2");place(2,"Result");
  navigationSlot(0x33,0);navigationSlot(0x34,1);navigationSlot(0x35,2);
  var r=art.control("AnvilTextInput",360,54);navigationButton(0x36,r);buttonList.add(new NameInput(r));updateScreen();
 }
 @Override public void updateScreen(){
  ItemStack input=inventorySlots.getSlot(0).getItem();String current=input.save(new net.minecraft.nbt.CompoundTag()).toString();
  if(!current.equals(signature)){
   signature=current;name=input.isEmpty()?"":input.getHoverName().getString();
   if(!input.isEmpty()){
    String initial=input.hasCustomHoverName()?name:"";
    ((AnvilMenu)inventorySlots).setItemName(initial);
    mc.player.connection.send(new ServerboundRenameItemPacket(initial));
   }
  }
  if(!buttonList.isEmpty())buttonList.get(0).enabled=!input.isEmpty()&&inventorySlots.getCarried().isEmpty();
 }
 @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my){
  art.backgrounds();art.label(I18n.get("container.repair"),"AnvilText");art.label(I18n.get("container.inventory"),"inventoryLabel");
  boolean result=inventorySlots.getSlot(2).hasItem();
  if(!result&&(inventorySlots.getSlot(0).hasItem()||inventorySlots.getSlot(1).hasItem()))art.object("AnvilCross");
  int cost=((AnvilMenu)inventorySlots).getCost();
  if(cost>0){boolean expensive=cost>=40&&!mc.player.getAbilities().instabuild;
   if(expensive||result){boolean can=mc.player.getAbilities().instabuild||mc.player.experienceLevel>=cost;
    String text=expensive?Ps4UiArt.text("db7b95e9"):String.format(Ps4UiArt.text("f3a668fd"),cost);
    String control=!expensive&&can?"costLabelGreen":"costLabelRed";
    art.coloredLabel(text,control,!expensive&&can?"FJ_Label_Green":"FJ_Label_Red");
   }
  }
  drawHolders(mx,my);
 }
 @Override protected void actionPerformed(GuiButton b){
  if(!b.enabled)return;
  mc.setScreen(new ConsoleKeyboard(this,I18n.get("container.repair"),name,value->{
   name=value;ItemStack input=inventorySlots.getSlot(0).getItem();
   String rename=!input.hasCustomHoverName()&&name.equals(input.getHoverName().getString())?"":name;
   ((AnvilMenu)inventorySlots).setItemName(rename);mc.player.connection.send(new ServerboundRenameItemPacket(rename));
  },NAME_LIMIT,value->true));
 }
 private final class NameInput extends GuiButton {
  private final Ps4UiArt.Rect bounds;
  NameInput(Ps4UiArt.Rect r){super(0,(int)r.x(),(int)r.y(),(int)Math.ceil(r.w()),(int)Math.ceil(r.h()),"");bounds=r;}
  @Override public void drawButton(net.minecraft.client.Minecraft game,int mx,int my){
   hovered=enabled&&bounds.contains(mx,my);float[] m=art.matrix("AnvilTextInput");
   art.image(hovered?"TextInputSelectedBorder":"TextInputBGBorder",art.rect(m[4]-3,m[5]-3,462*.7922058f,54*1.111114f));
   art.image("TextInputBGBorder",art.rect(m[4],m[5],456*.789505f,48*1.125f));
   art.image("TextInputBG",art.rect(m[4]+3,m[5]+3,450*.78665f,42*1.14285f));
   // Keep the original input artwork; center the white name within its inner face.
   art.text(name,art.rect(m[4]+3,m[5]+(54-24)/2f,450*.78665f,24),0xffffffff,true,3);
  }
 }
}

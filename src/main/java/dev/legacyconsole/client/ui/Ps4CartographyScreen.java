package dev.legacyconsole.client.ui;

import dev.legacyconsole.port.GuiButton;
import dev.legacyconsole.port.RenameMapMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** PS4 cartography scene; map item/NBT operations stay on the authoritative Java server. */
public final class Ps4CartographyScreen extends Ps4ContainerScreen {
  private String mapName="", inputSignature="";
  public Ps4CartographyScreen(AbstractContainerScreen<?> original) {
    super(original,"CartographyMenu");
  }
  @Override protected void layoutControls() {
    place(0,"Ingredient"); place(1,"Ingredient2"); place(2,"Result");
    buttonList.add(new NameInput());
    for(int i=0;i<3;i++)navigationSlot(0x5b+i,i);
    navigationButton(0x5e,art.control("MapTextInput",360,54));
    updateScreen();
  }
  @Override public void updateScreen() {
    ItemStack input=inventorySlots.getSlot(0).getItem();
    String signature=input.save(new net.minecraft.nbt.CompoundTag()).toString();
    if(!signature.equals(inputSignature)) {
      inputSignature=signature;
      mapName=input.isEmpty()?"":input.getHoverName().getString();
      if(input.is(Items.FILLED_MAP)||input.is(Items.MAP)) {
        String name=input.hasCustomHoverName()?mapName:"";
        ((RenameMapMenu)inventorySlots).legacy$setMapName(name);
        mc.player.connection.send(new ServerboundRenameItemPacket(name));
      }
    }
    if(!buttonList.isEmpty())buttonList.get(0).enabled=input.is(Items.FILLED_MAP)||input.is(Items.MAP);
  }
  @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my) {
    art.backgrounds();
    art.label(I18n.get("container.cartography_table"),"TitleText");
    art.label(I18n.get("container.inventory"),"inventoryLabel");
    art.label(Ps4UiArt.text("dcd3c648"),"MapNameText");
    ItemStack input=inventorySlots.getSlot(0).getItem(), second=inventorySlots.getSlot(1).getItem();
    int mode=Ps4MenuLogic.cartographyMode(input.is(Items.FILLED_MAP),second.is(Items.MAP),
        second.is(Items.GLASS_PANE),second.is(Items.PAPER),second.isEmpty(),inventorySlots.getSlot(2).hasItem());
    String control=switch(mode){case 1,5->"Map_Map";case 2->"Map_Copy";case 3->"Map_Locked";
      case 4->"Map_Zoom";default->"Map_Empty";};
    String sprite=switch(mode){case 1,5->"cartography_table_map";case 2->"cartography_table_copy";
      case 3->"cartography_table_glass";case 4->"cartography_table_zoom";default->"cartography_table_empty";};
    art.image(sprite,art.control(control,66,66));
    String status=switch(mode){case 1->"a77592e8";case 2->"a89a0b2f";case 3->"2d955f51";
      case 4->"7a2ef634";case 5->"cc04e002";default->null;};
    if(status!=null)art.label(Ps4UiArt.text(status),"MapStatusText");
    drawHolders(mx,my);
  }
  @Override protected void actionPerformed(GuiButton button) {
    if(!button.enabled)return;
    mc.setScreen(new ConsoleKeyboard(this,I18n.get("legacyconsole.ps4.map_name"),mapName,value->{
      mapName=value;
      ItemStack input=inventorySlots.getSlot(0).getItem();
      String name=!input.hasCustomHoverName()&&value.equals(input.getHoverName().getString())?"":value;
      ((RenameMapMenu)inventorySlots).legacy$setMapName(name);
      mc.player.connection.send(new ServerboundRenameItemPacket(name));
    },Ps4MenuLogic.MAP_NAME_LIMIT,value->true));
  }
  private final class NameInput extends GuiButton {
    private final Ps4UiArt.Rect bounds;
    NameInput(){this(art.control("MapTextInput",360,54));}
    private NameInput(Ps4UiArt.Rect r){super(0,(int)r.x(),(int)r.y(),(int)Math.ceil(r.w()),
        (int)Math.ceil(r.h()),"");bounds=r;}
    @Override public void drawButton(net.minecraft.client.Minecraft game,int mx,int my){
      hovered=enabled&&bounds.contains(mx,my);
      float[] m=art.matrix("MapTextInput");
      // FJ_TextInput_Normal display list, skinHDInGame.fui: original border and face transforms.
      art.image(hovered?"TextInputSelectedBorder":"TextInputBGBorder",
          art.rect(m[4]-3,m[5]-3,462*.7922058f,54*1.111114f));
      art.image("TextInputBGBorder",art.rect(m[4],m[5],456*.789505f,48*1.125f));
      art.image("TextInputBG",art.rect(m[4]+3,m[5]+3,450*.78665f,42*1.14285f));
      art.textAt(mapName,m[4]+3,m[5]+3,450*.78665f,enabled?ConsoleTheme.DARK:ConsoleTheme.MUTED,false);
    }
  }
}

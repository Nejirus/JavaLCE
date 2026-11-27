package dev.legacyconsole.client.ui;

import dev.legacyconsole.port.Keyboard;
import java.io.IOException;
import java.util.ArrayList;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

/** mcps4 InventoryMenu1080 and 0x81e150's console-crafting layout. */
public final class ConsoleInventory extends Ps4ContainerScreen {
  private final int[][] original;
  private float equipmentOffset;

  public ConsoleInventory(Player player) {
    super(player.inventoryMenu,"InventoryMenu");
    original=new int[inventorySlots.slots.size()][2];
    for(int i=0;i<original.length;i++) {
      Slot slot=inventorySlots.getSlot(i);original[i][0]=slot.x;original[i][1]=slot.y;
    }
  }
  @Override protected void layoutControls() {
    float[] armor=art.matrix("armorList"),player=art.matrix("iggy_player");
    equipmentOffset=Ps4ContainerLogic.equipmentOffset(armor[4],50*armor[0],player[4],
        art.scene.player_bounds[1]-art.scene.player_bounds[0],art.panelBounds().w()/art.scale);
    var cells=new ArrayList<Ps4UiArt.Rect>();
    for(int i=0;i<4;i++) {
      var cell=shift(art.cell("armorList",0,i,1,4));place(5+i,cell);cells.add(cell);
    }
    navigationGrid(12,1,cells);
    var shield=shift(art.control("shieldList"));place(45,shield);
    navigationSlot(13,45);
    // Native console crafting hides the manual grid/result; C retains the
    // existing console/creative crafting route.
  }
  private Ps4UiArt.Rect shift(Ps4UiArt.Rect r) {
    return new Ps4UiArt.Rect(r.x()+equipmentOffset*art.scale,r.y(),r.w(),r.h());
  }
  @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my) {
    art.backgrounds();art.label(I18n.get("container.inventory"),"inventoryLabel");
    art.object("PlayerBackground",equipmentOffset);
    drawHolders(mx,my);
    String[] icons={"Head","Body","Legs","Feet"};
    for(int i=0;i<4;i++)if(!inventorySlots.getSlot(5+i).hasItem())
      art.emptyEquipment("Armour_Slot_"+icons[i],equipmentOffset);
    if(!inventorySlots.getSlot(45).hasItem())art.emptyEquipment("Armour_Slot_Shield",equipmentOffset);
    float[] p=art.matrix("iggy_player"),b=art.scene.player_bounds;
    var r=shift(art.rect(p[4]+b[0],p[5]+b[2],b[1]-b[0],b[3]-b[2]));
    // Java's entity renderer adapts the mesh/lighting; viewport, scale and foot
    // anchor are from UIScene's original player draw at 0x9f6f30.
    InventoryScreen.renderEntityInInventory(Math.round(r.x()+r.w()/2),
        Math.round(r.y()+Ps4ContainerLogic.playerFoot(r.h())),Math.round(Ps4ContainerLogic.playerScale(r.w())),
        r.x()+r.w()/2-mx,r.y()+r.h()/2-40*art.scale-my,mc.player);
    Ps4Effects.inventory(art);
  }
  @Override protected String[] actionPrompts() {
    return Ps4InventoryActions.prompts(getSlotUnderMouse(),inventorySlots.getCarried(),mc.player);
  }
  @Override protected void keyTyped(char c,int key)throws IOException {
    if(key==Keyboard.KEY_C){mc.player.closeContainer();ConsoleScreens.craftingOrCreative();}
    else super.keyTyped(c,key);
  }
  @Override public void onGuiClosed() {
    for(int i=0;i<original.length;i++) {
      inventorySlots.getSlot(i).x=original[i][0];inventorySlots.getSlot(i).y=original[i][1];
    }
    super.onGuiClosed();
  }
}

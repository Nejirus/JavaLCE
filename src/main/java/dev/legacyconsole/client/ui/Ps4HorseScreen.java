package dev.legacyconsole.client.ui;

import java.util.ArrayList;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.Llama;

/** PS4 horse scene; Java's server-owned horse menu supplies the actual transactions. */
public final class Ps4HorseScreen extends Ps4ContainerScreen {
  private final AbstractHorse horse;
  public Ps4HorseScreen(HorseInventoryScreen original,AbstractHorse horse){
    super(original,"HorseInventoryMenu");this.horse=horse;
  }
  @Override protected void layoutControls(){
    if(horse.isSaddleable()){place(0,"SlotSaddle");navigationSlot(0x4c,0);}
    if(horse.canWearArmor()){place(1,"SlotArmor");navigationSlot(0x4b,1);}
    if(horse instanceof AbstractChestedHorse chest&&chest.hasChest()){
      int columns=chest.getInventoryColumns();var cells=new ArrayList<Ps4UiArt.Rect>();
      // 0xa47400 scales the authored five-column list by the native horse's column count / 5.
      var r=art.control("DonkeyInventoryList");
      for(int row=0;row<3;row++)for(int col=0;col<columns;col++){
        var cell=new Ps4UiArt.Rect(r.x()+col*r.w()/5,r.y()+row*r.h()/3,r.w()/5,r.h()/3);
        place(2+row*columns+col,cell);cells.add(cell);
      }
      navigationGrid(0x4a,columns,cells);
    }
  }
  @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my){
    art.backgrounds();art.label(I18n.get("container.inventory"),"inventoryLabel");
    art.label(horse.getDisplayName().getString(),"horseinventoryText");
    drawHolders(mx,my);
    if(horse.isSaddleable()&&!inventorySlots.getSlot(0).hasItem())art.emptyEquipment("Saddle_Slot_Small",0);
    if(horse.canWearArmor()&&!inventorySlots.getSlot(1).hasItem())
      art.emptyEquipment(horse instanceof Llama?"Carpet_Slot":"HorseArmor_Slot_Small",0);
    float[] p=art.matrix("iggy_horse"),b=art.scene.player_bounds;
    var r=art.rect(p[4]+b[0],p[5]+b[2],b[1]-b[0],b[3]-b[2]);
    var portrait=Ps4Riding.DATA.portrait;
    // 0x9f6ba0: horse-specific 0.71 scale and -7.5 foot divisor, then Java's mesh/lighting adapter.
    InventoryScreen.renderEntityInInventory(Math.round(r.x()+r.w()/2),
        Math.round(r.y()+r.h()+r.h()/portrait.foot_divisor),Math.round(r.w()/portrait.aspect*portrait.scale),
        r.x()+r.w()/2-mx,r.y()+r.h()/2+portrait.mouse_y*art.scale-my,horse);
  }
  @Override protected String[] actionPrompts(){
    return Ps4InventoryActions.prompts(getSlotUnderMouse(),inventorySlots.getCarried(),mc.player);
  }
  @Override protected Ps4MenuNavigation.Route horseRoute(int section,int d,int x,int y){
    return Ps4ContainerLogic.horse(section,d,x,y,false,secondaryVisible(),horse.canWearArmor(),horse.isSaddleable(),
        horse instanceof AbstractChestedHorse chest?chest.getInventoryColumns():0);
  }
  @Override protected boolean secondaryVisible(){
    return horse instanceof AbstractChestedHorse chest&&chest.hasChest();
  }
}

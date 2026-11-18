package dev.legacyconsole.client.ui;

import dev.legacyconsole.client.input.ControllerScreen;
import dev.legacyconsole.port.GlStateManager;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import dev.legacyconsole.client.input.ControllerMenus;
import dev.legacyconsole.client.input.PadState;
import dev.legacyconsole.client.ConsoleAudio;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** FUI slot lists mapped to Java's synchronized menu slots. */
abstract class Ps4ContainerScreen extends ConsoleContainerScreen implements ControllerScreen {
  private final String sceneName;
  protected Ps4UiArt art;
  protected final Map<Integer, Ps4UiArt.Rect> holders = new HashMap<>();
  private record Section(int columns,int rows,List<Ps4UiArt.Rect> cells) {}
  private final Map<Integer,Section> sections=new LinkedHashMap<>();

  Ps4ContainerScreen(AbstractContainerScreen<?> original, String scene) {
    this(original.getMenu(),scene);
  }
  Ps4ContainerScreen(AbstractContainerMenu menu,String scene) {
    super(menu);
    sceneName=scene;
  }
  @Override public void initGui() {
    art = new Ps4UiArt(sceneName, width, height);
    var panel = art.panelBounds();
    xSize = (int)Math.ceil(panel.w()); ySize = (int)Math.ceil(panel.h());
    super.initGui();
    guiLeft = (int)Math.floor(art.x); guiTop = (int)Math.floor(art.y);
    mc.player.containerMenu = inventorySlots;
    holders.clear(); buttonList.clear();sections.clear();
    for (Slot slot : inventorySlots.slots) {
      slot.x=slot.y=-10000;
      if (slot.container != mc.player.getInventory())continue;
      int index = slot.getContainerSlot();
      if(index>=36)continue;
      place(slot.index, index<9 ? art.cell("hotbarList",index,0,9,1)
          : art.cell("inventoryList",(index-9)%9,(index-9)/9,9,3));
    }
    layoutControls();
    int hotbar=switch(sceneName){case "BrewingStandMenu"->0x29;case "EnchantingMenu"->0x21;
      case "CartographyMenu"->0x59;case "BeaconMenu"->0x38;case "AnvilMenu"->0x31;case "FurnaceMenu"->4;
      case "InventoryMenu"->10;case "ChestMenu","ChestLargeMenu"->0;default->throw new IllegalStateException(sceneName);};
    for(int i=0;i<2;i++) {
      List<Ps4UiArt.Rect> cells=new ArrayList<>();
      int rows=i==0?1:3;
      for(int row=0;row<rows;row++)for(int col=0;col<9;col++)cells.add(art.cell(i==0?"hotbarList":"inventoryList",col,row,9,rows));
      sections.put(hotbar+i,new Section(9,rows,cells));
    }
  }
  protected abstract void layoutControls();
  protected final void navigationGrid(int section,String control,int columns,int rows) {
    List<Ps4UiArt.Rect> cells=new ArrayList<>();
    for(int row=0;row<rows;row++)for(int column=0;column<columns;column++)cells.add(art.cell(control,column,row,columns,rows));
    sections.put(section,new Section(columns,rows,cells));
  }
  protected final void navigationGrid(int section,int columns,List<Ps4UiArt.Rect> cells) {
    sections.put(section,new Section(columns,cells.size()/columns,List.copyOf(cells)));
  }
  protected final void navigationSlot(int section,int slot) {
    var r=holders.get(slot);
    sections.put(section,new Section(1,1,List.of(new Ps4UiArt.Rect(guiLeft+r.x(),guiTop+r.y(),r.w(),r.h()))));
  }
  protected final void navigationButton(int section,Ps4UiArt.Rect bounds) {
    sections.put(section,new Section(0,0,List.of(bounds)));
  }
  protected boolean secondaryVisible(){return true;}
  protected final void place(int index, String control) { place(index,art.control(control)); }
  protected final void place(int index, Ps4UiArt.Rect r) {
    Ps4UiArt.Rect local = new Ps4UiArt.Rect(r.x()-guiLeft,r.y()-guiTop,r.w(),r.h());
    holders.put(index,local);
    Slot slot = inventorySlots.getSlot(index);
    float[] viewport=Ps4ItemGeometry.viewport(local.x(),local.y(),local.w(),local.h(),art.scale);
    slot.x = Math.round(viewport[0]); slot.y = Math.round(viewport[1]);
  }
  @Override protected int[] inventoryHighlightBounds(Slot slot) {
    var r=holders.get(slot.index);
    if(r==null)return null;
    return new int[]{(int)Math.floor(r.x()),(int)Math.floor(r.y()),
        (int)Math.ceil(r.x()+r.w())-(int)Math.floor(r.x()),
        (int)Math.ceil(r.y()+r.h())-(int)Math.floor(r.y())};
  }
  @Override protected boolean inventoryHighlightContains(Slot slot,int mx,int my) {
    var r=holders.get(slot.index); return r!=null&&r.contains(mx-guiLeft,my-guiTop);
  }
  @Override protected float[] inventoryHighlightFace(Slot slot) {
    var r=holders.get(slot.index); if(r==null)return null;
    return new float[]{r.x()+r.w()/20,r.y()+r.h()/20,r.w()*.9f,r.h()*.9f};
  }
  @Override protected float[] itemDisplayBounds(Slot slot) {
    var r=holders.get(slot.index);
    return r==null?null:Ps4ItemGeometry.viewport(r.x(),r.y(),r.w(),r.h(),art.scale);
  }
  @Override public float[] floatingItemBounds(int x,int y) {
    return Ps4ItemGeometry.carried(x+8,y+8,art.scale);
  }
  @Override protected void highlightSprite(float w,float h) {
    ConsoleTheme.spriteAt("ps4/workstations/slothighlight",0,0,w,h);
  }
  @Override public float[] controllerSlotBoundsExact(Slot slot) {
    var r=holders.get(slot.index);
    return r==null?ControllerScreen.super.controllerSlotBoundsExact(slot):
        new float[]{r.x(),r.y(),r.w(),r.h()};
  }
  protected void drawHolders(int mx,int my) {
    for (var r:holders.values()) art.image("IconHolder_1x1border",
        new Ps4UiArt.Rect(guiLeft+r.x(),guiTop+r.y(),r.w(),r.h()));
    drawInventoryHighlight(mx,my);
  }
  @Override protected void drawGuiContainerForegroundLayer(int x,int y) {}
  protected String[] actionPrompts() {
    return new String[]{"cross","Take / Select","square","Place One","triangle","Quick Move","circle","Back"};
  }
  @Override public void drawScreen(int mx,int my,float partial) {
    drawDefaultBackground(); super.drawScreen(mx,my,partial);
    ConsoleTheme.footer(width,height,actionPrompts());
  }
  @Override public boolean controllerPointer() { return true; }
  @Override public boolean controllerDirection(int direction) {
    if(sections.isEmpty())return true;
    int current=-1,cell=0;double distance=Double.POSITIVE_INFINITY;
    float px=ControllerMenus.pointerX(),py=ControllerMenus.pointerY();
    for(var entry:sections.entrySet())for(int i=0;i<entry.getValue().cells.size();i++) {
      var r=entry.getValue().cells.get(i);
      double dx=px-(r.x()+r.w()/2),dy=py-(r.y()+r.h()/2),d=dx*dx+dy*dy;
      if(d<distance){distance=d;current=entry.getKey();cell=i;}
    }
    Section from=sections.get(current);
    int d=direction==PadState.UP?1:direction==PadState.DOWN?2:direction==PadState.LEFT?3:4;
    int x=from.columns>0?cell%from.columns:0,y=from.columns>0?cell/from.columns:0;
    if(d==1)y--;else if(d==2)y++;else if(d==3)x--;else x++;
    Ps4UiArt.Rect target;
    if(x>=0&&y>=0&&x<from.columns&&y<from.rows)target=from.cells.get(y*from.columns+x);
    else {
      Ps4MenuNavigation.Route route=switch(sceneName){
        case "BrewingStandMenu"->Ps4MenuNavigation.brewing(current,d,x,false);
        case "EnchantingMenu"->Ps4MenuNavigation.enchanting(current,d,x,false);
        case "CartographyMenu"->Ps4MenuNavigation.cartography(current,d,x,false);
        case "BeaconMenu"->Ps4MenuNavigation.beacon(current,d,x,false,secondaryVisible());
        case "AnvilMenu"->Ps4MenuNavigation.anvil(current,d,x,false);
        case "FurnaceMenu"->Ps4MenuNavigation.furnace(current,d,x,false);
        case "InventoryMenu"->Ps4ContainerLogic.inventory(current,d,x,y,false);
        case "ChestMenu","ChestLargeMenu"->Ps4ContainerLogic.chest(current,d,false);
        default->throw new IllegalStateException(sceneName);
      };
      Section to=sections.get(route.section());if(to==null)return true;
      int[] position=Ps4MenuNavigation.position(current,route,d,x,y,to.columns,to.rows);
      int index=to.columns==0?0:position[1]*to.columns+position[0];
      target=to.cells.get(Math.max(0,Math.min(to.cells.size()-1,index)));
    }
    ControllerMenus.pointAt(Math.round(target.x()+target.w()/2),Math.round(target.y()+target.h()/2));
    ConsoleAudio.play("focus");return true;
  }
  @Override public boolean controllerButton(int button) { return false; }
}

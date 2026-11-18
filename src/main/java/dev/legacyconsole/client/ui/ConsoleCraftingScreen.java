package dev.legacyconsole.client.ui;

import dev.legacyconsole.LegacyConsole;
import dev.legacyconsole.client.ConsoleAudio;
import dev.legacyconsole.client.input.*;
import dev.legacyconsole.crafting.*;
import dev.legacyconsole.network.CraftRequest;
import dev.legacyconsole.port.*;
import java.io.IOException;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;

/** PS4 CraftingMenu1080: twelve horizontal families and the original vertical variant selector. */
public final class ConsoleCraftingScreen extends ConsoleContainerScreen implements ControllerScreen {
  private final ConsoleContainer container;
  private Ps4CraftingArt art;
  private List<ConsoleRecipeGroups.Group> groups=List.of();
  private final Map<Integer,Integer> variants=new HashMap<>();
  private int tab,selected,offset,ticks,seenRevision,inventoryHash;
  private int information=Ps4CraftingInformation.INVENTORY;
  private final CraftRepeater repeater=new CraftRepeater();
  private int repeatSource;
  private boolean showAll=true; // IUIScene_CraftingMenu ctor 0x9a8670, byte +0xde0.
  private static final int[] PERSONAL={0,1,2,4,5,6},TABLE={0,1,2,3,4,5,6};
  private boolean stone(){return container.kind==ConsoleContainer.STONECUTTER;}
  public int tutorialGroup(){return tab;}
  public ItemStack tutorialItem(){var recipe=entry();return recipe==null?ItemStack.EMPTY:recipe.output;}
  public boolean tutorialTable(){return container.table;}
  private int[] tabs(){return container.table?TABLE:PERSONAL;}
  private ConsoleRecipeGroups.Group group(){return groups.isEmpty()?null:groups.get(selected);}
  private RecipeCatalog.Entry entry(int delta) {
    var g=group();return g==null?null:g.recipes.get(Math.floorMod(variants.getOrDefault(g.id,0)+delta,g.recipes.size()));
  }
  private RecipeCatalog.Entry entry(){return entry(0);}
  private boolean available(RecipeCatalog.Entry e){return e!=null&&e.allocate(mc.player.getInventory().items)!=null;}
  public ConsoleCraftingScreen(ConsoleContainer container) {
    super(container);this.container=container;seenRevision=container.revision;
  }
  @Override public void initGui() {
    art=new Ps4CraftingArt(width,height);
    xSize=Math.round(1034*art.scale());ySize=Math.round(739*art.scale());
    super.initGui();guiLeft=(int)Math.floor(art.x());guiTop=(int)Math.floor(art.y());
    mc.player.containerMenu=container;refresh();
  }
  private void refresh() {
    var entries=stone()?StonecuttingRecipes.entries(mc.level):RecipeCatalog.entries(mc.level);
    if(!showAll)entries=entries.stream().filter(this::available).toList();
    groups=ConsoleRecipeGroups.groups(entries,stone()?-1:tab,container.table);
    selected=Math.max(0,Math.min(selected,groups.size()-1));
    offset=Math.max(0,Math.min(offset,Math.max(0,groups.size()-12)));
    inventoryHash=inventoryHash();rebuildButtons();
  }
  private int inventoryHash(){int hash=1;for(var stack:mc.player.getInventory().items)hash=31*hash+stack.save(new net.minecraft.nbt.CompoundTag()).hashCode();return hash;}
  private void toggleRecipes(){showAll=!showAll;selected=offset=0;repeater.cancel();refresh();ConsoleAudio.play("focus");}
  private void cycleInformation(){dev.legacyconsole.client.Ps4Hints.uiInput(2);information=Ps4CraftingInformation.next(information);repeater.cancel();ConsoleAudio.play("focus");}
  private Ps4CraftingArt.Rect selector(int i){return art.rect(30+(i-offset)*82,215,82,82);}
  private Ps4CraftingArt.Rect inventory(int i){return art.rect(556+i%9*48,i<27?466+i/9*48:634,48,48);}
  private GuiButton hit(int id,Ps4CraftingArt.Rect r) {
    return new GuiButton(id,(int)r.x(),(int)r.y(),(int)Math.ceil(r.w()),(int)Math.ceil(r.h()),"") {
      @Override public void drawButton(net.minecraft.client.Minecraft mc,int x,int y){}
    };
  }
  private void rebuildButtons() {
    buttonList.clear();if(art==null)return;
    if(!stone()) {
      for(int i=0;i<tabs().length;i++)buttonList.add(hit(200+i,art.rect(6+i*147,0,147,104)));
      for(int i=0;i<4;i++)buttonList.add(hit(900+i,art.rect(-113,119+i*120,135,120)));
    }
    for(int i=offset;i<Math.min(groups.size(),offset+12);i++)buttonList.add(hit(100+i,selector(i)));
    buttonList.add(hit(0,art.control("CraftingOutput")));
  }
  private void select(int i) {
    if(groups.isEmpty())return;
    selected=Math.floorMod(i,groups.size());
    if(selected<offset)offset=selected;if(selected>=offset+12)offset=selected-11;
    repeater.cancel();rebuildButtons();ConsoleAudio.play("focus");
  }
  private void variant(int delta) {
    var group=group();if(group==null)return;
    variants.put(group.id,Math.floorMod(variants.getOrDefault(group.id,0)+delta,group.recipes.size()));
    repeater.cancel();ConsoleAudio.play("focus");
  }
  private void tab(int delta) {
    if(stone())return;
    int[] tabs=tabs();int index=0;for(int i=0;i<tabs.length;i++)if(tabs[i]==tab)index=i;
    tab=tabs[Math.floorMod(index+delta,tabs.length)];selected=offset=0;repeater.cancel();refresh();ConsoleAudio.play("focus");
  }
  private void type(int type) {
    if(stone())return;
    type=Math.floorMod(type,4);if(type>0)mc.setScreen(new ConsoleSpecialCrafting(container,type));
  }
  private void craft(int batches) {
    var e=entry();if(!available(e)||!container.getCarried().isEmpty()){ConsoleAudio.play("fail");return;}
    LegacyConsole.NETWORK.sendToServer(new CraftRequest(container.containerId,e.key,batches));
    if(batches==1) {
      repeater.press(e.key,ticks);
      repeatSource=ControllerInput.INSTANCE.state.held(PadState.CROSS)?1:Mouse.isButtonDown(0)?2:3;
    }
  }
  @Override protected void actionPerformed(GuiButton button) {
    if(button.id>=900)type(button.id-900);
    else if(button.id>=200){int next=button.id-200;tab=tabs()[next];selected=offset=0;repeater.cancel();refresh();}
    else if(button.id>=100){if(selected==button.id-100)craft(1);else select(button.id-100);}
    else craft(1);
  }
  private void tabs(boolean active) {
    if(stone())return;
    int[] tabs=tabs();
    for(int i=0;i<tabs.length;i++)if((tabs[i]==tab)==active) {
      art.sprite(active?(i==0?"LargeTabLeft":i==tabs.length-1?"LargeTabRight":"LargeTabMiddle"):
          "LargeOffTab",6+i*147,active?0:10);
      var r=art.rect(46+i*147,active?18:28,72,72);
      ConsoleTheme.spriteAt("ps4/workstations/icon_"+ConsoleRecipeGroups.ICONS[tabs[i]],r.x(),r.y(),r.w(),r.h());
    }
    for(int i=0;i<4;i++)if((i==0)==active) {
      ItemStack icon=switch(i){case 0->new ItemStack(Items.CRAFTING_TABLE);case 1->new ItemStack(Items.WHITE_BANNER);
        case 2->new ItemStack(Items.FIREWORK_ROCKET);default->dyedArmor();};
      art.verticalTab(i,active,icon);
    }
  }
  private static ItemStack dyedArmor(){var stack=new ItemStack(Items.LEATHER_CHESTPLATE);((DyeableLeatherItem)stack.getItem()).setColor(stack,DyeColor.GREEN.getFireworkColor());return stack;}
  private void drawFamily(int i) {
    var g=groups.get(i);int index=Math.floorMod(variants.getOrDefault(g.id,0),g.recipes.size());
    var r=selector(i);
    if(i==selected&&g.recipes.size()>1) {
      boolean three=g.recipes.size()>2;
      art.sprite(three?"Crafting_3SlotLargeV":"Crafting_2SlotLargeV",
          (r.x()-art.x())/art.scale()-19,(r.y()-art.y())/art.scale()-(three?153:71));
      if(three) {
        var previous=g.recipes.get(Math.floorMod(index-1,g.recipes.size()));
        art.slot(previous.output,new Ps4CraftingArt.Rect(r.x(),r.y()-r.h(),r.w(),r.h()),false,!available(previous));
      }
      var next=g.recipes.get((index+1)%g.recipes.size());
      art.slot(next.output,new Ps4CraftingArt.Rect(r.x(),r.y()+r.h(),r.w(),r.h()),false,!available(next));
    }
    var e=g.recipes.get(index);art.slot(e.output,r,i==selected,!available(e));
  }
  private void drawGrid() {
    int size=container.table?3:2;
    var r=art.control(container.table?"IngredientsLayout":"IngredientsLayout_Small");
    var e=entry();
    List<ItemStack> cells=new ArrayList<>(Collections.nCopies(size*size,ItemStack.EMPTY));
    boolean[] missing=new boolean[size*size];
    if(e!=null) {
      boolean[] absent=e.missingCells(mc.player.getInventory().items);
      for(int i=0;i<e.cells.size();i++) {
        int target=e.shapeless?i:(i/e.width)*size+i%e.width;
        if(target>=cells.size()||e.cells.get(i).alternatives.isEmpty())continue;
        var ingredient=e.cells.get(i);
        ItemStack display=mc.player.getInventory().items.stream().filter(ingredient::matches).findFirst()
            .orElse(ingredient.alternatives.get(0)).copy();display.setCount(1);
        cells.set(target,display);missing[target]=absent[i];
      }
    }
    for(int i=0;i<cells.size();i++)art.slot(cells.get(i),new Ps4CraftingArt.Rect(
        r.x()+i%size*r.w()/size,r.y()+i/size*r.h()/size,r.w()/size,r.h()/size),false,missing[i]);
    art.sprite("Arrow_Small_Off",290,564);
    art.slot(e==null?ItemStack.EMPTY:e.output,art.control("CraftingOutput"),false,false);
  }
  @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my) {
    tabs(false);art.panels();tabs(true);
    art.label(stone()?Ps4UiArt.text("ec992395"):ConsoleRecipeGroups.NAMES[tab],31,143,460,true,ConsoleTheme.DARK);
    for(int i=offset;i<Math.min(groups.size(),offset+12);i++)if(i!=selected)drawFamily(i);
    if(!groups.isEmpty())drawFamily(selected);
    drawGrid();
    var e=entry();
    if(information==Ps4CraftingInformation.INVENTORY){
      art.label(Ps4CraftingArt.label("a89bcc3a"),535,412,465,true,ConsoleTheme.DARK);
      for(int i=0;i<36;i++)art.slot(container.getSlot(i).getItem(),inventory(i),false,false);
    }else if(information==Ps4CraftingInformation.DESCRIPTION){
      art.description(e==null?ItemStack.EMPTY:e.output);
    }else{
      art.label(Ps4CraftingArt.label("cb14bd42"),535,412,465,true,ConsoleTheme.DARK);
      art.ingredients(Ps4CraftingInformation.ingredients(e,mc.player.getInventory().items));
    }
    if(e!=null) {
      art.label(e.output.getHoverName().getString(),31,426,460,true,ConsoleTheme.DARK);
    }
  }
  @Override protected void drawSlot(Slot slot) {} // Native crafting displays inventory; it does not pick up stacks.
  @Override protected boolean isPointInRegion(int x,int y,int w,int h,int mx,int my){return false;}
  @Override protected void drawGuiContainerForegroundLayer(int x,int y){}
  @Override public void drawScreen(int x,int y,float partial) {
    drawDefaultBackground();super.drawScreen(x,y,partial);
    if(stone())ConsoleTheme.footer(width,height,"cross",Ps4CraftingArt.label("5f116e91"),"triangle",Ps4UiArt.text(showAll?"21ae8965":"2fddc05c"),"circle",Ps4CraftingArt.label("ded2ef45"),"square",Ps4CraftingArt.label(Ps4CraftingInformation.prompt(information)));
    else ConsoleTheme.footer(width,height,"cross",Ps4CraftingArt.label("5f116e91"),"circle",Ps4CraftingArt.label("ded2ef45"),
        "triangle",Ps4UiArt.text(showAll?"21ae8965":"2fddc05c"),"square",Ps4CraftingArt.label(Ps4CraftingInformation.prompt(information)),"left_bumper","","right_bumper",Ps4CraftingArt.label("06974812"),"left_trigger","","right_trigger",Ps4CraftingArt.label("f73b4f2d"));
  }
  @Override public void updateScreen() {
    ticks++;
    if(seenRevision!=container.revision) {
      seenRevision=container.revision;ConsoleAudio.play(container.status==NativeCrafting.OK?"craft":"fail");
      if(container.status!=NativeCrafting.OK)repeater.cancel();refresh();
    }
    if(inventoryHash!=inventoryHash())refresh();
    var e=entry();
    boolean held=repeatSource==1?ControllerInput.INSTANCE.state.held(PadState.CROSS):repeatSource==2?Mouse.isButtonDown(0):Keyboard.isKeyDown(Keyboard.KEY_RETURN);
    if(e==null||!available(e)||!Display.isActive())repeater.cancel();
    else if(repeater.repeat(held,e.key,ticks))LegacyConsole.NETWORK.sendToServer(new CraftRequest(container.containerId,e.key,1));
  }
  @Override protected void mouseClicked(int x,int y,int button)throws IOException {
    for(GuiButton b:buttonList)if(x>=b.xPosition&&y>=b.yPosition&&x<b.xPosition+b.width&&y<b.yPosition+b.height) {
      if(button==0)actionPerformed(b);else if(button==1&&b.id==0)craft(64);return;
    }
    if(!groups.isEmpty()) {
      var r=selector(selected);
      if(x>=r.x()&&x<r.x()+r.w()&&y>=r.y()-r.h()&&y<r.y()+r.h()*2)variant(y<r.y()?-1:1);
    }
  }
  @Override public void handleMouseInput()throws IOException{super.handleMouseInput();int wheel=Mouse.getEventDWheel();if(wheel!=0)variant(wheel>0?-1:1);}
  @Override protected void keyTyped(char c,int key)throws IOException {
    if(key==Keyboard.KEY_RETURN)craft(1);
    else if(key==Keyboard.KEY_O)toggleRecipes();
    else if(key==Keyboard.KEY_X)cycleInformation();
    else if(key==Keyboard.KEY_LEFT||key==Keyboard.KEY_RIGHT)select(selected+(key==Keyboard.KEY_LEFT?-1:1));
    else if(key==Keyboard.KEY_UP||key==Keyboard.KEY_DOWN)variant(key==Keyboard.KEY_UP?-1:1);
    else if(key==Keyboard.KEY_TAB)tab(isShiftKeyDown()?-1:1);
    else if(key==Keyboard.KEY_LBRACKET||key==Keyboard.KEY_RBRACKET){int d=key==Keyboard.KEY_LBRACKET?-1:1;if(isShiftKeyDown())type(d);else tab(d);}
    else super.keyTyped(c,key);
  }
  @Override public boolean controllerPointer(){return false;}
  @Override public boolean controllerDirection(int d){if(d==PadState.LEFT||d==PadState.RIGHT)select(selected+(d==PadState.LEFT?-1:1));else variant(d==PadState.UP?-1:1);return true;}
  @Override public boolean controllerButton(int b){if(b==PadState.CROSS){craft(1);return true;}if(b==PadState.TRIANGLE){toggleRecipes();return true;}if(b==PadState.SQUARE){cycleInformation();return true;}if(b==PadState.L1||b==PadState.R1){tab(b==PadState.L1?-1:1);return true;}if(b==PadState.L2||b==PadState.R2){type(b==PadState.L2?-1:1);return true;}return false;}
  @Override public void onGuiClosed(){repeater.cancel();super.onGuiClosed();}
}

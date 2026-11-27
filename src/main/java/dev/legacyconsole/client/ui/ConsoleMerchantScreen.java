package dev.legacyconsole.client.ui;

import dev.legacyconsole.LegacyConsole;
import dev.legacyconsole.client.ConsoleAudio;
import dev.legacyconsole.client.input.*;
import dev.legacyconsole.crafting.NativeTrading;
import dev.legacyconsole.network.TradeRequest;
import dev.legacyconsole.port.*;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.*;

/** PS4 UIScene_TradingMenu (0xad19a0..0xad58b0) with TradingMenu1080.fui.
 * The Java server supplies offers, prices, XP and inventory-funded transactions.
 */
public final class ConsoleMerchantScreen extends GuiMerchant implements ControllerScreen {
  private final Merchant merchant;
  private final MerchantMenu menu;
  private final String merchantName;
  private final Ps4TradeList trades=new Ps4TradeList();
  private final CraftRepeater repeater=new CraftRepeater();
  private Ps4UiArt art;
  private int ticks,seenHash;

  public ConsoleMerchantScreen(MerchantScreen original) {
    super(original);merchant=super.getMerchant();menu=(MerchantMenu)inventorySlots;
    merchantName=original.getTitle().getString();
  }
  @Override public Merchant getMerchant(){return merchant;}
  private MerchantOffers offers(){return menu.getOffers();}
  private MerchantOffer selectedOffer(){int i=trades.serverIndex();return i<0||i>=offers().size()?null:offers().get(i);}
  private Ps4UiArt.Rect offerCell(int column){return art.cell("TradingBar",column,0,Ps4TradeList.COLUMNS,1);}
  private Ps4UiArt.Rect inventoryCell(int index) {
    return index<9?art.cell("HotBar",index,0,9,1):art.cell("Inventory",(index-9)%9,(index-9)/9,9,3);
  }
  @Override public void initGui() {
    art=new Ps4UiArt("TradingMenu",width,height);var panel=art.panelBounds();
    xSize=(int)Math.ceil(panel.w());ySize=(int)Math.ceil(panel.h());super.initGui();
    guiLeft=(int)art.x;guiTop=(int)art.y;mc.player.containerMenu=inventorySlots;
    // PS4's inventory is a read-only view. Java merchant input slots stay hidden.
    for(Slot slot:inventorySlots.slots)slot.x=slot.y=-1000;
    rebuild();
  }
  private void rebuild() {
    if(trades.update(offers().size()))repeater.cancel();
    buttonList.clear();
    if(trades.left())buttonList.add(new Hit(1,scrollBounds("ScrollLeft")));
    if(trades.right())buttonList.add(new Hit(2,scrollBounds("ScrollRight")));
    for(int i=0;i<Ps4TradeList.COLUMNS;i++)buttonList.add(new Hit(100+i,offerCell(i)));
    buttonList.add(new Hit(0,art.control("Result")));
    // Java's SelectTrade packet also pulls costs into its hidden input slots.
    // PS4 selection only changes focus; TradeRequest carries the authoritative index.
    if(trades.serverIndex()>=0)menu.setSelectionHint(trades.serverIndex());
  }
  private void choose(int index) {
    if(!trades.choose(index))return;
    repeater.cancel();rebuild();ConsoleAudio.play("focus");
  }
  private boolean available(){var offer=selectedOffer();return offer!=null&&NativeTrading.canTrade(mc.player,offer)
      &&menu.getCarried().isEmpty();}
  private void trade() {
    if(!available()){repeater.cancel();ConsoleAudio.play("fail");return;}
    LegacyConsole.NETWORK.sendToServer(new TradeRequest(menu.containerId,trades.serverIndex()));
    repeater.press(trades.serverIndex(),ticks);ConsoleAudio.play("craft");
  }
  @Override protected void actionPerformed(GuiButton button) {
    if(button.id>=100) {
      int index=trades.offset()+button.id-100;
      if(index==trades.selected())trade();else choose(index);
    } else if(button.id==1||button.id==2)choose(trades.selected()+(button.id==1?-1:1));
    else trade();
  }
  @Override protected void mouseClicked(int x,int y,int button) throws IOException {
    if(button!=0)return;
    for(GuiButton hit:buttonList)if(((Hit)hit).bounds.contains(x,y)){actionPerformed(hit);return;}
  }
  @Override public void updateScreen() {
    ticks++;
    int hash=offers().createTag().hashCode();
    if(hash!=seenHash){seenHash=hash;rebuild();}
    boolean held=ControllerCursor.active()?ControllerInput.INSTANCE.state.held(PadState.CROSS):
        Mouse.isButtonDown(0)||Keyboard.isKeyDown(Keyboard.KEY_RETURN);
    if(!available())repeater.cancel();
    else if(repeater.repeat(held,trades.serverIndex(),ticks))
      LegacyConsole.NETWORK.sendToServer(new TradeRequest(menu.containerId,trades.serverIndex()));
  }
  @Override protected void drawSlot(Slot slot) {}
  private void slot(ItemStack stack,Ps4UiArt.Rect r,boolean warning,boolean exhausted) {
    art.image(exhausted?"IconHolderLocked_1x1border":warning?"IconHolderRed_1x1border":"IconHolder_1x1border",r);
    art.item(stack,r);
  }
  private Ps4UiArt.Rect scrollBounds(String control) {
    float[] m=art.matrix(control);var sprite=Ps4UiArt.DATA.sprites.get("scrollLeft");
    return art.rect(m[4]-9*m[0],m[5]-16*m[3],sprite.size[0]*m[0],sprite.size[1]*m[3]);
  }
  private int level(){return Math.max(0,Math.min(4,menu.getTraderLevel()-1));}
  private String levelText(){return Ps4UiArt.text("f77d295e").replace("%s",Integer.toString(level()+1));}
  private float currentProgress() {
    if(level()==4)return 1;
    int low=VillagerData.getMinXpPerLevel(level()+1),high=VillagerData.getMaxXpPerLevel(level()+1);
    return Math.max(0,Math.min(1,(float)(menu.getTraderXp()-low)/(high-low)));
  }
  @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my) {
    art.backgrounds();
    art.centeredLabel(menu.showProgressBar()?levelText()+" - "+merchantName:merchantName,"VillagerText");
    art.insetLabel(Ps4UiArt.text("14630b9e"),"InventoryLabel","FJ_SquareRecessedPanel");
    if(menu.showProgressBar()) {
      float[] m=art.matrix("TradingLevelLabel");
      art.centeredLabel(levelText(),"TradingLevelLabel");
      art.textAt(Ps4UiArt.text(Ps4TradeList.levelKey(level())),m[4],m[5]+30,369*m[0],ConsoleTheme.DARK,true);
      var offer=selectedOffer();
      int range=level()==4?1:VillagerData.getMaxXpPerLevel(level()+1)-VillagerData.getMinXpPerLevel(level()+1);
      art.experience(currentProgress(),offer==null?0:Ps4TradeList.preview(level(),currentProgress(),offer.getXp(),range));
    }
    for(int i=0;i<Ps4TradeList.COLUMNS;i++) {
      int id=trades.serverIndex(trades.offset()+i);MerchantOffer offer=id<0||id>=offers().size()?null:offers().get(id);
      slot(offer==null?ItemStack.EMPTY:offer.getResult(),offerCell(i),
          offer!=null&&!NativeTrading.canTrade(mc.player,offer),offer!=null&&offer.isOutOfStock());
    }
    // FJ_TradingMenu::setSelectedSlot (0x832f90): center the native highlight on its cell.
    var r=offerCell(trades.column());var highlight=Ps4UiArt.DATA.sprites.get("Craft_Highlight_L_Small");
    float extra=(highlight.size[0]*art.scale-r.w())/2;
    art.image("Craft_Highlight_L_Small",r.inset(-extra));
    for(int i=0;i<36;i++)slot(mc.player.getInventory().getItem(i),inventoryCell(i),false,false);
    MerchantOffer offer=selectedOffer();
    boolean[] missing=offer==null?new boolean[2]:NativeTrading.costWarnings(mc.player,offer);
    // 0x833370 centers a single-payment merchant's first holder on Result and
    // hides Request2. Keep two holders for villagers or Java offers with a second cost.
    boolean second=menu.showProgressBar()||offers().stream().anyMatch(o->!o.getCostB().isEmpty());
    var first=art.control("Request1");
    if(!second)first=new Ps4UiArt.Rect(first.x(),art.control("Result").y(),first.w(),first.h());
    slot(offer==null?ItemStack.EMPTY:offer.getCostA(),first,missing[0],false);
    if(second)slot(offer==null?ItemStack.EMPTY:offer.getCostB(),art.control("Request2"),missing[1],false);
    slot(offer==null?ItemStack.EMPTY:offer.getResult(),art.control("Result"),missing[0]||missing[1],false);
    art.object("RightArrow1");
    if(offer!=null&&offer.isOutOfStock())art.object("RedCross");
    if(trades.left())art.image("scrollLeft",scrollBounds("ScrollLeft"));
    if(trades.right())art.image("scrollRight",scrollBounds("ScrollRight"));
  }
  @Override protected void drawGuiContainerForegroundLayer(int x,int y) {}
  @Override protected boolean isPointInRegion(int x,int y,int w,int h,int mx,int my){return false;}
  @Override protected void handleMouseClick(Slot slot,int id,int button,ClickType type) {}
  @Override public void drawScreen(int mx,int my,float partial) {
    drawDefaultBackground();super.drawScreen(mx,my,partial);
    if(available())ConsoleTheme.footer(width,height,"cross",Ps4UiArt.text("80cf40eb"),"circle","Back");
    else ConsoleTheme.footer(width,height,"circle","Back");
    // Java item rendering writes depth; PS4's flat HTML overlay must remain above it.
    GlStateManager.pushMatrix();GlStateManager.translate(0,0,400);
    description();GlStateManager.popMatrix();
    for(int i=0;i<36;i++)if(inventoryCell(i).contains(mx,my)&&!mc.player.getInventory().getItem(i).isEmpty())
      renderToolTip(mc.player.getInventory().getItem(i),mx,my);
  }
  private void description() {
    var offer=selectedOffer();if(offer==null)return;
    var details=TradeDescription.details(offer.getResult(),mc.player);
    if(details.isEmpty())return;
    // 0x832d50 anchors OfferDescription beside the selector, vertically centered.
    var label=Ps4UiArt.DATA.small_label;int wrap=(int)((label.bounds[1]-label.bounds[0])/2);
    var lines=new java.util.ArrayList<TradeDescription.Line>();
    for(var line:details)for(String text:fontRendererObj.listFormattedStringToWidth(line.text(),wrap))
      lines.add(new TradeDescription.Line(text,line.color()));
    float glyph=Ps4ItemInformation.glyphHeight(mc.font,
        new TextComponent(lines.get(0).text()).getVisualOrderText(),2);
    var layout=Ps4PointerPanelLayout.layout(label.bounds[1]-label.bounds[0],lines.size()*20,glyph,true);
    var cell=offerCell(trades.column());float w=Math.max(24,layout.width()),h=Math.max(24,layout.height());
    float sx=(cell.x()-art.x)/art.scale,sy=(cell.y()-art.y)/art.scale;
    float x=trades.column()<5?sx+cell.w()/art.scale/2:sx+cell.w()/art.scale/2-w;
    float y=sy+cell.h()/art.scale/2-h/2;
    float[] last=Ps4UiArt.DATA.panels.get("FJ_PointerTextPanel").pieces.get(8).rect;
    art.panel("FJ_PointerTextPanel",x,y,w/(last[0]+last[2]),h/(last[1]+last[3]));
    for(int i=0;i<lines.size();i++)art.text(lines.get(i).text(),art.rect(x+layout.textX(),y+layout.textY()+i*20,
        label.bounds[1]-label.bounds[0],20),lines.get(i).color(),false,2);
  }
  @Override protected void keyTyped(char c,int key) throws IOException {
    if(key==Keyboard.KEY_LEFT)choose(trades.selected()-1);
    else if(key==Keyboard.KEY_RIGHT)choose(trades.selected()+1);
    else if(key==Keyboard.KEY_RETURN)trade();else super.keyTyped(c,key);
  }
  @Override public void handleMouseInput(){int d=Mouse.getEventDWheel();if(d!=0)choose(trades.selected()+(d>0?-1:1));}
  @Override public boolean controllerPointer(){return false;}
  @Override public boolean controllerDirection(int direction) {
    if(direction==PadState.LEFT||direction==PadState.RIGHT)choose(trades.selected()+(direction==PadState.LEFT?-1:1));
    return true;
  }
  @Override public boolean controllerButton(int button) {
    if(button==PadState.CROSS){trade();return true;}
    return button==PadState.L1||button==PadState.R1||button==PadState.L2||button==PadState.R2||
        button==PadState.L3||button==PadState.TRIANGLE||button==PadState.SQUARE;
  }
  @Override public void onGuiClosed(){repeater.cancel();super.onGuiClosed();}
  private static final class Hit extends GuiButton {
    final Ps4UiArt.Rect bounds;
    Hit(int id,Ps4UiArt.Rect r){super(id,(int)r.x(),(int)r.y(),(int)Math.ceil(r.w()),(int)Math.ceil(r.h()),"");bounds=r;}
    @Override public void drawButton(Minecraft game,int x,int y){}
  }
}

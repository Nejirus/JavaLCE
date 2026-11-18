package dev.legacyconsole.port;
import java.util.*;import java.io.IOException;
import net.minecraft.client.Minecraft;import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.*;import net.minecraft.world.item.ItemStack;import net.minecraft.network.chat.*;
import com.mojang.blaze3d.vertex.PoseStack;
public class GuiContainer extends AbstractContainerScreen<AbstractContainerMenu> {
 public final AbstractContainerMenu inventorySlots;
 public final List<GuiButton> buttonList=new ArrayList<>();
 protected Minecraft mc=Minecraft.getInstance();protected FontCompat fontRendererObj=FontCompat.get();
 protected int xSize=176,ySize=166,guiLeft,guiTop;protected float zLevel;
 private GuiButton pressed;
 private int tooltipMouseX,tooltipMouseY;
 public GuiContainer(AbstractContainerMenu menu){super(menu,Minecraft.getInstance().player.getInventory(),TextComponent.EMPTY);inventorySlots=menu;}
 @Override protected void init(){mc=minecraft;initGui();syncOrigin();dev.legacyconsole.client.ui.MenuFocus.enter(this,buttonList);}
 public void initGui(){imageWidth=xSize;imageHeight=ySize;super.init();guiLeft=leftPos;guiTop=topPos;}
 private void syncOrigin(){leftPos=guiLeft;topPos=guiTop;}
 protected boolean consoleUiScaling(){return false;}
 protected float consoleUiTopInset(){return 0;}
 public final dev.legacyconsole.client.ui.ConsoleContainerScale consoleUiTransform(){
  if(!consoleUiScaling())return dev.legacyconsole.client.ui.ConsoleContainerScale.IDENTITY;
  var family=dev.legacyconsole.client.ui.ConsoleTheme.glyphFamily();
  float top=0;for(var glyph:family.glyphs.values())top=Math.min(top,glyph.bounds[2]/family.units);
  float inset=consoleUiTopInset();
  return dev.legacyconsole.client.ui.ConsoleContainerScale.fit(width,height,guiLeft,guiTop-inset,xSize,ySize+inset,family.originY(height)+top,dev.legacyconsole.client.ClientConfig.consoleUiSize);
 }
 /** Absolute on-screen holder rectangle for cursor snapping and quick move. */
 public final float[] consoleSlotBounds(Slot slot){
  float[] bounds=this instanceof dev.legacyconsole.client.input.ControllerScreen control?control.controllerSlotBoundsExact(slot):new float[]{slot.x,slot.y,16,16};
  return consoleUiTransform().rectangle(guiLeft+bounds[0],guiTop+bounds[1],bounds[2],bounds[3]);
 }
 @Override public final void render(PoseStack pose,int x,int y,float partial){PortGraphics.pose=pose;syncOrigin();x=dev.legacyconsole.client.input.ControllerMenus.renderMouseX(this,x);y=dev.legacyconsole.client.input.ControllerMenus.renderMouseY(this,y);tooltipMouseX=x;tooltipMouseY=y;var transform=consoleUiTransform();drawScreen((int)Math.floor(transform.menuX(x)),(int)Math.floor(transform.menuY(y)),partial);}
 public void drawScreen(int x,int y,float partial){
  var transform=consoleUiTransform();boolean scaled=!transform.equals(dev.legacyconsole.client.ui.ConsoleContainerScale.IDENTITY);
  if(scaled){GlStateManager.pushMatrix();GlStateManager.translate(transform.offsetX(),transform.offsetY(),0);GlStateManager.scale(transform.scale(),transform.scale(),1);}
  try{super.render(PortGraphics.pose,x,y,partial);for(GuiButton b:buttonList)b.drawButton(mc,x,y);}
  finally{if(scaled)GlStateManager.popMatrix();}
  // Match the existing PS4 menus: native hover text stays at the pointer and
  // uses Minecraft's tooltip sizing and screen-edge placement.
  renderTooltip(PortGraphics.pose,tooltipMouseX,tooltipMouseY);
 }
 @Override protected void renderBg(PoseStack pose,float partial,int x,int y){drawGuiContainerBackgroundLayer(partial,x,y);}
 protected void drawGuiContainerBackgroundLayer(float partial,int x,int y){}
 @Override protected void renderLabels(PoseStack pose,int x,int y){drawGuiContainerForegroundLayer(x,y);}
 protected void drawGuiContainerForegroundLayer(int x,int y){}
 @Override protected void containerTick(){updateScreen();}public void updateScreen(){}
 @Override public void removed(){onGuiClosed();}public void onGuiClosed(){super.removed();}
 protected void actionPerformed(GuiButton b)throws IOException{}
 protected void keyTyped(char c,int key)throws IOException{super.keyPressed(Keyboard.toGlfw(key),0,0);}
 @Override public boolean keyPressed(int key,int scan,int mods){try{keyTyped('\0',Keyboard.fromGlfw(key));return true;}catch(IOException e){throw new IllegalStateException(e);}}
 @Override public boolean charTyped(char c,int mods){try{keyTyped(c,0);return true;}catch(IOException e){throw new IllegalStateException(e);}}
 protected void mouseClicked(int x,int y,int b)throws IOException{if(b==0)for(GuiButton button:buttonList)if(button.mousePressed(mc,x,y)){pressed=button;button.playPressSound(mc.getSoundManager());actionPerformed(button);return;}super.mouseClicked((double)x,(double)y,b);}
 @Override public boolean mouseClicked(double x,double y,int b){syncOrigin();var transform=consoleUiTransform();try{mouseClicked((int)Math.floor(transform.menuX(x)),(int)Math.floor(transform.menuY(y)),b);return true;}catch(IOException e){throw new IllegalStateException(e);}}
 protected void mouseReleased(int x,int y,int b){if(pressed!=null){pressed.mouseReleased(x,y);pressed=null;}super.mouseReleased(x,y,b);}
 @Override public boolean mouseReleased(double x,double y,int b){syncOrigin();var transform=consoleUiTransform();mouseReleased((int)Math.floor(transform.menuX(x)),(int)Math.floor(transform.menuY(y)),b);return true;}
 @Override public boolean mouseDragged(double x,double y,int b,double dx,double dy){syncOrigin();var transform=consoleUiTransform();return super.mouseDragged(transform.menuX(x),transform.menuY(y),b,dx/transform.scale(),dy/transform.scale());}
 public void handleMouseInput()throws IOException{}
 @Override public boolean mouseScrolled(double x,double y,double amount){Mouse.scroll(amount);try{handleMouseInput();}catch(IOException e){throw new IllegalStateException(e);}return true;}
 protected boolean isPointInRegion(int x,int y,int w,int h,int mx,int my){return super.isHovering(x,y,w,h,mx,my);}
 @Override protected boolean isHovering(int x,int y,int w,int h,double mx,double my){return isPointInRegion(x,y,w,h,(int)mx,(int)my);}
 protected void handleMouseClick(Slot slot,int id,int button,ClickType type){super.slotClicked(slot,id,button,type);}
 @Override protected void slotClicked(Slot slot,int id,int b,ClickType type){handleMouseClick(slot,id,b,type);}
 public void clickSlot(Slot slot,int id,int b,ClickType type){handleMouseClick(slot,id,b,type);}
 protected void drawSlot(Slot slot){super.renderSlot(PortGraphics.pose,slot);}
 @Override protected void renderSlot(PoseStack pose,Slot slot){drawSlot(slot);}
 public void drawDefaultBackground(){renderBackground(new PoseStack());}
 public boolean doesGuiPauseGame(){return false;}
 public void drawTexturedModalRect(int x,int y,int u,int v,int w,int h){Gui.drawModalRectWithCustomSizedTexture(x,y,u,v,w,h,256,256);}
 public void drawTexturedModalRect(int x,int y,net.minecraft.client.renderer.texture.TextureAtlasSprite sprite,int w,int h){blit(new PoseStack(),x,y,0,w,h,sprite);}
 protected void drawGradientRect(int l,int t,int r,int b,int a,int c){fillGradient(new PoseStack(),l,t,r,b,a,c);}
 protected void renderToolTip(ItemStack stack,int x,int y){renderTooltip(new PoseStack(),stack,x,y);}
 protected void drawHoveringText(List<String> lines,int x,int y){renderComponentTooltip(new PoseStack(),lines.stream().map(TextComponent::new).map(c->(Component)c).toList(),x,y);}
 public static void drawRect(int l,int t,int r,int b,int color){Gui.drawRect(l,t,r,b,color);}
 public static void drawCenteredString(FontCompat f,String s,int x,int y,int c){Gui.drawCenteredString(f,s,x,y,c);}
 public static boolean isShiftKeyDown(){return hasShiftDown();}
 public static void drawModalRectWithCustomSizedTexture(int x,int y,float u,float v,int w,int h,float tw,float th){Gui.drawModalRectWithCustomSizedTexture(x,y,u,v,w,h,tw,th);}
 public static void drawScaledCustomSizeModalRect(int x,int y,float u,float v,int uw,int vh,int w,int h,float tw,float th){Gui.drawScaledCustomSizeModalRect(x,y,u,v,uw,vh,w,h,tw,th);}
}

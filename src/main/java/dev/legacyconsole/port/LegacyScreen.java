package dev.legacyconsole.port;
import java.util.*;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
public class LegacyScreen extends Screen {
 public final List<GuiButton> buttonList=new ArrayList<>();
 protected Minecraft mc=Minecraft.getInstance();
 protected FontCompat fontRendererObj=FontCompat.get();
 protected float zLevel;
 private GuiButton pressedButton;
 public LegacyScreen(){super(TextComponent.EMPTY);}
 @Override protected void init(){mc=minecraft;buttonList.clear();initGui();dev.legacyconsole.client.ui.MenuFocus.enter(this,buttonList);}
 public void initGui(){}
 public void setWorldAndResolution(Minecraft game,int w,int h){init(game,w,h);}
 @Override public final void render(PoseStack pose,int x,int y,float partial){PortGraphics.pose=pose;drawScreen(x,y,partial);}
 public void drawScreen(int x,int y,float partial){for(GuiButton b:new ArrayList<>(buttonList))b.drawButton(mc,x,y);}
 @Override public void tick(){updateScreen();}
 public void updateScreen(){}
 @Override public void removed(){onGuiClosed();}
 public void onGuiClosed(){}
 protected void actionPerformed(GuiButton b)throws IOException{}
 protected void mouseClicked(int x,int y,int button)throws IOException{if(button==0)for(GuiButton b:new ArrayList<>(buttonList))if(b.mousePressed(mc,x,y)){pressedButton=b;b.playPressSound(mc.getSoundManager());actionPerformed(b);return;}}
 @Override public boolean mouseClicked(double x,double y,int b){try{mouseClicked((int)x,(int)y,b);return true;}catch(IOException e){throw new IllegalStateException(e);}}
 protected void mouseReleased(int x,int y,int b){if(pressedButton!=null){pressedButton.mouseReleased(x,y);pressedButton=null;}}
 @Override public boolean mouseReleased(double x,double y,int b){mouseReleased((int)x,(int)y,b);return true;}
 protected void mouseClickMove(int x,int y,int button,long time){}
 @Override public boolean mouseDragged(double x,double y,int b,double dx,double dy){mouseClickMove((int)x,(int)y,b,0);return true;}
 protected void keyTyped(char c,int key)throws IOException{if(key==Keyboard.KEY_ESCAPE)onClose();}
 @Override public boolean keyPressed(int key,int scan,int mods){Keyboard.event(key,true);try{keyTyped('\0',Keyboard.fromGlfw(key));return true;}catch(IOException e){throw new IllegalStateException(e);}}
 @Override public boolean charTyped(char c,int mods){try{keyTyped(c,0);return true;}catch(IOException e){throw new IllegalStateException(e);}}
 public void handleMouseInput()throws IOException{}
 @Override public boolean mouseScrolled(double x,double y,double amount){Mouse.scroll(amount);try{handleMouseInput();}catch(IOException e){throw new IllegalStateException(e);}return true;}
 public boolean doesGuiPauseGame(){return true;}
 @Override public boolean isPauseScreen(){return doesGuiPauseGame();}
 public void drawDefaultBackground(){renderBackground(new PoseStack());}
 public void drawWorldBackground(int tint){renderBackground(new PoseStack(),tint);}
 protected void drawHoveringText(List<String> lines,int x,int y){renderComponentTooltip(new PoseStack(),lines.stream().map(TextComponent::new).map(c->(Component)c).toList(),x,y);}
 protected void renderToolTip(ItemStack stack,int x,int y){renderTooltip(new PoseStack(),stack,x,y);}
 public void drawTexturedModalRect(int x,int y,int u,int v,int w,int h){Gui.drawModalRectWithCustomSizedTexture(x,y,u,v,w,h,256,256);}
 protected void drawGradientRect(int l,int t,int r,int b,int a,int c){fillGradient(new PoseStack(),l,t,r,b,a,c);}
 public static void drawRect(int l,int t,int r,int b,int c){Gui.drawRect(l,t,r,b,c);}
 public void drawCenteredString(FontCompat f,String s,int x,int y,int c){Gui.drawCenteredString(f,s,x,y,c);}
 public void drawString(FontCompat f,String s,int x,int y,int c){Gui.drawString(f,s,x,y,c);}
 public static String getClipboardString(){return Minecraft.getInstance().keyboardHandler.getClipboard();}
 public static void setClipboardString(String s){Minecraft.getInstance().keyboardHandler.setClipboard(s);}
 public static boolean isShiftKeyDown(){return Screen.hasShiftDown();}
 public static boolean isCtrlKeyDown(){return Screen.hasControlDown();}
 public void sendChatMessage(String s){sendMessage(s);}
 public void sendChatMessage(String s,boolean add){sendMessage(s,add);}
 public static void drawModalRectWithCustomSizedTexture(int x,int y,float u,float v,int w,int h,float tw,float th){Gui.drawModalRectWithCustomSizedTexture(x,y,u,v,w,h,tw,th);}
 public static void drawScaledCustomSizeModalRect(int x,int y,float u,float v,int uw,int vh,int w,int h,float tw,float th){Gui.drawScaledCustomSizeModalRect(x,y,u,v,uw,vh,w,h,tw,th);}
}

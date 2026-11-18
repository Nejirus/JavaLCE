package dev.legacyconsole.port;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
public class GuiButton extends Gui {
 public int id,xPosition,yPosition,width,height;
 public String displayString;
 public boolean enabled=true,visible=true,hovered;
 public GuiButton(int id,int x,int y,String label){this(id,x,y,200,20,label);}
 public GuiButton(int id,int x,int y,int w,int h,String label){this.id=id;xPosition=x;yPosition=y;width=w;height=h;displayString=label;}
 public void drawButton(Minecraft mc,int x,int y){hovered=mousePressed(mc,x,y);drawRect(xPosition,yPosition,xPosition+width,yPosition+height,hovered?0xff496fbc:0xff777777);drawCenteredString(FontCompat.get(),displayString,xPosition+width/2,yPosition+(height-8)/2,hovered?0xffffff00:0xffffffff);}
 public boolean mousePressed(Minecraft mc,int x,int y){return enabled&&visible&&x>=xPosition&&y>=yPosition&&x<xPosition+width&&y<yPosition+height;}
 public void mouseReleased(int x,int y){}
 public void playPressSound(SoundManager handler){dev.legacyconsole.client.ConsoleAudio.play("confirm");}
 public int getButtonWidth(){return width;}
 public void setWidth(int value){width=value;}
 public boolean isMouseOver(){return hovered;}
 protected void mouseDragged(Minecraft mc,int x,int y){}
}

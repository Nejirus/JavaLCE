package dev.legacyconsole.port;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;
import com.mojang.blaze3d.vertex.PoseStack;
public class GuiTextField {
 private final EditBox field;
 public int xPosition,yPosition,width,height;
 public GuiTextField(int id,FontCompat font,int x,int y,int w,int h){xPosition=x;yPosition=y;width=w;height=h;field=new EditBox(Minecraft.getInstance().font,x,y,w,h,TextComponent.EMPTY);}
 public String getText(){return field.getValue();} public void setText(String s){field.setValue(s);}
 public void setMaxStringLength(int i){field.setMaxLength(i);}public void setFocused(boolean b){field.setFocus(b);}public boolean isFocused(){return field.isFocused();}
 public void setCanLoseFocus(boolean b){field.setCanLoseFocus(b);}public void setEnabled(boolean b){field.setEditable(b);}public void setEnableBackgroundDrawing(boolean b){field.setBordered(b);}
 public void setTextColor(int c){field.setTextColor(c);} public void setDisabledTextColour(int c){field.setTextColorUneditable(c);}
 public boolean textboxKeyTyped(char c,int key){return key==0?field.charTyped(c,0):field.keyPressed(Keyboard.toGlfw(key),0,0);}
 public void mouseClicked(int x,int y,int b){field.mouseClicked(x,y,b);}public void updateCursorCounter(){field.tick();}
 public void drawTextBox(){field.x=xPosition;field.y=yPosition;field.setWidth(width);field.render(new PoseStack(),0,0,0);}
 public void setCursorPositionEnd(){field.moveCursorToEnd();}public void setCursorPosition(int p){field.moveCursorTo(p);}
 public void writeText(String s){field.insertText(s);}public int getCursorPosition(){return field.getCursorPosition();}
}

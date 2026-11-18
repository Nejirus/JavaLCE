package dev.legacyconsole.port;
import java.util.*;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.FormattedText;
public final class FontCompat {
 public int FONT_HEIGHT=9,lineHeight=9;
 public static FontCompat get(){return new FontCompat();}
 public int getStringWidth(String s){return Minecraft.getInstance().font.width(s);}
 public int width(String s){return getStringWidth(s);}
 public int drawString(String s,float x,float y,int c){return Minecraft.getInstance().font.draw(new PoseStack(),s,x,y,c);}
 public int drawString(String s,float x,float y,int c,boolean shadow){return shadow?drawStringWithShadow(s,x,y,c):drawString(s,x,y,c);}
 public int drawStringWithShadow(String s,float x,float y,int c){return Minecraft.getInstance().font.drawShadow(new PoseStack(),s,x,y,c);}
 public String trimStringToWidth(String s,int w){return Minecraft.getInstance().font.plainSubstrByWidth(s,w);}
 public String trimStringToWidth(String s,int w,boolean reverse){return Minecraft.getInstance().font.plainSubstrByWidth(s,w,reverse);}
 public List<String> listFormattedStringToWidth(String s,int w){List<String> out=new ArrayList<>();for(FormattedText t:Minecraft.getInstance().font.getSplitter().splitLines(s,w,net.minecraft.network.chat.Style.EMPTY))out.add(t.getString());return out;}
 public void drawSplitString(String s,int x,int y,int width,int color){for(String line:listFormattedStringToWidth(s,width)){drawString(line,x,y,color);y+=9;}}
}

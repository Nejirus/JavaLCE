package dev.legacyconsole.port;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
public class Gui extends GuiComponent {
 protected float zLevel;
 public static void drawRect(int l,int t,int r,int b,int color){fill(new PoseStack(),l,t,r,b,color);}
 protected void drawGradientRect(int l,int t,int r,int b,int a,int c){fillGradient(new PoseStack(),l,t,r,b,a,c);}
 public static void drawModalRectWithCustomSizedTexture(int x,int y,float u,float v,int w,int h,float tw,float th){blit(new PoseStack(),x,y,u,v,w,h,(int)tw,(int)th);}
 public static void drawScaledCustomSizeModalRect(int x,int y,float u,float v,int uw,int vh,int w,int h,float tw,float th){blit(new PoseStack(),x,y,w,h,u,v,uw,vh,(int)tw,(int)th);}
 public void drawTexturedModalRect(int x,int y,int u,int v,int w,int h){blit(new PoseStack(),x,y,u,v,w,h);}
 public void drawTexturedModalRect(int x,int y,TextureAtlasSprite sprite,int w,int h){blit(new PoseStack(),x,y,0,w,h,sprite);}
 public static void drawCenteredString(FontCompat font,String text,int x,int y,int color){font.drawStringWithShadow(text,x-font.getStringWidth(text)/2,y,color);}
 public static void drawString(FontCompat font,String text,int x,int y,int color){font.drawStringWithShadow(text,x,y,color);}
}

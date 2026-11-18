package dev.legacyconsole.client.ui;
import net.minecraft.advancements.Advancement;import net.minecraft.client.Minecraft;
import dev.legacyconsole.port.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
/** Carry the existing LCE notification panel over the native 1.17 advancement queue. */
public final class ConsoleAchievement {
 private ConsoleAchievement(){}
 public static void render(Advancement advancement,long elapsed){
  Minecraft game=Minecraft.getInstance();if(game.player==null||game.options.hideGui||advancement.getDisplay()==null)return;
  // ToastInstance applies a top-right translation before calling us. The LCE
  // panel has its own centered placement, using the native GUI depth range.
  PoseStack modelView=RenderSystem.getModelViewStack();modelView.pushPose();
  boolean depth=org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
  try {
  modelView.setIdentity();modelView.translate(0,0,1800f-net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane());RenderSystem.applyModelViewMatrix();
  String name=advancement.getDisplay().getTitle().getString(),subtitle=advancement.getDisplay().getFrame().getDisplayName().getString();
  FontCompat font=FontCompat.get();int width=Math.min(game.getWindow().getGuiScaledWidth()-24,Math.max(220,Math.max(font.getStringWidth(name),font.getStringWidth(subtitle))+84));int x=(game.getWindow().getGuiScaledWidth()-width)/2;
  float show=Math.min(1,Math.min(elapsed/250f,(5000-elapsed)/250f));int y=Math.round(-46+96*show);
  GlStateManager.disableDepth();GlStateManager.enableBlend();GlStateManager.color(1,1,1,1);
  ConsoleTheme.sprite("gui/sprites/tiles/pointer_panel",x,y,width,46);PortGraphics.bind(game.player.getSkinTextureLocation());
  Gui.drawScaledCustomSizeModalRect(x+8,y+9,8,8,8,8,28,28,64,64);Gui.drawScaledCustomSizeModalRect(x+8,y+9,40,8,8,8,28,28,64,64);
  ConsoleTheme.centered(ConsoleTheme.fit(name,width-88),x+width/2,y+10,ConsoleTheme.TEXT);ConsoleTheme.centered(ConsoleTheme.fit(subtitle,width-88),x+width/2,y+27,0xffffff55);
  ConsoleTheme.sprite("gui/sprites/tiles/small_panel",x+width-36,y+9,28,28);ConsoleTheme.item(advancement.getDisplay().getIcon(),x+width-30,y+15);GlStateManager.color(1,1,1,1);
  } finally {modelView.popPose();RenderSystem.applyModelViewMatrix();if(depth)GlStateManager.enableDepth();else GlStateManager.disableDepth();}
 }
}

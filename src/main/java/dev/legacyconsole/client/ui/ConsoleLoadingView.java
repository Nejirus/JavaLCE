package dev.legacyconsole.client.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import dev.legacyconsole.client.loading.ConsoleLoading;
import dev.legacyconsole.client.loading.LceLoadingLayout;
import dev.legacyconsole.client.loading.LceConnectionTimer;

/** LCE FullscreenProgress720 presentation with the shared Java main-menu logo. */
public final class ConsoleLoadingView {
  private ConsoleLoadingView() {}
  public static void draw(int width,int height,ConsoleLoading.State state,String tip,long elapsedMillis) {
    ConsoleTheme.background(width,height);
    ConsoleMenuLayout.logo(width,height);
    float scale=LceViewport.scale(width,height);
    if(state.kind()==ConsoleLoading.Kind.CONNECTING) {
      for(var square:LceConnectionTimer.squares(width,height,elapsedMillis)) {
        var bounds=square.bounds();PoseStack pose=new PoseStack();
        pose.translate(bounds.x(),bounds.y(),0);pose.scale(bounds.width(),bounds.height(),1);
        // The original shape (skin.swf character 29) is a white, un-stroked rectangle.
        net.minecraft.client.gui.GuiComponent.fill(pose,0,0,1,1,
            (Math.round(square.opacity()*255/256f)<<24)|0xffffff);
      }
      cancelFooter(width,height);
      return;
    }
    centered(state.title(),width/2f,LceViewport.y(width,height,300));
    centered(state.status(),width/2f,LceViewport.y(width,height,374));
    var bar=LceLoadingLayout.bar(width,height);
    ConsoleTheme.spriteAt("startup/progress_background",bar.x(),bar.y(),bar.width(),bar.height());
    var fill=LceLoadingLayout.fill(width,height,state.progress());
    if(fill.width()>0)ConsoleTheme.regionSprite("startup/progress_fill",fill.x(),fill.y(),fill.width(),fill.height(),
        0,0,LceLoadingLayout.progress(state.progress()),1);
    if(state.tips()&&!tip.isEmpty()) {
      var panel=LceLoadingLayout.tips(width,height);
      // FJ_PointerTextPanel's original scale9 grid is (8,8)-(16,16) in its 24px bitmap.
      var model=com.mojang.blaze3d.systems.RenderSystem.getModelViewStack();model.pushPose();
      try {
        model.translate(panel.x(),panel.y(),0);model.scale(scale,scale,1);
        com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
        ConsoleTheme.spriteAt("startup/tip_panel",0,0,800,110);
      } finally { model.popPose();com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix(); }
      var font=Minecraft.getInstance().font;int textWidth=Math.max(1,Math.round(panel.width()-40*scale));
      var lines=font.split(new net.minecraft.network.chat.TextComponent(tip),textWidth);
      float y=panel.y()+20*scale;
      for(var line:lines) {
        if(y+font.lineHeight>panel.y()+panel.height()-10*scale)break;
        font.drawShadow(new PoseStack(),line,(width-font.width(line))/2f,y,0xffffffff);
        y+=font.lineHeight+1;
      }
    }
    if(state.kind()==ConsoleLoading.Kind.JOINING)cancelFooter(width,height);
  }
  private static void cancelFooter(int width,int height) {
    if(ConsoleLoading.canCancelConnection())ConsoleTheme.footer(width,height,"circle","Cancel Join");
  }
  public static LceLoadingLayout.Bounds cancelBounds(int width,int height) {
    var family=ConsoleTheme.glyphFamily();
    var prompt=LceGlyphLayout.footer(family,width,height,LcePromptFont.INSTANCE::width,"circle","Cancel Join").get(0);
    float[] glyph=family.glyph("circle").bounds;
    float top=Math.min(prompt.y+glyph[2]/family.units,prompt.textY);
    float bottom=Math.max(prompt.y+glyph[3]/family.units,prompt.textY+LcePromptFont.INSTANCE.cellHeight);
    return new LceLoadingLayout.Bounds(prompt.x,top,prompt.nextX-prompt.x,bottom-top);
  }
  private static void centered(String text,float x,float middleY) {
    var font=Minecraft.getInstance().font;
    font.drawShadow(new PoseStack(),text,x-font.width(text)/2f,middleY-font.lineHeight/2f,0xffffffff);
  }
}

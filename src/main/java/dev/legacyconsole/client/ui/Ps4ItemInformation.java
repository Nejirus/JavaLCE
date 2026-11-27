package dev.legacyconsole.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.legacyconsole.client.ClientConfig;
import dev.legacyconsole.client.LceHudRenderer;
import dev.legacyconsole.port.GlStateManager;
import dev.legacyconsole.port.PortGraphics;
import dev.legacyconsole.port.ScaledResolution;
import dev.legacyconsole.port.ReflectionHelper;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Item tooltip adapter for PS4's pointer HTML panel and HUD SelectedLabel. */
public final class Ps4ItemInformation {
  private static final Field FONTS=ReflectionHelper.findField(Font.class,"fonts","f_92713_");
  private static final Field GLYPH_UP=ReflectionHelper.findField(BakedGlyph.class,"up","f_95207_");
  private static final Field GLYPH_DOWN=ReflectionHelper.findField(BakedGlyph.class,"down","f_95208_");
  private ItemStack previous=ItemStack.EMPTY;
  private int slot=-1,remaining;
  private List<Component> selected=List.of();

  @SubscribeEvent public void tick(TickEvent.ClientTickEvent event){
    if(event.phase!=TickEvent.Phase.END)return;
    var mc=Minecraft.getInstance();
    if(mc.player==null){previous=ItemStack.EMPTY;slot=-1;remaining=0;selected=List.of();return;}
    if(mc.isPaused())return;
    var stack=mc.player.getMainHandItem();int index=mc.player.getInventory().selected;
    if(Ps4SelectedItem.changed(index,slot,stack,previous)){
      slot=index;previous=stack.copy();remaining=stack.isEmpty()?0:Ps4InformationLayout.SELECTED_TICKS;
      selected=hotbarLines(stack,mc);
    }else if(remaining>0)remaining--;
  }
  public static List<Component> hotbarLines(ItemStack stack,Minecraft mc){
    if(stack.isEmpty())return List.of();
    var lines=new ArrayList<Component>();
    // 0xc78a90 calls ItemInstance::getTooltip(false), removes empty entries,
    // and keeps four entries plus "..." when more than five remain.
    for(var line:stack.getTooltipLines(mc.player,TooltipFlag.Default.NORMAL))
      if(!line.getString().isBlank())lines.add(line);
    if(lines.size()>Ps4InformationLayout.MAX_SELECTED_LINES){
      lines.subList(4,lines.size()).clear();lines.add(new TextComponent("..."));
    }
    return List.copyOf(lines);
  }
  /** Java adapter for PS4 0x863ea0(textField, 0), the first rendered glyph. */
  @SuppressWarnings("unchecked")
  static float glyphHeight(Font font,FormattedCharSequence text,float sourceTextScale){
    try{
      var fonts=(Function<ResourceLocation,FontSet>)FONTS.get(font);
      float[] height={0};
      text.accept((index,style,codePoint)->{
        var glyph=fonts.apply(style.getFont()).getGlyph(codePoint);
        try{height[0]=Math.abs(GLYPH_DOWN.getFloat(glyph)-GLYPH_UP.getFloat(glyph));}
        catch(IllegalAccessException e){throw new IllegalStateException(e);}
        return false;
      });
      return height[0]*sourceTextScale;
    }catch(IllegalAccessException e){throw new IllegalStateException(e);}
  }
  public void hotbar(PoseStack pose,float partial){
    var mc=Minecraft.getInstance();
    if(!ClientConfig.consoleHud||mc.player==null||mc.screen!=null||mc.options.hideGui||
        !mc.options.heldItemTooltips||remaining<=0||selected.isEmpty())return;
    var res=new ScaledResolution(mc);int width=res.getScaledWidth(),height=res.getScaledHeight();
    float base=Math.min(width/1920f,height/1080f);
    float factor=Ps4UiScale.factor(ClientConfig.hudSize),bottom;
    if(LceHudRenderer.supported(mc)){
      var hud=LceHudRenderer.layout(res);bottom=hud.rootY+(mc.gameMode.canHurtPlayer()?-16:11)*hud.factor+9*base*3;
    }else bottom=height-(mc.gameMode.canHurtPlayer()?59:45)-20-ClientConfig.hudLift+9*base*3;
    int textWidth=selected.stream().mapToInt(mc.font::width).max().orElse(1);
    float clearance=LceHudRenderer.supported(mc)?Ps4InformationLayout.crosshairClearance(mc.getWindow().getHeight(),ClientConfig.hudSize):24;
    var layout=Ps4InformationLayout.hotbar(width,height,bottom,3*base*factor,textWidth,selected.size(),clearance);
    float left=remaining-partial;float alpha=1;
    if(left<Ps4InformationLayout.FADE_TICKS){
      float[] fade=Ps4Information.DATA.selected_fade;
      int frame=Math.min(fade.length-1,Math.max(0,(int)((1-left/Ps4InformationLayout.FADE_TICKS)*fade.length)));
      alpha=fade[frame];
    }
    if(alpha<=.01)return;
    pose.pushPose();pose.translate(width/2f,layout.y(),0);pose.scale(layout.scale(),layout.scale(),1);
    int color=((int)(alpha*255)<<24)|0xffffff;
    for(int i=0;i<selected.size();i++){
      var line=selected.get(i);mc.font.drawShadow(pose,line,-mc.font.width(line)/2f,i*10,color);
    }
    pose.popPose();
  }

  @SubscribeEvent public void tooltip(RenderTooltipEvent.Pre event){
    var mc=Minecraft.getInstance();
    if(!ClientConfig.consoleMenus||mc.screen==null||event.getStack().isEmpty()||event.getComponents().isEmpty())return;
    event.setCanceled(true);
    var font=event.getFontRenderer();var components=event.getComponents();
    int width=components.stream().mapToInt(c->c.getWidth(font)).max().orElse(1);
    int height=components.stream().mapToInt(c->c.getHeight()).sum();
    float base=Math.min(mc.screen.width/1920f,mc.screen.height/1080f);
    float scale=base*Ps4UiScale.factor(ClientConfig.consoleUiSize),sourceTextScale=2;
    float glyph=glyphHeight(font,event.getStack().getHoverName().getVisualOrderText(),sourceTextScale);
    var panel=Ps4PointerPanelLayout.layout(width*sourceTextScale,height*sourceTextScale,glyph,false);
    float fit=Math.min(1,Math.min(mc.screen.width/(panel.width()*scale),mc.screen.height/(panel.height()*scale)));
    sourceTextScale*=fit;glyph*=fit;
    panel=Ps4PointerPanelLayout.layout(width*sourceTextScale,height*sourceTextScale,glyph,false);
    float textScale=sourceTextScale*scale;
    float w=Math.max(24,panel.width())*scale,h=Math.max(24,panel.height())*scale;
    float[] anchor=Ps4Information.DATA.cursor.get("PointerTextPanel");
    float x=event.getX()+anchor[4]*scale,y=event.getY()+anchor[5]*scale;
    if(x+w>mc.screen.width)x=event.getX()-w-anchor[4]*scale;
    x=Math.max(0,Math.min(x,mc.screen.width-w));y=Math.max(0,Math.min(y,mc.screen.height-h));
    PoseStack pose=event.getMatrixStack(),old=PortGraphics.pose;PortGraphics.pose=pose;
    GlStateManager.pushMatrix();GlStateManager.translate(0,0,500);GlStateManager.disableDepth();
    try{
      var art=new Ps4UiArt(x,y,scale);art.panel("FJ_PointerTextPanel",0,0,w/scale/36,h/scale/36);
      pose.pushPose();pose.translate(x+panel.textX()*scale,y+panel.textY()*scale,0);pose.scale(textScale,textScale,1);
      var buffers=mc.renderBuffers().bufferSource();int lineY=0;
      for(var component:components){component.renderText(font,0,lineY,pose.last().pose(),buffers);lineY+=component.getHeight();}
      buffers.endBatch();lineY=0;
      for(var component:components){component.renderImage(font,0,lineY,pose,mc.getItemRenderer(),400,mc.getTextureManager());lineY+=component.getHeight();}
      pose.popPose();
    }finally{GlStateManager.enableDepth();GlStateManager.color(1,1,1,1);GlStateManager.popMatrix();PortGraphics.pose=old;}
  }
}

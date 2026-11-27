package dev.legacyconsole.client.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.legacyconsole.client.ClientConfig;
import dev.legacyconsole.port.GlStateManager;
import dev.legacyconsole.port.PortGraphics;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.effect.MobEffectInstance;

/** 0xa4f690/0x8191c0 HUD tiles; 0xa65460/0x81dfa0 inventory bars. */
public final class Ps4Effects {
  private Ps4Effects() {}
  private static List<MobEffectInstance> effects(boolean hud){
    var result=new ArrayList<MobEffectInstance>();var player=Minecraft.getInstance().player;
    if(player!=null)for(var e:player.getActiveEffects())if(!hud||e.showIcon())result.add(e);
    if(hud)result.sort(Comparator.comparingInt(MobEffectInstance::getDuration).reversed());
    else result.sort(Comparator.comparingInt(e->Ps4Information.DATA.effect_order.getOrDefault(Integer.toString(nativeId(e)),nativeId(e))));
    return result;
  }
  private static int nativeId(MobEffectInstance effect){
    String key=effect.getEffect().getDescriptionId().replace("effect.minecraft.","");
    String name=switch(key){case "slow_falling"->"slow_fall";case "conduit_power"->"conduit";
      case "hero_of_the_village"->"village_hero_effect";case "bad_omen"->"bad_omen_effect";
      case "unluck"->"badluck";default->key;};
    for(var entry:Ps4Information.DATA.effect_icons.entrySet()){
      String icon=entry.getValue().toLowerCase(Locale.ROOT).replace("potion_effect_icon_","").replace("_small","");
      if(icon.equals(name))return Integer.parseInt(entry.getKey());
    }
    return 30+net.minecraft.world.effect.MobEffect.getId(effect.getEffect());
  }
  private static void icon(MobEffectInstance e,float x,float y,float scale,float alpha){
    String symbol=Ps4Information.DATA.effect_icons.get(Integer.toString(nativeId(e)));
    GlStateManager.color(1,1,1,alpha);
    if(symbol!=null){var sprite=Ps4UiArt.DATA.sprites.get(symbol);
      ConsoleTheme.regionSprite("ps4/workstations/"+sprite.texture,x,y,sprite.size[0]*scale,sprite.size[1]*scale,0,0,1,1,alpha);
    }else{
      // Registry effects added by Java/mods use their registered sprite; there
      // is no fabricated PS4 icon for an effect absent from the source build.
      var mc=Minecraft.getInstance();var sprite=mc.getMobEffectTextures().get(e.getEffect());
      com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0,sprite.atlas().location());
      GlStateManager.pushMatrix();GlStateManager.translate(x,y,0);GlStateManager.scale(scale,scale,1);
      net.minecraft.client.gui.GuiComponent.blit(PortGraphics.pose,0,0,0,18,18,sprite);GlStateManager.popMatrix();
    }
    GlStateManager.color(1,1,1,1);
  }
  public static void hud(PoseStack pose,int width,int height){
    var mc=Minecraft.getInstance();if(!ClientConfig.consoleHud||mc.player==null||mc.screen!=null||mc.options.hideGui)return;
    PoseStack old=PortGraphics.pose;PortGraphics.pose=pose;
    float scale=Math.min(width/1920f,height/1080f)*Ps4UiScale.factor(ClientConfig.hudSize);
    int good=0,bad=0;float edgeX=width*.05f,edgeY=height*.05f;
    try{for(var e:effects(true)){
      boolean beneficial=e.getEffect().isBeneficial();int col=beneficial?good++:bad++;
      float x=width-edgeX-(col+1)*70*scale,y=edgeY+(beneficial?0:70*scale);
      var art=new Ps4UiArt(x,y,scale);art.panel("FJ_PointerTextPanel",0,0,70f/36,70f/36);
      icon(e,x+6*scale,y+6*scale,3*scale,alpha(e.getDuration()));
    }}finally{PortGraphics.pose=old;}
  }
  public static float alpha(int ticks){
    return Ps4InformationLayout.effectAlpha(ticks);
  }
  static void inventory(Ps4UiArt menu){
    var effects=effects(false);if(effects.isEmpty())return;
    var data=Ps4Information.DATA;float dx=data.inventory_offset[0],dy=data.inventory_offset[1];
    float available=(Minecraft.getInstance().getWindow().getGuiScaledWidth()-menu.x)/menu.scale-dx;
    // The source has a small widget as well as the normal widget. Use it when
    // the right-hand margin cannot contain the normal bar (e.g. 4:3 windows).
    boolean small=available<390;float shrink=small?2f/3:1;
    float scale=menu.scale*Math.min(1,available/(390*shrink));
    if(scale<=0)return;
    var art=new Ps4UiArt(menu.x+dx*menu.scale,menu.y+dy*menu.scale,scale);
    float first=data.inventory_anchors.get("Effect1")[5],second=data.inventory_anchors.get("Effect2")[5],seventh=data.inventory_anchors.get("Effect7")[5];
    for(int i=0;i<effects.size();i++){
      var e=effects.get(i);float y=Ps4InformationLayout.effectY(i,effects.size(),first,second,seventh);
      var widget=small?data.small:data.normal;float[] panel=widget.get("PointerTextPanel");
      art.panel("FJ_PointerTextPanel",0,y,16.583419799804688f*panel[0],2.625f*panel[3]);
      float[] icon=widget.get("Icon");icon(e,art.x+icon[4]*art.scale,art.y+(y+icon[5])*art.scale,icon[0]*art.scale,1);
      String name=I18n.get(e.getEffect().getDescriptionId());
      if(e.getAmplifier()>0)name+=" "+I18n.get("potion.potency."+e.getAmplifier());
      float[] n=widget.get("EffectName"),t=widget.get("EffectDuration");
      var nameStyle=data.effect_labels.get(small?"FJ_Label_MobEffectNameSmall":"FJ_Label_MobEffectName");
      var timeStyle=data.effect_labels.get(small?"FJ_Label_MobEffectTimeSmall":"FJ_Label_MobEffectTime");
      art.text(name,art.rect(n[4],y+n[5],small?260:315,24),Integer.rotateRight((int)nameStyle.color,8),false,nameStyle.font_size/10);
      art.text(e.isNoCounter()?"**:**":Ps4InformationLayout.duration(e.getDuration()),art.rect(t[4],y+t[5],300,24),Integer.rotateRight((int)timeStyle.color,8),false,timeStyle.font_size/10);
    }
  }
}

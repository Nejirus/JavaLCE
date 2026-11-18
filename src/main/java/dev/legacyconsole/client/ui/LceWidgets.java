package dev.legacyconsole.client.ui;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.player.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.server.*;
import net.minecraft.client.resources.language.*;
import net.minecraft.client.sounds.*;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.model.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.level.saveddata.maps.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.flat.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.trading.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;
import net.minecraft.resources.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.sounds.*;
import net.minecraft.util.*;
import net.minecraft.server.*;
import net.minecraft.server.level.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.server.IntegratedServer;
import dev.legacyconsole.port.*;
import dev.legacyconsole.port.Gui;
import dev.legacyconsole.port.GlStateManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import dev.legacyconsole.port.ReflectionHelper;





/** FJ_CheckBox, FJ_TextInput and FJ_Slider art extracted from the original SD/HD graphics skins. */
public final class LceWidgets {
  private static final java.util.Map<GuiButton,float[]> EXACT = new java.util.WeakHashMap<>();
  private LceWidgets() {}

  static <T extends GuiButton> T place(T button,float x,float y,float w,float h) {
    EXACT.put(button,new float[] {x,y,w,h});
    button.xPosition=(int)Math.floor(x); button.yPosition=(int)Math.floor(y);
    button.width=(int)Math.ceil(x+w)-button.xPosition;
    button.height=(int)Math.ceil(y+h)-button.yPosition;
    return button;
  }

  static float[] exact(GuiButton button) { return EXACT.get(button); }

  static float[] bounds(GuiButton button) {
    float[] exact=exact(button);
    if (exact!=null) return exact;
    int[] r=ConsoleTheme.buttonBounds(button);
    return new float[] {r[0],r[1],r[2],r[3]};
  }

  static String path(String name) {
    return "lce/widgets/" + (Minecraft.getInstance().getWindow().getHeight() >= 1080 ? "hd/" : "sd/") + name;
  }

  /** The same original list-row artwork used by saved worlds and join players. */
  static void listButton(float[] bounds,boolean focused){
    String art=focused?path("list_button_selected"):
        "lce/play/"+(Minecraft.getInstance().getWindow().getHeight()>=1080?"hd/":"sd/")+"list_button";
    ConsoleTheme.spriteAt(art,bounds[0],bounds[1],bounds[2],bounds[3]);
  }

  public static final class Toggle {
    public final String label;
    public final boolean checked;
    Toggle(String label, boolean checked) { this.label = label; this.checked = checked; }
  }

  /** Presentation only. The original option callback remains the sole owner of its value. */
  public static Toggle toggle(String text) {
    if (text == null) return null;
    String plain = ChatFormatting.stripFormatting(text);
    int colon = plain.lastIndexOf(':');
    if (colon < 0) return null;
    String value = plain.substring(colon + 1).trim();
    String suffix = "";
    if (value.endsWith(" (Locked)")) { value = value.substring(0,value.length()-9); suffix = " (Locked)"; }
    if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true"))
      return new Toggle(plain.substring(0,colon).trim()+suffix,true);
    if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("false"))
      return new Toggle(plain.substring(0,colon).trim()+suffix,false);
    return null;
  }

  static void checkbox(GuiButton button, int[] bounds, boolean focus, Toggle state) {
    checkbox(button,new float[]{bounds[0],bounds[1],bounds[2],bounds[3]},focus,state);
  }

  static void checkbox(GuiButton button, float[] bounds, boolean focus, Toggle state) {
    float x = bounds[0], y = bounds[1] + (bounds[3] - 12) / 2f;
    ConsoleTheme.spriteAt(path(focus ? "checkbox_selected" : "checkbox"),x,y,12,12);
    if (state.checked) ConsoleTheme.spriteAt(path("tick"),x,y,14,12);
    FontCompat.get().drawString(ConsoleTheme.fit(state.label,(int)bounds[2]-16),
        bounds[0]+13,bounds[1]+(bounds[3]-8)/2,
        button.enabled ? (focus ? 0xffffff00 : ConsoleTheme.DARK) : 0xff777777,false);
  }

  static void button(GuiButton button,float[] r,boolean focus) {
    Toggle toggle=toggle(button.displayString);
    if (toggle!=null && button instanceof ConsoleButton && ((ConsoleButton)button).allowCheckbox) {
      checkbox(button,r,focus,toggle);return;
    }
    LceMenuButton.draw(button,r,focus);
  }

  /** FJ_Slider masks an unscaled 300-unit track and keeps its handle eight units wide. */
  static void slider(float[] bounds, double value, boolean focus, boolean enabled) {
    slider(bounds,value,focus,enabled,path(""));
  }
  static void slider(float[] bounds,double value,boolean focus,boolean enabled,String prefix){
    float x=bounds[0], y=bounds[1], w=bounds[2], h=bounds[3];
    float fraction=Math.min(1,w/300f);
    if (focus) {
      // FJ_Slider_Outline scales MainMenuButton_Outline to 302 by 18 logical units,
      // then masks the middle while retaining the original one-unit outer edge.
      ConsoleTheme.regionSprite(prefix+"slider_outline",x-1,y-1,w+1,h+2,
          0,0,(w+1)/302f,1);
      ConsoleTheme.regionSprite(prefix+"slider_outline",x+w,y-1,1,h+2,301f/302f,0,1,1);
    }
    ConsoleTheme.regionSprite(prefix+"slider",x,y,w,h,0,0,fraction,1);
    if (focus) {
      ConsoleTheme.regionSprite(prefix+"slider_highlight",x+1,y+1,w-2,h-2,0,0,fraction,1);
    }
    float thumb=x+(float)Math.max(0,Math.min(1,value))*(w-8);
    ConsoleTheme.spriteAt(prefix+"slider_handle",thumb,y,8,h);
    ConsoleTheme.regionSprite(prefix+(focus ? "slider_tint_active" : "slider_tint_inactive"),
        x,y,w,h,0,0,fraction,1);
    // The original terminator keeps the right edge after the background mask clips its width.
    ConsoleTheme.regionSprite(prefix+"slider",x+w-1,y,1,h,299f/300f,0,1,1);
    GlStateManager.color(1,1,1,1);
  }

  static void textInput(float x, float y, float w, float h, String text, boolean focus, boolean enabled) {
    if (focus) ConsoleTheme.spriteAt(path("text_selected"),x-1,y-1,w+2,h+2);
    ConsoleTheme.spriteAt(path("text_border"),x,y,w,h);
    ConsoleTheme.spriteAt(path("text_background"),x+1,y+1,w-2,h-2);
    FontCompat.get().drawString(ConsoleTheme.fit(text,(int)w-6),x+3,y+(h-8)/2,
        enabled ? 0xffffffff : 0xff777777,false);
  }

  static void label(String text,float x,float y,int color) {
    FontCompat.get().drawString(text,x,y,color,false);
  }

  /** The exact source DefineScalingGrid caps, in source pixels. */
  static void panel(String name, float x, float y, float w, float h) {
    panel(name,x,y,w,h,1);
  }

  static void panel(String name, float x, float y, float w, float h,float alpha) {
    boolean hd=Minecraft.getInstance().getWindow().getHeight()>=1080;
    float units=hd?3:2, size, left, right, top, bottom;
    if (name.equals("panel")) {
      size=hd?159:96;left=hd?48:32;right=hd?63:32;top=hd?53:32;bottom=top;
    } else if(name.equals("panel16")) {
      size=hd?72:48;left=right=top=bottom=hd?24:16;
    } else if(name.equals("square_recess")) {
      size=hd?63:48;left=right=top=bottom=hd?21:16;
    } else if (name.equals("pointer_panel")) {
      size=hd?36:24;left=right=top=bottom=hd?12:8;
    } else {
      size=hd?66:44;left=top=hd?22:15;right=bottom=hd?22:14;
    }
    float[] us={0,left/size,1-right/size,1}, vs={0,top/size,1-bottom/size,1};
    float[] xs={x,x+left/units,x+w-right/units,x+w};
    float[] ys={y,y+top/units,y+h-bottom/units,y+h};
    for (int row=0;row<3;row++) for(int col=0;col<3;col++)
      ConsoleTheme.regionSprite(path(name),xs[col],ys[row],xs[col+1]-xs[col],ys[row+1]-ys[row],
          us[col],vs[row],us[col+1],vs[row+1],alpha);
    GlStateManager.color(1,1,1,1);
  }
}

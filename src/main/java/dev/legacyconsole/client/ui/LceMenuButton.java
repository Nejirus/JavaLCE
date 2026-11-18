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

import java.util.WeakHashMap;



/** FJ_MenuButton / FJ_Button timelines and original skinGraphics pixels. */
final class LceMenuButton {
  private static final float[] FIRST = {1,.921875f,.8515625f,.78125f,.71875f,.6484375f,
      .6015625f,.6484375f,.7109375f,.76171875f,.8203125f,.859375f,.91015625f,.94921875f,1};
  private static final WeakHashMap<Screen, State> STATES = new WeakHashMap<>();

  private static final class State {
    final long entered = System.nanoTime();
    int initial = Integer.MIN_VALUE, pressed = Integer.MIN_VALUE;
    long pressedAt;
  }

  private LceMenuButton() {}

  static void enter(Screen screen) { STATES.put(screen,new State()); }

  static void press(GuiButton button) {
    Screen screen=Minecraft.getInstance().screen;
    if(screen==null)return;
    State state=STATES.computeIfAbsent(screen,s->new State());
    state.pressed=button.id;state.pressedAt=System.nanoTime();
  }

  static void draw(GuiButton button,float[] r,boolean focused) {
    Minecraft mc=Minecraft.getInstance();
    State state=STATES.computeIfAbsent(mc.screen,s->new State());
    if(focused && state.initial==Integer.MIN_VALUE)state.initial=button.id;
    long now=System.nanoTime();
    // Active_Pressed occupies eleven frames of the original 30 Hz timeline.
    boolean pressed=button.enabled && state.pressed==button.id
        && now-state.pressedAt<11_000_000_000L/30;
    boolean key=button instanceof ConsoleButton && ((ConsoleButton)button).keyboardKey;
    if(key) {
      // FJ_KeyboardButtons uses TextInputBG and LayoutButton_Outline, not the wide menu face.
      if(focused)ConsoleTheme.spriteAt(LceWidgets.path("key_outline"),r[0]-1,r[1]-1,r[2]+2,r[3]+2);
      float inset=pressed?1:0;
      ConsoleTheme.spriteAt(LceWidgets.path(((ConsoleButton)button).keyboardControl
          ?"text_border":"text_background"),r[0]+inset,r[1]+inset,r[2]-2*inset,r[3]-2*inset);
    } else ConsoleTheme.spriteAt(LceWidgets.path("menu_button"),r[0],r[1],r[2],r[3]);
    if(focused && !pressed && !key) {
      int frame=(int)((now-state.entered)*30/1_000_000_000L);
      float alpha=button.enabled?1:.6484375f;
      if(button.enabled && state.initial==button.id && frame>=0 && frame<FIRST.length)
        alpha=FIRST[frame];
      ConsoleTheme.regionSprite(LceWidgets.path("menu_button_selected"),r[0],r[1],r[2],r[3],
          0,0,1,1,alpha);
    }
    int color=button.enabled?(focused&&!pressed?0xffffff00:0xffffffff)
        :(focused?0xffffffa0:0xffcccccc);
    if(button.enabled && button instanceof ConsoleButton console && console.textColor!=null)color=console.textColor;
    String label=ChatFormatting.stripFormatting(button.displayString);
    LcePromptFont font=LcePromptFont.INSTANCE;
    label=font.fit(label,Math.max(0,(int)r[2]-8));
    // FJ_MenuButton::Init requests centered labels; use the installed runtime bitmap font.
    float[] ink=font.ink(label);
    float x=r[0]+(r[2]-ink[0]-ink[1])/2;
    float y=r[1]+(r[3]-7)/2-4;
    float units=mc.getWindow().getHeight()>=1080?3:2;
    // FJ_Label's zero-blur, black DropShadowFilter: distance 1, angle 0.785385.
    ConsoleTheme.promptLabel(label,x+(float)Math.cos(.7853851318359375)/units,
        y+(float)Math.sin(.7853851318359375)/units,0xff000000,false);
    ConsoleTheme.promptLabel(label,x,y,color,false);
  }
}

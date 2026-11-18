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





/** The original fully-open 1080p create/load scenes, converted to the 640x360 canvas. */
final class ConsoleLaunchLayout {
  final float x,y,w=245,h,sidebarX,sidebarY,sidebarW=167,sidebarH=216;
  private final boolean create;

  ConsoleLaunchLayout(int width,int height,boolean create) {
    this.create=create;
    float dx=(width-640)/2f,dy=(height-360)/2f;
    x=dx+(create?355:340)/3f;
    y=dy+(create?225:255)/3f;
    h=(create?675:682)/3f;
    sidebarX=dx+(create?1068:1053)/3f;
    sidebarY=dy+(create?237:270)/3f;
  }

  <T extends GuiButton> T place(T button,String control) {
    float rx,ry,rw=220,rh=20;
    if (create) {
      switch(control) {
        case "name":rx=38;ry=69;rw=218;rh=18;break;
        case "mode":rx=36;ry=143;break;
        case "difficulty":rx=38;ry=221;rw=.72845458984375f*300;rh=16;break;
        case "packs":rx=36;ry=324;rh=35.333f;break;
        case "online":rx=41;ry=447;rh=12;break;
        case "more":rx=36;ry=501;break;
        case "launch":rx=36;ry=578;break;
        default:throw new IllegalArgumentException(control);
      }
    } else {
      switch(control) {
        case "mode":rx=37;ry=185;break;
        case "difficulty":rx=39;ry=258;rw=.728485107421875f*300;rh=16;break;
        case "packs":rx=37;ry=356;rh=35.333f;break;
        case "online":rx=42;ry=472;rh=12;break;
        case "more":rx=37;ry=519;break;
        case "launch":rx=37;ry=589;break;
        default:throw new IllegalArgumentException(control);
      }
    }
    return LceWidgets.place(button,x+rx/3,y+ry/3,rw,rh);
  }

  void render(ConsolePacks.Selection packs) {
    // Source depth order: the description is below the main form. It starts open.
    packs.sidebar(sidebarX,sidebarY,sidebarW,sidebarH,create);
    LceWidgets.panel("panel",x,y,w,h);
  }
}

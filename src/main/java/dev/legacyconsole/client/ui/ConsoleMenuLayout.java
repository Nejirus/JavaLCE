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

import java.util.List;



/** LCE's 720p pause/settings scene coordinates projected into Minecraft GUI units. */
final class ConsoleMenuLayout extends Gui {
  private static final ResourceLocation LOGO =
      new ResourceLocation("textures/gui/title/minecraft.png");

  private static final ConsoleMenuLayout DRAW = new ConsoleMenuLayout();

  private ConsoleMenuLayout() {}

  static void background(int width, int height) {
    if (Minecraft.getInstance().level == null) ConsoleTheme.background(width, height);
    // MenuBackground720.swf: original solid black fill, alpha 0x3f.
    else drawRect(0, 0, width, height, 0x3f000000);
  }

  static void logo(int width, int height) {
    PortGraphics.bind(LOGO);
    GlStateManager.color(1, 1, 1, 1);
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    int y = Math.round(LceViewport.y(width,height,56));
    DRAW.drawTexturedModalRect(width / 2 - 137, y, 0, 0, 155, 44);
    DRAW.drawTexturedModalRect(width / 2 + 18, y, 0, 45, 119, 44);
  }

  static void rows(List<GuiButton> buttons, int width, int height, int buttonWidth) {
    int top = Math.round(LceViewport.y(width,height,250));
    int pitch =
        buttons.size() < 2
            ? 25
            : Math.min(25, Math.max(18, (height - 32 - top - 20) / (buttons.size() - 1)));
    for (int i = 0; i < buttons.size(); i++) {
      GuiButton b = buttons.get(i);
      b.xPosition = (width - buttonWidth) / 2;
      b.yPosition = top + i * pitch;
      b.width = buttonWidth;
      b.height = 20;
    }
  }

  static GuiButton activation(Screen screen, List<GuiButton> buttons) {
    GuiButton b = MenuFocus.vertical(buttons, MenuFocus.id(screen), 0);
    if (b != null) b.playPressSound(Minecraft.getInstance().getSoundManager());
    return b;
  }
}

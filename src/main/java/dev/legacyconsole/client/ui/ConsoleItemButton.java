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

import dev.legacyconsole.client.ConsoleAudio;
import dev.legacyconsole.client.input.ControllerCursor;



final class ConsoleItemButton extends GuiButton {
  ItemStack stack;
  boolean selected;
  boolean warning;
  final boolean verticalTab;
  String tabEdge = "middle";

  ConsoleItemButton(int id, int x, int y, int size, ItemStack stack, boolean tab) {
    super(id, x, y, size, size, "");
    this.stack = stack;
    verticalTab = tab;
  }

  @Override
  public void drawButton(Minecraft mc, int mx, int my) {
    // The container paints side tabs around its frame, in two separate passes.
    if (!visible || verticalTab) return;
    hovered =
        (!ControllerCursor.active() || ControllerCursor.pointerMode())
            && mx >= xPosition
            && my >= yPosition
            && mx < xPosition + width
            && my < yPosition + height;
    ConsoleTheme.sprite(
        warning ? "gui/sprites/container/red_icon_holder" : "lce/crafting_slot",
        xPosition,
        yPosition,
        width,
        height);
    ConsoleTheme.item(stack, xPosition + (width - 16) / 2, yPosition + (height - 16) / 2);
    if ((enabled && hovered || selected) && !verticalTab)
      ConsoleTheme.sprite(
          "gui/sprites/container/select_icon_highlight",
          xPosition - 2,
          yPosition - 2,
          width + 4,
          height + 4);
  }

  void drawTab() {
    if (!visible) return;
    ConsoleTheme.spriteSize(
        "gui/sprites/tiles/" + (selected ? "high" : "low") + "_vert_tab_" + tabEdge,
        xPosition,
        yPosition,
        width - .5f,
        height);
    // The frame-facing extension must not move the visible icon.
    ConsoleTheme.item(stack, xPosition + (31 - 16) / 2, yPosition + (height - 16) / 2);
  }

  @Override
  public void playPressSound(SoundManager h) {
    ConsoleAudio.play("focus");
  }
}

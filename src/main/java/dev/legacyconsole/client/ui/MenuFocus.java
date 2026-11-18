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
import dev.legacyconsole.client.input.ControllerMenus;
import java.util.*;



/** A single focus owner; pointer hover and controller focus never compete. */
public final class MenuFocus {
  private static Screen screen;
  private static int id = Integer.MIN_VALUE;

  private MenuFocus() {}

  /** LCE's explicit NavUp/NavDown links wrap at the first and last visible row. */
  public static GuiButton vertical(List<GuiButton> buttons, int current, int delta) {
    GuiButton origin = null, first = null;
    for (GuiButton b : buttons) {
      if (!b.enabled || !b.visible) continue;
      if (b.id == current) origin = b;
      if (first == null
          || b.yPosition < first.yPosition
          || b.yPosition == first.yPosition && b.xPosition < first.xPosition) first = b;
    }
    if (origin == null || delta == 0) return origin == null ? first : origin;
    GuiButton next = null, wrap = null;
    float best = Float.MAX_VALUE, wrapAcross = Float.MAX_VALUE;
    int x = origin.xPosition + origin.width / 2, y = origin.yPosition + origin.height / 2;
    for (GuiButton b : buttons) {
      if (!b.enabled || !b.visible) continue;
      int bx = b.xPosition + b.width / 2, by = b.yPosition + b.height / 2;
      int dy = by - y, across = Math.abs(bx - x);
      if ((delta > 0 && dy > 3) || (delta < 0 && dy < -3)) {
        float score = Math.abs(dy) + across * 3;
        if (score < best) {
          best = score;
          next = b;
        }
      }
      int wy = wrap == null ? 0 : wrap.yPosition + wrap.height / 2;
      if (wrap == null || (delta > 0 ? by < wy : by > wy) || by == wy && across < wrapAcross) {
        wrap = b;
        wrapAcross = across;
      }
    }
    return next == null ? wrap : next;
  }

  public static boolean move(Screen owner, List<GuiButton> buttons, int delta) {
    GuiButton next = vertical(buttons, id(owner), delta);
    if (next == null) return false;
    int previous = id(owner);
    ControllerCursor.navigation();
    ControllerMenus.focus(owner, next.id);
    if (previous != next.id) ConsoleAudio.play("focus");
    return true;
  }

  public static void enter(Screen next, List<GuiButton> buttons) {
    LceMenuButton.enter(next);
    screen = next;
    id = Integer.MIN_VALUE;
    GuiButton first = null;
    for (GuiButton b : buttons)
      if (b.enabled
          && b.visible
          && (first == null
              || b.yPosition < first.yPosition
              || b.yPosition == first.yPosition && b.xPosition < first.xPosition)) first = b;
    if (first != null) id = first.id;
  }

  public static void set(Screen owner, int value) {
    screen = owner;
    id = value;
  }

  public static int id(Screen owner) {
    return screen == owner ? id : Integer.MIN_VALUE;
  }

  public static boolean focused(GuiButton b, boolean hover) {
    return ControllerCursor.active() && !ControllerCursor.pointerMode()
        ? screen == Minecraft.getInstance().screen && id == b.id
        : hover;
  }

  public static GuiButton button(Screen owner) {
    List<GuiButton> buttons =
        LegacyWidgets.buttons(owner);
    for (GuiButton b : buttons) if (b.id == id(owner) && b.enabled && b.visible) return b;
    return null;
  }
}

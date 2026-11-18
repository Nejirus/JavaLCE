package dev.legacyconsole.client.input;
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

import dev.legacyconsole.LegacyConsole;
import dev.legacyconsole.client.ui.ConsoleTheme;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;



/** Own the native cursor only while the controller is the active input device. */
public final class ControllerCursor {
  private static Cursor blank, original;
  private static boolean controller, hidden, pointer, startupHold;
  private static int activation;
  private static final java.util.Deque<long[]> warps = new java.util.ArrayDeque<>();

  public static int activation() {
    return activation;
  }

  private ControllerCursor() {}

  public static boolean pointerMode() {
    return controller && pointer;
  }

  public static boolean active() {
    return controller;
  }

  public static void controller(boolean showPointer) {
    if (!controller) activation++;
    controller = true;
    pointer = showPointer;
    update();
  }

  public static void navigation() {
    controller(false);
  }

  public static void mouse() {
    controller = false;
    pointer = false;
    update();
  }

  public static void adoptStartup(Cursor startup, Cursor previous) {
    blank = startup;
    original = previous;
    hidden = startupHold = true;
    rememberWarp(Mouse.getX(), Mouse.getY());
    rememberWarp(Minecraft.getInstance().getWindow().getWidth() / 2,
        Minecraft.getInstance().getWindow().getHeight() / 2);
  }

  /** Called only after polling, so a connected neutral controller never exposes the cursor. */
  public static void ready() {
    startupHold = false;
    update();
  }

  public static void ignoreFocusWarp() {
    if (!Mouse.isCreated()) return;
    rememberWarp(Mouse.getX(), Mouse.getY());
    rememberWarp(Minecraft.getInstance().getWindow().getWidth() / 2,
        Minecraft.getInstance().getWindow().getHeight() / 2);
  }

  /** Retire the container pointer before Minecraft changes focus or draws the next screen. */
  public static void screenChanged() {
    pointer = false;
    if (controller) {
      // Vanilla's grab/ungrab recenters the mouse. That synthetic motion is not mouse input.
      ignoreFocusWarp();
      update();
    }
  }

  public static void warp(int x, int y) {
    rememberWarp(x, y);
    Mouse.setCursorPosition(x, y);
  }

  public static void warpGui(float x, float y) {
    Mouse.setGuiPosition(x, y);
    rememberWarp(Mouse.getX(), Mouse.getY());
  }

  private static void rememberWarp(int x, int y) {
    long now = System.nanoTime();
    while (!warps.isEmpty() && warps.peekFirst()[2] < now) warps.removeFirst();
    if (warps.size() > 128) warps.removeFirst();
    // Slow frames can deliver our native cursor events after the old 250 ms limit.
    // Match their coordinates so genuine mouse movement still switches input immediately.
    warps.addLast(new long[] {x, y, now + 2_000_000_000L});
  }

  public static void mouseEvent() {
    boolean action = Mouse.getEventButton() >= 0 || Mouse.getEventDWheel() != 0;
    boolean motion = Mouse.getEventDX() != 0 || Mouse.getEventDY() != 0;
    if (!action && !motion) return;
    if (startupHold && !action) return;
    if (!action) {
      long now = System.nanoTime();
      while (!warps.isEmpty() && warps.peekFirst()[2] < now) warps.removeFirst();
      for (long[] warp : warps)
        if (Math.abs(Mouse.getEventX() - warp[0]) <= 1
            && Math.abs(Mouse.getEventY() - warp[1]) <= 1) return;
    }
    mouse();
  }

  public static void update() {
    if (!Mouse.isCreated()) return;
    dev.legacyconsole.client.loading.ConsoleStartup.handoffCursor();
    if(controller && Minecraft.getInstance().screen!=null)Mouse.setNativeCursor(blank==null?new Cursor(16,16,0,0,1,org.lwjgl.BufferUtils.createIntBuffer(256),null):blank);
    boolean shouldHide = startupHold || controller || dev.legacyconsole.client.loading.ConsoleLoading.visible();
    try {
      if (shouldHide && !hidden) {
        if (blank == null) {
          int size = Cursor.getMinCursorSize();
          IntBuffer pixels = BufferUtils.createIntBuffer(size * size);
          blank = new Cursor(size, size, 0, 0, 1, pixels, null);
        }
        original = Mouse.getNativeCursor();
        Mouse.setNativeCursor(blank);
        hidden = true;
      } else if (!shouldHide && hidden) {
        Mouse.setNativeCursor(original);
        hidden = false;
        original = null;
      }
    } catch (Exception error) {
      LegacyConsole.log.debug("Cannot change native cursor", error);
    }
  }

  public static void draw(int x, int y) {
    update();
    if (!dev.legacyconsole.client.loading.ConsoleLoading.visible()
        && controller
        && pointer
        && ControllerMenus.pointerReady(Minecraft.getInstance().screen)) {
      GlStateManager.disableDepth();
      GlStateManager.pushMatrix();
      GlStateManager.translate(
          ControllerMenus.pointerX(), ControllerMenus.pointerY(), 0);
      ConsoleTheme.sprite("gui/sprites/container/pointer", -8, -8, 16, 16);
      GlStateManager.popMatrix();
    }
  }
}

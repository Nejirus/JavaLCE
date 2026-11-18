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
import dev.legacyconsole.client.ClientConfig;
import dev.legacyconsole.client.ConsoleAudio;
import dev.legacyconsole.client.ui.MenuFocus;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;



public final class ControllerMenus {
  private static final int CLICK=0,RELEASE=1,KEY=2,SLOT_CLICK=3;
  private static Screen lastScreen;
  private static int lastActivation;
  private static final MenuRepeater repeat = new MenuRepeater();
  private static float pointerX, pointerY;
  private static int lastWidth, lastHeight;

  private ControllerMenus() {}

  public static void tick(PadState pad, PadState previous) {
    Minecraft mc = Minecraft.getInstance();
    Screen screen = mc.screen;
    ScaledResolution resolution = new ScaledResolution(mc);
    if (screen != lastScreen) {
      lastScreen = screen;
      ControllerCursor.controller(usesCursor(screen));
      repeat.clear();
      pointerX = resolution.getScaledWidth() / 2f;
      pointerY = resolution.getScaledHeight() / 2f;
      lastWidth = screen.width;
      lastHeight = screen.height;
      List<GuiButton> initialButtons =
          LegacyWidgets.buttons(screen);
      MenuFocus.enter(screen, initialButtons);
      GuiButton first = MenuFocus.button(screen);
      if (first != null) {
        pointerX = first.xPosition + first.width / 2f;
        pointerY = first.yPosition + first.height / 2f;
      }
      if (screen instanceof ControllerScreen) ((ControllerScreen) screen).controllerOpened();
      position();
    } else if (lastActivation != ControllerCursor.activation()) {
      if (usesCursor(screen)) {
        pointerX = (float)Mouse.guiX();
        pointerY = (float)Mouse.guiY();
      } else if (!(screen instanceof GuiContainer)) {
        GuiButton focused = MenuFocus.button(screen);
        if (focused != null) {
          pointerX = focused.xPosition + focused.width / 2f;
          pointerY = focused.yPosition + focused.height / 2f;
        }
      }
    }
    if (screen.width != lastWidth || screen.height != lastHeight) {
      Slot hovered = screen instanceof GuiContainer container ? container.getSlotUnderMouse() : null;
      if (usesCursor(screen) && hovered != null && hovered.isActive()) {
        var container = (GuiContainer)screen;
        float[] bounds = container.consoleSlotBounds(hovered);
        pointerX = bounds[0] + bounds[2]/2f;
        pointerY = bounds[1] + bounds[3]/2f;
      } else {
        pointerX += (screen.width-lastWidth)/2f;
        pointerY += (screen.height-lastHeight)/2f;
      }
      pointerX = InputMath.clamp(pointerX,1,screen.width-2);
      pointerY = InputMath.clamp(pointerY,1,screen.height-2);
      lastWidth = screen.width;
      lastHeight = screen.height;
      position();
    }
    lastActivation = ControllerCursor.activation();
    if (screen instanceof ControllerScreen)
      ((ControllerScreen) screen).controllerTick(pad, previous);
    boolean analogCursor = usesCursor(screen);
    if (analogCursor) {
      ControllerCursor.controller(true);
      // Center a stopped pointer before handling a pickup in this same tick.
      movePointer(pad, 0);
    }
    int direction =
        pad.held(PadState.UP) || (!analogCursor && pad.ly > .6f)
            ? PadState.UP
            : pad.held(PadState.DOWN) || (!analogCursor && pad.ly < -.6f)
                ? PadState.DOWN
                : pad.held(PadState.LEFT) || (!analogCursor && pad.lx < -.6f)
                    ? PadState.LEFT
                    : pad.held(PadState.RIGHT) || (!analogCursor && pad.lx > .6f)
                        ? PadState.RIGHT
                        : 0;
    if (repeat.tick(System.nanoTime(), direction)) {
      ControllerCursor.navigation();
      if (!(screen instanceof ControllerScreen)
          || !((ControllerScreen) screen).controllerDirection(direction))
        navigate(screen, direction);
    }
    for (int button :
        new int[] {
          PadState.CROSS,
          PadState.CIRCLE,
          PadState.SQUARE,
          PadState.TRIANGLE,
          PadState.L1,
          PadState.R1,
          PadState.L2,
          PadState.R2,
          PadState.L3,
          PadState.OPTIONS,
          PadState.SHARE
        }) {
      if (!pad.held(button) || previous.held(button)) continue;
      if(button==PadState.CROSS&&dev.legacyconsole.client.loading.ConsoleStartup.confirmBlocked())continue;
      if(button==PadState.CROSS)
        dev.legacyconsole.client.ui.ConsoleTheme.buttonPressed(MenuFocus.button(screen));
      if (!(button == PadState.CROSS && ControllerCursor.pointerMode())
          && screen instanceof ControllerScreen
          && ((ControllerScreen) screen).controllerButton(button)) continue;
      if (button == PadState.CIRCLE || button == PadState.OPTIONS) {
        ConsoleAudio.play("back");
        invoke(KEY, screen, '\0', Keyboard.KEY_ESCAPE);
      } else if (button == PadState.CROSS || button == PadState.SQUARE) {
        int mouseButton = button == PadState.CROSS ? 0 : 1;
        invoke(CLICK, screen, (int) pointerX, (int) pointerY, mouseButton);
        if (mc.screen == screen)
          invoke(RELEASE, screen, (int) pointerX, (int) pointerY, mouseButton);
      } else if (button == PadState.TRIANGLE
          && screen instanceof GuiContainer
          && mc.player != null) {
        GuiContainer container = (GuiContainer) screen;
        for (Slot slot : container.inventorySlots.slots) {
          float[] bounds=container.consoleSlotBounds(slot);
          float x = bounds[0], y = bounds[1];
          if (pointerX >= x && pointerX < x + bounds[2] && pointerY >= y && pointerY < y + bounds[3])
            invoke(SLOT_CLICK, container, slot, slot.index, 0, ClickType.QUICK_MOVE);
        }
      } else if (button == PadState.L1 || button == PadState.R1) {
        invoke(KEY, screen, '\0', button == PadState.L1 ? Keyboard.KEY_LEFT : Keyboard.KEY_RIGHT);
      } else if (button == PadState.L2 || button == PadState.R2) {
        if (!scrollList(screen, button == PadState.L2 ? -30 : 30))
          invoke(KEY, screen, '\0', button == PadState.L2 ? Keyboard.KEY_PRIOR : Keyboard.KEY_NEXT);
      }
      if (mc.screen != screen) break;
    }
  }

  private static boolean usesCursor(Screen screen) {
    return screen instanceof GuiContainer
        && (!(screen instanceof ControllerScreen)
            || ((ControllerScreen) screen).controllerPointer());
  }

  /** Like LCE's scene-owned pointer, only draw after this container has initialized it. */
  static boolean pointerReady(Screen screen) {
    return screen != null && screen == lastScreen && usesCursor(screen);
  }

  public static int renderMouseX(Screen screen,int nativeX) {
    return ControllerCursor.pointerMode() && pointerReady(screen) ? (int)pointerX : nativeX;
  }

  public static int renderMouseY(Screen screen,int nativeY) {
    return ControllerCursor.pointerMode() && pointerReady(screen) ? (int)pointerY : nativeY;
  }

  /** Run at render speed, so the original console crosshair moves smoothly. */
  public static void movePointer(PadState pad, float delta) {
    Minecraft mc = Minecraft.getInstance();
    Screen screen = mc.screen;
    if (screen == null || screen != lastScreen || !ControllerCursor.active() || !usesCursor(screen))
      return;
    boolean inventory = usesCursor(screen);
    float x = inventory && ClientConfig.cursorStick != 1 ? pad.lx : pad.rx;
    float y = inventory && ClientConfig.cursorStick != 1 ? pad.ly : pad.ry;
    if (inventory && ClientConfig.cursorStick == 2) {
      x = InputMath.clamp(pad.lx + pad.rx, -1, 1);
      y = InputMath.clamp(pad.ly + pad.ry, -1, 1);
    }
    if (Math.abs(x) + Math.abs(y) < .001f) {
      snapToSlot((GuiContainer) screen);
      return;
    }
    if (delta <= 0) return;
    ControllerCursor.controller(true);
    float speed = ClientConfig.cursorSpeed
        * dev.legacyconsole.client.ui.LceViewport.cursorInputScale(screen.width,screen.height);
    pointerX =
        InputMath.clamp(pointerX + x * speed * delta, 1, screen.width - 2);
    pointerY =
        InputMath.clamp(pointerY - y * speed * delta, 1, screen.height - 2);
    position();
  }

  /** LCE AbstractContainerMenu: no stick input over a slot snaps to its center. */
  private static void snapToSlot(GuiContainer screen) {
    for (Slot slot : screen.inventorySlots.slots) {
      if (!slot.isActive() || slot.x < 0 || slot.y < 0) continue;
      float[] bounds = screen.consoleSlotBounds(slot);
      float left = bounds[0], top = bounds[1];
      if (pointerX >= left
          && pointerX < left + bounds[2]
          && pointerY >= top
          && pointerY < top + bounds[3]) {
        float x = left + bounds[2] / 2f, y = top + bounds[3] / 2f;
        if (pointerX != x || pointerY != y) {
          pointerX = x;
          pointerY = y;
          position();
        }
        return;
      }
    }
  }

  public static float pointerX() {
    return pointerX;
  }

  public static float pointerY() {
    return pointerY;
  }

  private static boolean scrollList(Screen screen, int amount) {
    for (Class<?> type = screen.getClass(); type != null; type = type.getSuperclass()) {
      for (Field field : type.getDeclaredFields())
        if (GuiSlot.class.isAssignableFrom(field.getType())) {
          try {
            field.setAccessible(true);
            GuiSlot list = (GuiSlot) field.get(screen);
            if (list != null) {
              list.scrollBy(amount);
              return true;
            }
          } catch (IllegalAccessException error) {
            LegacyConsole.log.debug("Cannot scroll menu list", error);
          }
        }
    }
    return false;
  }

  private static void navigate(Screen screen, int direction) {
    List<int[]> targets = new ArrayList<>();
    List<GuiButton> buttons =
        LegacyWidgets.buttons(screen);
    if (!(screen instanceof GuiContainer)
        && (direction == PadState.UP || direction == PadState.DOWN)
        && MenuFocus.move(screen, buttons, direction == PadState.UP ? -1 : 1)) return;
    for (GuiButton button : buttons)
      if (button.enabled && button.visible)
        targets.add(
            screen instanceof GuiContainer container
              ? new int[]{Math.round(container.consoleUiTransform().screenX(button.xPosition+button.width/2f)),Math.round(container.consoleUiTransform().screenY(button.yPosition+button.height/2f))}
              : new int[]{button.xPosition+button.width/2,button.yPosition+button.height/2});
    if (screen instanceof GuiContainer) {
      GuiContainer container = (GuiContainer) screen;
      for (Slot slot : container.inventorySlots.slots)
        if (slot.isActive() && slot.x >= 0 && slot.y >= 0)
          {float[] bounds=container.consoleSlotBounds(slot);targets.add(new int[]{Math.round(bounds[0]+bounds[2]/2),Math.round(bounds[1]+bounds[3]/2)});}
    }
    float best = Float.MAX_VALUE;
    int[] found = null;
    for (int[] target : targets) {
      float dx = target[0] - pointerX, dy = target[1] - pointerY;
      boolean valid =
          direction == PadState.UP
              ? dy < -3
              : direction == PadState.DOWN ? dy > 3 : direction == PadState.LEFT ? dx < -3 : dx > 3;
      if (!valid) continue;
      float along =
          direction == PadState.UP || direction == PadState.DOWN ? Math.abs(dy) : Math.abs(dx);
      float across =
          direction == PadState.UP || direction == PadState.DOWN ? Math.abs(dx) : Math.abs(dy);
      float score = along + across * 3;
      if (score < best) {
        best = score;
        found = target;
      }
    }
    if (found != null) {
      for (GuiButton b : buttons)
        if (b.xPosition + b.width / 2 == found[0] && b.yPosition + b.height / 2 == found[1])
          MenuFocus.set(screen, b.id);
      pointerX = found[0];
      pointerY = found[1];
      position();
      ControllerCursor.controller(usesCursor(screen));
      ConsoleAudio.play("focus");
    } else if (targets.isEmpty())
      invoke(
          KEY,
          screen,
          '\0',
          direction == PadState.UP
              ? Keyboard.KEY_UP
              : direction == PadState.DOWN
                  ? Keyboard.KEY_DOWN
                  : direction == PadState.LEFT ? Keyboard.KEY_LEFT : Keyboard.KEY_RIGHT);
  }

  private static void position() {
    Minecraft mc = Minecraft.getInstance();
    // Focus menus activate controls directly; they never move a mouse cursor.
    // Containers still need native hover coordinates for snapped slot selection.
    if (!(mc.screen instanceof GuiContainer)) return;
    ControllerCursor.warpGui(pointerX,pointerY);
  }

  public static void focus(Screen screen, int id) {
    MenuFocus.set(screen, id);
    GuiButton b = MenuFocus.button(screen);
    if (b != null) pointAt(b.xPosition + b.width / 2, b.yPosition + b.height / 2);
  }

  public static void reset() {
    lastScreen = null;
    ControllerCursor.screenChanged();
  }

  public static void pointAt(int x, int y) {
    pointerX = x;
    pointerY = y;
    position();
  }


  private static void invoke(int method,Screen screen,Object... a) {
    if(method==CLICK)screen.mouseClicked(((Number)a[0]).doubleValue(),((Number)a[1]).doubleValue(),(Integer)a[2]);
    else if(method==RELEASE)screen.mouseReleased(((Number)a[0]).doubleValue(),((Number)a[1]).doubleValue(),(Integer)a[2]);
    else if(method==KEY)screen.keyPressed(Keyboard.toGlfw((Integer)a[1]),0,0);
    else if(method==SLOT_CLICK&&screen instanceof GuiContainer container)container.clickSlot((Slot)a[0],(Integer)a[1],(Integer)a[2],(ClickType)a[3]);
  }
}

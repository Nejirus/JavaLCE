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

import com.studiohartman.jamepad.Configuration;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;
import dev.legacyconsole.LegacyConsole;
import dev.legacyconsole.client.ClientConfig;
import java.util.HashSet;
import java.util.Set;



public final class ControllerInput {
  public static final ControllerInput INSTANCE = new ControllerInput();
  public PadState state = PadState.EMPTY, previous = PadState.EMPTY;
  public boolean sprint, shiftKeyDown;
  public String status = "SDL2 waiting to start";
  private ControllerManager manager;
  private boolean attempted, neutralRequired, wasActive;
  private int active = -1;
  private long renderTime;
  private final Set<KeyMapping> injected = new HashSet<>();
  private final UseActionBuffer useBuffer = new UseActionBuffer();
  private final MenuInputBuffer menuBuffer = new MenuInputBuffer();
  private final BoatUseControl vehicleUse = new BoatUseControl();
  private final HotbarRepeater hotbar = new HotbarRepeater();
  private Object lastWorld;
  private boolean wasSleeping, wasRidingVehicle, useBlocked, menuInputBlocked;

  private ControllerInput() {}

  /** Input belongs to the intro scene while its loading cover is active. */
  public boolean startupConfirmHeld() {
    previous=state;state=poll();
    if(state.connected){ControllerCursor.navigation();ControllerCursor.ready();}
    return Display.isActive()&&state.connected&&state.held(PadState.CROSS);
  }

  /** Detect before the first main-menu draw; no action is dispatched during initialization. */
  public void prepareMenu() {
    state = poll();
    previous = state;
    if (state.connected) {
      ControllerCursor.ignoreFocusWarp();
      ControllerCursor.navigation();
    }
    ControllerCursor.ready();
    wasActive = Display.isActive();
  }

  public void tick() {
    Minecraft mc = Minecraft.getInstance();
    if(dev.legacyconsole.client.loading.ConsoleStartup.active()) {
      releaseKeys();startupConfirmHeld();sprint=shiftKeyDown=false;return;
    }
    previous = state;
    state = poll();
    dev.legacyconsole.client.loading.ConsoleStartup.confirmBlocked();
    boolean focused = Display.isActive();
    if (state.connected && (!previous.connected || focused && !wasActive)) {
      ControllerCursor.ignoreFocusWarp();
      ControllerCursor.navigation();
    }
    wasActive = focused;
    ControllerCursor.ready();
    releaseKeys();
    if (mc.level != lastWorld) {
      lastWorld = mc.level;
      useBuffer.clear();
      menuBuffer.clear();
      vehicleUse.clear();
      hotbar.clear();
      wasSleeping = false;
      wasRidingVehicle = false;
    }
    boolean sleeping = mc.player != null && mc.player.isSleeping();
    if (wasSleeping && !sleeping) blockUseAfterWake();
    wasSleeping = sleeping;
    if (!sleeping)
      useBlocked =
          useBuffer.tick(state.held(PadState.L2) || physical(mc.options.keyUse));
    if (useBlocked) suppressUseKey();
    menuInputBlocked =
        menuBuffer.tick(state.held(PadState.CROSS) || physical(mc.options.keyJump));
    if (menuInputBlocked) {
      suppress(mc.options.keyAttack);
      suppressUseKey();
    }
    if (menuInputBlocked || menuBuffer.blocksJump()) suppress(mc.options.keyJump);
    boolean ridingVehicle =
        mc.player != null && ConsoleVehicle.isVehicle(mc.player.getVehicle());
    if (ridingVehicle && !wasRidingVehicle && state.connected && mc.gui != null)
      mc.gui.setOverlayMessage(TextComponent.EMPTY,false);
    wasRidingVehicle = ridingVehicle;
    vehicleUse.tick(
        ridingVehicle,
        state.held(PadState.L2) || physical(mc.options.keyUse),
        mc.screen == null
            && mc.mouseHandler.isMouseGrabbed()
            && Display.isActive()
            && !useBlocked
            && !menuInputBlocked);
    if (vehicleUse.blocksUse()) suppressUseKey();
    if (vehicleUse.dismount()) hold(mc.options.keyShift, true);
    int wheel =
        hotbar.tick(
            System.nanoTime(),
            state.held(PadState.L1),
            state.held(PadState.R1),
            state.connected
                && Display.isActive()
                && mc.player != null
                && mc.mouseHandler.isMouseGrabbed()
                && mc.screen == null
                && !menuInputBlocked
                && !pressed(PadState.OPTIONS)
                && !pressed(PadState.SQUARE)
                && !pressed(PadState.TRIANGLE));
    if (!state.connected || !focused) {
      sprint = shiftKeyDown = false;
      ControllerCursor.mouse();
      return;
    }
    if (state.buttons != 0
        || Math.abs(state.lx) + Math.abs(state.ly) + Math.abs(state.rx) + Math.abs(state.ry)
            > .1f) {
      if (!ControllerCursor.active()) ControllerCursor.navigation();
    }
    ControllerCursor.update();
    if (dev.legacyconsole.client.loading.ConsoleLoading.visible()) {
      sprint=shiftKeyDown=false;
      if(pressed(PadState.CIRCLE))dev.legacyconsole.client.loading.ConsoleLoading.cancelConnection();
      return;
    }
    if(dev.legacyconsole.client.Ps4Hints.controller(state,previous)){releaseKeys();return;}
    if (hostShortcut(mc)) return;
    if (mc.screen != null) {
      sprint = false;
      if (ControllerCursor.active()) ControllerMenus.tick(state, previous);
      return;
    }
    if (mc.player == null || !mc.mouseHandler.isMouseGrabbed()) return;
    if (menuInputBlocked) return;
    if (pressed(PadState.OPTIONS)) {
      mc.setScreen(new dev.legacyconsole.client.ui.ConsolePauseMenu());
      return;
    }
    if (pressed(PadState.SQUARE)) {
      dev.legacyconsole.client.ui.ConsoleScreens.craftingOrCreative();
      return;
    }
    if (pressed(PadState.L3)) tap(mc.options.keyTogglePerspective);
    boolean flying = mc.player.getAbilities().flying;
    if (flying || ridingVehicle) shiftKeyDown = false;
    else if (pressed(PadState.R3)) shiftKeyDown = !shiftKeyDown;
    if (state.ly < .65f || mc.player.horizontalCollision) sprint = false;
    hold(
        mc.options.keyJump,
        !ridingVehicle && state.held(PadState.CROSS) && !menuBuffer.blocksJump());
    hold(
        mc.options.keyShift,
        vehicleUse.dismount() || (!ridingVehicle && (flying ? state.held(PadState.R3) : shiftKeyDown)));

    hold(mc.options.keyAttack, state.held(PadState.R2));
    hold(
        mc.options.keyUse,
        state.held(PadState.L2) && !useBlocked && !vehicleUse.blocksUse());
    if (pressed(PadState.R2)) tap(mc.options.keyAttack);
    if (pressed(PadState.L2) && !useBlocked && !vehicleUse.blocksUse())
      tap(mc.options.keyUse);
    if (pressed(PadState.TRIANGLE)) tap(mc.options.keyInventory);
    if (pressed(PadState.CIRCLE)) tap(mc.options.keyDrop);
    if (pressed(PadState.DOWN)) tap(mc.options.keySwapOffhand);
    if (pressed(PadState.UP) || pressed(PadState.TOUCHPAD))
      tap(mc.options.keyTogglePerspective);
    if (pressed(PadState.LEFT))
      mc.player.getInventory().selected = (mc.player.getInventory().selected + 8) % 9;
    if (pressed(PadState.RIGHT))
      mc.player.getInventory().selected = (mc.player.getInventory().selected + 1) % 9;
    if (wheel != 0) mc.player.getInventory().swapPaint(wheel);
  }

  private boolean hostShortcut(Minecraft mc) {
    if(!pressed(PadState.SHARE)||(mc.screen!=null && !(mc.screen instanceof dev.legacyconsole.client.ui.ConsolePauseMenu)))return false;
    dev.legacyconsole.client.ui.ConsoleHostOptions.requestOpen(mc.screen);return true;
  }

  private PadState poll() {
    if (!ClientConfig.controller) {
      status = "Controller disabled";
      return PadState.EMPTY;
    }
    if (!attempted) {
      attempted = true;
      try {
        Configuration config = new Configuration();
        config.maxNumControllers = 4;
        config.loadDatabaseInMemory = true;
        manager =
            new ControllerManager(
                config, "/assets/legacyconsole/controllerdb/gamecontrollerdb.txt");
        manager.initSDLGamepad();
        status = "SDL2 ready - connect a controller";
      } catch (RuntimeException | LinkageError error) {
        manager = null;
        status = "SDL2 unavailable (see latest.log)";
        LegacyConsole.log.warn(
            "Controller initialization failed; keyboard and mouse remain available", error);
      }
    }
    if (manager == null) return PadState.EMPTY;
    try {
      if (active >= 0
          && (ClientConfig.controllerIndex == -1 || active == ClientConfig.controllerIndex)) {
        ControllerState current = manager.getState(active);
        if (current.isConnected) return focus(new PadState(current));
      }
      for (int index = 0; index < 4; index++) {
        if (ClientConfig.controllerIndex >= 0 && ClientConfig.controllerIndex != index) continue;
        ControllerState current = manager.getState(index);
        if (current.isConnected) {
          active = index;
          neutralRequired = true;
          LegacyConsole.log.info(
              "Controller connected: {} (slot {})", current.controllerType, index);
          return focus(new PadState(current));
        }
      }
      active = -1;
      status = "SDL2 ready - no controller connected";
    } catch (RuntimeException | LinkageError error) {
      LegacyConsole.log.warn("Controller polling failed", error);
      manager = null;
      status = "SDL2 polling failed (see latest.log)";
    }
    return PadState.EMPTY;
  }

  private PadState focus(PadState current) {
    status = current.name + " - SDL2";
    if (!Display.isActive()) {
      neutralRequired = true;
      return current.neutral();
    }
    if (neutralRequired) {
      if (current.buttons != 0
          || Math.abs(current.lx)
                  + Math.abs(current.ly)
                  + Math.abs(current.rx)
                  + Math.abs(current.ry)
              > .1f) return current.neutral();
      neutralRequired = false;
    }
    return current;
  }

  public void camera() {
    long now = System.nanoTime();
    float delta = renderTime == 0 ? 0 : Math.min(.05f, (now - renderTime) / 1_000_000_000f);
    renderTime = now;
    if(dev.legacyconsole.client.loading.ConsoleStartup.active())return;
    Minecraft mc = Minecraft.getInstance();
    if (mc.screen != null) {
      ControllerMenus.movePointer(state, delta);
      return;
    }
    if (!state.connected
        || mc.player == null
        || mc.screen != null
        || !mc.mouseHandler.isMouseGrabbed()
        || menuInputBlocked
        || mc.isPaused()) return;
    float yaw = state.rx * Math.abs(state.rx) * ClientConfig.sensitivity * delta;
    float pitch = InputMath.pitch(state.ry, ClientConfig.sensitivity, delta, ClientConfig.invertY);
    mc.player.turn(yaw / .15f, pitch / .15f);
  }

  public boolean pressed(int button) {
    return state.held(button) && !previous.held(button);
  }

  public void blockUseAfterWake() {
    useBuffer.arm();
    useBlocked = true;
    suppressUseKey();
  }

  public void blockAfterMenu() {
    Minecraft mc = Minecraft.getInstance();
    menuBuffer.arm(state.held(PadState.CROSS) || physical(mc.options.keyJump));
    menuInputBlocked = true;
    suppress(mc.options.keyJump);
    suppress(mc.options.keyAttack);
    suppressUseKey();
  }

  public void menuOpened() {
    menuBuffer.clear();
    menuInputBlocked = false;
  }

  public void framePresented() {
    menuBuffer.framePresented();
    menuInputBlocked = menuBuffer.blocksActions();
  }

  public boolean blocksMenuMovement() {
    return menuInputBlocked;
  }

  public boolean blocksMenuJump() {
    return menuInputBlocked || menuBuffer.blocksJump();
  }

  public boolean blocksShieldUse() {
    return useBlocked || menuInputBlocked || vehicleUse.blocksUse();
  }

  private void suppressUseKey() {
    suppress(Minecraft.getInstance().options.keyUse);
  }

  private static void suppress(KeyMapping key) {
    key.setDown(false);
    while (key.consumeClick()) {}
  }

  /** Native mouse events occur after ClientTick.START, so guard their queued use too. */
  public boolean useMouseEvent(int button, boolean down) {
    Minecraft mc = Minecraft.getInstance();
    if (button >= 0
        && down
        && menuInputBlocked
        && mc.screen == null
        && (legacyKey(mc.options.keyAttack) == button - 100
            || legacyKey(mc.options.keyJump) == button - 100)) return true;
    if (button < 0 || legacyKey(mc.options.keyUse) != button - 100) return false;
    if (!down && !state.held(PadState.L2)) {
      useBuffer.clear();
      useBlocked = false;
    }
    return down && (useBlocked || menuInputBlocked) && mc.screen == null;
  }

  public void useKeyEvent(int key, boolean down) {
    Minecraft mc = Minecraft.getInstance();
    if (menuInputBlocked && mc.screen == null) {
      if (key == legacyKey(mc.options.keyAttack))
        suppress(mc.options.keyAttack);
      if (key == legacyKey(mc.options.keyJump)) suppress(mc.options.keyJump);
    }
    if (key != legacyKey(mc.options.keyUse)) return;
    if (!down && !state.held(PadState.L2)) {
      useBuffer.clear();
      useBlocked = false;
    } else if ((useBlocked || menuInputBlocked || vehicleUse.blocksUse())
        && mc.screen == null) suppressUseKey();
  }

  private void hold(KeyMapping key, boolean value) {
    if (value) {
      key.setDown(true);
      injected.add(key);
    }
  }

  private void releaseKeys() {
    for (KeyMapping key : injected) key.setDown(physical(key));
    injected.clear();
  }

  private static boolean physical(KeyMapping key) {
    var code=key.getKey();
    if(code.getType()==com.mojang.blaze3d.platform.InputConstants.Type.MOUSE)return Mouse.isButtonDown(code.getValue());
    return com.mojang.blaze3d.platform.InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(),code.getValue());
  }
  private static int legacyKey(KeyMapping key){return key.getKey().getType()==com.mojang.blaze3d.platform.InputConstants.Type.MOUSE?key.getKey().getValue()-100:Keyboard.fromGlfw(key.getKey().getValue());}

  private static void tap(KeyMapping key) {
    KeyMapping.click(key.getKey());
  }
}

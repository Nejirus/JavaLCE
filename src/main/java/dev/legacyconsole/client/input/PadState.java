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

import com.studiohartman.jamepad.ControllerState;
import dev.legacyconsole.client.ClientConfig;




public final class PadState {
  public static final int CROSS = 1,
      CIRCLE = 2,
      SQUARE = 4,
      TRIANGLE = 8,
      L1 = 16,
      R1 = 32,
      OPTIONS = 64,
      SHARE = 128,
      L3 = 256,
      R3 = 512,
      UP = 1024,
      DOWN = 2048,
      LEFT = 4096,
      RIGHT = 8192,
      L2 = 16384,
      R2 = 32768,
      TOUCHPAD = 65536;
  public static final PadState EMPTY = new PadState();
  public final boolean connected;
  public final int buttons;
  public final float lx, ly, rx, ry;
  public final float movementX,movementY;
  public final String name;

  private PadState() {
    connected = false;
    buttons = 0;
    lx = ly = rx = ry = 0;
    movementX=movementY=0;
    name = "No controller";
  }

  private PadState(String controllerName) {
    connected = true;
    name = controllerName;
    buttons = 0;
    lx = ly = rx = ry = 0;
    movementX=movementY=0;
  }

  public PadState neutral() {
    return connected ? new PadState(name) : EMPTY;
  }

  public PadState(ControllerState state) {
    connected = state.isConnected;
    name = state.controllerType;
    float[] left = InputMath.sdlStick(state.leftStickX, state.leftStickY, ClientConfig.deadzone);
    float[] right = InputMath.sdlStick(state.rightStickX, state.rightStickY, ClientConfig.deadzone);
    float[] movement=InputMath.lceMovementStick(state.leftStickX,state.leftStickY);
    movementX=movement[0];movementY=movement[1];
    lx = left[0];
    ly = left[1];
    rx = right[0];
    ry = right[1];
    buttons =
        (state.a ? CROSS : 0)
            | (state.b ? CIRCLE : 0)
            | (state.x ? SQUARE : 0)
            | (state.y ? TRIANGLE : 0)
            | (state.lb ? L1 : 0)
            | (state.rb ? R1 : 0)
            | (state.start ? OPTIONS : 0)
            | (state.back ? SHARE : 0)
            | (state.leftStickClick ? L3 : 0)
            | (state.rightStickClick ? R3 : 0)
            | (state.dpadUp ? UP : 0)
            | (state.dpadDown ? DOWN : 0)
            | (state.dpadLeft ? LEFT : 0)
            | (state.dpadRight ? RIGHT : 0)
            | (state.leftTrigger > .35f ? L2 : 0)
            | (state.rightTrigger > .35f ? R2 : 0)
            | (state.touchpadButton ? TOUCHPAD : 0);
  }

  public boolean held(int button) {
    return (buttons & button) != 0;
  }
}

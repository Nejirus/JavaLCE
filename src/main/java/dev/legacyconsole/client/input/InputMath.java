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





public final class InputMath {
  private InputMath() {}
  public static final int LCE_AXIS_DEADZONE=10000, LCE_AXIS_MAX=32767;

  /** Orbis 4J_Input: deadzone/subtraction and range scaling are per axis. */
  public static float lceMovementAxis(float value){
    int axis=Math.round(clamp(value,-1,1)*LCE_AXIS_MAX);
    if(axis>LCE_AXIS_DEADZONE)return (axis-LCE_AXIS_DEADZONE)/(float)(LCE_AXIS_MAX-LCE_AXIS_DEADZONE);
    if(axis<-LCE_AXIS_DEADZONE)return (axis+LCE_AXIS_DEADZONE)/(float)(LCE_AXIS_MAX-LCE_AXIS_DEADZONE);
    return 0;
  }

  public static float[] lceMovementStick(float x,float sdlY){
    return new float[]{lceMovementAxis(x),lceMovementAxis(-sdlY)};
  }

  /** SDL reports positive Y downward; gameplay and menu math use positive Y upward. */
  public static float[] sdlStick(float x, float y, float deadzone) {
    return radial(x, -y, deadzone);
  }

  public static float[] radial(float x, float y, float deadzone) {
    float length = (float) Math.sqrt(x * x + y * y);
    if (length <= deadzone || length == 0) return new float[] {0, 0};
    float scale = Math.min(1, (length - deadzone) / (1 - deadzone)) / length;
    return new float[] {x * scale, y * scale};
  }

  /** LocalPlayer.cpp runTreshold: sideways travel remains available. */
  public static boolean sprintForwardEnough(float forward) { return forward >= .8f; }

  /** Entity.turn in 1.17 adds pitch: positive-up input must decrease it. */
  public static float pitch(float up, float sensitivity, float seconds, boolean inverted) {
    return up * Math.abs(up) * sensitivity * seconds * (inverted ? 1 : -1);
  }

  public static float clamp(float value, float low, float high) {
    return Math.max(low, Math.min(high, value));
  }
}

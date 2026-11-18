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

import java.lang.reflect.Field;



/** LCE's analog stick input, translated onto Java 1.17's native rowing mechanics. */
public final class ConsoleBoat {
  private static final class NativeFields {
    static final Field TURN = net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField(Boat.class,"f_38266_");
  }

  private ConsoleBoat() {}

  public static boolean analog(Object object) {
    if (!(object instanceof Boat)) return false;
    Minecraft mc = Minecraft.getInstance();
    Boat boat = (Boat) object;
    if (!boat.level.isClientSide
        || mc.player == null
        || boat.getControllingPassenger() != mc.player
        || !ControllerInput.INSTANCE.state.connected) return false;
    float strafe = mc.player.input.leftImpulse,
        forward = mc.player.input.forwardImpulse;
    try {
      float turn = NativeFields.TURN.getFloat(boat) - strafe;
      NativeFields.TURN.setFloat(boat, turn);
      boat.setYRot(boat.getYRot()+turn);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
    float acceleration = acceleration(strafe, forward);
    boat.setDeltaMovement(boat.getDeltaMovement().add(Mth.sin(-boat.getYRot()*.017453292f)*acceleration,0,0));
    boat.setDeltaMovement(boat.getDeltaMovement().add(0,0,Mth.cos(boat.getYRot()*.017453292f)*acceleration));
    boat.setPaddleState(strafe < 0 || forward > 0, strafe > 0 || forward > 0);
    return true;
  }

  static float acceleration(float strafe, float forward) {
    return forward > 0 ? .04f * forward : forward < 0 ? .005f * forward : .005f * Math.abs(strafe);
  }
}

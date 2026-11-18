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

import java.util.function.DoubleUnaryOperator;



/** Reach an actual native slider value when a 5% nudge snaps back to its origin. */
final class NativeOptionStep {
  private NativeOptionStep() {}

  static float next(String name, float current, int direction,
      DoubleUnaryOperator normalize, DoubleUnaryOperator denormalize) {
    if (direction == 0) return current;
    int sign = direction > 0 ? 1 : -1;
    // G5's setters quantize these again after Options.denormalizeValue. Use
    // their real allowed values, preserving native shader incompatibility checks.
    int[] values = name.equals("AA_LEVEL") ? new int[] {0, 2, 4, 6, 8, 12, 16}
        : name.equals("AF_LEVEL") ? new int[] {1, 2, 4, 8, 16} : null;
    if (values != null) {
      if (sign > 0) {
        for (int value : values) if (value > current) return value;
      } else {
        for (int i = values.length - 1; i >= 0; i--)
          if (values[i] < current) return values[i];
      }
      return current;
    }
    double origin = normalize.applyAsDouble(current);
    for (int n = 1; n <= 20; n++) {
      double position = Math.max(0, Math.min(1, origin + sign * n / 20.0));
      float candidate = (float) denormalize.applyAsDouble(position);
      if (sign * (candidate - current) > 0) return candidate;
      if (position == 0 || position == 1) break;
    }
    return current;
  }
}

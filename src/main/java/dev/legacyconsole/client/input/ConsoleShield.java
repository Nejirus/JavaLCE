/*
 * Adapted from Legacy4J LegacyShieldControls by Icaro K. Bomfim and contributors.
 * Copyright (C) 2026 Icaro K. Bomfim. Licensed under LGPL-3.0.
 * See licenses/Legacy4J-controls-LICENSE.txt and CONTROLS-REFERENCE.json.
 */
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

import dev.legacyconsole.client.ClientConfig;
import java.lang.reflect.Field;

/** Legacy4J LegacyShieldControls, adapted to native Java 1.11 use/release transactions. */
public final class ConsoleShield {
  private static final ConsoleShield INSTANCE = new ConsoleShield();
  private static final Field CLICKS =
      net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField(KeyMapping.class,"f_90818_");
  private LocalPlayer player;
  private InteractionHand hand;

  private ConsoleShield() {}

  /** Called just before Minecraft checks its active hand, after hotbar/chat/menu inputs. */
  public static void tick() {
    Minecraft mc = Minecraft.getInstance();
    INSTANCE.update(mc);
  }

  private void update(Minecraft mc) {
    if (player != mc.player) {
      player = mc.player;
      hand = null;
    }
    if (player == null || mc.gameMode == null) {
      hand = null;
      return;
    }
    if (!owns(player)) hand = null;
    InteractionHand next = shieldHand(mc);
    if (hand != null && hand != next) {
      mc.gameMode.releaseUsingItem(player);
      hand = null;
    }
    if (next == null
        || player.isUsingItem()
        || mc.gameMode.isDestroying()
        || player.getCooldowns().isOnCooldown(player.getItemInHand(next).getItem())) return;
    mc.gameMode.useItem(player, mc.level, next);
    if (player.isUsingItem()
        && player.getUsedItemHand() == next
        && player.getUseItem().getItem() instanceof ShieldItem) hand = next;
  }

  private boolean owns(Player player) {
    return player != null
        && this.player == player
        && hand != null
        && player.isUsingItem()
        && player.getUsedItemHand() == hand
        && player.getUseItem().getItem() instanceof ShieldItem;
  }

  private InteractionHand shieldHand(Minecraft mc) {
    if (!available(mc)
        || !mc.options.keyShift.isDown()
        || pressed(mc.options.keyAttack)
        || pressed(mc.options.keyUse)
        || pressed(mc.options.keyPickItem)) return null;
    if (player.getOffhandItem().getItem() instanceof ShieldItem) return InteractionHand.OFF_HAND;
    return player.getMainHandItem().getItem() instanceof ShieldItem ? InteractionHand.MAIN_HAND : null;
  }

  private static boolean available(Minecraft mc) {
    return ClientConfig.crouchShield
        && mc.player != null
        && mc.level != null
        && mc.gameMode != null
        && mc.screen == null
        && mc.mouseHandler.isMouseGrabbed()
        && Display.isActive()
        && mc.player.isAlive()
        && !mc.player.isSleeping()
        && !mc.player.isSpectator()
        && !mc.player.isPassenger()
        && !mc.player.getAbilities().flying
        && !ControllerInput.INSTANCE.blocksShieldUse();
  }

  private static boolean pressed(KeyMapping key) {
    try {
      return key.isDown() || CLICKS.getInt(key) > 0;
    } catch (IllegalAccessException error) {
      throw new IllegalStateException(error);
    }
  }

  /** Release owned blocking if a menu, focus loss or a gameplay guard suspends input. */
  public static void releaseIfUnavailable() {
    Minecraft mc = Minecraft.getInstance();
    if (!available(mc)) INSTANCE.update(mc);
  }

  /** Minecraft must not release a shield it did not start with the Use button. */
  public static void stopUsing(MultiPlayerGameMode controller, Player player) {
    if (!INSTANCE.owns(player)) controller.releaseUsingItem(player);
  }

  /** Keep native block/entity interactions and non-shield item use in their original order. */
  public static InteractionResult manualUse(
      MultiPlayerGameMode controller, Player player, Level level, InteractionHand hand) {
    return ClientConfig.crouchShield && player.getItemInHand(hand).getItem() instanceof ShieldItem
        ? InteractionResult.PASS
        : controller.useItem(player, level, hand);
  }
}

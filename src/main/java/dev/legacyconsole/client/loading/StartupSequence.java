package dev.legacyconsole.client.loading;
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





/** Intro720's PC/PS4 labels: ESRB 1..261, Mojang 262..355, 4J 356..455, at 30 fps. */
public final class StartupSequence {
  public static final int[] LENGTHS = {261, 94, 100};
  public static final String[] LOGOS = {"rating", "mojang", "4j"};
  public static final long DURATION_MS = (455000L + 29) / 30;

  private StartupSequence() {}

  public static long stageStart(int stage) {
    int frames=0;for(int i=0;i<Math.min(stage,LENGTHS.length);i++)frames+=LENGTHS[i];
    return (frames*1000L+29)/30;
  }

  public static int stage(long milliseconds) {
    long frame = Math.max(0, milliseconds) * 30 / 1000;
    for (int i = 0; i < LENGTHS.length; i++) {
      if (frame < LENGTHS[i]) return i;
      frame -= LENGTHS[i];
    }
    return 3;
  }

  public static float opacity(long milliseconds) {
    long frame = Math.max(0, milliseconds) * 30 / 1000;
    for (int i = 0; i < LENGTHS.length; i++) {
      int length = LENGTHS[i];
      if (frame < length)
        return Math.max(
            0,
            Math.min(
                1,
                i == LENGTHS.length - 1
                    ? frame / 9f
                    : Math.min(frame / 9f, (length - 1 - frame) / 9f)));
      frame -= length;
    }
    return 1;
  }

  /** Retain 4J at full opacity after the timed intro or when the intro is skipped. */
  public static int logoStage(long milliseconds, boolean skipped) {
    return skipped ? LOGOS.length - 1 : Math.min(LOGOS.length - 1, stage(milliseconds));
  }
}

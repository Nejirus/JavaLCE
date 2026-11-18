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





/** Orbis_Minecraft.cpp: SetKeyRepeatRate(0.3f, 0.2f); Minecraft.cpp prefers Left Scroll. */
public final class HotbarRepeater {
  private static final long DELAY = 300_000_000L, INTERVAL = 200_000_000L;
  private final boolean[] held = new boolean[2];
  private final long[] next = {Long.MAX_VALUE, Long.MAX_VALUE};

  /** Native wheel direction: positive selects left, negative selects right. */
  public int tick(long now, boolean left, boolean right, boolean gameplay) {
    boolean previous = pulse(0, now, left, gameplay);
    boolean following = pulse(1, now, right, gameplay);
    return previous ? 1 : following ? -1 : 0;
  }

  private boolean pulse(int button, long now, boolean down, boolean gameplay) {
    if (!down || !gameplay) {
      held[button] = down;
      next[button] = Long.MAX_VALUE;
      return false;
    }
    if (!held[button]) {
      held[button] = true;
      next[button] = now + DELAY;
      return true;
    }
    if (now < next[button]) return false;
    next[button] = now + INTERVAL;
    return true;
  }

  public void clear() {
    held[0] = held[1] = false;
    next[0] = next[1] = Long.MAX_VALUE;
  }
}

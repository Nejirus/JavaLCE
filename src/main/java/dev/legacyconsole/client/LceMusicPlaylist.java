package dev.legacyconsole.client;
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

import java.util.Random;




/** Common/Audio/SoundEngine.cpp: shared PS3/PS4 overworld range and GetRandomishTrack. */
public final class LceMusicPlaylist {
  static final String[] TRACKS = {
    "calm1",
    "calm2",
    "calm3",
    "hal1",
    "hal2",
    "hal3",
    "hal4",
    "nuance1",
    "nuance2",
    "creative1",
    "creative2",
    "creative3",
    "creative4",
    "creative5",
    "creative6",
    "menu1",
    "menu2",
    "menu3",
    "menu4",
    "piano1",
    "piano2",
    "piano3",
    "nether1",
    "nether2",
    "nether3",
    "nether4",
    "the_end_dragon_alive"
  };
  private final boolean[] heard = new boolean[TRACKS.length];
  private final Random random;
  private int delay;

  public LceMusicPlaylist(Random random) {
    this.random = random;
  }

  public String next(int dimension) {
    if (dimension == 1) return TRACKS[26];
    int first = dimension == -1 ? 22 : 0;
    int last = dimension == -1 ? 25 : 21;
    boolean all = true;
    for (int i = first; i <= last; i++)
      if (!heard[i]) {
        all = false;
        break;
      }
    if (all) for (int i = first; i <= last; i++) heard[i] = false;
    int track = first;
    // Keep the source's bounded retry, including occasional repeats.
    for (int i = 0; i <= (last - first) / 2; i++) {
      track = first + random.nextInt(last - first + 1);
      if (!heard[track]) {
        heard[track] = true;
        break;
      }
    }
    return TRACKS[track];
  }

  public void completed() {
    delay = random.nextInt(20 * 60 * 3);
  }

  public boolean ready() {
    return ready(1);
  }

  public boolean ready(int sourceFrames) {
    if (delay > 0) {
      delay = Math.max(0, delay - sourceFrames);
      return false;
    }
    return true;
  }

  public static String asset(String track) {
    if (track.startsWith("creative")) return "music/game/creative/" + track;
    if (track.startsWith("menu")) return "music/menu/" + track;
    if (track.startsWith("nether")) return "music/game/nether/" + track;
    if (track.equals("the_end_dragon_alive")) return "music/game/end/boss";
    return "music/game/" + track;
  }
}

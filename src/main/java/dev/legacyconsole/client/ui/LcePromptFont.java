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

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;




/** UIBitmapFont/CFontData metrics from the original Mojangles7 runtime atlas. */
public final class LcePromptFont {
  public static final LcePromptFont INSTANCE;
  static {
    try (InputStreamReader in = new InputStreamReader(
        LcePromptFont.class.getResourceAsStream("/assets/legacyconsole/prompt_font.json"),
        StandardCharsets.UTF_8)) {
      INSTANCE = new Gson().fromJson(in, LcePromptFont.class);
    } catch (Exception error) {
      throw new ExceptionInInitializerError(error);
    }
  }

  public String atlas;
  public int width, height, cellWidth, cellHeight, space;
  public Map<Integer, Glyph> glyphs;

  public static final class Glyph {
    public int x, y, advance;
    // Actual opaque pixels, including the original leading and vertical cell padding.
    public int[] ink;
  }

  public Glyph glyph(int codepoint) {
    if (codepoint == 0x2019) codepoint = '\''; // UIBitmapFont::GetCodepointGlyph.
    Glyph result = glyphs.get(codepoint);
    return result == null ? glyphs.get((int) '?') : result;
  }

  public int width(String text) {
    return text.codePoints().map(cp -> glyph(cp).advance).sum();
  }

  public float[] ink(String text) {
    float left = Float.POSITIVE_INFINITY, top = left;
    float right = Float.NEGATIVE_INFINITY, bottom = right;
    int pen = 0;
    for (int cp : text.codePoints().toArray()) {
      Glyph g = glyph(cp);
      if (g.ink != null) {
        left = Math.min(left, pen + g.ink[0]);
        right = Math.max(right, pen + g.ink[2]);
        top = Math.min(top, g.ink[1]);
        bottom = Math.max(bottom, g.ink[3]);
      }
      pen += g.advance;
    }
    // A paired bumper has an empty label; retain the normal capital line for layout.
    return left == Float.POSITIVE_INFINITY
        ? new float[] {0, 0, 4, 11} : new float[] {left, right, top, bottom};
  }

  public String fit(String text, int available) {
    int end = 0, used = 0;
    while (end < text.length()) {
      int cp = text.codePointAt(end), advance = glyph(cp).advance;
      if (used + advance > available) break;
      used += advance;
      end += Character.charCount(cp);
    }
    return text.substring(0, end);
  }
}

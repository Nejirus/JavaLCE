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
import java.util.*;
import java.util.function.ToIntFunction;




/** Normal LCE tooltip display lists and the translated ToolTips.UpdateLayout algorithm. */
public final class LceGlyphLayout {
  private static final Map<String, Family> FAMILIES;
  private static final List<String> ORDER = Arrays.asList(
      "cross", "circle", "square", "triangle", "left_trigger", "right_trigger",
      "left_bumper", "right_bumper", "left_stick");

  static {
    try (InputStreamReader in = new InputStreamReader(
        LceGlyphLayout.class.getResourceAsStream("/assets/legacyconsole/glyphs.json"),
        StandardCharsets.UTF_8)) {
      FAMILIES = new Gson().fromJson(in,
          new com.google.gson.reflect.TypeToken<Map<String, Family>>() {}.getType());
    } catch (Exception error) {
      throw new ExceptionInInitializerError(error);
    }
  }

  private LceGlyphLayout() {}

  public static final class Layer {
    public String path;
    public float x, y, width, height;
  }

  public static final class Glyph {
    // Source getRect: left, right, top, bottom. Includes the authored transparent padding.
    public float[] bounds;
    public Layer[] layers;
  }

  public static final class Family {
    public float units, space, textY, textBottom, textInset, textGutter;
    public Map<String, Glyph> glyphs;

    public Glyph glyph(String name) {
      Glyph glyph = glyphs.get(name);
      if (glyph == null) throw new IllegalArgumentException("Unknown controller glyph: " + name);
      return glyph;
    }

    public float labelX(String name) {
      return (glyph(name).bounds[1] + textInset) / units;
    }

    public float labelY(String name, String label) {
      float[] symbol = glyph(name).bounds, ink = LcePromptFont.INSTANCE.ink(label);
      return (symbol[2] + symbol[3]) / (2 * units) - (ink[2] + ink[3]) / 2;
    }

    public float labelWidth(String text, ToIntFunction<String> textWidth) {
      float result = textWidth.applyAsInt(text);
      float adjustment = space - textWidth.applyAsInt(" ");
      for (int i = 0; i < text.length(); i++) if (text.charAt(i) == ' ') result += adjustment;
      return result;
    }

    public float width(String name, String label, ToIntFunction<String> textWidth) {
      Glyph glyph = glyph(name);
      // FJ_ToolTip.UpdateText moves the label to getRect.x + width; SetLabel adds two spaces.
      return (glyph.bounds[1] - Math.min(0, glyph.bounds[0]) + textGutter) / units
          + labelWidth(label, textWidth) + 2 * space;
    }

    public float originY(int height) {
      // ToolTips.UpdateLayout always uses Button A, even when that action is hidden.
      return height - (float) Math.floor(height * units * .05f) / units
          - Math.max(glyph("cross").bounds[3], textBottom) / units;
    }
  }

  public static Family family(String folder, int displayHeight) {
    String name = folder + (displayHeight >= 1080 && !folder.equals("ps3") ? "_hd" : "");
    Family family = FAMILIES.get(name);
    if (family == null) throw new IllegalArgumentException("Unknown controller family: " + name);
    return family;
  }

  public static final class Prompt {
    public final String glyph, label;
    public final float x, y, textX, textY, nextX;

    Prompt(Family family, String glyph, String label, float x, float y,
        ToIntFunction<String> textWidth) {
      this.glyph = glyph;
      this.label = label;
      this.x = x;
      this.y = y;
      textX = x + family.labelX(glyph);
      textY = y + family.labelY(glyph, label);
      nextX = x + family.width(glyph, label, textWidth);
    }
  }

  public static List<Prompt> footer(Family family, int width, int height,
      ToIntFunction<String> textWidth, String... actions) {
    if (actions.length % 2 != 0) throw new IllegalArgumentException("Unpaired footer action");
    List<String[]> ordered = new ArrayList<>();
    for (int i = 0; i < actions.length; i += 2)
      ordered.add(new String[] {actions[i], actions[i + 1]});
    // The source movie's action array fixes A/B/X/Y/LT/RT/LB/RB/LS order.
    ordered.sort(Comparator.comparingInt(a -> {
      int index = ORDER.indexOf(a[0]);
      return index < 0 ? ORDER.size() : index;
    }));
    float x = (float) Math.floor(width * family.units * .05f) / family.units;
    float y = family.originY(height);
    List<Prompt> result = new ArrayList<>();
    for (String[] action : ordered) {
      Prompt prompt = new Prompt(family, action[0], action[1], x, y, textWidth);
      result.add(prompt);
      x = prompt.nextX;
    }
    return result;
  }
}

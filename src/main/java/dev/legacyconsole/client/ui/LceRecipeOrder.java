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

import com.google.gson.*;
import dev.legacyconsole.crafting.RecipeCatalog;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;



/** C++ Recipes registration order is intentionally not alphabetized in the console menu. */
final class LceRecipeOrder {
  private static final Map<String, Integer> EXACT = new HashMap<>();
  private static final Map<Integer, Integer> ITEM = new HashMap<>();
  private static final Map<String, Integer> EXTENDED = new HashMap<>();

  static {
    try (Reader reader = resource("crafting_lce_order.json")) {
      int i = 0;
      for (JsonElement e :
          new JsonParser().parse(reader).getAsJsonObject().getAsJsonArray("order")) {
        JsonObject o = e.getAsJsonObject();
        int id = o.get("id").getAsInt();
        EXACT.putIfAbsent(id + ":" + o.get("meta").getAsInt(), i);
        ITEM.putIfAbsent(id, i++);
      }
    } catch (IOException e) {
      throw new ExceptionInInitializerError(e);
    }
    try (Reader reader = resource("crafting_legacy4j.json")) {
      JsonObject listing = new JsonParser().parse(reader).getAsJsonObject();
      int[] rank = {0};
      for (Map.Entry<String, JsonElement> e : listing.entrySet()) walk(e.getValue(), rank);
    } catch (IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private static Reader resource(String name) throws IOException {
    InputStream stream = LceRecipeOrder.class.getResourceAsStream("/assets/legacyconsole/" + name);
    if (stream == null) throw new FileNotFoundException(name);
    return new InputStreamReader(stream, StandardCharsets.UTF_8);
  }

  private static void walk(JsonElement e, int[] rank) {
    if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
      ItemStack s = ConsoleItemNames.resolve(e);
      if (!s.isEmpty())
        EXTENDED.putIfAbsent(Item.getId(s.getItem()) + ":" + s.getDamageValue(), rank[0]++);
    } else if (e.isJsonArray()) for (JsonElement value : e.getAsJsonArray()) walk(value, rank);
    else if (e.isJsonObject())
      for (Map.Entry<String, JsonElement> value : e.getAsJsonObject().entrySet())
        if (!value.getKey().equals("icon") && !value.getKey().equals("displayName"))
          walk(value.getValue(), rank);
  }

  static int rank(RecipeCatalog.Entry e) {
    int id = Item.getId(e.output.getItem());
    String key = id + ":" + e.output.getDamageValue();
    return EXACT.containsKey(key)
        ? EXACT.get(key) * 100
        : ITEM.containsKey(id)
            ? ITEM.get(id) * 100 + e.output.getDamageValue()
            : 100000 + EXTENDED.getOrDefault(key, 10000);
  }
}

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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;



/** UIScene_MainMenu.cpp's splash selection and customDrawSplash, with the original text file. */
final class LceSplash {
  private static final List<String> TEXT = load();
  private final String text = TEXT.get(index(Calendar.getInstance(), new Random(), TEXT.size()));

  private static List<String> load() {
    List<String> result = new ArrayList<>();
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                LceSplash.class.getResourceAsStream("/assets/legacyconsole/texts/lce_splashes.txt"),
                StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.replace("\uFEFF", "").trim();
        if (!line.isEmpty()) result.add(line);
      }
    } catch (IOException error) {
      throw new IllegalStateException("Cannot read LCE splashes", error);
    }
    if (result.size() <= 5) throw new IllegalStateException("Incomplete LCE splashes");
    return result;
  }

  static int index(Calendar date, Random random, int count) {
    int month = date.get(Calendar.MONTH) + 1, day = date.get(Calendar.DAY_OF_MONTH);
    if (month == 11 && day == 9) return 0;
    if (month == 6 && day == 1) return 1;
    if (month == 12 && day == 24) return 2;
    if (month == 1 && day == 1) return 3;
    return 5 + random.nextInt(count - 5);
  }

  void draw(int width, int height) {
    FontCompat font = FontCompat.get();
    GlStateManager.pushMatrix();
    GlStateManager.translate(LceViewport.standard(width,height)?LceViewport.x(width,height,862):width*862f/1280f,LceViewport.y(width,height,176),0);
    GlStateManager.rotate(-17, 0, 0, 1);
    float pulse =
        1.8f
            - Math.abs(
                (float) Math.sin((System.currentTimeMillis() % 1000) / 1000f * Math.PI * 2) * .1f);
    float scale = pulse * 100 / (font.getStringWidth(text) + 32);
    GlStateManager.scale(scale, scale, scale);
    font.drawStringWithShadow(text, -font.getStringWidth(text) / 2, -8, 0xffff00);
    GlStateManager.popMatrix();
    GlStateManager.color(1, 1, 1, 1);
  }
}

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

import java.io.IOException;
import java.util.*;



public final class ConsoleKeyBindings extends ConsoleVerticalList {
  private KeyMapping[] keys;
  private KeyMapping waiting;

  public ConsoleKeyBindings(Screen parent) {
    super(parent, "Keyboard Bindings");
    panelW = 310;
    panelH = 202;
  }

  @Override
  public void initGui() {
    keys = mc.options.keyMappings.clone();
    Arrays.sort(
        keys,
        Comparator.comparing(KeyMapping::getCategory)
            .thenComparing(KeyMapping::getName));
    super.initGui();
  }

  @Override
  protected int itemCount() {
    return keys.length;
  }

  @Override
  protected String itemLabel(int index) {
    KeyMapping key = keys[index];
    return ConsoleTheme.fit(I18n.get(key.getName()), waiting == key ? 95 : 150)
        + ": "
        + (waiting == key
            ? "> Press a key <"
            : key.getTranslatedKeyMessage().getString());
  }

  @Override
  protected void activateItem(int index) {
    waiting = keys[index];
    rebuildList();
  }

  private void bind(int key) {
    waiting.setKey(key < 0 ? com.mojang.blaze3d.platform.InputConstants.Type.MOUSE.getOrCreate(key + 100) : com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(Keyboard.toGlfw(key)));
    waiting = null;
    KeyMapping.resetMapping();
    mc.options.save();
    rebuildList();
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    if (waiting != null) {
      if (key == Keyboard.KEY_ESCAPE) {
        waiting = null;
        rebuildList();
      } else bind(key != 0 ? key : c + 256);
    } else super.keyTyped(c, key);
  }

  @Override
  protected void mouseClicked(int x, int y, int b) throws IOException {
    if (waiting != null) bind(b - 100);
    else super.mouseClicked(x, y, b);
  }
}

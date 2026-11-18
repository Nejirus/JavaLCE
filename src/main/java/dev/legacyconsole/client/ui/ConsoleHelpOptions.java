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



public final class ConsoleHelpOptions extends ConsolePanelScreen {
  public ConsoleHelpOptions(Screen parent) {
    super(parent, "Help & Options");
    panelW = 260;
    panelH = 184;
  }

  @Override
  public void initGui() {
    super.initGui();
    addRow(3, 0, "Change Skin Parts");
    addRow(0, 1, "How to Play");
    addRow(2, 2, "Settings");
    ConsoleMenuLayout.rows(buttonList, width, height, 225);
    MenuFocus.enter(this, buttonList);
  }

  @Override
  protected void actionPerformed(GuiButton b) {
    if (b.id == 0) mc.setScreen(new ConsoleHelpScreen(this));
    else if (b.id == 2) mc.setScreen(new ConsoleSettingsScreen(this));
    else if (b.id == 3) mc.setScreen(new ConsoleSkinScreen(this));
    else back();
  }

  @Override
  protected void shell() {
    ConsoleMenuLayout.background(width, height);
    ConsoleMenuLayout.logo(width, height);
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    if (key == Keyboard.KEY_UP || key == Keyboard.KEY_DOWN)
      MenuFocus.move(this, buttonList, key == Keyboard.KEY_UP ? -1 : 1);
    else if (key == Keyboard.KEY_RETURN) {
      GuiButton b = ConsoleMenuLayout.activation(this, buttonList);
      if (b != null) actionPerformed(b);
    } else super.keyTyped(c, key);
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    int wheel = Mouse.getEventDWheel();
    if (wheel != 0) MenuFocus.move(this, buttonList, wheel > 0 ? -1 : 1);
  }
}

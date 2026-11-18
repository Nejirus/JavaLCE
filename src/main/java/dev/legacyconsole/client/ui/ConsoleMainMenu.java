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



public final class ConsoleMainMenu extends GuiMainMenu {
  private int top;
  private final LceSplash splash = new LceSplash();

  @Override
  public void initGui() {
    super.initGui();
    buttonList.clear();
    top = Math.round(LceViewport.y(width,height,250));
    String[] labels = {"Play Game", "Help & Options", "Exit Game"};
    for (int i = 0; i < labels.length; i++)
      buttonList.add(
          new ConsoleButton(
              i,
              (width - ConsoleOptionLayout.WIDTH) / 2,
              top + i * ConsoleOptionLayout.PITCH,
              ConsoleOptionLayout.WIDTH,
              ConsoleOptionLayout.HEIGHT,
              labels[i]));
    MenuFocus.enter(this, buttonList);
    dev.legacyconsole.client.input.ControllerInput.INSTANCE.prepareMenu();
  }

  @Override
  protected void actionPerformed(GuiButton b) throws IOException {
    if (b.id == 0) mc.setScreen(new ConsolePlayScreen(this, 0));
    else if (b.id == 1) mc.setScreen(new ConsoleHelpOptions(this));
    else mc.stop();
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    if (key == Keyboard.KEY_UP || key == Keyboard.KEY_DOWN)
      MenuFocus.move(this, buttonList, key == Keyboard.KEY_UP ? -1 : 1);
    else if (key == Keyboard.KEY_RETURN) {
      GuiButton b = ConsoleMenuLayout.activation(this, buttonList);
      if (b != null) actionPerformed(b);
    }
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    int wheel = Mouse.getEventDWheel();
    if (wheel != 0) MenuFocus.move(this, buttonList, wheel > 0 ? -1 : 1);
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    ConsoleTheme.background(width, height);
    ConsoleMenuLayout.logo(width, height);
    splash.draw(width, height);
    for (GuiButton b : buttonList) b.drawButton(mc, mx, my);
    String build = "Minecraft 1.17.1 | Legacy Console Release " + dev.legacyconsole.LegacyConsole.VERSION;
    String copyright = "Copyright Mojang AB";
    ConsoleTheme.text(
        build, width - fontRendererObj.getStringWidth(build) - 6, height - 23, ConsoleTheme.MUTED);
    ConsoleTheme.text(
        copyright,
        width - fontRendererObj.getStringWidth(copyright) - 6,
        height - 12,
        ConsoleTheme.MUTED);
    ConsoleTheme.footer(width, height, "cross", "Select");
  }
}

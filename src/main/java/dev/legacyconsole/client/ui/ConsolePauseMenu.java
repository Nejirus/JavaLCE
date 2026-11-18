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

import dev.legacyconsole.LegacyConsole;
import dev.legacyconsole.client.input.*;
import java.io.IOException;
import java.util.*;



public final class ConsolePauseMenu extends GuiIngameMenu implements ControllerScreen {
  private String status = "";
  private final List<GuiButton> entries = new ArrayList<>();
  private int selected, first, visibleRows;

  @Override
  public void initGui() {
    entries.clear();
    int[] ids =
        mc.hasSingleplayerServer() ? new int[] {4, 246, 6, 5, 247, 1} : new int[] {4, 246, 6, 5, 1};
    String[] labels =
        mc.hasSingleplayerServer()
            ? new String[] {
              "Resume Game",
              "Help & Options",
              "Leaderboards",
              "Achievements",
              "Save Game",
              "Exit Game"
            }
            : new String[] {
              "Resume Game", "Help & Options", "Leaderboards", "Achievements", "Exit Game"
            };
    for (int i = 0; i < ids.length; i++) {
      entries.add(
          new ConsoleButton(
              ids[i], 0, 0, ConsoleOptionLayout.WIDTH, ConsoleOptionLayout.HEIGHT, labels[i]));
    }
    visibleRows = new ConsoleOptionLayout(width, height, entries.size(), 0).rows;
    selected = Math.max(0, Math.min(selected, entries.size() - 1));
    rebuild();
  }

  private void rebuild() {
    first = Math.max(0, Math.min(first, Math.max(0, entries.size() - visibleRows)));
    if (selected < first) first = selected;
    if (selected >= first + visibleRows) first = selected - visibleRows + 1;
    buttonList.clear();
    int top = Math.round(LceViewport.y(width,height,250));
    for (int i = first; i < Math.min(entries.size(), first + visibleRows); i++) {
      GuiButton entry = entries.get(i);
      buttonList.add(
          new ConsoleButton(
              entry.id,
              (width - ConsoleOptionLayout.WIDTH) / 2,
              top + (i - first) * ConsoleOptionLayout.PITCH,
              ConsoleOptionLayout.WIDTH,
              ConsoleOptionLayout.HEIGHT,
              entry.displayString));
    }
    MenuFocus.set(this, entries.get(selected).id);
  }

  private void move(int delta) {
    int previous = selected;
    selected = Math.floorMod(selected + delta, entries.size());
    rebuild();
    ControllerCursor.navigation();
    ControllerMenus.focus(this, entries.get(selected).id);
    if (previous != selected) dev.legacyconsole.client.ConsoleAudio.play("focus");
  }

  @Override
  protected void actionPerformed(GuiButton b) throws IOException {
    for (int i = 0; i < entries.size(); i++) if (entries.get(i).id == b.id) selected = i;
    MenuFocus.set(this, b.id);
    if (b.id == 246) mc.setScreen(new ConsoleHelpOptions(this));
    else if (b.id == 7) ConsoleHostOptions.requestOpen(this);
    else if (b.id == 247) {
      status = "Saving…";
      mc.getSingleplayerServer()
          .execute(
              () -> {
                mc.getSingleplayerServer().getPlayerList().saveAll();
                mc.getSingleplayerServer().saveAllChunks(false,true,true);
                mc.execute(() -> status = "Game Saved");
              });
    } else if (b.id == 1)
      mc.setScreen(
          new ConsoleConfirmScreen(
              this,
              "Exit Game",
              mc.hasSingleplayerServer()
                  ? "Save and exit to the main menu?"
                  : "Disconnect from this server?",
              () -> {
                try {
                  super.actionPerformed(b);
                } catch (IOException e) {
                  LegacyConsole.log.warn("Unable to exit game", e);
                }
              }));
    else super.actionPerformed(b);
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    ConsoleMenuLayout.background(width, height);
    ConsoleMenuLayout.logo(width, height);
    for (GuiButton b : buttonList) b.drawButton(mc, mx, my);
    int arrowX = (width + ConsoleOptionLayout.WIDTH) / 2 + 10;
    if (first > 0)
      ConsoleTheme.sprite(
          "gui/sprites/widget/scroll_up", arrowX, buttonList.get(0).yPosition + 6, 7, 5);
    if (first + visibleRows < entries.size())
      ConsoleTheme.sprite(
          "gui/sprites/widget/scroll_down",
          arrowX,
          buttonList.get(buttonList.size() - 1).yPosition + 9,
          7,
          5);
    if (!status.isEmpty() && !buttonList.isEmpty())
      ConsoleTheme.centered(status, width / 2, buttonList.get(0).yPosition - 14, ConsoleTheme.TEXT);
    ConsoleTheme.footer(width,height,"cross","Select","circle","Resume");
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    if (key == Keyboard.KEY_UP || key == Keyboard.KEY_DOWN) move(key == Keyboard.KEY_UP ? -1 : 1);
    else if (key == Keyboard.KEY_RETURN) {
      GuiButton b = ConsoleMenuLayout.activation(this, buttonList);
      if (b != null) actionPerformed(b);
    } else if (key == Keyboard.KEY_H) openHostOptions();
    else super.keyTyped(c, key);
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    int wheel = Mouse.getEventDWheel();
    if (wheel != 0) move(wheel > 0 ? -1 : 1);
  }

  private void openHostOptions() {
    ConsoleHostOptions.requestOpen(this);
  }

  @Override
  public boolean controllerDirection(int direction) {
    if (direction == PadState.UP || direction == PadState.DOWN)
      move(direction == PadState.UP ? -1 : 1);
    return true;
  }

  @Override
  public void controllerOpened() {
    selected = first = 0;
    rebuild();
    ControllerMenus.focus(this, entries.get(0).id);
  }

  @Override
  public boolean controllerButton(int button) {
    if (button == PadState.L2 || button == PadState.R2) {
      move(button == PadState.L2 ? -1 : 1);
      return true;
    }

    return false;
  }
}

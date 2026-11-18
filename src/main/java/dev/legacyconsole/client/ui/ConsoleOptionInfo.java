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

import dev.legacyconsole.client.input.*;
import java.io.IOException;
import java.util.*;



/** OptiFine's original option descriptions, scrollable at every GUI scale. */
final class ConsoleOptionInfo extends ConsolePanelScreen implements ControllerScreen {
  private final String[] description;
  private final List<String> lines = new ArrayList<>();
  private int first, visible;

  ConsoleOptionInfo(Screen parent, String title, String[] description) {
    super(parent, title);
    this.description = description;
  }

  @Override
  public void initGui() {
    panelW = Math.min(276, width - 38);
    int top = Math.round(LceViewport.y(width,height,56)) + 44 + 24;
    panelH = Math.min(194, height - 28 - top);
    panelX = (width - panelW) / 2;
    panelY = Math.max(top, (height - panelH) / 2);
    lines.clear();
    for (String line : description)
      lines.addAll(fontRendererObj.listFormattedStringToWidth(line, panelW - 24));
    visible = Math.max(1, (panelH - 20) / 10);
    first = Math.max(0, Math.min(first, lines.size() - visible));
    buttonList.clear();
  }

  private void scroll(int delta) {
    first = Math.max(0, Math.min(Math.max(0, lines.size() - visible), first + delta));
  }

  @Override
  protected void shell() {
    ConsoleMenuLayout.background(width, height);
    ConsoleMenuLayout.logo(width, height);
    ConsoleTheme.centered(
        ConsoleTheme.fit(title, panelW), width / 2, panelY - 12, ConsoleTheme.TEXT);
    ConsoleTheme.lcePanel(panelX, panelY, panelW, panelH);
    for (int i = first; i < Math.min(lines.size(), first + visible); i++)
      ConsoleTheme.text(
          lines.get(i), panelX + 12, panelY + 10 + (i - first) * 10, ConsoleTheme.DARK);
    if (first > 0)
      ConsoleTheme.sprite("gui/sprites/widget/scroll_up", panelX + panelW + 5, panelY + 8, 7, 5);
    if (first + visible < lines.size())
      ConsoleTheme.sprite(
          "gui/sprites/widget/scroll_down", panelX + panelW + 5, panelY + panelH - 13, 7, 5);
  }

  @Override
  protected String[] footerHints() {
    return new String[] {"circle", "Back"};
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    int wheel = Mouse.getEventDWheel();
    if (wheel != 0) scroll(wheel > 0 ? -1 : 1);
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    if (key == Keyboard.KEY_UP || key == Keyboard.KEY_PRIOR) scroll(-1);
    else if (key == Keyboard.KEY_DOWN || key == Keyboard.KEY_NEXT) scroll(1);
    else super.keyTyped(c, key);
  }

  @Override
  public boolean controllerDirection(int direction) {
    if (direction == PadState.UP || direction == PadState.DOWN)
      scroll(direction == PadState.UP ? -1 : 1);
    return true;
  }

  @Override
  public boolean controllerButton(int button) {
    if (button == PadState.L2 || button == PadState.R2) {
      scroll(button == PadState.L2 ? -1 : 1);
      return true;
    }
    return false;
  }
}

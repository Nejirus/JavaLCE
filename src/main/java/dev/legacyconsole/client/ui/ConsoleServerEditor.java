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



public final class ConsoleServerEditor extends ConsolePanelScreen implements ControllerScreen {
  private final ConsolePlayScreen play;
  private final ServerList servers;
  private final int index;
  private GuiTextField name, address;
  private String savedName, savedAddress;
  private ServerData.ServerPackStatus resources = ServerData.ServerPackStatus.PROMPT;

  public ConsoleServerEditor(ConsolePlayScreen play, ServerList servers, int index) {
    super(play, index == -2 ? "Direct Connection" : index < 0 ? "Add Server" : "Edit Server");
    this.play = play;
    this.servers = servers;
    this.index = index;
    panelH = 200;
    ServerData old = index >= 0 ? servers.get(index) : null;
    savedName = old == null ? "Minecraft Server" : old.name;
    savedAddress = old == null ? "" : old.ip;
    if (old != null) resources = old.getResourcePackStatus();
  }

  @Override
  public void initGui() {
    super.initGui();
    Keyboard.enableRepeatEvents(true);
    name = new GuiTextField(10, fontRendererObj, panelX + 24, panelY + 45, panelW - 48, 16);
    address = new GuiTextField(11, fontRendererObj, panelX + 24, panelY + 90, panelW - 48, 16);
    name.setEnableBackgroundDrawing(false);
    address.setEnableBackgroundDrawing(false);
    name.setMaxStringLength(128);
    address.setMaxStringLength(32500);
    name.setText(savedName);
    address.setText(savedAddress);
    name.setFocused(true);
    buttonList.add(new ConsoleTextInput(10, panelX + 18, panelY + 41, panelW - 36, 23, savedName));
    buttonList.add(new ConsoleTextInput(11, panelX + 18, panelY + 86, panelW - 36, 23, savedAddress));
    buttonList.add(
        new ConsoleButton(
            0,
            panelX + 18,
            panelY + 123,
            panelW - 36,
            20,
            "Server Textures: " + resources.getName().getString()));
    buttonList.add(
        new ConsoleButton(
            1,
            panelX + 18,
            panelY + 149,
            panelW - 36,
            20,
            index == -2 ? "Join Server" : "Save Server"));
    buttonList.add(new ConsoleButton(2, panelX + 18, panelY + 173, panelW - 36, 20, "Cancel"));
  }

  @Override
  protected void actionPerformed(GuiButton b) {
    if (b.id == 10 || b.id == 11) {
      enterText(b.id == 10);
    } else if (b.id == 0) {
      ServerData.ServerPackStatus[] modes = ServerData.ServerPackStatus.values();
      resources = modes[(resources.ordinal() + 1) % modes.length];
      b.displayString = "Server Textures: " + resources.getName().getString();
    } else if (b.id == 1) {
      if (address.getText().trim().isEmpty()) return;
      ServerData data =
          index >= 0
              ? servers.get(index)
              : new ServerData(name.getText().trim(), address.getText().trim(), false);
      data.name =
          name.getText().trim().isEmpty() ? "Minecraft Server" : name.getText().trim();
      data.ip = address.getText().trim();
      data.setResourcePackStatus(resources);
      if (index == -2) play.connect(data);
      else {
        if (index < 0) servers.add(data);
        else servers.replace(index, data);
        servers.save();
        mc.setScreen(play);
      }
    } else back();
  }

  @Override
  public void updateScreen() {
    name.updateCursorCounter();
    address.updateCursorCounter();
    for (GuiButton b : buttonList) {
      if (b.id == 1) b.enabled = !address.getText().trim().isEmpty();
      if (b.id == 10) b.displayString = name.getText().isEmpty() ? "Server Name" : name.getText();
      if (b.id == 11)
        b.displayString = address.getText().isEmpty() ? "Server Address" : address.getText();
    }
    savedName = name.getText();
    savedAddress = address.getText();
  }

  @Override
  public void onGuiClosed() {
    savedName = name.getText();
    savedAddress = address.getText();
    Keyboard.enableRepeatEvents(false);
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    if (key == Keyboard.KEY_TAB) {
      name.setFocused(!name.isFocused());
      address.setFocused(!name.isFocused());
    } else if (key == Keyboard.KEY_RETURN) {
      for (GuiButton b : buttonList) if (b.id == 1) actionPerformed(b);
    } else if (key == 1) back();
    else {
      name.textboxKeyTyped(c, key);
      address.textboxKeyTyped(c, key);
    }
  }

  @Override
  protected void mouseClicked(int x, int y, int b) throws IOException {
    super.mouseClicked(x, y, b);
    name.mouseClicked(x, y, b);
    address.mouseClicked(x, y, b);
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    super.drawScreen(mx, my, partial);
    ConsoleTheme.text("Server Name", panelX + 18, panelY + 29, ConsoleTheme.DARK);
    ConsoleTheme.text("Server Address", panelX + 18, panelY + 74, ConsoleTheme.DARK);
  }

  @Override
  protected String[] footerHints() {
    return new String[] {"cross", "Select / Edit", "square", "Enter Text", "circle", "Back"};
  }

  private void enterText(boolean editName) {
    name.setFocused(editName);
    address.setFocused(!editName);
    mc.setScreen(
        new ConsoleKeyboard(
            this,
            editName ? "Server Name" : "Server Address",
            editName ? savedName : savedAddress,
            value -> {
              if (editName) savedName = value;
              else savedAddress = value;
            }));
  }

  @Override
  public boolean controllerDirection(int d) {
    return false;
  }

  @Override
  public boolean controllerButton(int b) {
    if (b == PadState.SQUARE) {
      int id = MenuFocus.id(this);
      enterText(id == 10 || id != 11 && name.isFocused());
      return true;
    }
    if (b == PadState.L1 || b == PadState.R1) {
      boolean editName = MenuFocus.id(this) != 10;
      name.setFocused(editName);
      address.setFocused(!editName);
      ControllerMenus.focus(this, editName ? 10 : 11);
      return true;
    }
    return false;
  }
}

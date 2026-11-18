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

import dev.legacyconsole.client.ConsoleAudio;
import dev.legacyconsole.client.input.*;
import java.io.IOException;



/** LCE SignEntryMenu: four 15-character rows, its original board, and one final confirmation. */
public final class ConsoleSignScreen extends LegacyScreen implements ControllerScreen {
  private final SignBlockEntity sign;
  private final String[] original = new String[4], lines = new String[4];
  private int boardX, boardY, selected;
  private boolean submitted;

  public ConsoleSignScreen(SignBlockEntity sign) {
    this.sign = sign;
    for (int i = 0; i < 4; i++) original[i] = lines[i] = sign.getMessage(i,false).getString();
  }

  @Override
  public void initGui() {
    boardX = width / 2 - 72;
    boardY = height / 2 - 80;
    buttonList.clear();
    for (int i = 0; i < 4; i++) buttonList.add(new Line(i, boardX + 5, boardY + 3 + i * 15));
    buttonList.add(new ConsoleButton(4, width / 2 - 100, boardY + 160, 200, 20, "Done"));
    sign.setEditable(false);
    Keyboard.enableRepeatEvents(true);
  }

  private void edit(int row) {
    selected = row;
    ConsoleAudio.play("confirm");
    mc.setScreen(
        new ConsoleKeyboard(
            this,
            "Edit Sign - Line " + (row + 1),
            lines[row],
            value -> lines[row] = value,
            15,
            value -> fontRendererObj.getStringWidth(value) <= 90));
  }

  private void finish(boolean save) {
    if (submitted) return;
    submitted = true;
    for (int i = 0; i < 4; i++)
      sign.setMessage(i,new TextComponent(save ? lines[i] : original[i]));
    sign.setChanged();
    if (mc.getConnection() != null)
      mc.getConnection().send(new ServerboundSignUpdatePacket(sign.getBlockPos(),sign.getMessage(0,false).getString(),sign.getMessage(1,false).getString(),sign.getMessage(2,false).getString(),sign.getMessage(3,false).getString()));
    sign.setEditable(true);
    if (!save) ConsoleAudio.play("back");
    mc.setScreen(null);
  }

  @Override
  public void onGuiClosed() {
    // Opening the keyboard/settings is not final confirmation of the sign.
    Keyboard.enableRepeatEvents(false);
    sign.setEditable(true);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button.id < 4) edit(button.id);
    else finish(true);
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    if (key == Keyboard.KEY_ESCAPE) finish(false);
    else if (key == Keyboard.KEY_UP || key == Keyboard.KEY_DOWN)
      controllerDirection(key == Keyboard.KEY_UP ? PadState.UP : PadState.DOWN);
    else if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
      if (selected < 4) edit(selected);
      else {
        ConsoleAudio.play("confirm");
        finish(true);
      }
    } else if (selected < 4) {
      if (key == Keyboard.KEY_BACK && !lines[selected].isEmpty())
        lines[selected] = lines[selected].substring(0, lines[selected].length() - 1);
      else if (SharedConstants.isAllowedChatCharacter(c)
          && lines[selected].length() < 15
          && fontRendererObj.getStringWidth(lines[selected] + c) <= 90) lines[selected] += c;
    }
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    drawDefaultBackground();
    ConsoleTheme.centered("Edit Sign", width / 2, boardY - 25, ConsoleTheme.TEXT);
    ConsoleTheme.sprite("lce/sign_background", boardX, boardY, 144, 156);
    super.drawScreen(mx, my, partial);
    ConsoleTheme.footer(
        width, height, "cross", "Edit", "start_button", "Done", "circle", "Back");
  }

  @Override
  public void controllerOpened() {
    MenuFocus.set(this, selected);
  }

  @Override
  public boolean controllerDirection(int direction) {
    if (direction == PadState.UP || direction == PadState.DOWN) {
      selected = Math.floorMod(selected + (direction == PadState.UP ? -1 : 1), 5);
      MenuFocus.set(this, selected);
      ConsoleAudio.play("focus");
    }
    return true;
  }

  @Override
  public boolean controllerButton(int button) {
    if (button == PadState.CROSS) {
      if (selected < 4) edit(selected);
      else {
        ConsoleAudio.play("confirm");
        finish(true);
      }
      return true;
    }
    if (button == PadState.OPTIONS) {
      ConsoleAudio.play("confirm");
      finish(true);
      return true;
    }
    if (button == PadState.CIRCLE) {
      finish(false);
      return true;
    }
    return false;
  }

  private final class Line extends GuiButton {
    Line(int row, int x, int y) {
      super(row, x, y, 134, 15, "");
    }

    @Override
    public void playPressSound(SoundManager sounds) {
      // edit() owns the press sound for mouse, keyboard and controller alike.
    }

    @Override
    public void drawButton(Minecraft mc, int mx, int my) {
      hovered =
          mx >= xPosition && mx < xPosition + width && my >= yPosition && my < yPosition + height;
      boolean focus = MenuFocus.focused(this, hovered);
      if (hovered && !ControllerCursor.active()) selected = id;
      if (focus)
        ConsoleTheme.sprite(
            "gui/sprites/widget/text_field_highlighted", xPosition, yPosition, width, height);
      ConsoleTheme.centered(
          lines[id], xPosition + width / 2, yPosition + 6, focus ? ConsoleTheme.TEXT : 0xff000000);
    }
  }
}

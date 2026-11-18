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
import java.util.function.Consumer;
import java.util.function.Predicate;



public final class ConsoleKeyboard extends ConsolePanelScreen implements ControllerScreen {
  private static final String[] LETTERS = {"1234567890", "qwertyuiop", "asdfghjkl_", "zxcvbnm.-/"};
  private static final String[] SYMBOLS = {
    "1234567890", "!@#$%^&*()", "[]{}<>:;=+", ".,?-_/\\|'\""
  };
  private final Consumer<String> accept;
  private String value;
  private boolean shift, symbols;
  private final int maxLength;
  private final Predicate<String> valid;

  public ConsoleKeyboard(Screen parent, String title, String value, Consumer<String> accept) {
    this(parent, title, value, accept, 32767, s -> true);
  }

  public ConsoleKeyboard(
      Screen parent,
      String title,
      String value,
      Consumer<String> accept,
      int maxLength,
      Predicate<String> valid) {
    super(parent, title);
    this.value = value;
    this.accept = accept;
    this.maxLength = maxLength;
    this.valid = valid;
    panelW = 310;
    panelH = 210;
  }

  @Override
  public void initGui() {
    super.initGui();
    Keyboard.enableRepeatEvents(true);
    rebuild();
  }

  private void rebuild() {
    buttonList.clear();
    int cell = (panelW - 28) / 10;
    String[] keys = symbols ? SYMBOLS : LETTERS;
    for (int row = 0; row < 4; row++)
      for (int col = 0; col < 10; col++) {
        char c = keys[row].charAt(col);
        buttonList.add(
            new ConsoleButton(
                row * 10 + col,
                panelX + 14 + col * cell,
                panelY + 62 + row * 22,
                cell - 2,
                20,
                String.valueOf(shift ? Character.toUpperCase(c) : c)));
      }
    String[] actions = {"Shift", symbols ? "ABC" : "Symbols", "Space", "Delete"};
    for (int i = 0; i < 4; i++)
      buttonList.add(
          new ConsoleButton(
              40 + i,
              panelX + 14 + i * (panelW - 28) / 4,
              panelY + 153,
              (panelW - 28) / 4 - 2,
              20,
              actions[i]));
    buttonList.add(new ConsoleButton(44, panelX + 14, panelY + 180, panelW - 28, 20, "Done"));
    for (GuiButton button : buttonList) {
      ((ConsoleButton)button).keyboardKey=true;
      ((ConsoleButton)button).keyboardControl=button.id>=40;
    }
  }

  private void append(String s) {
    for (int i = 0; i < s.length() && value.length() < maxLength; i++) {
      char c = s.charAt(i);
      if (SharedConstants.isAllowedChatCharacter(c) && valid.test(value + c))
        value += c;
    }
  }

  private void erase() {
    if (!value.isEmpty()) value = value.substring(0, value.length() - 1);
  }

  @Override
  protected void actionPerformed(GuiButton b) {
    if (b.id < 40) append(b.displayString);
    else if (b.id == 40) {
      shift = !shift;
      rebuild();
    } else if (b.id == 41) {
      symbols = !symbols;
      rebuild();
    } else if (b.id == 42) append(" ");
    else if (b.id == 43) erase();
    else finish();
  }

  private void finish() {
    accept.accept(value);
    mc.setScreen(parent);
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    if (isCtrlKeyDown() && key == Keyboard.KEY_V) append(getClipboardString());
    else if (key == Keyboard.KEY_RETURN) finish();
    else if (key == Keyboard.KEY_BACK) erase();
    else if (key == 1) back();
    else if (SharedConstants.isAllowedChatCharacter(c))
      append(String.valueOf(c));
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    super.drawScreen(mx, my, partial);
    String visible = value;
    while (fontRendererObj.getStringWidth(visible) > panelW - 44 && !visible.isEmpty())
      visible = visible.substring(1);
    LceWidgets.textInput(panelX+14,panelY+32,panelW-28,23,
        visible+((System.currentTimeMillis()/500)%2==0?"_":""),true,true);
  }

  @Override
  protected String[] footerHints() {
    return new String[] {
      "cross",
      "Type",
      "square",
      "Space",
      "triangle",
      "Delete",
      "start_button",
      "Done",
      "circle",
      "Back"
    };
  }

  @Override
  public boolean controllerDirection(int d) {
    return false;
  }

  @Override
  public boolean controllerButton(int b) {
    if (b == PadState.SQUARE) {
      append(" ");
      return true;
    }
    if (b == PadState.TRIANGLE) {
      erase();
      return true;
    }
    if (b == PadState.L1) {
      shift = !shift;
      rebuild();
      return true;
    }
    if (b == PadState.R1) {
      symbols = !symbols;
      rebuild();
      return true;
    }
    if (b == PadState.OPTIONS) {
      finish();
      return true;
    }
    return false;
  }
}

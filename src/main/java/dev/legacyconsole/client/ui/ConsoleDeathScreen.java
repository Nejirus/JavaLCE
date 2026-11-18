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



/** UIScene_DeathMenu / DeathMenu720 over native respawn and hardcore handling. */
public final class ConsoleDeathScreen extends GuiGameOver implements ControllerScreen {
  private int fadeTicks;
  private boolean focused;

  public ConsoleDeathScreen(Component cause) {
    super(cause);
  }

  @Override
  public void initGui() {
    super.initGui();
    focused = false;
    fadeTicks = Math.max(fadeTicks, mc.player.deathTime);
    boolean hardcore = mc.level.getLevelData().isHardcore();
    buttonList.clear();
    buttonList.add(
        new ConsoleButton(
            0,
            width / 2 - 100,
            Math.round(LceViewport.y(width,height,400)),
            200,
            20,
            I18n.get(hardcore ? "deathScreen.spectate" : "deathScreen.respawn")));
    buttonList.add(
        new ConsoleButton(
            1, width / 2 - 100, Math.round(LceViewport.y(width,height,450)), 200, 20, "Exit Game"));
    for (GuiButton b : buttonList) b.enabled = false;
    MenuFocus.enter(this, buttonList);
  }

  @Override
  public void updateScreen() {
    super.updateScreen(); // Retain Java's initial respawn-button delay.
    fadeTicks = Math.min(21, fadeTicks + 1);
    if (!focused && !buttonList.isEmpty() && buttonList.get(0).enabled) {
      focused = true;
      MenuFocus.enter(this, buttonList);
      ControllerMenus.focus(this, 0);
    }
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    if (!button.enabled) return;
    if (button.id == 0) super.actionPerformed(button);
    else if (button.id == 1)
      mc.setScreen(
          new ConsoleConfirmScreen(
              this,
              "Exit Game",
              mc.hasSingleplayerServer()
                  ? "Save and exit to the main menu?"
                  : "Disconnect from this server?",
              () -> {
                if (mc.level != null) mc.level.disconnect();
                mc.clearLevel();
                mc.setScreen(new GuiMainMenu());
              }));
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    // Gui.cpp: DEATHFADE_DURATION=21, alpha 220, RGB 0x200000.
    int alpha = (int) (220 * Math.min(1, (fadeTicks + partial) / 21f));
    drawRect(0, 0, width, height, alpha << 24 | 0x200000);
    GlStateManager.pushMatrix();
    GlStateManager.scale(2, 2, 1);
    int titleY = Math.round(LceViewport.y(width,height,206)) / 2;
    // DeathMenu720 uses FJ_Label_OutlineTitle.
    for (int dy = -1; dy <= 1; dy++)
      for (int dx = -1; dx <= 1; dx++)
        if (dx != 0 || dy != 0)
          ConsoleTheme.centered("You Died!", width / 4 + dx, titleY + dy, 0xff000000);
    ConsoleTheme.centered("You Died!", width / 4, titleY, ConsoleTheme.TEXT);
    GlStateManager.popMatrix();
    for (GuiButton b : buttonList) b.drawButton(mc, mx, my);
    ConsoleTheme.footer(width, height, "cross", "Select");
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    if (key == Keyboard.KEY_UP || key == Keyboard.KEY_DOWN)
      MenuFocus.move(this, buttonList, key == Keyboard.KEY_UP ? -1 : 1);
    else if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
      GuiButton b = ConsoleMenuLayout.activation(this, buttonList);
      if (b != null) actionPerformed(b);
    }
    // LCE consumes Cancel: backing out cannot dismiss death or respawn the player.
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    int wheel = Mouse.getEventDWheel();
    if (wheel != 0) MenuFocus.move(this, buttonList, wheel > 0 ? -1 : 1);
  }

  @Override
  public boolean controllerDirection(int direction) {
    if (direction == PadState.UP || direction == PadState.DOWN)
      MenuFocus.move(this, buttonList, direction == PadState.UP ? -1 : 1);
    return true;
  }

  @Override
  public boolean controllerButton(int button) {
    return button == PadState.CIRCLE || button == PadState.OPTIONS;
  }
}

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



/** Active LCE sleeping path: darkened world and a use-button Wake Up prompt. */
public final class ConsoleSleepScreen extends LegacyScreen implements ControllerScreen {
  private boolean wakeSent;

  @Override
  public void initGui() {
    // Minecraft.cpp disables InBedChatScreen; LocalPlayer::handleMouseClick owns waking.
    buttonList.clear();
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }

  @Override
  public void updateScreen() {
    // Follow the server's normal wake flag, including sunrise and interrupted sleep.
    if (mc.player != null && !mc.player.isSleeping() && mc.screen == this)
      mc.setScreen(null);
  }

  private void wake() {
    if (wakeSent || mc.player == null || !mc.player.isSleeping()) return;
    wakeSent = true;
    ControllerInput.INSTANCE.blockUseAfterWake();
    mc.player.connection.send(
        new ServerboundPlayerCommandPacket(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
    // End the local bed pose with the request; the server still sends the final wake position.
    // This also keeps a held item from leaving the screen waiting on a stale sleep flag.
    mc.player.stopSleepInBed(false,false);
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    // The game already renders Gui.cpp's native sleep fade over the world.
    ConsoleTheme.footer(width, height, "left_trigger", "Wake Up");
  }

  @Override
  protected void mouseClicked(int x, int y, int button) throws IOException {
    if (button == 1) wake();
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    // Escape remains a keyboard wake shortcut; no keys can open or submit chat here.
    if (key == Keyboard.KEY_ESCAPE || key == Keyboard.fromGlfw(mc.options.keyUse.getKey().getValue())) wake();
  }

  @Override
  public void controllerOpened() {
    ControllerCursor.navigation();
  }

  @Override
  public boolean controllerDirection(int direction) {
    return true;
  }

  @Override
  public boolean controllerButton(int button) {
    // ControllerMenus dispatches press edges, so holding Use to enter bed cannot wake it.
    if (button == PadState.L2) wake();
    return true;
  }
}

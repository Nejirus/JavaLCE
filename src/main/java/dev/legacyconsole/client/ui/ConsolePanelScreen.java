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
import java.io.IOException;



public abstract class ConsolePanelScreen extends LegacyScreen {
  protected final Screen parent;
  protected String title;
  protected int panelX, panelY, panelW = 284, panelH = 194;

  protected ConsolePanelScreen(Screen parent, String title) {
    this.parent = parent;
    this.title = title;
  }

  @Override
  public void initGui() {
    panelW = Math.min(panelW, width - 16);
    panelH = Math.min(panelH, height - 34);
    panelX = (width - panelW) / 2;
    panelY = (height - panelH) / 2;
    buttonList.clear();
  }

  protected void addRow(int id, int row, String label) {
    buttonList.add(
        new ConsoleButton(id, panelX + 18, panelY + 31 + row * 24, panelW - 36, 20, label));
  }

  protected void addToggleRow(int id,int row,String label) {
    addRow(id,row,label);
    ConsoleButton button=(ConsoleButton)buttonList.get(buttonList.size()-1);
    button.allowCheckbox=true;
    button.yPosition+=4;
    button.height=ConsoleOptionLayout.CHECKBOX_HEIGHT;
    if(buttonList.size()>1) {
      GuiButton previous=buttonList.get(buttonList.size()-2);
      if(previous instanceof ConsoleButton && ((ConsoleButton)previous).allowCheckbox)
        button.yPosition=previous.yPosition+ConsoleOptionLayout.CHECKBOX_PITCH;
    }
  }

  protected void shell() {
    if (mc.level == null) ConsoleTheme.background(width, height);
    else drawDefaultBackground();
    ConsoleTheme.panel(panelX, panelY, panelW, panelH);
    ConsoleTheme.centered(title, width / 2, panelY + 13, ConsoleTheme.DARK);
  }

  protected void back() {
    ConsoleAudio.play("back");
    mc.setScreen(parent);
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    if (key == 1) back();
    else super.keyTyped(c, key);
  }

  protected String[] footerHints() {
    return new String[] {"cross", "Select", "circle", "Back"};
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    shell();
    super.drawScreen(mx, my, partial);
    ConsoleTheme.footer(width, height, footerHints());
  }
}

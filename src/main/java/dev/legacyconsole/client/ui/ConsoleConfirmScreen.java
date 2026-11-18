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





public final class ConsoleConfirmScreen extends ConsolePanelScreen {
  private final String message;
  private final Runnable confirmed;

  public ConsoleConfirmScreen(Screen parent, String title, String message, Runnable confirmed) {
    super(parent, title);
    this.message = message;
    this.confirmed = confirmed;
    panelH = 152;
  }

  @Override
  public void initGui() {
    boolean setting = parent instanceof ConsoleSettingsScreen;
    if (setting) panelW = ConsoleOptionLayout.WIDTH + 16;
    super.initGui();
    int inset = setting ? 8 : 18;
    buttonList.add(
        new ConsoleButton(0, panelX + inset, panelY + 94, panelW - inset * 2, 20, "Cancel"));
    buttonList.add(
        new ConsoleButton(
            1, panelX + inset, panelY + (setting ? 119 : 118), panelW - inset * 2, 20, "Confirm"));
  }

  @Override
  protected void actionPerformed(GuiButton b) {
    if (b.id == 1) confirmed.run();
    else back();
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    super.drawScreen(mx, my, partial);
    fontRendererObj.drawSplitString(
        message, panelX + 18, panelY + 36, panelW - 36, ConsoleTheme.DARK);
  }
}

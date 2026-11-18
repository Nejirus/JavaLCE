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



/** Retains every vanilla inventory slot ID; the manual crafting grid is hidden. */
public final class ConsoleInventory extends ConsoleContainerScreen implements ControllerScreen {
  private final int[][] original;

  public ConsoleInventory(Player player) {
    super(player.inventoryMenu);
    xSize = 215;
    ySize = 217;
    original = new int[inventorySlots.slots.size()][2];
    for (int i = 0; i < original.length; i++) {
      Slot s = inventorySlots.getSlot(i);
      original[i][0] = s.x;
      original[i][1] = s.y;
    }
  }

  @Override protected boolean consoleUiScaling(){return true;}

  @Override
  public void initGui() {
    super.initGui();
    mc.player.containerMenu = inventorySlots;
    guiTop = Math.max(2, (height - ySize) / 2 - 6);
    for (int i = 0; i < inventorySlots.slots.size(); i++) {
      Slot s = inventorySlots.getSlot(i);
      if (i <= 4) {
        s.x = -10000;
        s.y = -10000;
      } else if (i <= 8) {
        s.x = 66;
        s.y = 14 + (i - 5) * 21;
      } else if (i <= 35) {
        s.x = 15 + (i - 9) % 9 * 21;
        s.y = 117 + (i - 9) / 9 * 21;
      } else if (i <= 44) {
        s.x = 15 + (i - 36) * 21;
        s.y = 187;
      } else {
        s.x = 164;
        s.y = 78;
      }
    }
  }

  @Override
  public void onGuiClosed() {
    for (int i = 0; i < original.length; i++) {
      inventorySlots.getSlot(i).x = original[i][0];
      inventorySlots.getSlot(i).y = original[i][1];
    }
    super.onGuiClosed();
  }

  private void crafting() {
    mc.player.closeContainer();
    ConsoleScreens.craftingOrCreative();
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    if (key == Keyboard.KEY_C) crafting();
    else super.keyTyped(c, key);
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {}

  @Override
  public int[] controllerSlotBounds(Slot slot) {
    return new int[] {slot.x - 2, slot.y - 2, 21, 21};
  }

  @Override
  protected int[] inventoryHighlightBounds(Slot slot) {
    return slot.x < 0 || slot.y < 0 ? null : controllerSlotBounds(slot);
  }

  @Override
  public void drawTexturedModalRect(int x, int y, TextureAtlasSprite sprite, int w, int h) {
    // GuiContainer otherwise paints Java's empty equipment sprite over our highlight.
    // The reference equipment artwork has already been drawn in the background pass.
    if (w == 16 && h == 16)
      for (int i : new int[] {5, 6, 7, 8, 45}) {
        Slot slot = inventorySlots.getSlot(i);
        if (!slot.hasItem() && x == slot.x && y == slot.y) return;
      }
    super.drawTexturedModalRect(x, y, sprite, w, h);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partial, int mx, int my) {
    ConsoleTheme.panel(guiLeft, guiTop, xSize, ySize);
    ConsoleTheme.sprite("gui/sprites/tiles/entity_panel", guiLeft + 90, guiTop + 13, 63, 84);
    InventoryScreen.renderEntityInInventory(
        guiLeft + 121, guiTop + 92, 35, guiLeft + 121 - mx, guiTop + 40 - my, mc.player);
    String[] armor = {"head", "chest", "legs", "feet"};
    for (int i = 5; i < inventorySlots.slots.size(); i++) {
      Slot s = inventorySlots.getSlot(i);
      int[] bounds = controllerSlotBounds(s);
      ConsoleTheme.slot(guiLeft + bounds[0], guiTop + bounds[1], bounds[2], false);
      if (!s.hasItem() && (i <= 8 || i == 45))
        ConsoleTheme.sprite(
            "gui/sprites/container/" + (i == 45 ? "shield" : armor[i - 5]) + "_slot",
            guiLeft + s.x,
            guiTop + s.y,
            16,
            16);
    }
    drawInventoryHighlight(mx, my);
    ConsoleTheme.text("Inventory", guiLeft + 14, guiTop + 103, ConsoleTheme.DARK);
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    drawDefaultBackground();
    super.drawScreen(mx, my, partial);
    ConsoleTheme.footer(width,height,Ps4InventoryActions.prompts(getSlotUnderMouse(),mc.player.containerMenu.getCarried(),mc.player));
  }

  @Override
  public boolean controllerDirection(int direction) {
    return false;
  }

  @Override
  public boolean controllerButton(int button) {
    // IUIScene_AbstractContainerMenu maps Square/X to native right click: half / one.
    // ControllerMenus dispatches that click with the original slot and transaction IDs.
    return false;
  }

  @Override
  public boolean controllerPointer() {
    return true;
  }
}

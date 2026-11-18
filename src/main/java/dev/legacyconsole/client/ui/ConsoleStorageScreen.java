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





/** Console panel/slot artwork for containers whose actions are standard inventory clicks. */
public final class ConsoleStorageScreen extends ConsoleContainerScreen
    implements dev.legacyconsole.client.input.ControllerScreen {
  private final int[][] positions;
  private final String title;
  private final MenuData storage;
  private final boolean furnace;
  private final int shiftY;
  private boolean compact;

  public ConsoleStorageScreen(AbstractContainerScreen<?> original) {
    super(original.getMenu());
    xSize = original.getXSize() + 20;
    int height = original.getYSize();
    shiftY = height > 214 ? 0 : 4;
    ySize = height + shiftY * 2;
    furnace = original instanceof AbstractFurnaceScreen<?>;
    storage = furnace?new MenuData(inventorySlots):null;
    title = original.getTitle().getString();
    positions = new int[inventorySlots.slots.size()][2];
    for (int i = 0; i < positions.length; i++) {
      Slot s = inventorySlots.getSlot(i);
      positions[i][0] = s.x;
      positions[i][1] = s.y;
    }
  }

  @Override protected boolean consoleUiScaling(){return true;}

  @Override
  public void initGui() {
    xSize = furnace ? 214 : 215;
    int storageSlots = 0;
    for (Slot slot : inventorySlots.slots)
      if (slot.container != mc.player.getInventory()) storageSlots++;
    boolean dispenser = storageSlots == 9;
    int rows = dispenser || furnace ? 3 : (storageSlots + 8) / 9;
    int extra = (rows - 3) * 21;
    ySize = furnace ? 215 : 207 + extra;
    compact = ySize > height - 18;
    if (compact) ySize = Math.min(223, 114 + rows * 18);
    if (compact && !furnace) xSize = 188;
    super.initGui();
    guiTop = Math.max(2, (height - ySize) / 2 - 6);
    for (int i = 0; i < positions.length; i++) {
      Slot slot = inventorySlots.getSlot(i);
      if (slot.container == mc.player.getInventory()) {
        int n = slot.getContainerSlot();
        slot.x = compact ? 13 + (n % 9) * 18 : 15 + (n % 9) * 21;
        slot.y =
            compact
                ? positions[i][1]
                : (n < 9
                    ? (furnace ? 180 : 177) + extra
                    : (furnace ? 111 : 107) + (n - 9) / 9 * 21 + extra);
      } else if (furnace) {
        slot.x = i == 2 ? 161 : 77;
        slot.y = i == 0 ? 25 : i == 1 ? 72 : 50;
      } else {
        slot.x =
            compact ? 13 + i % 9 * 18 : (dispenser ? 77 : 15) + (i % (dispenser ? 3 : 9)) * 21;
        slot.y = compact ? positions[i][1] : 26 + i / (dispenser ? 3 : 9) * 21;
      }
    }
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int x, int y) {}

  @Override
  public int[] controllerSlotBounds(Slot slot) {
    int size = furnace && slot.index == 2 ? 32 : compact ? 18 : 21;
    int pad = furnace && slot.index == 2 ? 8 : compact ? 1 : 2;
    return new int[] {slot.x - pad, slot.y - pad, size, size};
  }

  @Override
  protected int[] inventoryHighlightBounds(Slot slot) {
    // The large furnace result holder deliberately retains its small native highlight.
    return furnace && slot.index == 2 ? null : controllerSlotBounds(slot);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partial, int mx, int my) {
    ConsoleTheme.lcePanel(guiLeft, guiTop, xSize, ySize);
    ConsoleTheme.text(title, guiLeft + 13, guiTop + 8, ConsoleTheme.DARK);
    int firstPlayer = Integer.MAX_VALUE;
    for (Slot s : inventorySlots.slots) {
      int[] bounds = controllerSlotBounds(s);
      ConsoleTheme.slot(guiLeft + bounds[0], guiTop + bounds[1], bounds[2], false);
      if (s.container == mc.player.getInventory()) firstPlayer = Math.min(firstPlayer, s.y);
    }
    if (firstPlayer != Integer.MAX_VALUE)
      ConsoleTheme.text(
          "Inventory",
          guiLeft + 13,
          guiTop + firstPlayer - (furnace ? 13 : compact ? 11 : 16),
          ConsoleTheme.DARK);
    if (furnace && storage != null) {
      int total = storage.getField(3);
      float cook = total == 0 ? 0 : storage.getField(2) / (float) total;
      ConsoleTheme.text(
          "Ingredient",
          guiLeft + 70 - fontRendererObj.getStringWidth("Ingredient"),
          guiTop + 32,
          ConsoleTheme.DARK);
      ConsoleTheme.text(
          "Fuel",
          guiLeft + 70 - fontRendererObj.getStringWidth("Fuel"),
          guiTop + 79,
          ConsoleTheme.DARK);
      ConsoleTheme.sprite("lce/arrow_off", guiLeft + 113, guiTop + 47, 36, 24);
      // LCE FurnaceMenu uses 24 arrow states and 12 flame states. Clip each
      // state at the original scale: rounding the source/destination separately
      // made the arrow stretch on alternating updates.
      int arrow = (int) (Math.max(0, Math.min(1, cook)) * 24);
      ConsoleTheme.clippedSprite(
          "lce/arrow_on", guiLeft + 113, guiTop + 47, 36, 24, arrow / 24f, 1, false);
      // The unlit source is already upright. Only the lit source needs flipping;
      // preserve the original PNG bytes for both layers.
      ConsoleTheme.sprite("lce/flame_off", guiLeft + 74, guiTop + 45, 24, 24);
      if (storage.getField(0) > 0) {
        int fuel = storage.getField(1);
        int lit =
            (int) (Math.max(0, Math.min(1, storage.getField(0) / (float) Math.max(1, fuel))) * 12);
        ConsoleTheme.clippedSprite(
            "lce/flame_on", guiLeft + 74, guiTop + 45, 24, 24, 1, lit / 12f, true);
      }
    }
    drawInventoryHighlight(mx, my);
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    drawDefaultBackground();
    super.drawScreen(mx, my, partial);
    ConsoleTheme.footer(
        width,
        height,
        "cross",
        "Take",
        "square",
        "Split",
        "triangle",
        "Quick Move",
        "circle",
        "Back");
  }

  @Override
  public boolean controllerPointer() {
    return true;
  }

  @Override
  public boolean controllerDirection(int direction) {
    return false;
  }

  @Override
  public boolean controllerButton(int button) {
    return false;
  }
}

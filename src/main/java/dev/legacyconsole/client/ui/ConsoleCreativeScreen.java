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

import dev.legacyconsole.client.ClientConfig;
import dev.legacyconsole.client.ConsoleAudio;
import dev.legacyconsole.client.input.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;



/** LCE ItemPickerMenu/TabSpec presentation with Minecraft's native creative transactions. */
public final class ConsoleCreativeScreen extends ConsoleContainerScreen
    implements ControllerScreen {
  private static final Method TAB =
      ReflectionHelper.findMethod(
          GuiContainerCreative.class, "setCurrentCreativeTab", "func_147050_b", CreativeModeTab.class);
  private static final Method CLICK =
      ReflectionHelper.findMethod(
          GuiContainerCreative.class,
          "handleMouseClick",
          "func_184098_a",
          Slot.class,
          int.class,
          int.class,
          ClickType.class);
  private final GuiContainerCreative nativeGui;
  private final ConsoleCreativeContainer picker;
  private List<ConsoleCreativeCatalog.Tab> tabs;
  private List<ItemStack> displayItems;
  private final int[] tabPages = new int[8];
  private int tab, page, stickDirection, stickTicks, lastHotbarSlot;
  private boolean initialized, editingSearch, draggingScroll;
  private String query = "";

  public ConsoleCreativeScreen(Player player) {
    this(player, new ConsoleCreativeContainer(player));
  }

  private ConsoleCreativeScreen(Player player, ConsoleCreativeContainer picker) {
    super(picker);
    this.picker = picker;
    nativeGui = new GuiContainerCreative(player);
  }

  private void call(Method method, Object... args) {
    try {
      method.invoke(nativeGui, args);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Native creative transaction failed", e);
    }
  }

  @Override protected boolean consoleUiScaling(){return true;}
  @Override protected float consoleUiTopInset(){return 36;}

  @Override
  public void initGui() {
    editingSearch = false;
    if (initialized) {
      net.minecraft.world.inventory.ContainerListener listener =
          ReflectionHelper.getPrivateValue(
              GuiContainerCreative.class, nativeGui, "listener", "field_147059_E");
      mc.player.inventoryMenu.removeSlotListener(listener);
    }
    nativeGui.setWorldAndResolution(mc, width, height);
    call(TAB, CreativeModeTab.TAB_BUILDING_BLOCKS);
    initialized = true;
    ySize = Math.min(212, height - 58);
    xSize = Math.min(ySize >= 210 ? 321 : 270, width - 8);
    super.initGui();
    mc.player.containerMenu = picker;
    guiLeft = (width - xSize) / 2;
    guiTop = Math.max(35, (height - ySize + 20) / 2);
    if (tabs == null) tabs = ConsoleCreativeCatalog.build();
    buttonList.clear();
    layout();
    rebuildItems();
  }

  private void layout() {
    int step = ySize >= 210 ? 27 : 22, firstY = ySize >= 210 ? 32 : 26;
    for (int i = 0; i < CreativePage.SIZE; i++) {
      Slot s = picker.getSlot(i);
      s.x = (ySize >= 210 ? 18 : 15) + i % 10 * step;
      s.y = firstY + i / 10 * step;
    }
    int start = (xSize - 9 * step) / 2 + (ySize >= 210 ? 5 : 3);
    for (int i = 0; i < 9; i++) {
      Slot s = picker.getSlot(CreativePage.SIZE + i);
      s.x = start + i * step;
      s.y = ySize - 29;
    }
  }

  private void rebuildItems() {
    displayItems = new ArrayList<>();
    if (query.isEmpty()) displayItems.addAll(tabs.get(tab).items);
    else {
      String term = query.toLowerCase(Locale.ROOT);
      Set<String> seen = new HashSet<>();
      for (ConsoleCreativeCatalog.Tab t : tabs)
        for (ItemStack s : t.items)
          if (s.getHoverName().getString().toLowerCase(Locale.ROOT).contains(term)
              && seen.add(s.serializeNBT().toString())) displayItems.add(s);
    }
    fill();
  }

  private int pages() {
    return CreativePage.count(displayItems.size());
  }

  private void fill() {
    page = Math.max(0, Math.min(page, pages() - 1));
    tabPages[tab] = page;
    // TabSpec::populateMenu: page offset, fill fifty slots, clear the remainder.
    for (int i = 0; i < CreativePage.SIZE; i++) {
      int index = page * CreativePage.SIZE + i;
      picker.catalog.setItem(
          i, index < displayItems.size() ? displayItems.get(index).copy() : ItemStack.EMPTY);
    }
  }

  private void choose(int next) {
    tab = Math.floorMod(next, tabs.size());
    query = "";
    page = tabPages[tab];
    rebuildItems();
    ConsoleAudio.play("tab");
  }

  private void scroll(int delta) {
    int before = page;
    page = Math.max(0, Math.min(page + delta, pages() - 1));
    if (page != before) {
      fill();
      ConsoleAudio.play("focus");
    }
  }

  private int scrollTop() {
    return guiTop + 25;
  }

  private int scrollHeight() {
    return ySize >= 210 ? 135 : 109;
  }

  private void scrollBar(int my) {
    // UIScene_CreativeMenu::ScrollBar rounds normalized pointer position to a tab page.
    page =
        CreativePage.at(
            (my - scrollTop() - 8) / (float) (scrollHeight() - 16), displayItems.size());
    fill();
  }

  private void search() {
    editingSearch = true;
    mc.setScreen(
        new ConsoleKeyboard(
            this,
            "Search Items",
            query,
            v -> {
              query = v;
              page = 0;
            }));
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partial, int mx, int my) {
    ConsoleTheme.portion("lce/creative_panel", guiLeft, guiTop - 34, xSize, 34, 0, 0, 643, 68);
    ConsoleTheme.portion("lce/creative_panel", guiLeft, guiTop, xSize, ySize, 0, 68, 643, 422);
    float scale = xSize / 643f;
    ConsoleTheme.lceTab(
        "lce/creative_tab_" + (tab == 0 ? "left" : tab == 7 ? "right" : "middle"),
        guiLeft + tab * 80 * scale,
        guiTop - 35.5f,
        scale,
        0);
    for (int i = 0; i < tabs.size(); i++)
      ConsoleTheme.sprite(
          "gui/sprites/icon/" + tabs.get(i).icon,
          guiLeft + i * xSize / 8 + (xSize / 8 - 24) / 2,
          guiTop - 29,
          24,
          24);
    ConsoleTheme.centered(
        ConsoleTheme.fit(query.isEmpty() ? tabs.get(tab).title : "Search: " + query, xSize - 26),
        guiLeft + xSize / 2,
        guiTop + 9,
        ConsoleTheme.DARK);
    for (Slot s : picker.slots) {
      boolean compact = ySize < 210;
      int pad = compact ? 3 : 5, size = compact ? 22 : 27;
      ConsoleTheme.sprite(
          "lce/crafting_slot", guiLeft + s.x - pad, guiTop + s.y - pad, size, size);
    }
    int sx = guiLeft + xSize - 25, sy = scrollTop(), sh = scrollHeight();
    ConsoleTheme.sprite("gui/sprites/tiles/square_recessed_panel", sx, sy, 13, sh);
    int thumb = sy + (pages() > 1 ? Math.round(page * (sh - 16) / (float) (pages() - 1)) : 0);
    ConsoleTheme.panel(sx - 1, thumb, 16, 16);
    if (page > 0) ConsoleTheme.sprite("gui/sprites/widget/scroll_up", sx, sy - 10, 13, 7);
    if (page < pages() - 1)
      ConsoleTheme.sprite("gui/sprites/widget/scroll_down", sx, sy + sh + 4, 13, 7);
    ConsoleTheme.text(
        (page + 1) + "/" + pages(), guiLeft + 9, guiTop + ySize - 15, ConsoleTheme.DARK);
    drawInventoryHighlight(mx, my);
  }

  @Override
  public int[] controllerSlotBounds(Slot slot) {
    int size = ySize < 210 ? 22 : 27, pad = ySize < 210 ? 3 : 5;
    return new int[] {slot.x - pad, slot.y - pad, size, size};
  }

  @Override
  protected int[] inventoryHighlightBounds(Slot slot) {
    return controllerSlotBounds(slot);
  }

  @Override
  protected float[] inventoryHighlightFace(Slot slot) {
    int[] bounds = controllerSlotBounds(slot);
    // Crafting_SlotLarge.png has a 3px bevel on each side of its 54px holder.
    float inset = bounds[2] / 18f, inner = bounds[2] * 48f / 54f;
    return new float[] {bounds[0] + inset, bounds[1] + inset, inner, inner};
  }

  @Override
  protected float[] itemDisplayBounds(Slot slot) {
    return inventoryHighlightFace(slot);
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mx, int my) {}

  private void nativeClick(Slot visible, int id, int button, ClickType type) {
    Slot actual = null;
    int nativeId = id;
    if (visible != null) {
      if (visible.index < CreativePage.SIZE) {
        actual = nativeGui.inventorySlots.getSlot(0);
        actual.set(visible.getItem().copy());
      } else actual = nativeGui.inventorySlots.getSlot(45 + visible.index - CreativePage.SIZE);
      nativeId = actual.index;
    }
    call(CLICK, actual, nativeId, button, type);
  }

  @Override
  protected void handleMouseClick(Slot slot, int id, int button, ClickType type) {
    creativeClick(slot, id, button, type);
    ConsoleAudio.inventoryAction(type, button);
  }

  private void creativeClick(Slot slot, int id, int button, ClickType type) {
    if (slot == null && id == -999) {
      // LCE handleOutsideClicked: drop the carried stack, or one item with right click.
      ItemStack carried = picker.getCarried();
      if (!carried.isEmpty()) {
        ItemStack dropped = carried.split(button == 0 ? carried.getCount() : 1);
        mc.gameMode.handleCreativeModeItemDrop(dropped);
        if (carried.isEmpty()) picker.setCarried(ItemStack.EMPTY);
      }
      return;
    }
    if (slot != null && slot.index < CreativePage.SIZE
        && (type == ClickType.PICKUP || type == ClickType.CLONE || type == ClickType.QUICK_MOVE)) {
      if (button == 1 && type == ClickType.PICKUP) { clearHotbar(); return; }
      int target = LceCreativeHotbar.take(mc.player.getInventory().items,
          slot.getItem(), type != ClickType.PICKUP, lastHotbarSlot);
      if (target >= 0) {
        lastHotbarSlot = target;
        picker.setCarried(ItemStack.EMPTY);
        mc.gameMode.handleCreativeModeItemAdd(mc.player.getInventory().getItem(target), 36 + target);
      }
      return;
    }
    if (slot != null && slot.index >= CreativePage.SIZE)
      lastHotbarSlot = slot.index - CreativePage.SIZE;
    nativeClick(slot, id, button, type);
  }

  private void clearHotbar() {
    for (int i = 0; i < 9; i++) {
      mc.player.getInventory().setItem(i, ItemStack.EMPTY);
      mc.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, 36 + i);
    }
    ConsoleAudio.play("confirm");
  }

  @Override
  protected void mouseClicked(int mx, int my, int button) throws IOException {
    if (button == 0 && my >= guiTop - 35 && my < guiTop && mx >= guiLeft && mx < guiLeft + xSize) {
      choose(Math.min(7, (mx - guiLeft) * 8 / xSize));
      return;
    }
    if (button == 0
        && mx >= guiLeft + xSize - 29
        && mx < guiLeft + xSize - 5
        && my >= scrollTop() - 11
        && my < scrollTop() + scrollHeight() + 12) {
      if (my < scrollTop()) scroll(-1);
      else if (my >= scrollTop() + scrollHeight()) scroll(1);
      else {
        draggingScroll = true;
        scrollBar(my);
      }
      return;
    }
    super.mouseClicked(mx, my, button);
  }

  @Override
  protected void mouseReleased(int mx, int my, int button) {
    if (!ControllerCursor.active() || !ControllerInput.INSTANCE.state.held(PadState.CROSS))
      draggingScroll = false;
    super.mouseReleased(mx, my, button);
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    if (Mouse.getEventDWheel() != 0) scroll(Mouse.getEventDWheel() > 0 ? -1 : 1);
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    if (key == Keyboard.KEY_TAB) {
      choose(tab + (isShiftKeyDown() ? -1 : 1));
      return;
    }
    if (key == Keyboard.KEY_F) {
      search();
      return;
    }
    if (key == Keyboard.KEY_PRIOR || key == Keyboard.KEY_NEXT) {
      scroll(key == Keyboard.KEY_PRIOR ? -1 : 1);
      return;
    }
    super.keyTyped(c, key);
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    if (draggingScroll) {
      if (Mouse.isButtonDown(0)
          || ControllerCursor.active() && ControllerInput.INSTANCE.state.held(PadState.CROSS))
        scrollBar(my);
      else draggingScroll = false;
    }
    drawDefaultBackground();
    super.drawScreen(mx, my, partial);
    ConsoleTheme.footer(
        width,
        height,
        "cross",
        "Take One",
        "square",
        "Clear Quick Select",
        "triangle",
        "Take Stack",
        "circle",
        "Back");
  }

  @Override
  public void updateScreen() {
    if (!mc.gameMode.hasInfiniteItems())
      mc.setScreen(new ConsoleInventory(mc.player));
  }

  @Override
  public void onGuiClosed() {
    if (!editingSearch) nativeGui.onGuiClosed();
    if (mc.player != null && mc.player.containerMenu == picker)
      mc.player.containerMenu = mc.player.inventoryMenu;
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
  public void controllerOpened() {
    ControllerMenus.pointAt(guiLeft + 28, guiTop + picker.getSlot(0).y + 8);
  }

  @Override
  public void controllerTick(PadState state, PadState previous) {
    float axis = ClientConfig.cursorStick == 1 ? state.ly : state.ry;
    int direction = ClientConfig.cursorStick == 2 || Math.abs(axis) < .65f ? 0 : axis > 0 ? -1 : 1;
    if (direction != stickDirection) stickTicks = 0;
    else stickTicks++;
    if (direction != 0 && (stickTicks == 0 || stickTicks >= 8 && stickTicks % 3 == 0))
      scroll(direction);
    stickDirection = direction;
  }

  @Override
  public boolean controllerButton(int button) {
    if (button == PadState.L1 || button == PadState.R1) {
      choose(tab + (button == PadState.L1 ? -1 : 1));
      return true;
    }
    if (button == PadState.L2 || button == PadState.R2) {
      scroll(button == PadState.L2 ? -1 : 1);
      return true;
    }
    if (button == PadState.TRIANGLE) {
      Slot s = getSlotUnderMouse();
      if (s != null && s.index < CreativePage.SIZE)
        handleMouseClick(s, s.index, 0, ClickType.CLONE);
      return true;
    }
    if (button == PadState.L3) {
      search();
      return true;
    }
    if (button == PadState.SQUARE) {
      clearHotbar();
      return true;
    }
    return false;
  }
}

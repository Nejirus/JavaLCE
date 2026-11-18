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



/** Legacy4J RenderableVList's row scrolling and focus behavior for option submenus. */
abstract class ConsoleVerticalList extends ConsolePanelScreen implements ControllerScreen {
  protected int first, selected, visibleRows;

  ConsoleVerticalList(Screen parent, String title) {
    super(parent, title);
    panelH = 202;
  }

  protected abstract int itemCount();

  protected abstract String itemLabel(int index);

  protected boolean itemIsToggle(int index) {return false;}

  protected abstract void activateItem(int index);

  @Override
  public void initGui() {
    rebuildList();
  }

  protected final void rebuildList() {
    selected = Math.max(0, Math.min(selected, itemCount()));
    boolean[] boxes=new boolean[itemCount()+1];
    for(int i=0;i<itemCount();i++)boxes[i]=itemIsToggle(i) && LceWidgets.toggle(itemLabel(i))!=null;
    ConsoleOptionLayout layout = new ConsoleOptionLayout(width,height,boxes,0,first,selected,
        ConsoleOptionLayout.CHECKBOX_PITCH);
    panelW=layout.w; panelH=layout.h; panelX=layout.x; panelY=layout.y;
    first=layout.first; visibleRows=layout.rows;
    buttonList.clear();
    for (int i = first; i < Math.min(itemCount() + 1, first + visibleRows); i++) {
      ConsoleButton button=new ConsoleButton(
              i == itemCount() ? 99 : 100 + i,
              panelX + 8,
              layout.rowY(i),
              panelW - 16,
              layout.rowHeight(i),
              i == itemCount() ? "Back" : itemLabel(i));
      button.allowCheckbox=i<itemCount() && itemIsToggle(i);
      buttonList.add(button);
    }
    MenuFocus.set(this, selected == itemCount() ? 99 : 100 + selected);
  }

  private void move(int delta) {
    selected = Math.floorMod(selected + delta, itemCount() + 1);
    rebuildList();
    ControllerMenus.focus(this, selected == itemCount() ? 99 : 100 + selected);
    ConsoleAudio.play("focus");
  }

  private void scroll(int delta) {
    move(delta);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button.id == 99) back();
    else if (button.id >= 100) {
      selected = button.id - 100;
      activateItem(selected);
    }
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    int wheel = Mouse.getEventDWheel();
    if (wheel != 0) scroll(wheel > 0 ? -1 : 1);
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    if (key == Keyboard.KEY_UP) move(-1);
    else if (key == Keyboard.KEY_DOWN) move(1);
    else if (key == Keyboard.KEY_PRIOR || key == Keyboard.KEY_NEXT)
      scroll(key == Keyboard.KEY_PRIOR ? -1 : 1);
    else if (key == Keyboard.KEY_RETURN) {
      ConsoleTheme.buttonPressed(MenuFocus.button(this));
      if (selected == itemCount()) back();
      else activateItem(selected);
    } else super.keyTyped(c, key);
  }

  @Override
  public void drawScreen(int x, int y, float partial) {
    super.drawScreen(x, y, partial);
    if (first > 0)
      ConsoleTheme.sprite("gui/sprites/widget/scroll_up", panelX + panelW + 5, panelY + 8, 7, 5);
    if (first + visibleRows < itemCount() + 1)
      ConsoleTheme.sprite(
          "gui/sprites/widget/scroll_down", panelX + panelW + 5, panelY + panelH - 13, 7, 5);
  }

  @Override
  protected void shell() {
    ConsoleMenuLayout.background(width, height);
    ConsoleMenuLayout.logo(width, height);
    ConsoleTheme.lcePanel(panelX, panelY, panelW, panelH);
    ConsoleTheme.centered(
        ConsoleTheme.fit(title, panelW), width / 2, panelY - 12, ConsoleTheme.TEXT);
  }

  @Override
  public void controllerOpened() {
    selected = first = 0;
    rebuildList();
    ControllerMenus.focus(this, 100);
  }

  @Override
  public boolean controllerDirection(int direction) {
    if (direction == PadState.UP || direction == PadState.DOWN)
      move(direction == PadState.UP ? -1 : 1);
    return true;
  }

  @Override
  public boolean controllerButton(int button) {
    if (button == PadState.L2 || button == PadState.R2) {
      scroll(button == PadState.L2 ? -1 : 1);
      return true;
    }
    if (button == PadState.CROSS) {
      GuiButton b = MenuFocus.button(this);
      if (b != null) actionPerformed(b);
      return true;
    }
    return false;
  }
}

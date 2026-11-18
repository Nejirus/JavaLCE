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





/** Help button geometry plus the original Options/UI checkbox row pitches. */
final class ConsoleOptionLayout {
  static final int WIDTH = 225, HEIGHT = 20, PITCH = 25, MARGIN = 8;
  static final int CHECKBOX_HEIGHT = 12, CHECKBOX_PITCH = 15, UI_CHECKBOX_PITCH = 18;
  final int x, y, w, h, rows, first;
  private final boolean[] checkboxes;
  private final int checkboxPitch, firstRow;

  ConsoleOptionLayout(int width, int height, int count, int messageHeight) {
    this(width, height, new boolean[Math.max(1, count)], messageHeight, 0, 0, CHECKBOX_PITCH);
  }

  ConsoleOptionLayout(int width, int height, boolean[] checkboxes, int messageHeight,
      int requestedFirst, int selected, int checkboxPitch) {
    this.checkboxes = checkboxes;
    this.checkboxPitch = checkboxPitch;
    w = Math.min(WIDTH + MARGIN * 2, width - 16);
    x = (width - w) / 2;
    y = Math.round(LceViewport.y(width,height,250)) - MARGIN;
    firstRow = y + MARGIN + messageHeight;
    int available = Math.max(HEIGHT, height - 28 - MARGIN - firstRow);
    int count = checkboxes.length;
    int lastPage = Math.max(0, count - 1), used = count == 0 ? 0 : rowHeight(lastPage);
    while (lastPage > 0 && used + gap(lastPage - 1) + rowHeight(lastPage - 1) <= available) {
      used += gap(lastPage - 1) + rowHeight(--lastPage);
    }
    selected = Math.max(0, Math.min(selected, count - 1));
    int start = Math.max(0, Math.min(requestedFirst, lastPage));
    if (selected < start) start = selected;
    while (start < selected && end(start, available) <= selected) start++;
    first = start;
    rows = end(first, available) - first;
    int content = rows == 0 ? 0 : rowY(first + rows - 1) - firstRow + rowHeight(first + rows - 1);
    h = MARGIN * 2 + messageHeight + content;
  }

  private int end(int start, int available) {
    int end = start, used = 0;
    while (end < checkboxes.length) {
      int next = rowHeight(end) + (end > start ? gap(end - 1) : 0);
      if (end > start && used + next > available) break;
      used += next;
      end++;
    }
    return end;
  }

  private int gap(int index) {
    return gap(checkboxes[index], checkboxes[index + 1], checkboxPitch);
  }

  static int gap(boolean checkbox, boolean nextCheckbox, int checkboxPitch) {
    if (checkbox && nextCheckbox) return checkboxPitch - CHECKBOX_HEIGHT;
    return checkbox ? 6 : PITCH - HEIGHT;
  }

  int rowHeight(int index) { return checkboxes[index] ? CHECKBOX_HEIGHT : HEIGHT; }

  int rowY(int index) {
    int result = firstRow;
    for (int i = first; i < index; i++) result += rowHeight(i) + gap(i);
    return result;
  }
}

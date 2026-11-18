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



/** Shared console sound for native inventory actions from mouse or controller. */
public abstract class ConsoleContainerScreen extends GuiContainer {
  private Slot highlightedSlot;

  public final boolean replacesNativeHighlight(int x,int y) {
    return highlightedSlot != null && highlightedSlot.x == x && highlightedSlot.y == y;
  }

  protected ConsoleContainerScreen(AbstractContainerMenu container) {
    super(container);
  }

  /** Regular holders opt in; composite and enlarged result slots keep their native overlay. */
  protected int[] inventoryHighlightBounds(Slot slot) {
    return null;
  }

  protected boolean inventoryHighlightContains(Slot slot, int mx, int my) {
    int[] bounds = inventoryHighlightBounds(slot);
    return bounds != null && inside(bounds, mx, my);
  }

  /** Visual inner face; source composites can have a different bevel than regular holders. */
  protected float[] inventoryHighlightFace(Slot slot) {
    int[] bounds = inventoryHighlightBounds(slot);
    return bounds == null
        ? null
        : new float[] {
          bounds[0] + bounds[2] / 20f,
          bounds[1] + bounds[3] / 20f,
          bounds[2] * 18f / 20f,
          bounds[3] * 18f / 20f
        };
  }

  /** Null preserves the original Java item viewport. */
  protected float[] itemDisplayBounds(Slot slot) {
    return null;
  }

  @Override
  protected void drawSlot(Slot slot) {
    float[] face = itemDisplayBounds(slot);
    if (face == null) {
      super.drawSlot(slot);
      return;
    }
    // UIScene::_customDrawSlotControl scales the native item viewport by its width/16.
    // Delegate drag splitting, counts and durability to the original renderer.
    GlStateManager.pushMatrix();
    GlStateManager.translate(face[0], face[1], 0);
    GlStateManager.scale(face[2] / 16f, face[3] / 16f, 1);
    GlStateManager.translate(-slot.x, -slot.y, 0);
    super.drawSlot(slot);
    GlStateManager.popMatrix();
  }

  protected final void drawInventoryHighlight(int mx, int my) {
    highlightedSlot = null;
    for (Slot slot : inventorySlots.slots) {
      int[] bounds = inventoryHighlightBounds(slot);
      if (bounds == null || !slot.isActive() || !inventoryHighlightContains(slot, mx, my))
        continue;
      highlightedSlot = slot;
      float[] face = inventoryHighlightFace(slot);
      // Original 20x20 holder: one-pixel bevel surrounding its 18x18 inner face.
      // Empty equipment artwork is already drawn; actual items render in the native item pass.
      GlStateManager.pushMatrix();
      GlStateManager.translate(guiLeft + face[0], guiTop + face[1], 0);
      highlightSprite(face[2], face[3]);
      GlStateManager.popMatrix();
      break;
    }
  }

  protected void highlightSprite(float width, float height) {
    ConsoleTheme.spriteSize("gui/sprites/container/slot_highlight", 0, 0, width, height);
  }

  private boolean inside(int[] bounds, int mx, int my) {
    return mx >= guiLeft + bounds[0]
        && mx < guiLeft + bounds[0] + bounds[2]
        && my >= guiTop + bounds[1]
        && my < guiTop + bounds[1] + bounds[3];
  }

  @Override
  protected boolean isPointInRegion(int x, int y, int w, int h, int mx, int my) {
    if (w == 16 && h == 16)
      for (Slot slot : inventorySlots.slots)
        if (slot.x == x && slot.y == y) {
          int[] bounds = inventoryHighlightBounds(slot);
          if (bounds != null) return inventoryHighlightContains(slot, mx, my);
          break;
        }
    return super.isPointInRegion(x, y, w, h, mx, my);
  }

  @Override
  protected void drawGradientRect(int x, int y, int right, int bottom, int start, int end) {
    if (highlightedSlot != null
        && x == highlightedSlot.x
        && y == highlightedSlot.y
        && right - x == 16
        && bottom - y == 16
        && start == 0x80ffffff
        && end == start) return;
    super.drawGradientRect(x, y, right, bottom, start, end);
  }

  @Override
  protected void handleMouseClick(Slot slot, int id, int button, ClickType type) {
    super.handleMouseClick(slot, id, button, type);
    ConsoleAudio.inventoryAction(type, button);
  }
}

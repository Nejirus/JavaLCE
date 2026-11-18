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
import java.util.function.*;



final class ConsoleSlider extends GuiButton {
  private final DoubleSupplier value;
  private final DoubleConsumer change;
  private final Supplier<String> label;
  private boolean dragging;
  private final boolean ps4Assets;
  private double lastDragValue = Double.NaN;

  ConsoleSlider(
      int id,
      int x,
      int y,
      int w,
      DoubleSupplier value,
      DoubleConsumer change,
      Supplier<String> label) {
    this(id,x,y,w,value,change,label,false);
  }
  ConsoleSlider(int id,int x,int y,int w,DoubleSupplier value,DoubleConsumer change,Supplier<String> label,boolean ps4Assets){
    super(id, x, y, w, 20, "");this.ps4Assets=ps4Assets;
    this.value = value;
    this.change = change;
    this.label = label;
  }

  @Override
  public void drawButton(Minecraft mc, int mx, int my) {
    if (!visible) return;
    float[] r = LceWidgets.bounds(this);
    hovered = ConsoleTheme.buttonHover(this, mx, my);
    boolean focus = MenuFocus.focused(this, hovered);
    if (dragging) {
      // Resource reloads may consume the release event while this GUI is open.
      // A released mouse must stop dragging before another expensive setting change.
      if (dev.legacyconsole.port.Mouse.isButtonDown(0)) update(mx);
      else dragging = false;
    }
    if(ps4Assets)LceWidgets.slider(r,value.getAsDouble(),focus,enabled,"ps4/ui/");else LceWidgets.slider(r,value.getAsDouble(),focus,enabled);
    displayString = label.get();
    drawCenteredString(
        FontCompat.get(),
        ConsoleTheme.fit(displayString, (int)r[2] - 8),
        xPosition + width / 2,
        (int)(r[1] + (r[3] - 8) / 2),
        enabled ? (focus ? 0xffffff00 : ConsoleTheme.TEXT) : ConsoleTheme.MUTED);
  }

  private void update(int mx) {
    float[] r = LceWidgets.bounds(this);
    double next = Math.max(0, Math.min(1, (mx - r[0] - 4) / (double) (r[2] - 8)));
    if (Double.compare(next, lastDragValue) == 0) return;
    lastDragValue = next;
    change.accept(next);
  }

  @Override
  public boolean mousePressed(Minecraft mc, int mx, int my) {
    if (!enabled || !visible || !ConsoleTheme.buttonHover(this, mx, my)) return false;
    dragging = true;
    lastDragValue = Double.NaN;
    update(mx);
    return true;
  }

  @Override
  public void mouseReleased(int x, int y) {
    dragging = false;
  }

  @Override
  public void playPressSound(SoundManager handler) {
    ConsoleAudio.play("focus");
  }
}

package dev.legacyconsole.client;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.eventbus.api.*;
import dev.legacyconsole.client.input.*;
import dev.legacyconsole.client.ui.*;
import dev.legacyconsole.port.*;

/** Console HUD placement over Forge's native, individually cancellable HUD layers. */
public final class ConsoleHud {
  private final Ps4ItemInformation information=new Ps4ItemInformation();
  @SubscribeEvent public void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event){information.tick(event);}
  @SubscribeEvent public void tooltip(net.minecraftforge.client.event.RenderTooltipEvent.Pre event){information.tooltip(event);}
  private static final Set<IIngameOverlay> BOTTOM = Set.of(
      ForgeIngameGui.HOTBAR_ELEMENT, ForgeIngameGui.ARMOR_LEVEL_ELEMENT,
      ForgeIngameGui.PLAYER_HEALTH_ELEMENT, ForgeIngameGui.FOOD_LEVEL_ELEMENT,
      ForgeIngameGui.AIR_LEVEL_ELEMENT, ForgeIngameGui.MOUNT_HEALTH_ELEMENT,
      ForgeIngameGui.JUMP_BAR_ELEMENT, ForgeIngameGui.EXPERIENCE_BAR_ELEMENT);
  private final Set<IIngameOverlay> pushed = new HashSet<>();
  private boolean original, tooltips, suppressed;

  @SubscribeEvent public void chat(RenderGameOverlayEvent.Chat event) {
    if (!ClientConfig.consoleHud) return;
    Minecraft mc = Minecraft.getInstance();
    ScaledResolution res = new ScaledResolution(mc);
    event.setPosX(12);
    event.setPosY(mc.screen instanceof net.minecraft.client.gui.screens.ChatScreen
        ? res.getScaledHeight() - 34 : LceHudRenderer.supported(mc)
        ? Math.round(LceHudRenderer.layout(res).rootY - 12)
        : res.getScaledHeight() - ClientConfig.hudLift - 85);
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void before(RenderGameOverlayEvent.Pre event) {
    if (!ClientConfig.consoleHud || event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
    Minecraft mc = Minecraft.getInstance();
    original = LceHudRenderer.supported(mc);
    tooltips = mc.options.heldItemTooltips;
    mc.options.heldItemTooltips = false;
    suppressed = true;
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void beforeLayer(RenderGameOverlayEvent.PreLayer event) {
    if (!ClientConfig.consoleHud) return;
    Minecraft mc = Minecraft.getInstance();
    IIngameOverlay layer = event.getOverlay();
    if(layer==ForgeIngameGui.POTION_ICONS_ELEMENT){event.setCanceled(true);return;}
    if (layer == ForgeIngameGui.CROSSHAIR_ELEMENT
        && (mc.screen != null || original && !mc.options.renderDebug)) {
      event.setCanceled(true);
      return;
    }
    if (!BOTTOM.contains(layer)) return;
    if (mc.screen != null || original) { event.setCanceled(true); return; }
    float factor = LceHudLayout.factor(ClientConfig.hudSize);
    float x = event.getWindow().getGuiScaledWidth() / 2f;
    float y = event.getWindow().getGuiScaledHeight();
    GlStateManager.pushMatrix();
    GlStateManager.translate(x, y - 20 - ClientConfig.hudLift, 0);
    GlStateManager.scale(factor, factor, 1);
    GlStateManager.translate(-x, -y, 0);
    pushed.add(layer);
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void afterLayer(RenderGameOverlayEvent.PostLayer event) {
    if (pushed.remove(event.getOverlay())) GlStateManager.popMatrix();
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void after(RenderGameOverlayEvent.Post event) {
    if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
    Minecraft mc = Minecraft.getInstance();
    ScaledResolution res = new ScaledResolution(mc);
    if (suppressed) {
      mc.options.heldItemTooltips = tooltips;
      suppressed = false;
      if (original && mc.screen == null && !mc.options.hideGui)
        LceHudRenderer.render(res, event.getPartialTicks());
      information.hotbar(event.getMatrixStack(),event.getPartialTicks());
      Ps4Effects.hud(event.getMatrixStack(),res.getScaledWidth(),res.getScaledHeight());
    }
    LcePaperDoll.render();
    if (!ClientConfig.consoleHud || mc.player == null || mc.screen != null
        || mc.options.hideGui || mc.options.renderDebug) return;
    if (ControllerInput.INSTANCE.state.connected)
      ConsoleTheme.footer(res.getScaledWidth(), res.getScaledHeight(), LceActionPrompts.actions(mc));
    else FontCompat.get().drawStringWithShadow("C  Crafting", 7, res.getScaledHeight() - 13, ConsoleTheme.TEXT);
    if (ClientConfig.coordinates)
      ConsoleTheme.text("X: " + mc.player.getBlockX() + ", Y: " + mc.player.getBlockY()
          + ", Z: " + mc.player.getBlockZ(), 12, LcePaperDoll.visible() ? 66 : 12, ConsoleTheme.TEXT);
  }
}

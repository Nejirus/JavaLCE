package dev.legacyconsole.client;
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

import dev.legacyconsole.client.ui.ConsoleTheme;
import java.lang.reflect.Method;
import java.util.Random;



/** Original HUD bitmaps and Hud.as state/slot layout, rendered by Java's native item renderer. */
public final class LceHudRenderer extends Gui {
  private static final LceHudRenderer DRAW = new LceHudRenderer();
  private static final ResourceLocation WIDGETS = new ResourceLocation("textures/gui/widgets.png");
  private final Random random = new Random();
  private int lastHealth = 20, flashUntil;

  /** Keep Java's extra health/hardcore/horse/spectator HUD when LCE has no matching assets. */
  public static boolean supported(Minecraft game) {
    return game.player != null && !game.player.isSpectator()
        && game.player.getMaxHealth() <= 20 && game.player.getAbsorptionAmount() <= 0
        && !game.player.hasEffect(MobEffects.WITHER)
        && !game.level.getLevelData().isHardcore()
        && !(game.player.getVehicle() instanceof LivingEntity);
  }

  public static LceHudLayout layout(ScaledResolution resolution) {
    return new LceHudLayout(resolution.getScaledWidth(), resolution.getScaledHeight(),
        Minecraft.getInstance().getWindow().getHeight(), resolution.getScaleFactor(), ClientConfig.hudLift,
        ClientConfig.hudSize);
  }

  public static void render(ScaledResolution resolution, float partial) {
    DRAW.draw(resolution, partial);
  }

  private void image(String name, float x, float y, float w, float h) {
    ConsoleTheme.spriteAt("lce/hud/" + name.toLowerCase(java.util.Locale.ROOT), x, y, w, h);
  }

  private void icon(String name, float x, float y) { image(name, x, y, 13.5f, 13.5f); }

  private void draw(ScaledResolution resolution, float partial) {
    Minecraft game = Minecraft.getInstance();
    Player player = game.player;
    LceHudLayout p = layout(resolution);
    if(!game.options.renderDebug)crosshair(game,resolution);
    float barY = p.rootY + 42;
    GlStateManager.pushMatrix();
    GlStateManager.translate(p.x, p.rootY, 0);
    GlStateManager.scale(p.factor, p.factor, 1);
    GlStateManager.translate(-p.x, -p.rootY, 0);
    GlStateManager.disableDepth();
    image("hotbar_item_back", p.x, barY, LceHudLayout.WIDTH, 33);
    image("hotbar_item_selected", p.x + player.getInventory().selected * 30 - 1.5f,
        barY - 1.5f, 36, 36);
    GlStateManager.pushMatrix();
    GlStateManager.translate(p.x, barY, 0);
    GlStateManager.scale(1.5f, 1.5f, 1);
    // Offhand is a 1.9 addition: retain its real Java widget and item, adjacent to LCE's bar.
    ItemStack offhand = player.getOffhandItem();
    int offX = player.getMainArm() == HumanoidArm.RIGHT ? -26 : 192;
    if (!offhand.isEmpty()) {
      PortGraphics.bind(WIDGETS);
      GlStateManager.color(1, 1, 1, 1);
      drawTexturedModalRect(offX - 3, -1, offX < 0 ? 24 : 53, 22, 29, 24);
    }
    GlStateManager.enableDepth();
    GlStateManager.enableRescaleNormal();
    RenderHelper.enableGUIStandardItemLighting();
    for(int slot=0;slot<9;slot++)ConsoleTheme.item(player.getInventory().items.get(slot),slot*20+3,3);
    if(!offhand.isEmpty())ConsoleTheme.item(offhand,offX,3);
    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableRescaleNormal();
    GlStateManager.disableDepth();
    GlStateManager.popMatrix();
    if (game.gameMode.canHurtPlayer()) {
      status(game, p);
      image("experience_bar_empty", p.x, p.rootY + 30, LceHudLayout.WIDTH, 7.5f);
      ConsoleTheme.croppedSprite("lce/hud/experience_bar_full", p.x, p.rootY + 30,
          LceHudLayout.WIDTH, 7.5f, 0, 0, Mth.clamp(player.experienceProgress, 0, 1), 1);
      if (player.experienceLevel > 0) {
        String label = Integer.toString(player.experienceLevel);
        // ExpBar is at +30; PlayerLevel is at -37 source px, with a +2 px text inset.
        GlStateManager.pushMatrix();
        GlStateManager.translate(p.x + LceHudLayout.WIDTH / 2, p.rootY + 12.5f, 0);
        GlStateManager.scale(1.5f, 1.5f, 1);
        float x = -FontCompat.get().getStringWidth(label) / 2f;
        float y = 0;
        FontCompat.get().drawString(label, x + 1, y, 0xff000000, false);
        FontCompat.get().drawString(label, x - 1, y, 0xff000000, false);
        FontCompat.get().drawString(label, x, y + 1, 0xff000000, false);
        FontCompat.get().drawString(label, x, y - 1, 0xff000000, false);
        FontCompat.get().drawString(label, x, y, 0xff80ff20, false);
        GlStateManager.popMatrix();
      }
    }
    GlStateManager.enableDepth();
    GlStateManager.color(1, 1, 1, 1);
    GlStateManager.popMatrix();
  }

  private void crosshair(Minecraft game,ScaledResolution resolution) {
    if(!game.options.getCameraType().isFirstPerson())return;
    int index=Math.max(0,Math.min(2,ClientConfig.hudSize));
    boolean hd=game.getWindow().getHeight()>=1080;
    float width=(hd?new float[]{44,68,102}:new float[]{30,45,68})[index]/(hd?3f:2f);
    float cx=resolution.getScaledWidth()/2f,cy=resolution.getScaledHeight()/2f;
    GlStateManager.enableBlend();
    GlStateManager.enableAlpha();
    GlStateManager.color(1,1,1,1);
    GlStateManager.tryBlendFuncSeparate(775,769,1,0);
    PortGraphics.bind(new ResourceLocation("legacyconsole","textures/lce/hud/hud_crosshair.png"));
    GlStateManager.pushMatrix();
    GlStateManager.translate(cx-width/2,cy-width/2,0);
    GlStateManager.scale(width/15,width/15,1);
    drawModalRectWithCustomSizedTexture(0,0,0,0,15,15,15,15);
    GlStateManager.popMatrix();
    // Java 1.11's combat cooldown has no LCE asset; retain its native indicator next to the crosshair.
    if(game.options.attackIndicator==AttackIndicatorStatus.CROSSHAIR) {
      float cooldown=game.player.getAttackStrengthScale(0);
      boolean ready=game.crosshairPickEntity instanceof LivingEntity && cooldown>=1
          && game.player.getCurrentItemAttackStrengthDelay()>5 && ((LivingEntity)game.crosshairPickEntity).isAlive();
      float factor=LceHudLayout.factor(index);
      PortGraphics.bind(new ResourceLocation("textures/gui/icons.png"));
      GlStateManager.pushMatrix();
      GlStateManager.translate(cx-8*factor,cy+width/2,0);
      GlStateManager.scale(factor,factor,1);
      if(ready)drawTexturedModalRect(0,0,68,94,16,16);
      else if(cooldown<1) {
        drawTexturedModalRect(0,0,36,94,16,4);
        drawTexturedModalRect(0,0,52,94,(int)(cooldown*17),4);
      }
      GlStateManager.popMatrix();
    }
    GlStateManager.tryBlendFuncSeparate(770,771,1,0);
  }

  private void status(Minecraft game, LceHudLayout p) {
    Player player = game.player;
    int ticks = game.gui.getGuiTicks(), health = Mth.ceil(player.getHealth());
    if (health != lastHealth && player.invulnerableTime > 0) flashUntil = ticks + 20;
    lastHealth = health;
    boolean flash = ticks < flashUntil && (flashUntil - ticks) / 3 % 2 == 1;
    boolean poison = player.hasEffect(MobEffects.POISON);
    boolean hunger = player.hasEffect(MobEffects.HUNGER);
    int food = player.getFoodData().getFoodLevel(), armor = player.getArmorValue();
    random.setSeed(ticks * 312871L);
    int fullAir = Mth.ceil((player.getAirSupply() - 2) * 10.0 / 300.0);
    int allAir = Mth.ceil(player.getAirSupply() * 10.0 / 300.0);
    for (int i = 0; i < 10; i++) {
      float x = p.x + i * 12, right = p.x + LceHudLayout.WIDTH - 13.5f - i * 12;
      float y = p.rootY + 15;
      if (health <= 4) y += random.nextInt(2) * 1.5f;
      if (player.hasEffect(MobEffects.REGENERATION) && i == ticks % 25) y -= 3;
      icon("Health_Background" + (flash ? "_Flash" : ""), x, y);
      String part = health >= (i + 1) * 2 ? "Full" : health == i * 2 + 1 ? "Half" : null;
      if (part != null) icon("Health_" + part + (poison ? "_Poison" : "")
          + (flash ? "_Flash" : ""), x, y);
      float foodY = p.rootY + 15;
      if (player.getFoodData().getSaturationLevel() <= 0 && ticks % (food * 3 + 1) == 0)
        foodY += (random.nextInt(3) - 1) * 1.5f;
      icon("HUD_Food_Background" + (hunger ? "_Poison" : ""), right, foodY);
      part = food >= (i + 1) * 2 ? "Full" : food == i * 2 + 1 ? "Half" : null;
      if (part != null) icon("HUD_Food_" + part + (hunger ? "_Poison" : ""), right, foodY);
      if (armor > 0) icon("HUD_Armour_" + (armor >= (i + 1) * 2 ? "Full"
          : armor == i * 2 + 1 ? "Half" : "Empty"), x, p.rootY);
      if (player.isEyeInFluid(net.minecraft.tags.FluidTags.WATER) && i < allAir)
        icon(i < fullAir ? "HUD_Air_Bubble" : "HUD_Air_Pop", right, p.rootY);
    }
  }
}

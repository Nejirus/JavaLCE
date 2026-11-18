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





/** HUD720/HUD1080 ScaleSmall/ScaleNormal/ScaleLarge display lists and 5% safe zone. */
public final class LceHudLayout {
  public static final float PIXEL = 1.5f, WIDTH = 182 * PIXEL;
  public final float x, rootY, hotbarY, factor, width;

  public LceHudLayout(int width, int height, int displayHeight, int scale, int extraLift) {
    this(width, height, displayHeight, scale, extraLift, 1);
  }

  public LceHudLayout(int width, int height, int displayHeight, int scale, int extraLift,
      int size) {
    int index = Math.max(0, Math.min(2, size));
    // A manually enlarged Java GUI can be narrower than the source HUD preset.
    // Keep the original preset when it fits, and fit its geometry inside LCE's 5% safe zone.
    factor = Math.min(factor(index), width * .9f / WIDTH);
    float sourceHeight = displayHeight >= 1080 ? 1080 : 720;
    float sourceY = (sourceHeight == 1080
        ? new float[] {851, 775, 698} : new float[] {567, 516, 476.3f})[index];
    rootY = Math.min((sourceY / sourceHeight - .05f) * height,
        height * .95f - 73.5f * factor) - extraLift;
    this.width = WIDTH * factor;
    x = (width - this.width) / 2f;
    hotbarY = rootY + 42 * factor;
  }

  public float slotX(int slot) { return x + (slot * 30 + 4.5f) * factor; }
  public float slotY() { return hotbarY + 4.5f * factor; }

  public static float factor(int size) {return new float[]{2f/3,1,4f/3}[Math.max(0,Math.min(2,size))];}
}

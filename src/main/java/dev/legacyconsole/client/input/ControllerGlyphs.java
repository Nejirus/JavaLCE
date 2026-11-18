package dev.legacyconsole.client.input;
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
import java.util.Locale;




/** LCE button-position semantics: A/B/X/Y or Cross/Circle/Square/Triangle. */
public final class ControllerGlyphs {
  public enum Style {
    PLAYSTATION_4("PlayStation 4", "ps4"),
    PLAYSTATION_3("PlayStation 3", "ps3"),
    XBOX_360("Xbox 360", "xbox360"),
    XBOX_ONE("Xbox One", "xboxone");
    public final String label, folder;
    Style(String label, String folder) { this.label = label; this.folder = folder; }
  }

  private ControllerGlyphs() {}

  public static Style detect(String name) {
    String n = name == null ? "" : name.toLowerCase(Locale.ROOT);
    if (n.contains("360") || n.contains("xinput")) return Style.XBOX_360;
    if (n.contains("xbox") || n.contains("x-box") || n.contains("x box")) return Style.XBOX_ONE;
    if (n.contains("playstation 3") || n.contains("playstation(r)3") || n.contains("ps3")
        || n.contains("dualshock 3") || n.contains("sixaxis")) return Style.PLAYSTATION_3;
    return Style.PLAYSTATION_4;
  }

  public static Style current() {
    int selected = ClientConfig.glyphStyle;
    return selected > 0 && selected <= Style.values().length
        ? Style.values()[selected - 1] : detect(ControllerInput.INSTANCE.state.name);
  }

  public static String optionLabel() {
    return ClientConfig.glyphStyle == 0 ? "Automatic (" + current().label + ")" : current().label;
  }
}

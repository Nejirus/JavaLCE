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

import java.util.*;



/** PS4 crafting type order; map operations use the native cartography table. */
final class ConsoleCraftingTypes {
  static final String[] NAMES = {"Crafting", "Banners", "Fireworks", "Dyeing"};

  static void add(List<GuiButton> buttons, int x, int y, int selected, int panelBorder) {
    ItemStack dyedArmor=new ItemStack(Items.LEATHER_CHESTPLATE);
    ((DyeableLeatherItem)dyedArmor.getItem()).setColor(dyedArmor,DyeColor.GREEN.getFireworkColor());
    ItemStack[] icons = {
      new ItemStack(Blocks.CRAFTING_TABLE),
      new ItemStack(Items.WHITE_BANNER),
      new ItemStack(Items.FIREWORK_ROCKET),
      dyedArmor
    };
    for (int i = 0; i < NAMES.length; i++) {
      ConsoleItemButton b =
          new ConsoleItemButton(900 + i, x - 29, y + 8 + i * 30, 31, icons[i], true);
      b.width = 29 + panelBorder;
      b.tabEdge = i == 0 ? "up" : i == NAMES.length - 1 ? "down" : "middle";
      b.selected = i == selected;
      buttons.add(b);
    }
  }

  static void draw(List<GuiButton> buttons, boolean selected) {
    for (GuiButton button : buttons)
      if (button instanceof ConsoleItemButton) {
        ConsoleItemButton tab = (ConsoleItemButton) button;
        if (tab.verticalTab && tab.selected == selected && tab.visible) {
          float scale=tab.height/125f,offset=selected?0:14*scale;
          ConsoleTheme.spriteAt("lce/ps4/crafting/largeverttab"+(selected?"on":"off"),
              tab.xPosition+offset,tab.yPosition,144*scale,125*scale);
          ConsoleTheme.centeredItem(tab.stack,tab.xPosition+Math.round(offset),tab.yPosition,
              Math.round(144*scale),tab.height,Math.round(72*scale));
        }
      }
  }
}

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

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;



/** The game's complete preset data, displayed with the reference console widgets. */
public final class ConsolePresetScreen extends ConsolePanelScreen {
  private final List<String> names = new ArrayList<>(), values = new ArrayList<>();
  private final Consumer<String> accept;
  private int page;

  public ConsolePresetScreen(Screen parent, boolean flat, Consumer<String> accept) {
    super(parent, flat ? "Superflat Presets" : "World Presets");
    this.accept = accept;
    panelW = 300;
    panelH = 202;
    Class<?> type=PresetFlatWorldScreen.class;
    try {
      Class.forName(type.getName(),true,type.getClassLoader());
      for(Field field:type.getDeclaredFields())if(Modifier.isStatic(field.getModifiers())&&List.class.isAssignableFrom(field.getType())){
        field.setAccessible(true);
        for(Object entry:(List<?>)field.get(null)){
          String name=null;java.util.function.Function<net.minecraft.core.Registry<Biome>,FlatLevelGeneratorSettings> generator=null;
          for(Field f:entry.getClass().getDeclaredFields()){f.setAccessible(true);Object value=f.get(entry);if(value instanceof Component component)name=component.getString();else if(value instanceof java.util.function.Function)generator=(java.util.function.Function<net.minecraft.core.Registry<Biome>,FlatLevelGeneratorSettings>)value;}
          if(name!=null&&generator!=null){FlatLevelGeneratorSettings settings=generator.apply(net.minecraft.data.BuiltinRegistries.BIOME);java.util.List<String> layers=new java.util.ArrayList<>();for(FlatLayerInfo l:settings.getLayersInfo())layers.add((l.getHeight()==1?"":l.getHeight()+"*")+net.minecraft.core.Registry.BLOCK.getKey(l.getBlockState().getBlock()));names.add(name);values.add(String.join(",",layers)+";"+net.minecraft.data.BuiltinRegistries.BIOME.getKey(settings.getBiome()));}
        }
        break;
      }
    }catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot read native presets",e);}
  }

  @Override
  public void initGui() {
    super.initGui();
    rebuild();
  }

  private void rebuild() {
    buttonList.clear();
    for (int i = 0; i < 5 && page * 5 + i < names.size(); i++)
      addRow(i, i, names.get(page * 5 + i));
    buttonList.add(new ConsoleButton(10, panelX + 18, panelY + 173, 40, 20, "<"));
    buttonList.add(new ConsoleButton(11, panelX + 62, panelY + 173, 40, 20, ">"));
    buttonList.add(
        new ConsoleButton(12, panelX + 106, panelY + 173, panelW - 124, 20, "Enter Preset Code"));
  }

  @Override
  protected void actionPerformed(GuiButton b) {
    if (b.id < 5) {
      accept.accept(values.get(page * 5 + b.id));
      mc.setScreen(parent);
    } else if (b.id == 10 || b.id == 11) {
      page = Math.floorMod(page + (b.id == 10 ? -1 : 1), Math.max(1, (names.size() + 4) / 5));
      rebuild();
    } else
      mc.setScreen(
          new ConsoleKeyboard(parent, "Preset Code", "", value -> accept.accept(value)));
  }
}

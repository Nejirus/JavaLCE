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





public final class ConsoleWorldOptions extends ConsolePanelScreen {
  private final ConsolePlayScreen play;
  private LevelSummary level;

  public ConsoleWorldOptions(ConsolePlayScreen play, LevelSummary level) {
    super(play, "Save Options");
    this.play = play;
    this.level = level;
    panelH = 188;
  }

  @Override
  public void initGui() {
    super.initGui();
    addRow(1, 0, "Rename World");
    addRow(2, 1, "Recreate World");
    addRow(3, 2, "Delete World");
    addRow(4, 3, "Back");
  }

  @Override protected void actionPerformed(GuiButton button){
    if(button.id==1)mc.setScreen(new ConsoleKeyboard(this,"Rename World",level.getLevelName(),name->{if(name.trim().isEmpty())return;try(var access=mc.getLevelSource().createAccess(level.getLevelId())){access.renameLevel(name.trim());level=access.getSummary();}catch(java.io.IOException e){throw new IllegalStateException("Cannot rename world",e);}}));
    else if(button.id==2)try{WorldInfo info=new WorldInfo(mc.getLevelSource().getBaseDir().resolve(level.getLevelId()).toFile());ConsoleCreateWorld create=new ConsoleCreateWorld(play);create.recreateFromExistingWorld(info);mc.setScreen(create);}catch(java.io.IOException e){throw new IllegalStateException("Cannot read world generator",e);}
    else if(button.id==3)mc.setScreen(new ConsoleConfirmScreen(this,"Delete World","Delete '"+level.getLevelName()+"'? This cannot be undone.",()->{try(var access=mc.getLevelSource().createAccess(level.getLevelId())){access.deleteLevel();}catch(java.io.IOException e){throw new IllegalStateException("Cannot delete world",e);}mc.setScreen(play);}));
    else back();
  }
}

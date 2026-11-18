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

import dev.legacyconsole.client.input.*;



/** LCE LoadMenu: launch settings on the left and texture-pack preview on the right. */
public final class ConsoleLoadWorld extends ConsolePanelScreen implements ControllerScreen {
  private final ConsolePlayScreen play;
  private final LevelSummary level;
  private WorldInfo info;
  private boolean changed;
  private ConsoleLaunchLayout layout;
  private ConsolePacks.Selection packs;
  private ConsolePackSelector packSelector;
  private ResourceLocation icon;
  private dev.legacyconsole.world.WorldPolicy origin;
  private java.io.File saveFolder;

  public ConsoleLoadWorld(ConsolePlayScreen play, LevelSummary level) {
    super(play, level.getLevelName());
    this.play = play;
    this.level = level;
    panelW = 245;
    panelH = 233;
  }

  @Override
  public void initGui() {
    super.initGui();
    if(info==null)try{info=new WorldInfo(mc.getLevelSource().getBaseDir().resolve(level.getLevelId()).toFile());}catch(java.io.IOException e){throw new IllegalStateException("Cannot read world settings",e);}
    saveFolder = mc.getLevelSource().getBaseDir().resolve(level.getLevelId()).toFile();
    if (origin == null && info != null)
      origin = dev.legacyconsole.world.WorldPolicy.read(saveFolder, info);
    if(info!=null && origin!=null) {boolean wasLocked=info.isDifficultyLocked();origin.enforce(info);if(survivalLocked()||wasLocked!=info.isDifficultyLocked())changed=true;}
    layout=new ConsoleLaunchLayout(width,height,false);
    panelX=Math.round(layout.x);panelY=Math.round(layout.y);
    panelW=Math.round(layout.w);panelH=Math.round(layout.h);
    if(packs==null)packs=new ConsolePacks.Selection();
    ConsoleButton mode=layout.place(new ConsoleButton(2,0,0,220,20,"Game Mode: "+modeName()),"mode");
    mode.enabled=info!=null && !info.isHardcoreModeEnabled() && !survivalLocked();
    buttonList.add(mode);
    ConsoleSlider difficulty=layout.place(new ConsoleSlider(3,0,0,220,
        ()->info==null?0:info.getDifficulty().getId()/3.0,
        value->{if(info!=null && !info.isHardcoreModeEnabled() && !info.isDifficultyLocked()) {
          info.setDifficulty(Difficulty.byId((int)Math.round(value*3)));changed=true;
        }},
        ()->"Difficulty: "+(info==null?"Unknown":info.isHardcoreModeEnabled()?I18n.get("gameMode.hardcore"):I18n.get(info.getDifficulty().getKey()))),"difficulty");
    difficulty.enabled=info!=null && !info.isHardcoreModeEnabled() && !info.isDifficultyLocked();
    buttonList.add(difficulty);
    packSelector=layout.place(new ConsolePackSelector(5,packs),"packs");buttonList.add(packSelector);
    ConsoleButton online=layout.place(new ConsoleButton(6,0,0,220,12,"Online Game: Off"),"online");
    online.enabled=false;online.allowCheckbox=true;buttonList.add(online);
    buttonList.add(layout.place(new ConsoleButton(4,0,0,220,20,"More World Options..."),"more"));
    buttonList.add(layout.place(new ConsoleButton(0,0,0,220,20,"Load Game"),"launch"));
  }

  private String modeName() {
    return info == null
        ? level.getGameMode().getName()
        : info.isHardcoreModeEnabled()
            ? "Survival"
            : I18n.get(
                "gameMode." + info.getGameType().getName());
  }

  private boolean survivalLocked() {
    return origin != null && origin.locked();
  }

  private void add(int id, int y, int w, String label) {
    buttonList.add(new ConsoleButton(id, panelX + 14, panelY + y, w, 20, label));
  }

  @Override
  protected void actionPerformed(GuiButton b) {
    if (b.id == 0)
      play.loadWorld(
          level,
          () -> {
            if (origin != null) dev.legacyconsole.world.WorldPolicy.preserve(saveFolder, origin);
            if (survivalLocked() && info != null) {
              origin.enforce(info);
              changed = true;
            }
            if (changed && info != null) {
              info.save();
            }
            if (info != null)
              ConsoleWorldLaunch.prepare(
                  level.getLevelId(), info.getGameType(), info.getDifficulty());
          });
    else if (b.id == 4) mc.setScreen(new ConsoleWorldSettings(this));
    else if (b.id == 5) packs.activate(this);
    else if (b.id == 2) cycleMode();
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    ConsoleTheme.background(width, height);
    packs.focus(packSelector.focused(mx,my));
    layout.render(packs);
    float x=layout.x,y=layout.y;
    LceWidgets.panel("square_recess",x+38f/3,y+9,31.5f,31.5f);
    if(icon==null)try {
      java.io.File file=level.getIcon();
      if(file.isFile())icon=mc.getTextureManager().register("legacy_launch_world",
          new DynamicTexture(com.mojang.blaze3d.platform.NativeImage.read(new java.io.FileInputStream(file))));
    } catch(java.io.IOException ignored) {}
    if(icon!=null) {
      PortGraphics.bind(icon);
      GlStateManager.color(1,1,1,1);
      GlStateManager.pushMatrix();
      GlStateManager.translate(x+14,y+31f/3,0);
      GlStateManager.scale(88f/3,88f/3,1);
      drawModalRectWithCustomSizedTexture(0,0,0,0,1,1,1,1);
      GlStateManager.popMatrix();
    } else ConsoleTheme.spriteAt("lce/widgets/default_pack_icon",x+14,y+31f/3,88f/3,88f/3);
    LceWidgets.label(ConsoleTheme.fit(level.getLevelName(),180),x+139f/3,y+26f/3,ConsoleTheme.DARK);
    LceWidgets.label("Created in "+(origin==null?level.getGameMode():origin.createdMode()).getName(),
        x+139f/3,y+77f/3,ConsoleTheme.DARK);
    LceWidgets.label(ConsoleTheme.fit("Seed: "+(info==null?"Unknown":info.getSeed()),220),
        x+37f/3,y+130f/3,ConsoleTheme.DARK);
    for(GuiButton button:buttonList)if(button.id==2)button.displayString="Game Mode: "+modeName();
    for (GuiButton b : buttonList) b.drawButton(mc, mx, my);
    ConsoleTheme.footer(width, height, "cross", "Select", "circle", "Back");
  }

  private void cycleMode() {
    if(info!=null && !info.isHardcoreModeEnabled() && !survivalLocked()) {
      info.setGameMode(info.getGameType()==GameType.CREATIVE?GameType.SURVIVAL:GameType.CREATIVE);
      changed=true;
    }
  }

  boolean hostLocked() {return info==null || survivalLocked() || info.isHardcoreModeEnabled();}
  boolean hostPrivileges() {return info!=null && info.areCommandsAllowed();}
  boolean fireSpreads() {return info!=null && info.getGameRules().getBoolean("doFireTick");}
  void toggleHostPrivileges() {
    if(hostLocked())return;info.setAllowCommands(!info.areCommandsAllowed());changed=true;
  }
  void toggleFireSpreads() {
    if(hostLocked())return;info.getGameRules().setOrCreateGameRule("doFireTick",String.valueOf(!fireSpreads()));changed=true;
  }

  @Override
  public boolean controllerDirection(int d) {
    if(MenuFocus.id(this)==5 && (d==PadState.LEFT || d==PadState.RIGHT)) {
      packs.move(d==PadState.LEFT?-1:1,packs.visibleSlots(220));return true;
    }
    if(MenuFocus.id(this)==2 && (d==PadState.LEFT || d==PadState.RIGHT)) {
      cycleMode();return true;
    }
    if(MenuFocus.id(this)==3 && (d==PadState.LEFT || d==PadState.RIGHT)) {
      if(info!=null && !info.isHardcoreModeEnabled() && !info.isDifficultyLocked()) {
        info.setDifficulty(Difficulty.byId(Math.max(0,Math.min(3,
            info.getDifficulty().getId()+(d==PadState.LEFT?-1:1)))));
        changed=true;
      }
      return true;
    }
    return false;
  }

  @Override public boolean controllerButton(int button) {
    // The source difficulty control responds to Left/Right, not confirm-to-cycle.
    return button==PadState.CROSS && MenuFocus.id(this)==3;
  }

  private final class Options extends ConsolePanelScreen {
    Options() {
      super(ConsoleLoadWorld.this, "World Options");
      panelH = 152;
    }

    @Override
    public void initGui() {
      super.initGui();
      addToggleRow(
          0, 0, "Host Privileges: " + (info != null && info.areCommandsAllowed() ? "On" : "Off"));
      if (survivalLocked()) {
        buttonList.get(0).enabled = false;
        buttonList.get(0).displayString = "Host Privileges: Off (Locked)";
      }
      addToggleRow(
          1,
          1,
          "Fire Spreads: "
              + (info != null && info.getGameRules().getBoolean("doFireTick")
                  ? "On"
                  : "Off"));
      if(survivalLocked())buttonList.get(buttonList.size()-1).enabled=false;
      addRow(2, 3, "Back");
    }

    @Override
    protected void actionPerformed(GuiButton b) {
      if (b.id == 2) {
        back();
        return;
      }
      if (info == null || survivalLocked()) return;
      if (b.id == 0) {
        if (survivalLocked()) return;
        info.setAllowCommands(!info.areCommandsAllowed());
      } else
        info.getGameRules()
            .setOrCreateGameRule(
                "doFireTick",
                String.valueOf(!info.getGameRules().getBoolean("doFireTick")));
      changed = true;
      initGui();
    }
  }
}

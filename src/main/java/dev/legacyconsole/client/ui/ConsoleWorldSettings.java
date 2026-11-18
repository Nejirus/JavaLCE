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

import dev.legacyconsole.client.LceActionPrompts;
import dev.legacyconsole.client.input.*;



/** The original HD LaunchMoreOptionsMenu tabs, seed input, checkboxes, and description panel. */
public final class ConsoleWorldSettings extends ConsolePanelScreen implements ControllerScreen {
  private final ConsoleCreateWorld create;
  private final ConsoleLoadWorld load;
  private int section;
  private final int[] focusIds={101,11};
  private float left,top;

  public ConsoleWorldSettings(ConsoleCreateWorld create) {
    super(create,"More World Options");this.create=create;this.load=null;
  }

  public ConsoleWorldSettings(ConsoleLoadWorld load) {
    super(load,"World Options");this.load=load;this.create=null;section=1;
  }

  @Override public void initGui() {
    super.initGui();
    left=(width-640)/2f+111;top=(height-360)/2f+113;
    panelX=Math.round(left);panelY=Math.round(top+26);panelW=249;panelH=169;
    rebuild();
  }

  private void add(int id,float rx,float ry,float w,float h,String text,boolean enabled) {
    GuiButton b=id==101?new ConsoleTextInput(id,0,0,220,18,text):new ConsoleButton(id,0,0,220,20,text);
    if(b instanceof ConsoleButton)((ConsoleButton)b).allowCheckbox=id==4 || id==7 || id==10 || id==11 || id==6 || id==13;
    b.enabled=enabled;buttonList.add(LceWidgets.place(b,left+rx/3,top+ry/3,w,h));
  }

  private String toggle(int id,String caption) {
    LceWidgets.Toggle state=LceWidgets.toggle(create.option(id).displayString);
    return caption+": "+(state!=null && state.checked?"On":"Off");
  }

  private void rebuild() {
    buttonList.clear();
    if(load!=null) {
      add(11,39,102,220,12,"Fire Spreads: "+(load.fireSpreads()?"On":"Off"),!load.hostLocked());
      add(6,39,147,220,12,"Host Privileges: "+(load.hostPrivileges()?"On":"Off")+(load.hostLocked()?" (Locked)":""),!load.hostLocked());
      return;
    }
    if(section==0 && create.legacyWorld()) {
      add(101,41,132,218,18,create.seedText(),true);
      add(13,39,192,220,12,"Find Balanced Seed: "+(create.findBalancedSeed()?"On":"Off"),create.balancedSeedAvailable());
      add(5,40,240,220,20,create.terrainLabel(),true);
      ConsoleSlider size=new ConsoleSlider(12,0,0,220,()->create.legacyWorldSize().ordinal()/3.0,
        value->create.legacyWorldSize((int)Math.round(value*3)),()->"World Size: "+create.legacyWorldSize().label);
      buttonList.add(LceWidgets.place(size,left+40f/3,top+104,220,20));
      ConsoleSlider scale=new ConsoleSlider(14,0,0,220,()->create.legacyBiomeScale().ordinal()/2.0,
        value->create.legacyBiomeScale((int)Math.round(value*2)),()->"Biome Scale: "+create.legacyBiomeScale().label);
      buttonList.add(LceWidgets.place(scale,left+40f/3,top+128,220,20));
      add(4,39,453,220,12,toggle(4,"Generate Structures"),create.option(4).enabled);
      add(7,39,498,220,12,toggle(7,"Bonus Chest"),create.option(7).enabled);
    } else if(section==0) {
      add(101,41,152,218,18,create.seedText(),true);
      String type=create.option(5).displayString;
      int colon=type.indexOf(':');if(colon>=0)type=type.substring(colon+1).trim();
      add(create.flatWorld()?8:5,40,302,create.legacyWorld()?104:220,20,create.flatWorld()?"Customize Superflat":create.terrainLabel(),true);
      if(create.legacyWorld()) {
        ConsoleSlider size=new ConsoleSlider(12,0,0,220,()->create.legacyWorldSize().ordinal()/3.0,
          value->create.legacyWorldSize((int)Math.round(value*3)),()->create.legacyWorldSize().label);
        buttonList.add(LceWidgets.place(size,left+388f/3,top+302f/3,104,20));
      }
      add(4,39,372,220,12,toggle(4,"Generate Structures"),create.option(4).enabled);
      add(7,39,418,220,12,toggle(7,"Bonus Chest"),create.option(7).enabled);
      add(10,39,464,220,12,"Superflat World: "+(create.flatWorld()?"On":"Off"),true);
      if(!create.flatWorld() && create.option(8).visible)
        add(8,39,510,220,20,create.option(8).displayString,create.option(8).enabled);
    } else {
      // Collapse absent online/PVP/trust controls, retaining the source 45px checkbox pitch.
      add(11,39,102,220,12,"Fire Spreads: "+(create.fireSpreads?"On":"Off"),true);
      add(6,39,147,220,12,toggle(6,"Host Privileges"),!create.hardcore() && create.option(6).enabled);
    }
  }

  @Override protected void actionPerformed(GuiButton button) {
    focusIds[section]=button.id;
    if(load!=null) {
      if(button.id==6)load.toggleHostPrivileges();else if(button.id==11)load.toggleFireSpreads();
      rebuild();ControllerMenus.focus(this,button.id);return;
    }
    if(button.id==12||button.id==14)return;
    if(button.id==13){create.toggleBalancedSeed();rebuild();ControllerMenus.focus(this,13);return;}
    if(button.id==101) {create.editSeed(this);return;}
    if(button.id==10)create.toggleFlat();
    else if(button.id==11)create.fireSpreads=!create.fireSpreads;
    else if(button.id==5)create.cycleTerrainType();
    else if(button.id==8)create.customize(this);
    else create.nativeAction(button.id);
    if(mc.screen==this) {rebuild();ControllerMenus.focus(this,button.id);}
  }

  @Override public void drawScreen(int mx,int my,float partial) {
    ConsoleTheme.background(width,height);
    if(load!=null) {
      LceWidgets.panel("pointer_panel",left+710f/3,top+30,197.667f,139.33f);
      LceWidgets.panel("panel16",left,top+26,248.667f,169.333f);
      ConsoleTheme.centered("World Options",Math.round(left+124.333f),Math.round(top+12),ConsoleTheme.TEXT);
      for(GuiButton button:buttonList)button.drawButton(mc,mx,my);
      ConsoleTheme.footer(width,height,"cross","Select","circle","Back");return;
    }
    // Original source depth order: inactive tabs, description, body, then selected tab.
    ConsoleTheme.spriteAt("lce/widgets/hd/world_tab_off",left,top+11f/3,373f/3,83f/3);
    ConsoleTheme.spriteAt("lce/widgets/hd/world_tab_off",left+373f/3,top+11f/3,373f/3,83f/3);
    LceWidgets.panel("pointer_panel",left+710f/3,top+30,197.667f,139.33f);
    LceWidgets.panel("panel16",left,top+26,248.667f,169.333f);
    ConsoleTheme.spriteAt("lce/widgets/hd/"+(section==0?"world_tab_world":"world_tab_game"),
        left+(section==0?0:373f/3),top,373f/3,30);
    tabLabel("World Options",left,section==0);
    tabLabel("Game Options",left+373f/3,section==1);
    if(section==0 && create.legacyWorld()) {
      LceWidgets.label("Seed",left+38f/3,top+31,ConsoleTheme.DARK);
    } else if(section==0) {
      LceWidgets.label("Seed",left+38f/3,top+112f/3,ConsoleTheme.DARK);
      LceWidgets.label("Leave blank for a random seed",left+39f/3,top+209f/3,ConsoleTheme.DARK);
      if(create.legacyWorld())LceWidgets.label("World Size",left+388f/3,top+262f/3,ConsoleTheme.DARK);
      LceWidgets.label(create.flatWorld()?"Superflat Options":"World Type",left+38f/3,top+262f/3,ConsoleTheme.DARK);
    }
    int id=MenuFocus.id(this);
    for(GuiButton b:buttonList)if(!ControllerCursor.active() && ConsoleTheme.buttonHover(b,mx,my))id=b.id;
    String key=id==4?"STRUCTURES":id==7?"BONUS_CHEST":id==10?"SUPERFLAT":id==6?"HOST_PRIVILEGES":id==11?"FIRE_SPREADS":null;
    String description=id==13?"Find a balanced selection of biomes when creating the world. Leave the seed blank to use this option.":id==14?"Choose the scale of the biomes in this Legacy world.":id==12?"The Legacy Overworld is "+create.legacyWorldSize().blocks+" x "+create.legacyWorldSize().blocks+" blocks. Nether and End use Minecraft 1.17.1 generation.":key==null?"":LceActionPrompts.source("IDS_GAMEOPTION_"+key);
    if(description!=null) {
      float y=top+106f/3;
      for(String line:fontRendererObj.listFormattedStringToWidth(description,176)) {
        if(y+9>top+162)break;
        LceWidgets.label(line,left+755f/3,y,ConsoleTheme.TEXT);y+=9;
      }
    }
    for(GuiButton b:buttonList)b.drawButton(mc,mx,my);
    ConsoleTheme.footer(width,height,"cross","Select","circle","Back","left_bumper","","right_bumper",
        section==0?"Game Options":"World Options");
  }

  private void tabLabel(String text,float x,boolean selected) {
    // LaunchMoreOptionsMenuExtended sets both tab labels to ALIGN_CENTER.
    LcePromptFont font=LcePromptFont.INSTANCE;float[] ink=font.ink(text);
    ConsoleTheme.promptLabel(text,x+(373f/3-ink[0]-ink[1])/2,
        top+(selected?26:34)/3f-1,ConsoleTheme.DARK,false);
  }

  @Override protected void mouseClicked(int x,int y,int button) throws java.io.IOException {
    if(load==null && button==0 && y>=top && y<top+26 && x>=left && x<left+249) {
      focusIds[section]=MenuFocus.id(this);section=x<left+373f/3?0:1;rebuild();ControllerMenus.reset();return;
    }
    super.mouseClicked(x,y,button);
  }

  @Override public boolean controllerDirection(int direction) {
    if(create!=null && section==0 && create.legacyWorld() && (MenuFocus.id(this)==12||MenuFocus.id(this)==14) &&
        (direction==PadState.LEFT || direction==PadState.RIGHT)) {
      int delta=direction==PadState.LEFT?-1:1;
      if(MenuFocus.id(this)==14){int old=create.legacyBiomeScale().ordinal();create.legacyBiomeScale(old+delta);if(old!=create.legacyBiomeScale().ordinal())dev.legacyconsole.client.ConsoleAudio.play("focus");}
      else {int old=create.legacyWorldSize().ordinal();create.legacyWorldSize(old+delta);if(old!=create.legacyWorldSize().ordinal())dev.legacyconsole.client.ConsoleAudio.play("focus");}
      return true;
    }
    return false;
  }

  @Override public void controllerOpened() {
    int id=focusIds[section];
    for(GuiButton button:buttonList)if(button.id==id && button.enabled) {ControllerMenus.focus(this,id);return;}
  }

  @Override public boolean controllerButton(int button) {
    if(button==PadState.CROSS && (MenuFocus.id(this)==12||MenuFocus.id(this)==14))return true;
    if(button==PadState.L1 || button==PadState.R1) {
      if(load!=null)return true;
      focusIds[section]=MenuFocus.id(this);section=1-section;rebuild();ControllerMenus.reset();return true;
    }
    if(button==PadState.SQUARE && section==0 && MenuFocus.id(this)==101) {
      create.editSeed(this);return true;
    }
    return false;
  }
}

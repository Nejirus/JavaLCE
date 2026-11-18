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
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;



/**
 * LCE CreateWorldMenu's name/seed, mode, difficulty and texture-pack columns over native world
 * creation.
 */
public final class ConsoleCreateWorld extends GuiCreateWorld implements ControllerScreen {
  public static Difficulty selectedDifficulty=Difficulty.NORMAL;
  public boolean fireSpreads(){return fireSpreads;}
  private final Map<Integer, GuiButton> models = new HashMap<>();
  private static final Method SAVE_NAME =
      ReflectionHelper.findMethod(GuiCreateWorld.class, "calcSaveDirName", "func_146314_g");
  private ConsolePacks.Selection packs;
  private ConsolePackSelector packSelector;
  private int edit = 100;
  private final Map<Integer,String> terrainSettings=new HashMap<>();
  private int nonFlatIndex;
  boolean fireSpreads=true;
  private boolean findBalancedSeed=true;
  private ConsoleLaunchLayout layout;

  public ConsoleCreateWorld(Screen parent) {
    super(parent);
  }

  private GuiTextField name() {
    return ReflectionHelper.getPrivateValue(
        GuiCreateWorld.class, this, "worldNameField", "field_146333_g");
  }

  private GuiTextField seed() {
    return ReflectionHelper.getPrivateValue(
        GuiCreateWorld.class, this, "worldSeedField", "field_146335_h");
  }

  private String mode() {
    return ReflectionHelper.getPrivateValue(
        GuiCreateWorld.class, this, "gameMode", "field_146342_r");
  }

  public GuiButton option(int id) {
    return models.get(id);
  }

  public void nativeAction(int id) {
    try {
      super.actionPerformed(models.get(id));
    } catch (IOException e) {
      throw new IllegalStateException("World option failed", e);
    }
  }

  @Override
  public void initGui() {
    super.initGui();
    models.clear();
    for (GuiButton b : buttonList) models.put(b.id, b);
    boolean more =
        ReflectionHelper.getPrivateValue(
            GuiCreateWorld.class, this, "inMoreWorldOptionsDisplay", "field_146344_y");
    if (!more) nativeAction(3);
    layout = new ConsoleLaunchLayout(width, height, true);
    if(packs==null)packs=new ConsolePacks.Selection();
    rebuild();
  }

  private void rebuild() {
    buttonList.clear();
    buttonList.add(layout.place(new ConsoleTextInput(100,0,0,220,18,name().getText()),"name"));
    buttonList.add(layout.place(new ConsoleButton(2,0,0,220,20,models.get(2).displayString),"mode"));
    buttonList.add(layout.place(new ConsoleSlider(102,0,0,220,
        ()->difficultyChoice()/4.0,
        value->difficultyChoice((int)Math.round(value*4)),
        ()->"Difficulty: "+(hardcore()?I18n.get("gameMode.hardcore"):
            I18n.get(selectedDifficulty.getKey()))),"difficulty"));
    packSelector=layout.place(new ConsolePackSelector(103,packs),"packs");
    buttonList.add(packSelector);
    ConsoleButton online=layout.place(new ConsoleButton(104,0,0,220,12,"Online Game: Off"),"online");
    online.enabled=false;online.allowCheckbox=true;buttonList.add(online);
    buttonList.add(layout.place(new ConsoleButton(3,0,0,220,20,"More World Options..."),"more"));
    ConsoleButton create=layout.place(new ConsoleButton(0,0,0,220,20,"Create New World"),"launch");
    create.enabled=!name().getText().trim().isEmpty();buttonList.add(create);
  }

  private void setMode(String target) {
    for(int i=0;i<3 && !mode().equals(target);i++)nativeAction(2);
  }

  private void cycleMode() {
    setMode("creative".equals(mode())?"survival":"creative");
    rebuild();
  }

  boolean hardcore() {return "hardcore".equals(mode());}
  int difficultyChoice() {return hardcore()?4:selectedDifficulty.getId();}
  void difficultyChoice(int value) {
    value=Math.max(0,Math.min(4,value));
    if(value==4)setMode("hardcore");
    else if(hardcore())setMode("survival");
    selectedDifficulty=value==4?Difficulty.HARD:Difficulty.byId(value);
    mc.options.save();
  }

  String seedText() {return seed().getText();}
  private int terrainIndex() {
    return ReflectionHelper.getPrivateValue(GuiCreateWorld.class,this,"selectedIndex","field_146331_K");
  }
  boolean legacyWorld(){return WorldPreset.WORLD_TYPES[terrainIndex()]==WorldPreset.LEGACY;}
  dev.legacyconsole.worldgen.Ps4WorldSize legacyWorldSize(){return legacySize;}
  void legacyWorldSize(int index){legacySize=dev.legacyconsole.worldgen.Ps4WorldSize.values()[Math.max(0,Math.min(3,index))];legacySizeChanged();}
  dev.legacyconsole.worldgen.Ps4BiomeScale legacyBiomeScale(){return legacyScale;}
  void legacyBiomeScale(int index){legacyScale=dev.legacyconsole.worldgen.Ps4BiomeScale.values()[Math.max(0,Math.min(2,index))];legacySizeChanged();}
  void balancedSeed(String value){setWorldSeed(value);}
  boolean balancedSeedAvailable(){return seedText().isEmpty();}
  boolean findBalancedSeed(){return dev.legacyconsole.worldgen.ps4.Ps4BalancedSeed.shouldSearch(legacyWorld(),findBalancedSeed,seedText());}
  void toggleBalancedSeed(){findBalancedSeed=!findBalancedSeed;}
  boolean flatWorld() {return WorldPreset.WORLD_TYPES[terrainIndex()]==WorldPreset.FLAT;}
  String terrainLabel() {
    return I18n.get(WorldPreset.WORLD_TYPES[terrainIndex()].getTranslateName());
  }
  void cycleTerrainType() {
    terrainSettings.put(terrainIndex(),chunkProviderSettingsJson);
    do {nativeAction(5);}while(flatWorld());
    nonFlatIndex=terrainIndex();
    chunkProviderSettingsJson=terrainSettings.getOrDefault(terrainIndex(),"");
  }
  void toggleFlat() {
    if(legacyWorld())return;
    terrainSettings.put(terrainIndex(),chunkProviderSettingsJson);
    int target=flatWorld()?nonFlatIndex:WorldPreset.FLAT.getWorldTypeID();
    if(!flatWorld())nonFlatIndex=terrainIndex();
    for(int i=0;i<32 && terrainIndex()!=target;i++)nativeAction(5);
    chunkProviderSettingsJson=terrainSettings.getOrDefault(terrainIndex(),"");
  }
  void customize(Screen returnScreen) {
    if(flatWorld())mc.setScreen(new ConsoleFlatWorld(returnScreen,chunkProviderSettingsJson,
        value->{chunkProviderSettingsJson=value;terrainSettings.put(terrainIndex(),value);}));
    else nativeAction(8);
  }

  void editSeed(Screen returnScreen) {edit(101,returnScreen);}


  private void edit(int id) {edit(id,this);}

  private void edit(int id,Screen returnScreen) {
    edit = id;
    GuiTextField field = id == 100 ? name() : seed();
    mc.setScreen(
        new ConsoleKeyboard(
            returnScreen,
            id == 100 ? "World Name" : "World Seed",
            field.getText(),
            value -> {
              field.setText(value);
              ReflectionHelper.setPrivateValue(
                  GuiCreateWorld.class,
                  this,
                  value,
                  id == 100 ? "worldName" : "worldSeed",
                  id == 100 ? "field_146330_J" : "field_146329_I");
              if (id == 100)
                try {
                  SAVE_NAME.invoke(this);
                } catch (ReflectiveOperationException e) {
                  throw new IllegalStateException(e);
                }
            }));
  }

  @Override
  protected void actionPerformed(GuiButton b) {
    if (b.id == 100 || b.id == 101) edit(b.id);
    else if (b.id == 2) cycleMode();
    else if (b.id == 103) packs.activate(this);
    else if (b.id == 3) mc.setScreen(new ConsoleWorldSettings(this));
    else if (b.id == 0) {
      if (name().getText().trim().isEmpty()) return;
      if(findBalancedSeed()){mc.setScreen(new ConsoleBalancedSeed(this,this));return;}
      launchWorld();
    }
  }

  void launchWorld() {
      try {
        SAVE_NAME.invoke(this);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException(e);
      }
      String folder =
          ReflectionHelper.getPrivateValue(
              GuiCreateWorld.class, this, "saveDirName", "field_146336_i");
      ConsoleWorldLaunch.prepare(
          folder,
          "creative".equals(mode()) ? GameType.CREATIVE : GameType.SURVIVAL,
          "hardcore".equals(mode()) ? Difficulty.HARD : selectedDifficulty,fireSpreads);
      boolean privileges=ReflectionHelper.getPrivateValue(GuiCreateWorld.class,this,"allowCheats","field_146340_t");
      dev.legacyconsole.world.WorldPolicy.prepareCreation(folder,
          "creative".equals(mode())?GameType.CREATIVE:GameType.SURVIVAL,
          !hardcore() && privileges,hardcore(),hardcore()?Difficulty.HARD:selectedDifficulty,fireSpreads);
      nativeAction(0);
  }

  @Override
  public void updateScreen() {}

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    if (key == Keyboard.KEY_ESCAPE) {
      nativeAction(1);
      return;
    }
    if (key == Keyboard.KEY_RETURN) {
      actionPerformed(new GuiButton(0, 0, 0, "Create"));
      return;
    }
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    ConsoleTheme.background(width, height);
    packs.focus(packSelector.focused(mx,my));
    layout.render(packs);
    LceWidgets.label("World Name",layout.x+35.05f/3,layout.y+31f/3,ConsoleTheme.DARK);
    for(GuiButton b:buttonList)if(b.id==2)b.displayString=hardcore()?"Game Mode: Survival":models.get(2).displayString;
    for (GuiButton b : buttonList) b.drawButton(mc, mx, my);
    ConsoleTheme.footer(
        width, height, "cross", "Select", "square", "Enter Text", "circle", "Back");
  }

  @Override
  public boolean controllerDirection(int d) {
    if(MenuFocus.id(this)==103 && (d==PadState.LEFT || d==PadState.RIGHT)) {
      packs.move(d==PadState.LEFT?-1:1,packs.visibleSlots(220));return true;
    }
    if (MenuFocus.id(this) == 2 && (d == PadState.LEFT || d == PadState.RIGHT)) {
      cycleMode();
      return true;
    }
    if (MenuFocus.id(this) == 102 && (d == PadState.LEFT || d == PadState.RIGHT)) {
      difficultyChoice(difficultyChoice()+(d==PadState.LEFT?-1:1));
      return true;
    }
    return false;
  }

  @Override
  public boolean controllerButton(int b) {
    if (b == PadState.CROSS && MenuFocus.id(this) == 102) return true;
    if (b == PadState.SQUARE) {
      int focus = MenuFocus.id(this);
      if(focus==100)edit(100);
      return true;
    }
    return false;
  }
}

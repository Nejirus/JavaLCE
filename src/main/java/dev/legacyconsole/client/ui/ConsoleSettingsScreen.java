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

import dev.legacyconsole.client.ClientConfig;
import dev.legacyconsole.client.ConsoleAudio;
import dev.legacyconsole.client.input.*;
import java.io.IOException;
import java.util.*;
import java.util.function.*;




/** Console vertical option lists; settings still use the game's native persistence. */
public final class ConsoleSettingsScreen extends ConsolePanelScreen implements ControllerScreen {
  private static final String[] SECTIONS = {
    "Options", "Audio", "Control", "Graphics", "User Interface"
  };
  private static final int CONTROL = 2, GRAPHICS = 3, USER_INTERFACE = 4;
  private final int section;
  private int first, selected, visibleRows;
  private final List<Setting> entries = new ArrayList<>();

  private static final class Setting {
    final Supplier<String> label;
    final IntConsumer step;
    DoubleSupplier value;
    DoubleConsumer change;
    boolean action;
    boolean binary;
    boolean ps4Slider;

    Setting(Supplier<String> label, IntConsumer step) {
      this.label = label;
      this.step = step;
    }
  }

  public ConsoleSettingsScreen(Screen parent) {
    this(parent, -1);
  }

  public ConsoleSettingsScreen(Screen parent, int section) {
    super(parent, section < 0 ? "Settings" : SECTIONS[section]);
    this.section = section;
    panelW = 276;
    panelH = 202;
  }

  private String on(boolean b) {
    return b ? "On" : "Off";
  }

  private void toggle(String name, BooleanSupplier read, Runnable change) {
    Setting setting=new Setting(() -> name + ": " + on(read.getAsBoolean()),d -> change.run());
    setting.binary=true;
    entries.add(setting);
  }

  private void action(String name, Runnable run) {
    Setting setting = new Setting(() -> name, d -> run.run());
    setting.action = true;
    entries.add(setting);
  }

  private void scalar(
      String name,
      DoubleSupplier read,
      DoubleConsumer write,
      double low,
      double high,
      DoubleFunction<String> format) {
    Setting s =
        new Setting(
            () -> name + ": " + format.apply(read.getAsDouble()),
            d ->
                write.accept(
                    Math.max(low, Math.min(high, read.getAsDouble() + d * (high - low) / 20))));
    s.value = () -> (read.getAsDouble() - low) / (high - low);
    s.change =
        v -> {
          write.accept(low + v * (high - low));
        };
    entries.add(s);
  }

  private void nativeOption(Option option) {
    if(option==Option.GUI_SCALE){entries.add(new Setting(()->"GUI Scale: "+(mc.options.guiScale==0?"Console Auto ("+new ScaledResolution(mc).getScaleFactor()+"x)":mc.options.guiScale+"x"),d->{int max=Math.max(1,Math.min(mc.getWindow().getWidth()/320,mc.getWindow().getHeight()/240));mc.options.guiScale=Math.floorMod(mc.options.guiScale+d,max+1);mc.resizeDisplay();}));return;}
    var nativeWidget=option.createButton(mc.options,0,0,ConsoleOptionLayout.WIDTH);
    Setting setting=new Setting(()->option.createButton(mc.options,0,0,ConsoleOptionLayout.WIDTH).getMessage().getString(),d->{
      if(option instanceof ProgressOption slider){slider.set(mc.options,slider.toValue(OptionWidgets.nextValue(slider,d)));}
      else OptionWidgets.step(option.createButton(mc.options,0,0,ConsoleOptionLayout.WIDTH),d);
    });
    if(nativeWidget instanceof CycleButton<?> cycle)setting.binary=cycle.getValue() instanceof Boolean;
    if(option instanceof ProgressOption slider){setting.value=()->slider.toPct(slider.get(mc.options));setting.change=v->slider.set(mc.options,slider.toValue(v));}
    entries.add(setting);
  }

  private void populate() {
    entries.clear();
    if (section < 0) {
      for (int i = 0; i < SECTIONS.length; i++) {
        final int category = i;
        action(
            SECTIONS[i],
            () ->
                mc.setScreen(
                    category == GRAPHICS && OptifineMenus.available()
                        ? OptifineMenus.video(this)
                        : new ConsoleSettingsScreen(this, category)));
      }
      action(
          "Reset to Defaults",
          () ->
              mc.setScreen(
                  new ConsoleConfirmScreen(
                      this,
                      "Reset to Defaults",
                      "Reset console interface, controller and menu audio settings to their"
                          + " defaults?",
                      () -> {
                        ClientConfig.resetDefaults();
                        mc.setScreen(this);
                      })));
      return;
    }
    if (section == 0) {
      nativeOption(Option.VIEW_BOBBING);
      toggle(
          "Legacy Movement",
          () -> ClientConfig.noSprintStrafe,
          () -> ClientConfig.noSprintStrafe = !ClientConfig.noSprintStrafe);
      nativeOption(Option.MAIN_HAND);
      action("Keyboard Bindings", () -> mc.setScreen(new ConsoleKeyBindings(this)));
      action("Language", () -> mc.setScreen(new ConsoleLanguageScreen(this)));
      action("Texture Packs", () -> mc.setScreen(new ConsolePackScreen(this)));
      action("Mods", () -> mc.setScreen(new net.minecraftforge.fmlclient.gui.screen.ModListScreen(this)));
    }
    if (section == 1) {
      SoundSource[] categories = {
        SoundSource.MUSIC,
        SoundSource.MASTER,
        SoundSource.RECORDS,
        SoundSource.WEATHER,
        SoundSource.BLOCKS,
        SoundSource.HOSTILE,
        SoundSource.NEUTRAL,
        SoundSource.PLAYERS,
        SoundSource.AMBIENT,
        SoundSource.VOICE
      };
      for (SoundSource c : categories)
        scalar(
            c == SoundSource.MASTER
                ? "Sound"
                : I18n.get("soundCategory." + c.getName()),
            () -> mc.options.getSoundSourceVolume(c),
            v -> mc.options.setSoundCategoryVolume(c, (float) v),
            0,
            1,
            v -> Math.round(v * 100) + "%");
      toggle(
          "Console Menu Sounds",
          () -> ClientConfig.menuSounds,
          () -> ClientConfig.menuSounds = !ClientConfig.menuSounds);
      entries.add(
          new Setting(
              () ->
                  "Console Music: "
                      + (!ClientConfig.menuMusic
                          ? "Vanilla"
                          : ClientConfig.musicMode == 0
                              ? "LCE Mix"
                              : ClientConfig.musicMode == 1 ? "Moog City 2" : "Beginning 2"),
              d -> {
                int next =
                    Math.floorMod((ClientConfig.menuMusic ? ClientConfig.musicMode : 3) + d, 4);
                ClientConfig.menuMusic = next != 3;
                ClientConfig.musicMode = Math.min(2, next);
              }));
      scalar(
          "Console Music Volume",
          () -> ClientConfig.musicVolume,
          v -> ClientConfig.musicVolume = (float) v,
          0,
          1,
          v -> Math.round(v * 100) + "%");
      nativeOption(Option.SHOW_SUBTITLES);
    }
    if (section == GRAPHICS) {
      nativeOption(Option.RENDER_CLOUDS);
      nativeOption(Option.GRAPHICS);
      nativeOption(Option.RENDER_DISTANCE);
      nativeOption(Option.GAMMA);
      nativeOption(Option.FOV);
      nativeOption(Option.AMBIENT_OCCLUSION);
      nativeOption(Option.FRAMERATE_LIMIT);
      nativeOption(Option.ENABLE_VSYNC);
      nativeOption(Option.PARTICLES);
      nativeOption(Option.ENTITY_SHADOWS);
      nativeOption(Option.USE_FULLSCREEN);
      nativeOption(Option.MIPMAP_LEVELS);
    }
    if (section == USER_INTERFACE) {
      nativeOption(Option.GUI_SCALE);
      Setting consoleSize=new Setting(()->"Console UI Size: "+(ClientConfig.consoleUiSize+1),d->ClientConfig.consoleUiSize=Math.max(0,Math.min(2,ClientConfig.consoleUiSize+d)));
      consoleSize.value=()->ClientConfig.consoleUiSize/2.0;
      consoleSize.change=v->ClientConfig.consoleUiSize=(int)Math.round(v*2);
      consoleSize.ps4Slider=true;entries.add(consoleSize);
      Setting hud = new Setting(() -> "HUD Size: " + (ClientConfig.hudSize + 1),
          d -> ClientConfig.hudSize = Math.max(0, Math.min(2, ClientConfig.hudSize + d)));
      hud.value = () -> ClientConfig.hudSize / 2.0;
      hud.change = v -> ClientConfig.hudSize = (int) Math.round(v * 2);
      entries.add(hud);
      scalar(
          "HUD Extra Lift",
          () -> ClientConfig.hudLift,
          v -> ClientConfig.hudLift = (int) Math.round(v),
          0,
          50,
          v -> Math.round(v) + "");
      toggle(
          "Console Menus",
          () -> ClientConfig.consoleMenus,
          () -> ClientConfig.consoleMenus = !ClientConfig.consoleMenus);
      toggle(
          "Console HUD",
          () -> ClientConfig.consoleHud,
          () -> ClientConfig.consoleHud = !ClientConfig.consoleHud);
      toggle(
          "Animated Character",
          () -> ClientConfig.animatedCharacter,
          () -> ClientConfig.animatedCharacter = !ClientConfig.animatedCharacter);
      toggle(
          "Display Coordinates",
          () -> ClientConfig.coordinates,
          () -> ClientConfig.coordinates = !ClientConfig.coordinates);
      toggle("Hints",()->ClientConfig.hints,()->ClientConfig.hints=!ClientConfig.hints);

      nativeOption(Option.ATTACK_INDICATOR);
      nativeOption(Option.CHAT_VISIBILITY);
      nativeOption(Option.CHAT_OPACITY);
      nativeOption(Option.CHAT_SCALE);
      nativeOption(Option.CHAT_COLOR);
      nativeOption(Option.FORCE_UNICODE_FONT);
    }
    if (section == CONTROL) {
      nativeOption(Option.AUTO_JUMP);
      toggle(
          "Crouch to Shield",
          () -> ClientConfig.crouchShield,
          () -> ClientConfig.crouchShield = !ClientConfig.crouchShield);
      scalar(
          "Look Sensitivity",
          () -> ClientConfig.sensitivity,
          v -> ClientConfig.sensitivity = (float) v,
          40,
          400,
          v -> Math.round(v) + "");
      scalar(
          "Menu Pointer Speed",
          () -> ClientConfig.cursorSpeed,
          v -> ClientConfig.cursorSpeed = (float) v,
          80,
          600,
          v -> Math.round(v) + "");
      toggle(
          "Controller",
          () -> ClientConfig.controller,
          () -> ClientConfig.controller = !ClientConfig.controller);
      toggle(
          "Invert Look",
          () -> ClientConfig.invertY,
          () -> ClientConfig.invertY = !ClientConfig.invertY);
      scalar(
          "Look / Menu Deadzone",
          () -> ClientConfig.deadzone,
          v -> ClientConfig.deadzone = (float) v,
          .05,
          .6,
          v -> Math.round(v * 100) + "%");
      entries.add(
          new Setting(
              () ->
                  "Controller: "
                      + (ClientConfig.controllerIndex < 0
                          ? "Automatic"
                          : "Slot " + (ClientConfig.controllerIndex + 1)),
              d ->
                  ClientConfig.controllerIndex =
                      Math.floorMod(ClientConfig.controllerIndex + 1 + d, 5) - 1));
      entries.add(
          new Setting(
              () ->
                  "Inventory Cursor: "
                      + new String[] {"Left Stick", "Right Stick", "Both Sticks"}
                          [ClientConfig.cursorStick],
              d -> ClientConfig.cursorStick = Math.floorMod(ClientConfig.cursorStick + d, 3)));
      entries.add(
          new Setting(
              () ->
                  "Controller Glyphs: "
                      + dev.legacyconsole.client.input.ControllerGlyphs.optionLabel(),
              d -> ClientConfig.glyphStyle = Math.floorMod(ClientConfig.glyphStyle + d, 5)));
      action("How to Play", () -> mc.setScreen(new ConsoleHelpScreen(this)));
    }
  }

  @Override
  public void initGui() {
    populate();
    selected = Math.max(0, Math.min(selected, entries.size() - 1));
    rebuild();
  }

  private void rebuild() {
    boolean[] boxes = new boolean[entries.size()];
    for (int i=0;i<boxes.length;i++) boxes[i]=entries.get(i).binary;
    ConsoleOptionLayout layout = new ConsoleOptionLayout(width,height,boxes,0,first,selected,
        section==USER_INTERFACE ? ConsoleOptionLayout.UI_CHECKBOX_PITCH : ConsoleOptionLayout.CHECKBOX_PITCH);
    panelW=layout.w; panelH=layout.h; panelX=layout.x; panelY=layout.y;
    first=layout.first; visibleRows=layout.rows;
    buttonList.clear();
    for (int i = first; i < Math.min(entries.size(), first + visibleRows); i++) {
      Setting setting = entries.get(i);
      int y = layout.rowY(i);
      GuiButton button =
          setting.value == null
              ? new ConsoleButton(
                  i, panelX + 8, y, panelW - 16, ConsoleOptionLayout.HEIGHT, setting.label.get())
              : new ConsoleSlider(
                  i, panelX + 8, y, panelW - 16, setting.value, setting.change, setting.label,setting.ps4Slider);
      button.height = layout.rowHeight(i);
      if(button instanceof ConsoleButton)((ConsoleButton)button).allowCheckbox=setting.binary;
      buttonList.add(button);
    }
    MenuFocus.set(this, selected);
  }

  private void move(int delta) {
    if (entries.isEmpty()) return;
    int previous = selected;
    selected = Math.floorMod(selected + delta, entries.size());
    rebuild();
    ControllerCursor.navigation();
    ControllerMenus.focus(this, selected);
    if (previous != selected) ConsoleAudio.play("focus");
  }

  private void scroll(int delta) {
    move(delta);
  }

  private void adjust(int index, int delta) {
    if (index < 0 || index >= entries.size()) return;
    int scale = mc.options.guiScale;
    entries.get(index).step.accept(delta);
    ClientConfig.save();
    mc.options.save();
    if (mc.screen != this) return;
    if (scale != mc.options.guiScale) {
      ScaledResolution resolution = new ScaledResolution(mc);
      setWorldAndResolution(mc, resolution.getScaledWidth(), resolution.getScaledHeight());
    } else rebuild();
    if (ControllerCursor.active()) ControllerMenus.focus(this, index);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    selected = button.id;
    if (!(button instanceof ConsoleSlider)) adjust(selected, 1);
  }

  @Override
  protected void mouseReleased(int x, int y, int button) {
    super.mouseReleased(x, y, button);
    ClientConfig.save();
    mc.options.save();
  }

  @Override
  public void onGuiClosed() {
    ClientConfig.save();
    mc.options.save();
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    int wheel = Mouse.getEventDWheel();
    if (wheel != 0) scroll(wheel > 0 ? -1 : 1);
  }

  @Override
  protected void keyTyped(char c, int key) throws IOException {
    if (key == Keyboard.KEY_UP) move(-1);
    else if (key == Keyboard.KEY_DOWN) move(1);
    else if (key == Keyboard.KEY_LEFT
        || key == Keyboard.KEY_RIGHT) {
      ControllerCursor.navigation();
      if (!entries.get(selected).action)
        adjust(selected, key == Keyboard.KEY_LEFT ? -1 : 1);
    } else if (key == Keyboard.KEY_PRIOR) scroll(-1);
    else if (key == Keyboard.KEY_NEXT) scroll(1);
    else if (key == Keyboard.KEY_RETURN) {
      ConsoleTheme.buttonPressed(MenuFocus.button(this));
      ControllerCursor.navigation();
      ConsoleAudio.play("confirm");
      adjust(selected, 1);
    } else super.keyTyped(c, key);
  }

  @Override
  protected void shell() {
    ConsoleMenuLayout.background(width, height);
    ConsoleMenuLayout.logo(width, height);
    if (section >= 0) ConsoleTheme.lcePanel(panelX, panelY, panelW, panelH);
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    super.drawScreen(mx, my, partial);
    {
      if (first > 0)
        ConsoleTheme.sprite("gui/sprites/widget/scroll_up", panelX + panelW + 5, panelY + 8, 7, 5);
      if (first + visibleRows < entries.size())
        ConsoleTheme.sprite(
            "gui/sprites/widget/scroll_down", panelX + panelW + 5, panelY + panelH - 13, 7, 5);
      if (section == CONTROL && panelY + panelH + 18 < height - 24)
        ConsoleTheme.centered(
            ConsoleTheme.fit(ControllerInput.INSTANCE.status, width - 20),
            width / 2,
            panelY + panelH + 8,
            ConsoleTheme.TEXT);
    }
  }

  @Override
  public void controllerOpened() {
    selected = first = 0;
    rebuild();
    ControllerMenus.focus(this, 0);
  }

  @Override
  public boolean controllerDirection(int direction) {
    if (direction == PadState.UP || direction == PadState.DOWN) {
      move(direction == PadState.UP ? -1 : 1);
    } else {
      GuiButton focused = MenuFocus.button(this);
      if (focused != null && focused.id < entries.size() && !entries.get(focused.id).action) {
        selected = focused.id;
        adjust(selected, direction == PadState.LEFT ? -1 : 1);
      }
    }
    return true;
  }

  @Override
  public boolean controllerButton(int button) {
    if (button == PadState.L2 || button == PadState.R2) {
      scroll(button == PadState.L2 ? -1 : 1);
      return true;
    }
    if (button == PadState.CROSS) {
      GuiButton focused = MenuFocus.button(this);
      if (focused != null) {
        selected = focused.id;
        ConsoleAudio.play("confirm");
        adjust(selected, 1);
      }
      return true;
    }
    return false;
  }
}

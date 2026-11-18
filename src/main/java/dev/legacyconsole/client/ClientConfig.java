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

import java.io.File;
import dev.legacyconsole.port.Configuration;




public final class ClientConfig {
  private static Configuration config;
  private static boolean resolutionDefaultsPending;
  private static boolean controlDefaultsPending;
  public static boolean controller = true, consoleMenus = true, consoleHud = true;
  public static boolean crouchShield = true;
  public static boolean animatedCharacter = true;
  public static boolean hints = true;
  public static boolean noSprintStrafe = true,
      invertY = false,
      menuSounds = true,
      menuMusic = true,
      coordinates = false;
  public static float deadzone = .18f, sensitivity = 170f, cursorSpeed = 210f, musicVolume = .55f;
  public static int controllerIndex = -1, musicMode = 0, cursorStick = 0;
  public static final float buttonScale = 1f, buttonBrightness = .67f;
  public static int glyphStyle = 0;
  public static int hudLift = 0;
  public static int hudSize = 1;
  public static int consoleUiSize = 1;

  private ClientConfig() {}

  public static void load(File directory) {
    config = new Configuration(new File(directory, "legacyconsole-client.cfg"));
    controller = config.getBoolean("enabled", "controller", true, "Enable SDL2 gamepad input.");
    controllerIndex =
        config.getInt(
            "index", "controller", -1, -1, 3, "-1 selects the first connected controller.");
    glyphStyle =
        config.getInt(
            "glyphStyle",
            "controller",
            0,
            0,
            4,
            "0 automatic; 1 PlayStation 4, 2 PlayStation 3, 3 Xbox 360, 4 Xbox One.");
    deadzone = config.getFloat("deadzone", "controller", .18f, .05f, .6f, "Look/menu radial deadzone. Legacy movement uses LCE's per-axis 10000/32767 deadzone.");
    sensitivity =
        config.getFloat(
            "sensitivity",
            "controller",
            170f,
            40f,
            400f,
            "Camera degrees per second at full stick.");
    cursorSpeed =
        config.getFloat(
            "cursorSpeed", "controller", 210f, 80f, 600f, "Menu cursor pixels per second.");
    cursorStick =
        config.getInt(
            "cursorStick",
            "controller",
            0,
            0,
            2,
            "Inventory pointer: 0 left stick (console), 1 right stick, 2 both.");
    invertY = config.getBoolean("invertY", "controller", false, "Invert camera vertical input.");
    crouchShield =
        config.getBoolean(
            "crouchShield",
            "controller",
            true,
            "Raise shields only by crouching; attacks and item use take priority.");
    // Apply the requested new default once to profiles written by earlier releases.
    if(!config.get("controller","crouchShieldDefaults218",false).getBoolean()) {
      crouchShield=true;
      config.get("controller","crouchShield",true).set(true);
      config.get("controller","crouchShieldDefaults218",false).set(true);
    }
    consoleMenus =
        config.getBoolean(
            "consoleMenus", "ui", true, "Console title, pause and inventory appearance.");
    consoleHud =
        config.getBoolean("consoleHud", "ui", true, "Original LCE HUD and controller prompts.");
    coordinates = config.getBoolean("coordinates", "ui", false, "Show player position.");
    hints = config.getBoolean("hints", "ui", true, "PS4 contextual hints. Guided tutorial-world prompts remain active.");
    animatedCharacter =
        config.getBoolean(
            "animatedCharacter",
            "ui",
            true,
            "LCE animated player while sneaking, sprinting or flying.");
    noSprintStrafe =
        config.getBoolean(
            "noSprintStrafe",
            "movement",
            true,
            "LCE movement: per-axis deadzone, sideways input retained, and walking below 0.8 forward input.");
    menuSounds =
        config.getBoolean("menuSounds", "audio", true, "Original LCE console interface sounds.");
    menuMusic =
        config.getBoolean(
            "menuMusic",
            "audio",
            true,
            "LCE shared menu/Survival/Creative music selection and original 60 Hz audio timing.");
    musicMode =
        config.getInt(
            "musicMode",
            "audio",
            0,
            0,
            2,
            "0: LCE track pool; optional menu overrides 1: Moog City 2; 2: Beginning 2.");
    musicVolume =
        config.getFloat(
            "musicVolume", "audio", .55f, 0f, 1f, "Relative to the Minecraft Music slider.");
    config.getCategory("ui").remove("buttonScale");
    config.getCategory("ui").remove("buttonBrightness");
    resolutionDefaultsPending = !config.get("ui", "consoleResolutionDefaults", false).getBoolean();
    controlDefaultsPending = !config.get("controller", "consoleControlsDefaults1171", false).getBoolean();
    if (resolutionDefaultsPending) {
      config.get("ui", "hudLift", 0).set(0);
    }
    hudLift =
        config.getInt("hudLift", "ui", 0, 0, 50, "Extra lift above LCE's original HUD position.");
    hudSize = config.getInt("hudSize", "ui", 1, 0, 2,
        "LCE HUD size: 0 Small, 1 Normal, 2 Large (the source's three HUD scenes).");
    consoleUiSize=config.getInt("consoleUiSize","ui",1,0,2,"Console UI size: 0 Small, 1 Normal, 2 Large. PS4 menu/popup adapter; HUD Size is independent.");
    if (config.hasChanged()) config.save();
  }

  /** Forge ignores saveOptions during loading; commit the migration at the first real menu. */
  public static boolean applyResolutionDefaults() {
    if ((!resolutionDefaultsPending && !controlDefaultsPending)
        || net.minecraft.client.Minecraft.getInstance().getOverlay()!=null) return false;
    Minecraft game = Minecraft.getInstance();
    if (resolutionDefaultsPending) game.options.guiScale = 0;
    if (controlDefaultsPending) game.options.autoJump = false;
    game.options.save();
    config.get("ui", "consoleResolutionDefaults", false).set(true);
    config.get("controller", "consoleControlsDefaults1171", false).set(true);
    config.save();
    resolutionDefaultsPending = false;
    controlDefaultsPending = false;
    return true;
  }

  /** LCE's confirmed profile reset, limited to the console mod's persisted options. */
  public static void resetDefaults() {
    controller = consoleMenus = consoleHud = noSprintStrafe = menuSounds = menuMusic = true;
    animatedCharacter = true;
    hints = true;
    invertY = coordinates = false;
    crouchShield = true;
    deadzone = .18f;
    sensitivity = 170f;
    cursorSpeed = 210f;
    musicVolume = .55f;
    controllerIndex = -1;
    musicMode = cursorStick = 0;
    glyphStyle = 0;
    Minecraft.getInstance().options.guiScale = 0;
    Minecraft.getInstance().options.save();
    hudLift = 0;
    hudSize = 1;
    consoleUiSize = 1;
    if (config != null) save();
  }

  public static void save() {
    config.get("controller", "enabled", true).set(controller);
    config.get("controller", "index", -1).set(controllerIndex);
    config.get("controller", "glyphStyle", 0).set(glyphStyle);
    config.get("controller", "deadzone", .18).set((double) deadzone);
    config.get("controller", "sensitivity", 170).set((double) sensitivity);
    config.get("controller", "cursorSpeed", 210).set((double) cursorSpeed);
    config.get("controller", "cursorStick", 0).set(cursorStick);
    config.get("controller", "invertY", false).set(invertY);
    config.get("controller", "crouchShield", true).set(crouchShield);
    config.get("ui", "consoleMenus", true).set(consoleMenus);
    config.get("ui", "consoleHud", true).set(consoleHud);
    config.get("ui", "animatedCharacter", true).set(animatedCharacter);
    config.get("ui", "coordinates", false).set(coordinates);
    config.get("ui", "hints", true).set(hints);
    config.get("movement", "noSprintStrafe", true).set(noSprintStrafe);
    config.get("audio", "menuSounds", true).set(menuSounds);
    config.get("audio", "menuMusic", true).set(menuMusic);
    config.get("audio", "musicMode", 0).set(musicMode);
    config.get("audio", "musicVolume", .55).set((double) musicVolume);
    config.getCategory("ui").remove("buttonScale");
    config.getCategory("ui").remove("buttonBrightness");
    config.get("ui", "hudLift", 0).set(hudLift);
    config.get("ui", "hudSize", 1).set(hudSize);
    config.get("ui","consoleUiSize",1).set(consoleUiSize);
    config.save();
  }
}

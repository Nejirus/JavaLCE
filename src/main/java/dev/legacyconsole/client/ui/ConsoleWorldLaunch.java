package dev.legacyconsole.client.ui;

import dev.legacyconsole.world.WorldPolicy;
import dev.legacyconsole.world.WorldOnlineGame;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelResource;

public final class ConsoleWorldLaunch {
  private static String folder;
  private static GameType mode;
  private static Difficulty difficulty;
  private static Boolean fire;
  private static boolean online;

  public static void prepare(String save, GameType type, Difficulty level) {
    prepare(save, type, level, null, false);
  }

  public static void prepare(String save, GameType type, Difficulty level, boolean fireSpreads) {
    prepare(save, type, level, fireSpreads, false);
  }

  public static void prepare(String save, GameType type, Difficulty level,
      Boolean fireSpreads, boolean onlineGame) {
    folder = save;
    mode = type;
    difficulty = level;
    fire = fireSpreads;
    online = onlineGame;
  }

  public static void clear() {
    folder = null;
    fire = null;
    online = false;
  }

  public static void tick(Minecraft mc) {
    if (folder == null || mc.player == null || mc.level == null
        || mc.getSingleplayerServer() == null) return;
    var server = mc.getSingleplayerServer();
    String actual = server.getWorldPath(LevelResource.ROOT).normalize().getFileName().toString();
    if (!folder.equals(actual)) {
      clear();
      return;
    }
    var id = mc.player.getUUID();
    var selected = mode;
    var diff = difficulty;
    var fireRule = fire;
    boolean share = online;
    clear();
    server.execute(() -> {
      // A newly created save now exists; retain its creation-menu choice as well.
      WorldOnlineGame.write(server.getWorldPath(LevelResource.ROOT), share);
      var policy = WorldPolicy.get(server.overworld());
      var player = server.getPlayerList().getPlayer(id);
      if (policy.locked()) {
        policy.enforce(server);
        if (!policy.difficultyLocked()) server.setDifficulty(diff, true);
      } else {
        if (player != null && !(server.isHardcore() && player.isSpectator()))
          player.setGameMode(selected);
        server.setDifficulty(diff, true);
        if (fireRule != null)
          server.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(fireRule, server);
      }
      if (!share || player == null) return;
      var gameType = policy.locked() ? server.getWorldData().getGameType() : selected;
      boolean commands = server.getWorldData().getAllowCommands();
      // ShareToLanScreen publishes on the client thread after the launch settings apply.
      mc.execute(() -> {
        if (mc.getSingleplayerServer() != server || mc.player == null
            || !id.equals(mc.player.getUUID()) || server.isPublished()) return;
        int port = HttpUtil.getAvailablePort();
        var message = server.publishServer(gameType, commands, port)
            ? new TranslatableComponent("commands.publish.started", port)
            : new TranslatableComponent("commands.publish.failed");
        mc.gui.getChat().addMessage(message);
        mc.updateTitle();
      });
    });
  }
}

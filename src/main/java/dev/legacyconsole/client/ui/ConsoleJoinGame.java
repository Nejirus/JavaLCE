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



/** UIScene_JoinMenu's game-details/player-list layout, populated by Java's real server status. */
public final class ConsoleJoinGame extends ConsolePanelScreen
    implements dev.legacyconsole.client.input.ControllerScreen {
  private final ConsolePlayScreen play;
  private final ServerData server;
  private final ServerStatusPinger pinger = new ServerStatusPinger();
  private boolean started;
  private int playerOffset;
  private int detailsWidth=184;

  public ConsoleJoinGame(ConsolePlayScreen play, ServerData server) {
    super(play, "Join Game");
    this.play = play;
    this.server = server;
    panelW = 225;
    panelH = 177;
  }

  @Override
  public void initGui() {
    super.initGui();
    panelH = Math.min(177, height - 74);
    detailsWidth=Math.min(184,Math.max(80,(width-16)*184/409));
    panelW=Math.min(225,width-16-detailsWidth);
    panelX=(width-panelW-detailsWidth)/2;
    panelY = (height - panelH) / 2 + Math.min(34, Math.max(0, (height - panelH) / 2 - 17));
    buttonList.add(new ConsoleButton(0, (width - 225) / 2, panelY - 25, 225, 20, "Join Game"));
    buttonList.add(new ConsoleButton(1, panelX + 8, panelY + panelH - 26, panelW - 16, 20, "Back"));
    if (!started) {
      started = true;
      Thread worker =
          new Thread(
              () -> {
                try {
                  pinger.pingServer(server,()->{});
                } catch (Exception e) {
                  server.ping = -1;
                  server.motd = new TextComponent("Unable to connect");
                }
              },
              "Console game details");
      worker.setDaemon(true);
      worker.start();
    }
  }

  @Override
  public void updateScreen() {
    pinger.tick();
  }

  @Override
  public void onGuiClosed() {
    pinger.removeAll();
  }

  @Override
  protected void actionPerformed(GuiButton b) {
    if (b.id == 0) play.connect(server);
    else back();
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    ConsoleTheme.background(width, height);
    ConsoleTheme.sprite(
        "gui/sprites/tiles/pointer_panel", panelX + panelW - 2, panelY + 5, detailsWidth+2, panelH - 10);
    ConsoleTheme.panel(panelX, panelY, panelW, panelH);
    ConsoleTheme.recess(panelX + 7, panelY + 9, panelW - 14, panelH - 42);
    int side = panelX + panelW + 5;
    int textWidth=Math.max(1,detailsWidth-15);
    ConsoleTheme.text(
        ConsoleTheme.fit(server.name, textWidth), side, panelY + 13, ConsoleTheme.TEXT);
    List<String> motd =
        fontRendererObj.listFormattedStringToWidth(
            server.motd == null ? "Connecting..." : server.motd.getString(), textWidth);
    for (int i = 0; i < Math.min((panelH - 87) / 10, motd.size()); i++)
      ConsoleTheme.text(motd.get(i), side, panelY + 32 + i * 10, ConsoleTheme.TEXT);
    ConsoleTheme.text(
        ConsoleTheme.fit(ServerStatusText.label("Version",server.version),textWidth), side, panelY + panelH - 43, ConsoleTheme.TEXT);
    ConsoleTheme.text(
        "Latency: " + (server.ping < 0 ? "..." : server.ping + " ms"),
        side,
        panelY + panelH - 31,
        ConsoleTheme.TEXT);
    ConsoleTheme.text(
        ConsoleTheme.fit(ServerStatusText.label("Players",server.status),textWidth),
        side,
        panelY + panelH - 19,
        ConsoleTheme.TEXT);
    String names=server.playerList==null?null:String.join("\n",server.playerList.stream().map(Component::getString).toList());
    String[] players = names == null || names.isEmpty() ? new String[0] : names.split("\n");
    JoinPlayerLayout list=new JoinPlayerLayout(panelX,panelY,panelW,panelH);
    int rows=list.rows;
    playerOffset=list.offset(playerOffset,players.length);
    if (players.length == 0)
      fontRendererObj.drawSplitString(
          server.status != null
                  && ChatFormatting.stripFormatting(server.status.getString())
                      .startsWith("0/")
              ? "No players online"
              : "Player list not provided",
          panelX + 15,
          panelY + 21,
          panelW - 30,
          ConsoleTheme.DARK);
    else
      for (int i = 0; i < rows && playerOffset + i < players.length; i++) {
        ConsoleButton player =
            new ConsoleButton(
                100 + i,
                list.x,
                list.rowY(i),
                list.width,
                JoinPlayerLayout.ROW_HEIGHT,
                players[playerOffset + i]);
        LceWidgets.place(player,player.xPosition,player.yPosition,player.width,player.height);
        boolean focused=ConsoleTheme.buttonHover(player,mx,my);
        LceWidgets.listButton(LceWidgets.bounds(player),focused);
        String label=ConsoleTheme.fit(player.displayString,player.width-8);
        fontRendererObj.drawString(label,player.xPosition+(player.width-fontRendererObj.getStringWidth(label))/2f,
            player.yPosition+(player.height-fontRendererObj.lineHeight)/2f,focused?0xffffff00:ConsoleTheme.TEXT,false);
      }
    if (players.length > rows) {
      ConsoleTheme.sprite("gui/sprites/widget/scroll_up", panelX + panelW - 11, panelY + 10, 7, 5);
      ConsoleTheme.sprite(
          "gui/sprites/widget/scroll_down", panelX + panelW - 11, panelY + panelH - 42, 7, 5);
    }
    for (GuiButton button : buttonList) button.drawButton(mc, mx, my);
    ConsoleTheme.footer(width, height, "cross", "Join / Select", "circle", "Back");
  }

  @Override
  public void handleMouseInput() throws java.io.IOException {
    super.handleMouseInput();
    int wheel = dev.legacyconsole.port.Mouse.getEventDWheel();
    if (wheel != 0) playerOffset += wheel > 0 ? -1 : 1;
  }

  @Override
  public boolean controllerDirection(int d) {
    return false;
  }

  @Override
  public boolean controllerButton(int b) {
    if (b == dev.legacyconsole.client.input.PadState.L2
        || b == dev.legacyconsole.client.input.PadState.R2) {
      playerOffset += b == dev.legacyconsole.client.input.PadState.L2 ? -1 : 1;
      return true;
    }
    return false;
  }
}

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

import dev.legacyconsole.LegacyConsole;
import dev.legacyconsole.client.ConsoleAudio;
import dev.legacyconsole.client.input.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.ImageIO;




/** Create World, Play Tutorial and saves on the left; available games on the right. */
public final class ConsolePlayScreen extends ConsolePanelScreen implements ControllerScreen {
  private static final ExecutorService PINGS=Executors.newFixedThreadPool(3,r->{
    Thread thread=new Thread(r,"Legacy server status");thread.setDaemon(true);return thread;
  });
  private final List<LevelSummary> worlds=new ArrayList<>();
  private final List<ServerData> lanServers=new ArrayList<>();
  private final Map<String,ResourceLocation> icons=new HashMap<>();
  private final LcePlayNavigation navigation;
  private final ServerStatusPinger pinger=new ServerStatusPinger();
  private ServerList servers;
  private LanServerDetection.LanServerList lanList;
  private LanServerDetection.LanServerDetector lanThread;
  private LcePlayLayout layout;
  private boolean entered,switched;
  private int lastColumn,pendingJoinSelection=-1;
  private long fadeStarted;
  private String error="";

  public ConsolePlayScreen(Screen parent,int previousTab) {
    super(parent,"Play Game");

    navigation=new LcePlayNavigation(5,previousTab==2?1:0);
  }

  @Override public void initGui() {
    layout=new LcePlayLayout(width,height,mc.getWindow().getHeight());
    loadData();startLan();
    navigation.update(worlds.size(),servers.size()+lanServers.size());
    if(pendingJoinSelection>=0) {
      navigation.select(1,pendingJoinSelection);pendingJoinSelection=-1;
    }
    if(!entered) {
      lastColumn=navigation.column;fadeStarted=System.nanoTime();entered=true;
    }
    rebuild();
  }

  private void loadData() {
    error="";worlds.clear();
    try {
      worlds.addAll(mc.getLevelSource().getLevelList());Collections.sort(worlds);
    } catch(Exception exception) {
      error="Unable to read world list";LegacyConsole.log.warn(error,exception);
    }
    servers=new ServerList(mc);
  }

  private void startLan() {
    if(lanThread!=null)return;
    lanList=new LanServerDetection.LanServerList();
    try {
      lanThread=new LanServerDetection.LanServerDetector(lanList);lanThread.start();
    } catch(IOException exception) {LegacyConsole.log.debug("LAN discovery unavailable",exception);}
  }

  private ServerData server(int index) {
    return index<servers.size()?servers.get(index):lanServers.get(index-servers.size());
  }

  private String rowTitle(int column,int index) {
    if(column==1)return server(index).name;
    if(index==LcePlayNavigation.CREATE_WORLD)return LcePlayLayout.label("IDS_CREATE_NEW_WORLD");
    if(index==LcePlayNavigation.PLAY_TUTORIAL)return "Play Tutorial";
    return worlds.get(index-LcePlayNavigation.FIRST_SAVED_WORLD).getLevelName();
  }

  private void rebuild() {
    navigation.update(worlds.size(),servers.size()+lanServers.size());
    buttonList.clear();
    for(int c=0;c<2;c++)for(int i=navigation.first[c];i<Math.min(navigation.count[c],navigation.first[c]+layout.rows);i++)
      buttonList.add(layout.place(new EntryButton(c,i),c,i-navigation.first[c]));
    if(lastColumn!=navigation.column) {
      lastColumn=navigation.column;fadeStarted=System.nanoTime();switched=true;
    }
    MenuFocus.set(this,navigation.id());
  }

  private void moved(boolean changed) {
    rebuild();ControllerMenus.focus(this,navigation.id());
    if(changed)ConsoleAudio.play("focus");
  }

  private void activate(int column,int index) {
    if(index<0 || index>=navigation.count[column])return;
    navigation.select(column,index);
    if(column==1)mc.setScreen(new ConsoleJoinGame(this,server(index)));
    else if(index==LcePlayNavigation.CREATE_WORLD)mc.setScreen(new ConsoleCreateWorld(this));
    else if(index==LcePlayNavigation.PLAY_TUTORIAL)ConsoleTutorialWorld.launch(this);
    else mc.setScreen(new ConsoleLoadWorld(this,worlds.get(index-LcePlayNavigation.FIRST_SAVED_WORLD)));
  }

  private void options() {
    int index=navigation.selected[navigation.column];
    if(navigation.column==0) {
      if(index>=LcePlayNavigation.FIRST_SAVED_WORLD)mc.setScreen(new ConsoleWorldOptions(this,worlds.get(index-LcePlayNavigation.FIRST_SAVED_WORLD)));
    } else if(index<servers.size())
      mc.setScreen(new ConsoleServerOptions(this,servers,index));
    else activate(1,index);
  }

  private void refresh() {pinger.removeAll();loadData();rebuild();}
  public void connect(ServerData server) {net.minecraft.client.gui.screens.ConnectScreen.startConnecting(this,mc,net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(server.ip),server);}
  public void loadWorld(LevelSummary level) {loadWorld(level,()->{});}

  public void loadWorld(LevelSummary level,Runnable beforeLaunch) {
    Runnable launch=()->{
      if(!level.isDisabled()) {
        beforeLaunch.run();
        ConsoleTutorialWorld.load(level.getLevelId());
      }
    };
    if(level.askToOpenWorld())mc.setScreen(new ConsoleConfirmScreen(this,"World Version",
        "This world was saved in "+level.getWorldVersionName().getString()+". Opening it in 1.17.1 may damage the save.",launch));
    else launch.run();
  }

  @Override protected void actionPerformed(GuiButton button) {
    if(button instanceof EntryButton) {
      EntryButton entry=(EntryButton)button;activate(entry.column,entry.index);
    }
  }

  @Override protected void mouseClicked(int x,int y,int button) throws IOException {
    for(GuiButton row:buttonList)if(ConsoleTheme.buttonHover(row,x,y)) {
      EntryButton entry=(EntryButton)row;
      navigation.select(entry.column,entry.index);MenuFocus.set(this,navigation.id());
      if(button==1) {options();return;}
    }
    super.mouseClicked(x,y,button);
  }

  @Override public void handleMouseInput() throws IOException {
    super.handleMouseInput();int wheel=Mouse.getEventDWheel();
    if(wheel!=0)moved(navigation.move(wheel>0?-1:1));
  }

  @Override protected void keyTyped(char c,int key) throws IOException {
    if(key==Keyboard.KEY_UP || key==Keyboard.KEY_DOWN)moved(navigation.move(key==Keyboard.KEY_UP?-1:1));
    else if(key==Keyboard.KEY_LEFT || key==Keyboard.KEY_RIGHT || key==Keyboard.KEY_TAB)moved(navigation.switchColumn());
    else if(key==Keyboard.KEY_PRIOR || key==Keyboard.KEY_NEXT)moved(navigation.page(key==Keyboard.KEY_PRIOR?-1:1));
    else if(key==Keyboard.KEY_RETURN)activate(navigation.column,navigation.selected[navigation.column]);
    else if(key==Keyboard.KEY_F5)refresh();
    else if(key==Keyboard.KEY_DELETE)options();
    else if(key==Keyboard.KEY_J)mc.setScreen(new JoinOptions());
    else super.keyTyped(c,key);
  }

  @Override public void updateScreen() {
    pinger.tick();
    if(lanList!=null && lanList.isDirty()) {
      lanServers.clear();
      for(LanServer server:lanList.getServers())lanServers.add(new ServerData(server.getMotd(),server.getAddress(),true));
      lanList.markClean();rebuild();
    }
    for(int i=0;i<servers.size();i++) {
      ServerData server=servers.get(i);
      if(!server.pinged) {
        server.pinged=true;server.ping=-2;server.motd=new TextComponent("Connecting…");
        PINGS.submit(()->{
          try {pinger.pingServer(server,()->{});}catch(Exception exception) {
            server.ping=-1;server.motd=new TextComponent("Unable to connect");
          }
        });
      }
    }
  }

  @Override public void onGuiClosed() {
    pinger.removeAll();
    if(lanThread!=null) {lanThread.interrupt();lanThread=null;}
    for(ResourceLocation texture:icons.values())mc.getTextureManager().release(texture);
    icons.clear();
  }

  private void label(String text,float x,float y,int color) {
    GlStateManager.pushMatrix();GlStateManager.translate(x,y,0);GlStateManager.scale(layout.scale,layout.scale,1);
    fontRendererObj.drawString(text,0,0,color,false);GlStateManager.popMatrix();
  }

  @Override public void drawScreen(int mx,int my,float partial) {
    ConsoleTheme.background(width,height);ConsoleMenuLayout.logo(width,height);
    // Unnamed depth-one FJ_Panel9Grid from LoadOrJoinMenu720/1080.
    // Recess fades composite against this opaque frame, as in the original scene.
    float[] backing=layout.background;
    LceWidgets.panel("panel",backing[0],backing[1],backing[2],backing[3]);
    if(!ControllerCursor.active())for(GuiButton button:buttonList)if(ConsoleTheme.buttonHover(button,mx,my)) {
      EntryButton entry=(EntryButton)button;
      if(navigation.select(entry.column,entry.index))moved(false);
      break;
    }
    long elapsed=System.nanoTime()-fadeStarted;
    for(int c=0;c<2;c++) {
      float[] r=layout.panels[c];
      float alpha=c==navigation.column?layout.alpha(true,elapsed):(switched?layout.alpha(false,elapsed):.5f);
      LceWidgets.panel("recess",r[0],r[1],r[2],r[3],alpha);
      float[] title=layout.titles[c];
      label(LcePlayLayout.label(c==0?"IDS_START_GAME":"IDS_JOIN_GAME"),title[0],title[1],ConsoleTheme.DARK);
    }
    for(GuiButton button:buttonList)button.drawButton(mc,mx,my);
    if(navigation.count[1]==0) {
      String text=LcePlayLayout.label("IDS_NO_GAMES_FOUND");
      label(text,layout.lists[1][0]+(layout.lists[1][2]-fontRendererObj.getStringWidth(text)*layout.scale)/2,
          layout.noGames[1],ConsoleTheme.DARK);
    }
    if(!error.isEmpty())label(ConsoleTheme.fit(error,200),layout.lists[0][0],layout.lists[0][1]+layout.rowHeight,0xffb03030);
    for(int c=0;c<2;c++) {
      float right=layout.lists[c][0]+layout.lists[c][2];
      float y=layout.lists[c][1]+layout.rows*layout.rowHeight+3*layout.scale;
      if(navigation.first[c]>0)ConsoleTheme.spriteAt(layout.image("scroll_up"),right-32*layout.scale,y,16*layout.scale,11*layout.scale);
      if(navigation.first[c]+layout.rows<navigation.count[c])ConsoleTheme.spriteAt(layout.image("scroll_down"),right-16*layout.scale,y,16*layout.scale,11*layout.scale);
    }
    List<String> hints=new ArrayList<>(Arrays.asList("cross","Select","circle","Back"));
    if(navigation.column==1 || navigation.selected[0]>=LcePlayNavigation.FIRST_SAVED_WORLD)Collections.addAll(hints,"square","Options");
    Collections.addAll(hints,"triangle","Join Options");
    ConsoleTheme.footer(width,height,hints.toArray(new String[0]));
  }

  @Override public void controllerOpened() {rebuild();ControllerMenus.focus(this,navigation.id());}
  @Override public boolean controllerDirection(int direction) {
    moved(direction==PadState.UP || direction==PadState.DOWN
        ?navigation.move(direction==PadState.UP?-1:1):navigation.switchColumn());return true;
  }
  @Override public boolean controllerButton(int button) {
    if(button==PadState.CROSS) {activate(navigation.column,navigation.selected[navigation.column]);return true;}
    if(button==PadState.SQUARE || button==PadState.R1) {options();return true;}
    if(button==PadState.TRIANGLE) {mc.setScreen(new JoinOptions());return true;}
    if(button==PadState.L2 || button==PadState.R2) {moved(navigation.page(button==PadState.L2?-1:1));return true;}
    return button==PadState.L1;
  }

  private final class EntryButton extends GuiButton {
    final int column,index;
    EntryButton(int column,int index) {
      super(LcePlayNavigation.id(column,index),0,0,200,30,rowTitle(column,index));
      this.column=column;this.index=index;
    }
    @Override public void drawButton(Minecraft game,int mx,int my) {
      hovered=ConsoleTheme.buttonHover(this,mx,my);
      boolean focus=ControllerCursor.active()?navigation.column==column && navigation.selected[column]==index:hovered;
      float[] r=LceWidgets.bounds(this);
      // Both states use the source's 60/90-pixel list-button art, including its bevel.
      LceWidgets.listButton(r,focus);
      ResourceLocation icon=icon(column,index);
      if(icon!=null) {
        PortGraphics.bind(icon);GlStateManager.color(1,1,1,1);
        GlStateManager.pushMatrix();GlStateManager.translate(r[0]+layout.iconX(column),r[1]+layout.iconY(),0);
        GlStateManager.scale(layout.iconSize,layout.iconSize,1);
        drawModalRectWithCustomSizedTexture(0,0,0,0,1,1,1,1);GlStateManager.popMatrix();
      } else if(column==1)ConsoleTheme.spriteAt("lce/widgets/default_pack_icon",r[0]+layout.iconX(column),r[1]+layout.iconY(),layout.iconSize,layout.iconSize);
      float textX=layout.textX(column);
      label(ConsoleTheme.fit(rowTitle(column,index),(int)((r[2]-textX)/layout.scale)-4),r[0]+textX,
          r[1]+(r[3]-fontRendererObj.lineHeight*layout.scale)/2,focus?0xffffff00:ConsoleTheme.TEXT);
    }
    @Override public boolean mousePressed(Minecraft game,int x,int y) {
      return enabled && visible && ConsoleTheme.buttonHover(this,x,y);
    }
    @Override public void playPressSound(SoundManager sounds) {ConsoleAudio.play("confirm");}
  }

  private ResourceLocation icon(int column,int index) {
    if(column==0 && index==LcePlayNavigation.CREATE_WORLD)return null; // Source AddDefaultButtons passes an empty texture name.
    if(column==0 && index==LcePlayNavigation.PLAY_TUTORIAL)return new ResourceLocation(LegacyConsole.ID,"textures/ps4/tutorial/icon.png");
    String key=column==0?"world:"+worlds.get(index-LcePlayNavigation.FIRST_SAVED_WORLD).getLevelId():"server:"+server(index).ip;
    if(icons.containsKey(key))return icons.get(key);
    try {
      BufferedImage image=null;
      if(column==0) {
        File file=worlds.get(index-LcePlayNavigation.FIRST_SAVED_WORLD).getIcon();
        if(file.isFile())image=ImageIO.read(file);
      } else if(server(index).getIconB64()!=null)
        image=ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(server(index).getIconB64())));
      if(image!=null && image.getWidth()==64 && image.getHeight()==64) {
        ResourceLocation texture=mc.getTextureManager().register("legacy_play_icon",new DynamicTexture(NativeImages.fromBufferedImage(image)));
        icons.put(key,texture);return texture;
      }
    } catch(Exception ignored) {}
    return null;
  }

  /** Native Java server-address actions replace platform-only console invitations. */
  private final class JoinOptions extends ConsolePanelScreen {
    JoinOptions() {super(ConsolePlayScreen.this,"Join Game");panelH=154;}
    @Override public void initGui() {
      super.initGui();addRow(0,0,"Add Server");addRow(1,1,"Direct Connection");addRow(2,2,"Refresh");addRow(3,3,"Back");
    }
    @Override protected void actionPerformed(GuiButton button) {
      if(button.id==0 || button.id==1) {
        pendingJoinSelection=button.id==0?servers.size():navigation.selected[1];
        mc.setScreen(new ConsoleServerEditor(ConsolePlayScreen.this,servers,button.id==0?-1:-2));
      } else if(button.id==2) {
        refresh();mc.setScreen(ConsolePlayScreen.this);
      } else back();
    }
  }
}

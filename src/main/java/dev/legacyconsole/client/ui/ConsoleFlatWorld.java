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

import dev.legacyconsole.client.ConsoleAudio;
import dev.legacyconsole.client.input.*;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;



/** Port of Legacy4J LegacyFlatWorldScreen. The supplied LCE revision has no layer editor. */
public final class ConsoleFlatWorld extends ConsolePanelScreen implements ControllerScreen {
  private static final String[] TABS={"Layers","Biomes","Properties"};
  private static final String[] FEATURES={"decoration","lake","village","biome_1","mineshaft","stronghold","oceanmonument"};
  private static final String[] FEATURE_NAMES={"Decorations","Lakes","Villages","Temples","Mineshafts","Strongholds","Ocean Monuments"};
  final FlatWorldModel model;
  private final Consumer<String> apply;
  private final List<Biome> biomes=new ArrayList<>();
  private final int[] selected=new int[3],first=new int[3];
  private int tab,rows;
  private boolean moving;

  public ConsoleFlatWorld(GuiCreateWorld create,String preset) {
    this(create,preset,value->create.chunkProviderSettingsJson=value);
  }
  public ConsoleFlatWorld(Screen parent,String preset,Consumer<String> apply) {
    super(parent,"Customize Superflat");this.apply=apply;model=new FlatWorldModel(preset);
    for(Biome biome:net.minecraft.data.BuiltinRegistries.BIOME)biomes.add(biome);
  }
  public String preset() {return model.preset();}
  public void preset(String value) {model.preset(value);selected[0]=first[0]=0;}
  private int count() {return tab==0?model.size():tab==1?biomes.size():FEATURES.length;}
  private int pitch() {return tab==2?15:30;}
  @Override public void initGui() {
    panelW=282;panelH=Math.min(248,height-76);
    panelX=(width-panelW-(width>=500?194:0))/2;panelY=(height-panelH)/2;
    rebuild();
  }
  private void rebuild() {
    buttonList.clear();selected[tab]=Math.max(0,Math.min(selected[tab],count()-1));
    rows=Math.max(1,(panelH-22)/pitch());
    first[tab]=Math.max(0,Math.min(first[tab],Math.max(0,count()-rows)));
    if(selected[tab]<first[tab])first[tab]=selected[tab];
    if(selected[tab]>=first[tab]+rows)first[tab]=selected[tab]-rows+1;
    for(int i=first[tab];i<Math.min(count(),first[tab]+rows);i++) {
      GuiButton b;
      if(tab==2) {
        ConsoleButton box=new ConsoleButton(100+i,panelX+11,panelY+11+(i-first[tab])*15,260,12,
            FEATURE_NAMES[i]+": "+(model.info.getWorldFeatures().containsKey(FEATURES[i])?"On":"Off"));
        box.allowCheckbox=true;b=box;
      } else b=new Row(i,panelX+11,panelY+11+(i-first[tab])*30);
      buttonList.add(b);
    }
    MenuFocus.set(this,100+selected[tab]);
  }
  private void select(int index) {
    int previous=selected[tab];selected[tab]=Math.floorMod(index,Math.max(1,count()));
    if(moving && tab==0)model.move(previous,selected[tab]);
    rebuild();ControllerMenus.focus(this,100+selected[tab]);
    if(previous!=selected[tab])ConsoleAudio.play("focus");
  }
  private void changeTab(int direction) {
    if(moving)return;tab=Math.floorMod(tab+direction,3);rebuild();ControllerMenus.focus(this,100+selected[tab]);ConsoleAudio.play("tab");
  }
  private void activate() {
    if(tab==0) {
      if(moving) {moving=false;return;}
      mc.setScreen(new LayerOptions(selected[0]));return;
    } else if(tab==1)model.info.setBiome(net.minecraft.data.BuiltinRegistries.BIOME.getId(biomes.get(selected[1])));
    else model.toggle(FEATURES[selected[2]]);
    rebuild();
  }
  private void presets() {if(!moving)mc.setScreen(new ConsolePresetScreen(this,true,this::preset));}
  @Override protected void actionPerformed(GuiButton button) {
    selected[tab]=button.id-100;activate();
  }
  @Override protected void back() {apply.accept(model.preset());super.back();}
  @Override protected void keyTyped(char c,int key) throws IOException {
    if(key==Keyboard.KEY_UP || key==Keyboard.KEY_DOWN)select(selected[tab]+(key==Keyboard.KEY_UP?-1:1));
    else if(key==Keyboard.KEY_RETURN) {ConsoleAudio.play("confirm");activate();}
    else if(key==Keyboard.KEY_LBRACKET || key==Keyboard.KEY_RBRACKET)changeTab(key==Keyboard.KEY_LBRACKET?-1:1);
    else if(key==Keyboard.KEY_X && tab==0)moving=!moving;
    else if(key==Keyboard.KEY_O)presets();
    else super.keyTyped(c,key);
  }
  @Override public void handleMouseInput() throws IOException {
    super.handleMouseInput();int wheel=Mouse.getEventDWheel();if(wheel!=0)select(selected[tab]+(wheel>0?-1:1));
  }
  @Override protected void mouseClicked(int x,int y,int button) throws IOException {
    if(button==0 && y>=panelY-24 && y<panelY && x>=panelX && x<panelX+panelW) {
      tab=Math.min(2,(x-panelX)/94);moving=false;rebuild();return;
    }
    super.mouseClicked(x,y,button);
  }
  @Override public void drawScreen(int mx,int my,float partial) {
    ConsoleTheme.background(width,height);
    for(int i=0;i<3;i++)if(i!=tab)ConsoleTheme.joinedTab(panelX+i*94,panelY,94,false,i==0?0:i==2?2:1,TABS[i]);
    if(width>=500) {
      LceWidgets.panel("pointer_panel",panelX+panelW-2,panelY+5,194,panelH-10);
      String description=tab==0?rowName(selected[0]):tab==1?FlatWorldModel.biomeName(biomes.get(selected[1])):FEATURE_NAMES[selected[2]];
      fontRendererObj.drawSplitString(description,panelX+panelW+6,panelY+13,180,ConsoleTheme.TEXT);
      if(tab==0)ConsoleTheme.text("Height: "+model.layer(selected[0]).getHeight(),panelX+panelW+6,panelY+40,ConsoleTheme.TEXT);
    }
    LceWidgets.panel("panel",panelX,panelY,panelW,panelH);
    LceWidgets.panel("recess",panelX+7,panelY+7,panelW-14,panelH-14);
    ConsoleTheme.joinedTab(panelX+tab*94,panelY,94,true,tab==0?0:tab==2?2:1,TABS[tab]);
    for(GuiButton button:buttonList)button.drawButton(mc,mx,my);
    if(first[tab]>0)ConsoleTheme.sprite("gui/sprites/widget/scroll_up",panelX+panelW-9,panelY+4,7,5);
    if(first[tab]+rows<count())ConsoleTheme.sprite("gui/sprites/widget/scroll_down",panelX+panelW-9,panelY+panelH-9,7,5);
    List<String> hints=new ArrayList<>(Arrays.asList("cross",tab==0?(moving?"Place":"Layer Options"):"Select","circle","Back"));
    if(tab==0 && !moving)Collections.addAll(hints,"square","Move Layer");
    if(!moving)Collections.addAll(hints,"triangle","Presets","left_bumper","","right_bumper","Select Tab");
    ConsoleTheme.footer(width,height,hints.toArray(new String[0]));
  }
  private String rowName(int index) {
    FlatLayerInfo layer=model.layer(index);ItemStack stack=new ItemStack(layer.getBlockState().getBlock());
    return stack.isEmpty()?layer.getBlockState().getBlock().getName().getString():stack.getHoverName().getString();
  }
  private void icon(ItemStack stack,float x,float y) {
    if(stack.isEmpty())return;GlStateManager.pushMatrix();GlStateManager.translate(x,y,0);GlStateManager.scale(1.25,1.25,1);ConsoleTheme.item(stack,0,0);GlStateManager.popMatrix();
  }
  private final class Row extends GuiButton {
    final int index;
    Row(int index,int x,int y) {super(100+index,x,y,260,30,"");this.index=index;}
    @Override public void drawButton(Minecraft game,int mx,int my) {
      hovered=ConsoleTheme.buttonHover(this,mx,my);boolean focus=MenuFocus.focused(this,hovered);
      String path=focus?LceWidgets.path("list_button_selected"):"lce/play/"+(mc.getWindow().getHeight()>=1080?"hd/":"sd/")+"list_button";
      ConsoleTheme.sprite(path,xPosition,yPosition,width,height);
      int color=focus?0xffffff00:ConsoleTheme.TEXT;
      if(tab==0) {
        FlatLayerInfo layer=model.layer(index);ItemStack stack=new ItemStack(layer.getBlockState().getBlock());
        ConsoleTheme.text(layer.getHeight()+"",xPosition+12,yPosition+11,color);icon(stack,xPosition+39,yPosition+5);
        ConsoleTheme.text(ConsoleTheme.fit(rowName(index),width-71),xPosition+67,yPosition+11,color);
      } else {
        Biome biome=biomes.get(index);ConsoleTheme.spriteAt(LceWidgets.path(focus?"checkbox_selected":"checkbox"),xPosition+6,yPosition+9,12,12);
        if(model.info.getBiome()==net.minecraft.data.BuiltinRegistries.BIOME.getId(biome))ConsoleTheme.spriteAt(LceWidgets.path("tick"),xPosition+6,yPosition+9,14,12);
        icon(new ItemStack(biome.getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial().getBlock()),xPosition+26,yPosition+5);
        ConsoleTheme.text(ConsoleTheme.fit(FlatWorldModel.biomeName(biome),width-58),xPosition+54,yPosition+11,color);
      }
    }
    @Override public void playPressSound(SoundManager handler) {ConsoleAudio.play("confirm");}
  }
  private final class LayerOptions extends ConsolePanelScreen {
    final int index;
    LayerOptions(int index) {super(ConsoleFlatWorld.this,"Layer Options");this.index=index;panelH=150;}
    @Override public void initGui() {
      super.initGui();addRow(0,0,"Edit Layer");addRow(1,1,"Add Layer");addRow(2,2,"Delete Layer");addRow(3,3,"Back");
      buttonList.get(1).enabled=model.height()<256;buttonList.get(2).enabled=model.size()>1;
    }
    @Override protected void actionPerformed(GuiButton b) {
      if(b.id==0 || b.id==1)mc.setScreen(new ConsoleFlatLayerSelector(ConsoleFlatWorld.this,model.layer(index),model.available(index,b.id==0),b.id==0,(block,metadata,height)->model.put(index,b.id==0,height,block,metadata)));
      else if(b.id==2) {model.remove(index);back();}
      else back();
    }
  }
  @Override public void controllerOpened() {rebuild();ControllerMenus.focus(this,100+selected[tab]);}
  @Override public boolean controllerDirection(int direction) {
    if(direction==PadState.UP || direction==PadState.DOWN)select(selected[tab]+(direction==PadState.UP?-1:1));return true;
  }
  @Override public boolean controllerButton(int button) {
    if(button==PadState.CROSS) {ConsoleAudio.play("confirm");activate();return true;}
    if(button==PadState.SQUARE && tab==0) {moving=!moving;return true;}
    if(button==PadState.TRIANGLE) {presets();return true;}
    if(button==PadState.L1 || button==PadState.R1) {changeTab(button==PadState.L1?-1:1);return true;}
    return false;
  }
}

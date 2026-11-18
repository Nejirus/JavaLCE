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



/** Legacy4J FlatWorldLayerSelector's block grid, height control and native flat-layer result. */
final class ConsoleFlatLayerSelector extends ConsolePanelScreen implements ControllerScreen {
  interface Apply {void layer(Block block,int metadata,int height);}
  private static final class Entry {
    final Block block;final int metadata;final ItemStack stack;
    Entry(Block block,int metadata,ItemStack stack) {this.block=block;this.metadata=metadata;this.stack=stack;}
    String name() {return stack.isEmpty()?block.getName().getString():stack.getHoverName().getString();}
  }
  private final List<Entry> entries=new ArrayList<>();
  private final Apply apply;
  private final int maxHeight;
  private int selected,heightValue=1,page,gridRows=5,pageSize=50;
  private int sliderY,detailY,okY;
  private boolean dragging;

  ConsoleFlatLayerSelector(Screen parent,FlatLayerInfo layer,int maxHeight,boolean edit,Apply apply) {
    super(parent,edit?"Edit Layer":"Add Layer");this.apply=apply;this.maxHeight=maxHeight;
    entries.add(new Entry(Blocks.AIR,0,ItemStack.EMPTY));
    entries.add(new Entry(Blocks.WATER,0,new ItemStack(Items.WATER_BUCKET)));
    entries.add(new Entry(Blocks.LAVA,0,new ItemStack(Items.LAVA_BUCKET)));
    for(Block block:net.minecraft.core.Registry.BLOCK) {
      if(block instanceof LiquidBlock)continue;
      Item item=Item.byBlock(block);if(item==Items.AIR)continue;
      NonNullList<ItemStack> variants=NonNullList.create();variants.add(new ItemStack(item));
      if(variants.isEmpty())variants.add(new ItemStack(item));
      for(ItemStack stack:variants)entries.add(new Entry(block,stack.getDamageValue(),stack));
    }
    if(edit) {
      heightValue=layer.getHeight();Block block=layer.getBlockState().getBlock();
      int metadata=0;
      for(int i=0;i<entries.size();i++)if(entries.get(i).block==block && entries.get(i).metadata==metadata) {selected=i;break;}
    }
  }
  @Override public void initGui() {
    panelW=325;gridRows=Math.max(1,Math.min(5,(height-170)/27));pageSize=gridRows*10;
    panelH=110+gridRows*27;panelX=(width-panelW)/2;panelY=(height-panelH)/2;
    sliderY=panelY+32+gridRows*27;detailY=sliderY+20;okY=detailY+29;
    page=selected/pageSize;rebuild();
  }
  private int pages() {return Math.max(1,(entries.size()+pageSize-1)/pageSize);}
  private void rebuild() {
    buttonList.clear();
    for(int i=0;i<pageSize;i++)
      buttonList.add(new Cell(i,panelX+23+i%10*27,panelY+24+i/10*27));
    ConsoleSlider height=new ConsoleSlider(100,panelX+21,sliderY,271,
        ()->maxHeight==1?0:(heightValue-1)/(double)(maxHeight-1),
        v->heightValue=1+(int)Math.round(v*(maxHeight-1)),()->"Layer Height: "+heightValue);
    height.height=16;buttonList.add(height);
    buttonList.add(new ConsoleButton(101,panelX+57,okY,200,20,"OK"));
  }
  private void page(int value) {
    int next=Math.max(0,Math.min(pages()-1,value));
    if(page==next)return;page=next;rebuild();ControllerMenus.focus(this,0);ConsoleAudio.play("focus");
  }
  private void scrollAt(int y) {page((int)Math.round((pages()-1)*(y-(panelY+24))/(double)Math.max(1,gridRows*27-1)));}
  @Override protected void actionPerformed(GuiButton b) {
    if(b.id<100) {selected=page*pageSize+b.id;}
    else if(b.id==101) {Entry entry=entries.get(selected);apply.layer(entry.block,entry.metadata,heightValue);back();}
  }
  @Override public void drawScreen(int mx,int my,float partial) {
    ConsoleTheme.background(width,height);LceWidgets.panel("panel",panelX,panelY,panelW,panelH);
    ConsoleTheme.centered(title,panelX+panelW/2,panelY+8,ConsoleTheme.DARK);
    LceWidgets.panel("recess",panelX+20,detailY,275,27);
    Entry selected=entries.get(this.selected);
    ConsoleTheme.text(heightValue+"",panelX+24,detailY+10,ConsoleTheme.TEXT);
    icon(selected.stack,panelX+50,detailY+3);
    ConsoleTheme.text(ConsoleTheme.fit(selected.name(),220),panelX+70,detailY+10,ConsoleTheme.TEXT);
    LceWidgets.panel("square_recess",panelX+299.5f,panelY+24,13,gridRows*27);
    LceWidgets.panel("panel16",panelX+297.5f,panelY+23+(pages()==1?0:page*(gridRows*27-15f)/(pages()-1)),16,16);
    if(page>0)ConsoleTheme.sprite("gui/sprites/widget/scroll_up",panelX+302,panelY+13,7,5);
    if(page+1<pages())ConsoleTheme.sprite("gui/sprites/widget/scroll_down",panelX+302,panelY+25+gridRows*27,7,5);
    for(GuiButton b:buttonList)b.drawButton(mc,mx,my);
    ConsoleTheme.footer(width,height,"cross","Select","circle","Back","left_trigger","Page Up","right_trigger","Page Down");
  }
  private void icon(ItemStack stack,float x,float y) {
    if(stack.isEmpty())return;GlStateManager.pushMatrix();GlStateManager.translate(x,y,0);GlStateManager.scale(1.25,1.25,1);ConsoleTheme.item(stack,0,0);GlStateManager.popMatrix();
  }
  private final class Cell extends GuiButton {
    Cell(int id,int x,int y) {super(id,x,y,27,27,"");enabled=page*pageSize+id<entries.size();}
    @Override public void drawButton(Minecraft mc,int mx,int my) {
      hovered=ConsoleTheme.buttonHover(this,mx,my);
      ConsoleTheme.sprite("lce/crafting_slot",xPosition,yPosition,27,27);
      if(!enabled)return;
      if(MenuFocus.focused(this,hovered))ConsoleTheme.spriteSize("gui/sprites/container/slot_highlight",xPosition+1,yPosition+1,25,25);
      icon(entries.get(page*pageSize+id).stack,xPosition+3.5f,yPosition+3.5f);
    }
    @Override public void playPressSound(SoundManager handler) {ConsoleAudio.play("focus");}
  }
  @Override public void handleMouseInput() throws IOException {
    super.handleMouseInput();int wheel=Mouse.getEventDWheel();if(wheel!=0)page(page+(wheel>0?-1:1));
  }
  @Override protected void mouseClicked(int x,int y,int button) throws IOException {
    if(button==0 && x>=panelX+299 && x<panelX+313 && y>=panelY+24 && y<panelY+24+gridRows*27) {dragging=true;scrollAt(y);return;}
    super.mouseClicked(x,y,button);
  }
  @Override protected void mouseClickMove(int x,int y,int button,long elapsed) {if(dragging)scrollAt(y);else super.mouseClickMove(x,y,button,elapsed);}
  @Override protected void mouseReleased(int x,int y,int button) {dragging=false;super.mouseReleased(x,y,button);}
  @Override protected void keyTyped(char c,int key) throws IOException {
    if(key==Keyboard.KEY_PRIOR || key==Keyboard.KEY_NEXT)page(page+(key==Keyboard.KEY_PRIOR?-1:1));
    else if(key==Keyboard.KEY_RETURN) {GuiButton b=MenuFocus.button(this);if(b!=null)actionPerformed(b);}
    else if(key==Keyboard.KEY_UP || key==Keyboard.KEY_DOWN || key==Keyboard.KEY_LEFT || key==Keyboard.KEY_RIGHT)
      controllerDirection(key==Keyboard.KEY_UP?PadState.UP:key==Keyboard.KEY_DOWN?PadState.DOWN:key==Keyboard.KEY_LEFT?PadState.LEFT:PadState.RIGHT);
    else super.keyTyped(c,key);
  }
  @Override public void controllerOpened() {ControllerMenus.focus(this,selected%pageSize);}
  @Override public boolean controllerDirection(int direction) {
    int id=MenuFocus.id(this),next=id;
    if(id==100 && (direction==PadState.LEFT || direction==PadState.RIGHT)) {
      heightValue=Math.max(1,Math.min(maxHeight,heightValue+(direction==PadState.LEFT?-1:1)));return true;
    }
    if(direction==PadState.DOWN)next=id<100?(id+10>=Math.min(pageSize,entries.size()-page*pageSize)?100:id+10):101;
    if(direction==PadState.UP)next=id==101?100:id==100?Math.min(pageSize,entries.size()-page*pageSize)-1:Math.max(0,id-10);
    if(direction==PadState.LEFT && id<100)next=Math.max(0,id-1);
    if(direction==PadState.RIGHT && id<100)next=Math.min(Math.min(pageSize,entries.size()-page*pageSize)-1,id+1);
    ControllerMenus.focus(this,next);if(next!=id)ConsoleAudio.play("focus");return true;
  }
  @Override public boolean controllerButton(int button) {
    if(button==PadState.L2 || button==PadState.R2) {page(page+(button==PadState.L2?-1:1));return true;}
    if(button==PadState.CROSS) {GuiButton b=MenuFocus.button(this);if(b!=null && b.id!=100) {b.playPressSound(mc.getSoundManager());actionPerformed(b);}return true;}
    return false;
  }
}

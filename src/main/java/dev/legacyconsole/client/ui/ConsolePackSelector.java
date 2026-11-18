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
import dev.legacyconsole.client.input.ControllerCursor;



/** FJ_TexturePackList: fixed-size horizontal slots, active preview, and confirm-to-apply. */
final class ConsolePackSelector extends GuiButton {
  final ConsolePacks.Selection selection;
  ConsolePackSelector(int id,ConsolePacks.Selection selection) {
    super(id,0,0,220,36,"");this.selection=selection;
  }

  boolean focused(int mx,int my) {
    return ControllerCursor.active() ? MenuFocus.focused(this,false) : ConsoleTheme.buttonHover(this,mx,my);
  }

  @Override public void drawButton(Minecraft mc,int mx,int my) {
    if (!visible) return;
    float[] r=LceWidgets.bounds(this);
    boolean focus=focused(mx,my);
    hovered=ConsoleTheme.buttonHover(this,mx,my);
    selection.focus(focus);
    LceWidgets.label("Texture Packs",r[0],r[1]-37f/3,focus?0xffffff00:ConsoleTheme.DARK);
    LceWidgets.panel("recess",r[0],r[1],r[2],r[3]);
    int count=selection.visibleSlots(r[2]);
    float inset=(r[2]-count*30)/2;
    for(int i=0;i<count && selection.first+i<selection.size();i++) {
      int index=selection.first+i;
      float x=r[0]+inset+i*30,y=r[1]+(r[3]-30)/2;
      selection.icon(index,x+1,y+1,28,28);
      if(selection.enabled(index)){
        ConsoleTheme.spriteAt(LceWidgets.path("checkbox"),x+17,y+17,12,12);
        ConsoleTheme.spriteAt(LceWidgets.path("tick"),x+17,y+17,14,12);
      }
      if(index==selection.preview)
        ConsoleTheme.spriteAt(LceWidgets.path("pack_outline"),x,y,30,30);
    }
    if(selection.first>0) arrow(r[0]+inset/2,r[1]+r[3]/2,true);
    if(selection.first+count<selection.size()) arrow(r[0]+r[2]-inset/2,r[1]+r[3]/2,false);
    GlStateManager.color(1,1,1,1);
  }

  private void arrow(float x,float y,boolean left) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x,y,0);
    if(left)GlStateManager.scale(-1,1,1);
    ConsoleTheme.spriteAt(LceWidgets.path("scroll_arrow"),-3,-5.5f,6,11);
    GlStateManager.popMatrix();
  }

  @Override public boolean mousePressed(Minecraft mc,int mx,int my) {
    if(!enabled || !visible || !ConsoleTheme.buttonHover(this,mx,my)) return false;
    // Controller confirm uses the list's focused entry. Its synthetic click is at
    // the widget center, which must not select a different visible mouse slot.
    if(ControllerCursor.active() && !ControllerCursor.pointerMode()
        && MenuFocus.id(mc.screen)==id) return true;
    float[]r=LceWidgets.bounds(this);
    int count=selection.visibleSlots(r[2]);float inset=(r[2]-count*30)/2;
    if(mx<r[0]+inset) {selection.move(-1,count);return false;}
    if(mx>=r[0]+r[2]-inset) {selection.move(1,count);return false;}
    int index=selection.first+(int)((mx-r[0]-inset)/30);
    if(index>=selection.size())return false;
    selection.preview=index;
    return true;
  }

  @Override public void playPressSound(SoundManager handler) {
    ConsoleAudio.play("confirm");
  }
}

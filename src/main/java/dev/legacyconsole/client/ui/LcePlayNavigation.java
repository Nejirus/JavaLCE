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





/** FJ_ButtonList: independent child focus, five visible rows, bounded and paged scrolling. */
final class LcePlayNavigation {
  static final int CREATE_WORLD=0, PLAY_TUTORIAL=1, FIRST_SAVED_WORLD=2;
  final int[] count={FIRST_SAVED_WORLD,0}, selected={0,0}, first={0,0};
  int column;
  private final int rows;

  LcePlayNavigation(int rows,int preferredColumn) {
    this.rows=rows;column=preferredColumn==1?1:0;
  }
  void update(int worlds,int games) {
    count[0]=Math.max(0,worlds)+FIRST_SAVED_WORLD;count[1]=Math.max(0,games);
    for(int c=0;c<2;c++)clamp(c);
    if(count[column]==0)column=0;
  }
  private void clamp(int c) {
    selected[c]=Math.max(0,Math.min(selected[c],count[c]-1));
    first[c]=Math.max(0,Math.min(first[c],Math.max(0,count[c]-rows)));
    if(selected[c]<first[c])first[c]=selected[c];
    if(selected[c]>=first[c]+rows)first[c]=selected[c]-rows+1;
  }
  boolean select(int c,int index) {
    if(c<0 || c>1 || count[c]==0)return false;
    int oldColumn=column,oldIndex=selected[column];
    column=c;selected[c]=Math.max(0,Math.min(index,count[c]-1));clamp(c);
    return column!=oldColumn || selected[c]!=oldIndex;
  }
  boolean switchColumn() {return select(1-column,selected[1-column]);}
  boolean move(int delta) {return select(column,selected[column]+delta);}
  boolean page(int direction) {
    int index=selected[column],top=first[column];
    int next=direction<0?(index>top?top:index-rows)
        :(index<top+rows-1?top+rows-1:index+rows);
    return select(column,next);
  }
  int id() {return id(column,selected[column]);}
  static int id(int column,int index) {return 100+index*2+column;}
}

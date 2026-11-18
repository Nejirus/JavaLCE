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

import java.io.IOException;
import java.util.List;



final class ConsolePacks {
  private static ResourceLocation defaultIcon;

  private ConsolePacks() {}

  private static String[] bindCurrentIcon() {
    Minecraft mc = Minecraft.getInstance();
    List<PackRepositoryBridge.Entry> entries =
        PackRepositoryBridge.get().getRepositoryEntries();
    if (!entries.isEmpty()) {
      PackRepositoryBridge.Entry e = entries.get(entries.size() - 1);
      e.bindTexturePackIcon(mc.getTextureManager());
      return new String[] {e.getResourcePackName(), e.getTexturePackDescription()};
    }
    try {
      if (defaultIcon == null)
        defaultIcon =
            mc.getTextureManager()
                .register(
                    "console_default_pack",
                    new DynamicTexture(
                        NativeImages.fromBufferedImage(PackRepositoryBridge.get().rprDefaultResourcePack.getPackImage())));
      PortGraphics.bind(defaultIcon);
    } catch (IOException e) {
      PortGraphics.bind(new ResourceLocation("textures/misc/unknown_pack.png"));
    }
    return new String[] {"Default", "The classic look of Minecraft."};
  }

  /** Assort.Selector.renderTooltipBox: adjacent pointer panel, icon/name, then description. */
  static void sidebar(int x, int y, int w, int h) {
    ConsoleTheme.sprite("gui/sprites/tiles/pointer_panel", x, y, w, h);
    String[] info = bindCurrentIcon();
    GlStateManager.color(1, 1, 1, 1);
    Gui.drawModalRectWithCustomSizedTexture(x + 7, y + 5, 0, 0, 32, 32, 32, 32);
    FontCompat.get()
        .drawSplitString(info[0], x + 43, y + 8, w - 52, ConsoleTheme.TEXT);
    FontCompat.get()
        .drawSplitString(info[1], x + 8, y + 43, w - 16, ConsoleTheme.TEXT);
  }

  static GuiButton selector(int id, int x, int y, int w, int h) {
    return new GuiButton(id, x, y, w, h, "") {
      @Override
      public void drawButton(Minecraft mc, int mx, int my) {
        hovered =
            mx >= xPosition && mx < xPosition + width && my >= yPosition && my < yPosition + height;
        boolean focus = MenuFocus.focused(this, hovered);
        ConsoleTheme.text(
            "Texture Packs", xPosition + 2, yPosition, focus ? 0xffffff00 : ConsoleTheme.DARK);
        String[] info = bindCurrentIcon();
        int size = Math.min(32, height - 11);
        GlStateManager.color(1, 1, 1, 1);
        Gui.drawModalRectWithCustomSizedTexture(
            xPosition + 10, yPosition + 11, 0, 0, size, size, size, size);
        ConsoleTheme.text(
            ConsoleTheme.fit(info[0], width - size - 28),
            xPosition + size + 20,
            yPosition + height / 2 + 2,
            ConsoleTheme.DARK);
        if (focus)
          ConsoleTheme.sprite(
              "gui/sprites/container/select_icon_highlight",
              xPosition + 8,
              yPosition + 9,
              size + 4,
              size + 4);
      }

      @Override
      public void playPressSound(SoundManager h) {
        dev.legacyconsole.client.ConsoleAudio.play("confirm");
      }
    };
  }

  /** Native resource packs replace the console DLC catalog; preview never reloads resources. */
  static final class Selection {
    final java.util.List<PackRepositoryBridge.Entry> entries;
    final PackSelectionStack selected;
    int applied,preview,first;
    private boolean focused;
    Selection() {
      Minecraft mc=Minecraft.getInstance();
      PackRepositoryBridge repository=PackRepositoryBridge.get();
      repository.updateRepositoryEntriesAll();
      entries=new java.util.ArrayList<>(repository.getRepositoryEntriesAll());
      java.util.List<PackRepositoryBridge.Entry> current=repository.getRepositoryEntries();
      selected=new PackSelectionStack(current.stream().map(PackRepositoryBridge.Entry::getResourcePackName).toList());
      String active=current.isEmpty()?null:current.get(current.size()-1).getResourcePackName();
      for(int i=0;i<entries.size();i++)if(entries.get(i).getResourcePackName().equals(active))applied=i+1;
      preview=applied;
      first=Math.max(0,applied-5);
    }
    int size() {return entries.size()+1;}
    int visibleSlots(float width) {return Math.max(1,(int)(width/30)-1);}
    void focus(boolean focus) {
      if(focused && !focus) {preview=applied;first=Math.max(0,Math.min(first,applied));}
      focused=focus;
    }
    void move(int delta,int visible) {
      int old=preview;preview=Math.max(0,Math.min(size()-1,preview+delta));
      if(preview<first)first=preview;
      if(preview>=first+visible)first=preview-visible+1;
      if(old!=preview)dev.legacyconsole.client.ConsoleAudio.play("focus");
    }
    String name() {return preview==0?"Default":entries.get(preview-1).getResourcePackName();}
    boolean enabled(int index){return index==0?selected.ids().isEmpty():selected.contains(entries.get(index-1).getResourcePackName());}
    String description() {
      if(preview==0)return selected.ids().isEmpty()?"The classic Minecraft textures, icons and user interface!":"Select Default to clear the selected texture packs.";
      String id=entries.get(preview-1).getResourcePackName();int priority=selected.priority(id);
      String status=priority==0?"Not selected. Select to add above the other packs.":"Selected: priority "+priority+" of "+selected.ids().size()+". Higher numbers override lower numbers. Select again to remove.";
      return status+"\n\n"+entries.get(preview-1).getTexturePackDescription();
    }
    void icon(int index,float x,float y,float width,float height) {
      Minecraft mc=Minecraft.getInstance();
      if(index==0) {
        try {
          if(defaultIcon==null)defaultIcon=mc.getTextureManager().register(
              "console_default_pack",new DynamicTexture(NativeImages.fromBufferedImage(PackRepositoryBridge.get().rprDefaultResourcePack.getPackImage())));
          PortGraphics.bind(defaultIcon);
        } catch(IOException error) {
          PortGraphics.bind(new ResourceLocation("textures/misc/unknown_pack.png"));
        }
      } else entries.get(index-1).bindTexturePackIcon(mc.getTextureManager());
        GlStateManager.color(1,1,1,1);
        GlStateManager.pushMatrix();GlStateManager.translate(x,y,0);
        GlStateManager.scale(width,height,1);
        Gui.drawModalRectWithCustomSizedTexture(0,0,0,0,1,1,1,1);
        GlStateManager.popMatrix();
    }
    void sidebar(float x,float y,float width,float height,boolean create) {
      LceWidgets.panel("pointer_panel",x,y,width,height);
      // Bind after panel construction so the very first frame cannot sample the panel texture.
      icon(preview,x+13,y+7,32,32);
      Minecraft mc=Minecraft.getInstance();
      float ty=y+7;
      for(String line:FontCompat.get().listFormattedStringToWidth(name(),(int)width-61)) {
        if(ty>=y+39)break;
        LceWidgets.label(line,x+48,ty,ConsoleTheme.TEXT);ty+=9;
      }
      ty=y+43;
      float comparison=create?134:128;
      for(String line:FontCompat.get().listFormattedStringToWidth(description(),(int)width-26)) {
        if(ty+9>y+comparison-4)break;
        LceWidgets.label(line,x+13,ty,ConsoleTheme.TEXT);ty+=9;
      }
      if(preview==0)ConsoleTheme.spriteAt("lce/widgets/default_pack_comparison",x+13,y+comparison,145,72);
    }
    void activate(Screen parent) {
      final int choice=preview;
      if(choice==0&&selected.ids().isEmpty())return;
      Runnable apply=()-> {
        Minecraft mc=Minecraft.getInstance();
        if(choice==0)selected.clear();else selected.toggle(entries.get(choice-1).getResourcePackName());
        java.util.List<PackRepositoryBridge.Entry> active=new java.util.ArrayList<>();
        for(String id:selected.ids())for(PackRepositoryBridge.Entry entry:entries)if(entry.getResourcePackName().equals(id)){active.add(entry);break;}
        applied=0;
        if(!selected.ids().isEmpty())for(int i=0;i<entries.size();i++)if(entries.get(i).getResourcePackName().equals(selected.ids().get(selected.ids().size()-1)))applied=i+1;
        preview=choice;
        applyPacks(active);
        if(mc.screen!=parent)mc.setScreen(parent);
      };
      if(choice>0 && !enabled(choice) && entries.get(choice-1).getPackFormat()!=7)
        Minecraft.getInstance().setScreen(new ConsoleConfirmScreen(parent,"Texture Pack Version",
            "This pack targets another Minecraft resource format. Apply it?",apply));
      else apply.run();
    }
  }

  private static void applyPacks(java.util.List<PackRepositoryBridge.Entry> active){
    Minecraft mc=Minecraft.getInstance();PackRepositoryBridge.get().setRepositories(active);
    mc.options.resourcePacks.clear();mc.options.incompatibleResourcePacks.clear();
    for(PackRepositoryBridge.Entry entry:active){
      mc.options.resourcePacks.add(entry.getResourcePackName());
      if(entry.getPackFormat()!=7)mc.options.incompatibleResourcePacks.add(entry.getResourcePackName());
    }
    mc.options.save();mc.reloadResourcePacks();
  }

  static void preview(int x, int y, int w, int h) {
    Minecraft mc = Minecraft.getInstance();
    ConsoleTheme.sprite("gui/sprites/tiles/panel_recess", x, y, w, h);
    List<PackRepositoryBridge.Entry> entries =
        PackRepositoryBridge.get().getRepositoryEntries();
    String name = "Default", description = "The classic look of Minecraft.";
    if (!entries.isEmpty()) {
      PackRepositoryBridge.Entry entry = entries.get(entries.size() - 1);
      entry.bindTexturePackIcon(mc.getTextureManager());
      name = entry.getResourcePackName();
      description = entry.getTexturePackDescription();
    } else {
      try {
        if (defaultIcon == null)
          defaultIcon =
              mc.getTextureManager()
                  .register(
                      "console_default_pack",
                      new DynamicTexture(
                          NativeImages.fromBufferedImage(PackRepositoryBridge.get().rprDefaultResourcePack.getPackImage())));
        PortGraphics.bind(defaultIcon);
      } catch (IOException e) {
        PortGraphics.bind(new ResourceLocation("textures/misc/unknown_pack.png"));
      }
    }
    GlStateManager.color(1, 1, 1, 1);
    Gui.drawModalRectWithCustomSizedTexture(x + (w - 32) / 2, y + 7, 0, 0, 32, 32, 32, 32);
    ConsoleTheme.centered(ConsoleTheme.fit(name, w - 10), x + w / 2, y + 46, ConsoleTheme.DARK);
    FontCompat.get().drawSplitString(description, x + 7, y + 61, w - 14, ConsoleTheme.DARK);
  }
}

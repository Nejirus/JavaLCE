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

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;



/** Original LoadOrJoinMenu720/1080 display lists, on the existing 640x360 canvas. */
final class LcePlayLayout {
  private static final JsonObject SOURCE;
  static {
    try (Reader reader=new InputStreamReader(LcePlayLayout.class.getResourceAsStream(
        "/assets/legacyconsole/play_layout.json"),StandardCharsets.UTF_8)) {
      SOURCE=new JsonParser().parse(reader).getAsJsonObject();
    } catch(Exception error) {throw new ExceptionInInitializerError(error);}
  }

  final float scale, rowHeight, iconSize;
  final int rows;
  final boolean hd;
  final float[][] panels=new float[2][], lists=new float[2][], titles=new float[2][];
  final float[] background, noGames;
  private final JsonObject source;
  private final float dx,dy;

  LcePlayLayout(int width,int height,int displayHeight) {
    hd=displayHeight>=1080;
    source=SOURCE.getAsJsonObject("scenes").getAsJsonObject(hd?"1080":"720");
    scale=Math.min(1,Math.min(width/640f,height/360f));
    dx=(width-640*scale)/2;dy=(height-360*scale)/2;
    // The console-only storage meter is absent; repeat the source top inset below the lists.
    background=rect("background_without_storage_footer");
    panels[0]=rect("left_panel");panels[1]=rect("right_panel");
    lists[0]=rect("left_list");lists[1]=rect("right_list");
    titles[0]=rect("left_title");titles[1]=rect("right_title");
    noGames=rect("no_games");
    rows=SOURCE.get("max_visible").getAsInt();
    rowHeight=source.get("row_height").getAsFloat()*scale;
    iconSize=source.get("icon_size").getAsFloat()*scale;
  }

  private float[] rect(String name) {
    JsonArray values=source.getAsJsonArray(name);
    float[] result=new float[values.size()];
    for(int i=0;i<result.length;i++)result[i]=values.get(i).getAsFloat()*scale+(i==0?dx:i==1?dy:0);
    return result;
  }

  <T extends GuiButton> T place(T button,int column,int row) {
    return LceWidgets.place(button,lists[column][0],lists[column][1]+row*rowHeight,
        lists[column][2],rowHeight);
  }

  private float rowScale(int column) {
    float nativeWidth=source.getAsJsonObject("bitmap_sizes").getAsJsonArray("list_button").get(0).getAsFloat();
    return lists[column][2]/scale/(nativeWidth/(hd?3:2));
  }
  float iconX(int column) {return source.get("icon_x").getAsFloat()*rowScale(column)*scale;}
  float iconY() {return source.get("icon_y").getAsFloat()*scale;}
  float textX(int column) {return source.get("text_x").getAsFloat()*rowScale(column)*scale;}

  float alpha(boolean focused,long elapsedNanos) {
    JsonArray frames=source.getAsJsonObject("fade").getAsJsonArray(focused?"FadeIn":"FadeOut");
    int index=(int)Math.min(frames.size()-1,Math.max(0,elapsedNanos*30/1_000_000_000L));
    return frames.get(index).getAsFloat();
  }

  String image(String name) {return "lce/play/"+(hd?"hd/":"sd/")+name;}
  static String label(String id) {return SOURCE.getAsJsonObject("strings").get(id).getAsString();}
}

package dev.legacyconsole.network;

import dev.legacyconsole.crafting.*;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public final class Ps4BannerCraftRequest {
  private final int window,batches;
  private final ItemStack banner;
  private final List<Ps4BannerPatterns.Layer> layers;
  public Ps4BannerCraftRequest(int window,ItemStack banner,List<Ps4BannerPatterns.Layer> layers,int batches) {
    this.window=window;this.banner=Ps4BannerPatterns.unit(banner);this.layers=List.copyOf(layers);this.batches=batches;
  }
  public Ps4BannerCraftRequest(FriendlyByteBuf buffer) {
    window=buffer.readInt();batches=buffer.readUnsignedByte();banner=buffer.readItem();
    int count=buffer.readUnsignedByte();if(count<1||count>6)throw new IllegalArgumentException("Invalid banner layer count");
    List<Ps4BannerPatterns.Layer> list=new ArrayList<>();
    for(int i=0;i<count;i++)list.add(new Ps4BannerPatterns.Layer(buffer.readUtf(16),buffer.readUnsignedByte(),buffer.readUtf(80)));
    layers=List.copyOf(list);
  }
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeInt(window);buffer.writeByte(batches);buffer.writeItem(banner);buffer.writeByte(layers.size());
    for(var layer:layers){buffer.writeUtf(layer.recipe(),16);buffer.writeByte(layer.color());buffer.writeUtf(layer.dye(),80);}
  }
  public static void handle(Ps4BannerCraftRequest packet,Supplier<NetworkEvent.Context> context) {
    var ctx=context.get();ctx.enqueueWork(() -> {
      var player=ctx.getSender();
      if(player==null || !(player.containerMenu instanceof ConsoleContainer menu))return;
      long tick=player.level.getGameTime();
      if(menu.containerId!=packet.window || menu.lastCraftTick==tick || !menu.stillValid(player))return;
      menu.lastCraftTick=tick;
      Ps4BannerCrafting.craft(player,menu,packet.banner,packet.layers,packet.batches);
    });ctx.setPacketHandled(true);
  }
}

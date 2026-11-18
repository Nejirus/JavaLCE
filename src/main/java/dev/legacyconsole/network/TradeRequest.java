package dev.legacyconsole.network;
import dev.legacyconsole.crafting.NativeTrading;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import java.util.*;import java.util.function.Supplier;
public final class TradeRequest {
 private final int window,index;private static final Map<ServerPlayer,Long> LAST=new WeakHashMap<>();
 public TradeRequest(int window,int index){this.window=window;this.index=index;}public TradeRequest(FriendlyByteBuf b){this(b.readInt(),b.readUnsignedShort());}
 public void encode(FriendlyByteBuf b){b.writeInt(window);b.writeShort(index);}
 public static void handle(TradeRequest m,Supplier<NetworkEvent.Context> ctx){var c=ctx.get();c.enqueueWork(()->{var p=c.getSender();if(p==null||!(p.containerMenu instanceof MerchantMenu menu)||menu.containerId!=m.window)return;long tick=p.level.getGameTime();if(Objects.equals(LAST.get(p),tick))return;LAST.put(p,tick);NativeTrading.trade(p,menu,m.index);});c.setPacketHandled(true);}
}

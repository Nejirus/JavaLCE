package dev.legacyconsole.network;
import dev.legacyconsole.crafting.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import java.util.function.Supplier;
public final class CraftRequest {
 private final int window,batches;private final long recipe;
 public CraftRequest(int window,long recipe,int batches){this.window=window;this.recipe=recipe;this.batches=batches;}
 public CraftRequest(FriendlyByteBuf buf){this(buf.readInt(),buf.readLong(),buf.readUnsignedByte());}
 public void encode(FriendlyByteBuf b){b.writeInt(window);b.writeLong(recipe);b.writeByte(batches);}
 public static void handle(CraftRequest m,Supplier<NetworkEvent.Context> ctx){var c=ctx.get();c.enqueueWork(()->{var p=c.getSender();if(p==null||!(p.containerMenu instanceof ConsoleContainer menu)||m.batches<1||m.batches>64)return;long tick=p.level.getGameTime();if(menu.containerId!=m.window||menu.lastCraftTick==tick||!menu.stillValid(p))return;menu.lastCraftTick=tick;NativeCrafting.craft(p,menu,RecipeCatalog.get(m.recipe,p.level,menu),m.batches);});c.setPacketHandled(true);}
}

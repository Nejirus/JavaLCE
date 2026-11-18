package dev.legacyconsole.network;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
public record HostState(CompoundTag state){
 public HostState(FriendlyByteBuf b){this(b.readNbt());}
 public void encode(FriendlyByteBuf b){b.writeNbt(state);}
 public static void handle(HostState p,Supplier<NetworkEvent.Context> context){var c=context.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->dev.legacyconsole.client.ui.ConsoleHostOptions.receive(p.state)));c.setPacketHandled(true);}
}

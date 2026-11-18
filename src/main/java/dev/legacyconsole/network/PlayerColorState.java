package dev.legacyconsole.network;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
public record PlayerColorState(CompoundTag state){
 public PlayerColorState(FriendlyByteBuf b){this(b.readNbt());}
 public void encode(FriendlyByteBuf b){b.writeNbt(state);}
 public static void handle(PlayerColorState p,Supplier<NetworkEvent.Context> context){var c=context.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->dev.legacyconsole.client.ClientPlayerColors.receive(p.state)));c.setPacketHandled(true);}
}

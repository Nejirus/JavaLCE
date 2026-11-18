package dev.legacyconsole.network;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
public record HostRequest(int action,int value,UUID target){
 public HostRequest(FriendlyByteBuf b){this(b.readVarInt(),b.readVarInt(),b.readBoolean()?b.readUUID():null);}
 public void encode(FriendlyByteBuf b){b.writeVarInt(action);b.writeVarInt(value);b.writeBoolean(target!=null);if(target!=null)b.writeUUID(target);}
 public static void handle(HostRequest p,Supplier<NetworkEvent.Context> context){var c=context.get();var sender=c.getSender();c.enqueueWork(()->{if(sender!=null)dev.legacyconsole.world.HostOptions.request(sender,p.action,p.value,p.target);});c.setPacketHandled(true);}
}

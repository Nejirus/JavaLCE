package dev.legacyconsole.network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import java.util.function.Supplier;
public final class TutorialInput {
 public final int action,mask,context;
 public TutorialInput(int action,int mask,int context){this.action=action;this.mask=mask;this.context=context;}
 public TutorialInput(FriendlyByteBuf b){action=b.readUnsignedByte();mask=b.readVarInt();context=b.readVarInt();}
 public void encode(FriendlyByteBuf b){b.writeByte(action);b.writeVarInt(mask);b.writeVarInt(context);}
 public static void handle(TutorialInput packet,Supplier<NetworkEvent.Context> context){var c=context.get();var player=c.getSender();c.enqueueWork(()->{if(player!=null&&packet.action>=0&&packet.action<=4)dev.legacyconsole.tutorial.Ps4TutorialWorld.get(player.getLevel()).input(player,packet.action,packet.mask,packet.context);});c.setPacketHandled(true);}
}

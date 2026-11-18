package dev.legacyconsole.network;
import dev.legacyconsole.LegacyConsole;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import java.util.function.Supplier;
public final class OpenCraftingRequest {
 public OpenCraftingRequest(){}public OpenCraftingRequest(FriendlyByteBuf buf){}
 public void encode(FriendlyByteBuf buf){}
 public static void handle(OpenCraftingRequest m,Supplier<NetworkEvent.Context> ctx){var c=ctx.get();c.enqueueWork(()->{var p=c.getSender();if(p!=null&&p.isAlive()&&!p.isSpectator()&&p.containerMenu==p.inventoryMenu)LegacyConsole.openCrafting(p,false,p.blockPosition());});c.setPacketHandled(true);}
}

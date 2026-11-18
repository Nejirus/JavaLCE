package dev.legacyconsole.joincompat.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
import java.util.*;import io.netty.buffer.Unpooled;
import net.minecraft.network.*;import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.*;import net.minecraftforge.registries.*;
/** Preserve full Forge synchronization; omit only our unused menu for peers lacking our mod. */
@Mixin(value=FMLHandshakeHandler.class,remap=false)
public abstract class HandshakeMixin {
 @Shadow @Final private Connection manager;
 @Shadow private int packetPosition;
 @Shadow private List<NetworkRegistry.LoginPayload> messageList;
 @Unique private int legacy$filtered=-1;
 @Inject(method="tickServer",at=@At("HEAD"),cancellable=true,remap=false)
 private void legacy$optionalMenu(CallbackInfoReturnable<Boolean> ci){
  if(manager.isMemoryConnection()||NetworkHooks.isVanillaConnection(manager)||packetPosition>=messageList.size()||packetPosition==legacy$filtered)return;
  NetworkRegistry.LoginPayload payload=messageList.get(packetPosition);
  if(!payload.getMessageContext().equals("Registry "+ForgeRegistries.CONTAINERS.getRegistryName()))return;
  FMLConnectionData remote=NetworkHooks.getConnectionData(manager);
  // Mod-list negotiation precedes registry messages. Wait for that reply before choosing a snapshot.
  if(remote==null){ci.setReturnValue(false);return;}
  if(remote.getModList().contains("legacyconsole"))return;
  FriendlyByteBuf input=new FriendlyByteBuf(payload.getData().duplicate());int discriminator=input.readVarInt();ResourceLocation name=input.readResourceLocation();if(!input.readBoolean())return;
  ForgeRegistry.Snapshot snapshot=ForgeRegistry.Snapshot.read(input);
  ResourceLocation ownMenu=new ResourceLocation("legacyconsole","crafting");
  if(!snapshot.ids.containsKey(ownMenu))return;snapshot.ids.remove(ownMenu);snapshot.aliases.remove(ownMenu);snapshot.dummied.remove(ownMenu);snapshot.overrides.remove(ownMenu);
  FriendlyByteBuf output=new FriendlyByteBuf(Unpooled.buffer());output.writeVarInt(discriminator);output.writeResourceLocation(name);output.writeBoolean(true);output.writeBytes(snapshot.getPacketData());
  messageList.set(packetPosition,new NetworkRegistry.LoginPayload(output,payload.getChannelName(),payload.getMessageContext()));legacy$filtered=packetPosition;
 }
}

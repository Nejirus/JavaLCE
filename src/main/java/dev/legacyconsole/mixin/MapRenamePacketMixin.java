package dev.legacyconsole.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.server.network.ServerGamePacketListenerImpl;import net.minecraft.server.level.ServerPlayer;import net.minecraft.network.protocol.PacketUtils;import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;import dev.legacyconsole.port.RenameMapMenu;
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MapRenamePacketMixin {
 @Shadow public ServerPlayer player;
 @Inject(method="handleRenameItem",at=@At("HEAD"),cancellable=true)private void legacy$rename(ServerboundRenameItemPacket packet,CallbackInfo ci){
  if(!(player.containerMenu instanceof RenameMapMenu menu))return;
  PacketUtils.ensureRunningOnSameThread(packet,(ServerGamePacketListenerImpl)(Object)this,player.getLevel());
  if(player.containerMenu.stillValid(player))menu.legacy$setMapName(packet.getName());else player.closeContainer();ci.cancel();
 }
}

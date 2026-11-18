package dev.legacyconsole.network;
import dev.legacyconsole.LegacyConsole;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.*;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
public final class ConsoleNetwork {
 private final SimpleChannel channel=NetworkRegistry.newSimpleChannel(new ResourceLocation(LegacyConsole.ID,"main"),()->"8",s->s.equals("8"),s->s.equals("8")||JoinCompatibility.enabled()&&(s.equals(NetworkRegistry.ABSENT)||s.equals(NetworkRegistry.ACCEPTVANILLA)));
 public void register(){
  channel.registerMessage(10,PlayerColorState.class,PlayerColorState::encode,PlayerColorState::new,PlayerColorState::handle,java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  channel.registerMessage(8,HostRequest.class,HostRequest::encode,HostRequest::new,HostRequest::handle,java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER));
  channel.registerMessage(9,HostState.class,HostState::encode,HostState::new,HostState::handle,java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  channel.registerMessage(0,OpenCraftingRequest.class,OpenCraftingRequest::encode,OpenCraftingRequest::new,OpenCraftingRequest::handle,java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER));
  channel.registerMessage(1,CraftRequest.class,CraftRequest::encode,CraftRequest::new,CraftRequest::handle,java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER));
  channel.registerMessage(2,DynamicCraftRequest.class,DynamicCraftRequest::encode,DynamicCraftRequest::new,DynamicCraftRequest::handle,java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER));
  channel.registerMessage(4,LegacyWorldProfile.class,LegacyWorldProfile::encode,LegacyWorldProfile::new,LegacyWorldProfile::handle,java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  channel.registerMessage(3,TradeRequest.class,TradeRequest::encode,TradeRequest::new,TradeRequest::handle,java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER));
  channel.registerMessage(5,Ps4BannerCraftRequest.class,Ps4BannerCraftRequest::encode,Ps4BannerCraftRequest::new,Ps4BannerCraftRequest::handle,java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER));
  channel.registerMessage(6,TutorialState.class,TutorialState::encode,TutorialState::new,TutorialState::handle,java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  channel.registerMessage(7,TutorialInput.class,TutorialInput::encode,TutorialInput::new,TutorialInput::handle,java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER));
 }
 public void sendTo(net.minecraft.server.level.ServerPlayer player,Object packet){channel.send(PacketDistributor.PLAYER.with(()->player),packet);}
 public boolean present(net.minecraft.network.Connection connection){return channel.isRemotePresent(connection);}
 public boolean present(net.minecraft.server.level.ServerPlayer player){return channel.isRemotePresent(player.connection.connection);}
 public void sendToServer(Object packet){channel.sendToServer(packet);}
}

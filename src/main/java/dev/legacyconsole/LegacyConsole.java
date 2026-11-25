package dev.legacyconsole;
import dev.legacyconsole.crafting.ConsoleContainer;
import dev.legacyconsole.network.ConsoleNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import net.minecraftforge.registries.*;
import net.minecraftforge.fmllegacy.RegistryObject;
import org.apache.logging.log4j.*;
@Mod(LegacyConsole.ID)
public final class LegacyConsole {
 public static final String ID="legacyconsole",VERSION="1.0.4";
 public static final Logger log=LogManager.getLogger("LegacyConsole");
 public static final ConsoleNetwork NETWORK=new ConsoleNetwork();
 private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(ForgeRegistries.CONTAINERS,ID);
 public static final RegistryObject<MenuType<ConsoleContainer>> CRAFTING=MENUS.register("crafting",()->IForgeContainerType.create(ConsoleContainer::client));
 public LegacyConsole(){
  net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.fml.IExtensionPoint.DisplayTest.class,()->new net.minecraftforge.fml.IExtensionPoint.DisplayTest(()->dev.legacyconsole.network.JoinCompatibility.enabled()?net.minecraftforge.fmllegacy.network.FMLNetworkConstants.IGNORESERVERONLY:VERSION,(remote,isServer)->VERSION.equals(remote)||net.minecraftforge.fmllegacy.network.FMLNetworkConstants.IGNORESERVERONLY.equals(remote)));
  dev.legacyconsole.worldgen.LegacyWorldgen.register();
  MinecraftForge.EVENT_BUS.register(new dev.legacyconsole.worldgen.LegacyWorldgen.Bounds());
  MENUS.register(FMLJavaModLoadingContext.get().getModEventBus());NETWORK.register();
  MinecraftForge.EVENT_BUS.register(this);
  MinecraftForge.EVENT_BUS.register(new dev.legacyconsole.world.WorldPolicy.Enforcer());
  MinecraftForge.EVENT_BUS.register(new dev.legacyconsole.world.HostOptions.Events());
  MinecraftForge.EVENT_BUS.register(new dev.legacyconsole.world.Ps4PlayerColors.Events());
  MinecraftForge.EVENT_BUS.register(new dev.legacyconsole.world.SignInteraction());
  MinecraftForge.EVENT_BUS.register(new dev.legacyconsole.tutorial.Ps4TutorialWorld.Events());
  DistExecutor.safeRunWhenOn(Dist.CLIENT,()->dev.legacyconsole.client.ClientProxy::register);
 }
 public static void openCrafting(ServerPlayer player,boolean table,BlockPos pos){
  openCrafting(player,table?ConsoleContainer.TABLE:ConsoleContainer.PERSONAL,pos);
 }
 public static void openCrafting(ServerPlayer player,int kind,BlockPos pos){
  dev.legacyconsole.crafting.MapCrafting.syncOwned(player);
  NetworkHooks.openGui(player,new SimpleMenuProvider((id,inventory,p)->new ConsoleContainer(id,inventory,kind,pos),new TextComponent("Crafting")),data->{data.writeByte(kind);data.writeBlockPos(pos);});
 }
 @SubscribeEvent public void recipesReloaded(net.minecraftforge.event.OnDatapackSyncEvent event){dev.legacyconsole.crafting.RecipeCatalog.invalidate();}
 @SubscribeEvent public void openTable(PlayerInteractEvent.RightClickBlock event){
  if(event.getPlayer() instanceof ServerPlayer remote&&dev.legacyconsole.network.JoinCompatibility.enabled()&&!NETWORK.present(remote))return;
  if(event.getPlayer().isShiftKeyDown()||event.getPlayer().isSpectator())return;
  var state=event.getWorld().getBlockState(event.getPos());
  int kind=state.is(Blocks.CRAFTING_TABLE)?ConsoleContainer.TABLE:state.is(Blocks.STONECUTTER)?ConsoleContainer.STONECUTTER:state.is(Blocks.LOOM)?ConsoleContainer.LOOM:-1;
  if(kind>=0){
   event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
   if(event.getPlayer() instanceof ServerPlayer p)openCrafting(p,kind,event.getPos());
  }
 }
}

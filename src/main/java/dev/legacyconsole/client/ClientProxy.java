package dev.legacyconsole.client;
import dev.legacyconsole.LegacyConsole;import dev.legacyconsole.client.ui.*;import dev.legacyconsole.client.input.*;
import net.minecraft.client.*;import net.minecraft.client.gui.screens.*;import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.worldselection.*;import net.minecraft.client.gui.screens.multiplayer.*;

import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.MenuScreens;import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.network.chat.*;
import net.minecraftforge.common.MinecraftForge;import net.minecraftforge.eventbus.api.*;import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.*;import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import dev.legacyconsole.port.*;
import java.lang.reflect.*;
public final class ClientProxy {
 private static final KeyMapping CRAFTING=new KeyMapping("key.legacyconsole.crafting",org.lwjgl.glfw.GLFW.GLFW_KEY_C,"key.categories.legacyconsole");
 private static final KeyMapping SETTINGS=new KeyMapping("key.legacyconsole.settings",org.lwjgl.glfw.GLFW.GLFW_KEY_F8,"key.categories.legacyconsole");
 private final ConsoleAudio audio=ConsoleAudio.shared();
 public ClientProxy(){MinecraftForge.EVENT_BUS.register(audio);}private final MenuHoverSound hover=new MenuHoverSound();
 public static void register(){ClientConfig.load(FMLPaths.CONFIGDIR.get().toFile());FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientProxy::setup);FMLJavaModLoadingContext.get().getModEventBus().addListener(LceMapRenderType::registerShaders);}
 private static void setup(FMLClientSetupEvent event){event.enqueueWork(()->{ClientRegistry.registerKeyBinding(CRAFTING);ClientRegistry.registerKeyBinding(SETTINGS);MenuScreens.register((net.minecraft.world.inventory.MenuType)LegacyConsole.CRAFTING.get(), (MenuScreens.ScreenConstructor)(menu,inventory,title)->((dev.legacyconsole.crafting.ConsoleContainer)menu).kind==dev.legacyconsole.crafting.ConsoleContainer.LOOM?new ConsoleSpecialCrafting((dev.legacyconsole.crafting.ConsoleContainer)menu,5):new ConsoleCraftingScreen((dev.legacyconsole.crafting.ConsoleContainer)menu));MinecraftForge.EVENT_BUS.register(new ClientProxy());MinecraftForge.EVENT_BUS.register(new ConsoleHud());MinecraftForge.EVENT_BUS.register(new Ps4Hints.Events());});}
 @SubscribeEvent public void recipesReloaded(net.minecraftforge.client.event.RecipesUpdatedEvent event){dev.legacyconsole.crafting.RecipeCatalog.invalidate();}
 @SubscribeEvent public void tick(TickEvent.ClientTickEvent event){if(event.phase!=TickEvent.Phase.START)return;Minecraft mc=Minecraft.getInstance();
  Mouse.frame();ControllerCursor.mouseEvent();
  if(dev.legacyconsole.client.loading.ConsoleStartup.active()){ControllerInput.INSTANCE.tick();return;}
  if(mc.options.tutorialStep!=net.minecraft.client.tutorial.TutorialSteps.NONE)mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
  ClientConfig.applyResolutionDefaults();audio.tick();ConsoleWorldLaunch.tick(mc);ControllerInput.INSTANCE.tick();ConsoleShield.releaseIfUnavailable();
  if(mc.player!=null&&!(mc.player.input instanceof ConsoleMovement))mc.player.input=new ConsoleMovement(mc.player.input);
  while(CRAFTING.consumeClick())if(mc.player!=null&&mc.screen==null&&!mc.player.isSpectator())ConsoleScreens.craftingOrCreative();
  while(SETTINGS.consumeClick())if(!(mc.screen instanceof ConsoleSettingsScreen))mc.setScreen(new ConsoleSettingsScreen(mc.screen instanceof AbstractContainerScreen?null:mc.screen));
 }
 @SubscribeEvent(priority=EventPriority.LOWEST) public void frame(TickEvent.RenderTickEvent event){if(event.phase==TickEvent.Phase.START){dev.legacyconsole.client.loading.ConsoleStartup.confirmBlocked();dev.legacyconsole.client.loading.ConsoleLoading.beginFrame();ControllerInput.INSTANCE.camera();}else{ControllerInput.INSTANCE.framePresented();dev.legacyconsole.client.loading.ConsoleLoading.render();ConsoleDisplayGamma.present();}}
 private static <T> T field(Object owner,Class<T> type){for(Class<?> c=owner.getClass();c!=null;c=c.getSuperclass())for(Field f:c.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())&&type.isAssignableFrom(f.getType()))try{f.setAccessible(true);return type.cast(f.get(owner));}catch(IllegalAccessException e){throw new IllegalStateException(e);}return null;}
 private static Screen parent(Screen source){Screen parent=field(source,Screen.class);return parent==null?Minecraft.getInstance().screen:parent;}
 @SubscribeEvent public void screen(GuiOpenEvent event){Minecraft mc=Minecraft.getInstance();Screen screen=event.getGui();hover.reset();ControllerMenus.reset();ControllerCursor.screenChanged();
  if(screen==null&&mc.screen!=null)ControllerInput.INSTANCE.blockAfterMenu();else if(screen!=null)ControllerInput.INSTANCE.menuOpened();
  if(screen==null||!ClientConfig.consoleMenus||screen instanceof LegacyScreen||screen instanceof GuiContainer)return;
  Screen replacement=null;
  if(OptifineMenus.handles(screen,mc.screen))replacement=OptifineMenus.wrap(screen,mc.screen);
  else if(screen instanceof TitleScreen)replacement=new ConsoleMainMenu();
  else if(screen instanceof PauseScreen)replacement=new ConsolePauseMenu();
  else if(screen instanceof InBedChatScreen)replacement=new ConsoleSleepScreen();
  else if(screen instanceof ChatScreen)replacement=new ConsoleChatScreen("");
  else if(screen instanceof DeathScreen)replacement=new ConsoleDeathScreen(field(screen,Component.class));
  else if(screen instanceof net.minecraft.client.gui.screens.OptionsScreen)replacement=new ConsoleSettingsScreen(parent(screen));
  else if(screen instanceof SelectWorldScreen)replacement=new ConsolePlayScreen(parent(screen),0);
  else if(screen instanceof JoinMultiplayerScreen)replacement=new ConsolePlayScreen(parent(screen),2);
  else if(screen instanceof net.minecraft.client.gui.screens.worldselection.CreateWorldScreen)replacement=new ConsoleCreateWorld(parent(screen));
  else if(screen instanceof InventoryScreen||screen instanceof CreativeModeInventoryScreen)replacement=new ConsoleInventory(mc.player);
  else if(screen instanceof MerchantScreen merchant)replacement=new ConsoleMerchantScreen(merchant);
  else if(screen instanceof SignEditScreen)replacement=new ConsoleSignScreen(field(screen,SignBlockEntity.class));
  else if(screen instanceof EnchantmentScreen)replacement=new Ps4EnchantingScreen((AbstractContainerScreen<?>)screen);
  else if(screen instanceof AnvilScreen)replacement=new Ps4AnvilScreen((AbstractContainerScreen<?>)screen);
  else if(screen instanceof AbstractFurnaceScreen<?>)replacement=new Ps4FurnaceScreen((AbstractContainerScreen<?>)screen);
  else if(screen instanceof BeaconScreen)replacement=new Ps4BeaconScreen((AbstractContainerScreen<?>)screen);
  else if(screen instanceof CartographyTableScreen)replacement=new Ps4CartographyScreen((AbstractContainerScreen<?>)screen);
  else if(screen instanceof BrewingStandScreen)replacement=new ConsoleBrewingScreen((AbstractContainerScreen<?>)screen);
  else if(screen instanceof ContainerScreen chest&&(chest.getMenu().getRowCount()==3||chest.getMenu().getRowCount()==6))replacement=new Ps4ChestScreen(chest);
  else if(screen instanceof LoomScreen||screen instanceof CartographyTableScreen||screen instanceof StonecutterScreen||screen instanceof GrindstoneScreen||screen instanceof SmithingScreen)replacement=new ConsoleWorkstationScreen((AbstractContainerScreen<?>)screen);
  else if(screen instanceof AbstractContainerScreen<?> container)replacement=new ConsoleStorageScreen(container);
  if(replacement!=null)event.setGui(replacement);
 }
 @SubscribeEvent public void logout(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent event){LegacyWorldView.clear();Ps4Hints.clear();ConsoleHostOptions.clear();ClientPlayerColors.clear();}
 @SubscribeEvent public void mouseClicked(GuiScreenEvent.MouseClickedEvent.Pre event){ControllerCursor.mouse();}
 @SubscribeEvent public void mouseScrolled(GuiScreenEvent.MouseScrollEvent.Pre event){ControllerCursor.mouse();}
 @SubscribeEvent public void loadingKey(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event){int key=event.getKeyCode();if((key==org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER||key==org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE)&&dev.legacyconsole.client.loading.ConsoleStartup.confirmBlocked())event.setCanceled(true);else if(key==org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE&&dev.legacyconsole.client.loading.ConsoleLoading.cancelConnection())event.setCanceled(true);}
 @SubscribeEvent public void drawn(GuiScreenEvent.DrawScreenEvent.Post event){ControllerCursor.draw(event.getMouseX(),event.getMouseY());int mx=ControllerMenus.renderMouseX(event.getGui(),event.getMouseX()),my=ControllerMenus.renderMouseY(event.getGui(),event.getMouseY());int id=MenuHoverSound.NONE;for(GuiButton b:LegacyWidgets.buttons(event.getGui()))if(b.enabled&&b.visible&&ConsoleTheme.buttonHover(b,mx,my)){id=b.id;break;}if(hover.observe(event.getGui(),id,Mouse.getEventDX()!=0||Mouse.getEventDY()!=0))ConsoleAudio.play("focus");}
}

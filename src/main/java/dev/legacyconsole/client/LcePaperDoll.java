package dev.legacyconsole.client;
import net.minecraft.client.Minecraft;import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
/** Source Gui.cpp's fullscreen anchor and 12-pixel model scale, using the native player renderer. */
public final class LcePaperDoll {
 private static LcePaperDollState state=new LcePaperDollState();private static LocalPlayer previous;private static boolean visible;
 public static boolean visible(){return visible;}
 public static void render(){Minecraft mc=Minecraft.getInstance();LocalPlayer player=mc.player;if(player!=previous){previous=player;state=new LcePaperDollState();}boolean gui=ClientConfig.consoleHud&&ClientConfig.animatedCharacter&&player!=null&&mc.screen==null&&!mc.options.hideGui&&!mc.options.renderDebug&&!player.isSpectator();visible=state.frame(gui,player!=null&&player.isShiftKeyDown(),player!=null&&player.isSprinting(),player!=null&&player.getAbilities().flying);if(visible)LcePaperDollRenderer.render(mc.getWindow().getHeight()>=720?29:51,39,12,player);}
}

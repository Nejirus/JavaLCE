package dev.legacyconsole.port;
import java.io.IOException;
import net.minecraft.client.gui.screens.*;
public class GuiIngameMenu extends LegacyScreen {
 @Override protected void actionPerformed(GuiButton b)throws IOException{
  if(b.id==4)mc.setScreen(null);
  else if(b.id==1){if(mc.level!=null)mc.level.disconnect();mc.clearLevel();mc.setScreen(new dev.legacyconsole.client.ui.ConsoleMainMenu());}
  else if(b.id==6)mc.setScreen(new net.minecraft.client.gui.screens.achievement.StatsScreen(this,mc.player.getStats()));
  else if(b.id==5)mc.setScreen(new net.minecraft.client.gui.screens.advancements.AdvancementsScreen(mc.player.connection.getAdvancements()));
 }
}

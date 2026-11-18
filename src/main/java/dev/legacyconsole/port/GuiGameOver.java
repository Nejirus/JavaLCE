package dev.legacyconsole.port;
import java.io.IOException;import net.minecraft.network.chat.Component;
public class GuiGameOver extends LegacyScreen {
 private int deathDelay;protected final Component causeOfDeath;
 public GuiGameOver(Component cause){causeOfDeath=cause;}
 @Override public void updateScreen(){if(++deathDelay>=20)for(GuiButton b:buttonList)b.enabled=true;}
 @Override protected void actionPerformed(GuiButton b)throws IOException{if(b.id==0){mc.player.respawn();mc.setScreen(null);}}
 @Override public boolean doesGuiPauseGame(){return false;}
}

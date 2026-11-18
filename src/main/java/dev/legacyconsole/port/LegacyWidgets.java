package dev.legacyconsole.port;
import java.util.*;import net.minecraft.client.gui.screens.Screen;import net.minecraft.client.gui.components.*;
public final class LegacyWidgets {
 private static final Map<Screen,List<GuiButton>> CACHE=new WeakHashMap<>();
 public static List<GuiButton> buttons(Screen screen){if(screen==null)return List.of();if(screen instanceof LegacyScreen legacy)return legacy.buttonList;if(screen instanceof GuiContainer container)return container.buttonList;
  List<GuiButton> rows=new ArrayList<>();int id=0;for(var child:screen.children())if(child instanceof AbstractWidget widget)rows.add(new NativeButton(id++,widget));return rows;
 }
 private static final class NativeButton extends GuiButton {final AbstractWidget widget;NativeButton(int id,AbstractWidget w){super(id,w.x,w.y,w.getWidth(),w.getHeight(),w.getMessage().getString());widget=w;enabled=w.active;visible=w.visible;}
 @Override public boolean mousePressed(net.minecraft.client.Minecraft mc,int x,int y){return widget.mouseClicked(x,y,0);}}
}

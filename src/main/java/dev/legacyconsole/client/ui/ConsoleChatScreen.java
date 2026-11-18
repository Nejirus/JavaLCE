package dev.legacyconsole.client.ui;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.ChatScreen;
/** Keep native history, clickable links and suggestions inside the console text field. */
public final class ConsoleChatScreen extends ChatScreen {
 public ConsoleChatScreen(String text){super(text);}
 @Override protected void init(){super.init();input.x=15;input.y=height-19;input.setWidth(width-30);input.setBordered(false);input.setTextColor(-1);}
 @Override public void render(PoseStack pose,int x,int y,float partial){ConsoleTheme.sprite("gui/sprites/widget/text_field_highlighted",8,height-25,width-16,21);super.render(pose,x,y,partial);}
}

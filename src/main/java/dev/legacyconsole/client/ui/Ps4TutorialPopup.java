package dev.legacyconsole.client.ui;

import dev.legacyconsole.client.Ps4Hints;
import dev.legacyconsole.port.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

/** TutorialPopup1080's original pointer panel at (1080,75), 750x315. */
public final class Ps4TutorialPopup {
 private Ps4TutorialPopup(){}
 public static void draw(String title,String text,String prompt,ItemStack item){
  if(item.isEmpty()){var icon=java.util.regex.Pattern.compile("\\{\\*ICON\\*}([0-9]+)\\{\\*/ICON\\*}").matcher(text);if(icon.find())item=dev.legacyconsole.tutorial.Ps4TutorialScript.icon(Integer.parseInt(icon.group(1)));}
  var mc=Minecraft.getInstance();int width=mc.getWindow().getGuiScaledWidth(),height=mc.getWindow().getGuiScaledHeight();
  var content=Ps4Hints.component((title.isEmpty()?"":title+"\n\n")+text+(prompt.isEmpty()?"":"\n\n"+prompt.replace("{*CONTROLLER_VK_A*}","{*JAVA_CONFIRM*}")));
  var lines=Ps4TutorialText.split(content,224);float textHeight=0;for(var line:lines)textHeight+=Ps4TutorialText.lineHeight(line);
  float h=Math.max(315,60+textHeight+(item.isEmpty()?0:60));
  var art=new Ps4UiArt("TutorialPopup",width,height,h);
  GlStateManager.pushMatrix();GlStateManager.translate(0,0,600);
  art.panel("FJ_PointerTextPanel",1080,75,20.834045f,h/36);
  float y=105;for(var line:lines){float lineHeight=Ps4TutorialText.lineHeight(line);var r=art.rect(1118,y+(lineHeight-27)/2,674,lineHeight);GlStateManager.pushMatrix();GlStateManager.translate(r.x(),r.y(),0);GlStateManager.scale(art.scale*3,art.scale*3,1);Ps4TutorialText.draw(line,0,0,0xffffff,true);GlStateManager.popMatrix();y+=lineHeight;}
  if(!item.isEmpty())art.item(item,art.rect(1407,264+h-315,50,50));
  GlStateManager.popMatrix();
 }
 public static void goal(String text){var mc=Minecraft.getInstance();var font=FontCompat.get();font.drawStringWithShadow(text,mc.getWindow().getGuiScaledWidth()-font.getStringWidth(text)-12,mc.getWindow().getGuiScaledHeight()-36,0xffffff);}
}

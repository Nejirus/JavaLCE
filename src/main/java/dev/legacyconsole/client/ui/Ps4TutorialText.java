package dev.legacyconsole.client.ui;

import dev.legacyconsole.client.input.ControllerGlyphs;
import dev.legacyconsole.client.input.ControllerInput;
import dev.legacyconsole.port.GlStateManager;
import dev.legacyconsole.port.PortGraphics;
import java.util.*;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

/** Inline source input tokens use the same layered artwork as the mod's footer. */
public final class Ps4TutorialText {
 // Orbis CMinecraftApp::GetInputReplacement/GetVKReplacement: 45x45, middle aligned.
 // Tutorial text is drawn at three source pixels per Java font unit.
 public static final float GLYPH_HEIGHT=45/3f;
 public static final List<String> GLYPHS=List.of("cross","circle","square","triangle","left_stick","right_stick","left_trigger","right_trigger","left_bumper","right_bumper","left_stick_button","right_stick_button","start_button","back_button");
 private static final Pattern TOKENS=Pattern.compile("\\{\\*([^}]+)\\*}");
 private static final Map<String,String> INPUT=Map.ofEntries(
  Map.entry("CONTROLLER_VK_A","cross"),Map.entry("JAVA_CONFIRM","cross"),Map.entry("CONTROLLER_VK_B","circle"),Map.entry("CONTROLLER_VK_X","square"),Map.entry("CONTROLLER_VK_Y","triangle"),
  Map.entry("CONTROLLER_ACTION_MOVE","left_stick"),Map.entry("CONTROLLER_MENU_NAVIGATE","left_stick"),Map.entry("CONTROLLER_ACTION_LOOK","right_stick"),
  Map.entry("CONTROLLER_ACTION_ACTION","right_trigger"),Map.entry("CONTROLLER_ACTION_USE","left_trigger"),Map.entry("CONTROLLER_ACTION_JUMP","cross"),
  Map.entry("CONTROLLER_ACTION_INVENTORY","triangle"),Map.entry("CONTROLLER_ACTION_CRAFTING","square"),Map.entry("CONTROLLER_ACTION_LEFT_SCROLL","left_bumper"),Map.entry("CONTROLLER_ACTION_RIGHT_SCROLL","right_bumper"),
  Map.entry("CONTROLLER_VK_LB","left_bumper"),Map.entry("CONTROLLER_VK_RB","right_bumper"),Map.entry("CONTROLLER_ACTION_DISMOUNT","right_stick_button"),Map.entry("CONTROLLER_ACTION_MENU_PAGEDOWN","right_trigger"));
 public static ResourceLocation font(String folder,int height){return new ResourceLocation("legacyconsole","tutorial_"+folder+(height>=1080&&!folder.equals("ps3")?"_hd":""));}
 public static float advance(String glyph,LceGlyphLayout.Family family){float[] b=family.glyph(glyph).bounds;return GLYPH_HEIGHT*(b[1]-b[0])/(b[3]-b[2])+2;}
 /** Public pure expansion also lets tests check bindings without a running client. */
 public static MutableComponent component(String source,boolean controller,String folder,int height,Map<String,String> keyboard){
  // Console stripWhitespaceForHtml removes authoring tabs before font layout.
  // Keep the Java popup's explicit paragraph breaks and the source {*B*} tokens.
  String text=source.replace("\r","").replace("\t","").replaceAll("\\{\\*ICON\\*}[0-9]+\\{\\*/ICON\\*}","");
  var result=new TextComponent("");var matches=TOKENS.matcher(text);int end=0;
  while(matches.find()){
   result.append(text.substring(end,matches.start()));String token=matches.group(1);
   if(token.equals("B"))result.append("\n");
   else if(token.equals("ICON_SHANK_01"))result.append(new TextComponent("\ue000").withStyle(s->s.withColor(0xffffff).withFont(new ResourceLocation("legacyconsole","ps4_tutorial"))));
   else if(token.equals("ICON_ARMOR_01"))result.append(new TextComponent("\ue001").withStyle(s->s.withColor(0xffffff).withFont(new ResourceLocation("legacyconsole","ps4_tutorial"))));
   else if(INPUT.containsKey(token)){
    result.append(" ");
    if(controller){String glyph=INPUT.get(token);char marker=(char)(0xe100+GLYPHS.indexOf(glyph));result.append(new TextComponent(String.valueOf(marker)).withStyle(s->s.withColor(0xffffff).withFont(font(folder,height))));}
    else result.append(keyboard.getOrDefault(token,""));
    result.append(" ");
   }
   end=matches.end();
  }
  result.append(text.substring(end));return result;
 }
 public static MutableComponent component(String source){
  var mc=Minecraft.getInstance();boolean crafting=mc.screen instanceof ConsoleCraftingScreen||mc.screen instanceof ConsoleSpecialCrafting;
  Map<String,String> key=new HashMap<>();key.put("CONTROLLER_VK_A",crafting?"Enter":"Left Click");key.put("JAVA_CONFIRM","Enter");key.put("CONTROLLER_VK_B","Esc");key.put("CONTROLLER_VK_X",crafting?"X":"Right Click");key.put("CONTROLLER_VK_Y","Shift + Click");
  key.put("CONTROLLER_ACTION_MOVE","WASD");key.put("CONTROLLER_MENU_NAVIGATE","Mouse");key.put("CONTROLLER_ACTION_LOOK","Mouse");
  key.put("CONTROLLER_ACTION_ACTION",mc.options.keyAttack.getTranslatedKeyMessage().getString());key.put("CONTROLLER_ACTION_USE",mc.options.keyUse.getTranslatedKeyMessage().getString());key.put("CONTROLLER_ACTION_JUMP",mc.options.keyJump.getTranslatedKeyMessage().getString());key.put("CONTROLLER_ACTION_INVENTORY",mc.options.keyInventory.getTranslatedKeyMessage().getString());
  key.put("CONTROLLER_ACTION_CRAFTING","C");key.put("CONTROLLER_ACTION_LEFT_SCROLL","Mouse Wheel");key.put("CONTROLLER_ACTION_RIGHT_SCROLL","Mouse Wheel");key.put("CONTROLLER_VK_LB","[");key.put("CONTROLLER_VK_RB","]");key.put("CONTROLLER_ACTION_DISMOUNT",mc.options.keyShift.getTranslatedKeyMessage().getString());key.put("CONTROLLER_ACTION_MENU_PAGEDOWN","Page Down");
  return component(source,ControllerInput.INSTANCE.state.connected,ControllerGlyphs.current().folder,mc.getWindow().getHeight(),key);
 }
 /** 1.17 has no space font provider. Measure inline artwork in the same splitter
  * that wraps its text, keeping the mod's original layered glyphs untouched. */
 public static float glyphAdvance(int cp,Style style){
  var font=style.getFont();if(!font.getNamespace().equals("legacyconsole")||!font.getPath().startsWith("tutorial_")||cp<0xe100||cp>=0xe100+GLYPHS.size())return -1;
  String folder=font.getPath().substring(9);boolean hd=folder.endsWith("_hd");if(hd)folder=folder.substring(0,folder.length()-3);
  return advance(GLYPHS.get(cp-0xe100),LceGlyphLayout.family(folder,hd?1080:720));
 }
 private static StringSplitter splitter(){return new StringSplitter((cp,style)->{float glyph=glyphAdvance(cp,style);return glyph>=0?glyph:Minecraft.getInstance().font.width(new TextComponent(new String(Character.toChars(cp))).setStyle(style));});}
 public static List<FormattedCharSequence> split(Component content,int width){return Language.getInstance().getVisualOrder(splitter().splitLines(content,width,Style.EMPTY));}
 public static float width(FormattedCharSequence line){return splitter().stringWidth(line);}
 /** Draw using precisely the same advances used for word wrapping. */
 public static void draw(FormattedCharSequence line,float x,float y,int color,boolean shadow){
  var mc=Minecraft.getInstance();float[] cursor={x};
  line.accept((index,style,cp)->{
   ResourceLocation font=style.getFont();boolean glyph=glyphAdvance(cp,style)>=0;
   if(glyph){
    String name=GLYPHS.get(cp-0xe100),folder=font.getPath().substring(9);boolean hd=folder.endsWith("_hd");if(hd)folder=folder.substring(0,folder.length()-3);
    var family=LceGlyphLayout.family(folder,hd?1080:720);float[] b=family.glyph(name).bounds;float scale=GLYPH_HEIGHT*family.units/(b[3]-b[2]);
    GlStateManager.pushMatrix();GlStateManager.translate(cursor[0]+1,y-(GLYPH_HEIGHT-9)/2,0);GlStateManager.scale(scale,scale,1);
    ConsoleTheme.glyph(family,name,-b[0]/family.units,-b[2]/family.units);GlStateManager.popMatrix();cursor[0]+=advance(name,family);
   }else{
    var part=new TextComponent(new String(Character.toChars(cp))).setStyle(style);if(shadow)mc.font.drawShadow(PortGraphics.pose,part,cursor[0],y,color);else mc.font.draw(PortGraphics.pose,part,cursor[0],y,color);cursor[0]+=mc.font.width(part);
   }return true;
  });
 }
 /** Flash HTML line boxes grow to accommodate middle-aligned inline images. */
 public static float lineHeight(FormattedCharSequence line){
  boolean[] image={false};line.accept((index,style,cp)->{if(style.getFont().getNamespace().equals("legacyconsole")&&style.getFont().getPath().startsWith("tutorial_")&&cp>=0xe100&&cp<0xe100+GLYPHS.size())image[0]=true;return true;});
  return image[0]?GLYPH_HEIGHT*3:30;
 }
 private Ps4TutorialText(){}
}

package dev.legacyconsole.client.ui;

import static org.junit.Assert.*;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.Test;

public class Ps4TutorialTextTest {
 @Test public void originalTutorialTabsNeverReachTheFontInAnyInputMode()throws Exception{
  try(var in=new InputStreamReader(getClass().getResourceAsStream("/assets/legacyconsole/tutorial/tasks.json"),StandardCharsets.UTF_8)){
   var messages=new ArrayList<String>();collectTabbed(new JsonParser().parse(in),messages);assertEquals("Original PS4 message/prompt fields containing authoring tabs",17,messages.size());
   for(var source:messages)for(boolean controller:List.of(false,true))for(String family:List.of("ps4","ps3","xbox360","xboxone"))for(int height:List.of(720,1440)){
    var text=Ps4TutorialText.component(source,controller,family,height,Map.of("CONTROLLER_VK_A","Enter","CONTROLLER_VK_B","Esc"));
    assertFalse(source,text.getString().contains("\t"));text.getVisualOrderText().accept((index,style,cp)->{assertNotEquals("Tab must not be measured or drawn as a missing glyph",9,cp);return true;});
   }
  }
 }
 private static void collectTabbed(JsonElement value,List<String> messages){
  if(value.isJsonObject())for(var entry:value.getAsJsonObject().entrySet())collectTabbed(entry.getValue(),messages);
  else if(value.isJsonArray())for(var entry:value.getAsJsonArray())collectTabbed(entry,messages);
  else if(value.isJsonPrimitive()&&value.getAsJsonPrimitive().isString()&&value.getAsString().contains("\t"))messages.add(value.getAsString());
 }
 @Test public void removingTabsPreservesAuthoredWordsParagraphsAndControllerSpans(){
  var text=Ps4TutorialText.component("\t\tFirst line.\n\n\tPress{*CONTROLLER_VK_X*}{*B*}\tNext line.",true,"ps4",1440,Map.of());
  assertEquals("First line.\n\nPress \ue102 \nNext line.",text.getString());
  assertEquals(Ps4TutorialText.font("ps4",1440),text.getSiblings().stream().filter(p->p.getString().equals("\ue102")).findFirst().orElseThrow().getStyle().getFont());
 }
 @Test public void inlineImageSizeAndLineBoxesFollowOriginalPs4Html()throws Exception{
  try(var in=new InputStreamReader(getClass().getResourceAsStream("/ps4/tutorial-map-239-native.json"),StandardCharsets.UTF_8)){
   var source=new JsonParser().parse(in).getAsJsonObject();for(var size:source.getAsJsonArray("inline_glyphs"))assertEquals(size.getAsJsonObject().get("size").getAsFloat(),Ps4TutorialText.GLYPH_HEIGHT*3,0);
  }
  var image=Ps4TutorialText.component("Press{*CONTROLLER_VK_X*}",true,"ps4",1440,Map.of());
  assertEquals(45,Ps4TutorialText.lineHeight(image.getVisualOrderText()),0);assertEquals(30,Ps4TutorialText.lineHeight(net.minecraft.util.FormattedCharSequence.forward("Plain text",net.minecraft.network.chat.Style.EMPTY)),0);
 }
 @Test public void everyFamilyUsesOriginalLayersAndMatchingWrapAdvances()throws Exception{
  for(String folder:List.of("ps4","ps3","xbox360","xboxone"))for(int height:List.of(720,1080,1440,2160)){
   var font=Ps4TutorialText.font(folder,height);var family=LceGlyphLayout.family(folder,height);
   try(var in=new InputStreamReader(getClass().getResourceAsStream("/assets/legacyconsole/font/"+font.getPath()+".json"),StandardCharsets.UTF_8)){
    assertEquals("1.17 rejects the space provider",0,new JsonParser().parse(in).getAsJsonObject().getAsJsonArray("providers").size());
    for(int i=0;i<Ps4TutorialText.GLYPHS.size();i++){String glyph=Ps4TutorialText.GLYPHS.get(i);assertTrue(family.glyph(glyph).layers.length>0);assertEquals(Ps4TutorialText.advance(glyph,family),Ps4TutorialText.glyphAdvance(0xe100+i,net.minecraft.network.chat.Style.EMPTY.withFont(font)),.001f);}
   }
  }
 }
 @Test public void inputTokensUseGlyphSpansInsteadOfUnicodeButtonShapes(){
  var text=Ps4TutorialText.component("Press{*CONTROLLER_VK_X*}; look{*CONTROLLER_ACTION_LOOK*}; use{*CONTROLLER_ACTION_MENU_PAGEDOWN*}.",true,"xboxone",1440,Map.of());
  assertFalse(text.getString().matches(".*[✕○□△].*"));assertEquals(3,text.getSiblings().stream().filter(p->p.getStyle().getFont().equals(Ps4TutorialText.font("xboxone",1440))).count());
  assertTrue(text.getString().contains(String.valueOf((char)(0xe100+Ps4TutorialText.GLYPHS.indexOf("right_trigger")))));
 }
 @Test public void armorAndFoodTokensKeepTheOriginalBitmapFont(){
  var text=Ps4TutorialText.component("2{*ICON_SHANK_01*} 1{*ICON_ARMOR_01*}",true,"ps4",1440,Map.of());
  assertEquals("2\ue000 1\ue001",text.getString());assertFalse(text.getString().contains("{*"));
 }
 @Test public void keyboardBindingsAndOriginalParagraphsRemainText(){
  var text=Ps4TutorialText.component("Press{*JAVA_CONFIRM*}{*B*}Use{*CONTROLLER_ACTION_USE*}",false,"ps4",1440,Map.of("JAVA_CONFIRM","Enter","CONTROLLER_ACTION_USE","Right Button"));
  assertEquals("Press Enter \nUse Right Button ",text.getString());
 }
}

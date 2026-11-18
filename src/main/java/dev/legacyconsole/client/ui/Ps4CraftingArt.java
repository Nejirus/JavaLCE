package dev.legacyconsole.client.ui;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.world.item.ItemStack;
import dev.legacyconsole.port.GlStateManager;

/** Original PS4 CraftingMenu1080.fui transforms and skinHDGraphicsInGame.fui PNGs. */
final class Ps4CraftingArt {
  static final Data DATA;
  static {
    try(var in=new InputStreamReader(Ps4CraftingArt.class.getResourceAsStream(
        "/assets/legacyconsole/ps4_crafting.json"),StandardCharsets.UTF_8)) {
      DATA=new Gson().fromJson(in,Data.class);
    } catch(Exception error){throw new ExceptionInInitializerError(error);}
  }
  static final class Data {
    float[] root,description_panel;
    List<String> tab_patterns;
    Map<String,float[]> controls;
    Map<String,float[]> vertical_tab_icons;
    Map<String,Sprite> sprites;
    Map<String,Panel> panels;
    Map<String,String> labels;
    List<Background> backgrounds;
  }
  static final class Sprite {String texture;float[] size;}
  static final class Piece {String texture;float[] rect;}
  static final class Panel {float[] grid;List<Piece> pieces;}
  static final class Background {String panel;float[] matrix;}
  record Rect(float x,float y,float w,float h) {
    boolean contains(double px,double py){return px>=x && py>=y && px<x+w && py<y+h;}
  }
  private final float x,y,scale;
  Ps4CraftingArt(int width,int height) {
    // The PS4 scene is authored at 1920x1080. Retain its aspect ratio on narrower displays.
    float left=Float.POSITIVE_INFINITY,top=left,right=Float.NEGATIVE_INFINITY,bottom=right;
    for(Background b:DATA.backgrounds){float[] m=b.matrix,last=DATA.panels.get(b.panel).pieces.get(8).rect;
      left=Math.min(left,m[4]);top=Math.min(top,m[5]);right=Math.max(right,m[4]+(last[0]+last[2])*m[0]);bottom=Math.max(bottom,m[5]+(last[1]+last[3])*m[3]);}
    var layout=Ps4UiScale.scene(width,height,dev.legacyconsole.client.ClientConfig.consoleUiSize,DATA.root[0],DATA.root[1],left,top,right,bottom);
    scale=layout.scale();x=layout.x();y=layout.y();
  }
  float x(){return x;}
  float y(){return y;}
  float scale(){return scale;}
  Rect rect(float x,float y,float w,float h){return new Rect(this.x+x*scale,this.y+y*scale,w*scale,h*scale);}
  Rect control(String name) {
    float[] m=DATA.controls.get(name);return rect(m[4],m[5],50*m[0],50*m[3]);
  }
  static String label(String id){return DATA.labels.get(id);}
  void sprite(String name,float x,float y) {
    Sprite sprite=DATA.sprites.get(name);draw(sprite.texture,rect(x,y,sprite.size[0],sprite.size[1]));
  }
  void image(String name,Rect bounds){draw(DATA.sprites.get(name).texture,bounds);}
  private void draw(String texture,Rect r) {
    ConsoleTheme.spriteAt("lce/ps4/crafting/"+texture,r.x,r.y,r.w,r.h);
  }
  void panels() {
    for(Background b:DATA.backgrounds) {
      float[] m=b.matrix;
      panel(b.panel,m[4],m[5],DATA.panels.get(b.panel).pieces.get(8).rect[0]+DATA.panels.get(b.panel).pieces.get(8).rect[2],m[0],m[3]);
    }
  }
  void descriptionPanel() {
    float[] d=DATA.description_panel;
    GlStateManager.pushMatrix();GlStateManager.translate(x+d[4]*scale,y+d[5]*scale,0);
    GlStateManager.scale(d[0],d[3],1);GlStateManager.translate(-x,-y,0);
    panel("FJ_SquareRecessedPanel",0,0,66,d[6],d[7]);GlStateManager.popMatrix();
  }
  private void panel(String name,float x,float y,float size,float sx,float sy) {
    Panel panel=DATA.panels.get(name);float left=panel.grid[0],right=size-panel.grid[1];
    float top=panel.grid[2],bottom=size-panel.grid[3];
    float[] xs={0,left,size*sx-right,size*sx};float[] ys={0,top,size*sy-bottom,size*sy};
    for(int i=0;i<9;i++)draw(panel.pieces.get(i).texture,rect(x+xs[i%3],y+ys[i/3],xs[i%3+1]-xs[i%3],ys[i/3+1]-ys[i/3]));
  }
  void label(String text,float x,float y,float width,boolean center,int color) {
    Rect r=rect(x,y,width,30);
    GlStateManager.pushMatrix();GlStateManager.translate(r.x,r.y,0);
    float fontScale=scale*3;GlStateManager.scale(fontScale,fontScale,1);
    String fitted=ConsoleTheme.fit(text,Math.round(r.w/fontScale));
    if(center)ConsoleTheme.centered(fitted,Math.round(r.w/fontScale/2),0,color);
    else ConsoleTheme.text(fitted,0,0,color);
    GlStateManager.popMatrix();
  }
  void description(ItemStack item){
    description(item,false);
  }
  void description(ItemStack item,boolean top){
    String text=dev.legacyconsole.client.Ps4Descriptions.get(item);if(text.isEmpty())return;
    float[] m=DATA.controls.get(top?"DescriptionTextTop":"DescriptionText");var bounds=rect(m[4],m[5],217.05f*m[0],100*m[3]);
    var mc=net.minecraft.client.Minecraft.getInstance();float fontScale=scale*3;
    var lines=mc.font.split(Ps4TutorialText.component(text),(int)(bounds.w/fontScale));
    GlStateManager.pushMatrix();GlStateManager.translate(bounds.x,bounds.y,0);GlStateManager.scale(fontScale,fontScale,1);
    for(int i=0;i<Math.min(lines.size(),(int)(bounds.h/fontScale/10));i++)Ps4TutorialText.draw(lines.get(i),0,i*10,ConsoleTheme.DARK,false);
    GlStateManager.popMatrix();
  }
  void ingredients(List<dev.legacyconsole.crafting.Ps4CraftingInformation.Line> lines){
    for(int i=0;i<Math.min(5,lines.size());i++){
      var line=lines.get(i);String key="Ingredient"+(i+1);slot(line.icon(),control(key),false,line.missing());
      float[] m=DATA.controls.get(key+"Desc");String name=line.icon().getHoverName().getString();
      if(line.alternatives().size()>1){
        String[] groups={"wool","planks","sandstone","red_sandstone","stonebrick","quartz_block","stone","wooden_slab","dye"};
        String[] hashes={"52c6df2e","a4fbd60e","6bb77ede","f36c79ed","d2631070","48008e1a","acc3dd75","68615d41","86f48c5a"};
        for(int g=0;g<groups.length;g++){var items=dev.legacyconsole.tutorial.Ps4TutorialScript.resolve(groups[g],-1);if(line.alternatives().stream().allMatch(s->items.contains(s.getItem()))||((groups[g].equals("planks")||groups[g].equals("wool")||groups[g].equals("wooden_slab"))&&inTag(line.alternatives(),groups[g].equals("wooden_slab")?"wooden_slabs":groups[g]))){name=label(hashes[g]);break;}}
      }
      Rect r=rect(m[4],m[5],217.05f*m[0],26);float size=scale*2;
      GlStateManager.pushMatrix();GlStateManager.translate(r.x,r.y,0);GlStateManager.scale(size,size,1);
      ConsoleTheme.text(ConsoleTheme.fit(name,Math.round(r.w/size)),0,0,ConsoleTheme.DARK);GlStateManager.popMatrix();
    }
  }
  private boolean inTag(List<ItemStack> items,String tag){return items.stream().allMatch(s->net.minecraft.tags.ItemTags.getAllTags().getTagOrEmpty(new net.minecraft.resources.ResourceLocation("minecraft",tag)).contains(s.getItem()));}
  void item(ItemStack stack,Rect bounds,float sourceSize) {
    if(stack.isEmpty())return;
    float size=sourceSize*scale;
    GlStateManager.pushMatrix();
    GlStateManager.translate(bounds.x+(bounds.w-size)/2,bounds.y+(bounds.h-size)/2,0);
    GlStateManager.scale(size/16f,size/16f,1);
    ConsoleTheme.item(stack,0,0);
    GlStateManager.popMatrix();
  }
  void verticalTab(int index,boolean active,ItemStack icon) {
    float[] tab=DATA.controls.get("TabV_"+(index+1)+(active?"":"_Off"));
    float[] holder=DATA.vertical_tab_icons.get(active?"on":"off");
    sprite(active?"LargeVertTabOn":"LargeVertTabOff",tab[4],tab[5]);
    item(icon,rect(tab[4]+holder[4],tab[5]+holder[5],24*holder[0],24*holder[3]),72);
  }
  void selectorArrows(Rect bounds) {
    float sx=(bounds.x-x)/scale,sy=(bounds.y-y)/scale;
    sprite("scrollUp",sx+17,sy-42);
    sprite("scrollDown",sx+17,sy+91);
  }
  void slot(ItemStack stack,Rect bounds,boolean focus,boolean missing) {
    slot(stack,bounds,focus,missing,true);
  }
  void slot(ItemStack stack,Rect bounds,boolean focus,boolean missing,boolean decorations) {
    image(missing?"IconHolderRed_1x1border":"IconHolder_1x1border",bounds);
    if(focus) {
      float extra=bounds.w*(108f/82-1)/2;
      image("Craft_Highlight_L_Small",new Rect(bounds.x-extra,bounds.y-extra,bounds.w+extra*2,bounds.h+extra*2));
    }
    if(stack.isEmpty())return;
    float[] viewport=Ps4ItemGeometry.viewport(bounds.x,bounds.y,bounds.w,bounds.h,scale);
    GlStateManager.pushMatrix();GlStateManager.translate(viewport[0],viewport[1],0);
    GlStateManager.scale(viewport[2]/16,viewport[3]/16,1);
    var game=net.minecraft.client.Minecraft.getInstance();
    GlStateManager.enableDepth();dev.legacyconsole.port.RenderHelper.enableGUIStandardItemLighting();
    try {
      game.getItemRenderer().renderAndDecorateItem(stack,0,0);
      if(decorations)game.getItemRenderer().renderGuiItemDecorations(game.font,stack,0,0);
    } finally {
      // FUI paints the next selector frame above previous custom-drawn items.
      // Match ConsoleTheme.item's state cleanup, including undecorated selectors.
      dev.legacyconsole.port.RenderHelper.disableStandardItemLighting();
      GlStateManager.disableLighting();GlStateManager.disableDepth();GlStateManager.popMatrix();
    }
  }
}

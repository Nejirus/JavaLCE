package dev.legacyconsole.client.ui;

import com.google.gson.Gson;
import dev.legacyconsole.port.GlStateManager;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

/** Rendering adapter for the original Orbis FUI display lists, in their 1920x1080 space. */
final class Ps4UiArt {
  static final Data DATA;
  static {
    try (var reader = new InputStreamReader(Ps4UiArt.class.getResourceAsStream(
        "/assets/legacyconsole/ps4_workstations.json"), StandardCharsets.UTF_8)) {
      DATA = new Gson().fromJson(reader, Data.class);
    } catch (Exception e) { throw new ExceptionInInitializerError(e); }
  }
  static final class Data {
    Map<String, Sprite> sprites;
    Map<String, Panel> panels;
    Map<String, Scene> scenes;
    Label label,centered_label,small_label;
    Map<String,Label> typography;
    Map<String,String> labels;
  }
  static final class Label { float[] bounds, matrix; long color; int alignment; float font_size; }
  static final class Sprite { String texture; float[] size; }
  static final class Piece { String texture; float[] rect; }
  static final class Panel { float[] grid; List<Piece> pieces; }
  static final class Node { String name,symbol; float[] matrix; }
  static final class Scene {
    float[] root;
    Map<String, Node> controls;
    @com.google.gson.annotations.SerializedName("static") List<Node> background;
    Map<String,List<Node>> objects;
    List<Node> experience;
  }
  record Rect(float x, float y, float w, float h) {
    boolean contains(double px, double py) { return px >= x && py >= y && px < x+w && py < y+h; }
    Rect inset(float n) { return new Rect(x+n, y+n, w-2*n, h-2*n); }
  }
  final Scene scene;
  final float x, y, scale;

  Ps4UiArt(String scene,int width,int height){this(scene,width,height,315);}
  Ps4UiArt(String scene,int width,int height,float popupHeight){
    this.scene=DATA.scenes.get(scene);
    Ps4UiScale.Layout layout;
    if(scene.equals("TutorialPopup"))layout=Ps4UiScale.popup(width,height,dev.legacyconsole.client.ClientConfig.consoleUiSize,popupHeight);
    else{
      float left=Float.POSITIVE_INFINITY,top=left,right=Float.NEGATIVE_INFINITY,bottom=right;
      for(Node node:this.scene.background){float[] m=node.matrix;float w,h;
        if(DATA.panels.containsKey(node.symbol)){float[] last=DATA.panels.get(node.symbol).pieces.get(8).rect;w=last[0]+last[2];h=last[1]+last[3];}
        else{var sprite=DATA.sprites.get(node.symbol);w=sprite.size[0];h=sprite.size[1];}
        left=Math.min(left,m[4]);top=Math.min(top,m[5]);right=Math.max(right,m[4]+w*m[0]);bottom=Math.max(bottom,m[5]+h*m[3]);
      }
      layout=Ps4UiScale.scene(width,height,dev.legacyconsole.client.ClientConfig.consoleUiSize,this.scene.root[0],this.scene.root[1],left,top,right,bottom);
    }
    scale=layout.scale();x=layout.x();y=layout.y();
  }
  Rect rect(float px, float py, float w, float h) {
    return new Rect(x+px*scale, y+py*scale, w*scale, h*scale);
  }
  float[] matrix(String control) { return scene.controls.get(control).matrix; }
  static String text(String key){return DATA.labels.get(key);}
  static int textColor(String style){return Integer.rotateRight((int)DATA.typography.get(style).color,8);}
  void styledText(String text,String style,float px,float py) {
    Label label=DATA.typography.get(style);
    Rect r=rect(px+label.matrix[4]+label.bounds[0],py+label.matrix[5]+label.bounds[2],
        label.bounds[1]-label.bounds[0],label.bounds[3]-label.bounds[2]);
    float fontScale=scale*3;
    GlStateManager.pushMatrix();GlStateManager.translate(r.x,r.y,0);GlStateManager.scale(fontScale,fontScale,1);
    String fitted=ConsoleTheme.fit(text,Math.max(1,(int)(r.w/fontScale)));
    int x=label.alignment==1?Math.round(r.w/fontScale)-dev.legacyconsole.port.FontCompat.get().getStringWidth(fitted):0;
    ConsoleTheme.text(fitted,x,0,Integer.rotateRight((int)label.color,8));GlStateManager.popMatrix();
  }
  Rect control(String name) {
    float[] m = matrix(name);
    return rect(m[4], m[5], 50*m[0], 50*m[3]);
  }
  Rect control(String name, float sourceWidth, float sourceHeight) {
    float[] m = matrix(name);
    return rect(m[4], m[5], sourceWidth*m[0], sourceHeight*m[3]);
  }
  Rect cell(String name, int col, int row, int columns, int rows) {
    Rect r = control(name);
    return new Rect(r.x+col*r.w/columns, r.y+row*r.h/rows, r.w/columns, r.h/rows);
  }
  Rect panelBounds() {
    Node n = scene.background.get(0);
    Panel p = DATA.panels.get(n.symbol);
    float[] last = p.pieces.get(8).rect;
    return rect(n.matrix[4], n.matrix[5], (last[0]+last[2])*n.matrix[0],
        (last[1]+last[3])*n.matrix[3]);
  }
  void backgrounds() {
    for (Node n : scene.background) {
      // Empty slot artwork is conditional; item rendering supplies its foreground.
      if (n.symbol.equals("LapisIcon") || n.symbol.equals("BrewingBlazePowderIcon")) continue;
      drawNode(n);
    }
  }
  void drawNode(Node n) {
    float[] m = n.matrix;
    if (DATA.panels.containsKey(n.symbol)) panel(n.symbol, m[4], m[5], m[0], m[3]);
    else {
      Sprite s = DATA.sprites.get(n.symbol);
      if (s == null) throw new IllegalArgumentException("Unexported PS4 bitmap: " + n.symbol);
      image(n.symbol, rect(m[4], m[5], s.size[0]*m[0], s.size[1]*m[3]));
    }
  }
  void emptyIcon(String symbol) {
    scene.background.stream().filter(n -> n.symbol.equals(symbol)).forEach(this::drawNode);
  }
  void object(String control) { scene.objects.get(control).forEach(this::drawNode); }
  void experience(float current,float preview) {
    for(Node n:scene.experience) {
      float fill=n.name.equals("PrevBarFiller")?preview:n.name.equals("ExpBarFiller")?current:1;
      Sprite s=DATA.sprites.get(n.symbol);float[] m=n.matrix;
      Rect r=rect(m[4],m[5],s.size[0]*m[0],s.size[1]*m[3]);
      GlStateManager.pushMatrix();GlStateManager.translate(r.x,r.y,0);
      GlStateManager.scale(r.w/s.size[0],r.h/s.size[1],1);
      ConsoleTheme.clippedSprite("ps4/workstations/"+s.texture,0,0,(int)s.size[0],(int)s.size[1],
          Math.max(0,Math.min(1,fill)),1,false,false);
      GlStateManager.popMatrix();
    }
  }
  void image(String symbol, Rect r) { draw(DATA.sprites.get(symbol).texture, r); }
  void progress(String control,String off,String on,float fraction,boolean horizontal) {
    progress(control,off,on,fraction,horizontal,true);
  }
  void progress(String control,String off,String on,float fraction,boolean horizontal,boolean bottom) {
    Sprite sprite=DATA.sprites.get(off);
    Rect r=control(control,sprite.size[0],sprite.size[1]);
    image(off,r);
    GlStateManager.pushMatrix();
    GlStateManager.translate(r.x,r.y,0);
    GlStateManager.scale(scale,scale,1);
    ConsoleTheme.clippedSprite("ps4/workstations/"+DATA.sprites.get(on).texture,0,0,
        (int)sprite.size[0],(int)sprite.size[1],horizontal?fraction:1,horizontal?1:fraction,false,bottom);
    GlStateManager.popMatrix();
  }
  void sprite(String symbol, float px, float py) {
    Sprite s = DATA.sprites.get(symbol);
    image(symbol, rect(px, py, s.size[0], s.size[1]));
  }
  private void draw(String texture, Rect r) {
    ConsoleTheme.spriteAt("ps4/workstations/"+texture, r.x, r.y, r.w, r.h);
  }
  void panel(String symbol, float px, float py, float sx, float sy) {
    Panel p = DATA.panels.get(symbol);
    float[] last = p.pieces.get(8).rect;
    float w = last[0]+last[2], h = last[1]+last[3];
    float[] xx = {0, p.grid[0], w*sx-(w-p.grid[1]), w*sx};
    float[] yy = {0, p.grid[2], h*sy-(h-p.grid[3]), h*sy};
    for (int i=0; i<9; i++) draw(p.pieces.get(i).texture,
        rect(px+xx[i%3], py+yy[i/3], xx[i%3+1]-xx[i%3], yy[i/3+1]-yy[i/3]));
  }
  void textAt(String text, float px, float py, float sourceWidth, int color, boolean center) {
    text(text, rect(px, py, sourceWidth, 30), color, center, 3);
  }
  void coloredLabel(String text,String control,String style){
    Label label=DATA.typography.get(style);float[] m=matrix(control);
    text(text,rect(m[4]+label.matrix[4]+label.bounds[0],m[5]+label.matrix[5]+label.bounds[2],
        (label.bounds[1]-label.bounds[0])*m[0],30),Integer.rotateRight((int)label.color,8),label.alignment==2,3);
  }
  void label(String text, String control) {
    label(text,control,0);
  }
  void centeredLabel(String text,String control) {
    Label label=DATA.centered_label;float[] m=matrix(control);
    Rect r=rect(m[4]+label.matrix[4]+label.bounds[0],m[5]+label.matrix[5]+label.bounds[2],
        (label.bounds[1]-label.bounds[0])*m[0],label.bounds[3]-label.bounds[2]);
    text(text,r,Integer.rotateRight((int)label.color,8),true,label.font_size/10);
  }
  void insetLabel(String text,String control,String panel) {
    label(text,control,DATA.panels.get(panel).grid[0]);
  }
  private void label(String text,String control,float leftInset) {
    float[] m = matrix(control);
    // Label transforms stretch the FUI text field, not the glyphs.
    Label label=DATA.label;
    // Native labels can grow past their authoring placeholder (e.g. Zoom Out & Clear).
    // Do not introduce a Java-only abbreviation of the original label text.
    text(text,rect(m[4]+label.matrix[4]+label.bounds[0]+leftInset,m[5]+label.matrix[5]+label.bounds[2],
        (label.bounds[1]-label.bounds[0])*m[0]-leftInset,30),Integer.rotateRight((int)label.color,8),false,3,false);
  }
  void text(String text, Rect bounds, int color, boolean center, float sourceFontScale) {
    text(text,bounds,color,center,sourceFontScale,true);
  }
  private void text(String text,Rect bounds,int color,boolean center,float sourceFontScale,boolean fit) {
    float fontScale = scale*sourceFontScale;
    GlStateManager.pushMatrix();
    GlStateManager.translate(bounds.x, bounds.y, 0);
    GlStateManager.scale(fontScale, fontScale, 1);
    int width = Math.max(1, (int)(bounds.w/fontScale));
    String fitted = fit ? ConsoleTheme.fit(text, width) : text;
    if (center) ConsoleTheme.centered(fitted, width/2, 0, color);
    else ConsoleTheme.text(fitted, 0, 0, color);
    GlStateManager.popMatrix();
  }
  void item(ItemStack stack, Rect r) {
    if (stack.isEmpty()) return;
    float size = Math.min(r.w,r.h)*.9f;
    GlStateManager.pushMatrix();
    GlStateManager.translate(r.x+(r.w-size)/2, r.y+(r.h-size)/2, 0);
    GlStateManager.scale(size/16, size/16, 1);
    ConsoleTheme.item(stack,0,0);
    GlStateManager.popMatrix();
  }
}

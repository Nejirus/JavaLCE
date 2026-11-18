package dev.legacyconsole.client.ui;
import java.util.*;import java.io.IOException;
import net.minecraft.client.*;import net.minecraft.client.gui.screens.Screen;import net.minecraft.client.gui.components.*;
import dev.legacyconsole.client.*;import dev.legacyconsole.client.input.*;import dev.legacyconsole.port.*;
/** Console option column backed by H1's actual native widgets, callbacks and shader files. */
public final class ConsoleOptifineScreen extends ConsolePanelScreen implements ControllerScreen {
 private final Screen delegate;private final List<Row> rows=new ArrayList<>();private int first,selected,visible;
 private final class Row {final AbstractWidget widget;final String pack;
  Row(AbstractWidget widget){this.widget=widget;pack=null;}Row(String pack){widget=null;this.pack=pack;}
  String label(){return pack==null?widget.getMessage().getString():pack.equals("OFF")?"Shaders: Off":pack.equals("(internal)")?"Internal":pack;}
  boolean selectedPack(){if(pack==null)return false;try{return pack.equals(Class.forName("net.optifine.shaders.Shaders").getField("currentShaderName").get(null));}catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot read the selected shader pack",e);}}
  boolean slider(){return widget instanceof AbstractSliderButton||widget!=null&&widget.getClass().getSimpleName().equals("GuiSliderShaderOption");}
  boolean binary(){return widget instanceof CycleButton<?> c&&c.getValue() instanceof Boolean;}
  double value(){return widget instanceof AbstractSliderButton s?OptionWidgets.value(s):((Number)OptifineMenus.call(OptifineMenus.call(widget,"getShaderOption",new Class<?>[0]),"getIndexNormalized",new Class<?>[0])).doubleValue();}
  void set(double value){double x=widget.x+4+Math.max(0,Math.min(1,value))*(widget.getWidth()-8);double y=widget.y+widget.getHeight()/2.0;delegate.mouseClicked(x,y,0);delegate.mouseReleased(x,y,0);}
  void step(int d){if(pack!=null){try{Class<?> shaders=Class.forName("net.optifine.shaders.Shaders");OptifineMenus.call(shaders,"setShaderPack",new Class<?>[]{String.class},pack);OptifineMenus.call(shaders,"uninit",new Class<?>[0]);OptifineMenus.call(shaders,"storeConfig",new Class<?>[0]);OptifineMenus.call(delegate,"updateButtons",new Class<?>[0]);mc.options.save();}catch(ClassNotFoundException e){throw new IllegalStateException(e);}}else if(slider()){
   if(widget instanceof AbstractSliderButton nativeSlider)set(OptionWidgets.nextValue(nativeSlider,d));else{Object option=OptifineMenus.call(widget,"getShaderOption",new Class<?>[0]);String[] values=(String[])OptifineMenus.call(option,"getValues",new Class<?>[0]);set(value()+d/(double)Math.max(1,values.length-1));}
  }else if(widget instanceof CycleButton<?>)OptionWidgets.step(widget,d);
  else if(widget.getClass().getName().equals("net.optifine.gui.GuiOptionButtonOF")){
   // IteratableOptionOF owns the setter and its resource/renderer side effects.
   // Use the signed native operation for controller arrows as well as clicks.
   Object option=OptifineMenus.call(widget,"getControlOption",new Class<?>[0]);
   OptifineMenus.call(mc.options,"setOptionValueOF",new Class<?>[]{Option.class,int.class},option,d);
   widget.setMessage((net.minecraft.network.chat.Component)OptifineMenus.call(mc.options,"getKeyComponentOF",new Class<?>[]{Option.class},option));
  }else{double x=widget.x+widget.getWidth()/2.0,y=widget.y+widget.getHeight()/2.0;delegate.mouseClicked(x,y,d<0?1:0);delegate.mouseReleased(x,y,d<0?1:0);}}
 }
 ConsoleOptifineScreen(Screen parent,Screen delegate){super(parent,delegate.getTitle().getString());this.delegate=delegate;}
 boolean wraps(Screen screen){return delegate==screen;}
 @Override protected void init(){super.init();MenuFocus.set(this,selected);}
 @Override public void initGui(){
  delegate.init(mc,width,height);title=delegate.getTitle().getString();rows.clear();
  if(delegate.getClass().getSimpleName().equals("GuiShaders"))try{Class<?> shaders=Class.forName("net.optifine.shaders.Shaders");for(Object pack:(List<?>)OptifineMenus.call(shaders,"listOfShaders",new Class<?>[0]))rows.add(new Row(pack.toString()));}catch(ClassNotFoundException e){throw new IllegalStateException(e);}
  for(var child:delegate.children())if(child instanceof AbstractWidget widget&&widget.visible)rows.add(new Row(widget));
  selected=Math.max(0,Math.min(selected,rows.size()-1));rebuild();
 }
 private void rebuild(){
  boolean[] binary=new boolean[rows.size()];for(int i=0;i<rows.size();i++)binary[i]=rows.get(i).binary();
  ConsoleOptionLayout layout=new ConsoleOptionLayout(width,height,binary,0,first,selected,ConsoleOptionLayout.CHECKBOX_PITCH);panelX=layout.x;panelY=layout.y;panelW=layout.w;panelH=layout.h;first=layout.first;visible=layout.rows;buttonList.clear();
  for(int i=first;i<Math.min(rows.size(),first+visible);i++){Row row=rows.get(i);int index=i;
   GuiButton b;if(row.slider())b=new ConsoleSlider(i,panelX+8,layout.rowY(i),panelW-16,row::value,row::set,row::label);
   else{ConsoleButton button=new ConsoleButton(i,panelX+8,layout.rowY(i),panelW-16,layout.rowHeight(i),row.label());button.allowCheckbox=row.binary();b=button;}
   b.enabled=row.widget==null||row.widget.active;buttonList.add(b);
  }MenuFocus.set(this,selected);
 }
 private void move(int d){if(rows.isEmpty())return;selected=Math.floorMod(selected+d,rows.size());rebuild();ControllerMenus.focus(this,selected);ConsoleAudio.play("focus");}
 private void adjust(int d){if(rows.isEmpty())return;rows.get(selected).step(d);mc.options.save();if(mc.screen==this){rebuild();ControllerMenus.focus(this,selected);}}
 @Override protected void actionPerformed(GuiButton b){selected=b.id;if(!(b instanceof ConsoleSlider))adjust(1);}
 @Override public void updateScreen(){delegate.tick();}
 @Override public void onGuiClosed(){delegate.removed();mc.options.save();}
 @Override protected void back(){mc.options.save();super.back();}
 @Override protected void shell(){ConsoleMenuLayout.background(width,height);ConsoleMenuLayout.logo(width,height);ConsoleTheme.lcePanel(panelX,panelY,panelW,panelH);ConsoleTheme.centered(title,width/2,panelY-13,ConsoleTheme.TEXT);}
 @Override public void drawScreen(int mx,int my,float partial){shell();for(GuiButton b:buttonList){Row row=rows.get(b.id);b.displayString=row.label();if(b instanceof ConsoleButton console)console.textColor=row.selectedPack()?0xff55ff55:null;b.drawButton(mc,mx,my);}int x=(width+ConsoleOptionLayout.WIDTH)/2+10;if(first>0)ConsoleTheme.sprite("gui/sprites/widget/scroll_up",x,panelY+6,7,5);if(first+visible<rows.size())ConsoleTheme.sprite("gui/sprites/widget/scroll_down",x,panelY+panelH-10,7,5);ConsoleTheme.footer(width,height,"cross","Select","circle","Back");}
 @Override protected void keyTyped(char c,int key)throws IOException{if(key==Keyboard.KEY_UP||key==Keyboard.KEY_DOWN)move(key==Keyboard.KEY_UP?-1:1);else if(key==Keyboard.KEY_LEFT||key==Keyboard.KEY_RIGHT)adjust(key==Keyboard.KEY_LEFT?-1:1);else if(key==Keyboard.KEY_RETURN)adjust(1);else super.keyTyped(c,key);}
 @Override public void handleMouseInput()throws IOException{super.handleMouseInput();int wheel=Mouse.getEventDWheel();if(wheel!=0)move(wheel>0?-1:1);}
 @Override public boolean controllerDirection(int d){if(d==PadState.UP||d==PadState.DOWN)move(d==PadState.UP?-1:1);else adjust(d==PadState.LEFT?-1:1);return true;}
 @Override public boolean controllerButton(int b){if(b==PadState.CROSS){adjust(1);return true;}return false;}
}

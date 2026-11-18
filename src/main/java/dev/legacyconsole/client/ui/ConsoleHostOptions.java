package dev.legacyconsole.client.ui;

import dev.legacyconsole.LegacyConsole;
import dev.legacyconsole.client.input.*;
import dev.legacyconsole.network.HostRequest;
import dev.legacyconsole.port.*;
import dev.legacyconsole.world.HostOptions;
import java.io.IOException;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;

/** PS4 InGameInfoMenu, InGameHostOptions, PlayerOptions and TeleportMenu scenes. */
public final class ConsoleHostOptions extends LegacyScreen implements ControllerScreen {
 private static Screen pendingParent;
 private static boolean awaiting;
 private final Screen parent;
 private CompoundTag state;
 private Ps4UiArt art;
 private int page,first,selected,weatherPreview=-1,pollTicks;
 private UUID target;
 private HostButton draggedSlider;
 private final List<Row> rows=new ArrayList<>();
 private record Row(int id,String text,int height,boolean enabled,int kind,boolean checked,UUID target){}
 private ConsoleHostOptions(Screen parent,CompoundTag state){this.parent=parent;this.state=state;}
 public static void clear(){awaiting=false;pendingParent=null;}
 public static void requestOpen(Screen parent){
  var mc=Minecraft.getInstance();if(mc.player==null||mc.getConnection()==null||!LegacyConsole.NETWORK.present(mc.getConnection().getConnection()))return;
  pendingParent=parent;awaiting=true;LegacyConsole.NETWORK.sendToServer(new HostRequest(HostOptions.OPEN,0,null));
 }
 public static void receive(CompoundTag state){
  var mc=Minecraft.getInstance();if(state==null)return;
  if(!state.getBoolean("enabled")){clear();if(mc.screen instanceof ConsoleHostOptions screen)mc.setScreen(screen.parent);return;}
  if(mc.screen instanceof ConsoleHostOptions screen){if(screen.weatherPreview>=0)state.putInt("weather",screen.weatherPreview);if(!state.equals(screen.state)){screen.state=state;screen.initGui();}}
  else if(awaiting){Screen parent=pendingParent;clear();mc.setScreen(new ConsoleHostOptions(parent,state));}
 }
 private CompoundTag player(UUID id){for(var v:state.getList("players",10)){CompoundTag p=(CompoundTag)v;if(p.getUUID("uuid").equals(id))return p;}return null;}
 private boolean admin(){return state.getBoolean("admin");}
 private boolean canTeleport(){var p=player(state.getUUID("viewer"));return admin()||p!=null&&p.getBoolean("teleport");}
 @Override public void updateScreen(){if(++pollTicks>=20){pollTicks=0;send(HostOptions.OPEN,0,null);}}
 private boolean self(){return target!=null&&target.equals(state.getUUID("viewer"));}
 private String text(String hash){return Ps4UiArt.text(hash);}
 private void row(int id,String label,int kind,boolean checked,boolean enabled){rows.add(new Row(id,label,3*(kind==1?ConsoleOptionLayout.CHECKBOX_HEIGHT:ConsoleOptionLayout.HEIGHT),enabled,kind,checked,null));}
 private void checkbox(int id,String label,boolean checked,boolean enabled){row(id,label,1,checked,enabled);}
 private String mode(int id){return net.minecraft.client.resources.language.I18n.get("gameMode."+GameType.byId(id).getName());}
 @Override public void initGui(){
  super.initGui();rows.clear();buttonList.clear();
  if(page==0){
   art=new Ps4UiArt("InGameInfoMenu",width,height);
   for(var v:state.getList("players",10)){var p=(CompoundTag)v;rows.add(new Row(400+rows.size(),p.getString("name"),90,true,0,false,p.getUUID("uuid")));}
  }else if(page==1){
   art=new Ps4UiArt("InGameHostOptions",width,height);
   int[] ids={0,1,2,3,4,5,12,13,15,16,14,17,9,6,7,8,18,10,11};
   String[] labels={"01de3701","ef311201","5e940e19","07a2d3b2","3decac90","2a7c56c4","d554d957","c0d12a27","d324428d",null,"4c920637","38c1c044","faaaeb73","4aac0bc3","78e0a40d","6f7d8591","65eef68e","dd235a1d","4296e646"};
   for(int n=0;n<ids.length;n++){int id=ids[n];String label=labels[n]==null?mode(state.getInt("mode")):text(labels[n]);
    if(id<10||id==18)checkbox(id,label,state.getBoolean("rule"+id),admin());
    else if(id==15)row(id,label+": "+net.minecraft.client.resources.language.I18n.get(Difficulty.byId(state.getInt("difficulty")).getKey()),2,false,admin());
    else if(id==16)row(id,label,2,false,admin());
    else if(id==17)row(id,label+": "+net.minecraft.client.resources.language.I18n.get(new String[]{"legacyconsole.ps4.clear","legacyconsole.ps4.rain","legacyconsole.ps4.thunder"}[state.getInt("weather")]),2,false,admin());
    else row(id,label,0,false,(id==10||id==11)?canTeleport():admin());
   }
  }else if(page==2){
   art=new Ps4UiArt("InGamePlayerOptions",width,height);var p=player(target);if(p==null){page=0;initGui();return;}
   boolean own=self(),protectedHost=p.getBoolean("owner");
   if(own){
    if(admin()||p.getBoolean("allowInvisible"))checkbox(109,text("9e0a027d"),p.getBoolean("invisible"),true);
    if(admin()||p.getBoolean("allowFly"))checkbox(107,text("65c86b6c"),p.getBoolean("fly"),true);
    if(admin()||p.getBoolean("allowExhaustion"))checkbox(108,text("8be63389"),p.getBoolean("exhaustion"),true);
   }else if(admin()){
    String[] keys={"build","doors","containers","attackPlayers","attackAnimals"};String[] labels={"0c1f0645","f7e7f9a1","da9c2af0","2a3b2570","2e30f7dd"};
    for(int i=0;i<5;i++)checkbox(100+i,text(labels[i]),!p.contains(keys[i])||p.getBoolean(keys[i]),!protectedHost);
    checkbox(105,text("ee5328fd"),p.getBoolean("moderator"),state.getBoolean("owner")&&!protectedHost);
    checkbox(109,text("bf2df1b6"),p.getBoolean("allowInvisible"),!protectedHost);
    checkbox(107,text("65c86b6c"),p.getBoolean("allowFly"),!protectedHost);
    checkbox(108,text("6d72db6a"),p.getBoolean("allowExhaustion"),!protectedHost);
    checkbox(106,text("77569663"),p.getBoolean("teleport"),!protectedHost);
   }
   if(admin()){row(111,mode(p.getInt("mode")),2,false,true);row(112,text("88f67c3e"),0,false,true);if(!own&&!protectedHost)row(110,text("4c8183da"),0,false,true);}
  }else{
   art=new Ps4UiArt("InGameTeleportMenu",width,height);
   for(var v:state.getList("players",10)){var p=(CompoundTag)v;UUID id=p.getUUID("uuid");if(!id.equals(state.getUUID("viewer")))rows.add(new Row(400+rows.size(),p.getString("name"),90,true,0,false,id));}
  }
  if(page==1&&!admin())rows.removeIf(row->row.id!=10&&row.id!=11);
  rebuild();
 }
 private String listControl(){return page==0||page>=3?"GamePlayers":page==1?"HostOptionsList":"PlayerOptionsList";}
 private void rebuild(){rebuild(-1);}
 private void rebuild(int reveal){
  buttonList.clear();if(page==0){var r=art.control("GameOptions",600,60);buttonList.add(new HostButton(new Row(300,"Host Options",60,canTeleport(),0,false,null),r));}
  var list=art.control(listControl());int[] heights=new int[rows.size()];boolean[] boxes=new boolean[rows.size()];
  for(int i=0;i<rows.size();i++){heights[i]=rows.get(i).height;boxes[i]=rows.get(i).kind==1;}
  var layout=new HostOptionsLayout(heights,boxes,list.h()/art.scale,first,reveal);first=layout.first;
  for(int i=first;i<layout.end;i++){var row=rows.get(i);
   buttonList.add(new HostButton(row,new Ps4UiArt.Rect(list.x(),list.y()+layout.y(i)*art.scale,list.w(),row.height*art.scale)));
  }
  if(buttonList.stream().noneMatch(b->b.id==selected&&b.enabled))selected=buttonList.stream().filter(b->b.enabled).mapToInt(b->b.id).findFirst().orElse(Integer.MIN_VALUE);
  ControllerMenus.focus(this,selected);
 }
 private void changePage(int page){this.page=page;if(page==0)weatherPreview=-1;first=0;selected=Integer.MIN_VALUE;draggedSlider=null;initGui();}
 private void send(int action,int value,UUID target){LegacyConsole.NETWORK.sendToServer(new HostRequest(action,value,target));}
 @Override protected void actionPerformed(GuiButton button){
  if(!(button instanceof HostButton b)||!b.enabled)return;selected=b.id;Row row=b.row;
  if(b.id==300){changePage(1);return;}
  if(page==0){target=row.target;changePage(2);return;}
  if(page>=3){send(page==3?HostOptions.TELEPORT_TO:HostOptions.TELEPORT_HERE,0,row.target);return;}
  if(page==1&&(row.id==10||row.id==11)){changePage(row.id==10?3:4);return;}
  if(row.kind==2)return; // Slider presses and drags choose an absolute stop, like Settings.
  adjust(row,1);
 }
 private int sliderMax(Row row){return row.id==15?3:2;}
 private int sliderValue(Row row){return row.id==15?state.getInt("difficulty"):row.id==17?state.getInt("weather"):row.id==111?player(target).getInt("mode"):state.getInt("mode");}
 private void setSlider(Row row,int value){
  value=Math.max(0,Math.min(sliderMax(row),value));if(value==sliderValue(row))return;
  if(row.id==17)weatherPreview=value;
  if(row.id==111)player(target).putInt("mode",value);
  else state.putInt(row.id==15?"difficulty":row.id==17?"weather":"mode",value);
  selected=row.id;send(row.id,value,page==2?target:null);initGui();
 }
 private void adjust(Row row,int direction){
  if(!row.enabled)return;
  if(row.kind==2){setSlider(row,sliderValue(row)+direction);return;}
  send(row.id,row.kind==1?(row.checked?0:1):0,page==2?target:null);
 }
 @Override public void drawScreen(int mx,int my,float partial){
  drawDefaultBackground();art.backgrounds();
  if(page==0)art.label("Players/Invite","Title");
  else if(page==2){var p=player(target);if(p!=null){
   // Java's player list supplies the index for the original sixteen FUI icon frames.
   int icon=Math.max(0,Math.min(15,p.getInt("colorIndex")));
   art.object("PlayerIcon"+icon);art.label(p.getString("name"),"Gamertag");
  }}
  else if(page>=3)art.label(text(page==3?"dd235a1d":"4296e646"),"Title");
  for(var b:buttonList)b.drawButton(mc,mx,my);
  ConsoleTheme.footer(width,height,"cross","Select","circle","Back");
 }
 @Override public void onClose(){if(page==0)mc.setScreen(parent);else changePage(page>=3?1:0);}
 @Override protected void keyTyped(char c,int key)throws IOException{
  if(key==Keyboard.KEY_UP||key==Keyboard.KEY_DOWN)controllerDirection(key==Keyboard.KEY_UP?PadState.UP:PadState.DOWN);
  else if(key==Keyboard.KEY_LEFT||key==Keyboard.KEY_RIGHT)controllerDirection(key==Keyboard.KEY_LEFT?PadState.LEFT:PadState.RIGHT);
  else if(key==Keyboard.KEY_RETURN)activateFocused();
  else super.keyTyped(c,key);
 }
 @Override public boolean controllerDirection(int direction){
  ControllerCursor.navigation();
  var focused=MenuFocus.button(this);
  if(direction==PadState.LEFT||direction==PadState.RIGHT){if(focused instanceof HostButton b&&b.row.kind==2)adjust(b.row,direction==PadState.LEFT?-1:1);return true;}
  if(direction!=PadState.UP&&direction!=PadState.DOWN)return true;
  int header=page==0?1:0,index=-1;boolean[] enabled=new boolean[rows.size()+header];
  if(header==1){enabled[0]=canTeleport();if(focused!=null&&focused.id==300)index=0;}
  for(int i=0;i<rows.size();i++){enabled[i+header]=rows.get(i).enabled;if(focused!=null&&rows.get(i).id==focused.id)index=i+header;}
  int next=HostOptionsLayout.nextEnabled(enabled,index,direction==PadState.UP?-1:1);
  if(next<0)return true;
  if(header==1&&next==0){selected=300;first=0;rebuild();}
  else{selected=rows.get(next-header).id;rebuild(next-header);}
  return true;
 }
 private void activateFocused(){
  if(MenuFocus.button(this) instanceof HostButton b&&b.row.kind!=2){
   b.playPressSound(mc.getSoundManager());LceMenuButton.press(b);actionPerformed(b);
  }
 }
 @Override public boolean controllerButton(int button){
  if(button==PadState.SHARE){mc.setScreen(null);return true;}
  // A focus menu confirms the highlighted row, independent of the previous pointer position.
  if(button==PadState.CROSS){activateFocused();return true;}
  return false;
 }
 @Override public void handleMouseInput()throws IOException{super.handleMouseInput();int d=Mouse.getEventDWheel();if(d!=0){draggedSlider=null;controllerDirection(d<0?PadState.DOWN:PadState.UP);}}
 @Override protected void mouseClickMove(int x,int y,int button,long time){if(button==0&&draggedSlider!=null)draggedSlider.updateSlider(x);}
 @Override protected void mouseReleased(int x,int y,int button){super.mouseReleased(x,y,button);draggedSlider=null;}
 @Override public boolean doesGuiPauseGame(){return false;}
 private final class HostButton extends GuiButton {
  final Row row;final Ps4UiArt.Rect bounds;
  HostButton(Row row,Ps4UiArt.Rect bounds){super(row.id,0,0,0,0,row.text);this.row=row;this.bounds=bounds;enabled=row.enabled;LceWidgets.place(this,bounds.x(),bounds.y(),bounds.w(),bounds.h());}
  @Override public boolean mousePressed(Minecraft game,int mx,int my){
   if(!enabled||!visible||!bounds.contains(mx,my))return false;
   selected=id;MenuFocus.set(ConsoleHostOptions.this,id);
   if(row.kind==2){draggedSlider=this;updateSlider(mx);}return true;
  }
  void updateSlider(int mx){
   float units=3*art.scale;
   setSlider(row,Math.round(Math.max(0,Math.min(1,(mx-bounds.x()-4*units)/(bounds.w()-8*units)))*sliderMax(row)));
  }
  @Override public void drawButton(Minecraft game,int mx,int my){
   hovered=enabled&&bounds.contains(mx,my);boolean focus=MenuFocus.focused(this,hovered);
   if(row.kind==1){
    art.image(focus?"Tickbox_Over":"FJ_Tickbox_Background",new Ps4UiArt.Rect(bounds.x(),bounds.y(),36*art.scale,36*art.scale));
    if(row.checked)art.image("Tick",new Ps4UiArt.Rect(bounds.x(),bounds.y(),36*art.scale,36*art.scale));
    art.text(displayString,new Ps4UiArt.Rect(bounds.x()+37*art.scale,bounds.y(),bounds.w()-37*art.scale,bounds.h()),Ps4UiArt.textColor(!enabled?"FJ_Label_CheckBoxGrey":focus?"FJ_Label_CheckBoxSelected":"FJ_Label_CheckBoxBlack"),false,3);
   }else{
    // Draw the same button/slider skins and labels as Settings, in this scene's units.
    float units=3*art.scale;float[] local={0,0,bounds.w()/units,bounds.h()/units};
    GlStateManager.pushMatrix();GlStateManager.translate(bounds.x(),bounds.y(),0);GlStateManager.scale(units,units,1);
    if(row.kind==2){
     LceWidgets.slider(local,sliderValue(row)/(double)sliderMax(row),focus,enabled);
     ConsoleTheme.centered(ConsoleTheme.fit(displayString,(int)local[2]-8),Math.round(local[2]/2),Math.round((local[3]-8)/2),enabled?(focus?0xffffff00:ConsoleTheme.TEXT):ConsoleTheme.MUTED);
    }else{
     String label=displayString;
     if(row.target!=null)displayString=LcePromptFont.INSTANCE.fit(label,Math.max(0,(int)local[2]-60));
     LceWidgets.button(this,local,focus);displayString=label;
    }
    GlStateManager.popMatrix();
    if(row.target!=null){
     var p=player(row.target);int icon=p==null?0:Math.max(0,Math.min(15,p.getInt("colorIndex")));
     var sprite=Ps4UiArt.DATA.sprites.get("MapIcon"+icon);
     art.image("MapIcon"+icon,new Ps4UiArt.Rect(bounds.x()+15*art.scale,bounds.y()+15*art.scale,sprite.size[0]*art.scale,sprite.size[1]*art.scale));
    }
   }
  }
 }
}

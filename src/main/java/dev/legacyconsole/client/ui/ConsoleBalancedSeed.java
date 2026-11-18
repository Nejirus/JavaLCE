package dev.legacyconsole.client.ui;

import dev.legacyconsole.client.input.*;
import dev.legacyconsole.port.GuiButton;
import dev.legacyconsole.worldgen.ps4.Ps4BalancedSeed;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import net.minecraft.client.gui.screens.Screen;

/** The original ten-second best-candidate search, with a cancelable console view. */
public final class ConsoleBalancedSeed extends ConsolePanelScreen implements ControllerScreen {
 private final ConsoleCreateWorld create;
 private final AtomicBoolean canceled=new AtomicBoolean();
 private final AtomicInteger attempts=new AtomicInteger();
 private CompletableFuture<Ps4BalancedSeed.Result> search;
 private String failure;
 ConsoleBalancedSeed(Screen parent,ConsoleCreateWorld create){super(parent,"Find Balanced Seed");this.create=create;panelW=284;panelH=126;}
 @Override public void initGui(){
  super.initGui();addRow(0,2,"Cancel");
  if(search==null){var size=create.legacyWorldSize();var scale=create.legacyBiomeScale();search=CompletableFuture.supplyAsync(()->Ps4BalancedSeed.find(new Random(),size,scale,canceled::get,attempts::set),net.minecraft.Util.backgroundExecutor());}
 }
 @Override public void updateScreen(){
  if(search!=null&&search.isDone()&&!canceled.get()&&failure==null){
   try{create.balancedSeed(Long.toString(search.join().seed()));mc.setScreen(parent);create.launchWorld();}
   catch(CompletionException error){failure="Seed search failed. Please try again.";buttonList.get(0).displayString="Back";}
  }
 }
 @Override public void drawScreen(int x,int y,float partial){
  super.drawScreen(x,y,partial);
  ConsoleTheme.centered(failure==null?"Finding a seed for your world...":failure,width/2,panelY+40,ConsoleTheme.DARK);
  if(failure==null)ConsoleTheme.centered("Seeds checked: "+attempts.get(),width/2,panelY+56,ConsoleTheme.DARK);
 }
 @Override protected void actionPerformed(GuiButton button){back();}
 @Override public void onGuiClosed(){canceled.set(true);}
 @Override public boolean controllerDirection(int direction){return false;}
 @Override public boolean controllerButton(int button){if(button==PadState.CIRCLE){back();return true;}return false;}
 @Override protected String[] footerHints(){return new String[]{"circle","Cancel"};}
}

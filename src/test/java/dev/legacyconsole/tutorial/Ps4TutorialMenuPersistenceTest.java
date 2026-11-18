package dev.legacyconsole.tutorial;
import static org.junit.Assert.*;
import dev.legacyconsole.network.TutorialState;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.*;
public class Ps4TutorialMenuPersistenceTest {
 @BeforeClass public static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
 @Test public void completedAndPartialGuidesSurviveNbtReload(){
  var p=new Ps4TutorialProgress();assertTrue(p.menuProgress(1,0,true));assertTrue(p.menuProgress(4,2,false));int length=Ps4TutorialProgress.menuLength(1);
  var restored=Ps4TutorialProgress.load(p.save(),500);assertTrue(restored.completedMenus.contains(1));assertEquals(Integer.valueOf(length),restored.menus.get(1));assertEquals(Integer.valueOf(2),restored.menus.get(4));assertFalse(restored.completedMenus.contains(4));
  assertFalse(restored.menuProgress(4,1,false));assertEquals(Integer.valueOf(2),restored.menus.get(4));assertFalse(restored.menuProgress(-1,0,true));assertFalse(restored.menuProgress(4,10000,false));
 }
 @Test public void oldSaveInfersOnlyGuidesWhoseCompletionWasAlreadyProven(){
  var p=new Ps4TutorialProgress();p.done[9]=true;p.stage=10;var old=p.save();old.remove("MenuProgress");old.remove("CompletedMenus");var restored=Ps4TutorialProgress.load(old,20);assertTrue(restored.completedMenus.contains(1));assertFalse(restored.completedMenus.contains(4));
  var fresh=new Ps4TutorialProgress();fresh.stage=9;var unfinished=fresh.save();unfinished.remove("MenuProgress");unfinished.remove("CompletedMenus");assertFalse(Ps4TutorialProgress.load(unfinished,20).completedMenus.contains(1));
 }
 @Test public void synchronizationRetainsMenusAlongsideGameplayFlagsAndCraftingHistory(){
  var p=new Ps4TutorialProgress();p.menuProgress(1,0,true);p.menuProgress(4,3,false);p.stage=10;p.flags=5;p.crafted.put("minecraft:glass",1);p.discs.set(2);
  var bytes=new FriendlyByteBuf(Unpooled.buffer());try{new TutorialState(p).encode(bytes);var packet=new TutorialState(bytes);assertEquals(p.stage,packet.stage);assertEquals(p.flags,packet.flags);assertEquals(p.crafted,packet.crafted);assertEquals(p.menus,packet.menus);assertEquals(p.completedMenus,packet.completedMenus);assertEquals(1,packet.discs);assertEquals(0,bytes.readableBytes());}finally{bytes.release();}
 }
}

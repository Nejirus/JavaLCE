package dev.legacyconsole.tutorial;

import static org.junit.Assert.*;
import dev.legacyconsole.client.Ps4Descriptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import org.junit.*;

public class Ps4TutorialTest {
 @BeforeClass public static void boot(){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();}
 @Test public void nativeTaskOrderAndTimingGateInput(){
  assertEquals(43,Ps4TutorialScript.SOURCE.gameplay.size());assertEquals(45,Ps4TutorialScript.GAMEPLAY.size());
  var p=new Ps4TutorialProgress();assertFalse(p.acknowledge(true,39));assertTrue(p.acknowledge(true,40));assertEquals("ControllerTask",p.current().kind);
  p.controls((1<<(31-25))|(1<<(29-25)),41);assertEquals(2,p.stage);
  p.controls((1<<(25-25))|(1<<(27-25)),42);assertEquals(3,p.stage);assertEquals("InfoTask",p.current().kind);
 }
 @Test public void basicChoiceCanReleaseTheOriginalAreaWithoutCompletingTheExtendedTrack(){
  var p=new Ps4TutorialProgress();while(!Ps4TutorialScript.GAMEPLAY.get(p.stage).kind.equals("ChoiceTask"))p.stage++;
  assertTrue(p.restricted());assertTrue(p.acknowledge(false,40));assertEquals(Ps4TutorialScript.finalIndex(),p.stage);assertFalse(p.restricted());
  assertEquals("857669aa",p.current().description_hash);
 }
 @Test public void areaConstraintAllowsReturnTowardTheSourceBox(){
  assertTrue(Ps4TutorialWorld.permits(-15,71,85,0,72,90));assertFalse(Ps4TutorialWorld.permits(-15,71,85,21,71,90));
  assertTrue(Ps4TutorialWorld.permits(30,71,90,25,71,90));assertFalse(Ps4TutorialWorld.permits(30,71,90,35,71,90));
 }
 @Test public void discGoalChecksSourceTagAndSurvivesSaveReload(){
  var data=new Ps4TutorialWorld();var p=new Ps4TutorialProgress();var disc=new ItemStack(Items.MUSIC_DISC_13);
  assertFalse(data.collectDisc(p,disc));disc.getOrCreateTag().putInt("4jdata",2);assertFalse(data.collectDisc(p,disc));
  disc.getOrCreateTag().putInt("4jdata",1);assertTrue(data.collectDisc(p,disc));assertFalse(data.collectDisc(p,disc));
  p.stage=7;p.counts[7]=1;var restored=Ps4TutorialProgress.load(p.save(),100);assertEquals(7,restored.stage);assertEquals(1,restored.counts[7]);assertEquals(1,restored.discs.cardinality());
 }
 @Test public void descriptionsPreferOriginalPs4AndUseLegacy4jForNewBlocks(){
  assertTrue(Ps4Descriptions.fromPs4(new ItemStack(Items.STONE)));
  assertEquals("Can be mined with a pickaxe to collect cobblestone.",Ps4Descriptions.get(new ItemStack(Items.STONE)));
  assertFalse(Ps4Descriptions.fromPs4(new ItemStack(Items.BEEHIVE)));
  assertEquals("An artificial alternative to the Bee Nest crafted with 3 Honeycombs and 6 Planks.",Ps4Descriptions.get(new ItemStack(Items.BEEHIVE)));
  assertFalse(Ps4Descriptions.get(new ItemStack(Items.DEEPSLATE)).isEmpty());
 }
 @Test public void legacyItemSelectorsResolveFlattenedVariants(){
  assertTrue(Ps4TutorialScript.resolve("log",0).contains(Items.OAK_LOG));
  assertTrue(Ps4TutorialScript.resolve("coal",1).contains(Items.CHARCOAL));
  assertTrue(Ps4TutorialScript.resolve("record_13",0).contains(Items.MUSIC_DISC_13));
  assertTrue(Ps4TutorialScript.icon(364).is(Items.COOKED_BEEF));
  assertTrue(Ps4TutorialScript.icon(355).is(Items.RED_BED));
 }
 @Test public void sourceInputActionsCannotDismissTheWrongPrompt(){
  var p=new Ps4TutorialProgress();assertFalse(p.acknowledge(false,40));assertEquals(0,p.stage);
  p.stage=p.done.length-1;p.activated=0;assertArrayEquals(new int[]{0},p.current().actions);
  assertFalse(p.acknowledge(false,40));assertTrue(p.acknowledge(true,40));assertNull(p.current());
 }
 @Test public void craftingHistoryDistinguishesCharcoalFromCoalAndSurvivesReload(){
  var p=new Ps4TutorialProgress();p.event("CraftTask",new ItemStack(Items.CHARCOAL),5);
  var restored=Ps4TutorialProgress.load(p.save(),10);assertEquals(Integer.valueOf(1),restored.crafted.get("minecraft:charcoal"));assertFalse(restored.crafted.containsKey("minecraft:coal"));
  var furnace=Ps4TutorialScript.flatten(Ps4TutorialScript.SOURCE.guided_states.get(4));
  var task=furnace.stream().filter(t->t.kind.equals("CraftTask")).findFirst().orElseThrow();assertEquals(-1,task.items.get(0).aux);assertTrue(task.matches(new ItemStack(Items.CHARCOAL)));assertTrue(task.matches(new ItemStack(Items.COAL)));
 }
}

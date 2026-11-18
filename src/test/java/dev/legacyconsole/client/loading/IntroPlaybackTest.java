package dev.legacyconsole.client.loading;

import static org.junit.Assert.*;
import org.junit.Test;

public class IntroPlaybackTest {
 @Test public void loadingHoldsTheFinalLogoUntilNativeResourcesAreReady() {
  IntroPlayback intro=new IntroPlayback();
  var view=intro.frame(60000,false,false);assertEquals(2,view.stage());assertEquals(1,view.opacity(),0);assertFalse(view.complete());
  assertTrue(intro.frame(60001,true,false).complete());
 }
 @Test public void aFreshConfirmSkipsTheEntireIntroFromEveryLogoWhenReady() {
  for(long time:new long[]{1000,9000,13000}) {
   IntroPlayback intro=new IntroPlayback();intro.frame(time,true,false);
   assertTrue(intro.frame(time+1,true,true).complete());
  }
 }
 @Test public void unreadyConfirmAdvancesOneLogoAndHoldingDoesNotRepeat() {
  IntroPlayback intro=new IntroPlayback();intro.frame(1000,false,false);
  assertEquals(1,intro.frame(1001,false,true).stage());
  assertEquals(1,intro.frame(1500,false,true).stage());
  intro.frame(1501,false,false);assertEquals(2,intro.frame(1502,false,true).stage());
  assertFalse(intro.frame(1503,false,true).complete());
 }
 @Test public void pressingDuringUnready4JCannotBypassLoadingOrBecomeADeferredSkip() {
  IntroPlayback intro=new IntroPlayback();intro.frame(12500,false,false);
  assertFalse(intro.frame(12501,false,true).complete());
  assertFalse(intro.frame(12502,true,true).complete());
  intro.frame(12503,true,false);assertTrue(intro.frame(12504,true,true).complete());
 }
 @Test public void confirmationRemainsConsumedUntilReleaseAndThenANewPressCanSelectPlay() {
  IntroPlayback intro=new IntroPlayback();intro.frame(1000,true,false);intro.frame(1001,true,true);
  assertTrue(intro.confirmBlocked(true));assertTrue(intro.confirmBlocked(true));
  assertFalse(intro.confirmBlocked(false));assertFalse(intro.confirmBlocked(true));
 }
 @Test public void aButtonAlreadyHeldWhenTheSceneStartsIsNotANewConfirmation() {
  IntroPlayback intro=new IntroPlayback();assertFalse(intro.frame(1000,true,true).complete());
  intro.frame(1001,true,false);assertTrue(intro.frame(1002,true,true).complete());
 }
 @Test public void naturalFadesRetainTheSourceFrameBoundaries() {
  IntroPlayback intro=new IntroPlayback();
  assertEquals(0,intro.frame(8667,false,false).opacity(),0);
  assertEquals(0,intro.frame(8700,false,false).opacity(),0);
  assertEquals(1,intro.frame(9000,false,false).opacity(),0);
  assertEquals(0,intro.frame(11800,false,false).opacity(),0);
  assertEquals(1,intro.frame(12134,false,false).opacity(),0);
 }
}

package dev.legacyconsole.world;
import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
public class SleepAnnouncementsTest {
 private final UUID first=new UUID(0,1),second=new UUID(0,2);
 @Test public void onlyNewSleepersAnnounceWhileOthersAreAwake(){
  assertEquals(Set.of(first),LceSleepAnnouncements.newSleepers(Set.of(),Set.of(first),2));
  assertEquals(Set.of(),LceSleepAnnouncements.newSleepers(Set.of(first),Set.of(first),2));
  assertEquals(Set.of(second),LceSleepAnnouncements.newSleepers(Set.of(first),Set.of(first,second),3));
 }
 @Test public void allAsleepAndWakeChangesDoNotCreateCounterOrSkippingAnnouncements(){
  assertTrue(LceSleepAnnouncements.newSleepers(Set.of(first),Set.of(first,second),2).isEmpty());
  assertTrue(LceSleepAnnouncements.newSleepers(Set.of(first),Set.of(),2).isEmpty());
 }
 @Test public void sourceMessagesDistinguishTheSleeperAndOtherRecipients(){
  assertEquals("You are sleeping in a bed. To skip to dawn, all players need to sleep in beds at the same time.",LceSleepAnnouncements.message(true,"Alex"));
  assertEquals("Alex is sleeping in a bed. To skip to dawn, all players need to sleep in beds at the same time.",LceSleepAnnouncements.message(false,"Alex"));
 }
}

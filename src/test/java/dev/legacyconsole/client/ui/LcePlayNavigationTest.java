package dev.legacyconsole.client.ui;

import static org.junit.Assert.*;
import org.junit.Test;

public class LcePlayNavigationTest {
  @Test public void tutorialFollowsCreateEvenWithoutSavesAndEmptyJoinCannotTakeFocus() {
    LcePlayNavigation nav=new LcePlayNavigation(5,1);nav.update(0,0);
    assertEquals(2,nav.count[0]);assertEquals(0,nav.column);
    assertEquals(100,nav.id());assertFalse(nav.switchColumn());
    assertTrue(nav.move(1));assertEquals(LcePlayNavigation.PLAY_TUTORIAL,nav.selected[0]);
    assertFalse(nav.move(1));assertTrue(nav.move(-1));assertFalse(nav.move(-1));
  }

  @Test public void sourcePageNavigationAndColumnsKeepIndependentSelections() {
    LcePlayNavigation nav=new LcePlayNavigation(5,0);nav.update(12,8);
    nav.page(1);assertEquals(4,nav.selected[0]);assertEquals(0,nav.first[0]);
    nav.page(1);assertEquals(9,nav.selected[0]);assertEquals(5,nav.first[0]);
    nav.switchColumn();nav.move(3);
    assertEquals(3,nav.selected[1]);assertEquals(0,nav.first[1]);
    nav.switchColumn();assertEquals(9,nav.selected[0]);assertEquals(5,nav.first[0]);
    nav.page(-1);assertEquals(5,nav.selected[0]);
    nav.page(-1);assertEquals(0,nav.selected[0]);assertEquals(0,nav.first[0]);
    assertFalse(nav.move(-1));nav.move(100);assertEquals(13,nav.selected[0]);
  }

  @Test public void disappearingGamesClampFocusAndVisibleRows() {
    LcePlayNavigation nav=new LcePlayNavigation(5,1);nav.update(10,12);nav.move(11);
    assertEquals(7,nav.first[1]);nav.update(2,1);
    assertEquals(0,nav.selected[1]);assertEquals(0,nav.first[1]);
    nav.update(0,0);assertEquals(0,nav.column);assertEquals(0,nav.selected[0]);
    for(int i=0;i<100;i++)assertNotEquals(LcePlayNavigation.id(0,i),LcePlayNavigation.id(1,i));
  }
}

package dev.legacyconsole.client;

import static org.junit.Assert.*;

import java.util.*;
import org.junit.Test;

public class LceMusicPlaylistTest {
  private static final class Scripted extends Random {
    final int[] values;
    int calls;

    Scripted(int... values) {
      this.values = values;
    }

    @Override
    public int nextInt(int bound) {
      int v = values[calls++ % values.length];
      assertTrue(v >= 0 && v < bound);
      return v;
    }
  }

  @Test
  public void overworldIncludesClassicCreativeAndMenuTracksInSourceOrder() {
    for (int i = 0; i < 22; i++) {
      LceMusicPlaylist pool = new LceMusicPlaylist(new Scripted(i));
      assertEquals(LceMusicPlaylist.TRACKS[i], pool.next(0));
    }
    assertEquals("creative1", LceMusicPlaylist.TRACKS[9]);
    assertEquals("menu1", LceMusicPlaylist.TRACKS[15]);
    assertEquals("piano3", LceMusicPlaylist.TRACKS[21]);
  }

  @Test
  public void retriesHeardTracksOnlyTheSourceNumberOfTimes() {
    Scripted random = new Scripted(0);
    LceMusicPlaylist pool = new LceMusicPlaylist(random);
    assertEquals("calm1", pool.next(0));
    assertEquals("calm1", pool.next(0));
    assertEquals(12, random.calls); // first pick + eleven bounded retries, not a shuffle.
  }

  @Test
  public void allHeardFlagsResetForThatRange() {
    Scripted random = new Scripted(0, 1, 2, 3, 0);
    LceMusicPlaylist pool = new LceMusicPlaylist(random);
    for (int i = 0; i < 4; i++) assertEquals("nether" + (i + 1), pool.next(-1));
    assertEquals("nether1", pool.next(-1));
    assertEquals(5, random.calls);
  }

  @Test
  public void noInitialDelayAndCompletionDelayCanReach3599SourceFrames() {
    LceMusicPlaylist pool = new LceMusicPlaylist(new Scripted(3599));
    assertTrue(pool.ready());
    pool.completed();
    for (int i = 0; i < 3599; i++) assertFalse(pool.ready());
    assertTrue(pool.ready());
  }

  @Test
  public void zeroDelayAndEndTrackFollowSourceDefaultTextureBehavior() {
    LceMusicPlaylist pool = new LceMusicPlaylist(new Scripted(0));
    pool.completed();
    assertTrue(pool.ready());
    assertEquals("the_end_dragon_alive", pool.next(1));
    assertEquals("music/game/end/boss", LceMusicPlaylist.asset(pool.next(1)));
  }
}

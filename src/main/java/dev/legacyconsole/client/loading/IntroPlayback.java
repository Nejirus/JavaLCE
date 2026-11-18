package dev.legacyconsole.client.loading;

/** LCE scene-owned confirmation, with loading readiness replacing the console's completed boot. */
public final class IntroPlayback {
 public record View(int stage,long time,float opacity,boolean complete) {}
 private long offset;
 private boolean sampled,wasHeld,finishRequested,releaseRequired;
 public View frame(long elapsed,boolean ready,boolean held) {
  long time=Math.max(0,elapsed+offset);
  boolean pressed=sampled&&held&&!wasHeld;sampled=true;wasHeld=held;
  if(pressed) {
   if(ready)finishRequested=true;
   else {
    int stage=Math.min(2,StartupSequence.stage(time));
    if(stage<2){long start=StartupSequence.stageStart(stage+1);offset=start-elapsed;time=start;}
   }
  }
  boolean complete=ready&&(finishRequested||time>=StartupSequence.DURATION_MS);
  if(complete)releaseRequired=held;
  return new View(Math.min(2,StartupSequence.stage(time)),time,StartupSequence.opacity(time),complete);
 }
 public boolean confirmBlocked(boolean held) {
  if(releaseRequired&&!held)releaseRequired=false;
  return releaseRequired;
 }
}

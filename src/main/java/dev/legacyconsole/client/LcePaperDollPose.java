package dev.legacyconsole.client;

/** Gui.cpp's fixed xd=-40 / yd=10, instead of the inventory's cursor-following pose. */
public final class LcePaperDollPose {
 public static final float YAW=(float)Math.atan(-40f/40)*20;
 public static final float PITCH=(float)Math.atan(10f/40)*20;
 private LcePaperDollPose() {}
 public static float cancelBodyYaw(float bodyYaw){return bodyYaw-(180+YAW);}
}

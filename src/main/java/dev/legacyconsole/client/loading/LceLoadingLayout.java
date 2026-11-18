package dev.legacyconsole.client.loading;

/** FullscreenProgress720 and skin.swf::FJ_ProgressBar_Loading coordinates. */
public final class LceLoadingLayout {
  public static final long TIP_INTERVAL_MS = 7000;
  public record Bounds(float x,float y,float width,float height) {}
  private LceLoadingLayout() {}
  public static float progress(float value) {
    return Float.isFinite(value) ? Math.max(0, Math.min(1, value)) : 0;
  }
  public static Bounds bar(int width,int height) { return project(width,height,320,390,640,20); }
  public static Bounds fill(int width,int height,float progress) {
    return project(width,height,322,392,636*progress(progress),16);
  }
  public static Bounds tips(int width,int height) { return project(width,height,240,510,800,110); }
  public static Bounds project(int width,int height,float x,float y,float w,float h) {
    float scale=dev.legacyconsole.client.ui.LceViewport.scale(width,height);
    return new Bounds(dev.legacyconsole.client.ui.LceViewport.x(width,height,x),
        dev.legacyconsole.client.ui.LceViewport.y(width,height,y),w*scale,h*scale);
  }
}

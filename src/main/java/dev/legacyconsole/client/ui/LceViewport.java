package dev.legacyconsole.client.ui;

/** UIScene's standard 480-line path and uniform 720-scene fallback. */
public final class LceViewport {
  private LceViewport() {}

  public static boolean standard(int width, int height) {
    return width > 0 && height > 0 && (long)width * 3 <= (long)height * 4;
  }

  public static float scale(int width, int height) {
    float scale = height / 720f;
    return standard(width,height) ? Math.min(width / 1280f, scale) : scale;
  }

  /** IUIScene_AbstractContainerMenu::UpdatePointer: SD scenes use 0.6 input scale. */
  public static float cursorInputScale(int width, int height) {
    return standard(width,height) ? .6f : 1;
  }

  public static float x(int width, int height, float sourceX) {
    float scale = scale(width,height);
    return (width - 1280 * scale) / 2 + sourceX * scale;
  }

  public static float y(int width, int height, float sourceY) {
    float scale = scale(width,height);
    float offset = standard(width,height) ? (height - 720 * scale) / 2 : 0;
    return offset + sourceY * scale;
  }
}

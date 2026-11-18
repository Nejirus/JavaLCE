package dev.legacyconsole.client.input;

/** GLFW window points, framebuffer pixels and GUI units are distinct on Retina displays. */
public record CursorCoordinates(int framebufferWidth, int framebufferHeight,
    int windowWidth, int windowHeight, int guiWidth, int guiHeight) {
  public record Point(double x, double y) {}

  public CursorCoordinates {
    framebufferWidth = Math.max(1, framebufferWidth);
    framebufferHeight = Math.max(1, framebufferHeight);
    windowWidth = Math.max(1, windowWidth);
    windowHeight = Math.max(1, windowHeight);
    guiWidth = Math.max(1, guiWidth);
    guiHeight = Math.max(1, guiHeight);
  }

  public Point toGui(double windowX, double windowY) {
    return new Point(windowX * guiWidth / windowWidth, windowY * guiHeight / windowHeight);
  }

  public Point toWindow(double guiX, double guiY) {
    return new Point(guiX * windowWidth / guiWidth, guiY * windowHeight / guiHeight);
  }

  /** Compatibility with the old bottom-origin pixel API; GUI warps bypass its quantization. */
  public Point toLegacy(double windowX, double windowY) {
    return new Point((int)(windowX * framebufferWidth / windowWidth),
        framebufferHeight - 1 - (int)(windowY * framebufferHeight / windowHeight));
  }

  public Point legacyToWindow(int x, int y) {
    return new Point(x * windowWidth / (double)framebufferWidth,
        (framebufferHeight - y - 1) * windowHeight / (double)framebufferHeight);
  }
}

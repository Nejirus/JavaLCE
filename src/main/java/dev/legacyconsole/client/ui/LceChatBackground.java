package dev.legacyconsole.client.ui;

/** HUD720/1080 Label1Background: viewport-wide shape, RGB 51 and authored alpha 127. */
public final class LceChatBackground {
  public record Span(int left,int right) {}
  private LceChatBackground() {}
  public static Span span(int screenWidth,float translatedX,float scale) {
    return new Span((int)Math.floor(-translatedX/scale),
        (int)Math.ceil((screenWidth-translatedX)/scale));
  }
  public static int color(int nativeColor) { return (nativeColor&0xff000000)|0x333333; }
}

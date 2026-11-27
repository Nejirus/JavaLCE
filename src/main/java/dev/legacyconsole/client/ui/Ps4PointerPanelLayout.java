package dev.legacyconsole.client.ui;

/** Native pointer/HTML panel sizing; glyph height is character zero's extent. */
public final class Ps4PointerPanelLayout {
  private Ps4PointerPanelLayout() {}
  public record Panel(float width,float height,float textX,float textY) {}

  public static Panel layout(float textWidth,float textHeight,float glyphHeight,boolean trading) {
    int glyph=(int)glyphHeight;
    // 0x7d57c0 / 0x7d5920 and 0x864f80. The authoring-time (3,5)
    // offset is replaced by these font-relative offsets when text is assigned.
    return new Panel(Math.round(textWidth+glyph*.8f),textHeight+glyph,
        Math.round(glyph*.35f),trading?glyph/2:Math.round(glyph*.5f));
  }
}

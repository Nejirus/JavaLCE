package dev.legacyconsole.client.ui;

/** mcps4 FJ_SlotList_Slot::addSlot (0x83b9b0), separate from its highlight. */
public final class Ps4ItemGeometry {
  private Ps4ItemGeometry() {}

  /** Native holders have a 20-unit bitmap and a rounded, one-unit bevel. */
  public static float[] viewport(float x, float y, float width, float height, float sceneScale) {
    float border = Math.round(width / sceneScale / 20f) * sceneScale;
    return new float[] {x + border, y + border, width - 2 * border, height - 2 * border};
  }

  /** skinHDInGame's iggy_pointerIcon placeholder, independent of CursorImage. */
  public static float[] carried(float pointerX, float pointerY, float sceneScale) {
    return new float[] {pointerX - 31 * sceneScale, pointerY - 31 * sceneScale,
        65 * 1.107696533203125f * sceneScale, 59 * 1.2203369140625f * sceneScale};
  }
}

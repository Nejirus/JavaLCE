package dev.legacyconsole.client.ui;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.junit.Test;

public class LcePromptTextTest {
  @Test
  public void runtimeAtlasRetainsCellBearingsAndBitmapAdvances() {
    LcePromptFont font = LcePromptFont.INSTANCE;
    assertEquals(453, font.glyphs.size());
    assertEquals(8, font.cellWidth);
    assertEquals(13, font.cellHeight);
    assertArrayEquals(new int[] {1, 4, 6, 11}, font.glyph('C').ink);
    assertArrayEquals(new int[] {1, 6, 6, 12}, font.glyph('g').ink);
    assertEquals(6, font.width("C"));
    assertEquals(3, font.width("l"));
    assertEquals(5, font.width(" "));
    assertEquals(10, font.width("  "));
  }

  @Test
  public void textInkIsCenteredForCapitalsDescendersAndEveryGlyphFamily() {
    for (String family : new String[] {"ps4", "ps3", "xbox360", "xboxone"})
      for (int height : new int[] {720, 1080, 1440, 2160}) {
        LceGlyphLayout.Family f = LceGlyphLayout.family(family, height);
        for (String glyph : f.glyphs.keySet())
          for (String label : new String[] {"Select", "Inventory", "Change Group", "Show Ingredients"}) {
            float[] ink = LcePromptFont.INSTANCE.ink(label), symbol = f.glyph(glyph).bounds;
            float inkCenter = f.labelY(glyph, label) + (ink[2] + ink[3]) / 2;
            assertEquals((symbol[2] + symbol[3]) / (2 * f.units), inkCenter, .00001f);
          }
      }
  }

  @Test
  public void gapIncludesTheActualRuntimeFontBearing() {
    for (int height : new int[] {720, 1080, 1440, 2160}) {
      LceGlyphLayout.Family f = LceGlyphLayout.family("ps4", height);
      float inkLeft = f.labelX("cross") + LcePromptFont.INSTANCE.ink("Select")[0];
      float iconRight = f.glyph("cross").bounds[1] / f.units;
      assertEquals(1 + 2 / f.units, inkLeft - iconRight, .00001f);
    }
  }

  @Test
  public void copiedAtlasMatchesTheMetricsAtOpaquePixels() throws Exception {
    LcePromptFont font = LcePromptFont.INSTANCE;
    BufferedImage atlas = ImageIO.read(getClass().getResourceAsStream(
        "/assets/legacyconsole/textures/" + font.atlas + ".png"));
    for (LcePromptFont.Glyph glyph : font.glyphs.values()) {
      int left = 8, top = 13, right = 0, bottom = 0;
      for (int y = 0; y < 13; y++) for (int x = 0; x < 8; x++)
        if ((atlas.getRGB(glyph.x + x, glyph.y + y) >>> 24) != 0) {
          left = Math.min(left, x); top = Math.min(top, y);
          right = Math.max(right, x + 1); bottom = Math.max(bottom, y + 1);
        }
      if (right == 0) assertNull(glyph.ink);
      else assertArrayEquals(new int[] {left, top, right, bottom}, glyph.ink);
    }
  }

  @Test
  public void alphaUploadMatchesCFontDataAndPreservesTranslucency() {
    BufferedImage original = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
    original.setRGB(0, 0, 0x40ebebeb);
    original.setRGB(1, 0, 0x00123456);
    BufferedImage mask = LceFontTexture.alphaMask(original);
    assertEquals(0x40ffffff, mask.getRGB(0, 0));
    assertEquals(0x00ffffff, mask.getRGB(1, 0));
    assertEquals(0x40ebebeb, original.getRGB(0, 0));
  }

  @Test
  public void unsupportedCodepointAndCurlyQuoteUseSourceFallbacks() {
    LcePromptFont font = LcePromptFont.INSTANCE;
    assertSame(font.glyph('?'), font.glyph(0x1f642));
    assertSame(font.glyph('\''), font.glyph(0x2019));
    assertEquals("Chan", font.fit("Change Group", font.width("Chan")));
  }

  @Test
  public void craftingPanelCycleAndPromptsFollowTheSource() {
    LceCraftingView view = LceCraftingView.INVENTORY;
    assertArrayEquals(new String[] {"cross", "Create", "circle", "Exit", "square", "Show Description",
        "left_bumper", "", "right_bumper", "Change Group"}, view.prompts(true));
    assertEquals(LceCraftingView.DESCRIPTION, view.next());
    assertEquals("Show Ingredients", view.next().nextLabel);
    assertEquals("Show Inventory", view.next().next().nextLabel);
    assertSame(view, view.next().next().next());
    for (LceCraftingView state : LceCraftingView.values()) {
      String all = String.join("|", state.prompts(false));
      assertFalse(all.contains("cross"));
      assertFalse(all.contains("triangle"));
      assertFalse(all.contains("Craft All"));
      assertFalse(all.contains("Craftable"));
    }
  }

  @Test
  public void craftingDescriptionsComeFromTheOriginalBindings() {
    assertEquals("Allows you to craft a more varied selection of items than the normal crafting.",
        LceCraftingDescriptions.get(58, 0));
    assertTrue(LceCraftingDescriptions.get(280, 0).startsWith("Used to craft torches"));
    assertEquals("", LceCraftingDescriptions.get(442, 0));
  }
}

package dev.legacyconsole.client.ui;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.Test;

public class LceGlyphLayoutTest {
  @Test
  public void ps4NormalFramesRetainBothSourceLayersAndTheirPadding() {
    LceGlyphLayout.Family sd = LceGlyphLayout.family("ps4", 720);
    LceGlyphLayout.Glyph face = sd.glyph("cross");
    assertArrayEquals(new float[] {0, 32, 5, 37}, face.bounds, .001f);
    assertEquals(2, face.layers.length);
    assertEquals(31.99365234375, face.layers[0].width, .00001);
    assertEquals(1, face.layers[1].x, .001);
    assertEquals(6, face.layers[1].y, .001);
    assertEquals(30, face.layers[1].width, .03);
    assertArrayEquals(new float[] {0, 49, 0, 45}, sd.glyph("left_bumper").bounds, .001f);
    assertEquals(2, sd.units, 0);
  }

  @Test
  public void highDefinitionUsesTheNormalTooltipRatherThanTheLargeButtonExport() {
    LceGlyphLayout.Family hd = LceGlyphLayout.family("ps4", 1080);
    assertArrayEquals(new float[] {0, 48, 4, 52}, hd.glyph("cross").bounds, .001f);
    assertEquals(48, hd.glyph("cross").layers[0].width, .001);
    assertEquals(-6, hd.glyph("left_bumper").layers[0].x, .001);
    assertEquals(74, hd.glyph("left_bumper").layers[0].width, .001);
    assertEquals(3, hd.units, 0);
    assertSame(hd, LceGlyphLayout.family("ps4", 1440));
    assertSame(hd, LceGlyphLayout.family("ps4", 2160));
  }

  @Test
  public void ps4DismountUsesTheLceR3ArtworkAtFaceButtonHeight() {
    for (int height : new int[] {720, 1080, 1440, 2160}) {
      var family = LceGlyphLayout.family("ps4", height);
      var prompts = LceGlyphLayout.footer(family, 640, 360, String::length,
          "cross", "Jump", "right_stick_button", "Dismount");
      var jump = prompts.get(0);
      var dismount = prompts.get(1);
      var cross = family.glyph(jump.glyph).bounds;
      var r3 = family.glyph(dismount.glyph).layers[0];
      assertEquals("lce/prompts/ps4/40", r3.path);
      assertEquals(cross[3] - cross[2], r3.height, .001f);
      assertEquals(jump.y + cross[2] / family.units, dismount.y + r3.y / family.units, .001f);
    }
  }

  @Test
  public void xbox360DismountMatchesTheFaceButtonRowAtBothScales() throws Exception {
    BufferedImage bitmap = ImageIO.read(getClass().getResourceAsStream(
        "/assets/legacyconsole/textures/lce/prompts/xbox360/ButtonRS.png"));
    for (int height : new int[] {720, 1080, 1440, 2160}) {
      var family = LceGlyphLayout.family("xbox360", height);
      var prompts = LceGlyphLayout.footer(family, 640, 360, String::length,
          "cross", "Jump", "right_stick_button", "Dismount");
      var jump = prompts.get(0);
      var dismount = prompts.get(1);
      var face = family.glyph(jump.glyph).bounds;
      var rs = family.glyph(dismount.glyph).layers[0];
      assertEquals("lce/prompts/xbox360/ButtonRS", rs.path);
      assertEquals((face[3] - face[2]) / family.units, rs.height / family.units, .001f);
      assertEquals(jump.y + face[2] / family.units, dismount.y + rs.y / family.units, .001f);
      assertEquals((double) bitmap.getWidth() / bitmap.getHeight(), rs.width / rs.height, .001);
    }
  }

  @Test
  public void reference720CoordinatesMatchTheSourceSafeZoneAndCommonBaseline() {
    LceGlyphLayout.Family sd = LceGlyphLayout.family("ps4", 720);
    List<LceGlyphLayout.Prompt> prompts = LceGlyphLayout.footer(
        sd, 640, 360, s -> s.length() * 4, "cross", "Select");
    LceGlyphLayout.Prompt p = prompts.get(0);
    assertEquals(64, p.x * 2, 0); // UIComponent_Tooltips: 5% of 1280.
    assertEquals(646, p.y * 2, 0); // 720 - 36 safe zone - 38 A/label getRect bottom.
    assertEquals(98, p.textX * 2, 0); // 64 + 32 normal symbol + 2 TextField inset.
    assertEquals(660, (p.textY + LcePromptFont.INSTANCE.ink("Select")[2]) * 2, 0);
    assertEquals(100, (p.textX + LcePromptFont.INSTANCE.ink("Select")[0]) * 2, 0);
    assertEquals(p.y, LceGlyphLayout.footer(sd, 640, 360, s -> 0,
        "left_trigger", "Use").get(0).y, 0); // Hidden A still supplies the baseline.
  }

  @Test
  public void actionsUseTheSourceOrderAndAdvanceByTheirActualWidthsAndTwoSpaces() {
    LceGlyphLayout.Family f = LceGlyphLayout.family("ps4", 720);
    List<LceGlyphLayout.Prompt> p = LceGlyphLayout.footer(f, 640, 360,
        s -> s.length() * 4, "right_bumper", "Change Group", "square", "Craft All",
        "left_bumper", "", "circle", "Back", "cross", "Craft");
    assertEquals("cross", p.get(0).glyph);
    assertEquals("circle", p.get(1).glyph);
    assertEquals("square", p.get(2).glyph);
    assertEquals("left_bumper", p.get(3).glyph);
    assertEquals("right_bumper", p.get(4).glyph);
    assertEquals(16 + 2 + 5 * 4 + 10, p.get(1).x - p.get(0).x, 0);
    assertEquals(24.5 + 2 + 10, p.get(4).x - p.get(3).x, 0);
    for (int i = 1; i < p.size(); i++) assertEquals(p.get(i - 1).nextX, p.get(i).x, 0);
  }

  @Test
  public void allControllerFamiliesHaveEveryActionAndRealSourceBitmaps() throws Exception {
    for (String name : new String[] {"ps4", "ps3", "xbox360", "xboxone"})
      for (int height : new int[] {720, 1080, 1440, 2160}) {
        LceGlyphLayout.Family f = LceGlyphLayout.family(name, height);
        assertEquals(18, f.glyphs.size());
        for (LceGlyphLayout.Glyph glyph : f.glyphs.values()) {
          assertTrue(glyph.bounds[1] > glyph.bounds[0]);
          assertTrue(glyph.bounds[3] > glyph.bounds[2]);
          assertTrue(glyph.layers.length > 0);
          for (LceGlyphLayout.Layer layer : glyph.layers) {
            BufferedImage bitmap = ImageIO.read(getClass().getResourceAsStream(
                "/assets/legacyconsole/textures/" + layer.path + ".png"));
            assertNotNull(bitmap);
            assertTrue(layer.width > 0 && layer.height > 0);
            assertEquals(bitmap.getWidth() / (double) bitmap.getHeight(),
                layer.width / (double) layer.height, .03);
          }
        }
      }
  }

  @Test
  public void everyNormalPromptRemainsOnScreenAtAllFourAutomaticScales() {
    int[] heights = {720, 1080, 1440, 2160}, scales = {2, 3, 4, 6};
    for (String name : new String[] {"ps4", "ps3", "xbox360", "xboxone"})
      for (int i = 0; i < heights.length; i++) {
        LceGlyphLayout.Family f = LceGlyphLayout.family(name, heights[i]);
        List<LceGlyphLayout.Prompt> prompts = LceGlyphLayout.footer(f, 640, 360,
            s -> s.length() * 4, "cross", "Craft", "circle", "Back", "square", "Craft All",
            "triangle", "Craftable", "left_bumper", "", "right_bumper", "Change Group");
        assertTrue(prompts.get(prompts.size() - 1).nextX < 608);
        for (LceGlyphLayout.Prompt p : prompts)
          for (LceGlyphLayout.Layer l : f.glyph(p.glyph).layers) {
            assertTrue((p.y + l.y / f.units) * scales[i] >= 0);
            assertTrue((p.y + (l.y + l.height) / f.units) * scales[i] < heights[i]);
          }
      }
  }

  @Test
  public void sourceFontSpaceAdvanceAppliesWithinLabelsAndBetweenActions() {
    for (int height : new int[] {720, 1080, 1440, 2160}) {
      LceGlyphLayout.Family f = LceGlyphLayout.family("ps4", height);
      // Embedded Mojangles7 uses half an em for space: 20px/2/2 or 30px/2/3.
      assertEquals(5, f.space, 0);
      assertEquals(37, f.labelWidth("Craft All", s -> s.length() * 4), 0);
      assertEquals(10, f.width("cross", "", s -> 0)
          - (f.glyph("cross").bounds[1] + f.textGutter) / f.units, 0);
    }
  }

  @Test
  public void ps3RetainsIts720SkinAndAuthoredNegativeBoundsAtHigherResolutions() {
    LceGlyphLayout.Family f = LceGlyphLayout.family("ps3", 2160);
    assertSame(LceGlyphLayout.family("ps3", 720), f);
    assertArrayEquals(new float[] {-1, 31, 6, 38}, f.glyph("cross").bounds, .001f);
    assertEquals(2, f.units, 0);
  }
}

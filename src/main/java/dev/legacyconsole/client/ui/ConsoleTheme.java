package dev.legacyconsole.client.ui;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.player.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.server.*;
import net.minecraft.client.resources.language.*;
import net.minecraft.client.sounds.*;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.model.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.level.saveddata.maps.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.flat.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.trading.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;
import net.minecraft.resources.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.sounds.*;
import net.minecraft.util.*;
import net.minecraft.server.*;
import net.minecraft.server.level.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.server.IntegratedServer;
import dev.legacyconsole.port.*;
import dev.legacyconsole.port.Gui;
import dev.legacyconsole.port.GlStateManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import dev.legacyconsole.port.ReflectionHelper;

import com.google.gson.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.imageio.ImageIO;



/** Legacy4J's original sprites, including their nine-slice metadata, on the 1.11 renderer. */
public final class ConsoleTheme extends Gui {
  public static final int TEXT = 0xffeeeeee,
      MUTED = 0xffc5c5c5,
      DARK = 0xff383838,
      PANEL = 0xffc6c6c6;
  private static final Map<String, Sprite> SPRITES = new HashMap<>();

  private ConsoleTheme() {}

  public static void lcePanel(int x, int y, int w, int h) {
    String[] rows = {"top", "mid", "bot"}, cols = {"l", "m", "r"};
    int[] widths = {8, w - 16, 8}, heights = {8, h - 16, 8};
    int dy = y;
    for (int i = 0; i < 3; i++) {
      int dx = x;
      for (int j = 0; j < 3; j++) {
        sprite("lce/panel_" + rows[i] + "_" + cols[j], dx, dy, widths[j], heights[i]);
        dx += widths[j];
      }
      dy += heights[i];
    }
  }

  private static final class Sprite {
    final ResourceLocation location;
    int width = 16, height = 16, left, right, top, bottom, logicalW, logicalH;

    Sprite(String path) {
      location = new ResourceLocation("legacyconsole", "textures/" + path + ".png");
      Minecraft mc = Minecraft.getInstance();
      try (InputStream in = mc.getResourceManager().getResource(location).getInputStream()) {
        BufferedImage image = ImageIO.read(in);
        width = image.getWidth();
        height = image.getHeight();
      } catch (IOException ignored) {
      }
      logicalW = width;
      logicalH = height;
      try (Reader reader =
          new InputStreamReader(
              mc.getResourceManager()
                  .getResource(new ResourceLocation(location.toString() + ".mcmeta"))
                  .getInputStream(),
              StandardCharsets.UTF_8)) {
        JsonObject metadata = new JsonParser().parse(reader).getAsJsonObject();
        JsonObject scale = metadata.getAsJsonObject("gui").getAsJsonObject("scaling");
        if (!"nine_slice".equals(scale.get("type").getAsString())) return;
        logicalW = scale.get("width").getAsInt();
        logicalH = scale.get("height").getAsInt();
        JsonElement border = scale.get("border");
        if (border.isJsonPrimitive()) left = right = top = bottom = border.getAsInt();
        else {
          JsonObject b = border.getAsJsonObject();
          left = b.get("left").getAsInt();
          right = b.get("right").getAsInt();
          top = b.get("top").getAsInt();
          bottom = b.get("bottom").getAsInt();
        }
      } catch (Exception ignored) {
      }
    }
  }

  public static void sprite(String path, int x, int y, int w, int h) {
    drawSprite(path, x, y, w, h, 1);
  }

  public static void spriteSize(String path, int x, int y, float w, float h) {
    drawSprite(path, x, y, w, h, 1);
  }

  public static void spriteAt(String path, float x, float y, float w, float h) {
    drawSprite(path, x, y, w, h, 1);
  }

  static void widgetAt(String path,float x,float y,float w,float h) {
    drawSprite(path,x,y,w,h,path.contains("highlighted") ? 1
        : dev.legacyconsole.client.ClientConfig.buttonBrightness);
    GlStateManager.color(1,1,1,1);
  }

  /** Normalized source crop with an unchanged texture-to-screen scale. */
  public static void croppedSprite(
      String path, float x, float y, float w, float h, float u0, float v0, float u1, float v1) {
    Sprite sprite = SPRITES.computeIfAbsent(path, Sprite::new);
    GlStateManager.disableLighting();
    GlStateManager.enableTexture2D();
    GlStateManager.color(1, 1, 1, 1);
    GlStateManager.enableBlend();
    PortGraphics.bind(sprite.location);
    quad(
        x + w * u0,
        y + h * v0,
        w * (u1 - u0),
        h * (v1 - v0),
        sprite.width * u0,
        sprite.height * v0,
        sprite.width * (u1 - u0),
        sprite.height * (v1 - v0),
        sprite.width,
        sprite.height);
  }

  /** Draw a normalized texture region into the complete destination rectangle. */
  public static void regionSprite(
      String path, float x, float y, float w, float h, float u0, float v0, float u1, float v1) {
    regionSprite(path,x,y,w,h,u0,v0,u1,v1,1);
  }

  public static void regionSprite(
      String path, float x, float y, float w, float h, float u0, float v0, float u1, float v1,
      float alpha) {
    Sprite sprite = SPRITES.computeIfAbsent(path, Sprite::new);
    GlStateManager.disableLighting();
    GlStateManager.enableTexture2D();
    GlStateManager.color(1, 1, 1, alpha);
    GlStateManager.enableBlend();
    PortGraphics.bind(sprite.location);
    quad(x, y, w, h, sprite.width * u0, sprite.height * v0,
        sprite.width * (u1 - u0), sprite.height * (v1 - v0), sprite.width, sprite.height);
  }

  /** Retain the source borders; only the flat face can shorten to meet the panel's join. */
  public static void lceTab(String path, float x, float y, float scale, int faceTrim) {
    Sprite source = SPRITES.computeIfAbsent(path, Sprite::new);
    GlStateManager.pushMatrix();
    GlStateManager.translate(x, y, 0);
    GlStateManager.scale(scale, .5f, 1);
    if (faceTrim == 0) sprite(path, 0, 0, source.width, source.height);
    else {
      portion(path, 0, 0, source.width, 12, 0, 0, source.width, 12);
      portion(path, 0, 12, source.width, 60 - faceTrim, 0, 12, source.width, 60);
      portion(
          path,
          0,
          72 - faceTrim,
          source.width,
          source.height - 72,
          0,
          72,
          source.width,
          source.height - 72);
    }
    GlStateManager.popMatrix();
  }

  public static void portion(
      String path, int x, int y, int w, int h, int sx, int sy, int sw, int sh) {
    Sprite sprite = SPRITES.computeIfAbsent(path, Sprite::new);
    GlStateManager.color(1, 1, 1, 1);
    GlStateManager.enableBlend();
    PortGraphics.bind(sprite.location);
    quad(x, y, w, h, sx, sy, sw, sh, sprite.width, sprite.height);
  }

  /** Reveal the fixed-size image without changing its texture-to-screen scale. */
  public static void clippedSprite(
      String path, int x, int y, int w, int h, float horizontal, float vertical, boolean flipY) {
    clippedSprite(path,x,y,w,h,horizontal,vertical,flipY,true);
  }

  public static void clippedSprite(
      String path, int x, int y, int w, int h, float horizontal, float vertical, boolean flipY, boolean bottom) {
    Sprite sprite = SPRITES.computeIfAbsent(path, Sprite::new);
    horizontal = Math.max(0, Math.min(1, horizontal));
    vertical = Math.max(0, Math.min(1, vertical));
    GlStateManager.disableLighting();
    GlStateManager.enableTexture2D();
    GlStateManager.color(1, 1, 1, 1);
    GlStateManager.enableBlend();
    PortGraphics.bind(sprite.location);
    quad(
        x,
        y + (bottom ? h * (1 - vertical) : 0),
        w * horizontal,
        h * vertical,
        0,
        sprite.height * (flipY ? (bottom ? vertical : 1) : (bottom ? 1 - vertical : 0)),
        sprite.width * horizontal,
        sprite.height * vertical * (flipY ? -1 : 1),
        sprite.width,
        sprite.height);
  }

  public static void widget(String path, int x, int y, int w, int h) {
    drawSprite(
        path,
        x,
        y,
        w,
        h,
        path.contains("highlighted") ? 1 : dev.legacyconsole.client.ClientConfig.buttonBrightness);
    GlStateManager.color(1, 1, 1, 1);
  }

  private static void drawSprite(String path, float x, float y, float w, float h, float tint) {
    Sprite s = SPRITES.computeIfAbsent(path, Sprite::new);
    GlStateManager.disableLighting();
    GlStateManager.color(tint, tint, tint, 1);
    GlStateManager.enableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    PortGraphics.bind(s.location);
    if (s.left + s.right + s.top + s.bottom == 0) {
      quad(x, y, w, h, 0, 0, s.width, s.height, s.width, s.height);
      return;
    }
    int l = Math.min(s.left, (int) w / 2),
        r = Math.min(s.right, (int) w / 2),
        t = Math.min(s.top, (int) h / 2),
        b = Math.min(s.bottom, (int) h / 2);
    float[] us = {
      0,
      s.left * s.width / (float) s.logicalW,
      s.width - s.right * s.width / (float) s.logicalW,
      s.width
    };
    float[] vs = {
      0,
      s.top * s.height / (float) s.logicalH,
      s.height - s.bottom * s.height / (float) s.logicalH,
      s.height
    };
    float[] xs = {x, x + l, x + w - r, x + w}, ys = {y, y + t, y + h - b, y + h};
    for (int row = 0; row < 3; row++)
      for (int col = 0; col < 3; col++)
        quad(
            xs[col],
            ys[row],
            xs[col + 1] - xs[col],
            ys[row + 1] - ys[row],
            us[col],
            vs[row],
            us[col + 1] - us[col],
            vs[row + 1] - vs[row],
            s.width,
            s.height);
  }

  static void quad(
      float x,
      float y,
      float w,
      float h,
      float u,
      float v,
      float uw,
      float vh,
      float tw,
      float th) {
    if (w <= 0 || h <= 0) return;
    Tesselator tess = Tesselator.getInstance();
    BufferBuilder buf = tess.getBuilder();
    buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
    buf.vertex(x, y + h, 0).uv(u / tw, (v + vh) / th).endVertex();
    buf.vertex(x + w, y + h, 0).uv((u + uw) / tw, (v + vh) / th).endVertex();
    buf.vertex(x + w, y, 0).uv((u + uw) / tw, v / th).endVertex();
    buf.vertex(x, y, 0).uv(u / tw, v / th).endVertex();
    tess.end();
  }

  public static void background(int width,int height){
    Ps4Panorama.render(width,height);
  }

  public static void progress(
      String path, int x, int y, int w, int h, float progress, boolean vertical) {
    Sprite s = SPRITES.computeIfAbsent(path, Sprite::new);
    PortGraphics.bind(s.location);
    GlStateManager.disableLighting();
    GlStateManager.enableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.color(.4f, .4f, .4f, 1);
    quad(x, y, w, h, 0, 0, s.width, s.height, s.width, s.height);
    GlStateManager.color(1, 1, 1, 1);
    progress = Math.max(0, Math.min(1, progress));
    if (vertical) {
      int filled = (int) (h * progress);
      quad(
          x,
          y + h - filled,
          w,
          filled,
          0,
          s.height * (1 - progress),
          s.width,
          s.height * progress,
          s.width,
          s.height);
    } else {
      int filled = (int) (w * progress);
      quad(x, y, filled, h, 0, 0, s.width * progress, s.height, s.width, s.height);
    }
  }

  public static void panel(int x, int y, int w, int h) {
    sprite("gui/sprites/tiles/small_panel", x, y, w, h);
  }

  public static void recess(int x, int y, int w, int h) {
    sprite("gui/sprites/tiles/panel_recess", x, y, w, h);
  }

  public static void slot(int x, int y, int size, boolean selected) {
    sprite("gui/sprites/container/icon_holder", x, y, size, size);
    if (selected) sprite("gui/sprites/container/slot_highlight", x - 1, y - 1, size + 2, size + 2);
  }

  public static void item(ItemStack stack, int x, int y) {
    if (stack.isEmpty()) return;
    Minecraft mc = Minecraft.getInstance();
    GlStateManager.color(1, 1, 1, 1);
    GlStateManager.enableDepth();
    RenderHelper.enableGUIStandardItemLighting();
    mc.getItemRenderer().renderAndDecorateItem(stack, x, y);
    mc.getItemRenderer().renderGuiItemDecorations(Minecraft.getInstance().font, stack, x, y);
    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableLighting();
    GlStateManager.disableDepth();
  }

  public static void itemScaled(ItemStack stack, int x, int y, int size) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x, y, 0);
    GlStateManager.scale(size / 16f, size / 16f, 1);
    item(stack, 0, 0);
    GlStateManager.popMatrix();
  }

  public static void centeredItem(ItemStack stack, int x, int y, int w, int h, int size) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + (w - size) / 2f, y + (h - size) / 2f, 0);
    GlStateManager.scale(size / 16f, size / 16f, 1);
    item(stack, 0, 0);
    GlStateManager.popMatrix();
  }

  /** Original 49.5px pitch; the open tail meets the small panel's 7.5px border. */
  public static void craftingIconTab(
      int x, int panelY, boolean selected, int index, ItemStack icon) {
    int y = panelY - 37;
    spriteSize(
        "gui/sprites/tiles/"
            + (selected ? "high" : "low")
            + "_tab_"
            + (index == 0 ? "left" : index >= 6 ? "right" : "middle"),
        x,
        y,
        51,
        44.5f);
    centeredItem(icon, x, y, 51, 43, 24);
  }

  public static void text(String value, int x, int y, int color) {
    FontCompat.get().drawString(value, x, y, color);
  }

  public static void centered(String value, int x, int y, int color) {
    text(value, x - FontCompat.get().getStringWidth(value) / 2, y, color);
  }

  public static String fit(net.minecraft.network.chat.Component value, int width) { return fit(value.getString(), width); }

  public static String fit(String value, int width) {
    return FontCompat.get().trimStringToWidth(value, Math.max(0, width));
  }

  public static LceGlyphLayout.Family glyphFamily() {
    return LceGlyphLayout.family(
        dev.legacyconsole.client.input.ControllerGlyphs.current().folder,
        Minecraft.getInstance().getWindow().getHeight());
  }

  /** Draw the authored display list at its native console scale, preserving its padding. */
  public static void glyph(String name, float x, float y) {
    glyph(glyphFamily(), name, x, y);
  }

  public static void glyph(LceGlyphLayout.Family family, String name, float x, float y) {
    GlStateManager.disableDepth();
    for (LceGlyphLayout.Layer layer : family.glyph(name).layers)
      drawSprite(layer.path, x + layer.x / family.units, y + layer.y / family.units,
          layer.width / family.units, layer.height / family.units, 1);
  }

  /** LCE ToolTips.UpdateLayout: 5% safe zone, common baseline, source widths and two spaces. */
  public static void footer(int width, int height, String... actions) {
    LceGlyphLayout.Family family = glyphFamily();
    LcePromptFont font = LcePromptFont.INSTANCE;
    for (LceGlyphLayout.Prompt prompt : LceGlyphLayout.footer(
        family, width, height, font::width, actions)) {
      glyph(family, prompt.glyph, prompt.x, prompt.y);
      promptLabel(prompt.label, prompt.textX, prompt.textY, TEXT, true);
    }
  }

  /** Runtime CFontData alpha masks retain their cell bearings and exact advances. */
  public static void promptLabel(String text, float x, float y, int colour, boolean shadow) {
    LcePromptFont font = LcePromptFont.INSTANCE;
    TextureManager textures = Minecraft.getInstance().getTextureManager();
    ResourceLocation atlas = new ResourceLocation("legacyconsole", "textures/" + font.atlas + ".png");
    if (!(textures.getTexture(atlas) instanceof LceFontTexture))
      textures.register(atlas, new LceFontTexture(atlas));
    PortGraphics.bind(atlas);
    GlStateManager.disableLighting();
    GlStateManager.disableDepth();
    GlStateManager.enableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    if (shadow) drawPromptMask(text, x + 1, y + 1, 0xff000000 | ((colour & 0xfcfcfc) >> 2));
    drawPromptMask(text, x, y, colour);
    GlStateManager.color(1, 1, 1, 1);
  }

  private static void drawPromptMask(String text, float x, float y, int colour) {
    LcePromptFont font = LcePromptFont.INSTANCE;
    GlStateManager.color((colour >> 16 & 255) / 255f, (colour >> 8 & 255) / 255f,
        (colour & 255) / 255f, (colour >>> 24) / 255f);
    for (int cp : text.codePoints().toArray()) {
      LcePromptFont.Glyph glyph = font.glyph(cp);
      if (glyph.ink != null)
        quad(x, y, font.cellWidth, font.cellHeight, glyph.x, glyph.y,
            font.cellWidth, font.cellHeight, font.width, font.height);
      x += glyph.advance;
    }
  }

  /** A list can lay out its controls at their final size, including full-width rows. */
  public interface ButtonBounds {
    int[] buttonBounds();
  }

  public static int[] buttonBounds(GuiButton b) {
    if (b instanceof ButtonBounds) return ((ButtonBounds) b).buttonBounds();
    float scale = dev.legacyconsole.client.ClientConfig.buttonScale;
    int minHeight=b instanceof ConsoleButton && ((ConsoleButton)b).allowCheckbox
        && LceWidgets.toggle(b.displayString)!=null ? ConsoleOptionLayout.CHECKBOX_HEIGHT : 14;
    int w = Math.max(14, Math.round(b.width * scale)),
        h = Math.max(minHeight, Math.round(b.height * scale));
    return new int[] {b.xPosition + (b.width - w) / 2, b.yPosition + (b.height - h) / 2, w, h};
  }

  public static boolean buttonHover(GuiButton b, int mx, int my) {
    float[] exact=LceWidgets.exact(b);
    if (exact!=null) return b.visible && mx>=exact[0] && my>=exact[1]
        && mx<exact[0]+exact[2] && my<exact[1]+exact[3];
    int[] r = buttonBounds(b);
    return b.visible && mx >= r[0] && my >= r[1] && mx < r[0] + r[2] && my < r[1] + r[3];
  }

  public static void nativeButton(GuiButton b, int mx, int my) {
    if (!b.visible) return;
    LceWidgets.button(b,LceWidgets.bounds(b),MenuFocus.focused(b,buttonHover(b,mx,my)));
  }

  public static void buttonPressed(GuiButton button) {
    if(button instanceof ConsoleButton && button.enabled) LceMenuButton.press(button);
  }

  public static void tab(int x, int y, int w, int h, boolean selected, String icon) {
    tabShape(
        x, y + 27, w, selected, icon.equals("structures") ? 0 : icon.equals("decoration") ? 2 : 1);
    sprite("gui/sprites/icon/" + icon, x + (w - 22) / 2, y + 2, 22, 22);
  }

  private static void tabShape(int x, int panelY, int w, boolean selected, int edge) {
    String path =
        "gui/sprites/tiles/"
            + (selected ? "high" : "low")
            + "_tab_"
            + (edge == 0 ? "left" : edge == 2 ? "right" : "middle");
    spriteSize(path, x, panelY - 28, w, 35.5f);
  }

  public static void joinedTab(int x, int panelY, int w, boolean selected, int edge, String label) {
    tabShape(x, panelY, w, selected, edge);
    centered(label, x + w / 2, panelY - 17, DARK);
  }
}

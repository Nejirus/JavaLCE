package dev.legacyconsole.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.io.UncheckedIOException;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;

/** Native Java map-lighting bridge; PS4 unexplored pixels are fully transparent. */
public final class LceMapRenderType extends RenderType {
  private static ShaderInstance mapShader;

  private LceMapRenderType() {
    super("legacyconsole_map", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
        VertexFormat.Mode.QUADS, 256, false, false, () -> {}, () -> {});
  }

  public static void registerShaders(RegisterShadersEvent event) {
    try {
      event.registerShader(new ShaderInstance(event.getResourceManager(),
          new ResourceLocation("legacyconsole", "map"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
          shader -> mapShader = shader);
    } catch (IOException exception) {
      throw new UncheckedIOException("Could not load the legacy map shader", exception);
    }
  }

  public static RenderType map(ResourceLocation texture) {
    return create("legacyconsole_map", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
        VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
            .setShaderState(new ShaderStateShard(() -> mapShader == null
                ? GameRenderer.getRendertypeTextShader() : mapShader))
            .setTextureState(new TextureStateShard(texture, false, false))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setLightmapState(LIGHTMAP)
            .setLayeringState(new LayeringStateShard("legacyconsole_map_alpha",
                OptifineMapAlpha::begin, OptifineMapAlpha::end))
            .createCompositeState(false));
  }
}

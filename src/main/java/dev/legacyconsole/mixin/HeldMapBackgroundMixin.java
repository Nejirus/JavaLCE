package dev.legacyconsole.mixin;

import net.minecraft.client.renderer.*;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

/** The 1.11.2 paper surrounds the inset map; the map texture owns unexplored pixels. */
@Mixin(ItemInHandRenderer.class)
public abstract class HeldMapBackgroundMixin {
  @Shadow @Final private static RenderType MAP_BACKGROUND;
  @Redirect(method="renderMap",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;",ordinal=0))
  private VertexConsumer legacy$paper(MultiBufferSource buffers,RenderType original) {
    return buffers.getBuffer(MAP_BACKGROUND);
  }
}

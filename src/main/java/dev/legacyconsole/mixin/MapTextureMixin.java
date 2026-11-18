package dev.legacyconsole.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

@Mixin(targets="net.minecraft.client.gui.MapRenderer$MapInstance")
public abstract class MapTextureMixin {
  @Unique private float legacy$iconLayer;
  @Inject(method="draw",at=@At("HEAD"))
  private void legacy$resetLayers(PoseStack pose,MultiBufferSource buffers,boolean frame,int light,CallbackInfo ci){
    legacy$iconLayer=dev.legacyconsole.client.ConsoleMapRenderer.ICON_DEPTH;
  }
  @ModifyArg(method="draw",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",ordinal=0),index=2)
  private double legacy$ps4IconLayer(double original){
    float depth=legacy$iconLayer;
    legacy$iconLayer=dev.legacyconsole.client.ConsoleMapRenderer.nextIconLayer(depth);
    return depth;
  }
  @ModifyConstant(method="draw",constant=@Constant(floatValue=-.01f))
  private float legacy$ps4Depth(float original) {
    return dev.legacyconsole.client.ConsoleMapRenderer.IMAGE_DEPTH;
  }
  @Redirect(method="<init>",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/RenderType;text(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
  private net.minecraft.client.renderer.RenderType legacy$mapType(net.minecraft.resources.ResourceLocation texture) {
    return dev.legacyconsole.client.LceMapRenderType.map(texture);
  }
  @Redirect(method="updateTexture",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/platform/NativeImage;setPixelRGBA(III)V",ordinal=0))
  private void legacy$unexplored(NativeImage image,int x,int y,int original) {
    image.setPixelRGBA(x,y,dev.legacyconsole.client.ConsoleMapRenderer.unexploredPixel(x,y));
  }
}

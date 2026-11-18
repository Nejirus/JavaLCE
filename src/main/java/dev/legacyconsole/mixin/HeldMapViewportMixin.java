package dev.legacyconsole.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Java viewport adapter: keep the one-handed map inside narrow viewports.
 * This adjusts the held pose, not PS4 map data or the existing paper inset. */
@Mixin(ItemInHandRenderer.class)
public abstract class HeldMapViewportMixin {
 @Inject(method="renderOneHandedMap",at=@At("HEAD"))
 private void legacy$viewport(PoseStack pose,MultiBufferSource buffers,int light,float equip,HumanoidArm arm,float swing,ItemStack item,CallbackInfo ci){
  var window=Minecraft.getInstance().getWindow();
  float scale=Math.min(1f,(float)window.getWidth()/Math.max(1,window.getHeight())/(16f/9f));
  pose.pushPose();pose.scale(scale,scale,1);
 }
 @Inject(method="renderOneHandedMap",at=@At("RETURN"))
 private void legacy$restoreViewport(PoseStack pose,MultiBufferSource buffers,int light,float equip,HumanoidArm arm,float swing,ItemStack item,CallbackInfo ci){pose.popPose();}
}

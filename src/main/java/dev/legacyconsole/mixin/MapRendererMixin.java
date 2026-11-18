package dev.legacyconsole.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
import com.mojang.blaze3d.vertex.PoseStack;import net.minecraft.client.Minecraft;import net.minecraft.client.gui.MapRenderer;import net.minecraft.client.renderer.MultiBufferSource;import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
@Mixin(MapRenderer.class)
public abstract class MapRendererMixin {
 // PS4 map pass 0xc6fba0: use the complete 0..128 image. Classic's inset is
 // already present in the map data; an extra transform would shrink it twice.
 @Inject(method="render",at=@At("RETURN"))private void legacy$coordinates(PoseStack pose,MultiBufferSource buffers,int id,MapItemSavedData map,boolean frame,int light,CallbackInfo ci){if(frame)return;var mc=Minecraft.getInstance();if(mc.player==null)return;String text=dev.legacyconsole.client.ConsoleMapRenderer.coordinates(mc.player.getX(),mc.player.getY(),mc.player.getZ());pose.pushPose();pose.translate(0,0,dev.legacyconsole.client.ConsoleMapRenderer.TEXT_DEPTH);mc.font.drawInBatch(text,0,0,0xff000000,false,pose.last().pose(),buffers,false,0,light);pose.popPose();}
}

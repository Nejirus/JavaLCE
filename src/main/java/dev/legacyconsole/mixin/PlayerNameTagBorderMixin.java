package dev.legacyconsole.mixin;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EntityRenderer.class)
public abstract class PlayerNameTagBorderMixin {
 @Shadow @Final protected EntityRenderDispatcher entityRenderDispatcher;
 @Shadow public abstract Font getFont();
 @Inject(method="renderNameTag",at=@At("TAIL"))
 private void legacy$playerBorder(Entity entity,Component label,PoseStack pose,MultiBufferSource buffers,int light,CallbackInfo ci){
  if(entity instanceof Player player)dev.legacyconsole.client.Ps4NameTagBorder.draw(player,label,pose,buffers,getFont(),entityRenderDispatcher);
 }
}

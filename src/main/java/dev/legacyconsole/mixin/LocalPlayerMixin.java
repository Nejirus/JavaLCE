package dev.legacyconsole.mixin;

import dev.legacyconsole.client.ClientConfig;
import dev.legacyconsole.client.input.InputMath;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
 @Inject(method="hasEnoughImpulseToStartSprinting",at=@At("HEAD"),cancellable=true)
 private void legacy$sprintThreshold(CallbackInfoReturnable<Boolean> ci){
  LocalPlayer player=(LocalPlayer)(Object)this;
  if(ClientConfig.noSprintStrafe&&!player.isPassenger())
   ci.setReturnValue(InputMath.sprintForwardEnough(player.input.forwardImpulse));
 }
}

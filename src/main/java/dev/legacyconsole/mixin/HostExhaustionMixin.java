package dev.legacyconsole.mixin;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Player.class)
abstract class HostExhaustionMixin {
 @Inject(method="causeFoodExhaustion",at=@At("HEAD"),cancellable=true)
 private void legacy$hostExhaustion(float amount,CallbackInfo ci){if(dev.legacyconsole.world.HostOptions.noExhaustion((Player)(Object)this))ci.cancel();}
}

package dev.legacyconsole.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.world.entity.vehicle.Boat;
@Mixin(Boat.class)
public abstract class BoatMixin {
 @Inject(method="controlBoat",at=@At("HEAD"),cancellable=true)private void legacy$analog(CallbackInfo ci){if(dev.legacyconsole.client.input.ConsoleBoat.analog(this))ci.cancel();}
}

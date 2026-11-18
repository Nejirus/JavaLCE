package dev.legacyconsole.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
@Pseudo @Mixin(targets="net.optifine.shaders.config.ShaderMacros",remap=false)
public abstract class OptifineShaderMacrosMixin {
 @Inject(method="getFixedMacroLines",at=@At("RETURN"),cancellable=true,require=0)private static void legacy$gamma(CallbackInfoReturnable<String> ci){ci.setReturnValue(dev.legacyconsole.client.ConsoleDisplayGamma.shaderHeader(ci.getReturnValue()));}
}

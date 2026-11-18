package dev.legacyconsole.mixin;

import net.minecraft.client.gui.components.toasts.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastComponent.class)
public abstract class ToastComponentMixin {
 @Inject(method="addToast",at=@At("HEAD"),cancellable=true)
 private void legacy$hideJavaTips(Toast toast,CallbackInfo ci){
  if(toast instanceof TutorialToast||toast instanceof RecipeToast)ci.cancel();
 }
}

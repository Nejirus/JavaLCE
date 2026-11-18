package dev.legacyconsole.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.client.gui.components.toasts.*;import net.minecraft.advancements.Advancement;import com.mojang.blaze3d.vertex.PoseStack;
@Mixin(AdvancementToast.class)
public abstract class AdvancementToastMixin {
 @Shadow @Final private Advancement advancement;
 @Unique private static Object legacy$latest;
 @Inject(method="<init>",at=@At("RETURN"))private void legacy$latest(Advancement value,CallbackInfo ci){legacy$latest=this;}
 @Inject(method="render",at=@At("HEAD"),cancellable=true)private void legacy$notification(PoseStack pose,ToastComponent owner,long elapsed,CallbackInfoReturnable<Toast.Visibility> ci){if(!dev.legacyconsole.client.ClientConfig.consoleHud)return;if(legacy$latest!=this){ci.setReturnValue(Toast.Visibility.HIDE);return;}dev.legacyconsole.client.ui.ConsoleAchievement.render(advancement,elapsed);ci.setReturnValue(elapsed<5000?Toast.Visibility.SHOW:Toast.Visibility.HIDE);}
}

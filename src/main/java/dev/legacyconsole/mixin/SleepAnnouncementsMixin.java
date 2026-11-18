package dev.legacyconsole.mixin;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class SleepAnnouncementsMixin {
 @Inject(method="announceSleepStatus",at=@At("HEAD"),cancellable=true)
 private void legacy$sleepMessages(CallbackInfo ci) {
  if(dev.legacyconsole.world.LceSleepAnnouncements.update((ServerLevel)(Object)this))ci.cancel();
 }
}

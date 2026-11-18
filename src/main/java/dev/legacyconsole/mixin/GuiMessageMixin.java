package dev.legacyconsole.mixin;

import dev.legacyconsole.client.ClientConfig;
import dev.legacyconsole.client.LceHudMessages;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMessageMixin {
  @Inject(method="setOverlayMessage",at=@At("HEAD"),cancellable=true)
  private void legacy$bedNotice(Component message,boolean animateColor,CallbackInfo ci) {
    if(ClientConfig.consoleHud&&LceHudMessages.isBedNotice(message)) {
      ((Gui)(Object)this).getChat().addMessage(message);
      ci.cancel();
    }
  }
}

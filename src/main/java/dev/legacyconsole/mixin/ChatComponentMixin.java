package dev.legacyconsole.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector4f;
import dev.legacyconsole.client.ClientConfig;
import dev.legacyconsole.client.ui.LceChatBackground;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
 @Redirect(method="render",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/components/ChatComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V",ordinal=0))
 private void legacy$historyBackground(PoseStack pose,int l,int t,int r,int b,int color) {
   if(ClientConfig.consoleHud) {
     // Include Forge's chat placement and native chat scale in the inverse X transform.
     // This keeps the strip at both viewport edges when chat width/scale or aspect changes.
     Vector4f origin=new Vector4f(0,0,0,1),unit=new Vector4f(1,0,0,1);
     origin.transform(pose.last().pose());unit.transform(pose.last().pose());
     var span=LceChatBackground.span(Minecraft.getInstance().getWindow().getGuiScaledWidth(),
         origin.x(),unit.x()-origin.x());
     l=span.left();r=span.right();color=LceChatBackground.color(color);
   }
   net.minecraft.client.gui.GuiComponent.fill(pose,l,t,r,b,color);
 }
}

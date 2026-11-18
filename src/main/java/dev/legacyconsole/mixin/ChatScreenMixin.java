package dev.legacyconsole.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.client.gui.screens.ChatScreen;import com.mojang.blaze3d.vertex.PoseStack;
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
 @Redirect(method="render",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/screens/ChatScreen;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V"))
 private void legacy$inputBackground(PoseStack pose,int l,int t,int r,int b,int color){if(!((Object)this instanceof dev.legacyconsole.client.ui.ConsoleChatScreen))net.minecraft.client.gui.GuiComponent.fill(pose,l,t,r,b,color);}
}

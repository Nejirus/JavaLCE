package dev.legacyconsole.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;
import dev.legacyconsole.client.ui.*;
/** Preserve the console inner-face highlight beneath native item rendering. */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenMixin {
 @Redirect(method="render",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderSlotHighlight(Lcom/mojang/blaze3d/vertex/PoseStack;IIII)V",remap=false))
 private void legacy$highlight(PoseStack pose,int x,int y,int z,int color){
  if((Object)this instanceof ConsoleContainerScreen screen && screen.replacesNativeHighlight(x,y))return;
  AbstractContainerScreen.renderSlotHighlight(pose,x,y,z,color);
 }
 @Redirect(method="renderSlot",at=@At(value="INVOKE",target="Lnet/minecraft/world/inventory/Slot;getNoItemIcon()Lcom/mojang/datafixers/util/Pair;"))
 private Pair<ResourceLocation,ResourceLocation> legacy$equipment(Slot slot){
  return (Object)this instanceof ConsoleInventory ? null : slot.getNoItemIcon();
 }
}

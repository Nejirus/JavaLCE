package dev.legacyconsole.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.resources.ResourceLocation;
import dev.legacyconsole.client.ui.*;
import dev.legacyconsole.port.GlStateManager;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Preserve the console inner-face highlight beneath native item rendering. */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenMixin {
 @Unique private boolean legacy$scaledCarried;
 @Inject(method="renderFloatingItem",at=@At("HEAD"))
 private void legacy$carriedBegin(ItemStack stack,int x,int y,String count,CallbackInfo ci){
  legacy$scaledCarried=false;
  if((Object)this instanceof ConsoleContainerScreen screen){
   float[] r=screen.floatingItemBounds(x,y);
   if(r!=null){
    GlStateManager.pushMatrix();legacy$scaledCarried=true;
    GlStateManager.translate(r[0],r[1],0);GlStateManager.scale(r[2]/16f,r[3]/16f,1);
    GlStateManager.translate(-x,-y,0);
   }
  }
 }
 @Inject(method="renderFloatingItem",at=@At("RETURN"))
 private void legacy$carriedEnd(ItemStack stack,int x,int y,String count,CallbackInfo ci){
  if(legacy$scaledCarried){GlStateManager.popMatrix();legacy$scaledCarried=false;}
 }
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

package dev.legacyconsole.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** PS4 CartographyMenu accepts both empty and filled maps (0x161d20). */
@Mixin(targets="net.minecraft.world.inventory.CartographyTableMenu$3")
public abstract class CartographyInputSlotMixin {
  // Forge's annotation processor cannot resolve this private anonymous class.
  // Include the supported 1.17.1 SRG name as well as its development name.
  @Inject(method={"mayPlace","m_5857_"},remap=false,at=@At("HEAD"),cancellable=true)
  private void legacy$emptyMap(ItemStack stack,CallbackInfoReturnable<Boolean> result) {
    if(stack.is(Items.MAP))result.setReturnValue(true);
  }
}

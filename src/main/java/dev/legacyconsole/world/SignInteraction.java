package dev.legacyconsole.world;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.level.block.entity.SignBlockEntity;import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
public final class SignInteraction {
 @SubscribeEvent public void use(PlayerInteractEvent.RightClickBlock event){var p=event.getPlayer();if(p.isShiftKeyDown()||p.isSpectator()||!(event.getWorld().getBlockEntity(event.getPos()) instanceof SignBlockEntity sign))return;ItemStack held=p.getItemInHand(event.getHand());if(held.getItem() instanceof DyeItem||held.is(Items.GLOW_INK_SAC)||held.is(Items.INK_SAC))return;event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);if(p instanceof ServerPlayer server){sign.setEditable(true);sign.setAllowedPlayerEditor(server.getUUID());server.openTextEdit(sign);}}
}

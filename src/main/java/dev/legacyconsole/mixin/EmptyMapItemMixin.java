package dev.legacyconsole.mixin;

import dev.legacyconsole.crafting.LegacyMapHistory;
import dev.legacyconsole.crafting.Ps4Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EmptyMapItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EmptyMapItem.class)
public abstract class EmptyMapItemMixin {
 @Redirect(method="use",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/MapItem;create(Lnet/minecraft/world/level/Level;IIBZZ)Lnet/minecraft/world/item/ItemStack;"))
 private ItemStack legacy$open(Level level,int x,int z,byte scale,boolean tracking,boolean unlimited,Level usedLevel,Player player,InteractionHand hand){
  if(Ps4Maps.size(level)==null)return MapItem.create(level,x,z,scale,tracking,unlimited);
  LegacyMapHistory history=LegacyMapHistory.get((ServerLevel)level);byte initialScale=history.nextScale(player.getUUID());
  ItemStack stack=MapItem.create(level,x,z,initialScale,tracking,unlimited);
  Integer id=MapItem.getMapId(stack);
  // PS4 centers the original double position; flooring a negative half-tile first
  // would move a map to the neighboring tile before the actual boundary.
  if(id!=null)level.setMapData(MapItem.makeKey(id),Ps4Maps.fresh(player.getX(),player.getZ(),initialScale,tracking,unlimited,level.dimension()));
  history.opened(player.getUUID());return stack;
 }
}

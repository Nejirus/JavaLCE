package dev.legacyconsole.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.world.item.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.level.Level;import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.core.BlockPos;import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;import net.minecraft.world.level.chunk.*;
import dev.legacyconsole.crafting.Ps4Maps;
@Mixin(MapItem.class)
public abstract class MapItemMixin {
 @Unique private static final ThreadLocal<MapItemSavedData> legacy$source=new ThreadLocal<>();
 @Unique private java.lang.ref.WeakReference<LevelChunk> legacy$emptyMapChunk=new java.lang.ref.WeakReference<>(null);
 @Inject(method="inventoryTick",at=@At("HEAD"))
 private void legacy$registeredMap(ItemStack stack,Level level,net.minecraft.world.entity.Entity owner,int slot,boolean selected,CallbackInfo ci){
  if(Ps4Maps.size(level)!=null&&owner instanceof Player player&&MapItem.getSavedData(stack,level)!=null)dev.legacyconsole.crafting.LegacyMapHistory.get((net.minecraft.server.level.ServerLevel)level).opened(player.getUUID());
 }
 @Inject(method="createNewSavedData",at=@At("HEAD"),cancellable=true)
 private static void legacy$create(Level level,int x,int z,int scale,boolean tracking,boolean unlimited,ResourceKey<Level> dimension,CallbackInfoReturnable<Integer> cir){
  if(Ps4Maps.size(level)==null||dimension!=Level.OVERWORLD)return;
  int id=level.getFreeMapId();level.setMapData(MapItem.makeKey(id),Ps4Maps.fresh(x,z,(byte)scale,tracking,unlimited,dimension));cir.setReturnValue(id);
 }
 /** PS4 ServerChunkCache returns an empty chunk beyond the finite world. */
 @Redirect(method="update",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;getChunkAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/chunk/LevelChunk;"))
 private LevelChunk legacy$sample(Level level,BlockPos pos){
  var size=Ps4Maps.size(level);
  if(size==null||size.containsBlock(pos.getX(),pos.getZ()))return level.getChunkAt(pos);
  LevelChunk empty=legacy$emptyMapChunk.get();
  if(empty==null||empty.getLevel()!=level){empty=new EmptyLevelChunk(level,new ChunkPos(0,0));legacy$emptyMapChunk=new java.lang.ref.WeakReference<>(empty);}
  return empty;
 }
 @Inject(method="onCraftedBy",at=@At("HEAD"))private void legacy$capture(ItemStack stack,Level level,Player player,CallbackInfo ci){legacy$source.set(dev.legacyconsole.crafting.MapCrafting.sourceMap(stack,player));}
 @Inject(method="onCraftedBy",at=@At("RETURN"))private void legacy$grid(ItemStack stack,Level level,Player player,CallbackInfo ci){try{dev.legacyconsole.crafting.MapCrafting.finishUpgrade(stack,player,legacy$source.get());}finally{legacy$source.remove();}}
}

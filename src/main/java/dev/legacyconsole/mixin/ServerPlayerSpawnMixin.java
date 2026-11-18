package dev.legacyconsole.mixin;

import dev.legacyconsole.crafting.Ps4Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** PS4 ServerPlayer constructor spawn branch (0xd001c1..0xd00575), adapted
 * to Java's initial/respawn player construction hook. Entity RNG stays transient. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerSpawnMixin {
 @Inject(method="fudgeSpawnLocation",at=@At("HEAD"),cancellable=true)
 private void legacy$spawn(ServerLevel level,CallbackInfo ci){
  var size=Ps4Maps.size(level);if(size==null)return;
  ServerPlayer player=(ServerPlayer)(Object)this;BlockPos spawn=level.getSharedSpawnPos(),pos=spawn;
  if(!level.dimensionType().hasCeiling()&&level.getServer().getWorldData().getGameType()!=GameType.ADVENTURE){
   int radius=Math.max(0,level.getGameRules().getInt(GameRules.RULE_SPAWN_RADIUS));
   int distance=(int)Math.floor(level.getWorldBorder().getDistanceToBorder(spawn.getX(),spawn.getZ()));
   radius=Math.min(radius,distance);if(distance<2)radius=1;
   int attempts=0,depth;boolean near;
   do{
    int x,z;
    do{x=spawn.getX()+player.getRandom().nextInt(radius*2+1)-radius;z=spawn.getZ()+player.getRandom().nextInt(radius*2+1)-radius;}while(!size.containsBlock(x,z));
    int y=level.getChunk(x>>4,z>>4).getHighestSectionPosition()+16;
    BlockPos.MutableBlockPos scan=new BlockPos.MutableBlockPos(x,y,z);
    while(scan.getY()>0){var state=level.getBlockState(scan.below());if(state.getMaterial().blocksMotion()&&!state.is(BlockTags.LEAVES))break;scan.move(0,-1,0);}
    pos=scan.immutable();
    // Preserve this executable's absolute-Y scan. The older 7th-gen source
    // starts at the ground Y instead; changing that would not match mcps4.
    depth=0;while(depth<128&&level.getBlockState(new BlockPos(x,depth,z)).is(Blocks.WATER))depth++;
    attempts++;near=level.getNearestPlayer(spawn.getX()+.5,spawn.getY(),spawn.getZ()+.5,3,false)!=null;
   }while(depth>1&&!near&&attempts<20);
  }
  player.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);ci.cancel();
 }
}

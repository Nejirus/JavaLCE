package dev.legacyconsole.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.*;

/** Read-only BucketItem::TestUse, with native 1.17 bucketable mobs and waterlogging. */
public final class LceBucketPrompts {
 private LceBucketPrompts() {}
 public static String use(Minecraft mc,Player player,ItemStack stack) {
  if(!(stack.getItem() instanceof BucketItem bucket)||mc.level==null)return null;
  Fluid fluid=bucket.getFluid();
  if(mc.hitResult instanceof EntityHitResult hit) {
   Entity target=hit.getEntity();
   if(fluid==Fluids.EMPTY&&target instanceof Cow cow&&!cow.isBaby()&&cow.isAlive())return LceActionPrompts.label("COLLECT");
   if(stack.is(Items.WATER_BUCKET)&&target instanceof Bucketable&&target.isAlive())return LceActionPrompts.label("COLLECT");
   return null;
  }
  BlockHitResult hit=NativeView.trace(mc.level,player,
      fluid==Fluids.EMPTY?ClipContext.Fluid.SOURCE_ONLY:ClipContext.Fluid.NONE);
  if(hit.getType()!=HitResult.Type.BLOCK)return null;
  BlockPos pos=hit.getBlockPos(),adjacent=pos.relative(hit.getDirection());
  if(!mc.level.mayInteract(player,pos)||!player.mayUseItemAt(adjacent,hit.getDirection(),stack))return null;
  BlockState state=mc.level.getBlockState(pos);
  if(fluid==Fluids.EMPTY)return canCollect(state)?LceActionPrompts.label("COLLECT"):null;
  BlockPos destination=canContain(mc,pos,state,fluid)?pos:adjacent;
  return canEmpty(mc,destination,fluid)||!destination.equals(adjacent)&&canEmpty(mc,adjacent,fluid)
      ?LceActionPrompts.label("EMPTY"):null;
 }
 public static boolean canCollect(BlockState state) {
  return state.getBlock() instanceof BucketPickup&&(state.getFluidState().isSource()||state.getBlock() instanceof PowderSnowBlock);
 }
 private static boolean canContain(Minecraft mc,BlockPos pos,BlockState state,Fluid fluid) {
  return state.getBlock() instanceof LiquidBlockContainer container&&container.canPlaceLiquid(mc.level,pos,state,fluid);
 }
 private static boolean canEmpty(Minecraft mc,BlockPos pos,Fluid fluid) {
  if(!(fluid instanceof FlowingFluid)||mc.level.isOutsideBuildHeight(pos))return false;
  BlockState state=mc.level.getBlockState(pos);
  return state.isAir()||state.canBeReplaced(fluid)||canContain(mc,pos,state,fluid);
 }
 private static final class NativeView extends Item {
  private NativeView(){super(new Item.Properties());}
  static BlockHitResult trace(net.minecraft.world.level.Level level,Player player,ClipContext.Fluid fluid) {
   return getPlayerPOVHitResult(level,player,fluid);
  }
 }
}

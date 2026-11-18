package dev.legacyconsole.mixin;

import dev.legacyconsole.worldgen.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeManager.class)
public abstract class LegacyBiomeManagerMixin {
 @Shadow @Final private BiomeManager.NoiseBiomeSource noiseBiomeSource;
 @Inject(method="getBiome",at=@At("HEAD"),cancellable=true)
 private void legacy$surfaceBiome(BlockPos pos,CallbackInfoReturnable<Biome> cir){
  LegacyBiomeSource source=noiseBiomeSource instanceof LegacyBiomeSource legacy?legacy:LegacyBiomeLookup.get((BiomeManager)(Object)this);
  if(source!=null)cir.setReturnValue(source.atBlock(pos.getX(),pos.getZ()));
 }
 @Inject(method="withDifferentSource",at=@At("RETURN"))
 private void legacyconsole$copyConsoleLookup(BiomeSource input,CallbackInfoReturnable<BiomeManager> result){
  LegacyBiomeSource source=noiseBiomeSource instanceof LegacyBiomeSource legacy?legacy:LegacyBiomeLookup.get((BiomeManager)(Object)this);
  if(source!=null)LegacyBiomeLookup.bind(result.getReturnValue(),source);
 }
}

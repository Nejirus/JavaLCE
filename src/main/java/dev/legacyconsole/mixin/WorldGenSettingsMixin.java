package dev.legacyconsole.mixin;

import dev.legacyconsole.worldgen.*;
import java.util.Properties;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Dedicated-server creation; saved worlds continue to use their own persisted codec. */
@Mixin(WorldGenSettings.class)
public abstract class WorldGenSettingsMixin {
 @Inject(method="create",at=@At("RETURN"),cancellable=true)
 private static void legacy$worldType(RegistryAccess registries,Properties props,CallbackInfoReturnable<WorldGenSettings> cir){
  String type=props.getProperty("level-type","");
  if(!type.equalsIgnoreCase("legacy")&&!type.equalsIgnoreCase("legacyconsole:legacy"))return;
  String name=props.getProperty("legacy-world-size","large");Ps4WorldSize size=null;
  for(Ps4WorldSize candidate:Ps4WorldSize.values())if(candidate.name().equalsIgnoreCase(name))size=candidate;
  if(size==null)throw new IllegalArgumentException("legacy-world-size must be classic, small, medium, or large");
  WorldGenSettings base=dev.legacyconsole.worldgen.LegacyWorldgen.explicitSeed(cir.getReturnValue(),props.getProperty("level-seed",""));cir.setReturnValue(LegacyWorldgen.create(registries,base,size,base.generateFeatures(),base.generateBonusChest(),Ps4BiomeScale.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,new com.google.gson.JsonPrimitive(props.getProperty("legacy-biome-scale","small"))).getOrThrow(false,message->{throw new IllegalArgumentException(message);})));
 }
}

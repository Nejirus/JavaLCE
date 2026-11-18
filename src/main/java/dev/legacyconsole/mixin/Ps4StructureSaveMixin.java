package dev.legacyconsole.mixin;
import dev.legacyconsole.worldgen.ps4.structure.Ps4StructureStart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(StructureFeature.class)
public abstract class Ps4StructureSaveMixin {
 @Inject(method="loadStaticStart",at=@At("HEAD"),cancellable=true)
 private static void legacyconsole$loadPs4Graph(ServerLevel level,CompoundTag tag,long seed,CallbackInfoReturnable<StructureStart<?>> result){if(tag.getBoolean("LegacyPS4"))result.setReturnValue(Ps4StructureStart.load(level,tag,seed));}
}

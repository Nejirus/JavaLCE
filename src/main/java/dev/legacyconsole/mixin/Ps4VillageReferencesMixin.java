package dev.legacyconsole.mixin;
import dev.legacyconsole.worldgen.LegacyChunkGenerator;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import java.util.stream.Stream;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(StructureFeatureManager.class)
public abstract class Ps4VillageReferencesMixin {
 @Shadow @Final private LevelAccessor level;
 @Inject(method="startsForFeature",at=@At("HEAD"),cancellable=true)
 private void legacyconsole$consoleVillageReach(SectionPos pos,StructureFeature<?> feature,CallbackInfoReturnable<Stream<? extends StructureStart<?>>> result){
  if(feature==StructureFeature.VILLAGE&&level instanceof WorldGenRegion region&&region.getLevel().getChunkSource().getGenerator() instanceof LegacyChunkGenerator generator)
   result.setReturnValue(generator.ps4Structures().villagesNear(pos.x(),pos.z()).stream());
 }
}

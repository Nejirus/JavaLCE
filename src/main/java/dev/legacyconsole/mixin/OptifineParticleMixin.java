package dev.legacyconsole.mixin;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.client.particle.*;
import java.util.*;

/** H1's omitted enqueues and Forge's unordered particle rendering. */
@Mixin(net.minecraft.client.particle.ParticleEngine.class)
public abstract class OptifineParticleMixin {
 @Shadow @Final private static List<ParticleRenderType> RENDER_ORDER;
 @Redirect(method="render",at=@At(value="INVOKE",target="Ljava/util/Map;keySet()Ljava/util/Set;",remap=false),remap=false)
 private Set<ParticleRenderType> legacy$layerOrder(Map<ParticleRenderType,?> layers){return dev.legacyconsole.client.ParticleLayerOrder.ordered(layers.keySet(),RENDER_ORDER,ParticleRenderType.CUSTOM);}
 @Redirect(method={"m_107367_","lambda$addBlockDestroyEffects$10"},at=@At(value="INVOKE",target="Lnet/optifine/reflect/Reflector;call(Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)Ljava/lang/Object;",remap=false),require=0,remap=false)
 private Object legacy$enqueueBlockParticle(Object target,@Coerce Object method,Object[] arguments){
  return dev.legacyconsole.client.OptifineBlockParticles.updateSprite(target,method,arguments);
 }
}

package dev.legacyconsole.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
import java.util.*;import java.util.concurrent.*;
import net.minecraft.client.sounds.SoundBufferLibrary;import net.minecraft.server.packs.resources.ResourceManager;import net.minecraft.resources.ResourceLocation;import com.mojang.blaze3d.audio.SoundBuffer;
@Mixin(SoundBufferLibrary.class)
public abstract class SoundBufferLibraryMixin {
 @Shadow @Final private ResourceManager resourceManager;
 @Shadow @Final private Map<ResourceLocation,CompletableFuture<SoundBuffer>> cache;
 @Redirect(method="getStream",at=@At(value="INVOKE",target="Lnet/minecraft/Util;backgroundExecutor()Ljava/util/concurrent/Executor;"))
 private Executor legacy$introWorker(ResourceLocation file,boolean loop){return dev.legacyconsole.client.ConsoleAudio.streamExecutor(file);}
 @Inject(method="getCompleteBuffer",at=@At("HEAD"),cancellable=true)
 private void legacy$pcm(ResourceLocation id,CallbackInfoReturnable<CompletableFuture<SoundBuffer>> ci){if(dev.legacyconsole.client.UiSoundCodec.handles(id))ci.setReturnValue(cache.computeIfAbsent(id,key->CompletableFuture.supplyAsync(()->dev.legacyconsole.client.UiSoundCodec.load(resourceManager,key),net.minecraft.Util.backgroundExecutor())));}
}

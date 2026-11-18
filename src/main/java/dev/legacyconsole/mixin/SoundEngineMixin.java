package dev.legacyconsole.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.sounds.*;import net.minecraft.client.resources.sounds.*;import net.minecraft.sounds.SoundSource;import com.mojang.blaze3d.audio.Channel;
import java.util.*;import com.google.common.collect.Multimap;
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin implements dev.legacyconsole.port.SoundEngineAccess {
 @Shadow private boolean loaded;
 @Unique public boolean legacy$ready(){return loaded;}
 @Invoker("loadLibrary") public abstract void legacy$initialize();
 @Shadow @Final private SoundBufferLibrary soundBuffers;
 @Shadow @Final private List<Sound> preloadQueue;
 @Unique private boolean legacy$initialReload;
 @Redirect(method="reload",at=@At(value="INVOKE",target="Lnet/minecraft/client/sounds/SoundEngine;destroy()V"))
 private void legacy$keepIntroStream(SoundEngine engine){legacy$initialReload=loaded&&dev.legacyconsole.client.ConsoleAudio.preservesStartupLibrary();if(!legacy$initialReload)engine.destroy();}
 @Inject(method="reload",at=@At("RETURN"))private void legacy$preloadInitialSounds(CallbackInfo ci){if(legacy$initialReload){legacy$initialReload=false;soundBuffers.preload(preloadQueue).thenRun(preloadQueue::clear);}}
 @Shadow @Final private Map<SoundInstance,ChannelAccess.ChannelHandle> instanceToChannel;
 @Shadow @Final private Multimap<SoundSource,SoundInstance> instanceBySource;
 @Shadow @Final private List<TickableSoundInstance> tickingSounds;
 @Shadow @Final private Map<SoundInstance,Integer> queuedSounds;
 @Shadow @Final private Map<SoundInstance,Integer> soundDeleteTime;
 @Shadow @Final private List<TickableSoundInstance> queuedTickableSounds;
 @Unique public void legacy$keepMusic(SoundInstance music){for(var entry:new ArrayList<>(instanceToChannel.entrySet()))if(entry.getKey()!=music){entry.getValue().execute(Channel::stop);instanceToChannel.remove(entry.getKey());}instanceBySource.values().removeIf(s->s!=music);tickingSounds.removeIf(s->s!=music);queuedSounds.keySet().removeIf(s->s!=music);soundDeleteTime.keySet().removeIf(s->s!=music);queuedTickableSounds.removeIf(s->s!=music);}
 @Unique public boolean legacy$isPlaying(SoundInstance music){ChannelAccess.ChannelHandle channel=instanceToChannel.get(music);return channel!=null&&!channel.isStopped();}
 @Unique public Runnable legacy$musicPump(SoundInstance music){ChannelAccess.ChannelHandle channel=instanceToChannel.get(music);return channel==null?null:()->channel.execute(source->{if(!source.stopped())source.updateStream();});}
 @Inject(method="pause",at=@At("RETURN"))private void legacy$musicPause(CallbackInfo ci){SoundInstance music=dev.legacyconsole.client.ConsoleAudio.music();ChannelAccess.ChannelHandle channel=instanceToChannel.get(music);if(channel!=null)channel.execute(Channel::unpause);}
}

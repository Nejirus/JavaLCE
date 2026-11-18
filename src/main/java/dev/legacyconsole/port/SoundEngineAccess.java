package dev.legacyconsole.port;
import net.minecraft.client.resources.sounds.SoundInstance;
public interface SoundEngineAccess {void legacy$keepMusic(SoundInstance music);boolean legacy$isPlaying(SoundInstance music);boolean legacy$ready();Runnable legacy$musicPump(SoundInstance music);void legacy$initialize();}

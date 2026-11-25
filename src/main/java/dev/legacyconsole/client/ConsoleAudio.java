package dev.legacyconsole.client;
import java.util.*;
import net.minecraft.client.Minecraft;import net.minecraft.client.resources.sounds.*;import net.minecraft.client.sounds.*;
import net.minecraft.resources.ResourceLocation;import net.minecraft.sounds.*;import net.minecraft.world.inventory.*;import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.sound.PlaySoundEvent;import net.minecraftforge.eventbus.api.SubscribeEvent;
import dev.legacyconsole.port.*;
public final class ConsoleAudio {
 private static volatile ConsoleAudio current;private volatile SoundInstance music;private SoundInstance record;private int started,recordStarted;private volatile Runnable streamPump;
 private static boolean earlyAttempted,earlyLibrary;
 private boolean ownsVanillaMusic;private int musicDimension;
 private final java.util.concurrent.ScheduledExecutorService streamer;
 public static synchronized ConsoleAudio shared(){if(current==null)new ConsoleAudio();return current;}
 private final LceMusicPlaylist playlist=new LceMusicPlaylist(new Random());private final LceAudioClock clock=new LceAudioClock();
 public ConsoleAudio(){current=this;
  // 1.17 queues streaming buffers from game ticks, which stop during world
  // loading and pause. Submit only this music channel's buffer work to the
  // native sound executor at the normal 20 Hz, independently of game ticks.
  streamer=java.util.concurrent.Executors.newSingleThreadScheduledExecutor(task->{Thread thread=new Thread(task,"Legacy music streaming");thread.setDaemon(true);return thread;});
  streamer.scheduleAtFixedRate(()->{if(current!=this)return;Runnable pump=streamPump;if(pump!=null)try{pump.run();}catch(java.util.concurrent.RejectedExecutionException closing){streamPump=null;}},0,50,java.util.concurrent.TimeUnit.MILLISECONDS);
 }
 public static void inventoryAction(ClickType type,int button){if(type!=ClickType.QUICK_CRAFT||AbstractContainerMenu.getQuickcraftHeader(button)==2)play("confirm");}
 private static SimpleSoundInstance ui(String name){return new SimpleSoundInstance(new ResourceLocation("legacyconsole","ui."+name),SoundSource.MASTER,LceUiSounds.gain(name),LceUiSounds.pitch(name,java.util.concurrent.ThreadLocalRandom.current()),false,0,SoundInstance.Attenuation.NONE,0,0,0,true);}
 public static void play(String name){if(ClientConfig.menuSounds)Minecraft.getInstance().getSoundManager().play(ui(name));}
 public static SoundInstance music(){return current==null?null:current.music;}
 public static void vanillaMusicTick(MusicManager manager){ConsoleAudio audio=shared();if(ClientConfig.menuMusic){if(!audio.ownsVanillaMusic){manager.stopPlaying();audio.ownsVanillaMusic=true;}return;}audio.ownsVanillaMusic=false;manager.tick();}
 public static boolean startupAssetsReady(){return Minecraft.getInstance().getSoundManager().getSoundEvent(new ResourceLocation("legacyconsole","music.menu2"))!=null;}
 public static boolean preservesStartupLibrary(){return earlyLibrary&&dev.legacyconsole.client.loading.ConsoleStartup.active();}
 public static java.util.concurrent.Executor streamExecutor(ResourceLocation file){ConsoleAudio audio=current;SoundInstance track=audio==null?null:audio.music;
  // Initial resource preparation saturates Minecraft's shared worker pool.
  // Decode only the intro's native music stream on its existing audio worker.
  if(dev.legacyconsole.client.loading.ConsoleStartup.active()&&track!=null&&track.getSound()!=null&&file.equals(track.getSound().getPath()))return audio.streamer;
  return net.minecraft.Util.backgroundExecutor();
 }
 public static void startupTick(){if(!dev.legacyconsole.client.loading.ConsoleStartup.active())return;Minecraft game=Minecraft.getInstance();
  if(!earlyAttempted&&game.getSoundManager()!=null&&game.getResourceManager().hasResource(new ResourceLocation("minecraft","sounds/music/menu/menu2.ogg"))){earlyAttempted=true;SoundEngineAccess engine=(SoundEngineAccess)((SoundManagerAccess)game.getSoundManager()).legacy$engine();if(!engine.legacy$ready())engine.legacy$initialize();earlyLibrary=engine.legacy$ready();}
  shared().tick();
 }
 public static void worldTransition(SoundManager manager){SoundInstance music=music();if(!ClientConfig.menuMusic||music==null){manager.stop();return;}((SoundEngineAccess)((SoundManagerAccess)manager).legacy$engine()).legacy$keepMusic(music);}
 private boolean playing(){var engine=((SoundManagerAccess)Minecraft.getInstance().getSoundManager()).legacy$engine();return ((SoundEngineAccess)engine).legacy$isPlaying(music);}
 public void tick(){Minecraft mc=Minecraft.getInstance();SoundManager sounds=mc.getSoundManager();if(!((SoundEngineAccess)((SoundManagerAccess)sounds).legacy$engine()).legacy$ready()||!startupAssetsReady()&&!earlyLibrary)return;int frames=clock.updates(System.nanoTime());if(!ClientConfig.menuMusic||ClientConfig.musicVolume<=0||mc.options.getSoundSourceVolume(SoundSource.MUSIC)<=0){streamPump=null;if(music!=null)sounds.stop(music);music=null;return;}
  if(record!=null){if(sounds.isActive(record)){recordStarted=20;return;}if(++recordStarted<20)return;record=null;playlist.completed();}
  int dimension=mc.level==null?0:mc.level.dimension()==Level.NETHER?-1:mc.level.dimension()==Level.END?1:0;
  // Menu and Overworld share the existing pool. A dimension-specific track
  // must be replaced before the active-channel early return below.
  if(music!=null&&musicDimension!=dimension){streamPump=null;sounds.stop(music);music=null;started=0;}
  if(music!=null&&playing()){if(streamPump==null)streamPump=((SoundEngineAccess)((SoundManagerAccess)sounds).legacy$engine()).legacy$musicPump(music);started=20;return;}if(music!=null&&++started<20)return;if(music!=null){streamPump=null;music=null;playlist.completed();}if(!playlist.ready(frames))return;
  String track=mc.level==null&&ClientConfig.musicMode!=0?(ClientConfig.musicMode==1?"menu2":"menu3"):playlist.next(dimension);
  musicDimension=dimension;music=new MusicRecord(new ResourceLocation("legacyconsole","music."+track));sounds.play(music);streamPump=((SoundEngineAccess)((SoundManagerAccess)sounds).legacy$engine()).legacy$musicPump(music);started=0;
 }
 @SubscribeEvent public void sound(PlaySoundEvent event){SoundInstance sound=event.getSound();if(sound==null)return;ResourceLocation id=sound.getLocation();if(ClientConfig.menuSounds&&id.getNamespace().equals("minecraft")&&id.getPath().equals("ui.button.click")){event.setResultSound(ui("confirm"));return;}if(!ClientConfig.menuMusic)return;
  if(sound.getSource()==SoundSource.RECORDS&&id.getPath().startsWith("music_disc.")){streamPump=null;if(music!=null)Minecraft.getInstance().getSoundManager().stop(music);music=null;record=sound;recordStarted=0;}
  if(id.getNamespace().equals("minecraft")&&id.getPath().startsWith("music."))event.setResultSound(null);
 }
 private static final class MusicRecord extends SimpleSoundInstance implements TickableSoundInstance {
  MusicRecord(ResourceLocation id){super(id,SoundSource.MUSIC,ClientConfig.musicVolume,1,false,0,SoundInstance.Attenuation.NONE,0,0,0,true);}
  @Override public WeighedSoundEvents resolve(SoundManager manager){if(manager.getSoundEvent(getLocation())!=null)return super.resolve(manager);
   // The installed music files exist before sounds.json has finished applying.
   // Resolve that same original file for the rating-screen stream.
   String track=getLocation().getPath().substring("music.".length());sound=new Sound("minecraft:"+LceMusicPlaylist.asset(track),1,1,1,Sound.Type.FILE,true,false,16);WeighedSoundEvents event=new WeighedSoundEvents(getLocation(),null);event.addSound(sound);return event;
  }
  public void tick(){}public boolean isStopped(){return false;}@Override public float getVolume(){return ClientConfig.musicVolume;}
 }
}

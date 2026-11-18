package dev.legacyconsole.client;
import java.io.*;import java.nio.*;import javax.sound.sampled.*;
import com.mojang.blaze3d.audio.SoundBuffer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
/** Decode the original LCE PCM files directly; no lossy conversion or spatial UI source. */
public final class UiSoundCodec {
 public static boolean handles(ResourceLocation id){return id.getNamespace().equals("legacyconsole")&&id.getPath().startsWith("sounds/ui/");}
 public static SoundBuffer load(ResourceManager resources,ResourceLocation id){try(var resource=resources.getResource(id);AudioInputStream input=AudioSystem.getAudioInputStream(new BufferedInputStream(resource.getInputStream()))){AudioFormat format=input.getFormat();if(format.getSampleSizeInBits()!=16||format.getChannels()!=1&&format.getChannels()!=2)throw new IOException("Unsupported source PCM format");byte[] data=input.readAllBytes();int channels=format.getChannels();ByteOrder order=format.isBigEndian()?ByteOrder.BIG_ENDIAN:ByteOrder.LITTLE_ENDIAN;ByteBuffer source=ByteBuffer.wrap(data).order(order);int frames=data.length/(channels*2);ByteBuffer output=org.lwjgl.BufferUtils.createByteBuffer(frames*2).order(ByteOrder.nativeOrder());for(int frame=0;frame<frames;frame++){int sample=source.getShort();if(channels==2)sample=(sample+source.getShort())/2;if(id.getPath().equals("sounds/ui/focus.ogg")){int remaining=frames-1-frame;if(remaining<32)sample=sample*Math.max(0,remaining)/31;}output.putShort((short)sample);}output.flip();return new SoundBuffer(output,new AudioFormat(format.getSampleRate(),16,1,true,ByteOrder.nativeOrder()==ByteOrder.BIG_ENDIAN));}catch(IOException|UnsupportedAudioFileException e){throw new IllegalStateException("Cannot decode original console sample "+id,e);}}
}

package dev.legacyconsole.network;

import dev.legacyconsole.worldgen.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import java.util.function.Supplier;

/** Optional client presentation of the finite console dimension; no custom biome IDs. */
public final class LegacyWorldProfile {
 public final long seed;public final int width;public final Ps4BiomeScale scale;
 public LegacyWorldProfile(LegacyChunkGenerator generator){seed=generator==null?0:generator.seed();width=generator==null?0:generator.size().blocks;scale=generator==null?Ps4BiomeScale.SMALL:generator.scale();}
 public LegacyWorldProfile(FriendlyByteBuf buffer){seed=buffer.readLong();width=buffer.readVarInt();int ordinal=buffer.readUnsignedByte();scale=Ps4BiomeScale.values()[Math.min(ordinal,2)];}
 public void encode(FriendlyByteBuf buffer){buffer.writeLong(seed);buffer.writeVarInt(width);buffer.writeByte(scale.ordinal());}
 public static void handle(LegacyWorldProfile packet,Supplier<NetworkEvent.Context> context){var c=context.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->dev.legacyconsole.client.LegacyWorldView.accept(packet)));c.setPacketHandled(true);}
}

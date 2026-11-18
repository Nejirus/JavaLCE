package dev.legacyconsole.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.legacyconsole.worldgen.ps4.Ps4BiomeData;
import dev.legacyconsole.worldgen.ps4.layers.Ps4Layers;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;

public final class LegacyBiomeSource extends BiomeSource {
 public static final Codec<LegacyBiomeSource> CODEC=RecordCodecBuilder.<LegacyBiomeSource>create(instance->instance.group(
  RegistryLookupCodec.<Biome>create(Registry.BIOME_REGISTRY).forGetter(source->source.registry),
  Codec.LONG.fieldOf("seed").forGetter(source->source.seed),
  Ps4WorldSize.CODEC.optionalFieldOf("size",Ps4WorldSize.LARGE).forGetter(source->source.size),
  Ps4BiomeScale.CODEC.optionalFieldOf("biome_scale",Ps4BiomeScale.SMALL).forGetter(source->source.scale)
 ).apply(instance,LegacyBiomeSource::new)).stable();
 private final Registry<Biome> registry;
 private final long seed;
 private final Ps4WorldSize size;
 private final Ps4BiomeScale scale;
 private final Ps4Layers layers;
 private final Biome[] mapped=new Biome[256];
 public LegacyBiomeSource(Registry<Biome> registry,long seed,Ps4WorldSize size){this(registry,seed,size,Ps4BiomeScale.SMALL);}
 public LegacyBiomeSource(Registry<Biome> registry,long seed,Ps4WorldSize size,Ps4BiomeScale scale){
  super(Ps4BiomeData.values().stream().filter(data->data.id!=8&&data.id!=9&&data.id!=127).map(data->resolve(registry,data)).distinct().collect(Collectors.toList()));
  this.registry=registry;this.seed=seed;this.size=size;this.scale=scale;layers=new Ps4Layers(seed,size.blocks,scale.ordinal());
  for(Ps4BiomeData data:Ps4BiomeData.values())mapped[data.id]=resolve(registry,data);
 }
 private static Biome resolve(Registry<Biome> registry,Ps4BiomeData data){
  Biome biome=registry.get(new ResourceLocation("minecraft",data.key));
  if(biome==null)throw new IllegalStateException("Missing biome for PS4 ID "+data.id+": minecraft:"+data.key);
  return biome;
 }
 public Registry<Biome> registry(){return registry;}
 public long seed(){return seed;}
 public Ps4WorldSize size(){return size;}
 public Ps4BiomeScale scale(){return scale;}
 public Ps4Layers layers(){return layers;}
 public Biome byPs4Id(int id){return mapped[id];}
 public Biome atBlock(int x,int z){return mapped[layers.blockBiome(x,z)];}
 @Override protected Codec<? extends BiomeSource> codec(){return CODEC;}
 @Override public LegacyBiomeSource withSeed(long seed){return new LegacyBiomeSource(registry,seed,size,scale);}
 @Override public Biome getNoiseBiome(int x,int y,int z){return mapped[layers.quartBiome(x,z)];}
}

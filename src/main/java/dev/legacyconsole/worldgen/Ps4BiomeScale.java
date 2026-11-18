package dev.legacyconsole.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

/** PS4 LevelData biome scale, stored as 0/1/2 by the original layer factory. */
public enum Ps4BiomeScale {
 SMALL("Small"),MEDIUM("Medium"),LARGE("Large");
 public final String label;
 Ps4BiomeScale(String label){this.label=label;}
 public static final Codec<Ps4BiomeScale> CODEC=Codec.STRING.comapFlatMap(name->{
  for(var value:values())if(value.name().equalsIgnoreCase(name))return DataResult.success(value);
  return DataResult.error("Unknown PS4 biome scale: "+name);
 },value->value.name().toLowerCase(Locale.ROOT));
}

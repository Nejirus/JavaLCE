package dev.legacyconsole.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

/** Sizes from LCE LevelData/LevelType, measured in Overworld blocks. */
public enum Ps4WorldSize {
 CLASSIC("Classic",864),SMALL("Small",1024),MEDIUM("Medium",3072),LARGE("Large",5120);
 public final String label;public final int blocks;
 Ps4WorldSize(String label,int blocks){this.label=label;this.blocks=blocks;}
 public static final Codec<Ps4WorldSize> CODEC=Codec.STRING.comapFlatMap(name->{
  for(Ps4WorldSize size:values())if(size.name().equalsIgnoreCase(name))return DataResult.success(size);
  return DataResult.error("Unknown PS4 world size: "+name);
 },size->size.name().toLowerCase(Locale.ROOT));
 public Ps4WorldSize next(){return values()[(ordinal()+1)%values().length];}
 /** PS4 Dimension::isWithinBounds: origin-centered, minimum inclusive, maximum exclusive. */
 public int minBlock(){return -blocks/2;}
 public int maxBlockExclusive(){return blocks/2;}
 public boolean containsBlock(int x,int z){return x>=minBlock()&&x<maxBlockExclusive()&&z>=minBlock()&&z<maxBlockExclusive();}
 public boolean containsChunk(int x,int z){int half=blocks/32;return x>=-half&&x<half&&z>=-half&&z<half;}
}

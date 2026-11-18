package dev.legacyconsole.crafting;

import dev.legacyconsole.worldgen.LegacyChunkGenerator;
import dev.legacyconsole.worldgen.Ps4WorldSize;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/** PS4 MapItem geometry; the native 128x128 image and packet format are retained. */
public final class Ps4Maps {
 private Ps4Maps(){}
 public static Ps4WorldSize size(Level level){
  return level instanceof ServerLevel server&&level.dimension()==Level.OVERWORLD
   &&server.getChunkSource().getGenerator() instanceof LegacyChunkGenerator generator?generator.size():null;
 }
 /** C round (ties away from zero) at scales 3/4; floor-based tiles at scales 0/1/2. */
 public static int center(double coordinate,int scale){
  if(scale<0||scale>4)throw new IllegalArgumentException("Map scale outside 0..4: "+scale);
  int width=128<<scale;double tile=coordinate/width;
  return scale<3?(int)Math.floor(tile)*width+width/2:(int)(Math.copySign(Math.floor(Math.abs(tile)+.5),tile)*width);
 }
 public static MapItemSavedData fresh(double x,double z,byte scale,boolean tracking,boolean unlimited,ResourceKey<Level> dimension){
  return new MapItemSavedData(center(x,scale),center(z,scale),scale,tracking,unlimited,false,dimension);
 }
 public static MapItemSavedData enlarged(MapItemSavedData source,MapItemSavedData result){
  return fresh(source.x,source.z,result.scale,result.trackingPosition,source.unlimitedTracking,result.dimension);
 }
}

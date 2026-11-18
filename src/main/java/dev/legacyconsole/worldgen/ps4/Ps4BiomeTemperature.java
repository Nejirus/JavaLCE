package dev.legacyconsole.worldgen.ps4;

import java.util.Random;

/** Registered biome temperature virtuals (0x85390/0x4d7380), with their
 * original fixed-seed noise and separate float/double rounding steps. */
public final class Ps4BiomeTemperature {
 private static final Ps4SurfaceNoise TEMPERATURE=new Ps4SurfaceNoise(new Random(1234),1);
 private static final Ps4SurfaceNoise FROZEN=new Ps4SurfaceNoise(new Random(3456),3);
 private Ps4BiomeTemperature(){}
 public static float at(int biome,int x,int y,int z){
  if(biome==48||biome==49)return frozen(x,y,z);
  Ps4BiomeData data=Ps4BiomeData.getBiome(biome);if(data==null)throw new IllegalArgumentException("Unknown original biome "+biome);
  return altitude(data.temperature,x,y,z);
 }
 static float frozen(int x,int y,int z){
  float value=0,a=(float)FROZEN.getValue(x*.05,z*.05),b=(float)Ps4BiomeSurfaces.wetlandNoise(x*.2,z*.2);
  if(a+b<.3f&&(float)Ps4BiomeSurfaces.wetlandNoise(x*.09,z*.09)<.8f)value=.2f;
  return altitude(value,x,y,z);
 }
 private static float altitude(float value,int x,int y,int z){
  if(y>64){float n=(float)(TEMPERATURE.getValue((double)((float)x*.125f),(double)((float)z*.125f))*4);value+=((n+y-64)*-.05f)/30;}
  return value;
 }
}

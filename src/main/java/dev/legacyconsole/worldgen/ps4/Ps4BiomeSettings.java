package dev.legacyconsole.worldgen.ps4;

/** Final decoration count stores from all 72 original Biome constructors
 * invoked by mcps4 Biome::initBiomes, 0x7f900. Each result is the starting
 * configuration, before biome-specific decoration mutates any counts. */
public final class Ps4BiomeSettings {
 private Ps4BiomeSettings(){}
 public static Ps4BiomeDecorator.Settings forBiome(int id){
  return switch(id){
   case 0,10,24,44,45,46,47,48,49->settings(0,0,.1F,2,1,0,0,0,0,1,3,1,0,20,12);
   case 1,129->settings(0,0,.05F,4,10,0,0,0,0,1,3,1,0,0,0);
   case 2,17,130->settings(0,-999,.1F,2,1,2,0,50,10,1,3,1,0,0,0);
   case 3,8,9,12,13,127,131,140,162->Ps4BiomeDecorator.Settings.defaults();
   case 4,18,27,28,155,156->settings(0,10,.1F,2,2,0,0,0,0,1,3,1,0,0,0);
   case 5,19,30,31,133,158->settings(0,10,.1F,2,1,0,1,0,0,1,3,1,0,0,0);
   case 6,134->settings(4,2,.1F,1,5,1,8,10,0,0,0,1,0,0,10);
   case 7,11->settings(0,0,.1F,2,1,0,0,0,0,1,3,1,0,0,12);
   case 42,43->new Ps4BiomeDecorator.Settings(0,0,.1F,2,1,0,0,0,0,1,3,1,0,0,4,12,true);
   case 14,15->settings(0,-100,.1F,-100,-100,0,1,0,0,1,3,1,1,0,0);
   case 16,25,26->settings(0,-999,.1F,2,1,0,0,0,0,1,3,1,0,0,0);
   case 20,34->settings(0,3,.1F,2,1,0,0,0,0,1,3,1,0,0,0);
   case 21,22,149->settings(0,50,.1F,4,25,0,0,0,0,1,3,1,0,0,0);
   case 23,151->settings(0,2,.1F,4,25,0,0,0,0,1,3,1,0,0,0);
   case 29,157->settings(0,-999,.1F,2,2,0,0,0,0,1,3,1,0,0,0);
   case 32,33,160,161->settings(0,10,.1F,2,7,1,3,0,0,1,3,1,0,0,0);
   case 35,36->settings(0,1,.1F,4,20,0,0,0,0,1,3,1,0,0,0);
   case 37,39,165,167->settings(0,-999,.1F,0,1,20,0,3,5,1,3,1,0,0,0);
   case 38,166->settings(0,5,.1F,0,1,20,0,3,5,1,3,1,0,0,0);
   case 40,41->settings(0,5,.1F,4,150,0,0,0,0,1,3,1,0,0,0);
   case 132->settings(0,6,.1F,100,1,0,0,0,0,1,3,1,0,0,0);
   case 163,164->settings(0,2,.1F,2,5,0,0,0,0,1,3,1,0,0,0);
   default->throw new IllegalArgumentException("Unregistered PS4 biome: "+id);
  };
 }
 private static Ps4BiomeDecorator.Settings settings(int waterlilies,int trees,float chance,int flowers,int grass,int deadBushes,int mushrooms,int reeds,int cacti,int gravel,int sand,int clay,int hugeMushrooms,int kelp,int seagrass){return new Ps4BiomeDecorator.Settings(waterlilies,trees,chance,flowers,grass,deadBushes,mushrooms,reeds,cacti,gravel,sand,clay,hugeMushrooms,kelp,seagrass,true);}
}

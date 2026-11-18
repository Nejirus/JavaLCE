package dev.legacyconsole.port;
public final class WorldPreset {
 public static final WorldPreset DEFAULT=new WorldPreset(0,"default"),FLAT=new WorldPreset(1,"flat"),LARGE_BIOMES=new WorldPreset(2,"large_biomes"),AMPLIFIED=new WorldPreset(3,"amplified"),LEGACY=new WorldPreset(4,"legacy");
 public static final WorldPreset[] WORLD_TYPES={DEFAULT,FLAT,LARGE_BIOMES,AMPLIFIED,LEGACY};
 private final int id;private final String key;private WorldPreset(int id,String key){this.id=id;this.key=key;}
 public int getWorldTypeID(){return id;}public String getTranslateName(){return "generator."+key;}
}

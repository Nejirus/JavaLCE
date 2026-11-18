package dev.legacyconsole.worldgen;

import java.util.*;
import net.minecraft.world.level.biome.BiomeManager;

/** Keep PS4's two-dimensional Voronoi lookup through native weather/tint APIs. */
public final class LegacyBiomeLookup {
 private static final Map<BiomeManager,LegacyBiomeSource> SOURCES=Collections.synchronizedMap(new WeakHashMap<>());
 private LegacyBiomeLookup(){}
 public static void bind(BiomeManager manager,LegacyBiomeSource source){SOURCES.put(manager,source);}
 public static void unbind(BiomeManager manager){SOURCES.remove(manager);}
 public static LegacyBiomeSource get(BiomeManager manager){return SOURCES.get(manager);}
}

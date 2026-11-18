package dev.legacyconsole.client;

import dev.legacyconsole.worldgen.*;
import dev.legacyconsole.network.LegacyWorldProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;

/** Original LCE ocean view and 2D biome selection for a finite Overworld. */
public final class LegacyWorldView {
 private static net.minecraft.client.multiplayer.ClientLevel level;
 private static LegacyBiomeSource source;
 private LegacyWorldView(){}
 public static boolean active(){return level!=null&&Minecraft.getInstance().level==level&&level.dimension()==Level.OVERWORLD&&source!=null;}
 public static void clear(){if(level!=null)LegacyBiomeLookup.unbind(level.getBiomeManager());level=null;source=null;}
 public static void accept(LegacyWorldProfile profile){
  clear();var current=Minecraft.getInstance().level;if(current==null||current.dimension()!=Level.OVERWORLD)return;
  for(var size:Ps4WorldSize.values())if(size.blocks==profile.width){level=current;source=new LegacyBiomeSource(current.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY),profile.seed,size,profile.scale);LegacyBiomeLookup.bind(current.getBiomeManager(),source);return;}
 }
}

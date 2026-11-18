package dev.legacyconsole.worldgen.ps4.structure;
import java.util.Random;
import net.minecraft.core.*;
import net.minecraft.resources.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
/** Target-version feature adapter for the original twelve non-template village elements. */
final class Ps4VillageDecoration {
 static void place(String id,WorldGenLevel level,ChunkGenerator generator,Random random,BlockPos pos){
  String key=id.replace("minecraft:","");String nativeKey=switch(key){case "oak_tree"->"oak";case "savanna_tree"->"acacia";case "spruce_tree","pine_tree"->"spruce";case "sweet_berry_bush"->"patch_berry_bush";case "hay_pile"->"pile_hay";case "melon_pile"->"pile_melon";case "pumpkin_pile"->"pile_pumpkin";case "snow_pile"->"pile_snow";case "ice_pile"->"pile_ice";default->null;};
  if(nativeKey!=null){var feature=level.registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY).get(new ResourceLocation(nativeKey));if(feature!=null)feature.place(level,generator,random,pos);return;}
  var block=switch(key){case "fern"->Blocks.FERN;case "cactus"->Blocks.CACTUS;case "melon_pile"->Blocks.MELON;case "pumpkin_pile"->Blocks.PUMPKIN;case "hay_pile"->Blocks.HAY_BLOCK;case "snow_pile"->Blocks.SNOW_BLOCK;case "ice_pile"->Blocks.PACKED_ICE;default->null;};
  if(block!=null&&level.isEmptyBlock(pos))level.setBlock(pos,block.defaultBlockState(),2);
 }
}

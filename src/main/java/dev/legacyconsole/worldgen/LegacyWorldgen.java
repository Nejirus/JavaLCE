package dev.legacyconsole.worldgen;

import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class LegacyWorldgen {
 private LegacyWorldgen(){}
 public static void register(){
  dev.legacyconsole.worldgen.ps4.structure.Ps4PieceTypes.initialize();
  Registry.register(Registry.BIOME_SOURCE,new ResourceLocation("legacyconsole","ps4"),LegacyBiomeSource.CODEC);
  Registry.register(Registry.CHUNK_GENERATOR,new ResourceLocation("legacyconsole","legacy"),LegacyChunkGenerator.CODEC);
 }
 public static WorldGenSettings explicitSeed(WorldGenSettings base,String text){
  if(text.isEmpty())return base;
  try{return base.withSeed(false,java.util.OptionalLong.of(Long.parseLong(text)));}catch(NumberFormatException ignored){return base;}
 }
 public static WorldGenSettings create(RegistryAccess registries,WorldGenSettings base,Ps4WorldSize size,boolean structures,boolean bonus){
  return create(registries,base,size,structures,bonus,Ps4BiomeScale.SMALL);
 }
 public static WorldGenSettings create(RegistryAccess registries,WorldGenSettings base,Ps4WorldSize size,boolean structures,boolean bonus,Ps4BiomeScale scale){
  LegacyChunkGenerator generator=new LegacyChunkGenerator(registries.registryOrThrow(Registry.BIOME_REGISTRY),base.seed(),size,structures,scale);
  return new WorldGenSettings(base.seed(),structures,bonus,WorldGenSettings.withOverworld(registries.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY),base.dimensions(),generator));
 }
 /** PS4 finite bounds depend on world size, never on the seed or spawn position. */
 public static void applyBorder(net.minecraft.world.level.border.WorldBorder border,Ps4WorldSize size){
  border.setCenter(0,0);border.setSize(size.blocks);border.setWarningBlocks(0);border.setWarningTime(0);border.setDamagePerBlock(0);border.setDamageSafeZone(0);
 }
 public static final class Bounds {
  private final java.util.Set<net.minecraft.server.MinecraftServer> initialized=java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
  @SubscribeEvent public void tick(net.minecraftforge.event.TickEvent.ServerTickEvent event){
   if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;
   var server=net.minecraftforge.fmllegacy.server.ServerLifecycleHooks.getCurrentServer();
   if(server==null||server.overworld()==null||!(server.overworld().getChunkSource().getGenerator() instanceof LegacyChunkGenerator)||!initialized.add(server))return;
   // Vanilla delegates Overworld border changes to other dimensions during startup.
   // Keep ordinary Legacy Nether/End behavior. The imported tutorial alone has
   // its saved 288-block Nether (XZSize=54, HellScale=3).
   for(ServerLevel level:server.getAllLevels())if(level.dimension()!=Level.OVERWORLD){level.getWorldBorder().setCenter(0,0);level.getWorldBorder().setSize(level.dimension()==Level.NETHER&&dev.legacyconsole.tutorial.Ps4TutorialWorld.get(server.overworld()).enabled()?288:59999968);}
  }
  @SubscribeEvent public void unload(WorldEvent.Unload event){if(event.getWorld() instanceof ServerLevel level)LegacyBiomeLookup.unbind(level.getBiomeManager());}
  @SubscribeEvent public void login(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event){profile(event.getPlayer());}
  @SubscribeEvent public void dimension(net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event){profile(event.getPlayer());}
  @SubscribeEvent public void respawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event){profile(event.getPlayer());}
  @SubscribeEvent public void spawn(WorldEvent.CreateSpawnPosition event){
   if(!(event.getWorld() instanceof ServerLevel level)||level.dimension()!=Level.OVERWORLD||!(level.getChunkSource().getGenerator() instanceof LegacyChunkGenerator generator))return;
   var pos=dev.legacyconsole.worldgen.ps4.Ps4Spawn.find(level,generator);
   level.setDefaultSpawnPos(pos,0);event.setCanceled(true);
   // Retain the port's native 1.17.1 bonus chest at the console-selected spawn.
   if(level.getServer().getWorldData().worldGenSettings().generateBonusChest())net.minecraft.world.level.levelgen.feature.Feature.BONUS_CHEST.configured(net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration.NONE).place(level,generator,level.random,pos);
  }
  private static void profile(net.minecraft.world.entity.player.Player player){
   if(player instanceof net.minecraft.server.level.ServerPlayer server&&dev.legacyconsole.LegacyConsole.NETWORK.present(server)){
    var generator=server.getLevel().getChunkSource().getGenerator();
    dev.legacyconsole.LegacyConsole.NETWORK.sendTo(server,new dev.legacyconsole.network.LegacyWorldProfile(generator instanceof LegacyChunkGenerator legacy?legacy:null));
   }
  }
  @SubscribeEvent public void load(WorldEvent.Load event){
   if(event.getWorld() instanceof ServerLevel level&&level.dimension()==Level.OVERWORLD&&level.getChunkSource().getGenerator() instanceof LegacyChunkGenerator generator){
    applyBorder(level.getWorldBorder(),generator.size());
    LegacyBiomeLookup.bind(level.getBiomeManager(),generator.source());
   }
  }
 }
}

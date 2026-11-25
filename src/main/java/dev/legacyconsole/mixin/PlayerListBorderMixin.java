package dev.legacyconsole.mixin;

import dev.legacyconsole.worldgen.LegacyChunkGenerator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Java assumes shared dimension borders; Legacy limits only the Overworld. */
@Mixin(PlayerList.class)
public abstract class PlayerListBorderMixin {
 @Redirect(method="sendLevelInfo",at=@At(value="INVOKE",
     target="Lnet/minecraft/server/level/ServerLevel;getWorldBorder()Lnet/minecraft/world/level/border/WorldBorder;"))
 private WorldBorder legacy$destinationBorder(ServerLevel source,ServerPlayer player,ServerLevel destination){
  return source.getChunkSource().getGenerator() instanceof LegacyChunkGenerator
      ?destination.getWorldBorder():source.getWorldBorder();
 }
}

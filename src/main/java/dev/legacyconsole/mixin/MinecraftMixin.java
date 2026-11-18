package dev.legacyconsole.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
import net.minecraft.client.Minecraft;import net.minecraft.client.sounds.SoundManager;import net.minecraft.client.sounds.MusicManager;import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.gui.screens.Overlay;
import dev.legacyconsole.client.loading.ConsoleStartup;
import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.level.Level;
import dev.legacyconsole.client.input.*;
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
 @Inject(method="setOverlay",at=@At("HEAD"),cancellable=true)
 private void legacy$startupOverlay(Overlay next,CallbackInfo ci){if(next!=null)ConsoleStartup.prepare();else if(ConsoleStartup.active()){ConsoleStartup.loadingComplete();ci.cancel();}}
 @Inject(method="runTick",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/platform/Window;updateDisplay()V"))
 private void legacy$startupFrame(boolean tick,CallbackInfo ci){if(ConsoleStartup.active())com.mojang.blaze3d.systems.RenderSystem.recordRenderCall(ConsoleStartup::render);}
 @Redirect(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/client/sounds/MusicManager;tick()V"))
 private void legacy$musicOwner(MusicManager manager){dev.legacyconsole.client.ConsoleAudio.vanillaMusicTick(manager);}
 @Redirect(method="updateScreenAndTick",at=@At(value="INVOKE",target="Lnet/minecraft/client/sounds/SoundManager;stop()V"))private void legacy$music(SoundManager sounds){dev.legacyconsole.client.ConsoleAudio.worldTransition(sounds);}
 @Inject(method="clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V",at=@At("HEAD"))
 private void legacy$leaveStart(net.minecraft.client.gui.screens.Screen next,CallbackInfo ci){dev.legacyconsole.client.loading.ConsoleLoading.leaving(((Minecraft)(Object)this).level!=null);}
 @Inject(method="clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V",at=@At("RETURN"))
 private void legacy$leaveDone(net.minecraft.client.gui.screens.Screen next,CallbackInfo ci){dev.legacyconsole.client.loading.ConsoleLoading.leaving(false);}
 @Inject(method="handleKeybinds",at=@At("HEAD"))private void legacy$shield(CallbackInfo ci){ConsoleShield.tick();}
 @Inject(method="startUseItem",at=@At("HEAD"),cancellable=true)private void legacy$bufferUse(CallbackInfo ci){if(ControllerInput.INSTANCE.blocksShieldUse())ci.cancel();}
 @Inject(method="startAttack",at=@At("HEAD"),cancellable=true)private void legacy$bufferAttack(CallbackInfo ci){if(ControllerInput.INSTANCE.blocksMenuMovement())ci.cancel();}
 @Redirect(method="startUseItem",at=@At(value="INVOKE",target="Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))private InteractionResult legacy$shieldUse(MultiPlayerGameMode game,Player p,Level level,InteractionHand hand){return ConsoleShield.manualUse(game,p,level,hand);}
 @Redirect(method="handleKeybinds",at=@At(value="INVOKE",target="Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"))private void legacy$shieldRelease(MultiPlayerGameMode game,Player player){ConsoleShield.stopUsing(game,player);}
}

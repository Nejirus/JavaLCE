package dev.legacyconsole.mixin;

import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ProgressScreen.class)
public interface ProgressScreenAccess {
  @Accessor("header") Component legacy$header();
  @Accessor("stage") Component legacy$stage();
  @Accessor("progress") int legacy$progress();
}

package dev.legacyconsole.mixin;

import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConnectScreen.class)
public interface ConnectScreenAccess {
  @Accessor("connection") Connection legacy$connection();
  @Accessor("status") Component legacy$status();
}

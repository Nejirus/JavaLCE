package dev.legacyconsole.mixin;

import net.minecraft.client.gui.screens.BackupConfirmScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BackupConfirmScreen.class)
public interface BackupConfirmScreenAccess {
 @Accessor("listener") BackupConfirmScreen.Listener legacy$listener();
}

package dev.legacyconsole.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.sounds.*;
@Mixin(SoundManager.class)
public abstract class SoundManagerMixin implements dev.legacyconsole.port.SoundManagerAccess {
 @Accessor("soundEngine") public abstract SoundEngine legacy$engine();
}

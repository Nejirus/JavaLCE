package dev.legacyconsole.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Synchronize a virtual cursor warp before a delayed/coalesced native callback arrives. */
@Mixin(MouseHandler.class)
public interface MouseHandlerAccess {
  @Accessor("xpos") void legacy$xpos(double x);
  @Accessor("ypos") void legacy$ypos(double y);
}

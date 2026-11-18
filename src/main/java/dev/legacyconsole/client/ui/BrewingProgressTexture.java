package dev.legacyconsole.client.ui;
import net.minecraft.client.renderer.texture.SimpleTexture;import net.minecraft.resources.ResourceLocation;
/** NativeImage now decodes source grayscale directly, replacing the old BufferedImage upload. */
public final class BrewingProgressTexture extends SimpleTexture {public BrewingProgressTexture(ResourceLocation id){super(id);}}

package dev.legacyconsole.port;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
public final class PortGraphics {
 public static PoseStack pose=new PoseStack();
 public static void bind(ResourceLocation texture){ RenderSystem.setShader(GameRenderer::getPositionTexShader);RenderSystem.setShaderTexture(0,texture); }
}

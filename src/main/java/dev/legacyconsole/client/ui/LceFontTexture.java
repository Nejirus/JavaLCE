package dev.legacyconsole.client.ui;
import java.io.*;import java.awt.image.BufferedImage;
import net.minecraft.client.renderer.texture.SimpleTexture;import net.minecraft.resources.ResourceLocation;import net.minecraft.server.packs.resources.ResourceManager;
import com.mojang.blaze3d.platform.*;
/** CFontData reads alpha only; source atlas pixels remain unchanged in the pack. */
public final class LceFontTexture extends SimpleTexture {
 public LceFontTexture(ResourceLocation id){super(id);}
 @Override public void load(ResourceManager resources)throws IOException{releaseId();try(var resource=resources.getResource(location);NativeImage image=NativeImage.read(resource.getInputStream())){for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)image.setPixelRGBA(x,y,(image.getPixelRGBA(x,y)&0xff000000)|0x00ffffff);TextureUtil.prepareImage(getId(),image.getWidth(),image.getHeight());image.upload(0,0,0,false);}}
 static BufferedImage alphaMask(BufferedImage source){BufferedImage mask=new BufferedImage(source.getWidth(),source.getHeight(),BufferedImage.TYPE_INT_ARGB);for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++)mask.setRGB(x,y,(source.getRGB(x,y)&0xff000000)|0xffffff);return mask;}
}

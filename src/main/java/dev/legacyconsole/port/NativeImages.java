package dev.legacyconsole.port;
import com.mojang.blaze3d.platform.NativeImage;import java.awt.image.BufferedImage;
public final class NativeImages {public static NativeImage fromBufferedImage(BufferedImage image){NativeImage result=new NativeImage(image.getWidth(),image.getHeight(),false);for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){int c=image.getRGB(x,y);result.setPixelRGBA(x,y,(c&0xff00ff00)|(c>>>16&255)|(c&255)<<16);}return result;}}

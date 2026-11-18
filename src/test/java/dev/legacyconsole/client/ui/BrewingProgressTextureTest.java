package dev.legacyconsole.client.ui;
import static org.junit.Assert.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import com.mojang.blaze3d.platform.NativeImage;
import dev.legacyconsole.port.NativeImages;
import org.junit.Test;
public class BrewingProgressTextureTest {
 @Test public void nativePngDecoderRetainsSourcePanelGray() throws Exception {
  try(InputStream in=getClass().getResourceAsStream("/assets/legacyconsole/textures/gui/sprites/container/brewing_progress.png");NativeImage image=NativeImage.read(in)){
   assertEquals(9,image.getWidth());assertEquals(28,image.getHeight());
   assertEquals(0xffc6c6c6,image.getPixelRGBA(0,0));assertEquals(0xff8b8b8b,image.getPixelRGBA(6,2));assertEquals(0xffffffff,image.getPixelRGBA(3,1));
  }
 }
 @Test public void rgbUploadsPreserveColorAndAlpha(){
  BufferedImage input=new BufferedImage(2,1,BufferedImage.TYPE_INT_ARGB);input.setRGB(0,0,0x64123456);input.setRGB(1,0,0x00abcdef);
  try(NativeImage image=NativeImages.fromBufferedImage(input)){assertEquals(0x64563412,image.getPixelRGBA(0,0));assertEquals(0x00efcdab,image.getPixelRGBA(1,0));}
 }
}

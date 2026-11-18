package dev.legacyconsole.client;
import net.minecraft.util.Mth;
public final class ConsoleMapRenderer {
 public static final float IMAGE_DEPTH=-.02f,TEXT_DEPTH=-.06f;
 // PS4 0xc6fdf1 and 0xc70243: decorations start in front of the image,
 // then advance by the original float addition for each rendered icon.
 public static final float ICON_DEPTH=-.04f,ICON_LAYER_STEP=-.01f;
 public static float nextIconLayer(float layer){return layer+ICON_LAYER_STEP;}
 public static String coordinates(double x,double y,double z){return "X: "+Mth.floor(x)+", Y: "+Mth.floor(y)+", Z: "+Mth.floor(z);}
 /** PS4 LUT 0xc6f870 stores zero for all four unexplored entries (not Java's checker). */
 public static int unexploredPixel(int x,int y){return 0;}
}

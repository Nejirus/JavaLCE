package dev.legacyconsole.port;
import net.minecraft.client.Minecraft;
public final class ScaledResolution {
 private final Minecraft mc;
 public ScaledResolution(Minecraft mc){this.mc=mc;}
 public int getScaledWidth(){return mc.getWindow().getGuiScaledWidth();}public int getScaledHeight(){return mc.getWindow().getGuiScaledHeight();}
 public double getScaledWidth_double(){return getScaledWidth();}public double getScaledHeight_double(){return getScaledHeight();}
 public int getScaleFactor(){return (int)mc.getWindow().getGuiScale();}
}

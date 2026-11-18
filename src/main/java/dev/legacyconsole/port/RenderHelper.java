package dev.legacyconsole.port;
public final class RenderHelper {
 public static void enableStandardItemLighting(){com.mojang.blaze3d.platform.Lighting.setupFor3DItems();}
 public static void disableStandardItemLighting(){com.mojang.blaze3d.platform.Lighting.setupForFlatItems();}
 public static void enableGUIStandardItemLighting(){com.mojang.blaze3d.platform.Lighting.setupFor3DItems();}
}

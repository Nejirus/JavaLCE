package dev.legacyconsole.port;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
public final class Display {
 public static boolean isActive(){return GLFW.glfwGetWindowAttrib(Minecraft.getInstance().getWindow().getWindow(),GLFW.GLFW_FOCUSED)==GLFW.GLFW_TRUE;}
 public static boolean isFullscreen(){return Minecraft.getInstance().getWindow().isFullscreen();}
 public static boolean isCreated(){return Minecraft.getInstance().getWindow()!=null;}
 public static int getWidth(){return Minecraft.getInstance().getWindow().getWidth();}public static int getHeight(){return Minecraft.getInstance().getWindow().getHeight();}
 public static boolean isCloseRequested(){return GLFW.glfwWindowShouldClose(Minecraft.getInstance().getWindow().getWindow());}
}

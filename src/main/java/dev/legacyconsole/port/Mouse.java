package dev.legacyconsole.port;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import dev.legacyconsole.client.input.CursorCoordinates;
import dev.legacyconsole.mixin.MouseHandlerAccess;
public final class Mouse {
 private static Cursor cursor;private static int wheel; private static int dx,dy,lastX,lastY;
 private static CursorCoordinates lastCoordinates;
 public static boolean isCreated(){return Minecraft.getInstance().getWindow()!=null;}
 private static CursorCoordinates coordinates(){var w=Minecraft.getInstance().getWindow();return new CursorCoordinates(w.getWidth(),w.getHeight(),w.getScreenWidth(),w.getScreenHeight(),w.getGuiScaledWidth(),w.getGuiScaledHeight());}
 public static int getX(){var mouse=Minecraft.getInstance().mouseHandler;return (int)coordinates().toLegacy(mouse.xpos(),mouse.ypos()).x();}
 public static int getY(){var mouse=Minecraft.getInstance().mouseHandler;return (int)coordinates().toLegacy(mouse.xpos(),mouse.ypos()).y();}
 public static double guiX(){var mouse=Minecraft.getInstance().mouseHandler;return coordinates().toGui(mouse.xpos(),mouse.ypos()).x();}
 public static double guiY(){var mouse=Minecraft.getInstance().mouseHandler;return coordinates().toGui(mouse.xpos(),mouse.ypos()).y();}
 public static void frame(){int x=getX(),y=getY();var space=coordinates();boolean resized=!space.equals(lastCoordinates);dx=resized?0:x-lastX;dy=resized?0:y-lastY;lastX=x;lastY=y;lastCoordinates=space;}
 public static int getEventDX(){return dx;}public static int getEventDY(){return dy;}
 public static int getEventX(){return getX();}public static int getEventY(){return getY();}
 public static void setCursorPosition(int x,int y){applyPosition(coordinates().legacyToWindow(x,y));}
 public static void setGuiPosition(double x,double y){applyPosition(coordinates().toWindow(x,y));}
 private static void applyPosition(CursorCoordinates.Point point){Minecraft mc=Minecraft.getInstance();var access=(MouseHandlerAccess)mc.mouseHandler;access.legacy$xpos(point.x());access.legacy$ypos(point.y());GLFW.glfwSetCursorPos(mc.getWindow().getWindow(),point.x(),point.y());}
 public static boolean isButtonDown(int key){return GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(),key)==GLFW.GLFW_PRESS;}
 public static int getButtonCount(){return 8;}public static int getEventButton(){return -1;}
 public static int getEventDWheel(){int old=wheel;wheel=0;return old;}public static void scroll(double amount){wheel=(int)Math.signum(amount)*120;}
 public static Cursor getNativeCursor(){return cursor;}
 public static void setNativeCursor(Cursor next){cursor=next;Minecraft mc=Minecraft.getInstance();if(!mc.mouseHandler.isMouseGrabbed())GLFW.glfwSetInputMode(mc.getWindow().getWindow(),GLFW.GLFW_CURSOR,next==null?GLFW.GLFW_CURSOR_NORMAL:GLFW.GLFW_CURSOR_HIDDEN);}
}

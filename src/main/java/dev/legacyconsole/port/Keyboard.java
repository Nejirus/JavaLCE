package dev.legacyconsole.port;
import java.util.*;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;
public final class Keyboard {
 public static final int KEYBOARD_SIZE=256,KEY_ESCAPE=1,KEY_RETURN=28,KEY_NUMPADENTER=156,KEY_SPACE=57,KEY_UP=200,KEY_DOWN=208,KEY_LEFT=203,KEY_RIGHT=205,KEY_PRIOR=201,KEY_NEXT=209,KEY_TAB=15,KEY_DELETE=211,KEY_BACK=14,KEY_LBRACKET=26,KEY_RBRACKET=27,KEY_LSHIFT=42,KEY_RSHIFT=54,KEY_LCONTROL=29,KEY_RCONTROL=157,KEY_LALT=56,KEY_RALT=184,KEY_HOME=199,KEY_END=207,KEY_Q=16,KEY_W=17,KEY_E=18,KEY_R=19,KEY_T=20,KEY_Y=21,KEY_U=22,KEY_I=23,KEY_O=24,KEY_P=25,KEY_A=30,KEY_S=31,KEY_D=32,KEY_F=33,KEY_G=34,KEY_H=35,KEY_J=36,KEY_K=37,KEY_L=38,KEY_Z=44,KEY_X=45,KEY_C=46,KEY_V=47,KEY_B=48,KEY_N=49,KEY_M=50,KEY_F1=59,KEY_F2=60,KEY_F3=61,KEY_F4=62,KEY_F5=63,KEY_F6=64,KEY_F7=65,KEY_F8=66,KEY_F9=67,KEY_F10=68,KEY_F11=87,KEY_F12=88,KEY_1=2,KEY_2=3,KEY_3=4,KEY_4=5,KEY_5=6,KEY_6=7,KEY_7=8,KEY_8=9,KEY_9=10,KEY_0=11;
 private static final Map<Integer,Integer> MAP=new HashMap<>();static{MAP.put(1,256);MAP.put(28,257);MAP.put(156,335);MAP.put(57,32);MAP.put(200,265);MAP.put(208,264);MAP.put(203,263);MAP.put(205,262);MAP.put(201,266);MAP.put(209,267);MAP.put(15,258);MAP.put(211,261);MAP.put(14,259);MAP.put(26,91);MAP.put(27,93);MAP.put(42,340);MAP.put(54,344);MAP.put(29,341);MAP.put(157,345);MAP.put(56,342);MAP.put(184,346);MAP.put(199,268);MAP.put(207,269);MAP.put(16,81);MAP.put(17,87);MAP.put(18,69);MAP.put(19,82);MAP.put(20,84);MAP.put(21,89);MAP.put(22,85);MAP.put(23,73);MAP.put(24,79);MAP.put(25,80);MAP.put(30,65);MAP.put(31,83);MAP.put(32,68);MAP.put(33,70);MAP.put(34,71);MAP.put(35,72);MAP.put(36,74);MAP.put(37,75);MAP.put(38,76);MAP.put(44,90);MAP.put(45,88);MAP.put(46,67);MAP.put(47,86);MAP.put(48,66);MAP.put(49,78);MAP.put(50,77);MAP.put(59,290);MAP.put(60,291);MAP.put(61,292);MAP.put(62,293);MAP.put(63,294);MAP.put(64,295);MAP.put(65,296);MAP.put(66,297);MAP.put(67,298);MAP.put(68,299);MAP.put(87,300);MAP.put(88,301);MAP.put(2,49);MAP.put(3,50);MAP.put(4,51);MAP.put(5,52);MAP.put(6,53);MAP.put(7,54);MAP.put(8,55);MAP.put(9,56);MAP.put(10,57);MAP.put(11,48);}
 private static int event; private static boolean state;
 public static int toGlfw(int key){return MAP.getOrDefault(key,key);}
 public static int fromGlfw(int key){return MAP.entrySet().stream().filter(e->e.getValue()==key).map(Map.Entry::getKey).findFirst().orElse(key);}
 public static void event(int key,boolean down){event=fromGlfw(key);state=down;}
 public static boolean isCreated(){return Minecraft.getInstance().getWindow()!=null;}
 public static boolean isKeyDown(int key){return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(),toGlfw(key))==GLFW.GLFW_PRESS;}
 public static int getEventKey(){return event;}public static boolean getEventKeyState(){return state;}public static boolean isRepeatEvent(){return false;}
 public static void enableRepeatEvents(boolean value){Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(value);}
 public static String getKeyName(int code){String name=GLFW.glfwGetKeyName(toGlfw(code),0);return name==null?Integer.toString(code):name;}
}

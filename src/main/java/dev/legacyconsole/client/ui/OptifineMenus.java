package dev.legacyconsole.client.ui;
import net.minecraft.client.Minecraft;import net.minecraft.client.gui.screens.*;import java.lang.reflect.*;
public final class OptifineMenus {
 public static boolean available(){try{Class.forName("net.optifine.shaders.Shaders",false,OptifineMenus.class.getClassLoader());return true;}catch(ClassNotFoundException e){return false;}}
 public static Screen video(Screen parent){return wrap(new VideoSettingsScreen(parent,Minecraft.getInstance().options),parent);}
 public static boolean handles(Screen screen,Screen parent){String name=screen.getClass().getName();return !(screen instanceof ConsoleOptifineScreen)&&(name.startsWith("net.optifine.gui.")||name.startsWith("net.optifine.shaders.gui.")||available()&&screen.getClass()==VideoSettingsScreen.class);}
 public static Screen wrap(Screen source,Screen parent){for(Screen previous=parent;previous instanceof ConsoleOptifineScreen console;previous=console.parent)if(console.wraps(source))return console;return new ConsoleOptifineScreen(parent,source);}
 static Object call(Object owner,String name,Class<?>[] types,Object...args){try{Method method=(owner instanceof Class<?> type?type:owner.getClass()).getMethod(name,types);return method.invoke(owner instanceof Class<?>?null:owner,args);}catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot call native OptiFine "+name,e);}}
 static Object field(Object owner,String...names){for(Class<?> type=owner.getClass();type!=null;type=type.getSuperclass())for(String name:names)try{Field field=type.getDeclaredField(name);field.setAccessible(true);return field.get(owner);}catch(NoSuchFieldException ignored){}catch(IllegalAccessException e){throw new IllegalStateException(e);}return null;}
}

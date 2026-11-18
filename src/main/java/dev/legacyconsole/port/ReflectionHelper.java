package dev.legacyconsole.port;
import java.lang.reflect.*;
public final class ReflectionHelper {
 public static Field findField(Class<?> type,String... names){for(Class<?> c=type;c!=null;c=c.getSuperclass())for(String n:names)try{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}catch(NoSuchFieldException ignored){}throw new IllegalStateException("Missing field in "+type.getName()+": "+java.util.Arrays.toString(names));}
 public static <T,E> T getPrivateValue(Class<? super E> type,E object,String... names){try{return (T)findField(type,names).get(object);}catch(IllegalAccessException e){throw new IllegalStateException(e);}}
 public static <T,E> void setPrivateValue(Class<? super E> type,E object,T value,String... names){try{findField(type,names).set(object,value);}catch(IllegalAccessException e){throw new IllegalStateException(e);}}
 public static Method findMethod(Class<?> type,String name,String srg,Class<?>... args){for(Class<?> c=type;c!=null;c=c.getSuperclass())for(String n:new String[]{name,srg})try{Method m=c.getDeclaredMethod(n,args);m.setAccessible(true);return m;}catch(NoSuchMethodException ignored){}throw new IllegalStateException("Missing method "+name+" in "+type.getName());}
}

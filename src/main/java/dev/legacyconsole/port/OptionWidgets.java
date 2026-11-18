package dev.legacyconsole.port;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ProgressOption;
public final class OptionWidgets {
 public static void step(AbstractWidget widget,int direction){if(widget instanceof CycleButton<?>)widget.mouseScrolled(widget.x+1,widget.y+1,-direction);else if(widget instanceof AbstractSliderButton){widget.keyPressed(direction<0?263:262,0,0);}else widget.mouseClicked(widget.x+widget.getWidth()/2.0,widget.y+widget.getHeight()/2.0,0);}
 private static ProgressOption option(AbstractSliderButton widget){for(Class<?> type=widget.getClass();type!=null;type=type.getSuperclass())for(var field:type.getDeclaredFields())if(ProgressOption.class.isAssignableFrom(field.getType()))try{field.setAccessible(true);return (ProgressOption)field.get(widget);}catch(IllegalAccessException e){throw new IllegalStateException(e);}return null;}
 public static double value(AbstractSliderButton widget){ProgressOption option=option(widget);if(option!=null)return option.toPct(option.get(Minecraft.getInstance().options));for(var field:AbstractSliderButton.class.getDeclaredFields())if(field.getType()==double.class)try{field.setAccessible(true);return field.getDouble(widget);}catch(IllegalAccessException e){throw new IllegalStateException(e);}throw new IllegalStateException("Native slider value unavailable");}
 public static double nextValue(ProgressOption option,int direction){double current=option.get(Minecraft.getInstance().options),pct=option.toPct(current);for(int i=1;i<=100;i++){double next=option.toValue(Math.max(0,Math.min(1,pct+direction*i*.01)));if(Math.abs(next-current)>1e-7)return option.toPct(next);}return pct;}
 public static double nextValue(AbstractSliderButton widget,int direction){ProgressOption option=option(widget);return option==null?Math.max(0,Math.min(1,value(widget)+direction*.05)):nextValue(option,direction);}
 public static void set(AbstractSliderButton widget,double value){widget.onClick(widget.x+4+Math.max(0,Math.min(1,value))*(widget.getWidth()-8),widget.y+widget.getHeight()/2.0);widget.onRelease(widget.x,widget.y);}
}

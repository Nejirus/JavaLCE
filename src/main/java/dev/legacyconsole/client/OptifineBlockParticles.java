package dev.legacyconsole.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.TerrainParticle;

/** Repair only H1's omitted enqueue after the original Forge sprite update. */
public final class OptifineBlockParticles {
 private static boolean checked;
 private static Object spriteUpdate;
 private static java.lang.reflect.Method call;
 private OptifineBlockParticles(){}
 public static Object updateSprite(Object target,Object method,Object[] arguments){
  if(!checked){
   checked=true;
   try{
    Class<?> config=Class.forName("net.optifine.Config");
    Class<?> reflector=Class.forName("net.optifine.reflect.Reflector");
    call=reflector.getMethod("call",Object.class,Class.forName("net.optifine.reflect.ReflectorMethod"),Object[].class);
    if("OptiFine_1.17.1_HD_U_H1".equals(config.getMethod("getVersion").invoke(null)))
     spriteUpdate=Class.forName("net.optifine.reflect.Reflector").getField("TerrainParticle_updateSprite").get(null);
   }catch(ReflectiveOperationException unsupported){throw new IllegalStateException("Cannot call the native OptiFine sprite updater",unsupported);}
  }
  try{
   Object result=call.invoke(null,target,method,arguments);
   if(method==spriteUpdate && result==target && target instanceof TerrainParticle particle)Minecraft.getInstance().particleEngine.add(particle);
   return result;
  }catch(ReflectiveOperationException failure){throw new IllegalStateException("Cannot update the native block particle",failure);}
 }
}

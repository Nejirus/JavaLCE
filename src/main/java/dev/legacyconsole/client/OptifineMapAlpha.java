package dev.legacyconsole.client;

import com.mojang.blaze3d.platform.GlStateManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/** Preserve OptiFine's emulated alpha state around the original blended map pass. */
final class OptifineMapAlpha {
  private static final Access ACCESS = find();
  private static Object previous;

  private record Access(Constructor<?> state, Method get, Method disable, Method set, Method flush) {}

  private static Access find() {
    try {
      Class<?> state = Class.forName("net.optifine.render.GlAlphaState");
      return new Access(state.getConstructor(), GlStateManager.class.getMethod("getAlphaState", state),
          GlStateManager.class.getMethod("disableAlphaTest"), GlStateManager.class.getMethod("setAlphaState", state),
          Class.forName("net.optifine.render.RenderUtils").getMethod("setFlushRenderBuffers", boolean.class));
    } catch (ClassNotFoundException | NoSuchMethodException absent) {
      return null;
    }
  }

  private static void alphaState(boolean capture) throws ReflectiveOperationException {
    // OptiFine changes alphaTestRef by flushing batched geometry. This hook runs
    // during the map RenderType's setup/clear: a recursive flush can draw the
    // other hand's item and replace the map binding with the item texture atlas.
    boolean flushing = (Boolean) ACCESS.flush.invoke(null, false);
    try {
      if (capture) {
        previous = ACCESS.state.newInstance();
        ACCESS.get.invoke(null, previous);
        ACCESS.disable.invoke(null);
      } else {
        ACCESS.set.invoke(null, previous);
      }
    } finally {
      ACCESS.flush.invoke(null, flushing);
    }
  }

  static void begin() {
    if (ACCESS == null) return;
    try {
      alphaState(true);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("Could not prepare OptiFine map blending", exception);
    }
  }

  static void end() {
    if (ACCESS == null || previous == null) return;
    try {
      alphaState(false);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("Could not restore OptiFine map blending", exception);
    } finally {
      previous = null;
    }
  }
}

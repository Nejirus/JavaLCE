package dev.legacyconsole.client.loading;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ReloadInstance;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.legacyconsole.client.ClientConfig;
import dev.legacyconsole.client.input.ControllerCursor;
import dev.legacyconsole.client.ui.ConsoleLoadingView;
import dev.legacyconsole.mixin.LevelLoadingScreenAccess;
import dev.legacyconsole.mixin.ProgressScreenAccess;
import dev.legacyconsole.mixin.ConnectScreenAccess;

/** Visual presentation over native loading; native completion, save and error paths still run. */
public final class ConsoleLoading {
  public enum Kind { NONE, CONNECTING, JOINING, ENTERING, LEAVING, RELOADING }
  public record State(Kind kind,String title,String status,float progress,boolean tips) {}
  private static final State NONE=new State(Kind.NONE,"","",0,false);
  private static final List<String> TIPS=readTips();
  private static final LceLoadingTips TIP_SEQUENCE=new LceLoadingTips(TIPS.size());
  private static final Field RELOAD=reloadField();
  private static boolean leaving;
  private static Screen leavingScreen;
  private static Kind lastKind=Kind.NONE;
  private static State frameState=NONE;
  private static long sceneStarted;

  private ConsoleLoading() {}
  public static void leaving(boolean value) { leaving=value; }
  public static boolean visible() {
    return nativeVisible()||frameState.kind()!=Kind.NONE;
  }
  private static boolean nativeVisible() {
    Minecraft mc=Minecraft.getInstance();
    return ClientConfig.consoleMenus&&!ConsoleStartup.active()&&(
        mc.getOverlay() instanceof LoadingOverlay || mc.screen instanceof ConnectScreen
        || mc.screen instanceof ProgressScreen
        || mc.screen instanceof LevelLoadingScreen || mc.screen instanceof ReceivingLevelScreen
        || mc.screen instanceof GenericDirtMessageScreen);
  }
  /** Keep the cover through the native overlay's final frame, when it clears itself mid-render. */
  public static void beginFrame() {
    frameState=nativeState();
    if(canCancelConnection()) {
      // Keep the original Java button/callback, with mouse bounds matching the LCE footer.
      var bounds=ConsoleLoadingView.cancelBounds(Minecraft.getInstance().getWindow().getGuiScaledWidth(),
          Minecraft.getInstance().getWindow().getGuiScaledHeight());
      var button=cancelButton();
      if(button!=null) {
        button.x=(int)Math.floor(bounds.x());button.y=(int)Math.floor(bounds.y());
        button.setWidth((int)Math.ceil(bounds.width()));button.setHeight((int)Math.ceil(bounds.height()));
      }
    }
  }
  public static boolean canCancelConnection() {
    Minecraft mc=Minecraft.getInstance();
    return ClientConfig.consoleMenus&&!ConsoleStartup.active()&&mc.getOverlay()==null
        &&mc.screen instanceof ConnectScreen;
  }
  private static net.minecraft.client.gui.components.Button cancelButton() {
    if(!(Minecraft.getInstance().screen instanceof ConnectScreen screen))return null;
    for(var child:screen.children())if(child instanceof net.minecraft.client.gui.components.Button button)return button;
    return null;
  }
  /** Abort DNS/login through Minecraft's own callback, preserving disconnect and return-screen handling. */
  public static boolean cancelConnection() {
    if(!canCancelConnection())return false;
    var button=cancelButton();
    if(button==null||!button.active)return false;
    button.onPress();frameState=NONE;
    return true;
  }
  public static State state() {
    State current=nativeState();
    return current.kind()==Kind.NONE?frameState:current;
  }
  private static State nativeState() {
    if(!nativeVisible()) { leavingScreen=null;return NONE; }
    Minecraft mc=Minecraft.getInstance();
    if(mc.getOverlay() instanceof LoadingOverlay overlay) {
      try {
        ReloadInstance reload=(ReloadInstance)RELOAD.get(overlay);
        return new State(Kind.RELOADING,"Loading","Please wait",LceLoadingLayout.progress(reload.getActualProgress()),true);
      } catch(IllegalAccessException e) { throw new IllegalStateException("Cannot read native reload progress",e); }
    }
    if(mc.screen instanceof ConnectScreenAccess screen) {
      // AnyToJoining uses ConnectingProgress's timer; AnyToStarting uses FullscreenProgress.
      if(screen.legacy$connection()==null)return new State(Kind.CONNECTING,"","",0,false);
      return new State(Kind.JOINING,"Connecting to host",text(screen.legacy$status()),0,true);
    }
    if(leaving)leavingScreen=mc.screen;
    // clearLevel returns before its last ProgressScreen is retired. Keep its save caption.
    if(mc.screen==leavingScreen)
      return new State(Kind.LEAVING,"Preparing to Save Level",text(mc.screen.getTitle()),0,true);
    leavingScreen=null;
    if(mc.screen instanceof LevelLoadingScreenAccess screen)
      return new State(Kind.ENTERING,"Loading level","Loading spawn area",
          LceLoadingLayout.progress(screen.legacy$progressListener().getProgress()/100f),true);
    if(mc.screen instanceof ProgressScreenAccess screen) {
      String stage=text(screen.legacy$stage());
      return new State(Kind.ENTERING,"Loading level",stage.isEmpty()?text(screen.legacy$header()):stage,
          LceLoadingLayout.progress(screen.legacy$progress()/100f),true);
    }
    return new State(Kind.ENTERING,"Loading level",mc.screen instanceof ReceivingLevelScreen
        ?"Downloading terrain":text(mc.screen.getTitle()),0,true);
  }
  public static void render() {
    State view=state();
    if(view.kind()==Kind.NONE) { lastKind=Kind.NONE;TIP_SEQUENCE.end();return; }
    long nowMillis=System.nanoTime()/1_000_000;
    if(lastKind!=view.kind()) {
      lastKind=view.kind();sceneStarted=nowMillis;
      if(view.tips())TIP_SEQUENCE.begin(nowMillis);else TIP_SEQUENCE.end();
    }
    Minecraft mc=Minecraft.getInstance();ControllerCursor.update();
    int width=mc.getWindow().getGuiScaledWidth(),height=mc.getWindow().getGuiScaledHeight();
    var projection=RenderSystem.getProjectionMatrix().copy();
    PoseStack model=RenderSystem.getModelViewStack();model.pushPose();
    try {
      RenderSystem.viewport(0,0,mc.getWindow().getWidth(),mc.getWindow().getHeight());
      RenderSystem.disableScissor();RenderSystem.colorMask(true,true,true,true);
      RenderSystem.setProjectionMatrix(com.mojang.math.Matrix4f.orthographic(width,-height,1000f,
          net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane()));
      model.setIdentity();model.translate(0,0,1000f-net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane());
      RenderSystem.applyModelViewMatrix();RenderSystem.disableDepthTest();
      RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.setShaderColor(1,1,1,1);
      int index=view.tips()?TIP_SEQUENCE.current(nowMillis):-1;
      ConsoleLoadingView.draw(width,height,view,index<0||TIPS.isEmpty()?"":TIPS.get(index),nowMillis-sceneStarted);
    } finally {
      model.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(projection);
      RenderSystem.setShaderColor(1,1,1,1);RenderSystem.enableDepthTest();
    }
  }
  private static String text(Component value) {
    if(value instanceof net.minecraft.network.chat.TranslatableComponent translated
        && !net.minecraft.client.resources.language.I18n.exists(translated.getKey()))return "";
    return value==null?"":value.getString();
  }
  private static Field reloadField() {
    for(Field f:LoadingOverlay.class.getDeclaredFields())if(ReloadInstance.class.isAssignableFrom(f.getType())) {
      f.setAccessible(true);return f;
    }
    throw new IllegalStateException("Native loading overlay has no reload instance");
  }
  private static List<String> readTips() {
    try(InputStream in=ConsoleLoading.class.getResourceAsStream("/assets/legacyconsole/startup_tips.txt")) {
      if(in==null)throw new IOException("Missing LCE tips");
      return new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8)).lines().filter(s->!s.isBlank()).toList();
    } catch(IOException e) { throw new IllegalStateException(e); }
  }
}

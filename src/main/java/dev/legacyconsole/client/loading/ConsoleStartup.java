package dev.legacyconsole.client.loading;

import java.io.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.DynamicTexture;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.legacyconsole.port.*;
import dev.legacyconsole.client.ConsoleAudio;
import dev.legacyconsole.client.input.*;

/** Intro720 is presented immediately before the native window swaps buffers. */
public final class ConsoleStartup {
 private static final Map<String,ResourceLocation> textures=new HashMap<>();
 private static final org.apache.logging.log4j.Logger LOG=org.apache.logging.log4j.LogManager.getLogger("LegacyConsole");
 private static long started;
 private static boolean installed,finished,fullscreen,loadingComplete,frameReady;
 private static final IntroPlayback playback=new IntroPlayback();
 private static IntroPlayback.View view=new IntroPlayback.View(0,0,0,false);
 private static Cursor blank,original;
 public static boolean active(){return installed&&!finished;}
 public static boolean frameReady(){return frameReady;}
 /** Called when Minecraft installs its first resource-loading overlay. */
 public static void prepare(){
  if(finished)return;installed=true;Minecraft game=Minecraft.getInstance();
  if(blank==null){original=Mouse.getNativeCursor();blank=new Cursor(16,16,0,0,1,org.lwjgl.BufferUtils.createIntBuffer(256),null);}
  Mouse.setNativeCursor(blank);
  if(!fullscreen){fullscreen=true;game.options.fullscreen=true;if(!game.getWindow().isFullscreen())game.getWindow().toggleFullScreen();}
 }
 public static boolean introFinished(){return view.complete()||view.time()>=StartupSequence.DURATION_MS;}
 private static boolean keyboardConfirm(){return Keyboard.isKeyDown(Keyboard.KEY_RETURN)||Keyboard.isKeyDown(Keyboard.KEY_SPACE);}
 public static boolean confirmBlocked(){return active()||playback.confirmBlocked(keyboardConfirm()||ControllerInput.INSTANCE.state.held(PadState.CROSS));}
 /** Consume the loader's final clear until its last frame has been covered. */
 public static void loadingComplete(){if(!loadingComplete){loadingComplete=true;LOG.info("Initial resource loading finished during console intro, elapsed {} ms",elapsed());}}
 private static long elapsed(){return started==0?0:(System.nanoTime()-started)/1000000;}
 public static void render(){
  if(finished)return;prepare();Minecraft game=Minecraft.getInstance();
  if(started==0){started=System.nanoTime();LOG.info("Console intro started before native frame presentation");}
  boolean confirm=ControllerInput.INSTANCE.startupConfirmHeld()||keyboardConfirm();
  long elapsed=elapsed();view=playback.frame(elapsed,loadingComplete,confirm);
  String stage=StartupSequence.LOGOS[view.stage()];
  int w=game.getWindow().getGuiScaledWidth(),h=game.getWindow().getGuiScaledHeight();
  com.mojang.math.Matrix4f projection=RenderSystem.getProjectionMatrix().copy();
  PoseStack matrix=RenderSystem.getModelViewStack();matrix.pushPose();
  try{
   RenderSystem.viewport(0,0,game.getWindow().getWidth(),game.getWindow().getHeight());
   RenderSystem.disableScissor();RenderSystem.colorMask(true,true,true,true);
   RenderSystem.setProjectionMatrix(com.mojang.math.Matrix4f.orthographic(w,-h,1000f,net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane()));
   matrix.setIdentity();matrix.translate(0,0,1000f-net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane());RenderSystem.applyModelViewMatrix();
   GlStateManager.disableDepth();GlStateManager.enableBlend();GlStateManager.color(1,1,1,1);
   Gui.drawRect(0,0,w,h,0xffffffff);PortGraphics.bind(texture(stage));
   // GuiComponent.fill disables blending; the original Intro720 alpha timeline needs it restored.
   GlStateManager.enableBlend();RenderSystem.defaultBlendFunc();
   GlStateManager.color(1,1,1,view.opacity());
   float scale=dev.legacyconsole.client.ui.LceViewport.scale(w,h);GlStateManager.translate(dev.legacyconsole.client.ui.LceViewport.x(w,h,0),dev.legacyconsole.client.ui.LceViewport.y(w,h,0),0);GlStateManager.scale(scale,scale,1);
   Gui.drawModalRectWithCustomSizedTexture(0,0,0,0,1280,720,1280,720);
  }finally{matrix.popPose();RenderSystem.applyModelViewMatrix();RenderSystem.setProjectionMatrix(projection);GlStateManager.color(1,1,1,1);GlStateManager.enableDepth();}
  frameReady=true;
  ConsoleAudio.startupTick();
  if(view.complete()){finish();game.setOverlay(null);}
 }
 private static ResourceLocation texture(String name){return textures.computeIfAbsent(name,key->{
  try(InputStream stream=ConsoleStartup.class.getResourceAsStream("/assets/legacyconsole/textures/startup/"+key+".png")){
   if(stream==null)throw new IOException("Missing startup image "+key);
   return Minecraft.getInstance().getTextureManager().register("legacy_startup_"+key,new DynamicTexture(NativeImage.read(stream)));
  }catch(IOException e){throw new IllegalStateException(e);}
 });}
 public static void finish(){finished=true;handoffCursor();for(var texture:textures.values())Minecraft.getInstance().getTextureManager().release(texture);textures.clear();LOG.info("Console intro handed off to main menu after {} ms",elapsed());}
 public static void handoffCursor(){if(!finished||blank==null)return;ControllerCursor.adoptStartup(blank,original);blank=original=null;}
}

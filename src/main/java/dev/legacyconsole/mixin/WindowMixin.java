package dev.legacyconsole.mixin;
import org.spongepowered.asm.mixin.*;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.*;
import com.mojang.blaze3d.platform.*;import dev.legacyconsole.client.ClientConfig;
import dev.legacyconsole.client.loading.ConsoleStartup;
import java.util.Optional;
import java.util.function.*;
import net.minecraftforge.fml.loading.progress.EarlyProgressVisualization;
import org.lwjgl.glfw.GLFW;
@Mixin(Window.class)
public abstract class WindowMixin {
 @Shadow @Final private long window;
 @Shadow @Final private ScreenManager screenManager;
 @Shadow private Optional<VideoMode> preferredFullscreenVideoMode;
 @Shadow private int width,height;
 @Shadow private int windowedWidth,windowedHeight;
 @Shadow private void setMode(){throw new AssertionError();}
 @Unique private boolean legacy$waitingForFrame=true;
 @ModifyVariable(method="<init>",at=@At("HEAD"),argsOnly=true)
 private static DisplayData legacy$fullscreen(DisplayData display){return display.isFullscreen?display:new DisplayData(display.width,display.height,display.fullscreenWidth,display.fullscreenHeight,true);}
 @Group(name="legacyStartupWindow",min=1,max=2)
 @Redirect(method="<init>",at=@At(value="INVOKE",target="Lnet/minecraftforge/fml/loading/progress/EarlyProgressVisualization;handOffWindow(Ljava/util/function/IntSupplier;Ljava/util/function/IntSupplier;Ljava/util/function/Supplier;Ljava/util/function/LongSupplier;)J",remap=false),require=0)
 private long legacy$hiddenWindow(EarlyProgressVisualization progress,IntSupplier requestedWidth,IntSupplier requestedHeight,Supplier<String> title,LongSupplier requestedMonitor){
  legacy$prepareHiddenWindow();
  try{return progress.handOffWindow(requestedWidth,requestedHeight,title,()->0L);}
  finally{GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,GLFW.GLFW_TRUE);}
 }
 // H1 reflects into Forge's handoff, with a native fallback for antialiasing.
 @Group(name="legacyStartupWindow",min=1,max=2)
 @Redirect(method="<init>",at=@At(value="INVOKE",target="Lnet/optifine/reflect/Reflector;callLong(Ljava/lang/Object;Lnet/optifine/reflect/ReflectorMethod;[Ljava/lang/Object;)J",remap=false),require=0)
 private long legacy$hiddenReflectedWindow(Object progress,@Coerce Object method,Object[] arguments){
  return legacy$hiddenWindow((EarlyProgressVisualization)progress,(IntSupplier)arguments[0],(IntSupplier)arguments[1],(Supplier<String>)arguments[2],(LongSupplier)arguments[3]);
 }
 @Group(name="legacyStartupWindow",min=1,max=2)
 @Redirect(method="<init>",at=@At(value="INVOKE",target="Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J",remap=false),require=0)
 private long legacy$hiddenOptifineWindow(int requestedWidth,int requestedHeight,CharSequence title,long requestedMonitor,long share){
  legacy$prepareHiddenWindow();
  try{return GLFW.glfwCreateWindow(width,height,title,0L,share);}
  finally{GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,GLFW.GLFW_TRUE);}
 }
 @Unique private void legacy$prepareHiddenWindow(){
  Monitor monitor=screenManager.getMonitor(GLFW.glfwGetPrimaryMonitor());
  if(monitor!=null){VideoMode mode=monitor.getPreferredVidMode(preferredFullscreenVideoMode);width=mode.getWidth();height=mode.getHeight();}
  // GLFW ignores VISIBLE=false for a fullscreen window. Create the context
  // hidden and windowed at the final dimensions, then enter native fullscreen
  // after the first intro frame has actually been swapped.
  GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE,GLFW.GLFW_FALSE);
 }
 @Redirect(method="<init>",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/platform/Window;setMode()V"))
 private void legacy$deferFullscreen(Window host){GLFW.glfwSetWindowPos(window,host.getX(),host.getY());}
 @Inject(method="updateDisplay",at=@At("RETURN"))
 private void legacy$revealPreparedFrame(CallbackInfo ci){
  if(!legacy$waitingForFrame||!ConsoleStartup.frameReady())return;
  legacy$waitingForFrame=false;
  int restoreWidth=windowedWidth,restoreHeight=windowedHeight;
  setMode();windowedWidth=restoreWidth;windowedHeight=restoreHeight;
  GLFW.glfwShowWindow(window);
  // Entering fullscreen already made the hidden window visible, so showing
  // it again does not trigger GLFW's normal focus-on-show behavior.
  GLFW.glfwFocusWindow(window);
 }
 @Inject(method="calculateScale",at=@At("HEAD"),cancellable=true)private void legacy$scale(int requested,boolean unicode,CallbackInfoReturnable<Integer> ci){Window w=(Window)(Object)this;if(ClientConfig.consoleMenus&&requested==0)ci.setReturnValue(dev.legacyconsole.client.ui.ConsoleDisplayScale.automaticFactor(w.getWidth(),w.getHeight()));}
}

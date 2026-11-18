package dev.legacyconsole.port;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
public final class GlStateManager {
 public enum SourceFactor {SRC_ALPHA,ONE,ZERO} public enum DestFactor {ONE_MINUS_SRC_ALPHA,ONE,ZERO}
 public static void pushMatrix(){RenderSystem.getModelViewStack().pushPose();}
 public static void popMatrix(){RenderSystem.getModelViewStack().popPose();RenderSystem.applyModelViewMatrix();}
 public static void translate(double x,double y,double z){RenderSystem.getModelViewStack().translate(x,y,z);RenderSystem.applyModelViewMatrix();}
 public static void scale(double x,double y,double z){RenderSystem.getModelViewStack().scale((float)x,(float)y,(float)z);RenderSystem.applyModelViewMatrix();}
 public static void rotate(float angle,float x,float y,float z){RenderSystem.getModelViewStack().mulPose(new Vector3f(x,y,z).rotationDegrees(angle));RenderSystem.applyModelViewMatrix();}
 public static void color(float r,float g,float b,float a){RenderSystem.setShaderColor(r,g,b,a);}public static void color(float r,float g,float b){color(r,g,b,1);}
 public static void enableBlend(){RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();}public static void disableBlend(){RenderSystem.disableBlend();}
 public static void enableDepth(){RenderSystem.enableDepthTest();}public static void disableDepth(){RenderSystem.disableDepthTest();}
 public static void enableCull(){RenderSystem.enableCull();}public static void disableCull(){RenderSystem.disableCull();}
 public static void enableTexture2D(){RenderSystem.enableTexture();}public static void disableTexture2D(){RenderSystem.disableTexture();}
 public static void enableAlpha(){}public static void disableAlpha(){}public static void enableColorMaterial(){}public static void enableRescaleNormal(){}public static void disableRescaleNormal(){}
 public static void enableLighting(){com.mojang.blaze3d.platform.Lighting.setupFor3DItems();}public static void disableLighting(){com.mojang.blaze3d.platform.Lighting.setupForFlatItems();}
 public static void clear(int mask){RenderSystem.clear(mask,MinecraftCompat.isMac());}
 public static void setActiveTexture(int unit){RenderSystem.activeTexture(unit);}
 public static void tryBlendFuncSeparate(int a,int b,int c,int d){RenderSystem.blendFuncSeparate(a,b,c,d);}
 public static void tryBlendFuncSeparate(SourceFactor a,DestFactor b,SourceFactor c,DestFactor d){RenderSystem.defaultBlendFunc();}
}

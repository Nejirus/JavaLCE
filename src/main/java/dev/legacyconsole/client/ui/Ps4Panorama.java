package dev.legacyconsole.client.ui;

import com.google.gson.Gson;
import dev.legacyconsole.port.GlStateManager;
import dev.legacyconsole.port.PortGraphics;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;

/** Original Panorama/Panorama_Night FUI timelines and unmodified Orbis JPEGs. */
public final class Ps4Panorama {
 private Ps4Panorama(){}
 private static final class Image {String texture,sha256;int[] size;}
 private static final class Data {Map<String,Image> images;Map<String,float[]> frames;}
 private static final Data DATA;
 private static final long START=Util.getMillis();
 static {
  try(var r=new InputStreamReader(Ps4Panorama.class.getResourceAsStream("/assets/legacyconsole/ps4_panorama.json"),StandardCharsets.UTF_8)){
   DATA=new Gson().fromJson(r,Data.class);
  }catch(Exception e){throw new ExceptionInInitializerError(e);}
 }
 public static float offset(double seconds,boolean night){
  float[] frames=DATA.frames.get(night?"night":"day");
  int frame=(int)(Math.max(0,seconds)*30%frames.length);
  return frames[frame];
 }
 public static void render(int width,int height){render(width,height,(Util.getMillis()-START)/1000.0,false);}
 static void render(int width,int height,double seconds,boolean night){
  float scale=Math.max(width/1920f,height/1080f),x=(width-1920*scale)/2;
  Image image=DATA.images.get(night?"night":"day");
  GlStateManager.disableDepth();GlStateManager.enableTexture2D();GlStateManager.disableBlend();GlStateManager.color(1,1,1,1);
  PortGraphics.bind(new ResourceLocation("legacyconsole","textures/ps4/panorama/"+image.texture));
  float shift=offset(seconds,night);
  for(int i=0;i<2;i++)ConsoleTheme.quad(x+(shift+i*6150)*scale,0,6150*scale,1080*scale,0,0,1,1,1,1);
  GlStateManager.enableBlend();GlStateManager.enableDepth();
 }
}

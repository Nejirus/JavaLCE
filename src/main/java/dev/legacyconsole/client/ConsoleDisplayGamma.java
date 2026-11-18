package dev.legacyconsole.client;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.player.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.server.*;
import net.minecraft.client.resources.language.*;
import net.minecraft.client.sounds.*;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.model.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.level.saveddata.maps.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.flat.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.trading.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;
import net.minecraft.resources.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.sounds.*;
import net.minecraft.util.*;
import net.minecraft.server.*;
import net.minecraft.server.level.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.server.IntegratedServer;
import dev.legacyconsole.port.*;
import dev.legacyconsole.port.Gui;
import dev.legacyconsole.port.GlStateManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import dev.legacyconsole.port.ReflectionHelper;

import dev.legacyconsole.LegacyConsole;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.lwjgl.opengl.*;




/** Optional LCE Look bridge: the console display curve runs once, after world and UI. */
public final class ConsoleDisplayGamma {
  private static final String MACRO="#define MC_LEGACY_CONSOLE_DISPLAY_GAMMA 1\n";
  private static Method getPack, getOption, getResource, optionValue;
  private static boolean searched, available, supported, failed;
  private static Object pack, delegatedPack;
  private static int program, texture, textureWidth, textureHeight, exponentUniform, imageUniform;

  private ConsoleDisplayGamma() {}

  private static boolean selected() {
    try {
      if(!searched) {
        searched=true;
        Class<?> shaders=Class.forName("net.optifine.shaders.Shaders",false,
            ConsoleDisplayGamma.class.getClassLoader());
        getPack=shaders.getMethod("getShaderPack");
        getOption=shaders.getMethod("getShaderOption",String.class);
        getResource=shaders.getMethod("getShaderPackResourceStream",String.class);
        optionValue=Class.forName("net.optifine.shaders.config.ShaderOption",false,
            ConsoleDisplayGamma.class.getClassLoader()).getMethod("getValue");
        available=true;
      }
      if(!available)return false;
      Object current=getPack.invoke(null);
      if(current!=pack) {
        pack=current;delegatedPack=null;supported=false;
        if(current!=null)try(InputStream stream=(InputStream)getResource.invoke(null,
            "/shaders/legacyconsole.properties")) {
          if(stream!=null) {
            Properties properties=new Properties();properties.load(stream);
            supported="1".equals(properties.getProperty("displayGamma"));
          }
        }
      }
      return supported;
    } catch(ReflectiveOperationException | IOException | LinkageError absent) {
      return false;
    }
  }

  /** Added to G5's fixed macros only for a pack that explicitly supports this bridge. */
  public static String shaderHeader(String original) {
    if(selected() && ready()) {
      delegatedPack=pack;
      return original.contains("#define MC_LEGACY_CONSOLE_DISPLAY_GAMMA ")?original:original+MACRO;
    }
    return original;
  }

  private static String source(String name) throws IOException {
    try(InputStream stream=ConsoleDisplayGamma.class.getResourceAsStream(
        "/assets/legacyconsole/shaders/"+name)) {
      if(stream==null)throw new IOException("Missing display gamma program");
      ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int count;
      while((count=stream.read(buffer))!=-1)out.write(buffer,0,count);
      return new String(out.toByteArray(),StandardCharsets.UTF_8);
    }
  }

  private static int compile(int type,String source) {
    int shader=GL20.glCreateShader(type);GL20.glShaderSource(shader,source);GL20.glCompileShader(shader);
    if(GL20.glGetShaderi(shader,GL20.GL_COMPILE_STATUS)==0) {
      String message=GL20.glGetShaderInfoLog(shader,4096);GL20.glDeleteShader(shader);
      throw new IllegalStateException(message);
    }
    return shader;
  }

  private static int vao,vbo;
  private static boolean ready(){
    if(program!=0)return true;if(failed)return false;
    try{if(!GL.getCapabilities().OpenGL32)return false;}catch(IllegalStateException noContext){return false;}
    int vertex=0,fragment=0;
    try{
      vertex=compile(GL20.GL_VERTEX_SHADER,"#version 150\nin vec2 Position; in vec2 UV0; out vec2 texcoord; void main(){texcoord=UV0;gl_Position=vec4(Position,0.0,1.0);}");
      fragment=compile(GL20.GL_FRAGMENT_SHADER,"#version 150\nuniform sampler2D image;uniform float exponent;in vec2 texcoord;out vec4 fragColor;void main(){vec4 c=texture(image,texcoord);fragColor=vec4(pow(clamp(c.rgb,0.0,1.0),vec3(exponent)),c.a);}");
      program=GL20.glCreateProgram();GL20.glAttachShader(program,vertex);GL20.glAttachShader(program,fragment);GL20.glBindAttribLocation(program,0,"Position");GL20.glBindAttribLocation(program,1,"UV0");GL20.glLinkProgram(program);if(GL20.glGetProgrami(program,GL20.GL_LINK_STATUS)==0)throw new IllegalStateException(GL20.glGetProgramInfoLog(program,4096));
      exponentUniform=GL20.glGetUniformLocation(program,"exponent");imageUniform=GL20.glGetUniformLocation(program,"image");
      int oldVao=GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING),oldBuffer=GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);vao=GL30.glGenVertexArrays();vbo=GL15.glGenBuffers();GL30.glBindVertexArray(vao);GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,vbo);GL15.glBufferData(GL15.GL_ARRAY_BUFFER,new float[]{-1,-1,0,0,3,-1,2,0,-1,3,0,2},GL15.GL_STATIC_DRAW);GL20.glEnableVertexAttribArray(0);GL20.glEnableVertexAttribArray(1);GL20.glVertexAttribPointer(0,2,GL11.GL_FLOAT,false,16,0);GL20.glVertexAttribPointer(1,2,GL11.GL_FLOAT,false,16,8);GL30.glBindVertexArray(oldVao);GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,oldBuffer);return true;
    }catch(RuntimeException error){failed=true;if(program!=0)GL20.glDeleteProgram(program);program=0;LegacyConsole.log.warn("Display gamma initialization failed",error);return false;}finally{if(vertex!=0)GL20.glDeleteShader(vertex);if(fragment!=0)GL20.glDeleteShader(fragment);}
  }

  /** OptiFine owns/saves the single Gamma option; no second setting is introduced. */
  public static int percent() {
    try {
      if(!selected())return -1;
      Object option=getOption.invoke(null,"LCE_GAMMA");
      return option==null?50:Math.max(0,Math.min(100,Integer.parseInt((String)optionValue.invoke(option))));
    } catch(ReflectiveOperationException | NumberFormatException absent) {return 50;}
  }

  private static void enabled(int capability,boolean value){if(value)GL11.glEnable(capability);else GL11.glDisable(capability);}
  public static void present(){
    Minecraft mc=Minecraft.getInstance();int percent=percent();if(percent<0||!ready()||mc.getWindow().getWidth()<=0||mc.getWindow().getHeight()<=0)return;if(mc.level!=null&&delegatedPack!=pack)return;
    int width=mc.getWindow().getWidth(),height=mc.getWindow().getHeight();int previousProgram=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM),previousVao=GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING),previousActive=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
    int[] viewport=new int[4];GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);boolean blend=GL11.glIsEnabled(GL11.GL_BLEND),depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),scissor=GL11.glIsEnabled(GL11.GL_SCISSOR_TEST),stencil=GL11.glIsEnabled(GL11.GL_STENCIL_TEST);boolean depthMask=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
    GL13.glActiveTexture(GL13.GL_TEXTURE0);int oldTexture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
    try{
      if(texture==0)texture=GL11.glGenTextures();GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);
      if(width!=textureWidth||height!=textureHeight){GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,width,height,0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,(ByteBuffer)null);textureWidth=width;textureHeight=height;}
      GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D,0,0,0,0,0,width,height);GL11.glViewport(0,0,width,height);GL11.glDisable(GL11.GL_BLEND);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDisable(GL11.GL_SCISSOR_TEST);GL11.glDisable(GL11.GL_STENCIL_TEST);GL11.glDepthMask(false);
      GL20.glUseProgram(program);GL20.glUniform1i(imageUniform,0);GL20.glUniform1f(exponentUniform,LceGamma.exponent(percent));GL30.glBindVertexArray(vao);GL11.glDrawArrays(GL11.GL_TRIANGLES,0,3);
    }finally{
      GL30.glBindVertexArray(previousVao);GL20.glUseProgram(previousProgram);GL11.glBindTexture(GL11.GL_TEXTURE_2D,oldTexture);GL13.glActiveTexture(previousActive);GL11.glViewport(viewport[0],viewport[1],viewport[2],viewport[3]);GL11.glDepthMask(depthMask);enabled(GL11.GL_BLEND,blend);enabled(GL11.GL_DEPTH_TEST,depth);enabled(GL11.GL_CULL_FACE,cull);enabled(GL11.GL_SCISSOR_TEST,scissor);enabled(GL11.GL_STENCIL_TEST,stencil);
    }
  }
}

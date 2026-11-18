package dev.legacyconsole.client;

import com.mojang.blaze3d.vertex.*;
import dev.legacyconsole.world.Ps4PlayerColors;
import java.util.OptionalDouble;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** c098b0's colored one-pixel rectangular outline, using the original GL line primitive. */
public final class Ps4NameTagBorder extends RenderType {
 private Ps4NameTagBorder(){super("legacyconsole_player_border",DefaultVertexFormat.POSITION_COLOR,VertexFormat.Mode.DEBUG_LINES,256,false,false,
  ()->{STATE.setupRenderState();OptifineLines.begin();},()->{OptifineLines.end();STATE.clearRenderState();});}
 private static final RenderType STATE=create("legacyconsole_player_border_state",DefaultVertexFormat.POSITION_COLOR,
  VertexFormat.Mode.DEBUG_LINES,256,false,false,CompositeState.builder()
   .setShaderState(new ShaderStateShard(GameRenderer::getPositionColorShader))
   .setLineState(new LineStateShard(OptionalDouble.of(1)))
   .setLayeringState(VIEW_OFFSET_Z_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY)
   .setDepthTestState(LEQUAL_DEPTH_TEST).setCullState(NO_CULL).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
 private static final RenderType BORDER=new Ps4NameTagBorder();
 /** H1's ShadersRender only recognizes the two vanilla line types by identity.
  * Apply its same balanced line-program hooks to our one-pixel line type. */
 private static final class OptifineLines {
  private static java.lang.reflect.Method begin,end;
  private static java.lang.reflect.Field loaded,world,shadow;
  private static boolean active;
  static {
   try{
    var shaders=Class.forName("net.optifine.shaders.Shaders",false,Ps4NameTagBorder.class.getClassLoader());
    begin=shaders.getMethod("beginLines");end=shaders.getMethod("endLines");
    loaded=shaders.getField("shaderPackLoaded");world=shaders.getField("isRenderingWorld");shadow=shaders.getField("isShadowPass");
   }catch(ReflectiveOperationException|LinkageError absent){begin=null;}
  }
  static void begin(){
   if(begin==null)return;
   try{if(loaded.getBoolean(null)&&world.getBoolean(null)&&!shadow.getBoolean(null)){begin.invoke(null);active=true;}}
   catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot enter OptiFine's line render pass",e);}
  }
  static void end(){
   if(!active)return;active=false;
   try{end.invoke(null);}catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot leave OptiFine's line render pass",e);}
  }
 }
 public static void draw(Player player,Component label,PoseStack pose,MultiBufferSource buffers,Font font,EntityRenderDispatcher dispatcher){
  int index=ClientPlayerColors.index(player.getUUID());
  if(index<0||!label.getString().equals(player.getDisplayName().getString())||!net.minecraftforge.client.ForgeHooksClient.isNameplateInRenderDistance(player,dispatcher.distanceToSqr(player)))return;
  var box=Ps4NameTagLayout.bounds(font.width(label),"deadmau5".equals(label.getString())?-10:0);
  pose.pushPose();pose.translate(0,player.getBbHeight()+.5F,0);pose.mulPose(dispatcher.cameraOrientation());pose.scale(-.025F,-.025F,.025F);
  var vertices=buffers.getBuffer(BORDER);int rgb=Ps4PlayerColors.nameTagColor(index);
  edge(vertices,pose,box.left(),box.top(),box.left(),box.bottom(),rgb);
  edge(vertices,pose,box.left(),box.bottom(),box.right(),box.bottom(),rgb);
  edge(vertices,pose,box.right(),box.bottom(),box.right(),box.top(),rgb);
  edge(vertices,pose,box.right(),box.top(),box.left(),box.top(),rgb);
  pose.popPose();
 }
 private static void edge(VertexConsumer out,PoseStack pose,float x1,float y1,float x2,float y2,int color){
  point(out,pose,x1,y1,color);point(out,pose,x2,y2,color);
 }
 private static void point(VertexConsumer out,PoseStack pose,float x,float y,int color){
  out.vertex(pose.last().pose(),x,y,0).color(color>>16&255,color>>8&255,color&255,255).endVertex();
 }
}

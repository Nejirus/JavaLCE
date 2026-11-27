package dev.legacyconsole.client;

import com.google.gson.Gson;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.client.event.EntityRenderersEvent;

/** Restore the inherited PS4 villager helmet alongside the witch's pointed hat. */
public final class Ps4WitchHood {
  private Ps4WitchHood() {}
  private static final class Cube {float[] position,size,uv;float inflate;boolean mirror;}
  private static final class Part {float[] transform;List<Cube> cubes;}
  private static final class Data {int[] texture_size;Part part;}

  public static void register(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(EntityType.WITCH,Renderer::new);
  }
  private static final class Renderer extends WitchRenderer {
    Renderer(EntityRendererProvider.Context context){super(context);addLayer(new Hood(this));}
  }
  private static final class Hood extends RenderLayer<Witch,WitchModel<Witch>> {
    private final ModelPart part;
    Hood(RenderLayerParent<Witch,WitchModel<Witch>> renderer){
      super(renderer);
      try(var reader=new InputStreamReader(Ps4WitchHood.class.getResourceAsStream(
          "/assets/legacyconsole/ps4_witch_model.json"),StandardCharsets.UTF_8)){
        var data=new Gson().fromJson(reader,Data.class);var cubes=CubeListBuilder.create();
        for(var cube:data.part.cubes){
          var p=cube.position;var s=cube.size;
          cubes.texOffs(Math.round(cube.uv[0]),Math.round(cube.uv[1])).mirror(cube.mirror)
              .addBox(p[0],p[1],p[2],s[0],s[1],s[2],new CubeDeformation(cube.inflate));
        }
        var mesh=new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("helmet",cubes,PartPose.ZERO);
        part=LayerDefinition.create(mesh,data.texture_size[0],data.texture_size[1]).bakeRoot().getChild("helmet");
      }catch(Exception e){throw new IllegalStateException("Cannot read original PS4 witch helmet",e);}
    }
    @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,Witch witch,
        float limbSwing,float limbAmount,float partial,float age,float headYaw,float headPitch){
      if(witch.isInvisible())return;
      pose.pushPose();getParentModel().getHead().translateAndRotate(pose);
      part.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(witch))),
          light,OverlayTexture.NO_OVERLAY);
      pose.popPose();
    }
  }
}

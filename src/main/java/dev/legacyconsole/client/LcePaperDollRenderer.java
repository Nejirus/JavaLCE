package dev.legacyconsole.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** The source keeps world body rotation for cape physics, and cancels it in the model matrix. */
public final class LcePaperDollRenderer {
 private LcePaperDollRenderer() {}
 public static void render(int x,int y,int scale,LocalPlayer player) {
  float yaw=player.getYRot(),yawOld=player.yRotO,pitch=player.getXRot(),pitchOld=player.xRotO;
  float head=player.yHeadRot,headOld=player.yHeadRotO,body=player.yBodyRot;
  var mc=Minecraft.getInstance();var dispatcher=mc.getEntityRenderDispatcher();
  Quaternion camera=dispatcher.cameraOrientation().copy();
  PoseStack model=RenderSystem.getModelViewStack();model.pushPose();
  try {
   model.translate(x,y,1050);model.scale(1,1,-1);RenderSystem.applyModelViewMatrix();
   PoseStack pose=new PoseStack();pose.translate(0,0,1000);pose.scale(scale,scale,scale);
   Quaternion rotation=Vector3f.ZP.rotationDegrees(180),tilt=Vector3f.XP.rotationDegrees(LcePaperDollPose.PITCH);
   rotation.mul(tilt);pose.mulPose(rotation);
   pose.mulPose(Vector3f.YP.rotationDegrees(LcePaperDollPose.cancelBodyYaw(body)));
   player.setYRot(body);player.yRotO=body;player.yHeadRot=body;player.yHeadRotO=body;
   player.setXRot(-LcePaperDollPose.PITCH);player.xRotO=-LcePaperDollPose.PITCH;
   Lighting.setupForEntityInInventory();tilt.conj();dispatcher.overrideCameraOrientation(tilt);
   dispatcher.setRenderShadow(false);var buffers=mc.renderBuffers().bufferSource();
   RenderSystem.runAsFancy(()->dispatcher.render(player,0,0,0,0,1,pose,buffers,0xf000f0));buffers.endBatch();
  } finally {
   player.setYRot(yaw);player.yRotO=yawOld;player.setXRot(pitch);player.xRotO=pitchOld;
   player.yHeadRot=head;player.yHeadRotO=headOld;dispatcher.setRenderShadow(true);dispatcher.overrideCameraOrientation(camera);
   model.popPose();RenderSystem.applyModelViewMatrix();Lighting.setupFor3DItems();
  }
 }
}

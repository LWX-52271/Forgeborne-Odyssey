package com.lwx.forgeborneodyssey.client.render;

import com.lwx.forgeborneodyssey.client.model.BoneArrowModel;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.entities.BoneArrow;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BoneArrowRenderer extends EntityRenderer<BoneArrow> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ForgeborneOdyssey.MOD_ID, "textures/entity/bone_arrow.png");

    private final BoneArrowModel<BoneArrow> model;

    public BoneArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new BoneArrowModel<>(context.bakeLayer(BoneArrowModel.LAYER_LOCATION));
    }

    @Override
    public void render(BoneArrow entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));

        float f = (float) entity.shakeTime - partialTick;
        if (f > 0.0F) {
            float f1 = -Mth.sin(f * 3.0F) * f;
            poseStack.mulPose(Axis.ZP.rotationDegrees(f1));
        }

        poseStack.translate(0.0D, -1.5D, 0.0D);

        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(
                buffer, this.model.renderType(this.getTextureLocation(entity)), false, false);
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(BoneArrow entity) {
        return TEXTURE;
    }
}
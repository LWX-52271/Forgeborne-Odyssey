package com.lwx.forgeborneodyssey.client.render;

import com.lwx.forgeborneodyssey.client.model.StoneSpearModel;
import com.lwx.forgeborneodyssey.entities.ThrownStoneSpear;
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

public class ThrownStoneSpearRenderer extends EntityRenderer<ThrownStoneSpear> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("forgeborneodyssey", "textures/entity/stone_spear.png");
    private final StoneSpearModel<ThrownStoneSpear> model;

    public ThrownStoneSpearRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new StoneSpearModel<>(context.bakeLayer(StoneSpearModel.LAYER_LOCATION));
    }

    @Override
    public void render(ThrownStoneSpear entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        float lerpYaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float lerpPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        poseStack.mulPose(Axis.YP.rotationDegrees(lerpYaw + 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(lerpPitch));

        poseStack.translate(0.0F, 0.0F, 17.0F / 16.0F);

        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(
                bufferSource, this.model.renderType(this.getTextureLocation(entity)), false, false);

        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownStoneSpear entity) {
        return TEXTURE;
    }
}
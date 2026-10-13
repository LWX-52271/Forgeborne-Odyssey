package com.lwx.forgeborneodyssey.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;

public class ThrownSpearRenderer<T extends AbstractArrow> extends EntityRenderer<T> {

    private final ResourceLocation texture;
    private final EntityModel<T> model;
    private final float yOffset;
    private final float zOffset;

    public ThrownSpearRenderer(EntityRendererProvider.Context context,
                               ResourceLocation texture,
                               EntityModel<T> model,
                               float yOffset,
                               float zOffset) {
        super(context);
        this.texture = texture;
        this.model = model;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        float lerpYaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float lerpPitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        poseStack.mulPose(Axis.YP.rotationDegrees(lerpYaw + 180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(lerpPitch));

        poseStack.translate(0.0F, yOffset, zOffset);

        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(
                bufferSource, this.model.renderType(this.getTextureLocation(entity)), false, false);

        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }
}
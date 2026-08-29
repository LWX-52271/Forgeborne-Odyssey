package com.lwx.forgeborneodyssey.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class GenericArrowRenderer<T extends AbstractArrow> extends EntityRenderer<T> {

    private final ResourceLocation texture;

    public GenericArrowRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
        super(context);
        this.texture = texture;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));

        float shake = (float) entity.shakeTime - partialTick;
        if (shake > 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(shake * 3.0F) * shake));
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.05625F, 0.05625F, 0.05625F);
        poseStack.translate(-4.0F, 0.0F, 0.0F);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(this.texture));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        vertex(matrix4f, matrix3f, vertexConsumer, -7, -2, -2, 0.0F, 0.15625F, -1.0F, 0.0F, 0.0F, packedLight);
        vertex(matrix4f, matrix3f, vertexConsumer, -7, -2, 2, 0.15625F, 0.15625F, -1.0F, 0.0F, 0.0F, packedLight);
        vertex(matrix4f, matrix3f, vertexConsumer, -7, 2, 2, 0.15625F, 0.3125F, -1.0F, 0.0F, 0.0F, packedLight);
        vertex(matrix4f, matrix3f, vertexConsumer, -7, 2, -2, 0.0F, 0.3125F, -1.0F, 0.0F, 0.0F, packedLight);
        vertex(matrix4f, matrix3f, vertexConsumer, 7, 2, -2, 0.0F, 0.15625F, 1.0F, 0.0F, 0.0F, packedLight);
        vertex(matrix4f, matrix3f, vertexConsumer, 7, -2, -2, 0.15625F, 0.15625F, 1.0F, 0.0F, 0.0F, packedLight);
        vertex(matrix4f, matrix3f, vertexConsumer, 7, -2, 2, 0.15625F, 0.3125F, 1.0F, 0.0F, 0.0F, packedLight);
        vertex(matrix4f, matrix3f, vertexConsumer, 7, 2, 2, 0.0F, 0.3125F, 1.0F, 0.0F, 0.0F, packedLight);

        for (int i = 0; i < 4; ++i) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            vertex(matrix4f, matrix3f, vertexConsumer, -8, -2, 0, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, packedLight);
            vertex(matrix4f, matrix3f, vertexConsumer, 8, -2, 0, 0.5F, 0.0F, 0.0F, 0.0F, 1.0F, packedLight);
            vertex(matrix4f, matrix3f, vertexConsumer, 8, 2, 0, 0.5F, 0.15625F, 0.0F, 0.0F, 1.0F, packedLight);
            vertex(matrix4f, matrix3f, vertexConsumer, -8, 2, 0, 0.0F, 0.15625F, 0.0F, 0.0F, 1.0F, packedLight);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void vertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer consumer,
                                float x, float y, float z, float u, float v,
                                float nx, float ny, float nz, int packedLight) {
        consumer.vertex(matrix4f, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, nx, ny, nz)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }
}
package com.lwx.forgeborneodyssey.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.lwx.forgeborneodyssey.client.model.BisonModel;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.entities.BisonEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BisonRenderer extends MobRenderer<BisonEntity, BisonModel<BisonEntity>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ForgeborneOdyssey.MOD_ID, "textures/entity/bison.png");
    private static final float MODEL_SCALE = 2.3F;
    private static final float BABY_SCALE = 0.5F;

    public BisonRenderer(EntityRendererProvider.Context context) {
        super(context, new BisonModel<>(context.bakeLayer(BisonModel.LAYER_LOCATION)), 1.2F);
    }

    @Override
    public void render(BisonEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        if (entity.isBaby()) {
            poseStack.scale(BABY_SCALE, BABY_SCALE, BABY_SCALE);
        }
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(BisonEntity entity) {
        return TEXTURE;
    }
}
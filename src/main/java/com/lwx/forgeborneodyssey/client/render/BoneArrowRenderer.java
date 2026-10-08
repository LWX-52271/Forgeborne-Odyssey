package com.lwx.forgeborneodyssey.client.render;

import com.lwx.forgeborneodyssey.client.model.BoneArrowModel;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.entities.BoneArrow;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BoneArrowRenderer extends ModArrowRenderer<BoneArrow> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ForgeborneOdyssey.MOD_ID, "textures/entity/bone_arrow.png");

    public BoneArrowRenderer(EntityRendererProvider.Context context) {
        super(context, TEXTURE, new BoneArrowModel<>(context.bakeLayer(BoneArrowModel.LAYER_LOCATION)));
    }
}
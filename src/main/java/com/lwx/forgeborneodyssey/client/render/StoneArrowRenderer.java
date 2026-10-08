package com.lwx.forgeborneodyssey.client.render;

import com.lwx.forgeborneodyssey.client.model.StoneArrowModel;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.entities.StoneArrow;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class StoneArrowRenderer extends ModArrowRenderer<StoneArrow> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ForgeborneOdyssey.MOD_ID, "textures/entity/stone_arrow.png");

    public StoneArrowRenderer(EntityRendererProvider.Context context) {
        super(context, TEXTURE, new StoneArrowModel<>(context.bakeLayer(StoneArrowModel.LAYER_LOCATION)));
    }
}
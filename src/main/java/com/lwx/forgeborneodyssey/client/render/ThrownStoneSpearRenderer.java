package com.lwx.forgeborneodyssey.client.render;

import com.lwx.forgeborneodyssey.client.model.StoneSpearModel;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.entities.ThrownStoneSpear;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ThrownStoneSpearRenderer extends ThrownSpearRenderer<ThrownStoneSpear> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ForgeborneOdyssey.MOD_ID, "textures/entity/stone_spear.png");

    public ThrownStoneSpearRenderer(EntityRendererProvider.Context context) {
        super(context, TEXTURE,
                new StoneSpearModel<>(context.bakeLayer(StoneSpearModel.LAYER_LOCATION)),
                0.0F, 17.0F / 16.0F);
    }
}
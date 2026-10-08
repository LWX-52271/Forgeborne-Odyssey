package com.lwx.forgeborneodyssey.client.render;

import com.lwx.forgeborneodyssey.client.model.CrudeStoneSpearModel;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.entities.ThrownCrudeStoneSpear;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ThrownCrudeStoneSpearRenderer extends ThrownSpearRenderer<ThrownCrudeStoneSpear> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ForgeborneOdyssey.MOD_ID, "textures/entity/crude_stone_spear.png");

    public ThrownCrudeStoneSpearRenderer(EntityRendererProvider.Context context) {
        super(context, TEXTURE,
                new CrudeStoneSpearModel<>(context.bakeLayer(CrudeStoneSpearModel.LAYER_LOCATION)),
                -1.5F, 0.0F);
    }
}
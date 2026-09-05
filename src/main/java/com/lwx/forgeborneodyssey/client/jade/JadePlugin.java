package com.lwx.forgeborneodyssey.client.jade;

import com.lwx.forgeborneodyssey.blocks.DryingRackBlock;
import com.lwx.forgeborneodyssey.blocks.StressBlock;
import com.lwx.forgeborneodyssey.blocks.TarKilnBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(DryingRackComponentProvider.INSTANCE, DryingRackBlock.class);
        registration.registerBlockComponent(OreGradeComponentProvider.INSTANCE, StressBlock.class);
        registration.registerBlockComponent(TarKilnComponentProvider.INSTANCE, TarKilnBlock.class);
    }
}
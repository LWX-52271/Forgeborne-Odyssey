package com.lwx.forgeborneodyssey.client.jade;

import com.lwx.forgeborneodyssey.blocks.DryingRackBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(DryingRackComponentProvider.INSTANCE, DryingRackBlock.class);
    }
}
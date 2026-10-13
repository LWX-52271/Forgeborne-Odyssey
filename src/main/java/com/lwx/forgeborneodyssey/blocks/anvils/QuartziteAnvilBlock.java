package com.lwx.forgeborneodyssey.blocks.anvils;

public class QuartziteAnvilBlock extends AbstractAnvilBlock {

    public QuartziteAnvilBlock() {
        super(createAnvilProperties(AnvilMaterial.QUARTZITE));
    }

    @Override
    protected AnvilMaterial getMaterial() {
        return AnvilMaterial.QUARTZITE;
    }
}
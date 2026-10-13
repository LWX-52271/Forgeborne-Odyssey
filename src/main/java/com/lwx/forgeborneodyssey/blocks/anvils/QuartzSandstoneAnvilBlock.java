package com.lwx.forgeborneodyssey.blocks.anvils;

public class QuartzSandstoneAnvilBlock extends AbstractAnvilBlock {

    public QuartzSandstoneAnvilBlock() {
        super(createAnvilProperties(AnvilMaterial.QUARTZ_SANDSTONE));
    }

    @Override
    protected AnvilMaterial getMaterial() {
        return AnvilMaterial.QUARTZ_SANDSTONE;
    }
}
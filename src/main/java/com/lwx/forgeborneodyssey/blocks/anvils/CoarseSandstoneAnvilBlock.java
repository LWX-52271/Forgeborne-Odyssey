package com.lwx.forgeborneodyssey.blocks.anvils;

public class CoarseSandstoneAnvilBlock extends AbstractAnvilBlock {

    public CoarseSandstoneAnvilBlock() {
        super(createAnvilProperties(AnvilMaterial.COARSE_SANDSTONE));
    }

    @Override
    protected AnvilMaterial getMaterial() {
        return AnvilMaterial.COARSE_SANDSTONE;
    }
}
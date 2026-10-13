package com.lwx.forgeborneodyssey.blocks.anvils;

public class BasaltAnvilBlock extends AbstractAnvilBlock {

    public BasaltAnvilBlock() {
        super(createAnvilProperties(AnvilMaterial.BASALT));
    }

    @Override
    protected AnvilMaterial getMaterial() {
        return AnvilMaterial.BASALT;
    }
}